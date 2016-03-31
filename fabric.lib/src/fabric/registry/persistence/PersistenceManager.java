/*
 * (C) Copyright IBM Corp. 2009, 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.persistence;

import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.core.properties.ConfigProperties;
import fabric.core.properties.Properties;
import fabric.registry.exception.PersistenceException;
import fabric.session.RegistryDescriptor;

/**
 * Controls access to the persistence service in use - either local (using a JDBC connection) or remote (using a Fabric
 * connection).
 *
 */
public class PersistenceManager {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2014";

    private final static String CLASS_NAME = PersistenceManager.class.getName();
    private final static String PACKAGE_NAME = PersistenceManager.class.getPackage().getName();

    private final static Logger logger = Logger.getLogger(PACKAGE_NAME);

    private static final String SINGLETON_JDBC_INSTANCE_CLASS = "fabric.registry.persistence.impl.SingletonJDBCPersistence";
    private static final String GAIAN_INSTANCE_CLASS = "fabric.registry.persistence.impl.GaianPersistence";
    private static final String DISTRIBUTED_JDBC_INSTANCE_CLASS = "fabric.registry.persistence.distributed.DistributedJDBCPersistence";
    private static Class concreteClass = null;

    private static Persistence instance = null;

    /* monitor to prevent more than one thread from initialising the Fabric connection */
    private static Object monitor = new Object();

    /** are we connected to the registry? */
    private static boolean connectedToRegistry = false;

    /**
     * Connects to a local Fabric Registry database node using JDBC. Only one connection is established per Fabric JVM.
     * 
     * @param dbUrl
     * @throws PersistenceException
     */
    public static void connect(String dbUrl, Properties config) throws PersistenceException {

        synchronized (monitor) {
            if (!connectedToRegistry) {
                try {
                    if (concreteClass == null) {
                        switch (config.getProperty(ConfigProperties.REGISTRY_TYPE, RegistryDescriptor.TYPE_GAIAN)) {
                            case RegistryDescriptor.TYPE_DISTRIBUTED:
                                concreteClass = Class.forName(DISTRIBUTED_JDBC_INSTANCE_CLASS);
                                break;
                            case RegistryDescriptor.TYPE_GAIAN:
                                concreteClass = Class.forName(GAIAN_INSTANCE_CLASS);
                                break;
                            case RegistryDescriptor.TYPE_SINGLETON:
                                concreteClass = Class.forName(SINGLETON_JDBC_INSTANCE_CLASS);
                                break;
                            default:
                                logger.log(Level.SEVERE, "FAILED TO establish a REGISTRY of TYPE "
                                        + config.getProperty(ConfigProperties.REGISTRY_TYPE));
                                break;
                        }
                    }

                    instance = (Persistence) concreteClass.newInstance();
                } catch (InstantiationException e) {
                    logger.log(Level.SEVERE, "FAILED TO INSTANTIATE PERSISTENCE");
                    throw new PersistenceException("FAILED TO INSTANTIATE PERSISTENCE", e);
                } catch (ClassNotFoundException e) {
                    logger.log(Level.SEVERE, "FAILED TO INSTANTIATE PERSISTENCE");
                    throw new PersistenceException("FAILED TO INSTANTIATE PERSISTENCE", e);
                } catch (IllegalAccessException e) {
                    logger.log(Level.SEVERE, "FAILED TO INSTANTIATE PERSISTENCE");
                    throw new PersistenceException("FAILED TO INSTANTIATE PERSISTENCE", e);
                }
                instance.init(dbUrl, config);
                instance.connect();
                connectedToRegistry = true;
            }
        }
    }

    /**
     * Closes the connection to the Fabric Registry database.
     * 
     * @throws PersistenceException
     */
    public static void disconnect() throws PersistenceException {
        if (instance != null) {
            instance.disconnect();
        }
    }

    /**
     * Returns the persistence service currently in use.
     * 
     * @return
     */
    public static Persistence getPersistence() {
        return instance;
    }

    /**
     * Returns a flag indicating whether the Registry connection is active or not.
     * 
     * @return
     */
    public static boolean isConnected() {
        return connectedToRegistry;
    }
}
