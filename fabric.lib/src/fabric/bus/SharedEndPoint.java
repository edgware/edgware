/*
 * (C) Copyright IBM Corp. 2007, 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.core.io.Channel;
import fabric.core.io.Config;
import fabric.core.io.EndPoint;
import fabric.core.io.ICallback;
import fabric.core.io.IEndPointCallback;
import fabric.core.io.InputTopic;
import fabric.core.io.OutputTopic;
import fabric.core.io.mqtt.MqttConfig;
import fabric.core.properties.Properties;

/**
 * Class representing a Fabric connection to a node using the <code>fabric.core.io</code> package.
 * <p>
 * The class builds on the basic <code>EndPoint</code> class by sharing the channels open against it. I.e. each channel
 * maintains a reference count, and each attempt to re-open it simply increments the reference count and returns the
 * existing channel. The channel is only closed when its reference count reaches zero.
 * </p>
 */
public class SharedEndPoint extends EndPoint {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2014";

    /*
     * Class fields
     */

    /** The broker's Fabric node name. */
    private String node = null;

    /** The broker's Fabric node interface name. */
    private String nodeInterface = null;

    /** The end-point connection to the node. */
    private EndPoint endpoint = null;

    /** The list of open channels */
    private HashMap<String, SharedChannel> channels = new HashMap<String, SharedChannel>();

    /*
     * Class methods
     */

    /**
     * Constructs a new endpoint.
     *
     * @param node
     *            the broker's Fabric node name.
     *
     * @param endpoint
     *            the <code>EndPoint</code> associated with this instance.
     */
    public SharedEndPoint(String node, String nodeInterface, EndPoint instance) {

        this(node, nodeInterface, instance, Logger.getLogger("fabric.bus"));

    }

    /**
     * Constructs a new endpoint.
     *
     * @param node
     *            the broker's Fabric node name.
     *
     * @param endpoint
     *            the <code>EndPoint</code> associated with this instance.
     */
    public SharedEndPoint(String node, String nodeInterface, EndPoint instance, Logger logger) {

        super(logger);
        this.node = node;
        this.nodeInterface = nodeInterface;
        this.endpoint = instance;

    }

    /**
     * @see fabric.core.io.EndPoint#configFactory(fabric.core.io.Config)
     */
    @Override
    public Config configFactory(Config source) {

        return endpoint.configFactory(source);
    }

    /**
     * @see fabric.core.io.EndPoint#configFactory(fabric.core.properties.Properties)
     */
    @Override
    public Config configFactory(Properties configProperties) {

        return endpoint.configFactory(configProperties);
    }

    /**
     * Connects to the end point.
     *
     * @param address
     *            the IP name/address of the end point (target broker).
     *
     * @param config
     *            end point configuration information object.
     *
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    @Override
    public void connect(Object address, Object config) throws UnsupportedOperationException, IOException {

        endpoint.connect(address, config);
        logger.log(Level.FINER, "Connected to endpoint [{0}]", address);

    }

    /**
     * Opens a default output channel (or increments the reference count on the existing one).
     *
     * @return the channel.
     *
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    public SharedChannel openOutputChannel() throws IOException, UnsupportedOperationException {

        return openIOChannel(null, null, null);
    }

    /**
     * Opens a channel (or increments the reference count on an existing one) to read from the specified inbound topic.
     *
     * @param inputTopic
     *            the inbound topic of the channel.
     *
     * @param callback
     *            set up an asynchronous read operation on the channel using this callback.
     *
     * @return the channel.
     *
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    public SharedChannel openInputChannel(InputTopic inputTopic, ICallback callback) throws IOException,
    UnsupportedOperationException {

        return openIOChannel(inputTopic, null, callback);
    }

    /**
     * Opens a channel (or increments the reference count on an existing one) to the specified local outbound topic.
     *
     * @param outputTopic
     *            the inbound topic of the channel.
     *
     * @return the channel.
     *
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    public SharedChannel openOutputChannel(OutputTopic outputTopic) throws IOException, UnsupportedOperationException {

        return openIOChannel(null, outputTopic, null);
    }

    /**
     * Opens a channel (or increments the reference count on an existing one) that will read from the specified input
     * topic, and write to the specified output topic.
     *
     * @param inputTopic
     *            the inbound topic of the channel.
     *
     * @param outputTopic
     *            the outbound topic of the channel.
     *
     * @param callback
     *            set up an asynchronous read operation on the channel using this callback.
     *
     * @return the channel.
     *
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    public SharedChannel openIOChannel(InputTopic inputTopic, OutputTopic outputTopic, ICallback callback)
            throws IOException, UnsupportedOperationException {

        logger.log(Level.FINEST, "Opening channel; local topic [{0}], remote topic [{1}], callback [{2}]",
                new Object[] {inputTopic, outputTopic, callback});

        /* Construct the key used to identify this channel */
        String channelKey = channelKey(inputTopic, outputTopic);

