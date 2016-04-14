/*
 * (C) Copyright IBM Corp. 2010, 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.ServiceDescriptor;
import fabric.TaskServiceDescriptor;
import fabric.bus.IBusServices;
import fabric.bus.feeds.impl.FeedList;
import fabric.bus.messages.IClientNotificationMessage;
import fabric.bus.messages.INotificationMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.plugins.IPluginConfig;
import fabric.bus.services.IBusServiceConfig;
import fabric.bus.services.INotificationManager;
import fabric.bus.services.IPersistentService;
import fabric.core.io.OutputTopic;

/**
 * Class handling service acknowledgment messages for the Fabric.
 */
public class NotificationManagerService extends BusService implements Runnable, IPersistentService,
INotificationManager {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010, 2012";

    /*
     * Class static fields
     */

    /*
     * Class fields
     */

    /** A local copy of the interface to Fabric management functions. */
    private IBusServices busServices = null;

    /** The table of notification records, keyed by correlation ID. */
    private final HashMap<String, ArrayList<NotificationRecord>> notificationRecords = new HashMap<String, ArrayList<NotificationRecord>>();

    /** Object used to synchronise with the mapper main thread */
    private final Object threadSync = new Object();

    /** Flag used to indicate when the main thread should terminate */
    private boolean isRunning = true;

    /*
     * Inner classes
     */

    /**
     * Class representing a connection message record, i.e. a message to send plus the details of the Fabric asset with
     * which it is associated.
     */
    private class NotificationRecord {

        /** The correlation ID for this notification record. */
        public String correlationID = null;

        /** The ID of the actor to receive the notification. */
        public String actor = null;

        /** The ID of the platform via which the actor is connected to the Fabric. */
        public String actorPlatform = null;

        /** The feed descriptor associated with this record. */
        public ServiceDescriptor serviceDescriptor = null;

        /** The message to be sent upon receipt of the specified event. */
        public IServiceMessage message = null;

        /** The event ID associated with this message. */
        public int event = IServiceMessage.EVENT_UNKNOWN;

        /** The timeout period for this notification (<code>null</code> indicates no timeout). */
        public long timeout = 0;

        /** Flag indicating if this notification should be retained, or removed when any related notification fires. */
        public boolean retained = false;

        /**
         * Constructs a new instance.
         *
         * @param correlationID
         *            the correlation ID associated with this record.
         *
         * @param serviceDescriptor
         *            the feed descriptor associated with this record.
         *
         * @param event
         *            the event ID associated with this message.
         *
         * @param actor
         *            the ID of the actor to receive the notification.
         *
         * @param actorPlatform
         *            the ID of the platform via which the actor is connected to the Fabric.
         *
         * @param message
         *            the message to be sent upon receipt of the specified event.
         *
         * @param timeout
         *            the timeout period (in milliseconds) until the notification times-out and the failure message is
         *            automatically delivered.
         *
         * @param retained
         *            flag indicating if this message should be retained until explicitly removed (<code>true</code>),
         *            or removed automatically when any other notification for this correlation ID is fired (
         *            <code>false</code> ).
         */
        public NotificationRecord(String correlationID, ServiceDescriptor serviceDescriptor, int event, String actor,
                String actorPlatform, IServiceMessage message, int timeout, boolean retained) {

            this.correlationID = correlationID;
            this.serviceDescriptor = serviceDescriptor;
            this.event = event;
            this.actor = actor;
            this.actorPlatform = actorPlatform;
            this.message = (IServiceMessage) message.replicate();

            /* If a timeout has been specified... */
            if (timeout != 0) {

                this.timeout = System.currentTimeMillis() + (timeout * 1000);

            }

            this.retained = retained;
        }
    }

    /*
     * Class methods
     */

    /**
     * Constructs a new instance.
     */
    public NotificationManagerService() {

        super(Logger.getLogger("fabric.bus.services"));
    }

    /**
     * Constructs a new instance.
     */
    public NotificationManagerService(Logger logger) {

        super(logger);
    }

    /**
     * @see fabric.bus.services.impl.BusService#initService(fabric.bus.plugins.IPluginConfig)
     */
    @Override
    public void initService(IPluginConfig config) {

        super.initService(config);

        /* Make a local copy of the accessor for Fabric management services */
        busServices = ((IBusServiceConfig) config).getFabricServices();

        /* Start the time out thread */
        Thread pluginThread = new Thread(this, "Notification-Manager");
        pluginThread.start();

    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        isRunning = true;

        /* Determine the period between timeout checks */
        long timeoutCheck = Integer.parseInt(config("fabric.notificationManager.timeoutCheck", "60")) * 1000;

        while (isRunning) {

            try {

                synchronized (threadSync) {

                    /* Make a note of the current time */
                    long currentTime = System.currentTimeMillis();

                    /* To hold the list of notifications that are fired */
                    ArrayList<NotificationRecord> firedNotifications = new ArrayList<NotificationRecord>();

                    /* For each list of notification records... */
                    for (Iterator<String> recordListIterator = notificationRecords.keySet().iterator(); recordListIterator
                            .hasNext();) {

                        /* Get the next list */
                        String nextKey = recordListIterator.next();
                        ArrayList<NotificationRecord> nextRecordList = notificationRecords.get(nextKey);

                        /* For each record in the list... */
                        for (NotificationRecord nextRecord : nextRecordList) {

                            /* If this record has timed-out... */
                            if (nextRecord.timeout != 0 && nextRecord.timeout < currentTime) {

                                logger.log(Level.FINE,
                                        "Timout; firing notification for correlation ID [{0}], service ID [{1}]",
                                        new Object[] {nextRecord.correlationID, nextRecord.serviceDescriptor});
                                logger.log(Level.FINE, "Notification message [{0}] bytes:\n", new Object[] {
                                        (nextRecord.message != null) ? nextRecord.message.toString().length() : 0,
                                                (nextRecord.message != null) ? nextRecord.message.toString() : ""});
                                deliverNotification(nextRecord.actor, nextRecord.actorPlatform, null,
                                        nextRecord.message, null);

                                /* Record this notification so that it can be removed later */
                                firedNotifications.add(nextRecord);

                            }
                        }
                    }

                    /* Remove notifications that have fired (or no longer need to be fired) */
                    for (NotificationRecord notification : firedNotifications) {
                        removeNotifications(notification.correlationID, notification.serviceDescriptor, false);
                    }

                }

                synchronized (threadSync) {
                    /* Sleep before the next timeout check */
                    threadSync.wait(timeoutCheck);
                }

            } catch (InterruptedException e) {

                /* Not too worried about this happening */

            } catch (Exception e) {

                logger.log(Level.WARNING, "Exception firing timeout notificatons: ", e);

            }
        }
    }

    /**
     * @see fabric.bus.services.IService#handleServiceMessage(fabric.bus.messages.IServiceMessage, INotificationMessage,
     *      IClientNotificationMessage[])
     */
    @Override
    public IServiceMessage handleServiceMessage(IServiceMessage request, INotificationMessage response,
            IClientNotificationMessage[] clientResponses) throws Exception {

        INotificationMessage message = (INotificationMessage) request;

        /* Extract the message details */
        String correlationID = message.getCorrelationID();
        int event = message.getEvent();
        FeedList feedList = request.getFeedList();

        /* Extract the notification-specific arguments from the message, to be added to the client message */
        String notificationArgs = message.getNotificationArgs();

        synchronized (threadSync) {

            /* If there are no feeds in the list... */
            if (feedList.size() == 0) {

                /* Fire the notifications */
                fireNotifications(correlationID, event, notificationArgs, message);

            } else {

                TaskServiceDescriptor[] feeds = feedList.getFeeds();

                /* For each feed... */
                for (int f = 0; f < feeds.length; f++) {

                    /* Fire the notification */
                    fireNotifications(correlationID, feeds[f], event, notificationArgs, message);

                }
            }
        }

        return request;

    }

    /**
     * @see fabric.bus.services.IPersistentService#stopService()
     */
    @Override
    public void stopService() {

        /* Tell the timer thread to stop... */
        isRunning = false;

        synchronized (threadSync) {
            threadSync.notifyAll();
        }

        logger.log(Level.FINE, "Service [{0}] stopped", getClass().getName());
    }

    /**
     * @see fabric.bus.services.INotificationManager#addNotification(java.lang.String, int, java.lang.String,
     *      java.lang.String, fabric.bus.messages.IServiceMessage, int, boolean)
     */
    @Override
    public void addNotification(String correlationID, int event, String actor, String actorPlatform,
            IServiceMessage message, int timeout, boolean retained) {

        addNotification(correlationID, null, event, actor, actorPlatform, message, timeout, retained);

    }

    /**
     * @see fabric.bus.services.INotificationManager#addNotification(java.lang.String, fabric.ServiceDescriptor, int,
     *      java.lang.String, java.lang.String, fabric.bus.messages.IServiceMessage, int, boolean)
     */
    @Override
    public void addNotification(String correlationID, ServiceDescriptor serviceDescriptor, int event, String actor,
            String actorPlatform, IServiceMessage message, int timeout, boolean retained) {

        NotificationRecord newRecord = new NotificationRecord(correlationID, serviceDescriptor, event, actor,
                actorPlatform, message, timeout, retained);

        synchronized (threadSync) {

            /* Add this message to the list for this correlation ID */
            String recordListKey = correlationID + '/' + serviceDescriptor;
            ArrayList<NotificationRecord> recordList = lookupSublist(recordListKey, notificationRecords);
            recordList.add(newRecord);

        }

    }

    /**
     * @see fabric.bus.services.INotificationManager#removeNotifications(java.lang.String)
     */
    @Override
    public void removeNotifications(String correlationID) {

        removeNotifications(correlationID, null, true);

    }

    /**
     * @see fabric.bus.services.INotificationManager#removeNotifications(java.lang.String, fabric.ServiceDescriptor)
     */
    @Override
    public void removeNotifications(String correlationID, ServiceDescriptor serviceDescriptor) {

        removeNotifications(correlationID, serviceDescriptor, true);

    }

    /**
     * Removes any and all time out notifications associated with a correlation ID.
     *
     * @param correlationID
     *            the correlation ID associated with the notification.
     *
     * @param serviceDescriptor
     *            the feed descriptor associated with the notification to be added.
     *
     * @param retained
     *            flag indicating if retained notifications should be removed (<code>true</code>), or not (
     *            <code>false</code>).
     */
    private void removeNotifications(String correlationID, ServiceDescriptor serviceDescriptor, boolean doRemoveRetained) {

        synchronized (threadSync) {

            /* If retained notifications are to be removed... */
            if (doRemoveRetained) {

                String recordListKey = correlationID + '/' + serviceDescriptor;
                notificationRecords.remove(recordListKey);

            }
            /* Else remove ALL non-retained notifications for this correlation ID */
            else {

                /* For each notification record... */
                for (Iterator<String> recordListIterator = notificationRecords.keySet().iterator(); recordListIterator
                        .hasNext();) {

                    /* Get the key of the next list */
                    String recordListKey = recordListIterator.next();

                    /* If this is a list for the specified correlation ID... */
                    if (recordListKey.startsWith(correlationID)) {

                        /* Get the list */
                        ArrayList<NotificationRecord> nextRecordList = notificationRecords.get(recordListKey);

                        /* For each record in the list... */
                        for (int n = 0; n < nextRecordList.size(); n++) {

                            /* If this is not a retained record... */
                            if (!nextRecordList.get(n).retained) {

                                /* Remove it */
                                nextRecordList.remove(n--);

                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @see fabric.bus.services.INotificationManager#fireNotifications(java.lang.String, int, java.lang.String,
     *      fabric.bus.messages.INotificationMessage)
     */
    @Override
    public void fireNotifications(String correlationID, int event, String notificationArgs, INotificationMessage trigger)
            throws Exception {

        fireNotifications(correlationID, null, event, notificationArgs, trigger);

    }

    /**
     * @see fabric.bus.services.INotificationManager#fireNotifications(java.lang.String, fabric.ServiceDescriptor, int,
     *      java.lang.String, fabric.bus.messages.INotificationMessage)
     */
    @Override
    public void fireNotifications(String correlationID, ServiceDescriptor serviceDescriptor, int event,
            String notificationArgs, INotificationMessage trigger) throws Exception {

        synchronized (threadSync) {

            /* Get the list of notifications for the specified correlation ID */
            String recordListKey = correlationID + '/' + serviceDescriptor;
            ArrayList<NotificationRecord> recordList = notificationRecords.get(recordListKey);

            /* If there are any... */
            if (recordList != null) {

                /* For each notification record... */
                for (NotificationRecord record : recordList) {

                    /* If the next record matches the specified event... */
                    if (record.event == event) {

                        /* Fire the notification */
                        deliverNotification(record.actor, record.actorPlatform, notificationArgs, record.message,
                                trigger);

                    }
                }

                /* If the notification event indicates that the message has been handled... */
                if (event == IServiceMessage.EVENT_MESSAGE_HANDLED) {

                    /* We can remove any pending messages that don't need to be retained */
                    removeNotifications(correlationID, serviceDescriptor, false);

                }
            }
        }
    }

    /*
     * Determines if a notification message is for an actor or a service and delivers accordingly.
     */
    public void deliverNotification(String actor, String actorPlatform, String notificationArgs,
            IServiceMessage notification, INotificationMessage trigger) throws Exception {

        /* Indicate the notification cause in the message being fired */
        IServiceMessage messageToFire = (IServiceMessage) notification.replicate();

        /* If this is a client notification message... */
        if (notification instanceof IClientNotificationMessage) {

            /* If there is a client to deliver too... */
            if (actor != null & actorPlatform != null) {

                deliverActorNotification(actor, actorPlatform, notificationArgs,
                        (IClientNotificationMessage) messageToFire, trigger);

            }
        }
        /* Else this is a service notification */
        else {

            deliverServiceNotification(notificationArgs, messageToFire, trigger);

        }
    }

    /**
     * @see fabric.bus.services.INotificationManager#deliverActorNotification(java.lang.String, java.lang.String,
     *      java.lang.String, IClientNotificationMessage, INotificationMessage)
     */
    @Override
    public void deliverActorNotification(String actor, String actorPlatform, String notificationArgs,
            IClientNotificationMessage actorNotification, INotificationMessage trigger) throws Exception {

        actorNotification.setNotificationArgs(notificationArgs);

        /* Deliver the message */
        String notificationTopic = config("fabric.commands.clients", null, homeNode(), actor, actorPlatform);

        logger.log(Level.FINER, "Delivering notification message [{0}] to topic [{1}]", new Object[] {
                actorNotification.getUID(), notificationTopic});
        logger.log(Level.FINEST, "Full notification message:\n{0}", actorNotification.toString());

        busServices.ioChannels().sendClientCommandsChannel.write(actorNotification.toWireBytes(), new OutputTopic(
                notificationTopic));

    }

    /**
     * @see fabric.bus.services.INotificationManager#deliverServiceNotification(java.lang.String,
     *      fabric.bus.messages.IServiceMessage, fabric.bus.messages.INotificationMessage)
     */
    @Override
    public void deliverServiceNotification(String notificationArgs, IServiceMessage notification,
            INotificationMessage trigger) throws Exception {

        logger.log(Level.FINER, "Delivering notification message [{0}] to node [{1}]", new Object[] {
                notification.getUID(), homeNode()});
        logger.log(Level.FINEST, "Full notification message:\n{0}", notification.toString());

        busServices.sendServiceMessage(notification, homeNode());

    }
}
