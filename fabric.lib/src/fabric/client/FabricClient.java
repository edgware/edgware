/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2006, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.Fabric;
import fabric.FabricBus;
import fabric.FabricShutdownHook;
import fabric.IFabricShutdownHookAction;
import fabric.bus.BusIOChannels;
import fabric.bus.feeds.ISubscription;
import fabric.bus.messages.FabricMessageFactory;
import fabric.bus.messages.IConnectionMessage;
import fabric.bus.messages.IFabricMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.messages.impl.ConnectionMessage;
import fabric.bus.services.IClientServiceDispatcher;
import fabric.bus.services.impl.ClientServiceDispatcher;
import fabric.client.services.ClientNotificationService;
import fabric.client.services.IClientNotificationHandler;
import fabric.client.services.IHomeNodeConnectivityCallback;
import fabric.core.io.ICallback;
import fabric.core.io.InputTopic;
import fabric.core.io.Message;
import fabric.core.io.OutputTopic;
import fabric.core.logging.LogUtil;
import fabric.core.properties.ConfigProperties;
import fabric.registry.Actor;
import fabric.registry.ActorFactory;
import fabric.registry.FabricRegistry;
import fabric.registry.Platform;
import fabric.registry.PlatformFactory;
import fabric.registry.Type;
import fabric.registry.TypeFactory;
import fabric.registry.exception.DuplicateKeyException;

/**
 * Class managing a client connection to the Fabric.
 * <p>
 * This class is used by Fabric client applications to establish a connection to the Fabric in order to:
 * <ul>
 * <li>Set up subscriptions.</li>
 * <li>Send control messages to the Fabric and connected assets.</li>
 * <li>Receive and handle service messages via the Fabric.</li>
 * <li>Register and de-register new platform types and platforms, actor types and actors.</li>
 * </ul>
 * This class provides the containing within which client service messages are handled. Such services are entirely
 * application defined, and provide a Fabric-based mechanism to communicate with and control Fabric clients.
 * </p>
 */
public class FabricClient extends FabricBus implements ICallback, IFabricShutdownHookAction, IFabricClientServices {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2006, 2012";

	/*
	 * Class static fields
	 */

	/**
	 * The current list of subscriptions, maintained in order that clean-up operations can be performed if the client
	 * terminates abnormally
	 */
	protected static HashMap<String, ISubscription> activeSubscriptions = new HashMap<String, ISubscription>();

	/** The shutdown hook for this JVM */
	protected static FabricShutdownHook shutdownHook = null;

	/*
	 * Class fields
	 */

	/** To hold the channels and topics used to connect to the Fabric. */
	protected final BusIOChannels ioChannels = new BusIOChannels();

	/** Flag indicating if the connection to the local Fabric node has been made. */
	protected boolean connectedToFabric = false;

	/** Callback to receive notifications when a connection is established/lost with the client's Fabric Manager. */
	protected IClientNotificationHandler connectionCallback = null;

	/** The manager for persistent client services. */
	protected IClientServiceDispatcher serviceDispatcher = null;

	/** The client notification service */
	protected ClientNotificationService notificationService = null;

	/** The ID of the actor associated with the connection. */
	protected String actor = null;

	/** The callback to handle Fabric service messages that are otherwise ignored. */
	protected IHomeNodeConnectivityCallback homeNodeConnectivityCallback = null;

	/**
	 * The ID of the platform associated with the connection, i.e. the actor's platform, for example the name of the
	 * application that is establishing the client connection.
	 */
	protected String platform = null;

	/** Flag indicating if a shutdown of this Fabric client is in progress */
	protected boolean shutdownInProgress = false;

	/*
	 * Static class initialization
	 */

	static {

		/* Register the shutdown hook for this JVM */
		shutdownHook = new FabricShutdownHook("fabric.client.FabricClient.shutdownHook");
		Runtime.getRuntime().addShutdownHook(shutdownHook);

	}

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 * 
	 * @param actor
	 *            the ID of the actor (i.e. the user) making this connection.
	 * 
	 * @param platform
	 *            the ID of the platform (i.e. the application, service, or process etc.) making this connection.
	 * 
	 * @throws Exception
	 */
	public FabricClient(String actor, String platform) throws Exception {

		super(Logger.getLogger("fabric.client"));

		initFabricConfig();

		String homeNode = homeNode();
		String traceTitle = String.format("%s:%s", homeNode, platform);
		initLogging("fabric.client", traceTitle);

		this.actor = actor;
		this.platform = platform;

		/* Register this instance with the shutdown hook */
		shutdownHook.addAction(this);

	}

