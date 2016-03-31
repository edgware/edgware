/*
 * (C) Copyright IBM Corp. 2010
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.client.services;

/**
 * Interface for classes providing Fabric notification services to clients.
 */
public interface IClientNotificationServices {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010";

	/*
	 * Interface constants
	 */

	/*
	 * Interface methods
	 */

	/**
	 * Registers a client handler Fabric notification messages received for the specified correlation ID.
	 * 
	 * @param correlationID
	 *            the correlation ID.
	 * 
	 * @param handler
	 *            the notification handler.
	 * 
	 * @return the previously registered handler, or <code>null</code> if there was not one.
	 */
	public IClientNotificationHandler registerNotificationHandler(String correlationID,
			IClientNotificationHandler handler);

	/**
	 * De-registers a client handler to handle Fabric notification messages received for the specified correlation ID.
	 * 
	 * @param correlationID
	 *            the correlation ID.
	 * 
	 * @return the previously registered handler, or <code>null</code> if there was not one.
	 */
	public IClientNotificationHandler deregisterNotificationHandler(String correlationID);
}
