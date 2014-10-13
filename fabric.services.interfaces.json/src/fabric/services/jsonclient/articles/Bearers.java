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
import fabric.registry.Bearer;
import fabric.registry.BearerFactory;
import fabric.registry.FabricRegistry;
import fabric.services.json.JSON;
import fabric.services.jsonclient.utilities.AdapterConstants;
import fabric.services.jsonclient.utilities.AdapterStatus;
import fabric.services.jsonclient.utilities.JsonUtils;

/**
 * Class that handles JSON commands that deal with Nodes.
 */
public class Bearers extends FabricBus {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/*
	 * Class methods
	 */

	/**
	 * Inserts a Bearer into the registry.
	 * 
	 * @param op
	 *            the full JSON operation object.
	 * 
	 * @param correlId
	 *            the operation correlation ID.
	 * 
	 * @return the JSON message with the status of the operation.
	 */

	public static JSON register(JSON op, String correlId) {

		AdapterStatus status = new AdapterStatus(correlId);

		try {

			String id = op.getString(AdapterConstants.FIELD_ID);
			String available = op.getString(AdapterConstants.FIELD_AVAILABILITY);

			if (id == null || id.length() == 0 || available == null
					|| (!available.equals("true") && !available.equals("false"))) {

				status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_REGISTER,
						AdapterConstants.ARTICLE_BEARER, AdapterConstants.STATUS_MSG_MISSING_FIELDS, correlId);

			} else {

				String availability = available.equals("true") ? "AVAILABLE" : "UNAVAILABLE";
				String description = op.getString(AdapterConstants.FIELD_DESCRIPTION);
				String attributes = op.getJSON(AdapterConstants.FIELD_ATTRIBUTES).toString();

				BearerFactory bearerFactory = FabricRegistry.getBearerFactory();
				Bearer bearer = bearerFactory.createBearer(id, availability, description, attributes, null);
				boolean success = bearerFactory.save(bearer);

				if (!success) {

					status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
							AdapterConstants.ARTICLE_BEARER, "Insert/update of bearer into the Registry failed",
							correlId);
				}
			}

		} catch (Exception e) {

			String message = e.getClass().getName() + ": " + e.getMessage();
			status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
					AdapterConstants.ARTICLE_BEARER, message, correlId);

		}

		return status.toJsonObject();
	}

	/**
	 * Deletes a Bearer from the registry.
	 * 
	 * @param bearerID
	 *            The ID of the node to be deleted.
	 * @param correlId
	 *            The correlation ID for the status message.
	 * @return the JSON message with the status of the operation.
	 */
	public static JSON deregister(String bearerID, String correlId) {

		AdapterStatus status = new AdapterStatus(correlId);

		if (bearerID == null) {

			status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_DEREGISTER,
					AdapterConstants.ARTICLE_BEARER, AdapterConstants.STATUS_MSG_MISSING_FIELDS, correlId);

		}

		BearerFactory bearerFactory = FabricRegistry.getBearerFactory(true);
		Bearer bearer = bearerFactory.getBearerById(bearerID);
		boolean success = bearerFactory.delete(bearer);

		if (success == false) {
			status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_DEREGISTER,
					AdapterConstants.ARTICLE_BEARER, AdapterConstants.STATUS_MSG_FAILED_DELETE, correlId);
		}

		return status.toJsonObject();
	}

	/**
	 * Queries the database for bearers.
	 * 
	 * @param op
	 * @param correlId
	 * @return the JSON message with the query result.
	 */
	public static JSON query(final JSON op, final String correlId) {

		JSON bearersQueryResult = new JSON();
		AdapterStatus status = new AdapterStatus(correlId);
		List<JSON> jsonList = new ArrayList<JSON>();

		try {

			BearerFactory bearerFactory = FabricRegistry.getBearerFactory();
			String querySQL = bearerNodesSQL(op);

			if (querySQL == null) {

				status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_QUERY,
						AdapterConstants.ARTICLE_BEARER, AdapterConstants.STATUS_MSG_BAD_SQL, correlId);
				bearersQueryResult = status.toJsonObject();

			} else {

				/* Lookup the bearer list in the Registry */
				Bearer[] resultArray = null;

				if ("".equals(querySQL)) {
					resultArray = bearerFactory.getAllBearers();
				} else {
					resultArray = bearerFactory.getBearers(querySQL);
				}

				/* Generate the response object */

				bearersQueryResult.putString(AdapterConstants.FIELD_OPERATION,
						AdapterConstants.OP_QUERY_RESPONSE_BEARERS);
				bearersQueryResult.putString(AdapterConstants.FIELD_CORRELATION_ID, correlId);

				/* For each bearer... */
				for (int i = 0; i < resultArray.length; i++) {

					Boolean available = "AVAILABLE".equals(resultArray[i].getAvailability()) ? true : false;

					JSON bearer = new JSON();
					bearer.putString(AdapterConstants.FIELD_ID, resultArray[i].getId());
					bearer.putString(AdapterConstants.FIELD_AVAILABILITY, available.toString());
					bearer.putString(AdapterConstants.FIELD_DESCRIPTION, resultArray[i].getDescription());

					String attributes = resultArray[i].getAttributes();
					if (attributes != null && !attributes.equals("null")) {
						JSON attributesJson = JsonUtils.stringTOJSON(attributes,
								"Attribute value is not a valid JSON object");
						bearer.putJSON(AdapterConstants.FIELD_ATTRIBUTES, attributesJson);
					}

					jsonList.add(bearer);
				}
				bearersQueryResult = bearersQueryResult.putArray(AdapterConstants.FIELD_BEARERS, jsonList);
			}

		} catch (Exception e) {

			String message = e.getClass().getName() + ": " + e.getMessage();
			status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_QUERY,
					AdapterConstants.ARTICLE_BEARER, message, correlId);
			bearersQueryResult = status.toJsonObject();

		}

		return bearersQueryResult;
	}

	/**
	 * Returns the SQL corresponding to the Bearers query.
	 * 
	 * @param op
	 *            The JSON object that needs converting to SQL.
	 * 
	 * @return The SQL for the query.
	 */
	private static String bearerNodesSQL(JSON op) {

		String querySQL = null;
		StringBuilder s = new StringBuilder();

		try {

			if (op.getString(AdapterConstants.FIELD_ID) != null) {

				s.append("BEARER_ID='");
				s.append(op.getString(AdapterConstants.FIELD_ID));
				s.append("' AND ");

			}

			s.append(JsonUtils.generateSQLLogic(op));

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
