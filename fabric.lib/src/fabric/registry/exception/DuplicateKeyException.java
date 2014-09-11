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
 * Exception representing a duplicate key conflict when inserting an object into the registry.
 */
public class DuplicateKeyException extends Exception {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	public DuplicateKeyException() {
	}

	public DuplicateKeyException(String message) {
		super(message);
	}

	public DuplicateKeyException(Throwable cause) {
		super(cause);
	}

	public DuplicateKeyException(String message, Throwable cause) {
		super(message, cause);
	}

}
