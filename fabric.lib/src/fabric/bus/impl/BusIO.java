/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2007, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.Fabric;
import fabric.FabricBus;
import fabric.FabricMetric;
import fabric.ServiceDescriptor;
import fabric.bus.BusIOChannels;
import fabric.bus.BusMessageHandler;
import fabric.bus.IBusIO;
import fabric.bus.NeighbourChannels;
import fabric.bus.SharedEndPoint;
import fabric.bus.feeds.impl.SubscriptionRecord;
import fabric.bus.messages.FabricMessageFactory;
import fabric.bus.messages.IFabricMessage;
import fabric.bus.messages.IFeedMessage;
import fabric.bus.messages.IMessagePayload;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.messages.impl.FeedMessage;
import fabric.bus.messages.impl.MessagePayload;
import fabric.bus.messages.impl.ServiceMessage;
import fabric.core.io.EndPoint;
import fabric.core.io.ICallback;
import fabric.core.io.IEndPointCallback;
import fabric.core.io.InputTopic;
import fabric.core.io.Message;
import fabric.core.io.MessageQoS;
import fabric.core.io.OutputTopic;
import fabric.core.io.mqtt.MqttConfig;
import fabric.core.io.mqtt.MqttEndPoint;
import fabric.core.logging.LogUtil;
import fabric.core.properties.ConfigProperties;
import fabric.registry.FabricRegistry;
import fabric.registry.NodeIpMapping;
import fabric.registry.NodeNeighbour;
import fabric.services.floodmessage.FloodRouting;
import fabric.session.NodeDescriptor;

/**
 * Class managing I/O on the Fabric bus.
 * <p>
 * This includes:
 * <ul>
 * <li>Placing messages onto the bus</li>
 * <li>Moving messages across the bus</li>
 * <li>Pulling messages off of the bus to fulfill user subscriptions</li>
 * </ul>
 */
public class BusIO extends FabricBus implements IBusIO, ICallback, IEndPointCallback {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2014";

	/*
	 * Class fields
	 */

	/** The handler for Fabric messages */
	private BusMessageHandler messageHandler = null;

	/** The list of nodes neighbouring this Fabric Manager */
	private final HashMap<NodeDescriptor, NeighbourChannels> neighbourChannelsTable = new HashMap<NodeDescriptor, NeighbourChannels>();

	/** To hold the channels and topics used by the Fabric Manager */
	private final BusIOChannels ioChannels = new BusIOChannels();

	/** The template topic for sending Fabric Manager commands to a neighbouring node */
	private String fabricCommandsBusTemplate = null;

	/** The template topic for sending data feed message to a neighbouring node */
	private String fabricFeedsBusTemplate = null;

	/** The template topic for the Fabric registry bus connection to a neighbouring node */
	private String fabricRegistryBusTemplate = null;

	/** The UID of the next message to be published onto the Fabric */
	private long fabricMessageUID = 0;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 */
	public BusIO() {

		super(Logger.getLogger("fabric.bus"));
		initConfig();
	}

	/**
	 * Constructs a new instance.
	 */
	public BusIO(Logger logger) {

		super(logger);
		initConfig();
	}

	/**
	 * Initializes neighbour connection configuration.
	 */
	private void initConfig() {

		fabricCommandsBusTemplate = config(ConfigProperties.TOPIC_SEND_SESSION_COMMANDS,
				ConfigProperties.TOPIC_SEND_SESSION_COMMANDS_DEFAULT);
		fabricFeedsBusTemplate = config(ConfigProperties.TOPIC_FEEDS_BUS, ConfigProperties.TOPIC_FEEDS_BUS_DEFAULT);
		fabricRegistryBusTemplate = config(ConfigProperties.REGISTRY_COMMAND_TOPIC,
				ConfigProperties.REGISTRY_COMMAND_TOPIC_DEFAULT);
	}

