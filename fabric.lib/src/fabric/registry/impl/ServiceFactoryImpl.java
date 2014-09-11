/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2011
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.Fabric;
import fabric.registry.FabricRegistry;
import fabric.registry.RegistryObject;
import fabric.registry.Service;
import fabric.registry.ServiceFactory;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.exception.RegistryQueryException;
import fabric.registry.persistence.IPersistenceResultRow;

/**
 * Implementation of the factory for <code>Service</code>s.
 */
public class ServiceFactoryImpl extends AbstractFactory implements ServiceFactory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2011";

	/*
	 * Class Constants
	 */

	/** Factory for local (singleton) Registry operations */
	protected static ServiceFactoryImpl localQueryInstance = null;

	/** Factory for distributed (gaian) Registry operations */
	protected static ServiceFactoryImpl remoteQueryInstance = null;

	/*
	 * Queries
	 */

	/** Select all records */
	private String SELECT_ALL_QUERY = null;

	/** Select records by type */
	private String BY_TYPE_QUERY = null;

	/** Select records for a particular platform and system */
	private String BY_SYSTEM_QUERY = null;

	/** Select records using an arbitrary WHERE clause */
	private String PREDICATE_QUERY = null;

	/** Select record by id */
	private String BY_ID_QUERY = null;

	/** Select all INPUT feeds */
	private String SELECT_ALL_INPUT_QUERY = null;

	/** Select all OUTPUT feeds */
	private String SELECT_ALL_OUTPUT_QUERY = null;

	/** Select all SOLICIT RESPONSE feeds */
	private String SELECT_ALL_SOLICIT_RESPONSE_QUERY = null;

	/** Select all REQUEST RESPONSE feeds */
	private String SELECT_ALL_REQUEST_RESPONSE_QUERY = null;

	/** Select all NOTIFICATION feeds */
	private String SELECT_ALL_NOTIFICATION_QUERY = null;

	/** Select all ONE WAY feeds */
	private String SELECT_ALL_ONE_WAY_QUERY = null;

	static {
		localQueryInstance = new ServiceFactoryImpl(true);
		remoteQueryInstance = new ServiceFactoryImpl(false);
	}

	public static ServiceFactoryImpl getInstance(boolean queryLocal) {

		if (queryLocal) {
			return localQueryInstance;
		} else {
			return remoteQueryInstance;
		}
	}

	private ServiceFactoryImpl(boolean queryLocal) {

		this.localOnly = queryLocal;

		SELECT_ALL_QUERY = Fabric.format("select * from %s", FabricRegistry.DATA_FEEDS);

		BY_TYPE_QUERY = Fabric.format("select * from %s where TYPE_ID='\\%s'", FabricRegistry.DATA_FEEDS);

		BY_SYSTEM_QUERY = Fabric.format("select * from %s where SERVICE_ID='\\%s' AND PLATFORM_ID='\\%s'",
				FabricRegistry.DATA_FEEDS);

		PREDICATE_QUERY = Fabric.format("select * from %s where \\%s", FabricRegistry.DATA_FEEDS);

		BY_ID_QUERY = Fabric.format("select * from %s where PLATFORM_ID='\\%s' AND SERVICE_ID='\\%s' AND ID='\\%s'",
				FabricRegistry.DATA_FEEDS);

		SELECT_ALL_INPUT_QUERY = Fabric.format("select * from %s where direction='%s'", FabricRegistry.DATA_FEEDS,
				Service.MODE_INPUT_FEED);

		SELECT_ALL_OUTPUT_QUERY = Fabric.format("select * from %s where direction='%s'", FabricRegistry.DATA_FEEDS,
				Service.MODE_OUTPUT_FEED);

		SELECT_ALL_SOLICIT_RESPONSE_QUERY = Fabric.format("select * from %s where direction='%s'",
				FabricRegistry.DATA_FEEDS, Service.MODE_SOLICIT_RESPONSE);

		SELECT_ALL_REQUEST_RESPONSE_QUERY = Fabric.format("select * from %s where direction='%s'",
				FabricRegistry.DATA_FEEDS, Service.MODE_REQUEST_RESPONSE);

		SELECT_ALL_NOTIFICATION_QUERY = Fabric.format("select * from %s where direction='%s'",
				FabricRegistry.DATA_FEEDS, Service.MODE_NOTIFICATION);

		SELECT_ALL_ONE_WAY_QUERY = Fabric.format("select * from %s where direction='%s'", FabricRegistry.DATA_FEEDS,
				Service.MODE_LISTENER);
	}

	/**
	 * @see fabric.registry.ServiceFactory#createOutputFeed(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public Service createOutputFeed(String platformId, String systemId, String id, String typeID) {

		return createService(platformId, systemId, id, typeID, Service.MODE_OUTPUT_FEED, null, null, null, null, null);
	}

	/**
	 * @see fabric.registry.ServiceFactory#createInputFeed(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public Service createInputFeed(String platformId, String systemId, String id, String typeID) {

		return createService(platformId, systemId, id, typeID, Service.MODE_INPUT_FEED, null, null, null, null, null);
	}

	/**
	 * @see fabric.registry.ServiceFactory#createSolicitRequest(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public Service createSolicitRequest(String platformId, String systemId, String serviceId, String typeId) {

		return createService(platformId, systemId, serviceId, serviceId, Service.MODE_SOLICIT_RESPONSE, null, null,
				null, null, null);
	}

	/**
	 * @see fabric.registry.ServiceFactory#createRequestResponse(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public Service createRequestResponse(String platformId, String systemId, String serviceId, String typeId) {

		return createService(platformId, systemId, serviceId, serviceId, Service.MODE_REQUEST_RESPONSE, null, null,
				null, null, null);
	}

	/**
	 * @see fabric.registry.ServiceFactory#createNotification(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public Service createNotification(String platformId, String systemId, String serviceId, String typeId) {

		return createService(platformId, systemId, serviceId, serviceId, Service.MODE_NOTIFICATION, null, null, null,
				null, null);
	}

	/**
	 * @see fabric.registry.ServiceFactory#createListener(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public Service createListener(String platformId, String systemId, String serviceId, String typeId) {

		return createService(platformId, systemId, serviceId, typeId, Service.MODE_LISTENER, null, null, null, null,
				null);
	}

	/**
	 * @see fabric.registry.ServiceFactory#createService(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public Service createService(String platformId, String systemId, String id, String typeID, String mode,
			String credentials, String availability, String description, String attributes, String attributesURI) {

		Service f = new ServiceImpl(platformId, systemId, id, typeID, mode, credentials, availability, description,
				attributes, attributesURI);
		return f;
	}

	/**
	 * @see fabric.registry.ServiceFactory#getAllServices()
	 */
	@Override
	public Service[] getAllServices() {

		Service[] services = null;
		try {
			services = queryServices(SELECT_ALL_QUERY);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return services;
	}

	/**
	 * @see fabric.registry.ServiceFactory#getAllInputFeeds()
	 */
	@Override
	public Service[] getAllInputFeeds() {

		Service[] services = null;
		try {
			services = queryServices(SELECT_ALL_INPUT_QUERY);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return services;
	}

	/**
	 * @see fabric.registry.ServiceFactory#getAllOutputFeeds()
	 */
	@Override
	public Service[] getAllOutputFeeds() {

		Service[] services = null;
		try {
			services = queryServices(SELECT_ALL_OUTPUT_QUERY);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return services;
	}

	/**
	 * @see fabric.registry.ServiceFactory#getAllSolicitResponses()
	 */
	@Override
	public Service[] getAllSolicitResponses() {

		Service[] services = null;
		try {
			services = queryServices(SELECT_ALL_SOLICIT_RESPONSE_QUERY);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return services;
	}

	/**
	 * @see fabric.registry.ServiceFactory#getAllRequestResponses()
	 */
	@Override
	public Service[] getAllRequestResponses() {

		Service[] services = null;
		try {
			services = queryServices(SELECT_ALL_REQUEST_RESPONSE_QUERY);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return services;
	}

	/**
	 * @see fabric.registry.ServiceFactory#getAllNotifications()
	 */
	@Override
	public Service[] getAllNotifications() {

		Service[] services = null;
		try {
			services = queryServices(SELECT_ALL_NOTIFICATION_QUERY);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return services;
	}

	/**
	 * @see fabric.registry.ServiceFactory#getAllListeners()
	 */
	@Override
	public Service[] getAllListeners() {

		Service[] services = null;
		try {
			services = queryServices(SELECT_ALL_ONE_WAY_QUERY);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return services;
	}

	/**
	 * @see fabric.registry.ServiceFactory#getServices(java.lang.String)
	 */
	@Override
	public Service[] getServices(String queryPredicates) throws RegistryQueryException {

		Service[] services = null;
		try {
			String query = Fabric.format(PREDICATE_QUERY, queryPredicates);
			services = queryServices(query);
		} catch (PersistenceException e) {
			e.printStackTrace();
			throw new RegistryQueryException(e.getMessage());
		}
		return services;
	}

	/**
	 * @see fabric.registry.ServiceFactory#getServicesByType(java.lang.String)
	 */
	@Override
	public Service[] getServicesByType(String type) {

		Service[] services = null;
		try {
			String query = Fabric.format(BY_TYPE_QUERY, type);
			services = queryServices(query);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return services;
	}

	/**
	 * Run a query for a list of Service objects.
	 * 
	 * @param sql
	 *            - the SQL query to execute.
	 * @return a list of Service objects or an empty list if no matches were found.
	 * @throws PersistenceException
	 */
	private Service[] queryServices(String sql) throws PersistenceException {

		Service[] services = null;
		RegistryObject[] objects = queryRegistryObjects(sql, this);
		if (objects != null && objects.length > 0) {
			services = new Service[objects.length];
			for (int x = 0; x < objects.length; x++) {
				services[x] = (Service) objects[x];
			}
		} else {
			services = new Service[0];
		}
		return services;
	}

	/**
	 * @see fabric.registry.Factory#save(fabric.registry.RegistryObject)
	 */
	@Override
	public boolean save(RegistryObject obj) throws IncompleteObjectException {

		if (obj != null && obj instanceof Service) {
			return super.save(obj, this);
		} else {
			return false;
		}
	}

	/**
	 * @see fabric.registry.Factory#delete(fabric.registry.RegistryObject)
	 */
	@Override
	public boolean delete(RegistryObject obj) {

		if (obj != null && obj instanceof Service) {
			return super.delete(obj, this);
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
		if (obj instanceof Service) {
			Service service = (Service) obj;
			buf.append("delete from " + FabricRegistry.DATA_FEEDS + " where(");
			buf.append("Platform_ID=").append(nullOrString(service.getPlatformId())).append(" AND ");
			buf.append("SERVICE_ID=").append(nullOrString(service.getSystemId())).append(" AND ");
			buf.append("ID=").append(nullOrString(service.getId())).append(")");
		}
		return buf.toString();
	}

	/**
	 * @see fabric.registry.impl.AbstractFactory#getUpdateSql(fabric.registry.RegistryObject)
	 */
	@Override
	public String getUpdateSql(RegistryObject obj) {

		StringBuffer buf = new StringBuffer();
		if (obj instanceof Service) {
			Service service = (Service) obj;
			buf.append("update " + FabricRegistry.DATA_FEEDS + " set ");
			buf.append("PLATFORM_ID=").append(nullOrString(service.getPlatformId())).append(",");
			buf.append("SERVICE_ID=").append(nullOrString(service.getSystemId())).append(",");
			buf.append("ID=").append(nullOrString(service.getId())).append(",");
			buf.append("Type_ID=").append(nullOrString(service.getTypeId())).append(",");
			buf.append("Credentials=").append(nullOrString(service.getCredentials())).append(",");
			buf.append("Availability=").append(nullOrString(service.getAvailability())).append(",");
			buf.append("Description=").append(nullOrString(service.getDescription())).append(",");
			buf.append("Attributes=").append(nullOrString(service.getAttributes())).append(",");
			buf.append("Direction=").append(nullOrString(service.getMode())).append(",");
			buf.append("Attributes_URI=").append(nullOrString(service.getAttributesURI()));
			buf.append(" WHERE ");

			/* if it exists, use the shadow values for the WHERE clause */
			if (service.getShadow() != null) {
				Service originalService = (Service) service.getShadow();
				buf.append(" PLATFORM_ID=").append(nullOrString(originalService.getPlatformId())).append(" AND");
				buf.append(" SERVICE_ID=").append(nullOrString(originalService.getSystemId())).append(" AND");
				buf.append(" ID=").append(nullOrString(originalService.getId()));
			} else {
				buf.append(" PLATFORM_ID=").append(nullOrString(service.getPlatformId())).append(" AND");
				buf.append(" SERVICE_ID=").append(nullOrString(service.getSystemId())).append(" AND");
				buf.append(" ID=").append(nullOrString(service.getId()));
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
		if (obj instanceof Service) {
			Service service = (Service) obj;
			buf.append("insert into " + FabricRegistry.DATA_FEEDS + " values(");
			buf.append(nullOrString(service.getPlatformId())).append(",");
			buf.append(nullOrString(service.getSystemId())).append(",");
			buf.append(nullOrString(service.getId())).append(",");
			buf.append(nullOrString(service.getTypeId())).append(",");
			buf.append(nullOrString(service.getMode())).append(",");
			buf.append(nullOrString(service.getCredentials())).append(",");
			buf.append(nullOrString(service.getAvailability())).append(",");
			buf.append(nullOrString(service.getDescription())).append(",");
			buf.append(nullOrString(service.getAttributes())).append(",");
			buf.append(nullOrString(service.getAttributesURI())).append(")");
		}
		return buf.toString();
	}

	/**
	 * @see fabric.registry.impl.AbstractFactory#create(java.sql.ResultSet)
	 */
	@Override
	public Service create(IPersistenceResultRow row) throws PersistenceException {

		Service service = null;
		if (row != null) {
			String platformId = row.getString(1);
			String systemId = row.getString(2);
			String id = row.getString(3);
			String typeID = row.getString(4);
			String direction = row.getString(5);
			String sc = row.getString(6);
			String availability = row.getString(7);
			String desc = row.getString(8);
			String attrs = row.getString(9);
			String attrsURI = row.getString(10);
			ServiceImpl impl = new ServiceImpl(platformId, systemId, id, typeID, direction, sc, availability, desc,
					attrs, attrsURI);

			/* preserve these values internally */
			impl.createShadow();

			service = impl;
		}
		return service;
	}

	/**
	 * @see fabric.registry.ServiceFactory#getServicesBySystem(java.lang.String, java.lang.String)
	 */
	@Override
	public Service[] getServicesBySystem(String platformId, String serviceId) {

		Service[] services = null;
		try {
			String query = Fabric.format(BY_SYSTEM_QUERY, serviceId, platformId);
			services = queryServices(query);
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

		if (obj != null && obj instanceof Service) {
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

		if (obj != null && obj instanceof Service) {
			return super.update(obj, this);
		} else {
			return false;
		}
	}

	/**
	 * @see fabric.registry.ServiceFactory#getServiceById(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Service getServiceById(String platformId, String serviceId, String feedId) {

		Service service = null;
		try {
			String query = Fabric.format(BY_ID_QUERY, platformId, serviceId, feedId);
			Service[] services = queryServices(query);
			if (services != null && services.length > 0) {
				service = services[0]; /* pick the first one - ignore any duplicates */
			}
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return service;
	}

}