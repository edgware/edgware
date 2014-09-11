/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import java.io.Serializable;

import fabric.registry.exception.IncompleteObjectException;

/**
 * Base interface for all Fabric Registry objects.
 */
public interface RegistryObject extends Serializable {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/*
	 * Interface methods
	 */

	/**
	 * Validates the attributes of a RegistryObject. For example, ensuring that any primary key columns are correctly
	 * specified.
	 * 
	 * @throws IncompleteObjectException
	 *             if the object does not have required fields set.
	 */
	public void validate() throws IncompleteObjectException;

	/**
	 * Check whether object in it's current state is valid.
	 * 
	 * @return true if the object's state is valid, false otherwise.
	 */
	public boolean isValid();

	/**
	 * Returns a RegistryObject's shadow, which is a protected copy of the object's state created when an object is
	 * loaded from the database.
	 * 
	 * If primary key attributes are altered, the shadow object is consulted when performing an update in order to
	 * update the correct row in the appropriate table.
	 * 
	 * This mechanism is primarily used internally.
	 * 
	 * @return a copy of the object.
	 */
	public RegistryObject getShadow();

	/**
	 * Checks whether the object's state has changed compared to when it was first loaded from the database.
	 * 
	 * @return true if the object's state has changed, false otherwise.
	 */
	public boolean hasChanged();

	public String key();
}