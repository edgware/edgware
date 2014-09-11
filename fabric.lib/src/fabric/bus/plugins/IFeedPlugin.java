/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2007, 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins;

import fabric.bus.messages.IFeedMessage;
import fabric.bus.routing.IRouting;

/**
 * Interface implemented by Fabric manager policy-enabled plug-ins.
 */
public interface IFeedPlugin extends IPlugin {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2009";

	/*
	 * Interface constants
	 */

	/*
	 * Constants used to define plug-in actions, communicated back to the calling Fabric Manager
	 */

	/** Continue to process the message as normal. */
	public static final int ACTION_CONTINUE = 0;

	/** Discard the message (can be overridden by a later plug-in). */
	public static final int ACTION_DISCARD = 1;

	/** Discard the message (cannot be overridden). */
	public static final int ACTION_DISCARD_IMMEDIATE = 2;

	/*
	 * Interface methods
	 */

	/**
	 * Handles a message.
	 * 
	 * @param message
	 *            the message.
	 * 
	 * @param routing
	 *            the routing for the feed message.
	 * 
	 * @param pluginAction
	 *            the action to take for this message (one of the <code>ACTION_</code> constants defined in the
	 *            <code>IFeedPlugin</code> interface) as defined by any earlier plugins invoked to handle the message;
	 *            this plug-in can override this action if required.
	 * 
	 * @return the plug-in determined action for the message, one of the <code>ACTION_</code> constants defined in the
	 *         <code>IFeedPlugin</code> interface.
	 */
	public int handleFeedMessage(IFeedMessage message, IRouting routing, int pluginAction);

}
