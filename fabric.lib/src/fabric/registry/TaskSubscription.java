/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

/**
 * Represents information about an actor's subscription to a particular task platform, system and feed.
 */
public interface TaskSubscription extends RegistryObject {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/**
	 * Get the task to which the system feed is assigned.
	 * 
	 * @return the task id/
	 */
	public String getTaskId();

	/**
	 * Set the task to which the system feed is assigned.
	 * 
	 * @param taskId
	 *            - the id of the task.
	 */
	public void setTaskId(String taskId);

	/**
	 * Get the actor that is subscribing to the task.
	 * 
	 * @return the actor id.
	 */
	public String getActorId();

	/**
	 * Set the actor that is subscribing to the task.
	 * 
	 * @param actorId
	 *            - the actor id.
	 */
	public void setActorId(String actorId);

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
	 * Get the ID of the system on the platform.
	 * 
	 * @return the id of the system, where '*' represents all systems on the platform.
	 */
	public String getSystemId();

	/**
	 * Set the id of a system on the platform.
	 * 
	 * @param systemId
	 *            - the id of a system, or '*' which represents all systems on the platform.
	 */
	public void setSystemId(String systemId);

	/**
	 * Get the id of the feed on the system.
	 * 
	 * @return the id of the feed, where '*' represents all feeds on the system.
	 */
	public String getFeedId();

	/**
	 * Set the ID of a feed on the system.
	 * 
	 * @param feedId
	 *            - the id of the feed, or '*' which represents all feeds on the system.
	 */
	public void setFeedId(String feedId);

	/**
	 * Get the platform to which the subscribing actor is attached.
	 * 
	 * @return the platform id of the actor.
	 */
	public String getActorPlatformId();

	/**
	 * Set the platform to which the subscribing actor is attached.
	 * 
	 * @param platformId
	 *            - the platform id.
	 */
	public void setActorPlatformId(String platformId);

}
