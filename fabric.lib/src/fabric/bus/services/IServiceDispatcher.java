/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.services;

import fabric.bus.messages.IClientNotificationMessage;
import fabric.bus.messages.INotificationMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.plugins.IDispatcher;

/**
 * Interface for Fabric service dispatchers.
 */
public interface IServiceDispatcher extends IDispatcher {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/*
	 * Interface methods
	 */

	/**
	 * Pre-registers a new service with this dispatcher.
	 * <p>
	 * Pre-registration allows the caller to instantiate and configure a service before its first use, or where the
	 * called needs direct access to the service instance. Generally this step is not required as the dispatch method is
	 * responsible for loading services if required.
	 * </p>
	 * 
	 * @param name
	 *            the name of the service to register.
	 * 
	 * @param arguments
	 *            the arguments to the service.
	 * 
	 * @param familyName
	 *            this service's family name.
	 * 
	 * @param description
	 *            the service description.
	 * 
	 * @return the new service instance.
	 */
	public IService registerService(String name, String arguments, String familyName, String description);

	/**
	 * Answers a fully instantiated service handler class.
	 * 
	 * @param name
	 *            the name of the handler class.
	 * 
	 * @param arguments
	 *            the arguments to the service.
	 * 
	 * @param familyName
	 *            this service's family name.
	 * 
	 * @param description
	 *            the service description.
	 * 
	 * @return the instance, or <code>null</code> if the instantiation failed.
	 */
	public IService serviceInstance(String name, String arguments, String familyName, String description);

	/**
	 * Load (if necessary) and invoke the services for a request message.
	 * 
	 * @param request
	 *            the service request message to handle.
	 * 
	 * @param response
	 *            a default Fabric notification message that can be modified by the service, or <code>null</code> if
	 *            there is none. This is the response message sent back node to node across the Fabric as the request is
	 *            processed.
	 * 
	 * @param clientResponses
	 *            an array of messages to be delivered to the caller that invoked the service upon receipt of a
	 *            corresponding service notification message(s), or <code>null</code> if there are none.
	 * 
	 * @return the message after processing by each of the services.
	 * 
	 * @throws Exception
	 */
	public IServiceMessage dispatch(IServiceMessage request, INotificationMessage response,
			IClientNotificationMessage[] clientResponses) throws Exception;

	/**
	 * Closes this dispatcher, stopping all of the registered services.
	 */
	@Override
	public void stopDispatcher();

}
