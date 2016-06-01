/*
 * (C) Copyright IBM Corp. 2006, 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.jsonclient;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.FabricBus;
import fabric.ServiceDescriptor;
import fabric.bus.messages.IClientNotificationMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.routing.IRouting;
import fabric.client.FabricPlatform;
import fabric.client.services.IClientNotification;
import fabric.client.services.IClientNotificationHandler;
import fabric.core.io.EndPoint;
import fabric.core.io.IEndPointCallback;
import fabric.core.io.mqtt.MqttConfig;
import fabric.core.json.JSON;
import fabric.core.json.JSONArray;
import fabric.core.logging.FLog;
import fabric.registry.FabricRegistry;
import fabric.registry.QueryScope;
import fabric.registry.SystemFactory;
import fabric.services.jsonclient.articles.Nodes;
import fabric.services.jsonclient.articles.Platforms;
import fabric.services.jsonclient.handler.OperationDispatcher;
import fabric.services.jsonclient.utilities.AdapterConstants;
import fabric.services.jsonclient.utilities.AdapterStatus;
import fabric.services.systems.RuntimeManager;

/**
 * JSON interface for Fabric clients.
 */
public abstract class JSONAdapter extends FabricBus implements IClientNotification, IClientNotificationHandler,
IEndPointCallback {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2006, 2014";

    /*
     * Class constants
     */

    /** The default task ID used by subscriptions from this service. */
    public static final String DEFAULT_TASK = "$def";

    /*
     * Class fields
     */

    /** The adapter's connection to the Fabric. */
    private FabricPlatform fabricPlatform = null;

    /** The adapter's user ID. */
    private String adapterUserID = null;

    /** The adapter's Registry ID. */
    private String adapterPlatformID = null;

    /** The adapter's platform type */
    private String adapterPlatformType = null;

    /** The manager for running systems. */
    private RuntimeManager runtimeManager = null;

    /*
     * Class methods
     */

    public JSONAdapter() {

        this(Logger.getLogger("fabric.services.jsonclient"));
    }

    public JSONAdapter(Logger logger) {

        this.logger = logger;
    }

    /**
     * Initialises the JSON adapter, connecting to the Fabric listening for, and actioning, adapter messages.
     *
     * @throws Exception
     */
    public void init() throws Exception {

        /* Connect to the Fabric */

        if (adapterUserID == null) {
            adapterUserID = MqttConfig.generateClient("JA");
        }

        if (adapterPlatformID == null) {
            adapterPlatformID = MqttConfig.generateClient("JA");
        }

        logger.log(Level.FINE, "JSON adapter user ID [{0}], platform ID [{1}]", new Object[] {adapterUserID,
                adapterPlatformID});

        fabricPlatform = new FabricPlatform(adapterUserID, adapterPlatformID);
        fabricPlatform.connect();
        fabricPlatform.homeNodeEndPoint().register(this);

        /* Register to receive notifications if the home node is lost */
        fabricPlatform.registerClientNotificationCallback(this);

        /* Register this platform */
        if (adapterPlatformType == null) {
            adapterPlatformType = "JSON_ADAPTOR";
        }
        fabricPlatform.registerPlatformType(adapterPlatformType, null, null, null);
        fabricPlatform.registerPlatform(adapterPlatformID, adapterPlatformType);

        /* Register this user ID */
        fabricPlatform.registerActorType("USER", null, null, null);
        fabricPlatform.registerActor(adapterUserID, "USER");

        runtimeManager = new RuntimeManager(fabricPlatform);
        runtimeManager.init();

        /* Passing through the home node */
        Nodes.setNode(fabricPlatform.homeNode());
        Platforms.setNode(fabricPlatform.homeNode());
    }

    /**
     * Restarts pre-registered systems.
     */
    protected void startSystems() {

        if (config("fabric.adapters.json.restartSystems", "true").equals("true")) {

            logger.info("Starting pre-registered systems");

            try {

                SystemFactory sf = FabricRegistry.getSystemFactory(QueryScope.LOCAL);
                fabric.registry.System[] systems = sf.getSystems("attributes like '%%\"autoStart\":\"true\"%%'");

                for (fabric.registry.System s : systems) {

                    try {

                        JSON systemAttr = new JSON(s.getAttributes());
                        String clientID = systemAttr.getString("clientID");

                        /* Build the start message */
                        String opString = String.format(
                                "{\"op\" : \"state:system\", \"id\" : \"%s/%s\", \"state\" : \"running\"}", s
                                .getPlatformId(), s.getId());
                        JSON op = new JSON(opString);

                        /* Start the system */
                        logger.log(Level.INFO, "System [{0}/{1}]: restarting with client ID [{2}]", new Object[] {
                                s.getPlatformId(), s.getId(), clientID});
                        handleAdapterMessage(op, null, clientID);

                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Error restarting service [{0}]: {1}", new Object[] {s.getId(),
                                e.getMessage()});
                    }
                }

            } catch (Exception e) {

                logger.log(Level.WARNING, "Error querying for restartable services: {0}", e.getMessage());

            }

        } else {

            logger.fine("Pre-registered system start not requested");

        }
    }

    public String getAdapterUserID() {
        return adapterUserID;
    }

    public void setAdapterUserID(String adapterUserID) {
        this.adapterUserID = adapterUserID;
    }

    public String getAdapterPlatformID() {
        return adapterPlatformID;
    }

    public void setAdapterPlatformID(String adapterPlatformID) {
        this.adapterPlatformID = adapterPlatformID;
    }

    public String getAdapterPlatformType() {
        return this.adapterPlatformType;
    }

    public void setAdapterPlatformType(String adapterPlatformType) {
        this.adapterPlatformType = adapterPlatformType;
    }

    /**
     * Stops and cleans up the JSON adapter.
     */
    public void stop() {

        runtimeManager.stop();
        fabricPlatform.deregisterActor(adapterUserID);
        fabricPlatform.deregisterPlatform(adapterPlatformID);
    }

    /**
     * Builds a subscription response message.
     *
     * @param outputFeedList
     *            the list of feeds to which subscriptions have been made.
     *
     * @param inputFeed
     *            the input feed to which the subscriptions are mapped.
     *
     * @param correlId
     *            The correlation ID of the request.
     *
     * @return the JSON object containing the response message.
     */
    public static JSON buildSubscriptionResponse(List<ServiceDescriptor> outputFeedList, ServiceDescriptor inputFeed,
            String correlId) {

        JSON subscriptionResponse = new JSON();

        /* Build the list of output feeds */
        List<String> outputFeeds = new ArrayList<String>();
        for (ServiceDescriptor nextFeed : outputFeedList) {
            outputFeeds.add(nextFeed.toString());
        }
        JSONArray outputFeedJSON = new JSONArray();
        outputFeedJSON.putStringList(outputFeeds);

        /* Build the full message */
        subscriptionResponse.putString(AdapterConstants.FIELD_OPERATION, AdapterConstants.OP_SUBSCRIPTIONS);
        subscriptionResponse.putJSONArray(AdapterConstants.FIELD_OUTPUT_FEEDS, outputFeedJSON);
        subscriptionResponse.putString(AdapterConstants.FIELD_INPUT_FEED, inputFeed.toString());
        subscriptionResponse.putString(AdapterConstants.FIELD_CORRELATION_ID, correlId);

        return subscriptionResponse;
    }

    /**
     * Builds a lost-subscription message.
     *
     * @param outputFeedList
     *            the list of feeds to which subscriptions have been lost.
     *
     * @param inputFeed
     *            the input feed to which the subscriptions were mapped.
     *
     * @return the JSON object containing the response message.
     */
    public static JSON buildLostSubscriptionMessage(List<ServiceDescriptor> outputFeedList, ServiceDescriptor inputFeed) {

        JSON lostSubscriptions = new JSON();

        /* Build the list of output feeds */
        List<String> outputFeeds = new ArrayList<String>();
        for (ServiceDescriptor nextFeed : outputFeedList) {
            outputFeeds.add(nextFeed.toString());
        }
        JSONArray outputFeedJSON = new JSONArray();
        outputFeedJSON.putStringList(outputFeeds);

        /* Build the full message */
        lostSubscriptions.putString(AdapterConstants.FIELD_OPERATION, AdapterConstants.OP_LOST_SUBSCRIPTIONS);
        lostSubscriptions.putJSONArray(AdapterConstants.FIELD_OUTPUT_FEEDS, outputFeedJSON);
        lostSubscriptions.putString(AdapterConstants.FIELD_INPUT_FEED, inputFeed.toString());

        return lostSubscriptions;
    }

    /**
     * Answers the name of the class implementing the system adapter proxy for the JSON Fabric client.
     *
     * @return the full class name.
     */
    public abstract String adapterProxy();

    /**
     * Handles a Fabric operation encoded in a JSON object.
     *
     * @param op
     *            the operation.
     *
     * @param correlationID
     *            the operation's correlation ID, or <code>null</code> if none.
     *
     * @param clientID
     *            adapter-specific ID of the client, used to target messages sent to the client.
     *
     * @return the response message.
     */
    public JSON handleAdapterMessage(JSON op, String correlationID, Object clientID) {

        /* To hold the response message (if any) */
        JSON response = null;

        try {

            /* Get the name of the operation */
            String operation = op.getString(AdapterConstants.FIELD_OPERATION).toLowerCase();
            logger.log(Level.FINEST, "Operation: {0}", operation);

            if (operation == null) {

                logger.log(Level.WARNING, "Operation field ([{0}]) missing, ignoring message:\n{1}", new Object[] {
                        AdapterConstants.FIELD_OPERATION, op.toString()});
                AdapterStatus status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_NONE,
                        AdapterConstants.ARTICLE_JSON, AdapterConstants.STATUS_MSG_BAD_OPERATION, correlationID);
                response = status.toJsonObject();

            } else {

                /* Extract the primary operation name */
                String primaryOperation = operation.split(":")[0];

                switch (primaryOperation) {

                    case AdapterConstants.OP_REGISTER:

                        response = OperationDispatcher.registration(operation, op, true, correlationID, clientID);
                        break;

                    case AdapterConstants.OP_DEREGISTER:

                        response = OperationDispatcher.registration(operation, op, false, correlationID, clientID);
                        break;

                    case AdapterConstants.OP_QUERY:
                    case AdapterConstants.OP_SQL_DELETE:
                    case AdapterConstants.OP_SQL_UPDATE:
                    case AdapterConstants.OP_SQL_SELECT:

                        response = OperationDispatcher.sql(operation, op, correlationID);
                        break;

                    case AdapterConstants.OP_STATE:

                        response = OperationDispatcher.stateChange(operation, clientID, op, runtimeManager,
                                adapterProxy(), correlationID);
                        break;

                    case AdapterConstants.OP_SERVICE_REQUEST:
                    case AdapterConstants.OP_SERVICE_RESPONSE:
                    case AdapterConstants.OP_NOTIFY:
                    case AdapterConstants.OP_PUBLISH:
                    case AdapterConstants.OP_SUBSCRIBE:
                    case AdapterConstants.OP_UNSUBSCRIBE:
                    case AdapterConstants.OP_DISCONNECT:

                        response = OperationDispatcher.serviceOperation(operation, op, runtimeManager, correlationID);
                        break;

                    default:

                        AdapterStatus status = new AdapterStatus(AdapterConstants.ERROR_PARSE,
                                AdapterConstants.OP_CODE_NONE, AdapterConstants.ARTICLE_JSON,
                                AdapterConstants.STATUS_MSG_BAD_OPERATION, correlationID);
                        response = status.toJsonObject();
                        break;

                }
            }

        } catch (Exception e) {

            logger.log(Level.FINER, "Exception handling message: {0}", e.getMessage());
            logger.log(Level.FINEST, "Full exception: ", e);
            logger.log(Level.FINEST, "Full message:\n{0}", op.toString());
            AdapterStatus status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_NONE,
                    AdapterConstants.ARTICLE_JSON, AdapterConstants.STATUS_MSG_BAD_JSON + ": " + e.getMessage(),
                    correlationID);
            response = status.toJsonObject();

        }

        return response;

    }

    /**
     * @see fabric.client.services.IClientNotification#homeNodeNotification(fabric.bus.messages.IServiceMessage)
     */
    @Override
    public void homeNodeNotification(final IServiceMessage message) {

        logger.log(Level.FINE, "Change in connectivity status to home node:\n{0}", message.toString());

        if (IServiceMessage.EVENT_CONNECTED.equals(message.getEvent())
                && fabricPlatform.actor().equals(message.getProperty(IServiceMessage.PROPERTY_ACTOR))
                && fabricPlatform.platform().equals(message.getProperty(IServiceMessage.PROPERTY_ACTOR_PLATFORM))
                && homeNode().equals(message.getProperty(IServiceMessage.PROPERTY_NODE))) {

            endPointReconnected(fabricPlatform.homeNodeEndPoint());

        }
    }

    /** @see fabric.client.services.IClientNotification#topologyNotification(fabric.bus.messages.IServiceMessage) */
    @Override
    public void topologyNotification(IServiceMessage message) {

        FLog.enter(logger, Level.FINER, this, "topologyUpdate", message.toString());

        String event = message.getEvent();
        String node = message.getProperty(IServiceMessage.PROPERTY_NODE);

        if (event != null) {

            try {

                switch (event) {

                    case IServiceMessage.EVENT_DISCONNECTED:

                        logger.log(Level.FINE, "Node [{0}] disconnected, pruning its subscriptions", node);
                        runtimeManager.pruneSubscriptions(node);
                        break;

                    case IServiceMessage.EVENT_CONNECTED:

                        logger.log(Level.FINE, "Node [{0}] connected, re-matching all subscriptions", node);
                        runtimeManager.pruneSubscriptions(node);
                        runtimeManager.matchSubscriptions();
                        break;

                    default:

                        logger.log(Level.FINE, "Unexpected event type [{0}] in topology notification message", event);
                        logger.log(Level.FINEST, "Full message:\n", message);
                        break;
                }

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Exception handling connection message for node [{0}]: {1}", new Object[] {
                        node, e.getMessage()});
                logger.log(Level.FINEST, "Full exception: ", e);
            }
        }

        FLog.exit(logger, Level.FINER, this, "topologyUpdate", null);
    }

    /**
     * @see fabric.client.services.IClientNotification#fabricNotification(fabric.bus.messages.IServiceMessage)
     */
    @Override
    public void fabricNotification(IServiceMessage message) {

        FLog.enter(logger, Level.FINER, this, "fabricNotification", message.toString());

        String action = message.getProperty(IServiceMessage.PROPERTY_NOTIFICATION_ACTION);
        String event = message.getEvent();

        switch ((event != null) ? event : "") {

            case IServiceMessage.EVENT_SUBSCRIPTION_LOST:

                IRouting route = message.getRouting();
                String notifyingNode = message.getProperty(IServiceMessage.PROPERTY_NOTIFYING_NODE);
                String startNode = route.startNode();

                if (!homeNode().equals(notifyingNode) && startNode != null && !startNode.equals(homeNode())) {

                    /* Clean up any subscriptions involving this node */
                    try {
                        runtimeManager.pruneSubscriptions(startNode);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Excpetion handling unsubscribe notification node [{0}]: {1}",
                                new Object[] {startNode, e.getMessage()});
                        logger.log(Level.FINEST, "Full exception: ", e);
                    }
                }

                break;

            default:

                break;
        }

        FLog.exit(logger, Level.FINER, this, "fabricNotification", null);
    }

    /**
     * Handles a notification message from the Fabric.
     *
     * @see fabric.client.services.IClientNotificationHandler#handleNotification(fabric.bus.messages.IClientNotificationMessage)
     */
    @Override
    public void handleNotification(final IClientNotificationMessage message) {

        FLog.enter(logger, Level.FINER, this, "handleNotification", message.toString());
        FLog.exit(logger, Level.FINER, this, "handleNotification", null);

    }

    /**
     * @see fabric.core.io.IEndPointCallback#endPointConnected(fabric.core.io.EndPoint)
     */
    @Override
    public void endPointConnected(final EndPoint ep) {

        logger.log(Level.FINEST, "End point connected");
    }

    /**
     * @see fabric.core.io.IEndPointCallback#endPointDisconnected(fabric.core.io.EndPoint)
     */
    @Override
    public void endPointDisconnected(final EndPoint ep) {

        logger.log(Level.FINEST, "End point disconnected");
    }

    /**
     * @see fabric.core.io.IEndPointCallback#endPointReconnected(fabric.core.io.EndPoint)
     */
    @Override
    public void endPointReconnected(final EndPoint ep) {

        logger.log(Level.FINEST, "End point reconnected");
    }

    /**
     * @see fabric.core.io.IEndPointCallback#endPointClosed(fabric.core.io.EndPoint)
     */
    @Override
    public void endPointClosed(final EndPoint ep) {

        logger.log(Level.FINEST, "End point closed");
    }

    /**
     * @see fabric.core.io.IEndPointCallback#endPointLost(fabric.core.io.EndPoint)
     */
    @Override
    public void endPointLost(EndPoint ep) {

        logger.log(Level.FINEST, "End point lost");
    }
}
