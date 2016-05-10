/*
 * (C) Copyright IBM Corp. 2007, 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.feeds.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.FabricMetric;
import fabric.ServiceDescriptor;
import fabric.TaskServiceDescriptor;
import fabric.bus.IBusServices;
import fabric.bus.feeds.ISubscriptionManager;
import fabric.bus.messages.IClientNotificationMessage;
import fabric.bus.messages.IFeedMessage;
import fabric.bus.messages.INotificationMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.messages.ITaskSubscriptions;
import fabric.bus.messages.impl.ClientNotificationMessage;
import fabric.bus.messages.impl.NotificationMessage;
import fabric.bus.plugins.IFeedPlugin;
import fabric.bus.plugins.IFeedPluginDispatcher;
import fabric.bus.plugins.IPluginConfig;
import fabric.bus.plugins.impl.FeedPluginDispatcher;
import fabric.bus.routing.IRouting;
import fabric.bus.routing.impl.StaticRouting;
import fabric.bus.services.IBusServiceConfig;
import fabric.bus.services.IPersistentService;
import fabric.bus.services.impl.BusService;
import fabric.core.io.MessageQoS;
import fabric.core.logging.FLog;
import fabric.registry.ActorPlugin;
import fabric.registry.FabricRegistry;
import fabric.registry.NodePlugin;
import fabric.registry.QueryScope;
import fabric.registry.Service;
import fabric.registry.TaskPlugin;
import fabric.registry.impl.AbstractFactory;
import fabric.services.messageforwarding.OutboundMessage;

/**
 * Handles subscriptions for the Fabric Manager including:
 * <ul>
 * <li>Subscribe and unsubscribe commands</li>
 * <li>Handling Fabric data feeds (plug-in application, routing, delivery to users, etc.)</li>
 * </ul>
 * Note that this subscription handler only supports <code>StaticRouting</code> for subscription messages.
 *
 */
public class SubscriptionManager extends BusService implements ISubscriptionManager, IPersistentService {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2012";

    /*
     * Class constants
     */

    /** Flag for in-bound plug-ins. */
    private static final String INBOUND = "INBOUND";

    /** Flag for out-bound plug-ins. */
    private static final String OUTBOUND = "OUTBOUND";

    /*
     * Class fields
     */

    /** The table of active subscriptions, keyed by subscription ID (created using the subscriptionID() method). */
    private final HashMap<String, SubscriptionRecord> activeSubscriptionIDs = new HashMap<String, SubscriptionRecord>();

    /**
     * The table of active subscriptions, keyed by feed names, with each entry containing the submap of active
     * subscriptions for the feed.
     */
    private final HashMap<String, ArrayList<SubscriptionRecord>> activeSubscriptions = new HashMap<String, ArrayList<SubscriptionRecord>>();

    /**
     * The count of active subscriptions for each task, keyed by task ID; when the count reaches zero the task plug-ins
     * are stopped.
     */
    private final HashMap<String, Integer> taskSubscriptionCounts = new HashMap<String, Integer>();

    /** Map of QoS settings for Fabric feeds, keyed by feed descriptor. */
    private final HashMap<String, MessageQoS> feedQoS = new HashMap<String, MessageQoS>();

    /** The manager for in-bound per-node plug-ins. */
    private FeedPluginDispatcher inboundNodeDispatcher = null;

    /** The manager for out-bound per-node plug-ins. */
    private FeedPluginDispatcher outboundNodeDispatcher = null;

    /**
     * The table of in-bound per-task plug-in dispatchers, keyed by feed names, with each entry containing the sub-table
     * of in-bound task plug-in dispatchers for the feed.
     */
    private final HashMap<String, HashMap<String, FeedPluginDispatcher>> inboundTaskDispatchers = new HashMap<String, HashMap<String, FeedPluginDispatcher>>();

    /**
     * The table of out-bound per-task plug-in dispatchers, keyed by feed names, with each entry containing the
     * sub-table of out-bound task plug-in dispatchers for the feed.
     */
    private final HashMap<String, HashMap<String, FeedPluginDispatcher>> outboundTaskDispatchers = new HashMap<String, HashMap<String, FeedPluginDispatcher>>();

    /** A local copy of the interface to Fabric management functions. */
    private IBusServices busServices = null;

    /*
     * Inner classes
     */

    /**
     * Data structure holding the information required for processing a single message.
     */
    class FeedHandlingMetaData {

        /* Flag indicating if the message has been modified by per-task or per-actor plug-ins */
        boolean messageModified = false;

        /* Flag indicating if the message is targeted at specific tasks/actors */
        boolean messageIsTargetted = false;

        /* The specific task/actor subscriptions at which this message is targeted */
        ITaskSubscriptions taskSubscriptions = null;

        /* The name of the feed with which this message is associated */
        String feedName = null;

        /* The action resulting from node plug-ins */
        int nodePluginAction = IFeedPlugin.ACTION_CONTINUE;

        /* The action resulting from task plug-ins */
        int taskPluginAction = IFeedPlugin.ACTION_CONTINUE;

        /* The action resulting from actor plug-ins */
        int actorPluginAction = IFeedPlugin.ACTION_CONTINUE;

        /* The list of active subscriptions at which this message is associated */
        ArrayList<SubscriptionRecord> feedSubscriptions = null;

        /* The in-bound task plug-in dispatchers for this feed */
        HashMap<String, FeedPluginDispatcher> inboundFeedTaskDispatchers = null;

        /* The out-bound task plug-in dispatchers for this feed */
        HashMap<String, FeedPluginDispatcher> outboundFeedTaskDispatchers = null;

        /*
         * Table recoding the nodes to which this message is to be sent next. The key for each entry in the table is the
         * node ID, and the value is a sub-table listing the subscriptions (for the current message) that use the node
         * as the next hop en route to the actor. The key for each entry in the sub-table of subscriptions is the string
         * value of the actual message, and the value is the list of subscriptions that need to receive the message.
         */
        HashMap<String, HashMap<IFeedMessage, ArrayList<SubscriptionRecord>>> nodeTable = null;

    }

    /*
     * Class methods
     */

    /**
     * Constructs a new instance.
     */
    public SubscriptionManager() {

        super(Logger.getLogger("fabric.bus.feeds"));

    }

    /**
     * @see fabric.bus.services.impl.BusService#initService(fabric.bus.plugins.IPluginConfig)
     */
    @Override
    public void initService(IPluginConfig config) {

        super.initService(config);

        /* Make a local copy of the accessor for Fabric management services */
        busServices = ((IBusServiceConfig) config).getFabricServices();

        try {

            initNodePlugins();

        } catch (Exception e) {

            logger.log(Level.WARNING, "Cannot initialize node plug-ins: ", e);

        }

    }

