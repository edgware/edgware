/*
 * (C) Copyright IBM Corp. 2009, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages.impl;

import java.util.HashMap;

import fabric.bus.feeds.impl.FeedList;
import fabric.bus.messages.IServiceMessage;
import fabric.core.xml.XML;

/**
 * A Fabric service message.
 */
public class ServiceMessage extends FabricMessage implements IServiceMessage {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2014";

	/*
	 * Class fields
	 */

	/** The name of the plug-in family associated with this message */
	private String serviceFamilyName = null;

	/** The name of the Fabric service associated with this message */
	private String serviceName = null;

	/** Flag indicating if this service message is to be processed en route (true) or at the target node (false) */
	private boolean actionEnRoute = false;

	/** Flag indicating if a notification is required when this message is processed (true) or not (false) */
	private boolean notification = false;

	/** The notification timeout period */
	private int notificationTimeout = 0;

	/** The list of feeds associated with this subscription. */
	private FeedList feedList = null;

	/*
	 * Class static fields
	 */

	/** Table of reason code name/code mappings */
	private static HashMap<String, Integer> nameToCode = new HashMap<String, Integer>();

	/** Table of reason code code/name mappings */
	private static HashMap<Integer, String> codeToName = new HashMap<Integer, String>();

	/** Table of resource type name/code mappings */
	private static HashMap<String, Integer> typeToCode = new HashMap<String, Integer>();

	/** Table of resource type code/name mappings */
	private static HashMap<Integer, String> codeToType = new HashMap<Integer, String>();

	/*
	 * Static class initialization
	 */

	static {

		/* Build the tables mapping between reason names and codes */

		nameToCode.put(EVENT_UNKNOWN_NAME, EVENT_UNKNOWN);
		codeToName.put(EVENT_UNKNOWN, EVENT_UNKNOWN_NAME);

		nameToCode.put(EVENT_CONNECTED_NAME, EVENT_CONNECTED);
		codeToName.put(EVENT_CONNECTED, EVENT_CONNECTED_NAME);

		nameToCode.put(EVENT_DISCONNECTED_NAME, EVENT_DISCONNECTED);
		codeToName.put(EVENT_DISCONNECTED, EVENT_DISCONNECTED_NAME);

		nameToCode.put(EVENT_ACTOR_REQUEST_NAME, EVENT_ACTOR_REQUEST);
		codeToName.put(EVENT_ACTOR_REQUEST, EVENT_ACTOR_REQUEST_NAME);

		nameToCode.put(EVENT_SUBSCRIPTION_LOST_NAME, EVENT_SUBSCRIPTION_LOST);
		codeToName.put(EVENT_SUBSCRIPTION_LOST, EVENT_SUBSCRIPTION_LOST_NAME);

		nameToCode.put(EVENT_MESSAGE_HANDLED_EN_ROUTE_NAME, EVENT_MESSAGE_HANDLED_EN_ROUTE);
		codeToName.put(EVENT_MESSAGE_HANDLED_EN_ROUTE, EVENT_MESSAGE_HANDLED_EN_ROUTE_NAME);

		nameToCode.put(EVENT_MESSAGE_HANDLED_NAME, EVENT_MESSAGE_HANDLED);
		codeToName.put(EVENT_MESSAGE_HANDLED, EVENT_MESSAGE_HANDLED_NAME);

		nameToCode.put(EVENT_MESSAGE_TIMEOUT_NAME, EVENT_MESSAGE_TIMEOUT);
		codeToName.put(EVENT_MESSAGE_TIMEOUT, EVENT_MESSAGE_TIMEOUT_NAME);

		nameToCode.put(EVENT_MESSAGE_FAILED_NAME, EVENT_MESSAGE_FAILED);
		codeToName.put(EVENT_MESSAGE_FAILED, EVENT_MESSAGE_FAILED_NAME);

		nameToCode.put(EVENT_SERVICE_REQUEST_NAME, EVENT_SERVICE_REQUEST);
		codeToName.put(EVENT_SERVICE_REQUEST, EVENT_SERVICE_REQUEST_NAME);

		nameToCode.put(EVENT_SUBSCRIBED_NAME, EVENT_SUBSCRIBED);
		codeToName.put(EVENT_SUBSCRIBED, EVENT_SUBSCRIBED_NAME);

		nameToCode.put(EVENT_UNSUBSCRIBED_NAME, EVENT_UNSUBSCRIBED);
		codeToName.put(EVENT_UNSUBSCRIBED, EVENT_UNSUBSCRIBED_NAME);

		/* Build the tables mapping between resource types and codes */

		typeToCode.put(TYPE_UNKNOWN_NAME, TYPE_UNKNOWN);
		codeToType.put(TYPE_UNKNOWN, TYPE_UNKNOWN_NAME);

		typeToCode.put(TYPE_NODE_NAME, TYPE_NODE);
		codeToType.put(TYPE_NODE, TYPE_NODE_NAME);

		typeToCode.put(TYPE_PLATFORM_NAME, TYPE_PLATFORM);
		codeToType.put(TYPE_PLATFORM, TYPE_PLATFORM_NAME);

		typeToCode.put(TYPE_SERVICE_NAME, TYPE_SERVICE);
		codeToType.put(TYPE_SERVICE, TYPE_SERVICE_NAME);

		typeToCode.put(TYPE_FEED_NAME, TYPE_FEED);
		codeToType.put(TYPE_FEED, TYPE_FEED_NAME);

		typeToCode.put(TYPE_ACTOR_NAME, TYPE_ACTOR);
		codeToType.put(TYPE_ACTOR, TYPE_ACTOR_NAME);

	}

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 */
	public ServiceMessage() {

		super();

		setFeedList(new FeedList());

		/* These changes shouldn't be reflected in the instance's "modified" status as this is a new instance */
		metaResetModified();

		/* But we do need to regenerate the XML */
		invalidateXMLCache();

	}

