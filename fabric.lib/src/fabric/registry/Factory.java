/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;

/**
 * Interface for all factories used to create, delete, read and update objects in the Fabric Registry.
 */
public interface Factory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/**
	 * Inserts the specified RegistryObject into the registry database.
	 * 
	 * @param obj
	 *            the RegistryObject to be inserted.
	 * 
	 * @return boolean true if the insert succeeded, false otherwise.
	 * @throws IncompleteObjectException
	 *             if the object does not have all required fields set.
	 * @throws DuplicateKeyException
	 *             if this object already exists in the database (i.e. primary keys conflict)
	 * @throws PersistenceException
	 *             if an unexpected error occurred accessing the database.
	 */
	public boolean insert(RegistryObject obj) throws IncompleteObjectException, DuplicateKeyException,
			PersistenceException;

	/**
	 * Update the specified RegistryObject in the registry database.
	 * 
	 * @param obj
	 *            the RegistryObject to update
	 * @return true if the update succeeded, false otherwise
	 * @throws IncompleteObjectException
	 *             if the object does not have all required fields set.
	 * @throws PersistenceException
	 *             if an unexpected error occurred accessing the database.
	 */
	public boolean update(RegistryObject obj) throws IncompleteObjectException, PersistenceException;

	/**
	 * Saves the specified RegistryObject to the database.
	 * 
	 * If it doesn't already exist, the object will be inserted as a new row in the appropriate table. Otherwise, the
	 * existing row will be updated.
	 * 
	 * @param obj
	 * @throws IncompleteObjectException
	 *             if the object does not have all required fields set.
	 * @return true if the object was saved; false if an error occurred.
	 */
	public boolean save(RegistryObject obj) throws IncompleteObjectException;

	/**
	 * Deletes the specified RegistryObject from the database.
	 * 
	 * @param obj
	 * @return true if object was deleted; false if the object was not found in the database or if an error was
	 *         encountered.
	 */
	public boolean delete(RegistryObject obj);
}