/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.persistence.distributed;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import fabric.registry.RegistryObject;
import fabric.registry.exception.PersistenceException;
import fabric.registry.impl.AbstractFactory;
import fabric.registry.persistence.impl.PersistenceResultKeys;
import fabric.registry.persistence.impl.PersistenceResultRow;

public class DistributedQueryResult implements java.io.Serializable {

	
	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	private final static String CLASS_NAME = DistributedQueryResult.class.getName();
	private final static String PACKAGE_NAME = DistributedQueryResult.class.getPackage().getName();

	private final static Logger logger = Logger.getLogger(PACKAGE_NAME);

	private static final long serialVersionUID = 134971301012513965L;

	protected Map<String, List<PersistenceResultRow>> nodeToResults;
	private PersistenceResultKeys colNames = null;
	private boolean exceptionOccurred = false;
	private String exceptionMessage = "";

	/**
	 * Construct a DistributedQueryResult using the resultSet from the nodeName
	 * 
	 * @param nodeName Where the resultSet came from
	 * @param resultSet The resultSet to construct DistributedQueryResult from
	 */
	public DistributedQueryResult(String nodeName, ResultSet resultSet) 
	{
		String METHOD_NAME = "constructor";
		logger.entering(CLASS_NAME, METHOD_NAME);
		if (nodeToResults==null)
		{
			nodeToResults = new ConcurrentHashMap<String, List<PersistenceResultRow>>();
		}
		if (resultSet!=null && colNames == null)
		{
			try {

				colNames = new PersistenceResultKeys(resultSet.getMetaData());
			}
			catch (SQLException e) {
				logger.warning("Problem reading metadata " + e.getMessage());
			}
			
		}
		appendResultSet(nodeName, resultSet);
		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

	
	private void appendResultSet(String nodeName, ResultSet resultSet) 
	{
		String METHOD_NAME = "constructor";
		logger.entering(CLASS_NAME, METHOD_NAME);
		List<PersistenceResultRow> nodeResults = new Vector<PersistenceResultRow>();
		if (resultSet!=null)
		{
			try
			{
				PersistenceResultRow rowResults = null;
				while (resultSet.next()) {
						rowResults = new PersistenceResultRow(resultSet, colNames);
					nodeResults.add(rowResults);
				}
			}
			catch (SQLException e) {
				logger.warning("Problem reading metadata " + e.getMessage());
			}
		}
		nodeToResults.put(nodeName, nodeResults);
		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

	/**
	 * 
	 * We don't want two things appending at the same time
	 * 
	 * @param sourceNode
	 * @param newResult
	 */
	public synchronized void append(DistributedQueryResult partialResult) {

		String METHOD_NAME = "append";
		logger.entering(CLASS_NAME, METHOD_NAME);
		partialResult.nodeToResults.keySet().iterator();
		for (Iterator<String> iterator = partialResult.nodeToResults.keySet().iterator(); iterator.hasNext();) {
			String nodeName = iterator.next();
			if (nodeToResults.containsKey(nodeName) && !nodeToResults.get(nodeName).isEmpty())
			{
				logger.warning("Ignoring as already established results for node " + nodeName);
			}
			else
			{
				nodeToResults.put(nodeName, partialResult.nodeToResults.get(nodeName));
			}
		}
		//Conscious Decision to not pull back remote exceptions, we are only concerned with local exceptions
//		if (partialResult.exceptionOccurred) {
//			this.exceptionOccurred = true;
//			this.exceptionMessage = this.exceptionMessage + partialResult.exceptionMessage;			
//		}
		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

	@Override
	public String toString() {
		String METHOD_NAME = "toString";
		logger.entering(CLASS_NAME, METHOD_NAME);
		String resultString = "";
		for (Iterator<String> iterator = nodeToResults.keySet().iterator(); iterator.hasNext();) {
			String nodeName = iterator.next();
			resultString = resultString + "nodename : " + nodeName + "\n";
			if (colNames != null) {
				resultString = resultString + colNames.toString() + "\n";
			}
			List<PersistenceResultRow> results = nodeToResults.get(nodeName);
			for (int i = 0; i < results.size(); i++) {
				PersistenceResultRow row = results.get(i);
				resultString = resultString + row.toString() + "\n";
			}
		}
		logger.exiting(CLASS_NAME, METHOD_NAME);
		return resultString;
	}

	public Object[] toObjectArray() throws PersistenceException {

		String METHOD_NAME = "toObjectArray";
		logger.entering(CLASS_NAME, METHOD_NAME);
		if (exceptionOccurred) {
			throw new PersistenceException(exceptionMessage);
		}
		List<Object> values = new ArrayList<Object>();
		for (Iterator<String> iterator = nodeToResults.keySet().iterator(); iterator.hasNext();) {
			String nodeName = iterator.next();
			List<PersistenceResultRow> results = nodeToResults.get(nodeName);
			for (int i = 0; i < results.size(); i++) {
				PersistenceResultRow row = results.get(i);			
				values.add(row.toArray());
			}
		}	
		Object[] result = values.toArray(new Object[] {});
		logger.exiting(CLASS_NAME, METHOD_NAME);
		return result;
	}
	
	
	/**
	 * Assumption here is that there is only possible result so we just take the first one we come across.
	 * 
	 * @return
	 */
	public String toStringResult() throws PersistenceException {

		String METHOD_NAME = "toStringResult";
		logger.entering(CLASS_NAME, METHOD_NAME);
		if (exceptionOccurred) {
			throw new PersistenceException(exceptionMessage);
		}
		String resultString = null;
		for (Iterator<String> iterator = nodeToResults.keySet().iterator(); iterator.hasNext();) {
			String nodeName = iterator.next();
			List<PersistenceResultRow> results = nodeToResults.get(nodeName);
			if (!results.isEmpty())
			{
				PersistenceResultRow row = results.get(0);
				if (!row.isEmpty())
				{
					resultString = row.getString(1);
					if (resultString != null ) {
						break;
					}
				}
			}
		}
		logger.exiting(CLASS_NAME, METHOD_NAME);
		return resultString;
	}
	
	
	public RegistryObject[] toRegistryObjects(AbstractFactory factory) throws PersistenceException {

		String METHOD_NAME = "toRegistryObjects";
		logger.entering(CLASS_NAME, METHOD_NAME);
		if (exceptionOccurred) {
			throw new PersistenceException(exceptionMessage);
		}
		ArrayList<RegistryObject> objects = new ArrayList<RegistryObject>();
		for (Iterator<String> iterator = nodeToResults.keySet().iterator(); iterator.hasNext();) {
			String nodeName = iterator.next();
			List<PersistenceResultRow> results = nodeToResults.get(nodeName);
			for (int i = 0; i < results.size(); i++) {
				PersistenceResultRow row = results.get(i);
				RegistryObject regObject = null;
				try {
					regObject = factory.create(row);
				} catch (PersistenceException e) {
					logger.warning("failed to process results row " + row.toString());
				}
				if (regObject != null) {
					objects.add(regObject);
				}
			}		
		}		
		RegistryObject[] resultObjects = objects.toArray(new RegistryObject[] {});
		logger.exiting(CLASS_NAME, METHOD_NAME);
		return resultObjects;
	}

	public void setException(Exception e, String nodeName) {
		
		String METHOD_NAME = "setException";
		logger.entering(CLASS_NAME, METHOD_NAME);
		exceptionOccurred = true;
		exceptionMessage = exceptionMessage + nodeName + ":" + e.getMessage() + "\n";
		logger.exiting(CLASS_NAME, METHOD_NAME);		
	}
	
	public boolean exceptionOccurred() {
		return exceptionOccurred;
	}


	public String getExceptionMessage() {
		return exceptionMessage;
	}

}
