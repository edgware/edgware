/*
 * (C) Copyright IBM Corp. 2016
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
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

/**
 * MQTT Socket class that deals with incoming and outgoing MQTT messages, and MQTT connections.
 */
public class NodeViewerSocket implements WebSocketListener, SocketHandler {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2016";

    private final String noNeighboursPayload = "{\"op\":\"query-result:neighbours\",\"nodes\":[]}";

    private Session session;
    private static HTTPAdapter adapter;

    private JSON response;

    /**
     * Creates an HTTP connection to the Fabric registry
     */
    public synchronized void init() {

        // Intialise 'response' with the equivalent of a blank reply
        try {
            response = new JSON(noNeighboursPayload);
        } catch (JsonParseException e) {
            returnError(Constants.JSONParse);
        } catch (JsonMappingException e) {
            returnError(Constants.JSONMapping);
        } catch (IOException e) {
            returnError(Constants.IOException);
        }

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

        sendWsMessage("WSConnected");
    }

    /**
     * Set the adapter for the socket.
     */
    private void setAdapter() {
        adapter = new HTTPAdapter();
    }

    /**
     * Upon connect grab session variable and initialize MQTT connection
     *
     * @param session
     */
    @Override
    public void onWebSocketConnect(Session session) {
        this.session = session;
        init();
    }

    /**
     * Handles incoming messages from the web front-end. If it's a broker URL then it connects, otherwise it is a
     * subscription message and we handle it accordingly.
     */
    @Override
    public void onWebSocketText(String message) {
        JSON obj = null;

        try {
            obj = new JSON(message);
        } catch (JsonParseException e) {
            returnError(Constants.JSONParse);
        } catch (JsonMappingException e) {
            returnError(Constants.JSONMapping);
        } catch (IOException e) {
            returnError(Constants.IOException);
        }

        /**
         * Handle a "refreshnodes" incoming message.
         */
        // if(message.contains("query:neighbours")){
        // JSON response = adapter.getJSONResponseMessage(obj, session, false);
        //
        // if(!this.response.toString().equals(response.toString()) &&
        // !response.toString().equals(noNeighboursPayload)){
        // this.response = response;
        // sendWsMessage(response.toString());
        // }
        // } else {
        adapter.getJSONResponseMessage(obj, session);
        // }
    }

    /**
     * Helper method to return the exception error as a string to the user to handle.
     *
     * @param errMessage
     *            The relevant error message
     */
    @Override
    public void returnError(String errorMessage) {
        try {
            session.getRemote().sendString(errorMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Throw error to server on websocket error/
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
     * Print out reason and status as to why socket session closed.
     *
     * @param statusCode
     * @param reason
     */
    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        System.out.println("Session closed: " + statusCode + " " + reason);
        session.close();
        adapter.stop();
    }

    /**
     * Inherited method from WebSocketListener that is currently not required.
     */
    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        // Not needed
    }

    public void sendWsMessage(String message) {
        try {
            session.getRemote().sendString(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
