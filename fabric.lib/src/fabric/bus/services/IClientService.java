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
 * Interface for classes that implement Fabric client services, i.e. services that execute in a Fabric client
 * application.
 * <p>
 * Classes implementing this interface are short lived, i.e. they are instantiated to handle a single message.
 * </p>
 */
public interface IClientService extends IService {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2014";

	/**
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
	 * Sets the Fabric I/O channels available to this service.
	 * 
	 * @param ioChannels
	 *            the I/O channels.
	 */
	public void setIOChannels(BusIOChannels ioChannels);

}
