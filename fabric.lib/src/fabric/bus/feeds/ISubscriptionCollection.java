/*
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.feeds;

/**
 * Interface for classes managing a collection of client subscriptions to Fabric data feeds.
 */
public interface ISubscriptionCollection {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

	/**
	 * Tear down all subscriptions managed by this collection.
	 * 
	 * @throws Exception
	 *             thrown if the unsubscribe fails.
	 */
	public void unsubscribe() throws Exception;

	/**
	 * Answers the set of subscriptions currently subscribed to.
	 * 
	 * @return the subscriptions.
	 */
	public ISubscription[] subscriptions();

	/**
	 * Causes the collection to refresh its list of feeds, creating new subscriptions to feeds that have been
	 * discovered.
	 * 
	 * @throws Exception
	 */
	public void refresh() throws Exception;
}
