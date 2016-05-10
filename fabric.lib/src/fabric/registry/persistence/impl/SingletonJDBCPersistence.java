/*
 * (C) Copyright IBM Corp. 2009, 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.persistence.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.Fabric;
import fabric.core.logging.FLog;
import fabric.core.properties.ConfigProperties;
import fabric.core.properties.Properties;
import fabric.registry.QueryScope;
import fabric.registry.RegistryObject;
import fabric.registry.exception.PersistenceException;
import fabric.registry.impl.AbstractFactory;
import fabric.registry.persistence.Persistence;
import fabric.registry.persistence.distributed.DistributedQueryResult;

/**
 * The JDBC based implementation of persistence
 */
public class SingletonJDBCPersistence extends Object implements Persistence {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2014";

    /** Connection to the Fabric Registry database. */
    private Connection fabricConnection = null;

    /** Lock object controlling access to the thread connection. */
    private Object fabricConnectionLock = new Object();

    /** URL of the Fabric Registry database, used for all queries, inserts, deletes and updates. */
    protected String fabricDbUrl = null;

    /** JDBC query timeout. */
    private int queryTimeout = 60;

    /** Cache of JDBC Statements, one per thread. */
    HashMap<String, Statement> statementCache = new HashMap<String, Statement>();

    /** Object used to synchronise Fabric Registry reconnection attempts. */
    private Object monitor = new Object();

    /** Thread pool for Registry connection test worker threads. */
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    /** Flag (set via configuration) indicating if the experimental Fabric Registry reconnection feature is enabled. */
    private boolean experimentalRegistryTimeoutMonitor = false;

    private final static String CLASS_NAME = SingletonJDBCPersistence.class.getName();
    private final static String PACKAGE_NAME = SingletonJDBCPersistence.class.getPackage().getName();

    private final static Logger logger = Logger.getLogger(PACKAGE_NAME);
    protected Properties config;

    /*
     * Inner classes
     */

    class Bool {

        boolean value = false;

        public Bool(boolean value) {
            this.value = value;
        }

        public void set(boolean value) {
            this.value = value;
        }

        public boolean get() {
            return value;
        }
    }

    /*
     * Class methods
     */

    /**
     * Default constructor.
     */
    public SingletonJDBCPersistence() {

    }

    @Override
    public void init(String url, Properties config) {

        this.config = config;
        this.fabricDbUrl = url;

        Fabric fabric = new Fabric();

        try {
            String configValue = fabric.config(ConfigProperties.REGISTRY_LOCAL_TIMEOUT, "-1");
            this.queryTimeout = Integer.parseInt(configValue);
        } catch (Exception e) {
            this.queryTimeout = -1;
        }

        try {
            String configValue = fabric.config("experimental.registry.timeoutMonitor", "false");
            this.experimentalRegistryTimeoutMonitor = Boolean.parseBoolean(configValue);
        } catch (Exception e) {
            this.experimentalRegistryTimeoutMonitor = false;
        }
    }

    /**
     * Answers a new JDBC statement instance.
     *
     * @return the new statement.
     *
     * @throws SQLException
     */
    private Statement createStatement() throws SQLException {

        Statement s = null;
        String threadName = Thread.currentThread().getName();

        if (experimentalRegistryTimeoutMonitor) {

            s = registryConnectionWithRefresh().createStatement();

        } else {

            s = registryConnection().createStatement();

            // synchronized (statementCache) {
            //
            // s = statementCache.get(threadName);
            //
            // if (s == null) {
            //
            // s = registryConnection().createStatement();
            //
            // if (queryTimeout != -1) {
            // s.setQueryTimeout(queryTimeout);
            // }
            //
            // statementCache.put(threadName, s);
            // }
            // }
        }

        return s;
    }

    /**
     * Answers the JDBC connection to the Registry.
     *
     * @return the JDBC connection.
     */
    private Connection registryConnection() {

        Connection connection = null;

        synchronized (fabricConnectionLock) {
            connection = fabricConnection;
        }

        return connection;
    }

