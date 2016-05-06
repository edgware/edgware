/*
 * (C) Copyright IBM Corp. 2009, 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages.impl;

import java.util.HashMap;

import fabric.Fabric;
import fabric.bus.feeds.impl.ServiceList;
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

    /** The type (class name) of the Fabric service associated with this message */
    private String serviceName = null;

    /** The short form of the type (class name) of the Fabric service associated with this message. */
    private String compactServiceName = null;

    /** Flag indicating if this service message is to be processed en route (true) or at the target node (false) */
    private boolean actionEnRoute = false;

    /** Flag indicating if a notification is required when this message is processed (true) or not (false) */
    private boolean notification = false;

    /** The notification timeout period */
    private int notificationTimeout = 0;

    /** The list of feeds associated with this subscription. */
    private ServiceList serviceList = null;

    /*
     * Class static fields
     */

    /** Table of resource type name/code mappings */
    private static HashMap<String, Integer> typeToCode = new HashMap<String, Integer>();

    /** Table of resource type code/name mappings */
    private static HashMap<Integer, String> codeToType = new HashMap<Integer, String>();

    /*
     * Class methods
     */

    /**
     * Constructs a new instance.
     */
    public ServiceMessage() {

        super();

        setServiceList(new ServiceList());

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
        this.serviceFamilyName = messageXML.get(element + '@' + IServiceMessage.ATTRIBUTE_PLUGIN_FAMILY);

        /* Get the service name (i.e. the class name) */
        this.compactServiceName = messageXML.get(element + '@' + IServiceMessage.ATTRIBUTE_FABRIC_SERVICE);
        String className = Fabric.longName(compactServiceName);
        this.serviceName = (className != null) ? className : compactServiceName;

        /* Get the "action en route" flag */
        String actionEnRouteString = messageXML.get(element + '@' + IServiceMessage.ATTRIBUTE_ACTION_IN_FLIGHT);
        if (actionEnRouteString != null) {
            this.actionEnRoute = Boolean.parseBoolean(actionEnRouteString);
        }

        /* Get the "notification required" flag */
        String notificationString = messageXML.get(element + '@' + IServiceMessage.ATTRIBUTE_NOTIFICATION_REQUIRED);
        if (notificationString != null) {
            this.notification = Boolean.parseBoolean(notificationString);
        }

        /* Get the "notification timeout" value */
        String notificationTimeoutString = messageXML.get(element + '@'
                + IServiceMessage.ATTRIBUTE_NOTIFICATION_TIMEOUT);
        if (notificationTimeoutString != null) {
            this.notificationTimeout = Integer.parseInt(notificationTimeoutString);
        }

        /* Get the list of feeds */
        serviceList.init(element, messageXML);

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
        messageXML.set(element + '@' + IServiceMessage.ATTRIBUTE_PLUGIN_FAMILY, serviceFamilyName);

        /* Set the service name (i.e. the class name) */
        messageXML.set(element + '@' + IServiceMessage.ATTRIBUTE_FABRIC_SERVICE, compactServiceName);

        /* Set the "action en route" flag */
        messageXML.set(element + '@' + IServiceMessage.ATTRIBUTE_ACTION_IN_FLIGHT, Boolean.toString(actionEnRoute));

        /* Set the "notification required" flag */
        messageXML.set(element + '@' + IServiceMessage.ATTRIBUTE_NOTIFICATION_REQUIRED, Boolean.toString(notification));

        /* Set the "notification timeout" value */
        messageXML.set(element + '@' + IServiceMessage.ATTRIBUTE_NOTIFICATION_TIMEOUT, Integer
                .toString(notificationTimeout));

        /* If there is a service list associated with this message... */
        if (serviceList != null) {
            /* Set the service list */
            serviceList.embed(element, messageXML);
        }
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

        /* Attempt to shorten the service name */
        String shortName = Fabric.shortName(serviceName);
        compactServiceName = (shortName != null) ? shortName : serviceName;

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
    public String getEvent() {

        String event = getProperty(PROPERTY_EVENT);

        if (event == null) {
            event = IServiceMessage.EVENT_UNKNOWN;
        }

        return event;

    }

    /**
     * @see fabric.bus.messages.IServiceMessage#setEvent(java.lang.String)
     */
    @Override
    public void setEvent(String event) {

        if (event == null) {
            event = IServiceMessage.EVENT_UNKNOWN;
        }

        setProperty(PROPERTY_EVENT, event);

    }

    /**
     * @see fabric.bus.messages.IServiceMessage#getResourceType()
     */
    @Override
    public String getResourceType() {

        String resourceType = getProperty(PROPERTY_RESOURCE_TYPE);

        if (resourceType == null) {
            resourceType = IServiceMessage.TYPE_UNKNOWN;
        }

        return resourceType;

    }

    /**
     * @see fabric.bus.messages.IServiceMessage#setResourceType(java.lang.String)
     */
    @Override
    public void setResourceType(String type) throws IllegalArgumentException {

        if (type == null) {
            type = IServiceMessage.TYPE_UNKNOWN;
        }

        setProperty(PROPERTY_RESOURCE_TYPE, type);

    }

    /**
     * @see fabric.bus.messages.IServiceMessage#getServiceList()
     */
    @Override
    public ServiceList getServiceList() {

        return serviceList;

    }

    /**
     * @see fabric.bus.messages.IServiceMessage#setServiceList(fabric.bus.feeds.impl.ServiceList)
     */
    @Override
    public void setServiceList(ServiceList serviceList) {

        /* Make a note of the old service list */
        ServiceList oldFeedList = this.serviceList;

        /* If there is currently a service list object... */
        if (oldFeedList != null) {

            /* Stop listening for changes to the old service list */
            oldFeedList.removeChangeListener(this);

        }

        /* Record the new service list */
        this.serviceList = serviceList;

        /* If there is currently a service list... */
        if (serviceList != null) {

            /* Start listening for changes to it */
            serviceList.addChangeListener(this);

        }

        /* Notify listeners that something has changed */
        fireChangeNotification("serviceList", oldFeedList, serviceList);

    }
}