	/**
	 * Opens the required channels to the local broker.
	 */
	public void openHomeNodeChannels() {

		try {

			logger.log(Level.FINE, "Fabric connecting on node {0} at address {1}:{2}", new Object[] {homeNode(),
					homeNodeEndPoint().ipName(), homeNodeEndPoint().ipPort()});

			/*
			 * Connect to the local node and open the ports
			 */

			/* The topic on which the Fabric Manager listens for commands */
			ioChannels.receiveCommands = new InputTopic(config(ConfigProperties.TOPIC_SEND_SESSION_COMMANDS,
					ConfigProperties.TOPIC_SEND_SESSION_COMMANDS_DEFAULT, homeNode()));
			ioChannels.receiveCommandsChannel = homeNodeEndPoint().openInputChannel(ioChannels.receiveCommands, this);
			logger.log(Level.FINER, "Listening on: {0}", ioChannels.receiveCommands);

			/* The topic on which the Fabric Manager sends commands */
			ioChannels.sendCommands = new OutputTopic(config(ConfigProperties.TOPIC_SEND_SESSION_COMMANDS,
					ConfigProperties.TOPIC_SEND_SESSION_COMMANDS_DEFAULT, homeNode()));
			ioChannels.sendCommandsChannel = homeNodeEndPoint().openOutputChannel(ioChannels.sendCommands);
			logger.log(Level.FINER, "Commands will be sent to: {0}", ioChannels.sendCommands);

			/*
			 * The channel on which the Fabric Manager publishes commands to locally attached clients (note that the
			 * client-specific topic must be provided when this channel is used)
			 */
			ioChannels.sendClientCommandsChannel = homeNodeEndPoint().openOutputChannel();
			logger.log(Level.FINER, "Client commands will be sent via channel: {0}",
					ioChannels.sendClientCommandsChannel);

			/* The channel on which the Fabric Manager publishes commands to locally attached platforms */
			ioChannels.sendPlatformCommandsChannel = homeNodeEndPoint().openOutputChannel();
			logger.log(Level.FINER, "Platform commands will be sent via channel: {0}",
					ioChannels.sendPlatformCommandsChannel);

			/* The channel on which the Fabric Manager publishes commands to locally attached services */
			ioChannels.sendServiceCommandsChannel = homeNodeEndPoint().openOutputChannel(ioChannels.sendCommands);
			logger.log(Level.FINER, "System commands will be sent to: {0}", ioChannels.sendCommands);

			/* The topic on which the Fabric Manager listens for locally connected data feeds */
			ioChannels.receiveLocalFeeds = new InputTopic(config("fabric.feeds.onramp", null, homeNode()));
			ioChannels.receiveLocalFeedsChannel = homeNodeEndPoint().openInputChannel(
					new InputTopic(ioChannels.receiveLocalFeeds + "/#"), this);
			logger.log(Level.FINER, "Listening for locally connected data feeds on: {0}", ioChannels.receiveLocalFeeds
					+ "/#");

			/* The topic on which the Fabric Manager listens for messages en route across the Fabric */
			ioChannels.receiveBus = new InputTopic(config(ConfigProperties.TOPIC_FEEDS_BUS,
					ConfigProperties.TOPIC_FEEDS_BUS_DEFAULT, homeNode()));
			ioChannels.receiveBusChannel = homeNodeEndPoint().openInputChannel(
					new InputTopic(ioChannels.receiveBus + "/#"), this);
			logger.log(Level.FINER, "Listening for bus data feed messages on: {0}", ioChannels.receiveBus + "/#");

			/* The topic on which the Fabric Manager listens for local replay messages */
			ioChannels.receiveLocalReplayFeeds = new InputTopic(config("fabric.feeds.replay", null, homeNode()));
			ioChannels.receiveLocalReplayFeedsChannel = homeNodeEndPoint().openInputChannel(
					new InputTopic(ioChannels.receiveLocalReplayFeeds + "/#"), this);
			logger.log(Level.FINER, "Listening for local replay messages on: {0}", ioChannels.receiveLocalReplayFeeds
					+ "/#");

			/* The topic on which the Fabric Manager listens for last will and testament messages */
			ioChannels.connectionComands = new InputTopic(config("fabric.commands.topology", null, "+"));
			ioChannels.connectionCommandsChannel = homeNodeEndPoint().openInputChannel(ioChannels.connectionComands,
					this);
			logger.log(Level.FINER, "Listening for LWT messages on: {0}", ioChannels.connectionComands);

			/* The topic on which the Fabric Manager publishes messages for local consumption */
			ioChannels.sendLocalSubscription = new OutputTopic(config("fabric.feeds.offramp", null, homeNode()));
			ioChannels.sendLocalSubscriptionChannel = homeNodeEndPoint().openOutputChannel(
					ioChannels.sendLocalSubscription);
			logger.log(Level.FINER, "Delivered messages will be sent to: {0}", ioChannels.sendLocalSubscription);

		} catch (Exception e) {

			logger.log(Level.SEVERE, "Cannot set up publish and/or subscribe topics with broker:", e);

		}
	}

