/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.feeds.impl;

import java.util.logging.Level;

import fabric.Fabric;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.messages.impl.ServiceMessage;
import fabric.bus.routing.IRouting;
import fabric.core.xml.XML;

/**
 * A Fabric subscription message.
 */
public class SubscriptionMessage extends ServiceMessage {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

	/*
	 * Class constants
	 */

	/*
	 * Class fields
	 */

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 */
	public SubscriptionMessage() {

		super();

		construct();
		setFeedList(new FeedList());

		/* These changes shouldn't be reflected in the instance's "modified" status as this is a new instance */
		metaResetModified();

	}

	/**
	 * Initializes an instance.
	 */
	private void construct() {

		/* Set the service name: i.e. indicate that this is a message for the feed manager */
		setServiceName(FeedManagerService.class.getName());

		/* Indicate that this is a built-in Fabric plug-in */
		setServiceFamilyName(Fabric.FABRIC_PLUGIN_FAMILY);

		/* Indicate that this message should be actioned along the route from subscriber to the publisher */
		setActionEnRoute(true);

	}

	/**
	 * @see fabric.bus.messages.impl.FabricMessage#init(java.lang.String, fabric.core.xml.XML)
	 */
	@Override
	public void init(String element, XML messageXML) throws Exception {

		super.init(element, messageXML);

		/* These changes shouldn't be reflected in the instance's "modified" status as this is a new instance */
		metaResetModified();

	}

	/**
	 * @see fabric.bus.messages.impl.FabricMessage#embed(java.lang.String, fabric.core.xml.XML)
	 */
	@Override
	public void embed(String element, XML messageXML) throws Exception {

		super.embed(element, messageXML);

	}

	/**
	 * Answers the actor ID associated with this message.
	 * 
	 * @return the actor ID.
	 */
	public String getActor() {

		return getProperty(PROPERTY_ACTOR);

	}

	/**
	 * Sets the actor ID associated with this message.
	 * 
	 * @param actor
	 *            the actor ID.
	 */
	public void setActor(String actor) {

		setProperty(PROPERTY_ACTOR, actor);

	}

	/**
	 * Answers the actor platform ID associated with this message.
	 * 
	 * @return the actor platform ID.
	 */
	public String getActorPlatform() {

		return getProperty(PROPERTY_ACTOR_PLATFORM);

	}

	/**
	 * Sets the actor platform ID associated with this message.
	 * 
	 * @param actorPlatform
	 *            the actor platform ID.
	 */
	public void setActorPlatform(String actorPlatform) {

		setProperty(PROPERTY_ACTOR_PLATFORM, actorPlatform);

	}

	/**
	 * Determines the node associated with the publisher.
	 * 
	 * @return the ID of the publisher's node.
	 */
	public String publisherNode() {

		/* To hold the result */
		String publisherNode = null;

		/* If this is a regular subscription (the message flows from subscriber to publisher)... */
		if (getAction().equals(ACTION_SUBSCRIBE)) {

			publisherNode = getRouting().endNode();

		}
		/* Else if this is the restoration of a failed subscription (the message flows from publisher to subscriber)... */
		else if (getAction().equals(ACTION_RESTORE_SUBSCRIPTION)) {

			publisherNode = getRouting().startNode();

		} else {

			logger.log(Level.WARNING,
					"Internal error -- unsupported subscription message type \"{0}\" in method \"{1}\", message: {2}",
					new Object[] {getAction(), "publisherNode()", toString()});

		}

		return publisherNode;

	}

	/**
	 * Determines the node associated with the subscriber.
	 * 
	 * @return the ID of the subscriber's node.
	 */
	public String subscriberNode() {

		/* To hold the result */
		String subscriberNode = null;

		/* If this is a regular subscription (the message flows from subscriber to publisher)... */
		if (getAction().equals(ACTION_SUBSCRIBE)) {

			subscriberNode = getRouting().startNode();

		}
		/* Else if this is the restoration of a failed subscription (the message flows from publisher to subscriber)... */
		else if (getAction().equals(ACTION_RESTORE_SUBSCRIPTION)) {

			subscriberNode = getRouting().endNode();

		} else {

			logger.log(Level.WARNING,
					"Internal error -- unsupported subscription message type \"{0}\" in method \"{1}\", message: {2}",
					new Object[] {getAction(), "subscriberNode()", toString()});

		}

		return subscriberNode;

	}

	/**
	 * Determines if this is the node to which the subscriber is attached.
	 * 
	 * @return <code>true</code> if this is the subscriber's node, <code>false</code> otherwise.
	 */
	public boolean isSubscriberNode() {

		String subscriberNode = subscriberNode();
		boolean isSubscriberNode = homeNode().equals(subscriberNode) || subscriberNode == null;
		return isSubscriberNode;

	}

	/**
	 * Determines if this is the node to which the published is attached.
	 * 
	 * @return <code>true</code> if this is the publisher's node, <code>false</code> otherwise.
	 */
	public boolean isPublisherNode() {

		String publisherNode = publisherNode();
		boolean isPublisherNode = homeNode().equals(publisherNode) || publisherNode == null;
		return isPublisherNode;

	}

	/**
	 * Determines if this is the first node in the route.
	 * 
	 * @return <code>true</code> if this is the first node in the route, <code>false</code> otherwise.
	 */
	public boolean isFirstNode() {

		/* To hold the result */
		boolean isFirstNode = false;

		/* If this is a regular subscription... */
		if (getAction().equals(ACTION_SUBSCRIBE)) {

			isFirstNode = getRouting().previousNode() != null;

		}
		/* Else if this is the restoration of a failed subscription... */
		else if (getAction().equals(ACTION_RESTORE_SUBSCRIPTION)) {

			isFirstNode = getRouting().previousNode() != null;

		} else {

			logger.log(Level.WARNING,
					"Internal error -- unsupported subscription message type \"{0}\" in method \"{1}\", message: {2}",
					new Object[] {getAction(), "isFirstNode()", toString()});

		}

		return isFirstNode;

	}

	/**
	 * Determines the route along which feed messages will flow from the publisher to the subscriber.
	 * <p>
	 * There are two types of subscription message:
	 * <ol>
	 * <li>The initial subscription request sent by the subscriber: this is sent subscriber -> publisher, and therefore
	 * the route for feed messages is the reverse of the route in the subscription message.</li>
	 * <li>An attempt to restore a failed subscription: this is sent publisher -> subscriber, and therefore the route
	 * for feed messages is the route in the subscription message.</li>
	 * </ol>
	 * </p>
	 * 
	 * @return the route for feed messages sent in response to this subscription.
	 */
	public IRouting feedRoute() {

		/* To hold the result */
		IRouting feedRoute = null;

		IRouting subscriptionRouting = getRouting();

		/* If this is a regular subscription (the message flows from subscriber to publisher)... */
		if (getAction().equals(IServiceMessage.ACTION_SUBSCRIBE)) {

			feedRoute = subscriptionRouting.returnRoute();

		}
		/* Else if this is the restoration of a failed subscription (the message flows from publisher to subscriber)... */
		else if (getAction().equals(IServiceMessage.ACTION_RESTORE_SUBSCRIPTION)) {

			feedRoute = subscriptionRouting;

		} else {

			logger.log(Level.WARNING,
					"Internal error -- unsupported subscription message type \"{0}\" in method \"{1}\", message: {2}",
					new Object[] {getAction(), "feedRoute()", toString()});

		}

		return feedRoute;

	}

}
