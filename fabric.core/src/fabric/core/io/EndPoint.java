/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2007, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.io;

import java.io.IOException;
import java.util.logging.Logger;

import fabric.core.io.mqtt.MqttEndPoint;
import fabric.core.properties.Properties;

/**
 * Class representing an I/O end point, i.e. a logical connection to a remote node.
 */
public abstract class EndPoint extends IOBase {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2014";

	/*
	 * Class fields
	 */

	/** The technology specific configuration associated with this instance. */
	protected Object config = null;

	/** The callback registered to receive notifications related to this endpoint. */
	protected IEndPointCallback callback = null;

	/** The class logger. */
	protected Logger logger;

	/*
	 * Class methods
	 */

	/**
	 * Construct a new instance.
	 */
	public EndPoint() {

		this(Logger.getLogger("fabric.core.io"));

	}

	/**
	 * Construct a new instance.
	 * 
	 * @param logger
	 *            the instance's logger.
	 */
	public EndPoint(Logger logger) {

		this.logger = logger;

	}

	/**
	 * Instantiates a technology-specific end point class matching the specification passed in the arguments.
	 * <p>
	 * <strong>Note:</strong> ultimately this should be table driven so that we can plug in new handlers.
	 * </p>
	 * <p>
	 * The currently supported domains are:
	 * <ul>
	 * <li><b><code>DOMAIN_PUBSUB</code></b></li>
	 * </ul>
	 * The currently supported protocols are:
	 * <ul>
	 * <li><b><code>PROTOCOL_MQTT</code></b></li>
	 * </ul>
	 * </p>
	 * 
	 * @param domain
	 *            determines the underlying technology to use for IO.
	 * 
	 * @param protocol
	 *            determines the underlying protocol.
	 * 
	 * @return the <code>EndPoint</code> object, ready to use.
	 * 
	 * @throws IOException
	 */
	public static EndPoint endPointFactory(int domain, int protocol) throws IOException {

		EndPoint endPoint = null;

		/* Decode the domain (to determine the underlying technology to be used to construct the fabric) */
		switch (domain) {

		case DOMAIN_PUBSUB:

			/* Decode the protocol and instantiate the correct EndPoint class */
			switch (protocol) {

			case PROTOCOL_MQTT:

				endPoint = new MqttEndPoint();
				break;

			default:

				throw new IOException("Invalid protocol: " + protocol);

			}

			break;

		default:

			throw new IOException("Invalid domain: " + domain);

		}

		return endPoint;
	}

	/**
	 * Connects to the end point.
	 * 
	 * @param arg1
	 *            technology-specific argument.
	 * @param config
	 *            technology-specific configuration argument.
	 * 
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	public abstract void connect(Object arg1, Object config) throws IOException, UnsupportedOperationException;

	/**
	 * Terminates the connection to the end point.
	 * 
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	public abstract void close() throws IOException, UnsupportedOperationException;

	/**
	 * Opens a one or two-way channel to the end point.
	 * 
	 * @param inputTopic
	 *            local input topic.
	 * 
	 * @param outputTopic
	 *            remote output topic.
	 * 
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	public abstract Channel channel(InputTopic inputTopic, OutputTopic outputTopic) throws IOException,
			UnsupportedOperationException;

	/**
	 * Gets the configuration associated with this instance.
	 * 
	 * @return the technology specific configuration object.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public abstract Object getConfig();

	/**
	 * Registers a callback to receive notifications related to the connectivity status of this endpoint.
	 * <p>
	 * <strong>Note:</strong> only one callback is supported at a time; any previously registered callback will be
	 * replaced.
	 * </p>
	 * 
	 * @param newCallback
	 *            the new callback or null if no callback is required.
	 * 
	 * @return the previous callback, or <code>null</code> if there was none.
	 */
	public IEndPointCallback register(IEndPointCallback newCallback) {

		IEndPointCallback oldCallback = callback;
		callback = newCallback;
		return oldCallback;

	}

	/**
	 * Constructs a new configuration object, which is a deep copy of the specified configuration.
	 * 
	 * @param source
	 *            the source configuration object.
	 * 
	 * @return a deep copy of the source configuration.
	 */
	public abstract Config configFactory(Config source);

	/**
	 * Constructs a new configuration object initialized from the specified configuration properties.
	 * 
	 * @param config
	 *            the configuration properties.
	 * 
	 * @return the new configuration object.
	 */
	public abstract Config configFactory(Properties configProperties);

}