        /* Lookup an existing matching channel */
        SharedChannel nodeChannel = channels.get(channelKey);

        /* If the channel does not exist... */
        if (nodeChannel == null) {

            /* Create and record a new one */
            Channel newChannel = endpoint.channel(inputTopic, outputTopic);

            /* Record it */
            nodeChannel = new SharedChannel(newChannel, channelKey, this);
            channels.put(channelKey, nodeChannel);

        } else {

            /* Return a reference to the existing one */
            nodeChannel.incRefCount();

        }

        /* If a callback has been supplied... */
        if (callback != null) {

            /* Set up an asynchronous read on the port */
            nodeChannel.read(callback);

        }

        logger.log(Level.FINEST, "Channel opened; input topic [{0}], output topic [{1}]", new Object[] {inputTopic,
                outputTopic});
        return nodeChannel;
    }

    /**
     * Decrements the reference count of a channel, and closes it if it's reference count is zero.
     *
     * @param channelKey
     *            the caller-defined name of the channel.
     *
     * @param doForceClose
     *            <code>true</code> if the channel should be closed even if there are outstanding references,
     *            <code>false</code> otherwise.
     *
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    public void closeChannel(String channelKey, boolean doForceClose) throws IOException, UnsupportedOperationException {

        /* Lookup the channel */
        SharedChannel nodeChannel = channels.get(channelKey);

        /* Close the channel */
        closeChannel(nodeChannel, doForceClose);

    }

    /**
     * Decrements the reference count of a channel, and closes it if it's reference count is zero.
     *
     * @param sharedChannel
     *            the channel being dereferenced.
     *
     * @param doForceClose
     *            <code>true</code> if the channel should be closed even if there are outstanding references,
     *            <code>false</code> otherwise.
     *
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    public void closeChannel(SharedChannel sharedChannel, boolean doForceClose) throws IOException,
    UnsupportedOperationException {

        /* If the channel exists... */
        if (sharedChannel != null) {

            /* Get the channel key */
            String channelKey = sharedChannel.channelKey();

            /* Decrement the reference count */
            logger.log(Level.FINER, "Removing reference to channel {0}", channelKey);
            sharedChannel.decRefCount();

            /* If there are no more references to the channel... */
            if (doForceClose || sharedChannel.refCount() == 0) {

                /* Close and remove it */
                logger.log(Level.FINER, "Closing channel {0} ({1} references)", new Object[] {channelKey,
                        new Integer(channelRefCount(channelKey))});
                sharedChannel.channel().close();
                channels.remove(channelKey);

            }

        } else {

            logger.log(Level.FINER, "Attempt to close null channel");

        }
    }

    /**
     * Counts the number of references to each channel open against a node.
     *
     * @return the number of open channels.
     */
    public int totalChannelRefs() {

        /* To hold the result */
        int countReferences = 0;

        /* For each channel... */
        for (Iterator<String> i = channels.keySet().iterator(); i.hasNext();) {

            /* Get the channel... */
            SharedChannel nextChannel = channels.get(i.next());

            /* Add its reference count to the total */
            countReferences += nextChannel.refCount();

        }

        return countReferences;

    }

    /**
     * Answers the number of references to the channel open against the specific topic of the end point.
     *
     * @param name
     *            the name of the channel.
     *
     * @return the number of references.
     */
    public int channelRefCount(String name) {

        /* To hold the result */
        int channelReferences = 0;

        /* Get the channel record */
        SharedChannel nodeChannel = channels.get(name);

        /* If there is a matching channel... */
        if (nodeChannel != null) {

            /* Get the number of references */
            channelReferences = nodeChannel.refCount();

        }

        return channelReferences;

    }

    /**
     * Closes this connection (including closing all open channels).
     *
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    @Override
    public void close() throws IOException, UnsupportedOperationException {

        logger.log(Level.FINE, "Closing endpoint connection to [{0}] (interface [{1}])", new Object[] {node,
                nodeInterface});

        /* Get the list of open channels */
        HashMap<String, SharedChannel> channelsCopy = (HashMap<String, SharedChannel>) channels.clone();
        Set<String> openChannelKeysSet = channelsCopy.keySet();
        String[] openChannelKeys = openChannelKeysSet.toArray(new String[openChannelKeysSet.size()]);

        /* For each open channel... */
        for (int c = 0; c < openChannelKeys.length; c++) {
            /* Close it */
            closeChannel(openChannelKeys[c], true);
        }

        /* Close the connection */
        endpoint.close();
    }

    /**
     * Generates the key used to identify a channel.
     *
     * @param inputTopic
     *            the local topic of the channel.
     *
     * @param outputTopic
     *            the remote topic of the channel.
     *
     * @return the key.
     */
    private String channelKey(InputTopic inputTopic, OutputTopic outputTopic) {

        String s = (inputTopic != null) ? inputTopic.name() : null;
        String p = (outputTopic != null) ? outputTopic.name() : null;
        return "[" + s + "][" + p + "]";
    }

    /**
     * Answers the Fabric node name associated with the end point.
     *
     * @return the node name.
     */
    public String node() {

        return node;
    }

    /**
     * Answers the Fabric node name associated with the end point.
     *
     * @return the node interface name.
     */
    public String nodeInterface() {

        return nodeInterface;
    }

    /**
     * Answers the IP address associated with the end point.
     *
     * @return the IP address.
     */
    public String ipAddress() {

        String ipAddress = ipName();
        int ipPort = ipPort();

        if (ipPort >= 0) {
            ipAddress += ":" + ipPort;
        }

        return ipAddress;
    }

    /**
     * Answers the IP name associated with the end point.
     *
     * @return the IP name.
     */
    public String ipName() {

        return ((MqttConfig) (endpoint.getConfig())).getIPHost();
    }

    /**
     * Answers the IP port associated with the end point.
     *
     * @return the IP port.
     */
    public int ipPort() {

        return ((MqttConfig) (endpoint.getConfig())).getIPPort();
    }

    /*
     * (non-Javadoc)
     * @see fabric.core.io.EndPoint#channel(fabric.core.io.InputTopic, fabric.core.io.OutputTopic)
     */
    @Override
    public Channel channel(InputTopic inputTopic, OutputTopic outputTopic) throws IOException,
    UnsupportedOperationException {

        Channel newChannel = openIOChannel(inputTopic, outputTopic, null);
        return newChannel;

    }

    /**
     * @see fabric.core.io.EndPoint#getConfig()
     */
    @Override
    public Object getConfig() {

        return endpoint.getConfig();
    }

    /**
     * @see fabric.core.io.EndPoint#register(fabric.core.io.IEndPointCallback)
     */
    @Override
    public IEndPointCallback register(final IEndPointCallback callback) {

        return endpoint.register(callback);
    }

}
