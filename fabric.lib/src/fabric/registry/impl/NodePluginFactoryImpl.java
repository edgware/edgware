/*
 * (C) Copyright IBM Corp. 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.FabricPlugin;
import fabric.registry.FabricRegistry;
import fabric.registry.NodePlugin;
import fabric.registry.NodePluginFactory;
import fabric.registry.RegistryObject;
import fabric.registry.QueryScope;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.exception.RegistryQueryException;
import fabric.registry.persistence.IPersistenceResultRow;

public class NodePluginFactoryImpl extends AbstractFactory implements NodePluginFactory {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    /** Factory for local (singleton) Registry operations */
    private static NodePluginFactoryImpl localQueryInstance = null;
    /** Factory for remote (distributed) Registry operations */
    private static NodePluginFactoryImpl remoteQueryInstance = null;

    /** Select all records */
    private String SELECT_ALL_QUERY_NODE_PLUGINS = null;

    /** Select all records for a particular node */
    private String BY_NODE_QUERY_NODE_PLUGINS = null;

    /** Select records using an arbitrary WHERE clause */
    private String PREDICATE_QUERY_NODE_PLUGINS = null;

    /*
     * Static initialisation
     */

    static {
        localQueryInstance = new NodePluginFactoryImpl(QueryScope.LOCAL);
        remoteQueryInstance = new NodePluginFactoryImpl(QueryScope.DISTRIBUTED);
    }

    public static NodePluginFactoryImpl getInstance(QueryScope queryScope) {
        if (queryScope == QueryScope.LOCAL) {
            return localQueryInstance;
        } else {
            return remoteQueryInstance;
        }
    }

    private NodePluginFactoryImpl(QueryScope queryScope) {
        this.queryScope = queryScope;

        SELECT_ALL_QUERY_NODE_PLUGINS = format("select * from %s", FabricRegistry.NODE_PLUGINS);

        BY_NODE_QUERY_NODE_PLUGINS = format("select * from %s where NODE_ID='\\%s'", FabricRegistry.NODE_PLUGINS);

        PREDICATE_QUERY_NODE_PLUGINS = format("select * from %s where \\%s", FabricRegistry.NODE_PLUGINS);
    }

    @Override
    public String getUpdateSql(RegistryObject obj) {
        StringBuffer buf = new StringBuffer();
        if (obj instanceof NodePlugin) {
            NodePlugin plugin = (NodePlugin) obj;
            buf.append("update ");
            buf.append(FabricRegistry.NODE_PLUGINS);
            buf.append(" set ");
            buf.append("DESCRIPTION=").append(nullOrString(plugin.getDescription())).append(",");
            buf.append("ARGUMENTS=").append(nullOrString(plugin.getArguments()));
            buf.append(" WHERE ");

            /* if it exists, use the shadow values for the WHERE clause */
            if (plugin.getShadow() != null) {
                NodePlugin shadow = (NodePlugin) plugin.getShadow();
                buf.append("NODE_ID='").append(shadow.getNodeId()).append("' AND ");
                buf.append("ORDINAL=").append(shadow.getOrdinal()).append(" AND ");
                buf.append("TYPE='").append(shadow.getPluginType()).append("'");
            } else {
                buf.append("NODE_ID='").append(plugin.getNodeId()).append("' AND ");
                buf.append("ORDINAL=").append(plugin.getOrdinal()).append(" AND ");
                buf.append("TYPE='").append(plugin.getPluginType()).append("'");
            }
        }
        return buf.toString();
    }

    @Override
    public String getInsertSql(RegistryObject obj) {
        StringBuffer buf = new StringBuffer();
        if (obj instanceof NodePlugin) {
            NodePlugin plugin = (NodePlugin) obj;
            buf.append("insert into ");
            buf.append(FabricRegistry.NODE_PLUGINS);
            buf.append(" values(");
            buf.append("'").append(plugin.getNodeId()).append("',");
            buf.append("'").append(plugin.getName()).append("',");
            buf.append(nullOrString(plugin.getFamilyName())).append(",");
            buf.append("'").append(plugin.getPluginType()).append("',");
            buf.append(plugin.getOrdinal()).append(",");
            buf.append(nullOrString(plugin.getDescription())).append(",");
            buf.append(nullOrString(plugin.getArguments())).append(")");
        }
        return buf.toString();
    }

    @Override
    public RegistryObject create(IPersistenceResultRow row) throws PersistenceException {
        NodePlugin plugin = null;
        if (row != null) {
            NodePluginImpl impl = new NodePluginImpl();
            impl.setNodeId(row.getString(1));
            impl.setName(row.getString(2));
            impl.setFamily(row.getString(3));
            impl.setPluginType(row.getString(4));
            impl.setOrdinal(row.getInt(5));
            impl.setDescription(row.getString(6));
            impl.setArguments(row.getString(7));

            /* preserve these values internally */
            impl.createShadow();

            plugin = impl;
        }
        return plugin;
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.PluginFactory#createNodePlugin(java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String, int, java.lang.String, java.lang.String)
     */
    @Override
    public NodePlugin createNodePlugin(String nodeId, String name, String family, String pluginType, int ordinal,
            String description, String arguments) {

        return new NodePluginImpl(nodeId, name, family, pluginType, ordinal, description, arguments);
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.PluginFactory#getAllNodePlugins()
     */
    @Override
    public NodePlugin[] getAllNodePlugins() {
        NodePlugin[] plugins = null;
        try {
            plugins = runNodePluginQuery(SELECT_ALL_QUERY_NODE_PLUGINS);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return plugins;
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.PluginFactory#getNodePlugins(java.lang.String)
     */
    @Override
    public NodePlugin[] getNodePlugins(String predicateQuery) throws RegistryQueryException {

        NodePlugin[] plugins = null;
        try {
            String query = format(PREDICATE_QUERY_NODE_PLUGINS, predicateQuery);
            plugins = runNodePluginQuery(query);
        } catch (PersistenceException e) {
            e.printStackTrace();
            throw new RegistryQueryException(e.getMessage());
        }
        return plugins;
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.PluginFactory#getNodePluginsByNode(java.lang.String)
     */
    @Override
    public NodePlugin[] getNodePluginsByNode(String id) {
        NodePlugin[] plugins = null;
        try {
            String query = format(BY_NODE_QUERY_NODE_PLUGINS, id);
            plugins = runNodePluginQuery(query);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return plugins;
    }

    private NodePlugin[] runNodePluginQuery(String sql) throws PersistenceException {
        NodePlugin[] plugins = null;
        RegistryObject[] objects = queryRegistryObjects(sql, this);
        if (objects != null && objects.length > 0) {
            // necessary
            plugins = new NodePlugin[objects.length];
            for (int k = 0; k < objects.length; k++) {
                plugins[k] = (NodePlugin) objects[k];
            }
        } else {
            plugins = new NodePlugin[0];
        }
        return plugins;
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.Factory#delete(fabric.registry.RegistryObject)
     */
    @Override
    public boolean delete(RegistryObject obj) {
        if (obj != null && obj instanceof FabricPlugin) {
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
        if (obj != null && obj instanceof FabricPlugin) {
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

        if (obj != null && obj instanceof FabricPlugin) {
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

        if (obj != null && obj instanceof FabricPlugin) {
            return super.update(obj, this);
        } else {
            return false;
        }
    }

    @Override
    public String getDeleteSql(RegistryObject obj) {

        StringBuffer buf = new StringBuffer();

        NodePlugin plugin = (NodePlugin) obj;
        buf.append("DELETE FROM " + FabricRegistry.NODE_PLUGINS + " WHERE NODE_ID = '");
        buf.append(plugin.getNodeId());
        buf.append("' AND NAME = '");
        buf.append(plugin.getName());
        buf.append("' AND FAMILY = '");
        buf.append(plugin.getFamilyName());
        buf.append("' AND TYPE = '");
        buf.append(plugin.getPluginType());
        buf.append("' AND ORDINAL = ");
        buf.append(plugin.getOrdinal());
        buf.append(" AND DESCRIPTION = '");
        buf.append(plugin.getDescription());
        buf.append("'");
        return buf.toString();
    }

}
