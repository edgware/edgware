/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.core.logging.LogUtil;

/**
 * Shutdown hook for Fabric applications.
 */
public class FabricShutdownHook extends Thread {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

	/*
	 * Class static fields
	 */

	/** The list of shutdown actions for this hook to invoke */
	private ArrayList<IFabricShutdownHookAction> actionList = new ArrayList<IFabricShutdownHookAction>();

	private Logger logger;

	public FabricShutdownHook(String name) {

		logger = Logger.getLogger(name);
	}

	/*
	 * Class methods
	 */

	/**
	 * Adds a new shutdown action to the list for this shutdown hook.
	 * <p>
	 * <strong>Note:</strong>
	 * <ul>
	 * <li>Shutdown actions will be invoked in the order in which they are registered.</li>
	 * <li>Each instance of an action class can only be added <em>once</em>.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param action
	 *            the class implementing clean-up actions.
	 */
	public void addAction(IFabricShutdownHookAction action) {

		/* If the action has not already been added... */
		if (!actionList.contains(action)) {

			/* Add the new action */
			actionList.add(action);

		}

	}

	/**
	 * Removes the <em>first</em> occurrence of the specified shutdown action from the list for this shutdown hook.
	 * 
	 * @param action
	 *            the action class to be removed.
	 */
	public void removeAction(IFabricShutdownHookAction action) {

		actionList.remove(action);

	}

	/**
	 * Shutdown hook entry point.
	 * <p>
	 * When called by the JVM, this method will invoke each of the registered shutdown actions.
	 * </p>
	 */
	@Override
	public void run() {

		logger.log(Level.INFO, "Performing pre-termination clean-up");

		/* For each clean-up action... */
		for (Iterator<IFabricShutdownHookAction> i = actionList.iterator(); i.hasNext();) {

			/* Get the next shutdown action */
			IFabricShutdownHookAction nextAction = i.next();

			try {

				/* Invoke the action */
				nextAction.shutdown();

			} catch (Exception e) {

				String actionInstance = nextAction.getClass().getName() + '@'
						+ Integer.toHexString(nextAction.hashCode());

				logger.log(Level.WARNING, "Shutdown action \"{0}\" failed with exception: {1}", new Object[] {
						actionInstance, LogUtil.stackTrace(e)});

			}
		}
	}
}
