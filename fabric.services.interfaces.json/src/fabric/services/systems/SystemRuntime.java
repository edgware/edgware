/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2010, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.systems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.Fabric;
import fabric.ServiceDescriptor;
import fabric.SystemDescriptor;
import fabric.bus.SharedChannel;
import fabric.bus.feeds.ISubscription;
import fabric.bus.feeds.ISubscriptionCallback;
import fabric.bus.messages.FabricMessageFactory;
import fabric.bus.messages.IClientNotificationMessage;
import fabric.bus.messages.IFeedMessage;
import fabric.bus.messages.IMessagePayload;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.messages.impl.MessagePayload;
import fabric.bus.messages.impl.ServiceMessage;
import fabric.bus.routing.impl.StaticRouting;
import fabric.client.FabricClient;
import fabric.client.FabricPlatform;
import fabric.client.services.IClientNotificationHandler;
import fabric.core.logging.LogUtil;
import fabric.registry.FabricRegistry;
import fabric.registry.Platform;
import fabric.registry.PlatformFactory;
import fabric.registry.Route;
import fabric.registry.RouteFactory;
import fabric.registry.System;
import fabric.registry.SystemFactory;

/**
 * Container class managing a service instance.
 */
public class SystemRuntime extends Fabric implements ISubscriptionCallback, IClientNotificationHandler {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010, 2014";

	/*
	 * Class fields
	 */

	/** The runtime manager that owns this instance. */
	private RuntimeManager runtimeManager = null;

	/** The ID of the system. */
	private SystemDescriptor systemDescriptor = null;

	/** The name of the service class. */
	private String className = null;

	/** The set of services associated with this system. */
	private SystemServices systemServices = null;

	/** The adapter-specific ID of the client, used to target messages sent to the client. */
	private Object client = null;

	/** The platform connection to the Fabric for this service instance. */
	private FabricPlatform fabricClient = null;

	/** The system instance itself */
	private ISystem systemInstance = null;

	/** Object used to lock access to the delivery flag. */
	private final Object deliveryLock = new Object();

	/** Flag indicating if Registry queries should be local or distributed. */
	private boolean doQueryLocal = false;

	/**
	 * Flag used to indicate if subscription messages should be delivered to the service instance (<code>true</code>) or
	 * not (<code>false</code>).
	 */
	private boolean deliveryEnabled = false;

	/** Flag indicating if this system is running (<code>true</code>) or stopped (<code>false</code>). */
	private boolean isRunning = false;

	/*
	 * Class methods
	 */

	public SystemRuntime() {

		this(Logger.getLogger("fabric.services.systems"));
	}

	public SystemRuntime(Logger logger) {

		this.logger = logger;
	}

	/**
	 * Constructs a new instance.
	 * 
	 * @param systemDescriptor
	 *            the service ID.
	 * 
	 * @param client
	 *            adapter-specific ID of the client, used to target messages sent to the client.
	 * 
	 * @param className
	 *            the name of the system class handling inbound messages from the Fabric to the system, implementing the
	 *            <code>ISystem</code> interface.
	 * 
	 * @param runtimeManager
	 *            the component manager that starting this instance.
	 */
	public SystemRuntime(SystemDescriptor systemDescriptor, Object client, String className,
			RuntimeManager runtimeManager) {

		super(Logger.getLogger("fabric.services.systems"));

		this.systemDescriptor = systemDescriptor;
		this.client = client;
		this.className = className;
		this.runtimeManager = runtimeManager;
		this.fabricClient = runtimeManager.fabricClient();

		/* Determine if Registry queries should be local or distributed */
		doQueryLocal = Boolean.parseBoolean(config("fabric.composition.queryLocal", "false"));

		/* Initialize the data feed manager */
		systemServices = new SystemServices(this);

	}

	/**
	 * Invokes the service instance's initialization method, and updates the Registry to indicated that it is available.
	 * 
	 * @throws Exception
	 */
	public void start() throws Exception {

		logger.log(Level.FINE, "Starting system \"{0}\"", systemDescriptor);

		/* Initialize this system's feeds */
		systemServices.initFeeds();

		/* Initialize the service instance */
		systemInstance.initializeInstance(systemDescriptor, this);

		/* Update the service's status in the Registry */
		updateServiceAvailability("AVAILABLE");
		isRunning = true;

		/* Enable the flow of messages into the service */
		synchronized (deliveryLock) {
			deliveryEnabled = true;
		}

	}

