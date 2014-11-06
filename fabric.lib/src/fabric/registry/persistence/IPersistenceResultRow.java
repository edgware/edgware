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

	/**
	 * This method will return the value of the designated column in the row as a String.
	 * @param index the first value is 1, the second is 2
	 * @return
	 */
	public String getString(int index);

	/**
	 * This method will return the value of the designated column in the row as a double.
	 * @param index the first value is 1, the second is 2
	 * @return
	 */
	public double getDouble(int index);

	/**
	 * This method will return the value of the designated column in the row as a float.
	 * @param index the first value is 1, the second is 2
	 * @return
	 */
	public float getFloat(int index);

	/**
	 * This method will return the value of the designated column in the row as a int.
	 * @param index the first value is 1, the second is 2
	 * @return
	 */
	public int getInt(int index);

	/**
	 * This method will return the value of the designated column in the row as a Timestamp.
	 * @param index the first value is 1, the second is 2
	 * @return
	 */
	public Timestamp getTimestamp(int index);
	
	/**
	 * This method will return the value of the designated column in the row as a Date.
	 * @param index the first value is 1, the second is 2
	 * @return
	 */
	public Date getDate(int index);

	/**
	 * This method will return the value of the designated column in the row as a String.
	 * @param 	Name of value to return
	 * @return
	 */
	public String getString(String key) throws PersistenceException;

	/**
	 * This method will return the value of the designated column in the row as a double.
	 * @param 	Name of value to return
	 * @return
	 */
	public double getDouble(String key) throws PersistenceException;

	/**
	 * This method will return the value of the designated column in the row as a float.
	 * @param 	Name of value to return
	 * @return
	 */
	public float getFloat(String key) throws PersistenceException;

	/**
	 * This method will return the value of the designated column in the row as a int.
	 * @param 	Name of value to return
	 * @return
	 */
	public int getInt(String key) throws PersistenceException;

	/**
	 * This method will return the value of the designated column in the row as a Timestamp.
	 * @param 	Name of value to return
	 * @return
	 */
	public Timestamp getTimestamp(String key) throws PersistenceException;
	
	/**
	 * This method will return the value of the designated column in the row as a Date.
	 * @param 	Name of value to return
	 * @return
	 */
	public Date getDate(String key) throws PersistenceException;
	
	/**
	 * Return an array of Objects corresponding to the values of this row
	 * @return
	 */
	public Object[] toArray();

	/**
	 * Is this Row empty
	 * @return
	 */
	public boolean isEmpty();

	/**
	 * Are Keys available for this row
	 * @return
	 */
	public boolean areKeysAvailable();
	
}
