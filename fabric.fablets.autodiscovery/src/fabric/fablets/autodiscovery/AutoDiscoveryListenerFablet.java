/*
 * (C) Copyright IBM Corp. 2010, 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.fablets.autodiscovery;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.FabricBus;
import fabric.bus.SharedChannel;
import fabric.bus.messages.IFabricMessage;
import fabric.bus.plugins.IFabletConfig;
import fabric.bus.plugins.IFabletPlugin;
import fabric.bus.plugins.IPluginConfig;
import fabric.core.io.OutputTopic;
import fabric.core.properties.ConfigProperties;
import fabric.registry.FabricRegistry;
import fabric.registry.NodeIpMapping;
import fabric.registry.QueryScope;
import fabric.session.NodeDescriptor;

/**
 * Fablet class to listen on a UDP Socket for autodiscovery requests.
 *
 * Bridge connections to external platform brokers requiring help or send config messages to the AutoDiscovery Fablet
 * for Node requests.
 */

public class AutoDiscoveryListenerFablet extends FabricBus implements IFabletPlugin {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010, 2014";

    /*
     * Class constants
     */

    private final static String CLASS_NAME = AutoDiscoveryListenerFablet.class.getName();
    private final static String PACKAGE_NAME = AutoDiscoveryListenerFablet.class.getPackage().getName();

    private final static Logger logger = Logger.getLogger(PACKAGE_NAME);

    /*
     * Class fields
     */

    /** The configuration object for this instance */
    @SuppressWarnings("unused")
    private IFabletConfig fabletConfig = null;

    /** Object used to synchronize with the mapper main thread */
    private final Object threadSync = new Object();

    /** Home node name */
    String myNodeName;

    /** Topic on which discovered asset messages are published (needs to be node specific; load from config) */
    private OutputTopic autodiscoveryTopic;

    /** Flag used to indicate when the main thread should terminate */
    private boolean isRunning = false;

    private boolean perfLoggingEnabled = false;
    private long timeToProcessMessage = 0;

    /** Indicates if listening to AutoDiscovery messages is enabled. */
    private boolean autoDiscoveryListenerEnabled = false;

    /** Listeners setup to listen on each interface/ */
    private List<NetworkInterfaceListener> interfaceListeners = new ArrayList<NetworkInterfaceListener>();

    /** Queue used to hold discovery requests for processing */
    private MulticastRequestQueue msgQueue = null;

    /** Thread used to monitor the node cache and remove neighbours that have not reported in */
    private DiscoverySweeper sweeper = null;

    /** Store a table of time at which nodes have been discovered */
    protected Hashtable<NodeDescriptor, Long> nodeLastSeen = new Hashtable<NodeDescriptor, Long>();

    /** Hold the messages corresponding to discovery of each node */
    protected Hashtable<NodeDescriptor, MulticastNodeMessage> nodeMessageCache = new Hashtable<NodeDescriptor, MulticastNodeMessage>();

    /** Discovery request queue depth */
    static int queueDepth = 1;

    private StringBuffer seenBuffer = null;

    /*
     * Class methods
     */

    public AutoDiscoveryListenerFablet() {
    }

    /**
     * @see fabric.services.fabricmanager.plugins.FabricPlugin#startPlugin(fabric.services.fabricmanager.plugins.PluginConfig)
     */
    @Override
    public void startPlugin(IPluginConfig pluginConfig) {

        fabletConfig = (IFabletConfig) pluginConfig;
        myNodeName = homeNode();
        perfLoggingEnabled = new Boolean(this.config(ConfigProperties.REGISTRY_DISTRIBUTED_PERF_LOGGING));
        queueDepth = Integer.parseInt(this.config(ConfigProperties.AUTO_DISCOVERY_QUEUE_DEPTH,
                ConfigProperties.AUTO_DISCOVERY_QUEUE_DEPTH_DEFAULT));
        String autoDiscoveryRequest = config(ConfigProperties.AUTO_DISCOVERY_LISTEN,
                ConfigProperties.AUTO_DISCOVERY_LISTEN_DEFAULT);
        if (autoDiscoveryRequest.equalsIgnoreCase("enabled")) {
            autoDiscoveryListenerEnabled = true;
        }

        if (autoDiscoveryListenerEnabled) {

            /* Create the request queue */
            msgQueue = new MulticastRequestQueue(AutoDiscoveryListenerFablet.queueDepth);

            /* Get all the IP mappings for my node */
            NodeIpMapping[] nodeIpMappings = FabricRegistry.getNodeIpMappingFactory(QueryScope.LOCAL)
                    .getAllMappingsForNode(myNodeName);

            /* For each mapping... */
            for (int i = 0; i < nodeIpMappings.length; i++) {
                /* Listen */
                NodeIpMapping nodeIpMapping = nodeIpMappings[i];
                NetworkInterfaceListener interfaceListener = new NetworkInterfaceListener(nodeIpMapping, this, msgQueue);
                interfaceListeners.add(interfaceListener);
            }

            autodiscoveryTopic = new OutputTopic(config(ConfigProperties.AUTO_DISCOVERY_TOPIC,
                    ConfigProperties.AUTO_DISCOVERY_TOPIC_DEFAULT, myNodeName));

        } else {

            logger.log(Level.INFO, "Auto discovery listener disabled");

        }
    }