    /**
     * @see fabric.bus.services.IService#handleServiceMessage(fabric.bus.messages.IServiceMessage, INotificationMessage,
     *      IClientNotificationMessage[])
     */
    @Override
    public IServiceMessage handleServiceMessage(IServiceMessage serviceMessage, INotificationMessage response,
            IClientNotificationMessage[] clientResponses) throws Exception {

        SubscriptionMessage message = (SubscriptionMessage) serviceMessage;

        /* Get the name of the action to perform */
        String action = message.getAction();

        /* If this is a subscribe command... */
        if (action.equals(IServiceMessage.ACTION_SUBSCRIBE)
                || action.equals(IServiceMessage.ACTION_RESTORE_SUBSCRIPTION)) {

            actionSubscribe(message);

        }
        /* Else if this is a unsubscribe command... */
        else if (action.equals(IServiceMessage.ACTION_UNSUBSCRIBE)) {

            actionUnsubscribe(message);

        } else {

            logger.log(Level.WARNING, "Unrecognized action: {0}", action);

        }

        return message;

    }

    /**
     * @see fabric.bus.services.IPersistentService#stopService()
     */
    @Override
    public void stopService() {

        /* Stop node plug-ins */
        inboundNodeDispatcher.stopDispatcher();

        /* Stop task plug-ins */
        for (Iterator<HashMap<String, FeedPluginDispatcher>> feedTaskDispatchersIterator = inboundTaskDispatchers
                .values().iterator(); feedTaskDispatchersIterator.hasNext();) {

            HashMap<String, FeedPluginDispatcher> taskDispatchersIterator = feedTaskDispatchersIterator.next();

            for (Iterator<FeedPluginDispatcher> dispatchersIterator = taskDispatchersIterator.values().iterator(); dispatchersIterator
                    .hasNext();) {

                FeedPluginDispatcher dispatcher = dispatchersIterator.next();
                dispatcher.stopDispatcher();

            }
        }

        /* Stop actor plug-ins */
        for (Iterator<ArrayList<SubscriptionRecord>> a = activeSubscriptions.values().iterator(); a.hasNext();) {

            ArrayList<SubscriptionRecord> feedSubscriptionList = a.next();

            for (Iterator<SubscriptionRecord> s = feedSubscriptionList.iterator(); s.hasNext();) {

                SubscriptionRecord nextSubscription = s.next();

                IFeedPluginDispatcher nextInboundDispatcher = nextSubscription.inboundActorDispatcher();
                nextInboundDispatcher.stopDispatcher();

                IFeedPluginDispatcher nextOutboundDispatcher = nextSubscription.outboundActorDispatcher();
                nextOutboundDispatcher.stopDispatcher();

            }

        }

        logger.log(Level.FINE, "Service [{0}] stopped", getClass().getName());
    }

    /**
     * @see fabric.bus.feeds.ISubscriptionManager#handleFeed(fabric.bus.messages.IFeedMessage)
     */
    @Override
    public void handleFeed(IFeedMessage nodeMessage) throws Exception {

        FLog.enter(logger, Level.FINER, this, "handleFeed", nodeMessage);

        /* Initialize the data structure used to manage the handling of individual feed messages */
        FeedHandlingMetaData fhmd = new FeedHandlingMetaData();

        /* Get the feed name */
        ServiceDescriptor serviceDescriptor = nodeMessage.metaGetFeedDescriptor();
        fhmd.feedName = serviceDescriptor.toString();

        logger.log(Level.FINEST, "Handling Fabric message from feed [{0}]", fhmd.feedName);

        /* Apply node plug-ins to the message */
        fhmd.nodePluginAction = inboundNodeDispatcher.dispatch(nodeMessage, IFeedPlugin.ACTION_CONTINUE);

        /* If the message is to be processed further... */
        if (fhmd.nodePluginAction == IFeedPlugin.ACTION_CONTINUE) {

            /* Get the list of active subscriptions for the feed associated with this message */
            fhmd.feedSubscriptions = activeSubscriptions.get(fhmd.feedName);

            /* Get the task plug-in dispatchers for this feed */
            fhmd.inboundFeedTaskDispatchers = inboundTaskDispatchers.get(fhmd.feedName);
            fhmd.outboundFeedTaskDispatchers = outboundTaskDispatchers.get(fhmd.feedName);

            /* Get the list of tasks with subscriptions for this feed */
            Iterator<String> tasks = feedTaskList(nodeMessage, fhmd);

            /* If there are any tasks to handle... */
            if (tasks != null) {

                /* Handle the feed message for each task with an active subscription */
                handleFeedForTaskList(nodeMessage, tasks, fhmd);

                /* Forward the message */
                sendFeedMessageToNextHop(fhmd);

            }

        } else {

            /* The message is not to be processed further */
            logger.log(Level.FINEST, "Discarding message from feed [{0}] due to in-bound node plug-in action",
                    fhmd.feedName);

        }

        FLog.exit(logger, Level.FINER, this, "handleFeed", null);
    }

    /**
     * Answers the list of tasks with subscriptions for this feed.
     *
     * @param message
     *            the feed message.
     *
     * @param fhmd
     *            feed handling meta-data for this message.
     *
     * @return the list of tasks.
     */
    private Iterator<String> feedTaskList(IFeedMessage message, FeedHandlingMetaData fhmd) {

        /* Extract the task and actor details from the message header */
        fhmd.taskSubscriptions = message.getSubscriptions();
        fhmd.messageIsTargetted = !fhmd.taskSubscriptions.isEmpty();

        /*
         * Prepare to iterate through the list of tasks, using either the list from the message itself if it is targeted
         * at specific tasks and actors, or the list of all tasks for the feed
         */

        Iterator<String> tasks = null;

        /* If the message is directed at specific tasks and actors... */
        if (fhmd.messageIsTargetted) {
            /* Iterate across the list contained in the message */
            tasks = fhmd.taskSubscriptions.taskIterator();
        }
        /* Else if there are any task dispatchers registered for this feed */
        else if (fhmd.inboundFeedTaskDispatchers != null) {
            /* Iterate across the dispatchers */
            tasks = fhmd.inboundFeedTaskDispatchers.keySet().iterator();
        }

        return tasks;

    }

    /**
     * Handles the processing of a feed message for the specified list of tasks.
     *
     * @param message
     *            the feed message.
     *
     * @param tasks
     *            the list of tasks.
     *
     * @param fhmd
     *            feed handling meta-data for this message.
     *
     * @throws Exception
     */
    private void handleFeedForTaskList(IFeedMessage message, Iterator<String> tasks, FeedHandlingMetaData fhmd)
        throws Exception {

        /*
         * Initialize a table recoding the nodes to which this message is to be sent next. The key for each entry in the
         * table is the node ID, and the value is a sub-table listing the subscriptions (for the current message) that
         * use the node as the next hop en route to the actor. The key for each entry in the sub-table of subscriptions
         * is the string value of the actual message, and the value is the list of subscriptions that need to receive
         * the message.
         */
        fhmd.nodeTable = new HashMap<String, HashMap<IFeedMessage, ArrayList<SubscriptionRecord>>>();

        /* While there are more tasks... */
        while (tasks.hasNext()) {

            /* Get the task name */
            String task = tasks.next();

            /* Task instrumentation */
            FabricMetric taskMetric = null;

            if (doInstrument()) {
                taskMetric = new FabricMetric(homeNode(), task, null, message.metaGetFeedDescriptor(),
                        message.getUID(), message.getOrdinal(), message.toXML().toBytes(), null);
                metrics().startTiming(taskMetric, FabricMetric.EVENT_TASK_PROCESSING_START);
            }

            /* Handle the message */
            handleFeedForTask(message, fhmd, task);

            if (doInstrument()) {
                metrics().endTiming(taskMetric, FabricMetric.EVENT_TASK_PROCESSING_STOP);
            }

        }
    }

