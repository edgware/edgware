/*
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

/**
 * Factory used to create CachedMessages and save/delete/query them in the Fabric Registry.
 * 
 * @see fabric.registry.FabricRegistry#getCachedMessageFactory()
 *
 */
public interface CachedMessageFactory extends Factory {
	
	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

	/**
	 * Create a CachedMessage.
	 * @param timestamp the timestamp, in milliseconds since epoch, for the message.
	 * @param source the source of the message.
	 * @param destination the destination of the message.
	 * @param message the body of the message.
	 * @return an instance of CachedMessage
	 */
	public CachedMessage createCachedMessage(long timestamp, String source, String destination, String message);
	
	/**
	 * Queries the Fabric Registry for cached messages that match the specified criteria. If either argument is null,
	 * that property is not used when filtering the results. The filters may contain the SQL wildcard character, '%'.
	 * @param source the filter on the source of the message
	 * @param destination the filter on the destination of the message
	 * @return the list of messages that match the criteria.
	 */
	public CachedMessage[] getMessages(String source, String destination);
	
	/**
	 * Returns all cached messages.
	 * @return all cached messages in the FabricRegistry.
	 */
	public CachedMessage[] getAllMessages();
}
