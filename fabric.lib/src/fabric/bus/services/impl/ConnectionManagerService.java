/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.bus.messages.FabricMessageFactory;
import fabric.bus.messages.IClientNotificationMessage;
import fabric.bus.messages.IConnectionMessage;
import fabric.bus.messages.INotificationMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.messages.impl.ConnectionMessage;
import fabric.bus.messages.impl.ServiceMessage;
import fabric.bus.plugins.IPluginConfig;
import fabric.bus.services.IBusServiceConfig;
import fabric.bus.services.IConnectionManager;
import fabric.bus.services.IPersistentService;
import fabric.core.logging.LogUtil;

/**
 * Handles connection/disconnection messages for the Fabric.
 * <p>
 * This service can be used to pre-register Fabric service messages that are to be sent upon receipt of a
 * connection/disconnection message (one of the messages that are automatically sent when a Fabric client connects to,
 * or unexpectedly disconnects from, a Fabric broker) from a node, platform, service, feed or actor. Such messages are
 * used by the Fabric to trigger the correct handling of a connection status change event.
 * </p>
 * <p>
 * A typical action would be to close any active subscriptions associated with a platform that has lost connectivity.
 * </p>
 */
public class ConnectionManagerService extends BusService implements IPersistentService, IConnectionManager {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

	/*
	 * Class static fields
	 */

	/** Action code to remove a registered message. */
	private static final int ACTION_REMOVE = 1;

	/** Action code to fire (send) a registered message. */
	private static final int ACTION_FIRE = 2;

	/*
	 * Class fields
	 */

	/**
	 * The table of connection messages.
	 * <p>
	 * Each entry, keyed by node name, is a list of messages associated with the assets of the node.
	 * </p>
	 */
	private final HashMap<String, ArrayList<MessageRecord>> connectionMessages = new HashMap<String, ArrayList<MessageRecord>>();

	/**
	 * The table of disconnection messages.
	 * <p>
	 * Each entry, keyed by node name, is a list of messages associated with the assets of the node.
	 * </p>
	 */
	private final HashMap<String, ArrayList<MessageRecord>> disconnectionMessages = new HashMap<String, ArrayList<MessageRecord>>();

	/** Table of all messages, keyed by message handle (maintained and used to simplify management operations) */
	private final HashMap<String, MessageRecord> allMessages = new HashMap<String, MessageRecord>();

	/** Flag indicating if connection/disconnection messages are to be actioned */
	private boolean doFireMessages = true;

	/*
	 * Inner classes
	 */

	/**
	 * Class representing a connection message record, i.e. a message to send plus the details of the Fabric asset with
	 * which it is associated.
	 */
	private class MessageRecord {

		/** The type of resource to which the message relates */
		public int resourceType = IConnectionMessage.TYPE_UNKNOWN;

		/** The ID of the node associated with this record (if any). */
		public String node = null;

		/** The ID of the platform associated with this record (if any). */
		public String platform = null;

		/** The ID of the system associated with this record (if any). */
		public String system = null;

		/** The ID of the feed associated with this record (if any). */
		public String feed = null;

		/** The ID of the actor associated with this record (if any). */
		public String actor = null;

		/** The record handle (used to identify the record in management operations). */
		public String handle = null;

		/** The message. */
		public IServiceMessage message = null;

		/**
		 * The type of status change that triggers the sending of this message, one of <code>EVENT_CONNECTED</code> or
		 * <code>EVENT_DISCONNECTED</code>.
		 */
		public int statusType = IConnectionMessage.TYPE_UNKNOWN;

		/** Flag indicating if this message is a single or multiple fire */
		public boolean singleFire = true;

