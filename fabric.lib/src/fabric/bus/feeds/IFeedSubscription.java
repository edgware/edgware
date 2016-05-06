/*
 * (C) Copyright IBM Corp. 2006, 2009
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.feeds;

import fabric.TaskServiceDescriptor;

/**
 * Interface for classes managing a client subscription to Fabric data feeds.
 */
public interface IFeedSubscription {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2006, 2009";

    /*
     * Interface methods
     */

    /**
     * Subscribes to the specified Fabric feeds.
     * <p>
     * This method will send a subscription request onto the Fabric (via the local Fabric Manager) to establish a
     * connection (multi-hop if necessary, using the <em>default</em> -- i.e. the first -- route from the Fabric
     * Registry) from the local node to the target node connected to the feed.
     * </p>
     * <p>
     * Parameters are either specific IDs, or <code>null</code>s to indicate that all IDs are to be matched. For
     * example, to match all feed types, on all systems, on all platforms, the specified platform, system, and feed type
     * would all be <code>null</code>.
     * </p>
     *
     * @param taskPattern
     *            the task pattern associated with the subscription.
     *
     * @param platformPattern
     *            the ID of the required platform, or <code>null</code> if all platforms are to be matched.
     *
     * @param systemPattern
     *            the ID of the required system, or <code>null</code> if all systems are to be matched.
     *
     * @param servicePattern
     *            the ID of the required feed type, or <code>null</code> if all feed types are to be matched.
     *
     * @param callback
     *            the callback to be invoked when a message arrives from the service(s).
     *
     * @return the list of feeds to which a subscription has been made.
     *
     * @throws UnsupportedOperationException
     *             thrown if this method is not implemented by the selected concrete class.
     *
     * @throws Exception
     *             thrown if the subscription fails.
     */
    public TaskServiceDescriptor[] subscribe(String taskPattern, String platformPattern, String systemPattern,
            String servicePattern, ISubscriptionCallback callback) throws Exception, UnsupportedOperationException;

    /**
     * Subscribes to the specified Fabric feeds.
     * <p>
     * This method will send a subscription request onto the Fabric (via the local Fabric Manager) to establish a
     * connection (multi-hop if necessary, using the <em>default</em> -- i.e. the first -- route from the Fabric
     * Registry) from the local node to the target node connected to the feed.
     * </p>
     * <p>
     * The fields of <code>feedPattern</code> are either specific IDs, or <code>null</code>s to indicate that all IDs
     * are to be matched. For example, to match all feed types, on all systems, on all platforms, the specified
     * platform, system, and feed type would all be <code>null</code>.
     * </p>
     *
     * @param feedPattern
     *            the feed pattern that will be matched for this subscription.
     *
     * @param callback
     *            the callback to be invoked when a message arrives from the service(s).
     *
     * @return the list of feeds to which a subscription has been made.
     *
     * @throws UnsupportedOperationException
     *             thrown if this method is not implemented by the selected concrete class.
     *
     * @throws Exception
     *             thrown if the subscription fails.
     */
    public TaskServiceDescriptor[] subscribe(TaskServiceDescriptor feedPattern, ISubscriptionCallback callback)
            throws Exception, UnsupportedOperationException;

    /**
     * Tear down the subscription(s) managed by this instance.
     * <p>
     * This method will send an <em>unsubscribe</em> command to the local Fabric Manager to tear down the multi-hop
     * connection from the local node to the target node(s) connected to the service(s).
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
     * Answers the list of services to which this instance is subscribed (or to which it is attempting to subscribe).
     *
     * @return the list of services.
     */
    public TaskServiceDescriptor[] serviceList();

}