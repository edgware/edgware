/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.services;

import fabric.bus.messages.IServiceMessage;

/**
 * Interface for classes handling connection/disconnection messages for the Fabric.
 * <p>
 * Connection managers can be used to pre-register Fabric service messages that are to be sent upon receipt of a
 * connection/disconnection message from a node, platform, service, feed, or actor.
 * </p>
 */
public interface IConnectionManager {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/*
	 * Interface methods
	 */

	/**
	 * Adds a new service message for connection events for the specified node.
	 * 
	 * @param node
	 *            the name of the node, or <code>null</code> for all nodes.
	 * 
	 * @param message
	 *            the service message to be sent.
	 * 
	 * @param statusType
	 *            the type of status change that will trigger the sending of this message, one of
	 *            <code>EVENT_CONNECTED</code> or <code>EVENT_DISCONNECTED</code>.
	 * 
	 * @param singleFire
	 *            flag indicating if this message is a single or multiple fire.
	 * 
	 * @return a handle identifying the message.
	 */
	public String addNodeMessage(String node, IServiceMessage message, int statusType, boolean singleFire);

	/**
	 * Adds a new service message for connection events for the specified platform.
	 * 
	 * @param node
	 *            the ID of the node, or <code>null</code> for all nodes.
	 * 
	 * @param platform
	 *            the ID of the platform, or <code>null</code> for all platforms.
	 * 
	 * @param message
	 *            the service message to be sent.
	 * 
	 * @param statusType
	 *            the type of status change that will trigger the sending of this message, one of
	 *            <code>EVENT_CONNECTED</code> or <code>EVENT_DISCONNECTED</code>.
	 * 
	 * @param singleFire
	 *            flag indicating if this message is a single or multiple fire.
	 * 
	 * @return a handle identifying the message.
	 */
	public String addPlatformMessage(String node, String platform, IServiceMessage message, int statusType,
			boolean singleFire);

	/**
	 * Adds a new service message for connection events for the specified service.
	 * 
	 * @param node
	 *            the ID of the node, or <code>null</code> for all nodes.
	 * 
	 * @param platform
	 *            the ID of the platform, or <code>null</code> for all platforms.
	 * 
	 * @param service
	 *            the ID of the service, or <code>null</code> for all services.
	 * 
	 * @param message
	 *            the service message to be sent.
	 * 
	 * @param statusType
	 *            the type of status change that will trigger the sending of this message, one of
	 *            <code>EVENT_CONNECTED</code> or <code>EVENT_DISCONNECTED</code>.
	 * 
	 * @param singleFire
	 *            flag indicating if this message is a single or multiple fire.
	 * 
	 * @return a handle identifying the message.
	 */
	public String addServiceMessage(String node, String platform, String service, IServiceMessage message,
			int statusType, boolean singleFire);

	/**
	 * Adds a new service message for connection events for the specified feed.
	 * 
	 * @param node
	 *            the ID of the node, or <code>null</code> for all nodes.
	 * 
	 * @param platform
	 *            the ID of the platform, or <code>null</code> for all platforms.
	 * 
	 * @param service
	 *            the ID of the service, or <code>null</code> for all services.
	 * 
	 * @param feed
	 *            the ID of the feed, or <code>null</code> for all feeds.
	 * 
	 * @param message
	 *            the service message to be sent.
	 * 
	 * @param statusType
	 *            the type of status change that will trigger the sending of this message, one of
	 *            <code>EVENT_CONNECTED</code> or <code>EVENT_DISCONNECTED</code>.
	 * 
	 * @param singleFire
	 *            flag indicating if this message is a single or multiple fire.
	 * 
	 * @return a handle identifying the message.
	 */
	public String addFeedMessage(String node, String platform, String service, String feed, IServiceMessage message,
			int statusType, boolean singleFire);