	/**
	 * @see fabric.core.io.ICallback#startCallback(java.lang.Object)
	 */
	@Override
	public void startCallback(Object arg1) {

		/* Unused */
	}

	/**
	 * @see fabric.core.io.ICallback#handleMessage(fabric.core.io.Message)
	 */
	@Override
	public synchronized void handleMessage(Message message) {

		logger.log(Level.FINER, "Handling message from topic \"{0}\":\n{1}", new Object[] {message.topic,
				new String((message.data != null) ? message.data : new byte[0])});

		String messageTopic = (String) message.topic;
		byte[] messageData = message.data;
		String messageString = new String(messageData);

		/* Instrumentation */
		FabricMetric metric = null;

		if (doInstrument()) {
			metric = new FabricMetric(homeNode(), null, null, null, null, -1, messageData, null);
			metrics().startTiming(metric, FabricMetric.EVENT_NODE_PROCESSING_START);
		}

		try {

			/* If this is a local message from a source attached directly to the node... */
			if (messageTopic.startsWith(ioChannels.receiveLocalFeeds.name())) {

				String feedTopic = ServiceDescriptor.extract(messageTopic);

				if (feedTopic.startsWith("$")) {

					try {
						floodFeedMessage(feedTopic, message.data);
					} catch (Exception e) {
						logger.log(Level.SEVERE, "Failed publish virtual service message", e);
					}

				} else {

					sendRawMessage(messageTopic, message.data, false);

				}

			}
			/* Else if it's a local replay message from a source attached directly to the node... */
			else if (messageTopic.startsWith(ioChannels.receiveLocalReplayFeeds.name())) {

				sendRawMessage(messageTopic, message.data, true);

			}
			/* Else this should be a message that we can parse */
			else {

				IFabricMessage parsedMessage = null;

				try {

					/* Parse the message */
					parsedMessage = FabricMessageFactory.create(messageTopic, messageData);

				} catch (Exception e) {

					logger.log(Level.WARNING, "Improperly formatted message received on topic {0}: {1}", new Object[] {
							messageTopic, messageString});
					logger.log(Level.FINE, "Exception:", e);

				}

				/* If this is a Fabric feed message... */
				if (parsedMessage instanceof IFeedMessage) {

					messageHandler.handleFeedMessage((IFeedMessage) parsedMessage);

				}
				/* Else if this is a Fabric service message... */
				else if (parsedMessage instanceof IServiceMessage) {

					logger.log(Level.FINEST, "Service message recevied on topic {0}: {1}", new Object[] {messageTopic,
							parsedMessage.toString()});
					messageHandler.handleServiceMessage((ServiceMessage) parsedMessage);

				}
				/* Else if this is any other kind of Fabric message... */
				else if (parsedMessage != null) {

					logger.log(Level.WARNING, "Unsupported Fabric message recevied on topic {0}: {1}", new Object[] {
							messageTopic, parsedMessage.toString()});

				}
				/*
				 * Else if it's an improperly formatted last will and testament message (any thing on this topic should
				 * be an IConnectionMessage)...
				 */
				else if (messageTopic.startsWith(ioChannels.connectionComands.name())) {

					logger.log(Level.WARNING,
							"Ignoring improperly formatted connection status (last-will-and-testament) message");

				} else {

					logger.log(Level.WARNING, "Ignoring improperly formatted message: {0}", messageString);

				}

			}

		} catch (Exception e) {

			logger.log(Level.WARNING, "Exception handling message received on topic \"{0}\":\n{1}\n{2}", new Object[] {
					messageTopic, messageString, LogUtil.stackTrace(e)});

		} finally {

			if (doInstrument()) {
				metrics().endTiming(metric, FabricMetric.EVENT_NODE_PROCESSING_STOP);
			}

		}
	}

