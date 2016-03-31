/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins;

import fabric.bus.messages.IFeedMessage;
import fabric.bus.routing.IRouting;

/**
 * Interface for Fabric message plug-in dispatchers.
 */
public interface IFeedPluginDispatcher extends IFabletDispatcher {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/*
	 * Interface methods
	 */

	/**
	 * Invoke the handlers for a message.
	 * 
	 * @param message
	 *            the XML message to handle.
	 * 
	 * @param pluginAction
	 *            the action to take for this message (one of the <code>ACTION_</code> constants defined in the
	 *            <code>IFeedPlugin</code> interface) as defined by any earlier plugins invoked to handle the message;
	 *            this plug-in can override this action if required.
	 * 
	 * @return the plug-in determined action for the message, one of the <code>ACTION_</code> constants defined in the
	 *         <code>IFeedPlugin</code> interface.
	 * 
	 * @throws Exception
	 *             thrown if an exception is thrown by a plug-in.
	 */
	public int dispatch(IFeedMessage message, int pluginAction) throws Exception;

	/**
	 * Invoke the handlers for a message.
	 * <p>
	 * Handlers invoked via this method can influence the route taken by the message.
	 * </p>
	 * 
	 * @param message
	 *            the XML message to handle.
	 * 
	 * @param routing
	 *            the routing information for the current and next hops in the messages route to the client.
	 * 
	 * @param pluginAction
	 *            the action to take for this message (one of the <code>ACTION_</code> constants defined in the
	 *            <code>IFeedPlugin</code> interface) as defined by any earlier plugins invoked to handle the message;
	 *            this plug-in can override this action if required.
	 * 
	 * @return the plug-in determined action for the message, one of the <code>ACTION_</code> constants defined in the
	 *         <code>IFeedPlugin</code> interface.
	 * 
	 * @throws Exception
	 *             thrown if an exception is thrown by a plug-in.
	 */
	public int dispatch(IFeedMessage message, IRouting routing, int pluginAction) throws Exception;

}
