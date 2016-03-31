/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.FeedRoutes;
import fabric.registry.ext.CustomQueryObject;

/**
 * Represents an applicable route for a given feed.
 */
public class FeedRoutesImpl extends CustomQueryObject implements FeedRoutes, Comparable<FeedRoutes> {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	private String taskId = null;

	private String platformId = null;

	private String systemId = null;

	private String feedId = null;

	private String endNodeId = null;

	private int ordinal = -1;

	private String route = null;

	protected FeedRoutesImpl() {
	}

	public FeedRoutesImpl(String taskID, String platformID, String systemID, String feedTypeID, String endNodeID,
			int ordinal, String route) {

		this.taskId = taskID;
		this.platformId = platformID;
		this.systemId = systemID;
		this.feedId = feedTypeID;
		this.endNodeId = endNodeID;
		this.ordinal = ordinal;
		this.route = route;

	}

	/**
	 * @see fabric.registry.FeedRoutes#getEndNodeId()
	 */
	@Override
	public String getEndNodeId() {
		return endNodeId;
	}

	/**
	 * @see fabric.registry.FeedRoutes#setEndNodeId(java.lang.String)
	 */
	@Override
	public void setEndNodeId(String endNodeId) {
		this.endNodeId = endNodeId;
	}

	/**
	 * @see fabric.registry.FeedRoutes#getFeedId()
	 */
	@Override
	public String getFeedId() {
		return feedId;
	}

	/**
	 * @see fabric.registry.FeedRoutes#setFeedId(java.lang.String)
	 */
	@Override
	public void setFeedId(String feedID) {
		this.feedId = feedID;
	}

	/**
	 * @see fabric.registry.FeedRoutes#getOrdinal()
	 */
	@Override
	public int getOrdinal() {
		return ordinal;
	}

	/**
	 * @see fabric.registry.FeedRoutes#setOrdinal(int)
	 */
	@Override
	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}

	/**
	 * @see fabric.registry.FeedRoutes#getPlatformId()
	 */
	@Override
	public String getPlatformId() {
		return platformId;
	}

	/**
	 * @see fabric.registry.FeedRoutes#setPlatformId(java.lang.String)
	 */
	@Override
	public void setPlatformId(String platformID) {
		this.platformId = platformID;
	}

	/**
	 * @see fabric.registry.FeedRoutes#getRoute()
	 */
	@Override
	public String getRoute() {
		return route;
	}

	/**
	 * @see fabric.registry.FeedRoutes#setRoute(java.lang.String)
	 */
	@Override
	public void setRoute(String route) {
		this.route = route;
	}

	/**
	 * @see fabric.registry.FeedRoutes#getServiceId()
	 */
	@Override
	public String getServiceId() {
		return systemId;
	}

	/**
	 * @see fabric.registry.FeedRoutes#setServiceId(java.lang.String)
	 */
	@Override
	public void setServiceId(String systemID) {
		this.systemId = systemID;
	}

	/**
	 * @see fabric.registry.FeedRoutes#getTaskId()
	 */
	@Override
	public String getTaskId() {
		return taskId;
	}

	/**
	 * @see fabric.registry.FeedRoutes#setTaskId(java.lang.String)
	 */
	@Override
	public void setTaskId(String taskID) {
		this.taskId = taskID;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer("FeedRoutes::");
		buffer.append(" Task ID: ").append(taskId).append(",");
		buffer.append(" Platform ID: ").append(platformId).append(",");
		buffer.append(" Sensor ID: ").append(systemId).append(",");
		buffer.append(" Feed ID: ").append(feedId).append(",");
		buffer.append(" End Node ID: ").append(endNodeId).append(",");
		buffer.append(" Ordinal: ").append(ordinal).append(",");
		buffer.append(" Route: ").append(route);
		return buffer.toString();
	}

	@Override
	public String key() {
		return this.getFeedId();
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FeedRoutes o) {
		/* compare by task, platform, service, feed, node and finally ordinal */
		if (!this.getTaskId().equals(o.getTaskId())) {
			return this.getTaskId().compareTo(o.getTaskId());
		}

		if (!this.getPlatformId().equals(o.getPlatformId())) {
			return this.getPlatformId().compareTo(o.getPlatformId());
		}

		if (!this.getServiceId().equals(o.getServiceId())) {
			return this.getServiceId().compareTo(o.getServiceId());
		}

		if (!this.getFeedId().equals(o.getFeedId())) {
			return this.getFeedId().compareTo(o.getFeedId());
		}

		if (!this.getEndNodeId().equals(o.getEndNodeId())) {
			return this.getEndNodeId().compareTo(o.getEndNodeId());
		}

		if (this.getOrdinal() > o.getOrdinal()) {
			return 1;
		} else if (this.getOrdinal() < o.getOrdinal()) {
			return -1;
		}

		return 0;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean equal = false;
		if (obj != null && obj.getClass().equals(this.getClass())) {
			FeedRoutes fr = (FeedRoutes) obj;
			if (fr.getTaskId() == null ? taskId == null
					: fr.getTaskId().equals(taskId) && fr.getPlatformId() == null ? platformId == null : fr
							.getPlatformId().equals(platformId)
							&& fr.getServiceId() == null ? systemId == null : fr.getServiceId().equals(systemId)
							&& fr.getFeedId() == null ? feedId == null : fr.getFeedId().equals(feedId)
							&& fr.getEndNodeId() == null ? endNodeId == null : fr.getEndNodeId().equals(endNodeId)
							&& fr.getOrdinal() == ordinal && fr.getRoute() == null ? route == null : fr.getRoute()
							.equals(route)) {

				equal = true;
			}
		}
		return equal;
	}

}