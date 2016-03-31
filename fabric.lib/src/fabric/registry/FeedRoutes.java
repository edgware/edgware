/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

/**
 * Represents an applicable route for a given feed.
 */
public interface FeedRoutes extends RegistryObject {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/**
	 * Get the last node in the route.
	 * 
	 * @return the identifier of the last node in the route
	 */
	public String getEndNodeId();

	/**
	 * Set the last node in the route.
	 * 
	 * @param endNodeID
	 *            the identifier of the last node in the route
	 */
	public void setEndNodeId(String endNodeID);

	/**
	 * Get the ID of the feed on the given system attached to the specified platform.
	 * 
	 * @return the ID of the feed.
	 */
	public String getFeedId();

	/**
	 * Set the ID of the feed on the given system attached to the specified platform.
	 * 
	 * @param feedID
	 *            the ID of the feed.
	 */
	public void setFeedId(String feedID);

	/**
	 * Get the ordinal of the route.
	 * 
	 * @return the integer ordinal of the route.
	 */
	public int getOrdinal();

	/**
	 * Set the ordinal of the route.
	 * 
	 * @param ordinal
	 *            the integer ordinal of the route.
	 */
	public void setOrdinal(int ordinal);

	/**
	 * Get the platform associated with this feed route.
	 * 
	 * @return the identifier of the platform.
	 */
	public String getPlatformId();

	/**
	 * Set the platform associated with this feed route,
	 * 
	 * @param platformId
	 *            the identifier of the platform.
	 */
	public void setPlatformId(String platformId);

	/**
	 * Get the route definition.
	 * 
	 * @return the string representing the route.
	 */
	public String getRoute();

	/**
	 * Set the route definition.
	 * 
	 * @param route
	 *            the string representing the route.
	 */
	public void setRoute(String route);

	/**
	 * Get the ID of the service attached to the specified platform.
	 * 
	 * @return the ID of the service.
	 */
	public String getServiceId();

	/**
	 * Set the ID of the service attached to the specified platform.
	 * 
	 * @param serviceId
	 *            the ID of the service.
	 */
	public void setServiceId(String serviceId);

	/**
	 * Get the identifier for the task.
	 * 
	 * @return the String identifier for the task.
	 */
	public String getTaskId();

	/**
	 * Set the identifier for the task.
	 * 
	 * @param taskId
	 *            the String identifier for the task.
	 */
	public void setTaskId(String taskId);
}
