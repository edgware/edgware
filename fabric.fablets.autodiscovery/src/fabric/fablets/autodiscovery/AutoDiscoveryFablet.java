/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2008, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.fablets.autodiscovery;

import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.FabricBus;
import fabric.bus.SharedChannel;
import fabric.bus.messages.IFabricMessage;
import fabric.bus.plugins.IFabletConfig;
import fabric.bus.plugins.IFabletPlugin;
import fabric.bus.plugins.IPluginConfig;
import fabric.core.io.ICallback;
import fabric.core.io.InputTopic;
import fabric.core.io.Message;
import fabric.core.logging.LogUtil;
import fabric.core.properties.ConfigProperties;
import fabric.registry.FabricRegistry;
import fabric.registry.NodeIpMapping;
import fabric.registry.NodeNeighbour;
import fabric.registry.Type;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.session.RegistryDescriptor;

/**
 * Fablet class to handle discovery configuration messages containing platform/service/feed definition information.
 * 
 * When configured, also looks on startup for a local INF file from which to load locally connected platforms into the
 * registry.
 * 
 */
public class AutoDiscoveryFablet extends FabricBus implements IFabletPlugin, ICallback {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2008, 2014";

	private final static String CLASS_NAME = AutoDiscoveryFablet.class.getName();
	private final static String PACKAGE_NAME = AutoDiscoveryFablet.class.getPackage().getName();

	private final static Logger logger = Logger.getLogger(PACKAGE_NAME);

	/** Home node name */
	private String myNodeName;

	/** The configuration object for this instance */
	@SuppressWarnings("unused")
	private IFabletConfig fabletConfig = null;

	/** The type of registry connection - centralised or gaian or distributed */
	private static String registryType = null;

	/** The topic on which discovery messages are published */
	private InputTopic topic;

	/** The channel on which messages will be received */
	private SharedChannel topicChannel = null;

	/** Object used to synchronize with the mapper main thread */
	private final Object threadSync = new Object();

	/** Flag used to indicate when the main thread should terminate */
	private boolean isRunning = false;

	private boolean perfLoggingEnabled = false;
	private long timeToProcessMessage = 0;

	public AutoDiscoveryFablet() {

	}

