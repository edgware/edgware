/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages;

/**
 * Interface representing a Fabric connection/disconnection message.
 * <p>
 * This corresponds to the messages that are automatically sent when a Fabric client connects to, or unexpectedly
 * disconnects from, a Fabric broker. Such messages are used by the Fabric to trigger handling of a connection status
 * change event.
 * </p>
 */
public interface IConnectionMessage extends IServiceMessage {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/*
	 * Interface methods
	 */

	/**
	 * Gets the ID of the node that generated the message.
	 * 
	 * @return the node ID.
	 */
	public String getNode();

	/**
	 * Sets the ID of the node that generated the message.
	 * 
	 * @param node
	 *            the node ID.
	 */
	public void setNode(String node);

	/**
	 * Gets the ID of the platform that generated the message.
	 * 
	 * @return the platform ID.
	 */
	public String getPlatform();

	/**
	 * Sets the ID of the platform that generated the message.
	 * 
	 * @param platform
	 *            the platform ID.
	 */
	public void setPlatform(String platform);

	/**
	 * Gets the ID of the service that generated the message.
	 * 
	 * @return the resource ID.
	 */
	public String getService();

	/**
	 * Sets the ID of the resource that generated the message.
	 * 
	 * @param service
	 *            the service ID.
	 */
	public void setService(String service);

	/**
	 * Gets the ID of the feed that generated the message.
	 * 
	 * @return the feed ID.
	 */
	public String getFeed();

	/**
	 * Sets the ID of the feed that generated the message.
	 * 
	 * @param feed
	 *            the feed ID.
	 */
	public void setFeed(String feed);

	/**
	 * Gets the ID of the actor that generated the message.
	 * 
	 * @return the actor ID.
	 */
	public String getActor();

	/**
	 * Sets the ID of the actor that generated the message.
	 * 
	 * @param actor
	 *            the actor ID.
	 */
	public void setActor(String actor);

	/**
	 * Gets the ID of the platform of the actor that generated the message.
	 * 
	 * @return the actor platform ID.
	 */
	public String getActorPlatform();

	/**
	 * Sets the ID of the platform of the actor that generated the message.
	 * 
	 * @param actor
	 *            the actor platform ID.
	 */
	public void setActorPlatform(String actorPlatform);

}