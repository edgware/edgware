/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import fabric.registry.exception.RegistryQueryException;

public interface SystemPluginFactory extends Factory {
	
	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/**
	 * Instantiates a SystemPlugin registry object.
	 * 
	 * @param nodeId
	 * @param name
	 * @param family
	 * @param pluginType
	 * @param description
	 * @param arguments
	 * @return
	 */
	public SystemPlugin createSystemPlugin(String nodeId, String name, String family, String pluginType, String description, String arguments);
	
	/**
	 * 
	 * @return
	 */
	public SystemPlugin[] getAllSystemPlugins();
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public SystemPlugin[] getSystemPluginsByNode(String id);	

	/**
	 * 
	 * @param predicateQuery
	 * @return
	 * @throws RegistryQueryException
	 */
	public SystemPlugin[] getSystemPlugins(String predicateQuery) throws RegistryQueryException;
	
}