    /**
     * Handles the processing of a feed message for the specified task.
     *
     * @param message
     *            the feed message.
     *
     * @param fhmd
     *            feed handling meta-data for this message.
     *
     * @param task
     *            the task.
     *
     * @throws Exception
     */
    private void handleFeedForTask(IFeedMessage message, FeedHandlingMetaData fhmd, String task) throws Exception {

        /* Apply the in-bound task plug-ins to the message */
        IFeedMessage taskMessage = (IFeedMessage) message.replicate();
        FeedPluginDispatcher inboundTaskDispatcher = fhmd.inboundFeedTaskDispatchers.get(task);
        fhmd.taskPluginAction = inboundTaskDispatcher.dispatch(taskMessage, fhmd.nodePluginAction);

        /* If the message is to be processed further... */
        if (fhmd.taskPluginAction == IFeedPlugin.ACTION_CONTINUE) {

            /* Determine if the message was modified by the plug-ins */
            fhmd.messageModified = (fhmd.messageModified == true) ? true : taskMessage.metaIsModified();

            /* If there are any subscriptions for this feed... */
            if (fhmd.feedSubscriptions != null) {

                List<String> actorList = null;

                /* If the message is directed at specific tasks and actors... */
                if (fhmd.messageIsTargetted) {
                    /* Get the list of actors for this message */
                    actorList = fhmd.taskSubscriptions.getActors(task);
                }

                /* Handle the message for each actor */
                handleFeedForActorList(taskMessage, task, actorList, fhmd);

            }

        } else {

            /* The message is not to be processed further */
            logger.log(Level.FINEST, "Discarding message from feed [{0}] due to in-bound task plug-in action",
                    fhmd.feedName);
            fhmd.messageModified = true;

        }
    }

    /**
     * Handles the processing of a feed message for the specified list of actors. If the actor list is <code>null</code>
     * then the message is processed for all subscribed actors.
     * <p>
     * After the per-actor processing has been carried out, then the out-bound task plug-ins are applied, and the
     * message is saved ready to be sent onward.
     * </p>
     *
     * @param taskMessage
     *            the feed message.
     *
     * @param task
     *            the task via which the actors are subscribed.
     *
     * @param actorList
     *            the list of actors.
     *
     * @param fhmd
     *            feed handling meta-data for this message.
     *
     * @throws Exception
     */
    private void handleFeedForActorList(IFeedMessage taskMessage, String task, List<String> actorList,
            FeedHandlingMetaData fhmd) throws Exception {

        /* For each subscription... */
        for (Iterator<SubscriptionRecord> s = fhmd.feedSubscriptions.iterator(); s.hasNext();) {

            /* Get the next subscription */
            SubscriptionRecord nextSubscription = s.next();

            /* If it is for the current task and actor... */
            if (nextSubscription.service().task().equals(task)
                    && (actorList == null || actorList.contains(nextSubscription.actor()))) {

                /* Actor instrumentation */
                FabricMetric actorMetric = null;

                if (doInstrument()) {
                    actorMetric = new FabricMetric(homeNode(), task, null, taskMessage.metaGetFeedDescriptor(),
                            taskMessage.getUID(), taskMessage.getOrdinal(), taskMessage.toXML().toBytes(), null);
                    metrics().startTiming(actorMetric, FabricMetric.EVENT_ACTOR_PROCESSING_START);
                }

                /* Handle the message for this actor */
                IFeedMessage actorMessage = handleFeedForActor(taskMessage, nextSubscription, fhmd);

                if (doInstrument()) {
                    metrics().endTiming(actorMetric, FabricMetric.EVENT_ACTOR_PROCESSING_STOP);
                }

                /* If the message is to be processed further... */
                if (actorMessage != null) {

                    /* Apply the out-bound task plug-ins to the message */
                    FeedPluginDispatcher outboundTaskDispatcher = fhmd.outboundFeedTaskDispatchers.get(task);
                    fhmd.taskPluginAction = outboundTaskDispatcher.dispatch(actorMessage, fhmd.actorPluginAction);

                    /* If the message is to be processed further... */
                    if (fhmd.taskPluginAction == IFeedPlugin.ACTION_CONTINUE) {

                        /* Determine if the message was modified by the plug-ins */
                        fhmd.messageModified = (fhmd.messageModified == true) ? true : actorMessage.metaIsModified();

                        /* Task and actor processing is now complete */
                        addMessageToSendList(nextSubscription, fhmd, actorMessage);

                    } else {

                        /* The message is not to be processed further */
                        logger.log(Level.FINEST,
                                "Discarding message from feed [{0}] due to out-bound task plug-in action",
                                fhmd.feedName);
                        fhmd.messageModified = true;

                    }
                }
            }
        }
    }

    /**
     * Handles the processing of a feed message for the specified actor subscription.
     *
     * @param taskMessage
     *            the feed message.
     *
     * @param fhmd
     *            feed handling meta-data for this message.
     *
     * @param subscription
     *            the details of the actor subscription.
     *
     * @return the feed message after processing.
     *
     * @throws Exception
     */
    private IFeedMessage handleFeedForActor(IFeedMessage taskMessage, SubscriptionRecord subscription,
            FeedHandlingMetaData fhmd) throws Exception {

        /* Apply the in-bound actor plug-ins to the message */
        IFeedMessage actorMessage = (IFeedMessage) taskMessage.replicate();
        fhmd.actorPluginAction = subscription.inboundActorDispatcher().dispatch(actorMessage, subscription.routing(),
                fhmd.taskPluginAction);

        /* If the message is to be processed further... */
        if (fhmd.actorPluginAction == IFeedPlugin.ACTION_CONTINUE) {

            /* Apply the out-bound actor plug-ins to the message */
            fhmd.actorPluginAction = subscription.outboundActorDispatcher().dispatch(actorMessage,
                    subscription.routing(), fhmd.actorPluginAction);

            /* If the message is to be processed further... */
            if (fhmd.taskPluginAction == IFeedPlugin.ACTION_CONTINUE) {

                /* Determine if the message was modified by the plug-ins */
                fhmd.messageModified = (fhmd.messageModified == true) ? true : actorMessage.metaIsModified();

            } else {

                /* The message is not to be processed further */
                logger.log(Level.FINEST, "Discarding message from feed [{0}] due to out-bound actor plug-in action",
                        fhmd.feedName);
                fhmd.messageModified = true;
                actorMessage = null;

            }

        } else {

            /* The message is not to be processed further */
            logger.log(Level.FINEST, "Discarding message from feed [{0}] due to in-bound actor plug-in action",
                    fhmd.feedName);
            fhmd.messageModified = true;
            actorMessage = null;

        }

        return actorMessage;

    }

