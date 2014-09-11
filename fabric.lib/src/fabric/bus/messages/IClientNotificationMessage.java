/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2010, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */
package fabric.bus.messages;

/**
 * Interface for a Fabric client notification message, used by the Fabric to notify clients of Fabric events.
 */
public interface IClientNotificationMessage extends INotificationMessage {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010, 2012";

	/*
	 * Class methods
	 */

	/**
	 * Answers the actor ID associated with this message.
	 * 
	 * @return the actor ID.
	 */
	public String getActor();

	/**
	 * Sets the actor ID associated with this message.
	 * 
	 * @param actor
	 *            the actor ID.
	 */
	public void setActor(String actor);

	/**
	 * Answers the actor platform ID associated with this message.
	 * 
	 * @return the actor platform ID.
	 */
	public String getActorPlatform();

	/**
	 * Sets the actor platform ID associated with this message.
	 * 
	 * @param actorPlatform
	 *            the actor platform ID.
	 */
	public void setActorPlatform(String actorPlatform);

}