	/**
	 * @see fabric.bus.messages.impl.FabricMessage#init(java.lang.String, fabric.core.xml.XML)
	 */
	@Override
	public void init(String element, XML messageXML) throws Exception {

		super.init(element, messageXML);

		/* Get the service family name */
		this.serviceFamilyName = messageXML.get(element + "@family");

		/* Get the service name (i.e. the class name) */
		this.serviceName = messageXML.get(element + "@serviceName");

		/* Get the "action en route" flag */
		String actionEnRouteString = messageXML.get(element + "@actionEnRoute");
		if (actionEnRouteString != null) {
			this.actionEnRoute = Boolean.parseBoolean(actionEnRouteString);
		}

		/* Get the "notification required" flag */
		String notificationString = messageXML.get(element + "@notify");
		if (notificationString != null) {
			this.notification = Boolean.parseBoolean(notificationString);
		}

		/* Get the "notification timeout" value */
		String notificationTimeoutString = messageXML.get(element + "@notifyTimeout");
		if (notificationTimeoutString != null) {
			this.notificationTimeout = Integer.parseInt(notificationTimeoutString);
		}

		/* Get the list of feeds */
		feedList.init(element, messageXML);

		/* These changes shouldn't be reflected in the instance's "modified" status as this is a new instance */
		metaResetModified();

	}

	/**
	 * @see fabric.bus.messages.impl.FabricMessage#embed(java.lang.String, fabric.core.xml.XML)
	 */
	@Override
	public void embed(String element, XML messageXML) throws Exception {

		super.embed(element, messageXML);

		/* Set the service family name */
		messageXML.set(element + "@family", serviceFamilyName);

		/* Set the service name (i.e. the class name) */
		messageXML.set(element + "@serviceName", serviceName);

		/* Set the "action en route" flag */
		messageXML.set(element + "@actionEnRoute", Boolean.toString(actionEnRoute));

		/* Set the "notification required" flag */
		messageXML.set(element + "@notify", Boolean.toString(notification));

		/* Set the "notification timeout" value */
		messageXML.set(element + "@notifyTimeout", Integer.toString(notificationTimeout));

		/* If there is a feed list associated with this message... */
		if (feedList != null) {
			/* Set the feed list */
			feedList.embed(element, messageXML);
		}
	}

	/**
	 * Answers the name corresponding to the specified event type ID.
	 * 
	 * @param eventType
	 *            the event ID to lookup.
	 * 
	 * @return the name.
	 */
	public static String getEventName(int eventType) {

		return codeToName.get(eventType);
	}

	/**
	 * Answers the event type ID corresponding to the specified event type name.
	 * 
	 * @param eventName
	 *            the event name to lookup.
	 * 
	 * @return the ID.
	 */
	public static int getEventID(String eventName) {

		return nameToCode.get(eventName);
	}

	/**
	 * Answers the name corresponding to the specified resource type ID.
	 * 
	 * @param resourceType
	 *            the resource ID to lookup.
	 * 
	 * @return the name.
	 */
	public static String getResourceName(int resourceType) {

		return codeToType.get(resourceType);
	}

	/**
	 * Answers the resource type ID corresponding to the specified resource type name.
	 * 
	 * @param resourceName
	 *            the resource name to lookup.
	 * 
	 * @return the ID.
	 */
	public static int getResourceID(int resourceName) {

		return typeToCode.get(resourceName);
	}

	/**
	 * @see fabric.bus.messages.IServiceMessage#getServiceFamilyName()
	 */
	@Override
	public String getServiceFamilyName() {

		return serviceFamilyName;

	}

	/**
	 * @see fabric.bus.messages.IServiceMessage#setServiceFamilyName(java.lang.String)
	 */
	@Override
	public void setServiceFamilyName(String serviceFamilyName) {

		String oldServiceFamilyName = this.serviceFamilyName;
		this.serviceFamilyName = serviceFamilyName;
		fireChangeNotification("serviceFamilyName", oldServiceFamilyName, serviceFamilyName);

	}

	/**
	 * @see fabric.bus.messages.IServiceMessage#getServiceName()
	 */
	@Override
	public String getServiceName() {

		return serviceName;

	}

