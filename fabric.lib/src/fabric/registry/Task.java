/*
 * (C) Copyright IBM Corp. 2008, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

/**
 * Represents a task to which platforms, system and feeds are assigned.
 */
public interface Task extends RegistryObject {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2008, 2014";

	/**
	 * Get the identifier for the task.
	 * 
	 * @return the identifier for the task
	 */
	public String getId();

	/**
	 * Set the identifier for the task.
	 * 
	 * @param id
	 *            the identifier for the task
	 */
	public void setId(String id);

	/**
	 * Gets the priority assigned to the task.
	 * 
	 * @return the priority as an integer.
	 */
	public int getPriority();

	/**
	 * Sets the priority of the task.
	 * 
	 * @param priority
	 *            - the priority for the task
	 */
	public void setPriority(int priority);

	/**
	 * Get the affiliation of the task.
	 * 
	 * @return the affiliation
	 */
	public String getAffiliation();

	/**
	 * Set the affiliation for this task.
	 * 
	 * @param affiliation
	 *            - the affiliation to assign
	 */
	public void setAffiliation(String affiliation);

	/**
	 * Get a description of the task.
	 * 
	 * @return the description
	 */
	public String getDescription();

	/**
	 * Set a description for the task.
	 * 
	 * @param description
	 *            - the description of the task.
	 */
	public void setDescription(String description);

	/**
	 * Get any task-specific details.
	 * 
	 * @return - the details or null if none are specified.
	 */
	public String getDetail();

	/**
	 * Set any task-specific details.
	 * 
	 * @param detail
	 *            - the details to set.
	 */
	public void setDetail(String detail);

	/**
	 * Get the task-specific details uri.
	 * 
	 * @return - the uri or null if one is not set.
	 */
	public String getDetailUri();

	/**
	 * Set a uri for the task-specific details.
	 * 
	 * @param uri
	 *            - the uri of the details.
	 */
	public void setDetailUri(String uri);

	/**
	 * Get the list of feeds assigned to this task.
	 * 
	 * @return the list of task feeds.
	 */
	public TaskService[] getTaskServices();

	/**
	 * Get the list of actor subscriptions to this task.
	 * 
	 * @return the list of subscriptions.
	 */
	public TaskSubscription[] getTaskSubscriptions();

}
