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
import fabric.registry.QueryScope;
import fabric.registry.RegistryObject;
import fabric.registry.SystemPlugin;
import fabric.registry.SystemPluginFactory;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.exception.RegistryQueryException;
import fabric.registry.persistence.IPersistenceResultRow;

public class SystemPluginFactoryImpl extends AbstractFactory implements SystemPluginFactory {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    /** Factory for local (singleton) Registry operations */
    private static SystemPluginFactoryImpl localQueryInstance = null;
    /** Factory for remote (distributed) Registry operations */
    private static SystemPluginFactoryImpl remoteQueryInstance = null;

    /** Select all records */
    private String SELECT_ALL_QUERY_SYSTEM_PLUGINS = null;

    /** Select all records for a particular node */
    private String BY_NODE_QUERY_SYSTEM_PLUGINS = null;

    /** Select records using an arbitrary WHERE clause */
    private String PREDICATE_QUERY_SYSTEM_PLUGINS = null;

    /*
     * Static initialisation
     */

    static {
        localQueryInstance = new SystemPluginFactoryImpl(QueryScope.LOCAL);
        remoteQueryInstance = new SystemPluginFactoryImpl(QueryScope.DISTRIBUTED);
    }

    public static SystemPluginFactoryImpl getInstance(QueryScope queryScope) {
        if (queryScope == QueryScope.LOCAL) {
            return localQueryInstance;
        } else {
            return remoteQueryInstance;
        }
    }

    private SystemPluginFactoryImpl(QueryScope queryScope) {
        this.queryScope = queryScope;

        SELECT_ALL_QUERY_SYSTEM_PLUGINS = format("select * from %s", FabricRegistry.SYSTEM_PLUGINS);

        BY_NODE_QUERY_SYSTEM_PLUGINS = format("select * %s where NODE_ID='\\%s'", FabricRegistry.SYSTEM_PLUGINS);

        PREDICATE_QUERY_SYSTEM_PLUGINS = format("select * from %s where \\%s", FabricRegistry.SYSTEM_PLUGINS);
    }

    @Override
    public String getUpdateSql(RegistryObject obj) {
        StringBuilder buf = new StringBuilder();
        if (obj instanceof SystemPlugin) {
            NodePlugin plugin = (NodePlugin) obj;
            buf.append("update ");
            buf.append(FabricRegistry.SYSTEM_PLUGINS);
            buf.append(" set ");
            buf.append("DESCRIPTION=").append(nullOrString(plugin.getDescription())).append(',');
            buf.append("ARGUMENTS=").append(nullOrString(plugin.getArguments()));
            buf.append(" WHERE ");

            /* if it exists, use the shadow values for the WHERE clause */
            if (plugin.getShadow() != null) {
                NodePlugin shadow = (NodePlugin) plugin.getShadow();
                buf.append("NODE_ID='").append(shadow.getNodeId()).append("' AND ");
                buf.append("NAME='").append(shadow.getName()).append("' AND ");
                buf.append("FAMILY='").append(shadow.getFamilyName()).append("' AND ");
                buf.append("TYPE='").append(shadow.getPluginType()).append('\'');
            } else {
                buf.append("NODE_ID='").append(plugin.getNodeId()).append("' AND ");
                buf.append("NAME='").append(plugin.getName()).append("' AND ");
                buf.append("FAMILY='").append(plugin.getFamilyName()).append("' AND ");
                buf.append("TYPE='").append(plugin.getPluginType()).append('\'');
            }
        }
        return buf.toString();
    }

    @Override
    public String getInsertSql(RegistryObject obj) {

        StringBuilder buf = new StringBuilder();
        if (obj instanceof SystemPlugin) {
            SystemPlugin plugin = (SystemPlugin) obj;
            buf.append("insert into ");
            buf.append(FabricRegistry.SYSTEM_PLUGINS);
            buf.append(" values(");
            buf.append('\'').append(plugin.getNodeId()).append('\'').append(',');
            buf.append('\'').append(plugin.getName()).append('\'').append(',');
            buf.append(nullOrString(plugin.getFamilyName())).append(',');
            buf.append('\'').append(plugin.getPluginType()).append('\'').append(',');
            buf.append(nullOrString(plugin.getDescription())).append(',');
            buf.append(nullOrString(plugin.getArguments())).append(')');
        }
        return buf.toString();
    }

    @Override
    public RegistryObject create(IPersistenceResultRow row) throws PersistenceException {
        SystemPlugin plugin = null;
        if (row != null) {
            SystemPluginImpl impl = new SystemPluginImpl();
            impl.setNodeId(row.getString(1));
            impl.setName(row.getString(2));
            impl.setFamily(row.getString(3));
            impl.setPluginType(row.getString(4));
            impl.setDescription(row.getString(5));
            impl.setArguments(row.getString(6));

            /* preserve these values internally */
            impl.createShadow();

            plugin = impl;
        }
        return plugin;
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.PluginFactory#createSystemPlugin(java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public SystemPlugin createSystemPlugin(String nodeId, String name, String family, String pluginType,
            String description, String arguments) {

        return new SystemPluginImpl(nodeId, name, family, pluginType, description, arguments);
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.PluginFactory#getAllSystemPlugins()
     */
    @Override
    public SystemPlugin[] getAllSystemPlugins() {
        SystemPlugin[] plugins = null;
        try {
            plugins = runSystemPluginQuery(SELECT_ALL_QUERY_SYSTEM_PLUGINS);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return plugins;
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.PluginFactory#getSystemPluginsByNode(java.lang.String)
     */
    @Override
    public SystemPlugin[] getSystemPluginsByNode(String id) {
        SystemPlugin[] plugins = null;
        try {
            String query = format(BY_NODE_QUERY_SYSTEM_PLUGINS, id);
            plugins = runSystemPluginQuery(query);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return plugins;
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.PluginFactory#getSystemPlugins(java.lang.String)
     */
    @Override
    public SystemPlugin[] getSystemPlugins(String predicateQuery) throws RegistryQueryException {

        SystemPlugin[] plugins = null;
        try {
            String query = format(PREDICATE_QUERY_SYSTEM_PLUGINS, predicateQuery);
            plugins = runSystemPluginQuery(query);
        } catch (PersistenceException e) {
            e.printStackTrace();
            throw new RegistryQueryException(e.getMessage());
        }
        return plugins;
    }

    private SystemPlugin[] runSystemPluginQuery(String sql) throws PersistenceException {
        SystemPlugin[] plugins = null;
        RegistryObject[] objects = queryRegistryObjects(sql, this);
        if (objects != null && objects.length > 0) {
            // necessary
            plugins = new SystemPlugin[objects.length];
            for (int k = 0; k < objects.length; k++) {
                plugins[k] = (SystemPlugin) objects[k];
            }
        } else {
            plugins = new SystemPlugin[0];
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

        StringBuilder buf = new StringBuilder();

        SystemPlugin plugin = (SystemPlugin) obj;
        buf.append("delete FROM ");
        buf.append(FabricRegistry.FABLET_PLUGINS);
        buf.append(" WHERE (");
        buf.append("NODE_ID='");
        buf.append(plugin.getNodeId()).append("' AND ");
        buf.append("NAME='");
        buf.append(plugin.getName()).append("' AND ");
        buf.append("FAMILY='");
        buf.append(plugin.getFamilyName()).append('\'');

        buf.append(" AND TYPE='");
        buf.append(plugin.getPluginType()).append('\'');

        buf.append(')');

        return buf.toString();
    }

}
