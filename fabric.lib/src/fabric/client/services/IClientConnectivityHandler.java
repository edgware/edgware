/*
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.client.services;

import fabric.bus.messages.IServiceMessage;

/**
 * Interface for classes handling client notification messages sent by the Fabric.
 */
public interface IClientConnectivityHandler {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

	/*
	 * Interface methods
	 */

	/**
	 * Handles:
	 * <ul>
	 * <li><code>IClientNotificationMessage</code> notifications received in response to Fabric connection requests.</li>
	 * <li><code>IConnectionMessage</code> home node connected/disconnected messages.</li>
	 * </ul>
	 * 
	 * @param message
	 *            the connectivity message.
	 */
	public void handleNodeConnectivity(IServiceMessage message);

}
