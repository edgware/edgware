/*
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */
package fabric.bus.feeds;

import fabric.FabricException;
import fabric.ReasonCode;

/**
 * Exception thrown for subscription errors occur (for example, unsubscribe when not subscribed).
 */
public class SubscriptionException extends FabricException {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/*
	 * Class constants
	 */

	private static final long serialVersionUID = -6839062577512012055L;

	/*
	 * Class fields
	 */

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 * 
	 * @param cause
	 *            the cause of the exception.
	 */
	public SubscriptionException(ReasonCode cause, String message) {
		super(cause, message);
	}
}
