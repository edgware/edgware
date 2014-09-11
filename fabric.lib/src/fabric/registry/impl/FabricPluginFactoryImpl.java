/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2010, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.FabricPlugin;
import fabric.registry.FabricPluginFactory;
import fabric.registry.FabricRegistry;
import fabric.registry.RegistryObject;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.exception.RegistryQueryException;
import fabric.registry.persistence.IPersistenceResultRow;

public class FabricPluginFactoryImpl extends AbstractFactory implements FabricPluginFactory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010, 2014";

	/** Factory for local (singleton) Registry operations */
	private static FabricPluginFactoryImpl localQueryInstance = null;
	/** Factory for remote (distributed) Registry operations */
	private static FabricPluginFactoryImpl remoteQueryInstance = null;

	/** Select all records */
	private String SELECT_ALL_QUERY_FABRIC_PLUGINS = null;

	/** Select all records for a particular node */
	private String BY_NODE_QUERY_FABRIC_PLUGINS = null;

	/** Select records using an arbitrary WHERE clause */
	private String PREDICATE_QUERY_FABRIC_PLUGINS = null;

	/*
	 * Static initialisation
	 */

	static {
		localQueryInstance = new FabricPluginFactoryImpl(true);
		remoteQueryInstance = new FabricPluginFactoryImpl(false);
	}

	public static FabricPluginFactoryImpl getInstance(boolean queryLocal) {
		if (queryLocal) {
			return localQueryInstance;
		} else {
			return remoteQueryInstance;
		}
	}

	private FabricPluginFactoryImpl(boolean queryLocal) {
		this.localOnly = queryLocal;

		SELECT_ALL_QUERY_FABRIC_PLUGINS = format("select * from %s", FabricRegistry.FABLET_PLUGINS);

		BY_NODE_QUERY_FABRIC_PLUGINS = format("select * from %s where NODE_ID='\\%s'", FabricRegistry.FABLET_PLUGINS);

		PREDICATE_QUERY_FABRIC_PLUGINS = format("select * from %s where \\%s", FabricRegistry.FABLET_PLUGINS);
	}

	@Override
	public String getUpdateSql(RegistryObject obj) {
		StringBuffer buf = new StringBuffer();
		if (obj instanceof FabricPlugin) {
			FabricPlugin plugin = (FabricPlugin) obj;
			buf.append("update ");
			buf.append(FabricRegistry.FABLET_PLUGINS);
			buf.append(" set ");
			buf.append("NODE_ID='").append(plugin.getNodeId()).append("',");
			buf.append("NAME='").append(plugin.getName()).append("',");
			buf.append("DESCRIPTION=").append(nullOrString(plugin.getDescription())).append(",");
			buf.append("ARGUMENTS=").append(nullOrString(plugin.getArguments()));
			buf.append(" WHERE ");

			/* if it exists, use the shadow values for the WHERE clause */
			if (plugin.getShadow() != null) {
				FabricPlugin shadow = (FabricPlugin) plugin.getShadow();
				buf.append("NODE_ID='").append(shadow.getNodeId()).append("' AND ");
				buf.append("NAME='").append(shadow.getName()).append("'");
			} else {
				buf.append("NODE_ID='").append(plugin.getNodeId()).append("' AND ");
				buf.append("NAME='").append(plugin.getName()).append("'");
			}
		}
		return buf.toString();
	}

	@Override
	public String getInsertSql(RegistryObject obj) {
		StringBuffer buf = new StringBuffer();
		if (obj instanceof FabricPlugin) {
			FabricPlugin plugin = (FabricPlugin) obj;
			buf.append("insert into ");
			buf.append(FabricRegistry.FABLET_PLUGINS);
			buf.append(" values(");
			buf.append("'").append(plugin.getNodeId()).append("',");
			buf.append("'").append(plugin.getName()).append("',");
			buf.append(nullOrString(plugin.getFamilyName())).append(",");
			buf.append(nullOrString(plugin.getDescription())).append(",");
			buf.append(nullOrString(plugin.getArguments())).append(")");
		}
		return buf.toString();
	}

	@Override
	public RegistryObject create(IPersistenceResultRow row) throws PersistenceException {
		FabricPlugin plugin = null;
		if (row != null) {
			FabricPluginImpl impl = new FabricPluginImpl();
			impl.setNodeId(row.getString(1));
			impl.setName(row.getString(2));
			impl.setFamily(row.getString(3));
			impl.setDescription(row.getString(4));
			impl.setArguments(row.getString(5));

			/* preserve these values internally */
			impl.createShadow();

			plugin = impl;
		}
		return plugin;
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.PluginFactory#createFabricPlugin(java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public FabricPlugin createFabricPlugin(String nodeId, String name, String family, String description,
			String arguments) {

		return new FabricPluginImpl(nodeId, name, family, description, arguments);
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.PluginFactory#getAllFabricPlugins()
	 */
	@Override
	public FabricPlugin[] getAllFabricPlugins() {
		FabricPlugin[] plugins = null;
		try {
			plugins = runFabricPluginQuery(SELECT_ALL_QUERY_FABRIC_PLUGINS);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return plugins;
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.PluginFactory#getFabricPlugins(java.lang.String)
	 */
	@Override
	public FabricPlugin[] getFabricPlugins(String predicateQuery) throws RegistryQueryException {

		FabricPlugin[] plugins = null;
		try {
			String query = format(PREDICATE_QUERY_FABRIC_PLUGINS, predicateQuery);
			plugins = runFabricPluginQuery(query);
		} catch (PersistenceException e) {
			e.printStackTrace();
			throw new RegistryQueryException(e.getMessage());
		}
		return plugins;
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.PluginFactory#getFabricPluginsByNode(java.lang.String)
	 */
	@Override
	public FabricPlugin[] getFabricPluginsByNode(String id) {
		FabricPlugin[] plugins = null;
		try {
			String query = format(BY_NODE_QUERY_FABRIC_PLUGINS, id);
			plugins = runFabricPluginQuery(query);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return plugins;
	}

	private FabricPlugin[] runFabricPluginQuery(String sql) throws PersistenceException {
		FabricPlugin[] plugins = null;
		RegistryObject[] objects = queryRegistryObjects(sql, this);
		if (objects != null && objects.length > 0) {
			// necessary
			plugins = new FabricPlugin[objects.length];
			for (int k = 0; k < objects.length; k++) {
				plugins[k] = (FabricPlugin) objects[k];
			}
		} else {
			plugins = new FabricPlugin[0];
		}
		return plugins;
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.Factory#delete(fabric.registry.RegistryObject)
	 */
	@Override
	public boolean delete(RegistryObject obj) {
		if (obj != null && obj instanceof FabricPlugin) {
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
		if (obj != null && obj instanceof FabricPlugin) {
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

		if (obj != null && obj instanceof FabricPlugin) {
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

		if (obj != null && obj instanceof FabricPlugin) {
			return super.update(obj, this);
		} else {
			return false;
		}
	}

	@Override
	public String getDeleteSql(RegistryObject obj) {

		StringBuffer buf = new StringBuffer();

		FabricPlugin plugin = (FabricPlugin) obj;
		buf.append("delete FROM ");
		buf.append(FabricRegistry.FABLET_PLUGINS);
		buf.append(" WHERE (");
		buf.append("NODE_ID='");
		buf.append(plugin.getNodeId()).append("' AND ");
		buf.append("NAME='");
		buf.append(plugin.getName()).append("' AND ");
		buf.append("FAMILY='");
		buf.append(plugin.getFamilyName()).append("'");

		buf.append(")");

		return buf.toString();
	}

}
