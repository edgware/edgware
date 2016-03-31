/*
 * (C) Copyright IBM Corp. 2011
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import fabric.registry.exception.RegistryQueryException;

/**
 * Factory used to create TaskServices (which represent DataFeeds associated with a particular Task)
 * and save/delete/query them in the Fabric Registry.
 * 
 * @see fabric.registry.FabricRegistry#getTaskServiceFactory()
 * 
 */
public interface TaskServiceFactory extends Factory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2011";

	/**
	 * Create a TaskService, specifying only the mandatory attributes.
	 * 
	 * @param taskId - the id of the task.
	 * @param platformId - the id of the platform or '*' for all platforms.
	 * @param serviceId - the id of a particular service, or '*' for all services on the platform.
	 * @param feedId - the id of a particular data feed, or '*' for all feeds for a particular service.
	 * @return a TaskService with the specified fields set; all other fields will be set to default values.
	 */
	public TaskService createTaskService(String taskId, String platformId, String serviceId, String feedId);
	
	/**
	 * Create a TaskService using the specified details.
	 * 
	 * @param taskId - the id of the task.
	 * @param platformId - the id of the platform or '*' for all platforms.
	 * @param serviceId - the id of a particular service, or '*' for all services on the platform.
	 * @param feedId - the id of a particular feed, or '*' for all feeds for a particular service.
	 * @param description - the task-specific description.
	 * @param configuration - the task-specific configuration.
	 * @param configurationURI - the task-specific configuration URI.
	 * @return the fully-populated TaskService.
	 */
	public TaskService createTaskService(String taskId, String platformId, String serviceId, String feedId, String description, String configuration, String configurationURI);
	
	/**
	 * Get the list of all DataFeeds assigned to Tasks as defined in the Fabric Registry.
	 * 
	 * @return a list of TaskServices or an empty list if none exist.
	 */
	public TaskService[] getAllTaskServices();
	
	/**
	 * Get the list of DataFeeds for a specific Task.
	 * 
	 * @param id - the task id.
	 * @return the list of TaskServices assigned to the Task or an empty list if none are assigned.
	 */
	public TaskService[] getTaskServicesByTask(String id);
	
	/**
	 * Get a list of DataFeeds assigned to Tasks using a custom WHERE-clause predicate.
	 * 
	 * @param queryPredicates - the predicates to use in the WHERE clause of the query.
	 * @return a list of TaskServices or an empty list if none are found that match.
	 * @throws RegistryQueryException if the predicates are malformed. 
	 */
	public TaskService[] getTaskServices(String queryPredicates) throws RegistryQueryException;
	
	/**
	 * Get a specific TaskService entry.
	 * 
	 * @param taskId - the task id.
	 * @param platformId - the name of the platform.
	 * @param serviceId - the name of the service.
	 * @param feedId - the name of the feed.
	 * @return the matching object or null if no match is found.
	 */
	public TaskService getTaskServiceById(String taskId, String platformId, String serviceId, String feedId);
	
	/**
	 * Deletes all TaskServices associated with the specified Task.
	 * 
	 * @param taskId - the task for which all TaskServices should be removed.
	 * @return true if the delete was successful or false if an error occurred.
	 */
	public boolean deleteTaskServicesForTask(String taskId);
}
