/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.platformmanager;

import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.bus.IBusServices;
import fabric.bus.messages.IClientNotificationMessage;
import fabric.bus.messages.INotificationMessage;
import fabric.bus.messages.IPlatformNotificationMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.plugins.IPluginConfig;
import fabric.bus.services.IBusServiceConfig;
import fabric.bus.services.IPersistentService;
import fabric.bus.services.IPlatformManager;
import fabric.bus.services.impl.BusService;
import fabric.core.io.OutputTopic;

/**
 * Class handling messages for Fabric platforms.
 * 
 */
public class PlatformManagerService extends BusService implements IPersistentService, IPlatformManager {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

	/*
	 * Class static fields
	 */

	/*
	 * Class fields
	 */

	/** A local copy of the interface to Fabric management functions. */
	private IBusServices busServices = null;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 */
	public PlatformManagerService() {

		super(Logger.getLogger("fabric.bus.services.platformmanager"));

	}

	/**
	 * @see fabric.bus.services.impl.BusService#initService(fabric.bus.plugins.IPluginConfig)
	 */
	@Override
	public void initService(IPluginConfig config) {

		super.initService(config);

		/* Make a local copy of the accessor for Fabric management services */
		busServices = ((IBusServiceConfig) config).getFabricServices();

	}

	/**
	 * @see fabric.bus.services.IService#handleServiceMessage(fabric.bus.messages.IServiceMessage, INotificationMessage,
	 *      IClientNotificationMessage[])
	 */
	@Override
	public IServiceMessage handleServiceMessage(IServiceMessage request, INotificationMessage response,
			IClientNotificationMessage[] clientResponses) throws Exception {

		IPlatformNotificationMessage message = (IPlatformNotificationMessage) request;

		/* Extract the message details */
		String platform = message.getPlatform();

		/* Deliver the message */
		notifyPlatform(platform, message);

		return request;

	}

	/**
	 * @see fabric.bus.services.IPersistentService#stopService()
	 */
	@Override
	public void stopService() {

		logger.log(Level.FINE, "Service stopped: {0}", getClass().getName());

	}

	/**
	 * @see fabric.bus.services.IPlatformManager#notifyPlatform(java.lang.String,
	 *      fabric.bus.messages.IPlatformNotificationMessage)
	 */
	@Override
	public void notifyPlatform(String platform, IPlatformNotificationMessage message) throws Exception {

		OutputTopic notificationTopic = new OutputTopic(config("fabric.commands.platforms", null, homeNode(), platform));

		logger.log(Level.FINEST, "Delivering message to platform \"{0}\" using topic \"{1}\":\n{2}", new Object[] {
				platform, notificationTopic, message.toString()});

		busServices.ioChannels().sendPlatformCommandsChannel.write(message.toWireBytes(), notificationTopic);

	}

	/**
	 * @see fabric.bus.services.IPlatformManager#notifyService(java.lang.String, java.lang.String,
	 *      fabric.bus.messages.IPlatformNotificationMessage)
	 */
	@Override
	public void notifyService(String platform, String service, IPlatformNotificationMessage message) throws Exception {

		OutputTopic notificationTopic = new OutputTopic(config("fabric.commands.services", null, homeNode(), platform,
				service));

		logger.log(Level.FINEST, "Delivering message to service \"{0}/{1}\" using topic \"{2}\":\n{3}", new Object[] {
				platform, service, notificationTopic, message.toString()});

		busServices.ioChannels().sendServiceCommandsChannel.write(message.toWireBytes(), notificationTopic);

	}
}
