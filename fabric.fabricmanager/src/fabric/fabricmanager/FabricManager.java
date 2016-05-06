/*
 * (C) Copyright IBM Corp. 2006, 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.fabricmanager;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.Fabric;
import fabric.FabricBus;
import fabric.FabricShutdownHook;
import fabric.IFabricShutdownHookAction;
import fabric.SystemDescriptor;
import fabric.bus.BusIOChannels;
import fabric.bus.BusMessageHandler;
import fabric.bus.IBusIO;
import fabric.bus.IBusServices;
import fabric.bus.NeighbourChannels;
import fabric.bus.feeds.IFeedManager;
import fabric.bus.feeds.impl.FeedManagerService;
import fabric.bus.feeds.impl.SubscriptionRecord;
import fabric.bus.impl.BusIO;
import fabric.bus.messages.IConnectionMessage;
import fabric.bus.messages.IFeedMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.messages.impl.ConnectionMessage;
import fabric.bus.plugins.IFabletDispatcher;
import fabric.bus.plugins.impl.FabletDispatcher;
import fabric.bus.services.IConnectionManager;
import fabric.bus.services.IFloodMessageService;
import fabric.bus.services.INotificationManager;
import fabric.bus.services.impl.ConnectionManagerService;
import fabric.bus.services.impl.FloodMessageService;
import fabric.bus.services.impl.NotificationManagerService;
import fabric.core.io.MessageQoS;
import fabric.core.properties.ConfigProperties;
import fabric.registry.FabricPlugin;
import fabric.registry.FabricRegistry;
import fabric.registry.Node;
import fabric.registry.NodeIpMapping;
import fabric.registry.QueryScope;
import fabric.registry.SystemFactory;
import fabric.registry.TaskService;
import fabric.registry.TaskServiceFactory;
import fabric.registry.Type;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.services.messageforwarding.MessageForwardingService;
import fabric.session.NodeDescriptor;

/**
 * The Fabric Manager main class.
 */
public class FabricManager extends FabricBus implements IBusServices, IFabricShutdownHookAction {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2006, 2014";

    /*
     * Class static fields
     */

    /** The shutdown hook for this JVM */
    private static FabricShutdownHook shutdownHook = null;

    /*
     * Class fields
     */

    /** The Fabric Manager node name. */
    private String name = null;

    /** The handler for Fabric messages. */
    private BusMessageHandler busMessageHandler = null;

    /** The handler for Fabric Manager I/O. */
    private BusIO busIO = null;

    /** The feed management service. */
    private IFeedManager feedManager = null;

    /** The connection management service. */
    private IConnectionManager connectionManager = null;

    /** The client notification service. */
    private INotificationManager notificationManager = null;

    /** Message forwarding service. */
    private MessageForwardingService forwardingManager = null;

    /** The manager for per-node Fablet plug-ins. */
    private IFabletDispatcher fabletDispatcher = null;

    /** The flood message service. */
    private IFloodMessageService floodService = null;

    /** Flag indicating if a shutdown of this Fabric client is in progress */
    private boolean shutdownInProgress = false;

    /*
     * Inner classes
     */

    /*
     * Static class initialization
     */

    static {

        /* Register the shutdown hook for this JVM */
        shutdownHook = new FabricShutdownHook("fabric.fabricmanager.FabricManager.shutdownHook");
        Runtime.getRuntime().addShutdownHook(shutdownHook);

    }

    /*
     * Class methods
     */

    /**
     * Fabric manager launcher.
     *
     * @param cla
     *            the command line arguments (unused).
     *
     * @throws Exception
     *             thrown if the Fabric Manager cannot be started. See the exception detail for more information.
     */
    public static void main(String[] cla) throws Exception {

        /* Start the Fabric Manager */
        FabricManager fm = new FabricManager();
        fm.init();
    }

    public FabricManager() {

        super(Logger.getLogger(FabricManager.class.getPackage().getName()));
    }

    /**
     * @see fabric.bus.IBusServices#busIO()
     */
    @Override
    public IBusIO busIO() {

        return busIO;
    }

    /**
     * @see fabric.bus.IBusServices#connectionManager()
     */
    @Override
    public IConnectionManager connectionManager() {

        return connectionManager;
    }

    /**
     * @see fabric.bus.IBusServices#feedManager()
     */
    @Override
    public IFeedManager feedManager() {

        return feedManager;
    }

    /**
     * @see fabric.bus.IBusServices#notificationManager()
     */
    @Override
    public INotificationManager notificationManager() {

        return notificationManager;
    }

