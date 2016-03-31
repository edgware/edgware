/*
 * (C) Copyright IBM Corp. 2008, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.Fabric;
import fabric.LocalConfig;
import fabric.core.properties.ConfigProperties;
import fabric.core.properties.Properties;
import fabric.session.RegistryDescriptor;

/**
 * Instantiates a class providing an interface, local or remote, to the Fabric Registry.
 */
public class RegistryFactory extends Fabric {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2008, 2012";

	/*
	 * Class static fields
	 */

	/** The descriptor for the active connection to the Fabric Registry. */
	private RegistryDescriptor descriptor = null;

	private static RegistryFactory instance = null;

	private static Object instanceLock = new Object();

	/*
	 * Class static methods
	 */

	/**
	 * Connects to the Registry using information from the Fabric configuration properties.
	 * 
	 * @throws Exception
	 *             thrown if there is a problem connecting to the Registry; see the exception detail for more
	 *             information.
	 */
	public static void factory() throws Exception {

		factory(null, null);

	}

	/**
	 * Connects to the Registry using information in the supplied Registry descriptor. If no descriptor is provided,
	 * then the connection is instantiated using information from the Fabric configuration properties.
	 * 
	 * @throws Exception
	 *             thrown if there is a problem connecting to the Registry; see the exception detail for more
	 *             information.
	 */
	public static void factory(RegistryDescriptor descriptor, LocalConfig localConfig) throws Exception {

		synchronized (instanceLock) {

			if (instance == null) {

				instance = new RegistryFactory();

				if (descriptor == null) {
					descriptor = instance.createDescriptor();
				}

				instance.init(descriptor, localConfig);
			}
		}
	}

	// /**
	// * Answers the flag indicating if a single centralized (<code>true</code>) or a distributed Gaian (
	// * <code>false</code>) Registry is to be used.
	// *
	// * @return the flag.
	// */
	// public static boolean isSingleton() {
	//
	// boolean isSingleton = true;
	//
	// synchronized (instanceLock) {
	//
	// if (instance != null) {
	// isSingleton = (instance.descriptor.type().equals(RegistryDescriptor.TYPE_SINGLETON)) ? true : false;
	// }
	// }
	//
	// return isSingleton;
	// }

	/*
	 * Class methods
	 */

	/**
	 * Cannot instantiate.
	 */
	private RegistryFactory() {

		super(Logger.getLogger("fabric.registry"));
	}

	/**
	 * Creates a Registry descriptor using information from the Fabric configuration properties.
	 * 
	 * @throws Exception
	 *             thrown if there is a problem connecting to the Registry; see the exception detail for more
	 *             information.
	 */
	private RegistryDescriptor createDescriptor() throws Exception {

		/* Get the Registry type from the configuration properties */
		String registryType = config(ConfigProperties.REGISTRY_TYPE, RegistryDescriptor.DEFAULT_TYPE);

		/* Get the Registry connection protocol name from the configuration properties */
		String registryProtocol = config(ConfigProperties.REGISTRY_PROTOCOL, RegistryDescriptor.DEFAULT_PROTOCOL);

		/* Get the Registry connection address from the configuration properties */
		String registryURI = config(ConfigProperties.REGISTRY_ADDRESS, RegistryDescriptor.DEFAULT_URI);

		/* Get the Registry reconnection flag from the configuration properties */
		boolean registryReconnect = Boolean.parseBoolean(config(ConfigProperties.REGISTRY_RECONNECT,
				RegistryDescriptor.DEFAULT_RECONNECT));

		RegistryDescriptor descriptor = null;

		try {

			descriptor = new RegistryDescriptor(registryType, registryProtocol, registryURI, registryReconnect);

		} catch (IllegalArgumentException e) {

			String message = format("Invalid Registry Descriptor: %s", new Object[] {e.getMessage()});
			logger.log(Level.SEVERE, message);
			throw new UnsupportedOperationException(message);

		}

		return descriptor;

	}

	/**
	 * Initializes a connection to the Registry using information in the specified Registry descriptor.
	 * 
	 * @param descriptor
	 *            the Registry descriptor.
	 * 
	 * @param localConfig
	 *            local configuration information.
	 * 
	 * @throws Exception
	 *             thrown if there is a problem connecting to the Registry; see the exception detail for more
	 *             information.
	 */
	private void init(RegistryDescriptor descriptor, LocalConfig localConfig) throws Exception {

		this.descriptor = descriptor;

		/*
		 * Determine if we are to use a direct ("jdbc") or indirect ("proxy") connection to the Registry, and make the
		 * connection
		 */

		/* If a direct JDBC connection is to be used... */
		if (descriptor.protocol().equals(RegistryDescriptor.PROTOCOL_JDBC)) {

			/* Connect to the registry */
			Properties config = (localConfig != null) ? localConfig : config();
			FabricRegistry.connectJDBC(descriptor.uri(), config);

			// }
			// /* Else if an indirect connection via a proxy node is to be used... */
			// else if (descriptor.protocol().equals(RegistryDescriptor.PROTOCOL_PROXY)) {
			//
			// /* NOT CURRENTLY SUPPORTED */
			//
			// String message = format("Proxy access to the Registry is not currenty supported: ",
			// new Object[] {descriptor.uri()});
			// logger.log(Level.SEVERE, message);
			// throw new UnsupportedOperationException(message);
			//
		} else {

			String message = format("Unsupported Registry protocol specified: %s", new Object[] {descriptor.protocol()});
			logger.log(Level.SEVERE, message);
			throw new UnsupportedOperationException(message);

		}
	}
}
