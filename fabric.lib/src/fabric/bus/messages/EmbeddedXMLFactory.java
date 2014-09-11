/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages;

import fabric.Fabric;
import fabric.core.xml.XML;

/**
 * Factory class for <code>IEmbeddedXML</code> classes.
 * 
 */
public class EmbeddedXMLFactory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

	/*
	 * Class static fields
	 */

	/*
	 * Class methods
	 */

	/**
	 * No instantiation of this class.
	 */
	private EmbeddedXMLFactory() {
	}

	/**
	 * Create a IEmbeddedXML instance from an existing XML representation.
	 * 
	 * @param element
	 *            the element containing the XML.
	 * 
	 * @param messageXML
	 *            the document from which this instance will be initialized.
	 * 
	 * @return the instance.
	 * 
	 * @throws ClassNotFoundException
	 *             thrown if the class cannot be found.
	 * 
	 * @throws InstantiationException
	 *             thrown if the class cannot be instantiated.
	 * 
	 * @throws IllegalAccessException
	 *             thrown if the class cannot be instantiated (for example, if there is no default constructor).
	 * 
	 * @throws Exception
	 *             thrown if the class cannot be initialized.
	 */
	public static IEmbeddedXML create(String element, XML messageXML) throws ClassNotFoundException,
			IllegalAccessException, InstantiationException, Exception {

		/* To hold the new instance */
		IEmbeddedXML instance = null;

		/* Get the message type */
		String type = messageXML.get(element + "@enc");

		/* If no message type has been specified... */
		if (type == null) {

			throw new IllegalArgumentException("No type attribute in XML");

		}

		/* Create a new instance */
		instance = (IEmbeddedXML) Fabric.instantiate(type);

		/* If we have created a new instance... */
		if (instance != null) {

			instance.init(element, messageXML);

		}

		return instance;

	}

}
