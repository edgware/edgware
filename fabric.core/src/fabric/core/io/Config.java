/*
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.io;

import fabric.core.properties.Properties;

/**
 * Base class for I/O configuration objects.
 */
public abstract class Config {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/*
	 * Class fields
	 */

	/** The IP name/address of the node. */
	private String ipHost = "127.0.0.1";

	/** To hold the default QoS for sending messages between nodes. */
	private MessageQoS defaultMessageQos = MessageQoS.RELIABLE;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance which is a deep copy of the specified instance.
	 */
	public Config(Config source) {

		/* Node address */
		this.ipHost = source.ipHost;

		/* QoS settings */
		this.defaultMessageQos = source.defaultMessageQos;

	}

	/**
	 * Constructs a new instance from the specified configuration properties.
	 * 
	 * @param config
	 *            the configuration properties.
	 */
	public Config(Properties config) {

		/* QoS settings */
		String messageQos = config.getProperty("io.defaultQos", "reliable");
		defaultMessageQos = (messageQos.equals("best-effort")) ? MessageQoS.BEST_EFFORT : MessageQoS.RELIABLE;

	}

	/**
	 * Gets the IP name/address of the node.
	 * 
	 * @return the IP name of the node.
	 */
	public String getIPHost() {

		return ipHost;
	}

	/**
	 * Sets the IP name/address of the node.
	 * 
	 * @param ipHost
	 *            the IP name of the node.
	 */
	public void setIPHost(String ipHost) {

		this.ipHost = ipHost;
	}

	/**
	 * Gets the default message QoS.
	 * 
	 * @return the default message QoS.
	 */
	public MessageQoS getDefaultMessageQos() {

		return defaultMessageQos;
	}

	/**
	 * Sets the default message QoS.
	 * 
	 * @return the default message QoS.
	 */
	public void setDefaultMessageQos(MessageQoS defaultMessageQos) {

		this.defaultMessageQos = defaultMessageQos;
	}
}
