/*
 * (C) Copyright IBM Corp. 2009
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.TaskNode;
import fabric.registry.exception.IncompleteObjectException;

/**
 * Implementation class for a <code>TaskNode</code>.
 */
public class TaskNodeImpl extends AbstractRegistryObject implements TaskNode {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

    private String taskId = null;
    private String nodeId = null;
    private String description = null;
    private String configuration = null;
    private String configurationUri = null;

    protected TaskNodeImpl() {
    }

    protected TaskNodeImpl(String taskId, String nodeId, String description, String configuration,
            String configurationUri) {
        this.taskId = taskId;
        this.nodeId = nodeId;
        this.description = description;
        this.configuration = configuration;
        this.configurationUri = configurationUri;
    }

    @Override
    public String getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    @Override
    public String getConfigurationUri() {
        return configurationUri;
    }

    @Override
    public void setConfigurationUri(String configurationUri) {
        this.configurationUri = configurationUri;
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
    public String getNodeId() {
        return nodeId;
    }

    @Override
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    @Override
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public void validate() throws IncompleteObjectException {
        if (taskId == null || taskId.length() == 0 || nodeId == null || nodeId.length() == 0) {
            throw new IncompleteObjectException("Missing or invalid task ID and/or node ID.");
        }
    }

    @Override
    public String toString() {
        StringBuilder buffy = new StringBuilder("TaskNode::");
        buffy.append(" Task ID: ").append(taskId);
        buffy.append(", Node ID: ").append(nodeId);
        buffy.append(", Description: ").append(description);
        buffy.append(", Configuration: ").append(configuration);
        buffy.append(", Configuration URI: ").append(configurationUri);
        return buffy.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof TaskNode) {
            TaskNode taskNode = (TaskNode) obj;
            if (taskNode.getTaskId().equals(taskId) && taskNode.getNodeId() == null ? nodeId == null : taskNode
                    .getNodeId().equals(nodeId)
                    && taskNode.getDescription() == null ? description == null : taskNode.getDescription().equals(
                    description)
                    && taskNode.getConfiguration() == null ? configuration == null : taskNode.getConfiguration()
                    .equals(configuration)
                    && taskNode.getConfigurationUri() == null ? configurationUri == null : taskNode
                    .getConfigurationUri().equals(configurationUri)) {

                return true;
            }
        }
        return false;
    }

    @Override
    public String key() {
        return new StringBuilder(this.getTaskId()).append('/').append(this.getNodeId()).toString();
    }
}
