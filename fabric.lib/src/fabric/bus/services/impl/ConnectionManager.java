/*
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
import fabric.bus.plugins.IPluginConfig;
import fabric.bus.services.IBusServiceConfig;
import fabric.bus.services.IConnectionManager;
import fabric.bus.services.IPersistentService;

/**
 * Handles connection/disconnection messages for the Fabric.
 * <p>
 * This service can be used to pre-register Fabric service messages that are to be sent upon receipt of a
 * connection/disconnection message (one of the messages that are automatically sent when a Fabric client connects to,
 * or unexpectedly disconnects from, a Fabric broker) from a node, platform, system, service or actor. Such messages are
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
        public String resourceType = IServiceMessage.TYPE_UNKNOWN;

        /** The ID of the node associated with this record (if any). */
        public String node = null;

        /** The ID of the platform associated with this record (if any). */
        public String platform = null;

        /** The ID of the system associated with this record (if any). */
        public String system = null;

        /** The ID of the service associated with this record (if any). */
        public String service = null;

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
        public String event = IServiceMessage.EVENT_UNKNOWN;

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
         *            the ID of the platform (required for platform, system, service, and actor resource types).
         *            <code>null</code> indicates all platforms.
         *
         * @param system
         *            the ID of the system (required for system and service resource types). <code>null</code> indicates
         *            all systems.
         *
         * @param service
         *            the ID of the service (required just for feed resource types). <code>null</code> indicates all
         *            feeds.
         *
         * @param actor
         *            the ID of the actor (required just for actor resource types). <code>null</code> indicates all
         *            actors.
         *
         * @param message
         *            the message.
         *
         * @param event
         *            the type of status change that triggered the sending of this message, one of
         *            <code>EVENT_CONNECTED</code> or <code>EVENT_DISCONNECTED</code>.
         *
         * @param singleFire
         *            flag indicating if this message is a single or multiple fire.
         */
        public MessageRecord(String resourceType, String node, String platform, String system, String service,
                String actor, IServiceMessage message, String event, boolean singleFire) {

            this.message = (IServiceMessage) message.replicate();
            this.singleFire = singleFire;
            this.event = event;
            this.handle = FabricMessageFactory.generateUID();
            this.resourceType = resourceType;

            /* Decode the record subject... */
            switch ((resourceType != null) ? resourceType : "") {

                case IServiceMessage.TYPE_NODE:

                    this.node = node;
                    break;

                case IServiceMessage.TYPE_PLATFORM:

                    this.node = node;
                    this.platform = platform;
                    break;

                case IServiceMessage.TYPE_SYSTEM:

                    this.node = node;
                    this.platform = platform;
                    this.system = system;
                    break;

                case IServiceMessage.TYPE_SERVICE:

                    this.node = node;
                    this.platform = platform;
                    this.system = system;
                    this.service = service;
                    break;

                case IServiceMessage.TYPE_ACTOR:

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

        logger.log(Level.FINE, "Service [{0}] initialised (family [{1}])", new Object[] {config.getName(),
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
        String event = message.getEvent();
        String resourceType = message.getResourceType();
        String node = message.getNode();
        String platform = message.getPlatform();
        String system = message.getSystem();
        String service = message.getService();
        String actor = message.getActor();

        /* TODO: sometimes useful to make this "INFO" for debug */
        logger.log(Level.FINE, "Connection manager message: event [{0}], type [{1}], node [{2}], service [{3}]",
                new Object[] {message.getProperty(ConnectionMessage.PROPERTY_EVENT),
                        message.getProperty(ConnectionMessage.PROPERTY_RESOURCE_TYPE), node, service});

        /* Fire the "all node" messages */
        doAction(ACTION_FIRE, resourceType, "", platform, system, service, actor, event);

        /* If this message is targeted at a specific node... */
        if (node != null) {
            doAction(ACTION_FIRE, resourceType, node, platform, system, service, actor, event);
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
     *            the ID of the platform (required for platform, system, service, and actor resource types).
     *            <code>null</code> indicates all platforms.
     *
     * @param system
     *            the ID of the system (required for system and service resource types). <code>null</code> indicates all
     *            systems.
     *
     * @param service
     *            the ID of the service (required just for service resource types). <code>null</code> indicates all
     *            services.
     *
     * @param actor
     *            the ID of the actor (required just for actor resource types). <code>null</code> indicates all actors.
     *
     * @param event
     *            the type of event that triggered the sending of this message, one of <code>EVENT_CONNECTED</code> or
     *            <code>EVENT_DISCONNECTED</code>.
     */
    private void doAction(int action, String resourceType, String node, String platform, String system, String service,
            String actor, String event) {

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
            switch ((resourceType != null) ? resourceType : "") {

                case IServiceMessage.TYPE_ACTOR:

                    /* If this is an actor record... */
                    if (IServiceMessage.TYPE_ACTOR.equals(nextRecord.resourceType)) {

                        /* If this record matches the actor... */
                        if (actor == null || nextRecord.actor == null || nextRecord.actor.equals(actor)) {
                            doAction = true;
                        } else {
                            doAction = false;
                        }
                    }

                    break;

                case IServiceMessage.TYPE_SERVICE:

                    /* If this record matches the service... */
                    if (service == null || nextRecord.service == null || nextRecord.service.equals(service)) {
                        doAction = true;
                    } else {
                        doAction = false;
                        break; // Only break if the service does not match, otherwise we need to check the system next
                    }

                case IServiceMessage.TYPE_SYSTEM:

                    /* If this record matches the system... */
                    if (system == null || nextRecord.system == null || nextRecord.system.equals(system)) {
                        doAction = true;
                    } else {
                        doAction = false;
                        break; // Only break if the system does not match, otherwise we need to check the platform next
                    }

                case IServiceMessage.TYPE_PLATFORM:

                    /* If this record matches the platform... */
                    if (platform == null || nextRecord.platform == null || nextRecord.platform.equals(platform)) {
                        doAction = true;
                    } else {
                        doAction = false;
                    }
                    break;

                case IServiceMessage.TYPE_NODE:

                    /* These records are always actioned */
                    doAction = true;
                    break;

                default:

                    logger.log(Level.WARNING, "Internal error: invalid resource type ([{0}]); no action will be taken",
                            resourceType);
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
                            messageToSend.setProperty("event:" + IServiceMessage.PROPERTY_EVENT, event);
                            messageToSend.setProperty("event:" + IServiceMessage.PROPERTY_RESOURCE_TYPE, resourceType);
                            messageToSend.setProperty("event:" + IServiceMessage.PROPERTY_NODE, node);
                            messageToSend.setProperty("event:" + IServiceMessage.PROPERTY_PLATFORM, platform);
                            messageToSend.setProperty("event:" + IServiceMessage.PROPERTY_SYSTEM, system);
                            messageToSend.setProperty("event:" + IServiceMessage.PROPERTY_SERVICE, service);
                            messageToSend.setProperty("event:" + IServiceMessage.PROPERTY_ACTOR, actor);

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
     *            the ID of the platform (required for platform, system, service, and actor resource types).
     *            <code>null</code> indicates all platforms.
     *
     * @param system
     *            the ID of the system (required for system and service resource types). <code>null</code> indicates all
     *            systems.
     *
     * @param service
     *            the ID of the service (required just for service resource types). <code>null</code> indicates all
     *            services.
     *
     * @param actor
     *            the ID of the actor (required just for actor resource types). <code>null</code> indicates all actors.
     *
     * @param event
     *            the type of status change that triggered the sending of this message, one of
     *            <code>EVENT_CONNECTED</code> or <code>EVENT_DISCONNECTED</code>.
     *
     * @param table
     *            the table of messages for the node.
     *
     * @param singleFire
     *            flag indicating if this message is a single or multiple fire.
     */
    private String addMessage(String resourceType, String node, String platform, String system, String service,
            String actor, IServiceMessage message, String event, boolean singleFire) {

        /* Get the message table corresponding to the status type */
        HashMap<String, ArrayList<MessageRecord>> messageTable = messageTable(event);

        /* To hold the record for this message */
        MessageRecord newRecord = null;

        /* If we have a valid message table... */
        if (messageTable != null) {

            /* Get the list of messages for this node */
            ArrayList<MessageRecord> nodeMessages = messageList(node, messageTable);

            /* Record the new message in the list for the specified node */
            newRecord = new MessageRecord(resourceType, node, platform, system, service, actor, message, event,
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
    private HashMap<String, ArrayList<MessageRecord>> messageTable(String event) {

        /* To hold the result */
        HashMap<String, ArrayList<MessageRecord>> messageTable = null;

        /* Decode the status type... */
        switch ((event != null) ? event : "") {

            case IServiceMessage.EVENT_CONNECTED:

                /* We need the connection message table */
                messageTable = connectionMessages;
                break;

            case IServiceMessage.EVENT_DISCONNECTED:

                /* We need the disconnection message table */
                messageTable = disconnectionMessages;
                break;

            default:

                String errorMessage = format("Internal error: invalid status type (%s)", event);
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
            logger.log(Level.WARNING, "Cannot push message onto bus: {0}", e.getMessage());
            logger.log(Level.FINEST, "Full exception: ", e);
            logger.log(Level.FINEST, "Full message:\n{0}", message);
        }
    }

    /**
     * @see fabric.bus.services.IPersistentService#stopService()
     */
    @Override
    public void stopService() {

        logger.log(Level.FINE, "Service [{0}] stopped", getClass().getName());
    }

    /**
     * @see fabric.bus.services.IConnectionManager#addNodeMessage(java.lang.String, fabric.bus.messages.IServiceMessage,
     *      java.lang.String, boolean)
     */
    @Override
    public String addNodeMessage(String node, IServiceMessage message, String event, boolean singleFire) {

        return addMessage(IServiceMessage.TYPE_NODE, node, null, null, null, null, message, event, singleFire);

    }

    /**
     * @see fabric.bus.services.IConnectionManager#addPlatformMessage(java.lang.String, java.lang.String,
     *      fabric.bus.messages.IServiceMessage, java.lang.String, boolean)
     */
    @Override
    public String addPlatformMessage(String node, String platform, IServiceMessage message, String event,
            boolean singleFire) {

        return addMessage(IServiceMessage.TYPE_PLATFORM, node, platform, null, null, null, message, event, singleFire);

    }

    /**
     * @see fabric.bus.services.IConnectionManager#addServiceMessage(java.lang.String, java.lang.String,
     *      java.lang.String, fabric.bus.messages.IServiceMessage, java.lang.String, boolean)
     */
    @Override
    public String addServiceMessage(String node, String platform, String system, IServiceMessage message, String event,
            boolean singleFire) {

        return addMessage(IServiceMessage.TYPE_SYSTEM, node, platform, system, null, null, message, event, singleFire);

    }

    /**
     * @see fabric.bus.services.IConnectionManager#addFeedMessage(java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, fabric.bus.messages.IServiceMessage, java.lang.String, boolean)
     */
    @Override
    public String addFeedMessage(String node, String platform, String system, String service, IServiceMessage message,
            String event, boolean singleFire) {

        return addMessage(IServiceMessage.TYPE_SERVICE, node, platform, system, service, null, message, event,
                singleFire);

    }

    /**
     * @see fabric.bus.services.IConnectionManager#addActorMessage(java.lang.String, java.lang.String, java.lang.String,
     *      fabric.bus.messages.IServiceMessage, java.lang.String, boolean)
     */
    @Override
    public String addActorMessage(String node, String platform, String actor, IServiceMessage message, String event,
            boolean singleFire) {

        return addMessage(IServiceMessage.TYPE_ACTOR, node, platform, null, null, actor, message, event,
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
            HashMap<String, ArrayList<MessageRecord>> messageTable = messageTable(record.event);

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
     * @see fabric.bus.services.IConnectionManager#removeNodeMessage(java.lang.String, java.lang.String)
     */
    @Override
    public void removeNodeMessage(String node, String event) {

        doAction(ACTION_REMOVE, IServiceMessage.TYPE_NODE, node, null, null, null, null, event);

    }

    /**
     * @see fabric.bus.services.IConnectionManager#removePlatformMessage(java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void removePlatformMessage(String node, String platform, java.lang.String event) {

        doAction(ACTION_REMOVE, IServiceMessage.TYPE_PLATFORM, node, platform, null, null, null, event);

    }

    /**
     * @see fabric.bus.services.IConnectionManager#removeServiceMessage(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void removeServiceMessage(String node, String platform, String system, String event) {

        doAction(ACTION_REMOVE, IServiceMessage.TYPE_SYSTEM, node, platform, system, null, null, event);

    }

    /**
     * @see fabric.bus.services.IConnectionManager#removeFeedMessage(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void removeFeedMessage(String node, String platform, String system, String service, String event) {

        doAction(ACTION_REMOVE, IServiceMessage.TYPE_SERVICE, node, platform, system, service, null, event);

    }

    /**
     * @see fabric.bus.services.IConnectionManager#removeActorMessage(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void removeActorMessage(String node, String platform, String actor, String event) {

        doAction(ACTION_REMOVE, IServiceMessage.TYPE_ACTOR, node, platform, null, null, actor, event);

    }
}
