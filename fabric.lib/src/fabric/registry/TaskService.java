/*
 * (C) Copyright IBM Corp. 2011
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

/**
 * Represents information about a Service assigned to a particular Task.
 */
public interface TaskService extends RegistryObject {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2011";

	/**
	 * Get the task to which the Service is assigned.
	 * 
	 * @return the task id/
	 */
	public String getTaskId();

	/**
	 * Set the task to which the Service is assigned.
	 * 
	 * @param taskId
	 *            - the id of the task.
	 */
	public void setTaskId(String taskId);

	/**
	 * Get the platform associated with the task.
	 * 
	 * @return the id of the platform.
	 */
	public String getPlatformId();

	/**
	 * Set the platform associated with the task.
	 * 
	 * @param platformId
	 *            - the platform id.
	 */
	public void setPlatformId(String platformId);

	/**
	 * Get the id of the system on the platform.
	 * 
	 * @return the id of the system, where '*' represents all systems on the platform.
	 */
	public String getSystemId();

	/**
	 * Set the id of the system on the platform.
	 * 
	 * @param systemId
	 *            - the id of the system, or '*' which represents all systems on the platform.
	 */
	public void setSystemId(String systemId);

	/**
	 * Get the name of the service.
	 * 
	 * @return the name of the service, where '*' represents all services for a particular system.
	 */
	public String getServiceId();

	/**
	 * Set the name of the service.
	 * 
	 * @param serviceId
	 *            - the id of the service, or '*' which represents all services for a particular system.
	 */
	public void setServiceId(String serviceId);

	/**
	 * Get the task-specific description for the Service.
	 * 
	 * @return the description.
	 */
	public String getDescription();

	/**
	 * Set the task-specific description for the Service.
	 * 
	 * @param description
	 *            - the description.
	 */
	public void setDescription(String description);

	/**
	 * Get the task-specific configuration for the Service.
	 * 
	 * @return the configuration.
	 */
	public String getConfiguration();

	/**
	 * Set the task-specific configuration for the Service.
	 * 
	 * @param configuration
	 *            - the configuration.
	 */
	public void setConfiguration(String configuration);

	/**
	 * Get the task-specific configuration URI for the Service.
	 * 
	 * @return the configuration URI.
	 */
	public String getConfigurationURI();

	/**
	 * Set the task-specific configuration URI for the Service.
	 * 
	 * @param uri
	 *            - the configuration URI.
	 */
	public void setConfigurationURI(String uri);

	/**
	 * Get the Registry node from which this information originated.
	 * 
	 * @return the name of the Fabric Registry node.
	 */
	public String getOriginNode();

	/**
	 * Set the name of the Fabric Registry node where this object was created.
	 * 
	 * @param registryNode
	 *            - the name of the Fabric Registry node.
	 * 
	 *            Note: this method is used internally by the Fabric only. Setting a value before saving this object
	 *            will not result in a record being created in the specified Fabric Registry.
	 */
	public void setOriginNode(String registryNode);
}
