/*
 * (C) Copyright IBM Corp. 2011
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.FabricRegistry;
import fabric.registry.QueryScope;
import fabric.registry.RegistryObject;
import fabric.registry.TaskService;
import fabric.registry.TaskServiceFactory;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.MalformedPredicateException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.exception.RegistryQueryException;
import fabric.registry.persistence.IPersistenceResultRow;
import fabric.registry.persistence.PersistenceManager;

/**
 * Implementation of the factory for <code>TaskService</code>s.
 */
public class TaskServiceFactoryImpl extends AbstractFactory implements TaskServiceFactory {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2011";

    /*
     * Class constants
     */

    /** Factory for local (singleton) Registry operations */
    private static TaskServiceFactoryImpl localQueryInstance = null;

    /** Factory for distributed (Gaian) Registry operations */
    private static TaskServiceFactoryImpl remoteQueryInstance = null;

    /*
     * Queries
     */

    /** Select records using an arbitrary WHERE clause */
    private String PREDICATE_QUERY = null;
    /** Select all records */
    private String SELECT_ALL_SENSOR_FEEDS = null;
    /** Select records for a particular task Id */
    private String SELECT_SENSOR_FEEDS_BY_TASK = null;
    /** Select a specific task service by ID */
    private String SELECT_BY_ID = null;

    /** Delete records for a particular task */
    private static String DELETE_FEEDS_BY_TASK = "delete from " + FabricRegistry.TASK_SYSTEMS + " where TASK_ID='%s'";

    /*
     * Static initialisation
     */
    static {
        localQueryInstance = new TaskServiceFactoryImpl(QueryScope.LOCAL);
        remoteQueryInstance = new TaskServiceFactoryImpl(QueryScope.DISTRIBUTED);
    }

    private TaskServiceFactoryImpl(QueryScope queryScope) {

        this.queryScope = queryScope;

        PREDICATE_QUERY = format("select * from %s where \\%s", FabricRegistry.TASK_SYSTEMS);
        SELECT_ALL_SENSOR_FEEDS = format("select * from %s", FabricRegistry.TASK_SYSTEMS);
        SELECT_SENSOR_FEEDS_BY_TASK = format("select * from %s where TASK_ID='\\%s'", FabricRegistry.TASK_SYSTEMS);
        SELECT_BY_ID = format(
                "select * from %s where TASK_ID='\\%s' and PLATFORM_ID='\\%s' and SERVICE_ID='\\%s' and DATA_FEED_ID='\\%s'",
                FabricRegistry.TASK_SYSTEMS);
    }

    /**
     * Get the single instance of this factory.
     *
     * @return the factory instance.
     */
    public static TaskServiceFactoryImpl getInstance(QueryScope queryScope) {

        if (queryScope == QueryScope.LOCAL) {
            return localQueryInstance;
        } else {
            return remoteQueryInstance;
        }
    }

    @Override
    public RegistryObject create(IPersistenceResultRow row) throws PersistenceException {

        TaskService taskSensorFeed = null;
        if (row != null) {
            TaskServiceImpl impl = new TaskServiceImpl();
            impl.setTaskId(row.getString(1));
            impl.setPlatformId(row.getString(2));
            impl.setSystemId(row.getString(3));
            impl.setServiceId(row.getString(4));
            impl.setDescription(row.getString(5));
            impl.setConfigurationURI(row.getString(6));
            impl.setConfiguration(row.getString(7));

            /* Add Gaian origin node */
            if (this == remoteQueryInstance) {
                impl.setOriginNode(row.getString("GDB_NODE"));
            }

            /* preserve these values internally */
            impl.createShadow();

            taskSensorFeed = impl;
        }
        return taskSensorFeed;
    }

    @Override
    public TaskService createTaskService(String taskId, String platformId, String serviceId, String feedId) {

        return createTaskService(taskId, platformId, serviceId, feedId, null, null, null);
    }

    @Override
    public TaskService createTaskService(String taskId, String platformId, String serviceId, String feedId,
            String description, String configuration, String configurationUri) {

        return new TaskServiceImpl(taskId, platformId, serviceId, feedId, description, configuration, configurationUri);
    }