    /**
     * @see fabric.bus.IBusServices#forwardingManager()
     */
    @Override
    public MessageForwardingService forwardingManager() {

        return forwardingManager;

    }

    /**
     * @see fabric.bus.IBusServices#stop()
     */
    @Override
    public void stop() {

        logger.log(Level.INFO, "Shutting down the Fabric Manager");

        fabletDispatcher.stopDispatcher();

        busMessageHandler.stop();

        /* Disconnect from the Fabric */
        disconnectFabric();

        logger.log(Level.INFO, "Fabric Manager shutdown");

    }

    /**
     * Initializes this instance of the Fabric Manager.
     *
     * @throws Exception
     *             thrown if an initialization error is encountered. See the exception detail for more information.
     */
    public void init() throws Exception {

        /*
         * Basic initialisation
         */

        /* Get access to the core configuration information for the Fabric Manager */
        initFabricConfig();
        name = homeNode();

        String signon1 = "Edgware Fabric Manager";
        String signon2 = String.format("Build %s", getBuildVersion());
        String signon3 = String.format("Node [%s]", name);
        System.out.println(signon1 + '\n' + signon2 + '\n' + signon3);

        /* We can now access enough configuration information to start logging */
        initLogging("fabric.fabricmanager", name);
        logger.log(Level.INFO, signon1);
        logger.log(Level.INFO, signon2);
        logger.log(Level.INFO, signon3);

        /* Establish a connection to the Fabric Registry database */
        initRegistry();

        /* Now that we have a Registry connection we can access the full Fabric configuration for this node */
        initNodeConfig();

        /* Clean up the Registry from the last run */
        cleanRegistry();

        /*
         * Join the Fabric
         */

        /* Connect to the local broker */
        IConnectionMessage connectionMessage = new ConnectionMessage(homeNode(), IServiceMessage.EVENT_CONNECTED);
        IConnectionMessage disconnectionMessage = new ConnectionMessage(homeNode(),
                IServiceMessage.EVENT_DISCONNECTED);
        initConnectionMessage(connectionMessage, disconnectionMessage);
        connectFabric();

        /* Add this node to the Registry (initially marked as unavailable) */
        registerNode();

        /* Get access to bus I/O and configure it with our handler for Fabric messages */
        busIO = new BusIO();
        busMessageHandler = new BusMessageHandler(this);
        busIO.setBusMessageHandler(busMessageHandler);

        /* Load pre-defined services */
        loadServices();

        /* Start to listen for incoming messages */
        busIO.openHomeNodeChannels();

        /* Pass the Registry API the connection to the home node */
        FabricRegistry.homeNodeEndPoint = busIO.homeNodeEndPoint();

        /* Load pre-defined Fablets */
        initFablets();

        /* Configure the instrumentation for this instance */
        initInstrumentation();

        /* Mark the node as available in the Registry */
        announceAvailability();

        /* Register this instance with the shutdown hook */
        shutdownHook.addAction(this);

        logger.log(Level.INFO, "Fabric Manager on [{0}] running", name);

    }

