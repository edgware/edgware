/*
 * (C) Copyright IBM Corp. 2009
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.NodeIpMapping;
import fabric.registry.NodeNeighbour;
import fabric.registry.QueryScope;
import fabric.registry.exception.IncompleteObjectException;

/**
 * Implementation class for a Fabric node neighbour.
 */
public class NodeNeighbourImpl extends AbstractRegistryObject implements NodeNeighbour {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

    private String nodeId = null;
    private String nodeInterface = null;
    private String neighbourId = null;
    private String neighbourInterface = null;
    private String discoveredBy = null;
    private String availability = null;
    private String bearerId = null;
    private String connectionAttributes = null;
    private String connectionAttributesUri = null;

    protected NodeNeighbourImpl() {
    }

    protected NodeNeighbourImpl(String nodeId, String nodeInterface, String neighbourId, String neighbourInterface,
            String discoveredBy, String availability, String bearerId, String connectionAttributes, String uri) {
        this.nodeId = nodeId;
        this.nodeInterface = nodeInterface;
        this.neighbourId = neighbourId;
        this.neighbourInterface = neighbourInterface;
        this.discoveredBy = discoveredBy;
        this.availability = availability;
        this.bearerId = bearerId;
        this.connectionAttributes = connectionAttributes;
        this.connectionAttributesUri = uri;
    }

    @Override
    public String getConnectionAttributes() {
        return connectionAttributes;
    }

    @Override
    public void setConnectionAttributes(String connectionAttributes) {
        this.connectionAttributes = connectionAttributes;
    }

    @Override
    public String getConnectionAttributesUri() {
        return connectionAttributesUri;
    }

    @Override
    public void setConnectionAttributesUri(String connectionAttributesUri) {
        this.connectionAttributesUri = connectionAttributesUri;
    }

    @Override
    public String getNeighbourId() {
        return neighbourId;
    }

    @Override
    public void setNeighbourId(String neighbourId) {
        this.neighbourId = neighbourId;
    }

    @Override
    public String getNeighbourInterface() {
        return neighbourInterface;
    }

    @Override
    public void setNeighbourInterface(String neighbourInterface) {
        this.neighbourInterface = neighbourInterface;
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
    public String getNodeInterface() {
        return nodeInterface;
    }

    @Override
    public void setNodeInterface(String nodeInterface) {
        this.nodeInterface = nodeInterface;
    }

    @Override
    public String getDiscoveredBy() {
        return discoveredBy;
    }

    @Override
    public void setDiscoveredBy(String discoveredBy) {
        this.discoveredBy = discoveredBy;
    }

    @Override
    public String getAvailability() {
        return availability;
    }

    @Override
    public void setAvailability(String availability) {
        this.availability = availability;
    }

    @Override
    public String getBearerId() {
        return bearerId;
    }

    @Override
    public void setBearerId(String bearerId) {
        this.bearerId = bearerId;
    }

    @Override
    public void validate() throws IncompleteObjectException {
        if (nodeId == null || nodeId.length() == 0 || neighbourInterface == null || neighbourInterface.length() == 0
                || neighbourId == null || neighbourId.length() == 0 || neighbourInterface == null
                || neighbourInterface.length() == 0) {
            throw new IncompleteObjectException("Missing node and/or neighbour id or interface.");
        }
    }

    @Override
    public String toString() {
        StringBuffer buffy = new StringBuffer("NodeNeighbour::");
        buffy.append(" Node ID: ").append(nodeId);
        buffy.append(" Node Interface: ").append(nodeInterface);
        buffy.append(" Neighbour ID: ").append(neighbourId);
        buffy.append(" Neighbour Interface: ").append(neighbourInterface);
        buffy.append(", Connection Attributes: ").append(connectionAttributes);
        buffy.append(", Connection Attributes URI: ").append(connectionAttributesUri);
        return buffy.toString();
    }

    @Override
    public boolean equals(Object obj) {
        boolean equal = false;
        if (obj != null && obj instanceof NodeNeighbour) {
            NodeNeighbour neighbour = (NodeNeighbour) obj;
            if (neighbour.getNodeId().equals(nodeId) && neighbour.getNodeInterface() == null ? nodeInterface == null
                    : neighbour.getNodeInterface().equals(nodeInterface) && neighbour.getNeighbourId() == null ? neighbourId == null
                            : neighbour.getNeighbourId().equals(neighbourId)
                                    && neighbour.getNeighbourInterface() == null ? neighbourInterface == null
                                    : neighbour.getNeighbourInterface().equals(neighbourInterface)
                                            && neighbour.getDiscoveredBy() == null ? discoveredBy == null
                                            : neighbour.getDiscoveredBy().equals(discoveredBy)
                                                    && neighbour.getAvailability() == null ? availability == null
                                                    : neighbour.getAvailability().equals(availability)
                                                            && neighbour.getBearerId() == null ? bearerId == null
                                                            : neighbour.getBearerId().equals(bearerId)
                                                                    && neighbour.getConnectionAttributes() == null ? connectionAttributes == null
                                                                    : neighbour.getConnectionAttributes().equals(
                                                                            connectionAttributes)
                                                                            && neighbour.getConnectionAttributesUri() == null ? connectionAttributesUri == null
                                                                            : neighbour.getConnectionAttributesUri()
                                                                                    .equals(connectionAttributesUri)) {

                equal = true;
            }
        }
        return equal;
    }

    @Override
    public String key() {

        return new StringBuffer(this.getNodeId()).append("/").append(this.getNodeInterface()).append("/").append(
                this.getNeighbourId()).append("/").append(this.getNeighbourInterface()).toString();
    }

    @Override
    public NodeIpMapping getIpMappingForNeighbour() {
        return NodeIpMappingFactoryImpl.getInstance(QueryScope.LOCAL).getMappingForNode(neighbourId,
                neighbourInterface);
    }

}
