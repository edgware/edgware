/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2009, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.persistence;

import fabric.core.properties.Properties;
import fabric.registry.RegistryObject;
import fabric.registry.exception.PersistenceException;
import fabric.registry.impl.AbstractFactory;

/**
 * The persistence interface defines how an implementation could connect and disconnect to the underlying persistence
 * method. It also defines methods for retrieving and storing registry objects.
 */

public interface Persistence {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2014";

	/**
	 * 
	 * @param Url
	 *            a type/protocol-specific string that can be used to connect to the Registry
	 * @param config
	 *            the fabric configuration properties
	 * @throws PersistenceException
	 */
	public void init(String Url, Properties config) throws PersistenceException;

	/**
	 * Establishes a connection to the persistent data store.
	 * 
	 * @throws PersistenceException
	 */
	public void connect() throws PersistenceException;

	/**
	 * Drops the connection to the persistent data store.
	 * 
	 * @throws PersistenceException
	 */
	public void disconnect() throws PersistenceException;
	
	/**
	 * 
	 * Config for the Persistence which cannot be read from the Registry until connection has completed.
	 * 
	 * This method is called after connection and node setup to establish any config which can be defined within the registry
	 * 
	 */
	public void initNodeConfig(Properties nodeConfig) throws PersistenceException;

	/**
	 * Run a query for Registry objects using the specified SQL SELECT statements.
	 * 
	 * This method will return all columns for matching rows in the database.
	 * 
	 * @param queryString
	 *            - the SELECT statement to execute.
	 * @param factory
	 *            - the object factory used to convert each row into the appropriate registry object.
	 * @param localOnly
	 *            - indicates whether the query should only reflect local registry only
	 * @return an array of objects or null if no results were returned from the database.
	 * 
	 * @throws PersistenceException
	 *             if an error occurs running the specified SELECT statement.
	 */
	public RegistryObject[] queryRegistryObjects(String queryString, AbstractFactory factory, boolean localOnly)
			throws PersistenceException;

	/**
	 * Run a query that is only expected to return a simple string value. This method can be used for cases where only a
	 * single value is required (e.g. to check a particularly field of a certain object).
	 * 
	 * @param queryString
	 * @param localOnly
	 *            - indicates whether the query should reflect local registry only
	 * 
	 * @return an integer value.
	 * 
	 * @throws PersistenceException
	 *             - if the SELECT results in an error or an integer value is not being returned.
	 */
	public String queryString(String sqlString, boolean localOnly) throws PersistenceException;

	/**
	 * Runs a generic query and expresses the results as an array of Object arrays.
	 * 
	 * @param queryString
	 *            - the SQL SELECT to execute.
	 * @param localOnly
	 *            - indicates whether the query should reflect local registry only
	 * @return the matches for the query or an empty list otherwise.
	 * 
	 * @throws PersistenceException
	 */
	public Object[] query(String queryString, boolean localOnly) throws PersistenceException;

	public boolean updateRegistryObject(String updateString, boolean ignoreDuplicateWarning)
			throws PersistenceException;

	public boolean updateRegistryObject(String updateString) throws PersistenceException;

	public boolean updateRegistryObjects(String[] updateStrings) throws PersistenceException;
}