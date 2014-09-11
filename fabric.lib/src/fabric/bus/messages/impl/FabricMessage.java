/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages.impl;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import fabric.Notifier;
import fabric.bus.messages.FabricMessageFactory;
import fabric.bus.messages.IFabricMessage;
import fabric.bus.messages.IMessagePayload;
import fabric.bus.messages.IReplicate;
import fabric.bus.routing.IRouting;
import fabric.bus.routing.MessageRoutingFactory;
import fabric.core.xml.XML;

/**
 * Class representing a Fabric message.
 */
public abstract class FabricMessage extends Notifier implements IFabricMessage {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2014";

	/*
	 * Class fields
	 */

	/** The XML namespace for Fabric messages when serialized as XML. */
	private String xmlNamespace = null;

	/** The type of the message. */
	private String type = null;

	/** The unique identifier for this message. */
	private String uid = null;

	/** The correlation ID for this message. */
	private String correlationID = null;

	/** The message's properties (a table of name/value pairs). */
	private MessageProperties properties = null;

	/** The message's routing information. */
	private IRouting routing = null;

	/** The message payload. */
	private IMessagePayload payload = null;

	/** The topic associated with this message. */
	private String topic = null;

	/** Flag indicating if this instance has been modified since the XML was last generated. */
	private boolean isModified = false;

	/** Cache of the XML form of the message. */
	private XML xmlCache = null;

