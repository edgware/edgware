/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.session;

/**
 * Class describing the attributes of a connection to the Fabric Registry.
 */
public class RegistryDescriptor {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

	/*
	 * Class constants
	 */

	/* Registry types */

	/** Constant indicating a singleton (centralized) Fabric Registry. */
	public static final String TYPE_SINGLETON = "singleton";

	/** Constant indicating a distributed (Gaian) Fabric Registry. */
	public static final String TYPE_GAIAN = "gaian";

	/** Constant indicating a distributed Fabric Registry. */
	public static final String TYPE_DISTRIBUTED = "distributed";

	/* Registry protocols */

	/** Constant indicating that JDBC is the Fabric Registry access protocol. */
	public static final String PROTOCOL_JDBC = "jdbc";

	/** Constant indicating that the Fabric Registry proxy access protocol will be used. */
	public static final String PROTOCOL_PROXY = "proxy";

	/* Descriptor defaults */

	/** The default Registry type. */
	public static final String DEFAULT_TYPE = TYPE_SINGLETON;

	/** The default Registry protocol. */
	public static final String DEFAULT_PROTOCOL = PROTOCOL_JDBC;

	/** The default Registry URI. */
	public static final String DEFAULT_URI = "jdbc:derby://localhost:6414/FABRIC;user=fabric;password=fabric";

	/** The default Registry retry flag. */
	public static final String DEFAULT_RECONNECT = "true";

	/*
	 * Class fields
	 */

	/** The Registry type. */
	private String type = null;

	/** The Registry connection protocol. */
	private String protocol = null;

	/** The Registry URI. */
	private String uri = null;

	/** Reconnection flag. */
	private boolean reconnect = false;

	/*
	 * Static class initialization
	 */

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 * 
	 * @param type
	 *            the type of Fabric Registry, one of the values <code>TYPE_SINGLETON</code> or
	 *            <code>TYPE_DISTRIBUTED</code>.
	 * 
	 * @param protocol
	 *            the protocol used to communicate with the Registry, one of the values <code>PROTOCOL_JDBC</code> or
	 *            <code>PROTOCOL_PROXY</code>.
	 * 
	 * @param uri
	 *            a type/protocol-specific string that can be used to connect to the Registry.
	 * 
	 * @param reconnect
	 *            flag indicating whether the Fabric should attempt to reestablish a Registry connection in the event
	 *            the Registry is not available at runtime.
	 */
	public RegistryDescriptor(String type, String protocol, String uri, boolean reconnect) {

		if (TYPE_GAIAN.equals(type) || TYPE_SINGLETON.equals(type) || TYPE_DISTRIBUTED.equals(type)) {
			this.type = type;
		} else {
			throw new IllegalArgumentException("Unrecognized Registry type: " + type);
		}

		if (PROTOCOL_JDBC.equals(protocol) || PROTOCOL_PROXY.equals(protocol)) {
			this.protocol = protocol;
		} else {
			throw new IllegalArgumentException("Unrecognized Registry protocol: " + protocol);
		}

		this.uri = uri;
		this.reconnect = reconnect;

	}

	/**
	 * Answers the URI of the Fabric Registry.
	 * 
	 * @return a type/protocol-specific string that can be used to connect to the Registry.
	 */
	public String uri() {

		return uri;

	}

	/**
	 * Answers the protocol used to communicate with the Registry. This can indicate either a JDBC or Fabric messaging
	 * access protocol.
	 * 
	 * @return <code>PROTOCOL_JDBC</code> for a JDBC connection to the Registry or <code>PROTOCOL_PROXY</code> for a
	 *         Fabric proxy connection to the Registry.
	 */
	public String protocol() {

		return protocol;

	}

	/**
	 * Answers the flag controlling whether the Fabric should attempt to reconnect to the Registry if the connection is
	 * lost. This does not effect retries on startup which will always be attempted.
	 * 
	 * @return <code>true</code> if reconnect should be attempted, <code>false</code> otherwise.
	 */
	public boolean reconnect() {

		return reconnect;

	}

	/**
	 * Answers the type of Fabric Registry. This can indicate either a singleton (centralized) Registry or a distributed
	 * (Gaian) Registry.
	 * 
	 * @return <code>TYPE_SINGLETON</code> for a centralized Registry or <code>TYPE_DISTRIBUTED</code> for a distributed
	 *         Registry.
	 */
	public String type() {

		return type;

	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		String name = getClass().getName();
		String hashCode = Integer.toHexString(hashCode());
		String content = "type:" + type + ";protocol:" + protocol + ";reconnect:" + reconnect + ";uri:" + uri;
		return name + '@' + hashCode + '(' + content + ')';

	}
}
