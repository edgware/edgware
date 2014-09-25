/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import java.util.logging.Level;

import fabric.Fabric;
import fabric.FabricBus;
import fabric.bus.routing.IRoutingFactory;
import fabric.registry.FabricRegistry;
import fabric.registry.RegistryObject;
import fabric.registry.Route;
import fabric.registry.RouteFactory;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.exception.RegistryQueryException;
import fabric.registry.persistence.IPersistenceResultRow;

/**
 * Implementation of the factory for <code>Route</code>s.
 */
public class RouteFactoryImpl extends AbstractFactory implements RouteFactory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/*
	 * Class constants
	 */

	/** Factory for centralised (singleton) Registry operations */
	private static RouteFactoryImpl localQueryInstance = null;
	/** Factory for distributed (gaian) Registry operations */
	private static RouteFactoryImpl remoteQueryInstance = null;

	/*
	 * Queries
	 */

	/** column selector used in all queries */
	private String SELECT_COLUMNS = null;
	/** Select all records */
	private String SELECT_ALL_QUERY = null;
	/** Select records for a particular starting node */
	private String BY_START_NODE_QUERY = null;
	/** Select records using an arbitrary WHERE clause */
	private String PREDICATE_QUERY = null;

	/*
	 * Static initialisation
	 */
	static {
		localQueryInstance = new RouteFactoryImpl(true);
		remoteQueryInstance = new RouteFactoryImpl(false);
	}

	private RouteFactoryImpl(boolean queryLocal) {
		this.localOnly = queryLocal;

		SELECT_COLUMNS = format("r.start_node_id, r.end_node_id, r.ordinal, r.route from %s as r ",
				FabricRegistry.ROUTES);
		SELECT_ALL_QUERY = format("select 'all' as type, %s", SELECT_COLUMNS);
		BY_START_NODE_QUERY = format("select 'byStartNode' as type, %s where START_NODE_ID='\\%s'", SELECT_COLUMNS);
		PREDICATE_QUERY = format("select 'predicate' as type, %s where \\%s", SELECT_COLUMNS);
	}

	public static RouteFactoryImpl getInstance(boolean queryLocal) {

		if (queryLocal) {
			return localQueryInstance;
		} else {
			return remoteQueryInstance;
		}
	}

	@Override
	public RegistryObject create(IPersistenceResultRow row) throws PersistenceException {

		Route route = null;

		if (row != null) {

			RouteImpl impl = new RouteImpl();
			impl.setStartNode(row.getString("START_NODE_ID"));
			impl.setEndNode(row.getString("END_NODE_ID"));
			impl.setOrdinal(row.getInt("ORDINAL"));
			impl.setRoute(row.getString("ROUTE"));

			if (row.getString("TYPE").equals("pointToPoint")) {

				/* If there is no route... */
				if (impl.getStartNode().equals("*")) {

					/* Get the node from which the query was run */
					String targetStartNode = row.getString("TARGET_START_NODE");

					/* Get the target node */
					String targetEndNode = row.getString("TARGET_END_NODE");

					/* If the feed is directly from the client node... */
					if (targetStartNode.equals(targetEndNode)) {

						/* No route to worry about */
						impl.setRoute("nodes=" + targetStartNode);

					} else {

						/* Generate a direct-connect route */
						impl.setRoute("factory=fabric.bus.routing.DynamicRoutingFactory");

					}
				}

			}

			/* preserve these values internally */
			impl.createShadow();

			route = impl;
		}

		return route;
	}

	@Override
	public String getDeleteSql(RegistryObject obj) {

		StringBuffer buf = new StringBuffer();
		if (obj instanceof Route) {
			Route route = (Route) obj;
			buf.append("delete from " + FabricRegistry.ROUTES + " where(");
			buf.append("START_NODE_ID='").append(route.getStartNode()).append("' AND ");
			buf.append("END_NODE_ID='").append(route.getEndNode()).append("')");
		}
		return buf.toString();
	}

	@Override
	public String getInsertSql(RegistryObject obj) {

		StringBuffer buf = new StringBuffer();
		if (obj instanceof Route) {
			Route route = (Route) obj;
			buf.append("insert into " + FabricRegistry.ROUTES + " values(");
			buf.append(nullOrString(route.getStartNode())).append(",");
			buf.append(nullOrString(route.getEndNode())).append(",");
			buf.append(route.getOrdinal()).append(",");
			buf.append(nullOrString(route.getRoute())).append(")");
		}
		return buf.toString();
	}

	@Override
	public String getUpdateSql(RegistryObject obj) {

		StringBuffer buf = new StringBuffer();
		if (obj instanceof Route) {
			Route route = (Route) obj;
			buf.append("update " + FabricRegistry.ROUTES + " set ");
			buf.append("START_NODE_ID='").append(route.getStartNode()).append("',");
			buf.append("END_NODE_ID='").append(route.getEndNode()).append("',");
			buf.append("ORDINAL=").append(route.getOrdinal()).append(",");
			buf.append("ROUTE=").append(nullOrString(route.getRoute()));
			buf.append(" WHERE");

			/* if it exists, use the shadow values for the WHERE clause */
			if (route.getShadow() != null) {
				Route shadow = (Route) route.getShadow();
				buf.append(" START_NODE_ID='").append(shadow.getStartNode()).append("'");
				buf.append(" AND END_NODE_ID='").append(shadow.getEndNode()).append("'");
			} else {
				buf.append(" START_NODE_ID='").append(route.getStartNode()).append("'");
				buf.append(" AND END_NODE_ID='").append(route.getEndNode()).append("'");
			}
		}
		return buf.toString();
	}

	// public Route createRoute(String startNode, String endNode, int ordinal, String route) {
	//
	// return new RouteImpl(startNode, endNode, ordinal, route);
	// }

	@Override
	public Route[] getAllRoutes() {

		Route[] routes = null;
		try {
			routes = runQuery(SELECT_ALL_QUERY);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return routes;
	}

	@Override
	public Route[] getRouteByStartingNodeId(String id) {

		Route[] routes = null;
		try {
			String query = format(BY_START_NODE_QUERY, id);
			routes = runQuery(query);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return routes;
	}

	@Override
	public Route[] getRoutes(String startNode, String endNode) {

		Route[] routes = null;

		try {

			/* Build the query */
			String query = "select ";
			query += "'pointToPoint' as type, ";
			query += "r.start_node_id, ";
			query += "r.end_node_id, ";
			query += "r.ordinal, ";
			query += "r.route, ";
			query += "'" + startNode + "' as target_start_node, ";
			query += "'" + endNode + "' as target_end_node ";
			query += "from " + FabricRegistry.ROUTES + " as r ";
			query += "where ((r.start_node_id='" + startNode + "' and r.end_node_id='" + endNode + "') or ";
			query += "(r.start_node_id='*' and r.end_node_id='*')) ";
			query += "order by r.ordinal";

			routes = runQuery(query);

		} catch (PersistenceException e) {

			e.printStackTrace();

		}

		return routes;
	}

	@Override
	public Route[] getRoutes(String queryPredicates) throws RegistryQueryException {

		Route[] routes = null;
		try {
			String query = format(PREDICATE_QUERY, queryPredicates);
			routes = runQuery(query);
		} catch (PersistenceException e) {
			e.printStackTrace();
			throw new RegistryQueryException("Invalid query: " + PREDICATE_QUERY + queryPredicates);
		}
		return routes;
	}

	private Route[] runQuery(String sql) throws PersistenceException {

		Route[] routes = null;
		RegistryObject[] objects = queryRegistryObjects(sql, this);
		if (objects != null && objects.length > 0) {
			// necessary
			routes = new Route[objects.length];
			for (int k = 0; k < objects.length; k++) {
				routes[k] = (Route) objects[k];
			}
		} else {
			routes = new Route[0];
		}
		return routes;
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.Factory#delete(fabric.registry.RegistryObject)
	 */
	@Override
	public boolean delete(RegistryObject obj) {

		if (obj != null && obj instanceof Route) {
			return super.delete(obj, this);
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.Factory#save(fabric.registry.RegistryObject)
	 */
	@Override
	public boolean save(RegistryObject obj) throws IncompleteObjectException {

		if (obj != null && obj instanceof Route) {
			return super.save(obj, this);
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.Factory#insert(fabric.registry.RegistryObject)
	 */
	@Override
	public boolean insert(RegistryObject obj) throws IncompleteObjectException, DuplicateKeyException,
			PersistenceException {

		if (obj != null && obj instanceof Route) {
			return super.insert(obj, this);
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.Factory#update(fabric.registry.RegistryObject)
	 */
	@Override
	public boolean update(RegistryObject obj) throws IncompleteObjectException, PersistenceException {

		if (obj != null && obj instanceof Route) {
			return super.update(obj, this);
		} else {
			return false;
		}
	}

	@Override
	public String[] getRouteNodes(String startNode, String endNode, String routeDesc) throws Exception {

		String[] nodes = null;

		if (endNode.equals("$virtual")) {

			/* $virtual designates global feeds, always accessed via the local node */
			nodes = new String[] {startNode};

		} else if (routeDesc.toLowerCase().startsWith("nodes=")) {

			String nodeList = routeDesc.substring(6);
			nodes = nodeList.trim().split("\\s*,\\s*");

		} else if (routeDesc.toLowerCase().startsWith("factory=")) {

			String className = routeDesc.substring(8);
			IRoutingFactory fac = (IRoutingFactory) Fabric.instantiate(className);
			nodes = fac.getRouteNodes(startNode, endNode);

		} else if (routeDesc.toLowerCase().startsWith("<")) {

			nodes = FabricBus.unpackRoute(routeDesc);

		} else {

			logger.log(Level.WARNING, "Unrecognised routing type %1. Defaulting to dynamic routing.", routeDesc);

		}

		if (nodes.length == 0) {
			/* Try the default point-to-point route */
			nodes = new String[] {startNode, endNode};
		}

		return nodes;
	}
}