    /**
     * Removes old information from the Registry left over from the last Fabric Manager was run.
     * <p>
     * The tables cleaned up are:
     * <ul>
     * <li><code>ACTORS</code></li>
     * <li><code>BEARERS</code></li>
     * <li><code>DATA_FEEDS</code></li>
     * <li><code>NODE_IP_MAPPING</code></li>
     * <li><code>NODES</code></li>
     * <li><code>PLATFORMS</code></li>
     * <li><code>SERVICES</code></li>
     * <li><code>TASK_NODES</code></li>
     * <li><code>TASK_SERVICES</code></li>
     * <li><code>TASK_SUBSCRIPTIONS</code></li>
     * <li><code>TASKS</code></li>
     * </ul>
     */
    private void cleanRegistry() {

        if (config("fabric.node.cleanRegistry", "true").equals("true")) {

            logger.info("Cleaning Registry");

            String template = "DELETE FROM %s WHERE ( %s IS NULL OR NOT %s LIKE '%%\"persistent\":\"true\"%%' )";

            String[] updates = new String[] {String.format(template, "ACTORS", "ATTRIBUTES", "ATTRIBUTES"),
                    String.format(template, "BEARERS", "ATTRIBUTES", "ATTRIBUTES"),
                    String.format(template, "DATA_FEEDS", "ATTRIBUTES", "ATTRIBUTES"), "DELETE FROM NODE_IP_MAPPING",
                    String.format(template, "FEED_TYPES", "ATTRIBUTES", "ATTRIBUTES"),
                    String.format(template, "NODES", "ATTRIBUTES", "ATTRIBUTES"),
                    String.format(template, "NODE_TYPES", "ATTRIBUTES", "ATTRIBUTES"),
                    String.format(template, "PLATFORMS", "ATTRIBUTES", "ATTRIBUTES"),
                    String.format(template, "PLATFORM_TYPES", "ATTRIBUTES", "ATTRIBUTES"),
                    String.format(template, "SERVICES", "ATTRIBUTES", "ATTRIBUTES"),
                    String.format(template, "SERVICE_TYPES", "ATTRIBUTES", "ATTRIBUTES"),
                    String.format(template, "TASK_NODES", "CONFIGURATION", "CONFIGURATION"),
                    "DELETE FROM TASK_SUBSCRIPTIONS", String.format(template, "TASKS", "TASK_DETAIL", "TASK_DETAIL"),};

            try {
                FabricRegistry.runUpdates(updates);
            } catch (PersistenceException e) {
                logger.log(Level.WARNING, "Registry clean failed: ", e);
            }

            /* Get the list of remaining systems */
            SystemFactory sf = FabricRegistry.getSystemFactory(QueryScope.LOCAL);
            fabric.registry.System[] systems = sf.getAll();
            SystemDescriptor[] systemDescs = new SystemDescriptor[systems.length];
            for (int s = 0; s < systems.length; s++) {
                systemDescs[s] = new SystemDescriptor(systems[s].getPlatformId(), systems[s].getId());
            }
            List<SystemDescriptor> systemDescsList = Arrays.asList(systemDescs);

            /* Get the list of task services from the Registry, and remove those that are no longer needed */
            TaskServiceFactory tsf = FabricRegistry.getTaskServiceFactory(QueryScope.LOCAL);
            TaskService[] taskServices = tsf.getAllTaskServices();
            for (int ts = 0; ts < taskServices.length; ts++) {

                if (taskServices[ts].getConfiguration() == null
                        || !(taskServices[ts].getConfiguration().matches(".*\"persistent\":\"true\".*"))) {

                    SystemDescriptor tsSystemDesc = new SystemDescriptor(taskServices[ts].getPlatformId(),
                            taskServices[ts].getSystemId());

                    if (!systemDescsList.contains(tsSystemDesc)) {
                        logger.log(Level.FINEST, "Deleting task service record [{0}]", taskServices[ts]);
                        tsf.delete(taskServices[ts]);
                    }
                }
            }

        } else {

            logger.warning("Registry clean disabled");

        }
    }

    /**
     * Loads meta data for the Fabric node into the Registry.
     */
    private void registerNode() {

        /* Add the node type if not already present */

        Type nodeType = FabricRegistry.getTypeFactory().createNodeType(config(ConfigProperties.NODE_TYPE), null, null,
                null);

        try {
            try {
                FabricRegistry.getTypeFactory(QueryScope.LOCAL).insert(nodeType);
            } catch (DuplicateKeyException dke) {
                FabricRegistry.getTypeFactory(QueryScope.LOCAL).update(nodeType);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Cannot register node type [{0}]: {1}", new Object[] {nodeType.toString(),
                    e.getMessage()});
            logger.log(Level.FINEST, "Full exception: ", e);
        }

        /* Add the node itself */
        Node node = FabricRegistry.getNodeFactory().createNode(homeNode(), config(ConfigProperties.NODE_TYPE));
        node.setAffiliation(config(ConfigProperties.NODE_AFFILIATION));
        node.setDescription(config(ConfigProperties.NODE_DESCRIPTION));
        node.setReadiness("DEPLOYED");
        node.setAvailability("UNAVAILABLE");

        try {
            FabricRegistry.save(node);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Cannot register node [{0}]: {1}", new Object[] {node.toString(), e.getMessage()});
            logger.log(Level.FINEST, "Full exception: ", e);
        }

        logPossibleInterfaces();

