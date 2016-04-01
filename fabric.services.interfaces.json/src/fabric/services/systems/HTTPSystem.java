/*
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.systems;

import java.io.IOException;
import java.util.logging.Level;

import org.eclipse.jetty.websocket.api.Session;

import fabric.ServiceDescriptor;
import fabric.SystemDescriptor;
import fabric.bus.feeds.ISubscription;
import fabric.bus.messages.IFeedMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.core.logging.FLog;
import fabric.services.json.JSON;

/**
 * Class implementing the adapter proxy for an HTTP JSON Fabric client.
 */
public class HTTPSystem extends JSONSystem {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/*
	 * Class fields
	 */

	/* To hold the session via which to communicate with the client */
	private Session session = null;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 */
	public HTTPSystem() {
	}

	/**
	 * @see fabric.bus.feeds.ISubscriptionCallback#startSubscriptionCallback()
	 */
	@Override
	public void startSubscriptionCallback() {

		super.startSubscriptionCallback();
	}

	/**
	 * @see fabric.bus.feeds.ISubscriptionCallback#handleSubscriptionMessage(fabric.bus.messages.IFeedMessage)
	 */
	@Override
	public void handleSubscriptionMessage(IFeedMessage message) {

		super.handleSubscriptionMessage(message);
	}

	/**
	 * @see fabric.services.systems.JSONSystem#handleSubscriptionEvent(fabric.bus.feeds.ISubscription, int,
	 *      fabric.bus.messages.IServiceMessage)
	 */
	@Override
	public void handleSubscriptionEvent(ISubscription subscription, int event, IServiceMessage message) {

		super.handleSubscriptionEvent(subscription, event, message);
	}

	/**
	 * @see fabric.bus.feeds.ISubscriptionCallback#cancelSubscriptionCallback()
	 */
	@Override
	public void cancelSubscriptionCallback() {

		super.cancelSubscriptionCallback();
	}

	/**
	 * @see fabric.services.systems.JSONSystem#initializeInstance(fabric.SystemDescriptor,
	 *      fabric.services.systems.SystemRuntime)
	 */
	@Override
	public void initializeInstance(SystemDescriptor systemDescriptor, SystemRuntime container) {

		super.initializeInstance(systemDescriptor, container);
	}

	/**
	 * @see fabric.services.systems.JSONSystem#startInstance(fabric.SystemDescriptor)
	 */
	@Override
	public void startInstance(SystemDescriptor systemDescriptor) {

		super.startInstance(systemDescriptor);
	}

	/**
	 * @see fabric.services.systems.JSONSystem#stopInstance(fabric.SystemDescriptor)
	 */
	@Override
	public void stopInstance(SystemDescriptor systemDescriptor) {

		super.stopInstance(systemDescriptor);
	}

	/**
	 * @see fabric.services.systems.ISystem#handleInput(java.lang.String, fabric.bus.messages.IFeedMessage)
	 */
	@Override
	public void handleInput(String inputFeedID, IFeedMessage message) {

		/* Build and send the JSON message to the client */
		String jsonMessage = inputToJSON(message);
		sendToClient(jsonMessage);
	}

	/**
	 * @see fabric.services.systems.ISystem#handleSolicitedResponse(java.lang.String, java.lang.String,
	 *      fabric.bus.messages.IFeedMessage)
	 */
	@Override
	public void handleSolicitedResponse(String correlationID, String responseFeedID, IFeedMessage message) {

		/* Build and send the JSON message to the client */
		String jsonMessage = solicitedResponseToJSON(correlationID, message);
		sendToClient(jsonMessage);
	}

	/**
	 * @see fabric.services.systems.ISystem#handleRequestResponse(java.lang.String, java.lang.String,
	 *      fabric.bus.messages.IFeedMessage, fabric.ServiceDescriptor)
	 */
	@Override
	public void handleRequestResponse(String correlationID, ServiceDescriptor sendTo, IFeedMessage message,
			ServiceDescriptor replyTo) {

		/* Build and send the JSON message to the client */
		String jsonMessage = requestResponsetoJSON(correlationID, message, replyTo);
		sendToClient(jsonMessage);
	}

	/**
	 * @see fabric.services.systems.ISystem#handleOneWay(java.lang.String, fabric.bus.messages.IFeedMessage)
	 */
	@Override
	public void handleOneWay(String requestResponseFeedID, IFeedMessage message) {

		/* Build and send the JSON message to the client */
		String jsonMessage = oneWayToJSON(message);
		sendToClient(jsonMessage);
	}

	/**
	 * @see fabric.services.systems.JSONSystem#sendToClient(java.lang.String)
	 */
	@Override
	public void sendToClient(String jsonMessage) {

		try {

			if (session == null) {
				/* Get the session via which to communicate with the client */
				session = (Session) container.getClient();
			}

			session.getRemote().sendString(jsonMessage);

		} catch (IOException e) {

			JSON json = null;
			String type = jsonMessage;

			try {
				json = new JSON(jsonMessage);
				type = json.getString("op");
			} catch (Exception e1) {
				/* Nothing to do here */
			}

			logger.log(Level.SEVERE, "Failed to deliver \"{0}\" message to client via session \"{1}\": {2}",
					new Object[] {type, session, FLog.stackTrace(e)});

		}
	}
}
