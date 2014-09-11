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
import fabric.registry.RegistryObject;
import fabric.registry.TaskSubscription;
import fabric.registry.TaskSubscriptionFactory;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.exception.RegistryQueryException;
import fabric.registry.persistence.IPersistenceResultRow;
import fabric.registry.persistence.PersistenceManager;

/**
 * Implementation of the factory for <code>TaskSubscription</code>s.
 */
public class TaskSubscriptionFactoryImpl extends AbstractFactory implements TaskSubscriptionFactory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/*
	 * Class constants
	 */

	/** Factory for local (singleton) Registry operations */
	private static TaskSubscriptionFactoryImpl localQueryInstance = null;

	/** Factory for distributed (Gaian) Registry operations */
	private static TaskSubscriptionFactoryImpl remoteQueryInstance = null;

	/*
	 * Queries
	 */
	/** Select records using an arbitrary WHERE clause */
	private String PREDICATE_QUERY = null;
	/** Select all records */
	private String SELECT_ALL_TASK_ACTORS = null;
	/** Select all records for a particular task Id */
	private String SELECT_SUBSCRIPTIONS_BY_TASK = null;

	/** Delete records for a particular task ID */
	private static String DELETE_SUBSCRIPTIONS_BY_TASK = "delete from " + FabricRegistry.TASK_SUBSCRIPTIONS
			+ " where TASK_ID='%s'";

	/*
	 * Static initialisation
	 */
	static {
		localQueryInstance = new TaskSubscriptionFactoryImpl(true);
		remoteQueryInstance = new TaskSubscriptionFactoryImpl(false);
	}

	private TaskSubscriptionFactoryImpl(boolean queryLocal) {
		this.localOnly = queryLocal;

		PREDICATE_QUERY = format("select * from %s where \\%s", FabricRegistry.TASK_SUBSCRIPTIONS);
		SELECT_ALL_TASK_ACTORS = format("select * from %s", FabricRegistry.TASK_SUBSCRIPTIONS);
		SELECT_SUBSCRIPTIONS_BY_TASK = format("select * from %s where TASK_ID='\\%s'",
				FabricRegistry.TASK_SUBSCRIPTIONS);
	}

	public static TaskSubscriptionFactoryImpl getInstance(boolean queryLocal) {
		if (queryLocal) {
			return localQueryInstance;
		} else {
			return remoteQueryInstance;
		}
	}

	@Override
	public RegistryObject create(IPersistenceResultRow row) throws PersistenceException {
		TaskSubscription taskClient = null;
		if (row != null) {
			TaskSubscriptionImpl impl = new TaskSubscriptionImpl();
			impl.setTaskId(row.getString(1));
			impl.setActorId(row.getString(2));
			impl.setPlatformId(row.getString(3));
			impl.setSystemId(row.getString(4));
			impl.setFeedId(row.getString(5));
			impl.setActorPlatformId(row.getString(6));

			/* preserve these values internally */
			impl.createShadow();

			taskClient = impl;
		}
		return taskClient;
	}

	/**
	 * @see fabric.registry.TaskClientFactory#createTaskClient(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public TaskSubscription createTaskSubscription(String taskId, String actorId, String platformId, String systemId,
			String feedId, String actorPlatformId) {
		return new TaskSubscriptionImpl(taskId, actorId, platformId, systemId, feedId, actorPlatformId);
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.TaskClientFactory#getTaskClients(java.lang.String)
	 */
	@Override
	public TaskSubscription[] getTaskSubscriptions(String queryPredicates) throws RegistryQueryException {
		TaskSubscription[] taskClients = null;
		String query = null;
		try {
			query = format(PREDICATE_QUERY, queryPredicates);
			taskClients = runQuery(query);
		} catch (PersistenceException e) {
			e.printStackTrace();
			throw new RegistryQueryException("Error occurred running query: " + query);
		}
		return taskClients;
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.TaskClientFactory#getTaskClientsByTask(java.lang.String)
	 */
	@Override
	public TaskSubscription[] getTaskSubscriptionsByTask(String taskId) {
		TaskSubscription[] taskClients = null;
		try {
			String query = format(SELECT_SUBSCRIPTIONS_BY_TASK, taskId);
			taskClients = runQuery(query);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return taskClients;
	}

	public boolean deleteClientsForTask(String taskId) {
		String deleteSql = format(DELETE_SUBSCRIPTIONS_BY_TASK, taskId);
		try {
			boolean success = PersistenceManager.getPersistence().updateRegistryObject(deleteSql);
			return success;
		} catch (PersistenceException e) {
			e.printStackTrace();
			return false;
		}
	}

	private TaskSubscription[] runQuery(String sql) throws PersistenceException {
		TaskSubscription[] taskClients = null;
		RegistryObject[] objects = queryRegistryObjects(sql, this);
		if (objects != null && objects.length > 0) {
			// necessary
			taskClients = new TaskSubscription[objects.length];
			for (int k = 0; k < objects.length; k++) {
				taskClients[k] = (TaskSubscription) objects[k];
			}
		} else {
			taskClients = new TaskSubscription[0];
		}
		return taskClients;
	}

	@Override
	public String getDeleteSql(RegistryObject obj) {
		StringBuffer buf = new StringBuffer();
		if (obj instanceof TaskSubscription) {
			TaskSubscription taskClient = (TaskSubscription) obj;
			buf.append("delete from " + FabricRegistry.TASK_SUBSCRIPTIONS + " where(");
			buf.append("TASK_ID='").append(taskClient.getTaskId()).append("' AND");
			buf.append(" ACTOR_ID='").append(taskClient.getActorId()).append("' AND");
			buf.append(" PLATFORM_ID='").append(taskClient.getPlatformId()).append("' AND");
			buf.append(" SERVICE_ID='").append(taskClient.getSystemId()).append("' AND");
			buf.append(" DATA_FEED_ID='").append(taskClient.getFeedId()).append("' AND");
			buf.append(" ACTOR_PLATFORM_ID='").append(taskClient.getActorPlatformId()).append("')");
		}
		return buf.toString();
	}

	@Override
	public String getInsertSql(RegistryObject obj) {
		StringBuffer buf = new StringBuffer();
		if (obj instanceof TaskSubscription) {
			TaskSubscription taskClient = (TaskSubscription) obj;
			buf.append("insert into " + FabricRegistry.TASK_SUBSCRIPTIONS + " values(");
			buf.append("'").append(taskClient.getTaskId()).append("',");
			buf.append("'").append(taskClient.getActorId()).append("',");
			buf.append("'").append(taskClient.getPlatformId()).append("',");
			buf.append("'").append(taskClient.getSystemId()).append("',");
			buf.append("'").append(taskClient.getFeedId()).append("',");
			buf.append("'").append(taskClient.getActorPlatformId()).append("')");
		}
		return buf.toString();
	}

	@Override
	public String getUpdateSql(RegistryObject obj) {
		StringBuffer buf = new StringBuffer();
		if (obj instanceof TaskSubscription) {
			TaskSubscription taskClient = (TaskSubscription) obj;
			buf.append("update " + FabricRegistry.TASK_SUBSCRIPTIONS + " set ");
			buf.append("TASK_ID='").append(taskClient.getTaskId()).append("',");
			buf.append("ACTOR_ID='").append(taskClient.getActorId()).append("',");
			buf.append("PLATFORM_ID='").append(taskClient.getPlatformId()).append("',");
			buf.append("SERVICE_ID='").append(taskClient.getSystemId()).append("',");
			buf.append("DATA_FEED_ID='").append(taskClient.getFeedId()).append("',");
			buf.append("ACTOR_PLATFORM_ID='").append(taskClient.getActorPlatformId()).append("'");
			buf.append(" WHERE ");

			/* if it exists, use the shadow values for the WHERE clause */
			if (taskClient.getShadow() != null) {
				TaskSubscription shadow = (TaskSubscription) taskClient.getShadow();
				buf.append("TASK_ID='").append(shadow.getTaskId()).append("' AND ");
				buf.append("ACTOR_ID='").append(shadow.getActorId()).append("' AND ");
				buf.append("PLATFORM_ID='").append(shadow.getPlatformId()).append("' AND ");
				buf.append("SERVICE_ID='").append(shadow.getSystemId()).append("' AND ");
				buf.append("DATA_FEED_ID='").append(shadow.getFeedId()).append("' AND ");
				buf.append("ACTOR_PLATFORM_ID='").append(shadow.getActorPlatformId()).append("'");
			} else {
				buf.append("TASK_ID='").append(taskClient.getTaskId()).append("' AND ");
				buf.append("ACTOR_ID='").append(taskClient.getActorId()).append("' AND ");
				buf.append("PLATFORM_ID='").append(taskClient.getPlatformId()).append("' AND ");
				buf.append("SERVICE_ID='").append(taskClient.getSystemId()).append("' AND ");
				buf.append("DATA_FEED_ID='").append(taskClient.getFeedId()).append("' AND ");
				buf.append("ACTOR_PLATFORM_ID='").append(taskClient.getActorPlatformId()).append("'");
			}
		}

		return buf.toString();
	}

	/**
	 * @see fabric.registry.Factory#delete(fabric.registry.RegistryObject)
	 */
	@Override
	public boolean delete(RegistryObject obj) {
		if (obj != null && obj instanceof TaskSubscription) {
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
		if (obj != null && obj instanceof TaskSubscription) {
			/* special case - delete any existing subscription since update sql will never be used for this table */
			// if (obj.getShadow() != null) {
			// TaskSubscription sub = (TaskSubscription)obj.getShadow();
			// super.delete(sub, this);
			// }
			return super.save(obj, this);
		} else {
			return false;
		}
	}

	/**
	 * @see fabric.registry.TaskSubscriptionFactory#getAllTaskSubscriptions()
	 */
	@Override
	public TaskSubscription[] getAllTaskSubscriptions() {
		TaskSubscription[] taskClients = null;
		try {
			taskClients = runQuery(SELECT_ALL_TASK_ACTORS);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return taskClients;
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.Factory#insert(fabric.registry.RegistryObject)
	 */
	@Override
	public boolean insert(RegistryObject obj) throws IncompleteObjectException, DuplicateKeyException,
			PersistenceException {

		if (obj != null && obj instanceof TaskSubscription) {
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

		if (obj != null && obj instanceof TaskSubscription) {
			return super.update(obj, this);
		} else {
			return false;
		}
	}

}
