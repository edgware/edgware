/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.FabricRegistry;
import fabric.registry.NodeConfig;
import fabric.registry.NodeConfigFactory;
import fabric.registry.RegistryObject;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.exception.RegistryQueryException;
import fabric.registry.persistence.IPersistenceResultRow;

/**
 * Implementation of the factory for <code>NodeConfig</code>s.
 */
public class NodeConfigFactoryImpl extends AbstractFactory implements NodeConfigFactory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/** Factory for centralised (singleton) Registry operations */
	private static NodeConfigFactoryImpl localQueryInstance = null;

	/** Factory for distributed (gaian) Registry operations */
	private static NodeConfigFactoryImpl remoteQueryInstance = null;

	/*
	 * Queries
	 */
	/** Select all records */
	private String SELECT_ALL_QUERY = null;

	/** Select records by name */
	private String BY_NODE_AND_NAME_QUERY = null;

	/** Select records using an arbitrary WHERE clause */
	private String PREDICATE_QUERY = null;

	static {
		localQueryInstance = new NodeConfigFactoryImpl(true);
		remoteQueryInstance = new NodeConfigFactoryImpl(false);
	}

	public static NodeConfigFactory getInstance(boolean queryLocal) {

		if (queryLocal) {
			return localQueryInstance;
		} else {
			return remoteQueryInstance;
		}
	}

	private NodeConfigFactoryImpl(boolean queryLocal) {

		this.localOnly = queryLocal;

		SELECT_ALL_QUERY = format("select * from %s", FabricRegistry.NODE_CONFIG);
		BY_NODE_AND_NAME_QUERY = format("select * from %s where NODE_ID='\\%s' and NAME='\\%s'",
				FabricRegistry.NODE_CONFIG);
		PREDICATE_QUERY = format("select * from %s where \\%s", FabricRegistry.NODE_CONFIG);
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.impl.AbstractFactory#create(java.sql.ResultSet)
	 */
	@Override
	public RegistryObject create(IPersistenceResultRow row) throws PersistenceException {

		NodeConfig nc = null;

		if (row != null) {

			NodeConfigImpl ncImpl = new NodeConfigImpl();
			ncImpl.setNode(row.getString(1));
			ncImpl.setName(row.getString(2));
			ncImpl.setValue(row.getString(3));

			/* preserve these values internally */
			ncImpl.createShadow();

			nc = ncImpl;
		}

		return nc;
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.impl.AbstractFactory#getDeleteSql(fabric.registry.RegistryObject)
	 */
	@Override
	public String getDeleteSql(RegistryObject obj) {

		StringBuffer buf = new StringBuffer();

		if (obj instanceof NodeConfig) {

			NodeConfig nc = (NodeConfig) obj;

			buf.append("delete from " + FabricRegistry.NODE_CONFIG + " where(");
			buf.append("NODE_ID='").append(nc.getNode()).append("' AND ");
			buf.append("NAME='").append(nc.getName()).append("')");

		}

		return buf.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.impl.AbstractFactory#getInsertSql(fabric.registry.RegistryObject)
	 */
	@Override
	public String getInsertSql(RegistryObject obj) {

		StringBuffer buf = new StringBuffer();

		if (obj instanceof NodeConfig) {

			NodeConfig nc = (NodeConfig) obj;

			buf.append("insert into " + FabricRegistry.NODE_CONFIG + "  values(");
			buf.append("'").append(nc.getNode()).append("',");
			buf.append("'").append(nc.getName()).append("',");
			buf.append("'").append(nc.getValue()).append("')");
		}

		return buf.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.impl.AbstractFactory#getUpdateSql(fabric.registry.RegistryObject)
	 */
	@Override
	public String getUpdateSql(RegistryObject obj) {

		StringBuffer buf = new StringBuffer();

		if (obj instanceof NodeConfig) {

			NodeConfig nc = (NodeConfig) obj;

			buf.append("update " + FabricRegistry.NODE_CONFIG + " set ");
			buf.append("VALUE='").append(nc.getValue()).append("'");
			buf.append(" WHERE ");

			NodeConfig configValue = null;

			/* If it exists, use the shadow values for the WHERE clause */
			if (nc.getShadow() != null) {
				configValue = (NodeConfig) nc.getShadow();
			} else {
				configValue = nc;
			}

			buf.append("NODE_ID='").append(configValue.getNode()).append("' AND ");
			buf.append("NAME='").append(configValue.getName()).append("'");
		}

		return buf.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.NodeConfigFactory#createNodeConfig(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public NodeConfig createNodeConfig(String node, String name, String value) {

		return new NodeConfigImpl(node, name, value);
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.NodeConfigFactory#getAllNodeConfig()
	 */
	@Override
	public NodeConfig[] getAllNodeConfig() {

		NodeConfig[] nc = null;

		try {
			nc = runQuery(SELECT_ALL_QUERY);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}

		return nc;
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.NodeConfigFactory#getNodeConfigByName(java.lang.String, java.lang.String)
	 */
	@Override
	public NodeConfig getNodeConfigByName(String node, String name) {

		NodeConfig nc = null;

		try {

			String query = format(BY_NODE_AND_NAME_QUERY, node, name);
			NodeConfig[] ncs = runQuery(query);

			if (ncs != null && ncs.length > 0) {
				nc = ncs[0];
			}

		} catch (PersistenceException e) {
			e.printStackTrace();
		}

		return nc;
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.NodeConfigFactory#getNodeConfig(java.lang.String)
	 */
	@Override
	public NodeConfig[] getNodeConfig(String predicate) throws RegistryQueryException {

		NodeConfig[] ncs = null;

		try {

			String query = format(PREDICATE_QUERY, predicate);
			ncs = runQuery(query);

		} catch (PersistenceException e) {
			e.printStackTrace();
		}

		return ncs;
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.Factory#delete(fabric.registry.RegistryObject)
	 */
	@Override
	public boolean delete(RegistryObject obj) {

		if (obj != null && obj instanceof NodeConfig) {
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

		if (obj != null && obj instanceof NodeConfig) {
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

		if (obj != null && obj instanceof NodeConfig) {
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

		if (obj != null && obj instanceof NodeConfig) {
			return super.update(obj, this);
		} else {
			return false;
		}
	}

	/**
	 * Run the specified SQL query and return the results as an array.
	 * 
	 * @param sql
	 *            the query.
	 * 
	 * @return the result array.
	 * 
	 * @throws PersistenceException
	 */
	private NodeConfig[] runQuery(String sql) throws PersistenceException {

		NodeConfig[] ncs = null;

		RegistryObject[] objects = queryRegistryObjects(sql, this);

		if (objects != null && objects.length > 0) {

			/* Necessary */

			ncs = new NodeConfig[objects.length];

			for (int k = 0; k < objects.length; k++) {
				ncs[k] = (NodeConfig) objects[k];
			}

		} else {

			ncs = new NodeConfig[0];

		}

		return ncs;
	}
}
