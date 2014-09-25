/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2010, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.systems;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.Fabric;
import fabric.FabricBus;
import fabric.ServiceDescriptor;
import fabric.SystemDescriptor;
import fabric.TaskServiceDescriptor;
import fabric.bus.feeds.ISubscription;
import fabric.bus.feeds.ISubscriptionCallback;
import fabric.bus.feeds.impl.Subscription;
import fabric.bus.messages.IFeedMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.client.FabricPlatform;
import fabric.core.logging.LogUtil;
import fabric.registry.FabricRegistry;
import fabric.registry.System;
import fabric.registry.SystemFactory;

/**
 * Manages a set of active systems.
 */
public class RuntimeManager extends FabricBus implements ISubscriptionCallback {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010, 2014";

	/*
	 * Class constants
	 */

	/** The descriptor for the Registry update notification service. */
	private static final TaskServiceDescriptor registryUpdatesDescriptor = new TaskServiceDescriptor("DEFAULT",
			"$fabric", "$registry", "$registry_updates");

	/*
	 * Class fields
	 */

	/** To hold the list of active systems, including both those in the running and stopped states. */
	private final HashMap<SystemDescriptor, SystemRuntime> activeSystems = new HashMap<SystemDescriptor, SystemRuntime>();

	/** Flag indicating if Registry queries should be local or distributed. */
	private boolean doQueryLocal = false;

	/** The connection to the Fabric. */
	private FabricPlatform fabricClient = null;

	/** The subscription to Registry update notifications. */
	private ISubscription registryUpdates = null;

	/*
	 * Class methods
	 */

	public RuntimeManager() {

		this(Logger.getLogger("fabric.services.systems"));
	}