    /**
     * Answers the JDBC connection to the Registry, checking its validity and renewing if necessary.
     *
     * @return the JDBC connection.
     */
    private Connection registryConnectionWithRefresh() {

        final Bool isThreadComplete = new Bool(false);
        final Bool doCloseConnection = new Bool(false);

        Object waitMonitor = new Object();

        Runnable testConnectionWorker = new Runnable() {

            @Override
            public void run() {

                Connection connection = null;

                synchronized (fabricConnectionLock) {

                    if (fabricConnection == null) {
                        jdbcConnect();
                    }

                    connection = fabricConnection;
                }

                try {
                    Statement s = fabricConnection.createStatement();
                    s.executeQuery("values(1)");
                } catch (SQLException e) {
                    synchronized (doCloseConnection) {
                        doCloseConnection.set(true);
                        logger.log(Level.FINE, "Exception using Registry connection: {0}", e.getMessage());
                    }
                }

                synchronized (isThreadComplete) {
                    isThreadComplete.set(true);
                }

                synchronized (waitMonitor) {
                    waitMonitor.notify();
                }

                // try {
                // Thread.sleep(1);
                // } catch (InterruptedException e1) {
                // }

                synchronized (doCloseConnection) {
                    if (doCloseConnection.get()) {
                        try {
                            connection.close();
                        } catch (SQLException e) {
                            logger.log(Level.FINE, "Could not close stale Registry connection: {0}", e.getMessage());
                        }
                    }
                }
            }
        };

        executor.execute(testConnectionWorker);

        long waitFor = 5000;
        long start = System.currentTimeMillis();
        long end = 0;
        boolean interrupted = false;
        do {
            try {
                interrupted = false;
                synchronized (waitMonitor) {
                    waitMonitor.wait(waitFor);
                }
            } catch (InterruptedException e) {
                interrupted = true;
            }
            end = System.currentTimeMillis();
        } while (interrupted && ((end - start) < waitFor));

        synchronized (isThreadComplete) {

            synchronized (doCloseConnection) {

                if (!isThreadComplete.get() || doCloseConnection.get()) {

                    synchronized (fabricConnectionLock) {

                        /* Refresh the JDBC connection */
                        logger.log(Level.FINE, "Resetting JDBC connection");
                        fabricConnection = null;
                        jdbcConnect();

                        /* Tell the thread to clean up the connection */
                        doCloseConnection.set(true);

                        try {
                            start = System.currentTimeMillis();
                            Statement s = fabricConnection.createStatement();
                            s.executeQuery("values(1)");
                            end = System.currentTimeMillis();
                            logger.log(Level.FINE, "Query took [{0}] seconds with the new connection",
                                    (end - start) / 1000);
                        } catch (SQLException e) {
                            synchronized (doCloseConnection) {
                                doCloseConnection.set(true);
                                logger.log(Level.FINE, "Exception using Registry connection: {0}", e.getMessage());
                            }
                        }
                    }
                }
            }
        }

        return fabricConnection;
    }

    /**
     * Establish a connection to the underlying Derby database for inserts/deletes/updates.
     *
     */
    @Override
    public void connect() throws PersistenceException {

        jdbcConnect();
    }