	/**
	 * Builds and sends a flood message to distribute a virtual feed message across the Fabric.
	 * 
	 * @param feedTopic
	 *            the topic to which the message will be delivered on each node.
	 * 
	 * @param messageData
	 *            the message payload.
	 * 
	 * @throws Exception
	 */
	private void floodFeedMessage(String feedTopic, byte[] messageData) throws Exception {

		ServiceMessage serviceMessage = new ServiceMessage();

		serviceMessage.setServiceFamilyName(Fabric.FABRIC_PLUGIN_FAMILY);
		serviceMessage.setActionEnRoute(true);
		serviceMessage.setNotification(false);
		serviceMessage.setCorrelationID(FabricMessageFactory.generateUID());

		serviceMessage.setServiceName("fabric.services.proxypublisher.ProxyPublisherService");
		serviceMessage.setAction(IServiceMessage.ACTION_PUBLISH_ON_NODE);
		serviceMessage.setEvent(IServiceMessage.EVENT_ACTOR_REQUEST);

		serviceMessage.setProperty(IServiceMessage.PROPERTY_DELIVER_TO_FEED, feedTopic);

		IMessagePayload messagePayload = new MessagePayload();
		messagePayload.setPayloadBytes(messageData);
		serviceMessage.setPayload(messagePayload);

		serviceMessage.setRouting(new FloodRouting(homeNode()));

		try {
			ioChannels.sendServiceCommandsChannel.write(serviceMessage.toWireBytes());
		} catch (Exception e) {
			logger.log(Level.WARNING,
					"Internal error: cannot convert {0} to bytes, message cannot be pushed onto the bus",
					FeedMessage.class.getName());
			logger.log(Level.WARNING, "Internal error: ", e);
		}
	}

	/**
	 * @see fabric.core.io.ICallback#cancelCallback(java.lang.Object)
	 */
	@Override
	public void cancelCallback(Object arg1) {

		/* Unused */
	}

	/**
	 * @see fabric.bus.IBusIO#nodeName()
	 */
	@Override
	public String nodeName() {

		return homeNode();
	}

	/**
	 * @see fabric.bus.IBusIO#connectNeighbour(java.lang.String)
	 */
	@Override
	public NeighbourChannels connectNeighbour(String neighbour) {

		NeighbourChannels neighbourChannels = null;
		NodeDescriptor nodeDescriptor = createDescriptor(neighbour);

		while (neighbourChannels == null && nodeDescriptor != null) {

			/* Get the the existing Fabric connection to the node */
			neighbourChannels = neighbourChannelsTable.get(nodeDescriptor);

			/* If we don't currently have a connection to the neighbour... */
			if (neighbourChannels == null) {

				logger.log(Level.FINER, "Attempting to connect to potential new neighbour {0}", neighbour);

				/* Connect to the neighbour */
				neighbourChannels = connectNeighbour(nodeDescriptor, fabricCommandsBusTemplate, fabricFeedsBusTemplate,
						fabricRegistryBusTemplate);

				/* If the connection was successful... */
				if (neighbourChannels != null) {

					logger.log(Level.FINER, "Connected to new neighbour: {0}", neighbour);
					neighbourChannelsTable.put(nodeDescriptor, neighbourChannels);

				} else {

					/* Mark this nodeDescriptor as unavailable */
					FabricRegistry.getNodeNeighbourFactory(true).markUnavailable(homeNode(), nodeDescriptor);
					/*
					 * Move onto next possible nodeDescriptor, previous one should be marked unavailable and not
					 * returned.
					 */
					nodeDescriptor = createDescriptor(neighbour);
				}

				/*
				 * Reset all static neighbours to available - nothing else currently does this and we should retry them
				 * again next time.
				 */
				FabricRegistry.getNodeNeighbourFactory(true).markStaticNeighboursAsAvailable(homeNode());
			}
		}

		if (nodeDescriptor == null) {
			logger.log(Level.WARNING, "Could not connect to potential new neighbour \"{0}\"; node details not found",
					neighbour);
		}

		return neighbourChannels;
	}

