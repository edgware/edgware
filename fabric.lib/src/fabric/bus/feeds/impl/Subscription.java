/*
 * (C) Copyright IBM Corp. 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.feeds.impl;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.ReasonCode;
import fabric.TaskServiceDescriptor;
import fabric.bus.BusIOChannels;
import fabric.bus.SharedChannel;
import fabric.bus.feeds.ISubscription;
import fabric.bus.feeds.ISubscriptionCallback;
import fabric.bus.feeds.SubscriptionException;
import fabric.bus.messages.FabricMessageFactory;
import fabric.bus.messages.IClientNotificationMessage;
import fabric.bus.messages.IFabricMessage;
import fabric.bus.messages.IFeedMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.messages.impl.ServiceMessage;
import fabric.bus.routing.impl.StaticRouting;
import fabric.client.FabricClient;
import fabric.client.services.IClientNotificationHandler;
import fabric.core.io.ICallback;
import fabric.core.io.InputTopic;
import fabric.core.io.Message;
import fabric.core.logging.FLog;
import fabric.registry.FabricRegistry;
import fabric.registry.FeedRoutes;
import fabric.registry.QueryScope;
import fabric.registry.RegistryObject;
import fabric.registry.TaskSubscription;
import fabric.registry.TaskSubscriptionFactory;
import fabric.registry.exception.FactoryCreationException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.impl.FeedRoutesFactoryImpl;

/**
 * Class to manage a single client subscription to a Fabric data feed.
 *
 */
public class Subscription implements ISubscription, ICallback, IClientNotificationHandler {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

    private Logger logger;

    /** To hold the channels and topics used to communicate with the local Fabric Manager. */
    private BusIOChannels ioChannels = null;

    /** The correlation ID for this subscription */
    private final String correlationID = FabricMessageFactory.generateUID();

    /** The services represented by this subscription. */
    private TaskServiceDescriptor activeServiceDescriptor = null;

    /** The unsubscribe message for this subscriber. */
    private ServiceMessage unsubscribeMessage;

    /** The name of the topic upon which feed messages will arrive. */
    private String topic = null;

    /** The channel upon which feed messages will arrive. */
    private SharedChannel channel = null;

    /** The client connection to the Fabric. */
    private FabricClient fabricClient = null;

    /** The client callback to receive incoming Fabric feed messages. */
    private ISubscriptionCallback callback = null;

    /** The route between the publisher and the subscriber. */
    private String[] subscriptionRoute = null;

    /**
     * Create a new Subscription instance for the given FabricClient
     *
     * @param fabricClient
     * @throws IllegalStateException
     *             if the client is not currently connected to the Fabric
     */
    public Subscription(FabricClient fabricClient) throws IllegalStateException {

        this.logger = Logger.getLogger("fabric.bus.feeds");

        if (fabricClient.isConnectedToFabric()) {
            this.fabricClient = fabricClient;
            this.ioChannels = fabricClient.getIOChannels();
        } else {
            logger.log(Level.SEVERE, "Not connected to the Fabric; subscriptions not possible");
            throw new IllegalStateException("Client not connected");
        }
    }

    /**
     * @see ISubscription#subscribe(String, String, String, String, ISubscriptionCallback)
     */
    @Override
    public TaskServiceDescriptor subscribe(String task, String platform, String system, String feed,
            ISubscriptionCallback callback) throws IllegalArgumentException, Exception {

        return subscribe(new TaskServiceDescriptor(task, platform, system, feed), null, callback);
    }

    /**
     * @see ISubscription#subscribe(TaskServiceDescriptor, ISubscriptionCallback)
     */
    @Override
    public TaskServiceDescriptor subscribe(TaskServiceDescriptor feed, ISubscriptionCallback callback)
            throws IllegalArgumentException, Exception {

        return subscribe(feed, null, callback);
    }

