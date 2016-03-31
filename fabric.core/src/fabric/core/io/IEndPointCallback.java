/*
 * (C) Copyright IBM Corp. 2013
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.io;

/**
 * Interface for callbacks responding to end point events.
 */
public interface IEndPointCallback {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2013";

	/*
	 * Interface methods
	 */

	/**
	 * Invoked when an endpoint connects by request.
	 * 
	 * @param ep
	 *            the endpoint generating the event.
	 */
	public void endPointConnected(EndPoint ep);

	/**
	 * Invoked when an endpoint disconnects unexpectedly.
	 * 
	 * @param ep
	 *            the endpoint generating the event.
	 */
	public void endPointDisconnected(EndPoint ep);

	/**
	 * Invoked when an endpoint reconnects after an unexpected disconnection.
	 * 
	 * @param ep
	 *            the endpoint generating the event.
	 */
	public void endPointReconnected(EndPoint ep);

	/**
	 * Invoked when an endpoint has been closed by request.
	 * 
	 * @param ep
	 *            the endpoint generating the event.
	 */
	public void endPointClosed(EndPoint ep);

	/**
	 * Invoked when an endpoint has been closed by lost, i.e. any and all connection retries have failed.
	 * 
	 * @param ep
	 *            the endpoint generating the event.
	 */
	public void endPointLost(EndPoint ep);

}
