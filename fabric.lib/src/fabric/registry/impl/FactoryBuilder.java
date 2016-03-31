/*
 * (C) Copyright IBM Corp. 2009, 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.registry.exception.FactoryCreationException;

public class FactoryBuilder {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

    /**
     * Instantiates an object factory using the specified class.
     * 
     * @param factoryClass
     *            - a class that extends fabric.registry.impl.AbstractFactory
     * @return an instance of the object factory or null if the specified class does not extend
     *         fabric.registry.impl.AbstractFactory.
     * @throws FactoryCreationException
     *             if there is an error trying to instantiate an object from the specified class.
     */
    public static AbstractFactory createFactory(Class factoryClass) throws FactoryCreationException {
        AbstractFactory factory = null;
        try {
            Object obj = factoryClass.newInstance();
            if (obj != null && obj instanceof AbstractFactory) {
                factory = (AbstractFactory) obj;
            }
        } catch (IllegalAccessException e) {
            Logger logger = Logger.getLogger("fabric.registry");
            logger.log(Level.WARNING, "Error occurred accessing factory class: ", e);
            throw new FactoryCreationException(e);
        } catch (InstantiationException e) {
            Logger logger = Logger.getLogger("fabric.registry");
            logger.log(Level.WARNING, "Error occurred instantiating factory class: ", e);
            throw new FactoryCreationException(e);
        }
        return factory;
    }
}
