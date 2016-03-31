/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins.impl;

import fabric.bus.IBusServices;
import fabric.bus.plugins.IFabletConfig;

/**
 * Class representing configuration information for a Fabric Manager message plug-in.
 */
public class FabletConfig extends PluginConfig implements IFabletConfig {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/*
	 * Class fields
	 */

	/** Interface to Fabric management services. */
	protected IBusServices busServices = null;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 */
	public FabletConfig() {
	}

	/**
	 * @see fabric.bus.plugins.IFabletConfig#getFabricServices()
	 */
	@Override
	public IBusServices getFabricServices() {
		return busServices;
	}

	/**
	 * @see fabric.bus.plugins.IFabletConfig#setFabricServices(fabric.bus.IBusServices)
	 */
	@Override
	public void setFabricServices(IBusServices busServices) {
		this.busServices = busServices;
	}
}
