/*
 * (C) Copyright IBM Corp. 2007, 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins.impl;

import fabric.ServiceDescriptor;
import fabric.bus.plugins.IFeedPluginConfig;

/**
 * Class representing configuration information for a Fabric Manager message plug-in.
 */
public class FeedPluginConfig extends FabletConfig implements IFeedPluginConfig {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2009";

	/*
	 * Class fields
	 */

	/** The ID of the task (if any). */
	protected String task = null;

	/** The ID of the actor (if any). */
	protected String actor = null;

	/** The ID of the Fabric feed. */
	protected ServiceDescriptor feed = null;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 */
	public FeedPluginConfig() {
	}

	/**
	 * @see fabric.bus.plugins.IFeedPluginConfig#getActor()
	 */
	@Override
	public String getActor() {
		return actor;
	}

	/**
	 * @see fabric.bus.plugins.IFeedPluginConfig#setActor(java.lang.String)
	 */
	@Override
	public void setActor(String actor) {
		this.actor = actor;
	}

	/**
	 * @see fabric.bus.plugins.IFeedPluginConfig#getTask()
	 */
	@Override
	public String getTask() {
		return task;
	}

	/**
	 * @see fabric.bus.plugins.IFeedPluginConfig#setTask(java.lang.String)
	 */
	@Override
	public void setTask(String task) {
		this.task = task;
	}

	/**
	 * @see fabric.bus.plugins.IFeedPluginConfig#getFeed()
	 */
	@Override
	public ServiceDescriptor getFeed() {
		return feed;
	}

	/**
	 * @see fabric.bus.plugins.IFeedPluginConfig#setFeed(fabric.ServiceDescriptor)
	 */
	@Override
	public void setFeed(ServiceDescriptor feed) {
		this.feed = feed;
	}

}
