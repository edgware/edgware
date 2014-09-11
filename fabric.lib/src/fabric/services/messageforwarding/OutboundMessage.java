/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.messageforwarding;

import fabric.bus.feeds.impl.SubscriptionRecord;
import fabric.bus.messages.IFeedMessage;
import fabric.core.io.MessageQoS;

/**
 * Class representing a Fabric feed message on the queue ready for sending.
 */
public class OutboundMessage {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/*
	 * Class constants
	 */

	/*
	 * Class fields
	 */

	/** The action to be performed for this message (either forward to another node or deliver to an actor). */
	private Action action = Action.UNKNOWN;

	/** The feed from which this message originated. */
	private String descriptor = null;

	/** The network QoS (QoS) to be used with this message. */
	private MessageQoS messageQoS = MessageQoS.DEFAULT;

	/** The node to which this message is to be sent. */
	private String node = null;

	/** The message to be sent. */
	private IFeedMessage message = null;

	/** Details of the actors subscription. */
	private SubscriptionRecord subscription = null;

	/*
	 * Class methods
	 */

	/**
	 * Private default constructor.
	 */
	private OutboundMessage() {

	}

	/**
	 * Constructs a new instance of a message to be sent across the bus (i.e. node to node).
	 * 
	 * @param message
	 *            the message to be sent.
	 * 
	 * @param node
	 *            the node to which this message is to be sent.
	 * 
	 * @param descriptor
	 *            the feed from which this message originated.
	 * 
	 * @param action
	 *            the action to be performed for this message (either forward to another node or deliver to an actor).
	 * 
	 * @param messageQoS
	 *            the network QoS (QoS) to be used with this message.
	 */
	public OutboundMessage(IFeedMessage message, String node, String descriptor, MessageQoS messageQoS) {

		this.action = Action.FORWARD;
		this.message = (IFeedMessage) message.replicate();
		this.node = node;
		this.descriptor = descriptor;
		this.messageQoS = messageQoS;

	}

	/**
	 * Constructs a new instance of a message to be delivered to an actor.
	 * 
	 * @param message
	 *            the message to be sent.
	 * 
	 * @param descriptor
	 *            the feed from which this message originated.
	 * 
	 * @param messageQoS
	 *            the network QoS (QoS) to be used with this message.
	 */
	public OutboundMessage(IFeedMessage message, SubscriptionRecord subscription, String descriptor,
			MessageQoS messageQoS) {

		this.action = Action.DELIVER;
		this.message = (IFeedMessage) message.replicate();
		this.subscription = subscription;
		this.descriptor = descriptor;
		this.messageQoS = messageQoS;

	}

	/**
	 * Answers the action to be performed for this message (either forward to another node or deliver to an actor).
	 * 
	 * @return the action.
	 */
	public Action action() {

		return action;

	}

	/**
	 * Answers the feed from which this message originated.
	 * 
	 * @return the feed descriptor.
	 */
	public String descriptor() {

		return descriptor;

	}

	/**
	 * Answers the network QoS (QoS) to be used with this message.
	 * 
	 * @return the QoS setting.
	 */
	public MessageQoS messageQos() {

		return messageQoS;

	}

	/**
	 * Answers the node to which this message is to be sent.
	 * 
	 * @return the node ID.
	 */
	public String node() {

		return node;

	}

	/**
	 * Answers the message to be sent.
	 * 
	 * @return the message.
	 */
	public IFeedMessage message() {

		return message;

	}

	/**
	 * Answers details of the actors subscription.
	 * 
	 * @return the subscription.
	 */
	public SubscriptionRecord subscription() {

		return subscription;

	}
}