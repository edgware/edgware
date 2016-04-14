/*
 * (C) Copyright IBM Corp. 2007, 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.Fabric;
import fabric.bus.plugins.IFabletConfig;
import fabric.bus.plugins.IFabletHandler;
import fabric.bus.plugins.IFabletPlugin;

/**
 * Class representing the Fablet plug-ins.
 *
 */
public class FabletHandler extends PluginHandler implements IFabletHandler {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2012";

    /*
     * Class static fields
     */

    /** Count of active shutdown hooks. */
    private static int fabletCount = 1;

    /** Object used to lock access to the shutdown hook count. */
    private static Object fabletCountLock = new Object();

    /*
     * Class fields
     */

    private Logger logger;
    /** The configuration information for this plug-in */
    private IFabletConfig pluginConfig = null;

    /** To hold the plug-in instance */
    private IFabletPlugin fablet = null;

    /*
     * Class methods
     */

    /**
     * Constructs a new instance.
     *
     * @param pluginConfig
     *            the configuration information for this plug-in.
     */
    public FabletHandler(IFabletConfig pluginConfig) {

        super(pluginConfig);

        this.logger = Logger.getLogger("fabric.bus.plugins");
        /* Record the configuration for use later */
        this.pluginConfig = pluginConfig;

    }

    /**
     * @see fabric.bus.plugins.IPluginHandler#start()
     */
    @Override
    public void start() {

        String className = pluginConfig().getName();

        try {

            /* Instantiate the class */
            fablet = (IFabletPlugin) Fabric.instantiate(className);

        } catch (Throwable t) {

            logger.log(Level.WARNING, "Failed to instantiate fablet [{0}]: {1}", new Object[] {className,
                    t.getMessage()});
            logger.log(Level.FINEST, "Full exception: ", t);

        }

        if (fablet != null) {

            try {
                /* Invoke the initialization method */
                fablet.startPlugin(pluginConfig);

                /* If the plug-in runs on its own thread... */
                if (fablet instanceof Runnable) {

                    /* Start it now */
                    Thread pluginThread = new Thread(fablet);
                    synchronized (fabletCountLock) {
                        className = className.substring(className.lastIndexOf('.') + 1, className.length());
                        pluginThread.setName("Fablet-" + fabletCount + '-' + className);
                        fabletCount++;
                    }
                    pluginThread.start();

                }

            } catch (Throwable t) {

                logger.log(Level.WARNING, "Fablet initialization failed for [{0}], argument(s) [{1}]: {2}",
                        new Object[] {pluginConfig.getName(), pluginConfig.getArguments(), t.getMessage()});
                logger.log(Level.FINEST, "Full exception: }", t);

            }
        }
    }

    /**
     * @see fabric.bus.plugins.IPluginHandler#stop()
     */
    @Override
    public void stop() {

        if (fablet != null) {
            /* Invoke the initialization method */
            try {

                /* Tell the plug-in instance to close */
                fablet.stopPlugin();

            } catch (Throwable t) {

                logger.log(Level.WARNING, "Failed to stop plugin: ", t);

            }
        }
    }

}
