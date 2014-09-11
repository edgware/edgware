/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.jsonclient.utilities;

/**
 * This class defines constants used throughout the JSON adapter.
 */
public class AdapterConstants {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/*
	 * Class constants
	 */

	/*
	 * Constants used to generate the status codes returned by the JSON adpater. A status code is made by combining a 1
	 * digit ERROR_ code with a 2 digit OP_ code and a 2 digit ENTITY_ code. For example, to indicate an error parsing
	 * the JSON to register a new node, the status code would be expressed as: String status = ERROR_PARSE + OP_REGISTER
	 * + ARTICLE_NODE;
	 */

	public static final int OK = 0x00;

	/* Error codes used in status messages. */
	public static final int ERROR_PARSE = 0x01;
	public static final int ERROR_ACTION = 0x02;

	/* Operation codes used in status messages. */
	public static final int OP_CODE_NONE = 0x01;
	public static final int OP_CODE_REGISTER = 0x02;
	public static final int OP_CODE_DEREGISTER = 0x03;
	public static final int OP_CODE_QUERY = 0x04;
	public static final int OP_CODE_STATE = 0x05;
	public static final int OP_CODE_PUBLISH = 0x06;
	public static final int OP_CODE_SUBSCRIBE = 0x07;
	public static final int OP_CODE_UNSUBSCRIBE = 0x08;
	public static final int OP_CODE_NOTIFY = 0x09;
	public static final int OP_CODE_REQUEST = 0x0A;
	public static final int OP_CODE_RESPONSE = 0x0B;

	/* Entity codes used in status messages */
	public static final int ARTICLE_NONE = 0x01;
	public static final int ARTICLE_JSON = 0x02;
	public static final int ARTICLE_NODE = 0x03;
	public static final int ARTICLE_LOCAL_NODE = 0x04;
	public static final int ARTICLE_NODE_TYPE = 0x05;
	public static final int ARTICLE_BEARER = 0x06;
	public static final int ARTICLE_BEARER_TYPE = 0x07;
	public static final int ARTICLE_PLATFORM = 0x08;
	public static final int ARTICLE_PLATFORM_TYPE = 0x09;
	public static final int ARTICLE_SYSTEM = 0x0A;
	public static final int ARTICLE_SYSTEM_TYPE = 0x0B;
	public static final int ARTICLE_SERVICE_TYPE = 0x0C;
	public static final int ARTICLE_USER = 0x0D;
	public static final int ARTICLE_USER_TYPE = 0x0E;

	/* Supported operations */

	public static final String OP_REGISTER = "register"; // Primary operation, further qualified with an article
	public static final String OP_DEREGISTER = "deregister"; // Primary operation, further qualified with an article
	public static final String OP_QUERY = "query"; // Primary operation, further qualified with an article

	public static final String OP_REGISTER_NODE = "register:node";
	public static final String OP_DEREGISTER_NODE = "deregister:node";
	public static final String OP_QUERY_LOCAL_NODE = "query:local-node";
	public static final String OP_QUERY_NODES = "query:nodes";
	public static final String OP_REGISTER_NODE_TYPE = "register:node-type";
	public static final String OP_DEREGISTER_NODE_TYPE = "deregister:node-type";
	public static final String OP_QUERY_NODE_TYPES = "query:node-types";
	public static final String OP_QUERY_RESPONSE_LOCAL_NODE = "query-result:local-node";
	public static final String OP_QUERY_RESPONSE_NODES = "query-result:nodes";
	public static final String OP_QUERY_RESPONSE_NODE_TYPES = "query-result:node-types";
	public static final String OP_QUERY_RESPONSE_BEARERS = "query-result:bearers";

	public static final String OP_REGISTER_BEARER = "register:bearer";
	public static final String OP_DEREGISTER_BEARER = "deregister:bearer";
	public static final String OP_QUERY_BEARERS = "query:bearers";

	public static final String OP_REGISTER_PLATFORM = "register:platform";
	public static final String OP_DEREGISTER_PLATFORM = "deregister:platform";
	public static final String OP_QUERY_PLATFORMS = "query:platforms";
	public static final String OP_QUERY_RESPONSE_PLATFORMS = "query-result:platforms";

