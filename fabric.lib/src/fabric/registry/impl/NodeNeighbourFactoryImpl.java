/*
 * (C) Copyright IBM Corp. 2009
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.FabricRegistry;
import fabric.registry.NodeNeighbour;
import fabric.registry.NodeNeighbourFactory;
import fabric.registry.RegistryObject;
import fabric.registry.QueryScope;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.exception.RegistryQueryException;
import fabric.registry.persistence.IPersistenceResultRow;
import fabric.registry.persistence.PersistenceManager;
import fabric.session.NodeDescriptor;

/**
 * Implementation of the factory for <code>NodeNeighbour</code>'s.
 *
 */
public class NodeNeighbourFactoryImpl extends AbstractFactory implements NodeNeighbourFactory {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

    /*
     * Class fields
     */

    /** Factory for local (singleton) Registry operations */
    private static NodeNeighbourFactoryImpl localQueryInstance = null;

    /** Factory for distributed Registry operations */
    private static NodeNeighbourFactoryImpl remoteQueryInstance = null;

    /* Query definitions */

    /** Select all records. */
    private String SELECT_ALL_QUERY = null;

    /** Select records matching a specified node ID. */
    private String UNIQUE_BY_ID_QUERY = null;

    /** Select records using an arbitrary SQL <code>WHERE</code> clause. */
    private String PREDICATE_QUERY = null;

    private String AVAILABLE_NEIGHBOURS_QUERY = null;

    /** Delete records matching a specified node ID */
    private static String DELETE_NEIGHBOURS_BY_NODE = null;

    /** SQL to mark all Static Neighbours as Available */
    private static String SET_STATIC_NEIGHBOURS_TO_AVAILABLE = null;

    /** SQL to Mark Neighbours Unavailable. */
    private static String MARK_UNAVAILABLE = null;

    /*
     * Class static initialization
     */

    static {

        /* Create an instance for local (singleton) Registry operations */
        localQueryInstance = new NodeNeighbourFactoryImpl(QueryScope.LOCAL);

        /* Create an instance for distributed (Gaian) Registry operations */
        remoteQueryInstance = new NodeNeighbourFactoryImpl(QueryScope.DISTRIBUTED);
    }

    /*
     * Class methods
     */

    protected NodeNeighbourFactoryImpl(QueryScope queryScope) {
        this.queryScope = queryScope;

        /* Build the predefined queries to reflect the selected tables */

        SELECT_ALL_QUERY = format("select * from %s", FabricRegistry.NODE_NEIGHBOURS);

        UNIQUE_BY_ID_QUERY = format("select * from %s where node_id='\\%s'", FabricRegistry.NODE_NEIGHBOURS);

        PREDICATE_QUERY = format("select * from %s where \\%s", FabricRegistry.NODE_NEIGHBOURS);

        AVAILABLE_NEIGHBOURS_QUERY = format(
                "select * from %s where NODE_ID='\\%s' and NEIGHBOUR_ID='\\%s' AND AVAILABILITY='"
                        + NodeNeighbour.AVAILABLE + "'", FabricRegistry.NODE_NEIGHBOURS);

        /** Delete records matching a specified node ID */
        DELETE_NEIGHBOURS_BY_NODE = "delete from " + FabricRegistry.NODE_NEIGHBOURS + " where NODE_ID='\\%s'";

        SET_STATIC_NEIGHBOURS_TO_AVAILABLE = format("UPDATE %s SET AVAILABILITY='" + NodeNeighbour.AVAILABLE
                + "' WHERE DISCOVEREDBY='" + NodeNeighbour.DISCOVEREDBY_STATIC + "' AND NODE_ID='\\%s'",
                FabricRegistry.NODE_NEIGHBOURS);

        MARK_UNAVAILABLE = format("UPDATE %s SET AVAILABILITY='" + NodeNeighbour.UNAVAILABLE
                + "' WHERE NODE_ID='\\%s' AND NEIGHBOUR_ID='\\%s' AND NEIGHBOUR_INTERFACE='\\%s' ",
                FabricRegistry.NODE_NEIGHBOURS);

    }

    public static NodeNeighbourFactoryImpl getInstance(QueryScope queryScope) {
        if (queryScope == QueryScope.LOCAL) {
            return localQueryInstance;
        } else {
            return remoteQueryInstance;
        }
    }

