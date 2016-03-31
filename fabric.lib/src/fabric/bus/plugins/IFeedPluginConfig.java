/*
 * (C) Copyright IBM Corp. 2007, 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins;

import fabric.ServiceDescriptor;

/**
 * Interface representing configuration information for a Fabric Manager message plug-in.
 */
public interface IFeedPluginConfig extends IFabletConfig {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2009";

	/*
	 * Interface methods
	 */

	/**
	 * Answers the actor ID.
	 * 
	 * @return the ID.
	 */
	public String getActor();

	/**
	 * Sets the actor ID.
	 * 
	 * @param actor
	 *            the ID.
	 */
	public void setActor(String actor);

	/**
	 * Answers the task ID.
	 * 
	 * @return the ID.
	 */
	public String getTask();

	/**
	 * Sets the task ID.
	 * 
	 * @param task
	 *            the ID.
	 */
	public void setTask(String task);

	/**
	 * Answers the feed descriptor.
	 * 
	 * @return the descriptor.
	 */
	public ServiceDescriptor getFeed();

	/**
	 * Sets the feed descriptor.
	 * 
	 * @param feed
	 *            the descriptor.
	 */
	public void setFeed(ServiceDescriptor feed);

}