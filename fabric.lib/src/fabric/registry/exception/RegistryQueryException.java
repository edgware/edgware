/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.exception;

/**
 * Exception which is thrown when an attempt is made to run an invalid SQL query using the custom SQL interface.
 */
public class RegistryQueryException extends Exception {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public RegistryQueryException() {
	}

	/**
	 * 
	 * @param msg
	 */
	public RegistryQueryException(String msg) {
		super(msg);
	}
}