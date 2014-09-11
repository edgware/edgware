/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2007, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.io;

/**
 * Class representing a message received on an I/O channel.
 */
public class Message {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2014";

	/*
	 * Class fields
	 */

	/** The topic (target) of the message */
	public Object topic = null;

	/** The message body */
	public byte[] data = null;

	/*
	 * Class methods
	 */

	/**
	 * Assigns the values of the specified message to this message.
	 * 
	 * @param source
	 *            the source message.
	 */
	public void set(Message source) {

		this.topic = source.topic;
		this.data = source.data;

	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		byte[] nonNullData = (data != null) ? data : new byte[0];
		String toString = "topic [" + topic + "] data [" + new String(nonNullData) + "]";
		return toString;

	}
}
