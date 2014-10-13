/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.jsonclient.articles;

import fabric.FabricBus;
import fabric.registry.FabricRegistry;
import fabric.registry.Platform;
import fabric.registry.PlatformFactory;
import fabric.services.json.JSON;
import fabric.services.json.JSONArray;
import fabric.services.jsonclient.utilities.AdapterConstants;
import fabric.services.jsonclient.utilities.AdapterStatus;
import fabric.services.jsonclient.utilities.JsonUtils;

/**
 * Class that handles JSON commands that deal with Platforms.
 */
public class Platforms extends FabricBus {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	public static String homeNode = null;

	/*
	 * Class methods
	 */

	public static String getNode() {

		return homeNode;
	}

	public static void setNode(String homeNode) {

		Platforms.homeNode = homeNode;
	}

	/**
	 * Inserts a Platform into the registry.
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

			String platformId = jsonOpObject.getString(AdapterConstants.FIELD_ID);
			String platformTypeId = jsonOpObject.getString(AdapterConstants.FIELD_TYPE);

			if (platformId == null || platformTypeId == null) {

				status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_REGISTER,
						AdapterConstants.ARTICLE_PLATFORM, AdapterConstants.STATUS_MSG_MISSING_FIELDS, correlId);

			} else {

				JSON location = jsonOpObject.getJSON(AdapterConstants.FIELD_LOCATION);

				PlatformFactory platformFactory = FabricRegistry.getPlatformFactory();

				Double lat = location.getDouble(AdapterConstants.FIELD_LATITUDE);
				Double lon = location.getDouble(AdapterConstants.FIELD_LONGITUDE);
				Double alt = location.getDouble(AdapterConstants.FIELD_ALTITUDE);

				Platform platform = platformFactory.createPlatform(platformId, platformTypeId, getNode(), null, // affiliation
						null, // Security classification,
						"DEPLOYED", // Readiness,
						"AVAILABLE", // Availability,
						(lat.equals(Double.NaN)) ? 0.0 : lat, // Latitude
						(lon.equals(Double.NaN)) ? 0.0 : lon, // Longitude
						(alt.equals(Double.NaN)) ? 0.0 : alt, // Altitude
						0.0, // Bearing
						0.0, // Velocity
						jsonOpObject.getString(AdapterConstants.FIELD_DESCRIPTION), // Description
						jsonOpObject.getString(AdapterConstants.FIELD_ATTRIBUTES), // Attributes
						null); // Attributes URI
				boolean success = platformFactory.save(platform);

				if (!success) {

					status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
							AdapterConstants.ARTICLE_PLATFORM, "Insert/update of platform into the Registry failed",
							correlId);

				}
			}

		} catch (Exception e) {

			String message = e.getClass().getName() + ": " + e.getMessage();
			status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
					AdapterConstants.ARTICLE_PLATFORM, message, correlId);

		}

		return status.toJsonObject();
	}

	/**
	 * Deletes a Platform from the registry.
	 * 
	 * @param platformId
	 *            The ID of the platform to be deleted.
	 * @param correlId
	 *            The correlation ID for the status message.
	 * @return A status code.
	 */
	public static JSON deregister(String platformId, String correlId) {

		AdapterStatus status = new AdapterStatus(correlId);

		if (platformId == null) {
			status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_DEREGISTER,
					AdapterConstants.ARTICLE_PLATFORM, AdapterConstants.STATUS_MSG_MISSING_FIELDS, correlId);
		} else {
			PlatformFactory platformFactory = FabricRegistry.getPlatformFactory(true);
			Platform platform = platformFactory.getPlatformById(platformId);
			boolean complete = platformFactory.delete(platform);
			if (complete == false) {
				status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_DEREGISTER,
						AdapterConstants.ARTICLE_PLATFORM, AdapterConstants.STATUS_MSG_FAILED_DELETE, correlId);
			}
		}
		return status.toJsonObject();
	}

	/**
	 * Returns the result of the Systems query a JSON Object.
	 * 
	 * @param correlId
	 *            The correlation ID of the request.
	 */
	public static JSON query(JSON jsonOpObject, final String correlId) {

		JSON platformsQueryResult = new JSON();
		AdapterStatus status = new AdapterStatus(correlId);
		JSONArray platformList = new JSONArray();

		try {

			PlatformFactory platformFactory = FabricRegistry.getPlatformFactory();
			String querySQL = queryPlatformsSQL(jsonOpObject);

			if (querySQL == null) {
				status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_QUERY,
						AdapterConstants.ARTICLE_PLATFORM, AdapterConstants.STATUS_MSG_BAD_SQL, correlId);
				platformsQueryResult = status.toJsonObject();
			} else {

				Platform[] resultArray = null;

				if ("".equals(querySQL)) {
					resultArray = platformFactory.getAllPlatforms();
				} else {
					resultArray = platformFactory.getPlatforms(querySQL);
				}

				platformsQueryResult.putString(AdapterConstants.FIELD_OPERATION,
						AdapterConstants.OP_QUERY_RESPONSE_PLATFORMS);
				platformsQueryResult.putString(AdapterConstants.FIELD_CORRELATION_ID, correlId);

				for (int i = 0; status.isOK() && i < resultArray.length; i++) {

					JSON nextPlatform = new JSON();

					nextPlatform.putString(AdapterConstants.FIELD_ID, resultArray[i].getId());
					nextPlatform.putString(AdapterConstants.FIELD_TYPE, resultArray[i].getTypeId());
					nextPlatform
							.putJSON(AdapterConstants.FIELD_LOCATION, JsonUtils.buildLocationObject(resultArray[i]));

					String description = resultArray[i].getDescription();
					if (description != null && !description.equals("null")) {
						nextPlatform.putString(AdapterConstants.FIELD_DESCRIPTION, description);
					}

					String attributes = resultArray[i].getAttributes();
					if (attributes != null && !attributes.equals("null")) {
						JSON attributesJson = JsonUtils.stringTOJSON(attributes,
								"Attribute value is not a valid JSON object");
						nextPlatform.putJSON(AdapterConstants.FIELD_ATTRIBUTES, attributesJson);
					}

					platformList.add(nextPlatform);
				}

				platformsQueryResult.putJSONArray(AdapterConstants.FIELD_PLATFORMS, platformList);
			}
		} catch (Exception e) {
			String message = e.getClass().getName() + ": " + e.getMessage();
			status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_QUERY,
					AdapterConstants.ARTICLE_PLATFORM, message, correlId);
			platformsQueryResult = status.toJsonObject();
		}

		return platformsQueryResult;
	}

	/**
	 * Returns the SQL relevant to the Platforms query.
	 * 
	 * @param jsonOpObject
	 *            The JSON object that needs converting to SQL.
	 * @return The SQL for the query.
	 */
	private static String queryPlatformsSQL(JSON jsonOperationObject) {

		String querySQL = null;
		StringBuilder s = new StringBuilder();
		try {
			if (jsonOperationObject.getString(AdapterConstants.FIELD_ID) != null) {
				s.append("PLATFORM_ID='");
				s.append(jsonOperationObject.getString(AdapterConstants.FIELD_ID));
				s.append("' AND ");
			}
			s.append(JsonUtils.generateSQLLogic(jsonOperationObject));
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