    /**
     * @see ISubscription#subscribe(TaskServiceDescriptor, String[], ISubscriptionCallback)
     */
    @Override
    public TaskServiceDescriptor subscribe(TaskServiceDescriptor serviceDescriptor, String[] route,
            ISubscriptionCallback callback) throws IllegalArgumentException, Exception {

        // Ensure the feed does not contain wildcards
        if (serviceDescriptor.service() == null || serviceDescriptor.service().equals("*")
                || serviceDescriptor.platform() == null || serviceDescriptor.platform().equals("*")
                || serviceDescriptor.system() == null || serviceDescriptor.system().equals("*")
                || serviceDescriptor.task() == null || serviceDescriptor.task().equals("*")) {
            throw new IllegalArgumentException("Task service descriptor must not contain null or wildcards");
        }

        if (activeServiceDescriptor != null) {
            throw new SubscriptionException(ReasonCode.ALREADY_SUBSCRIBED, "Subscription already active");
        }

        activeServiceDescriptor = serviceDescriptor;

        logger.log(Level.FINE, "Subscribing to feed [{0}]", serviceDescriptor);

        this.callback = callback;

        topic = generateTopic(activeServiceDescriptor);
        channel = fabricClient.homeNodeEndPoint().openInputChannel(new InputTopic(topic), this);
        logger.log(Level.FINER, "Listening for subscription messages on [{0}]", topic);

        /* No route specified, so determine one for ourselves */
        if (route == null) {

            /* Get the matching feeds and their routes */
            FeedRoutes feedRoute = getFeedRoute(activeServiceDescriptor);

            if (feedRoute != null) {
                route = FabricRegistry.getRouteFactory().getRouteNodes(fabricClient.homeNode(),
                        feedRoute.getEndNodeId(), feedRoute.getRoute());
            }
        }

        /* If there is still no route available... */
        if (route == null) {

            /* We've failed to subscribe */
            String message = String.format("Subscription failed; cannot find route to feed \"%s\"",
                    activeServiceDescriptor);
            logger.fine(message);
            activeServiceDescriptor = null;
            throw new SubscriptionException(ReasonCode.NO_ROUTE, message);

        } else {

            /* Record the route used by this subscription */
            subscriptionRoute = Arrays.copyOf(route, route.length);

            /*
             * Build the subscription message
             */

            SubscriptionMessage subscriptionMessage = new SubscriptionMessage();

            /* Set the correlation ID */
            subscriptionMessage.setCorrelationID(correlationID);

            /* Indicate that we want notifications that this message is handled */
            subscriptionMessage.setNotification(true);

            /* Register to receive notifications related to this subscription */
            fabricClient.registerNotificationHandler(correlationID, this);

            /* Configure this as a subscription message */
            subscriptionMessage.setAction(IServiceMessage.ACTION_SUBSCRIBE);
            subscriptionMessage.setEvent(IServiceMessage.EVENT_ACTOR_REQUEST);
            subscriptionMessage.setProperty(IServiceMessage.PROPERTY_ACTOR, fabricClient.actor());
            subscriptionMessage.setProperty(IServiceMessage.PROPERTY_ACTOR_PLATFORM, fabricClient.platform());

            /* Store the feed list in the subscription message payload */
            FeedList feedList = new FeedList();
            feedList.addFeed(activeServiceDescriptor);
            subscriptionMessage.setFeedList(feedList);

            /* Set the message's routing */
            StaticRouting messageRouting = new StaticRouting(route);
            subscriptionMessage.setRouting(messageRouting);

            /* Send the subscribe command to the local Fabric Manager */
            logger.log(Level.FINER, "Sending subscribe command:\n{0}", subscriptionMessage.toString());
            ioChannels.sendCommandsChannel.write(subscriptionMessage.toWireBytes());

            /* Update the Client with the subscription */
            fabricClient.registerSubscription(this);

            /* Update the Registry with this subscription */

            TaskSubscriptionFactory factory = FabricRegistry.getTaskSubscriptionFactory();

            TaskSubscription ts = factory.createTaskSubscription(activeServiceDescriptor.task(), fabricClient.actor(),
                    activeServiceDescriptor.platform(), activeServiceDescriptor.system(), activeServiceDescriptor
                    .service(), fabricClient.platform());
            try {
                factory.save(ts);
            } catch (IncompleteObjectException e) {
                logger.log(Level.WARNING, "Internal error: ", e);
            }

            /* Build a corresponding unsubscribe command and record it */
            unsubscribeMessage = (SubscriptionMessage) subscriptionMessage.replicate();
            unsubscribeMessage.setAction(IServiceMessage.ACTION_UNSUBSCRIBE);

        }

        return activeServiceDescriptor;
    }

