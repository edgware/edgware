/*
 * (C) Copyright IBM Corp. 2006, 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import fabric.bus.services.IPluginRegistry;
import fabric.core.logging.FabricFormatter;
import fabric.core.properties.ConfigProperties;
import fabric.core.properties.Properties;
import fabric.core.util.Formatter;
import fabric.registry.RegistryFactory;
import fabric.registry.persistence.PersistenceManager;

/**
 * Base Fabric class containing common utility methods.
 */
public class Fabric {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2006, 2014";

    /*
     * Class constants
     */

    /** The family name for built-in Fabric services */
    public static final String FABRIC_PLUGIN_FAMILY = "fab";

    /*
     * Class static fields
     */

    /** The configuration information for this instance */
    private static Properties config = null;

    /** The name of the local Fabric (broker) node */
    private static String homeNode = null;

    /** Flag indicating if instrumentation is required */
    private static boolean doInstrument = false;

    /** Manager for the metrics recorded at run-time */
    private static MetricsManager metricsManager = null;

    /** Build version number for the fabric */
    private String buildVersion = null;

    /** The OSGi plugin registry */
    private static IPluginRegistry pluginRegistry = null;

    /** Table mapping short class names to their full paths (used to expand short names used in Fabric messages) */
    private static HashMap<String, String> shortNames = new HashMap<String, String>();

    /** Table mapping full class names to their short names (used to compact names used in Fabric messages) */
    private static HashMap<String, String> longNames = new HashMap<String, String>();

    /**
     * Static configuration
     */

    static {

        /* Populate the class short-name/long-name and long-name/short-name maps */

        longNames.put("ClientNotificationMessage", "fabric.bus.messages.impl.ClientNotificationMessage");
        shortNames.put("fabric.bus.messages.impl.ClientNotificationMessage", "ClientNotificationMessage");

        longNames.put("ClientNotificationService", "fabric.client.services.ClientNotificationService");
        shortNames.put("fabric.client.services.ClientNotificationService", "ClientNotificationService");

        longNames.put("ConnectionManagerService", "fabric.bus.services.impl.ConnectionManagerService");
        shortNames.put("fabric.bus.services.impl.ConnectionManagerService", "ConnectionManagerService");

        longNames.put("ConnectionMessage", "fabric.bus.messages.impl.ConnectionMessage");
        shortNames.put("fabric.bus.messages.impl.ConnectionMessage", "ConnectionMessage");

        longNames.put("FeedManagerService", "fabric.bus.feeds.impl.FeedManagerService");
        shortNames.put("fabric.bus.feeds.impl.FeedManagerService", "FeedManagerService");

        longNames.put("FeedMessage", "fabric.bus.messages.impl.FeedMessage");
        shortNames.put("fabric.bus.messages.impl.FeedMessage", "FeedMessage");

        longNames.put("FloodRouting", "fabric.services.floodmessage.FloodRouting");
        shortNames.put("fabric.services.floodmessage.FloodRouting", "FloodRouting");

        longNames.put("NotificationManagerService", "fabric.bus.services.impl.NotificationManagerService");
        shortNames.put("fabric.bus.services.impl.NotificationManagerService", "NotificationManagerService");

        longNames.put("NotificationMessage", "fabric.bus.messages.impl.NotificationMessage");
        shortNames.put("fabric.bus.messages.impl.NotificationMessage", "NotificationMessage");

        longNames.put("ProxyPublisherService", "fabric.services.proxypublisher.ProxyPublisherService");
        shortNames.put("fabric.services.proxypublisher.ProxyPublisherService", "ProxyPublisherService");

        longNames.put("ServiceMessage", "fabric.bus.messages.impl.ServiceMessage");
        shortNames.put("fabric.bus.messages.impl.ServiceMessage", "ServiceMessage");

        longNames.put("StaticRouting", "fabric.bus.routing.impl.StaticRouting");
        shortNames.put("fabric.bus.routing.impl.StaticRouting", "StaticRouting");

        longNames.put("SubscriptionMessage", "fabric.bus.feeds.impl.SubscriptionMessage");
        shortNames.put("fabric.bus.feeds.impl.SubscriptionMessage", "SubscriptionMessage");

    }

