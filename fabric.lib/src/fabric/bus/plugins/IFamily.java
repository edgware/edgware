/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins;

import fabric.bus.messages.IFabricMessage;

/**
 * Interface for family management classes, i.e. classes providing access to the shared resources for an individual
 * family of Fabric plug-ins.
 * <p>
 * An instance of this type is passed to each member of a family before it is run.
 * </p>
 */
public interface IFamily {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/*
	 * Interface methods
	 */

	/**
	 * Gets the shared object for a family of plug-ins.
	 * <p>
	 * The data itself is represented by a single object, the type of which is defined by the family members. Only the
	 * members of the family have access to the data.
	 * </p>
	 * 
	 * @return the shared object for the family.
	 */
	public Object getSharedData();

	/**
	 * Sets the shared object for a family of plug-ins.
	 * <p>
	 * The data itself is represented by a single object, the type of which is defined by the family members. Only the
	 * members of the family have access to the data.
	 * </p>
	 * 
	 * @param sharedData
	 *            the shared object.
	 */
	public void setSharedData(Object sharedData);

	/**
	 * Registers a plug-in to receive family control messages.
	 * <p>
	 * Registration is optional; each plug-in can choose to opt-in or opt-out of the receipt of such messages, although
	 * by default each plug-in is registered (i.e. control messages are enabled).
	 * </p>
	 * 
	 * @param plugin
	 *            the plug-in to be registered.
	 */
	public void enableControlMessages(IPlugin plugin);

	/**
	 * De-registers a plug-in from receiving family control messages.
	 * <p>
	 * Registration is optional; each plug-in can choose to opt-in or opt-out of the receipt of such messages, although
	 * by default each plug-in is registered.
	 * </p>
	 * 
	 * @param plugin
	 *            the plug-in to be de-registered.
	 */
	public void disableControlMessages(IPlugin plugin);

	/**
	 * Delivers a control message to registered family members.
	 * 
	 * @param message
	 *            the control message.
	 */
	public void deliverControlMessage(IFabricMessage message);

}