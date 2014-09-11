/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2007, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins.impl;

import fabric.bus.plugins.IPluginConfig;
import fabric.bus.plugins.IPluginHandler;

/**
 * Class representing Fabric plug-ins.
 */
public abstract class PluginHandler implements IPluginHandler {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2012";

	/*
	 * Class fields
	 */

	/** The configuration information for this plug-in */
	private IPluginConfig pluginConfig = null;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 * 
	 * @param pluginConfig
	 *            the configuration information for this plug-in.
	 */
	public PluginHandler(IPluginConfig pluginConfig) {

		/* Record the configuration for use later */
		this.pluginConfig = pluginConfig;

	}

	/**
	 * @see fabric.bus.plugins.IPluginHandler#pluginConfig()
	 */
	@Override
	public IPluginConfig pluginConfig() {

		return pluginConfig;

	}
}
