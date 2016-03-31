/*
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages;

/**
 * Interface for a Fabric platform notification message, used by the Fabric to notify platforms of Fabric events.
 */
public interface IPlatformNotificationMessage extends IServiceMessage {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

	/*
	 * Class methods
	 */

	/**
	 * Answers the platform ID associated with this message.
	 * 
	 * @return the platform ID.
	 */
	public String getPlatform();

	/**
	 * Sets the platform ID associated with this message.
	 * 
	 * @param platform
	 *            the actor platform ID.
	 */
	public void setPlatform(String platform);

}
