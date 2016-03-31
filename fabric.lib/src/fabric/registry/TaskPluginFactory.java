/*
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import fabric.registry.exception.RegistryQueryException;

public interface TaskPluginFactory extends Factory {
	
	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/**
	 * 
	 * @param nodeId
	 * @param taskId
	 * @param ordinal
	 * @param name
	 * @param description
	 * @param arguments
	 * @param platformId
	 * @param systemId
	 * @param feedId
	 * @return
	 */
	public TaskPlugin createTaskPlugin(String nodeId, String taskId, String name, String family, String pluginType, int ordinal, String description, String arguments, String platformId, String systemId, String feedId);
	
	/**
	 * 
	 * @return
	 */
	public TaskPlugin[] getAllTaskPlugins();
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public TaskPlugin[] getTaskPluginsByNode(String id);
	
	/**
	 * 
	 * @param predicateQuery
	 * @return
	 * @throws RegistryQueryException
	 */
	public TaskPlugin[] getTaskPlugins(String predicateQuery) throws RegistryQueryException;
	
	
	
	
}
