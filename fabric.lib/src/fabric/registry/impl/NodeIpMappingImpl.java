/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.core.properties.ConfigProperties;
import fabric.registry.NodeIpMapping;
import fabric.registry.exception.IncompleteObjectException;

/**
 * Implementation class for a Fabric <code>Node</code>'s IP address.
 */
public class NodeIpMappingImpl extends AbstractRegistryObject implements NodeIpMapping {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	private String nodeId = null;
	private String nodeInterface = null;
	private String ipAddress = null;
	private int port = Integer.parseInt(ConfigProperties.MQTT_REMOTE_PORT_DEFAULT);

	protected NodeIpMappingImpl() {
	}

	protected NodeIpMappingImpl(String nodeId, String nodeInterface, String ipAddress, int port) {
		this.nodeId = nodeId;
		this.nodeInterface = nodeInterface;
		this.ipAddress = ipAddress;
		this.port = port;
	}

	@Override
	public String getIpAddress() {
		return ipAddress;
	}

	@Override
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	@Override
	public String getNodeId() {
		return nodeId;
	}

	@Override
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	@Override
	public void setNodeInterface(String nodeInterface) {

		this.nodeInterface = nodeInterface;
	}

	@Override
	public String getNodeInterface() {

		return nodeInterface;
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public void validate() throws IncompleteObjectException {
		if (nodeId == null || nodeId.length() == 0 || nodeInterface == null || nodeInterface.length() == 0
				|| ipAddress == null || ipAddress.length() == 0) {
			throw new IncompleteObjectException("Missing node name and/or node interface and/or ip address.");
		}
	}

	@Override
	public String toString() {
		StringBuffer buffy = new StringBuffer("Node IP Mapping::");
		buffy.append(" Node ID: ").append(nodeId);
		buffy.append(" Node Interface: ").append(nodeInterface);
		buffy.append(", IP address: ").append(ipAddress);
		buffy.append(", Port: ").append(port);
		return buffy.toString();
	}

	@Override
	public boolean equals(Object obj) {
		boolean equal = false;
		if (obj != null && obj instanceof NodeIpMapping) {
			NodeIpMapping ipMapping = (NodeIpMapping) obj;
			if (ipMapping.getNodeId().equals(nodeId)) {
				if ((ipMapping.getNodeInterface() == null && nodeInterface == null)
						|| (ipMapping.getNodeInterface() != null && ipMapping.getNodeInterface().equals(nodeInterface))) {
					if ((ipMapping.getIpAddress() == null && ipAddress == null)
							|| (ipMapping.getIpAddress() != null && ipMapping.getIpAddress().equals(ipAddress))) {
						if (ipMapping.getPort() == port) {
							equal = true;
						}
					}
				}
			}
		}
		return equal;
	}

	@Override
	public String key() {
		return this.getNodeId();
	}

}
