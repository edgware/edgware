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
import fabric.registry.TaskNode;
import fabric.registry.TaskNodeFactory;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.exception.RegistryQueryException;
import fabric.registry.persistence.IPersistenceResultRow;

/**
 * Implementation of the factory for <code>TaskNode</code>s.
 */
public class TaskNodeFactoryImpl extends AbstractFactory implements TaskNodeFactory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/*
	 * Constants
	 */

	/** Factory for local (singleton) Registry operations */
	private static TaskNodeFactoryImpl localQueryInstance = null;

	/** Factory for remote (distributed) Registry operations */
	private static TaskNodeFactoryImpl remoteQueryInstance = null;

	/*
	 * Queries
	 */

	/** Select all records */
	private String SELECT_ALL_QUERY = "select * from " + FabricRegistry.TASK_NODES;
	/** Select records for a particular task */
	private String BY_TASK_QUERY = "select * from " + FabricRegistry.TASK_NODES + " where TASK_ID='";
	/** Select records using an arbitrary WHERE clause */
	private String PREDICATE_QUERY = "select * from " + FabricRegistry.TASK_NODES + " where ";

	/*
	 * Static initialisation
	 */
	static {
		localQueryInstance = new TaskNodeFactoryImpl(true);
		remoteQueryInstance = new TaskNodeFactoryImpl(true);
	}

	private TaskNodeFactoryImpl(boolean queryLocal) {
		this.localOnly = queryLocal;

		SELECT_ALL_QUERY = format("select * from %s", FabricRegistry.TASK_NODES);
		BY_TASK_QUERY = format("select * from %s where TASK_ID='\\%s'", FabricRegistry.TASK_NODES);
		PREDICATE_QUERY = format("select * from %s where \\%s", FabricRegistry.TASK_NODES);
	}

	public static TaskNodeFactoryImpl getInstance(boolean queryLocal) {
		if (queryLocal) {
			return localQueryInstance;
		} else {
			return remoteQueryInstance;
		}
	}

	@Override
	public RegistryObject create(IPersistenceResultRow row) throws PersistenceException {
		TaskNode taskNode = null;
		if (row != null) {
			TaskNodeImpl impl = new TaskNodeImpl();
			impl.setTaskId(row.getString(1));
			impl.setNodeId(row.getString(2));
			impl.setDescription(row.getString(3));
			impl.setConfiguration(row.getString(4));
			impl.setConfigurationUri(row.getString(5));

			/* preserve these values internally */
			impl.createShadow();

			taskNode = impl;
		}
		return taskNode;
	}

	@Override
	public String getDeleteSql(RegistryObject obj) {
		StringBuffer buf = new StringBuffer();
		if (obj instanceof TaskNode) {
			TaskNode taskNode = (TaskNode) obj;
			buf.append("delete from " + FabricRegistry.TASK_NODES + " where(");
			buf.append("TASK_ID='").append(taskNode.getTaskId()).append("' AND ");
			buf.append("NODE_ID='").append(taskNode.getNodeId()).append("')");
		}
		return buf.toString();
	}

	@Override
	public String getInsertSql(RegistryObject obj) {
		StringBuffer buf = new StringBuffer();
		if (obj instanceof TaskNode) {
			TaskNode taskNode = (TaskNode) obj;
			buf.append("insert into " + FabricRegistry.TASK_NODES + " values(");
			buf.append(nullOrString(taskNode.getTaskId())).append(",");
			buf.append(nullOrString(taskNode.getNodeId())).append(",");
			buf.append(nullOrString(taskNode.getDescription())).append(",");
			buf.append(nullOrString(taskNode.getConfiguration())).append(",");
			buf.append(nullOrString(taskNode.getConfigurationUri())).append(")");
		}
		return buf.toString();
	}

	@Override
	public String getUpdateSql(RegistryObject obj) {
		StringBuffer buf = new StringBuffer();
		if (obj instanceof TaskNode) {
			TaskNode taskNode = (TaskNode) obj;
			buf.append("update " + FabricRegistry.TASK_NODES + " set ");
			buf.append("TASK_ID='").append(taskNode.getTaskId()).append("',");
			buf.append("NODE_ID='").append(taskNode.getNodeId()).append("',");
			buf.append("DESCRIPTION=").append(nullOrString(taskNode.getDescription())).append(",");
			buf.append("TASK_CONFIGURATION=").append(nullOrString(taskNode.getConfiguration())).append(",");
			buf.append("TASK_CONFIGURATION_URI=").append(nullOrString(taskNode.getConfigurationUri()));
			buf.append(" WHERE");

			/* if it exists, use the shadow values for the WHERE clause */
			if (taskNode.getShadow() != null) {
				TaskNode shadow = (TaskNode) taskNode.getShadow();
				buf.append(" TASK_ID='").append(shadow.getTaskId()).append("'");
				buf.append(" AND NODE_ID='").append(shadow.getNodeId()).append("'");
			} else {
				buf.append(" TASK_ID='").append(taskNode.getTaskId()).append("'");
				buf.append(" AND NODE_ID='").append(taskNode.getNodeId()).append("'");
			}
		}
		return buf.toString();
	}

	/**
	 * @see fabric.registry.TaskNodeFactory#createTaskNode(java.lang.String, java.lang.String)
	 */
	@Override
	public TaskNode createTaskNode(String taskId, String nodeId) {
		return createTaskNode(taskId, nodeId, null, null, null);
	}

	/**
	 * @see fabric.registry.TaskNodeFactory#createTaskNode(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	@Override
	public TaskNode createTaskNode(String taskId, String nodeId, String description, String configuration,
			String configurationUri) {

		return new TaskNodeImpl(taskId, nodeId, description, configuration, configurationUri);
	}

	/**
	 * @see fabric.registry.TaskNodeFactory#getAllTaskNodes()
	 */
	@Override
	public TaskNode[] getAllTaskNodes() {
		TaskNode[] taskNodes = null;
		try {
			taskNodes = runQuery(SELECT_ALL_QUERY);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return taskNodes;
	}

	/**
	 * @see fabric.registry.TaskNodeFactory#getTaskNodesByTask(java.lang.String)
	 */
	@Override
	public TaskNode[] getTaskNodesByTask(String taskId) {
		TaskNode[] taskNodes = null;
		try {
			String query = format(BY_TASK_QUERY, taskId);
			taskNodes = runQuery(query);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return taskNodes;
	}

	/**
	 * @see fabric.registry.TaskNodeFactory#getTaskNodes(java.lang.String)
	 */
	@Override
	public TaskNode[] getTaskNodes(String queryPredicates) throws RegistryQueryException {

		TaskNode[] taskNodes = null;
		try {
			String query = format(PREDICATE_QUERY, queryPredicates);
			taskNodes = runQuery(query);
		} catch (PersistenceException e) {
			e.printStackTrace();
			throw new RegistryQueryException("Invalid query: " + PREDICATE_QUERY + queryPredicates);
		}
		return taskNodes;
	}

	private TaskNode[] runQuery(String sql) throws PersistenceException {
		TaskNode[] taskNodes = null;
		RegistryObject[] objects = queryRegistryObjects(sql, this);
		if (objects != null && objects.length > 0) {
			// necessary
			taskNodes = new TaskNode[objects.length];
			for (int k = 0; k < objects.length; k++) {
				taskNodes[k] = (TaskNode) objects[k];
			}
		} else {
			taskNodes = new TaskNode[0];
		}
		return taskNodes;
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.Factory#delete(fabric.registry.RegistryObject)
	 */
	@Override
	public boolean delete(RegistryObject obj) {
		if (obj != null && obj instanceof TaskNode) {
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
		if (obj != null && obj instanceof TaskNode) {
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

		if (obj != null && obj instanceof TaskNode) {
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

		if (obj != null && obj instanceof TaskNode) {
			return super.update(obj, this);
		} else {
			return false;
		}
	}

}
