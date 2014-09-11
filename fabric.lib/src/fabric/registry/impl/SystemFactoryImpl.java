/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2011
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.FabricRegistry;
import fabric.registry.RegistryObject;
import fabric.registry.System;
import fabric.registry.SystemFactory;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.exception.RegistryQueryException;
import fabric.registry.persistence.IPersistenceResultRow;

/**
 * Implementation of the factory for <code>System</code>'s.
 */
public class SystemFactoryImpl extends AbstractFactory implements SystemFactory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2011";

	/*
	 * Class Constants
	 */

	protected static SystemFactoryImpl localQueryInstance = null;
	protected static SystemFactoryImpl remoteQueryInstance = null;

	/*
	 * Queries
	 */

	/** Select all records */
	private String SELECT_ALL_QUERY = null;
	/** Select record by ID */
	private String BY_ID_QUERY = null;
	/** Select records by type ID */
	private String BY_TYPE_QUERY = null;
	/** Select records for a given platform */
	private String BY_PLATFORM_QUERY = null;
	/** Select records using an arbitrary WHERE clause */
	private String PREDICATE_QUERY = null;
	/** Select all records of kind 'SYSTEM' */
	private String SELECT_ALL_SYSTEMS_QUERY = null;

	/*
	 * Static initialisation
	 */
	static {
		localQueryInstance = new SystemFactoryImpl(true);
		remoteQueryInstance = new SystemFactoryImpl(false);
	}

	private SystemFactoryImpl(boolean queryLocal) {

		this.localOnly = queryLocal;

		SELECT_ALL_QUERY = format("select * from %s", FabricRegistry.SYSTEMS);
		BY_ID_QUERY = format("select * from %s where ID='\\%s' AND PLATFORM_ID='\\%s'", FabricRegistry.SYSTEMS);
		BY_TYPE_QUERY = format("select * from %s where TYPE_ID='\\%s'", FabricRegistry.SYSTEMS);
		BY_PLATFORM_QUERY = format("select * from %s where PLATFORM_ID='\\%s'", FabricRegistry.SYSTEMS);
		PREDICATE_QUERY = format("select * from %s where \\%s", FabricRegistry.SYSTEMS);

		SELECT_ALL_SYSTEMS_QUERY = format("select * from %s where kind = '\\%s'", FabricRegistry.SYSTEMS,
				System.SERVICE_KIND);
	}

	/*
	 * Class methods
	 */
	public static SystemFactoryImpl getInstance(boolean queryLocal) {

		if (queryLocal) {
			return localQueryInstance;
		} else {
			return remoteQueryInstance;
		}
	}

	/*
	 * Instance methods
	 */

	/**
	 * @see fabric.registry.SystemFactory#createSystem(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public System createSystem(String platformID, String id, String typeID) {

		return createSystem(platformID, id, typeID, "SERVICE", null, null, null, 0.0, 0.0, 0.0, 0.0, 0.0, null, null,
				null);
	}

	/**
	 * @see fabric.registry.SystemFactory#createSystem(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String, double, double, double, double, double,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public System createSystem(String platformID, String systemId, String typeID, String securityClassification,
			String readiness, String availability, double latitude, double longitude, double altitude, double bearing,
			double velocity, String description, String attributes, String attributesURI) {

		return createSystem(platformID, systemId, typeID, "SERVICE", securityClassification, readiness, availability,
				latitude, longitude, altitude, bearing, velocity, description, attributes, attributesURI);
	}

	/**
	 * Instantiate a system using all supplied arguments.
	 * 
	 * @param platformID
	 * @param systemId
	 * @param typeID
	 * @param kind
	 * @param credentials
	 * @param readiness
	 * @param availability
	 * @param latitude
	 * @param longitude
	 * @param altitude
	 * @param bearing
	 * @param velocity
	 * @param description
	 * @param attributes
	 * @param attributesURI
	 * @return
	 */
	private System createSystem(String platformID, String systemId, String typeID, String kind, String credentials,
			String readiness, String availability, double latitude, double longitude, double altitude, double bearing,
			double velocity, String description, String attributes, String attributesURI) {

		System s = new SystemImpl(platformID, systemId, typeID, "SERVICE", credentials, readiness, availability,
				latitude, longitude, altitude, bearing, velocity, description, attributes, attributesURI);
		return s;
	}

	/**
	 * @see fabric.registry.SystemFactory#getAll()
	 */
	@Override
	public System[] getAll() {

		System[] systems = null;
		try {
			systems = runQuery(SELECT_ALL_QUERY);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return systems;
	}

	/**
	 * @see fabric.registry.SystemFactory#getAllSystems()
	 */
	@Override
	public System[] getAllSystems() {

		System[] systems = null;
		try {
			systems = runQuery(SELECT_ALL_SYSTEMS_QUERY);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return systems;
	}

	/**
	 * @see fabric.registry.SystemFactory#getSystems(java.lang.String)
	 */
	@Override
	public System[] getSystems(String queryPredicates) throws RegistryQueryException {

		System[] systems = null;
		try {
			String query = format(PREDICATE_QUERY, queryPredicates);
			systems = runQuery(query);
		} catch (PersistenceException e) {
			e.printStackTrace();
			throw new RegistryQueryException(e.getMessage());
		}
		return systems;
	}

	/**
	 * @see fabric.registry.SystemFactory#getSystemsById(java.lang.String, java.lang.String)
	 */
	@Override
	public System getSystemsById(String platformId, String systemId) {

		System system = null;
		try {
			String query = format(BY_ID_QUERY, systemId, platformId);
			System[] systems = runQuery(query);
			if (systems != null && systems.length > 0) {
				system = systems[0];
			}
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return system;
	}

	/**
	 * @see fabric.registry.SystemFactory#getSystemsByType(java.lang.String)
	 */
	@Override
	public System[] getSystemsByType(String type) {

		System[] systems = null;
		try {
			String query = format(BY_TYPE_QUERY, type);
			systems = runQuery(query);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return systems;
	}

	/**
	 * Run a query against the Registry for a list of systems.
	 */
	private System[] runQuery(String sql) throws PersistenceException {

		System[] systems = null;
		RegistryObject[] objects = queryRegistryObjects(sql, this);
		if (objects != null && objects.length > 0) {
			// necessary
			systems = new System[objects.length];
			for (int k = 0; k < objects.length; k++) {
				systems[k] = (System) objects[k];
			}
		} else {
			systems = new System[0];
		}
		return systems;
	}

	/**
	 * @see fabric.registry.Factory#delete(fabric.registry.RegistryObject)
	 */
	@Override
	public boolean delete(RegistryObject obj) {

		if (obj != null && obj instanceof System) {
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

		if (obj != null && obj instanceof System) {
			return super.save(obj, this);
		} else {
			return false;
		}
	}

	/**
	 * @see fabric.registry.impl.AbstractFactory#getDeleteSql(fabric.registry.RegistryObject)
	 */
	@Override
	public String getDeleteSql(RegistryObject obj) {

		StringBuffer buf = new StringBuffer();
		if (obj instanceof System) {
			System system = (System) obj;
			buf.append("delete from " + FabricRegistry.SYSTEMS + " where(");
			buf.append("ID=").append(nullOrString(system.getId())).append(" AND ");
			buf.append("Type_ID=").append(nullOrString(system.getTypeId())).append(" AND ");
			buf.append("Platform_ID=").append(nullOrString(system.getPlatformId())).append(")");
		}
		return buf.toString();
	}

	/**
	 * @see fabric.registry.impl.AbstractFactory#getUpdateSql(fabric.registry.RegistryObject)
	 */
	@Override
	public String getUpdateSql(RegistryObject obj) {

		StringBuffer buf = new StringBuffer();
		if (obj instanceof System) {
			System system = (System) obj;
			buf.append("update " + FabricRegistry.SYSTEMS + " set ");
			buf.append("ID=").append(nullOrString(system.getId())).append(",");
			buf.append("PLATFORM_ID=").append(nullOrString(system.getPlatformId())).append(",");
			buf.append("TYPE_ID=").append(nullOrString(system.getTypeId())).append(",");
			buf.append("KIND=").append(nullOrString(system.getKind())).append(",");
			buf.append("CREDENTIALS=").append(nullOrString(system.getCredentials())).append(",");
			buf.append("READINESS=").append(nullOrString(system.getReadiness())).append(",");
			buf.append("AVAILABILITY=").append(nullOrString(system.getAvailability())).append(",");
			buf.append("LATITUDE=").append(system.getLatitude()).append(",");
			buf.append("LONGITUDE=").append(system.getLongitude()).append(",");
			buf.append("ALTITUDE=").append(system.getAltitude()).append(",");
			buf.append("BEARING=").append(system.getBearing()).append(",");
			buf.append("VELOCITY=").append(system.getVelocity()).append(",");
			buf.append("DESCRIPTION=").append(nullOrString(system.getDescription())).append(",");
			buf.append("ATTRIBUTES=").append(nullOrString(system.getAttributes())).append(",");
			buf.append("ATTRIBUTES_URI=").append(nullOrString(system.getAttributesURI()));
			buf.append(" WHERE ");

			/* if it exists, use the shadow values for the WHERE clause */
			if (system.getShadow() != null) {
				System shadow = (System) system.getShadow();
				buf.append("ID=").append(nullOrString(shadow.getId())).append(" AND ");
				buf.append("PLATFORM_ID=").append(nullOrString(shadow.getPlatformId()));
			} else {
				buf.append("ID=").append(nullOrString(system.getId())).append(" AND ");
				buf.append("PLATFORM_ID=").append(nullOrString(system.getPlatformId()));
			}
		}
		return buf.toString();
	}

	/**
	 * @see fabric.registry.impl.AbstractFactory#getInsertSql(fabric.registry.RegistryObject)
	 */
	@Override
	public String getInsertSql(RegistryObject obj) {

		StringBuffer buf = new StringBuffer();
		if (obj instanceof System) {
			System system = (System) obj;
			buf.append("insert into " + FabricRegistry.SYSTEMS + " values(");
			buf.append(nullOrString(system.getPlatformId())).append(",");
			buf.append(nullOrString(system.getId())).append(",");
			buf.append(nullOrString(system.getTypeId())).append(",");
			buf.append(nullOrString(system.getKind())).append(",");
			buf.append(nullOrString(system.getCredentials())).append(",");
			buf.append(nullOrString(system.getReadiness())).append(",");
			buf.append(nullOrString(system.getAvailability())).append(",");
			buf.append(system.getLatitude()).append(",");
			buf.append(system.getLongitude()).append(",");
			buf.append(system.getAltitude()).append(",");
			buf.append(system.getBearing()).append(",");
			buf.append(system.getVelocity()).append(",");
			buf.append(nullOrString(system.getDescription())).append(",");
			buf.append(nullOrString(system.getAttributes())).append(",");
			buf.append(nullOrString(system.getAttributesURI())).append(")");
		}
		return buf.toString();
	}

	/**
	 * @see fabric.registry.impl.AbstractFactory#create(java.sql.ResultSet)
	 */
	@Override
	public RegistryObject create(IPersistenceResultRow row) throws PersistenceException {

		System system = null;
		if (row != null) {
			SystemImpl impl = new SystemImpl();
			impl.setPlatformId(row.getString(1));
			impl.setId(row.getString(2));
			impl.setTypeId(row.getString(3));
			impl.setKind(row.getString(4));
			impl.setCredentials(row.getString(5));
			impl.setReadiness(row.getString(6));
			impl.setAvailability(row.getString(7));
			impl.setLatitude(row.getDouble(8));
			impl.setLongitude(row.getDouble(9));
			impl.setAltitude(row.getDouble(10));
			impl.setBearing(row.getDouble(11));
			impl.setVelocity(row.getDouble(12));
			impl.setDescription(row.getString(13));
			impl.setAttributes(row.getString(14));
			impl.setAttributesURI(row.getString(15));

			/* preserve these values internally */
			impl.createShadow();

			system = impl;
		}
		return system;
	}

	/**
	 * @see fabric.registry.SystemFactory#getSystemsByPlatform(java.lang.String)
	 */
	@Override
	public System[] getSystemsByPlatform(String platformId) {

		System[] systems = null;
		try {
			String query = format(BY_PLATFORM_QUERY, platformId);
			systems = runQuery(query);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return systems;
	}

	/**
	 * @see fabric.registry.Factory#insert(fabric.registry.RegistryObject)
	 */
	@Override
	public boolean insert(RegistryObject obj) throws IncompleteObjectException, DuplicateKeyException,
			PersistenceException {

		if (obj != null && obj instanceof System) {
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

		if (obj != null && obj instanceof System) {
			return super.update(obj, this);
		} else {
			return false;
		}
	}

}
