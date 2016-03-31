/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins;

import fabric.bus.IBusServices;

/**
 * Interface representing configuration information for a Fabric Manager message plug-in.
 */
public interface IFabletConfig extends IPluginConfig {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

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