    @Override
    public RegistryObject create(IPersistenceResultRow row) throws PersistenceException {
        NodeNeighbour neighbour = null;
        if (row != null) {
            NodeNeighbourImpl impl = new NodeNeighbourImpl();
            impl.setNodeId(row.getString(1));
            impl.setNodeInterface(row.getString(2));
            impl.setNeighbourId(row.getString(3));
            impl.setNeighbourInterface(row.getString(4));
            impl.setDiscoveredBy(row.getString(5));
            impl.setAvailability(row.getString(6));
            impl.setBearerId(row.getString(7));
            impl.setConnectionAttributes(row.getString(8));
            impl.setConnectionAttributesUri(row.getString(9));

            /* preserve these values internally */
            impl.createShadow();

            neighbour = impl;
        }
        return neighbour;
    }

    @Override
    public NodeNeighbour createNodeNeighbour(String nodeId, String nodeInterface, String neighbourId,
            String neighbourInterface, String discoveredBy, String availability, String bearerId,
            String connectionAttributes, String uri) {
        return new NodeNeighbourImpl(nodeId, nodeInterface, neighbourId, neighbourInterface, discoveredBy,
                availability, bearerId, connectionAttributes, uri);
    }

    @Override
    public String getDeleteSql(RegistryObject obj) {
        StringBuffer buf = new StringBuffer();
        if (obj instanceof NodeNeighbour) {
            NodeNeighbour neighbour = (NodeNeighbour) obj;
            buf.append("delete from " + FabricRegistry.NODE_NEIGHBOURS + " where(");
            buf.append("NODE_ID='").append(neighbour.getNodeId()).append("' AND ");
            buf.append("NODE_INTERFACE='").append(neighbour.getNodeInterface()).append("' AND ");
            buf.append("NEIGHBOUR_ID='").append(neighbour.getNeighbourId()).append("' AND ");
            buf.append("NEIGHBOUR_INTERFACE='").append(neighbour.getNeighbourInterface()).append("')");
        }
        return buf.toString();
    }

    @Override
    public String getInsertSql(RegistryObject obj) {
        StringBuffer buf = new StringBuffer();
        if (obj instanceof NodeNeighbour) {
            NodeNeighbour neighbour = (NodeNeighbour) obj;
            buf.append("insert into " + FabricRegistry.NODE_NEIGHBOURS + " values(");
            buf.append("'").append(neighbour.getNodeId()).append("',");
            buf.append("'").append(neighbour.getNodeInterface()).append("',");
            buf.append("'").append(neighbour.getNeighbourId()).append("',");
            buf.append("'").append(neighbour.getNeighbourInterface()).append("',");
            buf.append("'").append(neighbour.getDiscoveredBy()).append("',");
            buf.append("'").append(neighbour.getAvailability()).append("',");
            buf.append("'").append(neighbour.getBearerId()).append("',");
            buf.append("'").append(neighbour.getConnectionAttributes()).append("',");
            buf.append("'").append(neighbour.getConnectionAttributesUri()).append("')");
        }
        return buf.toString();
    }

    @Override
    public String getUpdateSql(RegistryObject obj) {
        StringBuffer buf = new StringBuffer();
        if (obj instanceof NodeNeighbour) {
            NodeNeighbour neighbour = (NodeNeighbour) obj;
            buf.append("update " + FabricRegistry.NODE_NEIGHBOURS + " set ");
            buf.append("DISCOVEREDBY='").append(neighbour.getDiscoveredBy()).append("',");
            buf.append("AVAILABILITY='").append(neighbour.getAvailability()).append("',");
            buf.append("BEARER_ID='").append(neighbour.getBearerId()).append("',");
            buf.append("CONNECTION_ATTRIBUTES='").append(neighbour.getConnectionAttributes()).append("',");
            buf.append("CONNECTION_ATTRIBUTES_URI='").append(neighbour.getConnectionAttributesUri()).append("'");
            buf.append(" WHERE");

            /* if it exists, use the shadow values for the WHERE clause */
            if (neighbour.getShadow() != null) {
                NodeNeighbour shadow = (NodeNeighbour) neighbour.getShadow();
                buf.append(" NODE_ID='").append(shadow.getNodeId()).append("' AND");
                buf.append(" NODE_INTERFACE='").append(shadow.getNodeInterface()).append("' AND");
                buf.append(" NEIGHBOUR_ID='").append(shadow.getNeighbourId()).append("' AND");
                buf.append(" NEIGHBOUR_INTERFACE='").append(shadow.getNeighbourInterface()).append("'");
            } else {
                buf.append(" NODE_ID='").append(neighbour.getNodeId()).append("' AND");
                buf.append(" NODE_INTERFACE='").append(neighbour.getNodeInterface()).append("' AND");
                buf.append(" NEIGHBOUR_ID='").append(neighbour.getNeighbourId()).append("' AND");
                buf.append(" NEIGHBOUR_INTERFACE='").append(neighbour.getNeighbourInterface()).append("'");
            }
        }
        return buf.toString();
    }