	/**
	 * Instantiates the system.
	 * 
	 * @throws Exception
	 */
	public ISystem instantiate() throws Exception {

		systemInstance = (ISystem) instantiate(className);

		return systemInstance;

	}

	/**
	 * Invokes the service instances' start method to inform it to start processing.
	 */
	public void startInstance() {

		logger.log(Level.FINE, "Starting service \"{0}\"", systemDescriptor);

		/* Inform the system to start processing */
		systemInstance.startInstance(systemDescriptor);

	}

	/**
	 * Invokes the service instances' stop method.
	 */
	public void stop() {

		logger.log(Level.FINE, "Stopping service \"{0}\"", systemDescriptor);

		/* Disable the flow of messages into the service */
		synchronized (deliveryLock) {
			deliveryEnabled = false;
		}

		try {

			/* Inform the service to stop */
			systemInstance.stopInstance(systemDescriptor);

			/* Update the service's status in the Registry */
			updateServiceAvailability("UNAVAILABLE");

			/* Disconnect this service instance from the Fabric */
			disconnectFabric();

		} catch (Exception e) {

			logger.log(Level.SEVERE, "Error stopping service instance \"{0}\": {1}", new Object[] {systemDescriptor,
					LogUtil.stackTrace(e)});

		}
	}

	/**
	 * Closes the Fabric client connection for this service instance.
	 */
	public void disconnectFabric() {

		try {

			/* Close this services feeds */
			systemServices.closeFeeds();

		} catch (Exception e) {

			logger.log(Level.SEVERE, "Error disconnecting service instance \"{0}\" from the Fabric: {1}", new Object[] {
					systemDescriptor, LogUtil.stackTrace(e)});

		}
	}

	/**
	 * @see fabric.bus.feeds.ISubscriptionCallback#startSubscriptionCallback()
	 */
	@Override
	public void startSubscriptionCallback() {

		systemInstance.startSubscriptionCallback();

	}

	/***
	 * @see fabric.bus.feeds.ISubscriptionCallback#handleSubscriptionMessage(fabric.bus.messages.IFeedMessage)
	 */
	@Override
	public void handleSubscriptionMessage(IFeedMessage message) {

		if (deliveryEnabled) {

			/* If this message is a solicited response... */
			if (systemServices.solicitResponseFeeds().containsKey(message.metaGetFeedDescriptor())) {

				systemInstance.handleSolicitedResponse(message.getCorrelationID(), message.metaGetFeedDescriptor()
						.service(), message);

			}
			/* Else if this message is a request message requiring a response... */
			else if (systemServices.requestResponseFeeds().containsKey(message.metaGetFeedDescriptor())) {

				/* Get the reply-to descriptor */
				String replyToFeed = message.getProperty(IServiceMessage.PROPERTY_REPLY_TO_FEED);
				ServiceDescriptor replyToFeedDescriptor = new ServiceDescriptor(replyToFeed);

				systemInstance.handleRequestResponse(message.getCorrelationID(), message.metaGetFeedDescriptor(),
						message, replyToFeedDescriptor);

			}
			/* Else if this message is a one-way message (a message that does not require a response)... */
			else if (systemServices.oneWayFeeds().containsKey(message.metaGetFeedDescriptor())) {

				systemInstance.handleOneWay(message.metaGetFeedDescriptor().service(), message);

			}
			/* Else if this message is from a wired input feed... */
			else if (systemServices.wiredInputFeeds().containsKey(message.metaGetFeedDescriptor())) {

				/* Get the input feed corresponding to this message (i.e. map the incoming feed to a local input feed) */
				ServiceDescriptor localInputDescriptor = systemServices.wiredInputFeedMappings().get(
						message.metaGetFeedDescriptor());

				/* Set a new property in the message with the input feed descriptor */
				message.metaSetProperty(ISystem.META_INPUT_FEED_DESCRIPTOR, localInputDescriptor);

				systemInstance.handleInput(localInputDescriptor.service(), message);

			} else {

				/* This is not one of the subscriptions that we manage, so deliver it to the default handler */
				systemInstance.handleSubscriptionMessage(message);

			}
		}
	}

