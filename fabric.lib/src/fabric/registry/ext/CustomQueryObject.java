/*
 * (C) Copyright IBM Corp. 2008
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.ext;

import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.impl.AbstractRegistryObject;

/**
 * Object representing a single row of a result set from a custom query (i.e. not one of the defined core Fabric
 * Registry objects).
 */
public abstract class CustomQueryObject extends AbstractRegistryObject {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2008";

	/**
	 * Validates the object to determine whether it can be persisted in the Fabric Registry. Since this object is only
	 * used for queries, an exception is thrown if this method is called.
	 */
	@Override
	public void validate() throws IncompleteObjectException {
		/* prevent this object from being saved */
		throw new IncompleteObjectException("This object is used in queries only and cannot be saved!");
	}
}
