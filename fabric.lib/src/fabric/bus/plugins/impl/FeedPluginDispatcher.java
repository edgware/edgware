/*
 * (C) Copyright IBM Corp. 2007, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins.impl;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.TaskServiceDescriptor;
import fabric.bus.IBusServices;
import fabric.bus.messages.IFeedMessage;
import fabric.bus.plugins.IFeedPlugin;
import fabric.bus.plugins.IFeedPluginConfig;
import fabric.bus.plugins.IFeedPluginDispatcher;
import fabric.bus.plugins.IFeedPluginHandler;
import fabric.bus.plugins.IPluginConfig;
import fabric.bus.plugins.IPluginHandler;
import fabric.bus.routing.IRouting;
import fabric.registry.NodePlugin;
import fabric.registry.TaskPlugin;

/**
 * Base class for Fabric plug-in dispatchers.
 */
public class FeedPluginDispatcher extends FabletDispatcher implements IFeedPluginDispatcher {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2012";

	/*
	 * Class methods
	 */

	public FeedPluginDispatcher() {

		super(Logger.getLogger("fabric.bus.plugins"));
	}

	public FeedPluginDispatcher(Logger logger) {

		super(logger);
	}

	/**
	 * Loads and registers a list of node plug-ins.
	 * 
	 * @param plugins
	 *            the list of plug-ins to load.
	 * 
	 * @param busServices
	 *            interface to Fabric management services.
	 * 
	 * @return the plug-in dispatcher responsible for managing the plug-ins.
	 */
	public static FeedPluginDispatcher nodePluginFactory(String homeNode, NodePlugin[] plugins, IBusServices busServices) {

		Logger myLogger = Logger.getLogger("fabric.bus.plugins");

		/* Get the dispatcher for this task */
		FeedPluginDispatcher dispatcher = new FeedPluginDispatcher();
		dispatcher.setFabricServices(busServices);

		/* While there are more plug-ins... */
		for (int p = 0; p < plugins.length; p++) {

			/* Create and initialize the plug-in */
			myLogger.log(Level.INFO, "Starting node plugin: {0} [{1}]", new Object[] {plugins[p].getName(),
					plugins[p].getFamilyName()});

			IFeedPluginConfig pluginConfig = (IFeedPluginConfig) dispatcher.initPluginConfig();
			pluginConfig.setName(plugins[p].getName());
			pluginConfig.setArguments(plugins[p].getArguments());
			pluginConfig.setFamilyName(plugins[p].getFamilyName());
			pluginConfig.setFamily(dispatcher.family(plugins[p].getFamilyName()));
			pluginConfig.setNode(homeNode);
			pluginConfig.setDescription(plugins[p].getDescription());
			pluginConfig.setMetricManager(busServices.metrics());

			IFeedPluginHandler plugin = (IFeedPluginHandler) dispatcher.initPluginHandler(pluginConfig);

			plugin.start();

			/* Register the new plug-in with the dispatcher */
			dispatcher.register(plugin);
		}

		return dispatcher;

	}

	/**
	 * Loads and registers a list of task plug-ins.
	 * 
	 * @param plugins
	 *            the list of plug-ins to load.
	 * 
	 * @param busServices
	 *            interface to Fabric management services.
	 * 
	 * @param taskFeed
	 *            the feed descriptor.
	 * 
	 * @return the plug-in dispatcher responsible for managing the plug-ins.
	 */
	public static FeedPluginDispatcher taskPluginFactory(String homeNode, TaskPlugin[] plugins,
			IBusServices busServices, TaskServiceDescriptor taskFeed) {

		Logger myLogger = Logger.getLogger("fabric.bus.plugins");

		/* Get the dispatcher for this task */
		FeedPluginDispatcher dispatcher = new FeedPluginDispatcher();
		dispatcher.setFabricServices(busServices);

		/* While there are more plug-ins... */
		for (int p = 0; p < plugins.length; p++) {

			/* Create and initialize the plug-in */
			myLogger.log(Level.INFO, "Starting task plugin: {0} [{1}]", new Object[] {plugins[p].getName(),
					plugins[p].getFamilyName()});

			IFeedPluginConfig pluginConfig = (IFeedPluginConfig) dispatcher.initPluginConfig();
			pluginConfig.setName(plugins[p].getName());
			pluginConfig.setArguments(plugins[p].getArguments());
			pluginConfig.setFamilyName(plugins[p].getFamilyName());
			pluginConfig.setFamily(dispatcher.family(plugins[p].getFamilyName()));
			pluginConfig.setNode(homeNode);
			pluginConfig.setDescription(plugins[p].getDescription());
			pluginConfig.setMetricManager(busServices.metrics());
			pluginConfig.setTask(taskFeed.task());
			pluginConfig.setFeed(taskFeed);

			IFeedPluginHandler plugin = (IFeedPluginHandler) dispatcher.initPluginHandler(pluginConfig);

			plugin.start();

			/* Register the new plug-in with the dispatcher */
			dispatcher.register(plugin);
		}

		return dispatcher;

	}