	/**
	 * @see fabric.client.services.IClientNotificationHandler#handleNotification(fabric.bus.messages.IClientNotificationMessage)
	 */
	@Override
	public void handleNotification(IClientNotificationMessage message) {

		logger.log(Level.FINEST, "handleNotification() callback invoked with message:\n{0}", message);

	}

	/**
	 * @see fabric.bus.feeds.ISubscriptionCallback#handleSubscriptionEvent(fabric.bus.feeds.ISubscription, int,
	 *      fabric.bus.messages.IServiceMessage)
	 */
	@Override
	public void handleSubscriptionEvent(ISubscription subscription, int event, IServiceMessage message) {

		logger.log(Level.FINEST, "handleDisconnectMessage() callback invoked with message:\n{0}", message);
		systemInstance.handleSubscriptionEvent(subscription, event, message);

	}

	/**
	 * @see fabric.bus.feeds.ISubscriptionCallback#cancelSubscriptionCallback()
	 */
	@Override
	public void cancelSubscriptionCallback() {

		logger.log(Level.FINEST, "cancelSubscriptionCallback() callback invoked");
		systemInstance.cancelSubscriptionCallback();

	}

	/**
	 * Sends a feed message back onto the bus.
	 * 
	 * @param outputFeedID
	 *            the ID of the feed to which the message is to be sent.
	 * 
	 * @param message
	 *            the message to send.
	 * 
	 * @throws Exception
	 *             thrown if the message cannot be forwarded.
	 */
	public void forward(String outputFeedID, IFeedMessage message) throws Exception {

		/* If this is a valid output feed... */
		if (systemServices.outputFeedIDs().contains(outputFeedID)) {

			ServiceDescriptor serviceDescriptor = new ServiceDescriptor(systemDescriptor.platform(), systemDescriptor
					.system(), outputFeedID);
			SharedChannel channel = systemServices.busOutputFeeds().get(serviceDescriptor);

			/* If this channel exists... */
			if (channel != null) {
				channel.write(message.toWireBytes());
			}

		} else {

			String error = format("Invalid output feed ID: %s", outputFeedID);
			logger.log(Level.SEVERE, error);
			throw new IllegalArgumentException(error);

		}
	}

	/**
	 * Publishes a new payload onto the on-ramp.
	 * 
	 * @param outputFeedID
	 *            the ID of the feed to which the payload is to be sent.
	 * 
	 * @param payload
	 *            the payload to send.
	 * 
	 * @throws Exception
	 *             thrown if the message cannot be forwarded.
	 */
	public void publish(String outputFeedID, byte[] payload) throws Exception {

		/* If this is a valid output feed... */
		if (systemServices.outputFeedIDs().contains(outputFeedID)) {

			ServiceDescriptor serviceDescriptor = new ServiceDescriptor(systemDescriptor.platform(), systemDescriptor
					.system(), outputFeedID);
			SharedChannel channel = systemServices.onrampOutputFeeds().get(serviceDescriptor);

			/* If this channel exists... */
			if (channel != null) {
				channel.write(payload);
			}

		} else {

			String error = format("Invalid output feed ID: %s", outputFeedID);
			logger.log(Level.SEVERE, error);
			throw new IllegalArgumentException(error);

		}
	}

	/**
	 * Publishes a new payload onto the bus.
	 * 
	 * @param outputFeedService
	 *            the ID of the output-feed service generating the message.
	 * 
	 * @param payload
	 *            the payload to send.
	 * 
	 * @param encoding
	 *            the encoding of the request message.
	 * 
	 * @throws Exception
	 *             thrown if the message cannot be forwarded.
	 */
	public void publish(ServiceDescriptor outputFeedService, byte[] payload, String encoding) throws Exception {

		/* If this is a valid output feed... */
		if (systemServices.outputFeedIDs().contains(outputFeedService.service())) {

			/* Build and send the bus feed message */
			IFeedMessage message = fabricClient.wrapRawMessage(payload, false);
			message.setProperty("encoding", encoding);
			forward(outputFeedService.service(), message);

		} else {

			String error = format("Invalid output-feed service: %s", outputFeedService);
			logger.log(Level.SEVERE, error);
			throw new IllegalArgumentException(error);

		}
	}