    /*
     * (non-Javadoc)
     * @see fabric.services.fabricmanager.plugins.FabricPlugin#stopPlugin()
     */
    @Override
    public void stopPlugin() {
        if (autoDiscoveryListenerEnabled) {

            /* Close socket */
            for (Iterator<NetworkInterfaceListener> iterator = interfaceListeners.iterator(); iterator.hasNext();) {
                NetworkInterfaceListener interfaceListener = iterator.next();
                interfaceListener.close();
            }
            if (sweeper != null) {
                sweeper.stop();
            }
            /* Shutdown threads */
            synchronized (msgQueue) {
                msgQueue.notifyAll();
            }
            /* Tell the main thread to stop... */
            isRunning = false;

        } else {

            /* Tell the main thread to stop... */
            isRunning = false;
            /* ...and wake it up */
            synchronized (threadSync) {
                threadSync.notify();
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        if (autoDiscoveryListenerEnabled) {

            /* Initialise the message queue and processor */
            logger.log(Level.FINER, "Discovery enabled; initialising request queue and processor thread");

            /* Bring sweeper online */
            if (sweeper == null) {
                /* Create and start the sweeper thread, used to remove node neighbours that have disappeared */
                sweeper = new DiscoverySweeper(this);
                new Thread(sweeper, "Auto-Discovery-Sweeper").start();
            }

            /* Start all the multicast Listeners */
            for (int i = 0; i < interfaceListeners.size(); i++) {
                NetworkInterfaceListener interfaceListener = interfaceListeners.get(i);
                new Thread(interfaceListener, "Auto-Discovery-Listener").start();
            }

            isRunning = true;

            /* Loop waiting for messages... */
            while (isRunning) {

                try {

                    synchronized (msgQueue) {
                        msgQueue.wait();
                    }

                } catch (InterruptedException e) {
                    logger.log(Level.WARNING, "Error trying wait on DiscoveryRequestQueue: ", e);
                }

                logger.log(Level.FINEST, "Discovery queue processor commencing message processing");

                while (msgQueue.isNotEmpty()) {

                    if (perfLoggingEnabled) {
                        timeToProcessMessage = System.currentTimeMillis();
                    }

                    MulticastMessage message = msgQueue.nextMessage();
                    processMessage(message);

                    if (perfLoggingEnabled) {
                        timeToProcessMessage = System.currentTimeMillis() - timeToProcessMessage;
                        logger.log(Level.FINEST, "Time taken to process message {0} was {1} milliseconds",
                                new Object[] {message, timeToProcessMessage});
                    }
                }
            }

        } else {

            /* Autodiscovery is disabled so go dormant */
            while (isRunning) {
                try {
                    synchronized (threadSync) {
                        threadSync.wait();
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * Process a discovery request from clients, platforms and nodes.
     *
     *
     * @param message
     */
    private void processMessage(MulticastMessage message) {

        /* Unpack the message , checking it is valid (we only handle Node messages) */
        if (message instanceof MulticastNodeMessage && ((MulticastNodeMessage) message).unpack()) {

            MulticastNodeMessage nodeMessage = (MulticastNodeMessage) message;
            logger.log(Level.FINE, "Discovery message from [{0}]", new Object[] {nodeMessage.getNodeDescriptor()});

            /* Update the node cache */
            synchronized (nodeLastSeen) {

                /* Only publish the discovery message if we've not heard from this node */
                if (!nodeLastSeen.containsKey(nodeMessage.getNodeDescriptor())) {
                    publishMessage(nodeMessage.getDiscoveryMessage(MulticastNodeMessage.AVAILABLE));
                    nodeMessageCache.put(nodeMessage.getNodeDescriptor(), nodeMessage);
                }

                /* Always update last-heard-from timestamp */
                nodeLastSeen.put(nodeMessage.getNodeDescriptor(), new Date().getTime());

                /* Log the complete list of nodes we've seen */
                if (logger.isLoggable(Level.FINEST)) {

                    seenBuffer = new StringBuffer();
                    Iterator<NodeDescriptor> nc_it = nodeLastSeen.keySet().iterator();

                    while (nc_it.hasNext()) {

                        if (seenBuffer.length() == 0) {
                            seenBuffer.append(nc_it.next());
                        } else {
                            seenBuffer.append(" ");
                            seenBuffer.append(nc_it.next());
                        }
                    }

                    logger.log(Level.FINEST, "Nodes seen: {0}", new Object[] {seenBuffer.toString()});
                }
            }

        } else {

            logger.log(Level.FINE, "Unexpected message [0]", new Object[] {new String(message.getPacket().getData(), 0,
                    message.getPacket().getLength())});
        }
    }

    /*
     * (non-Javadoc)
     * @see fabric.services.fabricmanager.FabletPlugin#handleControlMessage(fabric
     * .services.fabricmanager.FabricMessage)
     */
    @Override
    public void handleControlMessage(IFabricMessage message) {

        /* Not supported */
    }

    /**
     * Publish a message to the local broker.
     *
     * @param msg
     */
    public void publishMessage(String msg) {

        try {

            SharedChannel configChannel = homeNodeEndPoint().openOutputChannel(autodiscoveryTopic);
            byte[] configMessageBuf = msg.getBytes();
            configChannel.write(configMessageBuf);
            homeNodeEndPoint().closeChannel(configChannel, false);

        } catch (Exception e) {

            logger.log(Level.WARNING, "Could not write to topic [{0}] on local broker: {1}", new Object[] {
                    autodiscoveryTopic, e.getMessage()});

        }
    }

}
