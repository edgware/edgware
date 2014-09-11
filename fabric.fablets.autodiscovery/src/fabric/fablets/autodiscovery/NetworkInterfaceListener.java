/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.fablets.autodiscovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.core.net.NetworkUtils;
import fabric.core.properties.ConfigProperties;
import fabric.registry.NodeIpMapping;

public class NetworkInterfaceListener implements Runnable {
	
	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";
	
	private final static String CLASS_NAME = NetworkInterfaceListener.class.getName();
	private final static String PACKAGE_NAME = NetworkInterfaceListener.class.getPackage().getName();
	private final static Logger logger = Logger.getLogger(PACKAGE_NAME);
	
	/** Object used to synchronize with the mapper main thread */
	private final Object threadSync = new Object();
	
	/** The IpMapping for my Node's interface to listen on. */
	private NodeIpMapping myIpMapping = null;
	@SuppressWarnings("unused")
	private AutoDiscoveryListenerFablet myFablet = null;
	/** Queue on which to put any received autodiscovery multicast requests. */
	private MulticastRequestQueue queue = null;

	/** Flag used to indicate when the main thread should terminate */
	private boolean isRunning = false;

	private boolean perfLoggingEnabled = false;
	private long perfTiming = 0;

	private boolean acceptall = false;
	
	/** Indicates if our configuration is valid */
	private boolean validConfiguration = true;
	
	/** The MulticastSocket we are listening for a discovery on. */
	MulticastSocket udpListenRequestSocket = null;

	/** Short-hand version of the subnet mask specified as the number of bits */
	private short subnetPrefix = 0;
	/** The subnet mask for the local node */
	private byte[] subnetMask = null;
	/** The network address for the local node - used for comparison with discovered hosts */
	private InetAddress networkAddress = null;

	/** IP-related information */
	/** Hosts to consider (For example they are in the same subnet (or on same machine) and therefore should be monitored */
	private List<String> hostsToConsider = new ArrayList<String>();
	/** Host which are not to be considered (for example they are not in the same subnet or on same machine) */
	private List<String> hostsNotToConsider = new ArrayList<String>();
	
	private int maxDatagramPacketBufferSize = 256;
	
