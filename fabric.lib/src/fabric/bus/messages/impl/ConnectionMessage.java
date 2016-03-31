/*
 * (C) Copyright IBM Corp. 2009, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.messages.impl;

import fabric.Fabric;
import fabric.bus.messages.FabricMessageFactory;
import fabric.bus.messages.IConnectionMessage;
import fabric.bus.services.impl.ConnectionManagerService;
import fabric.core.xml.XML;

/**
 * Class representing a Fabric connection/disconnection message.
 * <p>
 * This corresponds to the messages that are automatically sent when a Fabric client connects to, or unexpectedly
 * disconnects from, a Fabric broker. Such messages are used by the Fabric to trigger handling of a connection status
 * change event.
 * </p>
 */
public class ConnectionMessage extends ServiceMessage implements IConnectionMessage {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 */
	public ConnectionMessage() {

		super();

		construct(IConnectionMessage.TYPE_UNKNOWN, null, null, null, null, null, IConnectionMessage.EVENT_UNKNOWN);

		/* These changes shouldn't be reflected in the instance's "modified" status as this is a new instance */
		metaResetModified();

	}

	/**
	 * Constructs a new instance.
	 * 
	 * @param node
	 *            the name of the node, or <code>null</code> for all nodes.
	 * 
	 * @param statusType
	 *            the type of status change that will trigger the sending of this message, one of
	 *            <code>EVENT_CONNECTED</code> or <code>EVENT_DISCONNECTED</code>.
	 */
	public ConnectionMessage(String node, int statusType) {

		/* Initialize the message */
		super();
		construct(IConnectionMessage.TYPE_NODE, node, null, null, null, null, statusType);

	}

	/**
	 * Constructs a new instance.
	 * 
	 * @param node
	 *            the name of the node, or <code>null</code> for all nodes.
	 * 
	 * @param platform
	 *            the ID of the platform, or <code>null</code> for all platforms.
	 * 
	 * @param statusType
	 *            the type of status change that will trigger the sending of this message, one of
	 *            <code>EVENT_CONNECTED</code> or <code>EVENT_DISCONNECTED</code>.
	 */
	public ConnectionMessage(String node, String platform, int statusType) {

		/* Initialize the message */
		super();
		construct(IConnectionMessage.TYPE_PLATFORM, node, platform, null, null, null, statusType);

	}

	/**
	 * Constructs a new instance.
	 * 
	 * @param node
	 *            the name of the node, or <code>null</code> for all nodes.
	 * 
	 * @param platform
	 *            the ID of the platform, or <code>null</code> for all platforms.
	 * 
	 * @param system
	 *            the ID of the system, or <code>null</code> for all systems.
	 * 
	 * @param statusType
	 *            the type of status change that will trigger the sending of this message, one of
	 *            <code>EVENT_CONNECTED</code> or <code>EVENT_DISCONNECTED</code>.
	 */
	public ConnectionMessage(String node, String platform, String system, int statusType) {

		/* Initialize the message */
		super();
		construct(IConnectionMessage.TYPE_SERVICE, node, platform, system, null, null, statusType);

	}

	/**
	 * Constructs a new instance.
	 * 
	 * @param node
	 *            the name of the node, or <code>null</code> for all nodes.
	 * 
	 * @param platform
	 *            the ID of the platform, or <code>null</code> for all platforms.
	 * 
	 * @param system
	 *            the ID of the system, or <code>null</code> for all systems.
	 * 
	 * @param feed
	 *            the ID of the feed, or <code>null</code> for all feeds.
	 * 
	 * @param statusType
	 *            the type of status change that will trigger the sending of this message, one of
	 *            <code>EVENT_CONNECTED</code> or <code>EVENT_DISCONNECTED</code>.
	 */
	public ConnectionMessage(String node, String platform, String system, String feed, int statusType) {

		/* Initialize the message */
		super();
		construct(IConnectionMessage.TYPE_FEED, node, platform, system, feed, null, statusType);

	}

	/**
	 * Constructs a new instance.
	 * 
	 * @param statusType
	 *            the type of status change that will trigger the sending of this message, one of
	 *            <code>EVENT_CONNECTED</code> or <code>EVENT_DISCONNECTED</code>.
	 * 
	 * @param node
	 *            the name of the node, or <code>null</code> for all nodes.
	 * 
	 * @param platform
	 *            the ID of the platform, or <code>null</code> for all platforms.
	 * 
	 * @param actor
	 *            the ID of the feed, or <code>null</code> for all actors.
	 */
	public ConnectionMessage(int statusType, String node, String platform, String actor) {

		/* Initialize the message */
		super();
		construct(IConnectionMessage.TYPE_ACTOR, node, platform, null, null, actor, statusType);

	}

