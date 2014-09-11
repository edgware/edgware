/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2007, 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.feeds;

import fabric.bus.messages.IFeedMessage;
import fabric.bus.messages.IServiceMessage;

/**
 * Interface for a callback invoked to handle the receipt of a Fabric feed message.
 */
public interface ISubscriptionCallback {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2009";

	/*
	 * Class methods
	 */

	/**
	 * Invoked when this callback is being initialized.
	 */
	public void startSubscriptionCallback();

	/**
	 * Invoked to deliver a Fabric feed message.
	 * 
	 * @param message
	 *            the feed message.
	 */
	public void handleSubscriptionMessage(IFeedMessage message);

	/**
	 * Invoked when an event occurs for a subscription.
	 * 
	 * @param subscription
	 *            the subscription the event occurred against
	 * @param event
	 *            the event type
	 * @param message
	 *            the event message.
	 * 
	 */
	public void handleSubscriptionEvent(ISubscription subscription, int event, IServiceMessage message);

	/**
	 * Invoked when this callback is being canceled.
	 */
	public void cancelSubscriptionCallback();
}