	/**
	 * Adds a new service message for connection events for the specified actor.
	 * 
	 * @param node
	 *            the ID of the node, or <code>null</code> for all nodes.
	 * 
	 * @param platform
	 *            the ID of the platform, or <code>null</code> for all platforms.
	 * 
	 * @param actor
	 *            the ID of the feed, or <code>null</code> for all actors.
	 * 
	 * @param message
	 *            the service message to be sent.
	 * 
	 * @param statusType
	 *            the type of status change that will trigger the sending of this message, one of
	 *            <code>EVENT_CONNECTED</code> or <code>EVENT_DISCONNECTED</code>.
	 * 
	 * @param singleFire
	 *            flag indicating if this message is a single or multiple fire.
	 * 
	 * @return a handle identifying the message.
	 */
	public String addActorMessage(String node, String platform, String actor, IServiceMessage message, int statusType,
			boolean singleFire);

	/**
	 * Removes the specified message registered for connection events.
	 * <p>
	 * If the message has already been removed then no action is taken (i.e. the call is a no-op).
	 * </p>
	 * 
	 * @param handle
	 *            the handle of the message to be removed.
	 */
	public void removeMessage(String handle);

	/**
	 * Removes service messages registered for connection events for the specified node.
	 * 
	 * @param node
	 *            the name of the node, or <code>null</code> for all nodes.
	 * 
	 * @param statusType
	 *            the type of status change that triggered the sending of this message, one of
	 *            <code>EVENT_CONNECTED</code> or <code>EVENT_DISCONNECTED</code>.
	 */
	public void removeNodeMessage(String node, int statusType);

	/**
	 * Removes service messages registered for connection events for the specified platform.
	 * 
	 * @param node
	 *            the ID of the node, or <code>null</code> for all nodes.
	 * 
	 * @param platform
	 *            the ID of the platform, or <code>null</code> for all platforms.
	 * 
	 * @param statusType
	 *            the type of status change that triggered the sending of this message, one of
	 *            <code>EVENT_CONNECTED</code> or <code>EVENT_DISCONNECTED</code>.
	 */
	public void removePlatformMessage(String node, String platform, int statusType);

	/**
	 * Removes service messages registered for connection events for the specified service.
	 * 
	 * @param node
	 *            the ID of the node, or <code>null</code> for all nodes.
	 * 
	 * @param platform
	 *            the ID of the platform, or <code>null</code> for all platforms.
	 * 
	 * @param service
	 *            the ID of the service, or <code>null</code> for all services.
	 * 
	 * @param statusType
	 *            the type of status change that triggered the sending of this message, one of
	 *            <code>EVENT_CONNECTED</code> or <code>EVENT_DISCONNECTED</code>.
	 */
	public void removeServiceMessage(String node, String platform, String service, int statusType);

	/**
	 * Removes service messages registered for connection events for the specified feed.
	 * 
	 * @param node
	 *            the ID of the node, or <code>null</code> for all nodes.
	 * 
	 * @param platform
	 *            the ID of the platform, or <code>null</code> for all platforms.
	 * 
	 * @param service
	 *            the ID of the service, or <code>null</code> for all services.
	 * 
	 * @param feed
	 *            the ID of the feed, or <code>null</code> for all feeds.
	 * 
	 * @param statusType
	 *            the type of status change that triggered the sending of this message, one of
	 *            <code>EVENT_CONNECTED</code> or <code>EVENT_DISCONNECTED</code>.
	 */
	public void removeFeedMessage(String node, String platform, String service, String feed, int statusType);

	/**
	 * Removes service messages registered for connection events for the specified actor.
	 * 
	 * @param node
	 *            the ID of the node, or <code>null</code> for all nodes.
	 * 
	 * @param platform
	 *            the ID of the platform, or <code>null</code> for all platforms.
	 * 
	 * @param actor
	 *            the ID of the feed, or <code>null</code> for all actors.
	 * 
	 * @param statusType
	 *            the type of status change that triggered the sending of this message, one of
	 *            <code>EVENT_CONNECTED</code> or <code>EVENT_DISCONNECTED</code>.
	 */
	public void removeActorMessage(String node, String platform, String actor, int statusType);

}