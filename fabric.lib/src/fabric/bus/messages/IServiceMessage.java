/*
 * (C) Copyright IBM Corp. 2009, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages;

import fabric.bus.feeds.impl.FeedList;

/**
 * Interface defining a Fabric service message, i.e. the messages passed between and handled by Fabric services.
 */
public interface IServiceMessage extends IFabricMessage {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

	/*
	 * Class constants
	 */

	/* Pre-defined Fabric message property names */

	/** The name of the Fabric action property. */
	public static final String PROPERTY_ACTION = "f:action";

	/** The name of the Fabric event property. */
	public static final String PROPERTY_EVENT = "f:event";

	/** The name of the actor ID property. */
	public static final String PROPERTY_ACTOR = "f:actor";

	/** The name of the actor platform property. */
	public static final String PROPERTY_ACTOR_PLATFORM = "f:actorPlatform";

	/** The name of the node ID property. */
	public static final String PROPERTY_NODE = "f:node";

	/** The name of the service composition ID property. */
	public static final String PROPERTY_COMPOSITION = "f:composition";

	/** The name of the platform ID property. */
	public static final String PROPERTY_PLATFORM = "f:platform";

	/** The name of the service ID property. */
	public static final String PROPERTY_SERVICE = "f:service";

	/** The name of the notifying service property. */
	public static final String PROPERTY_NOTIFYING_SERVICE = "f:notifyingService";

	/** The name of the notifying node property. */
	public static final String PROPERTY_NOTIFYING_NODE = "f:notifyingNode";

	/** The name of the notification timeout property. */
	public static final String PROPERTY_NOTIFICATION_TIMEOUT = "f:notifyTimeout";

	/** The name of the notification trigger property. */
	public static final String PROPERTY_NOTIFICATION_TRIGGER = "f:notificationTrigger";

	/** The name of the notification event property. */
	public static final String PROPERTY_NOTIFICATION_EVENT = "f:notificationEvent";

	/** The name of the notification event property. */
	public static final String PROPERTY_NOTIFICATION_ACTION = "f:notificationAction";

	/** The custom argument string included in a notification message. */
	public static final String PROPERTY_NOTIFICATION_ARGS = "f:notificationArgs";

	/** The descriptor of the reply-to feed for this message. */
	public static final String PROPERTY_REPLY_TO_FEED = "f:replyToFeed";

	/** The descriptor of the deliver-to feed for this message. */
	public static final String PROPERTY_DELIVER_TO_FEED = "f:deliverToFeed";

	/** The name of the feed ID property. */
	public static final String PROPERTY_FEED = "f:feed";

	/** The name of the task ID property. */
	public static final String PROPERTY_TASK = "f:task";

	/** The name of the resource type property. */
	public static final String PROPERTY_RESOURCE_TYPE = "f:asset";

	/** The SOA service class name. */
	public static final String PROPERTY_SERVICE_CLASS = "f:serviceClass";

	/** The name of the service ID property. */
	public static final String PROPERTY_SERVICE_ID = "f:serviceInstance";

	/* Pre-defined Fabric event codes used in service messages, and their equivalent names */

	/** Unknown event. */
	public static final int EVENT_UNKNOWN = 0;
	public static final String EVENT_UNKNOWN_NAME = "unknown";

	/** Network connection event. */
	public static final int EVENT_CONNECTED = 1;
	public static final String EVENT_CONNECTED_NAME = "connected";

	/** Network disconnection event. */
	public static final int EVENT_DISCONNECTED = 2;
	public static final String EVENT_DISCONNECTED_NAME = "disconnected";

	/** Actor request. */
	public static final int EVENT_ACTOR_REQUEST = 3;
	public static final String EVENT_ACTOR_REQUEST_NAME = "actor_request";

	/** Subscription failed. */
	public static final int EVENT_SUBSCRIPTION_LOST = 4;
	public static final String EVENT_SUBSCRIPTION_LOST_NAME = "subscription_lost";

	/** A message has been handled en route to the final node. */
	public static final int EVENT_MESSAGE_HANDLED_EN_ROUTE = 5;
	public static final String EVENT_MESSAGE_HANDLED_EN_ROUTE_NAME = "request_actioned_en_route";

