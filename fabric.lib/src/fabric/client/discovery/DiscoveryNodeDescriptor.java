/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2006, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.client.discovery;

import java.net.NetworkInterface;
import java.util.Comparator;

import fabric.session.NodeDescriptor;

/**
 * An extention of the NodeDescriptor class to include information about how long the node took to respond and which
 * network interface it was found on.
 */
public class DiscoveryNodeDescriptor extends NodeDescriptor {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2006, 2012";

	/**
	 * Time in milliseconds the node took to respond
	 */
	private long time;

	/**
	 * The NetworkInterface the broadcast was sent on
	 */
	private NetworkInterface iface;

	public DiscoveryNodeDescriptor(String id, String nodeInterface, String ipAddress, int port, long time,
			NetworkInterface iface) {
		super(id, nodeInterface, ipAddress, port);
		this.time = time;
		this.iface = iface;
	}

	/**
	 * Returns the time in milliseconds that the Fabric Manager took to respond to the broadcast
	 * 
	 * @return the time
	 */
	public long getResponseTime() {
		return time;
	}

	/**
	 * Returns the NetworkInterface that the broadcast that initiated the response was sent on
	 * 
	 * @return the network interface
	 */
	public NetworkInterface getIface() {
		return iface;
	}

	public static class ResponseTimeComparitor implements Comparator<DiscoveryNodeDescriptor> {

		@Override
		public int compare(DiscoveryNodeDescriptor arg0, DiscoveryNodeDescriptor arg1) {

			if (arg0.time > arg1.time) {
				return -1;
			} else if (arg0.time < arg1.time) {
				return 1;
			}
			return 0;
		}

	}

}
