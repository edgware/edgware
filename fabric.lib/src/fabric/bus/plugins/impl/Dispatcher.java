/*
 * (C) Copyright IBM Corp. 2009, 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins.impl;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.FabricBus;
import fabric.bus.plugins.IDispatcher;
import fabric.bus.plugins.IFamily;
import fabric.bus.plugins.IPluginConfig;

/**
 * Base class for Fabric plug-in dispatchers.
 */
public abstract class Dispatcher extends FabricBus implements IDispatcher {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

    /*
     * Class fields
     */

    /** The table of plug-in management objects (one entry per plug-in family). */
    private static HashMap<String, Family> familyManagement = new HashMap<String, Family>();

    /*
     * Class methods
     */

    public Dispatcher(Logger logger) {

        super(logger);
    }

    /**
     * @see fabric.bus.plugins.IDispatcher#initPluginConfig()
     */
    @Override
    public IPluginConfig initPluginConfig() {

        IPluginConfig config = new PluginConfig();
        logger.log(Level.INFO, "Service [{0}] started", getClass().getName());
        return config;

    }

    /**
     * @see fabric.bus.plugins.IDispatcher#family(java.lang.String)
     */
    @Override
    public IFamily family(String familyName) {

        Family family = null;

        synchronized (familyManagement) {

            /* Get the current family management object */
            family = familyManagement.get(familyName);

            /* If none has been defined... */
            if (family == null) {

                /* Initialize it */
                family = new Family();
                familyManagement.put(familyName, family);
                logger.log(Level.FINEST, "Added a family management object for family [{0}]", familyName);

            }

        }

        return family;

    }

    /**
     * @see fabric.bus.plugins.IDispatcher#removeFamily(java.lang.String)
     */
    @Override
    public void removeFamily(String familyName) {

        synchronized (familyManagement) {

            familyManagement.remove(familyName);
            logger.log(Level.INFO, "Removed a family management object {0}", familyName);

        }

    }
}
