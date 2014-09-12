/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

/**
 */

package fabric.tools.rest.servlet;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.sql.SQLNonTransientException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import fabric.services.json.JSON;
import fabric.services.jsonclient.HTTPAdapter;

public class JSONSocket implements WebSocketListener, SocketHandler {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	private Session session;
	private static HTTPAdapter adapter;

	/**
	 * Creates an HTTP connection to the Fabric registry
	 */
	public synchronized void init() {
		try {
			if (adapter == null) {
				setAdapter();
			}
			adapter.init();
		} catch (SQLNonTransientException e) {
			returnError(Constants.ConnectException);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * If the connection to the Fabric Registry is successful, then we set it to be the adapter for the socket.
	 */
	private void setAdapter() {
		adapter = new HTTPAdapter();
	}

	/**
	 * Upon connect grab session variable and initialise Fabric connection
	 * 
	 * @param session
	 */
	@Override
	public void onWebSocketConnect(Session session) {
		this.session = session;
		init();
	}

	/**
	 * Handles incoming messages from the web front-end
	 */
	@Override
	public void onWebSocketText(String message) {
		if (session == null) {
			System.out.println("No session found");
			return;
		}

		JSON obj;
		try {
			obj = new JSON(message);
			adapter.getJSONResponseMessage(obj, session);
		} catch (JsonParseException e) {
			returnError(Constants.JSONParse);
		} catch (JsonMappingException e) {
			returnError(Constants.JSONMapping);
		} catch (IOException e) {
			returnError(Constants.IOException);
		}
	}

	/**
	 * Helper method to return the exception error as a string to the user to handle
	 * 
	 * @param errMessage
	 *            The relevant error message
	 */
	@Override
	public void returnError(String errMessage) {
		try {
			session.getRemote().sendString(errMessage);
		} catch (IOException err) {
			err.printStackTrace();
		}
	}

	/**
	 * Returns a pre-defined error to the server on websocket error
	 * 
	 * @param cause
	 */
	@Override
	public void onWebSocketError(Throwable cause) {
		if (cause instanceof ConnectException) {
			returnError(Constants.ConnectException);
		} else if (cause instanceof SocketTimeoutException) {
			returnError(Constants.SocketTimeout);
		}
	}

	/**
	 * Print out reason and status as to why socket session closed
	 * 
	 * @param statusCode
	 * @param reason
	 */
	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		System.out.println("Session closed: " + statusCode + " " + reason);
	}

	/**
	 * Inherited method from WebSocketListener that is currently not required.
	 */
	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int len) {
		// Not needed
	}
}
