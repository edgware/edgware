/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2006
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Data structure used to record an XML element and its attributes (but not its content).
 */
public class XMLElement extends XMLNode {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2006";

	/*
	 * Class fields
	 */

	/** To hold the attributes of the element */
	private HashMap<String, String> attributes = new HashMap<String, String>();

	/** To hold the sub-nodes of this element */
	private ArrayList<XMLNode> subnodes = new ArrayList<XMLNode>();

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 * 
	 * @param nodeName
	 *            the name of the node.
	 * @param parent
	 *            the parent node.
	 */
	public XMLElement(String nodeName, XMLNode parent) {

		super(nodeName, parent);

	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		StringBuffer toString = new StringBuffer();

		try {

			/* Add the element nodeName */
			toString.append(getNodeName());

			/* Add its attributes */

			Iterator<String> a = attributes.keySet().iterator();

			while (a.hasNext()) {
				String attributeName = a.next();
				String attributeValue = attributes.get(attributeName);
				String encodedAttributeValue = XML.encodeEntityRefs(attributeValue);
				toString.append(" " + attributeName + "=\"" + encodedAttributeValue + "\"");
			}

		} catch (Exception e) {

			e.printStackTrace();
			toString.setLength(0);
			toString.append(super.toString());

		}

		return toString.toString();
	}

	public HashMap<String, String> getAttributes() {

		return attributes;
	}

	public void setAttributes(HashMap<String, String> attributes) {

		this.attributes = attributes;
	}

	public ArrayList<XMLNode> getSubnodes() {

		return subnodes;
	}

	public void setSubnodes(ArrayList<XMLNode> subnodes) {

		this.subnodes = subnodes;
	}
}
