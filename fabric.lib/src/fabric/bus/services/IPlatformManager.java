/*
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */
package fabric.bus.services;

import fabric.bus.messages.IPlatformNotificationMessage;

/**
 * Interface for classes handling platform messages for the Fabric.
 * <p>
 * When a message is sent to a platform it is not delivered directly to the platform concerned, but to the platform
 * manager running in the Fabric Manager.
 * </p>
 * <p>
 * When a platform message is received, a corresponding message is sent to the platform. The payload of this message is
 * set to the payload of the incoming platform message.
 * </p>
 */
public interface IPlatformManager {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

	/*
	 * Interface methods
	 */

	/**
	 * Sends a message to the specified platform connected to the local node.
	 * <p>
	 * This method is a mechanism to directly communicate with a platform from the Fabric.
	 * 
	 * @param platform
	 *            the platform to receive the notification.
	 * 
	 * @param message
	 *            the message is to be sent.
	 */
	public void notifyPlatform(String platform, IPlatformNotificationMessage message) throws Exception;

	/**
	 * Sends a message to the specified service connected to the local node.
	 * <p>
	 * This method is a mechanism to directly communicate with a service from the Fabric.
	 * 
	 * @param platform
	 *            the platform that hosts the service.
	 * 
	 * @param service
	 *            the platform to receive the notification.
	 * 
	 * @param message
	 *            the message is to be sent.
	 */
	public void notifyService(String platform, String service, IPlatformNotificationMessage message) throws Exception;
}