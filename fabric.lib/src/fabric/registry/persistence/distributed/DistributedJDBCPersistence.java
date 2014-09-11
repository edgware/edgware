/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.persistence.distributed;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import fabric.Fabric;
import fabric.bus.SharedChannel;
import fabric.bus.messages.FabricMessageFactory;
import fabric.bus.messages.IFabricMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.messages.impl.MessagePayload;
import fabric.bus.messages.impl.ServiceMessage;
import fabric.core.io.ICallback;
import fabric.core.io.InputTopic;
import fabric.core.io.Message;
import fabric.core.io.OutputTopic;
import fabric.core.properties.ConfigProperties;
import fabric.core.properties.Properties;
import fabric.registry.FabricRegistry;
import fabric.registry.RegistryObject;
import fabric.registry.exception.PersistenceException;
import fabric.registry.impl.AbstractFactory;
import fabric.registry.persistence.Persistence;
import fabric.registry.persistence.impl.SingletonJDBCPersistence;
import fabric.services.floodmessage.FloodRouting;

public class DistributedJDBCPersistence implements Persistence, ICallback {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	private final static String CLASS_NAME = DistributedJDBCPersistence.class.getName();
	private final static String PACKAGE_NAME = DistributedJDBCPersistence.class.getPackage().getName();
	private final static Logger logger = Logger.getLogger(PACKAGE_NAME);

	SharedChannel commandChannel;
	SharedChannel resultChannel;

	private Properties myConfig;

	// The service
	public static String SERVICE_NAME = DistributedPersistenceFablet.class.getName();
	public static String PLUGIN_FAMILY = Fabric.FABRIC_PLUGIN_FAMILY;

	// Possible ACTIONS
	public static final String QUERY_ACTION = "Query";
	public static final String PARTIAL_RESULT_ACTION = "PartialResult";
	public static final String FINAL_RESULT_ACTION = "FinalResult";

	private OutputTopic commandChannelTopic;
	private InputTopic resultChannelTopic;

	// Milliseconds timeout to wait for responses to query
	public static String DEFAULT_RESPONSE_TIMEOUT = "15000";
	public static String DEFAULT_RESPONSE_TIMEOUT_DECREMENT = "500";
	private int queryTimeOut = -1;
	private int queryTimeOutDecrement = -1;
	private String nodeName;

	private SingletonJDBCPersistence localJDBCPersistence;

	private ConcurrentHashMap<String, DistributedQueryResult> resultByCorrelationId = new ConcurrentHashMap<String, DistributedQueryResult>();
	private Map<String, DistributedQueryWaitThread> waitThreadsByCorrelationId = new TreeMap<String, DistributedQueryWaitThread>();

	private boolean fabricConnected = false;

	/**
	 * 
	 */
	public DistributedJDBCPersistence() {

		localJDBCPersistence = new SingletonJDBCPersistence();
	}

	public static String getCommandTopic(Properties props, String nodeName) {

		String METHOD_NAME = "getCommandTopic";
		logger.entering(CLASS_NAME, METHOD_NAME, new Object[] {props, nodeName});
		String commandTopicName = props.lookupProperty(ConfigProperties.REGISTRY_COMMAND_TOPIC,
				ConfigProperties.REGISTRY_COMMAND_TOPIC_DEFAULT, nodeName);
		logger.exiting(CLASS_NAME, METHOD_NAME, commandTopicName);
		return commandTopicName;
	}

	public static String getResultTopic(Properties props, String nodeName) {

		String METHOD_NAME = "init";
		logger.entering(CLASS_NAME, METHOD_NAME, new Object[] {props, nodeName});
		String resultTopicName = props.lookupProperty(ConfigProperties.REGISTRY_RESULT_TOPIC,
				ConfigProperties.REGISTRY_RESULT_TOPIC_DEFAULT, nodeName);
		logger.exiting(CLASS_NAME, METHOD_NAME, resultTopicName);
		return resultTopicName;
	}

