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
import fabric.bus.plugins.IFabletConfig;
import fabric.bus.plugins.IFabletHandler;
import fabric.bus.plugins.IFabletPlugin;
import fabric.core.logging.LogUtil;

/**
 * Class representing the Fablet plug-ins.
 * 
 */
public class FabletHandler extends PluginHandler implements IFabletHandler {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2012";

	/*
	 * Class fields
	 */

	private Logger logger;
	/** The configuration information for this plug-in */
	private IFabletConfig pluginConfig = null;

	/** To hold the plug-in instance */
	private IFabletPlugin fablet = null;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 * 
	 * @param pluginConfig
	 *            the configuration information for this plug-in.
	 */
	public FabletHandler(IFabletConfig pluginConfig) {

		super(pluginConfig);

		this.logger = Logger.getLogger("fabric.bus.plugins");
		/* Record the configuration for use later */
		this.pluginConfig = pluginConfig;

	}

	/**
	 * @see fabric.bus.plugins.IPluginHandler#start()
	 */
	@Override
	public void start() {

		try {

			logger.log(Level.FINE, "Starting fablet handler {0}", pluginConfig.getName());

			/* Instantiate the class */
			fablet = (IFabletPlugin) Fabric.instantiate(pluginConfig.getName());

		} catch (Throwable t) {

			logger.log(Level.WARNING, "Failed to create plugin:\n", t);

		}

		if (fablet != null) {

			try {
				/* Invoke the initialization method */
				fablet.startPlugin(pluginConfig);

				/* If the plug-in runs on its own thread... */
				if (fablet instanceof Runnable) {

					/* Start it now */
					Thread pluginThread = new Thread(fablet);
					pluginThread.start();

				}

			} catch (Throwable t) {

				logger.log(Level.WARNING, "Plugin initialization failed for class {0}, arguments \"{1}\": {2}",
						new Object[] {pluginConfig.getName(), pluginConfig.getArguments(), LogUtil.stackTrace(t)});

			}
		}
	}

	/**
	 * @see fabric.bus.plugins.IPluginHandler#stop()
	 */
	@Override
	public void stop() {

		if (fablet != null) {
			/* Invoke the initialization method */
			try {

				/* Tell the plug-in instance to close */
				fablet.stopPlugin();

			} catch (Throwable t) {

				logger.log(Level.WARNING, "Failed to stop plugin", t);

			}
		}
	}

}
