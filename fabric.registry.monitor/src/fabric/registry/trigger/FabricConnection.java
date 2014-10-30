/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.trigger;

import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import fabric.Fabric;
import fabric.core.io.mqtt.MqttConfig;

/**
 * Class that manages the Fabric connection used to send Registry update notification messages.
 */
class FabricConnection implements Runnable, MqttCallback {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2014";

	/*
	 * Class constants
	 */

	/** The Fabric descriptor for the update data feed. */
	private final static String REGISTRY_UPDATE_SERVICE = "$fabric/$registry/$registry_updates";

	/*
	 * Class fields
	 */

	/** Connection to the local Fabric broker. */
	private MqttClient mqttClient = null;

	/** The notification topic on the broker. */
	private String onrampTopic = null;

	/** The name of the Fabric home node. */
	private String homeNode = null;

	/** Thread synchronisation object. */
	final Object isConnectedLock = new Object();

	/** Flag indicating if the connection thread has been fully initialised. */
	private boolean isConnected = false;

	/** Accessor for Fabric configuration */
	private Fabric fabric = null;

	/** The retry interval for connection attempts to the broker. */
	private int retryInterval = 30;

	/*
	 * Class methods
	 */

	/**
	 * Constructor.
	 */
	public FabricConnection() {

		/* Get configuration values for Fabric triggers */

		fabric = new Fabric();
		fabric.initFabricConfig();

		homeNode = fabric.config("fabric.node");
		onrampTopic = MessageFormat
				.format(fabric.config("fabric.feeds.onramp", "$fabric/{0}/$feeds/$onramp"), homeNode)
				+ '/' + REGISTRY_UPDATE_SERVICE;
		System.out.println("Sending update messages to Fabric node " + homeNode + " on topic " + onrampTopic);

		String configRetryInterval = fabric.config("registry.broker.retryInterval", "30");
		try {
			retryInterval = Integer.parseInt(configRetryInterval);
		} catch (NumberFormatException e2) {
			/* Use the default value */
			System.out
					.println("Invalid value for configuration setting registry.broker.retryInterval (must be an integer value): "
							+ configRetryInterval);
		}
		System.out.println("Re-try interval for the broker connection: " + retryInterval + " seconds");
	}

	/**
	 * Starts the thread managing the Fabric connection.
	 */
	public void start() {

		/* Start the thread */
		Thread fabricThread = new Thread(this);
		fabricThread.start();

		/*
		 * Wait for the connection to complete; the number of retries is limited as this is blocking the Registry from
		 * running
		 */
		for (int maxRetries = 0; !isConnected() && maxRetries < 10; maxRetries++) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		try {

			/* Initialise MQTT */
			mqttInit();

			/* Connect to the local broker */
			mqttConnect(null, null, null);

		} catch (Exception e) {
			System.out.println("Failed to connect to the Fabric:\n" + e.getMessage());
		}

		synchronized (isConnectedLock) {
			isConnected = true;
		}
	}

	/**
	 * Creates a MQTT client object.
	 * 
	 * @throws IOException
	 *             If there was a problem creating the MQTT client.
	 */
	private void mqttInit() throws IOException {

		try {

			mqttClient = new MqttClient("tcp://" + "localhost:1884", MqttConfig.generateClient("REG_"));
			mqttClient.setCallback(this);

		} catch (MqttException e) {

			throw new IOException(e.getMessage());

		}

	}

	/**
	 * Connects to the configured broker.
	 * 
	 * @param lwtTopic
	 *            the last will and testament topic, or <code>null</code> if none.
	 * 
	 * @param connectMessage
	 *            the connection message, or <code>null</code> if none.
	 * 
	 * @param disconnectMessage
	 *            the disconnection (last will and testament) message, or <code>null</code> if none.
	 */
	private void mqttConnect(String lwtTopic, String connectMessage, String disconnectMessage) throws IOException {

		boolean connected = false;

		while (!connected) {

			try {

				System.out.println(String.format("Connecting to broker at address %s as %s", mqttClient.getServerURI(),
						mqttClient.getClientId()));

				if (lwtTopic == null || disconnectMessage == null) {

					MqttConnectOptions connOpts = new MqttConnectOptions();
					connOpts.setCleanSession(true);
					connOpts.setKeepAliveInterval(60);
					mqttClient.connect(connOpts);

				} else {

					MqttConnectOptions connOpts = new MqttConnectOptions();
					connOpts.setCleanSession(true);
					connOpts.setKeepAliveInterval(60);
					connOpts.setWill(lwtTopic, disconnectMessage.getBytes(), 2, false);
					mqttClient.connect(connOpts);

					/* If there is a connection message... */
					if (connectMessage != null) {

						/* Publish it */
						byte[] payload = connectMessage.getBytes();
						mqttClient.getTopic(lwtTopic).publish(payload, 2, false);

					}
				}

				connected = true;
				System.out.println("Connected to broker");

			} catch (Exception e) {

				System.out.println("MQTT connection failed; retrying in " + retryInterval + " seconds");

				try {
					Thread.sleep(retryInterval * 1000);
				} catch (Exception e1) {
				}

			}
		}
	}

	/**
	 * Answers <code>true</code> if the connection to the Fabric is active, <code>false</code> otherwise.
	 * 
	 * @return the connection status.
	 */
	public boolean isConnected() {

		boolean isConnected = false;

		synchronized (isConnectedLock) {
			isConnected = this.isConnected;
		}

		return isConnected;
	}

	/**
	 * Send the specified service message.
	 * 
	 * @param message
	 *            the message to send.
	 * 
	 * @throws Exception
	 */
	public void sendRegistryUpdate(String update) throws Exception {

		try {

			if (isConnected()) {

				/* Send the message to the target node */
				mqttClient.publish(onrampTopic, update.getBytes(), 2, false);

			} else {

				System.out.println("Not currently connected to the Fabric, trigger will not be distributed: " + update);

			}

		} catch (Exception e) {

			String message = "Fabric trigger not sent: cannot contact local broker";
			System.out.println(message);
			// throw new Exception(message, e);

		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.paho.client.mqttv3.MqttCallback#connectionLost(java.lang.Throwable)
	 */
	@Override
	public void connectionLost(Throwable arg0) {

		synchronized (isConnectedLock) {
			isConnected = false;
		}

		System.out.println("Connection to the broker lost, re-trying...");

		try {
			mqttConnect(null, null, null);
		} catch (IOException e) {
			System.out.println("Cannot reconnect to the broker:\n" + e);
		}

		synchronized (isConnectedLock) {
			isConnected = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.paho.client.mqttv3.MqttCallback#deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken)
	 */
	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.paho.client.mqttv3.MqttCallback#messageArrived(java.lang.String,
	 * org.eclipse.paho.client.mqttv3.MqttMessage)
	 */
	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {

	}
}
