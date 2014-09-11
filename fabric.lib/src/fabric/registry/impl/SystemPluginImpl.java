/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.NodePlugin;
import fabric.registry.SystemPlugin;
import fabric.registry.exception.IncompleteObjectException;

/**
 * Implementation class for a registered <code>SystemPlugin</code>.
 */
public class SystemPluginImpl extends AbstractRegistryObject implements SystemPlugin {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	private String nodeId = null;
	private String name = null;
	private String family = null;
	private String pluginType = "INBOUND";
	private String description = null;
	private String arguments = null;

	protected SystemPluginImpl() {

	}

	protected SystemPluginImpl(String nodeId, String pluginType, String name, String description, String arguments,
			String family) {
		this.nodeId = nodeId;
		this.name = name;
		this.family = family;
		this.pluginType = pluginType;
		this.description = description;
		this.arguments = arguments;
	}

	@Override
	public String getArguments() {
		return arguments;
	}

	@Override
	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
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
	public void validate() throws IncompleteObjectException {
		if (nodeId == null || nodeId.length() == 0 || name == null || name.length() == 0 || family == null
				|| family.length() == 0
				|| (!pluginType.equalsIgnoreCase("INBOUND") && !pluginType.equalsIgnoreCase("OUTBOUND"))) {

			throw new IncompleteObjectException(
					"Missing nodeId, plugin class name or family. Type must also be specified as either 'INBOUND' or 'OUTBOUND' (defaults to 'INBOUND' if left null).");
		}
	}

	@Override
	public String toString() {
		StringBuffer buffy = new StringBuffer("NodePlugin::");
		buffy.append(" Node ID: ").append(nodeId);
		buffy.append(", Name: ").append(name);
		buffy.append(", Family: ").append(family);
		buffy.append(" Plugin Type: ").append(pluginType);
		buffy.append(", Description: ").append(description);
		buffy.append(", Arguments: ").append(arguments);
		return buffy.toString();
	}

	@Override
	public boolean equals(Object obj) {
		boolean equal = false;
		if (obj != null && obj instanceof NodePlugin) {
			NodePlugin plugin = (NodePlugin) obj;
			if (plugin.getNodeId().equals(nodeId) && plugin.getName().equalsIgnoreCase(name)
					&& plugin.getPluginType().equals(pluginType) && plugin.getFamilyName() == null ? family == null
					: plugin.getFamilyName().equalsIgnoreCase(family) && plugin.getDescription() == null ? description == null
							: plugin.getDescription().equalsIgnoreCase(description) && plugin.getArguments() == null ? arguments == null
									: plugin.getArguments().equalsIgnoreCase(arguments)) {

				equal = true;
			}
		}
		return equal;
	}

	@Override
	public String getPluginType() {
		return pluginType;
	}

	@Override
	public boolean isInbound() {
		if (pluginType.equalsIgnoreCase("INBOUND")) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isOutbound() {
		if (pluginType.equalsIgnoreCase("OUTBOUND")) {
			return true;
		}
		return false;
	}

	@Override
	public void setInbound(boolean inbound) {
		if (inbound) {
			pluginType = "INBOUND";
		} else {
			pluginType = "OUTBOUND";
		}
	}

	@Override
	public void setOutbound(boolean outbound) {
		if (outbound) {
			pluginType = "OUTBOUND";
		} else {
			pluginType = "INBOUND";
		}
	}

	@Override
	public void setPluginType(String pluginType) {
		this.pluginType = pluginType;
	}

	@Override
	public String getFamilyName() {
		return family;
	}

	@Override
	public void setFamily(String family) {
		this.family = family;
	}

	@Override
	public String key() {
		return new StringBuffer(this.getNodeId()).append("/").append(this.getName()).append("/").append(
				this.getFamilyName()).append("/").append(this.getPluginType()).toString();
	}

}
