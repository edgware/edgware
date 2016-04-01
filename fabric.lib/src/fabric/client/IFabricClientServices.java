/*
 * (C) Copyright IBM Corp. 2010
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.client;

import fabric.client.services.IClientNotificationServices;
import fabric.client.services.ITopologyChange;

/**
 * Interface for classes supporting Fabric client operations.
 */
public interface IFabricClientServices extends IClientNotificationServices, ITopologyChange {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010";

	/*
	 * Interface methods
	 */

	/**
	 * Registers a callback to receive notifications that the client's home Fabric node has connected/disconnected.
	 * 
	 * @param callback
	 *            the callback.
	 */
	public ITopologyChange registerHomeNodeConnectivityCallback(ITopologyChange callback);

}
