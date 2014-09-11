/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.Bearer;
import fabric.registry.BearerFactory;
import fabric.registry.FabricRegistry;
import fabric.registry.RegistryObject;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.exception.RegistryQueryException;
import fabric.registry.persistence.IPersistenceResultRow;

/**
 * Implementation of the factory for <code>Bearer</code>s.
 */
public class BearerFactoryImpl extends AbstractFactory implements BearerFactory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/** Factory for centralised (singleton) Registry operations */
	private static BearerFactoryImpl localQueryInstance = null;

	/** Factory for distributed (gaian) Registry operations */
	private static BearerFactoryImpl remoteQueryInstance = null;

	/*
	 * Queries
	 */

	/** Select all records */
	private String SELECT_ALL_QUERY = null;

	/** Select records by ID */
	private String BY_ID_QUERY = null;

	/** Select records using an arbitrary WHERE clause */
	private String PREDICATE_QUERY = null;

	static {
		localQueryInstance = new BearerFactoryImpl(true);
		remoteQueryInstance = new BearerFactoryImpl(false);
	}

	public static BearerFactory getInstance(boolean queryLocal) {

		if (queryLocal) {
			return localQueryInstance;
		} else {
			return remoteQueryInstance;
		}
	}

	private BearerFactoryImpl(boolean queryLocal) {

		this.localOnly = queryLocal;

		SELECT_ALL_QUERY = format("select * from %s", FabricRegistry.BEARERS);
		BY_ID_QUERY = format("select * from %s where BEARER_ID='\\%s'", FabricRegistry.BEARERS);
		PREDICATE_QUERY = format("select * from %s where \\%s", FabricRegistry.BEARERS);
	}

	@Override
	public RegistryObject create(IPersistenceResultRow row) throws PersistenceException {

		Bearer bearer = null;
		if (row != null) {
			BearerImpl impl = new BearerImpl();
			impl.setId(row.getString(1));
			impl.setAvailability(row.getString(2));
			impl.setDescription(row.getString(3));
			impl.setAttributes(row.getString(4));
			impl.setAttributesURI(row.getString(5));

			/* Preserve these values internally */
			impl.createShadow();

			bearer = impl;
		}
		return bearer;
	}

	@Override
	public String getDeleteSql(RegistryObject obj) {

		StringBuffer buf = new StringBuffer();
		if (obj instanceof Bearer) {
			Bearer bearer = (Bearer) obj;
			buf.append("delete from " + FabricRegistry.BEARERS + " where(");
			buf.append("Bearer_ID='").append(bearer.getId()).append("')");
		}
		return buf.toString();
	}

	@Override
	public String getInsertSql(RegistryObject obj) {

		StringBuffer buf = new StringBuffer();
		if (obj instanceof Bearer) {
			Bearer bearer = (Bearer) obj;
			buf.append("insert into " + FabricRegistry.BEARERS + "  values(");
			buf.append("'").append(bearer.getId()).append("',");
			buf.append("'").append(bearer.getAvailability()).append("',");
			buf.append("'").append(bearer.getDescription()).append("',");
			buf.append("'").append(bearer.getAttributes()).append("',");
			buf.append("'").append(bearer.getAttributesURI()).append("')");
		}
		return buf.toString();
	}

	@Override
	public String getUpdateSql(RegistryObject obj) {

		StringBuffer buf = new StringBuffer();
		if (obj instanceof Bearer) {
			Bearer bearer = (Bearer) obj;
			buf.append("update " + FabricRegistry.BEARERS + " set ");
			buf.append("BEARER_ID='").append(bearer.getId()).append("',");
			buf.append("AVAILABILITY='").append(bearer.getAvailability()).append("',");
			buf.append("DESCRIPTION='").append(bearer.getDescription()).append("',");
			buf.append("ATTRIBUTES='").append(bearer.getAttributes()).append("',");
			buf.append("ATTRIBUTES_URI='").append(bearer.getAttributesURI()).append("'");
			buf.append(" WHERE ");

			/* If it exists, use the shadow values for the WHERE clause */
			if (bearer.getShadow() != null) {
				Bearer shadow = (Bearer) bearer.getShadow();
				buf.append("BEARER_ID='").append(shadow.getId()).append("'");
			} else {
				buf.append("BEARER_ID='").append(bearer.getId()).append("'");
			}
		}
		return buf.toString();
	}

	/**
	 * @see fabric.registry.NodeFactory#createBearer(java.lang.String, java.lang.String)
	 */
	@Override
	public Bearer createBearer(String id, String availability) {

		return createBearer(id, availability, null, null, null);
	}

	/**
	 * @see fabric.registry.BearerFactory#createBearer(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	@Override
	public Bearer createBearer(String id, String availability, String description, String attributes,
			String attributesURI) {

		return new BearerImpl(id, availability, description, attributes, attributesURI);
	}

	/**
	 * @see fabric.registry.BearerFactory#getAllBearers()
	 */
	@Override
	public Bearer[] getAllBearers() {

		Bearer[] bearers = null;
		try {
			bearers = runQuery(SELECT_ALL_QUERY);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return bearers;
	}

	@Override
	public Bearer getBearerById(String id) {

		Bearer bearer = null;
		try {
			String query = format(BY_ID_QUERY, id);
			Bearer[] bearers = runQuery(query);
			if (bearers != null && bearers.length > 0) {
				bearer = bearers[0];
			}
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return bearer;
	}

	@Override
	public Bearer[] getBearers(String queryPredicates) throws RegistryQueryException {

		Bearer[] bearers = null;
		try {
			String query = format(PREDICATE_QUERY, queryPredicates);
			bearers = runQuery(query);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return bearers;
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.Factory#delete(fabric.registry.RegistryObject)
	 */
	@Override
	public boolean delete(RegistryObject obj) {

		if (obj != null && obj instanceof Bearer) {
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

		if (obj != null && obj instanceof Bearer) {
			return super.save(obj, this);
		} else {
			return false;
		}
	}

	private Bearer[] runQuery(String sql) throws PersistenceException {

		Bearer[] bearers = null;
		RegistryObject[] objects = queryRegistryObjects(sql, this);
		if (objects != null && objects.length > 0) {
			// Necessary
			bearers = new Bearer[objects.length];
			for (int k = 0; k < objects.length; k++) {
				bearers[k] = (Bearer) objects[k];
			}
		} else {
			bearers = new Bearer[0];
		}
		return bearers;
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.Factory#insert(fabric.registry.RegistryObject)
	 */
	@Override
	public boolean insert(RegistryObject obj) throws IncompleteObjectException, DuplicateKeyException,
			PersistenceException {

		if (obj != null && obj instanceof Bearer) {
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

		if (obj != null && obj instanceof Bearer) {
			return super.update(obj, this);
		} else {
			return false;
		}
	}

}
