/*
 * (C) Copyright IBM Corp. 2009
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.FabricPlugin;
import fabric.registry.exception.IncompleteObjectException;

/**
 * Implementation of the class representing a registered <code>Fablet</code>.
 */
public class FabricPluginImpl extends AbstractRegistryObject implements FabricPlugin {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

    private String nodeId = null;
    private String name = null;
    private String family = null;
    private String description = null;
    private String arguments = null;

    protected FabricPluginImpl() {

    }

    protected FabricPluginImpl(String nodeId, String name, String family, String description, String arguments) {
        this.nodeId = nodeId;
        this.name = name;
        this.family = family;
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
    public String getFamilyName() {
        return family;
    }

    @Override
    public void setFamily(String family) {
        this.family = family;
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
                || family.length() == 0) {

            throw new IncompleteObjectException("Missing nodeId and/or plugin class name and/or family name.");
        }
    }

    @Override
    public String toString() {
        StringBuilder buffy = new StringBuilder("FabricPlugin::");
        buffy.append(" Node ID: ").append(nodeId);
        buffy.append(", Name: ").append(name);
        buffy.append(", Family: ").append(family);
        buffy.append(", Description: ").append(description);
        buffy.append(", Arguments: ").append(arguments);
        return buffy.toString();
    }

    @Override
    public boolean equals(Object obj) {
        boolean equal = false;
        if (obj != null && obj instanceof FabricPlugin) {
            FabricPlugin fabricPlugin = (FabricPlugin) obj;
            if (fabricPlugin.getNodeId().equals(nodeId) && fabricPlugin.getName().equalsIgnoreCase(name)
                    && fabricPlugin.getFamilyName() == null ? family == null : fabricPlugin.getFamilyName()
                    .equalsIgnoreCase(family)
                    && fabricPlugin.getDescription() == null ? description == null : fabricPlugin.getDescription()
                    .equalsIgnoreCase(description)
                    && fabricPlugin.getArguments() == null ? arguments == null : fabricPlugin.getArguments()
                    .equalsIgnoreCase(arguments)) {

                equal = true;
            }
        }
        return equal;
    }

    @Override
    public String key() {

        return new StringBuilder(this.getNodeId()).append('/').append(this.getName()).append('/').append(
                this.getFamilyName()).toString();
    }

}
