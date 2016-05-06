/*
 * (C) Copyright IBM Corp. 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.core.properties.Properties;
import fabric.registry.FabricRegistry;
import fabric.registry.QueryScope;
import fabric.registry.exception.PersistenceException;

/**
 * Provides access to Fabric configuration settings.
 * <p>
 * Sources of Fabric configuration information are checked in the following order:
 * <ol>
 * <li>System properties.</li>
 * <li>A specified local properties file.</li>
 * <li>The <em>default</em> configuration settings table in the Fabric Registry.</li>
 * </ol>
 * All configuration settings are name/value pairs, with optional insert values, following the syntax of Java resource
 * bundles.
 * </p>
 */
public class LocalConfig extends Properties implements IConfig {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

    /** To hold a local cache of properties loaded from an external source (file or the Fabric Registry). */
    private HashMap<String, String> cachedProperties = new HashMap<String, String>();

    /** Flag indicating local or remote (distributed) queries */
    protected QueryScope queryScope = QueryScope.LOCAL;

    /*
     * Class methods
     */

    /**
     * Constructs a new instance, initialized from the specified properties file.
     *
     * @param configFile
     *            Fabric properties file name/path.
     */
    public LocalConfig(String configFile) {

        super(configFile, Logger.getLogger("fabric.config"));

    }

    /**
     * Constructs a new instance, initialized from the specified properties file.
     *
     * @param configFile
     *            Fabric properties file name/path.
     *
     * @param logger
     *            the class logger.
     */
    public LocalConfig(String configFile, Logger logger) {

        super(configFile, logger);

    }

    /**
     * Constructs a new instance, initialized from an existing properties object.
     *
     * @param source
     *            The source properties.
     */
    public LocalConfig(Properties source) {

        this(source, Logger.getLogger("fabric"));
    }

    /**
     * Constructs a new instance, initialized from an existing properties object.
     *
     * @param source
     *            The source properties.
     *
     * @param logger
     *            the class logger.
     */
    public LocalConfig(Properties source, Logger logger) {

        super(source, logger);

        if (source instanceof LocalConfig) {
            this.cachedProperties = (HashMap<String, String>) ((LocalConfig) source).cachedProperties.clone();
        } else {
            cachedProperties = new HashMap<String, String>();
        }
    }

    /**
     * @see fabric.IConfig#getLocalProperty(java.lang.String)
     */
    @Override
    public String getLocalProperty(String key) {

        return super.getProperty(key);

    }

    /**
     * @see fabric.IConfig#getLocalProperty(java.lang.String, java.lang.String)
     */
    @Override
    public String getLocalProperty(String key, String propertyDefault) {

        return super.getProperty(key, propertyDefault);

    }

    /**
     * @see fabric.IConfig#getLocalProperty(java.lang.String, java.lang.Object)
     */
    @Override
    public String getLocalProperty(String key, Object... inserts) {

        return super.getProperty(key, inserts);

    }

    /**
     * @see fabric.IConfig#getLocalProperty(java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public String getLocalProperty(String key, String propertyDefault, Object... inserts) {

        return super.getProperty(key, propertyDefault, inserts);

    }

    /**
     * @see fabric.IConfig#getProperty(java.lang.String)
     */
    @Override
    public String getProperty(String key) {

        return getProperty(key, null, (Object[]) null);

    }

    /**
     * @see fabric.IConfig#getProperty(java.lang.String, java.lang.String)
     */
    @Override
    public String getProperty(String key, String propertyDefault) {

        return getProperty(key, propertyDefault, (Object[]) null);

    }

    /**
     * @see fabric.IConfig#getProperty(java.lang.String, java.lang.Object)
     */
    @Override
    public String getProperty(String key, Object... inserts) {

        return getProperty(key, null, inserts);

    }

    /**
     * @see fabric.IConfig#getProperty(java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public String getProperty(String key, String propertyDefault, Object... inserts) {

        String propertyValue = null;

        /* If the property value is in the local cache... */
        if (cachedProperties.containsKey(key)) {

            propertyValue = cachedProperties.get(key);
            logger.log(Level.FINEST, "Property lookup: using cache value [{0}] = [{1}]", new Object[] {key,
                    propertyValue, propertyDefault});

        } else {

            logger.log(Level.FINEST, "Looking up key [{0}] (default value is [{1}])", new Object[] {key,
                    propertyDefault});

            /* Try and get the property value from a local source */
            propertyValue = getLocalProperty(key);

            /* If we didn't find it... */
            if (propertyValue == null) {

                /* Check the registry */
                propertyValue = lookupRegistryProperty(key);

                /* If we still haven't found a value... */
                if (propertyValue == null) {

                    /* Use the default */
                    logger.log(Level.FINER, "Property lookup: using default value [{0}] = [{1}]", new Object[] {key,
                            propertyDefault});
                    propertyValue = propertyDefault;

                } else {

                    logger.log(Level.FINER, "Property lookup: using Registry value [{0}] = [{1}]", new Object[] {key,
                            propertyValue});

                }

            } else {

                logger.log(Level.FINER, "Property lookup: using properties file value [{0}] = [{1}]", new Object[] {
                        key, propertyValue});

            }

            /* If we have a value from any source... */
            if (propertyValue != null) {

                /* Cache it for later */
                cachedProperties.put(key, propertyValue);

            }
        }

        /* If we've got a configuration property value and a set of inserts... */
        if (propertyValue != null && inserts != null && inserts.length > 0) {

            /* Merge the inserts */
            propertyValue = mergeInserts(propertyValue, inserts);

        }

        return propertyValue;
    }

    /**
     * Answers the value of the specified configuration property from the default look-up table in the Fabric Registry.
     *
     * @param key
     *            the name of the configuration property.
     *
     * @return the configuration property value, or <code>null</code> if it has not been set.
     */
    protected String lookupRegistryProperty(String key) {

        String propertyValue = null;

        try {

            if (FabricRegistry.isConnected()) {

                /* Query the default configuration properties table in the Registry */
                propertyValue = FabricRegistry.runStringQuery("select VALUE from " + FabricRegistry.DEFAULT_CONFIG
                        + " where NAME='" + key + "'", queryScope);
                logger.log(Level.FINEST, "Default configuration lookup for key [{0}] returned [{1}]", new Object[] {
                        key, propertyValue});

            }

        } catch (PersistenceException e) {

            logger.log(Level.WARNING, "Exception looking up configuration property [{0}]: {1}", new Object[] {key,
                    e.getMessage()});
            logger.log(Level.FINEST, "Full exception: ", e);

        }

        return propertyValue;
    }

}