	public static final String OP_REGISTER_PLATFORM_TYPE = "register:platform-type";
	public static final String OP_DEREGISTER_PLATFORM_TYPE = "deregister:platform-type";
	public static final String OP_QUERY_PLATFORM_TYPES = "query:platform-types";
	public static final String OP_QUERY_RESPONSE_PLATFORM_TYPES = "query-result:platform-types";

	public static final String OP_REGISTER_SYSTEM = "register:system";
	public static final String OP_DEREGISTER_SYSTEM = "deregister:system";
	public static final String OP_QUERY_SYSTEMS = "query:systems";
	public static final String OP_QUERY_RESPONSE_SYSTEMS = "query-result:systems";

	public static final String OP_REGISTER_SYSTEM_TYPE = "register:system-type";
	public static final String OP_DEREGISTER_SYSTEM_TYPE = "deregister:system-type";
	public static final String OP_QUERY_SYSTEM_TYPES = "query:system-types";
	public static final String OP_QUERY_RESPONSE_SYSTEM_TYPES = "query-result:system-types";

	public static final String OP_REGISTER_SERVICE_TYPE = "register:service-type";
	public static final String OP_DEREGISTER_SERVICE_TYPE = "deregister:service-type";

	public static final String OP_REGISTER_USER = "register:user";
	public static final String OP_DEREGISTER_USER = "deregister:user";
	public static final String OP_QUERY_USERS = "query:users";
	public static final String OP_QUERY_RESPONSE_USERS = "query-result:users";

	public static final String OP_REGISTER_USER_TYPE = "register:user-type";
	public static final String OP_DEREGISTER_USER_TYPE = "deregister:user-type";
	public static final String OP_QUERY_USER_TYPES = "query:user-types";
	public static final String OP_QUERY_RESPONSE_USER_TYPES = "query-result:user-types";

	public static final String OP_FEED_MESSAGE = "feed-message";

	public static final String OP_STATE = "state";
	public static final String OP_STATE_SYSTEM = "state:system";

	public static final String OP_SERVICE_REQUEST = "request";
	public static final String OP_SERVICE_RESPONSE = "response";

	public static final String OP_NOTIFY = "notify";
	public static final String OP_NOTIFICATION = "notification";

	public static final String OP_PUBLISH = "publish";
	public static final String OP_SUBSCRIBE = "subscribe";
	public static final String OP_UNSUBSCRIBE = "unsubscribe";
	public static final String OP_SUBSCRIPTIONS = "subscriptions";

	public static final String OP_DISCONNECT = "disconnect";

	public static final String OP_SQL_DELETE = "sql-delete";
	public static final String OP_SQL_SELECT = "sql-select";
	public static final String OP_SQL_UPDATE = "sql-update";

	/* JSON object fields */
	public static final String FIELD_CLIENT_ID = "client-id";
	public static final String FIELD_ID = "id";
	public static final String FIELD_NODE_INTERFACE = "nodeInterface";
	public static final String FIELD_TYPE = "type";
	public static final String FIELD_SERVICES = "services";
	public static final String FIELD_AVAILABILITY = "available";
	public static final String FIELD_DESCRIPTION = "desc";
	public static final String FIELD_ATTRIBUTES = "attr";
	public static final String FIELD_CORRELATION_ID = "correl";
	public static final String FIELD_PLATFORM = "platform";
	public static final String FIELD_PLATFORM_TYPE = "platform-type";
	public static final String FIELD_PLATFORM_TYPES = "platform-types";
	public static final String FIELD_SYSTEM = "system";
	public static final String FIELD_SYSTEM_TYPE = "system-type";
	public static final String FIELD_USER = "user";
	public static final String FIELD_USERS = "users";
	public static final String FIELD_USER_TYPE = "user-type";
	public static final String FIELD_USER_TYPES = "user-types";
	public static final String FIELD_NODE = "node";
	public static final String FIELD_NODE_TYPE = "node-type";
	public static final String FIELD_NODE_TYPES = "node-types";
	public static final String FIELD_NODES = "nodes";
	public static final String FIELD_LOCATION = "loc";
	public static final String FIELD_AFFIL = "affil";
	public static final String FIELD_STATUS = "status";
	public static final String FIELD_MESSAGE = "msg";
	public static final String FIELD_ENCODING = "encoding";
	public static final String FIELD_OPERATION = "op";
	public static final String FIELD_SYSTEMS = "systems";
	public static final String FIELD_SYSTEM_TYPES = "system-types";
	public static final String FIELD_PLATFORMS = "platforms";
	public static final String FIELD_PLATFORM_ID = "platform-id";
	public static final String FIELD_ADDRESS = "address";
	public static final String FIELD_INPUT_FEED = "input-feed";
	public static final String FIELD_STATE = "state";
	public static final String FIELD_MODE = "mode";
	public static final String FIELD_BEARER = "bearer";
	public static final String FIELD_BEARERS = "bearers";
	public static final String FIELD_SERVICE_TYPE = "service-type";
	public static final String FIELD_REQUEST_RESPONSE = "request-response";
	public static final String FIELD_SOLICIT_RESPONSE = "solicit-response";
	public static final String FIELD_LISTENER = "listener";
	public static final String FIELD_NOTIFICATION = "notification";
	public static final String FIELD_OUTPUT_FEED = "output-feed";
	public static final String FIELD_OUTPUT_FEEDS = "output-feeds";
	public static final String FIELD_SUFFIX_TYPE = "-type";
	public static final String FIELD_SQL_KEY = "sql-key";
	public static final String FIELD_SQL_NODE = "sql-node";
	public static final String FIELD_SQL_TABLE = "sql-table";
	public static final String FIELD_SQL_UPDATE_RESULT = "sql-update-result";
	public static final String FIELD_SQL_VALUE = "sql-value";

