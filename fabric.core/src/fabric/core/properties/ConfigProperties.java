/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.properties;

/**
 * Constant definitions for Fabric configuration property names.
 */
public class ConfigProperties {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/*
	 * Class constants
	 */

	/* Discovery */

	/** Node discovery configuration property: <code>true</code> for enabled, <code>false</code> otherwise. */
	public static final String NODE_DISCOVERY = "discovery.node";

	/** Registry discovery configuration property: <code>true</code> for enabled, <code>false</code> otherwise. */
	public static final String REGISTRY_DISCOVERY = "discovery.registry";

	/* Registry configuration */

	/** Registry type configuration property. */
	public static final String REGISTRY_TYPE = "registry.type";

	/** Registry protocol configuration property. */
	public static final String REGISTRY_PROTOCOL = "registry.protocol";

	/** Registry address configuration property. */
	public static final String REGISTRY_ADDRESS = "registry.address";

	/**
	 * Registry UID configuration property: allows the distributed Registry to check if two connected Fabric managers
	 * are using the same Registry.
	 */
	public static final String REGISTRY_UID = "registry.uid";

	/** Registry reconnection flag configuration property: <code>true</code> for enabled, <code>false</code> otherwise. */
	public static final String REGISTRY_RECONNECT = "registry.reconnect";

	/**
	 * Configuration property to enable performance logging at <code>FINE</code> or <code>INFO</code> log levels for the
	 * distributed Registry
	 */
	public static final String REGISTRY_DISTRIBUTED_PERF_LOGGING = "fabric.perf.logging";

	/** Timeout configuration property indicating how long the distributed Registry will wait for a response. */
	public static final String REGISTRY_DISTRIBUTED_TIMEOUT = "registry.distributed.timeout";

	/**
	 * Configuration property indicating the amount by which the distributed Registry timeout is reduced at each hop in
	 * the flood, to avoid requests timing-out before partial results are returned.
	 **/
	public static final String REGISTRY_DISTRIBUTED_TIMEOUT_DECREMENT = "registry.distributed.timeout.decrement";

	/**
	 * Configuration property indicating whether remote distributed queries received should be forwarded to our
	 * neighbours. Setting this to true will increase network traffic but would allow distributed queries to reach nodes
	 * not visible to the original querying node.
	 */
	public static final String REGISTRY_DISTRIBUTED_FLOOD_REMOTE_QUERY = "registry.distributed.flood.remote.query";

	/** Default value for indicating whether remote distributed queries should be forwarded to our neighbours */
	public static final String REGISTRY_DISTRIBUTED_FLOOD_REMOTE_QUERY_DEFAULT = "true";

	/** Distributed query topic configuration property. */
	public static final String REGISTRY_COMMAND_TOPIC = "registry.distributed.command.topic";

	/** Default value for the distributed query topic. */
	public static final String REGISTRY_COMMAND_TOPIC_DEFAULT = "$fabric/{0}/$registry/$command";

	/** Distributed query result topic configuration property. */
	public static final String REGISTRY_RESULT_TOPIC = "registry.distributed.result.topic";

	/** Default value for the distributed query result topic. */
	public static final String REGISTRY_RESULT_TOPIC_DEFAULT = "$fabric/{0}/$registry/$results";

	/* Node configuration */

	/** Node name configuration property. */
	public static final String NODE_NAME = "fabric.node";

	/** The default node name. */
	public static final String DEFAULT_NAME = "default";

	/** Configuration property for the interface(s) to which the node will connect (comma-separated list). */
	public static final String NODE_INTERFACES = "fabric.node.interfaces";

	/** Default interface(s) to which the node will connect. */
	public static final String NODE_INTERFACES_DEFAULT = "lo0";

	/*
	 * Fabric topic configuration
	 */

	/** Fabric session <em>send</em> service commands topic configuration property. */
	public static final String TOPIC_SEND_SESSION_COMMANDS = "fabric.commands.bus";

