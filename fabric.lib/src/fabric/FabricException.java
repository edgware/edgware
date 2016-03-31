/*
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */
package fabric;


/**
 * Exception thrown when Fabric errors occur.
 */
public class FabricException extends Exception {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/*
	 * Class constants
	 */

	private static final long serialVersionUID = 6880534927302972779L;

	/*
	 * Class fields
	 */

	/** The cause of the exception */
	private ReasonCode cause = ReasonCode.UNKNOWN;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 * 
	 * @param cause
	 *            the cause of the exception.
	 */
	public FabricException(ReasonCode cause, String message) {
		super(message);
		this.cause = cause;
	}

	/**
	 * Answers the cause of this exception.
	 * 
	 * @return the cause.
	 */
	public ReasonCode getReason() {
		return cause;
	}
}
