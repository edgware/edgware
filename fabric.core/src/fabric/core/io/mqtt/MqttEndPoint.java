/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2007, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.io.mqtt;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import fabric.core.io.Channel;
import fabric.core.io.Config;
import fabric.core.io.EndPoint;
import fabric.core.io.InputTopic;
import fabric.core.io.Message;
import fabric.core.io.OutputTopic;
import fabric.core.logging.LogUtil;
import fabric.core.properties.Properties;
import fabric.core.util.Split;

/**
 * Class representing an I/O end point, i.e. a logical connection to a remote node, implemented using MQTT or MQTT-S as
 * the protocol.
 */
public class MqttEndPoint extends EndPoint implements MqttCallback {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2014";

	/*
	 * Class constants
	 */

	/*
	 * Class fields
	 */

	/** MQTT client connection. */
	private MqttClient mqttClient = null;

	/** The socket for MQTT-S messages. */
	private DatagramSocket datagramSocket = null;

	/** The address for MQTT-S messages. */
	private InetAddress datagramAddress = null;

	/** The set of channels open against this end point. */
	private final HashMap<String, MqttChannel> channels = new HashMap<String, MqttChannel>();

	/** The set of subscribed topics (one per channel). */
	private final ArrayList<String> topics = new ArrayList<String>();

	/** The configuration settings for this end point. */
	private MqttConfig config = null;

	/** The class logger. */
	protected Logger logger;

	/*
	 * Class methods
	 */

	/**
	 * Construct a new instance.
	 */
	public MqttEndPoint() {

		this(Logger.getLogger("fabric.core.io.mqtt"));
	}

	/**
	 * Construct a new instance.
	 * 
	 * @param logger
	 *            the instance's logger.
	 */
	public MqttEndPoint(Logger logger) {

		this.logger = logger;
	}

	/**
	 * @see fabric.core.io.EndPoint#configFactory(fabric.core.io.Config)
	 */
	@Override
	public Config configFactory(Config source) {

		return new MqttConfig((MqttConfig) source);
	}

	/**
	 * @see fabric.core.io.EndPoint#configFactory(fabric.core.properties.Properties)
	 */
	@Override
	public Config configFactory(Properties configProperties) {

		return new MqttConfig(configProperties);
	}

