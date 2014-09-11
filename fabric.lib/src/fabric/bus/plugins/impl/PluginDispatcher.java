/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2007, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.bus.plugins.IPluginDispatcher;
import fabric.bus.plugins.IPluginHandler;

/**
 * Base class for Fabric plug-in dispatchers.
 */
public abstract class PluginDispatcher extends Dispatcher implements IPluginDispatcher {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2012";

	/*
	 * Class fields
	 */

	/** The list of plug-ins registered with this dispatcher. */
	private ArrayList<IPluginHandler> plugins = new ArrayList<IPluginHandler>();

	/*
	 * Class methods
	 */

	public PluginDispatcher(Logger logger) {

		super(logger);
	}

	/**
	 * @see fabric.bus.plugins.IPluginDispatcher#register(fabric.bus.plugins.impl.PluginHandler)
	 */
	@Override
	public void register(IPluginHandler plugin) {

		plugins.add(plugin);
	}

	/**
	 * Answers the list of plug-ins registered with this dispatcher.
	 * 
	 * @return the plug-ins.
	 */
	public ArrayList<IPluginHandler> plugins() {

		return plugins;
	}

	/**
	 * @see fabric.bus.plugins.IDispatcher#stopDispatcher()
	 */
	@Override
	public void stopDispatcher() {

		/* Close each of the plug-ins registered with this dispatcher */

		Iterator<IPluginHandler> p = plugins.iterator();

		while (p.hasNext()) {

			IPluginHandler plugin = p.next();

			logger.log(Level.FINER, "Stopping plugin {0}", plugin.pluginConfig().getName());
			plugin.stop();

		}
	}
}
