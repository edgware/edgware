/*
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.feeds.impl;

import java.util.HashMap;
import java.util.logging.Level;

import fabric.TaskServiceDescriptor;
import fabric.bus.feeds.ISubscriptionCallback;
import fabric.client.FabricClient;
import fabric.registry.FabricRegistry;
import fabric.registry.FeedRoutes;

public class WildcardSubscription extends SubscriptionCollection {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

	private TaskServiceDescriptor taskServiceDescriptor = null;

	public WildcardSubscription(FabricClient fabricClient) {

		super(fabricClient);
	}

	public void subscribe(String taskPattern, String platformPattern, String systemPattern, String feedTypePattern,
			ISubscriptionCallback callback) throws Exception {

		subscribe(new TaskServiceDescriptor(taskPattern, platformPattern, systemPattern, feedTypePattern), callback);
	}

	public void subscribe(TaskServiceDescriptor feed, ISubscriptionCallback callback) throws Exception {

		if (taskServiceDescriptor != null) {
			throw new IllegalStateException("Subscription already defined");
		}

		super.setCallback(callback);

		taskServiceDescriptor = feed;

		refresh();
	}

	@Override
	public void refresh() throws Exception {

		FeedRoutes[] matchingFeeds = getFeedRoutes(taskServiceDescriptor);

		HashMap<String, String[]> routesToEndNodeTable = new HashMap<String, String[]>();

		/* For each matching feed.. */
		for (int m = 0; m < matchingFeeds.length; m++) {

			TaskServiceDescriptor matchingFeed = new TaskServiceDescriptor(matchingFeeds[m].getTaskId(), matchingFeeds[m]
					.getPlatformId(), matchingFeeds[m].getServiceId(), matchingFeeds[m].getFeedId());

			/* If this feed has been handled already (we're only using the route with the lowest ordinal)... */
			if (subscriptions.containsKey(matchingFeed)) {

				/* Ignore it */

			} else {

				String[] nodes;
				if (!routesToEndNodeTable.containsKey(matchingFeeds[m].getEndNodeId())) {
					nodes = FabricRegistry.getRouteFactory().getRouteNodes(fabricClient.homeNode(),
							matchingFeeds[m].getEndNodeId(), matchingFeeds[m].getRoute());
					routesToEndNodeTable.put(matchingFeeds[m].getEndNodeId(), nodes);
				} else {
					nodes = routesToEndNodeTable.get(matchingFeeds[m].getEndNodeId());
				}
				if (nodes.length > 0) {
					subscribe(matchingFeed, nodes);
					logger.log(Level.FINE, "Added feed {0}", matchingFeed);
				}
			}

		}
	}
}
