/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.jsonclient.utilities;

import fabric.registry.Platform;
import fabric.services.json.JSON;
import fabric.services.json.JSONArray;

/**
 * This class provides some null gating utilities for JSON objects.
 */
public class JsonUtils {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/**
	 * Catches any exceptions and returns a null. Used if the key doesn't exist in the JSON object.
	 * 
	 * @param jsonObj
	 *            The full JSON operation object.
	 * @return String
	 */
	@Deprecated
	public static String getString(String key, JSON jsonObj) {

		try {
			return jsonObj.getString(key);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Catches any exceptions and returns a null. Used if the key doesn't exist in the JSON object.
	 * 
	 * @param jsonObj
	 *            The full JSON operation object.
	 * @return double
	 */
	@Deprecated
	public static double getDouble(String key, JSON jsonObj) {

		try {
			return jsonObj.getDouble(key);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Catches any exceptions and returns a null. Used if the key doesn't exist in the JSON object.
	 * 
	 * @param jsonObj
	 *            The full JSON operation object.
	 * @return JSONObject
	 */
	@Deprecated
	public static JSON getJsonObject(String key, JSON jsonObj) {

		try {
			return jsonObj.getJSON(key);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Catches any exceptions and returns a null. Used if the key doesn't exist in the JSON object.
	 * 
	 * @param jsonObj
	 *            The full JSON operation object.
	 * @return JSONObject
	 */
	@Deprecated
	public static JSONArray getJsonArray(String key, JSON jsonObj) {

		try {
			return jsonObj.getJSONArray(key);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns the modified JSON object only if the field is not null. Else return the original object.
	 * 
	 * @param key
	 *            The key to be used.
	 * @param value
	 *            The value to be stored.
	 * @param system
	 *            The JSON Object being constructed.
	 */
	@Deprecated
	public static JSON buildJson(String key, JSON value, JSON object) {

		if (value != null) {
			object.putJSON(key, value);
		}
		return object;
	}

	/**
	 * Returns the modified JSON object only if the field is not null. Else return the original object.
	 * 
	 * @param key
	 *            The key to be used.
	 * @param value
	 *            The value to be stored.
	 * @param system
	 *            The JSON Object being constructed.
	 */
	@Deprecated
	public static JSON buildJson(String key, double value, JSON object) {

		if (value != 0) {
			object.putDouble(key, value);
		}
		return object;
	}

	/**
	 * Returns the modified JSON object only if the field is not null. Else return the original object.
	 * 
	 * @param key
	 *            The key to be used.
	 * @param value
	 *            The value to be stored.
	 * @param system
	 *            The JSON Object being constructed.
	 */
	@Deprecated
	public static JSON buildJson(String key, String value, JSON object) {

		if (value != null && !value.equals("null")) {
			object.putString(key, value);
		}

		return object;

	}

	/**
	 * Creates a JSON object with location information pulled from a Platform object.
	 * 
	 * @param platform
	 *            The platform object that contains location information.
	 * @return A location JSON Object.
	 */
	public static JSON buildLocationObject(Platform platform) {

		JSON location = new JSON();
		location = location.putDouble(AdapterConstants.FIELD_LATITUDE, platform.getLatitude());
		location = location.putDouble(AdapterConstants.FIELD_LONGITUDE, platform.getLongitude());
		if (location.toString().equals("{}")) {
			location = null;
		}
		return location;
	}

	/**
	 * Generates SQL for a generic query type from any JSON object
	 * 
	 * @param jsonOpbject
	 *            The JSON object from which the query data is taken.
	 * 
	 * @return A string containing SQL.
	 */
	public static String generalSQLLogic(JSON jsonOpbject) {

		String querySQL = null;
		StringBuilder s = new StringBuilder();

		/* If the query contains a type ID... */
		if (jsonOpbject.getString(AdapterConstants.FIELD_TYPE) != null) {
			s.append("TYPE_ID='");
			s.append(jsonOpbject.getString(AdapterConstants.FIELD_TYPE));
			s.append("' AND ");
		}

		/* If the query contains a location.. */
		if (!jsonOpbject.getJSON(AdapterConstants.FIELD_LOCATION).toString().equals("{}")) {
			JSON jsonLocation = jsonOpbject.getJSON(AdapterConstants.FIELD_LOCATION);
			s.append("LATITUDE>=");
			s.append(jsonLocation.getDouble(AdapterConstants.FIELD_LOCATION_BOTTOM));
			s.append(" AND LATITUDE<=");
			s.append(jsonLocation.getDouble(AdapterConstants.FIELD_LOCATION_TOP));
			s.append(" AND ");
			s.append("LONGITUDE>=");
			s.append(jsonLocation.getDouble(AdapterConstants.FIELD_LOCATION_LEFT));
			s.append(" AND LONGITUDE<=");
			s.append(jsonLocation.getDouble(AdapterConstants.FIELD_LOCATION_RIGHT));
			s.append(" AND ");
		}

		String attributes = jsonOpbject.getJSON(AdapterConstants.FIELD_ATTRIBUTES).toString();
		attributes = attributes.substring(attributes.indexOf('{') + 1, attributes.lastIndexOf('}'));

		/* If the query contains attributes... */
		if (jsonOpbject.getString(AdapterConstants.FIELD_ATTRIBUTES) != null) {
			s.append("ATTRIBUTES LIKE '%");
			s.append(attributes);
			s.append("%' AND ");
		}

		/* If the query contains a description... */
		if (jsonOpbject.getString(AdapterConstants.FIELD_DESCRIPTION) != null) {
			s.append("DESCRIPTION='");
			s.append(jsonOpbject.getString(AdapterConstants.FIELD_DESCRIPTION));
			s.append("' AND ");
		}

		querySQL = s.toString();
		return querySQL;
	}
}
