/*
 * (C) Copyright IBM Corp. 2009, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.services.impl;

import java.util.logging.Logger;

import fabric.FabricBus;
import fabric.bus.plugins.IPluginConfig;
import fabric.bus.services.IBusService;

/**
 * Base class for Fabric services, providing core functionality.
 * 
 */
public abstract class BusService extends FabricBus implements IBusService {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

	/*
	 * Class fields
	 */

	/** The service configuration. */
	private IPluginConfig config = null;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 */
	public BusService() {

		super(Logger.getLogger("fabric.bus.services"));

	}

	/**
	 * Constructs a new instance.
	 */
	public BusService(Logger logger) {

		super(logger);

	}

	/**
	 * @see fabric.bus.services.IService#initService(fabric.bus.plugins.IPluginConfig)
	 */
	@Override
	public void initService(IPluginConfig config) {

		this.config = config;

	}

	/**
	 * @see fabric.bus.services.IService#serviceConfig()
	 */
	@Override
	public IPluginConfig serviceConfig() {

		return config;

	}

}