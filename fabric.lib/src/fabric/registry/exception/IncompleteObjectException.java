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
 * Exception which is thrown when an attempt is made to save a Registry object which has missing or invalid fields that
 * are required in order to satisfy key contrainsts within the database.
 */
public class IncompleteObjectException extends Exception {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public IncompleteObjectException() {
	}

	/**
	 * 
	 * @param msg
	 */
	public IncompleteObjectException(String msg) {
		super(msg);
	}

}
