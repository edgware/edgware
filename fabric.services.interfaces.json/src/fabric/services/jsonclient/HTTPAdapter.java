/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.jsonclient;

import org.eclipse.jetty.websocket.api.Session;

import fabric.services.json.JSON;
import fabric.services.jsonclient.utilities.AdapterConstants;

/**
 * HTTP adapter for JSON Fabric clients.
 */
public class HTTPAdapter extends JSONAdapter {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/** The name of the class to act as a proxy for systems started by this adapter. */
	private static final String HTTP_SYSTEM_ADAPTER = "fabric.services.systems.HTTPSystem";

	/** Adapter ID */
	public static String adapterId = "";

	/**
	 * @see fabric.services.jsonclient.JSONAdapter#init()
	 */
	@Override
	public void init() throws Exception {
		super.init();
	}

	/**
	 * @see fabric.services.jsonclient.JSONAdapter#adapterProxy()
	 */
	@Override
	public String adapterProxy() {
		return HTTP_SYSTEM_ADAPTER;
	}

	/**
	 * Method that handles the incoming JSON message from HTTP client. Used when you expect a response message from
	 * Fabric for non-SQL items
	 */
	public JSON getJSONResponseMessage(JSON obj, Session session) {
		/*
		 * Correlation id is null so JSONAdapter can handle the case whereby no correlation id exists
		 */
		String correlationId = null;
		JSON response = new JSON();
		correlationId = obj.getString("correl");
		boolean isSQLQuery = obj.getString(AdapterConstants.FIELD_SQL_KEY) != null
				|| obj.getString(AdapterConstants.FIELD_SQL_TABLE) != null;

		try {
			response = handleAdapterMessage(obj, correlationId, session);
			session.getRemote().sendString(response.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return response;
	}

}
