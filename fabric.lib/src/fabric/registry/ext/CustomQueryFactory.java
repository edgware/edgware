/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.ext;

import fabric.registry.RegistryObject;
import fabric.registry.exception.PersistenceException;
import fabric.registry.impl.AbstractFactory;
import fabric.registry.persistence.IPersistenceResultRow;

/**
 * Service class for developers that want to perform custom queries and therefore don't want to implement all the other
 * methods required to save and delete objects.
 * 
 * Factories that subclass this object need only implement the create(ResultSet) and create their custom object from a
 * resultset row.
 */
public abstract class CustomQueryFactory extends AbstractFactory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/**
	 * @see fabric.registry.impl.AbstractFactory#create(java.sql.ResultSet)
	 */
	@Override
	public abstract RegistryObject create(IPersistenceResultRow row) throws PersistenceException;

	/**
	 * @see fabric.registry.impl.AbstractFactory#getDeleteSql(fabric.registry.RegistryObject)
	 */
	@Override
	public String getDeleteSql(RegistryObject obj) {
		/* not implemented */
		throw new UnsupportedOperationException("Delete is not supported by CustomQueryFactories");
	}

	/**
	 * @see fabric.registry.impl.AbstractFactory#getInsertSql(fabric.registry.RegistryObject)
	 */
	@Override
	public String getInsertSql(RegistryObject obj) {
		/* not implemented */
		throw new UnsupportedOperationException("Insert is not supported by CustomQueryFactories");
	}

	/**
	 * @see fabric.registry.impl.AbstractFactory#getUpdateSql(fabric.registry.RegistryObject)
	 */
	@Override
	public String getUpdateSql(RegistryObject obj) {
		/* not implemented */
		throw new UnsupportedOperationException("Update is not supported by CustomQueryFactories");
	}

}