		/**
		 * Constructs a new instance.
		 * 
		 * @param resourceType
		 *            the type of resource to which the record relates.
		 * 
		 * @param node
		 *            the ID of the node associated with this record (required for all resource resource types).
		 *            <code>null</code> indicates all nodes.
		 * 
		 * @param platform
		 *            the ID of the platform (required for platform, system, feed, and actor resource types).
		 *            <code>null</code> indicates all platforms.
		 * 
		 * @param system
		 *            the ID of the system (required for system and feed resource types). <code>null</code> indicates
		 *            all systems.
		 * 
		 * @param feed
		 *            the ID of the feed (required just for feed resource types). <code>null</code> indicates all feeds.
		 * 
		 * @param actor
		 *            the ID of the actor (required just for actor resource types). <code>null</code> indicates all
		 *            actors.
		 * 
		 * @param message
		 *            the message.
		 * 
		 * @param statusType
		 *            the type of status change that triggered the sending of this message, one of
		 *            <code>EVENT_CONNECTED</code> or <code>EVENT_DISCONNECTED</code>.
		 * 
		 * @param singleFire
		 *            flag indicating if this message is a single or multiple fire.
		 */
		public MessageRecord(int resourceType, String node, String platform, String system, String feed, String actor,
				IServiceMessage message, int statusType, boolean singleFire) {

			this.message = (IServiceMessage) message.replicate();
			this.singleFire = singleFire;
			this.statusType = statusType;
			this.handle = FabricMessageFactory.generateUID();
			this.resourceType = resourceType;

			/* Decode the record subject... */
			switch (resourceType) {

			case IConnectionMessage.TYPE_NODE:

				this.node = node;
				break;

			case IConnectionMessage.TYPE_PLATFORM:

				this.node = node;
				this.platform = platform;
				break;

			case IConnectionMessage.TYPE_SERVICE:

				this.node = node;
				this.platform = platform;
				this.system = system;
				break;

			case IConnectionMessage.TYPE_FEED:

				this.node = node;
				this.platform = platform;
				this.system = system;
				this.feed = feed;
				break;

			case IConnectionMessage.TYPE_ACTOR:

				this.node = node;
				this.platform = platform;
				this.actor = actor;
				break;

			default:

				throw new IllegalArgumentException("Unrecognized resource type: " + resourceType);

			}
		}
	}

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 */
	public ConnectionManagerService() {

		super(Logger.getLogger(ConnectionManagerService.class.getPackage().getName()));

	}

	/**
	 * @see fabric.bus.services.impl.BusService#initService(fabric.bus.plugins.IPluginConfig)
	 */
	@Override
	public void initService(IPluginConfig config) {

		super.initService(config);

		/* Determine if connection/disconnection messages are to be actioned */
		String doFireMessagesString = config("fabric.connectionManager.fireActionMessages", "true");
		doFireMessages = Boolean.parseBoolean(doFireMessagesString);

		/* Warn the user if connection/disconnection messages are not being actioned */
		if (!doFireMessages) {
			logger.log(
					Level.WARNING,
					"Connection/disconnection messages will not be actioned (configuration property fabric.connectionManager.fireActionMessages=false)");
		}

		logger.log(Level.FINE, "Service {0} in family {1} initialised", new Object[] {config.getName(),
				config.getFamilyName()});

	}

	/**
	 * @see fabric.bus.services.IService#handleServiceMessage(fabric.bus.messages.IServiceMessage, INotificationMessage,
	 *      IClientNotificationMessage[])
	 */
	@Override
	public IServiceMessage handleServiceMessage(IServiceMessage request, INotificationMessage response,
			IClientNotificationMessage[] clientResponses) throws Exception {

		IConnectionMessage message = (IConnectionMessage) request;

		/* Extract the message details */
		int event = message.getEvent();
		int resourceType = message.getResourceType();
		String node = message.getNode();
		String platform = message.getPlatform();
		String system = message.getService();
		String feed = message.getFeed();
		String actor = message.getActor();

		//this used to be at FINE, DCJ requested change to debug
		logger.log(Level.INFO, "ConnectionManager message: " + "event="
				+ message.getProperty(ConnectionMessage.PROPERTY_EVENT) + " type="
				+ message.getProperty(ConnectionMessage.PROPERTY_RESOURCE_TYPE) + " node=" + node + " feed=" + feed);

		/* Fire the "all node" messages */
		doAction(ACTION_FIRE, resourceType, "", platform, system, feed, actor, event);

		/* If this message is targeted at a specific node... */
		if (node != null) {

			doAction(ACTION_FIRE, resourceType, node, platform, system, feed, actor, event);

		}

		return message;
	}

