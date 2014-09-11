/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2007, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus;

import java.io.IOException;

import fabric.bus.feeds.impl.SubscriptionRecord;
import fabric.bus.messages.IFeedMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.core.io.MessageQoS;
import fabric.session.NodeDescriptor;

/**
 * Interface for classes managing I/O on the Fabric bus.
 * <p>
 * This includes:
 * <ul>
 * <li>Placing messages onto the bus</li>
 * <li>Moving messages across the bus</li>
 * <li>Pulling messages off of the bus to fulfill user subscriptions</li>
 * </ul>
 */
public interface IBusIO {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2014";

	/*
	 * Class methods
	 */

	/**
	 * Answers the name of the local Fabric node.
	 * 
	 * @return the Fabric Manager name.
	 */
	public String nodeName();

	/**
	 * Gets the connection to the specified neighbour, attempting to create a new one if necessary.
	 * 
	 * @param neighbour
	 *            the name of the neighbouring node to which a connection is required.
	 * 
	 * @return the connection to the neighbour, or <code>null</code> if no connection is currently available.
	 */
	public NeighbourChannels connectNeighbour(String neighbour);

	/**
	 * Answers the I/O channels for the specified neighbour.
	 * 
	 * @param id
	 *            the neighbour ID.
	 * 
	 * @return the channels.
	 */
	public NeighbourChannels neighbourChannels(String id);

	/**
	 * Answers the list of connected neighbours.
	 * 
	 * @return the list.
	 */
	public NodeDescriptor[] connectedNeighbours();

	/**
	 * Close and remove all connections to the specified neighbour.
	 * 
	 * @param id
	 *            the neighbour ID.
	 * 
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 */
	public void disconnectNeighbour(String id) throws UnsupportedOperationException, IOException;

	/**
	 * Close and remove the connection to the specified neighbour. Will then try and reconnect again on any remaining
	 * available interface.
	 * 
	 * @param nodeDescriptor
	 *            the neighbour ID.
	 * 
	 * @return <code>null</code> if no retry, or retry unsuccessful; otherwise the I/O channels for the neighbour.
	 * 
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 */
	public NeighbourChannels disconnectNeighbour(NodeDescriptor nodeDescriptor, boolean doRetry)
			throws UnsupportedOperationException, IOException;

	/**
	 * Wraps a message received from a locally connected system in a Fabric message envelope.
	 * 
	 * @param messageData
	 *            the message to republish (becomes the payload of the Fabric feed message).
	 * 
	 * @param isReplay
	 *            flag indicating if this is a replay (<code>true</code>) or original (<code>false</code>) message.
	 * 
	 * @return the wrapped message.
	 * 
	 * @throws IOException
	 *             thrown if the message cannot be published.
	 */
	public IFeedMessage wrapRawMessage(byte[] messageData, boolean isReplay) throws IOException;

	/**
	 * Wraps a message received from a locally connected system in a Fabric message envelope, and publishes it to the
	 * Fabric bus for processing and delivery.
	 * 
	 * @param fullTopic
	 *            the full topic name upon which the message was received.
	 * 
	 * @param messageData
	 *            the message to republish (becomes the payload of the Fabric feed message).
	 * 
	 * @param isReplay
	 *            flag indicating if this is a replay (<code>true</code>) or original (<code>false</code>) message.
	 * 
	 * @throws IOException
	 *             thrown if the message cannot be published.
	 */
	public void sendRawMessage(String fullTopic, byte[] messageData, boolean isReplay) throws IOException;

	/**
	 * Sends a Fabric <em>service</em> message across the bus to the specified Fabric node.
	 * 
	 * @param message
	 *            the message to forward.
	 * 
	 * @param node
	 *            the target node.
	 * 
	 * @throws Exception
	 *             thrown if the message cannot be forwarded.
	 */
	public void sendServiceMessage(IServiceMessage message, String node) throws Exception;

	/**
	 * Sends a Fabric <em>service</em> message across the bus to the specified Fabric nodes.
	 * 
	 * @param message
	 *            the message to forward.
	 * 
	 * @param nodes
	 *            the target nodes.
	 * 
	 * @throws Exception
	 *             thrown if the message cannot be forwarded.
	 */
	public void sendServiceMessage(IServiceMessage message, String[] nodes) throws Exception;

	/**
	 * Sends a feed message across the bus to the specified Fabric node.
	 * 
	 * @param node
	 *            the ID of the node.
	 * 
	 * @param feedTopic
	 *            the topic associated with the feed.
	 * 
	 * @param message
	 *            the message to send.
	 * 
	 * @param qos
	 *            the message MessageQoS setting.
	 * 
	 * @throws Exception
	 *             thrown if the message cannot be forwarded.
	 */
	public void sendFeedMessage(String node, String feedTopic, IFeedMessage message, MessageQoS qos) throws Exception;

	/**
	 * Delivers a Fabric message to a locally connected (to the current Fabric node) client; i.e. send the message to
	 * its the final destination.
	 * 
	 * @param feedTopic
	 *            the topic associated with the feed.
	 * 
	 * @param message
	 *            the message to be sent.
	 * 
	 * @param subscription
	 *            the details of the subscription being fulfilled.
	 * 
	 * @param qos
	 *            the message QoS setting.
	 * 
	 * @throws Exception
	 *             thrown if the message cannot be delivered.
	 */
	public void deliverFeedMessage(String feedTopic, IFeedMessage message, SubscriptionRecord subscription,
			MessageQoS qos) throws Exception;

	/**
	 * Gets the data structure holding the local channels and topics used by this instance.
	 * 
	 * @return the local channels and topics.
	 */
	public BusIOChannels ioChannels();
}