    /**
     * Records a message ready for sending. The data structures used enable optimized forwarding of messages, ensuring
     * that duplicate messages are not necessarily sent to a node.
     *
     * @param subscription
     *            the subscription associated with this message.
     *
     * @param fhmd
     *            feed handling meta-data for this message.
     *
     * @param message
     *            the message.
     */
    private void addMessageToSendList(SubscriptionRecord subscription, FeedHandlingMetaData fhmd, IFeedMessage message) {

        /*
         * Save the message and the details of this subscription ready for sending
         */

        /* Get the name of the next node (hop) for this message en route to the actor */
        String[] nextNodes = subscription.routing().nextNodes();

        /* If there is no next hop... */
        if (nextNodes == null || nextNodes.length == 0) {

            /* The message has arrived to deliver it to the local actor */
            nextNodes = new String[] {homeNode()};

        }

        /* For each node... */
        for (int n = 0; n < nextNodes.length; n++) {

            /* Get the table of subscriptions involving this node */
            HashMap<String, ArrayList<Subscription>> messageTable = lookupSubmap(nextNodes[n], fhmd.nodeTable);

            /* Get the list of subscriptions that will receive this message */
            ArrayList<SubscriptionRecord> subscriptionList = lookupSublist(message, messageTable);

            /* Add the subscription */
            subscriptionList.add(subscription);

        }
    }

    /**
     * Initializes the node plug-ins for this instance.
     *
     * @throws Exception
     */
    void initNodePlugins() throws Exception {

        /* Initialize in-bound plug-ins */
        inboundNodeDispatcher = nodeDispatcherFactory(INBOUND);

        /* Initialize out-bound plug-ins */
        outboundNodeDispatcher = nodeDispatcherFactory(OUTBOUND);

    }

    /**
     * Factory method to initialize a plug-in dispatcher for node plug-ins.
     *
     * @param type
     *            the type of the plug-in ("<code>inbound</code>" or "<code>outbound</code>").
     *
     * @throws Exception
     */
    private FeedPluginDispatcher nodeDispatcherFactory(String type) throws Exception {

        /* Get the list plug-ins for this node (local query only) */
        String predicate = format("(node_id='%s' or node_id='*') and type='%s' order by ordinal", homeNode(), type);
        NodePlugin[] nodePlugins = FabricRegistry.getNodePluginFactory(QueryScope.LOCAL).getNodePlugins(predicate);

        /* Initialize the dispatcher for this list of plug-ins */
        FeedPluginDispatcher nodeDispatcher = FeedPluginDispatcher.nodePluginFactory(homeNode(), nodePlugins,
                busServices);

        return nodeDispatcher;

    }

    /**
     * Factory method to initialize a plug-in dispatcher for a task/feed/node combination.
     *
     * @param taskFeed
     *            the feed descriptor.
     *
     * @param type
     *            the type of the plug-in ("<code>inbound</code>" or "<code>outbound</code>").
     *
     * @throws Exception
     */
    private FeedPluginDispatcher taskDispatcherFactory(TaskServiceDescriptor taskFeed, String type,
            HashMap<String, HashMap<String, FeedPluginDispatcher>> taskDispatchers) throws Exception {

        ServiceDescriptor feed = new ServiceDescriptor(taskFeed);

        /* Get the list of plug-ins for this task/feed/node combination (local query only) */
        String predicate = format("(task_id='%s' or task_id='*') and (platform_id='%s' or platform_id='*') and "
                + "(service_id='%s' or service_id='*') and " + "(data_feed_id='%s' or data_feed_id='*') and "
                + "(node_id='%s' or node_id='*') and type='%s' order by ordinal", taskFeed.task(), taskFeed.platform(),
                taskFeed.system(), taskFeed.service(), homeNode(), type);
        TaskPlugin[] taskPlugins = FabricRegistry.getTaskPluginFactory(QueryScope.LOCAL).getTaskPlugins(predicate);

        HashMap<String, FeedPluginDispatcher> feedTaskDispatchers = lookupSubmap(feed.toString(), taskDispatchers);
        FeedPluginDispatcher taskDispatcher = feedTaskDispatchers.get(taskFeed.task());

        /* If we haven't loaded the task plug-ins yet... */
        if (taskDispatcher == null) {

            /* Initialize the dispatcher for this list of plug-ins and record it */
            taskDispatcher = FeedPluginDispatcher.taskPluginFactory(homeNode(), taskPlugins, busServices, taskFeed);
            feedTaskDispatchers.put(taskFeed.task(), taskDispatcher);

        }

        return taskDispatcher;

    }

    /**
     * Factory method to initialize a plug-in dispatcher for a actor/task/feed/node combination.
     *
     * @param actor
     *            the actor.
     *
     * @param taskFeed
     *            the feed descriptor.
     *
     * @param type
     *            the type of the plug-in ("<code>inbound</code>" or "<code>outbound</code>").
     *
     * @throws Exception
     */
    private FeedPluginDispatcher actorDispatcherFactory(String actor, TaskServiceDescriptor taskFeed, String type)
        throws Exception {

        /* Get the list of plug-ins for this task/feed/node combination (local query only) */
        String predicate = format("(actor_id='%s' or actor_id='*') and (task_id='%s' or task_id='*') and "
                + "(platform_id='%s' or platform_id='*') and " + "(service_ID='%s' or service_id='*') and "
                + "(data_feed_id='%s' or data_feed_id='*') and (node_id='%s' or node_id='*') "
                + "and type='%s' order by ordinal ", actor, taskFeed.task(), taskFeed.platform(), taskFeed.system(),
                taskFeed.service(), homeNode(), type);
        ActorPlugin[] actorPlugins = FabricRegistry.getActorPluginFactory(QueryScope.LOCAL).getActorPlugins(predicate);

        /* Initialize the dispatcher for this list of plug-ins */
        FeedPluginDispatcher actorDispatcher = FeedPluginDispatcher.actorPluginFactory(homeNode(), actorPlugins,
                busServices, taskFeed, actor);

        return actorDispatcher;

    }