	/**
	 * Subscribes to an output feed service.
	 * 
	 * @param outputFeedService
	 *            the publishing service.
	 * 
	 * @param inputFeedService
	 *            the local service to which feed messages will be delivered.
	 * 
	 * @throws Exception
	 *             thrown if the message cannot be forwarded.
	 */
	public void subscribe(ServiceDescriptor outputFeedService, ServiceDescriptor inputFeedService) throws Exception {

		/* If this is a valid input feed... */
		if (systemServices.inputFeedIDs().contains(inputFeedService.service())) {

			/* Subscribe */
			systemServices.wireInputFeed(outputFeedService, inputFeedService.service());

		} else {

			String error = format("Invalid input-feed service: %s", inputFeedService);
			logger.log(Level.SEVERE, error);
			throw new IllegalArgumentException(error);

		}
	}

	/**
	 * Unsubscribes from one or more output feed services.
	 * 
	 * @param outputFeedServices
	 *            the list of output feed services. If <code>null</code> then all feeds associated with the specified
	 *            input feed service are unsubscribed.
	 * 
	 * @param inputFeedService
	 *            the local service to which feed messages are being delivered.
	 * 
	 * @throws Exception
	 *             thrown if the message cannot be forwarded.
	 */
	public void unsubscribe(ServiceDescriptor[] outputFeedServices, ServiceDescriptor inputFeedService)
			throws Exception {

		/* If this is a valid input feed... */
		if (systemServices.inputFeedIDs().contains(inputFeedService.service())) {

			/* If no output feed services have been specified... */
			if (outputFeedServices == null) {

				/* To hold the list of feeds to unsubscribe from */
				ArrayList<ServiceDescriptor> outputFeedList = new ArrayList<ServiceDescriptor>();

				/* Get the mappings between input-feeds and output-feeds for this system */
				HashMap<ServiceDescriptor, ServiceDescriptor> feedMappings = systemServices.wiredInputFeedMappings();

				/* For each input-feed/output-feed mapping... */
				for (ServiceDescriptor nextOutputFeed : feedMappings.keySet()) {

					/* If this is an output-feed associated with our target input-feed... */
					if (feedMappings.get(nextOutputFeed).equals(inputFeedService)) {
						outputFeedList.add(nextOutputFeed);
					}
				}

				outputFeedServices = outputFeedList.toArray(new ServiceDescriptor[0]);
			}

			/* For each output feed service... */
			for (ServiceDescriptor outputFeed : outputFeedServices) {

				/* Unsubscribe */
				systemServices.unwireInputFeed(outputFeed);

			}

		} else {

			String error = format("Invalid input-feed service: %s", inputFeedService);
			logger.log(Level.SEVERE, error);
			throw new IllegalArgumentException(error);

		}
	}

	/**
	 * Sends a <em>notification</em> message to a remote service.
	 * 
	 * @param notificationFeedID
	 *            the local name of the <em>notification</em> feed (wired to a remote service in the Fabric Registry).
	 * 
	 * @param notification
	 *            the notification message.
	 * 
	 * @throws Exception
	 */
	public void notify(String notificationFeedID, byte[] notification) throws Exception {

		/* If this is a valid notification feed... */
		if (systemServices.notifyFeedIDs().contains(notificationFeedID)) {

			/* Determine the target of the notification message */
			ServiceDescriptor notificationFeedDescriptor = new ServiceDescriptor(systemDescriptor.platform(),
					systemDescriptor.system(), notificationFeedID);
			ArrayList<ServiceDescriptor> deliverToFeedDescriptors = systemServices.wiredNotificationFeedMappings().get(
					notificationFeedDescriptor);

			/* If this is a wired feed... */
			if (deliverToFeedDescriptors != null) {

				/* For each target... */
				for (ServiceDescriptor nextDescriptor : deliverToFeedDescriptors) {

					/* Build the notification service message */
					IServiceMessage notificationMessage = buildServiceMessage(nextDescriptor, null, null, notification);

					/* Send the notification message to the local Fabric Manager */
					sendServiceMessage(notificationMessage);

				}

			}

		} else {

			String error = format("Invalid notification feed ID: %s", notificationFeedID);
			logger.log(Level.SEVERE, error);
			throw new IllegalArgumentException(error);

		}
	}