	/**
	 * Determines the node descriptor for the specified node name.
	 * <p>
	 * The method will determine the network interface to be used based upon the order given in the
	 * <code>ConfigProperties.NODE_INTERFACES</code> configuration property.
	 * </p>
	 * 
	 * @param neighbourId
	 *            the Fabric name of the node to which a connection is being made.
	 * 
	 * @return the descriptor.
	 */
	private NodeDescriptor createDescriptor(String neighbourId) {

		NodeDescriptor nodeDescriptor = null;

		/* Establish possible Neighbour Entries from Node_Neighbours */
		NodeNeighbour[] neighbours = FabricRegistry.getNodeNeighbourFactory(true).getAvailableNeighboursEntries(
				homeNode(), neighbourId);

		String[] interfaces = config()
				.getProperty(ConfigProperties.NODE_INTERFACES, NodeDescriptor.loopbackInterface()).split(",");

		int i = 0;
		// Loop in order of local interfaces configured
		while (nodeDescriptor == null && i < interfaces.length) {
			String localInterface = interfaces[i];
			// Loop through neighbours for a match
			int j = 0;
			while (nodeDescriptor == null && j < neighbours.length) {
				NodeNeighbour neighbour = neighbours[j];
				if (neighbour.getNodeInterface().equals(localInterface)) {
					NodeIpMapping nodeIPMapping = neighbour.getIpMappingForNeighbour();
					nodeDescriptor = new NodeDescriptor(nodeIPMapping);
				}
				j++;
			}
			i++;
		}
		return nodeDescriptor;
	}

	/**
	 * Connect to a specific neighbouring Fabric node.
	 * 
	 * @param neighbourDescriptor
	 *            the neighbour to which a connection is to be made.
	 * 
	 * @param fabricCommandsBusTemplate
	 *            the template topic for Fabric Manager commands to the neighbour.
	 * 
	 * @param fabricFeedsBusTemplate
	 *            the template topic for the Fabric message bus connection to the neighbour.
	 * 
	 * @param fabricRegistryBusTemplate
	 *            the template topic for the Fabric registry bus connection to the neighbour.
	 * 
	 * @return the connection to the new neighbour.
	 */
	private NeighbourChannels connectNeighbour(NodeDescriptor neighbourDescriptor, String fabricCommandsBusTemplate,
			String fabricFeedsBusTemplate, String fabricRegistryBusTemplate) {

		/* To hold the connection object for the new neighbour */
		NeighbourChannels newNeighbour = null;

		try {

			/* Connect to the node and open the channels */

			SharedEndPoint neighbourEndPoint = connectNode(neighbourDescriptor);

			if (neighbourEndPoint != null) {

				/* Register to receive notifications about connectivity issues with this end point */
				neighbourEndPoint.register(this);

				/* Open the core set of channels for this node */
				newNeighbour = new NeighbourChannels(neighbourDescriptor, neighbourEndPoint, fabricCommandsBusTemplate,
						fabricFeedsBusTemplate, fabricRegistryBusTemplate, this);

			}

		} catch (Exception e) {

			logger.log(Level.WARNING, "Cannot connect to Fabric neighbour node {0}: {1}", new Object[] {
					neighbourDescriptor, LogUtil.stackTrace(e)});

		}

		return newNeighbour;
	}

	/**
	 * @see fabric.bus.IBusIO#neighbourChannels(java.lang.String)
	 */
	@Override
	public NeighbourChannels neighbourChannels(String id) {

		return neighbourChannelsTable.get(id);
	}

	/**
	 * @see fabric.bus.IBusIO#connectedNeighbours()
	 */
	@Override
	public NodeDescriptor[] connectedNeighbours() {

		Set<NodeDescriptor> neighbours = neighbourChannelsTable.keySet();
		return neighbours.toArray(new NodeDescriptor[0]);

	}

	/**
	 * @see fabric.bus.IBusIO#disconnectNeighbour(java.lang.String)
	 */
	@Override
	public void disconnectNeighbour(String id) throws UnsupportedOperationException, IOException {

		HashMap<NodeDescriptor, NeighbourChannels> neighbourChannelsTableCopy = (HashMap<NodeDescriptor, NeighbourChannels>) neighbourChannelsTable
				.clone();

		for (NodeDescriptor nodeDescriptor : neighbourChannelsTableCopy.keySet()) {
			if (nodeDescriptor.name().equals(id)) {
				disconnectNeighbour(nodeDescriptor, false);
			}
		}
	}

