/*
 * (C) Copyright IBM Corp. 2006, 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.bus.SharedEndPoint;
import fabric.bus.messages.IConnectionMessage;
import fabric.bus.messages.IFeedMessage;
import fabric.bus.messages.impl.FeedMessage;
import fabric.bus.messages.impl.MessagePayload;
import fabric.core.io.EndPoint;
import fabric.core.io.IOBase;
import fabric.core.io.mqtt.MqttConfig;
import fabric.core.properties.ConfigProperties;
import fabric.core.xml.XML;
import fabric.registry.FabricRegistry;
import fabric.registry.Route;
import fabric.registry.RouteFactory;
import fabric.registry.exception.PersistenceException;
import fabric.session.NodeDescriptor;

/**
 * Base Fabric class containing common utility methods for Fabric bus operations.
 */
public class FabricBus extends Fabric {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2006, 2014";

    /*
     * Class static fields
     */

    /** The connection to the local broker */
    private static SharedEndPoint homeNodeEndPoint = null;

    /** To hold the map of active connections to other nodes */
    private static HashMap<NodeDescriptor, SharedEndPoint> connectedNodes = new HashMap<NodeDescriptor, SharedEndPoint>();

    /** To hold the connection message, sent when this process connects to the Fabric */
    private static IConnectionMessage connectionMessage = null;

    /** To hold the disconnection message, sent when this process disconnects from the Fabric */
    private static IConnectionMessage disconnectionMessage = null;

    /** Flag indicating feed messages are to include the message ordinal element */
    private Boolean includeOrdinal = null;

    /** The UID of the next message to be published onto the Fabric */
    private long fabricMessageUID = 0;

    /*
     * Class methods
     */

    public FabricBus() {

        super(Logger.getLogger("fabric"));
    }

    public FabricBus(Logger logger) {

        super(logger);
    }

    /**
     * Wraps a message received from a locally connected system in a Fabric message envelope.
     *
     * @param messageData
     *            the message to republish (becomes the payload of the Fabric feed message).
     *
     * @param isReplay
     *            flag indicating if this is a replay (<code>true</code>) or original (<code>false</code>) message.
     *
     * @return the message.
     *
     * @throws IOException
     *             thrown if the message cannot be published.
     */
    public IFeedMessage wrapRawMessage(byte[] messageData, boolean isReplay) throws IOException {

        /* Package the incoming resource message as a Fabric feed message */

        FeedMessage message = new FeedMessage();

        if (includeOrdinal == null) {
            includeOrdinal = Boolean.parseBoolean(config().getProperty("fabric.message.element.ordinal", "false"));
        }

        if (includeOrdinal) {
            message.setOrdinal(fabricMessageUID);
        }

        fabricMessageUID++;

        if (isReplay) {
            message.setReplay(isReplay);
        }

        MessagePayload payload = new MessagePayload();
        payload.setPayload(messageData);
        message.setPayload(payload);

        return message;

    }

    /**
     * Answers the default (or first) route from the current node to the specified target node.
     *
     * @param targetNode
     *            the target node.
     *
     * @return the list of hops (nodes) forming the route, or null if there is no known route.
     *
     * @throws Exception
     *             thrown if a problem is encountered while obtaining or parsing the route from the Fabric Registry.
     */
    public String[] defaultRoute(String targetNode) throws Exception {

        /* To hold the result */
        String routeNodes[] = null;

        /* Get the known routes to the target node */
        RouteFactory routeFactory = FabricRegistry.getRouteFactory();
        Route[] routes = routeFactory.getRoutes(homeNode(), targetNode);

        /* If we have found any routes... */
        if (routes.length >= 1) {

            /* Extract and record the route hops from the first route */
            routeNodes = routeFactory.getRouteNodes(homeNode(), targetNode, routes[0].getRoute());

        }

        return routeNodes;
    }

    /**
     * Connects to the Fabric.
     */
    public void connectFabric() {

        int retryInterval = 5;

        do {
            /* Join the Fabric by connecting to the local broker */
            homeNodeEndPoint = connectHomeNode();

            if (homeNodeEndPoint == null) {
                logger.log(
                        Level.WARNING,
                        "Retrying connection to home node broker in {0} second(s) (ensure that the broker is running and configured correctly)",
                        retryInterval);
                try {
                    Thread.sleep(retryInterval * 1000);
                } catch (InterruptedException e) {
                }
            }
        } while (homeNodeEndPoint == null);
    }

    /**
     * Connect to the local (home) Fabric node.
     *
     * @return the connection to the home node.
     */
    public SharedEndPoint connectHomeNode() {

        NodeDescriptor nodeDescriptor = null;

        String node = config(ConfigProperties.NODE_NAME);
        int port = Integer.parseInt(config(ConfigProperties.BROKER_LOCAL_PORT,
                ConfigProperties.BROKER_LOCAL_PORT_DEFAULT));
        String address = config(ConfigProperties.HOME_BROKER_ADDRESS);

        if (address == null) {

            /* Build the node descriptor for the home node, assuming use of the loopback interface */
            nodeDescriptor = new NodeDescriptor(node, port);

        } else {

            /* Build the node descriptor for the home node */
            String iface = config(ConfigProperties.HOME_BROKER_INTERFACE);
            nodeDescriptor = new NodeDescriptor(node, iface, address, port);
        }

        /* Connect to the local node */
        return connectNode(nodeDescriptor);

    }

