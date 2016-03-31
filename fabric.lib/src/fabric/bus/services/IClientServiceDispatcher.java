/*
 * (C) Copyright IBM Corp. 2009, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.services;

import fabric.bus.BusIOChannels;
import fabric.client.FabricClient;

/**
 * Interface for Fabric client service dispatchers.
 */
public interface IClientServiceDispatcher extends IServiceDispatcher {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2014";

	/*
	 * Interface methods
	 */

	/**
	 * Sets the Fabric client associated with this client service.
	 * 
	 * @param client
	 *            the Fabric client.
	 */
	public void setFabricClient(FabricClient client);

	/**
	 * Sets the Fabric bus I/O channels available to this dispatcher.
	 * 
	 * @param ioChannels
	 *            the Fabric bus I/O channels available to this dispatcher.
	 */
	public void setIOChannels(BusIOChannels ioChannels);

}