    /**
     * Establish the connection with the Registry.
     *
     * If it is not available, keep retrying at regular intervals.
     */
    private void jdbcConnect() {

        boolean connected = false;
        int retryInterval = 5;

        synchronized (fabricConnectionLock) {

            /* if we were connected, tidy up the previous connection where possible */
            if (fabricConnection != null) { /* runtime connection drop */
                logger.log(Level.FINEST,
                        "Testing if Registry connection has already been established by another thread");
                Statement stmt = null;
                ResultSet rs = null;
                try {
                    stmt = fabricConnection.createStatement();
                    rs = stmt.executeQuery("values(1)");
                    /* if we get here we just return */
                    logger.log(Level.FINER, "Registry connection already established");
                    return;
                } catch (SQLException e) {
                    if (e.getSQLState().startsWith("08")) { /* not connected */
                        try {
                            fabricConnection.close(); /* attempt to release any resources */
                        } catch (Exception e1) {
                        } finally {
                            fabricConnection = null;
                        }
                    } else {
                        /* some other exception */
                    }
                } finally {
                    if (rs != null) {
                        try {
                            rs.close();
                        } catch (SQLException e) {
                        }
                    }
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (SQLException e) {
                        }
                    }
                }
            }

            logger.log(Level.FINER, "Connecting to the Registry");
            /* try until we get a connection */
            while (!connected) {
                try {
                    Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
                    fabricConnection = DriverManager.getConnection(fabricDbUrl);
                    connected = true;

                    logger.log(Level.FINER, "Registry connection established");

                } catch (Exception e) { /* log the exception and retry in 10 seconds */

                    logger.log(
                            Level.WARNING,
                            "Failed to connect to Fabric Registry (ensure that the Registry is running); retrying in {0} second(s): [{1}]",
                            new Object[] {retryInterval, e.getMessage()});
                    logger.log(Level.FINEST, "Full exception: ", e);

                    /* Wait before retrying */
                    try {
                        Thread.sleep(retryInterval * 1000);
                    } catch (InterruptedException e1) {
                        logger.log(Level.WARNING, "Sleep interrupted: ", e1);
                    }
                }
            }
        }
    }

    @Override
    public void disconnect() throws PersistenceException {

        synchronized (fabricConnectionLock) {

            if (fabricConnection != null) {
                try {
                    fabricConnection.close();
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Failed to disconnect from Fabric Registry: ", e.getMessage());
                    logger.log(Level.FINEST, "Full exception: ", e);
                    throw new PersistenceException();
                }
            }
        }
    }

    @Override
    public RegistryObject[] queryRegistryObjects(String sqlString, AbstractFactory factory, QueryScope queryScope)
            throws PersistenceException {

        ArrayList<RegistryObject> objects = new ArrayList<RegistryObject>();
        Statement stmt = null;
        ResultSet rs = null;
        SQLException thrownException = null;

        try {
            stmt = createStatement();
            rs = stmt.executeQuery(sqlString);
            RegistryObject regObject = null;
            PersistenceResultKeys keys = new PersistenceResultKeys(rs.getMetaData());
            while (rs.next()) {
                regObject = factory.create(new PersistenceResultRow(rs, keys));
                if (regObject != null) {
                    objects.add(regObject);
                }
            }
        } catch (SQLException sqlEx) {
            thrownException = sqlEx;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex1) {
                    logger.log(Level.WARNING, "Error closing result set! ", thrownException);
                }
            }
        }

        if (thrownException != null) {

            logger.log(Level.WARNING, "Error executing Registry query [{0}], SQL state [{1}], error code [{2}]: {3}",
                    new Object[] {sqlString, thrownException.getSQLState(), thrownException.getErrorCode(),
                            thrownException.getMessage()});
            logger.log(Level.FINEST, "Full exception: ", thrownException);
        }

        if ((thrownException != null && thrownException.getSQLState().startsWith("08")) || registryConnection() == null) {
            /* 08 prefix codes are connection exceptions in Derby */
            String reconnectEnabled = config.getProperty("registry.reconnect", "false");
            logger.log(Level.FINER, "Reconnect enabled: " + reconnectEnabled);
            if (reconnectEnabled.equalsIgnoreCase("true")) {
                logger.log(Level.WARNING, "Registry connection lost... attempting to reconnect...");
                synchronized (monitor) {
                    jdbcConnect();
                }
            }
        } else { // anything else, log it and throw exception
            if (thrownException != null) {
                throw new PersistenceException("Error occurred executing query.", thrownException.getMessage(),
                        thrownException.getErrorCode(), thrownException.getSQLState());
            }
        }

        return objects.toArray(new RegistryObject[] {});
    }

    protected int queryInt(String sqlString, QueryScope queryScope) throws PersistenceException {

        int returnValue = 0;
        Statement stmt = null;
        ResultSet rs = null;
        SQLException thrownException = null;

        try {
            stmt = createStatement();
            rs = stmt.executeQuery(sqlString);
            while (rs.next()) {
                /* only expecting a single int value */
                returnValue = rs.getInt(1);
            }
        } catch (SQLException sqlEx) {
            thrownException = sqlEx;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex1) {
                    logger.log(Level.WARNING, "Error closing result set! ", thrownException);
                }
            }
        }

        if (thrownException != null) {
            logger.log(Level.WARNING, "Error executing Registry query [{0}], SQL state [{1}], error code [{2}]: {3}",
                    new Object[] {sqlString, thrownException.getSQLState(), thrownException.getErrorCode(),
                            thrownException.getMessage()});
            logger.log(Level.FINEST, "Full exception: ", thrownException);
        }

        if ((thrownException != null && thrownException.getSQLState().startsWith("08")) || registryConnection() == null) {
            /* 08 prefix codes are connection exceptions in Derby */
            String reconnectEnabled = config.getProperty("registry.reconnect", "false");
            logger.log(Level.FINER, "Reconnect enabled: " + reconnectEnabled);
            if (reconnectEnabled.equalsIgnoreCase("true")) {
                logger.log(Level.WARNING, "Registry connection lost... attempting to reconnect...");
                synchronized (monitor) {
                    jdbcConnect();
                }
            }
        } else { // anything else, log it and throw exception
            if (thrownException != null) {
                throw new PersistenceException("Error occurred executing query.", thrownException.getMessage(),
                        thrownException.getErrorCode(), thrownException.getSQLState());
            }
        }

        return returnValue;
    }

    /**
     * @see fabric.registry.persistence.Persistence#queryString(java.lang.String)
     */
    @Override
    public String queryString(String queryString, QueryScope queryScope) throws PersistenceException {

        String returnValue = null;

        Statement stmt = null;
        ResultSet rs = null;
        SQLException thrownException = null;

        try {
            stmt = createStatement();
            rs = stmt.executeQuery(queryString);
            while (rs.next()) {
                /* only expecting a single row and a single column value */
                returnValue = rs.getString(1);
                break;
            }
        } catch (SQLException sqlEx) {
            thrownException = sqlEx;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex1) {
                    logger.log(Level.WARNING, "Error closing result set! ", thrownException);
                    // log.log(this, ILogger.ERROR, Fabric.message("registry.resultset.failed"));
                }
            }
        }

        if (thrownException != null) {
            logger.log(Level.WARNING, "Error executing Registry query [{0}], SQL state [{1}], error code [{2}]: {3}",
                    new Object[] {queryString, thrownException.getSQLState(), thrownException.getErrorCode(),
                            thrownException.getMessage()});
            logger.log(Level.FINEST, "Full exception: ", thrownException);
        }

        if ((thrownException != null && thrownException.getSQLState().startsWith("08")) || registryConnection() == null) {
            /* 08 prefix codes are connection exceptions in Derby */
            String reconnectEnabled = config.getProperty("registry.reconnect", "false");
            logger.log(Level.FINER, "Reconnect enabled: " + reconnectEnabled);
            if (reconnectEnabled.equalsIgnoreCase("true")) {
                logger.log(Level.WARNING, "Registry connection lost... attempting to reconnect...");
                synchronized (monitor) {
                    jdbcConnect();
                }
            }
        } else { // anything else, log it and throw exception
            if (thrownException != null) {
                throw new PersistenceException("Error occurred executing query.", thrownException.getMessage(),
                        thrownException.getErrorCode(), thrownException.getSQLState());
            }
        }

        return returnValue;
    }

    @Override
    public Object[] query(String sqlString, QueryScope queryScope) throws PersistenceException {

        Object[] returnValues = null;
        List<Object> values = null;

        Statement stmt = null;
        ResultSet rs = null;
        SQLException thrownException = null;

        try {
            stmt = createStatement();
            rs = stmt.executeQuery(sqlString);
            values = new ArrayList<Object>();
            while (rs.next()) {
                values.add(processResultRow(rs));
            }
        } catch (SQLException sqlEx) {
            thrownException = sqlEx;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex1) {
                    logger.log(Level.WARNING, "Error closing result set! ", thrownException);
                }
            }
        }

        if (thrownException != null) {
            logger.log(Level.WARNING, "Error executing Registry query [{0}], SQL state [{1}], error code [{2}]: {3}",
                    new Object[] {sqlString, thrownException.getSQLState(), thrownException.getErrorCode(),
                    thrownException.getMessage()});
            logger.log(Level.FINEST, "Full exception: ", thrownException);
        }

        if ((thrownException != null && thrownException.getSQLState().startsWith("08")) || registryConnection() == null) {
            /* 08 prefix codes are connection exceptions in Derby */
            String reconnectEnabled = config.getProperty("registry.reconnect", "false");
            logger.log(Level.FINER, "Reconnect enabled: " + reconnectEnabled);
            if (reconnectEnabled.equalsIgnoreCase("true")) {
                logger.log(Level.WARNING, "Registry connection lost... attempting to reconnect...");
                synchronized (monitor) {
                    jdbcConnect();
                }
            }
        } else { // anything else, log it and throw exception
            if (thrownException != null) {
                throw new PersistenceException("Error occurred executing query.", thrownException.getMessage(),
                        thrownException.getErrorCode(), thrownException.getSQLState());
            }
        }

        returnValues = values.toArray(new Object[] {});
        return returnValues;
    }

    /**
     * For the given row, build an array of the column values.
     *
     * @param rs
     *            - the ResultSet to process.
     * @return an Object array representing the column values of the result set.
     * @throws SQLException
     *             if an error occurs manipulating the ResultSet.
     */
    private Object[] processResultRow(ResultSet rs) throws SQLException {

        Object[] columnValues = null;
        if (rs != null) {
            /* process each column value */
            columnValues = new Object[rs.getMetaData().getColumnCount()];
            for (int x = 0; x < rs.getMetaData().getColumnCount(); x++) {
                switch (rs.getMetaData().getColumnType(x + 1)) {
                    case Types.CHAR:
                        columnValues[x] = rs.getString(x + 1);
                        break;
                    case Types.DATE:
                        columnValues[x] = rs.getDate(x + 1);
                        break;
                    case Types.DOUBLE:
                        columnValues[x] = rs.getDouble(x + 1);
                        break;
                    case Types.FLOAT:
                        columnValues[x] = rs.getFloat(x + 1);
                        break;
                    case Types.INTEGER:
                        columnValues[x] = rs.getInt(x + 1);
                        break;
                    case Types.SMALLINT:
                        columnValues[x] = rs.getInt(x + 1);
                        break;
                    case Types.TIMESTAMP:
                        columnValues[x] = rs.getTimestamp(x + 1);
                        break;
                    case Types.VARCHAR:
                        columnValues[x] = rs.getString(x + 1);
                        break;
                    default:
                        logger.log(Level.WARNING, "Unrecognised data type for column [{0}] - excluding from results.",
                                new Object[] {rs.getMetaData().getColumnName(x)});
                        break;
                }
            }
        }

        return columnValues;
    }

    @Override
    public boolean updateRegistryObject(String sqlString) throws PersistenceException {

        return this.updateRegistryObject(sqlString, false);
    }

    @Override
    public boolean updateRegistryObject(String sqlString, boolean ignoreDuplicateWarning) throws PersistenceException {

        if (registryConnection() != null) {

            Statement stmt = null;

            try {

                stmt = createStatement();
                stmt.executeUpdate(sqlString);

            } catch (SQLException e) {

                if (e.getSQLState().equals("23505") && !ignoreDuplicateWarning) {

                    logger.log(Level.FINEST, "Duplicate key error encountered for statement [{0}]", sqlString);
                    throw new PersistenceException("Error during registry update: ", e.getMessage(), e.getErrorCode(),
                            e.getSQLState());

                } else {

                    if (e.getSQLState().startsWith("08")) { /* 08 prefix codes are connection exceptions in Derby */

                        logger.log(Level.WARNING, "Registry connection lost; attempting to reconnect");
                        String reconnectEnabled = config.getProperty("registry.reconnect", "false");

                        if (reconnectEnabled.equalsIgnoreCase("true")) {
                            synchronized (monitor) {
                                jdbcConnect();
                            }
                        }

                    } else { // anything else, log it and throw exception

                        logger.log(Level.WARNING, "Failed to execute update statement [{0}]: {1}", new Object[] {
                                sqlString, e.getMessage()});
                        logger.log(Level.FINEST, "Full exception: ", e);
                        throw new PersistenceException("Error occurred exeuting update.", e.getMessage(), e
                                .getErrorCode(), e.getSQLState());

                    }
                }
            }

            return true;

        } else {

            return false;

        }
    }

    /**
     * Close the connection at the end of the life of this class.
     */
    @Override
    protected void finalize() throws Throwable {
        disconnect();
        super.finalize();
    }

    @Override
    public boolean updateRegistryObjects(String[] sqlStrings) throws PersistenceException {

        if (registryConnection() != null) {
            try {

                Statement stmt = createStatement();

                for (int z = 0; z < sqlStrings.length; z++) {
                    stmt.addBatch(sqlStrings[z]);
                }

                int[] rowCounts = stmt.executeBatch();

                if (logger.isLoggable(Level.FINEST)) {

                    Integer[] rowCountObjs = new Integer[rowCounts.length];
                    for (int rc = 0; rc < rowCounts.length; rc++) {
                        rowCountObjs[rc] = rowCounts[rc];
                    }

                    logger.log(Level.FINEST, "{0} rows updated", FLog.arrayAsString(rowCountObjs));
                }

            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to execute batch updates: ", e);
                String sqlState = e.getSQLState();
                if (sqlState.equals("XJ208")) {
                    SQLException sqlEx = e.getNextException();
                    while (sqlEx != null) {
                        if (sqlEx.getSQLState().equals("23505")) {
                            sqlState = sqlEx.getSQLState();
                            break;
                        }
                        sqlEx = sqlEx.getNextException();
                    }
                    if (!sqlState.equals("23505")) { /* if not a duplicate key exception */
                        throw new PersistenceException("Exception occurred running batched updates.", e.getMessage(), e
                                .getErrorCode(), sqlState);
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public DistributedQueryResult getDistributedQueryResult(String sqlString, String nodeName)
            throws PersistenceException {

        DistributedQueryResult queryResult = null;
        Statement stmt = null;
        ResultSet rs = null;
        SQLException thrownException = null;

        try {
            stmt = createStatement();
            rs = stmt.executeQuery(sqlString);
            queryResult = new DistributedQueryResult(nodeName, rs);
        } catch (SQLException sqlEx) {
            thrownException = sqlEx;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex1) {
                    logger.log(Level.WARNING, "Error closing result set! ", thrownException);
                    // log.log(this, ILogger.ERROR, Fabric.message("registry.resultset.failed"));
                }
            }
        }

        if (thrownException != null) {
            logger.log(Level.WARNING, "Error executing Registry query [{0}], SQL state [{1}], error code [{2}]: {3}",
                    new Object[] {sqlString, thrownException.getSQLState(), thrownException.getErrorCode(),
                            thrownException.getMessage()});
            logger.log(Level.FINEST, "Full exception: ", thrownException);
        }

        if ((thrownException != null && thrownException.getSQLState().startsWith("08")) || registryConnection() == null) {
            /* 08 prefix codes are connection exceptions in Derby */
            String reconnectEnabled = config.getProperty("registry.reconnect", "false");
            logger.log(Level.FINER, "Reconnect enabled: " + reconnectEnabled);
            if (reconnectEnabled.equalsIgnoreCase("true")) {
                logger.log(Level.WARNING, "Registry connection lost... attempting to reconnect...");
                synchronized (monitor) {
                    jdbcConnect();
                }
            }
        } else { // anything else, log it and throw exception
            if (thrownException != null) {
                throw new PersistenceException("Error occurred executing query.", thrownException.getMessage(),
                        thrownException.getErrorCode(), thrownException.getSQLState());
            }
        }

        return queryResult;
    }

    @Override
    public void initNodeConfig(Properties config) throws PersistenceException {
        // No node specific config to establish
        return;
    }

}