    /**
     * @see fabric.registry.impl.AbstractFactory#getDeleteSql(fabric.registry.RegistryObject)
     */
    @Override
    public String getDeleteSql(RegistryObject obj) {

        StringBuilder buf = new StringBuilder();
        if (obj instanceof TaskService) {
            TaskService taskService = (TaskService) obj;
            buf.append("delete from " + FabricRegistry.TASK_SYSTEMS + " where(");
            buf.append("TASK_ID='").append(taskService.getTaskId()).append("' AND");
            buf.append(" DATA_FEED_ID='").append(taskService.getServiceId()).append("' AND");
            buf.append(" SERVICE_ID='").append(taskService.getSystemId()).append("' AND");
            buf.append(" PLATFORM_ID='").append(taskService.getPlatformId()).append("')");
        }
        return buf.toString();
    }

    /**
     * @see fabric.registry.impl.AbstractFactory#getInsertSql(fabric.registry.RegistryObject)
     */
    @Override
    public String getInsertSql(RegistryObject obj) {

        StringBuilder buf = new StringBuilder();
        if (obj instanceof TaskService) {
            TaskService taskService = (TaskService) obj;
            buf.append("insert into " + FabricRegistry.TASK_SYSTEMS + " values(");
            buf.append(nullOrString(taskService.getTaskId())).append(',');
            buf.append(nullOrString(taskService.getPlatformId())).append(',');
            buf.append(nullOrString(taskService.getSystemId())).append(',');
            buf.append(nullOrString(taskService.getServiceId())).append(',');
            buf.append(nullOrString(taskService.getDescription())).append(',');
            buf.append(nullOrString(taskService.getConfiguration())).append(',');
            buf.append(nullOrString(taskService.getConfigurationURI())).append(')');
        }
        return buf.toString();
    }

    /**
     * @see fabric.registry.impl.AbstractFactory#getUpdateSql(fabric.registry.RegistryObject)
     */
    @Override
    public String getUpdateSql(RegistryObject obj) {

        StringBuilder buf = new StringBuilder();
        if (obj instanceof TaskService) {
            TaskService taskService = (TaskService) obj;
            buf.append("update " + FabricRegistry.TASK_SYSTEMS + " set ");
            buf.append("TASK_ID='").append(taskService.getTaskId()).append('\'').append(',');
            buf.append("PLATFORM_ID='").append(taskService.getPlatformId()).append('\'').append(',');
            buf.append("SERVICE_ID='").append(taskService.getSystemId()).append('\'').append(',');
            buf.append("DATA_FEED_ID='").append(taskService.getServiceId()).append('\'').append(',');
            buf.append("DESCRIPTION=").append(nullOrString(taskService.getDescription())).append(',');
            buf.append("CONFIGURATION=").append(nullOrString(taskService.getConfiguration())).append(',');
            buf.append("CONFIGURATION_URI=").append(nullOrString(taskService.getConfigurationURI()));
            buf.append(" WHERE");

            /* if it exists, use the shadow values for the WHERE clause */
            if (taskService.getShadow() != null) {
                TaskService shadow = (TaskService) taskService.getShadow();
                buf.append(" TASK_ID='").append(shadow.getTaskId()).append("' AND");
                buf.append(" DATA_FEED_ID='").append(shadow.getServiceId()).append("' AND");
                buf.append(" SERVICE_ID='").append(shadow.getSystemId()).append("' AND");
                buf.append(" PLATFORM_ID='").append(shadow.getPlatformId()).append('\'');
            } else {
                buf.append(" TASK_ID='").append(taskService.getTaskId()).append("' AND");
                buf.append(" DATA_FEED_ID='").append(taskService.getServiceId()).append("' AND");
                buf.append(" SERVICE_ID='").append(taskService.getSystemId()).append("' AND");
                buf.append(" PLATFORM_ID='").append(taskService.getPlatformId()).append('\'');
            }
        }
        return buf.toString();
    }

    /**
     * @throws
     * @see fabric.registry.TaskServiceFactory#getTaskServices(java.lang.String)
     */
    @Override
    public TaskService[] getTaskServices(String queryPredicates) throws RegistryQueryException {

        TaskService[] taskServices = null;
        String query = null;
        try {
            /* check the predicate for basic things like double-quotes etc. */
            queryPredicates = parsePredicate(queryPredicates);
            /* build the full query */
            query = format(PREDICATE_QUERY, queryPredicates);
            /* execute */
            taskServices = runTaskServiceQuery(query);
        } catch (PersistenceException e) {
            e.printStackTrace();
            /* check the SQLState to identify the specific error */
            if (e.getSqlState().startsWith("42X") || e.getSqlState().startsWith("42Y")
                    || e.getSqlState().startsWith("42Z")) {
                /* Apache Derby SQL compilation error - problem with the predicate */
                throw new MalformedPredicateException("Invalid predicate for query: " + queryPredicates);
            } else {
                throw new RegistryQueryException("Error occurred running query: " + query);
            }
        }
        return taskServices;
    }

