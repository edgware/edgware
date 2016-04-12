/*
 * (C) Copyright IBM Corp. 2008, 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import fabric.Fabric;
import fabric.FabricBus;
import fabric.ServiceDescriptor;
import fabric.TaskServiceDescriptor;
import fabric.bus.feeds.ISubscription;
import fabric.bus.feeds.ISubscriptionCallback;
import fabric.bus.feeds.ISubscriptionCollection;
import fabric.bus.feeds.impl.WildcardSubscription;
import fabric.bus.messages.FabricMessageFactory;
import fabric.bus.messages.IClientNotificationMessage;
import fabric.bus.messages.IFeedMessage;
import fabric.bus.messages.IMessagePayload;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.messages.impl.MessagePayload;
import fabric.bus.messages.impl.ServiceMessage;
import fabric.bus.routing.impl.StaticRouting;
import fabric.client.services.IClientNotification;
import fabric.client.services.IClientNotificationHandler;
import fabric.core.io.mqtt.MqttConfig;
import fabric.registry.FabricRegistry;
import fabric.registry.Platform;
import fabric.registry.PlatformFactory;
import fabric.registry.Route;
import fabric.registry.RouteFactory;

/**
 * Fabric test class.
 */
public class FabricClientTester extends FabricBus implements ISubscriptionCallback, IClientNotification,
        IClientNotificationHandler {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2008, 2012";

    /*
     * Class fields
     */

    /** The client connection to the Fabric. */
    private FabricClient fabricClient = null;

    /** The ID of the actor using this application */
    private String actor = null;

    /** The Registry ID for this application */
    private String actorPlatform = null;

    /** A subscription instance. */
    private ISubscriptionCollection feedSubscription1 = null;

    /** The Fabric feed to which we will subscribe. */
    private TaskServiceDescriptor feed1 = null;

    /** The Fabric feed to which we will subscribe. */
    private final TaskServiceDescriptor feed2 = null;

    /** The maximum number of messages received from the feed before we unsubscribe. */
    private int maxMessages = 0;

    /** The current number of messages received from the feed. */
    private int messageCount = 0;

    /*
     * Class methods
     */

    /**
     * Test harness entry point.
     * <p>
     * The test client accepts several arguments:
     * <ol>
     * <li>The ID of the actor making the subscription.</li>
     * <li>The ID of the actor's platform (i.e. an identifier for this application).</li>
     * <li>The name of the task with which the subscription is associated.</li>
     * <li>The name of the platform that is the target of the subscription (may be the '<code>*</code>' wildcard).</li>
     * <li>The name of the system that is the target of the subscription (may be the '<code>*</code>' wildcard).</li>
     * <li>The name of the feed that is the target of the subscription (may be the '<code>*</code>' wildcard).</li>
     * <li>The maximum number of messages that will be received before the client application will unsubscribe and exit.
     * </li>
     * </ol>
     *
     * @param cla
     *            the command line arguments.
     */
    public static void main(String[] args) {

        /*
         * fabclient -actor actor-id -task task-id [-platform platform-name] [-service service-name] [-feed feed-type]]
         * [-max max-messages] [-registry registry-ip] node
         */

        Options options = new Options();

        Option op1 = new Option("actor", true, "actor-id");
        op1.setRequired(true);
        options.addOption(op1);

        Option op2 = new Option("task", true, "task-id");
        op2.setRequired(true);
        options.addOption(op2);

        options.addOption(new Option("platform", true, "platform-name"));
        options.addOption(new Option("service", true, "service-name"));
        options.addOption(new Option("feed", true, "feed-type"));
        options.addOption(new Option("max", true, "max-messages"));
        options.addOption(new Option("registry", true, "registry-ip"));

        CommandLineParser parser = new PosixParser();
        CommandLine line = null;
        try {
            // parse the command line arguments
            line = parser.parse(options, args);

            if (line.getArgs().length != 1) {
                throw new ParseException("Missing node name");
            }

        } catch (ParseException exp) {
            System.out.println("Unexpected exception:" + exp.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("fabclient", options);
            System.exit(1);
        }

        String registryIP = line.getOptionValue("registry", "localhost");
        System.setProperty("registry.address", "jdbc:derby://" + registryIP
                + ":6414/FABRIC;user=fabric;password=fabric");
        System.setProperty("registry.type", "gaian");

        FabricClientTester testharness = new FabricClientTester();

        try {

            testharness.run(line.getArgs()[0], line.getOptionValue("actor"), line.getOptionValue("task"), line
                    .getOptionValue("platform", "'*'"), line.getOptionValue("service", "'*'"), line.getOptionValue(
                    "feed", "'*'"), line.getOptionValue("max", "5"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FabricClientTester() {

        super(Logger.getLogger("fabric.client"));
    }

    /**
     * Connects to the Fabric and sets up the specified subscription.
     *
     * @param node
     *            the ID of the node to connect to.
     *
     * @param actor
     *            the ID of the actor.
     *
     * @param task
     *            the name of the task with which the subscription is associated.
     *
     * @param platform
     *            the name of the platform that is the target of the subscription (may be the '<code>*</code>'
     *            wildcard).
     *
     * @param system
     *            the name of the system that is the target of the subscription (may be the '<code>*</code>' wildcard).
     *
     * @param feed
     *            the name of the feed that is the target of the subscription (may be the '<code>*</code>' wildcard).
     *
     * @param maxMessagesString
     *            the maximum number of messages that will be received before the client application will unsubscribe
     *            and exit.
     *
     * @throws Exception
     */
    public void run(String node, String actor, String task, String platform, String system, String feed,
            String maxMessagesString) throws Exception {

        /* Determine how many messages to accept */
        maxMessages = Integer.parseInt(maxMessagesString);

        if (platform.equals("'*'")) {
            platform = "*";
        }
        if (system.equals("'*'")) {
            system = "*";
        }
        if (feed.equals("'*'")) {
            feed = "*";
        }

        /* Connect to the Fabric */
        this.actor = actor;
        this.actorPlatform = MqttConfig.generateClient("FCT_");

        feed1 = new TaskServiceDescriptor(task, platform, system, feed);

        fabricClient = new FabricClient(node, actor, actorPlatform, this);

        /* Register to receive notifications if the home node is lost */
        fabricClient.registerClientNotificationCallback(this);

        fabricClient.connect();

        /* Register this platform */
        fabricClient.registerPlatformType("APPLICATION", null, null, null);
        fabricClient.registerPlatform(actorPlatform, "APPLICATION");

        /* Register this actor */
        fabricClient.registerActorType("USER", null, null, null);
        fabricClient.registerActor(actor, "USER");

        /* Subscribe to a Fabric data feed */
        // feed2 = new TaskServiceDescriptor("CONTROL_ROOM_MONITORING", "TANK_2", "PG1", "BAR");
        // feedSubscription2 = new FeedSubscription(fabricClient);
        // feedList = feedSubscription2.subscribe(feed2, this);
        // traceFeedList(feedList);
        // traceFeedList(feedSubscription2.feedList());
        // trace(this, INFO, "Correlation ID = '%s'", feedSubscription2.correlationID());

    }

    /**
     * Send a test service message.
     *
     * @throws Exception
     */
    private void sendServiceMessage() throws Exception {

        /* Initialize a service message to hold the request */
        ServiceMessage serviceMessage = new ServiceMessage();

        /* Indicate that this is a built-in Fabric plug-in */
        serviceMessage.setServiceFamilyName(Fabric.FABRIC_PLUGIN_FAMILY);

        /* Indicate that this message should not be actioned along the route from subscriber to the publisher */
        serviceMessage.setActionEnRoute(false);

        /* Generate and set a correlation ID */
        String correlationID = FabricMessageFactory.generateUID();
        serviceMessage.setCorrelationID(correlationID);

        /* Indicate that we do not want notifications that this message is handled */
        serviceMessage.setNotification(true);

        /* Register to receive notifications related to this message */
        fabricClient.registerNotificationHandler(correlationID, this);

        /*
         * Configure this as a proxy message.
         */

        /* Set the service name: i.e. indicate that this is a message for the feed manager */
        serviceMessage.setServiceName("fabric.services.proxypublisher.ProxyPublisherService");
        serviceMessage.setAction(IServiceMessage.ACTION_PUBLISH_ON_NODE);
        serviceMessage.setEvent(IServiceMessage.EVENT_ACTOR_REQUEST);

        /* Indicate who and where this message originates from */
        serviceMessage.setProperty(IServiceMessage.PROPERTY_ACTOR, fabricClient.actor());
        serviceMessage.setProperty(IServiceMessage.PROPERTY_ACTOR_PLATFORM, fabricClient.platform());

        /* Set the target feed */
        ServiceDescriptor publishToDescriptor = new ServiceDescriptor("EXTRACTION", "ENTITY", "ENTITY");
        serviceMessage.setProperty(IServiceMessage.PROPERTY_DELIVER_TO_FEED, publishToDescriptor.toString());

        /* Store the notification payload in the notification message */
        IMessagePayload messagePayload = new MessagePayload();
        messagePayload.setPayloadBytes("hello, world".getBytes());
        serviceMessage.setPayload(messagePayload);

        /* Set the message's routing */
        String[] routeNodes = getRouteNodes(publishToDescriptor);
        StaticRouting messageRouting = new StaticRouting(routeNodes);
        serviceMessage.setRouting(messageRouting);

        /* Send the notification message to the local Fabric Manager */
        sendServiceMessage(serviceMessage);

    }

    /**
     * Get the route (node list) to a target feed.
     *
     * @param serviceDescriptor
     *            the target feed
     *
     * @return the list of nodes, or <code>null</code> if no route is found.
     *
     * @throws Exception
     */
    private String[] getRouteNodes(ServiceDescriptor serviceDescriptor) throws Exception {

        /* To hold the result */
        String[] routeNodes = null;

        /* Get the node to which were connected */
        String homeNode = homeNode();

        /* Get the node to which the feed's platform is connected */
        PlatformFactory platformFactory = FabricRegistry.getPlatformFactory();
        Platform targetPlatform = platformFactory.getPlatformById(serviceDescriptor.platform());
        String targetNode = targetPlatform.getNodeId();

        /* Get the routes to the specified feed */
        RouteFactory routeFactory = FabricRegistry.getRouteFactory();
        Route[] routesToFeed = routeFactory.getRoutes(homeNode, targetNode);

        /* If there is at least one route... */
        if (routesToFeed.length > 0) {

            /* Since these routes are sorted by ordinal we take the first, and extract the node list */
            routeNodes = routeFactory.getRouteNodes(homeNode, targetNode, routesToFeed[0].getRoute());

        } else {

            String message = format("No route found from node '%s' to node '%s'", homeNode, targetNode);
            logger.log(Level.SEVERE, message);
            throw new Exception(message);

        }

        return routeNodes;
    }

    private void traceFeedList(TaskServiceDescriptor[] feedList) {

        logger.log(Level.INFO, "Feed list:");

        for (int f = 0; feedList != null && f < feedList.length; f++) {
            logger.log(Level.INFO, "[{0}] {1}", new Object[] {f, feedList[f]});
        }
    }

    /**
     * Send the specified service message.
     *
     * @param message
     *            the message to send.
     *
     * @throws Exception
     */
    private void sendServiceMessage(IServiceMessage serviceMessage) throws Exception {

        /* Send the message to the local Fabric Manager */

        logger.log(Level.FINER, "Sending service message to local Fabric Manager: {0}", serviceMessage.toString());

        try {

            fabricClient.getIOChannels().sendCommandsChannel.write(serviceMessage.toWireBytes());

        } catch (Exception e) {

            String targetNode = serviceMessage.getRouting().endNode();
            String message = format("Cannot send notification service message to node '%s'", targetNode);
            logger.log(Level.SEVERE, message);
            throw new Exception(message, e);

        }

    }

    /**
     * @see fabric.bus.feeds.ISubscriptionCallback#startSubscriptionCallback()
     */
    @Override
    public void startSubscriptionCallback() {

        logger.log(Level.FINE, "startSubscriptionCallback(): not currently implemented in the test client");

    }

    /**
     * @see fabric.bus.feeds.ISubscriptionCallback#handleSubscriptionMessage(fabric.bus.messages.IFeedMessage)
     */
    @Override
    public void handleSubscriptionMessage(IFeedMessage message) {

        logger.log(Level.INFO, "Message {0} (ordinal {1}) arrived on topic {2}:\n{3}", new Object[] {messageCount,
                message.getOrdinal(), message.metaGetTopic(), message.getPayload().toString()});

        System.out.println("Message" + messageCount + " (ordinal " + message.getOrdinal() + ") arrived on topic "
                + message.metaGetTopic() + ":\n" + message.getPayload().toString());

        /* If we have received the maximum number of messages... */
        if (++messageCount == maxMessages) {

            try {
                /* Unsubscribe */
                feedSubscription1.unsubscribe();

                /* De-register this actor */
                fabricClient.deregisterActor(actor);
                fabricClient.deregisterActorType("TEST ACTOR");

                /* De-register this platform */
                fabricClient.deregisterPlatform(actorPlatform);
                fabricClient.deregisterPlatformType("TEST APP");

                /* Disconnect from the Fabric */
                fabricClient.close();

                /* All done */
                System.exit(0);

            } catch (Exception e) {

                logger.log(Level.WARNING, "Exception unsubscribing/disconnecting from the Fabric: ", e);

            }
        }
    }

    /**
     * @see fabric.bus.feeds.ISubscriptionCallback#handleDisconnectMessage(fabric.bus.messages.IServiceMessage)
     */
    @Override
    public void handleSubscriptionEvent(ISubscription subscription, int event, IServiceMessage message) {

        logger.log(Level.FINE, "handleSubscriptionEvent(): {0} type={1}", new Object[] {subscription.toString(), event});

    }

    /**
     * @see fabric.bus.feeds.ISubscriptionCallback#cancelSubscriptionCallback()
     */
    @Override
    public void cancelSubscriptionCallback() {

        logger.log(Level.FINE, "cancelSubscriptionCallback(): not currently implemented in the test client");

    }

    /**
     * @see fabric.client.services.IClientNotification#homeNodeNotification(fabric.bus.messages.IServiceMessage)
     */
    @Override
    public void homeNodeNotification(IServiceMessage message) {

        logger.log(Level.INFO, "homeNodeNotification(): IServiceMessage received: " + message);

    }

    /** @see fabric.client.services.IClientNotification#topologyNotification(fabric.bus.messages.IServiceMessage) */
    @Override
    public void topologyNotification(IServiceMessage message) {

        logger.log(Level.INFO, "topologyNotification(): IServiceMessage received: " + message);

    }

    /**
     * @see fabric.client.services.IClientNotification#fabricNotification(fabric.bus.messages.IServiceMessage)
     */
    @Override
    public void fabricNotification(IServiceMessage message) {

        logger.log(Level.INFO, "fabricNotification(): IServiceMessage received: " + message);

    }

    /**
     * @see fabric.client.services.IClientNotificationHandler#handleNotification(fabric.bus.messages.IClientNotificationMessage)
     */
    @Override
    public void handleNotification(IClientNotificationMessage message) {

        logger.log(Level.INFO, "IClientNotificationMessage received: " + message);

        try {

            if (IServiceMessage.EVENT_CONNECTED == message.getNotificationEvent()) {
                logger.log(Level.INFO, "Connected to home node");

                feedSubscription1 = new WildcardSubscription(fabricClient);
                ((WildcardSubscription) feedSubscription1).subscribe(feed1, this);

                /* Example for how to regularly refresh the subscription collection every 5 seconds */
                // new Thread() { public void run() {
                // while(true) {
                // try {
                // Thread.sleep(5000);
                // System.out.println("----");
                // for (ISubscription sub : feedSubscription1.subscriptions()) {
                // System.out.println(" -"+sub);
                // }
                // System.out.println("----");
                // feedSubscription1.refresh();
                // }catch(Exception e) {
                // e.printStackTrace();
                // }
                // }
                //
                // }}.start();

            } else if (IServiceMessage.EVENT_DISCONNECTED == message.getEvent()) {
                logger.log(Level.INFO, "Disconnected from home node");

            }

        } catch (Exception e) {

            logger.log(Level.WARNING, "Clean-up and re-subscription failed: ", e);

        }

    }

}