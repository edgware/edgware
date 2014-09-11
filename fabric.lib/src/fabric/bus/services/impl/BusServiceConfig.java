/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2007, 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.services.impl;

import fabric.MetricsManager;
import fabric.bus.IBusServices;
import fabric.bus.plugins.IFamily;
import fabric.bus.plugins.impl.PluginConfig;
import fabric.bus.services.IBusServiceConfig;

/**
 * Class representing configuration information for a Fabric Manager service.
 */
public class BusServiceConfig extends PluginConfig implements IBusServiceConfig {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2009";

	/*
	 * Class fields
	 */

	/** The Fabric Manager I/O handler. */
	protected IBusServices busServices = null;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 */
	public BusServiceConfig() {
	}

	/**
	 * Constructs a new instance.
	 * 
	 * @param name
	 *            the name of the plug-in class.
	 * 
	 * @param arguments
	 *            the plug-in's initialization data.
	 * 
	 * @param familyName
	 *            the plug-in's family name.
	 * 
	 * @param family
	 *            the plug-in family's family management object.
	 * 
	 * @param node
	 *            the name of the processing node.
	 * 
	 * @param busServices
	 *            the Fabric Manager I/O handler.
	 * 
	 * @param description
	 *            the plug-in's description.
	 * 
	 * @param metrics
	 *            list to hold the metrics recorded by this instance of the plug-in.
	 */
	public BusServiceConfig(String name, String arguments, String familyName, IFamily family, String node,
			IBusServices busServices, String description, MetricsManager metrics) {

		super(name, arguments, familyName, family, node, description, metrics);
		init(busServices);

	}

	/**
	 * Constructs a new instance from an existing instance.
	 * 
	 * @param sourceConfig
	 *            an existing handler configuration instance.
	 */
	public BusServiceConfig(BusServiceConfig sourceConfig) {

		super(sourceConfig.name, sourceConfig.arguments, sourceConfig.familyName, sourceConfig.family,
				sourceConfig.node, sourceConfig.description, sourceConfig.metrics);
		init(sourceConfig.busServices);

	}

	/**
	 * Initializes a new instance.
	 * 
	 * @param busIO
	 *            the handler for Fabric Manager I/O.
	 */
	private void init(IBusServices busServices) {

		this.busServices = busServices;

	}

	/**
	 * @see fabric.bus.services.IBusServiceConfig#getFabricServices()
	 */
	@Override
	public IBusServices getFabricServices() {

		return busServices;

	}

	/**
	 * @see fabric.bus.services.IBusServiceConfig#setFabricServices(fabric.bus.IBusServices)
	 */
	@Override
	public void setFabricServices(IBusServices busServices) {

		this.busServices = busServices;

	}
}
