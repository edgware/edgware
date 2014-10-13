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
import fabric.registry.Actor;
import fabric.registry.ActorFactory;
import fabric.registry.FabricRegistry;
import fabric.services.json.JSON;
import fabric.services.jsonclient.utilities.AdapterConstants;
import fabric.services.jsonclient.utilities.AdapterStatus;
import fabric.services.jsonclient.utilities.JsonUtils;

/**
 * Class that handles JSON commands that deal with Users (formerly Actors).
 */
public class Users extends FabricBus {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/**
	 * Inserts a User into the registry.
	 * 
	 * @param op
	 *            The full JSON operation object.
	 * 
	 * @param correlId
	 *            The correlation ID of the request.
	 * 
	 * @return A JSON status object.
	 */
	public static JSON register(final JSON jsonOpObject, String correlId) {

		AdapterStatus status = new AdapterStatus(correlId);

		try {

			String id = jsonOpObject.getString(AdapterConstants.FIELD_ID);
			String type = jsonOpObject.getString(AdapterConstants.FIELD_TYPE);

			if (id == null || type == null) {

				status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_REGISTER,
						AdapterConstants.ARTICLE_USER, AdapterConstants.STATUS_MSG_MISSING_FIELDS, correlId);

			} else {

				ActorFactory actorFactory = FabricRegistry.getActorFactory();
				Actor actor = actorFactory.createActor(id, type, null, // roles
						null, // credentials
						jsonOpObject.getString(AdapterConstants.FIELD_AFFIL), jsonOpObject
								.getString(AdapterConstants.FIELD_DESCRIPTION), jsonOpObject
								.getString(AdapterConstants.FIELD_ATTRIBUTES), null); // attributesURI
				boolean success = actorFactory.save(actor);

				if (!success) {

					status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
							AdapterConstants.ARTICLE_USER, "Insert/update of platform type into the Registry failed",
							correlId);

				}
			}

		} catch (Exception e) {

			String message = e.getClass().getName() + ": " + e.getMessage();
			status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
					AdapterConstants.ARTICLE_USER, message, correlId);

		}

		return status.toJsonObject();
	}

	/**
	 * Deletes a User from the registry.
	 * 
	 * @param userId
	 *            The ID of the User to be deleted.
	 * @param correlId
	 *            The correlation ID for the status message.
	 * @return A JSON status object.
	 */
	public static JSON deregister(String userId, String correlId) {

		AdapterStatus status = new AdapterStatus(correlId);
		if (userId == null) {
			status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_DEREGISTER,
					AdapterConstants.ARTICLE_USER_TYPE, AdapterConstants.STATUS_MSG_MISSING_FIELDS, correlId);
		} else {
			ActorFactory actorFactory = FabricRegistry.getActorFactory(true);
			Actor actor = actorFactory.getActorById(userId);
			boolean complete = actorFactory.delete(actor);
			if (complete == false) {
				status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_DEREGISTER,
						AdapterConstants.ARTICLE_USER, AdapterConstants.STATUS_MSG_FAILED_DELETE, correlId);
			}
		}
		return status.toJsonObject();
	}

	/**
	 * Queries the database for Users.
	 * 
	 * @param jsonOpObject
	 * @param correlId
	 * @return The result of the query.
	 */

	public static JSON query(JSON jsonOpObject, final String correlId) {

		JSON usersQueryResult = new JSON();
		AdapterStatus status = new AdapterStatus(correlId);
		List<JSON> jsonList = new ArrayList<JSON>();

		try {
			ActorFactory userFactory = FabricRegistry.getActorFactory();
			String querySQL = queryUsersSQL(jsonOpObject);

			if (querySQL == null) {
				status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_QUERY,
						AdapterConstants.ARTICLE_USER, AdapterConstants.STATUS_MSG_BAD_SQL, correlId);
				usersQueryResult = status.toJsonObject();
			} else {

				Actor[] resultArray = null;

				if ("".equals(querySQL)) {
					resultArray = userFactory.getAllActors();
				} else {
					resultArray = userFactory.getActors(querySQL);
				}

				usersQueryResult.putString(AdapterConstants.FIELD_OPERATION, AdapterConstants.OP_QUERY_RESPONSE_USERS);
				usersQueryResult.putString(AdapterConstants.FIELD_CORRELATION_ID, correlId);

				for (int i = 0; i < resultArray.length; i++) {
					JSON user = new JSON();
					user.putString(AdapterConstants.FIELD_ID, resultArray[i].getId());
					user.putString(AdapterConstants.FIELD_TYPE, resultArray[i].getTypeId());
					user.putString(AdapterConstants.FIELD_DESCRIPTION, resultArray[i].getDescription());

					String attributes = resultArray[i].getAttributes();
					if (attributes != null && !attributes.equals("null")) {
						JSON attributesJson = JsonUtils.stringTOJSON(attributes,
								"Attribute value is not a valid JSON object");
						user.putJSON(AdapterConstants.FIELD_ATTRIBUTES, attributesJson);
					}

					jsonList.add(user);
				}
				usersQueryResult = usersQueryResult.putArray(AdapterConstants.FIELD_USERS, jsonList);
			}
		} catch (Exception e) {
			String message = e.getClass().getName() + ": " + e.getMessage();
			status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_QUERY,
					AdapterConstants.ARTICLE_USER, message, correlId);
			usersQueryResult = status.toJsonObject();
		}
		return usersQueryResult;
	}

	/**
	 * Returns the SQL relevant to the Users' query.
	 * 
	 * @param jsonOpObject
	 *            The JSON object that needs converting to SQL.
	 * @return The SQL for the query.
	 */
	private static String queryUsersSQL(JSON jsonOpObject) {

		String querySQL = null;
		StringBuilder s = new StringBuilder();
		try {
			if (jsonOpObject.getString(AdapterConstants.FIELD_ID) != null) {
				s.append("ACTOR_ID='");
				s.append(jsonOpObject.getString(AdapterConstants.FIELD_ID));
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
