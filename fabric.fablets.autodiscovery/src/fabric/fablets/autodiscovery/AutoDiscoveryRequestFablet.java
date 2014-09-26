/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2010, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.fablets.autodiscovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.FabricBus;
import fabric.bus.messages.IFabricMessage;
import fabric.bus.plugins.IFabletConfig;
import fabric.bus.plugins.IFabletPlugin;
import fabric.bus.plugins.IPluginConfig;
import fabric.core.io.ICallback;
import fabric.core.io.Message;
import fabric.core.logging.LogUtil;
import fabric.core.properties.ConfigProperties;
import fabric.registry.FabricRegistry;
import fabric.registry.Node;
import fabric.registry.NodeIpMapping;

/**
 * Fablet class to broadcast/multicast autodiscovery requests on a UDP Socket.
 * 
 * These messages take the form
 * 
 * N!:&ltNODE_TYPE>:&ltNODE_NAME>:&ltNODE_INTERFACE>:&ltNODE_PORT>:&ltNODE_AFFILITAION>:&ltSEQUENCE_NUMBER>:&
 * ltBROADCAST_TIME>
 * 
 * <ul>
 * <li>&ltNODE_TYPE> indicates the Node Type of this node</li>
 * <li>&ltNODE_NAME> The name of the Node</li>
 * <li>&ltNODE_INTERFACE> The interface on the Node from which this AutoDiscovery Broadcast Originated</li>
 * <li>&ltNODE_PORT> The port on the Node for MQTT connections</li>
 * <li>&ltNODE_AFFILIATION> The affiliation as recorded in NODES table of Registry for this node</li>
 * <li>&ltSEQUENCE_NUMBER> increments with every broadcast issued</li>
 * <li>&ltBROADCAST_TIME> is broadcast time expressed as milliseconds since the time 00:00:00 UTC on January 1, 1970</li>
 * </ul>
 */
public class AutoDiscoveryRequestFablet extends FabricBus implements IFabletPlugin, ICallback {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010, 2014";

	private final static String CLASS_NAME = AutoDiscoveryRequestFablet.class.getName();
	private final static String PACKAGE_NAME = AutoDiscoveryRequestFablet.class.getPackage().getName();

	private final static Logger logger = Logger.getLogger(PACKAGE_NAME);

	/** The configuration object for this instance. */
	private IFabletConfig fabletConfig = null;

	/** Object used to synchronize with the mapper main thread. */
	private final Object threadSync = new Object();

	/** Flag used to indicate when the main thread should terminate. */
	private boolean isRunning = false;

	/**
	 * Holds a Map from all discoveryRequestStrings to their MulticastSocket. Each Node can have multiple interfaces so
	 * there can be more than one discoveryRequestString with corresponding MulticastSocket
	 */
	private Map<String, MulticastSocket> requestToSocket = new HashMap<String, MulticastSocket>();
	private Map<String, NodeIpMapping> requestToNodeIpMapping = new HashMap<String, NodeIpMapping>();
	
	private List<String> failedRequests = new ArrayList<String>();

	/** Frequency with which to issue the AutoDiscovery broadcast. */
	private int autoDiscoveryFrequency;
	/** Port on which to issue the AutoDiscovery broadcast. */
	private int multicastPort;
	/** Broadcast group for the AutoDiscovery Broadcast. */
	private InetAddress multicastGroupAddress = null;
	private int broadcastMessageTTL = 0;
	/** Indicates if the broadcast of AutoDiscovery messages is enabled. */
	private boolean autoDiscoveryRequestEnabled = false;
	/** NodeName for AutdoDiscovery message. */
	private String myNodeName;

	private boolean perfLoggingEnabled = false;

	/** Sequence Number for Autodiscovery message. */
	private int requestOrdinal = 0;

	/*
	 * Class methods
	 */
	public AutoDiscoveryRequestFablet() {
	}

