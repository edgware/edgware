/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.systems;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;

import fabric.ServiceDescriptor;
import fabric.SystemDescriptor;
import fabric.bus.feeds.ISubscription;
import fabric.bus.messages.IFeedMessage;
import fabric.bus.messages.IServiceMessage;

/**
 * Class implementing the adapter proxy for an HTTP JSON Fabric client.
 */
public class HTTPSystem extends JSONSystem {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/*
	 * Class methods
	 */

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

		/* Build the JSON message to deliver to the client */
		String jsonMessage = inputToJSON(message);

		/* Send the message to the client */
		sendResponse((Session) container.getClient(), jsonMessage);
	}

	/**
	 * @see fabric.services.systems.ISystem#handleSolicitedResponse(java.lang.String, java.lang.String,
	 *      fabric.bus.messages.IFeedMessage)
	 */
	@Override
	public void handleSolicitedResponse(String correlationID, String responseFeedID, IFeedMessage message) {

		/* Build the JSON message to deliver to the client */
		String jsonMessage = solicitedResponseToJSON(correlationID, message);

		/* Send the message to the client */
		sendResponse((Session) container.getClient(), jsonMessage);
	}

	/**
	 * @see fabric.services.systems.ISystem#handleRequestResponse(java.lang.String, java.lang.String,
	 *      fabric.bus.messages.IFeedMessage, fabric.ServiceDescriptor)
	 */
	@Override
	public void handleRequestResponse(String correlationID, ServiceDescriptor sendTo, IFeedMessage message,
			ServiceDescriptor replyTo) {

		/* Build the JSON message to deliver to the client */
		String jsonMessage = requestResponsetoJSON(correlationID, message, replyTo);

		/* Send the message to the client */
		sendResponse((Session) container.getClient(), jsonMessage);
	}

	/**
	 * @see fabric.services.systems.ISystem#handleOneWay(java.lang.String, fabric.bus.messages.IFeedMessage)
	 */
	@Override
	public void handleOneWay(String requestResponseFeedID, IFeedMessage message) {

		/* Build the JSON message to deliver to the client */
		String jsonMessage = oneWayToJSON(message);

		/* Send the message to the client */
		sendResponse((Session) container.getClient(), jsonMessage);
	}

	private void sendResponse(Session session, String message) {

		try {
			session.getRemote().sendString(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
