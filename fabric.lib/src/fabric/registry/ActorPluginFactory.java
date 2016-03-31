/*
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import fabric.registry.exception.RegistryQueryException;

public interface ActorPluginFactory extends Factory {
	
	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/**
	 * Instantiates an ActorPlugin registry object.
	 * 
	 * @param nodeId
	 * @param taskId
	 * @param actorId
	 * @param name
	 * @param family
	 * @param pluginType
	 * @param ordinal
	 * @param description
	 * @param arguments
	 * @param platformId
	 * @param systemId
	 * @param feedId
	 * @return
	 */
	public ActorPlugin createActorPlugin(String nodeId, String taskId, String actorId, String name, String family, String pluginType, int ordinal, String description, String arguments, String platformId, String systemId, String feedId);

	/**
	 * 
	 * @return
	 */
	public ActorPlugin[] getAllActorPlugins();

	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public ActorPlugin[] getActorPluginsByNode(String id);

	/**
	 * 
	 * @param predicateQuery
	 * @return
	 * @throws RegistryQueryException
	 */
	public ActorPlugin[] getActorPlugins(String predicateQuery) throws RegistryQueryException;

	
}
