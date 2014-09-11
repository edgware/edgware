/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2007, 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages;

import fabric.ServiceDescriptor;
import fabric.bus.messages.impl.TaskSubscriptions;

/**
 * Interface defining a Fabric feed subscription message.
 */
public interface IFeedMessage extends IFabricMessage {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2009";

	/**
	 * Gets the Fabric feed descriptor associated with this message.
	 * 
	 * @return the ID of the Fabric feed.
	 */
	public ServiceDescriptor metaGetFeedDescriptor();

	/**
	 * Sets the Fabric feed descriptor for this message.
	 * 
	 * @param descriptor
	 *            the feed descriptor.
	 */
	public void metaSetFeedDescriptor(ServiceDescriptor descriptor);

	/**
	 * Gets the data feed message number.
	 * 
	 * @return the message number.
	 */
	public long getOrdinal();

	/**
	 * Sets the data feed message number.
	 * 
	 * @param ordinal
	 *            the message number.
	 */
	public void setOrdinal(long ordinal);

	/**
	 * Gets the flag indicating if this is a replay message.
	 * 
	 * @return <code>true</code> if this is a replayed message, <code>false</code> otherwise.
	 */
	public boolean isReplay();

	/**
	 * Sets the flag indicating if this is a replay message (<code>true</code> if this is a replay message,
	 * <code>false</code> otherwise).
	 * 
	 * @param isReplay
	 *            the flag value.
	 */
	public void setReplay(boolean isReplay);

	/**
	 * Gets the table of task and client IDs from this Fabric feed message.
	 * <p>
	 * This information is recorded in a table where each key is a task ID, and each value is a list of client IDs.
	 * </p>
	 * 
	 * @return the current subscriptions.
	 */
	public TaskSubscriptions getSubscriptions();

	/**
	 * Sets the table of task and client IDs for this Fabric feed message.
	 * <p>
	 * This information is recorded in a table where each key is a task ID, and each value is a list of client IDs.
	 * </p>
	 * 
	 * @param newSubscriptions
	 *            the subscriptions.
	 */
	public void setSubscriptions(TaskSubscriptions newSubscriptions);

}