/*
 * (C) Copyright IBM Corp. 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric;

import java.util.logging.Level;

import fabric.core.properties.ConfigProperties;
import fabric.core.properties.Properties;
import fabric.registry.FabricRegistry;
import fabric.registry.exception.PersistenceException;
import fabric.registry.persistence.PersistenceManager;

/**
 * Provides access to node configuration settings.
 * <p>
 * If a node setting is not available for a specific key then the Fabric default will be used.
 * </p>
 */
public class NodeConfig extends LocalConfig {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

    /*
     * Class fields
     */

    /** To hold the ID of the node for which configuration information is required. */
    private final String node;

    /*
     * Class methods
     */

    /**
     * Constructs a new instance, initialized from the specified properties file.
     *
     * @param node
     *            the ID of the node for which configuration information is required.
     *
     * @param configFile
     *            Fabric properties file name/path.
     *
     * @throws IllegalStateException
     *             thrown if no connection to the Registry has been made.
     */
    public NodeConfig(String node, String configFile) {

        super(configFile);

        this.node = node;

        /* If Registry-based configuration is not available... */
        if (!PersistenceManager.isConnected()) {

            throw new IllegalStateException("Registry connection required.");

        }
    }

    /**
     * Constructs a new instance, initialized from the specified properties file.
     *
     * @param node
     *            the ID of the node for which configuration information is required.
     *
     * @param configFile
     *            Fabric properties file name/path.
     *
     * @throws IllegalStateException
     *             thrown if no connection to the Registry has been made.
     */
    public NodeConfig(Properties source) {

        super(source);
        this.node = getProperty(ConfigProperties.NODE_NAME, "default");

        /* If Registry-based configuration is not available... */
        if (!PersistenceManager.isConnected()) {

            throw new IllegalStateException("Registry connection required.");

        }
    }

    /**
     * Answers the value of the specified node configuration property from the following configuration tables in the
     * Fabric Registry (in order):
     * <ol>
     * <li>Node</li>
     * <li>Default</li>
     * </ol>
     *
     * @param key
     *            the name of the configuration property.
     *
     * @return the configuration property value, or <code>null</code> if it has not been set.
     */
    @Override
    protected String lookupRegistryProperty(String key) {

        String propertyValue = null;

        try {

            /* Query the node configuration properties table in the Registry */
            propertyValue = FabricRegistry.runStringQuery("select VALUE from " + FabricRegistry.NODE_CONFIG
                    + " where NODE_ID='" + node + "' and NAME='" + key + "'", queryScope);
            logger.log(Level.FINEST, "Registry lookup for key [{0}] returned [{1}]", new Object[] {key, propertyValue});

            /* If we didn't find a value... */
            if (propertyValue == null) {

                /* Query the default configuration properties table in the Registry */
                propertyValue = super.lookupRegistryProperty(key);

            }

        } catch (PersistenceException e) {

            logger.log(Level.WARNING, "Exception looking up configuration property [{0}]: {1}", new Object[] {key,
                    e.getMessage()});
            logger.log(Level.FINEST, "Full exception: ", e);

        }

        return propertyValue;
    }

    /**
     * Answers the node for which configuration information is being provided.
     *
     * @return the node ID.
     */
    public String node() {

        return node;

    }
}
