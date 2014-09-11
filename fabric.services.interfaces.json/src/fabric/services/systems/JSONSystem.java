/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.systems;

import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.Fabric;
import fabric.ServiceDescriptor;
import fabric.SystemDescriptor;
import fabric.bus.feeds.ISubscription;
import fabric.bus.messages.IFeedMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.services.jsonclient.utilities.AdapterConstants;

/**
 * Class implementing the adapter proxy for a JSON Fabric client.
 */
public abstract class JSONSystem extends Fabric implements ISystem {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/*
	 * Class constants
	 */

	private static final String INPUT_JSON = "{" + //
			"\"" + AdapterConstants.FIELD_OPERATION + "\":\"" + AdapterConstants.OP_FEED_MESSAGE + "\"," + // Operation
			"\"" + AdapterConstants.FIELD_OUTPUT_FEED + "\":\"%s\"," + // Output feed
			"\"" + AdapterConstants.FIELD_MESSAGE + "\":%s," + // Payload
			"\"" + AdapterConstants.FIELD_INPUT_FEED + "\":\"%s\"," + // Input feed
			"\"" + AdapterConstants.FIELD_ENCODING + "\":\"%s\"" + // Encoding
			"}";

	private static final String SOLICITED_RESPONSE_JSON = "{" + //
			"\"" + AdapterConstants.FIELD_OPERATION + "\":\"" + AdapterConstants.OP_SERVICE_RESPONSE + "\"," + // Operation
			"\"" + AdapterConstants.FIELD_SOLICIT_RESPONSE + "\":\"%s\"," + // Solicit response
			"\"" + AdapterConstants.FIELD_MESSAGE + "\":%s," + // Payload
			"\"" + AdapterConstants.FIELD_REQUEST_RESPONSE + "\":\"%s\"," + // Request response
			"\"" + AdapterConstants.FIELD_CORRELATION_ID + "\":\"%s\"," + // Correlation ID
			"\"" + AdapterConstants.FIELD_ENCODING + "\":\"%s\"" + // Encoding
			"}";

	private static final String REQUEST_RESPONSE_JSON = "{" + //
			"\"" + AdapterConstants.FIELD_OPERATION + "\":\"" + AdapterConstants.OP_SERVICE_REQUEST + "\"," + // Operation
			"\"" + AdapterConstants.FIELD_REQUEST_RESPONSE + "\":[\"%s\"]," + // Request response
			"\"" + AdapterConstants.FIELD_MESSAGE + "\":%s," + // Payload
			"\"" + AdapterConstants.FIELD_SOLICIT_RESPONSE + "\":\"%s\"," + // Solicit response
			"\"" + AdapterConstants.FIELD_CORRELATION_ID + "\":\"%s\"," + // Correlation ID
			"\"" + AdapterConstants.FIELD_ENCODING + "\":\"%s\"" + // Encoding
			"}";

	private static final String ONE_WAY_JSON = "{" + //
			"\"" + AdapterConstants.FIELD_OPERATION + "\":\"" + AdapterConstants.OP_NOTIFICATION + "\"," + // Operation
			"\"" + AdapterConstants.FIELD_LISTENER + "\":\"%s\"," + // Listener
			"\"" + AdapterConstants.FIELD_MESSAGE + "\":%s," + // Payload
			"\"" + AdapterConstants.FIELD_NOTIFICATION + "\":\"%s\"," + // Notification
			"\"" + AdapterConstants.FIELD_ENCODING + "\":\"%s\"" + // Encoding
			"}";

	/*
	 * Class fields
	 */

	/** The ID of the system for which this instance is a proxy. */
	protected SystemDescriptor systemDescriptor = null;

	/** This instances container. */
	protected SystemRuntime container = null;

	/*
	 * Class methods
	 */

	public JSONSystem() {

		this(Logger.getLogger("fabric.services.systems"));
	}

	public JSONSystem(Logger logger) {

		this.logger = logger;
	}

	/**
	 * @see fabric.bus.feeds.ISubscriptionCallback#startSubscriptionCallback()
	 */
	@Override
	public void startSubscriptionCallback() {

		/* No action required */
		logger.log(Level.FINEST, "startSubscriptionCallback()");

	}

	/**
	 * @see fabric.bus.feeds.ISubscriptionCallback#handleSubscriptionMessage(fabric.bus.messages.IFeedMessage)
	 */
	@Override
	public void handleSubscriptionMessage(IFeedMessage message) {

		/* No action required */
	}

	/**
	 * @see fabric.bus.feeds.ISubscriptionCallback#handleSubscriptionEvent(fabric.bus.feeds.ISubscription, int,
	 *      fabric.bus.messages.IServiceMessage)
	 */
	@Override
	public void handleSubscriptionEvent(ISubscription subscription, int event, IServiceMessage message) {

		/* No action required */
	}

	/**
	 * @see fabric.bus.feeds.ISubscriptionCallback#cancelSubscriptionCallback()
	 */
	@Override
	public void cancelSubscriptionCallback() {

		/* No action required */
	}

	/**
	 * @see fabric.services.systems.ISystem#initializeInstance(fabric.SystemDescriptor,
	 *      fabric.services.systems.SystemRuntime)
	 */
	@Override
	public void initializeInstance(SystemDescriptor systemDescriptor, SystemRuntime container) {

		this.container = container;
	}

