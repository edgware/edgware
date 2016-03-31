/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.TaskPlugin;
import fabric.registry.exception.IncompleteObjectException;

/**
 * Implementation class for a registered <code>TaskPlugin</code>.
 */
public class TaskPluginImpl extends AbstractRegistryObject implements TaskPlugin {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	private String nodeId = null;
	private String missionId = null;
	private int ordinal = 0;
	private String pluginType = "INBOUND";
	private String name = null;
	private String description = null;
	private String arguments = null;
	private String platformId = null;
	private String systemId = null;
	private String feedId = null;
	private String family = null;

	protected TaskPluginImpl() {

	}

	protected TaskPluginImpl(String nodeId, String taskId, String name, String family, String pluginType, int ordinal,
			String description, String arguments, String platformId, String systemId, String feedId) {
		this.nodeId = nodeId;
		this.missionId = taskId;
		this.name = name;
		this.family = family;
		this.pluginType = pluginType;
		this.ordinal = ordinal;
		this.description = description;
		this.arguments = arguments;
		this.platformId = platformId;
		this.systemId = systemId;
		this.feedId = feedId;
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
	public String getFeedId() {
		return feedId;
	}

	@Override
	public void setFeedId(String feedId) {
		this.feedId = feedId;
	}

	@Override
	public String getTaskId() {
		return missionId;
	}

	@Override
	public void setTaskId(String missionId) {
		this.missionId = missionId;
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
	public int getOrdinal() {
		return ordinal;
	}

	@Override
	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}

	@Override
	public String getPlatformId() {
		return platformId;
	}

	@Override
	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}

	@Override
	public String getSensorId() {
		return systemId;
	}

	@Override
	public void setSensorId(String systemId) {
		this.systemId = systemId;
	}

	@Override
	public void validate() throws IncompleteObjectException {
		if (nodeId == null || nodeId.length() == 0 || missionId == null || missionId.length() == 0 || name == null
				|| name.length() == 0 || family == null || family.length() == 0
				|| (!pluginType.equalsIgnoreCase("INBOUND") && !pluginType.equalsIgnoreCase("OUTBOUND"))) {

			throw new IncompleteObjectException(
					"Missing nodeId, missionId, plugin class name and/or family. Type must also be specified as either 'INBOUND' or 'OUTBOUND' (defaults to 'INBOUND' if left null).");
		}
	}

	@Override
	public String toString() {
		StringBuffer buffy = new StringBuffer("MissionPlugin::");
		buffy.append(" Node ID: ").append(nodeId);
		buffy.append(", Task ID: ").append(missionId);
		buffy.append(", Name: ").append(name);
		buffy.append(", Family: ").append(family);
		buffy.append(", Plugin Type: ").append(pluginType);
		buffy.append(", Ordinal: ").append(ordinal);
		buffy.append(", Description: ").append(description);
		buffy.append(", Arguments: ").append(arguments);
		buffy.append(", Platform ID: ").append(platformId);
		buffy.append(", Sensor ID: ").append(systemId);
		buffy.append(", Feed Type ID: ").append(feedId);
		return buffy.toString();
	}

	@Override
	public boolean equals(Object obj) {
		boolean equal = false;
		if (obj != null && obj instanceof TaskPlugin) {
			TaskPlugin taskPlugin = (TaskPlugin) obj;
			if (taskPlugin.getNodeId().equals(nodeId) && taskPlugin.getTaskId().equalsIgnoreCase(missionId)
					&& taskPlugin.getName().equalsIgnoreCase(name) && taskPlugin.getFamilyName() == null ? family == null
					: taskPlugin.getFamilyName().equalsIgnoreCase(family)
							&& taskPlugin.getPluginType().equals(pluginType) && taskPlugin.getOrdinal() == ordinal
							&& taskPlugin.getDescription() == null ? description == null : taskPlugin.getDescription()
							.equalsIgnoreCase(description)
							&& taskPlugin.getArguments() == null ? arguments == null : taskPlugin.getArguments()
							.equalsIgnoreCase(arguments)
							&& taskPlugin.getPlatformId() == null ? platformId == null : taskPlugin.getPlatformId()
							.equalsIgnoreCase(platformId)
							&& taskPlugin.getSensorId() == null ? systemId == null : taskPlugin.getSensorId()
							.equalsIgnoreCase(systemId)
							&& taskPlugin.getFeedId() == null ? feedId == null : taskPlugin.getFeedId()
							.equalsIgnoreCase(feedId)) {

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
		return new StringBuffer(this.getNodeId()).append("/").append(this.getTaskId()).append("/").append(
				this.getName()).append("/").append(this.getFamilyName()).append("/").append(this.getPluginType())
				.append("/").append(this.getOrdinal()).append("/").append(this.getPlatformId()).append("/").append(
						this.getSensorId()).append("/").append(this.getFeedId()).toString();
	}
}
