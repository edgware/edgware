/*
 * (C) Copyright IBM Corp. 2009, 2016
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages;

import fabric.bus.feeds.impl.ServiceList;

/**
 * Interface defining a Fabric service message, i.e. the messages passed between and handled by Fabric services.
 */
public interface IServiceMessage extends IFabricMessage {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2016";

    /*
     * Class constants
     */

    /* Pre-defined Fabric message attribute names */

    /** The name of the "action in flight" property. */
    public static final String ATTRIBUTE_ACTION_IN_FLIGHT = "aif";

    /** The name of the notification-required property. */
    public static final String ATTRIBUTE_NOTIFICATION_REQUIRED = "ntfy";

    /** The name of the notification timeout property. */
    public static final String ATTRIBUTE_NOTIFICATION_TIMEOUT = "ttl";

    /** The name of the plugin family property. */
    public static final String ATTRIBUTE_PLUGIN_FAMILY = "fmly";

    /** The name of the Fabric service class name property. */
    public static final String ATTRIBUTE_FABRIC_SERVICE = "fs";

    /* Pre-defined Fabric message property names */

    /** The name of the Fabric action property. */
    public static final String PROPERTY_ACTION = "actn";

    /** The name of the Fabric event property. */
    public static final String PROPERTY_EVENT = "evnt";

    /** The name of the actor ID property. */
    public static final String PROPERTY_ACTOR = "actr";

    /** The name of the actor platform property. */
    public static final String PROPERTY_ACTOR_PLATFORM = "actrPlt";

    /** The name of the node ID property. */
    public static final String PROPERTY_NODE = "node";

    /** The name of the service composition ID property. */
    public static final String PROPERTY_COMPOSITION = "cmpn";

    /** The name of the platform ID property. */
    public static final String PROPERTY_PLATFORM = "plt";

    /** The name of the system ID property. */
    public static final String PROPERTY_SYSTEM = "sys";

    /** The name of the notifying service property. */
    public static final String PROPERTY_NOTIFYING_FABRIC_SERVICE = "ntfySrv";

    /** The name of the notifying node property. */
    public static final String PROPERTY_NOTIFYING_NODE = "ntfyNode";

    /** The name of the notification trigger property. */
    // public static final String PROPERTY_NOTIFICATION_TRIGGER = "ntfnTrgr";

    /** The name of the notification event property. */
    public static final String PROPERTY_NOTIFICATION_EVENT = "ntfnEvnt";

    /** The name of the notification event property. */
    public static final String PROPERTY_NOTIFICATION_ACTION = "ntfnActn";

    /** The custom argument string included in a notification message. */
    public static final String PROPERTY_NOTIFICATION_ARGS = "ntfnArgs";

    /** The descriptor of the reply-to feed for this message. */
    public static final String PROPERTY_REPLY_TO_SERVICE = "rplyTo";

    /** The descriptor of the deliver-to feed for this message. */
    public static final String PROPERTY_DELIVER_TO_SERVICE = "dlvrTo";

    /** The name of the feed ID property. */
    public static final String PROPERTY_SERVICE = "srv";

    /** The name of the task ID property. */
    public static final String PROPERTY_TASK = "tsk";

    /** The name of the resource type property. */
    public static final String PROPERTY_RESOURCE_TYPE = "asst";

    /** The SOA service class name. */
    // public static final String PROPERTY_SERVICE_CLASS = "srvClss";

    /** The name of the service ID property. */
    public static final String PROPERTY_SERVICE_ID = "srvInst";

    /** The name of the message payload encoding property. */
    public static final String PROPERTY_ENCODING = "enc";

    /* Pre-defined Fabric event codes used in service messages, and their equivalent names */

    /** Unknown event. */
    public static final String EVENT_UNKNOWN = "unknwn";

    /** Network connection event. */
    public static final String EVENT_CONNECTED = "ctd";

    /** Network disconnection event. */
    public static final String EVENT_DISCONNECTED = "disctd";

    /** Actor request. */
    public static final String EVENT_ACTOR_REQUEST = "actrRqst";

    /** Subscription failed. */
    public static final String EVENT_SUBSCRIPTION_LOST = "subLost";

    /** A message has been handled en route to the final node. */
    public static final String EVENT_MESSAGE_HANDLED_IN_FLIGHT = "actndInFlght";

    /** A message has been handled. */
    public static final String EVENT_MESSAGE_HANDLED = "actnd";

