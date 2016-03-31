/*
 * (C) Copyright IBM Corp. 2007, 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.io.mqtt;

import fabric.core.io.Config;
import fabric.core.properties.ConfigProperties;
import fabric.core.properties.Properties;
import fabric.core.util.RandomID;

/**
 * MQTT/MQTT-S I/O configuration information.
 */
public class MqttConfig extends Config {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2014";

    /*
     * Class constants
     */

    /** The maximum length of the prefix for a generated client ID */
    public static final int MAX_CLIENT_ID_PREFIX = 10;

    /** MQTT QOS setting 0 */
    public static final byte MQTT_QOS_0 = 0;

    /** MQTT QOS setting 1 */
    public static final byte MQTT_QOS_1 = 1;

    /** MQTT QOS setting 2 */
    public static final byte MQTT_QOS_2 = 2;

    /*
     * Class fields
     */

    /** The IP port of the broker (default 1883). */
    private int brokerIpPort = 1883;

    /** Message sent when connection is lost (<em>disconnect</em> message); default "<code>0</code>". */
    private String disconnectMessage = "0";

    /** Message sent when connection is established (<em>connect</em> message); default "<code>1</code>". */
    private String connectMessage = "1";

    /** The name of the broker topic for connection messages (no default). */
    private String connectionMessageTopic = null;

    /** The client ID (no default). */
    private String client = null;

    /** The MQTT QoS setting (defaults to <code>2</code>). */
    private byte mqttQos = MQTT_QOS_2;

    /** The "clean start" flag (defaults to <code>true</code>). */
    private boolean cleanStart = true;

    /** The "retain publication" flag (defaults to <code>false</code>). */
    private boolean retain = false;

    /** The number of connection retries before failing. */
    private int connectRetries = 3;

    /** The interval between connection retries (milliseconds). */
    private int connectRetriesInterval = 1000;

    /** Indicates if the MQTT-S is enabled. */
    private boolean mqttsEnabled = false;

    /**
     * The maximum size of a message (in bytes) that can be sent as <code>MessageQoS.BEST_EFFORT</code> (currently the
     * space available in a single UDP packet).
     */
    private int maxMqttsPayload = 500;

    /*
     * Class methods
     */

    /**
     * Constructs a new instance which is a deep copy of the specified instance.
     */
    public MqttConfig(MqttConfig source) {

        super(source);

        /* Client settings */
        this.client = source.client;

        /* Broker settings */
        this.connectionMessageTopic = source.connectionMessageTopic;
        this.disconnectMessage = source.disconnectMessage;
        this.connectMessage = source.connectMessage;
        this.brokerIpPort = source.brokerIpPort;
        this.mqttQos = source.mqttQos;
        this.cleanStart = source.cleanStart;
        this.retain = source.retain;
        this.connectRetries = source.connectRetries;
        this.connectRetriesInterval = source.connectRetriesInterval;

        /* QoS settings */
        this.mqttsEnabled = source.mqttsEnabled;
        this.maxMqttsPayload = source.maxMqttsPayload;

    }

    /**
     * Constructs a new instance from the specified configuration properties.
     * 
     * @param config
     *            the configuration properties.
     */
    public MqttConfig(Properties config) {

        super(config);

        /* Client settings */
        client = MqttConfig.generateClient(config.getProperty("mqtt.clientId", generateClient("")));

        /* Broker settings */
        brokerIpPort = Integer.parseInt(config.getProperty(ConfigProperties.BROKER_REMOTE_PORT, Integer
                .toString(brokerIpPort)));
        mqttQos = decodeMqttQoS(config.getProperty(ConfigProperties.MQTT_QOS, Byte.toString(mqttQos)));
        cleanStart = Boolean.parseBoolean(config.getProperty(ConfigProperties.MQTT_CLEAN_START, Boolean
                .toString(cleanStart)));
        retain = Boolean.parseBoolean(config.getProperty(ConfigProperties.MQTT_RETAIN, Boolean.toString(retain)));
        connectRetries = Integer.parseInt(config.getProperty(ConfigProperties.MQTT_CONNECT_RETRIES, Integer
                .toString(connectRetries)));
        connectRetriesInterval = Integer.parseInt(config.getProperty(ConfigProperties.MQTT_CONNECT_RETRIES_INTERVAL,
                Integer.toString(connectRetriesInterval)));

        /* QoS settings */
        maxMqttsPayload = Integer.parseInt(config.getProperty("mqtts.maxPayload", "500"));
        mqttsEnabled = Boolean.parseBoolean(config.getProperty("mqtts.enabled", "false"));

    }

    /**
     * Gets the IP port of the broker.
     * 
     * @return the IP port of the broker.
     */
    public int getIPPort() {

        return brokerIpPort;
    }

    /**
     * Sets the IP port of the broker.
     * 
     * @param brokerIpPort
     *            the IP port of the broker.
     */
    public void setBrokerIpPort(int brokerIpPort) {

        this.brokerIpPort = brokerIpPort;
    }

    /**
     * Answers the MQTT quality of service (QoS) byte value corresponding to one of the following string
     * representations:
     * <ul>
     * <li>QOS_0 or 0</li>
     * <li>QOS_1 or 1</li>
     * <li>QOS_2 or 2</li>
     * </ul>
     * 
     * @param qosString
     *            the string representation of the QoS value.
     * 
     * @return the QoS value.
     */
    public static byte decodeMqttQoS(String qosString) {

        byte qos = 2;

        if (qosString.equals("QOS_0") || qosString.equals("0")) {

            qos = new Byte((byte) 0);

        } else if (qosString.equals("QOS_1") || qosString.equals("1")) {

            qos = new Byte((byte) 1);

        } else if (qosString.equals("QOS_2") || qosString.equals("2")) {

            qos = new Byte((byte) 2);
        }

        return qos;

    }