	/** The message object's meta data properties. */
	private final HashMap<String, Object> metaProperties = new HashMap<String, Object>();

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 */
	public FabricMessage() {

		super(Logger.getLogger("fabric.bus.messages"));

		/* Get the name of the XML namespace for Fabric messages */
		xmlNamespace = config("fabric.xml.namespace", "http://fabric.org");

		type = getClass().getName();
		uid = FabricMessageFactory.generateUID();
		setProperties(new MessageProperties());
		setPayload(new MessagePayload());

		/* These changes shouldn't be reflected in the instance's "modified" status as this is a new instance */
		metaResetModified();

		/* But we do need to regenerate the XML */
		invalidateXMLCache();

		/* Listen for changes to embedded objects */
		addChangeListener(this);

	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#type()
	 */
	@Override
	public String type() {

		return type;

	}

	/**
	 * @see fabric.bus.messages.IEmbeddedXML#init(java.lang.String, fabric.core.xml.XML)
	 */
	@Override
	public void init(String element, XML messageXML) throws Exception {

		/* Get the message's unique identifier */
		uid = messageXML.get(element + "@uid");

		/* Get the message's correlation ID */
		correlationID = messageXML.get(element + "@cid");

		/* Get the message properties */
		properties.init(element, messageXML);

		/* Get the routing information from the message */
		setRouting(MessageRoutingFactory.construct(element, messageXML));

		/* Get the payload */
		payload.init(element, messageXML);

		/* These changes shouldn't be reflected in the instance's "modified" status as this is a new instance */
		metaResetModified();

	}

	/**
	 * @see fabric.bus.messages.IEmbeddedXML#embed(java.lang.String, fabric.core.xml.XML)
	 */
	@Override
	public void embed(String element, XML messageXML) throws Exception {

		/* Set the XML namespace */
		messageXML.set(element + "@xmlns:f", xmlNamespace);

		/* Set the message type */
		messageXML.set(element + "@type", type);

		/* Set the message's unique identifier */
		messageXML.set(element + "@uid", uid);

		/* If there is a correlation ID... */
		if (correlationID != null) {
			/* Serialize it */
			messageXML.set(element + "@cid", correlationID);
		}

		/* If there are any properties associated with this message... */
		if (properties != null) {
			/* Set the message properties */
			properties.embed(element, messageXML);
		}

		/* If there is any routing information associated with this message... */
		if (routing != null) {
			/* Serialize it */
			routing.embed(element, messageXML);
		}

		/* If there is a payload associated with this message... */
		if (payload != null) {
			/* Set the payload */
			payload.embed(element, messageXML);
		}

	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(String key) {

		return properties.getProperty(key);

	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#setProperty(java.lang.String, java.lang.String)
	 */
	@Override
	public void setProperty(String key, String value) {

		properties.setProperty(key, value);

	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#getProperties()
	 */
	@Override
	public MessageProperties getProperties() {

		return properties;

	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#setProperties(fabric.bus.messages.impl.MessageProperties)
	 */
	@Override
	public void setProperties(MessageProperties properties) {

		/* Make a note of the old properties */
		MessageProperties oldProperties = this.properties;

		/* If there is currently a properties object... */
		if (oldProperties != null) {

			/* Stop listening for changes to the old properties object */
			oldProperties.removeChangeListener(this);

		}

		/* Record the new properties */
		this.properties = properties;

		/* If there is currently a properties object... */
		if (properties != null) {

			/* Start listening for changes to it */
			properties.addChangeListener(this);

		}

		/* Notify listeners that something has changed */
		fireChangeNotification("properties", oldProperties, properties);

	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#propertyKeys()
	 */
	@Override
	public Iterator<String> propertyKeys() {

		return properties.propertyKeys();

	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#getUID()
	 */
	@Override
	public String getUID() {

		return uid;

	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#setUID(java.lang.String)
	 */
	@Override
	public void setUID(String uid) {

		String oldUID = this.uid;

		this.uid = uid;

		fireChangeNotification("uid", oldUID, uid);

	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#setCorrelationID(java.lang.String)
	 */
	@Override
	public void setCorrelationID(String correlationID) {

		String oldCorrelationID = this.correlationID;

		this.correlationID = correlationID;

		fireChangeNotification("cid", oldCorrelationID, correlationID);

	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#getCorrelationID()
	 */
	@Override
	public String getCorrelationID() {

		return correlationID;

	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#getRouting()
	 */
	@Override
	public IRouting getRouting() {

		return routing;

	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#setRouting(fabric.bus.routing.IRouting)
	 */
	@Override
	public void setRouting(IRouting routing) {

		/* Make a note of the old routing object */
		IRouting oldRouting = this.routing;

		/* If there is currently a routing object... */
		if (oldRouting != null) {

			/* Stop listening for changes to the old routing object */
			oldRouting.removeChangeListener(this);

		}

		/* Record the new routing object */
		this.routing = routing;

		/* If there is currently a routing object... */
		if (routing != null) {

			/* Start listening for changes to it */
			routing.addChangeListener(this);

		}

		/* Notify listeners that something has changed */
		fireChangeNotification("routing", oldRouting, routing);

	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#metaGetTopic()
	 */
	@Override
	public String metaGetTopic() {

		return topic;

	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#metaSetTopic(java.lang.String)
	 */
	@Override
	public void metaSetTopic(String topic) {

		this.topic = topic;

	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#getPayload()
	 */
	@Override
	public IMessagePayload getPayload() {

		return payload;

	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#setPayload(fabric.bus.messages.IMessagePayload)
	 */
	@Override
	public void setPayload(IMessagePayload payload) {

		/* Make a note of the old payload */
		IMessagePayload oldPayload = this.payload;

		/* If there is currently a payload object... */
		if (oldPayload != null) {

			/* Stop listening for changes to the old payload object */
			oldPayload.removeChangeListener(this);

		}

		/* Record the new payload */
		this.payload = payload;

		/* If there is currently a payload object... */
		if (payload != null) {

			/* Start listening for changes to it */
			payload.addChangeListener(this);

		}

		/* Notify listeners that something has changed */
		fireChangeNotification("payload", oldPayload, payload);

	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#toXML()
	 */
	@Override
	public XML toXML() throws Exception {

		if (xmlCache == null) {

			xmlCache = new XML();
			embed("/f:fabric", xmlCache);

		}

		return xmlCache;

	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#toWireBytes()
	 */
	@Override
	public byte[] toWireBytes() throws Exception {

		return toXML().toBytes();

	}

	/**
	 * Invalidates the cached XML for this message.
	 */
	protected void invalidateXMLCache() {

		xmlCache = null;

	}

	/**
	 * Set the flag indicating if this message has been modified.
	 * 
	 * @param isModified
	 *            <code>true</code> if the status is to be set to <em>modified</em>, <code>false</code> otherwise.
	 */
	protected void metaSetModified(boolean isModified) {

		this.isModified = isModified;
		invalidateXMLCache();

	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#metaIsModified()
	 */
	@Override
	public boolean metaIsModified() {

		return isModified;

	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#metaResetModified()
	 */
	@Override
	public void metaResetModified() {

		isModified = false;

	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#metaGetProperty(java.lang.String)
	 */
	@Override
	public Object metaGetProperty(String key) {

		return metaProperties.get(key);

	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#metaSetProperty(java.lang.String, java.lang.Object)
	 */
	@Override
	public void metaSetProperty(String key, Object value) {

		metaProperties.put(key, value);

	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#metaPropertyKeys()
	 */
	@Override
	public Iterator<String> metaPropertyKeys() {

		return metaProperties.keySet().iterator();

	}

	/**
	 * @see fabric.bus.messages.IFabricMessage#toString()
	 */
	@Override
	public String toString() {

		String toString = null;

		try {

			XML xml = toXML();
			toString = xml.toString();

		} catch (Exception e) {

			e.printStackTrace();
			toString = super.toString();

		}

		return toString;

	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return toString().hashCode();

	}

	/**
	 * Compares two Fabric messages by comparing their string representations. Note that this comparison does not
	 * consider meta-data associated with the messages. Therefore, two messages that differ in their meta-data only will
	 * be considered equal.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object target) {

		String thisToString = toString();
		String targetToString = target.toString();

		return thisToString.equals(targetToString);

	}

	/**
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {

		super.propertyChange(event);

		/* Something has changed, so invalidate the cached XML form of this instance */
		metaSetModified(true);

	}

	/**
	 * @see fabric.bus.messages.IReplicate#replicate()
	 */
	@Override
	public IReplicate replicate() {

		IFabricMessage replica = null;

		try {
			XML messageXML = toXML();
			replica = FabricMessageFactory.create(metaGetTopic(), messageXML);
		} catch (Exception e) {
			logger.fine("Failed to replicate message:\n" + e);
		}

		return replica;

	}
}
