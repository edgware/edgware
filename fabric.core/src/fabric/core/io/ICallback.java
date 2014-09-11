/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2007
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.io;

/**
 * Interface for a callback invoked to handle input on an I/O channel.
 */
public interface ICallback {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007";

	/*
	 * Class methods
	 */

	/**
	 * Invoked when this callback is being initialised.
	 * 
	 * @param arg1
	 *            technology-specific argument.
	 */
	public void startCallback(Object arg1);

	/**
	 * Invoked when data is available on the inbound port of the associated channel.
	 * 
	 * @param message
	 *            the mesage.
	 */
	public void handleMessage(Message message);

	/**
	 * Invoked when this callback is being cancelled.
	 * 
	 * @param arg1
	 *            technology-specific argument.
	 */
	public void cancelCallback(Object arg1);
}
