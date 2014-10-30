/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.persistence.impl;

import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import fabric.registry.exception.PersistenceException;
import fabric.registry.persistence.IPersistenceResultRow;

/**
 * Class to represent and allow access to a 'row' of results from a Persistence Query
 * 
 * Primarily used by Factories to create Registry Objects.
 * 
 */

public class PersistenceResultRow implements IPersistenceResultRow, java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2606400109159913008L;

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	private final static String CLASS_NAME = PersistenceResultRow.class.getName();
	private final static String PACKAGE_NAME = PersistenceResultRow.class.getPackage().getName();

	private final static Logger logger = Logger.getLogger(PACKAGE_NAME);
	private PersistenceResultKeys keys;
	private boolean keysAvailable = false;
	private List<Object> values = new ArrayList<Object>();
	
	/**
	 * Construct a row from JsonNode and keys
	 */
	public PersistenceResultRow(JsonNode row, PersistenceResultKeys keys) {
		if (keys != null && !keys.isEmpty()) {
			this.keys = keys;
			keysAvailable = true;
		}
		//Verify this JsonNode is an array of Strings
		if (row.isArray()) {
			for (Iterator<JsonNode> iterator = row.elements(); iterator.hasNext();) 
			{
				JsonNode rowValue = iterator.next();
				if (rowValue.isTextual()) {
					values.add(rowValue.asText());
				}
			}
		}
	}

	
	public PersistenceResultRow(ResultSet rs, PersistenceResultKeys keys) {

//		String METHOD_NAME = "constructor";
//		logger.entering(CLASS_NAME, METHOD_NAME);
		init(rs, keys);
//		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

	/**
	 * Constructor from result set 
	 * @param rs
	 */
	public PersistenceResultRow(ResultSet rs) {

//		String METHOD_NAME = "constructor";
//		logger.entering(CLASS_NAME, METHOD_NAME);
		init(rs, null);
//		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

	/**
	 * 
	 * @param rs
	 * @param keys
	 */
	private void init(ResultSet rs, PersistenceResultKeys keys) {

		if (keys != null && !keys.isEmpty()) {
			this.keys = keys;
			keysAvailable = true;
		}
		values = new ArrayList<Object>();
		try {
			int columns = rs.getMetaData().getColumnCount();
			for (int i = 0; i < columns; i++) {
				values.add(rs.getObject(i + 1));
			}
		} catch (SQLException e) {
			logger.warning("Failed to process resultSet " + CLASS_NAME + "is empty");
			logger.warning(e.getMessage());
		}

	}

	/**
	 * 
	 * @param key
	 * @return
	 * @throws PersistenceException
	 */
	private int getIndex(String key) throws PersistenceException {

		int index = -1;
		if (keys.isEmpty()) {
			throw new PersistenceException("No keys available to getResult by key");
		} else {
			index = keys.getIndex(key);
		}
		if (index == -1) {
			throw new PersistenceException("key not found");
		}
		return index + 1;
	}

	/**
	 * 
	 * @param index
	 *            the first value is 1, the second is 2
	 * @return
	 */
	@Override
	public String getString(int index) {

		Object value = values.get(index - 1);
		if (value instanceof String) {
			return (String) value;
		} else {
			return (value != null) ? String.valueOf(values.get(index - 1)) : null;
		}
	}

	/**
	 * 
	 * @param key
	 *            Name of value to return
	 * @return
	 */
	@Override
	public String getString(String key) throws PersistenceException {

		return getString(getIndex(key));
	}

	/**
	 * 
	 * @param index
	 *            the first value is 1, the second is 2
	 * @return
	 */
	@Override
	public double getDouble(int index) {

		Object value = values.get(index - 1);
		if ( value == null) {
			return 0;			
		} else if (value instanceof Double) {
			return (Double) value;
		} else {
			return Double.valueOf(getString(index));
		}
	}

	/**
	 * 
	 * @param key
	 *            Name of value to return
	 * @return
	 */
	@Override
	public double getDouble(String key) throws PersistenceException {

		return getDouble(getIndex(key));
	}

	/**
	 * 
	 * @param index
	 *            the first value is 1, the second is 2
	 * @return
	 */
	@Override
	public float getFloat(int index) {

		Object value = values.get(index - 1);
		if (value instanceof Float) {
			return (Float) value;
		} else {
			return Float.valueOf(getString(index));
		}
	}

	/**
	 * 
	 * @param key
	 *            Name of value to return
	 * @return
	 */
	@Override
	public float getFloat(String key) throws PersistenceException {

		return getFloat(getIndex(key));
	}

	/**
	 * 
	 * @param index
	 *            the first value is 1, the second is 2
	 * @return
	 */
	@Override
	public int getInt(int index) {

		Object value = values.get(index - 1);
		if (value instanceof Integer) {
			return (Integer) value;
		} else {
			return Integer.valueOf(getString(index));
		}
	}

	/**
	 * 
	 * @param key
	 *            Name of value to return
	 * @return
	 */
	@Override
	public int getInt(String key) throws PersistenceException {

		return getInt(getIndex(key));
	}

	/**
	 * 
	 * @param index
	 *            the first value is 1, the second is 2
	 * @return
	 */
	@Override
	public java.util.Date getDate(int index) {

		Object value = values.get(index - 1);
		if (value instanceof Date) {
			return (Date) value;
		} else {
			return Date.valueOf(getString(index));
		}
	}

	/**
	 * 
	 * @param key
	 *            Name of value to return
	 * @return
	 */
	@Override
	public java.util.Date getDate(String key) throws PersistenceException {

		return getDate(getIndex(key));
	}

	/**
	 * 
	 * @param index
	 *            the first value is 1, the second is 2
	 * @return
	 */
	@Override
	public Timestamp getTimestamp(int index) {

		Object value = values.get(index - 1);
		if (value instanceof Timestamp) {
			return (Timestamp) value;
		} else {
			return Timestamp.valueOf(getString(index));
		}
	}

	/**
	 * 
	 * @param key
	 *            Name of value to return
	 * @return
	 */
	@Override
	public Timestamp getTimestamp(String key) throws PersistenceException {

		return getTimestamp(getIndex(key));
	}

	@Override
	public String toString() {

//		String METHOD_NAME = "toString";
//		logger.entering(CLASS_NAME, METHOD_NAME);
		String resultString = "";
		if (!values.isEmpty()) {
			for (int i = 0; i < values.size(); i++) {
				resultString = resultString + values.get(i) + "\t";
			}
		}
//		logger.exiting(CLASS_NAME, METHOD_NAME);
		return resultString;
	}

	@Override
	public Object[] toArray() {

		return values.toArray(new Object[] {});
	}

	@Override
	public boolean isEmpty() {

		return values.isEmpty();
	}


	@Override
	public boolean areKeysAvailable() {
		return keysAvailable;
	}

	/**
	 * Method to convert this row to Json.
	 * 
	 * @param jsonGenerator
	 * @throws JsonGenerationException
	 * @throws IOException
	 */
	public void toJson(JsonGenerator jsonGenerator) throws JsonGenerationException, IOException {
		jsonGenerator.writeStartArray();
		for (int i = 0; i < values.size(); i++) {
			Object value = values.get(i);
			//The get methods will automatically try to convert a String back to the object requested 
			// so for now using toString()
			jsonGenerator.writeString(value.toString());
		}
		jsonGenerator.writeEndArray();
	}
	
	

}