/*
 * (C) Copyright IBM Corp. 2007
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.xml;

/**
 * Handler class for XML tokens in the style of a SAX parser <code>DefaultHandler</code>. This class is intended for use
 * when no SAX parser is available in the target system.
 * <p>
 * <strong>Note:</strong> this is <em>not</em> a SAX implementation; it simply provides a subset of the capabilities of
 * SAX.
 * </p>
 */
public interface IXMLTokenHandler {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007";

	/*
	 * Interface methods
	 */

	/**
	 * Invoked at the beginning of each XML element in the input stream.
	 * 
	 * @param name
	 *            the element name.
	 * 
	 * @param elementAttributes
	 *            the element attributes, or an empty list if there are none.
	 */
	public void xmlElementStart(String name, XMLAttributes elementAttributes);

	/**
	 * Receives the character data inside an XML element.
	 * 
	 * @param characters
	 *            the characters.
	 */
	public void xmlElementData(char[] characters);

	/**
	 * Receives an XML comment.
	 * 
	 * @param characters
	 *            the characters.
	 */
	public void xmlComment(char[] characters);

	/**
	 * Invoked at the end of each XML element in the input stream.
	 * 
	 * @param name
	 *            the element name.
	 */
	public void xmlElementEnd(String name);

	/**
	 * Invoked when a processing instruction is found in the XML input stream.
	 * 
	 * @param pi
	 *            the processing instruction.
	 */
	public void xmlProcessingInstruction(String pi);
}