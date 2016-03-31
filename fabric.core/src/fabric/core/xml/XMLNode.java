/*
 * (C) Copyright IBM Corp. 2006
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.xml;

/**
 * Base class for data structures used to record XML nodes (for example elements and content).
 */
public class XMLNode {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2006";

	/*
	 * Class fields
	 */

	/** The nodeName of the element */
	private String nodeName = null;

	/** The parent node of this node */
	private XMLNode parent = null;

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
	public XMLNode(String nodeName, XMLNode parent) {

		this.parent = parent;
		this.nodeName = nodeName;
	}

	public String getNodeName() {

		return nodeName;
	}

	public void setNodeName(String nodeName) {

		this.nodeName = nodeName;
	}

	public XMLNode getParent() {

		return parent;
	}

	public void setParent(XMLNode parent) {

		this.parent = parent;
	}
}
