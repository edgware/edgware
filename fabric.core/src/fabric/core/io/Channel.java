/*
 * (C) Copyright IBM Corp. 2007, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.io;

import java.io.IOException;

/**
 * Class representing an I/O channel to an end point.
 */
public abstract class Channel extends IOBase {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2014";

	/*
	 * Class methods
	 */

	/**
	 * Opens this channel.
	 * 
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	public abstract void open() throws IOException, UnsupportedOperationException;

	/**
	 * Closes this channel.
	 * 
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	public abstract void close() throws IOException, UnsupportedOperationException;

	/**
	 * Writes a message to the end point associated with this channel.
	 * 
	 * @param message
	 *            the message data.
	 * 
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	public abstract void write(byte[] message) throws IOException, UnsupportedOperationException;

	/**
	 * Writes a message to the end point associated with this channel.
	 * 
	 * @param message
	 *            the mesage data.
	 * 
	 * @param remoteTopic
	 *            the name of the remote (target) topic.
	 * 
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	public abstract void write(byte[] message, OutputTopic remoteTopic) throws IOException,
			UnsupportedOperationException;

	/**
	 * Writes a message to the end point associated with this channel.
	 * 
	 * @param message
	 *            the message data.
	 * 
	 * @param remoteTopic
	 *            the name of the remote (target) topic.
	 * 
	 * @param qos
	 *            the quality of service required for this message.
	 * 
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	public abstract void write(byte[] message, OutputTopic remoteTopic, MessageQoS qos) throws IOException,
			UnsupportedOperationException;

	/**
	 * Reads the next message from the end point associated with this channel, blocking until the message is available.
	 * 
	 * @param replyContainer
	 *            to hold the reply message.
	 * 
	 * @return the next available message.
	 * 
	 * @throws IOException
	 * 
	 * @throws UnsupportedOperationException
	 * 
	 * @throws IllegalStateException
	 *             thrown if this channel is configured for asynchronous I/O (i.e. one or more callbacks has been
	 *             registered).
	 */
	public abstract byte[] read(Message replyContainer) throws IOException, UnsupportedOperationException,
			IllegalStateException;

	/**
	 * Asynchronously reads the next available message from the end point associated with this channel.
	 * 
	 * @param callback
	 *            invoked when the next message is available.
	 * 
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	public abstract void read(ICallback callback) throws IOException, UnsupportedOperationException;

	/**
	 * Gets the ID of the local (inbound) topic associated with this channel.
	 * 
	 * @return the inbound topic.
	 * 
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	public abstract InputTopic inputTopic() throws IOException, UnsupportedOperationException;

	/**
	 * Gets the ID of the (outbound) topic associated with this channel.
	 * 
	 * @return the outbound topic.
	 * 
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	public abstract OutputTopic outputTopic() throws IOException, UnsupportedOperationException;

	/**
	 * Cancel the specified callback.
	 * 
	 * @param the
	 *            callback.
	 * 
	 * @return the callback, if registered.
	 */
	public abstract Object cancelCallback(ICallback callback);

	/**
	 * Cancel all callbacks.
	 */
	public abstract void cancelCallbacks();
}
