/*
 * (C) Copyright IBM Corp. 2010, 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages.impl;

import fabric.bus.messages.IClientNotificationMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.client.services.ClientNotificationService;
import fabric.core.xml.XML;

/**
 * A Fabric client notification message, used by the Fabric to notify clients of Fabric events.
 */
public class ClientNotificationMessage extends NotificationMessage implements IClientNotificationMessage {

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
    public ClientNotificationMessage() {

        super();

        construct();

        /* These changes shouldn't be reflected in the instance's "modified" status as this is a new instance */
        metaResetModified();

    }

    /**
     * Constructs a new instance based upon an existing instance.
     * 
     * @param source
     *            the routing instance to replicate.
     */
    public ClientNotificationMessage(ClientNotificationMessage source) {

        super(source);

        setActor(source.getProperty(PROPERTY_ACTOR));
        setActorPlatform(source.getProperty(PROPERTY_ACTOR_PLATFORM));

        construct();

        /* These changes shouldn't be reflected in the instance's "modified" status as this is a new instance */
        metaResetModified();

    }

    /**
     * Constructs a new instance associated with the specified service message.
     * 
     * @param event
     *            the event type for which this message will be a notification (one of the <code>EVENT_</code> constants
     *            defined in <code>IServiceMessage</code>).
     * 
     * @param requestMessage
     *            the service request message with which this notification is associated.
     */
    public ClientNotificationMessage(String event, IServiceMessage requestMessage) {

        super(requestMessage);

        setCorrelationID(requestMessage.getCorrelationID());
        setEvent(event);
        setActor(requestMessage.getProperty(PROPERTY_ACTOR));
        setActorPlatform(requestMessage.getProperty(PROPERTY_ACTOR_PLATFORM));

        construct();

        /* These changes shouldn't be reflected in the instance's "modified" status as this is a new instance */
        metaResetModified();

    }

    /**
     * Initializes an instance.
     */
    private void construct() {

        /* Set the service name: i.e. indicate that this is a message for the feed manager */
        setServiceName(ClientNotificationService.class.getName());

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
     * @see fabric.bus.messages.IClientNotificationMessage#getActor()
     */
    @Override
    public String getActor() {

        return getProperty(PROPERTY_ACTOR);

    }

    /**
     * @see fabric.bus.messages.IClientNotificationMessage#setActor(java.lang.String)
     */
    @Override
    public void setActor(String actor) {

        setProperty(PROPERTY_ACTOR, actor);

    }

    /**
     * @see fabric.bus.messages.IClientNotificationMessage#getActorPlatform()
     */
    @Override
    public String getActorPlatform() {

        return getProperty(PROPERTY_ACTOR_PLATFORM);

    }

    /**
     * @see fabric.bus.messages.IClientNotificationMessage#setActorPlatform(java.lang.String)
     */
    @Override
    public void setActorPlatform(String actorPlatform) {

        setProperty(PROPERTY_ACTOR_PLATFORM, actorPlatform);

    }
}
