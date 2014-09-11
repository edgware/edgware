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

public interface NodePluginFactory extends Factory {
	
	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/**
	 * 
	 * @param nodeId
	 * @param ordinal
	 * @param name
	 * @param description
	 * @param arguments
	 * @return
	 */
	public NodePlugin createNodePlugin(String nodeId, String name, String family, String pluginType, int ordinal, String description, String arguments);
	

	/**
	 * 
	 * @return
	 */
	public NodePlugin[] getAllNodePlugins();
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public NodePlugin[] getNodePluginsByNode(String id);
	
	/**
	 * 
	 * @param predicateQuery
	 * @return
	 * @throws RegistryQueryException
	 */
	public NodePlugin[] getNodePlugins(String predicateQuery) throws RegistryQueryException;
	
}
