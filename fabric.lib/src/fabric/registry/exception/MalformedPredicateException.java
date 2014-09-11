/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2011
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.exception;

/**
 * Exception which is thrown when parsing an invalid query predicate.
 */
public class MalformedPredicateException extends RegistryQueryException {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2011";

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public MalformedPredicateException() {
	}

	/**
	 * 
	 * @param msg
	 */
	public MalformedPredicateException(String msg) {
		super(msg);
	}
}