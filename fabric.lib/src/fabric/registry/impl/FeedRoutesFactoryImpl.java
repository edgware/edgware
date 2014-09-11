/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.FabricRegistry;
import fabric.registry.RegistryObject;
import fabric.registry.exception.PersistenceException;
import fabric.registry.ext.CustomQueryFactory;
import fabric.registry.persistence.IPersistenceResultRow;

/**
 * Factory class used to create FeedRoutes objects. Also builds the SQL query used to search for applicable routes.
 */
public class FeedRoutesFactoryImpl extends CustomQueryFactory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

	/**
	 * @see fabric.registry.ext.CustomQueryFactory#create(java.sql.ResultSet)
	 */
	@Override
	public RegistryObject create(IPersistenceResultRow row) throws PersistenceException {

		FeedRoutesImpl impl = new FeedRoutesImpl();

		impl.setTaskId(row.getString("TASK_ID"));
		impl.setPlatformId(row.getString("PLATFORM_ID"));
		impl.setServiceId(row.getString("SERVICE_ID"));
		impl.setFeedId(row.getString("DATA_FEED_ID"));
		impl.setEndNodeId(row.getString("NODE_ID"));
		impl.setOrdinal(row.getInt("ORDINAL"));

		/* Get the route from the Routes table row */
		String lookupRoute = row.getString("ROUTE");

		/* Get the first column - the starting node for which this query was run */
		String actualStartingNode = row.getString(1);
		/* the start node found in the current row of the routes table */
		String lookupStartNode = row.getString("START_NODE_ID");

		/* If there is no route... */
		if (lookupStartNode.equals("*")) {

			/* If the feed is directly from the client node... */
			if (actualStartingNode.equals(impl.getEndNodeId())) {

				/* No route to worry about */
				lookupRoute = "nodes=" + actualStartingNode;

			} else {
				/* Generate a direct-connect route */
				lookupRoute = "factory=fabric.bus.routing.DynamicRoutingFactory";
			}
		}

		impl.setRoute(lookupRoute);

		return impl;
	}

	/**
	 * Gets the list of Fabric feeds (and their default routes from the specified start node) matching the supplied
	 * specification.
	 * <p>
	 * In the case of IDs, a value of <code>"*"</code> is used to indicate that all platforms, and/or systems, and/or
	 * feeds are to be matched. For example, to match all feeds, on all systems, on all platforms, the specified
	 * platform, system and feed IDs would <code>"*"</code>.
	 * </p>
	 * 
	 * @param task
	 * @param platform
	 * @param systemId
	 *            - the ID of system attached to the platform
	 * @param feed
	 * @param startNode
	 * @return a String containing the complete SQL query
	 */
	public static String getRouteQuery(String task, String platform, String systemId, String feedId, String startNode) {

		/* Build the query */
		String query = "select ";
		query += "'" + startNode + "' as my_node, ";
		query += "ts.task_id, ";
		query += "ts.platform_id, ";
		query += "ts.service_id, ";
		query += "ts.data_feed_id, ";
		query += "r.start_node_id, ";
		query += "p.node_id, ";
		query += "r.ordinal, ";
		query += "r.route ";
		query += "from ";
		query += FabricRegistry.ROUTES + " as r, ";
		query += FabricRegistry.TASK_SYSTEMS + " as ts, ";
		query += FabricRegistry.PLATFORMS + " as p ";
		query += "where ";

		if (!task.equals("*")) {
			query += "ts.task_id='" + task + "' and ";
		}

		if (!platform.equals("*")) {
			query += "ts.platform_id='" + platform + "' and ";
		}

		if (!systemId.equals("*")) {
			query += "ts.service_id='" + systemId + "' and ";
		}

		if (!feedId.equals("*")) {
			query += "ts.data_feed_id='" + feedId + "' and ";
		}

		query += "ts.platform_id = p.platform_id and ";
		query += "(" + "(p.node_id='$FABRIC' and r.start_node_id='*' and r.end_node_id='*')" + " or "
				+ "(r.start_node_id='" + startNode + "' and r.end_node_id=p.node_id)" + " or "
				+ "(r.start_node_id='*' and r.end_node_id='*')" + ") ";
		query += "order by ";
		query += "ts.task_id, ";
		query += "ts.platform_id, ";
		query += "ts.service_id, ";
		query += "ts.data_feed_id, ";
		query += "p.node_id, ";
		query += "r.ordinal";

		return query;
	}

}