	@Override
	public void init(String Url, Properties config) throws PersistenceException {

		String METHOD_NAME = "init";
		logger.entering(CLASS_NAME, METHOD_NAME, new Object[] {Url, config});
		localJDBCPersistence.init(Url, config);
		myConfig = config;
		nodeName = config.getProperty(ConfigProperties.NODE_NAME);
		commandChannelTopic = new OutputTopic(config.lookupProperty(ConfigProperties.REGISTRY_COMMAND_TOPIC,
				ConfigProperties.REGISTRY_COMMAND_TOPIC_DEFAULT, nodeName));
		resultChannelTopic = new InputTopic(config.lookupProperty(ConfigProperties.REGISTRY_RESULT_TOPIC,
				ConfigProperties.REGISTRY_RESULT_TOPIC_DEFAULT, nodeName));
		logger.finest("RequestTopic = " + commandChannelTopic + " , ResponseTopicName = " + resultChannelTopic);
		queryTimeOut = new Integer(config.getProperty(ConfigProperties.REGISTRY_DISTRIBUTED_TIMEOUT,
				DEFAULT_RESPONSE_TIMEOUT));
		queryTimeOutDecrement = new Integer(config.getProperty(ConfigProperties.REGISTRY_DISTRIBUTED_TIMEOUT_DECREMENT,
				DEFAULT_RESPONSE_TIMEOUT_DECREMENT));
		logger.finest("Query Response timeout set to = " + queryTimeOut);
		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

	@Override
	public void connect() throws PersistenceException {

		String METHOD_NAME = "connect";
		logger.entering(CLASS_NAME, METHOD_NAME);
		localJDBCPersistence.connect();
		// Connection for Distributed setup on first query , needs full Fabric initialisation first
		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

	@Override
	public void disconnect() throws PersistenceException {

		String METHOD_NAME = "disconnect";
		logger.entering(CLASS_NAME, METHOD_NAME);
		localJDBCPersistence.disconnect();
		if (commandChannel != null) {
			try {
				commandChannel.close();
			} catch (IOException e) {
				logger.warning("Couldn't close Channel to " + commandChannelTopic);
				throw new PersistenceException("Couldn't close Channel to " + commandChannelTopic, e);
			}
		}
		if (resultChannel != null) {
			try {
				resultChannel.close();
			} catch (IOException e) {
				logger.warning("Couldn't close Channel to " + resultChannelTopic);
				throw new PersistenceException("Couldn't close Channel to " + resultChannelTopic, e);
			}
		}
		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

	private void checkFabricConnection() throws PersistenceException {

		String METHOD_NAME = "checkFabricConnection";
		logger.entering(CLASS_NAME, METHOD_NAME);
		if (!fabricConnected) {
			try {
				if (FabricRegistry.homeNodeEndPoint == null) {
					logger.warning("Fabric has not been fully initialisation cannot Connect to Fabric");
					throw new PersistenceException("Couldn't open Channels, Fabric has not been fully initialised");
				}
				commandChannel = FabricRegistry.homeNodeEndPoint.openOutputChannel(commandChannelTopic);
				resultChannel = FabricRegistry.homeNodeEndPoint.openInputChannel(resultChannelTopic, this);
				logger.finest("Created channel");
			} catch (UnsupportedOperationException e) {
				logger.warning("Couldn't open Channel to " + commandChannelTopic + " and to " + resultChannelTopic);
				throw new PersistenceException("Couldn't open Channel to " + commandChannelTopic + " and to "
						+ resultChannelTopic, e);
			} catch (IOException e) {
				logger.warning("Couldn't open Channel to " + commandChannelTopic + " and to " + resultChannelTopic);
				throw new PersistenceException("Couldn't open Channel to " + commandChannelTopic + " and to "
						+ resultChannelTopic, e);
			}
			fabricConnected = true;
		}
		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

	@Override
	public RegistryObject[] queryRegistryObjects(String queryString, AbstractFactory factory, boolean localOnly)
			throws PersistenceException {

		String METHOD_NAME = "queryRegistryObjects";
		logger.entering(CLASS_NAME, METHOD_NAME, new Object[] {queryString, factory, localOnly});
		RegistryObject[] results = null;
		if (localOnly) {
			results = localJDBCPersistence.queryRegistryObjects(queryString, factory, localOnly);
		} else {
			ServiceMessage serviceMessage = constructMessage(queryString);
			DistributedQueryResult queryResult = distributeQuery(serviceMessage);

			if (queryResult != null) {
				results = queryResult.toRegistryObjects(factory);
			}
		}
		logger.exiting(CLASS_NAME, METHOD_NAME, results);
		return results;
	}

	@Override
	public String queryString(String sqlString, boolean localOnly) throws PersistenceException {

		String METHOD_NAME = "queryString";
		logger.entering(CLASS_NAME, METHOD_NAME, new Object[] {sqlString, localOnly});
		String result;
		if (localOnly) {
			result = localJDBCPersistence.queryString(sqlString, localOnly);
		} else {
			ServiceMessage serviceMessage = constructMessage(sqlString);
			DistributedQueryResult queryResult = distributeQuery(serviceMessage);
			result = queryResult.toStringResult();
		}
		logger.exiting(CLASS_NAME, METHOD_NAME, result);
		return result;
	}

	@Override
	public Object[] query(String queryString, boolean localOnly) throws PersistenceException {

		String METHOD_NAME = "query";
		logger.entering(CLASS_NAME, METHOD_NAME, new Object[] {queryString, localOnly});
		Object[] result;
		if (localOnly) {
			result = localJDBCPersistence.query(queryString, localOnly);
		} else {
			ServiceMessage serviceMessage = constructMessage(queryString);
			DistributedQueryResult queryResult = distributeQuery(serviceMessage);
			result = queryResult.toObjectArray();
		}
		logger.exiting(CLASS_NAME, METHOD_NAME, result);
		return result;
	}

	@Override
	public boolean updateRegistryObject(String updateString, boolean ignoreDuplicateWarning)
			throws PersistenceException {

		String METHOD_NAME = "updateRegistryObject";
		logger.entering(CLASS_NAME, METHOD_NAME, updateString);
		// Updates are local Only
		boolean result = localJDBCPersistence.updateRegistryObject(updateString, ignoreDuplicateWarning);
		logger.exiting(CLASS_NAME, METHOD_NAME, result);
		return result;
	}

	@Override
	public boolean updateRegistryObject(String updateString) throws PersistenceException {

		String METHOD_NAME = "updateRegistryObject";
		logger.entering(CLASS_NAME, METHOD_NAME, updateString);
		// Updates are local Only
		boolean result = localJDBCPersistence.updateRegistryObject(updateString);
		logger.exiting(CLASS_NAME, METHOD_NAME, result);
		return result;
	}

	@Override
	public boolean updateRegistryObjects(String[] updateStrings) throws PersistenceException {

		String METHOD_NAME = "updateRegistryObjects";
		logger.entering(CLASS_NAME, METHOD_NAME, updateStrings);
		// Updates are local Only
		boolean result = localJDBCPersistence.updateRegistryObjects(updateStrings);
		logger.exiting(CLASS_NAME, METHOD_NAME, result);
		return result;
	}

	private ServiceMessage constructMessage(String query) throws PersistenceException {

		String METHOD_NAME = "constructMessage";
		logger.entering(CLASS_NAME, METHOD_NAME, query);
		checkFabricConnection();

		String myCorrelationId = FabricMessageFactory.generateUID();
		logger.finest("Correlation ID for message : " + myCorrelationId);
		/* Create the service message */
		ServiceMessage serviceMessage = new ServiceMessage();

		/* Set the service name: i.e. indicate that this is a message for the registry query service */
		serviceMessage.setServiceName(SERVICE_NAME);
		/* Indicate that this is a built-in Fabric plug-in */
		serviceMessage.setServiceFamilyName(PLUGIN_FAMILY);

		// Decrease the timeout in the message for onward waiting
		int newQueryTimeOut = queryTimeOut - queryTimeOutDecrement;
		// If our timeout has reached 0 then we don't flood any further and log a message
		if (newQueryTimeOut < 1) {
			logger.warning("timeout too low or timeoutDecrement too high query will not last even one hop! Resetting to allow a single hop");
			serviceMessage.setProperty(ConfigProperties.REGISTRY_DISTRIBUTED_TIMEOUT, Integer.toString(queryTimeOut));
		} else {
			serviceMessage
					.setProperty(ConfigProperties.REGISTRY_DISTRIBUTED_TIMEOUT, Integer.toString(newQueryTimeOut));
		}

		serviceMessage.setProperty(ConfigProperties.REGISTRY_DISTRIBUTED_TIMEOUT_DECREMENT, Integer
				.toString(queryTimeOutDecrement));

		serviceMessage.setRouting(new FloodRouting(nodeName));

		serviceMessage.setCorrelationID(myCorrelationId);
		serviceMessage.setNotificationTimeout(queryTimeOut);

		serviceMessage.setAction(DistributedJDBCPersistence.QUERY_ACTION);
		
		serviceMessage.setNotification(false);
		serviceMessage.setActionEnRoute(true);

		// Add query to service message
		MessagePayload mp = new MessagePayload();
		mp.setPayloadText(query);
		serviceMessage.setPayload(mp);

		logger.exiting(CLASS_NAME, METHOD_NAME, serviceMessage);
		return serviceMessage;
	}

	/*
	 * 
	 */
	private DistributedQueryResult distributeQuery(ServiceMessage serviceMessage) throws PersistenceException {

		String METHOD_NAME = "distributeQuery";
		logger.entering(CLASS_NAME, METHOD_NAME, serviceMessage);
		DistributedQueryResult result = null;
		try {
			/* Send the command to the local Fabric Manager */
			logger.fine("Sending command: " + serviceMessage.toXML());
			DistributedQueryWaitThread thread = new DistributedQueryWaitThread(queryTimeOut, serviceMessage
					.getCorrelationID());
			waitThreadsByCorrelationId.put(serviceMessage.getCorrelationID(), thread);
			thread.start();
			commandChannel.write(serviceMessage.toWireBytes());
			thread.join();
			if (resultByCorrelationId.containsKey(serviceMessage.getCorrelationID())) {
				result = resultByCorrelationId.remove(serviceMessage.getCorrelationID());
			} else {
				logger.finer("No result retrieved for query");
			}
		} catch (Exception e) {
			throw new PersistenceException("Failed to send distributed query", e);
		}
		if (result != null && result.exceptionOccurred()) {
			throw new PersistenceException(result.getExceptionMessage());
		}
		if (result != null) {
			logger.finest("Results:" + result.toString());
		}
		logger.exiting(CLASS_NAME, METHOD_NAME, result);
		return result;
	}

	@Override
	public void handleMessage(Message message) {

		String METHOD_NAME = "handleMessage";
		logger.entering(CLASS_NAME, METHOD_NAME, message);
		String messageTopic = (String) message.topic;
		byte[] messageData = message.data;
		IFabricMessage parsedMessage = null;
		try {

			/* Parse the message */
			parsedMessage = FabricMessageFactory.create(messageTopic, messageData);
			if (parsedMessage instanceof IServiceMessage) {
				IServiceMessage serviceMessage = (IServiceMessage) parsedMessage;
				String action = serviceMessage.getAction();
				String correlationId = serviceMessage.getCorrelationID();

				switch (action) {
				case DistributedJDBCPersistence.FINAL_RESULT_ACTION:
					if (waitThreadsByCorrelationId.containsKey(correlationId)) {
						logger.finest("This is a correlationId I am looking for");
						ByteArrayInputStream bytein = new ByteArrayInputStream(serviceMessage.getPayload()
								.getPayloadBytes());
						ObjectInputStream in = new ObjectInputStream(bytein);
						DistributedQueryResult result = (DistributedQueryResult) in.readObject();
						in.close();
						bytein.close();
						logger.finest("Got the DistributedQueryResult");
						resultByCorrelationId.put(correlationId, result);
						waitThreadsByCorrelationId.remove(correlationId).interrupt();
					}
					break;
				default:
					logger.finest("Ignoring action " + action);
					break;
				}
			}
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}
		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

	@Override
	public void cancelCallback(Object arg1) {


	}

	@Override
	public void startCallback(Object arg1) {

	

	}

	public DistributedQueryResult getDistributedQueryResult(String sqlString, String nodeName)
			throws PersistenceException {

		return localJDBCPersistence.getDistributedQueryResult(sqlString, nodeName);
	}
}