	/* Table types */
	public static final String TABLE_DEFAULT_CONFIG = "DEFAULT_CONFIG";
	public static final String TABLE_NODE_CONFIG = "NODE_CONFIG";

	/* JSON object field values */
	public static final String FIELD_VALUE_ENCODING_ASCII = "ascii";
	public static final String FIELD_VALUE_UKNOWN = "unknown";

	/* Supported Modes */
	public static final String MODE_INPUT = "input-feed";
	public static final String MODE_OUTPUT = "output-feed";
	public static final String MODE_NOTIFY = "notification";
	public static final String MODE_LISTEN = "listener";
	public static final String MODE_SOLICIT = "solicit-response";
	public static final String MODE_RESPONSE = "request-response";

	/* Supported Directions */
	public static final String DIRECTION_INPUT = "input";
	public static final String DIRECTION_OUTPUT = "output";
	public static final String DIRECTION_SOLICIT_RESPONSE = "solicit_response";
	public static final String DIRECTION_REQUEST_RESPONSE = "request_response";
	public static final String DIRECTION_NOTIFICATION = "notification";
	public static final String DIRECTION_ONE_WAY = "one_way";

	/* Supported States */
	public static final String STATE_AVAILABLE = "AVAILABLE";
	public static final String STATE_UNAVAILABLE = "UNAVAILABLE";
	public static final String STATE_RUNNING = "running";
	public static final String STATE_STOPPED = "stopped";

	/* JSON object location fields */
	public static final String FIELD_LATITUDE = "lat";
	public static final String FIELD_LONGITUDE = "long";
	public static final String FIELD_ALTITUDE = "alt";
	public static final String FIELD_LOCATION_TOP = "top";
	public static final String FIELD_LOCATION_BOTTOM = "bottom";
	public static final String FIELD_LOCATION_LEFT = "left";
	public static final String FIELD_LOCATION_RIGHT = "right";

	/* Messages */
	public static final String STATUS_MSG_OK = "OK";
	public static final String STATUS_MSG_MISSING_FIELDS = "Required fields are missing.";
	public static final String STATUS_MSG_FAILED_DELETE = "Failed to delete article from Registry.";
	public static final String STATUS_MSG_BAD_JSON = "Invalid JSON.";
	public static final String STATUS_MSG_BAD_SQL = "Invalid query request.";
	public static final String STATUS_MSG_BAD_OPERATION = "Unrecognized operation.";
	public static final String STATUS_MSG_BAD_MODE = "Unrecognized mode.";
	public static final String STATUS_MSG_BAD_STATE = "Unrecognized system state.";
	public static final String STATUS_MSG_UNRECOGNIZED_TYPE = "Unrecognized type.";

}
