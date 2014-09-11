/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2007, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.Fabric;
import fabric.FabricMetric;
import fabric.bus.messages.IFeedMessage;
import fabric.bus.plugins.IFeedPlugin;
import fabric.bus.plugins.IFeedPluginConfig;
import fabric.bus.plugins.IFeedPluginHandler;
import fabric.bus.routing.IRouting;
import fabric.core.logging.LogUtil;

/**
 * Class representing the Fablet plug-ins.
 * 
 */
public class FeedPluginHandler extends PluginHandler implements IFeedPluginHandler {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2012";

	/*
	 * Class fields
	 */

	/** The configuration information for this plug-in */
	private IFeedPluginConfig pluginConfig = null;

	/** To hold the plug-in instance */
	private IFeedPlugin feedPlugin = null;

	private Logger logger;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 * 
	 * @param pluginConfig
	 *            the configuration information for this plug-in.
	 */
	public FeedPluginHandler(IFeedPluginConfig pluginConfig) {

		super(pluginConfig);

		this.logger = Logger.getLogger("fabric.bus.plugins.FeedPluginHandler");

		/* Record the configuration for use later */
		this.pluginConfig = pluginConfig;

	}

	/**
	 * @see fabric.bus.plugins.IPluginHandler#start()
	 */
	@Override
	public void start() {

		try {
			logger.log(Level.INFO, "Starting feed plugin handler {0}", pluginConfig.getName());
			/* Instantiate the class */
			feedPlugin = (IFeedPlugin) Fabric.instantiate(pluginConfig.getName());

		} catch (Throwable t) {

			logger.log(Level.WARNING, "Failed to create plugin", t);
		}

		if (feedPlugin != null) {

			try {

				/* Invoke the initialization method */
				feedPlugin.startPlugin(pluginConfig);

			} catch (Throwable t) {

				logger.log(Level.WARNING, "Plugin initialization failed for class {0}, arguments \"{1}\": {2}",
						new Object[] {pluginConfig.getName(), pluginConfig.getArguments(), LogUtil.stackTrace(t)});

			}
		}
	}

	/**
	 * @see fabric.bus.plugins.IFeedPluginHandler#run(fabric.bus.messages.IFeedMessage, fabric.bus.routing.IRouting,
	 *      int)
	 */
	@Override
	public int run(IFeedMessage message, IRouting routing, int pluginAction) {

		if (feedPlugin != null) {

			/* Instrumentation */
			FabricMetric metric = null;

			try {

				if (Fabric.doInstrument()) {
					metric = new FabricMetric(routing.startNode(), pluginConfig.getTask(), pluginConfig.getActor(),
							message.metaGetFeedDescriptor(), message.getUID(), message.getOrdinal(), message.toXML()
									.toBytes(), pluginConfig.getName());
					pluginConfig.getMetricManager().startTiming(metric, FabricMetric.EVENT_PLUGIN_PROCESSING_START);
				}

				/* Invoke the plug-in's message handler */
				pluginAction = feedPlugin.handleFeedMessage(message, routing, pluginAction);
				logger.log(Level.FINE, "Plugin message handler invoked");

			} catch (Throwable t) {

				logger.log(Level.WARNING, "Invocation of plug-in failed", t);

			} finally {

				if (Fabric.doInstrument()) {
					pluginConfig.getMetricManager().endTiming(metric, FabricMetric.EVENT_PLUGIN_PROCESSING_STOP);
				}

			}
		}

		return pluginAction;

	}

	/**
	 * @see fabric.bus.plugins.IPluginHandler#stop()
	 */
	@Override
	public void stop() {

		if (feedPlugin != null) {

			/* Invoke the initialization method */
			try {
				/* Tell the plug-in instance to close */
				feedPlugin.stopPlugin();

			} catch (Throwable t) {

				logger.log(Level.WARNING, "Failed to stop plugin", t);

			}
		}
	}

}
