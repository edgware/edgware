/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */


package fabric.fablets.autodiscovery;

import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.core.io.ICallback;
import fabric.core.io.Message;
import fabric.core.properties.ConfigProperties;
import fabric.session.NodeDescriptor;

/**
 * Thread used to scan the cache of discovered nodes and remove any that have not been heard from within the expiration
 * window.
 * 
 */
public class DiscoverySweeper implements Runnable, ICallback {

	/** Flag controlling the lifecycle of this thread */
	private boolean isSweeping = true;

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	private final static String CLASS_NAME = DiscoverySweeper.class.getName();
	private final static String PACKAGE_NAME = DiscoverySweeper.class.getPackage().getName();
	private final static Logger logger = Logger.getLogger(PACKAGE_NAME);

	/** my owning fablet which holds the caches for autodiscovery */
	private AutoDiscoveryListenerFablet myFablet = null;

	/** Timeout value used to determine that a neighbour is no longer visible and should be removed */
	private int nodeTimeout = 0;

	/** Interval (in millis) at which the sweeper checks for node neighbours that are no longer visible */
	private int listenTimeout = 0;

	protected DiscoverySweeper(AutoDiscoveryListenerFablet myFablet) {
		this.myFablet = myFablet;
		/** Timeout value used to determine that a neighbour is no longer visible and should be removed */
		nodeTimeout = new Integer(myFablet.config(ConfigProperties.AUTO_DISCOVERY_TIMEOUT,
				ConfigProperties.AUTO_DISCOVERY_TIMEOUT_DEFAULT)).intValue();

		/** Interval (in millis) at which the sweeper checks for node neighbours that are no longer visible */
		listenTimeout = new Integer(myFablet.config(ConfigProperties.AUTO_DISCOVERY_SWEEPER_INTERVAL,
				ConfigProperties.AUTO_DISCOVERY_SWEEPER_INTERVAL_DEFAULT)).intValue();

	}

	@Override
	public void run() {
		String METHOD_NAME = "run";
		logger.entering(CLASS_NAME, METHOD_NAME);

		long last_time_checked = new Date().getTime();

		logger.log(Level.FINE, "Neighbour sweeper thread coming online.");

		while (isSweeping) {
			long current_time = new Date().getTime();

			/* initially, wait a proportion of time before checking node cache */
			if ((current_time - last_time_checked) > nodeTimeout / 6) {

				synchronized (myFablet.nodeLastSeen) {
					Iterator<NodeDescriptor> cache_it = myFablet.nodeLastSeen.keySet().iterator();

					/* Check each node, if we've not seen a node for more than the node_timeout, delete it */
					while (cache_it.hasNext()) {
						NodeDescriptor n = cache_it.next();
						if (current_time - myFablet.nodeLastSeen.get(n) > nodeTimeout) {

							// Tell the Fabric (AutoDiscoveryFablet) that the node has disappeared
							myFablet.publishMessage(myFablet.nodeMessageCache.get(n).getDiscoveryMessage(
									MulticastNodeMessage.UNAVAILABLE));

							logger.log(
									Level.FINE,
									"We haven't seen the following node for {0} seconds, deleting it as a neighbour: {1}",
									new Object[] {nodeTimeout, n});

							cache_it.remove(); /*
												 * must remove elements from the iterator rather than the hashtable
												 * itself to avoid ConcurrentModificationException
												 */
							myFablet.nodeMessageCache.remove(n);
						}
					}
				}
			}

			/* go to sleep for the timeout period */
			try {
				Thread.sleep(listenTimeout);
			} catch (InterruptedException e) {
			}
		}
		logger.exiting(CLASS_NAME, METHOD_NAME);
	}

	protected void stop() {
		isSweeping = false;
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.core.io.Callback#cancelCallback(java.lang.Object)
	 */
	@Override
	public void cancelCallback(Object arg1) {

		/* Nothing to do here */
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.core.io.Callback#cancelCallback(java.lang.Object)
	 */
	@Override
	public void startCallback(Object arg1) {

		/* Nothing to do here */
	}

	/*
	 * (non-Javadoc)
	 * @see fabric.core.io.Callback#handleMessage(fabric.core.io.Message)
	 */
	@Override
	public synchronized void handleMessage(Message message) {

		/* Not used */
	}
}
