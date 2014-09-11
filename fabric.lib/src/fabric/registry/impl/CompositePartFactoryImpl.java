/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2010
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.CompositePart;
import fabric.registry.CompositePartFactory;
import fabric.registry.FabricRegistry;
import fabric.registry.RegistryObject;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.exception.RegistryQueryException;
import fabric.registry.persistence.IPersistenceResultRow;

/**
 * Implementation of the factory for <code>CompositePart</code>s.
 */
public class CompositePartFactoryImpl extends AbstractFactory implements CompositePartFactory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010";

	/*
	 * Class Constants
	 */

	/** Factory for local (singleton) Registry operations */
	protected static CompositePartFactoryImpl localQueryInstance = null;
	/** Factory for distributed (gaian) Registry operations */
	protected static CompositePartFactoryImpl remoteQueryInstance = null;

	/*
	 * Queries
	 */
	/** Select all records */
	private String SELECT_ALL_QUERY = null;
	/** Select records using an arbitrary WHERE clause */
	private String PREDICATE_QUERY = null;
	/** Select record by id */
	private String BY_ID_QUERY = null;

	static {
		localQueryInstance = new CompositePartFactoryImpl(true);
		remoteQueryInstance = new CompositePartFactoryImpl(false);
	}

	public static CompositePartFactoryImpl getInstance(boolean queryLocal) {
		if (queryLocal) {
			return localQueryInstance;
		} else {
			return remoteQueryInstance;
		}
	}

	private CompositePartFactoryImpl(boolean queryLocal) {
		this.localOnly = queryLocal;

		SELECT_ALL_QUERY = format("select * from %s", FabricRegistry.COMPOSITE_PARTS);
		PREDICATE_QUERY = format("select * from %s where \\%s", FabricRegistry.COMPOSITE_PARTS);
		BY_ID_QUERY = format(
				"select * from %s where COMPOSITE_ID='\\%s' AND SERVICE_PLATFORM_ID='\\%s' AND SERVICE_ID='\\%s'",
				FabricRegistry.COMPOSITE_PARTS);
	}

	@Override
	public RegistryObject create(IPersistenceResultRow row) throws PersistenceException {
		CompositePart service = null;
		if (row != null) {
			String compositeId = row.getString(1);
			String servicePlatformId = row.getString(2);
			String serviceId = row.getString(3);
			String attrs = row.getString(4);
			String attrsURI = row.getString(5);
			CompositePartImpl impl = new CompositePartImpl(compositeId, servicePlatformId, serviceId, attrs, attrsURI);

			/* preserve these values internally */
			impl.createShadow();

			service = impl;
		}
		return service;
	}

	@Override
	public String getDeleteSql(RegistryObject obj) {
		StringBuffer buf = new StringBuffer();
		if (obj instanceof CompositePart) {
			CompositePart service = (CompositePart) obj;
			buf.append("delete from " + FabricRegistry.COMPOSITE_PARTS + " where (");
			buf.append("Composite_ID='").append(service.getCompositeId()).append("' AND ");
			buf.append("Service_Platform_ID='").append(service.getServicePlatformId()).append("' AND ");
			buf.append("Service_ID='").append(service.getServiceId()).append("')");
		}
		return buf.toString();
	}

	@Override
	public String getInsertSql(RegistryObject obj) {
		StringBuffer buf = new StringBuffer();
		if (obj instanceof CompositePart) {
			CompositePart service = (CompositePart) obj;
			buf.append("insert into " + FabricRegistry.COMPOSITE_PARTS + " values(");
			buf.append("'").append(service.getCompositeId()).append("',");
			buf.append("'").append(service.getServicePlatformId()).append("',");
			buf.append("'").append(service.getServiceId()).append("',");
			buf.append("").append(nullOrString(service.getAttributes())).append(",");
			buf.append("").append(nullOrString(service.getAttributesURI())).append(")");
		}
		return buf.toString();
	}

	@Override
	public String getUpdateSql(RegistryObject obj) {
		StringBuffer buf = new StringBuffer();
		if (obj instanceof CompositePart) {
			CompositePart service = (CompositePart) obj;
			buf.append("update " + FabricRegistry.COMPOSITE_PARTS + " set ");
			buf.append("COMPOSITE_ID='").append(service.getCompositeId()).append("',");
			buf.append("SERVICE_PLATFORM_ID='").append(service.getServicePlatformId()).append("',");
			buf.append("SERVICE_ID='").append(service.getServiceId()).append("',");
			buf.append("Attributes=").append(nullOrString(service.getAttributes())).append(",");
			buf.append("Attributes_URI=").append(nullOrString(service.getAttributesURI()));
			buf.append(" WHERE ");

			/* if it exists, use the shadow values for the WHERE clause */
			if (service.getShadow() != null) {
				CompositePart originalService = (CompositePart) service.getShadow();
				buf.append(" COMPOSITE_ID='").append(originalService.getCompositeId()).append("' AND ");
				buf.append(" SERVICE_PLATFORM_ID='").append(originalService.getServicePlatformId()).append("' AND ");
				buf.append(" SERVICE_ID='").append(originalService.getServiceId()).append("'");
			} else {
				buf.append(" COMPOSITE_ID='").append(service.getCompositeId()).append("' AND ");
				buf.append(" SERVICE_PLATFORM_ID='").append(service.getServicePlatformId()).append("' AND ");
				buf.append(" SERVICE_ID='").append(service.getServiceId()).append("'");
			}
		}
		return buf.toString();
	}

	@Override
	public CompositePart create(String compositeId, String servicePlatformId, String serviceId) {

		return create(compositeId, servicePlatformId, serviceId, null, null);
	}

	@Override
	public CompositePart create(String compositeId, String servicePlatformId, String serviceId, String attributes,
			String attributesURI) {

		CompositePart cp = new CompositePartImpl(compositeId, servicePlatformId, serviceId, attributes, attributesURI);
		return cp;
	}

	@Override
	public CompositePart[] get(String queryPredicates) throws RegistryQueryException {

		CompositePart[] systems = null;
		try {
			String query = format(PREDICATE_QUERY, queryPredicates);
			systems = queryServices(query);
		} catch (PersistenceException e) {
			e.printStackTrace();
			throw new RegistryQueryException(e.getMessage());
		}
		return systems;
	}

	@Override
	public CompositePart[] getAll() {

		CompositePart[] systems = null;
		try {
			String query = format(SELECT_ALL_QUERY);
			systems = queryServices(query);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return systems;
	}

	@Override
	public CompositePart getById(String compositeId, String servicePlatformId, String serviceId) {

		CompositePart service = null;
		try {
			String query = format(BY_ID_QUERY, compositeId, servicePlatformId, serviceId);
			CompositePart[] systems = queryServices(query);
			if (systems != null && systems.length > 0) {
				service = systems[0]; /* pick the first one - ignore any duplicates */
			}
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return service;
	}

	private CompositePart[] queryServices(String sql) throws PersistenceException {
		CompositePart[] systems = null;
		RegistryObject[] objects = queryRegistryObjects(sql, this);
		if (objects != null && objects.length > 0) {
			systems = new CompositePart[objects.length];
			for (int x = 0; x < objects.length; x++) {
				systems[x] = (CompositePart) objects[x];
			}
		} else {
			systems = new CompositePart[0];
		}
		return systems;
	}

	@Override
	public boolean delete(RegistryObject obj) {
		if (obj != null && obj instanceof CompositePart) {
			return super.delete(obj, this);
		} else {
			return false;
		}
	}

	@Override
	public boolean insert(RegistryObject obj) throws IncompleteObjectException, DuplicateKeyException,
			PersistenceException {

		if (obj != null && obj instanceof CompositePart) {
			return super.insert(obj, this);
		} else {
			return false;
		}
	}

	@Override
	public boolean save(RegistryObject obj) throws IncompleteObjectException {

		if (obj != null && obj instanceof CompositePart) {
			return super.save(obj, this);
		} else {
			return false;
		}
	}

	@Override
	public boolean update(RegistryObject obj) throws IncompleteObjectException, PersistenceException {

		if (obj != null && obj instanceof CompositePart) {
			return super.update(obj, this);
		} else {
			return false;
		}
	}

}
