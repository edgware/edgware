/*
 * (C) Copyright IBM Corp. 2009, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.routing;

import fabric.Fabric;
import fabric.core.xml.XML;

/**
 * Data structure representing the the route embedded in a Fabric message.
 * 
 */
public abstract class MessageRoutingFactory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

	/*
	 * Class methods
	 */

	/**
	 * No instantiation of this class.
	 */
	private MessageRoutingFactory() {

	}

	/**
	 * Create a Fabric routing instance from an existing XML representation.
	 * 
	 * @param element
	 *            the element containing the XML.
	 * 
	 * @param messageXML
	 *            the Fabric message containing routing information.
	 * 
	 * @throws Exception
	 */
	public static IRouting construct(String element, XML messageXML) throws Exception {

		/* To hold the new instance */
		IRouting instance = null;

		/* Get the message type */
		String type = messageXML.get(element + "/f:routing@type");

		/* If no message type has been specified... */
		if (type != null) {

			/* Create a new instance */
			instance = (IRouting) Fabric.instantiate(type);

		}
		
		/* If we have created a new instance... */
		if (instance != null) {
			
			instance.init(element, messageXML);
			
		}

		return instance;

	}

}
