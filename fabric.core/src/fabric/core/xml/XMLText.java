/*
 * (C) Copyright IBM Corp. 2006
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.xml;

/**
 * Data structure used to record an XML element's content.
 */
public class XMLText extends XMLNode {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2006";

	/*
	 * Class fields
	 */

	/** To hold the text of the XML node */
	private String text = "";

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 * 
	 * @param text
	 *            the text of the node.
	 * @param parent
	 *            the parent node.
	 */
	public XMLText(String text, XMLNode parent) {

		super("$", parent);

		if (text != null) {
			this.text = XML.decodeEntityRefs(text);
		} else {
			this.text = "";
		}

	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		String toString = null;

		if (text != null) {
			toString = XML.encodeEntityRefs(text);
		} else {
			toString = "" + null;
		}

		return toString;

	}

	public String getText() {

		return text;

	}

	public void setText(String text) {

		this.text = text;

	}

	public void appendText(String text) {

		this.text += text;

	}
}
