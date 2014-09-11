/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009, 2012
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
import fabric.bus.messages.IEmbeddedXML;
import fabric.bus.messages.IReplicate;
import fabric.core.xml.XML;
import fabric.core.xml.XMLNode;

/**
 * Class representing a set of properties (name/value pairs) embedded in a Fabric message.
 */
public class MessageProperties extends Notifier implements IEmbeddedXML {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

	/*
	 * Class constants
	 */

	/* Message payload encoding */

	/** The message's properties (a table of name/value pairs). */
	private HashMap<String, String> properties = new HashMap<String, String>();

	/** Cache of the XML form of the message. */
	private XML xmlCache = null;

	/**
	 * Constructs a new instance.
	 */
	public MessageProperties() {

		super(Logger.getLogger("fabric.bus.messages"));
		addChangeListener(this);

	}

	/**
	 * Constructs a new instance, initialized from the specified instance.
	 * 
	 * @param source
	 *            the instance to copy.
	 */
	public MessageProperties(MessageProperties source) {

		this();
		properties = (HashMap<String, String>) source.properties.clone();
		xmlCache = null;

	}

	/**
	 * @see fabric.bus.messages.IEmbeddedXML#init(java.lang.String, fabric.core.xml.XML)
	 */
	@Override
	public void init(String element, XML messageXML) throws Exception {

		properties = new HashMap<String, String>();

		/* Get the XML paths for the properties */
		String elementPath = XML.expandPath(element);
		elementPath = XML.regexpEscape(elementPath);
		String[] propertyPaths = messageXML.getPaths(elementPath + "/f:props\\[.*\\]/.*\\[.*\\]");

		/* For each property... */
		for (int p = 0; p < propertyPaths.length; p++) {

			/* Get the property name (with a path of the form ".../properties/<property-name>[0]/$[0]") */
			XMLNode xmlElement = messageXML.getNode(propertyPaths[p]);
			String name = xmlElement.getNodeName();

			/* Get the parameter value (with a path of the form "/fabric/properties/property[n]") */
			p++;
			String value = messageXML.get(propertyPaths[p]);

			/* Save it away */
			properties.put(name, value);

		}

		xmlCache = null;

	}

	/**
	 * @see fabric.bus.messages.IEmbeddedXML#embed(java.lang.String, fabric.core.xml.XML)
	 */
	@Override
	public void embed(String element, XML messageXML) throws Exception {

		Iterator<String> i = properties.keySet().iterator();

		/* While there are more properties... */
		while (i.hasNext()) {

			/* Get the property name */
			String name = i.next();

			/* Get the property value */
			String value = properties.get(name);

			/* Serialize the property to the XML */
			messageXML.set(element + "/f:props/%s", value, name);

		}

	}

	/**
	 * Gets the value of the specified property.
	 * 
	 * @param key
	 *            the name of the property, or <code>null</code> if it does not exist.
	 * 
	 * @return the value.
	 */
	public String getProperty(String key) {

		return properties.get(key);

	}

	/**
	 * Sets the value of the specified property.
	 * <p>
	 * Setting a property value to <code>null</code> will remove the property.
	 * </p>
	 * 
	 * @param key
	 *            the name of the property.
	 * 
	 * @param value
	 *            the new value from the property, or <code>null</code> to remove the property.
	 */
	public void setProperty(String key, String value) {

		String oldValue = properties.get(key);

		/* If a value has been supplied... */
		if (value != null) {

			properties.put(key, value);

		} else {

			properties.remove(key);

		}

		fireChangeNotification("properties", oldValue, value);

	}

	/**
	 * Answers an iterator across the set of property names from the message properties.
	 * 
	 * @return the key name iterator.
	 */
	public Iterator<String> propertyKeys() {

		return properties.keySet().iterator();

	}

	/**
	 * Empties the message properties.
	 */
	public void empty() {

		properties.clear();
		fireChangeNotification("properties", null, null);

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
	 * @see fabric.bus.messages.IReplicate#replicate()
	 */
	@Override
	public IReplicate replicate() {

		return new MessageProperties(this);

	}

	/**
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {

		super.propertyChange(event);

		/* Something has changed, so invalidate the cached XML form of this instance */
		xmlCache = null;

	}
}
