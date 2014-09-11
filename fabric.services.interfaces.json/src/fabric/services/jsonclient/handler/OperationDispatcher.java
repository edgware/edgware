/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.jsonclient.handler;

import fabric.FabricBus;
import fabric.services.json.JSON;
import fabric.services.jsonclient.articles.Bearers;
import fabric.services.jsonclient.articles.NodeTypes;
import fabric.services.jsonclient.articles.Nodes;
import fabric.services.jsonclient.articles.PlatformTypes;
import fabric.services.jsonclient.articles.Platforms;
import fabric.services.jsonclient.articles.Registry;
import fabric.services.jsonclient.articles.ServiceTypes;
import fabric.services.jsonclient.articles.SystemTypes;
import fabric.services.jsonclient.articles.Systems;
import fabric.services.jsonclient.articles.UserTypes;
import fabric.services.jsonclient.articles.Users;
import fabric.services.jsonclient.utilities.AdapterConstants;
import fabric.services.jsonclient.utilities.AdapterStatus;
import fabric.services.systems.RuntimeManager;

/**
 * Class that dispatches all incoming operations to their respective handlers.
 */
public class OperationDispatcher extends FabricBus {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/*
	 * Class methods
	 */

	/**
	 * Handles a query operation.
	 * 
	 * @param operation
	 *            The name of the requested operation.
	 * 
	 * @param op
	 *            The full operation message.
	 * 
	 * @param correlId
	 *            The correlation ID of the request.
	 * 
	 * @return the operation response.
	 */
	public static JSON query(final String operation, final JSON op, String correlId) {

		JSON response = null;

		/* Determine what operation has been requested, and invokes the correct class and method. */

		switch (operation) {

		case AdapterConstants.OP_QUERY_LOCAL_NODE:

			response = Nodes.queryLocalNode(correlId);
			break;

		case AdapterConstants.OP_QUERY_NODES:

			response = Nodes.query(op, correlId);
			break;

		case AdapterConstants.OP_QUERY_NODE_TYPES:
			response = NodeTypes.query(op, correlId);
			break;

		case AdapterConstants.OP_QUERY_BEARERS:

			response = Bearers.query(op, correlId);
			break;

		case AdapterConstants.OP_QUERY_SYSTEMS:

			response = Systems.query(op, correlId);
			break;

		case AdapterConstants.OP_QUERY_SYSTEM_TYPES:

			response = SystemTypes.query(op, correlId);
			break;

		case AdapterConstants.OP_QUERY_PLATFORMS:

			response = Platforms.query(op, correlId);
			break;

		case AdapterConstants.OP_QUERY_PLATFORM_TYPES:

			response = PlatformTypes.query(op, correlId);
			break;

		case AdapterConstants.OP_QUERY_USERS:

			response = Users.query(op, correlId);
			break;

		case AdapterConstants.OP_QUERY_USER_TYPES:

			response = UserTypes.query(op, correlId);
			break;

		case AdapterConstants.OP_SQL_DELETE:
			response = Registry.executeDeleteQuery(op);
			break;

		case AdapterConstants.OP_SQL_SELECT:

			response = Registry.executeSelectQuery(op);
			break;

		case AdapterConstants.OP_SQL_UPDATE:

			response = Registry.executeUpdateQuery(op);
			break;

		default:

			AdapterStatus status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_NONE,
					AdapterConstants.ARTICLE_JSON, AdapterConstants.STATUS_MSG_BAD_OPERATION, correlId);
			response = status.toJsonObject();
			break;
		}
		return response;
	}

	/**
	 * Handles a registration operation.
	 * 
	 * @param operation
	 *            The name of the requested operation.
	 * 
	 * @param op
	 *            The full operation message.
	 * 
	 * @param correlId
	 *            The correlation ID of the request.
	 * 
	 * @param isRegister
	 *            flag indicating if this is a registration (<code>true</code>) or deregistration (<code>false</code>)
	 *            operation.
	 * 
	 * @return the operation response.
	 */
	public static JSON registration(final String operation, final JSON op, boolean isRegister, String correlId) {

		JSON response = null;
		String id = null;
		String subOperation = operation.split(":")[1];

		if (subOperation.endsWith(AdapterConstants.FIELD_SUFFIX_TYPE)) {
			id = op.getString(AdapterConstants.FIELD_TYPE);
		} else {
			id = op.getString(AdapterConstants.FIELD_ID);
		}

		/* Determine what operation has been requested, and invokes the correct class and method. */

		switch (subOperation) {

		case AdapterConstants.FIELD_NODE:

			if (isRegister) {
				response = Nodes.register(op, correlId);
			} else {
				response = Nodes.deregister(id, correlId);
			}
			break;

		case AdapterConstants.FIELD_NODE_TYPE:

			if (isRegister) {
				response = NodeTypes.register(op, correlId);
			} else {
				response = NodeTypes.deregister(id, correlId);
			}
			break;

		case AdapterConstants.FIELD_BEARER:

			if (isRegister) {
				response = Bearers.register(op, correlId);
			} else {
				response = Bearers.deregister(id, correlId);
			}
			break;

		case AdapterConstants.FIELD_PLATFORM:

			if (isRegister) {
				response = Platforms.register(op, correlId);
			} else {
				response = Platforms.deregister(id, correlId);
			}
			break;

		case AdapterConstants.FIELD_PLATFORM_TYPE:

			if (isRegister) {
				response = PlatformTypes.register(op, correlId);
			} else {
				response = PlatformTypes.deregister(id, correlId);
			}
			break;

		case AdapterConstants.FIELD_SYSTEM:

			if (isRegister) {
				response = Systems.register(op, correlId);
			} else {
				response = Systems.deregister(id, correlId);
			}
			break;

		case AdapterConstants.FIELD_SYSTEM_TYPE:

			if (isRegister) {
				response = SystemTypes.register(op, correlId);
			} else {
				response = SystemTypes.deregister(id, correlId);
			}
			break;

		case AdapterConstants.FIELD_SERVICE_TYPE:

			if (isRegister) {
				response = ServiceTypes.register(op, correlId);
			} else {
				response = ServiceTypes.deregister(id, correlId);
			}
			break;

		case AdapterConstants.FIELD_USER:

			if (isRegister) {
				response = Users.register(op, correlId);
			} else {
				response = Users.deregister(id, correlId);
			}
			break;

		case AdapterConstants.FIELD_USER_TYPE:

			if (isRegister) {
				response = UserTypes.register(op, correlId);
			} else {
				response = UserTypes.deregister(id, correlId);
			}
			break;

		default:

			AdapterStatus status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_NONE,
					AdapterConstants.ARTICLE_JSON, AdapterConstants.STATUS_MSG_BAD_OPERATION, correlId);
			response = status.toJsonObject();
			break;

		}

		return response;
	}

	/**
	 * Handles a system state change operation.
	 * 
	 * @param operation
	 *            The name of the requested operation.
	 * 
	 * @param client
	 *            adapter-specific ID of the client, used to target messages sent to the client.
	 * 
	 * @param op
	 *            The full operation message.
	 * 
	 * @param runtimeManager
	 *            the runtime manager instance responsible for managing this system instance.
	 * 
	 * @param adapterProxy
	 *            the name of the class implementing the system adapter proxy for the JSON Fabric client.
	 * 
	 * @param correlId
	 *            The correlation ID of the request.
	 * 
	 * @return the operation response.
	 */
	public static JSON stateChange(String operation, Object client, JSON op, RuntimeManager runtimeManager,
			String adapterProxy, String correlId) {

		JSON response = null;

		/* Determine what operation has been requested, and invoke the correct class and method. */

		String subOperation = operation.split(":")[1];

		switch (subOperation) {

		case AdapterConstants.FIELD_SYSTEM:

			response = Systems.changeState(op, client, runtimeManager, adapterProxy, correlId);
			break;

		default:

			AdapterStatus status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_NONE,
					AdapterConstants.ARTICLE_JSON, AdapterConstants.STATUS_MSG_BAD_OPERATION, correlId);
			response = status.toJsonObject();
			break;

		}

		return response;
	}

	/**
	 * Handles a system request/response operation.
	 * 
	 * @param operation
	 *            The name of the requested operation.
	 * 
	 * @param op
	 *            The full operation message.
	 * 
	 * @param runtimeManager
	 *            the runtime manager instance responsible for managing this system instance.
	 * 
	 * @param correlId
	 *            The correlation ID of the request.
	 * 
	 * @return the operation response.
	 */
	public static JSON serviceOperation(String operation, JSON op, RuntimeManager runtimeManager, String correlId) {

		JSON response = null;

		/* Determine what operation has been requested, and invokes the correct class and method. */

		switch (operation) {

		case AdapterConstants.OP_SERVICE_REQUEST:

			response = Systems.request(op, runtimeManager, correlId);
			break;

		case AdapterConstants.OP_SERVICE_RESPONSE:

			response = Systems.response(op, runtimeManager, correlId);
			break;

		case AdapterConstants.OP_NOTIFY:

			response = Systems.notify(op, runtimeManager, correlId);
			break;

		case AdapterConstants.OP_PUBLISH:

			response = Systems.publish(op, runtimeManager, correlId);
			break;

		case AdapterConstants.OP_SUBSCRIBE:

			response = Systems.subscribe(op, runtimeManager, correlId);
			break;

		case AdapterConstants.OP_UNSUBSCRIBE:

			response = Systems.unsubscribe(op, runtimeManager, correlId);
			break;

		case AdapterConstants.OP_DISCONNECT:

			response = Systems.disconnect(op, runtimeManager);
			break;

		default:

			AdapterStatus status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_NONE,
					AdapterConstants.ARTICLE_JSON, AdapterConstants.STATUS_MSG_BAD_OPERATION, correlId);
			response = status.toJsonObject();
			break;

		}

		return response;
	}
}
