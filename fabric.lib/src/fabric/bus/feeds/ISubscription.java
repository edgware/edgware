/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.feeds;

import fabric.TaskServiceDescriptor;

/**
 * Interface for classes managing a single client subscription to a Fabric data feed.
 */
public interface ISubscription {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

	/**
	 * Subscribes to the specified Fabric feed.
	 * <p>
	 * This method will send a subscription request onto the Fabric (via the local Fabric Manager) to establish a
	 * connection (multi-hop if necessary, using the <em>default</em> -- i.e. the first -- route from the Fabric
	 * Registry) from the local node to the target node connected to the feed.
	 * </p>
	 * <p>
	 * All parameters must be specified as this subscription must point at an existing feed.
	 * </p>
	 * 
	 * @param task
	 *            the task associated with the subscription.
	 * 
	 * @param platform
	 *            the ID of the required platform.
	 * 
	 * @param system
	 *            the ID of the required system.
	 * 
	 * @param feedType
	 *            the ID of the required feed type.
	 * 
	 * @param callback
	 *            the callback to be invoked when a Fabric message arrives from the feed(s).
	 * 
	 * @return the feed to which a subscription has been made.
	 * 
	 * @throws IllegalArgumentException
	 *             thrown if the arguments do not represent a single feed
	 * 
	 * @throws Exception
	 *             thrown if the subscription fails.
	 */
	public TaskServiceDescriptor subscribe(String task, String platform, String system, String feed,
			ISubscriptionCallback callback) throws IllegalArgumentException, Exception;

	/**
	 * Subscribes to the specified Fabric feed.
	 * <p>
	 * This method will send a subscription request onto the Fabric (via the local Fabric Manager) to establish a
	 * connection (multi-hop if necessary, using the <em>default</em> -- i.e. the first -- route from the Fabric
	 * Registry) from the local node to the target node connected to the feed.
	 * </p>
	 * <p>
	 * The fields of <code>feedPattern</code> must identify a specific feed.
	 * </p>
	 * 
	 * @param feed
	 *            the feed that will be used for this subscription.
	 * 
	 * @param callback
	 *            the callback to be invoked when a Fabric message arrives from the feed.
	 * 
	 * @return the feed to which a subscription has been made.
	 * 
	 * @throws IllegalArgumentException
	 *             thrown if the feed does not represent a single feed
	 * 
	 * @throws Exception
	 *             thrown if the subscription fails.
	 */
	public TaskServiceDescriptor subscribe(TaskServiceDescriptor feed, ISubscriptionCallback callback)
			throws IllegalArgumentException, Exception;

	/**
	 * Subscribes to the specified Fabric feed using the specified route to the end node
	 * <p>
	 * This method will send a subscription request onto the Fabric (via the local Fabric Manager) to establish a
	 * connection. It will use the specified list of nodes as the route from the local node to the target node connected
	 * to the feed.
	 * </p>
	 * <p>
	 * The fields of <code>feedPattern</code> must identify a specific feed.
	 * </p>
	 * 
	 * @param feed
	 *            the feed that will be used for this subscription.
	 * @param nodes
	 *            the nodes to route the subscription through
	 * @param callback
	 *            the callback to be invoked when a Fabric message arrives from the feed.
	 * 
	 * @return the feed to which a subscription has been made.
	 * 
	 * @throws IllegalArgumentException
	 *             thrown if the feed does not represent a single feed
	 * 
	 * @throws Exception
	 *             thrown if the subscription fails.
	 */
	public TaskServiceDescriptor subscribe(TaskServiceDescriptor feed, String[] route, ISubscriptionCallback callback)
			throws IllegalArgumentException, Exception;

	/**
	 * Tear down the subscription to the active Fabric feed managed by this instance.
	 * <p>
	 * This method will send an <em>unsubscribe</em> command to the local Fabric Manager to tear down the multi-hop
	 * connection from the local node to the target node connected to the feed.
	 * </p>
	 * 
	 * @throws Exception
	 *             thrown if the unsubscribe fails.
	 */
	public void unsubscribe() throws Exception;

	/**
	 * Answers the correlation ID for this subscription.
	 * 
	 * @return the correlationID.
	 */
	public String correlationID();

	/**
	 * Answers the feed to which this instance is subscribed (or to which it is attempting to subscribe).
	 * 
	 * @return the feed.
	 */
	public TaskServiceDescriptor feed();

}
