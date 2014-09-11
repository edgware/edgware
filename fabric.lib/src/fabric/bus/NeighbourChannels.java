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

import fabric.core.io.ICallback;
import fabric.core.io.InputTopic;
import fabric.core.io.OutputTopic;
import fabric.core.properties.Properties;
import fabric.session.NodeDescriptor;

/**
 * Class managing the channels to a neighbouring Fabric node.
 */
public class NeighbourChannels {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2014";

	/*
	 * Class fields
	 */

	/** The neighbour. */
	private NodeDescriptor neighbourDescriptor = null;

	/** The endpoint to the neighbour. */
	private SharedEndPoint neighbourEndPoint = null;

	/** The channel for Fabric command bus messages. */
	private SharedChannel commandBusChannel = null;

	/** The channel for Fabric feed bus messages. */
	private SharedChannel feedBusChannel = null;

	/** The channel for Fabric Distributed Registry bus messages. */
	private SharedChannel registryBusChannel = null;

	/** The neighbour's Fabric feed bus topic. */
	private OutputTopic outboundFeedBus = null;

	/** The local Fabric command bus topic. */
	private InputTopic inboundCommandBus = null;

	/** The neighbour's Fabric command bus topic. */
	private OutputTopic outboundCommandBus = null;

	/** The neighbour's Fabric Distributed Registry topic. */
	private OutputTopic outboundRegistryBus = null;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 * 
	 * @param neighbourDescriptor
	 *            the ID of the neighbour to which the connection is to be made.
	 * 
	 * @param neighbourEndPoint
	 *            the connection to this neighbour.
	 * 
	 * @param fabricCommandsBusTemplate
	 *            the template topic for Fabric Manager commands to the neighbour.
	 * 
	 * @param fabricFeedsBusTemplate
	 *            the template topic for the Fabric message bus connection to the neighbour.
	 * 
	 * @param fabricRegistryBusTemplate
	 *            the template topic for the Fabric registry bus connection to the neighbour.
	 * 
	 * @param callback
	 *            the handler callback for inbound messages on the channel.
	 * 
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	public NeighbourChannels(NodeDescriptor neighbourDescriptor, SharedEndPoint neighbourEndPoint,
			String fabricCommandsBusTemplate, String fabricFeedsBusTemplate, String fabricRegistryBusTemplate,
			ICallback callback) throws UnsupportedOperationException, IOException {

		this.neighbourDescriptor = neighbourDescriptor;
		this.neighbourEndPoint = neighbourEndPoint;

		/* Open a channel for commands to the neighbour */
		OutputTopic sendNeighbourFMCommands = new OutputTopic(Properties.mergeInserts(fabricCommandsBusTemplate,
				neighbourDescriptor.name()));
		openCommandBusChannel(sendNeighbourFMCommands);

		/* Open a channel to forward Fabric feed messages to the neighbour */
		OutputTopic sendNeighbourBus = new OutputTopic(Properties.mergeInserts(fabricFeedsBusTemplate,
				neighbourDescriptor.name()));
		openFeedBusChannel(sendNeighbourBus, callback);

		/* Open a channel to forward Fabric registry messages to the neighbour */
		OutputTopic registryNeighbourBus = new OutputTopic(Properties.mergeInserts(fabricRegistryBusTemplate,
				neighbourDescriptor.name()));
		openRegistryBusChannel(registryNeighbourBus);
	}

	/**
	 * Opens the Fabric feed bus channel to a neighbour.
	 * 
	 * @param outboundFeedBus
	 *            the neighbour's Fabric feed bus topic.
	 * 
	 * @param callback
	 *            the handler callback for inbound messages on the channel.
	 * 
	 * @return the channel.
	 * 
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	private SharedChannel openFeedBusChannel(OutputTopic outboundFeedBus, ICallback callback)
			throws UnsupportedOperationException, IOException {

		this.outboundFeedBus = outboundFeedBus;
		feedBusChannel = neighbourEndPoint.openOutputChannel(outboundFeedBus);

		return feedBusChannel;
	}

	/**
	 * Closes the Fabric feed bus channel to the neighbour.
	 * 
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	private void closeFeedBusChannel() throws UnsupportedOperationException, IOException {

		neighbourEndPoint.closeChannel(feedBusChannel, false);
		outboundFeedBus = null;
	}

	/**
	 * Answers the channel for Fabric feed bus messages.
	 * 
	 * @return the channel.
	 */
	public SharedChannel feedBusChannel() {

		return feedBusChannel;
	}

