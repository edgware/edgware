/*
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

/**
 * Represents a node configuration value in the Registry.
 */
public interface NodeConfig extends DefaultConfig {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/**
	 * Get the name of the node.
	 * 
	 * @return the node name.
	 */
	public String getNode();

	/**
	 * Set the name of the node.
	 * 
	 * @param name
	 *            the new node name.
	 */
	public void setNode(String node);
}
