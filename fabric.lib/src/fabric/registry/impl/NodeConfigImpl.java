/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.NodeConfig;
import fabric.registry.exception.IncompleteObjectException;

/**
 * Implementation class for Fabric <code>NodeConfig</code>.
 */
public class NodeConfigImpl extends DefaultConfigImpl implements NodeConfig {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	private String node = null;

	protected NodeConfigImpl() {

	}

	protected NodeConfigImpl(String node, String name, String value) {

		super(name, value);
		this.node = node;
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.NodeConfig#getNode()
	 */
	@Override
	public String getNode() {

		return node;
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.NodeConfig#setNode(java.lang.String)
	 */
	@Override
	public void setNode(String node) {

		this.node = node;

	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.RegistryObject#validate()
	 */
	@Override
	public void validate() throws IncompleteObjectException {

		super.validate();

		if (node == null || node.length() == 0) {
			throw new IncompleteObjectException("Node not specified.");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		StringBuffer buffy = new StringBuffer("NodeConfig::");
		buffy.append(" Node: ").append(node);
		buffy.append(" Name: ").append(getName());
		buffy.append(", Value: ").append(getValue());
		return buffy.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		boolean isEqual = false;

		if (obj instanceof NodeConfigImpl) {

			NodeConfigImpl dc = (NodeConfigImpl) obj;

			if (dc.getNode() == null ? node == null : dc.getNode().equals(node) && super.equals(obj)) {

				isEqual = true;
			}
		}

		return isEqual;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return toString().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.RegistryObject#key()
	 */
	@Override
	public String key() {

		return new StringBuffer(this.getNode()).append('/').append(this.getName()).toString();
	}

}