	public FabricClient(String node, String actor, String platform) throws Exception {

		this(actor, platform);
		this.setHomeNode(node);
	}

	/**
	 * Constructs a new instance.
	 * 
	 * @param actor
	 *            the ID of the actor (i.e. the user) making this connection.
	 * 
	 * @param platform
	 *            the ID of the platform (i.e. the application, service, or process etc.) making this connection.
	 * 
	 * @param connectionCallback
	 *            callback to be notified when the client connects to its Fabric Manager.
	 * @throws Exception
	 */
	public FabricClient(String actor, String platform, IClientNotificationHandler connectionCallback) throws Exception {

		this(actor, platform);
		this.connectionCallback = connectionCallback;

	}

	/**
	 * Constructs a new instance.
	 * 
	 * @param node
	 *            the ID of the Fabric home node for this client.
	 * 
	 * @param actor
	 *            the ID of the actor (i.e. the user) making this connection.
	 * 
	 * @param platform
	 *            the ID of the platform (i.e. the application, service, or process etc.) making this connection.
	 * 
	 * @param connectionCallback
	 *            callback to be notified when the client connects to its Fabric Manager.
	 * 
	 * @throws Exception
	 */
	public FabricClient(String node, String actor, String platform, IClientNotificationHandler connectionCallback)
			throws Exception {

		this(actor, platform, connectionCallback);
		this.setHomeNode(node);

	}

	/**
	 * Answers the ID of the actor (i.e. the user) making this connection.
	 * 
	 * @return the actor ID
	 */
	public String actor() {

		return actor;
	}

	/**
	 * Answers the ID of the platform (i.e. the application, service, or process etc.) making this connection.
	 * 
	 * @return the platform ID
	 */
	public String platform() {

		return platform;
	}

	/**
	 * Registers a new active subscription.
	 * <p>
	 * <strong>Note:</strong> this method is for internal Fabric use only.
	 * </p>
	 * 
	 * @param subscription
	 *            the subscription.
	 */
	public void registerSubscription(ISubscription subscription) {

		synchronized (shutdownHook) {

			if (!shutdownInProgress) {

				/* Generate the class instance string */
				String classInstance = subscription.getClass().getName() + '@'
						+ Integer.toHexString(subscription.hashCode());

				/* Record the subscription */
				activeSubscriptions.put(classInstance, subscription);

			}
		}
	}

	/**
	 * De-registers a new active subscription.
	 * <p>
	 * <strong>Note:</strong> this method is for internal Fabric use only.
	 * </p>
	 * 
	 * @param subscription
	 *            the subscription.
	 */
	public synchronized void deregisterSubscription(ISubscription subscription) {

		synchronized (shutdownHook) {

			if (!shutdownInProgress) {

				/* Generate the class instance string */
				String classInstance = subscription.getClass().getName() + '@'
						+ Integer.toHexString(subscription.hashCode());

				/* Remove the subscription */
				activeSubscriptions.remove(classInstance);

			}
		}
	}

	/**
	 * @see fabric.IFabricShutdownHookAction#shutdown()
	 */
	@Override
	public void shutdown() {

		/* Note that a shutdown is in progress to prevent updates to the data structures used in this method */
		synchronized (shutdownHook) {
			shutdownInProgress = true;
		}

		/* Close all active subscriptions */
		unsubscribeAll();

	}

	/**
	 * Unsubscribe from all active subscriptions.
	 */
	protected void unsubscribeAll() {

		/* For each active subscription... */
		for (Iterator<String> i = activeSubscriptions.keySet().iterator(); i.hasNext();) {

			/* Get the key of the next subscription */
			String key = i.next();

			/* Get the subscription */
			ISubscription nextSubscription = activeSubscriptions.get(key);

			try {

				/* Clean-up the subscription */
				nextSubscription.unsubscribe();

			} catch (Exception e) {

				String instanceName = nextSubscription.getClass().getName() + '@'
						+ Integer.toHexString(nextSubscription.hashCode());
				logger.log(Level.FINER, "Unsubscribe failed in \"{0}\": {1}", new Object[] {instanceName,
						LogUtil.stackTrace(e)});

			}
		}

		/* Deregister actor platform */
		// deregisterPlatform(platform());

	}

