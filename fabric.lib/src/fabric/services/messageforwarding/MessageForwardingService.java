/*
 * (C) Copyright IBM Corp. 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.messageforwarding;

import java.util.LinkedList;
import java.util.logging.Level;

import fabric.bus.IBusServices;
import fabric.bus.messages.IClientNotificationMessage;
import fabric.bus.messages.INotificationMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.plugins.IPluginConfig;
import fabric.bus.services.IBusServiceConfig;
import fabric.bus.services.IPersistentService;
import fabric.bus.services.impl.BusService;

/**
 * Manages the queue of feed messages to be sent to neighbouring nodes.
 */
public class MessageForwardingService extends BusService implements IPersistentService, Runnable {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    /*
     * Class constants
     */

    /*
     * Class fields
     */

    /** A local copy of the interface to Fabric management functions. */
    private IBusServices busServices = null;

    /** To hold the sleep interval for the worker thread. */
    private int sleepInterval = 1000;

    /** The message queue */
    private final LinkedList<OutboundMessage> messageQueue = new LinkedList<OutboundMessage>();

    /** To hold the reference to the worker thread */
    private Thread workerThread = null;

    /** Flag used to indicate when the worker thread should terminate */
    private boolean isRunning = false;

    /*
     * Inner classes
     */

    /*
     * Class methods
     */

    /**
     * Constructs a new instance.
     */
    public MessageForwardingService() {

        super();

    }

    /**
     * Adds a new message to the tail of the queue.
     *
     * @param message
     *            the message to add.
     */
    public void add(OutboundMessage message) {

        synchronized (messageQueue) {

            messageQueue.add(message);

        }
    }

    /**
     * @see fabric.bus.services.impl.BusService#initService(fabric.bus.plugins.IPluginConfig)
     */
    @Override
    public void initService(IPluginConfig config) {

        super.initService(config);

        /* Make a local copy of the accessor for Fabric management services */
        busServices = ((IBusServiceConfig) config).getFabricServices();

        /* Determine the sleep interval for the worker thread */
        sleepInterval = Integer.parseInt(config().getProperty("fabric.messageForwarding.sleepInterval", "1000"));

        /* Start the worker thread */
        workerThread = new Thread(this, "Message-Forwarding-Service");
        workerThread.start();

    }

    /**
     * @see fabric.bus.services.IService#handleServiceMessage(fabric.bus.messages.IServiceMessage,INotificationMessage,
     *      IClientNotificationMessage[])
     */
    @Override
    public IServiceMessage handleServiceMessage(IServiceMessage request, INotificationMessage response,
            IClientNotificationMessage[] clientResponses) throws Exception {

        /* No functionality required here */
        return null;
    }

    /**
     * @see fabric.bus.services.IPersistentService#stopService()
     */
    @Override
    public void stopService() {

        /* Tell the worker thread to stop */
        isRunning = false;
        logger.log(Level.FINE, "Service [{0}] stopped", getClass().getName());

    }

    /**
     * Entry point for the thread responsible for sending queued messages.
     */
    @Override
    public void run() {

        /* Indicate that the worker thread is running */
        isRunning = true;

        while (isRunning) {

            /* To hold the next message to be processed */
            OutboundMessage nextMessage = null;

            synchronized (messageQueue) {

                /* If the message queue is not empty... */
                if (!messageQueue.isEmpty()) {

                    /* Get the head of the queue */
                    nextMessage = messageQueue.remove();

                }
            }

            /* If we have a message... */
            if (nextMessage != null) {

                switch (nextMessage.action()) {

                    case FORWARD:

                        try {

                            busServices.sendFeedMessage(nextMessage.node(), nextMessage.descriptor(), nextMessage
                                    .message(), nextMessage.messageQos());

                        } catch (Exception e) {

                            logger.log(Level.WARNING, "Failed to send message to service [{0}] on node [{1}]: {2}",
                                    new Object[] {nextMessage.descriptor(), nextMessage.node(), e.getMessage()});
                            logger.log(Level.FINEST, "Full exception: ", e);

                        }

                        break;

                    case DELIVER:

                        try {

                            busServices.deliverFeedMessage(nextMessage.descriptor(), nextMessage.message(), nextMessage
                                    .subscription(), nextMessage.messageQos());

                        } catch (Exception e) {

                            logger.log(Level.WARNING, "Failed to deliver message to feed [{0}] for user [{1}]: {2}",
                                    new Object[] {nextMessage.descriptor(), nextMessage.subscription().actor(),
                                    e.getMessage()});
                            logger.log(Level.FINEST, "Full exception: ", e);

                        }

                        break;

                    default:

                        logger.log(Level.WARNING, "Internal error, unsupported message action: {0}", nextMessage
                                .action().toString());
                        break;

                }

            } else {

                /* Nothing to do right now so sleep for a while */

                try {

                    Thread.sleep(sleepInterval);

                } catch (InterruptedException e) {
                    /* Ignore */
                }
            }
        }
    }
}