	/**
	 * @see fabric.bus.messages.IServiceMessage#setServiceName(java.lang.String)
	 */
	@Override
	public void setServiceName(String serviceName) {

		String oldServiceName = this.serviceName;
		this.serviceName = serviceName;
		fireChangeNotification("serviceName", oldServiceName, serviceName);

	}

	/**
	 * @see fabric.bus.messages.IServiceMessage#getActionEnRoute()
	 */
	@Override
	public boolean getActionEnRoute() {

		return actionEnRoute;

	}

	/**
	 * @see fabric.bus.messages.IServiceMessage#setActionEnRoute(boolean)
	 */
	@Override
	public void setActionEnRoute(boolean actionEnRoute) {

		boolean oldActionEnRoute = this.actionEnRoute;
		this.actionEnRoute = actionEnRoute;
		fireChangeNotification("actionEnRoute", Boolean.toString(oldActionEnRoute), Boolean.toString(actionEnRoute));

	}

	/**
	 * @see fabric.bus.messages.IServiceMessage#getNotification()
	 */
	@Override
	public boolean getNotification() {

		return notification;

	}

	/**
	 * @see fabric.bus.messages.IServiceMessage#setNotification(boolean)
	 */
	@Override
	public void setNotification(boolean notificationRequired) {

		boolean oldNotificationRequired = this.notification;
		this.notification = notificationRequired;
		fireChangeNotification("notify", Boolean.toString(oldNotificationRequired), Boolean
				.toString(notificationRequired));

	}

	/**
	 * @see fabric.bus.messages.IServiceMessage#getNotificationTimeout()
	 */
	@Override
	public int getNotificationTimeout() {

		return notificationTimeout;

	}

	/**
	 * @see fabric.bus.messages.IServiceMessage#setNotificationTimeout(int)
	 */
	@Override
	public void setNotificationTimeout(int notificationTimeout) {

		int oldNotificationTimeout = this.notificationTimeout;
		this.notificationTimeout = notificationTimeout;
		fireChangeNotification("notifyTimeout", Integer.toString(oldNotificationTimeout), Integer
				.toString(notificationTimeout));

	}

	/**
	 * @see fabric.bus.messages.IServiceMessage#getAction()
	 */
	@Override
	public String getAction() {

		return getProperty(PROPERTY_ACTION);

	}

	/**
	 * @see fabric.bus.messages.IServiceMessage#setAction(String)
	 */
	@Override
	public void setAction(String action) {

		setProperty(PROPERTY_ACTION, action);

	}

	/**
	 * @see fabric.bus.messages.IServiceMessage#getEvent()
	 */
	@Override
	public int getEvent() {

		String eventString = getProperty(PROPERTY_EVENT);
		int event = (eventString != null) ? nameToCode.get(eventString) : IServiceMessage.EVENT_UNKNOWN;
		return event;

	}

	/**
	 * @see fabric.bus.messages.IServiceMessage#setEvent(int)
	 */
	@Override
	public void setEvent(int event) {

		String eventString = codeToName.get(event);

		if (eventString == null) {
			eventString = codeToName.get(EVENT_UNKNOWN);
		}

		setProperty(PROPERTY_EVENT, eventString);

	}

	/**
	 * @see fabric.bus.messages.IServiceMessage#getResourceType()
	 */
	@Override
	public int getResourceType() {

		String resourceTypeString = getProperty(ConnectionMessage.PROPERTY_RESOURCE_TYPE);
		int resourceType = (resourceTypeString != null) ? typeToCode.get(resourceTypeString)
				: IServiceMessage.TYPE_UNKNOWN;
		return resourceType;

	}

	/**
	 * @see fabric.bus.messages.IServiceMessage#setResourceType(int)
	 */
	@Override
	public void setResourceType(int type) throws IllegalArgumentException {

		String resourceTypeString = codeToType.get(type);

		if (resourceTypeString == null) {
			resourceTypeString = codeToType.get(TYPE_UNKNOWN);
		}

		setProperty(ConnectionMessage.PROPERTY_RESOURCE_TYPE, resourceTypeString);

	}

	/**
	 * @see fabric.bus.messages.IServiceMessage#getFeedList()
	 */
	@Override
	public FeedList getFeedList() {

		return feedList;

	}

	/**
	 * @see fabric.bus.messages.IServiceMessage#setFeedList(fabric.bus.feeds.impl.FeedList)
	 */
	@Override
	public void setFeedList(FeedList feedList) {

		/* Make a note of the old feed list */
		FeedList oldFeedList = this.feedList;

		/* If there is currently a feed list object... */
		if (oldFeedList != null) {

			/* Stop listening for changes to the old feed list */
			oldFeedList.removeChangeListener(this);

		}

		/* Record the new feed list */
		this.feedList = feedList;

		/* If there is currently a feed list... */
		if (feedList != null) {

			/* Start listening for changes to it */
			feedList.addChangeListener(this);

		}

		/* Notify listeners that something has changed */
		fireChangeNotification("feedList", oldFeedList, feedList);

	}
}