    /*
     * Class methods
     */

    protected Logger logger;

    public Fabric() {

        this(Logger.getLogger("fabric"));
    }

    public Fabric(Logger logger) {

        this.logger = logger;
    }

    /**
     * Loads the core configuration information for this Fabric application, including the location of the home Fabric
     * node and the Fabric Registry.
     * <p>
     * The name of the configuration file to be used can be set in the system property <code>fabric.config</code>. If
     * this property has not been set then the default file
     * <code>$FABRIC_HOME/osgi/configuration/fabricConfig_default.properties</code> is used.
     * </p>
     * <p>
     * <em><strong>Note:</strong> this method of Registry initialization is being replaced as part of the new Fabric
     * programming model (see <code>fabric.session.FabricSession</code>).</em>
     * </p>
     */
    public synchronized void initFabricConfig() {

        if (Fabric.config == null) {

            String configFile = null;

            /* Lookup the name of the configuration settings file in the environment variable */
            configFile = System.getenv("FABRIC_CONFIG");

            /* If the bundle name has not been set... */
            if (configFile == null) {

                /* Lookup the name of the configuration settings file in the system properties */
                configFile = System.getProperty("fabric.config");

                /* If the bundle name has not been set... */
                if (configFile == null) {

                    configFile = System.getenv("FABRIC_HOME") + "/osgi/configuration/fabricConfig_default.properties";

                }
            }

            /* Load the settings */
            Fabric.config = new LocalConfig(configFile);
        }

    }

    /**
     * Initialize access to the Fabric Registry for this instance.
     * <p>
     * <em><strong>Note:</strong> this method of Registry initialization is being replaced as part of the new Fabric
     * programming model (see <code>fabric.session.FabricSession</code>).</em>
     * </p>
     *
     * @throws Exception
     *             thrown if there is a problem initializing the Registry.
     */
    public void initRegistry() throws Exception {

        RegistryFactory.factory();

    }

    /**
     * Sets the Fabric configuration settings.
     * <p>
     * <em><strong>Note:</strong> this method of Registry initialization is being replaced as part of the new Fabric
     * programming model (see <code>fabric.session.FabricSession</code>).</em>
     * </p>
     *
     * @param config
     *            the configuration settings.
     */
    public static void setConfig(IConfig config) {

        Fabric.config = (Properties) config;

    }

    /*
     * Class methods
     */

    /**
     * Sets the Fabric configuration file used by this VM.
     * <p>
     * The name of the configuration file to be used can be set in the system property <code>fabric.config</code>. If
     * this property has not been set then the default file
     * $FABRIC_HOME/osgi/configuration/fabricConfig_default.properties is used.
     * </p>
     */
    public synchronized void initNodeConfig() throws Exception {

        Fabric.config = new NodeConfig(Fabric.config);

        /* Setup any Node specific configuration for Persistence */
        PersistenceManager.getPersistence().initNodeConfig(Fabric.config);

    }

    /**
     * Initializes logging settings for Fabric components.
     *
     * @param loggerName
     *            the name of the logger for this session.
     *
     * @param traceTitle
     *            the title string associated with trace messages from this session.
     */
    public void initLogging(String loggerName, String traceTitle) {

        /* Create and configure an instance of the Fabric logging formatter */
        boolean longMessages = Boolean.parseBoolean(config("logging.messages.long", "false"));
        FabricFormatter formatter = new FabricFormatter(longMessages);
        formatter.setTitle(traceTitle);

        /* Get the full list of handlers */
        Logger logger = Logger.getLogger("");
        Handler[] handlers = logger.getHandlers();

        /* For each handler... */
        for (int h = 0; h < handlers.length; h++) {

            if (handlers[h] instanceof FileHandler) {
                handlers[h].setFormatter(formatter);
            } else if (handlers[h] instanceof ConsoleHandler) {
                handlers[h].setFormatter(formatter);
            } else {
                handlers[h].setFormatter(formatter);
            }
        }

        /* Lower the logging level for the Paho MQTT client */
        // Logger mqttLogger = Logger.getLogger("org.eclipse.paho.client.mqttv3");
        // Level mqttLevel = Level.parse(config("org.eclipse.paho.client.mqttv3.level", "INFO"));
        // mqttLogger.setLevel(mqttLevel);
    }

