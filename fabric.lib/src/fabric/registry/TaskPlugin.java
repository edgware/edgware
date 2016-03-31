/*
 * (C) Copyright IBM Corp. 2008, 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

/**
 * Represents information about a TaskPlugin.
 */
public interface TaskPlugin extends NodePlugin {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2008, 2009";

	/**
	 * Get the task with which this plugin is associated.
	 * 
	 * @return the task identifier.
	 */
	public String getTaskId();

	/**
	 * Set the task with which this plugin is associated.
	 * 
	 * @param taskId
	 *            - the task identifier.
	 */
	public void setTaskId(String taskId);

	/**
	 * Get the platform with which this plugin is associated.
	 * 
	 * @return the platform id, where a value of '*' represents all platforms.
	 */
	public String getPlatformId();

	/**
	 * Set the platform with which this plugin is associated.
	 * 
	 * @param platformId
	 *            - the platform id, or a value of '*' which represents all platforms.
	 */
	public void setPlatformId(String platformId);

	/**
	 * Get the id of the system on the associated platform with which this plugin is associated.
	 * 
	 * @return the system id, where a value of '*' represents all systems on the given platform.
	 */
	public String getSensorId();

	/**
	 * Set the id of the system on the associated platform with which this plugin is associated.
	 * 
	 * @param systemId
	 *            - the system id, or a value of '*' which represents all systems on the given platform.
	 */
	public void setSensorId(String systemId);

	/**
	 * Get the id of the feed on the associated system.
	 * 
	 * @return the feed id, where a value of '*' represents all feeds on the given system.
	 */
	public String getFeedId();

	/**
	 * Set the id of the feed on the associated system.
	 * 
	 * @param feedId
	 *            - feed id, or a value of '*' which respresents all feeds on the given system.
	 */
	public void setFeedId(String feedId);

}
