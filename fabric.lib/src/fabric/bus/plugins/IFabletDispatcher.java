/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins;

import fabric.bus.IBusServices;

/**
 * Interface for Fabric message plug-in dispatchers.
 */
public interface IFabletDispatcher extends IPluginDispatcher {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/*
	 * Interface methods
	 */

	/**
	 * Answers the interface to Fabric management services.
	 * 
	 * @return the accessor for Fabric management services.
	 */
	public IBusServices getFabricServices();

	/**
	 * Sets the interface to Fabric management services.
	 * 
	 * @param busServices
	 *            the accessor for Fabric management services.
	 */
	public void setFabricServices(IBusServices busServices);

}