	/**
	 * Convenience method to register a new actor type.
	 * 
	 * @param typeID
	 *            the ID of the type. The type ID must be unique.
	 * 
	 * @param description
	 *            the plain text description of the type. May be <code>null</code>.
	 * 
	 * @param attributes
	 *            the attributes of the type (plain or structured text, e.g. XML, CSV, name/value pairs etc.). May be
	 *            <code>null</code>.
	 * 
	 * @param attributesURI
	 *            the URI of the attributes of the type. May be <code>null</code>.
	 * 
	 * @return <code>true</code> if the new actor type was registered, <code>false</code> otherwise.
	 */
	public boolean registerActorType(String typeID, String description, String attributes, String attributesURI) {

		boolean success = true;

		try {

			TypeFactory typeFactory = FabricRegistry.getTypeFactory(true);

			if (null == typeFactory.getActorType(typeID)) {
				Type type = typeFactory.createActorType(typeID, description, attributes, attributesURI);
				typeFactory.insert(type);
				logger.log(Level.FINEST, "Actor type {0} registered", typeID);
			}
		} catch (DuplicateKeyException dke) {
			logger.log(Level.FINEST, "Not registering: actor type \"{0}\" already exists", typeID);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Cannot register actor type \"{0}\": {1}", new Object[] {typeID,
					LogUtil.stackTrace(e)});
			success = false;
		}

