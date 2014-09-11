/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.services.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.bus.messages.IClientNotificationMessage;
import fabric.bus.messages.INotificationMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.messages.impl.CachableMessage;
import fabric.bus.services.IPersistentService;
import fabric.core.logging.LogUtil;
import fabric.registry.CachedMessage;
import fabric.registry.CachedMessageFactory;
import fabric.registry.FabricRegistry;

/**
 * This service will cause any messages it receives to be stored in the registry.
 */
public class MessageCacheService extends BusService implements IPersistentService {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 */
	public MessageCacheService() {

		super(Logger.getLogger("fabric.bus.services"));

	}

	/**
	 * Constructs a new instance.
	 */
	public MessageCacheService(Logger logger) {

		super(logger);

	}

	/**
	 * @see fabric.bus.services.IService#handleServiceMessage(fabric.bus.messages.IServiceMessage,
	 *      fabric.bus.messages.INotificationMessage, fabric.bus.messages.IClientNotificationMessage[])
	 */
	@Override
	public IServiceMessage handleServiceMessage(IServiceMessage message, INotificationMessage response,
			IClientNotificationMessage[] clientResponses) throws Exception {

		/* Only store messages of the correct type */
		if (message instanceof CachableMessage) {
			CachableMessage dm = (CachableMessage) message;

			CachedMessageFactory cmf = FabricRegistry.getCachedMessageFactory();
			CachedMessage cm = cmf.createCachedMessage(System.currentTimeMillis(), dm.getSource(), dm.getDestination(),
					dm.getMessage());
			try {
				FabricRegistry.save(cm);
				logger.log(Level.FINEST, "Saved message {0} to cache", message.getUID());
			} catch (Exception e) {
				logger.log(Level.WARNING, "Failed to save cachable message \"{0}\": {1}", new Object[] {
						message.getUID(), LogUtil.stackTrace(e)});
			}
		}

		return message;
	}

	@Override
	public void stopService() {

		logger.log(Level.FINE, "Service stopped: {0}", getClass().getName());
	}

}
