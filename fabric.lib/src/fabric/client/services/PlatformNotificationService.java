/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2010, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.client.services;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.Fabric;
import fabric.SystemDescriptor;
import fabric.bus.BusIOChannels;
import fabric.bus.messages.IClientNotificationMessage;
import fabric.bus.messages.INotificationMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.messages.impl.PlatformNotificationMessage;
import fabric.bus.plugins.IPluginConfig;
import fabric.bus.services.IClientService;
import fabric.bus.services.IPersistentService;
import fabric.client.FabricClient;

/**
 * Handles notification messages sent from the Fabric to a platform.
 */
public class PlatformNotificationService extends Fabric implements IClientService, IPersistentService,
		IPlatformNotificationServices {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010, 2014";

	/*
	 * Class fields
	 */

	/** The service configuration. */
	private IPluginConfig config = null;

	/** The Fabric Client associated with this service. */
	private FabricClient fabricClient = null;

	/** Channels used for Fabric I/O */
	private BusIOChannels ioChannels = null;

	/** The notification handlers registered for each platform ID */
	private final HashMap<String, IPlatformNotificationHandler> platformNotificationHandlers = new HashMap<String, IPlatformNotificationHandler>();

	/** The notification handlers registered for each platform ID */
	private final HashMap<SystemDescriptor, IPlatformNotificationHandler> serviceNotificationHandlers = new HashMap<SystemDescriptor, IPlatformNotificationHandler>();

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 */
	public PlatformNotificationService() {

		super(Logger.getLogger("fabric.client.services"));

	}

	/**
	 * @see fabric.bus.services.IService#serviceConfig()
	 */
	@Override
	public IPluginConfig serviceConfig() {

		return config;

	}

	/**
	 * @see fabric.bus.services.IClientService#setFabricClient(fabric.client.FabricClient)
	 */
	@Override
	@Deprecated
	public void setFabricClient(FabricClient fabricClient) {

		this.fabricClient = fabricClient;

	}

	/**
	 * @see fabric.bus.services.IClientService#setIOChannels(fabric.bus.BusIOChannels)
	 */
	@Override
	public void setIOChannels(BusIOChannels ioChannels) {

		this.ioChannels = ioChannels;

	}

	/**
	 * @see fabric.bus.services.IService#initService(fabric.bus.plugins.IPluginConfig)
	 */
	@Override
	public void initService(IPluginConfig config) {

		this.config = config;

	}

	/**
	 * @see fabric.client.services.IPlatformNotificationServices#registerPlatformNotificationHandler(java.lang.String,
	 *      fabric.client.services.IPlatformNotificationHandler)
	 */
	@Override
	public IPlatformNotificationHandler registerPlatformNotificationHandler(String platform,
			IPlatformNotificationHandler handler) {

		logger.log(Level.FINEST, "Registering notification handler for platform \"{0}\": {1}", new Object[] {platform,
				"" + handler});

		IPlatformNotificationHandler oldHandler = platformNotificationHandlers.put(platform, handler);
		return oldHandler;

	}

	/**
	 * @see fabric.client.services.IPlatformNotificationServices#deregisterPlatformNotificationHandler(java.lang.String)
	 */
	@Override
	public IPlatformNotificationHandler deregisterPlatformNotificationHandler(String platform) {

		logger.log(Level.FINEST, "De-registering notification handler for platform \"{0}\"", platform);

		IPlatformNotificationHandler oldHandler = platformNotificationHandlers.remove(platform);
		return oldHandler;

	}

	/**
	 * @see fabric.client.services.IPlatformNotificationServices#registerServiceNotificationHandler(java.lang.String,
	 *      java.lang.String, fabric.client.services.IPlatformNotificationHandler)
	 */
	@Override
	public IPlatformNotificationHandler registerServiceNotificationHandler(String platform, String service,
			IPlatformNotificationHandler handler) {

		SystemDescriptor systemDescriptor = new SystemDescriptor(platform, service);
		IPlatformNotificationHandler oldHandler = serviceNotificationHandlers.put(systemDescriptor, handler);
		return oldHandler;

	}

	/**
	 * @see fabric.client.services.IPlatformNotificationServices#deregisterServiceNotificationHandler(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public IPlatformNotificationHandler deregisterServiceNotificationHandler(String platform, String service) {

		SystemDescriptor systemDescriptor = new SystemDescriptor(platform, service);
		IPlatformNotificationHandler oldHandler = serviceNotificationHandlers.remove(systemDescriptor);
		return oldHandler;

	}

	/**
	 * @see fabric.bus.services.IService#handleServiceMessage(fabric.bus.messages.IServiceMessage,INotificationMessage,
	 *      IClientNotificationMessage[])
	 */
	@Override
	public IServiceMessage handleServiceMessage(IServiceMessage message, INotificationMessage response,
			IClientNotificationMessage[] clientResponses) throws Exception {

		PlatformNotificationMessage notificationMessage = (PlatformNotificationMessage) message;

		/* Get the registered platform notification handler */
		IPlatformNotificationHandler notificationHandler = platformNotificationHandlers.get(notificationMessage
				.getPlatform());

		/* If there is a notification handler... */
		if (notificationHandler != null) {

			/* Invoke it */
			notificationHandler.handlePlatformNotification(notificationMessage);

		} else {

			logger.log(Level.FINE, "No handler registered for message for platform \"{0}\": {1}", new Object[] {
					message.getCorrelationID(), notificationMessage.toString()});

		}

		return message;

	}

	/**
	 * @see fabric.bus.services.IPersistentService#stopService()
	 */
	@Override
	public void stopService() {

		logger.log(Level.FINE, "Service stopped: {0}", getClass().getName());
	}
}
