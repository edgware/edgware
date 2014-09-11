/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.feeds.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.TaskServiceDescriptor;
import fabric.bus.feeds.ISubscription;
import fabric.bus.feeds.ISubscriptionCallback;
import fabric.bus.feeds.ISubscriptionCollection;
import fabric.bus.messages.IFeedMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.client.FabricClient;
import fabric.registry.FabricRegistry;
import fabric.registry.FeedRoutes;
import fabric.registry.RegistryObject;
import fabric.registry.exception.FactoryCreationException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.impl.FeedRoutesFactoryImpl;

/**
 * Base class for managing a collection of client subscriptions to Fabric data feeds.
 */
public abstract class SubscriptionCollection implements ISubscriptionCollection, ISubscriptionCallback {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

	protected Logger logger;

	/* Table of active subscriptions managed by this collection */
	protected HashMap<TaskServiceDescriptor, ISubscription> subscriptions = null;

	private ISubscriptionCallback callback = null;

	protected FabricClient fabricClient;

	protected SubscriptionCollection(FabricClient fabricClient) {

		this.logger = Logger.getLogger("fabric.bus.feeds");
		this.fabricClient = fabricClient;
		subscriptions = new HashMap<TaskServiceDescriptor, ISubscription>();
	}

	protected void subscribe(TaskServiceDescriptor taskServiceDescriptor, String[] route) throws Exception {

		// Check we are not already subscribed to this feed
		if (subscriptions.containsKey(taskServiceDescriptor)) {
			throw new IllegalStateException("Collection already subscribed to " + taskServiceDescriptor);
		}

		// Create the subscription object
		ISubscription subscription = new Subscription(fabricClient);

		if (route != null) {

			if (route.length == 0) {
				throw new IllegalArgumentException("Route cannot be zero-length");
			}

			// Subscribe using the provided route
			subscription.subscribe(taskServiceDescriptor, route, this);

		} else {

			// Subscribe without providing a route
			subscription.subscribe(taskServiceDescriptor, this);

		}

		subscriptions.put(taskServiceDescriptor, subscription);
	}

	protected void setCallback(ISubscriptionCallback callback) {

		this.callback = callback;
	}

	@Override
	public ISubscription[] subscriptions() {

		return subscriptions.values().toArray(new ISubscription[] {});
	}

	/**
	 * @see ISubscriptionCollection#unsubscribe()
	 */
	@Override
	public void unsubscribe() throws Exception {

		Iterator<TaskServiceDescriptor> it = subscriptions.keySet().iterator();
		while (it.hasNext()) {
			TaskServiceDescriptor feed = it.next();
			ISubscription sub = subscriptions.get(feed);
			sub.unsubscribe();
			it.remove();
		}
	}

	/**
	 * Get all the FeedRoute objects represented by the specified feed pattern.
	 * 
	 * @param feedPattern
	 * @return
	 * @throws PersistenceException
	 * @throws FactoryCreationException
	 */
	protected FeedRoutes[] getFeedRoutes(TaskServiceDescriptor feedPattern) throws PersistenceException,
			FactoryCreationException {

		// Query not restricted to just local
		RegistryObject[] objects = FabricRegistry.runQuery(FeedRoutesFactoryImpl.getRouteQuery(feedPattern.task(),
				feedPattern.platform(), feedPattern.system(), feedPattern.service(), fabricClient.homeNode()),
				FeedRoutesFactoryImpl.class, false);

		/* Get the matching list of feeds */
		FeedRoutes[] matchingFeeds = null;

		/* If matching feeds have been found... */
		if (objects != null && objects.length > 0) {

			/* Cast the results to FeedRoutes objects */

			matchingFeeds = new FeedRoutes[objects.length];

			/* For each feed... */
			for (int f = 0; f < objects.length; f++) {

				matchingFeeds[f] = (FeedRoutes) objects[f];
			}

		} else {

			logger.log(Level.FINE, "No matching feeds: {0} asset details found", (objects == null) ? "null"
					: objects.length);

			matchingFeeds = new FeedRoutes[0];

		}

		return matchingFeeds;
	}

	@Override
	public void cancelSubscriptionCallback() {

		if (this.callback != null) {
			this.callback.cancelSubscriptionCallback();
		}
	}

	@Override
	public void handleSubscriptionEvent(ISubscription subscription, int event, IServiceMessage message) {

		/* Remove the subscription from our table of managed subscriptions */
		if (event == IServiceMessage.EVENT_SUBSCRIPTION_LOST) {
			subscriptions.remove(subscription.feed());
		}

		if (this.callback != null) {
			this.callback.handleSubscriptionEvent(subscription, event, message);
		}
	}

	@Override
	public void handleSubscriptionMessage(IFeedMessage message) {

		if (this.callback != null) {
			this.callback.handleSubscriptionMessage(message);
		}
	}

	@Override
	public void startSubscriptionCallback() {

		if (this.callback != null) {
			this.callback.startSubscriptionCallback();
		}
	}

}
