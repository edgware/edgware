/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2007, 2008
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins;

/**
 * Interface for Fabric plug-in dispatchers.
 */
public interface IPluginDispatcher extends IDispatcher {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2008";

	/*
	 * Interface methods
	 */

	/**
	 * Registers a new plug-in with this dispatcher.
	 * 
	 * @param plugin
	 *            the plug-in to register.
	 */
	public void register(IPluginHandler plugin);

}
