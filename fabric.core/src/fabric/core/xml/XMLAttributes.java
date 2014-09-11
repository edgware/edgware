/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2007
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.xml;

import java.util.ArrayList;

/**
 * Class representing the list of attributes in an XML element.
 */
public class XMLAttributes extends ArrayList {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007";

	/*
	 * Class constants
	 */

	private static final long serialVersionUID = 6977733373211849504L;

	/*
	 * Inner classes
	 */

	/**
	 * Data structure to hold an attribute of an XML element.
	 */
	private class XMLAttribute {

		/** The name of the attribute */
		String name = null;

		/** The value of the attribute */
		String value = null;

		/**
		 * Construct a new instance.
		 * 
		 * @param name
		 * @param value
		 */
		XMLAttribute(String name, String value) {

			this.name = name;
			this.value = XML.decodeEntityRefs(value);

		}
	}

	/*
	 * Class methods
	 */

	/**
	 * Gets the name of an attribute.
	 * 
	 * @param index
	 *            the index of the attribute in the list for the XML element.
	 */
	public String getName(int index) {

		XMLAttribute entry = (XMLAttribute) get(index);
		return entry.name;

	}

	/**
	 * Gets the value of an attribute.
	 * 
	 * @param index
	 *            the index of the attribute in the list for the XML element.
	 */
	public String getValue(int index) {

		XMLAttribute entry = (XMLAttribute) get(index);
		return entry.value;

	}

	/**
	 * Adds a new attribute to the list for an XML element.
	 * 
	 * @param name
	 *            the name of the attribute.
	 * @param value
	 *            the value of the attribute.
	 */
	public void addAttribute(String name, String value) {

		XMLAttribute entry = new XMLAttribute(name, value);
		add(entry);

	}
}
