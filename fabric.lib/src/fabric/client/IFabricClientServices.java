/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2010
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.client;

import fabric.client.services.IClientNotificationServices;
import fabric.client.services.IHomeNodeConnectivityCallback;

/**
 * Interface for classes supporting Fabric client operations.
 */
public interface IFabricClientServices extends IClientNotificationServices, IHomeNodeConnectivityCallback {

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
	public IHomeNodeConnectivityCallback registerHomeNodeConnectivityCallback(IHomeNodeConnectivityCallback callback);

}
