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

import fabric.registry.FabricRegistry;
import fabric.registry.Type;
import fabric.registry.TypeFactory;
import fabric.services.json.JSON;
import fabric.services.jsonclient.utilities.AdapterConstants;
import fabric.services.jsonclient.utilities.AdapterStatus;
import fabric.services.jsonclient.utilities.JsonUtils;

/**
 * Class that handles JSON commands that deal with Node Types.
 */
public class NodeTypes {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/**
	 * Inserts a Node Type into the registry.
	 * 
	 * @param op
	 *            The full JSON operation object.
	 * 
	 * @param correlId
	 *            The correlation ID of the request.
	 * 
	 * @return A JSON status object.
	 */
	public static JSON register(JSON jsonOpObject, String correlId) {

		AdapterStatus status = new AdapterStatus(correlId);

		try {

			String typeid = jsonOpObject.getString(AdapterConstants.FIELD_TYPE);

			if (typeid == null) {

				status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_REGISTER,
						AdapterConstants.ARTICLE_NODE_TYPE, AdapterConstants.STATUS_MSG_MISSING_FIELDS, correlId);

			} else {

				TypeFactory typeFactory = FabricRegistry.getTypeFactory();
				Type type = typeFactory.createNodeType(typeid, jsonOpObject
						.getString(AdapterConstants.FIELD_DESCRIPTION), jsonOpObject
						.getString(AdapterConstants.FIELD_ATTRIBUTES), null); // attributesURI
				typeFactory.save(type);

			}

		} catch (Exception e) {

			String message = e.getClass().getName() + ": " + e.getMessage();
			status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
					AdapterConstants.ARTICLE_NODE_TYPE, message, correlId);

		}

		return status.toJsonObject();
	}

	/**
	 * Deletes a Node from the registry.
	 * 
	 * @param nodeTypeId
	 *            The ID of the Node to be deleted.
	 * @return A JSON status object.
	 */
	public static JSON deregister(String nodeTypeId, String correlId) {

		AdapterStatus status = new AdapterStatus(correlId);
		if (nodeTypeId == null) {
			status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_DEREGISTER,
					AdapterConstants.ARTICLE_NODE_TYPE, AdapterConstants.STATUS_MSG_MISSING_FIELDS, correlId);
		} else {
			TypeFactory typeFactory = FabricRegistry.getTypeFactory(true);
			Type NodeType = typeFactory.getNodeType(nodeTypeId);
			boolean complete = typeFactory.delete(NodeType);
			if (complete == false) {
				status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_DEREGISTER,
						AdapterConstants.ARTICLE_NODE_TYPE, AdapterConstants.STATUS_MSG_FAILED_DELETE, correlId);
			}
		}
		return status.toJsonObject();
	}

	/**
	 * Queries the database for Node types.
	 * 
	 * @param jsonOpObject
	 * @param correlId
	 * @return The result of the query.
	 */
	public static JSON query(final JSON jsonOpObject, final String correlId) {

		JSON nodeTypesQueryResult = new JSON();
		AdapterStatus status = new AdapterStatus(correlId);
		List<JSON> jsonList = new ArrayList<JSON>();

		try {
			TypeFactory typeFactory = FabricRegistry.getTypeFactory();
			String querySQL = queryNodeTypesSQL(jsonOpObject);
			if (querySQL == null) {
				status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_QUERY,
						AdapterConstants.ARTICLE_NODE_TYPE, AdapterConstants.STATUS_MSG_BAD_SQL, correlId);
				nodeTypesQueryResult = status.toJsonObject();
			} else {

				Type[] resultArray = null;

				if ("".equals(querySQL)) {
					resultArray = typeFactory.getAllNodeTypes();
				} else {
					resultArray = typeFactory.getNodeTypes(querySQL);
				}

				nodeTypesQueryResult.putString(AdapterConstants.FIELD_OPERATION,
						AdapterConstants.OP_QUERY_RESPONSE_NODE_TYPES);
				nodeTypesQueryResult.putString(AdapterConstants.FIELD_CORRELATION_ID, correlId);

				for (int i = 0; i < resultArray.length; i++) {

					JSON type = new JSON();

					type = type.putString(AdapterConstants.FIELD_TYPE, resultArray[i].getId());
					type = type.putString(AdapterConstants.FIELD_DESCRIPTION, resultArray[i].getDescription());

					String attributes = resultArray[i].getAttributes();
					if (attributes != null && !attributes.equals("null")) {
						JSON attributesJson = JsonUtils.stringTOJSON(attributes,
								"Attribute value is not a valid JSON object");
						type.putJSON(AdapterConstants.FIELD_ATTRIBUTES, attributesJson);
					}

					jsonList.add(type);
				}
				nodeTypesQueryResult = nodeTypesQueryResult.putArray(AdapterConstants.FIELD_NODE_TYPES, jsonList);
			}
		} catch (Exception e) {
			String message = e.getClass().getName() + ": " + e.getMessage();
			status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_QUERY,
					AdapterConstants.ARTICLE_NODE_TYPE, message, correlId);
			nodeTypesQueryResult = status.toJsonObject();
		}
		return nodeTypesQueryResult;
	}

	/**
	 * Returns the SQL relevant to the Node Type query.
	 * 
	 * @param jsonOpObject
	 *            The JSON object that needs converting to SQL.
	 * @return The SQL for the query.
	 */
	private static String queryNodeTypesSQL(JSON jsonOpObject) {

		String querySQL = "";
		StringBuilder s = new StringBuilder();
		try {
			if (jsonOpObject.getString(AdapterConstants.FIELD_TYPE) != null) {
				s.append("TYPE_ID='");
				s.append(jsonOpObject.getString(AdapterConstants.FIELD_TYPE));
				s.append("' AND ");
			}
			s.append(JsonUtils.generateSQLLogic(jsonOpObject));
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
