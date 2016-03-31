/*
 * (C) Copyright IBM Corp. 2008
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

/**
 * Represents information about a node assigned to a particular task.
 */
public interface TaskNode extends RegistryObject {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2008";
	
	/**
	 * Get the task-specific configuration of this node.
	 * 
	 * @return - the configuration or null otherwise.
	 */
	public String getConfiguration();
	
	/**
	 * Set the task-specific configuration of this node.
	 * 
	 * @param configuration - the configuration details.
	 */
	public void setConfiguration(String configuration);
	
	/**
	 * Get the task-specific configuration uri.
	 * 
	 * @return the configuration uri.
	 */
	public String getConfigurationUri();
	
	/**
	 * Set the task-specific configuration uri.
	 * 
	 * @param configurationUri - the configuration uri.
	 */
	public void setConfigurationUri(String configurationUri);
	
	/**
	 * Get the task-specific description for the node.
	 * 
	 * @return the description.
	 */
	public String getDescription();
	
	/**
	 * Set the task-specific description for the node.
	 * 
	 * @param description - the description.
	 */
	public void setDescription(String description);
	
	/**
	 * Get the node associated with the particular task.
	 * 
	 * @return the id of the node.
	 */
	public String getNodeId();
	
	/**
	 * Set the node associated with the particular task.
	 * 
	 * @param nodeId - the node id.
	 */
	public void setNodeId(String nodeId);
	
	/**
	 * Get the task with which this node is associated.
	 * 
	 * @return the task id.
	 */
	public String getTaskId();
	
	/**
	 * Set the task with this node is associated.
	 * 
	 * @param taskId - the task id.
	 */
	public void setTaskId(String taskId);
	
}