        // Determine ipAddresses from interface names
        String[] interfaceNames = config().getProperty(ConfigProperties.NODE_INTERFACES,
                NodeDescriptor.loopbackInterface()).split(",");
        for (int i = 0; i < interfaceNames.length; i++) {
            String interfaceName = interfaceNames[i];
            logger.finest("Using interface : " + interfaceName);
            String interfaceAddress = "127.0.0.1";

            try {
                /* Add the node/IP mapping */

                NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);

                if (networkInterface == null) {

                    logger.log(Level.WARNING, "Interface [{0}] not found, defaulting to [{1}]", new Object[] {
                            interfaceName, ConfigProperties.NODE_INTERFACES_DEFAULT});
                    interfaceName = ConfigProperties.NODE_INTERFACES_DEFAULT;
                    networkInterface = NetworkInterface.getByName(interfaceName);

                }

                if (networkInterface != null) {

                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                    List<String> interfaceAddresses = new ArrayList<String>();

                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();
                        if (address instanceof Inet4Address) {
                            String nextAddress = address.getHostAddress();
                            logger.log(Level.FINER, "Found address for interface [{0}]: {1}", new Object[] {
                                    interfaceName, nextAddress});
                            interfaceAddresses.add(nextAddress);
                        }
                    }

                    if (interfaceAddresses.size() > 0) {
                        interfaceAddress = interfaceAddresses.get(0);
                    }
                }
            } catch (SocketException e1) {
                e1.printStackTrace();
            }

            logger.log(Level.FINE, "Using address [{1}] via interface [{0}]", new Object[] {interfaceName,
                    interfaceAddress});
            NodeIpMapping ipMapping = FabricRegistry.getNodeIpMappingFactory().createNodeIpMapping(homeNode(),
                    interfaceName, interfaceAddress, Integer.parseInt(config(ConfigProperties.NODE_PORT, "1883")));

            if (ipMapping.getIpAddress().equalsIgnoreCase("localhost")
                    || ipMapping.getIpAddress().equalsIgnoreCase("127.0.0.1")) {

                logger.log(
                        Level.WARNING,
                        "Registering this node with IP address [{0}] will mean that nodes on remote systems will not able to connect to it",
                        ipMapping.getIpAddress());
            }

            try {
                FabricRegistry.save(ipMapping);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Cannot register IP mapping [{0}]: {1}", new Object[] {ipMapping.toString(),
                        e.getMessage()});
                logger.log(Level.FINEST, "Full exception: ", e);
            }
        }
    }

    /**
     * Loads the pre-defined services implementing standard Fabric functionality.
     */
    private void loadServices() {

        /* Subscription/feed manager service */
        feedManager = (IFeedManager) busMessageHandler.loadService(FeedManagerService.class.getName(),
                Fabric.FABRIC_PLUGIN_FAMILY);
        busMessageHandler.setFeedManager(feedManager);

        /* Connection manager service */
        connectionManager = (IConnectionManager) busMessageHandler.loadService(
                ConnectionManagerService.class.getName(), Fabric.FABRIC_PLUGIN_FAMILY);

        /* Client notification service */
        notificationManager = (INotificationManager) busMessageHandler.loadService(NotificationManagerService.class
                .getName(), Fabric.FABRIC_PLUGIN_FAMILY);

        /* Flood Service */
        floodService = (IFloodMessageService) busMessageHandler.loadService(FloodMessageService.class.getName(),
                Fabric.FABRIC_PLUGIN_FAMILY);

        /* Message forwarding service */
        forwardingManager = (MessageForwardingService) busMessageHandler.loadService(MessageForwardingService.class
                .getName(), Fabric.FABRIC_PLUGIN_FAMILY);

        /* SOA manager service */
        // soaManager = (IComponentManager) busMessageHandler.loadService(ComponentManager.class.getName(),
        // Fabric.FABRIC_PLUGIN_FAMILY);

    }

    /**
     * Loads the and initializes Fablets.
     *
     * @throws Exception
     */
    private void initFablets() throws Exception {

        fabletDispatcher = fabletDispatcherFactory();

    }

    /**
     * Factory method to initialize the Fablet plug-in dispatcher for a node.
     *
     * @throws Exception
     */
    private IFabletDispatcher fabletDispatcherFactory() throws Exception {

        /* Get the list plug-ins for this node (locally only) */
        String predicate = format("node_id='%s' or node_id='*'", homeNode());
        FabricPlugin[] fablets = FabricRegistry.getFabricPluginFactory(QueryScope.LOCAL).getFabricPlugins(predicate);

        /* Initialize the dispatcher for this list of Fablets */
        IFabletDispatcher fabletDispatcher = FabletDispatcher.fabletFactory(homeNode(), fablets, this);

        return fabletDispatcher;

    }

    /**
     * Announce the availability of this node via the Registry.
     */
    private void announceAvailability() {

        /* update the node in the local Registry */
        Node thisNode = FabricRegistry.getNodeFactory(QueryScope.LOCAL).getNodeById(homeNode());
        thisNode.setAvailability("AVAILABLE");
        try {
            FabricRegistry.save(thisNode);
        } catch (IncompleteObjectException e) {
            logger.log(Level.WARNING, "Internal error: unexpected exception: ", e);
        }
    }

    /**
     * @see fabric.bus.IBusIO#nodeName()
     */
    @Override
    public String nodeName() {

        return homeNode();
    }

    /**
     * @see fabric.bus.IBusIO#deliverFeedMessage(java.lang.String, fabric.bus.messages.IFeedMessage,
     *      fabric.bus.feeds.impl.SubscriptionRecord, fabric.core.io.MessageQoS)
     */
    @Override
    public void deliverFeedMessage(String feedTopic, IFeedMessage message, SubscriptionRecord subscription,
            MessageQoS qos) throws Exception {

        busIO.deliverFeedMessage(feedTopic, message, subscription, qos);
    }

    /**
     * @see fabric.bus.IBusIO#sendServiceMessage(fabric.bus.messages.IServiceMessage, java.lang.String)
     */
    @Override
    public void sendServiceMessage(IServiceMessage message, String node) throws Exception {

        busIO.sendServiceMessage(message, node);
    }

    /**
     * @see fabric.bus.IBusIO#sendServiceMessage(fabric.bus.messages.IServiceMessage, java.lang.String[])
     */
    @Override
    public void sendServiceMessage(IServiceMessage message, String[] nodes) throws Exception {

        busIO.sendServiceMessage(message, nodes);
    }

    /**
     * @see fabric.bus.IBusIO#sendFeedMessage(java.lang.String, java.lang.String, fabric.bus.messages.IFeedMessage,
     *      fabric.core.io.MessageQoS)
     */
    @Override
    public void sendFeedMessage(String node, String feedTopic, IFeedMessage message, MessageQoS qos) throws Exception {

        busIO.sendFeedMessage(node, feedTopic, message, qos);
    }

    /**
     * @see fabric.bus.IBusIO#ioChannels()
     */
    @Override
    public BusIOChannels ioChannels() {

        return busIO.ioChannels();
    }

    /**
     * @see fabric.bus.IBusIO#disconnectNeighbour(java.lang.String)
     */
    @Override
    public void disconnectNeighbour(String id) throws UnsupportedOperationException, IOException {

        busIO.disconnectNeighbour(id);
    }

    /**
     * @see fabric.bus.IBusIO#disconnectNeighbour(fabric.session.NodeDescriptor, boolean)
     */
    @Override
    public NeighbourChannels disconnectNeighbour(NodeDescriptor nodeDescriptor, boolean doRetry)
        throws UnsupportedOperationException, IOException {

        return busIO.disconnectNeighbour(nodeDescriptor, doRetry);
    }

    /**
     * @see fabric.bus.IBusIO#connectNeighbour(java.lang.String)
     */
    @Override
    public NeighbourChannels connectNeighbour(String neighbour) {

        return busIO.connectNeighbour(neighbour);
    }

    /**
     * @see fabric.bus.IBusIO#connectedNeighbours()
     */
    @Override
    public NodeDescriptor[] connectedNeighbours() {

        return busIO.connectedNeighbours();
    }

    /**
     * @see fabric.bus.IBusIO#neighbourChannels(java.lang.String)
     */
    @Override
    public NeighbourChannels neighbourChannels(String id) {

        return busIO.neighbourChannels(id);
    }

    /**
     * @see fabric.bus.IBusIO#wrapRawMessage(byte[], boolean)
     */
    @Override
    public IFeedMessage wrapRawMessage(byte[] messageData, boolean isReplay) throws IOException {

        return busIO.wrapRawMessage(messageData, isReplay);

    }

    /**
     * @see fabric.bus.IBusIO#sendRawMessage(java.lang.String, byte[], boolean)
     */
    @Override
    public void sendRawMessage(String fullTopic, byte[] messageData, boolean isReplay) throws IOException {

        busIO.sendRawMessage(fullTopic, messageData, isReplay);

    }

    /**
     * @see fabric.bus.services.IConnectionManager#addActorMessage(java.lang.String, java.lang.String, java.lang.String,
     *      fabric.bus.messages.IServiceMessage, java.lang.String, boolean)
     */
    @Override
    public String addActorMessage(String node, String platform, String actor, IServiceMessage message, String event,
            boolean singleFire) {

        return connectionManager.addActorMessage(node, platform, actor, message, event, singleFire);

    }

    /**
     * @see fabric.bus.services.IConnectionManager#addFeedMessage(java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, fabric.bus.messages.IServiceMessage, java.lang.String, boolean)
     */
    @Override
    public String addFeedMessage(String node, String platform, String system, String feed, IServiceMessage message,
            String event, boolean singleFire) {

        return connectionManager.addFeedMessage(node, platform, system, feed, message, event, singleFire);

    }

    /**
     * @see fabric.bus.services.IConnectionManager#addNodeMessage(java.lang.String, fabric.bus.messages.IServiceMessage,
     *      java.lang.String, boolean)
     */
    @Override
    public String addNodeMessage(String node, IServiceMessage message, String event, boolean singleFire) {

        return connectionManager.addNodeMessage(node, message, event, singleFire);

    }

    /**
     * @see fabric.bus.services.IConnectionManager#addPlatformMessage(java.lang.String, java.lang.String,
     *      fabric.bus.messages.IServiceMessage, java.lang.String, boolean)
     */
    @Override
    public String addPlatformMessage(String node, String platform, IServiceMessage message, String event,
            boolean singleFire) {

        return connectionManager.addPlatformMessage(node, platform, message, event, singleFire);

    }

    /**
     * @see fabric.bus.services.IConnectionManager#addServiceMessage(java.lang.String, java.lang.String,
     *      java.lang.String, fabric.bus.messages.IServiceMessage, java.lang.String, boolean)
     */
    @Override
    public String addServiceMessage(String node, String platform, String system, IServiceMessage message, String event,
            boolean singleFire) {

        return connectionManager.addServiceMessage(node, platform, system, message, event, singleFire);

    }

    /**
     * @see fabric.bus.services.IConnectionManager#removeActorMessage(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void removeActorMessage(String node, String platform, String actor, String event) {

        connectionManager.removeActorMessage(node, platform, actor, event);

    }

    /**
     * @see fabric.bus.services.IConnectionManager#removeFeedMessage(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void removeFeedMessage(String node, String platform, String system, String feed, String event) {

        connectionManager.removeFeedMessage(node, platform, system, feed, event);

    }

    /**
     * @see fabric.bus.services.IConnectionManager#removeMessage(java.lang.String)
     */
    @Override
    public void removeMessage(String handle) {

        connectionManager.removeMessage(handle);

    }

    /**
     * @see fabric.bus.services.IConnectionManager#removeNodeMessage(java.lang.String, java.lang.String)
     */
    @Override
    public void removeNodeMessage(String node, String event) {

        connectionManager.removeNodeMessage(node, event);

    }

    /**
     * @see fabric.bus.services.IConnectionManager#removePlatformMessage(java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void removePlatformMessage(String node, String platform, String event) {

        connectionManager.removePlatformMessage(node, platform, event);

    }

    /**
     * @see fabric.bus.services.IConnectionNotificationManager#removeServiceMessage(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void removeServiceMessage(String node, String platform, String system, String event) {

        connectionManager.removeServiceMessage(node, platform, system, event);

    }

    /**
     * @see fabric.IFabricShutdownHookAction#shutdown()
     */
    @Override
    public void shutdown() {

        if (!shutdownInProgress) {

            shutdownInProgress = true;

            /* Set the node availability to "unavailable" (use a local queries only, don't flood the network) */

            Node thisNode = FabricRegistry.getNodeFactory(QueryScope.LOCAL).getNodeById(name);

            if (thisNode != null) {

                try {
                    thisNode.setAvailability("UNAVAILABLE");
                    FabricRegistry.save(thisNode);
                } catch (IncompleteObjectException e) {
                    /* Not much we can do about this at this stage */
                }

                try {
                    /* Remove any registry entries for neighbour connections */
                    String removeNeighbours = "DELETE FROM " + FabricRegistry.NODE_NEIGHBOURS + " WHERE NODE_ID='"
                            + name + "'";
                    FabricRegistry.runUpdates(new String[] {removeNeighbours});
                } catch (PersistenceException e) {
                    /* Not much we can do about this at this stage */
                }
            }

            stop();
        }
    }

    /**
     * Log the intefaces available to this node.
     */
    private void logPossibleInterfaces() {

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface myInterface = interfaces.nextElement();
                if (myInterface.isUp() && !myInterface.isVirtual()) {
                    Enumeration<InetAddress> addresses = myInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();
                        if (address instanceof Inet4Address) {
                            logger.log(Level.FINEST, "Available interface: {0}:{1}", new Object[] {
                                    myInterface.getName(), address.getHostAddress()});
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

}
