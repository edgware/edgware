/*
 * (C) Copyright IBM Corp. 2009, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.services;

import fabric.bus.messages.IClientNotificationMessage;
import fabric.bus.messages.INotificationMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.plugins.IPluginConfig;

/**
 * Interface for classes that implement Fabric services.
 * <p>
 * Classes implementing this interface are short lived, i.e. they are instantiated to handle a single message.
 * </p>
 */
public interface IService {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

	/*
	 * Class constants
	 */

	/** The index of the "success" client notification message in the list passed to invokeService(). */
	public static final int NOTIFICATION_SUCCESS = 0;

	/** The index of the "failure" client notification message in the list passed to invokeService(). */
	public static final int NOTIFICATION_FAILURE = 1;

	/** The index of the "timeout" client notification message in the list passed to invokeService(). */
	public static final int NOTIFICATION_TIMEOUT = 2;

	/** The index of the "handled en route" client notification message in the list passed to invokeService(). */
	public static final int NOTIFICATION_SUCCESS_EN_ROUTE = 3;

	/*
	 * Interface methods
	 */

	/**
	 * Initializes this service instance.
	 * 
	 * @param config
	 *            the plug-in's configuration information.
	 */
	public void initService(IPluginConfig config);

	/**
	 * Answers the configuration for this service.
	 * 
	 * @return the configuration settings for this service.
	 */
	public IPluginConfig serviceConfig();

	/**
	 * Invokes the service to process a message.
	 * <p>
	 * Services can provide notifications back to the caller indicating the result of the service invocation. There are
	 * two types of notification message that are passed to this method to customize:
	 * <ol>
	 * <li>The <code>INotificationMessage</code> that will be sent back across the Fabric from <em>this</em> invocation
	 * of the service to the originating node (remembering that service invocation may be actioned on each node between
	 * the caller and the target node). The service can set the message's properties and payload to indicate the result
	 * of the service invocation.</li>
	 * <li>A set of three <code>IClientNotificationMessage</code> messages, one of which will be delivered to the caller
	 * to convey the result of the service invocation. The result (one of success, failure, or timeout) will determine
	 * which of the messages is delivered.</li>
	 * </ol>
	 * <code>INotificationMessage</code>s are not delivered directly to the client. When they are received on the
	 * originating node of the service invocation they trigger the delivery of a set of pre-registered messages that
	 * have both a matching correlation ID and a matching <em>event</em> code.
	 * </p>
	 * <p>
	 * The Fabric provides three such messages which can be customized to convey the status of the service invocation.
	 * (Note that these messages are only created on the node that originated the call, i.e. the node to which the
	 * caller is connected.)
	 * </p>
	 * <p>
	 * Upon receipt of an <code>INotificationMessage</code> on the originating node, with a status of success or
	 * failure, the corresponding <code>IClientNotificationMessage</code> message is delivered to the caller. Before
	 * delivery the properties, payload and feed list of the <code>INotificationMessage</code> are recorded in the
	 * <code>IClientNotificationMessage</code> (in a separate part of the message to the
	 * <code>IClientNotificationMessage</code>'s own properties and payload). The third
	 * <code>IClientNotificationMessage</code> message is delivered if no <code>IClientNotificationMessage</code> is
	 * received before a timeout period expires. This timeout period is set by the caller in the original service
	 * invocation.
	 * </p>
	 * 
	 * @param request
	 *            the message to be handled.
	 * 
	 * @param response
	 *            a default Fabric notification message that can be modified by the service, or <code>null</code> if
	 *            there is none. This is the response message sent back node to node across the Fabric as the request is
	 *            processed.
	 * 
	 * @param clientResponses
	 *            an array of messages to be delivered to the caller that invoked the service upon receipt of a
	 *            corresponding service notification message(s), or <code>null</code> if there are none. The messages
	 *            correspond to success (index <code>IService.NOTIFICATION_SUCCESS</code>), failure (index
	 *            <code>IService.NOTIFICATION_FAILURE</code>) and timeout (index
	 *            <code>IService.NOTIFICATION_TIMEOUT</code> ) of the service invocation.
	 * 
	 * @return the service message after processing by the service.
	 * 
	 * @throws Exception
	 *             thrown if the services throws an exception.
	 */
	public IServiceMessage handleServiceMessage(IServiceMessage request, INotificationMessage response,
			IClientNotificationMessage[] clientResponses) throws Exception;

}