	/**
	 * Constructs a new instance.
	 * 
	 * @param resourceType
	 *            the type of resource to which the record relates.
	 * 
	 * @param node
	 *            the ID of the node, or <code>null</code> for all nodes.
	 * 
	 * @param platform
	 *            the ID of the platform, or <code>null</code> for all platforms.
	 * 
	 * @param system
	 *            the ID of the system, or <code>null</code> for all systems.
	 * 
	 * @param feed
	 *            the ID of the feed, or <code>null</code> for all feeds.
	 * 
	 * @param actor
	 *            the ID of the feed, or <code>null</code> for all actors.
	 * 
	 * @param event
	 *            the event that will trigger the sending of this message, one of <code>EVENT_CONNECTED</code> or
	 *            <code>EVENT_DISCONNECTED</code>.
	 */
	public void construct(int resourceType, String node, String platform, String system, String feed, String actor,
			int event) {

		/* Indicate that this is a message for the subscription handler */
		setServiceName(ConnectionManagerService.class.getName());

		/* Indicate that this is a built-in Fabric plug-in */
		setServiceFamilyName(Fabric.FABRIC_PLUGIN_FAMILY);

		/* Configure the message */
		setNotification(false);
		setCorrelationID(FabricMessageFactory.generateUID());
		setResourceType(resourceType);
		setNode(node);
		setPlatform(platform);
		setService(system);
		setFeed(feed);
		setEvent(event);

		setActor(actor);

		if (actor != null) {
			setActorPlatform(platform);
		}

		/* These changes shouldn't be reflected in the instances "modified" status as this is a new instance */
		metaResetModified();

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
	 * @see fabric.bus.messages.IConnectionMessage#getNode()
	 */
	@Override
	public String getNode() {

		return getProperty(ConnectionMessage.PROPERTY_NODE);

	}

	/**
	 * @see fabric.bus.messages.IConnectionMessage#setNode(java.lang.String)
	 */
	@Override
	public void setNode(String node) {

		setProperty(ConnectionMessage.PROPERTY_NODE, node);

	}

	/**
	 * @see fabric.bus.messages.IConnectionMessage#getPlatform()
	 */
	@Override
	public String getPlatform() {

		return getProperty(ConnectionMessage.PROPERTY_PLATFORM);

	}

	/**
	 * @see fabric.bus.messages.IConnectionMessage#setPlatform(java.lang.String)
	 */
	@Override
	public void setPlatform(String platform) {

		setProperty(ConnectionMessage.PROPERTY_PLATFORM, platform);

	}

	/**
	 * @see fabric.bus.messages.IConnectionMessage#getActor()
	 */
	@Override
	public String getActor() {

		return getProperty(ConnectionMessage.PROPERTY_ACTOR);

	}

	/**
	 * @see fabric.bus.messages.IConnectionMessage#setActor(java.lang.String)
	 */
	@Override
	public void setActor(String actor) {

		setProperty(ConnectionMessage.PROPERTY_ACTOR, actor);

	}

	/**
	 * @see fabric.bus.messages.IConnectionMessage#getActorPlatform()
	 */
	@Override
	public String getActorPlatform() {

		return getProperty(ConnectionMessage.PROPERTY_ACTOR_PLATFORM);

	}

	/**
	 * @see fabric.bus.messages.IConnectionMessage#setActorPlatform(java.lang.String)
	 */
	@Override
	public void setActorPlatform(String actorPlatform) {

		setProperty(ConnectionMessage.PROPERTY_ACTOR_PLATFORM, actorPlatform);

	}

	/**
	 * @see fabric.bus.messages.IConnectionMessage#getFeed()
	 */
	@Override
	public String getFeed() {

		return getProperty(ConnectionMessage.PROPERTY_FEED);

	}

	/**
	 * @see fabric.bus.messages.IConnectionMessage#setFeed(java.lang.String)
	 */
	@Override
	public void setFeed(String feed) {

		setProperty(ConnectionMessage.PROPERTY_FEED, feed);

	}

	/**
	 * @see fabric.bus.messages.IConnectionMessage#getService()
	 */
	@Override
	public String getService() {

		return getProperty(ConnectionMessage.PROPERTY_SERVICE);

	}

	/**
	 * @see fabric.bus.messages.IConnectionMessage#setService(java.lang.String)
	 */
	@Override
	public void setService(String service) {

		setProperty(ConnectionMessage.PROPERTY_SERVICE, service);

	}
}