	/**
	 * Performs the specified action on the specified resource.
	 * 
	 * @param action
	 *            the Connection Manager action to perform.
	 * 
	 * @param resourceType
	 *            the type of resource to which the record relates.
	 * 
	 * @param node
	 *            the ID of the node associated with this record (required for all resource resource types).
	 *            <code>null</code> indicates all nodes.
	 * 
	 * @param platform
	 *            the ID of the platform (required for platform, system, feed, and actor resource types).
	 *            <code>null</code> indicates all platforms.
	 * 
	 * @param system
	 *            the ID of the system (required for system and feed resource types). <code>null</code> indicates all
	 *            systems.
	 * 
	 * @param feed
	 *            the ID of the feed (required just for feed resource types). <code>null</code> indicates all feeds.
	 * 
	 * @param actor
	 *            the ID of the actor (required just for actor resource types). <code>null</code> indicates all actors.
	 * 
	 * @param event
	 *            the type of event that triggered the sending of this message, one of <code>EVENT_CONNECTED</code> or
	 *            <code>EVENT_DISCONNECTED</code>.
	 */
	private void doAction(int action, int resourceType, String node, String platform, String system, String feed,
			String actor, int event) {

		/* Get the message table corresponding to the event type */
		HashMap<String, ArrayList<MessageRecord>> messageTable = messageTable(event);

		/* Get the list of messages for this node */
		ArrayList<MessageRecord> nodeMessages = messageList(node, messageTable);

		/* For each message... */
		for (int i = 0; i < nodeMessages.size(); i++) {

			/* Get the next record message */
			MessageRecord nextRecord = nodeMessages.get(i);

			/* Flag indicating if this message record should be actioned */
			boolean doAction = false;

			/* Decode the resource type... */
			switch (resourceType) {

			case IConnectionMessage.TYPE_ACTOR:

				/* If this is an actor record... */
				if (nextRecord.resourceType == IConnectionMessage.TYPE_ACTOR) {

					/* If this record matches the actor... */
					if (actor == null || nextRecord.actor == null || nextRecord.actor.equals(actor)) {
						doAction = true;
					} else {
						doAction = false;
					}

				}

				break;

			case IConnectionMessage.TYPE_FEED:

				/* If this record matches the feed... */
				if (feed == null || nextRecord.feed == null || nextRecord.feed.equals(feed)) {
					doAction = true;
				} else {
					doAction = false;
					break; // Only break if the feed does not match, otherwise we need to check the system next
				}

			case IConnectionMessage.TYPE_SERVICE:

				/* If this record matches the system... */
				if (system == null || nextRecord.system == null || nextRecord.system.equals(system)) {
					doAction = true;
				} else {
					doAction = false;
					break; // Only break if the system does not match, otherwise we need to check the platform next
				}

			case IConnectionMessage.TYPE_PLATFORM:

				/* If this record matches the platform... */
				if (platform == null || nextRecord.platform == null || nextRecord.platform.equals(platform)) {
					doAction = true;
				} else {
					doAction = false;
				}
				break;

			case IConnectionMessage.TYPE_NODE:

				/* These records are always actioned */
				doAction = true;
				break;

			default:

				String message = format("Internal error: invalid resource type (%d); no action will be taken",
						resourceType);
				logger.log(Level.WARNING, message);
				break;

			}

			/* If this message record is to be actioned... */
			if (doAction) {

				/* Decode the action... */
				switch (action) {

				case ACTION_REMOVE:

					/* Remove the message */
					nodeMessages.remove(i--);
					break;

				case ACTION_FIRE:

					/* If connection/disconnection messages are currently being actioned... */
					if (doFireMessages) {

						IServiceMessage messageToSend = (IServiceMessage) nextRecord.message.replicate();
						messageToSend.setProperty("f:event:event", ServiceMessage.getEventName(event));
						messageToSend.setProperty("f:event:resourceType", ServiceMessage.getResourceName(resourceType));
						messageToSend.setProperty("f:event:node", node);
						messageToSend.setProperty("f:event:platform", platform);
						messageToSend.setProperty("f:event:service", system);
						messageToSend.setProperty("f:event:feed", feed);
						messageToSend.setProperty("f:event:actor", actor);

						/* Fire the message */
						send(messageToSend);

					}

					/* If this is a single fire message... */
					if (nextRecord.singleFire) {

						/* Remove it */
						MessageRecord removed = nodeMessages.remove(i--);

						/* Remove the message from the "all messages" table used for management operations */
						allMessages.remove(removed.handle);

					}

					break;

				}
			}
		}
	}

