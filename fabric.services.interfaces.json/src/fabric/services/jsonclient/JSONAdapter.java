/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2006, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.jsonclient;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.FabricBus;
import fabric.ServiceDescriptor;
import fabric.bus.messages.IClientNotificationMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.client.FabricPlatform;
import fabric.client.services.IClientNotificationHandler;
import fabric.client.services.IHomeNodeConnectivityCallback;
import fabric.core.io.EndPoint;
import fabric.core.io.IEndPointCallback;
import fabric.core.io.mqtt.MqttConfig;
import fabric.core.logging.LogUtil;
import fabric.services.json.JSON;
import fabric.services.json.JSONArray;
import fabric.services.jsonclient.articles.Nodes;
import fabric.services.jsonclient.articles.Platforms;
import fabric.services.jsonclient.handler.OperationDispatcher;
import fabric.services.jsonclient.utilities.AdapterConstants;
import fabric.services.jsonclient.utilities.AdapterStatus;
import fabric.services.systems.RuntimeManager;

/**
 * JSON interface for Fabric clients.
 */
public abstract class JSONAdapter extends FabricBus implements IHomeNodeConnectivityCallback,
		IClientNotificationHandler, IEndPointCallback {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2006, 2014";

	/*
	 * Class constants
	 */

	/** The default task ID used by subscriptions from this service. */
	public static final String DEFAULT_TASK = "DEFAULT";

	/*
	 * Class fields
	 */

	/** The adapter's connection to the Fabric. */
	private FabricPlatform fabricPlatform = null;

	/** The adapter's user ID. */
	private String adapterUserID = null;

	/** The adapter's Registry ID. */
	private String adapterPlatformID = null;

	/** The manager for running systems. */
	private RuntimeManager runtimeManager = null;

	/*
	 * Class methods
	 */

	public JSONAdapter() {

		this(Logger.getLogger("fabric.services.jsonclient"));
	}

	public JSONAdapter(Logger logger) {

		this.logger = logger;
	}

	/**
	 * Initialises the JSON adapter, connecting to the Fabric listening for, and actioning, adapter messages.
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {

		/* Connect to the Fabric */
		adapterUserID = MqttConfig.generateClient("MQTTA");
		adapterPlatformID = MqttConfig.generateClient("MQTTA");
		fabricPlatform = new FabricPlatform(adapterUserID, adapterPlatformID);
		fabricPlatform.connect();
		fabricPlatform.homeNodeEndPoint().register(this);

		/* Register to receive notifications if the home node is lost */
		fabricPlatform.registerHomeNodeConnectivityCallback(this);

		/* Register this platform */
		fabricPlatform.registerPlatformType("MQTT_ADAPTOR", null, null, null);
		fabricPlatform.registerPlatform(adapterPlatformID, "MQTT_ADAPTOR");

		/* Register this user ID */
		fabricPlatform.registerActorType("USER", null, null, null);
		fabricPlatform.registerActor(adapterUserID, "USER");

		runtimeManager = new RuntimeManager(fabricPlatform);
		runtimeManager.init();

		/* Passing through the home node */
		Nodes.setNode(fabricPlatform.homeNode());
		Platforms.setNode(fabricPlatform.homeNode());

	}

	/**
	 * Stops and cleans up the JSON adapter.
	 */
	public void stop() {

		runtimeManager.stop();
		fabricPlatform.deregisterActor(adapterUserID);
		fabricPlatform.deregisterPlatform(adapterPlatformID);
	}

	/**
	 * Builds a subscription response message.
	 * 
	 * @param outputFeedList
	 *            the list of feeds to which subscriptions have been made.
	 * 
	 * @param inputFeed
	 *            the input feed to which the subscriptions are mapped.
	 * 
	 * @param correlId
	 *            The correlation ID of the request.
	 * 
	 * @return the JSON object containing the response message.
	 */
	public static JSON buildSubscriptionResponse(List<ServiceDescriptor> outputFeedList, ServiceDescriptor inputFeed,
			String correlId) {

		JSON subscriptionResponse = new JSON();

		/* Build the list of output feeds */
		List<String> outputFeeds = new ArrayList<String>();
		for (ServiceDescriptor nextFeed : outputFeedList) {
			outputFeeds.add(nextFeed.toString());
		}
		JSONArray outputFeedJSON = new JSONArray();
		outputFeedJSON.putStringList(outputFeeds);

		/* Build the full message */
		subscriptionResponse.putString(AdapterConstants.FIELD_OPERATION, AdapterConstants.OP_SUBSCRIPTIONS);
		subscriptionResponse.putJSONArray(AdapterConstants.FIELD_OUTPUT_FEEDS, outputFeedJSON);
		subscriptionResponse.putString(AdapterConstants.FIELD_INPUT_FEED, inputFeed.toString());
		subscriptionResponse.putString(AdapterConstants.FIELD_CORRELATION_ID, correlId);

		return subscriptionResponse;
	}

	/**
	 * Answers the name of the class implementing the system adapter proxy for the JSON Fabric client.
	 * 
	 * @return the full class name.
	 */
	public abstract String adapterProxy();

	/**
	 * Handles a Fabric operation encoded in a JSON object.
	 * 
	 * @param op
	 *            the operation.
	 * 
	 * @param correlationID
	 *            the operation's correlation ID, or <code>null</code> if none.
	 * 
	 * @param adapterClient
	 *            adapter-specific ID of the client, used to target messages sent to the client.
	 * 
	 * @return the response message.
	 */
	public JSON handleAdapterMessage(JSON op, String correlationID, Object adapterClient) {

		/* To hold the response message (if any) */
		JSON response = null;

		try {

			/* Get the name of the operation */
			String operation = op.getString(AdapterConstants.FIELD_OPERATION).toLowerCase();
			logger.log(Level.FINEST, "Operation: %s", operation);

			if (operation == null) {

				logger.log(Level.WARNING, "Operation field (\"{0}\") missing, ignoring message:\n{1}", new Object[] {
						AdapterConstants.FIELD_OPERATION, op.toString()});
				AdapterStatus status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_NONE,
						AdapterConstants.ARTICLE_JSON, AdapterConstants.STATUS_MSG_BAD_OPERATION, correlationID);
				response = status.toJsonObject();

			} else {

				/* Extract the primary operation name */
				String primaryOperation = operation.split(":")[0];

				switch (primaryOperation) {

				case AdapterConstants.OP_REGISTER:

					response = OperationDispatcher.registration(operation, op, true, correlationID);
					break;

				case AdapterConstants.OP_DEREGISTER:

					response = OperationDispatcher.registration(operation, op, false, correlationID);
					break;

				case AdapterConstants.OP_QUERY:

					response = OperationDispatcher.query(operation, op, correlationID);
					break;

				case AdapterConstants.OP_STATE:

					response = OperationDispatcher.stateChange(operation, adapterClient, op, runtimeManager,
							adapterProxy(), correlationID);
					break;

				case AdapterConstants.OP_SQL_DELETE:
					response = OperationDispatcher.query(operation, op, correlationID);
					break;

				case AdapterConstants.OP_SQL_UPDATE:
					response = OperationDispatcher.query(operation, op, correlationID);
					break;

				case AdapterConstants.OP_SQL_SELECT:
					response = OperationDispatcher.query(operation, op, correlationID);
					break;

				case AdapterConstants.OP_SERVICE_REQUEST:
				case AdapterConstants.OP_SERVICE_RESPONSE:
				case AdapterConstants.OP_NOTIFY:
				case AdapterConstants.OP_PUBLISH:
				case AdapterConstants.OP_SUBSCRIBE:
				case AdapterConstants.OP_UNSUBSCRIBE:
				case AdapterConstants.OP_DISCONNECT:

					response = OperationDispatcher.serviceOperation(operation, op, runtimeManager, correlationID);
					break;

				default:

					AdapterStatus status = new AdapterStatus(AdapterConstants.ERROR_PARSE,
							AdapterConstants.OP_CODE_NONE, AdapterConstants.ARTICLE_JSON,
							AdapterConstants.STATUS_MSG_BAD_OPERATION, correlationID);
					response = status.toJsonObject();
					break;

				}
			}

		} catch (Exception e) {

			logger.log(Level.FINER, "Exception handling message:\n{0}\n{1}", new Object[] {op.toString(),
					LogUtil.stackTrace(e)});
			AdapterStatus status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_NONE,
					AdapterConstants.ARTICLE_JSON, AdapterConstants.STATUS_MSG_BAD_JSON + ": " + e.getMessage(),
					correlationID);
			response = status.toJsonObject();

		}

		return response;

	}

	/**
	 * @see fabric.client.services.IHomeNodeConnectivityCallback#homeNodeConnectivity(fabric.bus.messages.IServiceMessage)
	 */
	@Override
	public void homeNodeConnectivity(final IServiceMessage message) {

		logger.log(Level.FINE, "Change in connectivity status to home node: {0}", message.toString());

		if (message.getEvent() == IServiceMessage.EVENT_CONNECTED
				&& fabricPlatform.actor().equals(message.getProperty(IServiceMessage.PROPERTY_ACTOR))
				&& fabricPlatform.platform().equals(message.getProperty(IServiceMessage.PROPERTY_ACTOR_PLATFORM))
				&& homeNode().equals(message.getProperty(IServiceMessage.PROPERTY_NODE))) {

			endPointReconnected(fabricPlatform.homeNodeEndPoint());

		}
	}

	/**
	 * Handles a notification message from the Fabric.
	 * 
	 * @see fabric.client.services.IClientNotificationHandler#handleNotification(fabric.bus.messages.IClientNotificationMessage)
	 */
	@Override
	public void handleNotification(final IClientNotificationMessage message) {

		logger.log(Level.FINEST, "IClientNotificationMessage received:\n{0}", message);

	}

	/**
	 * @see fabric.core.io.IEndPointCallback#endPointConnected(fabric.core.io.EndPoint)
	 */
	@Override
	public void endPointConnected(final EndPoint ep) {

		logger.log(Level.FINEST, "End point connected");
	}

	/**
	 * @see fabric.core.io.IEndPointCallback#endPointDisconnected(fabric.core.io.EndPoint)
	 */
	@Override
	public void endPointDisconnected(final EndPoint ep) {

		logger.log(Level.FINEST, "End point disconnected");
	}

	/**
	 * @see fabric.core.io.IEndPointCallback#endPointReconnected(fabric.core.io.EndPoint)
	 */
	@Override
	public void endPointReconnected(final EndPoint ep) {

		logger.log(Level.FINEST, "End point reconnected");
	}

	/**
	 * @see fabric.core.io.IEndPointCallback#endPointClosed(fabric.core.io.EndPoint)
	 */
	@Override
	public void endPointClosed(final EndPoint ep) {

		logger.log(Level.FINEST, "End point closed");
	}

	/**
	 * @see fabric.core.io.IEndPointCallback#endPointLost(fabric.core.io.EndPoint)
	 */
	@Override
	public void endPointLost(EndPoint ep) {

		logger.log(Level.FINEST, "End point lost");
	}
}