    /**
     * @see fabric.registry.TaskServiceFactory#getTaskServicesByTask(java.lang.String)
     */
    @Override
    public TaskService[] getTaskServicesByTask(String taskId) {

        TaskService[] taskServices = null;
        try {
            String query = format(SELECT_SENSOR_FEEDS_BY_TASK, taskId);
            taskServices = runTaskServiceQuery(query);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return taskServices;
    }

    /**
     * Run a query for TaskServices in the Registry.
     *
     * @param sql
     *            - the SQL query to execute.
     * @return a list of TaskService objects or an empty list if no matches were found.
     * @throws PersistenceException
     *             if an error occurs executing the query.
     */
    private TaskService[] runTaskServiceQuery(String sql) throws PersistenceException {

        TaskService[] taskSensorFeeds = null;
        RegistryObject[] objects = queryRegistryObjects(sql, this);
        if (objects != null && objects.length > 0) {
            // necessary
            taskSensorFeeds = new TaskService[objects.length];
            for (int k = 0; k < objects.length; k++) {
                taskSensorFeeds[k] = (TaskService) objects[k];
            }
        } else {
            taskSensorFeeds = new TaskService[0];
        }
        return taskSensorFeeds;
    }

    /**
     * @see fabric.registry.TaskServiceFactory#deleteTaskSensorsForTask(java.lang.String)
     */
    @Override
    public boolean deleteTaskServicesForTask(String taskId) {

        String deleteSql = format(DELETE_FEEDS_BY_TASK, taskId);
        try {
            boolean success = PersistenceManager.getPersistence().updateRegistryObject(deleteSql);
            return success;
        } catch (PersistenceException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @see fabric.registry.Factory#delete(fabric.registry.RegistryObject)
     */
    @Override
    public boolean delete(RegistryObject obj) {

        if (obj != null && obj instanceof TaskService) {
            return super.delete(obj, this);
        } else {
            return false;
        }
    }

    /**
     * @see fabric.registry.Factory#save(fabric.registry.RegistryObject)
     */
    @Override
    public boolean save(RegistryObject obj) throws IncompleteObjectException {

        if (obj != null && obj instanceof TaskService) {
            return super.save(obj, this);
        } else {
            return false;
        }
    }

    /**
     * @see fabric.registry.TaskServiceFactory#getAllTaskServices()
     */
    @Override
    public TaskService[] getAllTaskServices() {

        TaskService[] services = null;
        try {
            services = runTaskServiceQuery(SELECT_ALL_SENSOR_FEEDS);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return services;
    }

    /**
     * @see fabric.registry.Factory#insert(fabric.registry.RegistryObject)
     */
    @Override
    public boolean insert(RegistryObject obj) throws IncompleteObjectException, DuplicateKeyException,
        PersistenceException {

        if (obj != null && obj instanceof TaskService) {
            return super.insert(obj, this);
        } else {
            return false;
        }
    }

    /**
     * @see fabric.registry.Factory#update(fabric.registry.RegistryObject)
     */
    @Override
    public boolean update(RegistryObject obj) throws IncompleteObjectException, PersistenceException {

        if (obj != null && obj instanceof TaskService) {
            return super.update(obj, this);
        } else {
            return false;
        }
    }

    /**
     * @see fabric.registry.TaskServiceFactory#getTaskServiceById(java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public TaskService getTaskServiceById(String taskId, String platformId, String systemId, String feedId) {

        TaskService taskService = null;
        String query = null;
        try {
            query = format(SELECT_BY_ID, taskId, platformId, systemId, feedId);

            TaskService[] taskServices = runTaskServiceQuery(query);
            if (taskServices != null && taskServices.length > 0) {
                taskService = taskServices[0];
            }
        } catch (PersistenceException e) {
            e.printStackTrace();
        }

        return taskService;
    }

}
