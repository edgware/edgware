/*
 * (C) Copyright IBM Corp. 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.FabricPlugin;
import fabric.registry.FabricRegistry;
import fabric.registry.RegistryObject;
import fabric.registry.QueryScope;
import fabric.registry.TaskPlugin;
import fabric.registry.TaskPluginFactory;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.exception.RegistryQueryException;
import fabric.registry.persistence.IPersistenceResultRow;

public class TaskPluginFactoryImpl extends AbstractFactory implements TaskPluginFactory {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    /** Factory for local (singleton) Registry operations */
    private static TaskPluginFactoryImpl localQueryInstance = null;
    /** Factory for remote (distributed) Registry operations */
    private static TaskPluginFactoryImpl remoteQueryInstance = null;

    /** Select all records */
    private String SELECT_ALL_QUERY_TASK_PLUGINS = null;

    /** Select all records for a particular node */
    private String BY_NODE_QUERY_TASK_PLUGINS = null;

    /** Select records using an arbitrary WHERE clause */
    private String PREDICATE_QUERY_TASK_PLUGINS = null;

    /*
     * Static initialisation
     */

    static {
        localQueryInstance = new TaskPluginFactoryImpl(QueryScope.LOCAL);
        remoteQueryInstance = new TaskPluginFactoryImpl(QueryScope.DISTRIBUTED);
    }

    public static TaskPluginFactoryImpl getInstance(QueryScope queryScope) {
        if (queryScope == QueryScope.LOCAL) {
            return localQueryInstance;
        } else {
            return remoteQueryInstance;
        }
    }

    private TaskPluginFactoryImpl(QueryScope queryScope) {
        this.queryScope = queryScope;

        SELECT_ALL_QUERY_TASK_PLUGINS = format("select * from %s", FabricRegistry.TASK_PLUGINS);

        BY_NODE_QUERY_TASK_PLUGINS = format("select * from %s where NODE_ID='\\%s'", FabricRegistry.TASK_PLUGINS);

        PREDICATE_QUERY_TASK_PLUGINS = format("select * from %s where \\%s", FabricRegistry.TASK_PLUGINS);
    }

    @Override
    public String getUpdateSql(RegistryObject obj) {
        StringBuffer buf = new StringBuffer();
        if (obj instanceof TaskPlugin) {
            TaskPlugin plugin = (TaskPlugin) obj;
            buf.append("update ");
            buf.append(FabricRegistry.TASK_PLUGINS);
            buf.append(" set ");
            buf.append("DESCRIPTION=").append(nullOrString(plugin.getDescription())).append(",");
            buf.append("ARGUMENTS=").append(nullOrString(plugin.getArguments())).append(",");
            buf.append("PLATFORM_ID='").append(plugin.getPlatformId()).append("',");
            buf.append("SERVICE_ID='").append(plugin.getSensorId()).append("',");
            buf.append("DATA_FEED_ID=").append(plugin.getFeedId()).append("'");
            buf.append(" WHERE ");

            /* if it exists, use the shadow values for the WHERE clause */
            if (plugin.getShadow() != null) {
                TaskPlugin shadow = (TaskPlugin) plugin.getShadow();
                buf.append("NODE_ID='").append(shadow.getNodeId()).append("' AND ");
                buf.append("TASK_ID='").append(shadow.getTaskId()).append("' AND ");
                buf.append("NAME='").append(shadow.getName()).append("' AND ");
                buf.append("ORDINAL=").append(shadow.getOrdinal()).append(" AND ");
                buf.append("TYPE='").append(shadow.getPluginType()).append("'");
            } else {
                buf.append("NODE_ID='").append(plugin.getNodeId()).append("' AND ");
                buf.append("TASK_ID='").append(plugin.getTaskId()).append("' AND ");
                buf.append("NAME='").append(plugin.getName()).append("' AND ");
                buf.append("ORDINAL=").append(plugin.getOrdinal()).append(" AND ");
                buf.append("TYPE='").append(plugin.getPluginType()).append("'");
            }

        }
        return buf.toString();
    }

    @Override
    public String getInsertSql(RegistryObject obj) {
        StringBuffer buf = new StringBuffer();
        if (obj instanceof TaskPlugin) {
            TaskPlugin plugin = (TaskPlugin) obj;
            buf.append("insert into ");
            buf.append(FabricRegistry.TASK_PLUGINS);
            buf.append(" values(");
            buf.append("'").append(plugin.getNodeId()).append("',");
            buf.append("'").append(plugin.getTaskId()).append("',");
            buf.append("'").append(plugin.getName()).append("',");
            buf.append(nullOrString(plugin.getFamilyName())).append(",");
            buf.append("'").append(plugin.getPluginType()).append("',");
            buf.append(plugin.getOrdinal()).append(",");
            buf.append("'").append(plugin.getPlatformId()).append("',");
            buf.append("'").append(plugin.getSensorId()).append("',");
            buf.append("'").append(plugin.getFeedId()).append("',");
            buf.append(nullOrString(plugin.getDescription())).append(",");
            buf.append(nullOrString(plugin.getArguments())).append(")");
        }
        return buf.toString();
    }

    @Override
    public RegistryObject create(IPersistenceResultRow row) throws PersistenceException {
        TaskPlugin plugin = null;
        if (row != null) {

            TaskPluginImpl impl = new TaskPluginImpl();
            impl.setNodeId(row.getString(1));
            impl.setTaskId(row.getString(2));
            impl.setName(row.getString(3));
            impl.setFamily(row.getString(4));
            impl.setPluginType(row.getString(5));
            impl.setOrdinal(row.getInt(6));
            impl.setPlatformId(row.getString(7));
            impl.setSensorId(row.getString(8));
            impl.setFeedId(row.getString(9));
            impl.setDescription(row.getString(10));
            impl.setArguments(row.getString(11));

            /* preserve these values internally */
            impl.createShadow();

            plugin = impl;
        }
        return plugin;
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.PluginFactory#createTaskPlugin(java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public TaskPlugin createTaskPlugin(String nodeId, String missionId, String name, String family, String pluginType,
            int ordinal, String description, String arguments, String platformId, String systemId, String feedId) {

        return new TaskPluginImpl(nodeId, missionId, name, family, pluginType, ordinal, description, arguments,
                platformId, systemId, feedId);
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.PluginFactory#getAllTaskPlugins()
     */
    @Override
    public TaskPlugin[] getAllTaskPlugins() {
        TaskPlugin[] plugins = null;
        try {
            plugins = runTaskPluginQuery(SELECT_ALL_QUERY_TASK_PLUGINS);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return plugins;
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.PluginFactory#getTaskPluginsByNode(java.lang.String)
     */
    @Override
    public TaskPlugin[] getTaskPluginsByNode(String id) {
        TaskPlugin[] plugins = null;
        try {
            String query = format(BY_NODE_QUERY_TASK_PLUGINS, id);
            plugins = runTaskPluginQuery(query);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return plugins;
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.PluginFactory#getTaskPlugins(java.lang.String)
     */
    @Override
    public TaskPlugin[] getTaskPlugins(String predicateQuery) throws RegistryQueryException {

        TaskPlugin[] plugins = null;
        try {
            String query = format(PREDICATE_QUERY_TASK_PLUGINS, predicateQuery);
            plugins = runTaskPluginQuery(query);
        } catch (PersistenceException e) {
            e.printStackTrace();
            throw new RegistryQueryException(e.getMessage());
        }
        return plugins;
    }

    private TaskPlugin[] runTaskPluginQuery(String sql) throws PersistenceException {
        TaskPlugin[] plugins = null;
        RegistryObject[] objects = queryRegistryObjects(sql, this);
        if (objects != null && objects.length > 0) {
            // necessary
            plugins = new TaskPlugin[objects.length];
            for (int k = 0; k < objects.length; k++) {
                plugins[k] = (TaskPlugin) objects[k];
            }
        } else {
            plugins = new TaskPlugin[0];
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

        TaskPlugin plugin = (TaskPlugin) obj;
        buf.append("DELETE FROM " + FabricRegistry.TASK_PLUGINS + " WHERE NODE_ID = '");
        buf.append(plugin.getNodeId());
        buf.append("' AND TASK_ID = '");
        buf.append(plugin.getTaskId());
        buf.append("' AND NAME = '");
        buf.append(plugin.getName());
        buf.append("' AND FAMILY = '");
        buf.append(plugin.getFamilyName());
        buf.append("' AND TYPE = '");
        buf.append(plugin.getPluginType());
        buf.append("' AND ORDINAL = ");
        buf.append(plugin.getOrdinal());
        buf.append(" AND PLATFORM_ID = '");
        buf.append(plugin.getPlatformId());
        buf.append("' AND SERVICE_ID = '");
        buf.append(plugin.getSensorId());
        buf.append("' AND DATA_FEED_ID = '");
        buf.append(plugin.getFeedId());
        buf.append("' AND DESCRIPTION = '");
        buf.append(plugin.getDescription());
        buf.append("'");
        return buf.toString();
    }

}
