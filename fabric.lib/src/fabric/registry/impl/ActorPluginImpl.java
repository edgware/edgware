/*
 * (C) Copyright IBM Corp. 2009
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.ActorPlugin;
import fabric.registry.exception.IncompleteObjectException;

/**
 * Implementation of the Fabric <code>ActorPlugin</code>.
 */
public class ActorPluginImpl extends AbstractRegistryObject implements ActorPlugin {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

    private String nodeId = null;
    private String missionId = null;
    private String clientId = null;
    private int ordinal = 0;
    private String pluginType = "INBOUND";
    private String name = null;
    private String description = null;
    private String arguments = null;
    private String platformId = null;
    private String systemId = null;
    private String feedId = null;
    private String family = null;

    protected ActorPluginImpl() {

    }

    protected ActorPluginImpl(String nodeId, String missionId, String clientId, String name, String family,
            String pluginType, int ordinal, String description, String arguments, String platformId, String systemId,
            String feedId) {
        this.nodeId = nodeId;
        this.missionId = missionId;
        this.clientId = clientId;
        this.ordinal = ordinal;
        this.pluginType = pluginType;
        this.name = name;
        this.description = description;
        this.arguments = arguments;
        this.platformId = platformId;
        this.systemId = systemId;
        this.feedId = feedId;
        this.family = family;
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
    public String getActorId() {
        return clientId;
    }

    @Override
    public void setActorId(String clientId) {
        this.clientId = clientId;
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
    public boolean isValid() {
        try {
            this.validate();
            return true;
        } catch (IncompleteObjectException e) {
            return false;
        }
    }

    @Override
    public void validate() throws IncompleteObjectException {
        if (nodeId == null
                || nodeId.length() == 0
                || missionId == null
                || missionId.length() == 0
                || clientId == null
                || clientId.length() == 0
                || name == null
                || name.length() == 0
                || (pluginType == null || (!pluginType.equalsIgnoreCase("INBOUND") && !pluginType
                        .equalsIgnoreCase("OUTBOUND")))) {

            throw new IncompleteObjectException(
                    "Missing nodeId, clientId and/or plugin class name. Type must also be specified as either 'INBOUND' or 'OUTBOUND'.");
        }
    }

    @Override
    public String toString() {
        StringBuilder buffy = new StringBuilder("ClientPlugin::");
        buffy.append(" Node ID: ").append(nodeId);
        buffy.append(", Mission ID: ").append(missionId);
        buffy.append(", Client ID: ").append(clientId);
        buffy.append(", Ordinal: ").append(ordinal);
        buffy.append(", Plugin Type: ").append(pluginType);
        buffy.append(", Name: ").append(name);
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
        if (obj != null && obj instanceof ActorPlugin) {
            ActorPlugin clientPlugin = (ActorPlugin) obj;
            if (clientPlugin.getNodeId().equals(nodeId) && clientPlugin.getTaskId().equalsIgnoreCase(missionId)
                    && clientPlugin.getActorId().equalsIgnoreCase(clientId) && clientPlugin.getOrdinal() == ordinal
                    && clientPlugin.getPluginType() == null ? pluginType == null : clientPlugin.getPluginType().equals(
                    pluginType)
                    && clientPlugin.getName() == null ? name == null : clientPlugin.getName().equalsIgnoreCase(name)
                    && clientPlugin.getDescription() == null ? description == null : clientPlugin.getDescription()
                    .equalsIgnoreCase(description)
                    && clientPlugin.getArguments() == null ? arguments == null : clientPlugin.getArguments()
                    .equalsIgnoreCase(arguments)
                    && clientPlugin.getPlatformId() == null ? platformId == null : clientPlugin.getPlatformId()
                    .equalsIgnoreCase(platformId)
                    && clientPlugin.getSensorId() == null ? systemId == null : clientPlugin.getSensorId()
                    .equalsIgnoreCase(systemId)
                    && clientPlugin.getFeedId() == null ? feedId == null : clientPlugin.getFeedId().equalsIgnoreCase(
                    feedId)) {

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

        return new StringBuilder(this.getNodeId()).append('/').append(this.getTaskId()).append('/').append(
                this.getActorId()).append('/').append(this.getName()).append('/').append(this.getFamilyName()).append(
                '/').append(this.getPluginType()).append('/').append(this.getOrdinal()).append('/').append(
                this.getPlatformId()).append('/').append(this.getSensorId()).append('/').append(this.getFeedId())
                .toString();
    }

}
