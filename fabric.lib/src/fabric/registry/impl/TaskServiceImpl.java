/*
 * (C) Copyright IBM Corp. 2011
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.TaskService;
import fabric.registry.exception.IncompleteObjectException;

/**
 * Implementation class for a <code>TaskService</code>.
 */
public class TaskServiceImpl extends AbstractRegistryObject implements TaskService {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2011";

	private String taskId = null;
	private String platformId = null;
	private String serviceId = null;
	private String feedId = null;
	private String configuration = null;
	private String configurationUri = null;
	private String description = null;

	/* the Gaian node where this record was created */
	private String originNode = null;

	protected TaskServiceImpl() {

	}

	protected TaskServiceImpl(String taskId, String platformId, String serviceId, String feedId, String description,
			String configuration, String configurationUri) {
		this.taskId = taskId;
		this.platformId = platformId;
		this.serviceId = serviceId;
		this.feedId = feedId;
		this.description = description;
		this.configuration = configuration;
		this.configurationUri = configurationUri;
	}

	/**
	 * @see fabric.registry.TaskService#getConfiguration()
	 */
	@Override
	public String getConfiguration() {
		return configuration;
	}

	/**
	 * @see fabric.registry.TaskService#getConfigurationURI()
	 */
	@Override
	public String getConfigurationURI() {
		return configurationUri;
	}

	/**
	 * @see fabric.registry.TaskService#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @see fabric.registry.TaskService#getTaskId()
	 */
	@Override
	public String getTaskId() {
		return taskId;
	}

	/**
	 * @see fabric.registry.TaskService#setConfiguration(java.lang.String)
	 */
	@Override
	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	/**
	 * @see fabric.registry.TaskService#setConfigurationURI(java.lang.String)
	 */
	@Override
	public void setConfigurationURI(String uri) {
		this.configurationUri = uri;
	}

	/**
	 * @see fabric.registry.TaskService#setTaskId(java.lang.String)
	 */
	@Override
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	/**
	 * @see fabric.registry.TaskService#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @see fabric.registry.TaskService#getServiceId()
	 */
	@Override
	public String getServiceId() {
		return feedId;
	}

	/**
	 * @see fabric.registry.TaskService#setServiceId(java.lang.String)
	 */
	@Override
	public void setServiceId(String feedId) {
		this.feedId = feedId;
	}

	/**
	 * @see fabric.registry.TaskService#getSystemId()
	 */
	@Override
	public String getSystemId() {
		return serviceId;
	}

	/**
	 * @see fabric.registry.TaskService#setSystemId(java.lang.String)
	 */
	@Override
	public void setSystemId(String serviceId) {
		this.serviceId = serviceId;
	}

	/**
	 * @see fabric.registry.TaskService#getPlatformId()
	 */
	@Override
	public String getPlatformId() {
		return platformId;
	}

	/**
	 * @see fabric.registry.TaskService#setPlatformId(java.lang.String)
	 */
	@Override
	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}

	/**
	 * @see fabric.registry.RegistryObject#validate()
	 */
	@Override
	public void validate() throws IncompleteObjectException {
		if (taskId == null || taskId.length() == 0 || platformId == null || platformId.length() == 0
				|| serviceId == null || serviceId.length() == 0 || feedId == null || feedId.length() == 0) {

			throw new IncompleteObjectException("Task, platform, service and feed identifiers must be set.");
		}
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean equal = false;
		if (obj != null && obj instanceof TaskService) {
			TaskService tsf = (TaskService) obj;
			if (tsf.getSystemId().equals(serviceId) && tsf.getPlatformId().equals(platformId)
					&& tsf.getServiceId().equals(feedId) && tsf.getTaskId().equals(taskId)
					&& tsf.getDescription() == null ? description == null : tsf.getDescription().equals(description)
					&& tsf.getConfiguration() == null ? configuration == null : tsf.getConfiguration().equals(
					configuration)
					&& tsf.getConfigurationURI() == null ? configurationUri == null : tsf.getConfigurationURI().equals(
					configurationUri)) {

				equal = true;
			}
		}
		return equal;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buffy = new StringBuffer("TaskService::");
		buffy.append(" Task ID: ").append(taskId);
		buffy.append(", Platform ID: ").append(platformId);
		buffy.append(", System ID: ").append(serviceId);
		buffy.append(", Feed ID: ").append(feedId);
		buffy.append(", Description: ").append(description);
		buffy.append(", Configuration: ").append(configuration);
		buffy.append(", Configuration URI: ").append(configurationUri);
		if (originNode != null) {
			buffy.append(", Gaian Node: ").append(originNode);
		}
		return buffy.toString();
	}

	/**
	 * @see fabric.registry.RegistryObject#key()
	 */
	@Override
	public String key() {
		return new StringBuffer(this.getTaskId()).append("/").append(this.getPlatformId()).append("/").append(
				this.getSystemId()).append("/").append(this.getServiceId()).toString();
	}

	/**
	 * @see fabric.registry.TaskService#getOriginNode()
	 */
	@Override
	public String getOriginNode() {
		return originNode;
	}

	/**
	 * @see fabric.registry.TaskService#setOriginNode(java.lang.String)
	 */
	@Override
	public void setOriginNode(String registryNode) {
		this.originNode = registryNode;
	}
}