	/** Default Fabric session <em>send</em> service commands topic. */
	public static final String TOPIC_SEND_SESSION_COMMANDS_DEFAULT = "$fabric/{0}/$commands/$bus";

	/** Fabric session <em>receive</em> service commands topic configuration property. */
	public static final String TOPIC_RECEIVE_SESSION_COMMANDS = "fabric.commands.clients";

	/** Default Fabric session <em>receive</em> service commands topic. */
	public static final String TOPIC_RECEIVE_SESSION_COMMANDS_DEFAULT = "$fabric/{0}/$commands/$clients/{1}/{2}";

	/** Fabric session connection/disconnection messages topic configuration property. */
	public static final String TOPIC_RECEIVE_SESSION_TOPOLOGY = "fabric.commands.topology";

	/** Default Fabric session connection/disconnection messages topic. */
	public static final String TOPIC_RECEIVE_SESSION_TOPOLOGY_DEFAULT = "$fabric/{0}/$commands/$topology";

	/** Fabric session <em>receive</em> platform commands topic configuration property. */
	public static final String TOPIC_RECEIVE_PLATFORM_COMMANDS = "fabric.commands.platforms";

	/** Default Fabric session <em>receive</em> platform commands topic. */
	public static final String TOPIC_RECEIVE_PLATFORM_COMMANDS_DEFAULT = "$fabric/{0}/$commands/$platforms/{1}";

	/** Fabric session <em>receive</em> services commands topic configuration property. */
	public static final String TOPIC_RECEIVE_SERVICES_COMMANDS = "fabric.commands.services";

	/** Default Fabric session <em>receive</em> services commands topic. */
	public static final String TOPIC_RECEIVE_SERVICES_COMMANDS_DEFAULT = "$fabric/{0}/$commands/$services/{1}/{2} ";

	/** Fabric data feed bus topic configuration property. */
	public static final String TOPIC_FEEDS_BUS = "fabric.feeds.bus";

	/** Default Fabric data feed bus topic. */
	public static final String TOPIC_FEEDS_BUS_DEFAULT = "$fabric/{0}/$feeds/$bus";

	/** Fabric discovery messages topic configuration property. */
	public static final String AUTO_DISCOVERY_TOPIC = "fabric.discovery.topic";

	/** Default Fabric discovery messages topic. */
	public static final String AUTO_DISCOVERY_TOPIC_DEFAULT = "$fabric/{0}/$discovery";

	/*
	 * MQTT configuration
	 */

	/** Broker external IP port configuration property, integer value (for connections from other nodes). */
	public static final String MQTT_REMOTE_PORT = "mqtt.ip.port.remote";

	/** Default value for the broker external IP port, integer value (for connections from other nodes). */
	public static final String MQTT_REMOTE_PORT_DEFAULT = "1883";

	/** Broker local IP port configuration property, integer value (for connections from this node). */
	public static final String MQTT_LOCAL_PORT = "mqtt.ip.port.local";

	/** Default value for the broker local IP port, integer value (for connections from this node). */
	public static final String MQTT_LOCAL_PORT_DEFAULT = "1884";

	/** The MQTT client ID prefix configuration property. */
	public static final String MQTT_CLIENT_ID_PREFIX = "mqtt.clientId";

	/** The MQTT quality of service setting configuration property. */
	public static final String MQTT_QOS = "mqtt.qos";

	/** Configuration property indicating the number of times to try to re-establish a connection to a broker. */
	public static final String MQTT_CONNECT_RETRIES = "mqtt.connectRetries";

	/** Configuration property indicating the interval between trying to re-establish a broker connection. */
	public static final String MQTT_CONNECT_RETRIES_INTERVAL = "mqtt.connectRetries.interval";

	/** The MQTT clean start setting configuration property. */
	public static final String MQTT_CLEAN_START = "mqtt.cleanStart";

	/** The MQTT retain publication setting configuration property. */
	public static final String MQTT_RETAIN = "mqtt.retainPublication";

	/*
	 * Default node properties
	 */