	/**
	 * Sends a <em>notification</em> message to a remote service.
	 * 
	 * @param listenerService
	 *            the descriptor for the remote service to be notified.
	 * 
	 * @param notificationService
	 *            the descriptor for the local service that generated the notification.
	 * 
	 * @param notification
	 *            the notification message.
	 * 
	 * @param encoding
	 *            the encoding of the request message.
	 * 
	 * @param correlationID
	 *            the correlation ID associated with this request.
	 * 
	 * @throws Exception
	 */
	public String notify(ServiceDescriptor listenerService, ServiceDescriptor notificationService, byte[] notification,
			String encoding, String correlationID) throws Exception {

		/* If this is a valid notification feed... */
		if (systemServices.notifyFeedIDs().contains(notificationService.service())) {

			/* Build the request service message */
			IServiceMessage notificationMessage = buildServiceMessage(listenerService, notificationService,
					correlationID, notification);
			notificationMessage.setProperty("f:encoding", encoding);

			/* Send the notification message to the local Fabric Manager */
			sendServiceMessage(notificationMessage);

		} else {

			String error = format("Invalid notification service ID: %s", notificationService);
			logger.log(Level.SEVERE, error);
			throw new IllegalArgumentException(error);

		}

		return correlationID;

	}

	/**
	 * Sends a request message to a remote service.
	 * 
	 * @param solicitResponseFeedID
	 *            the local name of the service to be invoked (wired to a remote service in the Fabric Registry).
	 * 
	 * @param request
	 *            the request message.
	 * 
	 * @return the correlation ID for this request, or <code>null</code> of the feed is not wired to a request/response
	 *         service.
	 * 
	 * @throws Exception
	 */
	public String request(String solicitResponseFeedID, byte[] request) throws Exception {

		/* Generate a correlation ID */
		String correlationID = FabricMessageFactory.generateUID();

		/* Make the request */
		return request(correlationID, solicitResponseFeedID, request);

	}

	/**
	 * Sends a request message to a remote service, configured by wiring.
	 * 
	 * @param correlationID
	 *            the correlation ID associated with this request.
	 * 
	 * @param solicitResponseServiceID
	 *            the local name of the service to be invoked (wired to a remote service in the Fabric Registry).
	 * 
	 * @param request
	 *            the request message.
	 * 
	 * @return the correlation ID for this request, or <code>null</code> of the feed is not wired to a request/response
	 *         service.
	 * 
	 * @throws Exception
	 */
	public String request(String correlationID, String solicitResponseServiceID, byte[] request) throws Exception {

		String requestCorrelationID = null;

		/* If this is a valid solicit response feed... */
		if (systemServices.solicitResponseFeedIDs().contains(solicitResponseServiceID)) {

			/* Determine the target of the notification message */
			ServiceDescriptor solicitResponseFeedDescriptor = new ServiceDescriptor(systemDescriptor.platform(),
					systemDescriptor.system(), solicitResponseServiceID);
			ArrayList<ServiceDescriptor> deliverToFeedDescriptors = systemServices.wiredSolicitResponseFeedMappings()
					.get(solicitResponseFeedDescriptor);

			/* If this is a wired feed... */
			if (deliverToFeedDescriptors != null) {

				/* For each target... */
				for (ServiceDescriptor nextDescriptor : deliverToFeedDescriptors) {

					/* Build the notification service message */
					IServiceMessage solicitResponseMessage = buildServiceMessage(nextDescriptor,
							solicitResponseFeedDescriptor, correlationID, request);

					/* Send the notification message to the local Fabric Manager */
					sendServiceMessage(solicitResponseMessage);

				}

				requestCorrelationID = correlationID;

			}

		} else {

			String error = format("Invalid solicit response feed ID: %s", solicitResponseServiceID);
			logger.log(Level.SEVERE, error);
			throw new IllegalArgumentException(error);

		}

		return requestCorrelationID;

	}