	/**
	 * @see fabric.bus.IBusIO#disconnectNeighbour(fabric.session.NodeDescriptor, boolean)
	 */
	@Override
	public NeighbourChannels disconnectNeighbour(NodeDescriptor nodeDescriptor, boolean doRetry)
			throws UnsupportedOperationException, IOException {

		NeighbourChannels currentChannels = neighbourChannelsTable.remove(nodeDescriptor);
		NeighbourChannels newChannels = null;

		if (currentChannels != null) {
			currentChannels.closeChannels();
			disconnectNode(nodeDescriptor);
		}

		if (doRetry) {
			newChannels = connectNeighbour(nodeDescriptor.name());
		}

		return newChannels;
	}

	/**
	 * @see fabric.bus.IBusIO#sendRawMessage(java.lang.String, byte[], boolean)
	 */
	@Override
	public void sendRawMessage(String fullTopic, byte[] messageData, boolean isReplay) throws IOException {

		/* Package the incoming resource message as a Fabric feed message */
		IFeedMessage message = wrapRawMessage(messageData, isReplay);
		message.metaSetTopic(fullTopic);

		/* Republish the message onto the Fabric */
		byte[] fabricMessageBytes = null;

		try {

			fabricMessageBytes = message.toWireBytes();

		} catch (Exception e) {

			logger.log(Level.WARNING,
					"Internal error: cannot convert {0} to bytes, message cannot be pushed onto the bus: {1}",
					new Object[] {FeedMessage.class.getName(), LogUtil.stackTrace(e)});

		}

		ioChannels.receiveBusChannel.write(fabricMessageBytes, new OutputTopic(ioChannels.receiveBus.name() + '/'
				+ message.metaGetFeedDescriptor()));

	}

	/**
	 * @see fabric.bus.IBusIO#sendServiceMessage(fabric.bus.messages.IServiceMessage, java.lang.String)
	 */
	@Override
	public void sendServiceMessage(IServiceMessage message, String node) throws Exception {

		sendServiceMessage(message, new String[] {node});

	}

	/**
	 * @see fabric.bus.IBusIO#sendServiceMessage(fabric.bus.messages.IServiceMessage, java.lang.String[])
	 */
	@Override
	public void sendServiceMessage(IServiceMessage message, String[] nodes) throws Exception {

		/* For each node... */
		for (int n = 0; nodes != null && n < nodes.length; n++) {

			if (nodes[n].equalsIgnoreCase(homeNode())) {
				ioChannels.sendCommandsChannel.write(message.toWireBytes());
			} else {
				/* Get the connection to the neighbour */
				NeighbourChannels nodeConnection = connectNeighbour(nodes[n]);

				/* If we have a connection to the neighbour... */
				if (nodeConnection != null) {

					/* Forward the message */
					nodeConnection.commandBusChannel().write(message.toWireBytes());

				} else {

					logger.log(Level.WARNING, "No connection to node {0}, cannot send message", new Object[] {nodes[n]});

				}
			}
		}
	}

	/**
	 * @see fabric.bus.IBusIO#sendFeedMessage(java.lang.String, java.lang.String, fabric.bus.messages.IFeedMessage,
	 *      fabric.core.io.MessageQoS)
	 */
	@Override
	public void sendFeedMessage(String node, String feedTopic, IFeedMessage message, MessageQoS qos) throws Exception {

		/* Get the connection to the neighbour */
		NeighbourChannels nodeConnection = connectNeighbour(node);

		/* If we have a connection to the neighbour... */
		if (nodeConnection != null) {

			logger.log(Level.FINEST, "Sending feed {0} message to node {1}", new Object[] {feedTopic,
					nodeConnection.neighbourDescriptor()});
			String fullTopic = nodeConnection.outboundFeedBus().name() + '/' + feedTopic;
			nodeConnection.feedBusChannel().write(message.toWireBytes(), new OutputTopic(fullTopic));

		} else {

			logger.log(Level.WARNING, "No connection to node {0}, cannot send feed message", node);

		}
	}

