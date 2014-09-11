/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.persistence;

import java.sql.Timestamp;
import java.util.Date;

import fabric.registry.exception.PersistenceException;

public interface IPersistenceResultRow {
	
	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	String getString(int index);

	double getDouble(int index);

	Object[] toArray();

	boolean isEmpty();

	float getFloat(int index);

	int getInt(int index);

	Timestamp getTimestamp(int index);
	
	Date getDate(int index);

	String getString(String key) throws PersistenceException;

	double getDouble(String key) throws PersistenceException;

	float getFloat(String key) throws PersistenceException;

	int getInt(String key) throws PersistenceException;

	Timestamp getTimestamp(String key) throws PersistenceException;
	
	Date getDate(String key) throws PersistenceException;
	
}