	/**
	 * Sends a request message to a remote service.
	 * 
	 * @param correlationID
	 *            the correlation ID associated with this request.
	 * 
	 * @param requestResponseService
	 *            the descriptor for the remote service to be invoked.
	 * 
	 * @param solicitResponseService
	 *            the descriptor for the local service to which the response will be delivered.
	 * 
	 * @param request
	 *            the request message.
	 * 
	 * @param encoding
	 *            the encoding of the request message.
	 * 
	 * @return the correlation ID for this request, or <code>null</code> of the feed is not wired to a request/response
	 *         service.
	 * 
	 * @throws Exception
	 */
	public String request(String correlationID, ServiceDescriptor requestResponseService,
			ServiceDescriptor solicitResponseService, byte[] request, String encoding) throws Exception {

		/* If this is a valid solicit response feed... */
		if (systemServices.solicitResponseFeedIDs().contains(solicitResponseService.service())) {

			/* Build the request service message */
			IServiceMessage solicitResponseMessage = buildServiceMessage(requestResponseService,
					solicitResponseService, correlationID, request);
			solicitResponseMessage.setProperty("f:encoding", encoding);

			/* Send the notification message to the local Fabric Manager */
			sendServiceMessage(solicitResponseMessage);

		} else {

			String error = format("Invalid solicit response feed ID: %s", solicitResponseService);
			logger.log(Level.SEVERE, error);
			throw new IllegalArgumentException(error);

		}

		return correlationID;

	}

	/**
	 * Sends a response message to a remote service.
	 * 
	 * @param correlationID
	 *            the correlation ID associated with this response.
	 * 
	 * @param responseFeedDescriptor
	 *            the destination for the response message.
	 * 
	 * @param producerFeedDescriptor
	 *            the producer of the response message.
	 * 
	 * @param response
	 *            the request message.
	 * 
	 * @return the correlation ID associated with this request.
	 * 
	 * @throws Exception
	 */
	public String respond(String correlationID, ServiceDescriptor responseFeedDescriptor,
			ServiceDescriptor producerFeedDescriptor, byte[] response) throws Exception {

		/* Build the response service message */
		IServiceMessage responseMessage = buildServiceMessage(responseFeedDescriptor, producerFeedDescriptor,
				correlationID, response);

		/* Send the notification message to the local Fabric Manager */
		sendServiceMessage(responseMessage);

		return responseMessage.getCorrelationID();

	}

	/**
	 * Answers the adapter-specific ID of the client, used to target messages sent to the client.
	 * 
	 * @return the ID.
	 */
	public Object getClient() {

		return client;

	}

	/**
	 * Sets the adapter-specific ID of the client, used to target messages sent to the client.
	 * 
	 * @param clientID
	 *            the new client ID.
	 */
	public void setClient(String client) {

		this.client = client;

	}

	/**
	 * Answers the name of the service class.
	 * 
	 * @return the class name.
	 */
	public String className() {

		return className;

	}

	/**
	 * Answers the Fabric client class associated with this instance.
	 * 
	 * @return the Fabric client instance.
	 */
	public FabricClient fabricClient() {

		return runtimeManager.fabricClient();

	}

	/**
	 * Answers the list of <em>input</em> feed names.
	 * 
	 * @return the input feed names.
	 */
	public List<String> inputFeedIDs() {

		return systemServices.inputFeedIDs();

	}

	/**
	 * Answers <code>true</code> if the specified feed is managed by the container (rather than the service itself).
	 * 
	 * @return <code>true</code> if the feed is managed by the container, <code>false</code> otherwise.
	 */
	public boolean isManagedFeed(ServiceDescriptor serviceDescriptor) {

		boolean isManagedFeed = false;

		/* If this is a managed feed... */
		if (systemServices.solicitResponseFeeds().containsKey(serviceDescriptor)
				|| systemServices.requestResponseFeeds().containsKey(serviceDescriptor)
				|| systemServices.oneWayFeeds().containsKey(serviceDescriptor)
				|| systemServices.wiredInputFeeds().containsKey(serviceDescriptor)) {

			isManagedFeed = true;

		}

		return isManagedFeed;
	}

	/**
	 * Answers the list of <em>notify</em> feed names.
	 * 
	 * @return the notify feed names.
	 */
	public List<String> notifyFeedIDs() {

		return systemServices.notifyFeedIDs();

	}

