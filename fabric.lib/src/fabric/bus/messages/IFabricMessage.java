/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages;

import java.util.Iterator;

import fabric.bus.messages.impl.MessageProperties;
import fabric.bus.routing.IRouting;
import fabric.core.xml.XML;

/**
 * Base interface for all Fabric message types.
 */
public interface IFabricMessage extends IEmbeddedXML {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

	/*
	 * Interface methods
	 */

	/**
	 * Answers the type of the message.
	 * 
	 * @return the message type.
	 */
	public String type();

	/**
	 * Gets the value of the specified property from the message.
	 * 
	 * @param key
	 *            the name of the property.
	 * 
	 * @return the original property value.
	 */
	public String getProperty(String key);

	/**
	 * Sets the value of the specified property in the message.
	 * <p>
	 * Setting a property value to <code>null</code> will remove the property.
	 * </p>
	 * 
	 * @param key
	 *            the name of the property.
	 * 
	 * @param value
	 *            the new value of the property, or <code>null</code> to remove the property.
	 */
	public void setProperty(String key, String value);

	/**
	 * Answers an iterator across the set of property names from the message properties.
	 * 
	 * @return the properties key name iterator.
	 */
	public Iterator<String> propertyKeys();

	/**
	 * Gets the message properties.
	 * 
	 * @return the properties.
	 */
	public MessageProperties getProperties();

	/**
	 * Sets the message properties.
	 * 
	 * @param properties
	 *            the new message properties.
	 */
	public void setProperties(MessageProperties properties);

	/**
	 * Gets the message payload.
	 * 
	 * @return the payload.
	 */
	public IMessagePayload getPayload();

	/**
	 * Sets the message payload.
	 * 
	 * @param payload
	 *            the payload byte array.
	 */
	public void setPayload(IMessagePayload payload);

	/**
	 * Gets the unique identifier for this message.
	 * 
	 * @return the UID.
	 */
	public String getUID();

	/**
	 * Sets the unique identifier for this message.
	 * 
	 * @param uid
	 *            the new UID.
	 */
	public void setUID(String uid);

	/**
	 * Answers the correlation ID associated with this message.
	 * 
	 * @return the correlation ID.
	 */
	public String getCorrelationID();

	/**
	 * Sets the correlation ID associated with this message.
	 * 
	 * @param id
	 *            the event correlation ID.
	 */
	public void setCorrelationID(String id);

	/**
	 * Gets the message's routing information.
	 * 
	 * @return the routing information.
	 */
	public IRouting getRouting();

	/**
	 * Sets the message's routing information.
	 * 
	 * @param routing
	 *            the new routing information.
	 */
	public void setRouting(IRouting routing);

	/**
	 * Gets the XML form of the message.
	 * 
	 * @return the message XML.
	 * 
	 * @throws Exception
	 *             thrown if the conversion into XML fails.
	 */
	public XML toXML() throws Exception;

	/**
	 * Gets the wire form of the message.
	 * 
	 * @return the wire message bytes.
	 * 
	 * @throws Exception
	 *             thrown if the conversion into the wire format fails.
	 */
	public byte[] toWireBytes() throws Exception;

	/**
	 * Gets the flag indicating if this instance has been modified since it was created of the flag has been reset.
	 * 
	 * @return <code>true</code> if this instance has been modified (i.e. any of the setters have been called, or the
	 *         instance has been re-initialized with fresh XML), <code>false</code> otherwise.
	 */
	public boolean metaIsModified();

	/**
	 * Sets the flag indicating if this instance has been modified.
	 * <p>
	 * <strong>Important:</strong> this method must be used with extreme care, as incorrectly clearing the modified flag
	 * will affect the correct operation of the Fabric. Typically this method is only used by classes implementing new
	 * message types, where the current status of the flag can be well understood. It should be avoided in other
	 * circumstances, however it remains a public method to accommodate unforeseen requirements.
	 * </p>
	 */
	public void metaResetModified();

	/**
	 * Gets the topic associated with this message, if the message was received from the Fabric bus.
	 * 
	 * @return the topic name.
	 */
	public String metaGetTopic();

	/**
	 * Sets the topic associated with this message, if the message was received from the Fabric bus.
	 * 
	 * @param topic
	 *            the topic associated with this message.
	 */
	public void metaSetTopic(String topic);

	/**
	 * Gets the value of the specified property from the message object's meta data.
	 * <p>
	 * Note that this is user-defined meta data for use during the lifetime of the message object, and does not form
	 * part of the message itself.
	 * </p>
	 * 
	 * @param key
	 *            the name of the property.
	 * 
	 * @return the property value.
	 */
	public Object metaGetProperty(String key);

	/**
	 * Sets the value of the specified property in the message object's meta data.
	 * <p>
	 * Note that this is user-defined meta data for use during the lifetime of the message object, and does not form
	 * part of the message itself.
	 * </p>
	 * 
	 * @param key
	 *            the name of the property.
	 * 
	 * @param value
	 *            the new value from the property.
	 */
	public void metaSetProperty(String key, Object value);

	/**
	 * Answers an iterator across the set of property names from the message object's meta data.
	 * <p>
	 * Note that this is user-defined meta data for use during the lifetime of the message object, and does not form
	 * part of the message itself.
	 * </p>
	 * 
	 * @return the key name iterator.
	 */
	public Iterator<String> metaPropertyKeys();

}