/*
 * (C) Copyright IBM Corp. 2007
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.io;

import java.util.logging.Logger;

/**
 * Class representing an I/O end point, i.e. a logical connection to a remote node.
 */
public class IOBase {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007";

	/*
	 * Class constants
	 */

	/** A fabric implemented using pub/sub technology */
	public static final int DOMAIN_PUBSUB = 1;

	/** Pub/sub using the MQTT protocol */
	public static final int PROTOCOL_MQTT = 1;

	/*
	 * Class fields
	 */

	/** The maximum number of messages to buffer when reading from a port */
	protected int bufferLimit = 1;

	protected Logger logger;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 */
	public IOBase() {
		super();
		logger = java.util.logging.Logger.getLogger("fabric.core.io");
	}

	/**
	 * Sets the logger used by this instance.
	 * 
	 * @param logger
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	/**
	 * Sets the maximum size of the buffer for messages received on a port. Note that the limit is a message count not a
	 * byte count. As such, out of memory errors could still occur if both the message size and buffer size are large.
	 * <p>
	 * The behaviour of the fabric when this limit is reached is technology-specific. Messages may be blocked or
	 * discarded.
	 * </p>
	 * <p>
	 * <strong>Note:</strong> the default buffer limit is <code>1</code>.
	 * </p>
	 * 
	 * @param bufferLimit
	 *            the maximum number of <em>messages</em> that will be buffered (a value of <code>0</code> indicates
	 *            unlimited, subject to available memory).
	 */
	public void setBufferLimit(int bufferLimit) {

		this.bufferLimit = bufferLimit;
	}
}