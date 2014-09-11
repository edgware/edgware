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
 * Class that handles JSON commands that deal with Platform Types.
 */
public class PlatformTypes {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/*
	 * Class methods
	 */

	/**
	 * Inserts a Platform Type into the registry
	 * 
	 * @param jsonOpObject
	 *            the full JSON operation object.
	 * 
	 * @param correlId
	 *            the operation correlation ID.
	 * 
	 * @return the status of the operation.
	 */
	public static JSON register(JSON jsonOpObject, String correlId) {

		AdapterStatus status = new AdapterStatus(correlId);

		try {

			String typeId = jsonOpObject.getString(AdapterConstants.FIELD_TYPE);

			if (typeId == null) {

				status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_REGISTER,
						AdapterConstants.ARTICLE_PLATFORM_TYPE, AdapterConstants.STATUS_MSG_MISSING_FIELDS, correlId);

			} else {

				TypeFactory typeFactory = FabricRegistry.getTypeFactory();
				Type type = typeFactory.createPlatformType(typeId, jsonOpObject
						.getString(AdapterConstants.FIELD_DESCRIPTION), jsonOpObject
						.getString(AdapterConstants.FIELD_ATTRIBUTES), null); // attributesURI;
				boolean success = typeFactory.save(type);

				if (!success) {

					status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
							AdapterConstants.ARTICLE_PLATFORM_TYPE,
							"Insert/update of platform type into the Registry failed", correlId);

				}
			}

		} catch (Exception e) {

			String message = e.getClass().getName() + ": " + e.getMessage();
			status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
					AdapterConstants.ARTICLE_PLATFORM_TYPE, message, correlId);

		}

		return status.toJsonObject();
	}

	/**
	 * Deletes a Platform Type from the registry.
	 * 
	 * @param platformTypeId
	 *            The ID of the platform type to be deleted.
	 * 
	 * @param correlId
	 *            The correlation ID for the status message.
	 * 
	 * @return the status of the operation.
	 */
	public static JSON deregister(final String platformTypeId, final String correlId) {

		AdapterStatus status = new AdapterStatus(correlId);

		if (platformTypeId == null) {

			status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_DEREGISTER,
					AdapterConstants.ARTICLE_PLATFORM_TYPE, AdapterConstants.STATUS_MSG_MISSING_FIELDS, correlId);

		} else {

			TypeFactory typeFactory = FabricRegistry.getTypeFactory(true);
			Type platformType = typeFactory.getPlatformType(platformTypeId);
			boolean success = typeFactory.delete(platformType);

			if (success == false) {
				status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_DEREGISTER,
						AdapterConstants.ARTICLE_PLATFORM_TYPE, AdapterConstants.STATUS_MSG_FAILED_DELETE, correlId);
			}
		}

		return status.toJsonObject();
	}

	/**
	 * Queries the database for platform types.
	 * 
	 * @param jsonOpObject
	 * @param correlId
	 * @return The result of the query.
	 */
	public static JSON query(final JSON jsonOpObject, final String correlId) {

		JSON platformTypesQueryResult = new JSON();
		AdapterStatus status = new AdapterStatus(correlId);
		List<JSON> jsonList = new ArrayList<>();

		try {
			TypeFactory typeFactory = FabricRegistry.getTypeFactory();
			String querySQL = queryPlatformTypesSQL(jsonOpObject);
			if (querySQL == null) {
				status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_QUERY,
						AdapterConstants.ARTICLE_PLATFORM_TYPE, AdapterConstants.STATUS_MSG_BAD_SQL, correlId);
				platformTypesQueryResult = status.toJsonObject();
			} else {

				Type[] resultArray = null;

				if ("".equals(querySQL)) {
					resultArray = typeFactory.getAllPlatformTypes();
				} else {
					resultArray = typeFactory.getPlatformTypes(querySQL);
				}

				platformTypesQueryResult.putString(AdapterConstants.FIELD_OPERATION,
						AdapterConstants.OP_QUERY_RESPONSE_PLATFORM_TYPES);
				platformTypesQueryResult.putString(AdapterConstants.FIELD_CORRELATION_ID, correlId);

				for (int i = 0; i < resultArray.length; i++) {

					JSON type = new JSON();

					type.putString(AdapterConstants.FIELD_TYPE, resultArray[i].getId());
					type.putString(AdapterConstants.FIELD_DESCRIPTION, resultArray[i].getDescription());

					String attributes = resultArray[i].getAttributes();
					if (attributes != null && !attributes.equals("null")) {
						JSON attributesJson = new JSON(attributes);
						type.putJSON(AdapterConstants.FIELD_ATTRIBUTES, attributesJson);
					}

					jsonList.add(type);
				}
				platformTypesQueryResult = platformTypesQueryResult.putArray(AdapterConstants.FIELD_PLATFORM_TYPES,
						jsonList);
			}
		} catch (Exception e) {
			String message = e.getClass().getName() + ": " + e.getMessage();
			status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_QUERY,
					AdapterConstants.ARTICLE_PLATFORM_TYPE, message, correlId);
			platformTypesQueryResult = status.toJsonObject();
		}
		return platformTypesQueryResult;
	}

	/**
	 * Returns the SQL relevant to the Platform Type query.
	 * 
	 * @param jsonOpObject
	 *            The JSON object that needs converting to SQL.
	 * @return The SQL for the query.
	 */
	private static String queryPlatformTypesSQL(JSON jsonOpObject) {

		String querySQL = "";
		StringBuilder s = new StringBuilder();
		try {
			if (jsonOpObject.getString(AdapterConstants.FIELD_TYPE) != null) {
				s.append("TYPE_ID='");
				s.append(jsonOpObject.getString(AdapterConstants.FIELD_TYPE));
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