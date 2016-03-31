/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import fabric.registry.exception.RegistryQueryException;

/**
 * Factory used to create nodes that are associated with particular tasks and save/delete/query them in the
 * FabricRegistry.
 * 
 * This factory can be accessed by calling FabricRegistry.getTaskNodeFactory().
 */
public interface TaskNodeFactory extends Factory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/**
	 * Creates a TaskNode, specifying only the mandatory attributes.
	 * 
	 * @param taskId
	 *            - the id of the task.
	 * @param nodeId
	 *            - the id of the node.
	 * @return a TaskNode with only the specified fields set; all other fields will be set to default values.
	 */
	public TaskNode createTaskNode(String taskId, String nodeId);

	/**
	 * Create a TaskNode using the specified details.
	 * 
	 * @param taskId
	 *            - the id of the task
	 * @param nodeId
	 *            - the id of the node
	 * @param description
	 *            - the task-specific description for the node
	 * @param configuration
	 *            - the configuration of the node
	 * @param configurationUri
	 *            - the configuration uri of the node
	 * @return a populated TaskNode
	 */
	public TaskNode createTaskNode(String taskId, String nodeId, String description, String configuration,
			String configurationUri);

	/**
	 * Get the list of all task nodes defined in the Fabric Registry.
	 * 
	 * @return a list of task nodes.
	 */
	public TaskNode[] getAllTaskNodes();

	/**
	 * Get the list of nodes associated with a particular task.
	 * 
	 * @param taskId
	 *            - the task to search for
	 * @return a list of task nodes or null
	 */
	public TaskNode[] getTaskNodesByTask(String taskId);

	/**
	 * Get a list of task nodes using a custom WHERE-clause predicate.
	 * 
	 * @param queryPredicates
	 *            - the predicates to specify in the WHERE clause.
	 * @return a list of task nodes or null if there is no match (or none exist).
	 * @throws RegistryQueryException
	 */
	public TaskNode[] getTaskNodes(String queryPredicates) throws RegistryQueryException;

}