    /** A service operation has timed out. */
    public static final String EVENT_MESSAGE_TIMEOUT = "tout";

    /** A service operation has failed. */
    public static final String EVENT_MESSAGE_FAILED = "fld";

    /** Service request. */
    public static final String EVENT_SERVICE_REQUEST = "srvRqst";

    /** Subscribed/unsubscribed */
    public static final String EVENT_SUBSCRIBED = "subd";
    public static final String EVENT_UNSUBSCRIBED = "unsubd";

    /* Pre-defined Fabric action (command) codes used in service messages */

    /** Indicates a <em>subscribe</em> message. */
    public static final String ACTION_SUBSCRIBE = "sub";

    /** Indicates a message to restore a subscription that has become disconnected. */
    public static final String ACTION_RESTORE_SUBSCRIPTION = "rstrSubn";

    /** Indicates an <em>unsubscribe</em> message. */
    public static final String ACTION_UNSUBSCRIBE = "unsub";

    /** Indicates a request to publish a message to a feed on a remote node. */
    public static final String ACTION_PUBLISH_ON_NODE = "pblshOnNode";

    /** Indicates a request to initialize the services in a service composition on a node. */
    public static final String ACTION_INITIALIZE_COMPOSITION = "initCmpn";

    /** Indicates a request to start the services in a service composition on a node. */
    public static final String ACTION_START_COMPOSITION = "strtCmpn";

    /** Indicates a request to stop the services in a service composition on a node (services are requested to stop). */
    public static final String ACTION_STOP_COMPOSITION = "stpCmpn";

    /**
     * Indicates a request to forcibly stop the services in a service composition on a node (services are forcibly
     * stopped).
     */
    public static final String ACTION_TERMINATE_COMPOSITION = "termCmpn";

    /** Indicates a Fabric asset has changed its availability/readiness state. */
    public static final String ACTION_STATE_CHANGE = "sttChnge";

    /** Indicates a request to start a new instance of a SOA service. */
    @Deprecated
    public static final String ACTION_START_SERVICE_INSTANCE = "strtSrvInst";

    /** Indicates a request to stop an instance of a SOA service. */
    @Deprecated
    public static final String ACTION_STOP_SERVICE_INSTANCE = "stpSrvInst";

    /** Indicates a request to stop all instances of a SOA service. */
    @Deprecated
    public static final String ACTION_STOP_ALL_SERVICE_INSTANCES = "stpAllSrvInsts";

    /* The resource types with which messages can be associated, and their equivalent names */

    /** An unknown resource type. */
    public static final String TYPE_UNKNOWN = "unknwn";

    /** A node resource. */
    public static final String TYPE_NODE = "node";

    /** A platform resource. */
    public static final String TYPE_PLATFORM = "plt";

    /** A service resource. */
    public static final String TYPE_SYSTEM = "sys";

    /** A feed resource. */
    public static final String TYPE_SERVICE = "srv";

    /** An actor resource. */
    public static final String TYPE_ACTOR = "actr";

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
    public String getEvent();

    /**
     * Sets the event (i.e. the cause) code associated with this message.
     *
     * @param event
     *            the event code.
     */
    public void setEvent(String event);

    /**
     * Gets the type of the resource with which the message is associated, one of:
     * <ul>
     * <li>TYPE_NODE</li>
     * <li>TYPE_PLATFORM</li>
     * <li>TYPE_SYSTEM</li>
     * <li>TYPE_SERVICE</li>
     * <li>TYPE_ACTOR</li>
     * </ul>
     *
     * @return the resource type.
     */
    public String getResourceType();

    /**
     * Sets the type of the resource with which the message is associated, one of:
     * <ul>
     * <li>TYPE_NODE</li>
     * <li>TYPE_PLATFORM</li>
     * <li>TYPE_SYSTEM</li>
     * <li>TYPE_SERVICE</li>
     * <li>TYPE_ACTOR</li>
     * </ul>
     *
     * @param type
     *            the resource type.
     *
     * @throws IllegalArgumentException
     *             thrown if <code>type</code> is invalid.
     */
    public void setResourceType(String type) throws IllegalArgumentException;

    /**
     * Answers the list of services associated with this subscription.
     *
     * @return the service list.
     */
    public ServiceList getServiceList();

    /**
     * Sets the list of services associated with this subscription.
     *
     * @param serviceList
     *            the service list.
     */
    public void setServiceList(ServiceList serviceList);
}