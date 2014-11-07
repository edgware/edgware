/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.persistence.distributed;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fabric.core.logging.LogUtil;
import fabric.registry.RegistryObject;
import fabric.registry.exception.PersistenceException;
import fabric.registry.impl.AbstractFactory;
import fabric.registry.persistence.impl.PersistenceResultKeys;
import fabric.registry.persistence.impl.PersistenceResultRow;

public class DistributedQueryResult {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	private final static String CLASS_NAME = DistributedQueryResult.class.getName();
	private final static String PACKAGE_NAME = DistributedQueryResult.class.getPackage().getName();

	private final static Logger logger = Logger.getLogger(PACKAGE_NAME);

	// Json
	static public String JSON_DISTRIBUTED_QUERY_RESULTS = "distributedQueryResults";
	static public String JSON_NODE_RESULTS = "nodeResults";
	static public String JSON_EXCEPTIONS = "exceptions";

	static public String JSON_VALUES = "values";
	static public String JSON_NODENAME = "nodeName";

	protected Map<String, List<PersistenceResultRow>> nodeToResults = new ConcurrentHashMap<String, List<PersistenceResultRow>>();
	protected Map<String, List<String>> nodeToExceptionMessages = new ConcurrentHashMap<String, List<String>>();

	private PersistenceResultKeys colNames = null;
	private boolean exceptionOccurred = false;
	private String localExceptionMessage = "";

	public DistributedQueryResult() {
		super();
	}