	/**
	 * @see fabric.services.systems.ISystem#startInstance(fabric.SystemDescriptor)
	 */
	@Override
	public void startInstance(SystemDescriptor systemDescriptor) {

		/* No action required */
	}

	/**
	 * @see fabric.services.systems.ISystem#stopInstance(fabric.SystemDescriptor)
	 */
	@Override
	public void stopInstance(SystemDescriptor systemDescriptor) {

		/* No action required */
	}

	/**
	 * @see fabric.services.systems.ISystem#handleInput(java.lang.String, fabric.bus.messages.IFeedMessage)
	 */
	@Override
	public abstract void handleInput(String inputFeedID, IFeedMessage message);

	/**
	 * @see fabric.services.systems.ISystem#handleSolicitedResponse(java.lang.String, java.lang.String,
	 *      fabric.bus.messages.IFeedMessage)
	 */
	@Override
	public abstract void handleSolicitedResponse(String correlationID, String responseFeedID, IFeedMessage message);

	/**
	 * @see fabric.services.systems.ISystem#handleRequestResponse(java.lang.String, java.lang.String,
	 *      fabric.bus.messages.IFeedMessage, fabric.ServiceDescriptor)
	 */
	@Override
	public abstract void handleRequestResponse(String correlationID, ServiceDescriptor sendTo, IFeedMessage message,
			ServiceDescriptor replyTo);

	/**
	 * @see fabric.services.systems.ISystem#handleOneWay(java.lang.String, fabric.bus.messages.IFeedMessage)
	 */
	@Override
	public abstract void handleOneWay(String requestResponseFeedID, IFeedMessage message);

	/**
	 * Answers a Fabric JSON message corresponding to the specified Fabric feed message.
	 * 
	 * @param message
	 *            the Fabric feed message.
	 * 
	 * @return the JSON message.
	 */
	public String inputToJSON(IFeedMessage message) {

		String encoding = message.getProperty("f:encoding");
		encoding = (encoding == null) ? "ascii" : encoding;

		/* Build the JSON message to deliver to the client */
		String json = String.format(INPUT_JSON, //
				message.metaGetFeedDescriptor().toString(), // Output feed
				payloadToString(message), // Payload
				message.metaGetProperty(ISystem.META_INPUT_FEED_DESCRIPTOR).toString(), // Input feed
				encoding); // Encoding

		return json;
	}

	/**
	 * Answers a Fabric JSON message corresponding to the specified Fabric solicited response message.
	 * 
	 * @param message
	 *            the Fabric solicited response message.
	 * 
	 * @return the JSON message.
	 */
	public String solicitedResponseToJSON(String correlationID, IFeedMessage message) {

		String encoding = message.getProperty("f:encoding");
		encoding = (encoding == null) ? "ascii" : encoding;

		/* Build the JSON message to deliver to the client */
		String json = String.format(SOLICITED_RESPONSE_JSON, //
				message.getProperty("f:deliverToFeed"), // Solicit response
				payloadToString(message), // Payload
				message.getProperty(IServiceMessage.PROPERTY_REPLY_TO_FEED), // Request response
				correlationID, // Correlation ID
				encoding); // Encoding

		return json;
	}

	/**
	 * Answers a Fabric JSON message corresponding to the specified Fabric request response message.
	 * 
	 * @param message
	 *            the Fabric request response message.
	 * 
	 * @return the JSON message.
	 */
	public String requestResponsetoJSON(String correlationID, IFeedMessage message, ServiceDescriptor replyTo) {

		String encoding = message.getProperty("f:encoding");
		encoding = (encoding == null) ? "ascii" : encoding;

		/* Build the JSON message to deliver to the client */
		String json = String.format(REQUEST_RESPONSE_JSON, //
				message.getProperty("f:deliverToFeed"), // Request response
				payloadToString(message), // Payload
				replyTo.toString(), // Solicit response
				correlationID, // Correlation ID
				encoding); // Encoding

		return json;
	}

	/**
	 * Answers a Fabric JSON message corresponding to the specified Fabric one way message.
	 * 
	 * @param message
	 *            the Fabric one way message.
	 * 
	 * @return the JSON message.
	 */
	public String oneWayToJSON(IFeedMessage message) {

		String encoding = message.getProperty("f:encoding");
		encoding = (encoding == null) ? "ascii" : encoding;

		/* Build the JSON message to deliver to the client */
		String json = String.format(ONE_WAY_JSON, //
				message.getProperty("f:deliverToFeed"), // Listener
				payloadToString(message), // Payload
				message.getProperty(IServiceMessage.PROPERTY_REPLY_TO_FEED), // Notification
				encoding); // Encoding

		return json;
	}

	/**
	 * Gets the payload from a Fabric message, expected to be valid JSON, and adds it to the target JSON object with the
	 * specified field name.
	 * 
	 * @param target
	 *            the JSON object into which the payload is to be added.
	 * 
	 * @param field
	 *            the name of the new field in the JSON target.
	 * 
	 * @param message
	 *            the Fabric message containing the JSON payload.
	 */
	private String payloadToString(IFeedMessage message) {

		String jsonToString = null;
		String payload = new String(message.getPayload().getPayload());

		switch (payload.trim().charAt(0)) {

		case '[':
		case '{':

			jsonToString = payload;
			break;

		default:

			jsonToString = '"' + payload + '"';
			break;
		}

		return jsonToString;
	}
}
