/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.ActorPlugin;
import fabric.registry.ActorPluginFactory;
import fabric.registry.FabricPlugin;
import fabric.registry.FabricRegistry;
import fabric.registry.RegistryObject;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.exception.RegistryQueryException;
import fabric.registry.persistence.IPersistenceResultRow;

public class ActorPluginFactoryImpl extends AbstractFactory implements ActorPluginFactory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/** Factory for local (singleton) Registry operations */
	private static ActorPluginFactoryImpl localQueryInstance = null;
	/** Factory for remote (distributed) Registry operations */
	private static ActorPluginFactoryImpl remoteQueryInstance = null;

	/*
	 * Queries
	 */

	/** Select all records */
	private String SELECT_ALL_QUERY_ACTOR_PLUGINS = null;

	/** Select all records for a particular node */
	private String BY_NODE_QUERY_ACTOR_PLUGINS = null;

	/** Select records using an arbitrary WHERE clause */
	private String PREDICATE_QUERY_ACTOR_PLUGINS = null;

	/*
	 * Static initialisation
	 */

	static {
		localQueryInstance = new ActorPluginFactoryImpl(true);
		remoteQueryInstance = new ActorPluginFactoryImpl(false);
	}

	public static ActorPluginFactoryImpl getInstance(boolean queryLocal) {
		if (queryLocal) {
			return localQueryInstance;
		} else {
			return remoteQueryInstance;
		}
	}

	private ActorPluginFactoryImpl(boolean queryLocal) {
		this.localOnly = queryLocal;

		SELECT_ALL_QUERY_ACTOR_PLUGINS = format("select * from %s", FabricRegistry.ACTOR_PLUGINS);

		BY_NODE_QUERY_ACTOR_PLUGINS = format("select * from %s where NODE_ID='\\%s'", FabricRegistry.ACTOR_PLUGINS);

		PREDICATE_QUERY_ACTOR_PLUGINS = format("select * from %s where \\%s", FabricRegistry.ACTOR_PLUGINS);
	}

	@Override
	public RegistryObject create(IPersistenceResultRow row) throws PersistenceException {
		ActorPlugin plugin = null;
		if (row != null) {
			ActorPluginImpl impl = new ActorPluginImpl();
			impl.setNodeId(row.getString(1));
			impl.setTaskId(row.getString(2));
			impl.setActorId(row.getString(3));
			impl.setName(row.getString(4));
			impl.setFamily(row.getString(5));
			impl.setPluginType(row.getString(6));
			impl.setOrdinal(row.getInt(7));
			impl.setPlatformId(row.getString(8));
			impl.setSensorId(row.getString(9));
			impl.setFeedId(row.getString(10));
			impl.setDescription(row.getString(11));
			impl.setArguments(row.getString(12));
			/* preserve these values internally */
			impl.createShadow();
			plugin = impl;

		}
		return plugin;
	}

	@Override
	public String getInsertSql(RegistryObject obj) {
		StringBuffer buf = new StringBuffer();
		if (obj instanceof ActorPlugin) {
			ActorPlugin plugin = (ActorPlugin) obj;
			buf.append("insert into ");
			buf.append(FabricRegistry.ACTOR_PLUGINS);
			buf.append(" values(");
			buf.append("'").append(plugin.getNodeId()).append("',");
			buf.append("'").append(plugin.getTaskId()).append("',");
			buf.append("'").append(plugin.getActorId()).append("',");
			buf.append("'").append(plugin.getName()).append("',");
			buf.append(nullOrString(plugin.getFamilyName())).append(",");
			buf.append("'").append(plugin.getPluginType()).append("',");
			buf.append(plugin.getOrdinal()).append(",");
			buf.append("'").append(plugin.getPlatformId()).append("',");
			buf.append("'").append(plugin.getSensorId()).append("',");
			buf.append("'").append(plugin.getFeedId()).append("',");
			buf.append(nullOrString(plugin.getDescription())).append(",");
			buf.append(nullOrString(plugin.getArguments())).append(")");
		}
		return buf.toString();
	}

	@Override
	public String getUpdateSql(RegistryObject obj) {
		StringBuffer buf = new StringBuffer();
		if (obj instanceof ActorPlugin) {
			ActorPlugin plugin = (ActorPlugin) obj;
			buf.append("update ");
			buf.append(FabricRegistry.ACTOR_PLUGINS);
			buf.append(" set ");
			buf.append("NODE_ID='").append(plugin.getNodeId()).append("',");
			buf.append("TASK_ID='").append(plugin.getTaskId()).append("',");
			buf.append("ACTOR_ID='").append(plugin.getActorId()).append("',");
			buf.append("ORDINAL=").append(plugin.getOrdinal()).append(",");
			buf.append("TYPE='").append(plugin.getPluginType()).append("',");
			buf.append("DESCRIPTION=").append(nullOrString(plugin.getDescription())).append(",");
			buf.append("ARGUMENTS=").append(nullOrString(plugin.getArguments())).append(",");
			buf.append("PLATFORM_ID='").append(plugin.getPlatformId()).append("',");
			buf.append("SERVICE_ID='").append(plugin.getSensorId()).append("',");
			buf.append("DATA_FEED_ID=").append(plugin.getFeedId()).append("'");
			buf.append(" WHERE ");

			/* if it exists, use the shadow values for the WHERE clause */
			if (plugin.getShadow() != null) {
				ActorPlugin shadow = (ActorPlugin) plugin.getShadow();
				buf.append("NODE_ID='").append(shadow.getNodeId()).append("' AND ");
				buf.append("TASK_ID='").append(shadow.getTaskId()).append("' AND ");
				buf.append("ACTOR_ID='").append(shadow.getActorId()).append("' AND ");
				buf.append("NAME='").append(shadow.getName()).append("' AND ");
				buf.append("ORDINAL=").append(shadow.getOrdinal()).append(" AND ");
				buf.append("TYPE='").append(shadow.getPluginType());
				buf.append("'");
			} else {
				buf.append("NODE_ID='").append(plugin.getNodeId()).append("' AND ");
				buf.append("TASK_ID='").append(plugin.getTaskId()).append("' AND ");
				buf.append("ACTOR_ID='").append(plugin.getActorId()).append("' AND ");
				buf.append("NAME='").append(plugin.getName()).append("' AND ");
				buf.append("ORDINAL=").append(plugin.getOrdinal()).append(" AND ");
				buf.append("TYPE='").append(plugin.getPluginType());
				buf.append("'");
			}
		}
		return buf.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.PluginFactory#createClientPlugin(java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, int, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public ActorPlugin createActorPlugin(String nodeId, String missionId, String clientId, String name, String family,
			String pluginType, int ordinal, String description, String arguments, String platformId, String systemId,
			String feedId) {

		return new ActorPluginImpl(nodeId, missionId, clientId, name, family, pluginType, ordinal, description,
				arguments, platformId, systemId, feedId);
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.PluginFactory#getAllClientPlugins()
	 */
	@Override
	public ActorPlugin[] getAllActorPlugins() {
		ActorPlugin[] plugins = null;
		try {
			plugins = runActorPluginQuery(SELECT_ALL_QUERY_ACTOR_PLUGINS);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return plugins;
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.PluginFactory#getClientPlugins(java.lang.String)
	 */
	@Override
	public ActorPlugin[] getActorPlugins(String predicateQuery) throws RegistryQueryException {

		ActorPlugin[] plugins = null;
		try {
			String query = format(PREDICATE_QUERY_ACTOR_PLUGINS, predicateQuery);
			plugins = runActorPluginQuery(query);
		} catch (PersistenceException e) {
			e.printStackTrace();
			throw new RegistryQueryException(e.getMessage());
		}
		return plugins;
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.PluginFactory#getClientPluginsByNode(java.lang.String)
	 */
	@Override
	public ActorPlugin[] getActorPluginsByNode(String id) {
		ActorPlugin[] plugins = null;
		try {
			String query = format(BY_NODE_QUERY_ACTOR_PLUGINS, id);
			plugins = runActorPluginQuery(query);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return plugins;
	}

	private ActorPlugin[] runActorPluginQuery(String sql) throws PersistenceException {
		ActorPlugin[] plugins = null;
		RegistryObject[] objects = queryRegistryObjects(sql, this);
		if (objects != null && objects.length > 0) {
			// necessary
			plugins = new ActorPlugin[objects.length];
			for (int k = 0; k < objects.length; k++) {
				plugins[k] = (ActorPlugin) objects[k];
			}
		} else {
			plugins = new ActorPlugin[0];
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

		/* build up the buffer depending on the plugin type */
		ActorPlugin plugin = (ActorPlugin) obj;
		buf.append("DELETE FROM " + FabricRegistry.ACTOR_PLUGINS + " WHERE NODE_ID = '");
		buf.append(plugin.getNodeId());
		buf.append("' AND TASK_ID = '");
		buf.append(plugin.getTaskId());
		buf.append("' AND ACTOR_ID = '");
		buf.append(plugin.getActorId());
		buf.append("' AND NAME = '");
		buf.append(plugin.getName());
		buf.append("' AND FAMILY = '");
		buf.append(plugin.getFamilyName());
		buf.append("' AND TYPE = '");
		buf.append(plugin.getPluginType());
		buf.append("' AND ORDINAL = ");
		buf.append(plugin.getOrdinal());
		buf.append(" AND PLATFORM_ID = '");
		buf.append(plugin.getPlatformId());
		buf.append("' AND SERVICE_ID = '");
		buf.append(plugin.getSensorId());
		buf.append("' AND DATA_FEED_ID = '");
		buf.append(plugin.getFeedId());
		buf.append("' AND DESCRIPTION = '");
		buf.append(plugin.getDescription());
		buf.append("'");
		return buf.toString();
	}

}
