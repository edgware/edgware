/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.FabricRegistry;
import fabric.registry.NodeIpMapping;
import fabric.registry.NodeIpMappingFactory;
import fabric.registry.RegistryObject;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.persistence.IPersistenceResultRow;

/**
 * Factory used to create NodeIpMapping objects and persist them in the Fabric Registry.
 */
public class NodeIpMappingFactoryImpl extends AbstractFactory implements NodeIpMappingFactory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/*
	 * Class fields
	 */

	/** Factory for local (singleton) Registry operations */
	private static NodeIpMappingFactoryImpl localQueryInstance = null;

	/** Factory for distributed (Gaian) Registry operations */
	private static NodeIpMappingFactoryImpl remoteQueryInstance = null;

	/* Query definitions */

	/** Select all records. */
	private String SELECT_ALL_QUERY = null;

	/** Select records matching a specified node ID. */
	private String BY_ID_QUERY = null;

	/** Select records matching a specified node ID and node Interface. */
	private String BY_ID_AND_INTERFACE_QUERY = null;

	/** Select records using an arbitrary SQL <code>WHERE</code> clause. */
	private String PREDICATE_QUERY = null;

	/*
	 * Class static initialization
	 */

	static {

		/* Create an instance for local (singleton) Registry operations */
		localQueryInstance = new NodeIpMappingFactoryImpl(true);

		/* Create an instance for distributed (Gaian) Registry operations */
		remoteQueryInstance = new NodeIpMappingFactoryImpl(false);
	}

	/*
	 * Class methods
	 */

	private NodeIpMappingFactoryImpl(boolean queryLocal) {
		this.localOnly = queryLocal;

		/* Build the predefined queries to reflect the selected tables */

		SELECT_ALL_QUERY = format("select * from %s", FabricRegistry.NODE_IP_MAPPING);

		BY_ID_QUERY = format("select * from %s where NODE_ID='\\%s'", FabricRegistry.NODE_IP_MAPPING);

		BY_ID_AND_INTERFACE_QUERY = format("select * from %s where NODE_ID='\\%s' AND NODE_INTERFACE='\\%s'",
				FabricRegistry.NODE_IP_MAPPING);

		PREDICATE_QUERY = format("select * from %s where \\%s", FabricRegistry.NODE_IP_MAPPING);

	}

	/**
	 * Get the singleton instance of this factory.
	 * 
	 * @return an instance of the factory.
	 */
	public static NodeIpMappingFactoryImpl getInstance(boolean queryLocal) {
		if (queryLocal) {
			return localQueryInstance;
		} else {
			return remoteQueryInstance;
		}
	}

	@Override
	public RegistryObject create(IPersistenceResultRow row) throws PersistenceException {
		NodeIpMapping ipMapping = null;
		if (row != null) {
			NodeIpMappingImpl impl = new NodeIpMappingImpl();
			impl.setNodeId(row.getString(1));
			impl.setNodeInterface(row.getString(2));
			impl.setIpAddress(row.getString(3));
			impl.setPort(row.getInt(4));

			/* preserve these values internally */
			impl.createShadow();

			ipMapping = impl;
		}
		return ipMapping;
	}

	/**
	 * @see fabric.registry.NodeIpMappingFactory#createNodeIpMapping(java.lang.String, java.lang.String, int)
	 */
	@Override
	public NodeIpMapping createNodeIpMapping(String nodeId, String nodeInterface, String ipAddress, int port) {
		return new NodeIpMappingImpl(nodeId, nodeInterface, ipAddress, port);
	}

	@Override
	public String getDeleteSql(RegistryObject obj) {
		StringBuffer buf = new StringBuffer();
		if (obj instanceof NodeIpMapping) {
			NodeIpMapping ipMapping = (NodeIpMapping) obj;
			buf.append("delete from " + FabricRegistry.NODE_IP_MAPPING + " where(");
			buf.append("NODE_ID='").append(ipMapping.getNodeId()).append("' AND ");
			buf.append("NODE_INTERFACE='").append(ipMapping.getNodeInterface()).append("' AND ");
			buf.append("IP='").append(ipMapping.getIpAddress()).append("')");
		}
		return buf.toString();
	}

	@Override
	public String getInsertSql(RegistryObject obj) {
		StringBuffer buf = new StringBuffer();
		if (obj instanceof NodeIpMapping) {
			NodeIpMapping ipMapping = (NodeIpMapping) obj;
			buf.append("insert into " + FabricRegistry.NODE_IP_MAPPING + " values(");
			buf.append("'").append(ipMapping.getNodeId()).append("',");
			buf.append("'").append(ipMapping.getNodeInterface()).append("',");
			buf.append("'").append(ipMapping.getIpAddress()).append("',");
			buf.append(ipMapping.getPort()).append(")");
		}
		return buf.toString();
	}

	@Override
	public String getUpdateSql(RegistryObject obj) {
		StringBuffer buf = new StringBuffer();
		if (obj instanceof NodeIpMapping) {
			NodeIpMapping ipMapping = (NodeIpMapping) obj;
			buf.append("update " + FabricRegistry.NODE_IP_MAPPING + " set ");
			buf.append("NODE_ID='").append(ipMapping.getNodeId()).append("',");
			buf.append("NODE_INTERFACE='").append(ipMapping.getNodeInterface()).append("',");
			buf.append("IP='").append(ipMapping.getIpAddress()).append("',");
			buf.append("PORT=").append(ipMapping.getPort());
			buf.append(" WHERE ");

			/* if it exists, use the shadow values for the WHERE clause */
			if (ipMapping.getShadow() != null) {
				NodeIpMapping shadow = (NodeIpMapping) ipMapping.getShadow();
				buf.append("NODE_ID='").append(shadow.getNodeId()).append("'");
			} else {
				buf.append("NODE_ID='").append(ipMapping.getNodeId()).append("'");
			}
		}
		return buf.toString();
	}

	private NodeIpMapping[] runQuery(String sql) throws PersistenceException {
		NodeIpMapping[] ipMappings = null;
		RegistryObject[] objects = queryRegistryObjects(sql, this);
		if (objects != null && objects.length > 0) {
			// necessary
			ipMappings = new NodeIpMapping[objects.length];
			for (int k = 0; k < objects.length; k++) {
				ipMappings[k] = (NodeIpMapping) objects[k];
			}
		} else {
			ipMappings = new NodeIpMapping[0];
		}
		return ipMappings;
	}

	/**
	 * @see fabric.registry.NodeIpMappingFactory#getAllMappings()
	 */
	@Override
	public NodeIpMapping[] getAllMappings() {
		NodeIpMapping[] ipMappings = null;
		try {
			ipMappings = runQuery(SELECT_ALL_QUERY);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return ipMappings;
	}

	@Override
	public NodeIpMapping[] getAllMappingsForNode(String nodeId) {
		NodeIpMapping[] ipMappings = null;
		try {
			String query = format(BY_ID_QUERY, nodeId);
			ipMappings = runQuery(query);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return ipMappings;
	}

	@Override
	public NodeIpMapping getAnyMappingForNode(String nodeId) {
		NodeIpMapping ipMapping = null;
		NodeIpMapping[] ipMappings = getAllMappingsForNode(nodeId);
		if (ipMappings != null && ipMappings.length > 0) {
			ipMapping = ipMappings[0];
		}
		return ipMapping;
	}

	@Override
	public NodeIpMapping getMappingForNode(String nodeId, String nodeInterface) {
		NodeIpMapping ipMapping = null;
		try {
			String query = format(BY_ID_AND_INTERFACE_QUERY, nodeId, nodeInterface);
			NodeIpMapping[] ipMappings = runQuery(query);
			if (ipMappings != null && ipMappings.length > 0) {
				ipMapping = ipMappings[0];
			}
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return ipMapping;
	}

	/**
	 * @see fabric.registry.NodeIpMappingFactory#getMappings(java.lang.String)
	 */
	@Override
	public NodeIpMapping[] getMappings(String queryPredicates) {
		NodeIpMapping[] ipMappings = null;
		try {
			String query = format(PREDICATE_QUERY, queryPredicates);
			ipMappings = runQuery(query);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return ipMappings;
	}

	/**
	 * @see fabric.registry.Factory#delete(fabric.registry.RegistryObject)
	 */
	@Override
	public boolean delete(RegistryObject obj) {
		if (obj != null && obj instanceof NodeIpMapping) {
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
		if (obj != null && obj instanceof NodeIpMapping) {
			return super.save(obj, this);
		} else {
			return false;
		}
	}

	/**
	 * @see fabric.registry.Factory#insert(fabric.registry.RegistryObject)
	 */
	@Override
	public boolean insert(RegistryObject obj) throws IncompleteObjectException, DuplicateKeyException,
			PersistenceException {

		if (obj != null && obj instanceof NodeIpMapping) {
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

		if (obj != null && obj instanceof NodeIpMapping) {
			return super.update(obj, this);
		} else {
			return false;
		}
	}

}