	/**
	 * Answers the neighbour's Fabric feed bus topic.
	 * 
	 * @return the topic.
	 */
	public OutputTopic outboundFeedBus() {

		return outboundFeedBus;
	}

	/**
	 * Opens the command Fabric message bus channel to a neighbour.
	 * 
	 * @param outboundCommandBus
	 *            the neighbour's Fabric command bus topic.
	 * 
	 * @return the channel.
	 * 
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	private SharedChannel openCommandBusChannel(OutputTopic outboundCommandBus) throws UnsupportedOperationException,
			IOException {

		this.outboundCommandBus = outboundCommandBus;
		commandBusChannel = neighbourEndPoint.openOutputChannel(outboundCommandBus);
		return commandBusChannel;
	}

	/**
	 * Closes the command Fabric message bus channel to the neighbour.
	 * 
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	private void closeCommandBusChannel() throws UnsupportedOperationException, IOException {

		neighbourEndPoint.closeChannel(commandBusChannel, false);
		outboundCommandBus = null;
	}

	/**
	 * Answers the channel for Fabric command bus messages.
	 * 
	 * @return the channel.
	 */
	public SharedChannel commandBusChannel() {

		return commandBusChannel;
	}

	/**
	 * Answers the local Fabric command bus topic.
	 * 
	 * @return the topic.
	 */
	public InputTopic inboundCommandBus() {

		return inboundCommandBus;
	}

	/**
	 * Answers the neighbour's Fabric command bus topic.
	 * 
	 * @return the topic.
	 */
	public OutputTopic outboundCommandBus() {

		return outboundCommandBus;
	}

	/**
	 * Opens the Fabric registry bus channel to a neighbour.
	 * 
	 * @param outboundRegistryBus
	 *            the neighbour's Fabric registry bus topic.
	 * 
	 * @param callback
	 *            the handler callback for inbound messages on the channel.
	 * 
	 * @return the channel.
	 * 
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	private SharedChannel openRegistryBusChannel(OutputTopic outboundRegistryBus) throws UnsupportedOperationException,
			IOException {

		this.outboundRegistryBus = outboundRegistryBus;
		registryBusChannel = neighbourEndPoint.openOutputChannel(outboundRegistryBus);

		return registryBusChannel;
	}

	/**
	 * Closes the Fabric registry bus channel to the neighbour.
	 * 
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	private void closeRegistryBusChannel() throws UnsupportedOperationException, IOException {

		neighbourEndPoint.closeChannel(registryBusChannel, false);
		outboundRegistryBus = null;
	}

	/**
	 * Answers the channel for Fabric registry bus messages.
	 * 
	 * @return the channel.
	 */
	public SharedChannel registryBusChannel() {

		return registryBusChannel;
	}

	/**
	 * Answers the neighbour's Fabric registry bus topic.
	 * 
	 * @return the topic.
	 */
	public OutputTopic outboundRegistryBus() {

		return outboundRegistryBus;
	}

	/**
	 * Closes the channels associated with this neighbour.
	 * 
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	public void closeChannels() throws UnsupportedOperationException, IOException {

		closeCommandBusChannel();
		closeFeedBusChannel();
		closeRegistryBusChannel();
	}

	/**
	 * Answers the neighbour descriptor.
	 * 
	 * @return the descriptor.
	 */
	public NodeDescriptor neighbourDescriptor() {

		return neighbourDescriptor;
	}

	/**
	 * Answers the SharedEndPoint of the Neighbour.
	 * 
	 * @return the neighbourEndPoint.
	 */
	public SharedEndPoint neighbourEndPoint() {

		return neighbourEndPoint;
	}
}