	/** A message has been handled. */
	public static final int EVENT_MESSAGE_HANDLED = 6;
	public static final String EVENT_MESSAGE_HANDLED_NAME = "request_actioned";

	/** A service operation has timed out. */
	public static final int EVENT_MESSAGE_TIMEOUT = 7;
	public static final String EVENT_MESSAGE_TIMEOUT_NAME = "request_timeout";

	/** A service operation has failed. */
	public static final int EVENT_MESSAGE_FAILED = 8;
	public static final String EVENT_MESSAGE_FAILED_NAME = "request_failed";

	/** Service request. */
	public static final int EVENT_SERVICE_REQUEST = 9;
	public static final String EVENT_SERVICE_REQUEST_NAME = "service_request";

	/** Subscribed/unsubscribed */
	public static final int EVENT_SUBSCRIBED = 10;
	public static final String EVENT_SUBSCRIBED_NAME = "subscribed";
	public static final int EVENT_UNSUBSCRIBED = 11;
	public static final String EVENT_UNSUBSCRIBED_NAME = "unsubscribed";

	/* Pre-defined Fabric action (command) codes used in service messages */

	/** Indicates a <em>subscribe</em> message. */
	public static final String ACTION_SUBSCRIBE = "subscribe";

	/** Indicates a message to restore a subscription that has become disconnected. */
	public static final String ACTION_RESTORE_SUBSCRIPTION = "restore_subscription";

	/** Indicates an <em>unsubscribe</em> message. */
	public static final String ACTION_UNSUBSCRIBE = "unsubscribe";

	/** Indicates a request to publish a message to a feed on a remote node. */
	public static final String ACTION_PUBLISH_ON_NODE = "publishOnNode";

	/** Indicates a request to initialize the services in a service composition on a node. */
	public static final String ACTION_INITIALIZE_COMPOSITION = "initializeComposition";

	/** Indicates a request to start the services in a service composition on a node. */
	public static final String ACTION_START_COMPOSITION = "startComposition";

	/** Indicates a request to stop the services in a service composition on a node (services are requested to stop). */
	public static final String ACTION_STOP_COMPOSITION = "stopComposition";

	/**
	 * Indicates a request to forcibly stop the services in a service composition on a node (services are forcibly
	 * stopped).
	 */
	public static final String ACTION_TERMINATE_COMPOSITION = "terminateComposition";

	/** Indicates a Fabric asset has changed its availability/readiness state. */
	public static final String ACTION_STATE_CHANGE = "stateChange";

	/** Indicates a request to start a new instance of a SOA service. */
	@Deprecated
	public static final String ACTION_START_SERVICE_INSTANCE = "startServiceInstance";

	/** Indicates a request to stop an instance of a SOA service. */
	@Deprecated
	public static final String ACTION_STOP_SERVICE_INSTANCE = "stopServiceInstance";

	/** Indicates a request to stop all instances of a SOA service. */
	@Deprecated
	public static final String ACTION_STOP_ALL_SERVICE_INSTANCES = "stopAllServiceInstances";

	/* The resource types with which messages can be associated, and their equivalent names */

	/** An unknown resource type. */
	public static final int TYPE_UNKNOWN = 0;
	public static final String TYPE_UNKNOWN_NAME = "unknown";

	/** A node resource. */
	public static final int TYPE_NODE = 1;
	public static final String TYPE_NODE_NAME = "node";

	/** A platform resource. */
	public static final int TYPE_PLATFORM = 2;
	public static final String TYPE_PLATFORM_NAME = "platform";

	/** A service resource. */
	public static final int TYPE_SERVICE = 3;
	public static final String TYPE_SERVICE_NAME = "service";

	/** A feed resource. */
	public static final int TYPE_FEED = 4;
	public static final String TYPE_FEED_NAME = "feed";

	/** An actor resource. */
	public static final int TYPE_ACTOR = 5;
	public static final String TYPE_ACTOR_NAME = "actor";

	/*
	 * Class methods
	 */

	/**
	 * Answers the <em>family</em> name of the Fabric service associated with this message.
	 * 
	 * @return the family name of the service.
	 */
	public String getServiceFamilyName();

