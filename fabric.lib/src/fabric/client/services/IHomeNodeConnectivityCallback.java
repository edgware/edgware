/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2010
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.client.services;

import fabric.bus.messages.IServiceMessage;

/**
 * Interface for classes handling service messages received from the Fabric that are not otherwise handled.
 */
public interface IHomeNodeConnectivityCallback {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010";

	/*
	 * Interface constants
	 */

	/*
	 * Interface methods
	 */

	/**
	 * Handles a message indicating a change in the connectivity status of the client's home node.
	 * 
	 * @param message
	 *            the message.
	 */
	public void homeNodeConnectivity(IServiceMessage message);

}
