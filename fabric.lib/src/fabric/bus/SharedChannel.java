/*
 * (C) Copyright IBM Corp. 2007, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus;

import java.io.IOException;

import fabric.core.io.Channel;
import fabric.core.io.ICallback;
import fabric.core.io.InputTopic;
import fabric.core.io.Message;
import fabric.core.io.MessageQoS;
import fabric.core.io.OutputTopic;

/**
 * Class representing a Fabric channel to a node using the <code>fabric.core.io</code> package.
 * <p>
 * The class builds on the basic <code>Channel</code> class by maintaining a channel reference count to allow channel
 * sharing.
 * </p>
 */
public class SharedChannel extends Channel {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2014";

	/*
	 * Class fields
	 */

	/** The target channel */
	private Channel channel = null;

	/** The key by which this channel is referenced */
	private String channelKey = null;

	/** the endpoint with which this channel is associated. */
	private SharedEndPoint endpoint = null;

	/** The number of references to the channel */
	private int refCount = 1;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new channel.
	 * 
	 * @param channel
	 *            the <code>Channel</code> instance to manage.
	 * 
	 * @param channelKey
	 *            the key by which this channel is referenced.
	 * 
	 * @param endpoint
	 *            the endpoint with which this channel is associated.
	 */
	public SharedChannel(Channel instance, String channelKey, SharedEndPoint endpoint) {

		this.channel = instance;
		this.channelKey = channelKey;
		this.endpoint = endpoint;
	}

	/**
	 * Answers the underlying channel managed by this instance.
	 * 
	 * @return the channel.
	 */
	protected Channel channel() {

		return channel;
	}

	/**
	 * Returns the key by which this channel is referenced.
	 * 
	 * @return the channel key.
	 */
	public String channelKey() {

		return channelKey;
	}

	/**
	 * Answers the reference count associated with this channel.
	 * 
	 * @return the reference count.
	 */
	public int refCount() {

		return refCount;
	}

	/**
	 * Sets the reference count associated with this channel.
	 * 
	 * @param refCount
	 *            the new reference count.
	 */
	public void setRefCount(int refCount) {

		this.refCount = (refCount > 0) ? refCount : 0;
	}

	/**
	 * Increments the reference count associated with this channel.
	 * 
	 * @return the new reference count.
	 */
	public int incRefCount() {

		return ++refCount;
	}

	/**
	 * Decrements the reference count associated with this channel.
	 * 
	 * @return the new reference count.
	 */
	public int decRefCount() {

		return (refCount > 0) ? --refCount : 0;
	}

	/**
	 * @see fabric.core.io.Channel#open()
	 */
	@Override
	public void open() throws IOException, UnsupportedOperationException {

		channel.open();
	}

	/**
	 * @see fabric.core.io.Channel#close()
	 */
	@Override
	public void close() throws IOException, UnsupportedOperationException {

		endpoint.closeChannel(this, false);
	}

	/**
	 * @see fabric.core.io.Channel#write(byte[])
	 */
	@Override
	public void write(byte[] message) throws IOException, UnsupportedOperationException {

		channel.write(message);
	}

	/**
	 * @see fabric.core.io.Channel#write(byte[], java.lang.Object)
	 */
	@Override
	public void write(byte[] message, OutputTopic remoteTopic) throws IOException, UnsupportedOperationException {

		channel.write(message, remoteTopic);
	}

	/**
	 * @see fabric.core.io.Channel#write(byte[], java.lang.Object, fabric.core.io.MessageQoS)
	 */
	@Override
	public void write(byte[] message, OutputTopic remoteTopic, MessageQoS qos) throws IOException,
			UnsupportedOperationException {

		channel.write(message, remoteTopic, qos);
	}

	/**
	 * @see fabric.core.io.Channel#read(fabric.core.io.Message)
	 */
	@Override
	public byte[] read(Message replyContainer) throws IOException, UnsupportedOperationException, IllegalStateException {

		return channel.read(replyContainer);
	}

	/**
	 * @see fabric.core.io.Channel#read(fabric.core.io.ICallback)
	 */
	@Override
	public void read(ICallback callback) throws IOException, UnsupportedOperationException {

		channel.read(callback);
	}

	/**
	 * @see fabric.core.io.Channel#inputTopic()
	 */
	@Override
	public InputTopic inputTopic() throws IOException, UnsupportedOperationException {

		return channel.inputTopic();
	}

	/**
	 * @see fabric.core.io.Channel#outputTopic()
	 */
	@Override
	public OutputTopic outputTopic() throws IOException, UnsupportedOperationException {

		return channel.outputTopic();
	}

	/**
	 * @see fabric.core.io.Channel#cancelCallback(fabric.core.io.ICallback)
	 */
	@Override
	public Object cancelCallback(ICallback callback) {

		return channel.cancelCallback(callback);
	}

	/**
	 * @see fabric.core.io.Channel#cancelCallbacks()
	 */
	@Override
	public void cancelCallbacks() {

		channel.cancelCallbacks();
	}
}
