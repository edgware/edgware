/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.fablets.autodiscovery;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple queue on to which discovery requests are placed for processing by the DiscoveryQueueProcessor.
 * 
 */
public class MulticastRequestQueue {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	@SuppressWarnings("unused")
	private final static String CLASS_NAME = NetworkInterfaceListener.class.getName();
	private final static String PACKAGE_NAME = NetworkInterfaceListener.class.getPackage().getName();
	private final static Logger logger = Logger.getLogger(PACKAGE_NAME);

	/** Our reference to the underlying Queue */
	private java.util.Vector<MulticastMessage> queue;
	/** Maximum depth for queue */
	private int maxQueueDepth = 1;

	public MulticastRequestQueue(int depth) {
		this.maxQueueDepth = depth;
		queue = new java.util.Vector<MulticastMessage>(maxQueueDepth);
	}

	/**
	 * Add a new discovery request to the queue.
	 * 
	 * @param autoDiscoveryMulticastMessage
	 *            - the DatagramPacket containing the request.
	 */
	protected synchronized void addMsg(MulticastMessage autoDiscoveryMulticastMessage) {

		if (queue.size() != maxQueueDepth) {
			queue.add(autoDiscoveryMulticastMessage);
			// notify all waiting threads
			synchronized (this) {
				this.notifyAll();
			}
		} else { // overflow!
			logger.log(Level.WARNING, "Overflow on discovery request queue - discarding message : {0}", new Object[]{autoDiscoveryMulticastMessage.toString()});
		}
		logger.log(Level.FINEST, "Added {0} to the queue", new Object[]{autoDiscoveryMulticastMessage.toString()});
	}

	/**
	 * Destructively get the next message packet from the queue.
	 * 
	 * @return the next available discovery message or null if the queue is empty.
	 */
	protected synchronized MulticastMessage nextMessage() {

		if (queue.size() == 0) {
			return null; /* nothing on the queue */
		}
		MulticastMessage response =  queue.remove(0);
		logger.log(Level.FINEST, "removed {0} from queue ", new Object[]{response.toString()});
		return response;
	}

	/**
	 * Check whether the queue contains messages.
	 * 
	 * @return true is the queue is not empty, false otherwise.
	 */
	protected boolean isNotEmpty() {

		return !queue.isEmpty();
	}

	/**
	 * The size of the queue.
	 * 
	 * @return the actual size of the queue (0 or greater).
	 */
	protected int size() {
		return queue.size();
	}
}

