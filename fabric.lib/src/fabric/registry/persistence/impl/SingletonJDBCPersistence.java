/*
 * Licensed Materials - Property of IBM
 * 
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.core.properties.Properties;
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

	/* Connection to the fabric database */
	private Connection fabricConnection = null;

	/* Url of the Fabric database, used for all queries, inserts, deletes and updates */
	protected String fabricDbUrl = null;

	/* Object used to synchronise reconnection attempts */
	private Object monitor = new Object();

	private final static String CLASS_NAME = SingletonJDBCPersistence.class.getName();
	private final static String PACKAGE_NAME = SingletonJDBCPersistence.class.getPackage().getName();

	private final static Logger logger = Logger.getLogger(PACKAGE_NAME);
	protected Properties config;

	/**
	 * 
	 */
	public SingletonJDBCPersistence() {

	}

	@Override
	public void init(String Url, Properties config) {
		String METHOD_NAME = "init";
		logger.entering(CLASS_NAME, METHOD_NAME);
		this.config = config;
		this.fabricDbUrl = Url;
		logger.exiting(CLASS_NAME, METHOD_NAME);
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

		/* if we were connected, tidy up the previous connection where possible */
		if (fabricConnection != null) { /* runtime connection drop */
			logger.log(Level.FINER, "Test connection has not been reestablished by another thread...");
			Statement stmt = null;
			ResultSet rs = null;
			try {
				stmt = fabricConnection.createStatement();
				rs = stmt.executeQuery("values(1)");
				/* if we get here we just return */
				logger.log(Level.INFO, "Registry connection established.");
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

		logger.log(Level.FINER, "Connecting to the Registry...");
		/* try until we get a connection */
		while (!connected) {
			try {
				Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
				fabricConnection = DriverManager.getConnection(fabricDbUrl);
				connected = true;

				logger.log(Level.INFO, "Registry connection established.");

			} catch (Exception e) { /* log the exception and retry in 10 seconds */

				logger.log(Level.WARNING, "Failed to connect to Fabric Registry. Retrying in 10 seconds...", e);

				/* Wait before retrying */
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					logger.log(Level.WARNING, "Sleep interrupted:", e1);
				}
			}
		}
	}

	@Override
	public void disconnect() throws PersistenceException {

		if (fabricConnection != null) {
			try {
				fabricConnection.close();
			} catch (SQLException e) {
				logger.log(Level.WARNING, "Failed to disconnect from Fabric Registry.", e);
				throw new PersistenceException();
			}
		}
	}

	@Override
	public RegistryObject[] queryRegistryObjects(String sqlString, AbstractFactory factory, boolean localOnly)
			throws PersistenceException {

		ArrayList<RegistryObject> objects = new ArrayList<RegistryObject>();
		Statement stmt = null;
		ResultSet rs = null;
		SQLException thrownException = null;
		if (fabricConnection != null) {
			try {
				stmt = fabricConnection.createStatement();
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
						logger.log(Level.WARNING, "Error closing result set!", thrownException);
					}
				}
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException ex2) {
						logger.log(Level.WARNING, "Error closing statement!", thrownException);
					}
				}
			}
		} else { /* no connection - try to reestablish */
			logger.log(Level.WARNING, "Fabric Registry Error - connection is null!", thrownException);
		}

		if (thrownException != null) {
			logger.log(Level.WARNING, "Failed to execute Fabric Registry query: " + sqlString, thrownException);
			logger.log(Level.WARNING, "SQLState: " + thrownException.getSQLState());
			logger.log(Level.WARNING, "ErrorCode: " + thrownException.getErrorCode());
		}

		if ((thrownException != null && thrownException.getSQLState().startsWith("08")) || fabricConnection == null) {
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

	protected int queryInt(String sqlString, boolean localOnly) throws PersistenceException {

		int returnValue = 0;
		Statement stmt = null;
		ResultSet rs = null;
		SQLException thrownException = null;
		if (fabricConnection != null) {
			try {
				stmt = fabricConnection.createStatement();
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
						logger.log(Level.WARNING, "Error closing result set!", thrownException);
					}
				}
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException ex2) {
						logger.log(Level.WARNING, "Error closing statement!", thrownException);
					}
				}
			}
		} else {
			logger.log(Level.WARNING, "Fabric Registry Error - connection is null!", thrownException);
		}

		if (thrownException != null) {
			logger.log(Level.WARNING, "Failed to execute Fabric Registry query: " + sqlString, thrownException);
			logger.log(Level.WARNING, "SQLState: " + thrownException.getSQLState());
			logger.log(Level.WARNING, "ErrorCode: " + thrownException.getErrorCode());
		}

		if ((thrownException != null && thrownException.getSQLState().startsWith("08")) || fabricConnection == null) {
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
	public String queryString(String queryString, boolean localOnly) throws PersistenceException {

		String returnValue = null;

		Statement stmt = null;
		ResultSet rs = null;
		SQLException thrownException = null;
		if (fabricConnection != null) {
			try {
				stmt = fabricConnection.createStatement();
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
						logger.log(Level.WARNING, "Error closing result set!", thrownException);
						// log.log(this, ILogger.ERROR, Fabric.message("registry.resultset.failed"));
					}
				}
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException ex2) {
						logger.log(Level.WARNING, "Error closing statement!", thrownException);
						// log.log(this, ILogger.ERROR, Fabric.message("registry.statement.failed"));
					}
				}
			}
		} else {
			logger.log(Level.WARNING, "Fabric Registry Error - connection is null!", thrownException);
		}

		if (thrownException != null) {
			logger.log(Level.WARNING, "Failed to execute Fabric Registry query: " + queryString, thrownException);
			logger.log(Level.WARNING, "SQLState: " + thrownException.getSQLState());
			logger.log(Level.WARNING, "ErrorCode: " + thrownException.getErrorCode());
		}

		if ((thrownException != null && thrownException.getSQLState().startsWith("08")) || fabricConnection == null) {
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
	public Object[] query(String sqlString, boolean localOnly) throws PersistenceException {

		Object[] returnValues = null;
		List<Object> values = null;

		Statement stmt = null;
		ResultSet rs = null;
		SQLException thrownException = null;
		if (fabricConnection != null) {
			try {
				stmt = fabricConnection.createStatement();
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
						logger.log(Level.WARNING, "Error closing result set!", thrownException);
					}
				}
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException ex2) {
						logger.log(Level.WARNING, "Error closing statement!", thrownException);
					}
				}
			}
		} else {
			logger.log(Level.WARNING, "Fabric Registry Error - connection is null!", thrownException);
		}

		if (thrownException != null) {
			logger.log(Level.WARNING, "Failed to execute Fabric Registry query: " + sqlString, thrownException);
			logger.log(Level.WARNING, "SQLState: " + thrownException.getSQLState());
			logger.log(Level.WARNING, "ErrorCode: " + thrownException.getErrorCode());
		}

		if ((thrownException != null && thrownException.getSQLState().startsWith("08")) || fabricConnection == null) {
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
					logger.log(Level.WARNING, "Unrecognised data type for column \"{0}\" - excluding from results.",
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

		if (fabricConnection != null) {

			Statement stmt = null;

			try {

				stmt = fabricConnection.createStatement();
				int rowCount = stmt.executeUpdate(sqlString);
				logger.log(Level.FINEST, "{0} rows updated", new Object[] {rowCount});

			} catch (SQLException e) {

				if (e.getSQLState().equals("23505") && !ignoreDuplicateWarning) { /* just log a warning */

					logger.log(Level.FINE, "Duplicate key error encountered for statement: " + sqlString);
					throw new PersistenceException("Error occurred executing update.", e.getMessage(),
							e.getErrorCode(), e.getSQLState());

				} else {

					if (e.getSQLState().startsWith("08")) { /* 08 prefix codes are connection exceptions in Derby */

						logger.log(Level.WARNING, "Registry connection lost... attempting to reconnect...");
						String reconnectEnabled = config.getProperty("registry.reconnect", "false");

						if (reconnectEnabled.equalsIgnoreCase("true")) {
							synchronized (monitor) {
								jdbcConnect();
							}
						}

					} else { // anything else, log it and throw exception

						logger.log(Level.WARNING, "Failed to execute update statement: " + sqlString, e);
						// log.log(this, ILogger.ERROR, Fabric.message("registry.update.failed"));
						throw new PersistenceException("Error occurred exeuting update.", e.getMessage(), e
								.getErrorCode(), e.getSQLState());

					}
				}

			} finally {

				if (stmt != null) {

					try {
						stmt.close();
					} catch (SQLException e) {
						logger.log(Level.WARNING, "Error closing statement.", e);
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

		if (fabricConnection != null) {
			try {
				Statement stmt = fabricConnection.createStatement();
				for (int z = 0; z < sqlStrings.length; z++) {
					stmt.addBatch(sqlStrings[z]);
				}
				int[] rowCounts = stmt.executeBatch();
				logger.log(Level.FINEST, "{0} rows updated", new Object[] {rowCounts});
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
		if (fabricConnection != null) {
			try {
				stmt = fabricConnection.createStatement();
				rs = stmt.executeQuery(sqlString);
				queryResult = new DistributedQueryResult(nodeName, rs);
			} catch (SQLException sqlEx) {
				thrownException = sqlEx;
			} finally {
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException ex1) {
						logger.log(Level.WARNING, "Error closing result set!", thrownException);
						// log.log(this, ILogger.ERROR, Fabric.message("registry.resultset.failed"));
					}
				}
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException ex2) {
						logger.log(Level.WARNING, "Error closing statement!", thrownException);
						// log.log(this, ILogger.ERROR, Fabric.message("registry.statement.failed"));
					}
				}
			}
		} else {
			logger.log(Level.WARNING, "Fabric Registry Error - connection is null!", thrownException);
		}

		if (thrownException != null) {
			logger.log(Level.WARNING, "Failed to execute Fabric Registry query: " + sqlString, thrownException);
			logger.log(Level.WARNING, "SQLState: " + thrownException.getSQLState());
			logger.log(Level.WARNING, "ErrorCode: " + thrownException.getErrorCode());
		}

		if ((thrownException != null && thrownException.getSQLState().startsWith("08")) || fabricConnection == null) {
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

}