	/**
	 * @see fabric.bus.IBusIO#deliverFeedMessage(java.lang.String, fabric.bus.messages.IFeedMessage,
	 *      fabric.bus.feeds.impl.SubscriptionRecord, fabric.core.io.MessageQoS)
	 */
	@Override
	public void deliverFeedMessage(String feedTopic, IFeedMessage message, SubscriptionRecord subscription,
			MessageQoS qos) throws Exception {

		String fullTopic = ioChannels.sendLocalSubscription.name() + '/' + subscription.actor() + '/'
				+ subscription.actorPlatform() + '/' + subscription.feed().task() + '/' + feedTopic;

		logger.log(Level.FINEST, "Delivering feed {0} message to client {1}, task {2} using topic {3}", new Object[] {
				subscription.feed(), subscription.actor(), subscription.feed().task(), fullTopic});

		ioChannels.sendLocalSubscriptionChannel.write(message.toWireBytes(), new OutputTopic(fullTopic));

	}

	/**
	 * @see fabric.bus.IBusIO#ioChannels()
	 */
	@Override
	public BusIOChannels ioChannels() {

		return ioChannels;
	}

	/**
	 * Gets the current handler for Fabric messages.
	 * 
	 * @return the current handler.
	 */
	public BusMessageHandler getBusMessageHandler() {

		return messageHandler;
	}

	/**
	 * Sets the handler for Fabric messages.
	 * 
	 * @param messageHandler
	 *            the handler.
	 */
	public void setBusMessageHandler(BusMessageHandler messageHandler) {

		this.messageHandler = messageHandler;
	}

	/**
	 * @see fabric.core.io.IEndPointCallback#endPointConnected(fabric.core.io.EndPoint)
	 */
	@Override
	public void endPointConnected(EndPoint ep) {
		/* No action required */
	}

	/**
	 * @see fabric.core.io.IEndPointCallback#endPointDisconnected(fabric.core.io.EndPoint)
	 */
	@Override
	public void endPointDisconnected(EndPoint ep) {
		/* No action required */
	}

	/**
	 * @see fabric.core.io.IEndPointCallback#endPointReconnected(fabric.core.io.EndPoint)
	 */
	@Override
	public void endPointReconnected(EndPoint ep) {
		/* No action required */
	}

	/**
	 * @see fabric.core.io.IEndPointCallback#endPointClosed(fabric.core.io.EndPoint)
	 */
	@Override
	public void endPointClosed(EndPoint ep) {
		/* No action required */
	}

	/**
	 * @see fabric.core.io.IEndPointCallback#endPointLost(fabric.core.io.EndPoint)
	 */
	@Override
	public void endPointLost(EndPoint ep) {
		try {
			NodeDescriptor nodeDescriptor = null;
			/* Clean up the end point */
			if (ep instanceof SharedEndPoint) {
				SharedEndPoint sep = (SharedEndPoint) ep;
				disconnectNeighbour(nodeDescriptor, false);
				nodeDescriptor = new NodeDescriptor(sep.node(), sep.nodeInterface(), sep.ipAddress(), sep.ipPort()) ;
				//Mark this Neighbour Node Descriptor as Unavailable.
				FabricRegistry.getNodeNeighbourFactory(true).markUnavailable(homeNode(), nodeDescriptor );
			}
			else if (ep instanceof MqttEndPoint) {
				MqttEndPoint mep = (MqttEndPoint) ep;
				MqttConfig mqttConfig = (MqttConfig)mep.getConfig();
				//Retrive nodeId and nodeInterface for this mqtt endpoint
				NodeIpMapping[] nodeIpMappings = FabricRegistry.getNodeIpMappingFactory(true).getMappings("ip='" + mqttConfig.getIPHost() + "' and port =" + mqttConfig.getIPPort() );
				for (int i = 0; i < nodeIpMappings.length; i++) {
					nodeDescriptor = new NodeDescriptor(nodeIpMappings[i].getNodeId(), nodeIpMappings[i].getNodeInterface(), nodeIpMappings[i].getIpAddress(), nodeIpMappings[i].getPort()) ;
					disconnectNeighbour(nodeDescriptor, false);								
					//Mark this Neighbour Node Descriptor as Unavailable.
					FabricRegistry.getNodeNeighbourFactory(true).markUnavailable(homeNode(), nodeDescriptor );					
				}
			}
		} catch (UnsupportedOperationException | IOException e) {
			e.printStackTrace();
		}
	}
}
