/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import fabric.registry.exception.RegistryQueryException;

/**
 * Factory used to create task subscriptions and save/delete/query them in the Fabric Registry.
 * 
 * @see fabric.registry.FabricRegistry#getTaskSubscriptionFactory()
 */
public interface TaskSubscriptionFactory extends Factory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/**
	 * Create a TaskSubscription using the specified details.
	 * 
	 * @param taskId
	 *            - the id of the task.
	 * @param actorId
	 *            - the id of the actor.
	 * @param platformId
	 *            - the id of the platform or '*' which represents all platforms.
	 * @param systemId
	 *            - the id of the system or '*' which represents all systems on a platform.
	 * @param feedId
	 *            - the id of the feed or '*' which represents all feeds on a system.
	 * @param actorPlatformId
	 *            - the id of the platform the actor is attached to.
	 * @return a populated TaskSubscription object.
	 */
	public TaskSubscription createTaskSubscription(String taskId, String actorId, String platformId, String systemId,
			String feedId, String actorPlatformId);

	/**
	 * Get the list of all task subscriptions that are defined in the Fabric Registry.
	 * 
	 * @return a list of task subscriptions.
	 */
	public TaskSubscription[] getAllTaskSubscriptions();

	/**
	 * Get the list of task subscriptions using a custom WHERE-clause predicate.
	 * 
	 * @param queryPredicates
	 *            - the predicates used to form the WHERE clause of the query,
	 * @return a list of task subscriptions.
	 * @throws RegistryQueryException
	 *             if the predicates are malformed.
	 */
	public TaskSubscription[] getTaskSubscriptions(String queryPredicates) throws RegistryQueryException;

	/**
	 * Get the list of subscriptions to a particular task.
	 * 
	 * @param taskId
	 *            - the id of the task.
	 * @return the list of subscriptions to the task.
	 */
	public TaskSubscription[] getTaskSubscriptionsByTask(String taskId);

}
