/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.jsonclient.articles;

import java.util.ArrayList;
import java.util.List;

import fabric.FabricBus;
import fabric.registry.FabricRegistry;
import fabric.registry.Node;
import fabric.registry.NodeFactory;
import fabric.registry.NodeIpMapping;
import fabric.registry.NodeIpMappingFactory;
import fabric.services.json.JSON;
import fabric.services.jsonclient.utilities.AdapterConstants;
import fabric.services.jsonclient.utilities.AdapterStatus;
import fabric.services.jsonclient.utilities.JsonUtils;

/**
 * Class that handles JSON commands that deal with Nodes.
 */
public class Nodes extends FabricBus {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	private static String homeNode = null;

	/*
	 * Class methods
	 */

	/**
	 * Inserts a Node into the registry.
	 * 
	 * @param jsonOpObject
	 *            the full JSON operation object.
	 * 
	 * @param correlId
	 *            the operation correlation ID.
	 * 
	 * @return the JSON message with the status of the operation.
	 */

	public static JSON register(JSON jsonOpObject, String correlId) {

		AdapterStatus status = new AdapterStatus(correlId);

		try {

			String id = jsonOpObject.getString(AdapterConstants.FIELD_ID);
			String nodeInterface = jsonOpObject.getString(AdapterConstants.FIELD_NODE_INTERFACE);
			String address = jsonOpObject.getString(AdapterConstants.FIELD_ADDRESS).substring(5);
			String typeId = jsonOpObject.getString(AdapterConstants.FIELD_TYPE);

			if (id == null || address == null) {

				status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_REGISTER,
						AdapterConstants.ARTICLE_NODE, AdapterConstants.STATUS_MSG_MISSING_FIELDS, correlId);

			} else {

				if (typeId == null) {
					typeId = AdapterConstants.FIELD_VALUE_UKNOWN;
				}

				String[] ipParts = address.split(":");
				String ipAddress = ipParts[0];
				int port = Integer.parseInt(ipParts[1]);

				NodeFactory nodeFactory = FabricRegistry.getNodeFactory();
				Node node = nodeFactory.createNode(id, typeId, // Node type
						null, // affiliation
						null, // Security Classification
						null, // readiness
						null, // availability,
						0, // latitude,
						0, // longitude,
						0, // altitude,
						0, // bearing,
						0, // velocity,
						jsonOpObject.getString(AdapterConstants.FIELD_DESCRIPTION), jsonOpObject
								.getString(AdapterConstants.FIELD_ATTRIBUTES), null); // attributesURI;
				boolean success = nodeFactory.save(node);

				if (!success) {

					status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
							AdapterConstants.ARTICLE_NODE, "Insert/update of node into the Registry failed", correlId);

				} else {

					/* Map the Node to an IP address */

					NodeIpMappingFactory ipFactory = FabricRegistry.getNodeIpMappingFactory();
					NodeIpMapping ip = ipFactory.createNodeIpMapping(id, nodeInterface, ipAddress, port);
					success = ipFactory.save(ip);

					if (!success) {
						status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
								AdapterConstants.ARTICLE_NODE,
								"Insert/update of node/IP address mapping into the Registry failed", correlId);
					}
				}
			}

		} catch (Exception e) {

			String message = e.getClass().getName() + ": " + e.getMessage();
			status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
					AdapterConstants.ARTICLE_NODE, message, correlId);

		}

		return status.toJsonObject();
	}

	public static String getNode() {

		return homeNode;
	}

	public static void setNode(String homeNode) {

		Nodes.homeNode = homeNode;
	}

	/**
	 * Deletes a Node from the registry.
	 * 
	 * @param nodeId
	 *            The ID of the node to be deleted.
	 * @param correlId
	 *            The correlation ID for the status message.
	 * @return the JSON message with the status of the operation.
	 */
	public static JSON deregister(String nodeId, String correlId) {

		AdapterStatus status = new AdapterStatus(correlId);

		if (nodeId == null) {

			status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_DEREGISTER,
					AdapterConstants.ARTICLE_NODE, AdapterConstants.STATUS_MSG_MISSING_FIELDS, correlId);

		}

		NodeFactory nodeFactory = FabricRegistry.getNodeFactory(true);
		Node node = nodeFactory.getNodeById(nodeId);
		boolean success = nodeFactory.delete(node);

		if (success == false) {
			status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_DEREGISTER,
					AdapterConstants.ARTICLE_NODE, AdapterConstants.STATUS_MSG_FAILED_DELETE, correlId);
		}

		return status.toJsonObject();
	}

	/**
	 * Returns the local node ID
	 * 
	 * @param correlId
	 * @return the JSON message with the local node ID.
	 */

	public static JSON queryLocalNode(final String correlId) {

		String nodeID = getNode();
		JSON nodeIDQueryResult = new JSON();
		nodeIDQueryResult.putString(AdapterConstants.FIELD_OPERATION, AdapterConstants.OP_QUERY_RESPONSE_LOCAL_NODE);
		nodeIDQueryResult.putString(AdapterConstants.FIELD_CORRELATION_ID, correlId);
		nodeIDQueryResult.putString(AdapterConstants.FIELD_ID, nodeID);
		return nodeIDQueryResult;

	}

	/**
	 * Queries the database for nodes.
	 * 
	 * @param jsonOpObject
	 * @param correlId
	 * @return the JSON message with the query result.
	 */
	public static JSON query(final JSON jsonOpObject, final String correlId) {

		JSON nodesQueryResult = new JSON();
		AdapterStatus status = new AdapterStatus(correlId);
		List<JSON> jsonList = new ArrayList<JSON>();

		try {

			NodeFactory nodeFactory = FabricRegistry.getNodeFactory();
			String querySQL = queryNodesSQL(jsonOpObject);

			if (querySQL == null) {

				status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_QUERY,
						AdapterConstants.ARTICLE_NODE, AdapterConstants.STATUS_MSG_BAD_SQL, correlId);
				nodesQueryResult = status.toJsonObject();

			} else {

				/* Lookup the system list in the Registry */
				Node[] resultArray = null;

				if ("".equals(querySQL)) {
					resultArray = nodeFactory.getAllNodes();
				} else {
					resultArray = nodeFactory.getNodes(querySQL);
				}
				// TODO IP search
				// Query by IP address

				/* Generate the response object */

				nodesQueryResult.putString(AdapterConstants.FIELD_OPERATION, AdapterConstants.OP_QUERY_RESPONSE_NODES);
				nodesQueryResult.putString(AdapterConstants.FIELD_CORRELATION_ID, correlId);

				NodeIpMapping[] ipMap = null;

				/* For each Node... */
				for (int i = 0; i < resultArray.length; i++) {

					JSON system = new JSON();

					system.putString(AdapterConstants.FIELD_ID, resultArray[i].getId());
					ipMap = resultArray[i].getAllIpMappings();
					List<JSON> ipArray = new ArrayList<JSON>();
					for (int j = 0; j < ipMap.length; j++) {
						NodeIpMapping nodeIpMapping = ipMap[j];
						JSON ip = new JSON("ip://" + nodeIpMapping.getIpAddress() + ":" + nodeIpMapping.getPort());
						ipArray.add(ip);
					}
					system.putArray(AdapterConstants.FIELD_ADDRESS, ipArray);

					system.putString(AdapterConstants.FIELD_DESCRIPTION, resultArray[i].getDescription());

					String attributes = resultArray[i].getAttributes();
					if (attributes != null && !attributes.equals("null")) {
						JSON attributesJson = new JSON(attributes);
						system.putJSON(AdapterConstants.FIELD_ATTRIBUTES, attributesJson);
					}

					jsonList.add(system);
				}
				nodesQueryResult = nodesQueryResult.putArray(AdapterConstants.FIELD_NODES, jsonList);
			}

		} catch (Exception e) {

			String message = e.getClass().getName() + ": " + e.getMessage();
			status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_QUERY,
					AdapterConstants.ARTICLE_NODE, message, correlId);
			nodesQueryResult = status.toJsonObject();

		}

		return nodesQueryResult;
	}

	/**
	 * Returns the SQL corresponding to the Systems query.
	 * 
	 * @param jsonOpObject
	 *            The JSON object that needs converting to SQL.
	 * 
	 * @return The SQL for the query.
	 */
	private static String queryNodesSQL(JSON jsonOpObject) {

		String querySQL = null;
		StringBuilder s = new StringBuilder();

		try {

			if (jsonOpObject.getString(AdapterConstants.FIELD_ID) != null) {

				s.append("NODE_ID='");
				s.append(jsonOpObject.getString(AdapterConstants.FIELD_ID));
				s.append("' AND ");

			}

			s.append(JsonUtils.generalSQLLogic(jsonOpObject));

			querySQL = s.toString();

			/* Removes trailing AND in SQL query */
			if (querySQL.endsWith(" AND ")) {
				querySQL = querySQL.substring(0, querySQL.length() - 5);
			}

		} catch (Exception e) {

			querySQL = null;

		}

		return querySQL;
	}
}
