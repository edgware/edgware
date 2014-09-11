/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2007, 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins;

import fabric.bus.messages.IFabricMessage;

/**
 * Interface implemented by Fabric manager policy-enabled plug-ins.
 */
public interface IPlugin {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2009";

	/*
	 * Interface methods
	 */

	/**
	 * Initializes this plug-in instance.
	 * 
	 * @param pluginConfig
	 *            the configuration information for this plug-in.
	 */
	void startPlugin(IPluginConfig pluginConfig);

	/**
	 * Stops this plug-in instance.
	 */
	void stopPlugin();

	/**
	 * Passes a control message to the plug-in.
	 * 
	 * @param message
	 *            the control message
	 */
	public void handleControlMessage(IFabricMessage message);

}
