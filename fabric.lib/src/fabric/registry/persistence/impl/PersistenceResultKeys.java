/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.persistence.impl;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to represent and allow access to a 'row' of results from a Persistence Query
 * 
 * Primarily used by Factories to create Registry Objects.
 */

public class PersistenceResultKeys implements java.io.Serializable {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/**
	 * 
	 */
	private static final long serialVersionUID = 6079655378367428666L;

	private final static String CLASS_NAME = PersistenceResultKeys.class.getName();
	private final static String PACKAGE_NAME = PersistenceResultKeys.class.getPackage().getName();

	private final static Logger logger = Logger.getLogger(PACKAGE_NAME);

	private List<String> keys;

	public PersistenceResultKeys(ResultSetMetaData rsMetaData) {

		String METHOD_NAME = "constructor";
		logger.entering(CLASS_NAME, METHOD_NAME);
		keys = new Vector<String>();
		try {
			for (int i = 0; i < rsMetaData.getColumnCount(); i++) {
				keys.add(rsMetaData.getColumnLabel(i + 1));
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to process result set meta data: ", e);
		}

		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

	public boolean isEmpty() {

		return keys.isEmpty();
	}

	public int size() {

		return keys.size();
	}

	/**
	 * 
	 * @param index
	 *            the first key is 1, the second is 2
	 * @return
	 */
	public String get(int index) {

		return keys.get(index - 1);
	}

	public int getIndex(String key) {

		return keys.indexOf(key);
	}

	@Override
	public String toString() {

		// String METHOD_NAME = "toString";
		// logger.entering(CLASS_NAME, METHOD_NAME);
		String resultString = "";
		for (int i = 0; i < keys.size(); i++) {
			resultString = resultString + keys.get(i) + "\t";
		}
		// logger.exiting(CLASS_NAME, METHOD_NAME);
		return resultString;
	}

}
