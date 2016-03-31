/*
 * (C) Copyright IBM Corp. 2009, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus;

import fabric.MetricsManager;
import fabric.bus.feeds.IFeedManager;
import fabric.bus.services.IConnectionManager;
import fabric.bus.services.INotificationManager;
import fabric.services.messageforwarding.MessageForwardingService;

/**
 * Interface for classes supporting Fabric management operations.
 * 
 */
public interface IBusServices extends IBusIO, IConnectionManager {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

	/*
	 * Interface methods
	 */

	/**
	 * Answers an object providing an interface to Fabric bus IO operations.
	 * 
	 * @return the IO interface object.
	 */
	public IBusIO busIO();

	/**
	 * Answers the service providing subscription management services.
	 * 
	 * @return the service.
	 */
	public IFeedManager feedManager();

	/**
	 * Answers the service providing bus connection status management services.
	 * 
	 * @return the service.
	 */
	public IConnectionManager connectionManager();

	/**
	 * Answers the service providing event notification services.
	 * 
	 * @return the service.
	 */
	public INotificationManager notificationManager();

	/**
	 * Answers the service providing message forwarding services.
	 * 
	 * @return the service.
	 */
	public MessageForwardingService forwardingManager();

	/**
	 * Answers the service providing message handling profiling capture.
	 * 
	 * @return the service.
	 */
	public MetricsManager metrics();

	/**
	 * Stops the Fabric manager.
	 */
	public void stop();

}
