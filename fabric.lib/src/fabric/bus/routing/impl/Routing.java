/*
 * (C) Copyright IBM Corp. 2007, 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.routing.impl;

import java.beans.PropertyChangeEvent;
import java.util.logging.Logger;

import fabric.Fabric;
import fabric.Notifier;
import fabric.bus.messages.IFabricMessage;
import fabric.bus.messages.impl.MessageProperties;
import fabric.bus.routing.IRouting;
import fabric.core.xml.XML;

/**
 * Class representing the route embedded in a Fabric message.
 */
public abstract class Routing extends Notifier implements IRouting {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2012";

    /*
     * Class constants
     */

    /*
     * Class fields
     */

    /** The type (class name) of the routing algorithm. */
    private String type = null;

    /** The short form of the type (class name) of the message. */
    private String compactType = null;

    /** The route's properties (a table of name/value pairs) */
    private MessageProperties properties = new MessageProperties();

    /** Cache of the XML form of the message. */
    private XML xmlCache = null;

    private String currentNode;

    /*
     * Class methods
     */

    /**
     * Constructs a new instance.
     */
    public Routing() {

        super(Logger.getLogger("fabric.bus.routing"));

        this.currentNode = homeNode();

        type = getClass().getName();
        String shortName = Fabric.shortName(type);
        compactType = (shortName != null) ? shortName : type;

        addChangeListener(this);

    }

    /**
     * Constructs a new instance, initialized from the specified instance.
     *
     * @param source
     *            the instance to copy.
     */
    public Routing(Routing source) {

        this();
        properties = (MessageProperties) source.properties.replicate();
        xmlCache = null;

    }

    /**
     * @see fabric.bus.messages.IEmbeddedXML#init(java.lang.String, fabric.core.xml.XML)
     */
    @Override
    public void init(String element, XML messageXML) throws Exception {

        /* Get the message type */
        compactType = messageXML.get(element + "/rt@t");
        String type = Fabric.longName(compactType);
        this.type = (type != null) ? type : compactType;

        /* Get the message properties */
        properties.init(element + "/rt", messageXML);

        xmlCache = null;

    }

    /**
     * @see fabric.bus.messages.IEmbeddedXML#embed(java.lang.String, fabric.core.xml.XML)
     */
    @Override
    public void embed(String element, XML messageXML) throws Exception {

        /* Set the message type */
        messageXML.set(element + "/rt@t", compactType);

        /* Set the message properties */
        properties.embed(element + "/rt", messageXML);

    }

    /**
     * @see fabric.bus.routing.IRouting#getProperty(java.lang.String)
     */
    @Override
    public String getProperty(String key) {

        return properties.getProperty(key);

    }

    /**
     * @see fabric.bus.routing.IRouting#setProperty(java.lang.String, java.lang.String)
     */
    @Override
    public void setProperty(String key, String value) {

        String oldValue = properties.getProperty(key);

        properties.setProperty(key, value);

        fireChangeNotification("properties", oldValue, value);

    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        String toString = null;

        try {

            if (xmlCache == null) {

                xmlCache = new XML();
                embed("", xmlCache);

            }

            toString = xmlCache.toString();

        } catch (Exception e) {

            e.printStackTrace();
            toString = super.toString();

        }

        return toString;

    }

    /**
     * @see fabric.Notifier#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {

        super.propertyChange(event);

        /* Something has changed, so invalidate the cached XML form of this instance */
        xmlCache = null;

    }

    /**
     * @see fabric.bus.routing.IRouting#isDuplicate(IFabricMessage)
     */
    @Override
    public boolean isDuplicate(IFabricMessage message) {
        // Most routing implementations are never expected to cause duplicate messages
        return false;
    }

    /**
     * @see fabric.bus.routing.IRouting#currentNode()
     */
    @Override
    public String currentNode() throws UnsupportedOperationException {

        return currentNode;

    }

}
