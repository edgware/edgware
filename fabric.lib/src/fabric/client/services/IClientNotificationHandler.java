/*
 * (C) Copyright IBM Corp. 2010
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.client.services;

import fabric.bus.messages.IClientNotificationMessage;

/**
 * Interface for classes handling client notification messages sent by the Fabric.
 */
public interface IClientNotificationHandler {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010";

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
	public void handleNotification(IClientNotificationMessage message);

}
