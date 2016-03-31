/*
 * (C) Copyright IBM Corp. 2007, 2008
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins;

/**
 * Interface representing Fabric plug-ins.
 */
public interface IPluginHandler {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2008";

	/*
	 * Interface methods
	 */

	/**
	 * Instantiate and initialize the plug-in.
	 */
	public void start();

	/**
	 * Close the plug-in.
	 */
	public void stop();

	/**
	 * Gets the configuration information for this plug-in.
	 * 
	 * @return the configuration information for this plug-in.
	 */
	public IPluginConfig pluginConfig();

}