	/**
	 * 
	 * 
	 * @param nodeIpMapping nodeIpMapping to listen for autodiscovery messages on 
	 * @param myFablet provides configuration information and access to Queue for processing discovery messages
	 */
	public NetworkInterfaceListener(NodeIpMapping nodeIpMapping, AutoDiscoveryListenerFablet myFablet, MulticastRequestQueue queue) {
		String METHOD_NAME = "constructor";
		logger.entering(CLASS_NAME, METHOD_NAME);

		myIpMapping = nodeIpMapping;
		this.myFablet = myFablet;
		this.queue = queue;
		perfLoggingEnabled = new Boolean(myFablet.config(ConfigProperties.REGISTRY_DISTRIBUTED_PERF_LOGGING));

		acceptall = new Boolean(myFablet.config(ConfigProperties.AUTO_DISCOVERY_ACCEPT_ALL, ConfigProperties.AUTO_DISCOVERY_ACCEPT_ALL_DEFAULT));
		String multicastGroup = myFablet.config(ConfigProperties.AUTO_DISCOVERY_GROUP, ConfigProperties.AUTO_DISCOVERY_GROUP_DEFAULT);
		int multicastPort = new Integer(myFablet.config(ConfigProperties.AUTO_DISCOVERY_PORT, ConfigProperties.AUTO_DISCOVERY_PORT_DEFAULT)).intValue();

		try {	

			InetAddress multicastGroupAddress = InetAddress.getByName(multicastGroup);
			InetAddress interfaceListenRequestAddr = InetAddress.getByName(myIpMapping.getIpAddress());

			// Cache the subnet mask and network address for this interface */
			subnetPrefix = NetworkUtils.getNetworkPrefixLength(interfaceListenRequestAddr);
			subnetMask = NetworkUtils.buildSubNetMaskFromNetworkPrefix(subnetPrefix);
			logger.log(Level.FINE, "Network prefix: {0} bytes", "" + subnetPrefix);

			// Compute local network address - for comparison later */
			networkAddress = NetworkUtils.computeNetworkAddress(interfaceListenRequestAddr, subnetMask);
			logger.log(Level.FINE, "Computed local network address: {0}", "" + networkAddress.getHostAddress());

			// Get the list of local addresses for this system */
			String[] myLocalAddresses = NetworkUtils.getLocalAddressStrings();
			// Add local addresses to the monitor list */
			hostsToConsider.addAll(Arrays.asList(myLocalAddresses));
			
			logger.log(Level.FINE, "Node \"{0}\" Setting up to Listen for autodiscovery requests on interface {1} : {2} using multicast Address {3}", new Object[] {
					myFablet.myNodeName, myIpMapping.getNodeInterface(), myIpMapping.getIpAddress(), multicastGroup});

			if (multicastGroupAddress.isMulticastAddress()) {

				// Listen on a specific interface using joinGroup
				NetworkInterface listeningInterface = NetworkInterface.getByInetAddress(interfaceListenRequestAddr);
				InetSocketAddress isa = new InetSocketAddress(multicastGroupAddress, multicastPort);
				udpListenRequestSocket = new MulticastSocket(multicastPort);
				udpListenRequestSocket.setNetworkInterface(listeningInterface);
				udpListenRequestSocket.setInterface(interfaceListenRequestAddr);
				logger.log(Level.FINE, "Listening interface \"{0}\", InetSocketAddress \"{1}\"", new Object[] {
						listeningInterface.getDisplayName(), isa.toString()});

				udpListenRequestSocket.joinGroup(isa, null);
				logger.log(Level.INFO, "Multicast listen socket created for: {0} | {1} - {2} | {3}", new Object[] {
						multicastGroupAddress.toString(),
						NetworkInterface.getByInetAddress(interfaceListenRequestAddr).toString(),
						udpListenRequestSocket.getInterface().toString(),
						udpListenRequestSocket.getLocalAddress().toString()});
			} else {

				logger.log(
						Level.WARNING,
						"Discovery attempted on invalid multicast address: {0}. Check the Fabric Registry for misconfiguration.",
						new Object[] {multicastGroupAddress.toString()});
			}
		} catch (UnknownHostException e) {
			// Couldn't connect to socket - probably in use, possibly below 1024 
			logger.log(
					Level.WARNING,
					"Could not create broadcast/multicast socket to send to group \"{0}\" from interface \"{1}:{2}\": {3}",
					new Object[] {multicastGroup, myIpMapping.getNodeInterface(), myIpMapping.getIpAddress(),
							e.getMessage()});
		} catch (SocketException e) {
			// Couldn't connect to socket - probably in use, possibly below 1024 
			logger.log(
					Level.WARNING,
					"Could not create broadcast/multicast socket to send to group \"{0}\" from interface \"{1}:{2}\": {3}",
					new Object[] {multicastGroup, myIpMapping.getNodeInterface(), myIpMapping.getIpAddress(),
							e.getMessage()});
		} catch (IOException e) {
			// Couldn't connect to socket - probably in use, possibly below 1024 
			logger.log(
					Level.WARNING,
					"Could not create broadcast/multicast socket to send to group \"{0}\" from interface \"{1}:{2}\": {3}",
					new Object[] {multicastGroup, myIpMapping.getNodeInterface(), myIpMapping.getIpAddress(),
							e.getMessage()});
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

		isRunning = true;
		if (!validConfiguration) {
			isRunning = false;
		}
		while (isRunning) {
			// int max_length = 8096;
			byte[] buf = new byte[maxDatagramPacketBufferSize];
			DatagramPacket p = new DatagramPacket(buf, buf.length);

			try {
				udpListenRequestSocket.receive(p);
				if (perfLoggingEnabled) {
					perfTiming = System.currentTimeMillis();
				}
				// Have we seen this host before? - if not, check it  */
				if ((!hostsToConsider.contains(p.getAddress().getHostAddress()))
						&& (!hostsNotToConsider.contains(p.getAddress().getHostAddress()))) {
					logger.log(Level.FINER, "Discovered address which needs to be checked: {0}", p.getAddress()
							.getHostAddress());

					if (!acceptall) {
						checkSubnet(p);
					}
					else {
						// add it to list of known acceptable hosts 
						hostsToConsider.add(p.getAddress().getHostAddress());
					}
				}

				/* if the host is one to consider, add the message to the processing queue */
				if (hostsToConsider.contains(p.getAddress().getHostAddress())) {
					logger.log(Level.FINEST, "Discovered address is in our subnet: {0}", p.getAddress()
							.getHostAddress());

					if (logger.isLoggable(Level.FINER)) {
						String listenPayload = new String(p.getData(), 0, p.getLength());
						logger.log(Level.FINER, "DataGram Message from {0} added to Queue : {1}", new Object[]{myIpMapping.getNodeInterface(), listenPayload} );
					}
					queue.addMsg(new MulticastNodeMessage(p, myIpMapping.getNodeInterface()));

				} else { /* not in the same subnet - ignore it */
					logger.log(Level.FINER,
							"Discovered address is NOT in our subnet and is being ignored: {0}", p.getAddress()
							.getHostAddress());
				}

			} catch (SocketTimeoutException e) {
				/**
				 * Socket timed out waiting for a packet to arrive. This is expected, simple means a request has
				 * not arrived.
				 */
				logger.log(Level.FINEST, "Timed out waiting for autodiscovery request");

			} catch (IOException e) {

				logger.log(Level.WARNING, "Exception receiving UDP packet from socket: ", e);
			}
			if (perfLoggingEnabled) {
				perfTiming = System.currentTimeMillis() - perfTiming;
				String listenPayload = new String(p.getData(), 0, p.getLength());
				logger.log(Level.FINER, "PERF : Processed message {0} in {1} milliseconds.", new Object[]{listenPayload, perfTiming} );
			}
		}
		udpListenRequestSocket.close();
		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

	/**
	 * Checks incoming datagram source IP address to determine whether it is in the same subnet that the current Fabric
	 * Manager is monitoring.
	 * 
	 * @param p
	 *            the packet whose IP address needs to be checked.
	 * @throws UnknownHostException
	 *             if the network address of the packet cannot be determined.
	 */
	private void checkSubnet(DatagramPacket p) throws UnknownHostException {

		if (subnetPrefix < 32) {
			// Compute the source network //
			InetAddress foreignNetworkAddress = NetworkUtils.computeNetworkAddress(p.getAddress(), subnetMask);
			logger.log(Level.FINER, "Computed network address: {0}", foreignNetworkAddress.getHostAddress());

			// are the network addresses the same? 
			if (foreignNetworkAddress.getHostAddress().equals(networkAddress.getHostAddress())) {
				logger.log(Level.FINER, "Host is in the same subnet.");

				// add it to list of known acceptable hosts 
				hostsToConsider.add(p.getAddress().getHostAddress());

			} else {
				logger.log(Level.FINER, "Host is in a different subnet and will be ignored.");
				hostsNotToConsider.add(p.getAddress().getHostAddress());
			}
		} else { /* subnet mask of 255.255.255.255 - unknown territory */
			/* default to allowing discovery of all hosts */
			hostsToConsider.add(p.getAddress().getHostAddress());
		}
	}

	
	public void close() {
		String METHOD_NAME = "close";
		logger.entering(CLASS_NAME, METHOD_NAME);
		/* Tell the main thread to stop... */
		isRunning = false;
		/* ...and wake it up */
		synchronized (threadSync) {
			threadSync.notify();
		}
		logger.exiting(CLASS_NAME, METHOD_NAME);
	}
		
}
