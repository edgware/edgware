/*
 * (C) Copyright IBM Corp. 2007, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.bus.IBusServices;
import fabric.bus.plugins.IFabletConfig;
import fabric.bus.plugins.IFabletDispatcher;
import fabric.bus.plugins.IFabletHandler;
import fabric.bus.plugins.IPluginConfig;
import fabric.bus.plugins.IPluginHandler;
import fabric.registry.FabricPlugin;

/**
 * Base class for Fabric plug-in dispatchers.
 */
public class FabletDispatcher extends PluginDispatcher implements IFabletDispatcher {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2012";

	/*
	 * Class fields
	 */

	/** The interface to Fabric management services. */
	private IBusServices busServices = null;

	/*
	 * Class methods
	 */

	public FabletDispatcher(Logger logger) {

		super(logger);
	}

	public FabletDispatcher() {

		super(Logger.getLogger("fabric.bus.plugins"));
	}

	/**
	 * @see fabric.bus.plugins.IDispatcher#initPluginConfig()
	 */
	@Override
	public IPluginConfig initPluginConfig() {

		IFabletConfig config = new FabletConfig();
		config.setFabricServices(busServices);
		return config;

	}

	/**
	 * Loads and registers a list of Fablet plug-ins.
	 * 
	 * @param plugins
	 *            the list of plug-ins to load.
	 * 
	 * @param busServices
	 *            the interface to management services.
	 * 
	 * @return the plug-in dispatcher responsible for managing the plug-ins.
	 */
	public static IFabletDispatcher fabletFactory(String homeNode, FabricPlugin[] plugins,
			IBusServices busServices) {

		Logger myLogger = Logger.getLogger("fabric.bus.plugins");

		/* Get the dispatcher for this task */
		IFabletDispatcher dispatcher = new FabletDispatcher();
		dispatcher.setFabricServices(busServices);

		/* While there are more plug-ins... */
		for (int p = 0; p < plugins.length; p++) {

			/* Create and initialize the plug-in */
			myLogger.log(Level.INFO, "Starting fablet: {0} [{1}]", new Object[] {plugins[p].getName(),
					plugins[p].getFamilyName()});

			IPluginConfig pluginConfig = dispatcher.initPluginConfig();
			pluginConfig.setName(plugins[p].getName());
			pluginConfig.setArguments(plugins[p].getArguments());
			pluginConfig.setFamilyName(plugins[p].getFamilyName());
			pluginConfig.setFamily(dispatcher.family(plugins[p].getFamilyName()));
			pluginConfig.setNode(homeNode);
			pluginConfig.setDescription(plugins[p].getDescription());
			pluginConfig.setMetricManager(busServices.metrics());

			IPluginHandler plugin = dispatcher.initPluginHandler(pluginConfig);

			plugin.start();

			/* Register the new plug-in with the dispatcher */
			dispatcher.register(plugin);

		}

		return dispatcher;

	}

	/**
	 * @see fabric.bus.plugins.IDispatcher#initPluginHandler(fabric.bus.plugins.IPluginConfig)
	 */
	@Override
	public IPluginHandler initPluginHandler(IPluginConfig config) {

		IFabletHandler handler = new FabletHandler((IFabletConfig) config);
		return handler;

	}

	/**
	 * @see fabric.bus.plugins.IFabletDispatcher#getFabricServices()
	 */
	@Override
	public IBusServices getFabricServices() {

		return busServices;

	}

	/**
	 * @see fabric.bus.plugins.IFabletDispatcher#setFabricServices(fabric.bus.IBusServices)
	 */
	@Override
	public void setFabricServices(IBusServices busServices) {

		this.busServices = busServices;

	}
}
