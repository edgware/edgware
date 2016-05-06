/*
 * (C) Copyright IBM Corp. 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.feeds;

import fabric.TaskServiceDescriptor;

/**
 * Interface for classes managing a single client subscription to a service.
 */
public interface ISubscription {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

    /**
     * Subscribes to the specified service.
     * <p>
     * This method will send a subscription request onto the Fabric (via the local Fabric Manager) to establish a
     * connection (multi-hop if necessary, using the <em>default</em> -- i.e. the first -- route from the Fabric
     * Registry) from the local node to the target node connected to the service.
     * </p>
     * <p>
     * All parameters must be specified as this subscription must point at an existing service.
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
     * @param service
     *            the ID of the required service.
     *
     * @param callback
     *            the callback to be invoked when a message arrives from the service(s).
     *
     * @return the service to which a subscription has been made.
     *
     * @throws IllegalArgumentException
     *             thrown if the arguments do not represent a single service.
     *
     * @throws Exception
     *             thrown if the subscription fails.
     */
    public TaskServiceDescriptor subscribe(String task, String platform, String system, String service,
            ISubscriptionCallback callback) throws IllegalArgumentException, Exception;

    /**
     * Subscribes to the specified service.
     * <p>
     * This method will send a subscription request onto the Fabric (via the local Fabric Manager) to establish a
     * connection (multi-hop if necessary, using the <em>default</em> -- i.e. the first -- route from the Fabric
     * Registry) from the local node to the target node connected to the service.
     * </p>
     * <p>
     * The fields of <code>service</code> must identify a specific service.
     * </p>
     *
     * @param service
     *            the service that will be used for this subscription.
     *
     * @param callback
     *            the callback to be invoked when a message arrives from the service.
     *
     * @return the service to which a subscription has been made.
     *
     * @throws IllegalArgumentException
     *             thrown if the service does not represent a single service
     *
     * @throws Exception
     *             thrown if the subscription fails.
     */
    public TaskServiceDescriptor subscribe(TaskServiceDescriptor service, ISubscriptionCallback callback)
        throws IllegalArgumentException, Exception;

    /**
     * Subscribes to the specified service using the specified route to the end node
     * <p>
     * This method will send a subscription request onto the Fabric (via the local Fabric Manager) to establish a
     * connection. It will use the specified list of nodes as the route from the local node to the target node connected
     * to the service.
     * </p>
     * <p>
     * The fields of <code>service</code> must identify a specific service.
     * </p>
     *
     * @param service
     *            the service that will be used for this subscription.
     * @param nodes
     *            the nodes to route the subscription through
     * @param callback
     *            the callback to be invoked when a message arrives from the service.
     *
     * @return the service to which a subscription has been made.
     *
     * @throws IllegalArgumentException
     *             thrown if the service does not represent a single service
     *
     * @throws Exception
     *             thrown if the subscription fails.
     */
    public TaskServiceDescriptor subscribe(TaskServiceDescriptor service, String[] route, ISubscriptionCallback callback)
        throws IllegalArgumentException, Exception;

    /**
     * Re-sends the subscription message, to re-establish an existing subscription.
     *
     * @exception Exception
     *                thrown if there is no current subscription, of if there is an error sending the current
     *                subscription.
     */
    public void resubscribe() throws Exception;

    /**
     * Tear down the subscription managed by this instance.
     * <p>
     * This method will send an <em>unsubscribe</em> command to the local Fabric Manager to tear down the multi-hop
     * connection from the local node to the target node connected to the service.
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
     * Answers the service to which this instance is subscribed (or to which it is attempting to subscribe).
     *
     * @return the service.
     */
    public TaskServiceDescriptor service();

    /**
     * Answers the route (list of Fabric nodes) between the publisher and the subscriber.
     *
     * @return the route, or <code>null</code> if currently unsubscribed.
     */
    public String[] route();
}
