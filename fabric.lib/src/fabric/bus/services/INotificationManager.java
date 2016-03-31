/*
 * (C) Copyright IBM Corp. 2010, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.services;

import fabric.ServiceDescriptor;
import fabric.bus.messages.IClientNotificationMessage;
import fabric.bus.messages.INotificationMessage;
import fabric.bus.messages.IServiceMessage;

/**
 * Interface for classes handling service notification messages for the Fabric.
 * <p>
 * When a Fabric service is invoked it can <em>optionally</em> reply with a response message. This message is not
 * delivered directly to the actor concerned, but to the Notification Manager running in the Fabric Manager.
 * Notification messages can be associated with each service message sent across the Fabric and indicate, for example:
 * <ul>
 * <li><strong>Success:</strong> the corresponding service invocation succeeded.</li>
 * <li><strong>Failure:</strong> the corresponding service invocation failed.</li>
 * </ul>
 * </p>
 * <p>
 * When a success or failure message is received from a service with a correlation ID that matches a set of notification
 * messages, a corresponding set of pre-registered messages are triggered for delivery. The properties, payload, and
 * feed list of the incoming notification message are copied into these messges (in a separate part of the message to
 * their own).
 * </p>
 * <p>
 * If no success or failure message is received from a service within a user-defined timeout period then the registered
 * timeout message/s is/are sent. Note that this is not necessarily an indication of the failure of the service, just a
 * failure to respond within the timeout period.
 * </p>
 * <p>
 * When a success, failure, or timeout message is sent, <em>all</em> other messages for the corresponding correlation ID
 * are de-registered.
 * </p>
 * <p>
 * If a message is received from a service and there are no corresponding acknowledgment messages for the correlation ID
 * then it is ignored.
 * </p>
 */
public interface INotificationManager {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010, 2012";

	/*
	 * Interface methods
	 */

	/**
	 * Adds a new notification (e.g. success and failure/timeout messages) for a correlation ID and event.
	 * 
	 * @param correlationID
	 *            the correlation ID associated with the notification to be added.
	 * 
	 * @param event
	 *            the ID of the event associated with the notification to be added.
	 * 
	 * @param actor
	 *            the ID of the actor to receive the notification.
	 * 
	 * @param actorPlatform
	 *            the ID of the platform via which the actor is connected to the Fabric.
	 * 
	 * @param message
	 *            the notification message to be sent upon receipt of the specified event. Note that the correlation ID
	 *            and event are defined in the message.
	 * 
	 * @param timeout
	 *            the period (in seconds) until the notification times-out and the failure message is automatically
	 *            delivered.
	 * 
	 * @param retained
	 *            flag indicating if this message should be retained until explicitly removed (<code>true</code>), or
	 *            removed automatically when any other notification for this correlation ID is fired (<code>false</code>
	 *            ).
	 */
	public void addNotification(String correlationID, int event, String actor, String actorPlatform,
			IServiceMessage message, int timeout, boolean retained);

	/**
	 * Adds a new notification (e.g. success and failure/timeout messages) for a correlation ID, feed descriptor, and
	 * event.
	 * 
	 * @param correlationID
	 *            the correlation ID associated with the notification.
	 * 
	 * @param serviceDescriptor
	 *            the feed descriptor associated with the notification to be added.
	 * 
	 * @param event
	 *            the ID of the event associated with the notification to be added.
	 * 
	 * @param actor
	 *            the ID of the actor to receive the notification.
	 * 
	 * @param actorPlatform
	 *            the ID of the platform via which the actor is connected to the Fabric.
	 * 
	 * @param message
	 *            the notification message to be sent upon receipt of the specified event. Note that the correlation ID,
	 *            feed, and event are defined in the message.
	 * 
	 * @param timeout
	 *            the period (in seconds) until the notification times-out and the failure message is automatically
	 *            delivered.
	 * 
	 * @param retained
	 *            flag indicating if this message should be retained until explicitly removed (<code>true</code>), or
	 *            removed automatically when any other notification for this correlation ID is fired (<code>false</code>
	 *            ).
	 */
	public void addNotification(String correlationID, ServiceDescriptor serviceDescriptor, int event, String actor,
			String actorPlatform, IServiceMessage message, int timeout, boolean retained);

	/**
	 * Removes the set of notifications associated with a correlation ID.
	 * 
	 * @param correlationID
	 *            the correlation ID associated with the notifications to be removed.
	 */
	public void removeNotifications(String correlationID);

	/**
	 * Removes the set of notifications associated with a correlation ID and a specific feed descriptor.
	 * 
	 * @param correlationID
	 *            the correlation ID associated with the notifications to be removed.
	 * 
	 * @param serviceDescriptor
	 *            the feed descriptor associated with the notifications to be removed.
	 */
	public void removeNotifications(String correlationID, ServiceDescriptor serviceDescriptor);

	/**
	 * Fires a set of notifications associated with a correlation ID and event.
	 * 
	 * @param correlationID
	 *            the correlation ID associated with the notifications to be fired.
	 * 
	 * @param event
	 *            the ID of the event associated with the notification to be fired.
	 * 
	 * @param notificationArgs
	 *            a notification-specific value to be inserted into the client notification before it is fired.
	 * 
	 * @param trigger
	 *            the notification message.
	 */
	public void fireNotifications(String correlationID, int event, String notificationArgs, INotificationMessage trigger)
			throws Exception;

	/**
	 * Fires a set of notifications associated with a correlation ID, feed descriptor, and event.
	 * 
	 * @param correlationID
	 *            the correlation ID associated with the notifications to be fired.
	 * 
	 * @param serviceDescriptor
	 *            the feed descriptor associated with the notifications to be fired.
	 * 
	 * @param event
	 *            the ID of the event associated with the notifications to be fired.
	 * 
	 * @param notificationArgs
	 *            a notification-specific value to be inserted into the client notification before it is fired.
	 * 
	 * @param trigger
	 *            the notification message.
	 */
	public void fireNotifications(String correlationID, ServiceDescriptor serviceDescriptor, int event,
			String notificationArgs, INotificationMessage trigger) throws Exception;

	/**
	 * Sends a notification message directly to the specified actor on the specified platform connected to the local
	 * node.
	 * <p>
	 * This method is a mechanism to directly communicate with an actor from the Fabric.
	 * </p>
	 * 
	 * @param actor
	 *            the ID of the actor to receive the notification.
	 * 
	 * @param actorPlatform
	 *            the ID of the platform via which the actor is connected to the Fabric.
	 * 
	 * @param notificationArgs
	 *            a notification-specific value to be inserted into the client notification before it is fired.
	 * 
	 * @param actorNotification
	 *            the message to be sent to the actor.
	 * 
	 * @param trigger
	 *            the incoming trigger notification message.
	 */
	public void deliverActorNotification(String actor, String actorPlatform, String notificationArgs,
			IClientNotificationMessage actorNotification, INotificationMessage trigger) throws Exception;

	/**
	 * Sends a notification message directly to the specified service.
	 * 
	 * @param notificationArgs
	 *            a notification-specific value to be inserted into the client notification before it is fired.
	 * 
	 * @param serviceNotification
	 *            the message to be sent to the service.
	 * 
	 * @param trigger
	 *            the incoming trigger notification message.
	 */
	public void deliverServiceNotification(String notificationArgs, IServiceMessage serviceNotification,
			INotificationMessage trigger) throws Exception;

}