	/**
	 * Adds a message to the specified list.
	 * 
	 * @param resourceType
	 *            the type of resource to which the record relates.
	 * 
	 * @param node
	 *            the ID of the node associated with this record (required for all resource resource types).
	 *            <code>null</code> indicates all nodes.
	 * 
	 * @param platform
	 *            the ID of the platform (required for platform, system, feed, and actor resource types).
	 *            <code>null</code> indicates all platforms.
	 * 
	 * @param system
	 *            the ID of the system (required for system and feed resource types). <code>null</code> indicates all
	 *            systems.
	 * 
	 * @param feed
	 *            the ID of the feed (required just for feed resource types). <code>null</code> indicates all feeds.
	 * 
	 * @param actor
	 *            the ID of the actor (required just for actor resource types). <code>null</code> indicates all actors.
	 * 
	 * @param statusType
	 *            the type of status change that triggered the sending of this message, one of
	 *            <code>EVENT_CONNECTED</code> or <code>EVENT_DISCONNECTED</code>.
	 * 
	 * @param table
	 *            the table of messages for the node.
	 * 
	 * @param singleFire
	 *            flag indicating if this message is a single or multiple fire.
	 */
	private String addMessage(int resourceType, String node, String platform, String system, String feed, String actor,
			IServiceMessage message, int statusType, boolean singleFire) {

		/* Get the message table corresponding to the status type */
		HashMap<String, ArrayList<MessageRecord>> messageTable = messageTable(statusType);

		/* To hold the record for this message */
		MessageRecord newRecord = null;

		/* If we have a valid message table... */
		if (messageTable != null) {

			/* Get the list of messages for this node */
			ArrayList<MessageRecord> nodeMessages = messageList(node, messageTable);

			/* Record the new message in the list for the specified node */
			newRecord = new MessageRecord(resourceType, node, platform, system, feed, actor, message, statusType,
					singleFire);
			nodeMessages.add(newRecord);

			/* Record the new message in the "all messages" table used for management operations */
			allMessages.put(newRecord.handle, newRecord);

		}

		logger.log(Level.FINER, "Handling message: {0}", newRecord.toString());
		return newRecord.handle;

	}

	/**
	 * Answers the message table corresponding to the status type.
	 * 
	 * @param event
	 *            the type of event that triggered the sending of this message, one of <code>EVENT_CONNECTED</code> or
	 *            <code>EVENT_DISCONNECTED</code>.
	 * 
	 * @return the message table.
	 */
	private HashMap<String, ArrayList<MessageRecord>> messageTable(int event) {

		/* To hold the result */
		HashMap<String, ArrayList<MessageRecord>> messageTable = null;

		/* Decode the status type... */
		switch (event) {

		case IConnectionMessage.EVENT_CONNECTED:

			/* We need the connection message table */
			messageTable = connectionMessages;
			break;

		case IConnectionMessage.EVENT_DISCONNECTED:

			/* We need the disconnection message table */
			messageTable = disconnectionMessages;
			break;

		default:

			String errorMessage = format("Internal error: invalid status type (%d)", event);
			logger.log(Level.WARNING, errorMessage);
			break;

		}

		return messageTable;
	}

	/**
	 * Answers the list of message records for the specified node, from the specified table.
	 * <p>
	 * Note that a new list of messages will be created if one does not already exist.
	 * </p>
	 * 
	 * @param node
	 *            the name of the node.
	 * 
	 * @param table
	 *            the table of messages for the node.
	 * 
	 * @return the list of messages for the specified node.
	 */
	private ArrayList<MessageRecord> messageList(String node, HashMap<String, ArrayList<MessageRecord>> table) {

		/* Ensure that we have a valid node ID */
		node = (node != null) ? node : "";

		/* Get the list of messages for this node */
		ArrayList<MessageRecord> nodeMessages = lookupSublist(node, table);

		return nodeMessages;

	}

	/**
	 * Sends a message onto the bus.
	 * 
	 * @param message
	 *            the message to send.
	 */
	private void send(IServiceMessage message) {

		try {
			/* Push the message onto the bus for processing */
			IBusServiceConfig config = (IBusServiceConfig) serviceConfig();
			config.getFabricServices().sendServiceMessage(message, homeNode());
		} catch (Exception e) {
			/* Not much that we can do about this here, so report the error and move on */
			logger.log(Level.WARNING, "Cannot push message onto bus:\n{0}\n{1}", new Object[] {message,
					LogUtil.stackTrace(e)});
		}
	}

	/**
	 * @see fabric.bus.services.IPersistentService#stopService()
	 */
	@Override
	public void stopService() {

		logger.log(Level.FINE, "Service stopped: {0}", getClass().getName());
	}

	/**
	 * @see fabric.bus.services.IConnectionManager#addNodeMessage(java.lang.String, fabric.bus.messages.IServiceMessage,
	 *      int, boolean)
	 */
	@Override
	public String addNodeMessage(String node, IServiceMessage message, int statusType, boolean singleFire) {

		return addMessage(IConnectionMessage.TYPE_NODE, node, null, null, null, null, message, statusType, singleFire);

	}

	/**
	 * @see fabric.bus.services.IConnectionManager#addPlatformMessage(java.lang.String, java.lang.String,
	 *      fabric.bus.messages.IServiceMessage, int, boolean)
	 */
	@Override
	public String addPlatformMessage(String node, String platform, IServiceMessage message, int statusType,
			boolean singleFire) {

		return addMessage(IConnectionMessage.TYPE_PLATFORM, node, platform, null, null, null, message, statusType,
				singleFire);

	}

