/*
 * (C) Copyright IBM Corp. 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric;

import java.util.logging.Level;

import fabric.registry.FabricRegistry;
import fabric.registry.exception.PersistenceException;

/**
 * Provides access to platform configuration settings from the following configuration tables in the Fabric Registry (in
 * order):
 * <ol>
 * <li>Platform</li>
 * <li>Node</li>
 * <li>Default</li>
 * </ol>
 */
public class PlatformConfig extends NodeConfig {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

    /*
     * Class fields
     */

    /** To hold the ID of the platform for which configuration information is required. */
    private final String platform;

    /*
     * Class methods
     */

    /**
     * Constructs a new instance, initialized from the specified properties file.
     *
     * @param platform
     *            the ID of the platform for which configuration information is required.
     *
     * @param node
     *            the ID of the node for which configuration information is required.
     *
     * @param configFile
     *            Fabric properties file name/path.
     */
    public PlatformConfig(String platform, String node, String configFile) {

        super(node, configFile);
        this.platform = platform;

    }

    /**
     * Answers the platform for which configuration information is being provided.
     *
     * @return the platform ID.
     */
    public String platform() {

        return platform;

    }

    /**
     * Answers the value of the specified platform configuration property from the following configuration tables in the
     * Fabric Registry (in order):
     * <ol>
     * <li>Platform</li>
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

            /* Query the platform configuration properties table in the Registry */
            propertyValue = FabricRegistry.runStringQuery("select VALUE from " + FabricRegistry.PLATFORM_CONFIG
                    + " where PLATFORM_ID='" + platform + "' and NAME='" + key + "'", queryScope);
            logger.log(Level.FINEST, "Registry lookup for key [{0}] returned [{1}]", new Object[] {key, propertyValue});

            /* If we didn't find a value... */
            if (propertyValue == null) {

                /* Query the node and default configuration properties table in the Registry */
                propertyValue = super.lookupRegistryProperty(key);

            }

        } catch (PersistenceException e) {

            logger.log(Level.WARNING, "Exception looking up configuration property [{0}]: {1}", new Object[] {key,
                    e.getMessage()});
            logger.log(Level.FINEST, "Full exception: ", e);

        }

        return propertyValue;
    }

}
