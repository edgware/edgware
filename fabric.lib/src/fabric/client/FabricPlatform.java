/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.client;

import java.util.logging.Level;

import fabric.Fabric;
import fabric.client.services.IPlatformNotificationHandler;
import fabric.client.services.IPlatformNotificationServices;
import fabric.client.services.PlatformNotificationService;
import fabric.core.io.InputTopic;
import fabric.core.properties.ConfigProperties;

/**
 * Class managing a platform's connection to the Fabric.
 * <p>
 * This class is used by Fabric platforms to establish a connection to the Fabric in order to:
 * <ul>
 * <li>Set up subscriptions.</li>
 * <li>Send control messages to the Fabric and connected assets.</li>
 * <li>Receive and handle service messages via the Fabric.</li>
 * <li>Register and de-register new platform types and platforms, actor types and actors.</li>
 * </ul>
 * This class provides the container within which platform service messages are handled. Such services are entirely
 * application defined, and provide a Fabric-based mechanism to communicate with and control Fabric platforms.
 * </p>
 */
public class FabricPlatform extends FabricClient implements IPlatformNotificationServices {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

	/*
	 * Class fields
	 */

	/** The platform notification service */
	private PlatformNotificationService platformNotificationService = null;

	/** The ID of the service associated with the platform, i.e. where a platform and service are the same process. */
	protected String service = null;

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
	 * @throws Exception
	 */
	public FabricPlatform(String actor, String platform) throws Exception {

		super(actor, platform);

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
	 * @param the
	 *            ID of the service associated with the platform, i.e. where a platform and service are the same
	 *            process.
	 * @throws Exception
	 */
	public FabricPlatform(String actor, String platform, String service) throws Exception {

		super(actor, platform);

		this.service = service;

	}

	/**
	 * @see fabric.client.FabricClient#openHomeNodeChannels()
	 */
	@Override
	protected void openHomeNodeChannels() throws Exception {

		super.openHomeNodeChannels();

		try {

			/*
			 * The topic on which this client will receive platform commands from the Fabric (via the local node's
			 * Fabric Manager) for its associated platform
			 */
			ioChannels.receivePlatformCommands = new InputTopic(config(
					ConfigProperties.TOPIC_RECEIVE_PLATFORM_COMMANDS,
					ConfigProperties.TOPIC_RECEIVE_PLATFORM_COMMANDS_DEFAULT, homeNode(), platform));
			ioChannels.receivePlatformCommandsChannel = homeNodeEndPoint().openInputChannel(
					ioChannels.receivePlatformCommands, this);
			logger.log(Level.FINE, "Receiving Fabric platform commands from \"{0}\"",
					ioChannels.receivePlatformCommands);

			/* If a service has been specified... */
			if (service != null) {

				/*
				 * The topic on which this client will receive platform commands from the Fabric (via the local node's
				 * Fabric Manager) for its associated platform
				 */
				ioChannels.receiveServiceCommands = new InputTopic(config(
						ConfigProperties.TOPIC_RECEIVE_SERVICES_COMMANDS,
						ConfigProperties.TOPIC_RECEIVE_SERVICES_COMMANDS_DEFAULT, homeNode(), platform, service));
				ioChannels.receiveServiceCommandsChannel = homeNodeEndPoint().openInputChannel(
						ioChannels.receiveServiceCommands, this);
				logger.log(Level.FINE, "Receiving Fabric service commands from \"{0}\"",
						ioChannels.receiveServiceCommands);

			}

		} catch (Exception e) {

			logger.log(Level.WARNING, "Connection to home node channels failed: ", e);
			throw e;

		}
	}

	/**
	 * @see fabric.client.FabricClient#loadServices()
	 */
	@Override
	protected void loadServices() {

		super.loadServices();

		/* Platform notification service */
		platformNotificationService = (PlatformNotificationService) serviceDispatcher.registerService(
				PlatformNotificationService.class.getName(), null, Fabric.FABRIC_PLUGIN_FAMILY, null);

	}

	/**
	 * @see fabric.client.services.IPlatformNotificationServices#registerPlatformNotificationHandler(java.lang.String,
	 *      fabric.client.services.IPlatformNotificationHandler)
	 */
	@Override
	public IPlatformNotificationHandler registerPlatformNotificationHandler(String platform,
			IPlatformNotificationHandler handler) {

		return platformNotificationService.registerPlatformNotificationHandler(platform, handler);

	}

	/**
	 * @see fabric.client.services.IPlatformNotificationServices#deregisterPlatformNotificationHandler(java.lang.String)
	 */
	@Override
	public IPlatformNotificationHandler deregisterPlatformNotificationHandler(String platform) {

		return platformNotificationService.deregisterPlatformNotificationHandler(platform);

	}

	/**
	 * @see fabric.client.services.IPlatformNotificationServices#registerServiceNotificationHandler(java.lang.String,
	 *      java.lang.String, fabric.client.services.IPlatformNotificationHandler)
	 */
	@Override
	public IPlatformNotificationHandler registerServiceNotificationHandler(String platform, String service,
			IPlatformNotificationHandler handler) {

		return platformNotificationService.registerServiceNotificationHandler(platform, service, handler);

	}

	/**
	 * @see fabric.client.services.IPlatformNotificationServices#deregisterServiceNotificationHandler(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public IPlatformNotificationHandler deregisterServiceNotificationHandler(String platform, String service) {

		return platformNotificationService.deregisterServiceNotificationHandler(platform, service);

	}

	/**
	 * Answers the ID of the service associated with this connection.
	 * 
	 * @return the service ID
	 */
	public String service() {

		return service;

	}
}