	/** Node type configuration property. */
	public static final String NODE_TYPE = "node.type";

	/** Node affiliation configuration property. */
	public static final String NODE_AFFILIATION = "node.affiliation";

	/** Node description configuration property. */
	public static final String NODE_DESCRIPTION = "node.description";

	/** Node port configuration property. */
	public static final String NODE_PORT = "node.port";

	/*
	 * Auto discovery properties
	 */

	/** Configuration property to enable the broadcast of discovery messages. */
	public static final String AUTO_DISCOVERY_REQUEST = "autodiscovery.request";

	/** Default value for the flag to enable broadcast of discovery messages. */
	public static final String AUTO_DISCOVERY_REQUEST_DEFAULT = "enabled";

	/** Configuration property to enable the receipt of discovery messages. */
	public static final String AUTO_DISCOVERY_LISTEN = "autodiscovery.listen";

	/** Default value for the flag to enable receipt of discovery messages. */
	public static final String AUTO_DISCOVERY_LISTEN_DEFAULT = "disabled";

	/** Configuration property indicating the interval (milliseconds) between discovery broadcasts. */
	public static final String AUTO_DISCOVERY_FREQUENCY = "autodiscovery.frequency";

	/** Default interval between discovery broadcasts. */
	public static final String AUTO_DISCOVERY_FREQUENCY_DEFAULT = "30000";

	/** Configuration property indicating the port for auto discovery messages. */
	public static final String AUTO_DISCOVERY_PORT = "autodiscovery.port";

	/** Default port for auto discovery messages. */
	public static final String AUTO_DISCOVERY_PORT_DEFAULT = "61883";

	/** Configuration property indicating the group for multicast discovery messages. */
	public static final String AUTO_DISCOVERY_GROUP = "autodiscovery.group";

	/** Default group for multicast discovery messages. */
	public static final String AUTO_DISCOVERY_GROUP_DEFAULT = "225.0.18.83";

	/** Configuration property indicating the TTL for auto discovery multicast requests. */
	public static final String AUTO_DISCOVERY_TTL = "autodiscovery.ttl";

	/** Default value for TTL for auto discovery multicast requests. */
	public static final String AUTO_DISCOVERY_TTL_DEFAULT = "1";

	/**
	 * Configuration property indicating whether to process all autodiscovery messages, true means yes, false means only
	 * process those autodiscovery meessages from the interface's subnet.
	 */
	public static final String AUTO_DISCOVERY_ACCEPT_ALL = "autodiscovery.accept.all";

	/** Default value for Accepting all autodiscovery messages */
	public static final String AUTO_DISCOVERY_ACCEPT_ALL_DEFAULT = "false";

	/** Configuration property indicating the maximum auto discovery messages to handle at any point in time. */
	public static final String AUTO_DISCOVERY_QUEUE_DEPTH = "autodiscovery.queue.depth";

	/** Default value for the maximum auto discovery messages to handle at any point in time. */
	public static final String AUTO_DISCOVERY_QUEUE_DEPTH_DEFAULT = "200";

	/** Configuration Property indicating time after which a node if not seen becomes unavailable. */
	public static final String AUTO_DISCOVERY_TIMEOUT = "autodiscovery.timeout";

	/** Default value for the time(milliseconds) after which a node if not seen becomes unavailable. */
	public static final String AUTO_DISCOVERY_TIMEOUT_DEFAULT = "65000";

	/**
	 * Configuration property for interval (in milliseconds) at which the sweeper checks for node neighbours that are no
	 * longer visible
	 */
	public static final String AUTO_DISCOVERY_SWEEPER_INTERVAL = "autodiscovery.sweeper.interval";

	/**
	 * Default Value for Interval (in millis) at which the sweeper checks for node neighbours that are no longer visible
	 */
	public static final String AUTO_DISCOVERY_SWEEPER_INTERVAL_DEFAULT = "5510";

	/*
	 * Class methods
	 */

	/**
	 * Instantiation not permitted.
	 */
	private ConfigProperties() {

	}
}