	/**
	 * Construct a DistributedQueryResult using the resultSet from the nodeName
	 * 
	 * @param nodeName
	 *            Where the resultSet came from
	 * @param resultSet
	 *            The resultSet to construct DistributedQueryResult from
	 */
	public DistributedQueryResult(String nodeName, ResultSet resultSet) {
		String METHOD_NAME = "constructor";
		logger.entering(CLASS_NAME, METHOD_NAME);
		if (resultSet != null && colNames == null) {
			try {

				colNames = new PersistenceResultKeys(resultSet.getMetaData());
			} catch (SQLException e) {
				logger.warning("Problem reading metadata " + e.getMessage());
			}

		}
		appendResultSet(nodeName, resultSet);
		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

	private void appendResultSet(String nodeName, ResultSet resultSet) {
		String METHOD_NAME = "constructor";
		logger.entering(CLASS_NAME, METHOD_NAME);
		List<PersistenceResultRow> nodeResults = new Vector<PersistenceResultRow>();
		if (resultSet != null) {
			try {
				PersistenceResultRow rowResults = null;
				while (resultSet.next()) {
					rowResults = new PersistenceResultRow(resultSet, colNames);
					nodeResults.add(rowResults);
				}
			} catch (SQLException e) {
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
			if (nodeToResults.containsKey(nodeName) && !nodeToResults.get(nodeName).isEmpty()) {
				logger.warning("Ignoring as already established results for node " + nodeName);
			} else {
				nodeToResults.put(nodeName, partialResult.nodeToResults.get(nodeName));
			}
		}
		// Conscious Decision to not pull back remote exceptions, we are only concerned with local exceptions
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
		if (colNames != null) {
			resultString = resultString + "NodeName\t" + colNames.toString() + "\n";
		} else {
			resultString = resultString + " NO COLUMN NAMES AVAILABLE \n";
		}
		if (nodeToResults.isEmpty() && nodeToExceptionMessages.isEmpty()) {
			resultString = resultString + "NO RESULTS AVAILABLE";
		} else {
			for (Iterator<String> iterator = nodeToResults.keySet().iterator(); iterator.hasNext();) {
				String nodeName = iterator.next();
				List<PersistenceResultRow> results = nodeToResults.get(nodeName);
				if (results != null && !results.isEmpty()) {
					for (int i = 0; i < results.size(); i++) {
						PersistenceResultRow row = results.get(i);
						resultString = resultString + nodeName + ":\t" + row.toString() + "\n";
					}
				}
				List<String> exceptions = nodeToExceptionMessages.get(nodeName);
				if (exceptions != null && !exceptions.isEmpty()) {
					for (int i = 0; i < exceptions.size(); i++) {
						String exception = exceptions.get(i);
						resultString = resultString + nodeName + ":\t EXCEPTION -> " + exception + "\n";
					}
				}
			}
		}
		if (colNames != null) {
			resultString = resultString + "NodeName\t" + colNames.toString() + "\n";
		} else {
			resultString = resultString + " NO COLUMN NAMES AVAILABLE \n";
		}
		logger.exiting(CLASS_NAME, METHOD_NAME);
		return resultString;
	}

	public Object[] toObjectArray() throws PersistenceException {

		String METHOD_NAME = "toObjectArray";
		logger.entering(CLASS_NAME, METHOD_NAME);
		if (exceptionOccurred) {
			throw new PersistenceException(localExceptionMessage);
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
	 * Assumption here is that there is only one possible result so we just take the first one we come across.
	 * 
	 * @return
	 */
	public String toStringResult() throws PersistenceException {

		String METHOD_NAME = "toStringResult";
		logger.entering(CLASS_NAME, METHOD_NAME);
		if (exceptionOccurred) {
			throw new PersistenceException(localExceptionMessage);
		}
		String resultString = null;
		for (Iterator<String> iterator = nodeToResults.keySet().iterator(); iterator.hasNext();) {
			String nodeName = iterator.next();
			List<PersistenceResultRow> results = nodeToResults.get(nodeName);
			if (!results.isEmpty()) {
				PersistenceResultRow row = results.get(0);
				if (!row.isEmpty()) {
					resultString = row.getString(1);
					if (resultString != null) {
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
			throw new PersistenceException(localExceptionMessage);
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

	public void setLocalException(Exception e, String nodeName) {

		String METHOD_NAME = "setLocalException";
		logger.entering(CLASS_NAME, METHOD_NAME);
		localExceptionMessage = LogUtil.stackTrace(e);
		exceptionOccurred = true;
		addExceptionMessage(localExceptionMessage, nodeName);
		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

	public void addExceptionMessage(String exceptionMessage, String nodeName) {
		String METHOD_NAME = "setException";
		logger.entering(CLASS_NAME, METHOD_NAME);
		// Check we have an empty nodeResults to go with the exception
		if (!nodeToResults.containsKey(nodeName)) {
			nodeToResults.put(nodeName, new Vector<PersistenceResultRow>());
		}
		if (!nodeToExceptionMessages.containsKey(nodeName)) {
			nodeToExceptionMessages.put(nodeName, new Vector<String>());
		}
		nodeToExceptionMessages.get(nodeName).add(exceptionMessage);
		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

	public boolean exceptionOccurred() {
		return exceptionOccurred;
	}

	public String getLocalExceptionMessage() {
		return localExceptionMessage;
	}

	/**
	 * Convert to String (Json notation)
	 */
	public String toJsonString() {
		String METHOD_NAME = "toJsonString";

		logger.entering(CLASS_NAME, METHOD_NAME);
		String jsonString = null;

		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();

			JsonGenerator jsonGenerator = new JsonFactory().createGenerator(stream);
//			jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
			toJson(jsonGenerator);
			jsonGenerator.flush();
			jsonGenerator.close();
			jsonString = stream.toString("UTF-8");
		} catch (IOException e) {
			logger.warning("Problem building json " + e.getMessage());
		}
		logger.finest("JsonString = " + jsonString);
		logger.exiting(CLASS_NAME, METHOD_NAME);
		return jsonString;
	}

	/**
	 * Convert to Json
	 * 
	 * @return
	 */
	public void toJson(JsonGenerator jsonGenerator) throws JsonGenerationException, IOException {
		String METHOD_NAME = "toJson";
		logger.entering(CLASS_NAME, METHOD_NAME);

		jsonGenerator.writeStartObject(); // start root object
		jsonGenerator.writeObjectFieldStart(JSON_DISTRIBUTED_QUERY_RESULTS); // start distributedqueryresults

		if (colNames != null) {
			colNames.toJson(jsonGenerator); // column headers
		}

		jsonGenerator.writeArrayFieldStart(JSON_NODE_RESULTS); // start noderesults
		for (Iterator<String> iterator = nodeToResults.keySet().iterator(); iterator.hasNext();) {
			String nodeName = iterator.next();
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringField(JSON_NODENAME, nodeName); // nodename
			// values for the node
			jsonGenerator.writeArrayFieldStart(JSON_VALUES);// start values
			for (Iterator<PersistenceResultRow> resultRows = nodeToResults.get(nodeName).iterator(); resultRows
					.hasNext();) {
				PersistenceResultRow resultRow = resultRows.next();
				resultRow.toJson(jsonGenerator);
			}
			jsonGenerator.writeEndArray(); // end values
			// Exceptions for the node
			if (nodeToExceptionMessages.containsKey(nodeName)) {
				jsonGenerator.writeArrayFieldStart(JSON_EXCEPTIONS);
				for (Iterator<String> exceptions = nodeToExceptionMessages.get(nodeName).iterator(); exceptions
						.hasNext();) {
					String exception = exceptions.next();
					jsonGenerator.writeString(exception);
				}
				jsonGenerator.writeEndArray();
			}
			jsonGenerator.writeEndObject();
		}
		jsonGenerator.writeEndArray(); // end node results
		jsonGenerator.writeEndObject(); // end distributedqueryresults
		jsonGenerator.writeEndObject(); // closing root object
		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

	/**
	 * append to this resultset using the bytes provided which are in the format given
	 * 
	 * @param json
	 * @param format
	 */
	public void append(byte[] bytes, String format) {
		String METHOD_NAME = "append";
		logger.entering(CLASS_NAME, METHOD_NAME, new Object[] {bytes, format});
		switch (format) {
		case "json":
			appendFromJson(bytes);
			break;
		default:
			logger.warning("Format for distributedQueryResult Serialisation not supported.");
			break;
		}
		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

	/**
	 * Append to the resultSet using the bytes given which are in json format.
	 * 
	 * @param json
	 */
	private void appendFromJson(byte[] json) {
		String METHOD_NAME = "appendFromJson";
		logger.entering(CLASS_NAME, METHOD_NAME, new Object[] {json});
		logger.finer("Json = " + new String(json));
		try {
			ObjectMapper jsonObjectMapper = new ObjectMapper();
			JsonNode rootNode = jsonObjectMapper.readTree(json);
			// Only one distributedQueryResults object expected.
			JsonNode distributedQueryResults = rootNode.findValue(JSON_DISTRIBUTED_QUERY_RESULTS);
			// This should have a Column Names object
			JsonNode columnNamesJson = distributedQueryResults.findValue(PersistenceResultKeys.JSON_COLNAMES);
			if (columnNamesJson != null) {
				colNames = new PersistenceResultKeys(columnNamesJson);
			}
			// We can have several nodeResults
			JsonNode nodeResultsJson = distributedQueryResults.findValue(JSON_NODE_RESULTS);
			for (Iterator<JsonNode> nodeResultJsonIter = nodeResultsJson.elements(); nodeResultJsonIter.hasNext();) {
				JsonNode nodeResultJson = nodeResultJsonIter.next();
				List<PersistenceResultRow> nodeResults = new Vector<PersistenceResultRow>();
				// This should have a nodeName
				JsonNode nodeNameJson = nodeResultJson.findValue(JSON_NODENAME);
				String nodeName = nodeNameJson.asText();
				// Should also have a values object
				// This should have a nodeName
				JsonNode valuesJson = nodeResultJson.findValue(JSON_VALUES);
				for (Iterator<JsonNode> valueIter = valuesJson.elements(); valueIter.hasNext();) {
					JsonNode row = valueIter.next();
					nodeResults.add(new PersistenceResultRow(row, colNames));
				}
				JsonNode exceptionsJson = nodeResultJson.findValue(JSON_EXCEPTIONS);
				if (exceptionsJson != null) {
					List<String> exceptionMessages = new Vector<String>();
					nodeToExceptionMessages.put(nodeName, exceptionMessages);
					for (Iterator<JsonNode> exceptionIter = exceptionsJson.elements(); exceptionIter.hasNext();) {
						JsonNode exception = exceptionIter.next();
						if (exception.isTextual()) {
							exceptionMessages.add(exception.asText());
						}
					}
				}

				List<PersistenceResultRow> currentResult = nodeToResults.get(nodeName);
				if (currentResult == null || currentResult.isEmpty()) {
					nodeToResults.put(nodeName, nodeResults);
				}
			}
		} catch (JsonProcessingException e) {
			logger.warning("Error parsing Json : " + e.toString());
		} catch (IOException e) {
			logger.warning("Error parsing Json : " + e.toString());
		}
		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

}
