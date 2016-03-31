/*
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.services;

import fabric.bus.messages.IFabricMessage;

/**
 * Interface for services that handle Flood messages.
 *
 */
public interface IFloodMessageService {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

	
	/**
	 * Returns whether this message is a duplicate of one previously seen
	 * by the flood service
	 * 
	 * @param uid
	 * @return true if this message has already been handled, false otherwise.
	 */
	public boolean isDuplicate(String uid);
	
	/**
	 * Adds a message to the cache of seen messages.

	 * @param message the message
	 * @param ttl the time, in relative milliseconds, the message should be held in the cache
	 * @param retained (currently unused) whether the message should be retained for future nodes to receive 
	 */
	public void addMessage(IFabricMessage message, long ttl, boolean retained);
	
}