    /**
     * @see ISubscription#unsubscribe()
     */
    @Override
    public void unsubscribe() throws Exception {

        if (activeServiceDescriptor == null) {
            throw new SubscriptionException(ReasonCode.NOT_SUBSCRIBED, "No active subscription");
        }

        logger.log(Level.FINER, "Unsubscribing from feed {0}", activeServiceDescriptor);
        fabricClient.homeNodeEndPoint().closeChannel(channel, false);
        logger.log(Level.FINEST, "Stopped listening for subscription messages on \"{0}\"", topic);

        channel = null;
        topic = null;

        fabricClient.deregisterNotificationHandler(unsubscribeMessage.getCorrelationID());
        logger.log(Level.FINEST, "Sending unsubscribe command:\n{0}", unsubscribeMessage.toString());
        ioChannels.sendCommandsChannel.write(unsubscribeMessage.toWireBytes());

        /* De-register this subscription from the Fabric client */
        fabricClient.deregisterSubscription(this);

        /* Remove this subscription from the Registry */
        TaskSubscriptionFactory factory = FabricRegistry.getTaskSubscriptionFactory(QueryScope.LOCAL);

        TaskSubscription ts = factory.createTaskSubscription(activeServiceDescriptor.task(), fabricClient.actor(),
                activeServiceDescriptor.platform(), activeServiceDescriptor.system(),
                activeServiceDescriptor.service(), fabricClient.platform());
        factory.delete(ts);

    }

    @Override
    public String correlationID() {
        return correlationID;
    }

    @Override
    public TaskServiceDescriptor feed() {
        return activeServiceDescriptor;
    }

    @Override
    public String[] route() {
        return subscriptionRoute;
    }

    /**
     * Generates the topic name that matches the feed pattern for a subscription.
     *
     * @param pattern
     *            the pattern for which a topic is required.
     *
     * @return the topic.
     */
    private String generateTopic(TaskServiceDescriptor feed) {

        /* Work out the base part of the topic used for incoming feed messages */
        String topic = fabricClient.config("fabric.feeds.offramp", null, fabricClient.homeNode());

        /*
         * Construct the incoming topic for the feed, this takes the form: <base-
         * part>/<client-id>/<task-id>/<platform-id>/<system-id>/<feed-id>
         */

        /* Add the client ID */
        topic += '/' + fabricClient.actor();

        /* Add a separator */
        topic += '/';

        /* Add the actor platform ID */
        topic += fabricClient.platform();

        /* Add a separator */
        topic += '/';

        /* Add the task ID (or a wildcard) */
        if (feed.task().equals("*")) {
            topic += '+';
        } else {
            topic += feed.task();
        }

        /* Add a separator */
        topic += '/';

        /* Add the platform ID (or a wildcard) */
        if (feed.platform().equals("*")) {
            topic += '+';
        } else {
            topic += feed.platform();
        }

        /* Add a separator */
        topic += '/';

        /* Add the system ID (or a wildcard) */
        if (feed.system().equals("*")) {
            topic += '+';
        } else {
            topic += feed.system();
        }

        /* Add a separator */
        topic += '/';

        /* Add the feed ID (or a wildcard) */
        if (feed.service().equals("*")) {
            topic += '+';
        } else {
            topic += feed.service();
        }

        return topic;

    }

