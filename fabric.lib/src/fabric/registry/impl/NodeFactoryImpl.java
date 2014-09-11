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
import fabric.registry.Node;
import fabric.registry.NodeFactory;
import fabric.registry.RegistryObject;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.exception.RegistryQueryException;
import fabric.registry.persistence.IPersistenceResultRow;

/**
 * Implementation of the factory for <code>Node</code>s.
 */
public class NodeFactoryImpl extends AbstractFactory implements NodeFactory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/** Factory for centralised (singleton) Registry operations */
	private static NodeFactoryImpl localQueryInstance = null;
	/** Factory for distributed (gaian) Registry operations */
	private static NodeFactoryImpl remoteQueryInstance = null;

	/*
	 * Queries
	 */
	/** Select all records */
	private String SELECT_ALL_QUERY = null;
	/** Select records by ID */
	private String BY_ID_QUERY = null;
	/** Select records by type ID */
	private String BY_TYPE_QUERY = null;
	/** Select records using an arbitrary WHERE clause */
	private String PREDICATE_QUERY = null;

	static {
		localQueryInstance = new NodeFactoryImpl(true);
		remoteQueryInstance = new NodeFactoryImpl(false);
	}

	public static NodeFactory getInstance(boolean queryLocal) {
		if (queryLocal) {
			return localQueryInstance;
		} else {
			return remoteQueryInstance;
		}
	}

	private NodeFactoryImpl(boolean queryLocal) {
		this.localOnly = queryLocal;

		SELECT_ALL_QUERY = format("select * from %s", FabricRegistry.NODES);
		BY_ID_QUERY = format("select * from %s where NODE_ID='\\%s'", FabricRegistry.NODES);
		BY_TYPE_QUERY = format("select * from %s where TYPE_ID='\\%s'", FabricRegistry.NODES);
		PREDICATE_QUERY = format("select * from %s where \\%s", FabricRegistry.NODES);
	}

	@Override
	public RegistryObject create(IPersistenceResultRow row) throws PersistenceException {
		Node node = null;
		if (row != null) {
			NodeImpl impl = new NodeImpl();
			impl.setId(row.getString(1));
			impl.setTypeId(row.getString(2));
			impl.setAffiliation(row.getString(3));
			impl.setSecurityClassification(row.getString(4));
			impl.setReadiness(row.getString(5));
			impl.setAvailability(row.getString(6));
			impl.setLatitude(row.getDouble(7));
			impl.setLongitude(row.getDouble(8));
			impl.setAltitude(row.getDouble(9));
			impl.setBearing(row.getDouble(10));
			impl.setVelocity(row.getDouble(11));
			impl.setDescription(row.getString(12));
			impl.setAttributes(row.getString(13));
			impl.setAttributesURI(row.getString(14));

			/* preserve these values internally */
			impl.createShadow();

			node = impl;
		}
		return node;
	}

	@Override
	public String getDeleteSql(RegistryObject obj) {
		StringBuffer buf = new StringBuffer();
		if (obj instanceof Node) {
			Node node = (Node) obj;
			buf.append("delete from " + FabricRegistry.NODES + " where(");
			buf.append("Type_ID='").append(node.getTypeId()).append("' AND ");
			buf.append("NODE_ID='").append(node.getId()).append("')");
		}
		return buf.toString();
	}

	@Override
	public String getInsertSql(RegistryObject obj) {
		StringBuffer buf = new StringBuffer();
		if (obj instanceof Node) {
			Node node = (Node) obj;
			buf.append("insert into " + FabricRegistry.NODES + "  values(");
			buf.append("'").append(node.getId()).append("',");
			buf.append("'").append(node.getTypeId()).append("',");
			buf.append("'").append(node.getAffiliation()).append("',");
			buf.append("'").append(node.getSecurityClassification()).append("',");
			buf.append("'").append(node.getReadiness()).append("',");
			buf.append("'").append(node.getAvailability()).append("',");
			buf.append(node.getLatitude()).append(",");
			buf.append(node.getLongitude()).append(",");
			buf.append(node.getAltitude()).append(",");
			buf.append(node.getBearing()).append(",");
			buf.append(node.getVelocity()).append(",");
			buf.append("'").append(node.getDescription()).append("',");
			buf.append("'").append(node.getAttributes()).append("',");
			buf.append("'").append(node.getAttributesURI()).append("')");
		}
		return buf.toString();
	}

	@Override
	public String getUpdateSql(RegistryObject obj) {
		StringBuffer buf = new StringBuffer();
		if (obj instanceof Node) {
			Node node = (Node) obj;
			buf.append("update " + FabricRegistry.NODES + " set ");
			buf.append("NODE_ID='").append(node.getId()).append("',");
			buf.append("TYPE_ID='").append(node.getTypeId()).append("',");
			buf.append("AFFILIATION='").append(node.getAffiliation()).append("',");
			buf.append("CREDENTIALS='").append(node.getSecurityClassification()).append("',");
			buf.append("READINESS='").append(node.getReadiness()).append("',");
			buf.append("AVAILABILITY='").append(node.getAvailability()).append("',");
			buf.append("LATITUDE=").append(node.getLatitude()).append(",");
			buf.append("LONGITUDE=").append(node.getLongitude()).append(",");
			buf.append("ALTITUDE=").append(node.getAltitude()).append(",");
			buf.append("BEARING=").append(node.getBearing()).append(",");
			buf.append("VELOCITY=").append(node.getVelocity()).append(",");
			buf.append("DESCRIPTION='").append(node.getDescription()).append("',");
			buf.append("ATTRIBUTES='").append(node.getAttributes()).append("',");
			buf.append("ATTRIBUTES_URI='").append(node.getAttributesURI()).append("'");
			buf.append(" WHERE ");

			/* if it exists, use the shadow values for the WHERE clause */
			if (node.getShadow() != null) {
				Node shadow = (Node) node.getShadow();
				buf.append("NODE_ID='").append(shadow.getId()).append("'");
			} else {
				buf.append("NODE_ID='").append(node.getId()).append("'");
			}
		}
		return buf.toString();
	}

	/**
	 * @see fabric.registry.NodeFactory#createNode(java.lang.String, java.lang.String)
	 */
	@Override
	public Node createNode(String id, String typeId) {
		return createNode(id, typeId, null, null, null, null, 0.0, 0.0, 0.0, 0.0, 0.0, null, null, null);
	}

	/**
	 * @see fabric.registry.NodeFactory#createNode(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String, double, double, double, double, double,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Node createNode(String id, String typeId, String affiliation, String securityClassification,
			String readiness, String availability, double latitude, double longitude, double altitude, double bearing,
			double velocity, String description, String attributes, String attributesURI) {

		return new NodeImpl(id, typeId, affiliation, securityClassification, readiness, availability, latitude,
				longitude, altitude, bearing, velocity, description, attributes, attributesURI);
	}

	/**
	 * @see fabric.registry.NodeFactory#getAllNodes()
	 */
	@Override
	public Node[] getAllNodes() {
		Node[] nodes = null;
		try {
			nodes = runQuery(SELECT_ALL_QUERY);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return nodes;
	}

	@Override
	public Node getNodeById(String id) {
		Node node = null;
		try {
			String query = format(BY_ID_QUERY, id);
			Node[] nodes = runQuery(query);
			if (nodes != null && nodes.length > 0) {
				node = nodes[0];
			}
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return node;
	}

	@Override
	public Node[] getNodes(String queryPredicates) throws RegistryQueryException {
		Node[] nodes = null;
		try {
			String query = format(PREDICATE_QUERY, queryPredicates);
			nodes = runQuery(query);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return nodes;
	}

	@Override
	public Node[] getNodesByType(String typeId) {
		Node[] nodes = null;
		try {
			String query = format(BY_TYPE_QUERY, typeId);
			nodes = runQuery(query);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return nodes;
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.Factory#delete(fabric.registry.RegistryObject)
	 */
	@Override
	public boolean delete(RegistryObject obj) {
		if (obj != null && obj instanceof Node) {
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
		if (obj != null && obj instanceof Node) {
			return super.save(obj, this);
		} else {
			return false;
		}
	}

	private Node[] runQuery(String sql) throws PersistenceException {
		Node[] nodes = null;
		RegistryObject[] objects = queryRegistryObjects(sql, this);
		if (objects != null && objects.length > 0) {
			// necessary
			nodes = new Node[objects.length];
			for (int k = 0; k < objects.length; k++) {
				nodes[k] = (Node) objects[k];
			}
		} else {
			nodes = new Node[0];
		}
		return nodes;
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.Factory#insert(fabric.registry.RegistryObject)
	 */
	@Override
	public boolean insert(RegistryObject obj) throws IncompleteObjectException, DuplicateKeyException,
			PersistenceException {

		if (obj != null && obj instanceof Node) {
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

		if (obj != null && obj instanceof Node) {
			return super.update(obj, this);
		} else {
			return false;
		}
	}

}
