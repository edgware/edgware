/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */
package fabric.bus.feeds;

/**
 * Exception throw when an attempt is made to change an active subscription (for example, unsubscribe) when not
 * subscribed.
 */
public class SubscriptionException extends Exception {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/*
	 * Enumerated types
	 */

	public enum Reason {
		ALREADY_SUBSCRIBED, NOT_SUBSCRIBED, NO_ROUTE, UNKNOWN
	};

	/*
	 * Class constants
	 */

	private static final long serialVersionUID = -6839062577512012055L;

	/*
	 * Class fields
	 */

	/** The cause of the exception */
	private Reason cause = Reason.UNKNOWN;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 * 
	 * @param cause
	 *            the cause of the exception.
	 */
	public SubscriptionException(Reason cause, String message) {
		super(message);
		this.cause = cause;
	}

	/**
	 * Answers the cause of this exception.
	 * 
	 * @return the cause.
	 */
	public Reason getReason() {
		return cause;
	}
}
