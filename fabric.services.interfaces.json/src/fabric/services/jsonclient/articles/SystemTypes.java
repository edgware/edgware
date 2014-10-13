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
import java.util.Iterator;
import java.util.List;

import fabric.registry.FabricRegistry;
import fabric.registry.Type;
import fabric.registry.TypeFactory;
import fabric.services.json.JSON;
import fabric.services.json.JSONArray;
import fabric.services.jsonclient.utilities.AdapterConstants;
import fabric.services.jsonclient.utilities.AdapterStatus;
import fabric.services.jsonclient.utilities.JsonUtils;

/**
 * Class that handles JSON operations that deal with System Types.
 */
public class SystemTypes {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/**
	 * Inserts a System Type into the registry.
	 * 
	 * @param jsonOpObject
	 *            the full JSON operation object.
	 * 
	 * @param correlId
	 *            the operation correlation ID.
	 * 
	 * @return the status of the operation.
	 */
	public static JSON register(final JSON jsonOpObject, String correlId) {

		AdapterStatus status = new AdapterStatus(correlId);

		try {

			/* The get the ID of the system type */
			String typeid = jsonOpObject.getString(AdapterConstants.FIELD_TYPE);

			if (typeid == null) {

				status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_REGISTER,
						AdapterConstants.ARTICLE_SYSTEM_TYPE, AdapterConstants.STATUS_MSG_MISSING_FIELDS, correlId);

			} else {

				/* Build a comma-separated list of services associated with this system type */

				JSONArray servicesArray = jsonOpObject.getJSONArray(AdapterConstants.FIELD_SERVICES);
				StringBuilder serviceTypeList = new StringBuilder();
				boolean isValidMessage = true;

				for (Iterator<JSON> servicesIterator = servicesArray.iterator(); isValidMessage
						&& servicesIterator.hasNext();) {

					JSON nextServiceType = servicesIterator.next();
					String type = nextServiceType.getString(AdapterConstants.FIELD_TYPE);
					String mode = nextServiceType.getString(AdapterConstants.FIELD_MODE);
					mode = (mode != null) ? mode : "";
					String name = nextServiceType.getString(AdapterConstants.FIELD_ID);
					name = (name != null) ? name : type;

					switch (mode) {

					case AdapterConstants.MODE_OUTPUT:
					case AdapterConstants.MODE_INPUT:
					case AdapterConstants.MODE_NOTIFY:
					case AdapterConstants.MODE_LISTEN:
					case AdapterConstants.MODE_SOLICIT:
					case AdapterConstants.MODE_RESPONSE:
					case "":

						serviceTypeList.append(type);
						serviceTypeList.append(':');
						serviceTypeList.append(mode);
						serviceTypeList.append(':');
						serviceTypeList.append(name);
						serviceTypeList.append(",");

						break;

					default:

						status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_REGISTER,
								AdapterConstants.ARTICLE_SYSTEM_TYPE, AdapterConstants.STATUS_MSG_BAD_MODE, correlId);
						isValidMessage = false;

					}
				}

				if (isValidMessage) {

					if (serviceTypeList.toString().endsWith(",")) {
						serviceTypeList.setLength(serviceTypeList.length() - 1);
					}

					/*
					 * Add the system type to the Registry (a system's services are currently recorded in its attributes
					 * field)
					 */
					TypeFactory typeFactory = FabricRegistry.getTypeFactory();
					Type type = typeFactory.createSystemType(typeid, jsonOpObject
							.getString(AdapterConstants.FIELD_DESCRIPTION), serviceTypeList.toString(), null);
					boolean success = typeFactory.save(type);

					if (!success) {

						status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
								AdapterConstants.ARTICLE_SYSTEM_TYPE,
								"Insert/update of system type into the Registry failed", correlId);

					}
				}
			}

		} catch (Exception e) {

			String message = e.getClass().getName() + ": " + e.getMessage();
			status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
					AdapterConstants.ARTICLE_SYSTEM_TYPE, message, correlId);

		}

		return status.toJsonObject();
	}

	/**
	 * Deletes a System Type from the registry.
	 * 
	 * @param systemTypeId
	 *            The ID of the Type to be deleted.
	 * @param correlId
	 *            The correlation ID for the status message.
	 * @return A JSON status object.
	 */
	public static JSON deregister(String systemTypeId, String correlId) {

		AdapterStatus status = new AdapterStatus(correlId);

		if (systemTypeId == null) {
			status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_DEREGISTER,
					AdapterConstants.ARTICLE_SYSTEM_TYPE, AdapterConstants.STATUS_MSG_MISSING_FIELDS, correlId);
		} else {
			TypeFactory typeFactory = FabricRegistry.getTypeFactory(true);
			Type systemType = typeFactory.getSystemType(systemTypeId);
			boolean complete = typeFactory.delete(systemType);
			if (complete == false) {
				status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_DEREGISTER,
						AdapterConstants.ARTICLE_SYSTEM_TYPE, AdapterConstants.STATUS_MSG_FAILED_DELETE, correlId);
			}
		}
		return status.toJsonObject();
	}

	/**
	 * Returns the result of the System Type query a JSON Object.
	 * 
	 * @param correlId
	 *            The correlation ID of the request.
	 * 
	 * @return The query result JSON Object.
	 */
	public static JSON query(final JSON jsonOpObject, final String correlId) {

		// TODO Add the Service Types associated with the system type once new DB structure is in place.
		JSON systemTypesQueryResult = new JSON();
		AdapterStatus status = new AdapterStatus(correlId);
		List<JSON> jsonList = new ArrayList<JSON>();

		try {
			TypeFactory typeFactory = FabricRegistry.getTypeFactory();
			String querySQL = querySystemTypesSQL(jsonOpObject);
			if (querySQL == null) {
				status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_QUERY,
						AdapterConstants.ARTICLE_SYSTEM_TYPE, AdapterConstants.STATUS_MSG_BAD_SQL, correlId);
				systemTypesQueryResult = status.toJsonObject();
			} else {

				Type[] resultArray = null;

				if ("".equals(querySQL)) {
					resultArray = typeFactory.getAllSystemTypes();
				} else {
					resultArray = typeFactory.getSystemTypes(querySQL);
				}

				systemTypesQueryResult.putString(AdapterConstants.FIELD_OPERATION,
						AdapterConstants.OP_QUERY_RESPONSE_SYSTEM_TYPES);
				systemTypesQueryResult.putString(AdapterConstants.FIELD_CORRELATION_ID, correlId);

				for (int i = 0; i < resultArray.length; i++) {

					JSON type = new JSON();

					type.putString(AdapterConstants.FIELD_TYPE, resultArray[i].getId());
					type.putString(AdapterConstants.FIELD_DESCRIPTION, resultArray[i].getDescription());

					String attributes = resultArray[i].getAttributes();
					if (attributes != null && !attributes.equals("null")) {
						JSON attributesJson = JsonUtils.stringTOJSON(attributes,
								"Attribute value is not a valid JSON object");
						type.putJSON(AdapterConstants.FIELD_ATTRIBUTES, attributesJson);
					}

					jsonList.add(type);
				}
				systemTypesQueryResult.putArray(AdapterConstants.FIELD_SYSTEM_TYPES, jsonList);
			}
		} catch (Exception e) {
			String message = e.getClass().getName() + ": " + e.getMessage();
			status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_QUERY,
					AdapterConstants.ARTICLE_SYSTEM_TYPE, message, correlId);
			systemTypesQueryResult = status.toJsonObject();
		}
		return systemTypesQueryResult;
	}

	/**
	 * Returns the SQL relevant to the System Type query.
	 * 
	 * @param jsonOpObject
	 *            The JSON object that needs converting to SQL.
	 * @return The SQL for the query.
	 */
	private static String querySystemTypesSQL(JSON jsonOpObject) {

		String querySQL = "";
		StringBuilder s = new StringBuilder();
		try {
			if (jsonOpObject.getString(AdapterConstants.FIELD_TYPE) != null) {
				s.append("TYPE_ID='");
				s.append(jsonOpObject.getString(AdapterConstants.FIELD_TYPE));
				s.append("' AND ");
			}
			if (jsonOpObject.getString(AdapterConstants.FIELD_SERVICES) != null) {
				JSONArray serviceArray = null;
				JSON service = null;
				serviceArray = jsonOpObject.getJSONArray(AdapterConstants.FIELD_SERVICES);
				Iterator<JSON> it = serviceArray.iterator();
				while (it.hasNext()) {
					service = it.next();
					if (service.getString(AdapterConstants.FIELD_TYPE) != null) {
						s.append("ATTRIBUTES LIKE '%");
						s.append(service.getString(AdapterConstants.FIELD_TYPE));
						s.append("%' AND ");
					}

				}
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