		return success;

	}

	/**
	 * Convenience method to de-register an actor type.
	 * 
	 * @param typeID
	 *            the ID of the type. The type ID must be unique.
	 */
	public void deregisterActorType(String typeID) {

		TypeFactory typeFactory = FabricRegistry.getTypeFactory(true);
		Type actorType = typeFactory.getActorType(typeID);
		typeFactory.delete(actorType);

	}

	/**
	 * Convenience method to register a new platform type.
	 * 
	 * @param typeID
	 *            the ID of the type. The type ID must be unique.
	 * 
	 * @param description
	 *            the plain text description of the type. May be <code>null</code>.
	 * 
	 * @param attributes
	 *            the attributes of the type (plain or structured text, e.g. XML, CSV, name/value pairs etc.). May be
	 *            <code>null</code>.
	 * 
	 * @param attributesURI
	 *            the URI of the attributes of the type. May be <code>null</code>.
	 * 
	 * @return <code>true</code> if the new platform type was registered, <code>false</code> otherwise.
	 */
	public boolean registerPlatformType(String typeID, String description, String attributes, String attributesURI) {

		boolean success = true;

		try {

			TypeFactory typeFactory = FabricRegistry.getTypeFactory(true);

			if (null == typeFactory.getPlatformType(typeID)) {
				Type type = typeFactory.createPlatformType(typeID, description, attributes, attributesURI);
				typeFactory.insert(type);
				logger.log(Level.FINEST, "Platform type \"{0}\" registered", typeID);
			}

		} catch (DuplicateKeyException dke) {
			logger.log(Level.FINEST, "Not registering: platform type \"{0}\" already exists", typeID);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Cannot register platform type \"{0}\": {1}", new Object[] {typeID,
					LogUtil.stackTrace(e)});
			success = false;
		}

		return success;

	}

	/**
	 * Convenience method to de-register a platform type.
	 * 
	 * @param typeID
	 *            the ID of the type. The type ID must be unique.
	 */
	public void deregisterPlatformType(String typeID) {

		TypeFactory typeFactory = FabricRegistry.getTypeFactory(true);
		Type platformType = typeFactory.getPlatformType(typeID);
		typeFactory.delete(platformType);

	}

	/**
	 * Convenience method to register a new actor.
	 * <p>
	 * <strong>Note:</strong> If comprehensive exception handling is required (this method will return a simple
	 * success/failure flag) then the Fabric Registry APIs should be used directly.
	 * </p>
	 * 
	 * @param actorID
	 *            the ID of the actor. The actor ID must be unique.
	 * 
	 * @param typeID
	 *            the actor type; this must be a pre-registered type.
	 * 
	 * @return <code>true</code> if the new actor was registered, <code>false</code> otherwise.
	 */
	public boolean registerActor(String actorID, String typeID) {

		boolean success = true;

		try {

			ActorFactory actorFactory = FabricRegistry.getActorFactory(true);

			/* check if the actor already exists - if so, don't overwrite */
			Actor actor = actorFactory.getActorById(actorID);

			if (actor == null) {
				actor = actorFactory.createActor(actorID, typeID);
				actorFactory.insert(actor);
				logger.log(Level.FINEST, "Actor {0} registered", actorID);
			}

		} catch (DuplicateKeyException dke) {
			logger.log(Level.FINEST, "Not registering: actor {0} already exists", actorID);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Cannot register actor {0} with type {1}: {2}", new Object[] {actorID, typeID,
					LogUtil.stackTrace(e)});
			success = false;
		}

		return success;

	}

	/**
	 * Convenience method to de-register an actor.
	 * 
	 * @param actorID
	 *            the ID of the actor.
	 */
	public void deregisterActor(String actorID) {

		ActorFactory actorFactory = FabricRegistry.getActorFactory(true);
		Actor actor = actorFactory.getActorById(actorID);
		actorFactory.delete(actor);

	}

	/**
	 * Convenience method to register a new platform.
	 * <p>
	 * <strong>Note:</strong> If comprehensive exception handling is required (this method will return a simple
	 * success/failure flag) then the Fabric Registry APIs should be used directly.
	 * </p>
	 * 
	 * @param platformID
	 *            the ID of the platform. The platform ID must be unique.
	 * 
	 * @param typeID
	 *            the platform type; this must be a pre-registered type.
	 * 
	 * @return <code>true</code> if the new platform was registered, <code>false</code> otherwise.
	 */
	public boolean registerPlatform(String platformID, String typeID) {

		boolean success = true;

		try {

			PlatformFactory platformFactory = FabricRegistry.getPlatformFactory(true);

			if (null == platformFactory.getPlatformById(platformID)) {
				Platform platform = platformFactory.createPlatform(platformID, typeID, homeNode());
				platformFactory.insert(platform);
				logger.log(Level.FINEST, "Platform {0} registered", platformID);
			}

		} catch (DuplicateKeyException dke) {
			logger.log(Level.FINEST, "Not registering: platform {0} already exists", platformID);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Cannot register platform {0} with type {1}: {2}", new Object[] {platformID,
					typeID, LogUtil.stackTrace(e)});
			success = false;
		}

		return success;

	}

	/**
	 * Convenience method to de-register a platform.
	 * 
	 * @param platformID
	 *            the ID of the platform.
	 */
	public void deregisterPlatform(String platformID) {

		PlatformFactory platformFactory = FabricRegistry.getPlatformFactory(true);
		Platform platform = platformFactory.getPlatformById(platformID);
		platformFactory.delete(platform);

	}

	/**
	 * Connects to the Fabric ready to subscribe to data feeds and send/receive service messages.
	 * 
	 * @throws Exception
	 *             thrown if there is a problem connecting to the Fabric. See the exception detail for more information.
	 */
	public void connect() throws Exception {

		synchronized (ioChannels) {

			/* If we are not already connected to the Fabric using this instance... */
			if (!connectedToFabric) {

				/* Establish a connection to the Fabric Registry database */
				initRegistry();

				/* Access the full Fabric configuration for this node */
				initNodeConfig();

				/* Instantiate the service dispatcher */
				serviceDispatcher = new ClientServiceDispatcher();
				serviceDispatcher.setFabricClient(this);
				serviceDispatcher.setIOChannels(ioChannels);

				/* Load pre-defined services */
				loadServices();

				/* Configure the Fabric connection/disconnection messages for this client */

				IConnectionMessage connectionMessage = new ConnectionMessage(IConnectionMessage.EVENT_CONNECTED,
						homeNode(), platform, actor);
				connectionMessage.setNotification(true);
				registerNotificationHandler(connectionMessage.getCorrelationID(), connectionCallback);

				IConnectionMessage disconnectionMessage = new ConnectionMessage(IConnectionMessage.EVENT_DISCONNECTED,
						homeNode(), platform, actor);

				initConnectionMessage(connectionMessage, disconnectionMessage);

				logger.log(Level.FINE, "Connecting Client {0} via {1} on {2}", new Object[] {actor, platform,
						homeNode()});

				connectFabric();

				/* Listen for incoming Fabric messages */
				openHomeNodeChannels();

				// Set Registry IOChannels if not already set
				if (FabricRegistry.homeNodeEndPoint == null) {
					FabricRegistry.homeNodeEndPoint = homeNodeEndPoint();
				}

				connectedToFabric = true;

				logger.log(Level.FINE, "Fabric actor {0} connected via node {1}, on platform {2}", new Object[] {actor,
						homeNode(), platform});

			}
		}
	}

	/**
	 * Disconnects from the Fabric.
	 */
	public void close() {

		/* Close all active subscriptions */
		unsubscribeAll();

		/* Disconnect from the Fabric */
		disconnectFabric();

		/* De-register shutdown hook actions */
		shutdownHook.removeAction(this);

	}

	/**
	 * Opens the required channels to the local broker:
	 * <ul>
	 * <li>The channel for Fabric service messages (subscribe, unsubscribe, etc.).</li>
	 * <li>The channel for incoming Fabric service messages.</li>
	 * <li>The channel for incoming Fabric feed messages.</li>
	 * </ul>
	 * 
	 * @throws Exception
	 */
	protected void openHomeNodeChannels() throws Exception {

		try {

			/*
			 * The topic on which this client will send commands to the Fabric (via the local node's Fabric Manager)
			 */
			ioChannels.sendCommands = new OutputTopic(config(ConfigProperties.TOPIC_SEND_SESSION_COMMANDS,
					ConfigProperties.TOPIC_SEND_SESSION_COMMANDS_DEFAULT, homeNode()));
			ioChannels.sendCommandsChannel = homeNodeEndPoint().openOutputChannel(ioChannels.sendCommands);
			logger.log(Level.FINER, "Sending Fabric Manager commands to {0}", ioChannels.sendCommands);

			/*
			 * The topic on which this client will receive commands from the Fabric (via the local node's Fabric
			 * Manager)
			 */
			ioChannels.receiveCommands = new InputTopic(config(ConfigProperties.TOPIC_RECEIVE_SESSION_COMMANDS,
					ConfigProperties.TOPIC_RECEIVE_SESSION_COMMANDS_DEFAULT, homeNode(), actor, platform));
			ioChannels.receiveCommandsChannel = homeNodeEndPoint().openInputChannel(ioChannels.receiveCommands, this);
			logger.log(Level.FINER, "Receiving Fabric commands from {0}", ioChannels.receiveCommands);

			/*
			 * The topic on which the Fabric Manager will listen for connection/disconnection (i.e. last will and
			 * testament) messages
			 */
			ioChannels.connectionComands = new InputTopic(config(ConfigProperties.TOPIC_RECEIVE_SESSION_TOPOLOGY,
					ConfigProperties.TOPIC_RECEIVE_SESSION_TOPOLOGY_DEFAULT, homeNode()));
			ioChannels.connectionCommandsChannel = homeNodeEndPoint().openInputChannel(ioChannels.connectionComands,
					this);
			logger.log(Level.FINER, "Listening for last will and testament messages on {0}",
					ioChannels.connectionComands);

		} catch (Exception e) {

			logger.log(Level.SEVERE, "Connection to home node channels failed: ", e);
			throw e;

		}
	}

	/**
	 * Answers the current connection status.
	 * 
	 * @return <code>true</code> if connected to the Fabric, <code>false</code> otherwise.
	 */
	public boolean isConnectedToFabric() {

		return connectedToFabric;

	}

	/**
	 * Answers with a data structure containing the channels and topics used to connect to the Fabric.
	 * 
	 * @return the I/O channels and topics used to connect to the Fabric.
	 */
	public BusIOChannels getIOChannels() {

		return ioChannels;

	}

	/**
	 * @see fabric.core.io.ICallback#cancelCallback(java.lang.Object)
	 */
	@Override
	public void cancelCallback(Object arg1) {

		/* Nothing to do */

	}

	/**
	 * @see fabric.core.io.ICallback#handleMessage(fabric.core.io.Message)
	 */
	@Override
	public void handleMessage(Message message) {

		logger.log(Level.FINEST, "Handling message: {0}", message.toString());

		String messageTopic = (String) message.topic;
		byte[] messageData = message.data;
		String messageString = new String(messageData);

		try {

			IFabricMessage parsedMessage = null;

			try {

				/* Parse the message */
				parsedMessage = FabricMessageFactory.create(messageTopic, messageData);

			} catch (Exception e) {

				logger.log(Level.WARNING, "Improperly formatted message received on topic {0}: {1}", new Object[] {
						messageTopic, messageString});

			}

			/* If this is a Fabric service message... */
			if (parsedMessage instanceof IServiceMessage) {

				/* Handle it by the appropriate service */
				serviceDispatcher.dispatch((IServiceMessage) parsedMessage, null, null);

			} else {

				logger.log(Level.FINE, "Ignoring unexpected message on topic {0}: {1}", new Object[] {messageTopic,
						messageString});

			}

		} catch (Exception e) {

			logger.log(Level.WARNING, "Excpetion handling message received on topic {0}:\n{1}\n{2}", new Object[] {
					messageTopic, messageString, LogUtil.stackTrace(e)});

		}
	}

	/**
	 * @see fabric.core.io.ICallback#startCallback(java.lang.Object)
	 */
	@Override
	public void startCallback(Object arg1) {

		/* Nothing to do */

	}

	/**
	 * Loads the pre-defined services implementing standard Fabric functionality.
	 */
	protected void loadServices() {

		/* Client notification service */
		notificationService = (ClientNotificationService) serviceDispatcher.registerService(
				ClientNotificationService.class.getName(), null, Fabric.FABRIC_PLUGIN_FAMILY, null);

	}

	/**
	 * @see fabric.client.services.IClientNotificationServices#registerNotificationHandler(java.lang.String,
	 *      fabric.client.services.IClientNotificationHandler)
	 */
	@Override
	public IClientNotificationHandler registerNotificationHandler(String correlationID,
			IClientNotificationHandler handler) {

		return notificationService.registerNotificationHandler(correlationID, handler);

	}

	/**
	 * @see fabric.client.services.IClientNotificationServices#deregisterNotificationHandler(java.lang.String)
	 */
	@Override
	public IClientNotificationHandler deregisterNotificationHandler(String correlationID) {

		return notificationService.deregisterNotificationHandler(correlationID);

	}

	/**
	 * @see fabric.client.IFabricClientServices#registerHomeNodeConnectivityCallback(fabric.client.services.IHomeNodeConnectivityCallback
	 *      )
	 */
	@Override
	public IHomeNodeConnectivityCallback registerHomeNodeConnectivityCallback(IHomeNodeConnectivityCallback callback) {

		IHomeNodeConnectivityCallback oldCallback = homeNodeConnectivityCallback;
		homeNodeConnectivityCallback = callback;
		return oldCallback;

	}

	/**
	 * @see fabric.client.services.IHomeNodeConnectivityCallback#homeNodeConnectivity(fabric.bus.messages.IServiceMessage)
	 */
	@Override
	public void homeNodeConnectivity(IServiceMessage message) {

		if (homeNodeConnectivityCallback != null) {

			try {

				homeNodeConnectivityCallback.homeNodeConnectivity(message);

			} catch (Exception e) {

				logger.log(Level.WARNING, "Exception in client home node connectivity callback: ", e);

			}
		}
	}

	public String getActor() {

		return actor;
	}

	public void setActor(String actor) {

		this.actor = actor;
	}

	public String getPlatform() {

		return platform;
	}

	public void setPlatform(String platform) {

		this.platform = platform;
	}

	public static HashMap<String, ISubscription> getActiveSubscriptions() {

		return activeSubscriptions;
	}

	public static void setActiveSubscriptions(HashMap<String, ISubscription> activeSubscriptions) {

		FabricClient.activeSubscriptions = activeSubscriptions;
	}

	public void setConnectedToFabric(boolean connectedToFabric) {

		this.connectedToFabric = connectedToFabric;
	}

	public IClientServiceDispatcher getServiceDispatcher() {

		return serviceDispatcher;
	}

	public void setServiceDispatcher(IClientServiceDispatcher serviceDispatcher) {

		this.serviceDispatcher = serviceDispatcher;
	}

	public ClientNotificationService getNotificationService() {

		return notificationService;
	}

	public void setNotificationService(ClientNotificationService notificationService) {

		this.notificationService = notificationService;
	}

}