	/*
	 * (non-Javadoc)
	 * @see fabric.services.fabricmanager.plugins.FabricPlugin#startPlugin(fabric
	 * .services.fabricmanager.plugins.PluginConfig)
	 */
	@Override
	public void startPlugin(IPluginConfig pluginConfig) {
		String METHOD_NAME = "startPlugin";
		logger.entering(CLASS_NAME, METHOD_NAME);
		fabletConfig = (IFabletConfig) pluginConfig;
		myNodeName = homeNode();
		registryType = config(ConfigProperties.REGISTRY_TYPE);
		perfLoggingEnabled = new Boolean(this.config(ConfigProperties.REGISTRY_DISTRIBUTED_PERF_LOGGING));

		topic = new InputTopic(config(ConfigProperties.AUTO_DISCOVERY_TOPIC,
				ConfigProperties.AUTO_DISCOVERY_TOPIC_DEFAULT, homeNode()));

		// Empty registry of neighbours previously discovered
		String sql = "DELETE FROM FABRIC.NODE_NEIGHBOURS WHERE DiscoveredBy='" + CLASS_NAME + "'";
		logger.finest("Emptying the regsitry of previously discovered neighbours");
		try {
			FabricRegistry.runUpdates(new String[] {sql});
		} catch (PersistenceException e) {
			logger.log(Level.FINE, "Failed to empty the registry of previously discovered neighbours :", LogUtil
					.stackTrace(e));
		}
		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.services.fabricmanager.plugins.FabricPlugin#stopPlugin()
	 */
	@Override
	public void stopPlugin() {
		String METHOD_NAME = "stopPlugin";
		logger.entering(CLASS_NAME, METHOD_NAME);
		/* Tell the main thread to stop... */
		isRunning = false;

		/* ...and wake it up */
		synchronized (threadSync) {
			threadSync.notify();
		}

		if (topic != null) {
			try {
				homeNodeEndPoint().closeChannel(topicChannel, false);
			} catch (Exception e) {
				logger.log(Level.WARNING, "Closure of MQTT channel for topic \"{0}\" failed: {1}", new Object[] {topic,
						LogUtil.stackTrace(e)});
			}
		}
		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		String METHOD_NAME = "run";
		logger.entering(CLASS_NAME, METHOD_NAME);
		try {

			isRunning = true;
			try {
				topicChannel = homeNodeEndPoint().openInputChannel(topic, this);
			} catch (Exception e) {
				logger.log(Level.WARNING, "Failed to map topic \"{0}\": {1}", new Object[] {topic,
						LogUtil.stackTrace(e)});
			}

			while (isRunning) {

				try {
					synchronized (threadSync) {
						threadSync.wait();
					}
				} catch (InterruptedException e) {
					// Ignore
				}
			}
		} catch (Exception e1) {
			logger.log(Level.WARNING, "Plug-in \"{0}\" failed with exception: {1}", new Object[] {
					this.getClass().getName(), LogUtil.stackTrace(e1)});
		}
		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.core.io.Callback#cancelCallback(java.lang.Object)
	 */
	@Override
	public void cancelCallback(Object arg1) {

		// Nothing to do here

	}

	/*
	 * (non-Javadoc)
	 * @see fabric.core.io.Callback#handleMessage(fabric.core.io.Message)
	 */
	@Override
	public synchronized void handleMessage(Message message) {
		if (perfLoggingEnabled) {
			timeToProcessMessage = System.currentTimeMillis();
		}
		logger.log(Level.FINER, "Handling configuration message: {0}", message.toString());

		String messageTopic = (String) message.topic;
		/* If this is an autodiscovery config message... */
		if (messageTopic.indexOf(topic.name()) != -1) {
			String messageData = new String(message.data);
			String[] parts = messageData.split(",");
			// Expecting 8 pieces of data, supplied in "name=value" format
			if ((parts.length == 8) && checkForNameValuePairs(parts)) {

				if (parts[0].split("=")[0].equalsIgnoreCase("nt")) {
					processNodeDiscovery(parts);
				} else {
					// Unrecognised message - log a message and skip it
					logger.log(Level.WARNING, "Unrecognised message received on \"{0}\":\n{1}", new Object[] {
							messageTopic, messageData});
				}
			} else {

				// Unexpected message format - log a message and skip it
				logger.log(Level.WARNING, "Unexpected message received on \"{0}\":\n{1}", new Object[] {messageTopic,
						messageData});
			}
		}
		if (perfLoggingEnabled) {
			timeToProcessMessage = System.currentTimeMillis() - timeToProcessMessage;
			logger.log(Level.FINE, "Time taken to process message {0} was {1} milliseconds", new Object[] {
					new String(message.data), timeToProcessMessage});
		}
	}

	/**
	 * Registers the specified node, including IP address information, as well as updating the availability status of
	 * the node. If the specified node type does not exist, it will be created.
	 * 
	 * @param parts
	 *            - a String array containing all the individual tokens from the original message payload.
	 */
	private void processNodeDiscovery(String[] parts) {

		String nodeTypeId = parts[0].split("=")[1];
		String neighbourId = parts[1].split("=")[1];
		String neighbourInterface = parts[2].split("=")[1];
		String nodeIpAddress = parts[3].split("=")[1];
		String nodePort = parts[4].split("=")[1];
		String localInterface = parts[5].split("=")[1];
		String nodeStatusIndicator = parts[6].split("=")[1];
		String nodeAffiliation = parts[7].split("=")[1];

		logger.log(Level.FINER, "Node \"{0}\" has affiliation \"{1}\".", new Object[] {neighbourId, nodeAffiliation});

		if (!neighbourId.equals(myNodeName)) {
			String nodeAvailability = "UNKOWN";
			if (nodeStatusIndicator.equals("0")) {
				nodeAvailability = NodeNeighbour.UNAVAILABLE;
			} else if (nodeStatusIndicator.equals("1")) {
				nodeAvailability = NodeNeighbour.AVAILABLE;
			}

			// Create node_type entry and update IpMapping if it doesn't exist
			// only do this if not using a central registry
			if (!RegistryDescriptor.TYPE_SINGLETON.equalsIgnoreCase(registryType)) {
				logger.log(Level.FINE, "Update local registry with meta data for node {0}.", neighbourId);
				createNodeType(nodeTypeId);
				int port = new Integer(nodePort).intValue();
				updateIpMapping(neighbourId, neighbourInterface, nodeIpAddress, port);
			}
			updateNeighbourInformation(homeNode(), localInterface, neighbourId, neighbourInterface, nodeAvailability);
		}
	}

	/**
	 * Method will update the registry node_neighbours table, if the neighbour is no longer available it will be
	 * deleted, otherwise it will be added as available.
	 * 
	 * @param homeNode
	 * @param nodeInterface
	 * @param neighbourId
	 * @param neighbourInterface
	 * @param neighbourAvailability
	 */
	private void updateNeighbourInformation(String homeNode, String nodeInterface, String neighbourId,
			String neighbourInterface, String neighbourAvailability) {

		NodeNeighbour neighbour = FabricRegistry.getNodeNeighbourFactory(true).createNodeNeighbour(homeNode,
				nodeInterface, neighbourId, neighbourInterface, CLASS_NAME, neighbourAvailability, null, null, null);

		if (neighbourAvailability.equals(NodeNeighbour.AVAILABLE)) {
			try {
				FabricRegistry.save(neighbour);
				logger.log(Level.INFO, "Neighbour added: {0}.", neighbour);
			} catch (IncompleteObjectException e) {
				logger.log(Level.WARNING,
						"Failed to save neighbour {0} for node {1} - neighbour availability not updated:\n{2}",
						new Object[] {neighbourId, homeNode, e});
			}
		} else {

			// remove it
			FabricRegistry.delete(neighbour);
			logger.log(Level.INFO, "Neighbour removed: {0}, status: {1}", new Object[] {neighbour,
					neighbourAvailability});
		}
	}

	/**
	 * Updates the Node_ip_maping table with the ip information for the discovered node if this information is not
	 * currently present
	 * 
	 * @param nodeId
	 * @param nodeInterface
	 * @param nodeIpAddress
	 * @param port
	 */
	private void updateIpMapping(String nodeId, String nodeInterface, String nodeIpAddress, int port) {

		logger.log(Level.FINER, "Update ip mapping: Neighbour node: {0}, ipAddress: {1}, port: {2}", new Object[] {
				nodeId, nodeIpAddress, port});
		NodeIpMapping ipMapping = FabricRegistry.getNodeIpMappingFactory(true).createNodeIpMapping(nodeId,
				nodeInterface, nodeIpAddress, port);

		try {
			FabricRegistry.save(ipMapping);
		} catch (IncompleteObjectException e) {
			logger.log(Level.WARNING, "Failed to save ip mapping for node {0}.", new Object[] {nodeId});
		}
	}

	/**
	 * This method updated the NodeType table with the Type Id for the discovered Node if it is not already there
	 * 
	 * @param nodeTypeId
	 * @return
	 */
	private boolean createNodeType(String nodeTypeId) {

		boolean nodeTypeCreated = false;
		Type nodeType = FabricRegistry.getTypeFactory(true).getNodeType(nodeTypeId);
		if (nodeType == null) {
			nodeType = FabricRegistry.getTypeFactory(true).createNodeType(nodeTypeId, "Created by auto-discovery",
					null, null);
			try {
				nodeTypeCreated = FabricRegistry.save(nodeType);
			} catch (IncompleteObjectException e) {
				logger.log(Level.WARNING, "Failed to create node type {0}.", nodeTypeId);
			}
		} else {
			nodeTypeCreated = true;
		}
		return nodeTypeCreated;
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.core.io.Callback#startCallback(java.lang.Object)
	 */
	@Override
	public void startCallback(Object arg1) {
		/* Nothing to do here */
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
	 * Simple check to verify that data is supplied in "name=value" format
	 * 
	 * @param nameValuePairs
	 * @return
	 */
	private boolean checkForNameValuePairs(String[] nameValuePairs) {

		boolean correctDelimitersUsed = true;
		String[] temp = null;
		for (int i = 0; i < nameValuePairs.length; i++) {
			temp = nameValuePairs[i].split("=");
			if (temp.length != 2) {
				correctDelimitersUsed = false;
				break;
			}
		}
		return correctDelimitersUsed;
	}
}
