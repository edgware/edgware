/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.services.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.bus.messages.IClientNotificationMessage;
import fabric.bus.messages.INotificationMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.plugins.IPluginConfig;
import fabric.bus.plugins.IPluginHandler;
import fabric.bus.plugins.impl.Dispatcher;
import fabric.bus.plugins.impl.PluginConfig;
import fabric.bus.services.IPersistentService;
import fabric.bus.services.IService;
import fabric.bus.services.IServiceDispatcher;
import fabric.core.logging.LogUtil;

/**
 * Base class for Fabric plug-in dispatchers.
 */
public class ServiceDispatcher extends Dispatcher implements IServiceDispatcher {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

	/*
	 * Class fields
	 */

	/** The table of persistent services */
	private final HashMap<String, IService> services = new HashMap<String, IService>();

	/*
	 * Class methods
	 */

	public ServiceDispatcher() {

		super(Logger.getLogger("fabric.bus.services"));
	}

	/**
	 * @see fabric.bus.services.IServiceDispatcher#registerService(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public IService registerService(String name, String arguments, String familyName, String description) {

		IService ep = serviceInstance(name, arguments, familyName, description);
		if (ep != null) {
			logger.log(Level.FINE, "Service \"{0}\" registered in family \"{1}\" with arguments \"{2}\"", new Object[] {
					name, familyName, arguments});
		} else {
			logger.log(Level.WARNING,
					"Registration of Service \"{0}\" in family \"{1}\" with arguments \"{2}\" failed", new Object[] {
							name, familyName, arguments});
		}
		return ep;

	}

	/**
	 * @see fabric.bus.services.IServiceDispatcher#dispatch(fabric.bus.messages.IServiceMessage,
	 *      fabric.bus.messages.INotificationMessage, fabric.bus.messages.IClientNotificationMessage[])
	 */
	@Override
	public IServiceMessage dispatch(IServiceMessage requestIn, INotificationMessage response,
			IClientNotificationMessage[] clientResponses) throws Exception {

		logger.entering(this.getClass().getName(), "dispatch");

		/* To hold the request message to be returned after processing by the service */
		IServiceMessage requestOut = requestIn;

		/* Get the name of the service */
		String name = requestIn.getServiceName();

		/* Get the service family name */
		String familyName = requestIn.getServiceFamilyName();

		logger.log(Level.FINEST, "Handling message for service \"{0}\", family \"{1}\"",
				new Object[] {name, familyName});

		/* Get the active, or a new, instance of the service handler */
		IService service = serviceInstance(name, null, familyName, null);

		/* If we have successfully obtained a handler... */
		if (service != null) {

			try {

				/* Handle the request */
				logger.log(Level.FINE, "Invoking message handler for service \"{0}\"", name);
				requestOut = service.handleServiceMessage(requestIn, response, clientResponses);

			} catch (Exception e) {

				logger.log(Level.WARNING, "Exception handling message in service {0}: {1}", new Object[] {name,
						LogUtil.stackTrace(e)});

			}

		} else {

			logger.log(Level.FINE, "Service handler \"{0}\" not available to: {1}", new Object[] {name,
					this.getClass().getName()});

		}

		logger.exiting(this.getClass().getName(), "dispatch", requestOut);
		return requestOut;

	}

	/**
	 * @see fabric.bus.plugins.IDispatcher#stopDispatcher()
	 */
	@Override
	public void stopDispatcher() {

		/* For each persistent service... */
		for (Iterator<IService> i = services.values().iterator(); i.hasNext();) {

			/* Get the next service */
			IService service = i.next();

			/* Stop it */
			logger.log(Level.FINER, "Stopping service: {0}", service.serviceConfig().getName());
			((IPersistentService) service).stopService();
			logger.log(Level.FINER, "Service stopped: {0}", service.serviceConfig().getName());

		}
	}

	/**
	 * @see fabric.bus.services.IServiceDispatcher#serviceInstance(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public IService serviceInstance(String name, String arguments, String familyName, String description) {

		/* Generate the handler's persistence name */
		String fullName = familyName + '/' + name;

		/* Check if this service has already been instantiated (persistent handlers only) */
		IService service = services.get(fullName);

		/* If there is no active handler... */
		if (service == null) {

			try {
				/* Instantiate a new one */
				service = (IService) instantiate(name);

				/* If this is a persistent handler... */
				if (service instanceof IPersistentService) {

					/* Record it */
					services.put(fullName, service);

				}

				/* Initialize the configuration object */
				IPluginConfig config = initPluginConfig();
				config.setName(name);
				config.setArguments(arguments);
				config.setFamilyName(familyName);
				config.setFamily(family(familyName));
				config.setNode(homeNode());
				config.setDescription(description);
				// config.setMetricManager(null);
				service.initService(config);

			} catch (Throwable t) {

				logger.log(Level.WARNING, "Exception loading service handler class \"{0}\": {1}", new Object[] {name,
						LogUtil.stackTrace(t)});
				service = null;

			}
		}

		return service;
	}

	/**
	 * @see fabric.bus.plugins.impl.Dispatcher#initPluginConfig()
	 */
	@Override
	public IPluginConfig initPluginConfig() {

		IPluginConfig config = new PluginConfig();
		return config;

	}

	/**
	 * @see fabric.bus.plugins.IDispatcher#initPluginHandler(fabric.bus.plugins.IPluginConfig)
	 */
	@Override
	public IPluginHandler initPluginHandler(IPluginConfig config) {

		/* Not required */
		return null;

	}
}
