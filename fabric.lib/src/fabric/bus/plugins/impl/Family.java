/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.bus.messages.IFabricMessage;
import fabric.bus.plugins.IFamily;
import fabric.bus.plugins.IPlugin;
import fabric.core.logging.LogUtil;

/**
 * Class providing thread-safe access to the family management object for a single family.
 */
public class Family implements IFamily {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

	/*
	 * Class fields
	 */

	/** The synchronization object controlling access to family shared data. */
	private Object dataLock = new Object();

	/** The shared data. */
	private Object sharedData = null;

	/** The list of family members registered to receive control messages */
	private HashMap<String, IPlugin> controlMessageReceivers = new HashMap<String, IPlugin>();

	private Logger logger;

	/*
	 * Class methods
	 */

	public Family() {

		logger = Logger.getLogger("fabric.bus.plugins");
	}

	/**
	 * @see fabric.bus.plugins.IFamily#getSharedData()
	 */
	@Override
	public Object getSharedData() {

		Object sharedData = null;

		synchronized (dataLock) {

			sharedData = this.sharedData;

		}

		return sharedData;

	}

	/**
	 * @see fabric.bus.plugins.IFamily#setSharedData(java.lang.Object)
	 */
	@Override
	public void setSharedData(Object sharedData) {

		synchronized (dataLock) {

			this.sharedData = sharedData;

		}

	}

	/**
	 * @see fabric.bus.plugins.IFamily#disableControlMessages(fabric.bus.plugins.IPlugin)
	 */
	@Override
	public void disableControlMessages(IPlugin plugin) {

		String pluginInstance = plugin.getClass().getName() + '@' + Integer.toHexString(plugin.hashCode());

		synchronized (controlMessageReceivers) {

			controlMessageReceivers.put(pluginInstance, plugin);

		}

	}

	/**
	 * @see fabric.bus.plugins.IFamily#enableControlMessages(fabric.bus.plugins.IPlugin)
	 */
	@Override
	public void enableControlMessages(IPlugin plugin) {

		String pluginInstance = plugin.getClass().getName() + '@' + Integer.toHexString(plugin.hashCode());

		synchronized (controlMessageReceivers) {

			controlMessageReceivers.remove(pluginInstance);

		}

	}

	/**
	 * @see fabric.bus.plugins.IFamily#deliverControlMessage(fabric.bus.messages.IFabricMessage)
	 */
	@Override
	public void deliverControlMessage(IFabricMessage message) {

		synchronized (controlMessageReceivers) {

			/* For each registered plug-in... */
			for (Iterator<String> p = controlMessageReceivers.keySet().iterator(); p.hasNext();) {

				/* Get the next plug-in */
				String key = p.next();
				IPlugin nextPlugin = controlMessageReceivers.get(key);

				try {

					/* Invoke the handler */
					nextPlugin.handleControlMessage(message);

				} catch (Exception e) {

					logger.log(Level.WARNING, "Plug-in \"{0}\" failed to handle control message:\n{1}\n{2}",
							new Object[] {key, message, LogUtil.stackTrace(e)});

				}
			}
		}

	}
}