    /**
     * Actions a subscription request message for multiple feeds.
     *
     * @param message
     *            the subscription message.
     *
     * @throws Exception
     */
    private void actionSubscribe(SubscriptionMessage message) throws Exception {

        FLog.enter(logger, Level.FINER, this, "actionSubscribe", message);

        /* Extract the subscription parameters */
        String actor = message.getProperty(IServiceMessage.PROPERTY_ACTOR);
        String actorPlatform = message.getProperty(IServiceMessage.PROPERTY_ACTOR_PLATFORM);

        /* Get the list of feeds */
        ServiceList serviceList = message.getServiceList();
        TaskServiceDescriptor[] feeds = serviceList.getServices();

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Actioning subscription to service(s) [{0}] (actor [{1}], platform [{2}])",
                    new Object[] {FLog.arrayAsString(serviceList.getServices()), actor, actorPlatform});
        }

        /* For each feed... */
        for (int f = 0; f < feeds.length; f++) {

            /* Set up the feed subscription */
            subscribeToFeed(message, actor, actorPlatform, feeds[f]);

        }

        IRouting messageRoute = message.getRouting();

        /* If this is the node to which the subscriber is connected, and it's a multi-hop route... */
        if (message.isSubscriberNode() && !message.isPublisherNode()) {

            /*
             * Set-up timeout handling for this subscription (i.e. the actions to take if no acknowledgment is received
             * to indicate that the subscription was successful)
             */

            int timeout = Integer.parseInt(config("fabric.subscriptionManager.subscriptionTimeout", "120"));

            /* Client notification */

            /* Initialize the notification message */
            IClientNotificationMessage nm = new ClientNotificationMessage();
            nm.setCorrelationID(message.getCorrelationID());
            nm.setEvent(IServiceMessage.EVENT_MESSAGE_TIMEOUT);

            /* Indicate who this message is for */
            nm.setActor(message.getProperty(IServiceMessage.PROPERTY_ACTOR));
            nm.setActorPlatform(message.getProperty(IServiceMessage.PROPERTY_ACTOR_PLATFORM));

            /* Set the service(s) */
            ServiceList serviceListCopy = (ServiceList) serviceList.replicate();
            nm.setServiceList(serviceListCopy);

            /* Register the "subscription timeout" notification */
            busServices.notificationManager().addNotification(nm.getCorrelationID(), nm.getEvent(), nm.getActor(),
                    nm.getActorPlatform(), nm, timeout, false);

            /* Subscription clean-up */

            SubscriptionMessage subscriptionCleanup = (SubscriptionMessage) message.replicate();
            subscriptionCleanup.setAction(IServiceMessage.ACTION_UNSUBSCRIBE);
            subscriptionCleanup.setEvent(IServiceMessage.EVENT_MESSAGE_TIMEOUT);
            busServices.notificationManager().addNotification(subscriptionCleanup.getCorrelationID(),
                    subscriptionCleanup.getEvent(), subscriptionCleanup.getActor(),
                    subscriptionCleanup.getActorPlatform(), subscriptionCleanup, timeout, false);

        }
        FLog.exit(logger, Level.FINER, this, "actionSubscribe", null);
    }

    /**
     * Actions a subscribe command for a specific feed.
     *
     * @param message
     *            the subscription message.
     *
     * @param actor
     *            the name of the feed actor.
     *
     * @param actorPlatform
     *            the ID of the platform via which the actor is connected to the Fabric.
     *
     * @param taskFeed
     *            the feed descriptor.
     *
     * @throws Exception
     * @throws IOException
     */
    private void subscribeToFeed(SubscriptionMessage message, String actor, String actorPlatform,
            TaskServiceDescriptor taskFeed) throws Exception, IOException {

        /* Generate the ID for this subscription */
        ServiceDescriptor feed = new ServiceDescriptor(taskFeed);
        String subscriptionID = subscriptionID(actor, actorPlatform, taskFeed);

        /* If this subscription is not already active... */
        if (!activeSubscriptionIDs.containsKey(subscriptionID)) {

            logger.log(Level.FINE, "New subscription to [{0}]", taskFeed);

            /* Determine the QoS for this feed */
            lookupFeedQoS(feed);

            /* Get the task plug-in dispatchers for this feed/task */
            incrementTaskSubscriptionCount(taskFeed.task());
            taskDispatcherFactory(taskFeed, INBOUND, inboundTaskDispatchers);
            taskDispatcherFactory(taskFeed, OUTBOUND, outboundTaskDispatchers);

            /* Get the actor plug-in dispatcher for this feed/task/actor */
            FeedPluginDispatcher inboundActorDispatcher = actorDispatcherFactory(actor, taskFeed, INBOUND);
            FeedPluginDispatcher outboundActorDispatcher = actorDispatcherFactory(actor, taskFeed, OUTBOUND);

            /* Create and register the clean-up messages to be sent if/when the subscription is lost */
            ArrayList<String> cleanupMessageHandles = registerCleanupMessages(message, taskFeed);

            /* Determine the route for the feed messages associated with this subscription */
            IRouting feedRouting = message.route();

            /* Get the list of active subscriptions for this feed */
            ArrayList<SubscriptionRecord> feedSubscriptions = lookupSublist(feed.toString(), activeSubscriptions);

            /* Create and record a new subscription record */
            SubscriptionRecord subscription = new SubscriptionRecord(actor, actorPlatform, taskFeed, feedRouting,
                    MessageQoS.DEFAULT, inboundActorDispatcher, outboundActorDispatcher, cleanupMessageHandles);
            feedSubscriptions.add(subscription);

            /* Add this subscription to the active list */
            activeSubscriptionIDs.put(subscriptionID, subscription);

        } else {

            logger.log(Level.FINE, "Skipping subscription to [{0}], already subscribed for this actor/platform",
                    taskFeed);

        }
    }

    /**
     * Determines, from the Registry, what the QoS setting is for this feed.
     * <p>
     * This will be recorded as an attribute of the feed in the Registry, of the form:
     *
     * <pre>
     * qos=<em>&lt;value&gt</em>
     * </pre>
     *
     * where <code>&lt;value&gt;</code> is one of:
     * </p>
     * <p>
     * <ul>
     * <li><strong><code>reliable</code>:</strong> messages for this feed will be sent using a reliable (and typically
     * slower) protocol.</li>
     * <li><strong><code>best-effort</code>:</strong> messages for this feed will be sent using a best-effort (not
     * guaranteed, but typically faster) protocol.</li>
     * </ul>
     *
     * @param feed
     *            the feed descriptor.
     */
    private void lookupFeedQoS(ServiceDescriptor feed) {

        /* If the QoS setting for this feed has not already been determined... */
        if (!feedQoS.containsKey(feed)) {

            MessageQoS messageQoS = MessageQoS.DEFAULT;

            /* Get the feed's record from the Registry */
            Service feedRecord = FabricRegistry.getServiceFactory().getServiceById(feed.platform(), feed.system(),
                    feed.service());

            /* If there is a record... */
            if (feedRecord != null) {

                /* Get and unpack the feed's attributes */
                String attributes = feedRecord.getAttributes();
                Map<String, String> attributesMap = AbstractFactory.buildAttributesMap(attributes);

                String qosAttribute = null;

                /* If there is a QoS setting... */
                if (attributesMap != null && (qosAttribute = attributesMap.get("qos")) != null) {

                    messageQoS = (qosAttribute.equals("reliable")) ? MessageQoS.RELIABLE : MessageQoS.BEST_EFFORT;

                }
            }

            /* Save the setting */
            feedQoS.put(feed.toString(), messageQoS);

        }

    }

    /**
     * Builds and registers messages to be sent if the subscription is broken:
     * <ul>
     * <li>Auto-unsubscribe messages to be sent to neighboring nodes (either side of the current node, in the route
     * between the subscriber and publisher).</li>
     * <li>A client notification message to be sent to the subscriber.</li>
     * </ul>
     * <p>
     * This is used by the Fabric to automatically clean-up subscriptions when there is a break in the route to the data
     * feed. Note that we register one clean-up message per feed in the subscription message, not per subscription
     * message.
     * </p>
     *
     * @param message
     *            the subscription message.
     *
     * @param taskFeed
     *            the feed to which the subscription is being made.
     *
     * @return the list of handles for the clean-up messages registered for this subscription with the local connection
     *         manager service.
     */
    private ArrayList<String> registerCleanupMessages(SubscriptionMessage message, TaskServiceDescriptor taskFeed) {

        /* To hold the list of registered message handles */
        ArrayList<String> cleanupMessageHandles = new ArrayList<String>();

        /* Get the routing for the subscription message */
        StaticRouting subscriptionRouting = (StaticRouting) message.getRouting();

        /* Create template clean-up messages */
        SubscriptionMessage downstreamCleanupMessage = createCleanupMessage(message, taskFeed, true);
        SubscriptionMessage upstreamCleanupMessage = createCleanupMessage(message, taskFeed, false);

        String handle = null;

        /* If this is not the last node in the route to the publisher... */
        if (subscriptionRouting.nextNodes().length != 0) {

            String[] nextNodes = subscriptionRouting.nextNodes();

            /* For each node... */
            for (int n = 0; n < nextNodes.length; n++) {

                /*
                 * Register an auto-unsubscribe message to handle unexpected upstream disconnections (i.e. the next
                 * node(s) in the route)
                 */

                logger.log(Level.FINEST, "Registering clean-up for upstream disconnections (next node is [{0}])",
                        nextNodes[n]);

                // handle = busServices.addFeedMessage(nextNodes[n], taskFeed.platform(), taskFeed.system(),
                // taskFeed.service(), downstreamCleanupMessage, IServiceMessage.EVENT_DISCONNECTED, true);
                handle = busServices.addNodeMessage(nextNodes[n], downstreamCleanupMessage,
                        IServiceMessage.EVENT_DISCONNECTED, true);
                cleanupMessageHandles.add(handle);

            }
        }

        /* If this is not the first node in the route to the publisher... */
        if (subscriptionRouting.previousNode() != null) {

            /*
             * Register an auto-unsubscribe message to handle unexpected downstream disconnections (i.e. the next node
             * in the route, on the subscriber side)
             */

            logger.log(Level.FINEST,
                    "Registering clean-up for subscriber-side disconnections (previous node was [{0}]",
                    subscriptionRouting.previousNode());

            // handle = busServices.addFeedMessage(subscriptionRouting.previousNode(), taskFeed.platform(),
            // taskFeed.system(), taskFeed.service(), upstreamCleanupMessage, IServiceMessage.EVENT_DISCONNECTED, true);
            handle = busServices.addNodeMessage(subscriptionRouting.previousNode(), upstreamCleanupMessage,
                    IServiceMessage.EVENT_DISCONNECTED, true);
            cleanupMessageHandles.add(handle);

        }

        /* If this is the node to which the subscriber is attached... */
        if (message.isSubscriberNode()) {

            /* Register an auto-unsubscribe message to handle unexpected the disconnection of the subscriber */

            logger.log(Level.FINEST, "Registering clean-up for subscriber disconnections (subscriber node is [{0}]",
                    message.subscriberNode());

            String actor = message.getProperty(SubscriptionMessage.PROPERTY_ACTOR);
            String actorPlatform = message.getProperty(SubscriptionMessage.PROPERTY_ACTOR_PLATFORM);
            handle = busServices.addActorMessage(homeNode(), actorPlatform, actor, upstreamCleanupMessage,
                    IServiceMessage.EVENT_DISCONNECTED, true);
            cleanupMessageHandles.add(handle);

            /*
             * Make sure that the subscriber is notified when disconnections occur
             */

            /* Build and register the client notification message for the subscriber */
            registerSubscriberNotifications(message, taskFeed);

            /* Build and register the Fabric notification message that will trigger the above to be sent */

            INotificationMessage nm = new NotificationMessage();

            nm.setCorrelationID(message.getCorrelationID());
            nm.setEvent(IServiceMessage.EVENT_SUBSCRIPTION_LOST);

            /* Set the feed */
            TaskServiceDescriptor[] feedAsArray = new TaskServiceDescriptor[] {taskFeed};
            ServiceList serviceList = new ServiceList();
            serviceList.setServices(feedAsArray);
            nm.setServiceList(serviceList);

            handle = busServices.addFeedMessage(homeNode(), taskFeed.platform(), taskFeed.system(), taskFeed.service(),
                    nm, IServiceMessage.EVENT_DISCONNECTED, true);

            cleanupMessageHandles.add(handle);

        }

        /* If this is the node to which the publisher is attached... */
        if (message.isPublisherNode()) {

            /* Register an auto-unsubscribe message to handle unexpected loss of the publisher */

            logger.log(Level.FINEST, "Registering clean-up for publisher disconnections (publisher node was [{0}]",
                    message.publisherNode());

            handle = busServices.addFeedMessage(homeNode(), taskFeed.platform(), taskFeed.system(), taskFeed.service(),
                    downstreamCleanupMessage, IServiceMessage.EVENT_DISCONNECTED, true);
            cleanupMessageHandles.add(handle);

        }

        return cleanupMessageHandles;

    }

    /**
     * Creates and registers client notification messages that are fired if a feed subscription is lost.
     *
     * @param message
     *            the original subscription message.
     *
     * @param taskFeed
     *            the feed descriptor.
     */
    private void registerSubscriberNotifications(SubscriptionMessage message, TaskServiceDescriptor taskFeed) {

        /* Initialize the notification message */
        IClientNotificationMessage nm = new ClientNotificationMessage();
        nm.setCorrelationID(message.getCorrelationID());
        nm.setEvent(IServiceMessage.EVENT_SUBSCRIPTION_LOST);

        /* Indicate who this message is for */
        nm.setActor(message.getProperty(IServiceMessage.PROPERTY_ACTOR));
        nm.setActorPlatform(message.getProperty(IServiceMessage.PROPERTY_ACTOR_PLATFORM));

        /* Set the feed */
        TaskServiceDescriptor[] feedAsArray = new TaskServiceDescriptor[] {taskFeed};
        ServiceList serviceList = new ServiceList();
        serviceList.setServices(feedAsArray);
        nm.setServiceList(serviceList);

        /* Register the "subscription lost" notification */
        busServices.notificationManager().addNotification(nm.getCorrelationID(), taskFeed, nm.getEvent(),
                nm.getActor(), nm.getActorPlatform(), nm, 0, true);

    }

    /**
     * Creates a notification message to be sent to a client during auto-unsubscribe if any loss of connectivity occurs.
     *
     * @param message
     *            the original subscription message.
     *
     * @param taskFeed
     *            the feed for which a clean-up message is required.
     *
     * @param returnRoute
     *            flag indicating if the message will be sent back (<code>true</code>) or forward ( <code>false</code>)
     *            along the original route.
     *
     * @return the template message.
     */
    private SubscriptionMessage createCleanupMessage(SubscriptionMessage message, TaskServiceDescriptor taskFeed,
            boolean reverseRoute) {

        /* Initialize the template unsubscribe message */
        SubscriptionMessage cleanupTemplate = (SubscriptionMessage) message.replicate();
        cleanupTemplate.setAction(IServiceMessage.ACTION_UNSUBSCRIBE);
        cleanupTemplate.setEvent(IServiceMessage.EVENT_SUBSCRIPTION_LOST);

        /* If the route of the original message is to be reversed... */
        if (reverseRoute) {

            IRouting returnRoute = cleanupTemplate.getRouting().returnRoute();
            cleanupTemplate.setRouting(returnRoute);

        }

        /* Set the service list */
        ServiceList unsubscribeFeedList = cleanupTemplate.getServiceList();
        unsubscribeFeedList.setServices(new TaskServiceDescriptor[] {taskFeed});

        return cleanupTemplate;

    }

    /**
     * Actions an unsubscribe command.
     *
     * @param message
     *            the subscription message.
     *
     * @throws Exception
     */
    private void actionUnsubscribe(SubscriptionMessage message) throws Exception {

        /* Extract the unsubscribe parameters */
        String actor = message.getProperty(SubscriptionMessage.PROPERTY_ACTOR);
        String actorPlatform = message.getProperty(SubscriptionMessage.PROPERTY_ACTOR_PLATFORM);

        /* Get the list of feeds */
        ServiceList serviceList = message.getServiceList();
        TaskServiceDescriptor[] services = serviceList.getServices();

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Actioning unsubscription from service(s) [{0}] (actor [{1}], platform [{2}])",
                    new Object[] {FLog.arrayAsString(serviceList.getServices()), actor, actorPlatform});
        }

        /* For each service... */
        for (int s = 0; s < services.length; s++) {

            unsubscribeFromFeed(message.getCorrelationID(), message.getEvent(), actor, actorPlatform, services[s]);

        }

        /* Remove any remaining notifications for this subscription */
        busServices.notificationManager().removeNotifications(message.getCorrelationID());

    }

    /**
     * Actions an unsubscribe command for a specific feed.
     *
     * @param correlationID
     *            the correlation ID for this unsubscribe command.
     *
     * @param event
     *            the event that triggered the unsubscription.
     *
     * @param actor
     *            the name of the actor.
     *
     * @param actorPlatform
     *            the ID of the platform via which the actor is connected to the Fabric.
     *
     * @param taskFeed
     *            the name of the task and feed.
     *
     * @throws IOException
     */
    private void unsubscribeFromFeed(String correlationID, String event, String actor, String actorPlatform,
            TaskServiceDescriptor taskFeed) throws IOException {

        /* Generate the ID for this subscription */
        ServiceDescriptor feed = new ServiceDescriptor(taskFeed);
        String subscriptionID = subscriptionID(actor, actorPlatform, taskFeed);

        /* If this subscription is active... */
        if (activeSubscriptionIDs.containsKey(subscriptionID)) {

            logger.log(Level.FINE, "Stopping subscription to service [{0}]", taskFeed);

            /* Get the list of active subscriptions for this feed */
            ArrayList<SubscriptionRecord> feedSubscriptions = lookupSublist(feed.toString(), activeSubscriptions);

            /* For each subscription... */
            for (int s = 0; s < feedSubscriptions.size(); s++) {

                /* Get the next subscription */
                SubscriptionRecord nextSubscription = feedSubscriptions.get(s);

                /* If this is the subscription we want... */
                if (nextSubscription.actor().equals(actor) && nextSubscription.actorPlatform().equals(actorPlatform)
                        && nextSubscription.service().task().equals(taskFeed.task())) {

                    /* Stop the actor plug-ins */
                    nextSubscription.inboundActorDispatcher().stopDispatcher();
                    nextSubscription.outboundActorDispatcher().stopDispatcher();

                    /* De-register any clean-up messages associated with this subscription */
                    for (Iterator<String> i = nextSubscription.cleanupMessageHandles().iterator(); i.hasNext();) {
                        busServices.removeMessage(i.next());
                    }

                    fireSubscriberNotifications(correlationID, nextSubscription.service(), event);

                    /* Remove the subscription */
                    feedSubscriptions.remove(s);

                    /* Allow for the subscription list shifting down one */
                    s--;

                    /* Finished */
                    break;

                }
            }

            /* If there are no subscriptions left for this task... */
            if (decrementTaskSubscriptionCount(taskFeed.task()) == 0) {

                ArrayList<HashMap<String, FeedPluginDispatcher>> taskDispatchers = new ArrayList<HashMap<String, FeedPluginDispatcher>>();
                taskDispatchers.add(lookupSubmap(feed.toString(), inboundTaskDispatchers));
                taskDispatchers.add(lookupSubmap(feed.toString(), outboundTaskDispatchers));

                for (Iterator<HashMap<String, FeedPluginDispatcher>> i = taskDispatchers.iterator(); i.hasNext();) {

                    /* Get the next task plug-in dispatcher for this feed/task */
                    HashMap<String, FeedPluginDispatcher> feedTaskDispatchers = i.next();
                    FeedPluginDispatcher taskDispatcher = feedTaskDispatchers.get(taskFeed.task());

                    /* Stop the plug-ins */
                    taskDispatcher.stopDispatcher();

                    /* Remove the dispatcher */
                    feedTaskDispatchers.remove(taskFeed.task());

                }

            }

            /* Remove this subscription from the active list */
            activeSubscriptionIDs.remove(subscriptionID);

        } else {

            logger.log(Level.FINE,
                    "Skipping unsubscription from [{0}], no active subscription for this actor/platform", taskFeed);

        }
    }

    /**
     * Fires client notification messages for each feed being unsubscribed.
     *
     * @param correlationID
     *            the correlation ID of the subscription.
     *
     * @param serviceDescriptor
     *            the feed for which notifications are required.
     *
     * @param event
     *            the triggering event.
     */
    private void fireSubscriberNotifications(String correlationID, ServiceDescriptor serviceDescriptor, String event) {

        try {

            busServices.notificationManager().fireNotifications(correlationID, serviceDescriptor, event, null, null);

        } catch (Exception e) {

            logger.log(Level.WARNING,
                    "Cannot fire notifications for correlation ID [{0}], feed [{1}], event [{2}]: {3}", new Object[] {
                    correlationID, serviceDescriptor.toString(), event, e.getMessage()});
            logger.log(Level.FINEST, "Full exception: ", e);

        }

        /* De-register any remaining client notification messages associated with this subscription */
        busServices.notificationManager().removeNotifications(correlationID, serviceDescriptor);

    }

    /**
     * Answers a unique ID for a Fabric feed subscription.
     *
     * @param actor
     *            the name of the feed actor.
     *
     * @param actorPlatform
     *            the ID of the platform via which the actor is connected to the Fabric.
     *
     * @param feed
     *            the name of the feed.
     *
     * @return the subscription ID.
     */
    private String subscriptionID(String actor, String actorPlatform, TaskServiceDescriptor feed) {

        StringBuilder subscriptionID = new StringBuilder();
        subscriptionID.append(actor).append(':').append(actorPlatform).append(':').append(feed);
        return subscriptionID.toString();

    }

    /**
     * Increments the count of active subscriptions for the specified task.
     *
     * @param task
     *            the ID of the task.
     */
    private void incrementTaskSubscriptionCount(String task) {

        /* Get the current count */
        Integer count = taskSubscriptionCounts.get(task);

        /* If there is no count... */
        if (count == null) {
            /* Initialize it */
            count = 1;
        } else {
            /* Increment the current count */
            count++;
        }

        /* Set the new count */
        taskSubscriptionCounts.put(task, count);

    }

    /**
     * Decrements the count of active subscriptions for the specified task.
     *
     * @param task
     *            the ID of the task.
     *
     * @return the current count.
     */
    private int decrementTaskSubscriptionCount(String task) {

        /* Get the current count */
        Integer count = taskSubscriptionCounts.get(task);

        /* If there is a count... */
        if (count != null) {

            /* Decrement it */
            count--;

            /* If there are further subscriptions... */
            if (count != 0) {
                /* Update the count */
                taskSubscriptionCounts.put(task, count);
            } else {
                /* We don't need to worry about this task any more */
                taskSubscriptionCounts.remove(task);
            }

        }

        return count.intValue();

    }

    /**
     * Forwards a message to the next hop(s) in its route to a actor.
     *
     * @param fhmd
     *            feed handling meta-data for this message.
     *
     * @throws Exception
     *             thrown if the message cannot be sent. See the exception detail for more information.
     */
    private void sendFeedMessageToNextHop(FeedHandlingMetaData fhmd) throws Exception {

        /* For each node... */
        for (Iterator<String> n = fhmd.nodeTable.keySet().iterator(); n.hasNext();) {

            /* Get the next node ID */
            String node = n.next();

            /* Get the table of messages for the node */
            HashMap<IFeedMessage, ArrayList<SubscriptionRecord>> messageTable = fhmd.nodeTable.get(node);

            /* If the target node is the current node... */
            if (node.equals(homeNode())) {

                /* The message has arrived -- fulfill local subscriptions */
                sendFeedMessageToActor(messageTable, fhmd);

            } else {

                /* Forward the message across the Fabric to the node */
                forwardFeedMessageToNode(node, messageTable, fhmd);

            }
        }
    }

    /**
     * Forwards a table of messages to the specified node.
     *
     * @param node
     *            the node ID.
     *
     * @param messageTable
     *            the message table.
     *
     * @param fhmd
     *            feed handling meta-data for this message.
     *
     * @throws Exception
     *             thrown if the message cannot be sent. See the exception detail for more information.
     */
    private void forwardFeedMessageToNode(String node,
            HashMap<IFeedMessage, ArrayList<SubscriptionRecord>> messageTable, FeedHandlingMetaData fhmd)
        throws Exception {

        /* For each message to be forwarded to the node... */
        for (Iterator<IFeedMessage> m = messageTable.keySet().iterator(); m.hasNext();) {

            /* Get the message to be sent (which doubles up as the key) */
            IFeedMessage message = m.next();

            /*
             * If the message has been modified by per-task or per-actor processing, or it is targeted at a specific
             * node...
             */
            if (fhmd.messageModified || fhmd.messageIsTargetted) {

                /* Get the list of subscriptions relevant to this message */
                ArrayList<SubscriptionRecord> subscriptionList = messageTable.get(message);

                /* Now add information into the message so that the next hop knows to whom it belongs */

                Iterator<SubscriptionRecord> s = subscriptionList.iterator();

                /* For each subscription associated with the message... */
                for (int i = 0; s.hasNext(); i++) {

                    /* Get the next subscription */
                    SubscriptionRecord subscription = s.next();

                    /* Add the actor to the target list */
                    message.getSubscriptions().addActor(subscription.service().task(), subscription.actor());

                }
            }

            /* Apply the out-bound node plug-ins to the message */
            fhmd.nodePluginAction = outboundNodeDispatcher.dispatch(message, fhmd.taskPluginAction);

            /* If the message is to be processed further... */
            if (fhmd.nodePluginAction == IFeedPlugin.ACTION_CONTINUE) {

                /* Forward the message across the Fabric to the node */
                OutboundMessage outboundMessage = new OutboundMessage(message, node, fhmd.feedName, feedQoS
                        .get(fhmd.feedName));
                busServices.forwardingManager().add(outboundMessage);

            } else {

                /* The message is not to be processed further */
                logger.log(Level.FINEST, "Discarding message from feed [{0}] due to out-bound node plug-in action",
                        fhmd.feedName);

            }
        }
    }

    /**
     * Publish a message to local subscribers.
     *
     * @param messageTable
     *            the table of messages for the node.
     *
     * @param fhmd
     *            feed handling meta-data for this message.
     *
     * @throws Exception
     */
    private void sendFeedMessageToActor(HashMap<IFeedMessage, ArrayList<SubscriptionRecord>> messageTable,
            FeedHandlingMetaData fhmd) throws Exception {

        /* For each message to be published... */
        for (Iterator<IFeedMessage> m = messageTable.keySet().iterator(); m.hasNext();) {

            /* Get the message to be sent (which doubles up as the key) */
            IFeedMessage key = m.next();
            IFeedMessage message = (IFeedMessage) key.replicate();

            /* Apply the out-bound node plug-ins to the message */
            fhmd.nodePluginAction = outboundNodeDispatcher.dispatch(message, fhmd.taskPluginAction);

            /* If the message is to be processed further... */
            if (fhmd.nodePluginAction == IFeedPlugin.ACTION_CONTINUE) {

                /* Get the list of subscriptions relevant to this feed */
                ArrayList<SubscriptionRecord> subscriptionList = messageTable.get(key);

                /* While this message has more subscriptions to process... */
                for (Iterator<SubscriptionRecord> s = subscriptionList.iterator(); s.hasNext();) {

                    /* Get the next subscription */
                    SubscriptionRecord subscription = s.next();

                    /* Queue the message to be sent to the actor */
                    OutboundMessage outboundMessage = new OutboundMessage(message, subscription, fhmd.feedName, feedQoS
                            .get(fhmd.feedName));
                    busServices.forwardingManager().add(outboundMessage);

                }

            } else {

                /* The message is not to be processed further */
                logger.log(Level.FINEST, "Discarding message from feed [{0}] due to out-bound node plug-in action",
                        fhmd.feedName);

            }
        }
    }
}