	/**
	 * Sets the <em>family</em> name of the Fabric service associated with this message.
	 * 
	 * @param serviceFamilyName
	 *            the family name of the service.
	 */
	public void setServiceFamilyName(String serviceFamilyName);

	/**
	 * Answers the name of the Fabric service associated with this message.
	 * 
	 * @return the name of the service.
	 */
	public String getServiceName();

	/**
	 * Sets the name of the Fabric service associated with this message.
	 * 
	 * @param serviceName
	 *            the name of the service.
	 */
	public void setServiceName(String serviceName);

	/**
	 * Answers the flag indicating if service messages are to be processed en route (i.e. a each node between the
	 * sending node and the target node), or just at the target node.
	 * 
	 * @return <code>true</code> if the message is to be action at each node, <code>false</code> if it is to be actioned
	 *         at the target node only.
	 */
	public boolean getActionEnRoute();

	/**
	 * Sets the flag indicating if services messages are to be process en route (i.e. a each node between the sending
	 * node and the target node), or just at the target node.
	 * 
	 * @param actionEnRoute
	 *            <code>true</code> if the message is to be action at each node, <code>false</code> if it is to be
	 *            actioned at the target node only.
	 */
	public void setActionEnRoute(boolean actionEnRoute);

	/**
	 * Answers the flag indicating if a notification is required from nodes that handle this message.
	 * 
	 * @return <code>true</code> if a notification is required, <code>false</code> otherwise.
	 */
	public boolean getNotification();

	/**
	 * Sets the flag indicating if a notification is required from nodes that handle this message.
	 * 
	 * @param notificationRequired
	 *            <code>true</code> if a notification is required, <code>false</code> otherwise.
	 */
	public void setNotification(boolean notificationRequired);

	/**
	 * Answers the notification timeout period for this message, i.e. the time after which a failure notification will
	 * be automatically sent.
	 * 
	 * @return the notification timeout period (in seconds).
	 */
	public int getNotificationTimeout();

	/**
	 * Sets the notification timeout period for this message, i.e. the time after which a failure notification will be
	 * automatically sent.
	 * 
	 * @param timeout
	 *            the notification timeout period (in seconds).
	 */
	public void setNotificationTimeout(int timeout);

	/**
	 * Answers the action code associated with this message.
	 * 
	 * @return the message type defined action code.
	 */
	public String getAction();

	/**
	 * Sets the action code associated with this message.
	 * 
	 * @param event
	 *            the message type defined action code.
	 */
	public void setAction(String action);

	/**
	 * Answers the event (i.e. the cause) code associated with this message.
	 * 
	 * @return the event code.
	 */
	public int getEvent();

	/**
	 * Sets the event (i.e. the cause) code associated with this message.
	 * 
	 * @param event
	 *            the event code.
	 */
	public void setEvent(int event);

	/**
	 * Gets the type of the resource with which the message is associated, one of:
	 * <ul>
	 * <li>TYPE_NODE</li>
	 * <li>TYPE_PLATFORM</li>
	 * <li>TYPE_SERVICE</li>
	 * <li>TYPE_FEED</li>
	 * <li>TYPE_ACTOR</li>
	 * </ul>
	 * 
	 * @return the resource type.
	 */
	public int getResourceType();

	/**
	 * Sets the type of the resource with which the message is associated, one of:
	 * <ul>
	 * <li>TYPE_NODE</li>
	 * <li>TYPE_PLATFORM</li>
	 * <li>TYPE_SERVICE</li>
	 * <li>TYPE_FEED</li>
	 * <li>TYPE_ACTOR</li>
	 * </ul>
	 * 
	 * @param type
	 *            the resource type.
	 * 
	 * @throws IllegalArgumentException
	 *             thrown if <code>type</code> is invalid.
	 */
	public void setResourceType(int type) throws IllegalArgumentException;

	/**
	 * Answers the list of feeds associated with this subscription.
	 * 
	 * @return the feed list.
	 */
	public FeedList getFeedList();

	/**
	 * Sets the list of feeds associated with this subscription.
	 * 
	 * @param feedList
	 *            the feed list.
	 */
	public void setFeedList(FeedList feedList);
}