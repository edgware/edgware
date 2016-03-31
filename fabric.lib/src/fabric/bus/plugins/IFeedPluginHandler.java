/*
 * (C) Copyright IBM Corp. 2007, 2008
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins;

import fabric.bus.messages.IFeedMessage;
import fabric.bus.routing.IRouting;

/**
 * Interface representing the per-task and per-actor Fabric plug-ins for a data feed subscription.
 */
public interface IFeedPluginHandler extends IPluginHandler {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2008";

	/*
	 * Interface methods
	 */

	/**
	 * Invoke the plug-in to process the specified message.
	 * 
	 * @param message
	 *            the message to process.
	 * 
	 * @param routing
	 *            the routing information for the current and next hops in the messages route to the client.
	 * 
	 * @param pluginAction
	 *            the current action to take for this message (one of the <code>ACTION_</code> constants defined in the
	 *            interface) as defined by any earlier plugins invoked to handle the message; this plug-in can override
	 *            this action if required.
	 * 
	 * @return the plug-in determined action for the message, one of the <code>ACTION_</code> constants defined in the
	 *         interface.
	 */
	public int run(IFeedMessage message, IRouting routing, int pluginAction);

}