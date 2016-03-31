/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.services.impl;

import fabric.bus.IBusServices;
import fabric.bus.plugins.IPluginConfig;
import fabric.bus.services.IBusServiceConfig;
import fabric.bus.services.IBusServiceDispatcher;

/**
 * Fabric bus service dispatcher.
 */
public class BusServiceDispatcher extends ServiceDispatcher implements IBusServiceDispatcher {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/*
	 * Class fields
	 */

	/** The interface to Fabric management services. */
	private IBusServices busServices = null;

	/*
	 * Class methods
	 */

	/**
	 * @see fabric.bus.services.IBusServiceDispatcher#getBusServices()
	 */
	@Override
	public IBusServices getBusServices() {

		return busServices;

	}

	/**
	 * @see fabric.bus.services.IBusServiceDispatcher#setBusServices(fabric.bus.IBusServices)
	 */
	@Override
	public void setBusServices(IBusServices busServices) {

		this.busServices = busServices;

	}

	/**
	 * @see fabric.bus.services.impl.ServiceDispatcher#initPluginConfig()
	 */
	@Override
	public IPluginConfig initPluginConfig() {

		IBusServiceConfig config = new BusServiceConfig();
		config.setFabricServices(busServices);
		return config;

	}
}
