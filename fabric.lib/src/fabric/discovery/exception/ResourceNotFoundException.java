/*
 * (C) Copyright IBM Corp. 2011
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.discovery.exception;

/**
 * Exception class thrown when Fabric resources (Fabric Managers, Registries) are not discovered.
 */
public class ResourceNotFoundException extends Exception {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2011";

	/**
	 * Default constructor.
	 */
	public ResourceNotFoundException() {
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            - the error message to include.
	 */
	public ResourceNotFoundException(String message) {
		super(message);

	}

	/**
	 * Constructor.
	 * 
	 * @param cause
	 *            - the root cause exception to include.
	 */
	public ResourceNotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            - the error message to include.
	 * @param cause
	 *            - the root cause exception to include.
	 */
	public ResourceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