	public RuntimeManager(Logger logger) {

		this.logger = logger;
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param fabricClient
	 *            the connection to the Fabric.
	 */
	public RuntimeManager(FabricPlatform fabricClient) {

		this.fabricClient = fabricClient;

		/* Determine if Registry queries should be local or distributed */
		doQueryLocal = Boolean.parseBoolean(config("fabric.composition.queryLocal", "false"));

	}

	/**
	 * Initialises this instance.
	 */
	public void init() {

		try {

			/* Subscribe to Registry update notifications */
			registryUpdates = new Subscription(fabricClient);
			registryUpdates.subscribe(registryUpdatesDescriptor, this);

		} catch (Exception e) {

			String message = Fabric.format(
					"Cannot subscribe to service '%s'; active subscription functions will not be available: %s",
					LogUtil.stackTrace(e), registryUpdatesDescriptor);
			logger.log(Level.WARNING, message);

		}
	}

	/**
	 * Stops this instance.
	 */
	public void stop() {

		try {

			/* Unsubscribe from Registry update notifications */
			registryUpdates.unsubscribe();

		} catch (Exception e) {

			String message = Fabric.format("Cannot unsubscribe from feed '%s': %s", LogUtil.stackTrace(e),
					registryUpdatesDescriptor);
			logger.log(Level.WARNING, message);

		}
	}

	/**
	 * Starts a system.
	 * 
	 * @param systemDescriptor
	 *            the system to start.
	 * 
	 * @param client
	 *            adapter-specific identifier for the client making the request.
	 * 
	 * @param adapterProxy
	 *            the name of the class implementing the system adapter proxy for the JSON Fabric client.
	 * 
	 * @return the status.
	 */
	public RuntimeStatus start(SystemDescriptor systemDescriptor, Object client, String adapterProxy) {

		RuntimeStatus status = null;

		SystemRuntime systemRuntime = activeSystems.get(systemDescriptor);

		/* If there is already a running service instance with this name... */
		if (systemRuntime != null && systemRuntime.isRunning()) {

			String message = format("System already running: %s", systemDescriptor);
			logger.log(Level.WARNING, message);
			status = new RuntimeStatus(RuntimeStatus.Status.ALREADY_RUNNING, message);

		} else {

			/* Look up the system in the Registry */
			SystemFactory systemFactory = FabricRegistry.getSystemFactory(doQueryLocal);
			System system = systemFactory.getSystemsById(systemDescriptor.platform(), systemDescriptor.system());

			/* If no valid system was found... */
			if (system == null || system.getTypeId() == null) {

				String message = format("System '%s' not found", systemDescriptor.toString());
				logger.log(Level.WARNING, message);
				status = new RuntimeStatus(RuntimeStatus.Status.NOT_FOUND, message);

			} else {

				try {

					if (systemRuntime == null) {

						/* Create it */
						systemRuntime = new SystemRuntime(systemDescriptor, client, adapterProxy, this);

					}

					/* Instantiate the service */
					systemRuntime.instantiate();

					/* Initialize the new service instance */
					systemRuntime.start();

				} catch (Exception e) {

					String message = format("Failed to start system '%s':", e, systemDescriptor.toString());
					logger.log(Level.SEVERE, message);
					status = new RuntimeStatus(RuntimeStatus.Status.START_FAILED, message);

				}
			}
		}

		if (status == null) {
			/* The system was started OK */
			activeSystems.put(systemDescriptor, systemRuntime);
			status = new RuntimeStatus(RuntimeStatus.Status.OK, RuntimeStatus.MESSAGE_OK);
		}

		return status;
	}

	/**
	 * Stops a running instance of this service.
	 * 
	 * @param systemDescriptor
	 *            the service to stop.
	 * 
	 * @param response
	 *            the result of the operation.
	 * 
	 * @return the status.
	 */
	public RuntimeStatus stop(SystemDescriptor systemDescriptor) {

		RuntimeStatus status = null;

		/* Get the service instance */
		SystemRuntime systemRuntime = activeSystems.get(systemDescriptor);

		/* If there is not a running service instance with this name... */
		if (systemRuntime == null || !systemRuntime.isRunning()) {

			/* Make sure that this is reflected in the Registry */
			updateAvailability(systemDescriptor, "UNAVAILABLE");

			String message = format("System not running: %s", systemDescriptor);
			logger.log(Level.WARNING, message);
			status = new RuntimeStatus(RuntimeStatus.Status.NOT_RUNNING, message);

		} else {

			/* Stop the instance */
			systemRuntime.stop();

			/* Remove it from the manager */
			activeSystems.remove(systemDescriptor);

		}

		if (status == null) {
			/* The system was started OK */
			status = new RuntimeStatus(RuntimeStatus.Status.OK, RuntimeStatus.MESSAGE_OK);
		}

		return status;

	}

	/**
	 * Update the availability status of the service in the Registry.
	 * 
	 * @param systemDescriptor
	 *            the service for which status is being changed.
	 * 
	 * @param availability
	 *            the new availability status.
	 */
	private void updateAvailability(SystemDescriptor systemDescriptor, String availability) {

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
	 * Sends a request to a request-response service.
	 * 
	 * @param requestResponseService
	 *            the target for the request.
	 * 
	 * @param solicitResponseService
	 *            to reply-to service.
	 * 
	 * @param msg
	 *            the message payload.
	 * 
	 * @param encoding
	 *            the payload encoding.
	 * 
	 * @param correlId
	 *            the correlation ID for the request.
	 * 
	 * @return the status.
	 */
	public RuntimeStatus request(ServiceDescriptor requestResponseService, ServiceDescriptor solicitResponseService,
			String msg, String encoding, String correlId) {

		RuntimeStatus status = null;

		SystemDescriptor systemDescriptor = new SystemDescriptor(solicitResponseService.platform(),
				solicitResponseService.system());
		SystemRuntime systemRuntime = activeSystems.get(systemDescriptor);

		/* If there is a running service instance... */
		if (systemRuntime != null && systemRuntime.isRunning()) {

			try {

				/* Send the request message */
				systemRuntime.request(correlId, requestResponseService, solicitResponseService, msg.getBytes(),
						encoding);

			} catch (Exception e) {

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(baos);
				e.printStackTrace(ps);

				String message = format("Error sending request message to request/response service '%s': %s",
						requestResponseService, baos.toString());

				logger.log(Level.WARNING, message);
				status = new RuntimeStatus(RuntimeStatus.Status.SEND_REQUEST_FAILED, message);

			}

		} else {

			String message = format("System is not running: %s", systemDescriptor);
			logger.log(Level.WARNING, message);
			status = new RuntimeStatus(RuntimeStatus.Status.NOT_RUNNING, message);

		}

		if (status == null) {
			/* The system was started OK */
			status = new RuntimeStatus(RuntimeStatus.Status.OK, RuntimeStatus.MESSAGE_OK);
		}

		return status;
	}

	/**
	 * Sends a response to a solicit-response service.
	 * 
	 * @param sendTo
	 *            the target for the request.
	 * 
	 * @param producer
	 *            to producer of the result.
	 * 
	 * @param msg
	 *            the message payload.
	 * 
	 * @param encoding
	 *            the payload encoding.
	 * 
	 * @param correlId
	 *            the correlation ID for the request.
	 * 
	 * @return the status.
	 */
	public RuntimeStatus response(ServiceDescriptor sendTo, ServiceDescriptor producer, String msg, String encoding,
			String correlId) {

		RuntimeStatus status = null;

		SystemDescriptor systemDescriptor = new SystemDescriptor(producer.platform(), producer.system());
		SystemRuntime systemRuntime = activeSystems.get(systemDescriptor);

		/* If there is a running service instance... */
		if (systemRuntime != null && systemRuntime.isRunning()) {

			try {

				/* Send the response message */
				systemRuntime.respond(correlId, sendTo, producer, msg.getBytes());

			} catch (Exception e) {

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(baos);
				e.printStackTrace(ps);

				String message = format("Error sending response message to requesting service '%s': %s", sendTo, baos
						.toString());
				logger.log(Level.WARNING, message);
				status = new RuntimeStatus(RuntimeStatus.Status.SEND_REQUEST_FAILED, message);

			}

		} else {

			String message = format("System is not running: %s", (SystemDescriptor) producer);
			logger.log(Level.WARNING, message);
			status = new RuntimeStatus(RuntimeStatus.Status.NOT_RUNNING, message);

		}

		if (status == null) {
			/* The system was started OK */
			status = new RuntimeStatus(RuntimeStatus.Status.OK, RuntimeStatus.MESSAGE_OK);
		}

		return status;
	}

	/**
	 * Answers the Fabric client associated with this instance.
	 * 
	 * @return the Fabric client instance.
	 */
	public FabricPlatform fabricClient() {

		return fabricClient;

	}

	/**
	 * Sends a notification to a listener service.
	 * 
	 * @param listenerService
	 *            the target for the notification.
	 * 
	 * @param notificationService
	 *            the notifying service.
	 * 
	 * @param msg
	 *            the message payload.
	 * 
	 * @param encoding
	 *            the payload encoding.
	 * 
	 * @param correlId
	 *            the correlation ID for the request.
	 * 
	 * @return the status.
	 */
	public RuntimeStatus notify(ServiceDescriptor listenerService, ServiceDescriptor notificationService, String msg,
			String encoding, String correlId) {

		RuntimeStatus status = null;

		SystemDescriptor systemDescriptor = new SystemDescriptor(notificationService.platform(), notificationService
				.system());
		SystemRuntime systemRuntime = activeSystems.get(systemDescriptor);

		/* If there is a running service instance... */
		if (systemRuntime != null && systemRuntime.isRunning()) {

			try {

				/* Send the request message */
				systemRuntime.notify(listenerService, notificationService, msg.getBytes(), encoding, correlId);

			} catch (Exception e) {

				String message = format("Error sending notification message to listener service %s: %s",
						listenerService, LogUtil.stackTrace(e));
				logger.log(Level.WARNING, message);
				status = new RuntimeStatus(RuntimeStatus.Status.SEND_NOTIFICATION_FAILED, message);

			}

		} else {

			String message = format("System is not running: %s", systemDescriptor);
			logger.log(Level.WARNING, message);
			status = new RuntimeStatus(RuntimeStatus.Status.NOT_RUNNING, message);

		}

		if (status == null) {
			/* The system was started OK */
			status = new RuntimeStatus(RuntimeStatus.Status.OK, RuntimeStatus.MESSAGE_OK);
		}

		return status;
	}

	/**
	 * Publishes an output feed message.
	 * 
	 * @param outputFeedService
	 *            the publishing service.
	 * 
	 * @param msg
	 *            the message payload.
	 * 
	 * @param encoding
	 *            the payload encoding.
	 * 
	 * @return the status.
	 */
	public RuntimeStatus publish(ServiceDescriptor outputFeedService, String msg, String encoding) {

		RuntimeStatus status = null;

		SystemDescriptor systemDescriptor = new SystemDescriptor(outputFeedService.platform(), outputFeedService
				.system());
		SystemRuntime systemRuntime = activeSystems.get(systemDescriptor);

		/* If there is a running service instance... */
		if (systemRuntime != null && systemRuntime.isRunning()) {

			try {

				/* Publish the message */
				systemRuntime.publish(outputFeedService, msg.getBytes(), encoding);

			} catch (Exception e) {

				String message = format("Error publishing message to output-feed service %s:\n%s", outputFeedService, e);
				logger.log(Level.WARNING, message);
				status = new RuntimeStatus(RuntimeStatus.Status.PUBLISH_FAILED, message);

			}

		} else {

			String message = format("System is not running: %s", systemDescriptor);
			logger.log(Level.WARNING, message);
			status = new RuntimeStatus(RuntimeStatus.Status.NOT_RUNNING, message);

		}

		if (status == null) {
			/* The system was started OK */
			status = new RuntimeStatus(RuntimeStatus.Status.OK, RuntimeStatus.MESSAGE_OK);
		}

		return status;
	}

	/**
	 * Subscribes to an output feed.
	 * 
	 * @param outputFeedService
	 *            the publishing service.
	 * 
	 * @param inputFeedService
	 *            the local service to which feed messages will be delivered.
	 * 
	 * @return the status.
	 */
	public RuntimeStatus subscribe(ServiceDescriptor outputFeedService, ServiceDescriptor inputFeedService) {

		RuntimeStatus status = null;

		SystemDescriptor systemDescriptor = new SystemDescriptor(inputFeedService.platform(), inputFeedService.system());
		SystemRuntime systemRuntime = activeSystems.get(systemDescriptor);

		/* If there is a running system instance... */
		if (systemRuntime != null && systemRuntime.isRunning()) {

			try {

				/* Subscribe */
				systemRuntime.subscribe(outputFeedService, inputFeedService);

			} catch (Exception e) {

				String message = format("Error subscribing to output-feed service %s (input-feed %s):\n%s",
						outputFeedService, inputFeedService, e);
				logger.log(Level.WARNING, message);
				status = new RuntimeStatus(RuntimeStatus.Status.SUBSCRIBE_FAILED, message);

			}

		} else {

			String message = format("System is not running: %s", systemDescriptor);
			logger.log(Level.WARNING, message);
			status = new RuntimeStatus(RuntimeStatus.Status.NOT_RUNNING, message);

		}

		if (status == null) {
			/* The system was started OK */
			status = new RuntimeStatus(RuntimeStatus.Status.OK, RuntimeStatus.MESSAGE_OK);
		}

		return status;
	}

	/**
	 * Unsubscribes from one or more output feeds.
	 * 
	 * @param outputFeedServices
	 *            the list of output feed services. If <code>null</code> then all feeds associated with the specified
	 *            input feed service are unsubscribed.
	 * 
	 * @param inputFeedService
	 *            the local service to which feed messages are being delivered.
	 * 
	 * @return the status.
	 */
	public RuntimeStatus unsubscribe(ServiceDescriptor[] outputFeedServices, ServiceDescriptor inputFeedService) {

		RuntimeStatus status = null;

		SystemDescriptor systemDescriptor = new SystemDescriptor(inputFeedService.platform(), inputFeedService.system());
		SystemRuntime systemRuntime = activeSystems.get(systemDescriptor);

		/* If there is a running system instance... */
		if (systemRuntime != null && systemRuntime.isRunning()) {

			try {

				/* Unsubscribe */
				systemRuntime.unsubscribe(outputFeedServices, inputFeedService);

			} catch (Exception e) {

				String message = format("Error unsubscribing from output-feed services (input-feed %s):\n%s",
						inputFeedService, e);
				logger.log(Level.WARNING, message);
				status = new RuntimeStatus(RuntimeStatus.Status.UNSUBSCRIBE_FAILED, message);

			}

		} else {

			String message = format("System is not running: %s", systemDescriptor);
			logger.log(Level.WARNING, message);
			status = new RuntimeStatus(RuntimeStatus.Status.NOT_RUNNING, message);

		}

		if (status == null) {
			/* The system was started OK */
			status = new RuntimeStatus(RuntimeStatus.Status.OK, RuntimeStatus.MESSAGE_OK);
		}

		return status;
	}

	/**
	 * Cleans up any running systems associated with the specified client.
	 * 
	 * @param client
	 *            adapter-specific identifier for the client making the request.
	 * 
	 * @return the status.
	 */
	public void cleanup(Object client) {

		HashMap<SystemDescriptor, SystemRuntime> activeSystemsCopy = (HashMap<SystemDescriptor, SystemRuntime>) activeSystems
				.clone();

		/* For each active system... */
		for (SystemDescriptor nextSystem : activeSystemsCopy.keySet()) {

			/* Get the record for the next system */
			SystemRuntime nextRuntime = activeSystemsCopy.get(nextSystem);
			Object nextClient = nextRuntime.getClient();

			/* If the record is associated with the specified client... */
			if (nextClient != null && nextClient.equals(client)) {

				/* Stop the system */
				stop(nextSystem);

			}
		}
	}

	/**
	 * @see fabric.bus.feeds.ISubscriptionCallback#startSubscriptionCallback()
	 */
	@Override
	public void startSubscriptionCallback() {
		java.lang.System.out.println("startSubscriptionCallback()");
	}

	/**
	 * @see fabric.bus.feeds.ISubscriptionCallback#handleSubscriptionMessage(fabric.bus.messages.IFeedMessage)
	 */
	@Override
	public void handleSubscriptionMessage(IFeedMessage message) {
		byte[] payloadBytes = message.getPayload().getPayload();
		String payload = new String(payloadBytes);
		java.lang.System.out.println("handleSubscriptionMessage(): " + payload);
	}

	/**
	 * @see fabric.bus.feeds.ISubscriptionCallback#handleSubscriptionEvent(fabric.bus.feeds.ISubscription, int,
	 *      fabric.bus.messages.IServiceMessage)
	 */
	@Override
	public void handleSubscriptionEvent(ISubscription subscription, int event, IServiceMessage message) {
		java.lang.System.out.println("handleSubscriptionEvent():\n" + message.toString());
	}

	/**
	 * @see fabric.bus.feeds.ISubscriptionCallback#cancelSubscriptionCallback()
	 */
	@Override
	public void cancelSubscriptionCallback() {
		java.lang.System.out.println("cancelSubscriptionCallback()");
	}
}