    /**
     * Disconnects from the Fabric.
     */
    public void disconnectFabric() {

        logger.log(Level.FINE, "Disconnecting from the Fabric");

        /* For each node... */
        for (Iterator<NodeDescriptor> nodeKeys = connectedNodes.keySet().iterator(); nodeKeys.hasNext();) {

            NodeDescriptor nd = nodeKeys.next();
            logger.log(Level.FINE, "Closing connection to node: [{0}]", nd);

            /* Get the connection details for this node */
            SharedEndPoint node = connectedNodes.get(nd);

            /* If this node is not my local broker node... */
            if (node != homeNodeEndPoint) {
                try {
                    node.close();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Failed to close connection to node [{0}]: {1}", new Object[] {nd.name(),
                            e.getMessage()});
                    logger.log(Level.FINEST, "Full exception: ", e);
                }
            }
        }

        try {
            /* Disconnect from the Fabric Registry */
            FabricRegistry.disconnect();
        } catch (PersistenceException e1) {
            logger.log(Level.WARNING, "Failed to close connection to Fabric Registry: ", e1);
        }

        /* Stop instrumentation, if running */
        metrics().closeManager();

        /*
         * Give the last will and testament a chance to fire, and then close the connection to the local Fabric node.
         */

        try {
            Thread.sleep(1000);
        } catch (InterruptedException eSleep) {
        }

        try {
            homeNodeEndPoint.close();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to close connection to Fabric broker: ", e);
        }

        logger.log(Level.FINE, "Disconnected from the Fabric");
    }

    /**
     * Gets the end point connection to the local broker.
     *
     * @return the end point connection to the local broker.
     */
    public SharedEndPoint homeNodeEndPoint() {

        return homeNodeEndPoint;
    }

    /**
     * Gets a connection to the specified Fabric node.
     * <p>
     * If a connection has already been established then the existing connection is returned, otherwise a new one is
     * created.
     * </p>
     *
     * @param nodeDescriptor
     *            a descriptor for the node to which a connection is being made.
     *
     * @return the connection.
     */
    public SharedEndPoint connectNode(NodeDescriptor nodeDescriptor) {

        InetAddress inetAddress = null;
        SharedEndPoint sharedEndPoint = null;
        String nodeID = null;
        String nodeInterface = null;
        String ipAddress = null;
        int portNumber = 0;
        String fullIPAddress = null;

        if (nodeDescriptor == null) {

            logger.log(Level.WARNING, "No Fabric name/IP mapping for this node");

        } else {

            logger.log(Level.FINE,
                    "Establishing connection to broker for node [{0}] at address [{2}:{3}] via interface [{1}]",
                    new Object[] {nodeDescriptor.name(), nodeDescriptor.networkInterface(), nodeDescriptor.address(),
                            "" + nodeDescriptor.port()});

            try {

                /* Get the address of the target node */

                inetAddress = InetAddress.getByName(nodeDescriptor.address());

                if (inetAddress == null) {

                    /* Cannot resolve node name/ipaddress */
                    throw new Exception("Cannot resolve IP address for broker for node " + nodeDescriptor.name()
                            + "at address " + nodeDescriptor.address() + ":" + nodeDescriptor.port());

                } else {

                    nodeID = nodeDescriptor.name();
                    nodeInterface = nodeDescriptor.networkInterface();
                    ipAddress = inetAddress.getHostAddress();
                    portNumber = nodeDescriptor.port();
                    fullIPAddress = ipAddress + ":" + portNumber;

                    /* Check for an existing connection to the node */
                    sharedEndPoint = connectedNodes.get(nodeDescriptor);

                }

                /* If we are not already connected... */
                if (sharedEndPoint == null) {

                    /* Create an end point for connection to the remote node */
                    EndPoint endPoint = EndPoint.endPointFactory(IOBase.DOMAIN_PUBSUB, IOBase.PROTOCOL_MQTT);

                    /* Customize the configuration */

                    MqttConfig endPointConfig = (MqttConfig) endPoint.configFactory(config());

                    /* Node address */
                    endPointConfig.setIPHost(nodeDescriptor.address());
                    endPointConfig.setBrokerIpPort(nodeDescriptor.port());

                    /* Connect and disconnect messages */
                    String sendConnectionMessageTopic = config(ConfigProperties.TOPIC_RECEIVE_SESSION_TOPOLOGY, null,
                            nodeID);

                    endPointConfig.setConnectionMessageTopic(sendConnectionMessageTopic);
                    endPointConfig.setConnectMessage(connectionMessage.toString());
                    endPointConfig.setDisconnectMessage(disconnectionMessage.toString());

                    /* Establish a connection to the node */
                    sharedEndPoint = new SharedEndPoint(nodeID, nodeInterface, endPoint);
                    sharedEndPoint.connect(fullIPAddress, endPointConfig);

                    /* Record the connection */
                    connectedNodes.put(nodeDescriptor, sharedEndPoint);

                    logger.log(Level.FINE, "Connected to broker for node [{0}]", nodeID);

                } else {

                    logger.log(Level.FINE, "Already connected to broker for node [{0}]", nodeID);

                }

            } catch (Exception e) {

                sharedEndPoint = null;

                logger.log(Level.WARNING, "Unable to connect to broker for node [{0}]: {1}", new Object[] {nodeID,
                        e.getMessage()});
                logger.log(Level.FINEST, "Full exception: ", e);
            }
        }

        return sharedEndPoint;
    }

    /**
     * Disconnects from the specified Fabric node.
     *
     * @param node
     *            the Fabric node to be disconnected.
     */
    public void disconnectNode(NodeDescriptor node) {

        SharedEndPoint sharedEndPoint = null;

        try {

            /* Check for an existing connection to the node */
            sharedEndPoint = connectedNodes.remove(node);

            /* If we are connected... */
            if (sharedEndPoint != null) {

                /* Close the connection */
                sharedEndPoint.close();

            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Cannot disconnect from fabric node: ", e);
        }

    }

    /**
     * Gets the map of active connections to other nodes.
     *
     * @return The map of active connections to other nodes.
     */
    public HashMap<NodeDescriptor, SharedEndPoint> connectedNodes() {

        return connectedNodes;
    }

    /**
     * Initializes the Fabric connect/disconnect message for the current process.
     * <p>
     * These correspond the the last-will-and-testament messages associated with this process. Note that since each
     * process only has a single connection to each broker (shared by all publish/subscribe operations) there is only
     * one pair of connect/disconnect messages used.
     * </p>
     *
     * @param connectionMessage
     *            the connection message.
     *
     * @param disconnectionMessage
     *            the diconnectionMessage.
     */
    public void initConnectionMessage(IConnectionMessage connectionMessage, IConnectionMessage disconnectionMessage) {

        /* Record the connection message */
        this.connectionMessage = connectionMessage;

        if (this.connectionMessage != null) {
            logger.log(Level.FINEST, "Connection message set:\n{0}", connectionMessage);
        } else {
            logger.log(Level.WARNING,
                    "Connection message NOT set; Fabric topology management will not be available for this process");
        }

        /* Record the disconnection message */
        this.disconnectionMessage = disconnectionMessage;

        if (this.disconnectionMessage != null) {
            logger.log(Level.FINEST, "Disconnection message set:\n{0}", disconnectionMessage);
        } else {
            logger.log(Level.WARNING,
                    "Disconnection message NOT set; Fabric topology management will not be available for this process");
        }

    }

    /**
     * Utility method to unpack the Fabric route encoded in a Fabric XML route specification.
     *
     * @param route
     *            the string containing the XML encoded route.
     *
     * @return the list of route node IDs.
     *
     * @throws Exception
     *             thrown if a problem is encountered while parsing the route.
     */
    public static String[] unpackRoute(String route) throws Exception {

        /* To hold an ordered list of route nodes */
        ArrayList<String> nodeList = new ArrayList<String>();

        /* Parse the XML containing the routing information */
        XML routeXML = new XML();
        routeXML.parseString(route);

        /* Get the XML paths for the route nodes */
        String[] nodePaths = routeXML.getPaths("/route\\[.*\\]/nd\\[.*\\]");

        /* For each node... */
        for (int n = 0; n < nodePaths.length; n++) {

            /* Get the node ID (with a path of the form "/route/nd[n]@to") */
            String nodeID = routeXML.get(nodePaths[n] + "@to", n);

            /* Record it */
            nodeList.add(nodeID);

        }

        /* Convert to an array and return */
        String[] nodes = nodeList.toArray(new String[nodeList.size()]);
        return nodes;

    }

    /**
     * Utility method to trims a prefix string from the start of a topic name. This method will have no effect if
     * <code>prefix</code> is <code>null</code> or an empty string.
     *
     * @param prefix
     *            the prefix to remove.
     *
     * @param topic
     *            the topic name.
     *
     * @return the topic name sans prefix (if any).
     *
     */
    public static String trimPrefix(String prefix, String topic) {

        String trimmedTopic = topic;

        if (prefix == null || prefix.equals("")) {

            /* Do nothing */

        } else if (topic.equals(prefix)) {

            trimmedTopic = "";

        } else if (topic.startsWith(prefix)) {

            trimmedTopic = topic.substring(prefix.length() + 1, topic.length());

        }

        return trimmedTopic;
    }

    /**
     * Trims the wild card from the end of a topic name.
     *
     * @param topic
     *            the topic name.
     *
     * @return the topic name without the wild card (if any).
     *
     */
    public static String trimWildcard(String topic) {

        String trimmedTopic = topic;

        if (topic.endsWith("/#")) {
            trimmedTopic = topic.substring(0, topic.length() - 2);
        }

        return trimmedTopic;
    }

}