    /**
     * Answers the route for the feed descriptor.
     *
     * @param feedPattern
     *            the descriptor.
     *
     * @return the route for the feed
     *
     * @throws PersistenceException
     * @throws FactoryCreationException
     */
    private FeedRoutes getFeedRoute(TaskServiceDescriptor feedPattern) throws PersistenceException,
    FactoryCreationException {

        // Query not restricted to just local
        RegistryObject[] objects = FabricRegistry.runQuery(FeedRoutesFactoryImpl.getRouteQuery(feedPattern.task(),
                feedPattern.platform(), feedPattern.system(), feedPattern.service(), fabricClient.homeNode()),
                FeedRoutesFactoryImpl.class, QueryScope.DISTRIBUTED);

        /* If matching feeds have been found... */
        if (objects != null && objects.length > 0) {

            return (FeedRoutes) objects[0];

        } else {

            logger.log(Level.FINE, "No matching feeds found");

            return null;
        }
    }

    @Override
    public void startCallback(Object arg1) {

        try {
            /* Invoke the client callback */
            callback.startSubscriptionCallback();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception in subscription callback: ", e);
        }
    }

    @Override
    public void cancelCallback(Object arg1) {

        try {
            /* Invoke the client callback */
            callback.cancelSubscriptionCallback();
        } catch (Exception e) {
            /* There was an exception in the user's callback */
            logger.log(Level.WARNING, "Exception in subscription callback: ", e);
        }
    }

    @Override
    public void handleMessage(Message message) {

        FLog.enter(logger, Level.FINER, this, "handleMessage", message);

        String messageTopic = (String) message.topic;
        byte[] messageData = message.data;
        String messageString = new String((messageData != null) ? messageData : new byte[0]);

        logger.log(Level.FINER, "Handling message [{0}] from topic [{1}]", new Object[] {FLog.trim(messageString),
                message.topic});
        logger.log(Level.FINEST, "Full message:\n{0}", messageString);

        try {

            IFabricMessage parsedMessage = null;

            try {
                /* Parse the message */
                parsedMessage = FabricMessageFactory.create(messageTopic, messageData);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Improperly formatted message received on topic {0}: {1}", new Object[] {
                        messageTopic, messageString});
            }

            /* If this is a Fabric feed message... */
            if (parsedMessage instanceof IFeedMessage) {
                try {
                    /* Invoke the client callback */
                    callback.handleSubscriptionMessage((IFeedMessage) parsedMessage);
                } catch (Exception ce) {
                    logger.log(
                            Level.WARNING,
                            "Exception in subscription callback \"{0}\" (feed message handling) while handling message:\n{1}\n{2}",
                            new Object[] {callback.toString(), message, FLog.stackTrace(ce)});
                }

            } else {

                logger.log(Level.WARNING, "Ignoring unexpected message received on topic {0}: {1}", new Object[] {
                        messageString, messageTopic});
            }

        } catch (Exception e) {

            logger.log(Level.FINE, "Exception handling message received on topic [{0}]: {1}", new Object[] {
                    messageTopic, e.getMessage()});
            logger.log(Level.FINER, "Full message:\n{0}", messageString);
            logger.log(Level.FINEST, "Full exception: ", e);

        }

        FLog.exit(logger, Level.FINER, this, "handleMessage", null);
    }

    @Override
    public void handleNotification(IClientNotificationMessage message) {

        try {
            // Identify what event this is a notification for
            int eventType = message.getEvent();
            if (message.getNotificationAction() != null) {
                if (message.getNotificationAction().equals(IServiceMessage.ACTION_SUBSCRIBE)) {
                    eventType = IServiceMessage.EVENT_SUBSCRIBED;
                } else if (message.getNotificationAction().equals(IServiceMessage.ACTION_UNSUBSCRIBE)) {
                    eventType = IServiceMessage.EVENT_UNSUBSCRIBED;
                }
            }
            /* Invoke the client callback */
            callback.handleSubscriptionEvent(this, eventType, message);
        } catch (Exception ce) {
            logger.log(
                    Level.WARNING,
                    "Exception in subscription callback {0} (Fabric message handling) while handling message:\n{1}\n{2}",
                    new Object[] {callback.toString(), message, FLog.stackTrace(ce)});

        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
