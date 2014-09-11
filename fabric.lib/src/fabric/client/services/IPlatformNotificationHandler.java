/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.client.services;

import fabric.bus.messages.IPlatformNotificationMessage;

/**
 * Interface for classes handling platform notification messages sent by the Fabric.
 */
public interface IPlatformNotificationHandler {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

	/*
	 * Interface constants
	 */

	/*
	 * Interface methods
	 */

	/**
	 * Handles a notification message.
	 * 
	 * @param message
	 *            the message.
	 */
	public void handlePlatformNotification(IPlatformNotificationMessage message);

}