	/**
	 * Answers the list of <em>one way</em> feed names.
	 * 
	 * @return the one way feed names.
	 */
	public List<String> oneWayFeedIDs() {

		return systemServices.oneWayFeedIDs();

	}

	/**
	 * Answers the list of <em>output</em> feed names.
	 * 
	 * @return the output feed names.
	 */
	public List<String> outputFeedIDs() {

		return systemServices.outputFeedIDs();

	}

	/**
	 * Answers the list of <em>request response</em> feed names.
	 * 
	 * @return the request response feed names.
	 */
	public List<String> requestResponseFeedIDs() {

		return systemServices.requestResponseFeedIDs();

	}

	/**
	 * Answers the ID of this instance.
	 * 
	 * @return the ID.
	 */
	public SystemDescriptor systemDescriptor() {

		return systemDescriptor;

	}

	/**
	 * Answers the list of <em>solicit response</em> feed names.
	 * 
	 * @return the solicit response feed names.
	 */
	public List<String> solicitResponseFeedIDs() {

		return systemServices.solicitResponseFeedIDs();

	}

	/**
	 * Update the availability status of the service in the Registry.
	 * 
	 * @param availability
	 *            the new availability status.
	 * 
	 */
	private void updateServiceAvailability(String availability) {

		/* Lookup the service record in the Registry */
		SystemFactory systemFactory = FabricRegistry.getSystemFactory(doQueryLocal);
		System system = systemFactory.getSystemsById(systemDescriptor.platform(), systemDescriptor.system());

		try {

			/* Update the availability and commit */
			system.setAvailability(availability);
			systemFactory.update(system);

		} catch (Exception e) {

			logger.log(Level.SEVERE, "Cannot set availability to \"{0}\" on service \"{1}/{2}\": {3}", new Object[] {
					system.getAvailability(), system.getPlatformId(), system.getId(), LogUtil.stackTrace(e)});

		}
	}

	/**
	 * Send the specified service message.
	 * 
	 * @param message
	 *            the message to send.
	 * 
	 * @throws Exception
	 */
	public void sendServiceMessage(IServiceMessage serviceMessage) throws Exception {

		/* Send the message to the local Fabric Manager */

		logger.log(Level.FINEST, "Sending service message to local Fabric Manager: %s", serviceMessage.toString());

		try {

			fabricClient.getIOChannels().sendCommandsChannel.write(serviceMessage.toWireBytes());

		} catch (Exception e) {

			String targetNode = serviceMessage.getRouting().endNode();
			String message = format("Cannot send notification service message to node '%s': %s", targetNode, LogUtil
					.stackTrace(e));
			logger.log(Level.SEVERE, message);
			throw new Exception(message, e);

		}

	}

	/**
	 * Answers a partially configured proxy publisher service message.
	 * 
	 * @param deliverToFeed
	 *            the feed to which the message is to be delivered.
	 * 
	 * @param replyToFeed
	 *            the feed to receive the result (if any).
	 * 
	 * @param payload
	 *            the message payload.
	 * 
	 * @throws Exception
	 */
	private IServiceMessage buildServiceMessage(ServiceDescriptor deliverToFeed, ServiceDescriptor replyToFeed,
			byte[] payload) throws Exception {

		/* Generate a correlation ID */
		String correlationID = FabricMessageFactory.generateUID();

		/* Build and return the message */
		return buildServiceMessage(deliverToFeed, replyToFeed, correlationID, payload);

	}