	/**
	 * Loads and registers a list of actor plug-ins.
	 * 
	 * @param plugins
	 *            the list of plug-ins to load.
	 * 
	 * @param busServices
	 *            interface to Fabric management services.
	 * 
	 * @param taskFeed
	 *            the feed descriptor.
	 * 
	 * @param actor
	 *            the actor.
	 * 
	 * @return the plug-in dispatcher responsible for managing the plug-ins.
	 */
	public static FeedPluginDispatcher actorPluginFactory(String homeNode, TaskPlugin[] plugins,
			IBusServices busServices, TaskServiceDescriptor taskFeed, String actor) {

		Logger myLogger = Logger.getLogger("fabric.bus.plugins");

		/* Get the dispatcher for this task */
		FeedPluginDispatcher dispatcher = new FeedPluginDispatcher();
		dispatcher.setFabricServices(busServices);

		/* While there are more plug-ins... */
		for (int p = 0; p < plugins.length; p++) {

			/* Create and initialize the plug-in */
			myLogger.log(Level.INFO, "Starting actor plugin: {0} [{1}]", new Object[] {plugins[p].getName(),
					plugins[p].getFamilyName()});

			IFeedPluginConfig pluginConfig = (IFeedPluginConfig) dispatcher.initPluginConfig();
			pluginConfig.setName(plugins[p].getName());
			pluginConfig.setArguments(plugins[p].getArguments());
			pluginConfig.setFamilyName(plugins[p].getFamilyName());
			pluginConfig.setFamily(dispatcher.family(plugins[p].getFamilyName()));
			pluginConfig.setNode(homeNode);
			pluginConfig.setDescription(plugins[p].getDescription());
			pluginConfig.setMetricManager(busServices.metrics());
			pluginConfig.setTask(taskFeed.task());
			pluginConfig.setFeed(taskFeed);
			pluginConfig.setActor(actor);

			IFeedPluginHandler plugin = (IFeedPluginHandler) dispatcher.initPluginHandler(pluginConfig);

			plugin.start();

			/* Register the new plug-in with the dispatcher */
			dispatcher.register(plugin);

		}

		return dispatcher;

	}

	/**
	 * @see fabric.bus.plugins.IFeedPluginDispatcher#dispatch(fabric.bus.messages.IFeedMessage, int)
	 */
	@Override
	public int dispatch(IFeedMessage message, int pluginAction) throws Exception {

		return dispatch(message, null, pluginAction);

	}

	/**
	 * @see fabric.bus.plugins.IFeedPluginDispatcher#dispatch(fabric.bus.messages.IFeedMessage,
	 *      fabric.bus.routing.IRouting, int)
	 */
	@Override
	public int dispatch(IFeedMessage message, IRouting routing, int pluginAction) throws Exception {

		/* Invoke each of the plug-ins for this dispatcher */

		Iterator<IPluginHandler> p = plugins().iterator();

		int c = 0;
		/* While the message has not been dropped and there are more plug-ins... */
		while (p.hasNext() && pluginAction != IFeedPlugin.ACTION_DISCARD_IMMEDIATE) {

			/* Get the next plug-in */
			IFeedPluginHandler plugin = (IFeedPluginHandler) p.next();
			IFeedPluginConfig pluginConfig = (IFeedPluginConfig) plugin.pluginConfig();

			logger.log(Level.FINE, "Invoking plug-in {0} (node {1}, task {2}, actor {3})", new Object[] {
					pluginConfig.getName(), pluginConfig.getNode(), pluginConfig.getTask(), pluginConfig.getActor()});

			/* Invoke the handler */
			pluginAction = plugin.run(message, routing, pluginAction);
			c++;
		}

		return pluginAction;
	}

	/**
	 * @see fabric.bus.plugins.IDispatcher#initPluginConfig()
	 */
	@Override
	public IPluginConfig initPluginConfig() {

		IFeedPluginConfig config = new FeedPluginConfig();
		config.setFabricServices(getFabricServices());
		return config;

	}

	/**
	 * @see fabric.bus.plugins.IDispatcher#initPluginHandler(fabric.bus.plugins.IPluginConfig)
	 */
	@Override
	public IPluginHandler initPluginHandler(IPluginConfig config) {

		IFeedPluginHandler handler = new FeedPluginHandler((IFeedPluginConfig) config);
		return handler;

	}
}