    /**
     * Answers the value of the specified Fabric configuration setting.
     *
     * @param key
     *            the name of the property.
     *
     * @return the property value, or <code>null</code> if it has not been set.
     */
    public String config(String key) {

        /* Return the configuration setting */
        return config(key, null, (Object[]) null);

    }

    /**
     * Answers the value of the specified Fabric configuration setting.
     *
     * @param key
     *            the name of the property.
     *
     * @param propertyDefault
     *            the default value for this property.
     *
     * @return the property value, or the default if it has not been set.
     */
    public String config(String key, String propertyDefault) {

        /* Return the configuration setting */
        return config.getProperty(key, propertyDefault, (Object[]) null);

    }

    /**
     * Answers the value of the specified Fabric configuration setting.
     *
     * @param key
     *            the name of the property.
     *
     * @param propertyDefault
     *            the default value for this property.
     *
     * @param inserts
     *            property insert values.
     *
     * @return the property value, or the default if it has not been set.
     */
    public String config(String key, String propertyDefault, Object... inserts) {

        /* Return the configuration setting */
        return config.getProperty(key, propertyDefault, inserts);

    }

    /**
     * Answers the full set of Fabric configuration settings.
     *
     * @return the configuration settings.
     */
    public Properties config() {

        return config;

    }

    /**
     * Initialize instrumentation for this Fabric VM.
     *
     * @throws IOException
     *             thrown if there is a problem opening the instrumentation persistence file.
     */
    public void initInstrumentation() throws IOException {

        doInstrument = Boolean.parseBoolean(config("instrumentation.enable", "false"));
        String fileName = config("instrumentation.fileName", null, homeNode());
        int metricsBufferSize = Integer.parseInt(config("instrumentation.buffer", "100"));

        metrics().initManager(doInstrument, fileName, metricsBufferSize);

    }

    /**
     * Answers the metrics manager for this Fabric VM.
     *
     * @return the metrics manager.
     */
    public MetricsManager metrics() {

        if (metricsManager == null) {
            metricsManager = new MetricsManager();
        }
        return metricsManager;
    }

    /**
     * Answers the instrumentation status (enabled or disabled).
     *
     * @return <code>true</code> if instrumentation is enabled for this Fabric VM, <code>false</code> otherwise.
     */
    public static boolean doInstrument() {

        return doInstrument;
    }

    /**
     * Sets the instrumentation status (enabled or disabled).
     *
     * @param doInstrument
     *            <code>true</code> if instrumentation is to be enabled for this Fabric VM, <code>false</code>
     *            otherwise.
     */
    public void setInstrument(boolean doInstrument) {

        Fabric.doInstrument = doInstrument;
    }

    /**
     * Sets the OSGi plugin registry.
     *
     * @param reg
     *            the registry.
     */
    public static void setPluginRegistry(IPluginRegistry reg) {

        Fabric.pluginRegistry = reg;
    }

    /**
     * Instantiates the named class using the configured class loader.
     *
     * @param className
     *            the name of the class to instantiate.
     *
     * @return the new instance.
     *
     * @throws ClassNotFoundException
     *             thrown if the class cannot be found.
     *
     * @throws InstantiationException
     *             thrown if the class cannot be instantiated.
     *
     * @throws IllegalAccessException
     *             thrown if the class cannot be instantiated (for example, if there is no default constructor).
     */
    public static Object instantiate(String className) throws ClassNotFoundException, IllegalAccessException,
        InstantiationException {

        Object instantiatedClass = null;

        if (Fabric.pluginRegistry != null) {
            /* Running inside OSGi, so defer instantiation to the plugin registry */
            instantiatedClass = Fabric.pluginRegistry.getPluginInstance(className);
        }

        if (instantiatedClass == null) {
            Class<?> loadedClass = Fabric.class.getClassLoader().loadClass(className);
            instantiatedClass = loadedClass.newInstance();
        }

        return instantiatedClass;
    }

