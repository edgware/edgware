/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2007, 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.services;

import fabric.bus.IBusServices;
import fabric.bus.plugins.IPluginConfig;

/**
 * Interface representing configuration information for a Fabric Manager service plug-in.
 */
public interface IBusServiceConfig extends IPluginConfig {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2009";

	/*
	 * Interface methods
	 */

	/**
	 * Answers the interface to Fabric management services.
	 * 
	 * @return the Fabric management services accessor.
	 */
	public IBusServices getFabricServices();

	/**
	 * Sets the interface to Fabric management services.
	 * 
	 * @param busServices
	 *            the Fabric management services accessor.
	 */
	public void setFabricServices(IBusServices busServices);

}