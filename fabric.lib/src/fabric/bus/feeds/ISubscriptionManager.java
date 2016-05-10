/*
 * (C) Copyright IBM Corp. 2007, 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.feeds;

import fabric.bus.messages.IFeedMessage;
import fabric.bus.services.IBusService;

/**
 * Interface for services that handle Fabric feed subscriptions, including:
 * <ul>
 * <li>Subscribe and unsubscribe commands</li>
 * <li>Handling Fabric data feeds (plug-in application, routing, delivery to users, etc.)</li>
 * </ul>
 */
public interface ISubscriptionManager extends IBusService {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2009";

	/*
	 * Interface methods
	 */

	/**
	 * Handles a Fabric message, invoking any plug-ins associated with it, and routing it to the next node or local
	 * subscriber.
	 * <p>
	 * Optionally Fabric messages can be associated with one or more specific tasks/clients. Where this is the case the
	 * method will restrict its handling to those listed.
	 * </p>
	 * 
	 * @param nodeMessage
	 *            the message.
	 * 
	 * @throws Exception
	 *             thrown if an error is encountered whilst handling a feed message. See the exception detail for more
	 *             information.
	 */
	public void handleFeed(IFeedMessage nodeMessage) throws Exception;

}