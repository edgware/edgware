/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.fablets.autodiscovery;

import java.net.DatagramPacket;
import java.util.logging.Logger;

/**
 * This class represents a Multicast Autodiscovery message received while it awaits processing and putting onto an MQTT topic.
 */
public class MulticastMessage {
	
	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";
	
	@SuppressWarnings("unused")
	private final static String CLASS_NAME = MulticastMessage.class.getName();
	private final static String PACKAGE_NAME = MulticastMessage.class.getPackage().getName();
	@SuppressWarnings("unused")
	private final static Logger logger = Logger.getLogger(PACKAGE_NAME);

	/** The multicast packet recieved. */
	private DatagramPacket packet = null;
	/** The local Interface on which the packet was received */
	private String localInterface = null;

	public MulticastMessage(DatagramPacket p, String interfaceReceivedOn) {
		this.setPacket(p);
		this.setLocalInterface(interfaceReceivedOn);
	}

	public String getLocalInterface() {
		return localInterface;
	}

	public void setLocalInterface(String localInterface) {
		this.localInterface = localInterface;
	}

	public DatagramPacket getPacket() {
		return packet;
	}

	public void setPacket(DatagramPacket packet) {
		this.packet = packet;
	}
	
	public String toString() {
		return new String(packet.getData(), 0, packet.getLength()) + " from " + localInterface;
	}

	/** This method will unpack the multicast message, should be overridden by subclasses 
	 * 
	 * @return boolean indicating if the unpacking of the message was successful.
	 */
	public boolean unpack() {
		return false;
	}

}
