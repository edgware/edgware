/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages;

import fabric.core.xml.XML;

/**
 * Interface for classes that can be serialized as XML, and then embedded into another XML document. Conversely they can
 * be initialized from XML embedded in a XML document.
 */
public interface IEmbeddedXML extends INotifier, IReplicate {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/*
	 * Interface methods
	 */

	/**
	 * Serializes this instance into the specified XML document.
	 * 
	 * @param element
	 *            the element that will contain the XML.
	 * 
	 * @param messageXML
	 *            the XML into which this instance will be serialized.
	 * 
	 * @throws Exception
	 *             thrown if the instance cannot be embedded.
	 */
	public void embed(String element, XML messageXML) throws Exception;

	/**
	 * Initializes this instance from XML embedded in specified document.
	 * 
	 * @param element
	 *            the element containing the XML.
	 * 
	 * @param messageXML
	 *            the document from which this instance will be initialized.
	 * 
	 * @throws Exception
	 *             thrown if the instance cannot be initialized from the XML.
	 */
	public void init(String element, XML messageXML) throws Exception;

}