	/**
	 * Connects to the end point.
	 * 
	 * @param address
	 *            the IP name/address of the end point (target broker).
	 * 
	 * @param config
	 *            <code>MqttConfig</code> configuration information object.
	 * 
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	@Override
	public void connect(Object address, Object config) throws IOException, UnsupportedOperationException {

		/* If we haven't already initialized... */
		if (mqttClient == null) {

			/* Record the Fabric configuration information */
			this.config = new MqttConfig((MqttConfig) config);

			/* Initialise MQTT */
			mqttInit();

			/* Connect to the local broker */
			mqttConnect();

			/* Prepare to send MQTT-S messages */
			mqttsConnect();

			/* If a callback is registered... */
			if (callback != null) {
				try {
					callback.endPointConnected(this);
				} catch (Exception e) {
					logger.log(Level.WARNING, "Exception in callback {0}.endPointConnected(): {1}", new Object[] {
							classID(callback), LogUtil.stackTrace(e)});
				}
			}

		} else {

			logger.log(Level.FINE, "Already connected");
			throw new IOException("Already connected");

		}
	}

	/**
	 * Terminates the connection to the end point.
	 * 
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	@Override
	public void close() throws IOException, UnsupportedOperationException {

		try {
			/* Close each of the channels that are currently open */
			logger.finer("Unsubscribing.");
			MqttChannel[] channelList = channelList();
			for (int c = 0; c < channelList.length; c++) {
				channelList[c].close();
			}
		} catch (Exception e) {
			logger.log(Level.FINER, "Unsubscribe failed: ", e);
		}

		try {
			/* If set, publish the disconnect message */
			if (config.getConnectionMessageTopic() != null && config.getDisconnectMessage() != null) {
				logger.finer("Publishing disconnect message");
				mqttClient.publish(config.getConnectionMessageTopic(), config.getDisconnectMessage().getBytes(), config
						.getMqttQos(), true);
			}
		} catch (Exception e) {
			logger.log(Level.FINER, "Cannot publish disconnect message: ", e);
		}

		try {
			/* Disconnect from the broker */
			mqttClient.disconnect();
		} catch (Exception e) {
			logger.log(Level.FINER, "Cannot disconnect from the broker: ", e);
		}

		try {
			/* If a callback is registered... */
			if (callback != null) {
				callback.endPointClosed(this);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Exception in callback {0}.endPointClosed(): {1}", new Object[] {
					classID(callback), LogUtil.stackTrace(e)});
		}
	}

	/**
	 * Opens a one or two-way channel to the end point (target broker).
	 * 
	 * @param inputTopic
	 *            the name of the inbound topic.
	 * 
	 * @param outputTopic
	 *            the name of the outbound topic.
	 * 
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	@Override
	public Channel channel(InputTopic inputTopic, OutputTopic outputTopic) throws IOException,
			UnsupportedOperationException {

		MqttChannel mqttChannel = new MqttChannel(this, inputTopic, outputTopic);

		/* Make a note of the new channel (if there is one) */
		synchronized (channels) {
			if (inputTopic != null) {
				channels.put(inputTopic.name(), mqttChannel);
			}
		}

		/* Make a note of the new topic */
		synchronized (topics) {
			if (inputTopic != null) {
				topics.add(inputTopic.name());
			}
		}

		/* Now we can listen for incoming messages */
		mqttChannel.open();

		return mqttChannel;
	}

	/**
	 * @see org.eclipse.paho.client.mqttv3.MqttCallback#connectionLost(java.lang.Throwable)
	 */
	@Override
	public void connectionLost(Throwable t) {
		logger.log(Level.INFO, "MQTT connection to \"{0}\" as \"{1}\" lost. Retrying {2} times", new Object[] {config.getbrokerIpAddress(),
				config.getClient(), config.getConnectRetries()});
		/* If a callback is registered... */
		if (callback != null) {
			try {
				callback.endPointDisconnected(this);
			} catch (Exception e) {
				logger.log(Level.WARNING, "Exception in callback {0}.endPointDisconnected(): {1}", new Object[] {
						classID(callback), LogUtil.stackTrace(e)});
			}
		}

		logger.log(Level.FINE, "MQTT connection lost, re-trying...");

		try {

			mqttConnect();

			/* Reconnect each of the current channels */

			logger.log(Level.FINER, "Re-subscribing to channel topics...");

			MqttChannel[] channelList = channelList();

			for (int c = 0; c < channelList.length; c++) {
				channelList[c].subscribe();
			}

			logger.log(Level.FINE, "Reconnected and resubscribed");

			/* If a callback is registered... */
			if (callback != null) {
				try {
					callback.endPointReconnected(this);
				} catch (Exception e) {
					logger.log(Level.WARNING, "Exception in callback {0}.endPointReconnected(): {1}", new Object[] {
							classID(callback), LogUtil.stackTrace(e)});
				}
			}

		} catch (IOException e1) {

			logger.log(Level.FINEST, "Cannot reconnect to the broker: ");
			callback.endPointLost(this);

		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.paho.client.mqttv3.MqttCallback#messageArrived(java.lang.String,
	 * org.eclipse.paho.client.mqttv3.MqttMessage)
	 */
	@Override
	public void messageArrived(String topic, MqttMessage msg) throws Exception {

		byte[] messageBytes = msg.getPayload();
		int qos = msg.getQos();
		boolean retain = msg.isRetained();

		logger.log(Level.FINEST, "Message received by end point \"{0}\" on topic \"{1}\":\n{2}", new Object[] {this,
				topic, new String(messageBytes)});

		boolean messageHandled = true;

		/* Build the message */
		Message message = new Message();
		message.topic = topic;
		byte[] data = new byte[messageBytes.length];
		System.arraycopy(messageBytes, 0, data, 0, messageBytes.length);
		message.data = data;

		/* Pass the message to the channel */

		synchronized (channels) {

			/* Get the channel for this topic */
			String[] subscribedTopics = matchTopicToSubscriptions(topic);

			/* If there is one... */
			if (subscribedTopics.length > 0) {

				/* For each topic... */
				for (int t = 0; t < subscribedTopics.length; t++) {

					MqttChannel channel = channels.get(subscribedTopics[t]);

					/* Pass the message */
					messageHandled = channel.messageArrived(message);

				}

			} else {

				/* We can't handle the message, so drop it */
				logger.log(Level.WARNING, "No channel open for topic \"{0}\", dropping message:\n{1}", new Object[] {
						topic, message.toString()});
				messageHandled = false;

			}
		}

		logger.log(Level.FINEST, "Message handled (status: \"{0}\")", Boolean.toString(messageHandled));

	}

	/**
	 * Clean-up when a channel is closed.
	 * 
	 * @param channel
	 *            the channel being closed.
	 * 
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	protected void dispose(MqttChannel channel) throws UnsupportedOperationException {

		/* Get the name of the inbound topic */
		String inboundTopic = channel.inputTopic().name();

		/* Clean up references this topic */
		synchronized (topics) {
			topics.remove(inboundTopic);
		}

		/* Clean up references to the channel */
		synchronized (channels) {
			channels.remove(inboundTopic);
		}
	}

	/**
	 * Gets the configuration settings for this end point.
	 * 
	 * @return the configuration settings.
	 */
	@Override
	public Object getConfig() {

		return config;

	}

	/**
	 * Gets the MQTT client connection for this end point.
	 * 
	 * @return the client connection.
	 */
	public MqttClient getMqttClient() {

		return mqttClient;

	}

	/**
	 * Gets the datagam socket created for MQTT-S messages.
	 * 
	 * @return the socket.
	 */
	public DatagramSocket getDatagramSocket() {

		return datagramSocket;

	}

	/**
	 * Answers the address for MQTT-S messages.
	 * 
	 * @return the address.
	 */
	public InetAddress getDatagramAddress() {

		return datagramAddress;

	}

	/**
	 * Creates a MQTT client object.
	 * 
	 * @throws IOException
	 *             If there was a problem creating the MQTT client.
	 */
	private void mqttInit() throws IOException {

		try {

			mqttClient = new MqttClient("tcp://" + config.getIPHost() + ":" + config.getIPPort(), config.getClient(),
					null);
			mqttClient.setCallback(this);

		} catch (MqttException e) {

			throw new IOException(e.getMessage());

		}

	}

	/**
	 * Connects to the configured broker.
	 */
	private void mqttConnect() throws IOException {

		boolean connected = false;
		int maxRetries = config.getConnectRetries();
		int retryInterval = config.getConnectionRetryInterval();
		int retries = 0;

		while (!connected) {

			try {

				logger.log(Level.FINE, "Connecting to \"{0}\" as \"{1}\"", new Object[] {config.getbrokerIpAddress(),
						config.getClient()});
				logger.log(Level.FINER, "with ConnectionMessageTopic \"{0}\" with LWT payload \"{1}\"", new Object[] {config.getConnectionMessageTopic(),
						config.getDisconnectMessage()});

				/* If there is not last will and testament... */
				if (config.getConnectionMessageTopic() == null) {

					MqttConnectOptions connOpts = new MqttConnectOptions();
					connOpts.setCleanSession(config.isCleanStart());
					connOpts.setKeepAliveInterval(60);
					mqttClient.connect(connOpts);

				} else {

					MqttConnectOptions connOpts = new MqttConnectOptions();
					connOpts.setCleanSession(config.isCleanStart());
					connOpts.setKeepAliveInterval(60);
					connOpts.setWill(mqttClient.getTopic(config.getConnectionMessageTopic()), config
							.getDisconnectMessage().getBytes(), config.getMqttQos(), false);
					mqttClient.connect(connOpts);

					/* If there is a connection message... */
					if (config.getConnectMessage() != null) {

						/* Publish it */
						byte[] payload = config.getConnectMessage().getBytes();
						mqttClient.getTopic(config.getConnectionMessageTopic()).publish(payload, config.getMqttQos(),
								false);

					}
				}

				connected = true;

				logger.log(Level.INFO, "Connected to \"{0}\" as \"{1}\"", new Object[] {config.getbrokerIpAddress(),
						config.getClient()});

			} catch (Exception e) {

				logger.log(Level.FINER, "MQTT connection failed (retry {0} of {1}): {2}", new Object[] {retries,
						maxRetries, e.getMessage()});

				/* If retries are not unlimited... */
				if (maxRetries != -1) {

					/* If we have reached the maximum number of retries... */
					if (++retries > maxRetries) {

						logger.log(Level.INFO, "MQTT connection retries exhausted, will not reconnect to \"{0}\" as \"{1}\"", new Object[] {config.getbrokerIpAddress(),
						config.getClient()});
						
						throw new IOException(e);

					} else {
						/* Wait before retrying */
						try {
							Thread.sleep(retryInterval);
						} catch (InterruptedException e1) {
						}
					}
				}
			}
		}
	}

	/**
	 * Creates a socket to connect to send MQTT-S messages.
	 */
	private void mqttsConnect() throws IOException {

		boolean connected = false;
		int maxRetries = config.getConnectRetries();
		int retries = 0;

		while (config.isMqttsEnabled() && !connected) {

			try {

				/* Establish the UDP connection */
				datagramSocket = new DatagramSocket();
				datagramAddress = InetAddress.getByName(config.getIPHost());
				connected = true;

			} catch (Exception e) {

				logger.log(Level.FINER, "MQTT-S connection failed (retry {0} of {1}): {2}", new Object[] {retries,
						maxRetries, e.getMessage()});

				/* If retries are not unlimited... */
				if (maxRetries != -1) {

					/* If we have reached the maximum number of retries... */
					if (++retries > maxRetries) {

						throw new IOException(e);

					}
				}
			}
		}
	}

	/**
	 * Checks the specified topic for a match in the list subscribed to by the channels of this end point, taking
	 * account of wildcards used in the channel topic names.
	 * 
	 * @param topic
	 *            the topic to check.
	 * 
	 * @return the list of names of the matching channel topics.
	 */
	private String[] matchTopicToSubscriptions(String topic) {

		ArrayList<String> matchingTopics = new ArrayList<String>();

		synchronized (topics) {

			/* Get the list of active topics */
			Iterator<String> topicIterator = topics.iterator();

			String[] topicParts = Split.divide(topic, "/");
			int t = 0;

			/* For each registered topic... */
			for (t = 0; topicIterator.hasNext(); t++) {

				boolean isMatch = false;
				boolean complete = false;
				int p = 0;

				String nextTopic = topicIterator.next();
				String[] nextTopicParts = Split.divide(nextTopic, "/");

				for (p = 0; p < nextTopicParts.length && !complete; p++) {

					if (nextTopicParts[p].equals("#")) {

						/* Match found */
						complete = true;
						isMatch = true;

					} else if (nextTopicParts[p].equals("+")) {

						/* Still matches, skip this level */
						isMatch = true;

					} else if (p >= topicParts.length) {

						/* No match */
						complete = true;
						isMatch = false;

					} else if (nextTopicParts[p].equals(topicParts[p])) {

						/* Still matches */
						isMatch = true;

					} else {

						/* No match */
						complete = true;
						isMatch = false;
					}
				}

				/* If we ran out of "next topic" parts without completing the match... */
				if (p < topicParts.length && !complete) {
					isMatch = false;
				}

				/* If a match was found... */
				if (isMatch) {

					/* Save it */
					matchingTopics.add(nextTopic);

				}
			}
		}

		/* Convert the list of matching topics to an array */
		String[] matchingTopicNames = new String[matchingTopics.size()];
		matchingTopicNames = matchingTopics.toArray(matchingTopicNames);

		return matchingTopicNames;
	}

	/**
	 * Gets the list of active channels.
	 * 
	 * @return the list of channels.
	 */
	private MqttChannel[] channelList() {

		MqttChannel[] channelList = null;

		synchronized (channels) {

			Set<Entry<String, MqttChannel>> entrySet = channels.entrySet();
			Iterator<Entry<String, MqttChannel>> channelIterator = entrySet.iterator();
			channelList = new MqttChannel[entrySet.size()];

			for (int c = 0; channelIterator.hasNext(); c++) {
				channelList[c] = channelIterator.next().getValue();
			}

		}

		return channelList;
	}

	/**
	 * @see org.eclipse.paho.client.mqttv3.MqttCallback#deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken)
	 */
	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {

	}

	/**
	 * Generates a unique ID string for a class instance (in case <code>toString()</code> has been overridden).
	 * 
	 * @return the ID string.
	 */
	private String classID(final Object object) {

		return object.getClass().getName() + '@' + object.hashCode();

	}
}
