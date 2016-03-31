/*
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.client.services;

/**
 * Interface for classes providing Fabric notification services to platforms.
 */
public interface IPlatformNotificationServices {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

	/*
	 * Interface constants
	 */

	/*
	 * Interface methods
	 */

	/**
	 * Registers the handler Fabric notification messages received for the specified platform.
	 * 
	 * @param platform
	 *            the platform ID.
	 * 
	 * @param handler
	 *            the notification handler.
	 * 
	 * @return the previously registered handler, or <code>null</code> if there was not one.
	 */
	public IPlatformNotificationHandler registerPlatformNotificationHandler(String platform,
			IPlatformNotificationHandler handler);

	/**
	 * De-registers the handler to handle Fabric notification messages received for the specified platform.
	 * 
	 * @param platform
	 *            the platform ID.
	 * 
	 * @return the previously registered handler, or <code>null</code> if there was not one.
	 */
	public IPlatformNotificationHandler deregisterPlatformNotificationHandler(String platform);

	/**
	 * Registers the handler Fabric notification messages received for the specified service.
	 * 
	 * @param platform
	 *            the platform ID.
	 * 
	 * @param service
	 *            the service ID.
	 * 
	 * @param handler
	 *            the notification handler.
	 * 
	 * @return the previously registered handler, or <code>null</code> if there was not one.
	 */
	public IPlatformNotificationHandler registerServiceNotificationHandler(String platform, String service,
			IPlatformNotificationHandler handler);

	/**
	 * De-registers the handler to handle Fabric notification messages received for the specified service.
	 * 
	 * @param platform
	 *            the platform ID.
	 * 
	 * @param service
	 *            the service ID.
	 * 
	 * @return the previously registered handler, or <code>null</code> if there was not one.
	 */
	public IPlatformNotificationHandler deregisterServiceNotificationHandler(String platform, String service);
}