	/**
	 * @see fabric.bus.services.IConnectionManager#addServiceMessage(java.lang.String, java.lang.String,
	 *      java.lang.String, fabric.bus.messages.IServiceMessage, int, boolean)
	 */
	@Override
	public String addServiceMessage(String node, String platform, String system, IServiceMessage message,
			int statusType, boolean singleFire) {

		return addMessage(IConnectionMessage.TYPE_SERVICE, node, platform, system, null, null, message, statusType,
				singleFire);

	}

	/**
	 * @see fabric.bus.services.IConnectionManager#addFeedMessage(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, fabric.bus.messages.IServiceMessage, int, boolean)
	 */
	@Override
	public String addFeedMessage(String node, String platform, String system, String feed, IServiceMessage message,
			int statusType, boolean singleFire) {

		return addMessage(IConnectionMessage.TYPE_FEED, node, platform, system, feed, null, message, statusType,
				singleFire);

	}

	/**
	 * @see fabric.bus.services.IConnectionManager#addActorMessage(java.lang.String, java.lang.String, java.lang.String,
	 *      fabric.bus.messages.IServiceMessage, int, boolean)
	 */
	@Override
	public String addActorMessage(String node, String platform, String actor, IServiceMessage message, int statusType,
			boolean singleFire) {

		return addMessage(IConnectionMessage.TYPE_ACTOR, node, platform, null, null, actor, message, statusType,
				singleFire);

	}

	/**
	 * @see fabric.bus.services.IConnectionManager#removeMessage(java.lang.String)
	 */
	@Override
	public void removeMessage(String handle) {

		/* Get the record for the message to be deleted */
		MessageRecord record = allMessages.get(handle);

		/* If this handle is still in use... */
		if (record != null) {

			/* Get the message table corresponding to the status type */
			HashMap<String, ArrayList<MessageRecord>> messageTable = messageTable(record.statusType);

			/* Get the list of messages for this node */
			ArrayList<MessageRecord> nodeMessages = messageList(record.node, messageTable);

			/* For each message... */
			for (int i = 0; i < nodeMessages.size(); i++) {

				/* Get the next message record */
				MessageRecord nextRecord = nodeMessages.get(i);

				/* If we have the required record... */
				if (nextRecord.handle.equals(handle)) {

					/* Remove the message */
					nodeMessages.remove(i--);

					/* Remove the message from the "all messages" table used for management operations */
					allMessages.remove(handle);

					/* All done */
					break;

				}
			}
			logger.log(Level.FINER, "Removed message: {0}", record.toString());

		}
	}

	/**
	 * @see fabric.bus.services.IConnectionManager#removeNodeMessage(java.lang.String, int)
	 */
	@Override
	public void removeNodeMessage(String node, int statusType) {

		doAction(ACTION_REMOVE, IConnectionMessage.TYPE_NODE, node, null, null, null, null, statusType);

	}

	/**
	 * @see fabric.bus.services.IConnectionManager#removePlatformMessage(java.lang.String, java.lang.String, int)
	 */
	@Override
	public void removePlatformMessage(String node, String platform, int statusType) {

		doAction(ACTION_REMOVE, IConnectionMessage.TYPE_PLATFORM, node, platform, null, null, null, statusType);

	}

	/**
	 * @see fabric.bus.services.IConnectionManager#removeServiceMessage(java.lang.String, java.lang.String,
	 *      java.lang.String, int)
	 */
	@Override
	public void removeServiceMessage(String node, String platform, String system, int statusType) {

		doAction(ACTION_REMOVE, IConnectionMessage.TYPE_SERVICE, node, platform, system, null, null, statusType);

	}

	/**
	 * @see fabric.bus.services.IConnectionManager#removeFeedMessage(java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, int)
	 */
	@Override
	public void removeFeedMessage(String node, String platform, String system, String feed, int statusType) {

		doAction(ACTION_REMOVE, IConnectionMessage.TYPE_FEED, node, platform, system, feed, null, statusType);

	}

	/**
	 * @see fabric.bus.services.IConnectionManager#removeActorMessage(java.lang.String, java.lang.String,
	 *      java.lang.String, int)
	 */
	@Override
	public void removeActorMessage(String node, String platform, String actor, int statusType) {

		doAction(ACTION_REMOVE, IConnectionMessage.TYPE_ACTOR, node, platform, null, null, actor, statusType);

	}
}
