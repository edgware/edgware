/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2010, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.systems;

import fabric.ServiceDescriptor;
import fabric.SystemDescriptor;
import fabric.bus.feeds.ISubscriptionCallback;
import fabric.bus.messages.IFeedMessage;

/**
 * Interfaces for classes implementing the body of a Fabric system.
 */
public interface ISystem extends ISubscriptionCallback {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010, 2014";

	/*
	 * Class constants
	 */

	/**
	 * Constants representing meta properties in the feed messages that will be delivered to this component service
	 * instance.
	 */
	public static final String META_INPUT_FEED_DESCRIPTOR = "inputFeedDescriptor";

	/*
	 * Interface methods
	 */

	/**
	 * Initializes a new system instance.
	 * <p>
	 * Services should perform all initialization necessary to be ready to start processing. No processing should take
	 * place, however, until the <code>startInstance()</code> method has also been called.
	 * 
	 * @param systemDescriptor
	 *            the service to initialize.
	 * 
	 * @param container
	 *            this instance's container.
	 */
	public void initializeInstance(SystemDescriptor systemDescriptor, SystemRuntime container);

	/**
	 * Starts a new system instance.
	 * <p>
	 * Systems must wait until this method has been called before producing messages.
	 * </p>
	 * 
	 * @param systemDescriptor
	 *            the system to start.
	 * 
	 * @param fabricClient
	 *            this system's connection to the Fabric.
	 */
	public void startInstance(SystemDescriptor systemDescriptor);

	/**
	 * Stops a system instance.
	 * 
	 * @param systemDescriptor
	 *            the system to stop.
	 */
	public void stopInstance(SystemDescriptor systemDescriptor);

	/**
	 * Invoked to deliver a Fabric feed message to one of the system's <em>input</em> feeds.
	 * 
	 * @param feed
	 *            the feed to which this message has been delivered.
	 * 
	 * @param message
	 *            the feed message.
	 */
	public void handleInput(String inputFeedID, IFeedMessage message);

	/**
	 * Invoked to deliver a response message (i.e. the reply to a request/response invocation).
	 * 
	 * @param correlationID
	 *            the correlation ID to the original request.
	 * 
	 * @param feed
	 *            the feed to which this message has been delivered.
	 * 
	 * @param message
	 *            the feed message.
	 */
	public void handleSolicitedResponse(String correlationID, String responseFeedID, IFeedMessage message);

	/**
	 * Invoked to deliver a request message requiring a response (i.e. a request/response invocation).
	 * 
	 * @param correlationID
	 *            the correlation ID of the request.
	 * 
	 * @param feed
	 *            the feed to which this message has been delivered.
	 * 
	 * @param message
	 *            the feed message.
	 * 
	 * @param replyTo
	 *            the feed descriptor to receive the response.
	 */
	public void handleRequestResponse(String correlationID, ServiceDescriptor sendTo, IFeedMessage message,
			ServiceDescriptor replyTo);

	/**
	 * Invoked to deliver a one-way message (i.e. a message that does not require a response).
	 * 
	 * @param feed
	 *            the feed to which this message has been delivered.
	 * 
	 * @param message
	 *            the feed message.
	 */
	public void handleOneWay(String requestResponseFeedID, IFeedMessage message);

}