    private NodeNeighbour[] runQuery(String sql) throws PersistenceException {
        NodeNeighbour[] neighbours = null;
        RegistryObject[] objects = queryRegistryObjects(sql, this);
        if (objects != null && objects.length > 0) {
            // necessary
            neighbours = new NodeNeighbour[objects.length];
            for (int k = 0; k < objects.length; k++) {
                neighbours[k] = (NodeNeighbour) objects[k];
            }
        } else {
            neighbours = new NodeNeighbour[0];
        }
        return neighbours;
    }

    @Override
    public NodeNeighbour[] getAllNeighbours() {
        NodeNeighbour[] neighbours = null;
        try {
            neighbours = runQuery(SELECT_ALL_QUERY);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return neighbours;
    }

    @Override
    public NodeNeighbour[] getUniqueNeighboursByNeighbourId(String nodeId) {
        NodeNeighbour[] neighbours = null;
        try {
            String query = format(UNIQUE_BY_ID_QUERY, nodeId);
            neighbours = runQuery(query);
        } catch (PersistenceException e) {
            e.printStackTrace();

        }
        return neighbours;
    }

    @Override
    public NodeNeighbour[] getNeighbours(String queryPredicates) throws RegistryQueryException {
        NodeNeighbour[] neighbours = null;
        try {
            String query = format(PREDICATE_QUERY, queryPredicates);
            neighbours = runQuery(query);
        } catch (PersistenceException e) {
            e.printStackTrace();
            throw new RegistryQueryException(e.getMessage());
        }
        return neighbours;
    }

    public boolean deleteNeighboursForNode(String nodeId) {
        String sql = format(DELETE_NEIGHBOURS_BY_NODE, nodeId);
        try {
            boolean success = PersistenceManager.getPersistence().updateRegistryObject(sql);
            return success;
        } catch (PersistenceException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.Factory#delete(fabric.registry.RegistryObject)
     */
    @Override
    public boolean delete(RegistryObject obj) {
        if (obj != null && obj instanceof NodeNeighbour) {
            return super.delete(obj, this);
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.Factory#save(fabric.registry.RegistryObject)
     */
    @Override
    public boolean save(RegistryObject obj) throws IncompleteObjectException {
        if (obj != null && obj instanceof NodeNeighbour) {
            return super.save(obj, this);
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.Factory#insert(fabric.registry.RegistryObject)
     */
    @Override
    public boolean insert(RegistryObject obj) throws IncompleteObjectException, DuplicateKeyException,
    PersistenceException {

        if (obj != null && obj instanceof NodeNeighbour) {
            return super.insert(obj, this);
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.Factory#update(fabric.registry.RegistryObject)
     */
    @Override
    public boolean update(RegistryObject obj) throws IncompleteObjectException, PersistenceException {

        if (obj != null && obj instanceof NodeNeighbour) {
            return super.update(obj, this);
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.NodeNeighbourFactory#getAvailableNeighboursEntries(java.lang.String, java.lang.String)
     */
    @Override
    public NodeNeighbour[] getAvailableNeighboursEntries(String nodeId, String neighbourId) {
        NodeNeighbour[] neighbours = null;
        try {
            String query = format(AVAILABLE_NEIGHBOURS_QUERY, nodeId, neighbourId);
            neighbours = runQuery(query);
        } catch (PersistenceException e) {
            e.printStackTrace();

        }
        return neighbours;

    }

    @Override
    public boolean markStaticNeighboursAsAvailable(String localNode) {
        String sql = format(SET_STATIC_NEIGHBOURS_TO_AVAILABLE, localNode);
        try {
            boolean success = PersistenceManager.getPersistence().updateRegistryObject(sql);
            return success;
        } catch (PersistenceException e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public boolean markUnavailable(String localNode, NodeDescriptor nodeDescriptor) {
        String sql = format(MARK_UNAVAILABLE, localNode, nodeDescriptor.name(), nodeDescriptor.networkInterface());
        try {
            boolean success = PersistenceManager.getPersistence().updateRegistryObject(sql);
            return success;
        } catch (PersistenceException e) {
            e.printStackTrace();
            return false;
        }

    }

}
