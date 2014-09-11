/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins;

/**
 * Interface for Fabric service dispatchers.
 */
public interface IDispatcher {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/*
	 * Interface methods
	 */

	/**
	 * Creates the configuration object for a plug-in.
	 * 
	 * @return the configuration object.
	 */
	public IPluginConfig initPluginConfig();

	/**
	 * Creates the handler for a plug-in.
	 * 
	 * @param config
	 *            the plug-in configuration.
	 * 
	 * @return the handler.
	 */
	public IPluginHandler initPluginHandler(IPluginConfig config);

	/**
	 * Gets the family management object for a family of plug-ins. The type of the data is defined by the family
	 * members.
	 * <p>
	 * If there is currently no family management object defined for a family then this method will initialize it.
	 * </p>
	 * 
	 * @param familyName
	 *            the plug-in family name.
	 * 
	 * @return the family management object for the plug-in family.
	 */
	public IFamily family(String familyName);

	/**
	 * Deletes the family management object for a family of plug-ins. The type of the data is defined by the family
	 * members.
	 * 
	 * @param familyName
	 *            the plug-in family name.
	 */
	public void removeFamily(String familyName);

	/**
	 * Closes this dispatcher, stopping all of the registered plug-ins.
	 */
	public void stopDispatcher();
}
