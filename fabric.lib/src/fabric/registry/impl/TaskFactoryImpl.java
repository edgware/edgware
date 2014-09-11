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
import fabric.registry.Task;
import fabric.registry.TaskFactory;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.exception.RegistryQueryException;
import fabric.registry.persistence.IPersistenceResultRow;

/**
 * Implementation of the factory for <code>Task</code>'s.
 */
public class TaskFactoryImpl extends AbstractFactory implements TaskFactory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/** Factory for local (singleton) Registry operations */
	private static TaskFactoryImpl localQueryInstance = null;

	/** Factory for remote (gaian) Registry operations */
	private static TaskFactoryImpl remoteQueryInstance = null;

	/*
	 * Queries
	 */

	/** Select all records */
	private String SELECT_ALL_QUERY = null;
	/** Select a particular record */
	private String BY_ID_QUERY = null;
	/** Select records using an arbitrary WHERE clause */
	private String PREDICATE_QUERY = null;

	/*
	 * Static initialisation
	 */
	static {
		localQueryInstance = new TaskFactoryImpl(true);
		remoteQueryInstance = new TaskFactoryImpl(false);
	}

	private TaskFactoryImpl(boolean queryLocal) {
		this.localOnly = queryLocal;

		SELECT_ALL_QUERY = format("select * from %s", FabricRegistry.TASKS);
		/** Select a particular record */
		BY_ID_QUERY = format("select * from %s where TASK_ID='\\%s'", FabricRegistry.TASKS);
		/** Select records using an arbitrary WHERE clause */
		PREDICATE_QUERY = format("select * from %s where \\%s", FabricRegistry.TASKS);
	}

	public static TaskFactory getInstance(boolean queryLocal) {
		if (queryLocal) {
			return localQueryInstance;
		} else {
			return remoteQueryInstance;
		}
	}

	@Override
	public RegistryObject create(IPersistenceResultRow row) throws PersistenceException {
		Task task = null;
		if (row != null) {
			TaskImpl impl = new TaskImpl();
			impl.setId(row.getString(1));
			impl.setPriority(row.getInt(2));
			impl.setAffiliation(row.getString(3));
			impl.setDescription(row.getString(4));
			impl.setDetail(row.getString(5));
			impl.setDetailUri(row.getString(6));

			/* preserve these values internally */
			impl.createShadow();

			task = impl;
		}
		return task;
	}

	@Override
	public String getDeleteSql(RegistryObject obj) {
		StringBuffer buf = new StringBuffer();
		if (obj instanceof Task) {
			Task task = (Task) obj;
			buf.append("delete from " + FabricRegistry.TASKS + " where(");
			buf.append("TASK_ID='").append(task.getId()).append("')");
		}
		return buf.toString();
	}

	@Override
	public String getInsertSql(RegistryObject obj) {
		StringBuffer buf = new StringBuffer();
		if (obj instanceof Task) {
			Task task = (Task) obj;
			buf.append("insert into " + FabricRegistry.TASKS + " values(");
			buf.append("'").append(task.getId()).append("',");
			buf.append(task.getPriority()).append(",");
			buf.append(nullOrString(task.getAffiliation())).append(",");
			buf.append(nullOrString(task.getDescription())).append(",");
			buf.append(nullOrString(task.getDetail())).append(",");
			buf.append(nullOrString(task.getDetailUri())).append(")");
		}
		return buf.toString();
	}

	@Override
	public String getUpdateSql(RegistryObject obj) {
		StringBuffer buf = new StringBuffer();
		if (obj instanceof Task) {
			Task task = (Task) obj;
			buf.append("update " + FabricRegistry.TASKS + " set ");
			buf.append("TASK_ID='").append(task.getId()).append("',");
			buf.append("PRIORITY=").append(task.getPriority()).append(",");
			buf.append("AFFILIATION=").append(nullOrString(task.getAffiliation())).append(",");
			buf.append("DESCRIPTION=").append(nullOrString(task.getDescription())).append(",");
			buf.append("TASK_DETAIL=").append(nullOrString(task.getDetail())).append(",");
			buf.append("TASK_DETAIL_URI=").append(nullOrString(task.getDetailUri())).append("");
			buf.append(" WHERE ");

			/* if it exists, use the shadow values for the WHERE clause */
			if (task.getShadow() != null) {
				Task shadow = (Task) task.getShadow();
				buf.append("TASK_ID='").append(shadow.getId()).append("'");
			} else {
				buf.append("TASK_ID='").append(task.getId()).append("'");
			}
		}
		return buf.toString();
	}

	/**
	 * @see fabric.registry.TaskFactory#createTask(java.lang.String)
	 */
	@Override
	public Task createTask(String id) {
		return createTask(id, 10, null, null, null, null);
	}

	/**
	 * @see fabric.registry.TaskFactory#createTask(java.lang.String, int, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	@Override
	public Task createTask(String id, int priority, String affiliation, String description, String detail,
			String detailUri) {
		return new TaskImpl(id, priority, affiliation, description, detail, detailUri);
	}

	/**
	 * @see fabric.registry.TaskFactory#getAllTasks()
	 */
	@Override
	public Task[] getAllTasks() {
		Task[] tasks = null;
		try {
			tasks = runQuery(SELECT_ALL_QUERY);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return tasks;
	}

	/**
	 * @see fabric.registry.TaskFactory#getTaskById(java.lang.String)
	 */
	@Override
	public Task getTaskById(String id) {
		Task task = null;
		try {
			String query = format(BY_ID_QUERY, id);
			Task[] tasks = runQuery(query);
			if (tasks != null && tasks.length > 0) {
				task = tasks[0];
			}
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return task;
	}

	@Override
	public Task[] getTasksWithPredicates(String queryPredicates) throws RegistryQueryException {
		Task[] tasks = null;
		try {
			String query = format(PREDICATE_QUERY, queryPredicates);
			tasks = runQuery(query);
		} catch (PersistenceException e) {
			e.printStackTrace();
			throw new RegistryQueryException(e.getMessage());
		}
		return tasks;
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.Factory#save(fabric.registry.RegistryObject)
	 */
	@Override
	public boolean save(RegistryObject obj) throws IncompleteObjectException {
		if (obj != null && obj instanceof Task) {
			return super.save(obj, this);
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.Factory#delete(fabric.registry.RegistryObject)
	 */
	@Override
	public boolean delete(RegistryObject obj) {
		if (obj != null && obj instanceof Task) {
			return super.delete(obj, this);
		} else {
			return false;
		}
	}

	/**
	 * 
	 * @param sql
	 * @return
	 * @throws PersistenceException
	 */
	private Task[] runQuery(String sql) throws PersistenceException {
		Task[] tasks = null;
		RegistryObject[] objects = queryRegistryObjects(sql, this);
		if (objects != null && objects.length > 0) {
			// necessary
			tasks = new Task[objects.length];
			for (int k = 0; k < objects.length; k++) {
				tasks[k] = (Task) objects[k];
			}
		} else {
			tasks = new Task[0];
		}
		return tasks;
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.registry.Factory#insert(fabric.registry.RegistryObject)
	 */
	@Override
	public boolean insert(RegistryObject obj) throws IncompleteObjectException, DuplicateKeyException,
			PersistenceException {

		if (obj != null && obj instanceof Task) {
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

		if (obj != null && obj instanceof Task) {
			return super.update(obj, this);
		} else {
			return false;
		}
	}
}