	/**
	 * Answers a partially configured proxy publisher service message.
	 * 
	 * @param deliverToFeed
	 *            the feed to which the message is to be delivered.
	 * 
	 * @param sourceFeed
	 *            the feed that generated the message.
	 * 
	 * @param correlationID
	 *            the correlation ID associated with this message.
	 * 
	 * @param payload
	 *            the message payload.
	 * 
	 * @throws Exception
	 */
	private IServiceMessage buildServiceMessage(ServiceDescriptor deliverToFeed, ServiceDescriptor sourceFeed,
			String correlationID, byte[] payload) throws Exception {

		/* Initialize a service message to hold the request */
		ServiceMessage serviceMessage = new ServiceMessage();

		/* Indicate that this is a built-in Fabric plug-in */
		serviceMessage.setServiceFamilyName(Fabric.FABRIC_PLUGIN_FAMILY);

		/* Indicate that this message should not be actioned along the route from subscriber to the publisher */
		serviceMessage.setActionEnRoute(false);

		/* If we have been provided with a correlation ID... */
		if (correlationID != null) {
			serviceMessage.setCorrelationID(correlationID);
		}

		/* Indicate that we do not want notifications that this message is handled */
		serviceMessage.setNotification(false);

		/* Register to receive notifications related to this message */
		// fabricClient.registerNotificationHandler(correlationID, this);

		/*
		 * Configure this as a proxy message.
		 */

		/* Set the service name: i.e. indicate that this is a message for the feed manager */
		serviceMessage.setServiceName("fabric.services.proxypublisher.ProxyPublisherService");
		serviceMessage.setAction(IServiceMessage.ACTION_PUBLISH_ON_NODE);
		serviceMessage.setEvent(IServiceMessage.EVENT_ACTOR_REQUEST);

		/* Indicate who and where this message originates from */
		serviceMessage.setProperty(IServiceMessage.PROPERTY_ACTOR, fabricClient.actor());
		serviceMessage.setProperty(IServiceMessage.PROPERTY_ACTOR_PLATFORM, fabricClient.platform());

		/* Set the target feed */
		serviceMessage.setProperty(IServiceMessage.PROPERTY_DELIVER_TO_FEED, deliverToFeed.toString());

		/* If there is a reply-to feed... */
		if (sourceFeed != null) {

			serviceMessage.setProperty(IServiceMessage.PROPERTY_REPLY_TO_FEED, sourceFeed.toString());

		}

		/* Store the notification payload in the notification message */
		IMessagePayload messagePayload = new MessagePayload();
		messagePayload.setPayloadBytes(payload);
		serviceMessage.setPayload(messagePayload);

		/* Set the message's routing */
		String[] routeNodes = getRouteNodes(deliverToFeed);
		StaticRouting messageRouting = new StaticRouting(routeNodes);
		serviceMessage.setRouting(messageRouting);

		return serviceMessage;

	}

	/**
	 * Get the route (node list) to a target feed.
	 * 
	 * @param serviceDescriptor
	 *            the target feed
	 * 
	 * @return the list of nodes, or <code>null</code> if no route is found.
	 * 
	 * @throws Exception
	 */
	private String[] getRouteNodes(ServiceDescriptor serviceDescriptor) throws Exception {

		/* To hold the result */
		String[] routeNodes = null;

		/* Get the node to which were connected */
		String homeNode = fabricClient.homeNode();

		/* Get the node to which the feed's platform is connected */
		PlatformFactory platformFactory = FabricRegistry.getPlatformFactory(doQueryLocal);
		Platform targetPlatform = platformFactory.getPlatformById(serviceDescriptor.platform());
		String targetNode = targetPlatform.getNodeId();

		/* Get the routes to the specified feed */
		RouteFactory routeFactory = FabricRegistry.getRouteFactory(doQueryLocal);
		Route[] routesToFeed = routeFactory.getRoutes(homeNode, targetNode);

		/* If there is at least one route... */
		if (routesToFeed.length > 0) {

			/* Since these routes are sorted by ordinal we take the first, and extract the node list */
			routeNodes = routeFactory.getRouteNodes(homeNode, targetNode, routesToFeed[0].getRoute());

		} else {

			String message = format("No route found from node '%s' to node '%s'", homeNode, targetNode);
			logger.log(Level.SEVERE, message);
			throw new Exception(message);

		}

		return routeNodes;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		/*
		 * Note: the name of the service instance is formed from the name of the service class, and the ID of this
		 * service instance.
		 */

		/* Initialize the instance with the name of the container Fabric service class */
		StringBuffer toString = new StringBuffer(systemDescriptor.toString());
		toString.append('/');
		toString.append(className);

		return toString.toString();

	}

	/**
	 * Answers the flag indicating if this system is running.
	 * 
	 * @return <code>true</code> if this system is running, <code>false</code> otherwise.
	 */
	public boolean isRunning() {

		return isRunning;
	}
}