	/**
	 * @see fabric.services.fabricmanager.plugins.FabricPlugin#startPlugin(fabric.services.fabricmanager.plugins.PluginConfig)
	 */
	@Override
	public void startPlugin(IPluginConfig pluginConfig) {

		String METHOD_NAME = "startPlugin";
		logger.entering(CLASS_NAME, METHOD_NAME);

		fabletConfig = (IFabletConfig) pluginConfig;
		String autoDiscoveryRequest = config(ConfigProperties.AUTO_DISCOVERY_REQUEST,
				ConfigProperties.AUTO_DISCOVERY_REQUEST_DEFAULT);
		if (autoDiscoveryRequest.equalsIgnoreCase("enabled")) {
			autoDiscoveryRequestEnabled = true;
		}
		myNodeName = fabletConfig.getNode();
		perfLoggingEnabled = new Boolean(this.config(ConfigProperties.REGISTRY_DISTRIBUTED_PERF_LOGGING));

		if (autoDiscoveryRequestEnabled) {

			/* Establish discovery configuration */
			multicastPort = new Integer(config(ConfigProperties.AUTO_DISCOVERY_PORT,
					ConfigProperties.AUTO_DISCOVERY_PORT_DEFAULT)).intValue();
			autoDiscoveryFrequency = new Integer(config(ConfigProperties.AUTO_DISCOVERY_FREQUENCY,
					ConfigProperties.AUTO_DISCOVERY_FREQUENCY_DEFAULT)).intValue();
			String multicastGroup = config(ConfigProperties.AUTO_DISCOVERY_GROUP,
					ConfigProperties.AUTO_DISCOVERY_GROUP_DEFAULT);

			/* Establish the MulticastAddress, disabling autodiscovery if any problems occur */
			try {

				multicastGroupAddress = InetAddress.getByName(multicastGroup);

				if (!multicastGroupAddress.isMulticastAddress()) {
					logger.log(
							Level.WARNING,
							"Discovery attempted on invalid multicast address: {0}. Check the Fabric Registry for misconfiguration. Discovery Broadcast disabled.",
							multicastGroupAddress.toString());
					autoDiscoveryRequestEnabled = false;
					logger.exiting(CLASS_NAME, METHOD_NAME);
					return;
				}

			} catch (UnknownHostException e) {

				logger.log(
						Level.WARNING,
						"Cannot establish the Multicast Group Address for AutoDiscovery broadcasts, Group = {0}. Discovery Broacdcast disabled.",
						new Object[] {multicastGroup, LogUtil.stackTrace(e)});
				autoDiscoveryRequestEnabled = false;
				logger.exiting(CLASS_NAME, METHOD_NAME);
				return;

			}

			logger.log(Level.FINE,
					"Sending autodiscovery requests to the group: {0} on port {1} with Frequency {2} milliseconds",
					new Object[] {multicastGroup, multicastPort, autoDiscoveryFrequency});

			broadcastMessageTTL = new Integer(config(ConfigProperties.AUTO_DISCOVERY_TTL,
					ConfigProperties.AUTO_DISCOVERY_TTL_DEFAULT)).intValue();
			logger.log(Level.FINER, "Broadcast Message Time To Live = {0}", new Object[] {broadcastMessageTTL});

			openRequestSockets();

		} else {
			logger.log(Level.INFO, "Auto discovery requestor disabled");
		}
		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

	/**
	 * Multicast on the all configured external interface IP addresses for this node lookup should be a local Registry
	 * query
	 */
	private void openRequestSockets() {

		Node localNode = FabricRegistry.getNodeFactory(true).getNodeById(myNodeName);
		String nodeType = localNode.getTypeId();

		/* Obtain all IPMappings for this node */
		NodeIpMapping[] nodeIpMappings = FabricRegistry.getNodeIpMappingFactory(true).getAllMappingsForNode(myNodeName);

		/* For each IPMapping open a MulticastSocket and construct a request String in requestToSocketMap... */
		for (int i = 0; i < nodeIpMappings.length; i++) {

			NodeIpMapping nodeIpMapping = nodeIpMappings[i];
			String nodePort = Integer.toString(nodeIpMapping.getPort());
			String nodeInterface = nodeIpMapping.getNodeInterface();
			String nodeAffiliation = localNode.getAffiliation();
			String request = "N!:" + nodeType + ":" + myNodeName + ":" + nodeInterface + ":" + nodePort + ":"
					+ nodeAffiliation;
			openRequestSocket(request, nodeIpMapping);
			
		}
	}
	
	/**
	 * Opens the multicast socket
	 * 
	 * @param request message to be sent
	 * @param nodeIpMapping ipmapping information for opening multicast socket
	 */
	private void openRequestSocket(String request, NodeIpMapping nodeIpMapping) {
		InetAddress interfaceSendRequestAddr = null;

		try {

			interfaceSendRequestAddr = InetAddress.getByName(nodeIpMapping.getIpAddress());
			MulticastSocket udpSendRequestSocket;
			udpSendRequestSocket = new MulticastSocket();
			udpSendRequestSocket.setTimeToLive(broadcastMessageTTL);
			udpSendRequestSocket.setNetworkInterface(NetworkInterface.getByInetAddress(interfaceSendRequestAddr));
			udpSendRequestSocket.joinGroup(multicastGroupAddress);

			logger.log(Level.INFO, "Multicast send socket created for: {0} | {1} - {2} | {3}", new Object[] {
					multicastGroupAddress.toString(),
					NetworkInterface.getByInetAddress(interfaceSendRequestAddr).toString(),
					udpSendRequestSocket.getInterface().toString(),
					udpSendRequestSocket.getLocalAddress().toString()});

			requestToSocket.put(request, udpSendRequestSocket);
			requestToNodeIpMapping.put(request, nodeIpMapping);

		} catch (UnknownHostException e) {
			logger.log(Level.INFO,
					"Couldn't get an InetAddress for the IP {0} for interface {1} on node {2} : {3}", new Object[] {
					nodeIpMapping.getIpAddress(), nodeIpMapping.getNodeInterface(),
					nodeIpMapping.getNodeId(), LogUtil.stackTrace(e)});
			failedRequests.add(request);
		} catch (SocketException e) {
			logger.log(
					Level.WARNING,
					"Could not create broadcast/multicast socket to send to group \"{0}\" from interface \"{1}\": {2}",
					new Object[] {multicastGroupAddress.toString(), interfaceSendRequestAddr.toString(),
							LogUtil.stackTrace(e)});
			failedRequests.add(request);
		} catch (IOException e) {
			logger.log(
					Level.WARNING,
					"Could not create broadcast/multicast socket to send to group \"{0}\" from interface \"{1}\": {2}",
					new Object[] {multicastGroupAddress.toString(), interfaceSendRequestAddr.toString(),
							LogUtil.stackTrace(e)});
			failedRequests.add(request);
		}
	}

	/**
	 * @see fabric.services.fabricmanager.plugins.FabricPlugin#stopPlugin()
	 */
	@Override
	public void stopPlugin() {

		if (autoDiscoveryRequestEnabled) {

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

		closeRequestSockets();
	}

	/**
	 * Close all request sockets.
	 */
	private void closeRequestSockets() {

		for (Iterator<MulticastSocket> iterator = requestToSocket.values().iterator(); iterator.hasNext();) {
			MulticastSocket socket = iterator.next();
			socket.close();
		}

		requestToSocket.clear();
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		isRunning = true;
		String sendRequest = null;
		long timeOfBroadcast = 0;
		long timeTakenToBroadcast = 0;

		if (autoDiscoveryRequestEnabled) {

			while (isRunning) {

				if (perfLoggingEnabled) {
					timeTakenToBroadcast = System.currentTimeMillis();
				}

				//Try to reopen sockets for any failed requests
				List<String> oldFailedRequests = new ArrayList<String>();
				oldFailedRequests.addAll(failedRequests);
				failedRequests.clear();
				for (Iterator<String> iter = oldFailedRequests.iterator(); iter
						.hasNext();) {
					String request = (String) iter.next();
					NodeIpMapping nodeIpMapping = requestToNodeIpMapping.get(request);
					//Re-establish our Ip address in case we have switched networks
					String interfaceName = nodeIpMapping.getNodeInterface();
					try {
						NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);
						if (networkInterface != null) {
							Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
							while (addresses.hasMoreElements()) {
								InetAddress address = addresses.nextElement();
								if (address instanceof Inet4Address) {
									if (!address.getHostAddress().equals(nodeIpMapping.getIpAddress())) {
										nodeIpMapping.setIpAddress(address.getHostAddress());
										//Persist our ipaddress
										try {
											FabricRegistry.save(nodeIpMapping);
											requestToNodeIpMapping.put(request, nodeIpMapping);
										} catch (Exception e) {
											logger.log(Level.WARNING, "Cannot register IP mapping \"{0}\": {1}", new Object[] {
													nodeIpMapping.toString(), LogUtil.stackTrace(e)});
										}
									}
									break;
								}
							}
						}
						//re-establish a socket for multicast discovery messages
						openRequestSocket(request, nodeIpMapping);				
					} catch (SocketException e1) {
						failedRequests.add(request);
					}
					
				}
							
				/* For each interface's MulticastSocket... */
				for (Iterator<String> iterator = requestToSocket.keySet().iterator(); iterator.hasNext();) {

					/* Send a multicast discovery message */

					String request = (String) iterator.next();
					MulticastSocket udpSendRequestSocket = requestToSocket.get(request);
					if (requestOrdinal == Integer.MAX_VALUE) {
						requestOrdinal = 0;
					}
					requestOrdinal++;

					timeOfBroadcast = System.currentTimeMillis();
					sendRequest = request + ":" + Integer.toString(requestOrdinal) + ":"
							+ Long.toString(timeOfBroadcast);
					byte[] rbuf = sendRequest.getBytes();
					DatagramPacket p = new DatagramPacket(rbuf, rbuf.length, multicastGroupAddress, multicastPort);

					try {
						udpSendRequestSocket.send(p);
					}
					catch (IOException e) {
						/* Could not send packet */
						logger.log(Level.WARNING, "Could not send broadcast/multicast packet to group \"{0}\": {1}",
								new Object[] {multicastGroupAddress.getHostAddress(), e.getMessage()});

						/* Clean everything up and restart */
						udpSendRequestSocket.close();
						failedRequests.add(request);						
					}
				}

				for (Iterator<String> iter = failedRequests.iterator(); iter
						.hasNext();) {
					String request = (String) iter.next();
					requestToSocket.remove(request);					
				}

				/* Wait before we send the next Broadcasts */
				try {
					Thread.sleep(autoDiscoveryFrequency);
				} catch (InterruptedException e) {
					/* Ignore */
				}
			}

			if (perfLoggingEnabled) {
				timeTakenToBroadcast = System.currentTimeMillis() - timeTakenToBroadcast;
				logger.log(Level.FINE, "Time taken to Broadcast discovery on {0} interfaces was {1}", new Object[] {
						requestToSocket.size(), timeTakenToBroadcast});
			}

		} else {

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
	 * @see fabric.core.io.Callback#cancelCallback(java.lang.Object)
	 */
	@Override
	public void cancelCallback(Object arg1) {
		/* Nothing to do here */
	}

	/**
	 * @see fabric.core.io.Callback#handleMessage(fabric.core.io.Message)
	 */
	@Override
	public synchronized void handleMessage(Message message) {
		/* Never Used */
	}

	/**
	 * @see fabric.core.io.Callback#startCallback(java.lang.Object)
	 */
	@Override
	public void startCallback(Object arg1) {
		/* Nothing to do here */
	}

	/**
	 * @see fabric.services.fabricmanager.FabletPlugin#handleControlMessage(fabric
	 *      .services.fabricmanager.FabricMessage)
	 */
	@Override
	public void handleControlMessage(IFabricMessage message) {
		/* Nothing to do here */
	}
}
