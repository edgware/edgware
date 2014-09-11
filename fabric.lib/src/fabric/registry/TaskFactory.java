/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import fabric.registry.exception.RegistryQueryException;

/**
 * Factory used to create Tasks and save/delete/query them in the Fabric Registry.
 * 
 * This factory can be accessed by calling FabricRegistry.getTaskFactory();
 */
public interface TaskFactory extends Factory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/**
	 * Creates a Task, specifying only the mandatory attributes.
	 * 
	 * @param id
	 *            - the identifier for the task.
	 * @return a Task object with only the id field set; all other fields are set to default values.
	 */
	public Task createTask(String id);

	/**
	 * Creates a Task using the specified details.
	 * 
	 * @param id
	 *            - the identifier for the task.
	 * @param priority
	 *            - the priority of the task.
	 * @param affiliation
	 *            - the affiliation of the task.
	 * @param description
	 *            - the description of the task.
	 * @param detail
	 *            - any task-specific details.
	 * @param detailUri
	 *            - a task-specific details uri.
	 * @return a populated Task object.
	 */
	public Task createTask(String id, int priority, String affiliation, String description, String detail,
			String detailUri);

	/**
	 * Get a list of all the Tasks defined in the Fabric Registry.
	 * 
	 * @return a list of all tasks.
	 */
	public Task[] getAllTasks();

	/**
	 * Gets the details for a specific task.
	 * 
	 * @param id
	 *            - the identifier of the task.
	 * @return the matching task object or null if no match is found.
	 */
	public Task getTaskById(String id);

	/**
	 * Gets a list of all Tasks using a custom WHERE-clause predicate.
	 * 
	 * @param queryPredicates
	 *            - the predicates to use that form the WHERE clause
	 * @return - a matching list of tasks or null if no match is found.
	 * @throws RegistryQueryException
	 */
	public Task[] getTasksWithPredicates(String queryPredicates) throws RegistryQueryException;

}