    /**
     * Generates a random client ID.
     * 
     * @param prefix
     *            the caller-supplied prefix for the client ID.
     * 
     * @return the random client ID, prefixed with the caller supplied prefix.
     */
    public static String generateClient(String prefix) {

        return RandomID.generate(prefix, MAX_CLIENT_ID_PREFIX);

    }

    /**
     * Gets the flag indicating if the MQTT-S is enabled.
     * 
     * @return <code>true</code> if MQTT-S is enabled, <code>false</code> otherwise.
     */
    public boolean isMqttsEnabled() {

        return mqttsEnabled;
    }

    /**
     * Gets the maximum size of a message (in bytes) that can be sent as <code>MessageQoS.BEST_EFFORT</code> (currently
     * the space available in a single UDP packet).
     * 
     * @return the maximum size in bytes.
     */
    public int getMaxMqttsPayload() {

        return maxMqttsPayload;
    }

    /**
     * Gets the "clean start" flag (defaults to <code>true</code>).
     * 
     * @return the current flag value or the default.
     */
    public boolean isCleanStart() {

        return cleanStart;
    }

    /**
     * Sets the "clean start" flag (defaults to <code>true</code>).
     * 
     * @param cleanStart
     *            the new value.
     */
    public void setCleanStart(boolean cleanStart) {

        this.cleanStart = cleanStart;
    }

    /**
     * Gets the client ID (no default).
     * 
     * @return the client ID or <code>null</code> if it has not been set.
     */
    public String getClient() {

        return client;

    }

    /**
     * Sets the client ID (defaults to a generated UUID).
     * 
     * @param client
     *            the client ID to set.
     */
    public void setClient(String client) {

        this.client = client;
    }

    /**
     * The "retain publication" flag (defaults to <code>false</code>).
     * 
     * @return the current flag value or the default.
     */
    public boolean isRetain() {

        return retain;
    }

    /**
     * Sets the "retain publication" flag (defaults to <code>false</code>).
     * 
     * @param retain
     *            the new value.
     */
    public void setRetain(boolean retainPublication) {

        this.retain = retainPublication;
    }

    /**
     * Gets the IP address of the broker (no default).
     * 
     * @return the address.
     */
    public String getbrokerIpAddress() {

        String brokerIpAddress = getIPHost();

        if (brokerIpPort >= 0) {
            brokerIpAddress += ":" + brokerIpPort;
        }

        return brokerIpAddress;
    }

    /**
     * Gets the QoS setting (defaults to <code>2</code>).
     * 
     * @return the QoS setting.
     */
    public byte getMqttQos() {

        return mqttQos;
    }

    /**
     * Sets the QoS setting (defaults to <code>2</code>).
     * 
     * @param mqttQos
     *            The QoS setting.
     */
    public void setMqttQos(byte qos) {

        this.mqttQos = qos;
    }

    /**
     * Gets the number of connection retries before failing (default 0).
     * 
     * @return the number of retries.
     */
    public int getConnectRetries() {

        return connectRetries;
    }

    /**
     * Sets the number of connection retries before failing (default 0).
     * 
     * @param retries
     *            the number of retries.
     */
    public void setConnectRetries(int retries) {

        this.connectRetries = retries;
    }

    /**
     * Gets the message sent when connection is lost (<em>disconnect</em> message).
     * 
     * @return the disconnection message.
     */
    public String getDisconnectMessage() {

        return disconnectMessage;
    }

    /**
     * Sets the message sent when connection is lost (<em>disconnect</em> message).
     * 
     * @param disconnectMessage
     *            the disconnection message.
     */
    public void setDisconnectMessage(String disconnectMessage) {

        this.disconnectMessage = disconnectMessage;
    }

    /**
     * Gets the message sent when connection is established (<em>connect</em> message).
     * 
     * @return the connection message.
     */
    public String getConnectMessage() {

        return connectMessage;
    }

    /**
     * Sets the message sent when connection is established (<em>connect</em> message).
     * 
     * @param reconnectMessage
     *            the connection message.
     */
    public void setConnectMessage(String reconnectMessage) {

        this.connectMessage = reconnectMessage;
    }

    /**
     * Gets the name of the connection message topic (no default).
     * 
     * @return The name of the last will and testament topic.
     */
    public String getConnectionMessageTopic() {

        return connectionMessageTopic;
    }

    /**
     * Sets the name of the connection message topic (no default).
     * 
     * @param connectionMessageTopic
     *            The name of the topic.
     */
    public void setConnectionMessageTopic(String connectionMessageTopic) {

        this.connectionMessageTopic = connectionMessageTopic;
    }

    /**
     * Gets the interval (milliseconds) between connection retries.
     * 
     * @return the retry interval.
     */
    public int getConnectionRetryInterval() {

        return connectRetriesInterval;
    }

    /**
     * Sets the interval (milliseconds) between connection retries before failing.
     * 
     * @param retries
     *            the number of retries.
     */
    public void setConnectRetriesInterval(int interval) {

        this.connectRetriesInterval = interval;
    }
}
