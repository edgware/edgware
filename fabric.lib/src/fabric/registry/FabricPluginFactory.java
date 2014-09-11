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

public interface FabricPluginFactory extends Factory {
	
	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/**
	 * Instantiates a FabricPlugin registry object.
	 * 
	 * @param nodeId
	 * @param name
	 * @param description
	 * @param arguments
	 * @return
	 */
	public FabricPlugin createFabricPlugin(String nodeId, String name, String family, String description, String arguments);

	/**
	 * 
	 * @return
	 */
	public FabricPlugin[] getAllFabricPlugins();
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public FabricPlugin[] getFabricPluginsByNode(String id);
	
	/**
	 * 
	 * @param predicateQuery
	 * @return
	 * @throws RegistryQueryException
	 */
	public FabricPlugin[] getFabricPlugins(String predicateQuery) throws RegistryQueryException;
	
}