    /**
     * Lookup a sub-map in a map of maps, creating it if required.
     *
     * @param key
     *            the key of the sub-map.
     *
     * @param map
     *            the map of maps.
     *
     * @return the sub-map.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public HashMap lookupSubmap(String key, HashMap map) {

        /* Get the sub-map from the map of maps */
        HashMap subMap = (HashMap) map.get(key);

        /* If it doesn't exist... */
        if (subMap == null) {

            /* Create it */
            subMap = new HashMap();
            map.put(key, subMap);

        }

        return subMap;

    }

    /**
     * Lookup a sub-list in a map of lists, creating it if required.
     *
     * @param key
     *            the key of the sub-list.
     *
     * @param map
     *            the map of lists.
     *
     * @return the sub-list.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ArrayList lookupSublist(Object key, HashMap map) {

        /* Get the sub-list from the map of lists */
        ArrayList subList = (ArrayList) map.get(key);

        /* If it doesn't exist... */
        if (subList == null) {

            /* Create it */
            subList = new ArrayList();
            map.put(key, subList);

        }

        return subList;

    }

    /**
     * Gets the name of the local Fabric (broker) node.
     *
     * @return the name of the local Fabric (broker) node.
     */
    public String homeNode() {

        if (homeNode == null) {

            homeNode = System.getenv("FABRIC_NODE");

            if (homeNode == null && config != null) {

                /* Read it from the configuration file */
                homeNode = config(ConfigProperties.NODE_NAME, "default");

            }
        }

        return homeNode;
    }

    public void setHomeNode(String node) {

        homeNode = node;
    }

    /**
     * Determines if a string is null or empty.
     *
     * @param string
     *            the string to test.
     *
     * @return <code>true</code> if the string is non-null and not empty, <code>false</code> otherwise.
     */
    public boolean isEmpty(String string) {

        boolean isEmpty = false;

        if (string != null && !string.equals("")) {
            isEmpty = true;
        }

        return isEmpty;
    }

    /**
     * Get the build version number of the fabric.
     *
     * The version number is read from the MANIFEST.MF which is updated by the build process.
     *
     * @return the version number.
     */
    public String getBuildVersion() {

        /* first time only */
        if (buildVersion == null) {
            try {
                /* locate the manifest for the fabric.lib jar */
                Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(
                        "META-INF/MANIFEST.MF");
                URL url = null;
                while (resources.hasMoreElements()) {
                    url = resources.nextElement();
                    if (url.toString().indexOf("fabric.lib") != -1) {
                        break; /* this is the one we want */
                    }
                }

                /* Read the manifest file */
                URLConnection urlConn = url.openConnection();
                InputStream stream = urlConn.getInputStream();

                if (stream == null) {
                    buildVersion = "Unable to get inputstream handle for MANIFEST.MF.";
                } else {
                    Manifest manifest = new Manifest(stream);

                    Attributes attributes = manifest.getMainAttributes();
                    /* Implementation-Version field contains the build number */
                    String impVersion = attributes.getValue("Implementation-Version");
                    if (impVersion != null) {
                        buildVersion = impVersion;
                    } else {
                        buildVersion = "Missing Implementation-Version attribute in MANIFEST.MF.";
                    }
                }

            } catch (IOException e) {
                buildVersion = "Error reading MANFEST.MF.";
            }
        }

        return buildVersion;
    }

    /**
     * Format a string with inserts.
     *
     * @param format
     *            format string.
     *
     * @param inserts
     *            insert values.
     *
     * @return the formatter string.
     */
    public static String format(String format, Object... inserts) {

        Formatter f = new Formatter();
        f.format(format, inserts);
        return f.toString();
    }

    /**
     * Answers the long name (package name plus class name) for a class.
     *
     * @return the long name for the class, or <code>null</code> if one has not been registered.
     */
    public static String longName(String shortName) {
        return longNames.get(shortName);
    }

    /**
     * Answers the short name (class name) for a full name (package name plus class name).
     *
     * @return the short name for the class, or <code>null</code> if one has not been registered.
     */
    public static String shortName(String longName) {
        return shortNames.get(longName);
    }
}
