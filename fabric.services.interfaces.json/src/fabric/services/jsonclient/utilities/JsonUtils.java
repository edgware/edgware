/*
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.jsonclient.utilities;

import java.io.IOException;

import fabric.services.json.JSON;
import fabric.services.json.JSONArray;

/**
 * This class provides some null gating utilities for JSON objects.
 */
public class JsonUtils {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/*
	 * Class methods
	 */

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
	 * Answers a JSON object corresponding to a string containing JSON.
	 * <p>
	 * If the string does not contain valid JSON then the specified error message is returned in a JSON object of the
	 * form:
	 * </p>
	 * 
	 * <pre>
	 * {
	 *    "$error":"&lt;errorMessage&gt;",
	 *    "$value":"&lt;jsonString&gt;"
	 * }
	 * </pre>
	 * 
	 * @param jsonString
	 *            the JSON string to parse into a JSON object.
	 * 
	 * @param errorMessage
	 *            the error message to use.
	 * 
	 * @return the JSON object, or an error message encoded in a JSON object, or <code>null</code> if there are JSON
	 *         parsing errors building the return value.
	 */
	public static JSON stringTOJSON(String jsonString, String errorMessage) {

		JSON json = null;

		try {

			if (jsonString != null && !jsonString.equals("null") && !jsonString.equals("")) {
				json = new JSON(jsonString);
			}

		} catch (Exception e) {

			try {
				String errorJSON = String.format("{\"$error\":\"%s\",\"$value\":\"%s\"}", errorMessage, jsonString);
				json = new JSON(errorJSON);
			} catch (IOException e1) {
				/* Just return null */
			}

		}

		return json;
	}
}
