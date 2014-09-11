/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2010, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages.impl;

import fabric.Fabric;
import fabric.bus.messages.INotificationMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.routing.IRouting;
import fabric.bus.services.impl.NotificationManagerService;
import fabric.core.xml.XML;

/**
 * A Fabric client notification message, used by the Fabric to notify clients of Fabric events.
 */
public class NotificationMessage extends ServiceMessage implements INotificationMessage {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010, 2012";

	/*
	 * Class fields
	 */

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 */
	public NotificationMessage() {

		super();

		construct();

		/* These changes shouldn't be reflected in the instance's "modified" status as this is a new instance */
		metaResetModified();

	}

	/**
	 * Constructs a new instance associated with the specified service message.
	 * <p>
	 * The notification message will be give a default event type of:
	 * <ul>
	 * <li><code>IServiceMessage.EVENT_MESSAGE_HANDLED</code> if the notification is being created on the last node to
	 * run the service.</li>
	 * <li><code>IServiceMessage.EVENT_MESSAGE_HANDLED_EN_ROUTE</code> if the notification is being created on an
	 * intermediate node.</li>
	 * </ul>
	 * This is determined by consulting the routing information in the source <code>IServiceMessage</code>.
	 * </p>
	 * <p>
	 * The message will also be configured with:
	 * <ul>
	 * <li>The correlation ID of the service message.</li>
	 * <li>The notification event.</li>
	 * <li>The notification action.</li>
	 * <li>The ID of the notifying node (the node that the message is being created on.</li>
	 * <li>The ID of the notifying service (the service indicated in the <code>IServiceMessage</code>).</li>
	 * <li>The return route to the node that originated the service invocation.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param requestMessage
	 *            the service request message with which this notification is associated.
	 */
	public NotificationMessage(IServiceMessage requestMessage) {

		super();

		construct();

		setCorrelationID(requestMessage.getCorrelationID());
		setProperty(IServiceMessage.PROPERTY_NOTIFICATION_EVENT, getEventName(requestMessage.getEvent()));
		setProperty(IServiceMessage.PROPERTY_NOTIFICATION_ACTION, requestMessage.getAction());
		setProperty(IServiceMessage.PROPERTY_NOTIFYING_NODE, homeNode());
		setProperty(IServiceMessage.PROPERTY_NOTIFYING_SERVICE, requestMessage.getServiceName());

		/* Set the route for this notification message (if any) */

		IRouting requestMessageRouting = requestMessage.getRouting();
		IRouting notificationRoute = null;

		/* If this message contains routing information... */
		if (requestMessageRouting != null) {

			/* Get the return route and set it in the notification message */
			notificationRoute = requestMessageRouting.returnRoute();
			setRouting(notificationRoute);

		}

		/* If this is the last node to process the message... */
		if (requestMessageRouting == null || homeNode().equals(requestMessageRouting.endNode())
				|| requestMessageRouting.endNode() == null) {
			setEvent(IServiceMessage.EVENT_MESSAGE_HANDLED);
		} else {
			setEvent(IServiceMessage.EVENT_MESSAGE_HANDLED_EN_ROUTE);
		}

		/* These changes shouldn't be reflected in the instance's "modified" status as this is a new instance */
		metaResetModified();

	}

	/**
	 * Initializes an instance.
	 */
	private void construct() {

		/* Set the service name: i.e. indicate that this is a message for the feed manager */
		setServiceName(NotificationManagerService.class.getName());

		/* Indicate that this is a built-in Fabric plug-in */
		setServiceFamilyName(Fabric.FABRIC_PLUGIN_FAMILY);

		/* Indicate that this message should be actioned along the route from subscriber to the publisher */
		setActionEnRoute(false);

	}

	/**
	 * @see fabric.bus.messages.impl.FabricMessage#init(java.lang.String, fabric.core.xml.XML)
	 */
	@Override
	public void init(String element, XML messageXML) throws Exception {

		super.init(element, messageXML);

		/* These changes shouldn't be reflected in the instance's "modified" status as this is a new instance */
		metaResetModified();

	}

	/**
	 * @see fabric.bus.messages.impl.FabricMessage#embed(java.lang.String, fabric.core.xml.XML)
	 */
	@Override
	public void embed(String element, XML messageXML) throws Exception {

		super.embed(element, messageXML);

	}

	/**
	 * @see fabric.bus.messages.INotificationMessage#getNotificationEvent()
	 */
	@Override
	public int getNotificationEvent() {

		String eventName = getProperty(IServiceMessage.PROPERTY_NOTIFICATION_EVENT);
		return getEventID(eventName);

	}

	/**
	 * @see fabric.bus.messages.INotificationMessage#setNotificationEvent(int)
	 */
	@Override
	public void setNotificationEvent(int eventType) {

		String eventTypeName = getEventName(eventType);
		setProperty(IServiceMessage.PROPERTY_NOTIFICATION_EVENT, eventTypeName);

	}

	/**
	 * @see fabric.bus.messages.INotificationMessage#getNotificationAction()
	 */
	@Override
	public String getNotificationAction() {

		return getProperty(IServiceMessage.PROPERTY_NOTIFICATION_ACTION);

	}

	/**
	 * @see fabric.bus.messages.INotificationMessage#setNotificationAction(String)
	 */
	@Override
	public void setNotificationAction(String action) {

		setProperty(IServiceMessage.PROPERTY_NOTIFICATION_ACTION, action);

	}

	/**
	 * @see fabric.bus.messages.INotificationMessage#getNotificationArgs()
	 */
	@Override
	public String getNotificationArgs() {

		return getProperty(PROPERTY_NOTIFICATION_ARGS);

	}

	/**
	 * @see fabric.bus.messages.INotificationMessage#setNotificationArgs(java.lang.String)
	 */
	@Override
	public void setNotificationArgs(String args) {

		setProperty(PROPERTY_NOTIFICATION_ARGS, args);

	}
}
