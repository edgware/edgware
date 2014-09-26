/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2009, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.persistence.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.core.properties.Properties;
import fabric.registry.FabricRegistry;
import fabric.registry.RegistryObject;
import fabric.registry.exception.PersistenceException;
import fabric.registry.impl.AbstractFactory;

/**
 * The GAIAN/Derby JDBC based implementation of persistence
 */
public class GaianPersistence extends SingletonJDBCPersistence {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2014";

	private final static String CLASS_NAME = GaianPersistence.class.getName();
	private final static String PACKAGE_NAME = GaianPersistence.class.getPackage().getName();
	
	private final static Logger logger= Logger.getLogger(PACKAGE_NAME);

	public GaianPersistence() {

	}

	@Override
	public void init(String Url, Properties config) {

		this.config = config;
		this.fabricDbUrl = Url;
	}

	/**
	 * Maps SQL tablenames to gaian equivalent for distributed queries
	 * 
	 * @param sql
	 * @return
	 */
	private String mapSQLForGaian(String sql) {

		String METHOD_NAME = "mapSQLForGaian";
		logger.entering(CLASS_NAME, METHOD_NAME, sql);
		if (logger.isLoggable(Level.FINE)) {
			/* Split on FABRIC. as an indicator for table names, this way we can debug any we may be missing */
			String[] sqlParts = sql.split("FABRIC.");
			for (int i = 0; i < sqlParts.length; i++) {
				logger.fine("Found part '" + sqlParts[i] + "'");
			}
		}

		sql = sql.replaceAll(FabricRegistry.TASK_SUBSCRIPTIONS, "FABRIC.G_TASK_SUBSCRIPTIONS");
		sql = sql.replaceAll(FabricRegistry.NODES, "FABRIC.G_NODES");
		sql = sql.replaceAll(FabricRegistry.NODE_TYPES, "FABRIC.G_NODE_TYPES");
		sql = sql.replaceAll(FabricRegistry.PLATFORM_TYPES, "FABRIC.G_PLATFORM_TYPES");
		sql = sql.replaceAll(FabricRegistry.ACTOR_TYPES, "FABRIC.G_ACTOR_TYPES");
		sql = sql.replaceAll(FabricRegistry.FEED_TYPES, "FABRIC.G_FEED_TYPES");
		sql = sql.replaceAll(FabricRegistry.ROUTES, "FABRIC.G_ROUTES");
		sql = sql.replaceAll(FabricRegistry.NODE_NEIGHBOURS, "FABRIC.G_NODE_NEIGHBOURS");
		sql = sql.replaceAll(FabricRegistry.TASK_SYSTEMS, "FABRIC.G_TASK_SERVICES");
		sql = sql.replaceAll(FabricRegistry.ACTORS, "FABRIC.G_ACTORS");
		sql = sql.replaceAll(FabricRegistry.TASK_NODES, "FABRIC.G_TASK_NODES");
		sql = sql.replaceAll(FabricRegistry.TASKS, "FABRIC.G_TASKS");
		sql = sql.replaceAll(FabricRegistry.SYSTEM_TYPES, "FABRIC.G_SERVICE_TYPES");
		sql = sql.replaceAll(FabricRegistry.PLATFORMS, "FABRIC.G_PLATFORMS");
		sql = sql.replaceAll(FabricRegistry.NODE_IP_MAPPING, "FABRIC.G_NODE_IP_MAPPING");
		sql = sql.replaceAll(FabricRegistry.BEARERS, "FABRIC.G_BEARERS");
		sql = sql.replaceAll(FabricRegistry.SYSTEMS, "FABRIC.G_SERVICES");
		sql = sql.replaceAll(FabricRegistry.SYSTEM_WIRING, "FABRIC.G_SERVICE_WIRING");
		sql = sql.replaceAll(FabricRegistry.MESSAGE_CACHE, "FABRIC.G_MESSAGE_CACHE");
		sql = sql.replaceAll(FabricRegistry.COMPOSITE_PARTS, "FABRIC.G_COMPOSITE_PARTS");
		sql = sql.replaceAll(FabricRegistry.COMPOSITE_SYSTEMS, "FABRIC.G_COMPOSITE_SERVICES");
		sql = sql.replaceAll(FabricRegistry.DATA_FEEDS, "FABRIC.G_DATA_FEEDS");

		// switch on tablename and convert
		// rather than insert G_?

		// SQL WHICH CREATES THE GAIAN VIEWS.. which aren't yet mapped
		// -- TASK_PLUGINS view
		// create VIEW Fabric.G_TASK_PLUGINS as SELECT * FROM new com.ibm.db2j.GaianTable('TASK_PLUGINS',
		// 'with_provenance') TPLT;
		// -- ACTOR_PLUGINS view
		// create VIEW Fabric.G_ACTOR_PLUGINS as SELECT * FROM new com.ibm.db2j.GaianTable('ACTOR_PLUGINS',
		// 'with_provenance') APLT;
		// -- NODE_PLUGINS view
		// create VIEW Fabric.G_NODE_PLUGINS as SELECT * FROM new com.ibm.db2j.GaianTable('NODE_PLUGINS',
		// 'with_provenance') NPLT;
		// -- FABLET_PLUGINS view
		// create VIEW Fabric.G_FABLET_PLUGINS as SELECT * FROM new com.ibm.db2j.GaianTable('FABLET_PLUGINS',
		// 'with_provenance') FPLT;
		// -- SYSTEM_PLUGINS view
		// create VIEW Fabric.G_SYSTEM_PLUGINS as SELECT * FROM new com.ibm.db2j.GaianTable('SYSTEM_PLUGINS',
		// 'with_provenance') SPLT;

		logger.exiting(CLASS_NAME, METHOD_NAME, sql);
		return sql;
	}

	@Override
	public RegistryObject[] queryRegistryObjects(String queryString, AbstractFactory factory, boolean localOnly)
			throws PersistenceException {

		if (!localOnly) {
			queryString = mapSQLForGaian(queryString);
		}
		return super.queryRegistryObjects(queryString, factory, localOnly);
	}

	@Override
	public int queryInt(String queryString, boolean localOnly) throws PersistenceException {

		if (!localOnly) {
			queryString = mapSQLForGaian(queryString);
		}
		return super.queryInt(queryString, localOnly);
	}

	@Override
	public String queryString(String queryString, boolean localOnly) throws PersistenceException {

		if (!localOnly) {
			queryString = mapSQLForGaian(queryString);
		}
		return super.queryString(queryString, localOnly);
	}

	@Override
	public Object[] query(String queryString, boolean localOnly) throws PersistenceException {

		if (!localOnly) {
			queryString = mapSQLForGaian(queryString);
		}
		return super.query(queryString, localOnly);
	}

}