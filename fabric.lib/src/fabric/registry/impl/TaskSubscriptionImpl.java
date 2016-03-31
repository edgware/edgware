/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.TaskSubscription;
import fabric.registry.exception.IncompleteObjectException;

/**
 * Implementation of <code>TaskSubscription</code>.
 */
public class TaskSubscriptionImpl extends AbstractRegistryObject implements TaskSubscription {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	private String taskId = null;
	private String actorId = null;
	private String platformId = null;
	private String systemId = null;
	private String feedId = null;
	private String actorPlatformId = null;

	private String hashString = null;

	protected TaskSubscriptionImpl() {

	}

	protected TaskSubscriptionImpl(String taskId, String actorId, String platformId, String systemId, String feedId,
			String clientPlatformId) {

		this.taskId = taskId;
		this.actorId = actorId;
		this.platformId = platformId;
		this.systemId = systemId;
		this.feedId = feedId;
		this.actorPlatformId = clientPlatformId;

	}

	@Override
	public String getActorId() {

		return actorId;
	}

	@Override
	public void setActorId(String actorId) {

		this.actorId = actorId;
	}

	@Override
	public String getFeedId() {

		return feedId;
	}

	@Override
	public void setFeedId(String feedId) {

		this.feedId = feedId;
	}

	@Override
	public String getPlatformId() {

		return platformId;
	}

	@Override
	public void setPlatformId(String platformId) {

		this.platformId = platformId;
	}

	@Override
	public String getTaskId() {

		return taskId;
	}

	@Override
	public void setTaskId(String taskId) {

		this.taskId = taskId;
	}

	@Override
	public void validate() throws IncompleteObjectException {

		if (taskId == null || taskId.length() == 0 || actorId == null || actorId.length() == 0 || platformId == null
				|| platformId.length() == 0 || systemId == null || systemId.length() == 0 || feedId == null
				|| feedId.length() == 0 || actorPlatformId == null || actorPlatformId.length() == 0) {

			throw new IncompleteObjectException(
					"Task, client, platform, system and feed IDs, and node are all required.");
		}
	}

	@Override
	public String getActorPlatformId() {

		return actorPlatformId;
	}

	@Override
	public void setActorPlatformId(String clientNodeId) {

		this.actorPlatformId = clientNodeId;
	}

	@Override
	public String getSystemId() {

		return systemId;
	}

	@Override
	public void setSystemId(String systemId) {

		this.systemId = systemId;
	}

	/**
	 * Checks that 2 subscriptions are identical.
	 */
	@Override
	public boolean equals(Object obj) {

		boolean equal = false;
		if (obj != null && obj.getClass().equals(this.getClass())) {
			TaskSubscription tsub = (TaskSubscription) obj;
			if (tsub.getActorId() == null ? actorId == null : tsub.getActorId().equals(actorId)
					&& tsub.getActorPlatformId() == null ? actorPlatformId == null : tsub.getActorPlatformId().equals(
					actorPlatformId)
					&& tsub.getFeedId() == null ? feedId == null : tsub.getFeedId().equals(feedId)
					&& tsub.getSystemId() == null ? systemId == null : tsub.getSystemId().equals(systemId)
					&& tsub.getPlatformId() == null ? platformId == null : tsub.getPlatformId().equals(platformId)
					&& tsub.getTaskId() == null ? taskId == null : tsub.getTaskId().equals(taskId)) {

				equal = true;
			}
		}
		return equal;
	}

	/**
	 * Simple implementation of hashcode.
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		if (hashString == null) {
			hashString = taskId + "/" + actorId + "/" + actorPlatformId + "/" + platformId + "/" + systemId + "/"
					+ feedId;
		}
		return hashString.hashCode();
	}

	@Override
	public String toString() {

		StringBuffer buffy = new StringBuffer("TaskSubscription::");
		buffy.append(" Task ID: ").append(taskId);
		buffy.append(", Actor ID: ").append(actorId);
		buffy.append(", Actor Platform ID: ").append(actorPlatformId);
		buffy.append(", Platform ID: ").append(platformId);
		buffy.append(", Platform Sensor ID: ").append(systemId);
		buffy.append(", Sensor Feed ID: ").append(feedId);
		return buffy.toString();
	}

	@Override
	public String key() {

		return new StringBuffer(this.getTaskId()).append("/").append(this.getActorId()).append("/").append(
				this.getPlatformId()).append("/").append(this.getSystemId()).append("/").append(this.getFeedId())
				.append("/").append(this.getActorPlatformId()).toString();
	}

}
