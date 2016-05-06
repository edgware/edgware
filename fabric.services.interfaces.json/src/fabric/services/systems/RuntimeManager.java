/*
 * (C) Copyright IBM Corp. 2010, 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.systems;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.Fabric;
import fabric.FabricBus;
import fabric.ServiceDescriptor;
import fabric.SystemDescriptor;
import fabric.TaskServiceDescriptor;
import fabric.bus.feeds.ISubscription;
import fabric.bus.feeds.ISubscriptionCallback;
import fabric.bus.feeds.impl.Subscription;
import fabric.bus.messages.IFeedMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.client.FabricPlatform;
import fabric.core.logging.FLog;
import fabric.registry.FabricRegistry;
import fabric.registry.Node;
import fabric.registry.NodeFactory;
import fabric.registry.QueryScope;
import fabric.registry.Service;
import fabric.registry.ServiceFactory;
import fabric.registry.System;
import fabric.registry.SystemFactory;
import fabric.registry.exception.RegistryQueryException;
import fabric.services.json.JSON;
import fabric.services.json.JSONArray;
import fabric.services.jsonclient.JSONAdapter;

/**
 * Manages a set of active systems.
 */
public class RuntimeManager extends FabricBus implements ISubscriptionCallback {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010, 2014";

    /*
     * Class static fields
     */

    /** The descriptor for the Registry update notification service. */
    private static final TaskServiceDescriptor registryUpdatesDescriptor = new TaskServiceDescriptor("$def", "$fab",
            "$reg", "$updates");

    /** Fabric class instance used to access utility methods only. */
    private static final Fabric fabric = new Fabric();

    /*
     * Class fields
     */

    /**
     * To hold the list of active systems, including both those in the running and stopped states.
     */
    private final HashMap<SystemDescriptor, SystemRuntime> activeSystems = new HashMap<SystemDescriptor, SystemRuntime>();

    /** To hold the list of subscription requests for active systems. */
    private final HashMap<ServiceDescriptor, List<ServiceDescriptor>> systemSubscriptionRequests = new HashMap<ServiceDescriptor, List<ServiceDescriptor>>();

    /** Flag indicating if Registry queries should be local or distributed. */
    private QueryScope queryScope = QueryScope.DISTRIBUTED;

    /** The connection to the Fabric. */
    private FabricPlatform fabricClient = null;

    /** The subscription to Registry update notifications. */
    private ISubscription registryUpdates = null;

    /** Flag indicating if Registry updates should be actioned. */
    private boolean actionTopologyUpdates = true;

    /** Object used to protect access to wiring code. */
    private Object wiringLock = new Object();

    /** Object used to protect access to refresh thread control flag. */
    private Object refreshLock = new Object();

    /** Flag indicating if the refresh thread should exit. */
    private boolean doRefresh = true;

    /*
     * Inner classes
     */

    /**
     * Class implementing a periodic-refresh thread.
     */
    private class Refresh implements Runnable {

        /**
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {

            String action = "prune";
            String intervalConfig = null;
            int interval = 0;

            try {
                intervalConfig = fabric.config("fabric.runtimeManager.refreshInterval", "-1");
                interval = Integer.parseInt(intervalConfig);
            } catch (Exception e) {
                interval = 6;
                logger.log(
                        Level.WARNING,
                        "Invalid value configured for Runtime Manager refresh interval ([{0}]); using default value ({1})",
                        new Object[] {intervalConfig, interval});
            }

            logger.log(Level.INFO, "Runtime Manager refresh thread running");

            while (interval != -1) {

                try {
                    Thread.sleep(interval * 1000);
                } catch (InterruptedException e) {
                }

                synchronized (refreshLock) {

                    if (doRefresh) {

                        synchronized (wiringLock) {

                            switch (action) {

                                case "prune":

                                    logger.log(Level.FINE, "Pruning subscriptions for lost nodes");

                                    for (String n : missingRouteNodes()) {

                                        try {

                                            pruneSubscriptions(n);

                                        } catch (Exception e) {

                                            logger.log(
                                                    Level.WARNING,
                                                    "Exception cleaning up broken subscription using missing node [{0}]: {1}",
                                                    new Object[] {n, e.getMessage()});
                                            logger.log(Level.FINEST, "Full exception:", e);

                                        }
                                    }

                                    action = "match";
                                    break;

                                case "match":

                                    logger.log(Level.FINE, "Re-matching subscriptions");

                                    try {

                                        matchSubscriptions();

                                    } catch (Exception e) {

                                        logger.log(Level.WARNING, "Exception in subscription refresh: {0}", e
                                                .getMessage());
                                        logger.log(Level.FINEST, "Full exception:", e);

                                    }

                                    action = "prune";
                                    break;
                            }
                        }

                    } else {

                        break;

                    }
                }
            }
            logger.log(Level.INFO, "Runtime Manager refresh thread stopped");
        }

        /**
         * Determines the list of nodes that are used in an active subscription, but which are not currently visible (as
         * determined by a distributed node query).
         *
         * @return the list of missing nodes.
         */
        protected List<String> missingRouteNodes() {

            List<String> missingRouteNodes = new ArrayList<String>();

            if (!activeSystems.isEmpty()) {

                /* Get the list of nodes known to the Fabric */
                NodeFactory nf = FabricRegistry.getNodeFactory(QueryScope.DISTRIBUTED);
                Node[] visibleNodes = nf.getAllNodes();
                List<String> visibleNodeList = new ArrayList<String>();
                for (Node n : visibleNodes) {
                    visibleNodeList.add(n.getId());
                }

                /* For each active system... */
                for (SystemRuntime sys : activeSystems.values()) {

                    /* Get the list of feed subscriptions for this system */
                    HashMap<ServiceDescriptor, ISubscription> wif = sys.wiredInputFeeds();

                    /* For each subscription... */
                    for (ISubscription sub : wif.values()) {

                        /* For each node used in the route for this subscription... */
                        for (String routeNode : sub.route()) {

                            /* If the node is not visible and hasn't been recorded already... */
                            if (!visibleNodeList.contains(routeNode) && !missingRouteNodes.contains(routeNode)) {
                                missingRouteNodes.add(routeNode);
                            }
                        }
                    }
                }
            }
            return missingRouteNodes;
        }
    }

    /*
     * Class methods
     */

    public RuntimeManager() {

        this(Logger.getLogger("fabric.services.systems"));
    }

    public RuntimeManager(Logger logger) {

        this.logger = logger;
    }

    /**
     * Creates a new instance.
     *
     * @param fabricClient
     *            the connection to the Fabric.
     */
    public RuntimeManager(FabricPlatform fabricClient) {

        this.fabricClient = fabricClient;

        /* Determine if Registry queries should be local or distributed */
        boolean doQueryLocal = Boolean.parseBoolean(config("fabric.runtimeManager.queryLocal", "true"));
        this.queryScope = doQueryLocal ? QueryScope.LOCAL : QueryScope.DISTRIBUTED;

    }

    /**
     * Initialises this instance.
     */
    public void init() {

        try {

            /* Subscribe to Registry update notifications */
            registryUpdates = new Subscription(fabricClient);
            registryUpdates.subscribe(registryUpdatesDescriptor, this);

            /* Determine if topology updates should be actioned */
            String config = null;
            try {
                config = fabric.config("fabric.runtimeManager.actionTopologyUpdates", "true");
                actionTopologyUpdates = Boolean.parseBoolean(config);
            } catch (Exception e) {
                actionTopologyUpdates = true;
                logger.log(
                        Level.WARNING,
                        "Invalid value configured for Runtime Manager Registry update monitor ([{0}]); using default value ({1})",
                        new Object[] {config, actionTopologyUpdates});
            }

        } catch (Exception e) {

            logger.log(Level.WARNING,
                    "Cannot subscribe to service [{0}]; active subscription functions will not be available: {1}",
                    new Object[] {registryUpdatesDescriptor, e.getMessage()});
            logger.log(Level.FINEST, "Full exception: ", e);

        }

        /* Start periodic subscription refreshes */
        (new Thread(new Refresh(), "Runtime-Manager-Refresh")).start();
    }

    /**
     * Stops this instance.
     */
    public void stop() {

        try {

            /* Stop periodic subscription refreshes */
            synchronized (refreshLock) {
                doRefresh = false;
            }

            /* Unsubscribe from Registry update notifications */
            registryUpdates.unsubscribe();

        } catch (Exception e) {

            logger.log(Level.WARNING, "Cannot unsubscribe from feed [{0}]: {1}", new Object[] {
                    registryUpdatesDescriptor, e.getMessage()});
            logger.log(Level.FINEST, "Full exception: ", e);

        }
    }

    /**
     * Starts a system.
     *
     * @param systemDescriptor
     *            the system to start.
     *
     * @param client
     *            adapter-specific identifier for the client making the request.
     *
     * @param adapterProxy
     *            the name of the class implementing the system adapter proxy for the JSON Fabric client.
     *
     * @return the status.
     */
    public synchronized RuntimeStatus start(SystemDescriptor systemDescriptor, Object client, String adapterProxy) {

        RuntimeStatus status = null;

        SystemRuntime systemRuntime = activeSystems.get(systemDescriptor);

        /* If there is already a running service instance with this name... */
        if (systemRuntime != null && systemRuntime.isRunning()) {

            String message = format("System [%s]: already running", systemDescriptor);
            logger.log(Level.INFO, message);
            status = new RuntimeStatus(RuntimeStatus.Status.ALREADY_RUNNING, message);

        } else {

            /* Look up the system in the Registry */
            SystemFactory systemFactory = FabricRegistry.getSystemFactory(QueryScope.LOCAL);
            System system = systemFactory.getSystemsById(systemDescriptor.platform(), systemDescriptor.system());

            /* If no valid system was found... */
            if (system == null || system.getTypeId() == null) {

                String message = format("System [%s] not found", systemDescriptor.toString());
                logger.log(Level.WARNING, message);
                status = new RuntimeStatus(RuntimeStatus.Status.NOT_FOUND, message);

            } else {

                try {

                    if (systemRuntime == null) {

                        /* Create it */
                        systemRuntime = new SystemRuntime(systemDescriptor, client, adapterProxy, this);

                    }

                    /* Instantiate the service */
                    systemRuntime.instantiate();

                    /* Initialize the new service instance */
                    systemRuntime.start();

                } catch (Exception e) {

                    String message = format("Failed to start system [%s]: %s", systemDescriptor.toString(), e
                            .getMessage());
                    logger.log(Level.SEVERE, message);
                    logger.log(Level.FINEST, "Full exception: ", e);
                    status = new RuntimeStatus(RuntimeStatus.Status.START_FAILED, message);

                }
            }
        }

        if (status == null) {
            /* The system was started OK */
            activeSystems.put(systemDescriptor, systemRuntime);
            status = new RuntimeStatus(RuntimeStatus.Status.OK, RuntimeStatus.MESSAGE_OK);
        }

        return status;
    }

    /**
     * Stops a running instance of this service.
     *
     * @param systemDescriptor
     *            the service to stop.
     *
     * @param response
     *            the result of the operation.
     *
     * @return the status.
     */
    public synchronized RuntimeStatus stop(SystemDescriptor systemDescriptor) {

        RuntimeStatus status = null;

        /* Get the service instance */
        SystemRuntime systemRuntime = activeSystems.get(systemDescriptor);

        /* If there is not a running service instance with this name... */
        if (systemRuntime == null || !systemRuntime.isRunning()) {

            /* Make sure that this is reflected in the Registry */
            updateAvailability(systemDescriptor, "UNAVAILABLE");

            String message = format("System not running: %s", systemDescriptor);
            logger.log(Level.WARNING, message);
            status = new RuntimeStatus(RuntimeStatus.Status.NOT_RUNNING, message);

        } else {

            /* Attempt to stop the instance */
            if (systemRuntime.stop()) {

                /* Remove it from the manager */
                activeSystems.remove(systemDescriptor);

            } else {

                status = new RuntimeStatus(RuntimeStatus.Status.STILL_RUNNING,
                        "Cannot stop systems with autostart enabled");

            }
        }

        if (status == null) {
            /* The system was stopped OK */
            status = new RuntimeStatus(RuntimeStatus.Status.OK, RuntimeStatus.MESSAGE_OK);
        }

        return status;

    }

    /**
     * Update the availability status of the service in the Registry.
     *
     * @param systemDescriptor
     *            the service for which status is being changed.
     *
     * @param availability
     *            the new availability status.
     */
    private void updateAvailability(SystemDescriptor systemDescriptor, String availability) {

        /* Lookup the service record in the Registry */
        SystemFactory systemFactory = FabricRegistry.getSystemFactory(queryScope);
        System system = systemFactory.getSystemsById(systemDescriptor.platform(), systemDescriptor.system());

        try {

            /* Update the availability and commit */
            system.setAvailability(availability);
            systemFactory.update(system);

        } catch (Exception e) {

            logger.log(Level.SEVERE, "Cannot set availability to [{0}] on service [{1}/{2}]: {3}", new Object[] {
                    system.getAvailability(), system.getPlatformId(), system.getId(), e.getMessage()});
            logger.log(Level.FINEST, "Full exception: ", e);

        }
    }

    /**
     * Sends a request to a request-response service.
     *
     * @param requestResponseService
     *            the target for the request.
     *
     * @param solicitResponseService
     *            to reply-to service.
     *
     * @param msg
     *            the message payload.
     *
     * @param encoding
     *            the payload encoding.
     *
     * @param correlId
     *            the correlation ID for the request.
     *
     * @return the status.
     */
    public synchronized RuntimeStatus request(ServiceDescriptor requestResponseService,
            ServiceDescriptor solicitResponseService, String msg, String encoding, String correlId) {

        RuntimeStatus status = null;

        SystemDescriptor systemDescriptor = solicitResponseService.toSystemDescriptor();
        SystemRuntime systemRuntime = activeSystems.get(systemDescriptor);

        /* If there is a running service instance... */
        if (systemRuntime != null && systemRuntime.isRunning()) {

            try {

                /* Send the request message */
                systemRuntime.request(correlId, requestResponseService, solicitResponseService, msg.getBytes(),
                        encoding);

            } catch (Exception e) {

                String message = format("Error sending request message to request/response service [%s]:\n%s",
                        requestResponseService, e.getMessage());
                logger.log(Level.WARNING, message);
                logger.log(Level.FINEST, "Full exception: ", e);
                status = new RuntimeStatus(RuntimeStatus.Status.SEND_REQUEST_FAILED, message);

            }

        } else {

            String message = format("System is not running: %s", systemDescriptor);
            logger.log(Level.WARNING, message);
            status = new RuntimeStatus(RuntimeStatus.Status.NOT_RUNNING, message);

        }

        if (status == null) {
            /* The system was started OK */
            status = new RuntimeStatus(RuntimeStatus.Status.OK, RuntimeStatus.MESSAGE_OK);
        }

        return status;
    }

    /**
     * Sends a response to a solicit-response service.
     *
     * @param sendTo
     *            the target for the request.
     *
     * @param producer
     *            to producer of the result.
     *
     * @param msg
     *            the message payload.
     *
     * @param encoding
     *            the payload encoding.
     *
     * @param correlId
     *            the correlation ID for the request.
     *
     * @return the status.
     */
    public synchronized RuntimeStatus response(ServiceDescriptor sendTo, ServiceDescriptor producer, String msg,
            String encoding, String correlId) {

        RuntimeStatus status = null;

        SystemDescriptor systemDescriptor = producer.toSystemDescriptor();
        SystemRuntime systemRuntime = activeSystems.get(systemDescriptor);

        /* If there is a running service instance... */
        if (systemRuntime != null && systemRuntime.isRunning()) {

            try {

                /* Send the response message */
                systemRuntime.respond(correlId, sendTo, producer, msg.getBytes());

            } catch (Exception e) {

                String message = format("Error sending response message to requesting service [%s]:\n%s", sendTo, e
                        .getMessage());
                logger.log(Level.WARNING, message);
                logger.log(Level.FINEST, "Full exception: ", e);
                status = new RuntimeStatus(RuntimeStatus.Status.SEND_REQUEST_FAILED, message);

            }

        } else {

            String message = format("System is not running: %s", (SystemDescriptor) producer);
            logger.log(Level.WARNING, message);
            status = new RuntimeStatus(RuntimeStatus.Status.NOT_RUNNING, message);

        }

        if (status == null) {
            /* The system was started OK */
            status = new RuntimeStatus(RuntimeStatus.Status.OK, RuntimeStatus.MESSAGE_OK);
        }

        return status;
    }

    /**
     * Answers the Fabric client associated with this instance.
     *
     * @return the Fabric client instance.
     */
    public FabricPlatform fabricClient() {

        return fabricClient;

    }

    /**
     * Sends a notification to a listener service.
     *
     * @param listenerService
     *            the target for the notification.
     *
     * @param notificationService
     *            the notifying service.
     *
     * @param msg
     *            the message payload.
     *
     * @param encoding
     *            the payload encoding.
     *
     * @param correlId
     *            the correlation ID for the request.
     *
     * @return the status.
     */
    public synchronized RuntimeStatus notify(ServiceDescriptor listenerService, ServiceDescriptor notificationService,
            String msg, String encoding, String correlId) {

        RuntimeStatus status = null;

        SystemDescriptor systemDescriptor = notificationService.toSystemDescriptor();
        SystemRuntime systemRuntime = activeSystems.get(systemDescriptor);

        /* If there is a running service instance... */
        if (systemRuntime != null && systemRuntime.isRunning()) {

            try {

                /* Send the request message */
                systemRuntime.notify(listenerService, notificationService, msg.getBytes(), encoding, correlId);

            } catch (Exception e) {

                String message = format("Error sending notification message to listener service [%s]:\n%s",
                        listenerService, e.getMessage());
                logger.log(Level.WARNING, message);
                logger.log(Level.FINEST, "Full exception: ", e);
                status = new RuntimeStatus(RuntimeStatus.Status.SEND_NOTIFICATION_FAILED, message);

            }

        } else {

            String message = format("System is not running: %s", systemDescriptor);
            logger.log(Level.WARNING, message);
            status = new RuntimeStatus(RuntimeStatus.Status.NOT_RUNNING, message);

        }

        if (status == null) {
            /* The system was started OK */
            status = new RuntimeStatus(RuntimeStatus.Status.OK, RuntimeStatus.MESSAGE_OK);
        }

        return status;
    }

    /**
     * Publishes an output feed message.
     *
     * @param outputFeedService
     *            the publishing service.
     *
     * @param msg
     *            the message payload.
     *
     * @param encoding
     *            the payload encoding.
     *
     * @return the status.
     */
    public synchronized RuntimeStatus publish(ServiceDescriptor outputFeedService, String msg, String encoding) {

        RuntimeStatus status = null;

        SystemDescriptor systemDescriptor = outputFeedService.toSystemDescriptor();
        SystemRuntime systemRuntime = activeSystems.get(systemDescriptor);

        /* If there is a running service instance... */
        if (systemRuntime != null && systemRuntime.isRunning()) {

            try {

                /* Publish the message */
                systemRuntime.publish(outputFeedService, msg.getBytes(), encoding);

            } catch (Exception e) {

                String message = format("Error publishing message to output-feed service [%s]:\n%s", outputFeedService,
                        e.getMessage());
                logger.log(Level.WARNING, message);
                logger.log(Level.FINEST, "Full exception: ", e);
                status = new RuntimeStatus(RuntimeStatus.Status.PUBLISH_FAILED, message);

            }

        } else {

            String message = format("System is not running: %s", systemDescriptor);
            logger.log(Level.WARNING, message);
            status = new RuntimeStatus(RuntimeStatus.Status.NOT_RUNNING, message);

        }

        if (status == null) {
            /* The system was started OK */
            status = new RuntimeStatus(RuntimeStatus.Status.OK, RuntimeStatus.MESSAGE_OK);
        }

        return status;
    }

    /**
     * Subscribes to a list of output feeds.
     *
     * @param outputFeedPatterns
     *            the list of output feeds (which may include wildcards) to which subscriptions are to be made.
     *
     * @param inputFeed
     *            the local service to which feed messages will be delivered.
     *
     * @param subscribedList
     *            to hold the list of actual subscriptions made.
     */
    public RuntimeStatus subscribe(ServiceDescriptor[] outputFeedPatterns, ServiceDescriptor inputFeed,
            List<ServiceDescriptor> subscribedList) throws Exception {

        RuntimeStatus subscribeStatus = RuntimeStatus.STATUS_OK;

        synchronized (wiringLock) {

            /* For each output feed pattern... */
            for (int sf = 0; sf < outputFeedPatterns.length; sf++) {

                /* Record this subscription request */
                @SuppressWarnings("unchecked")
                List<ServiceDescriptor> subscriptionList = fabric.lookupSublist(inputFeed, systemSubscriptionRequests);
                if (!subscriptionList.contains(outputFeedPatterns[sf])) {
                    subscriptionList.add(outputFeedPatterns[sf]);
                }

                /* Get the list of feeds that match the requested feed pattern (it may contain wildcards) */
                ServiceDescriptor[] feedsMatchingPattern = queryMatchingFeeds(outputFeedPatterns[sf]);

                /* While there are more feeds... */
                for (int f = 0; f < feedsMatchingPattern.length; f++) {

                    /* Subscribe to the feed */
                    subscribeStatus = subscribe(feedsMatchingPattern[f], inputFeed, outputFeedPatterns[sf]);

                    if (subscribeStatus.isOK()) {
                        subscribedList.add(feedsMatchingPattern[f]);
                    }
                }
            }
        }

        return subscribeStatus;
    }

    /**
     * Find the list of feeds in the Registry matching the specified pattern.
     *
     * @param feedPattern
     *            the pattern to match.
     *
     * @return the list of matching feeds.
     *
     * @throws RegistryQueryException
     */
    private static ServiceDescriptor[] queryMatchingFeeds(ServiceDescriptor feedPattern) throws RegistryQueryException {

        /* To hold the results */
        ServiceDescriptor[] matchingFeeds = null;

        /* To hold the predicate required to find matching feeds in the Registry */
        String queryPredicate = null;

        /* If the feed descriptor does not contain any wildcards... */
        if (!feedPattern.toString().contains("*")) {

            matchingFeeds = new ServiceDescriptor[] {feedPattern};

        } else {

            /* Generate the SQL predicate required to identify the matching feeds */

            String platform = feedPattern.platform();
            String platformPredicate = null;

            if (platform.contains("*")) {
                platformPredicate = String.format("platform_id like '%s'", platform.replace('*', '%'));
            } else {
                platformPredicate = String.format("platform_id = '%s'", platform);
            }

            String service = feedPattern.system();
            String servicePredicate = null;

            if (service.contains("*")) {
                servicePredicate = String.format("service_id like '%s'", service.replace('*', '%'));
            } else {
                servicePredicate = String.format("service_id = '%s'", service);
            }

            String feed = feedPattern.service();
            String feedPredicate = null;

            if (feed.contains("*")) {
                feedPredicate = String.format("id like '%s'", feed.replace('*', '%'));
            } else {
                feedPredicate = String.format("id = '%s'", feed);
            }

            queryPredicate = String.format("direction = 'output' and %s and %s and %s", platformPredicate,
                    servicePredicate, feedPredicate);

            /* Generate the list of matching feeds */
            ServiceFactory sf = FabricRegistry.getServiceFactory();
            Service[] registryFeedList = sf.getServices(queryPredicate);
            matchingFeeds = new ServiceDescriptor[registryFeedList.length];

            /* For each matching feed... */
            for (int f = 0; f < matchingFeeds.length; f++) {

                /* Create a feed descriptor */
                matchingFeeds[f] = new ServiceDescriptor(registryFeedList[f].getPlatformId(), registryFeedList[f]
                        .getSystemId(), registryFeedList[f].getId());

            }
        }

        return matchingFeeds;
    }

    /**
     * Subscribes to an output feed.
     *
     * @param outputFeedService
     *            the publishing service.
     *
     * @param inputFeedService
     *            the local service to which feed messages will be delivered.
     *
     * @param pattern
     *            the original subscription pattern.
     *
     * @return the status.
     */
    public RuntimeStatus subscribe(ServiceDescriptor outputFeedService, ServiceDescriptor inputFeedService,
            ServiceDescriptor pattern) {

        RuntimeStatus status = null;

        synchronized (wiringLock) {

            SystemDescriptor systemDescriptor = inputFeedService.toSystemDescriptor();
            SystemRuntime systemRuntime = activeSystems.get(systemDescriptor);

            /* If there is a running system instance... */
            if (systemRuntime != null && systemRuntime.isRunning()) {

                try {

                    /* Subscribe */
                    boolean isNewSubscription = systemRuntime.subscribe(outputFeedService, inputFeedService);

                    if (!isNewSubscription) {

                        String message = format("Already subscribed to output-feed service [%s] (input-feed [%s])",
                                outputFeedService, inputFeedService);
                        logger.log(Level.FINEST, message);
                        status = new RuntimeStatus(RuntimeStatus.Status.ALREADY_SUBSCRIBED, message);

                    } else {

                        recordSystemSubscription(systemDescriptor, pattern, inputFeedService);

                    }

                } catch (Exception e) {

                    String message = format("Error subscribing to output-feed service [%s] (input-feed [%s]): %s",
                            outputFeedService, inputFeedService, e.getMessage());
                    logger.log(Level.WARNING, message);
                    logger.log(Level.FINEST, "Full exception: ", e);
                    status = new RuntimeStatus(RuntimeStatus.Status.SUBSCRIBE_FAILED, message);

                }

            } else {

                String message = format("System is not running: %s", systemDescriptor);
                logger.log(Level.WARNING, message);
                status = new RuntimeStatus(RuntimeStatus.Status.NOT_RUNNING, message);

            }

            if (status == null) {
                status = new RuntimeStatus(RuntimeStatus.Status.OK, RuntimeStatus.MESSAGE_OK);
            }
        }

        return status;
    }

    /**
     * Records in the Registry an active subscription pattern/input-feed pair against a system.
     *
     * @param systemDescriptor
     *
     * @param pattern
     *
     * @param inputFeed
     *
     * @throws Exception
     */
    protected void recordSystemSubscription(SystemDescriptor systemDescriptor, ServiceDescriptor pattern,
            ServiceDescriptor inputFeed) throws Exception {

        String patternAndMatch = inputFeed.toString() + '=' + pattern.toString();
        boolean doAddSubscription = true;

        /* Lookup the system in the Registry */
        SystemFactory sf = FabricRegistry.getSystemFactory();
        System system = sf.getSystemsById(systemDescriptor.platform(), systemDescriptor.system());

        /* Get the system's attributes */
        String attrString = system.getAttributes();
        JSON attr = new JSON((attrString != null) ? attrString : "{}");

        /* Get the list of active subscription patterns */
        JSONArray subscriptions = attr.getJSONArray("subscriptions");
        subscriptions = (subscriptions != null) ? subscriptions : new JSONArray();

        /* For each pattern... */
        for (int p = 0; doAddSubscription && p < subscriptions.size(); p++) {
            String next = subscriptions.getString(p);
            if (patternAndMatch.equals(next)) {
                doAddSubscription = false;
            }
        }

        if (doAddSubscription) {
            /* Add the new pattern and save */
            subscriptions.add(patternAndMatch);
            attr.putJSONArray("subscriptions", subscriptions);
            system.setAttributes(attr.toString());
            sf.save(system);
        }
    }

    /**
     * Unsubscribes from one or more output feeds.
     *
     * @param outputFeedServices
     *            the list of output feed services. If <code>null</code> then all feeds associated with the specified
     *            input feed service are unsubscribed.
     *
     * @param inputFeedService
     *            the local service to which feed messages are being delivered.
     *
     * @return the status.
     */
    public RuntimeStatus unsubscribe(ServiceDescriptor[] outputFeedServices, ServiceDescriptor inputFeedService) {

        RuntimeStatus status = null;

        synchronized (wiringLock) {

            SystemDescriptor systemDescriptor = inputFeedService.toSystemDescriptor();
            SystemRuntime systemRuntime = activeSystems.get(systemDescriptor);

            /* If there is a running system instance... */
            if (systemRuntime != null && systemRuntime.isRunning()) {

                try {

                    /* Get the list of subscription requests for this input feed */
                    @SuppressWarnings("unchecked")
                    List<ServiceDescriptor> subscriptionList = fabric.lookupSublist(inputFeedService,
                            systemSubscriptionRequests);

                    /* If a list of output feeds was provided... */
                    if (outputFeedServices != null && outputFeedServices.length > 0) {

                        /* Forget this subscription request */

                        for (ServiceDescriptor nextDescriptor : outputFeedServices) {
                            subscriptionList.remove(nextDescriptor);
                            removeSystemSubscription(systemDescriptor, nextDescriptor, inputFeedService);
                        }

                    } else {

                        /* Forget all subscriptions */
                        subscriptionList.clear();
                        removeAllSystemSubscription(systemDescriptor);

                    }

                    /* Unsubscribe */

                    systemRuntime.unsubscribe(outputFeedServices, inputFeedService);

                } catch (Exception e) {

                    String message = format("Error unsubscribing from output-feed services (input-feed [%s]):\n%s",
                            inputFeedService, e.getMessage());
                    logger.log(Level.WARNING, message);
                    logger.log(Level.FINEST, "Full exception: ", e);
                    status = new RuntimeStatus(RuntimeStatus.Status.UNSUBSCRIBE_FAILED, message);

                }

            } else {

                String message = format("System is not running: %s", systemDescriptor);
                logger.log(Level.WARNING, message);
                status = new RuntimeStatus(RuntimeStatus.Status.NOT_RUNNING, message);

            }

            if (status == null) {
                /* The unsubscribe completed OK */
                status = new RuntimeStatus(RuntimeStatus.Status.OK, RuntimeStatus.MESSAGE_OK);
            }
        }

        return status;
    }

    /**
     * Removes from the Registry an active subscription pattern/input-feed pair recorded against a system.
     *
     * @param pattern
     *
     * @param systemDescriptor
     *
     * @throws Exception
     */
    protected void removeSystemSubscription(SystemDescriptor systemDescriptor, ServiceDescriptor pattern,
            ServiceDescriptor inputFeed) throws Exception {

        String patternAndMatch = pattern.toString() + '=' + inputFeed.toString();

        /* Lookup the system in the Registry */
        SystemFactory sf = FabricRegistry.getSystemFactory();
        System system = sf.getSystemsById(systemDescriptor.platform(), systemDescriptor.system());

        /* Get the system's attributes */
        String attrString = system.getAttributes();
        JSON attr = new JSON((attrString != null) ? attrString : "{}");

        /* Get the list of active subscription patterns */
        JSONArray subscriptions = attr.getJSONArray("subscriptions");

        JSONArray newSubscriptions = new JSONArray();

        /* For each subscription... */
        for (int p = 0; p < subscriptions.size(); p++) {
            String next = subscriptions.getString(p);
            if (!patternAndMatch.equals(next)) {
                newSubscriptions.add(next);
            }
        }

        if (newSubscriptions.size() != subscriptions.size()) {
            /* Add the new pattern and save */
            attr.putJSONArray("subscriptions", newSubscriptions);
            system.setAttributes(attr.toString());
            sf.save(system);
        }
    }

    /**
     * Removes from the Registry an active subscription pattern/input-feed pair recorded against a system.
     *
     * @param pattern
     *
     * @param systemDescriptor
     *
     * @throws Exception
     */
    protected void removeAllSystemSubscription(SystemDescriptor systemDescriptor) throws Exception {

        /* Lookup the system in the Registry */
        SystemFactory sf = FabricRegistry.getSystemFactory();
        System system = sf.getSystemsById(systemDescriptor.platform(), systemDescriptor.system());

        /* Remove any subscriptions from the system's attributes */
        String attrString = system.getAttributes();
        JSON attr = new JSON((attrString != null) ? attrString : "{}");
        attr.putJSONArray("subscriptions", new JSONArray());

        /* Update the system in the Registry */
        system.setAttributes(attr.toString());
        sf.save(system);

    }

    /**
     * Actions a notification of a new system on the bus, attempting to fulfill any outstanding subscriptions that might
     * now be satisfied.
     *
     * @param systemNotification
     *            a JSON message containing details of the new system.
     *
     * @throws Exception
     */
    private void matchSubscriptions(JSON systemNotification) throws Exception {

        /* Get the list of new services */

        JSONArray servicesJSON = systemNotification.getJSONArray("services");
        List<ServiceDescriptor> newServices = new ArrayList<ServiceDescriptor>();

        for (JSON serviceJSON : servicesJSON) {

            /* Build a descriptor for the next new service */
            ServiceDescriptor nextService = new ServiceDescriptor(serviceJSON.getString("id"));
            newServices.add(nextService);

        }

        /* For each input feed that has subscription requests associated with it... */
        for (ServiceDescriptor nextInputFeed : systemSubscriptionRequests.keySet()) {

            /* To hold the list of new subscriptions that we make (if any) */
            List<ServiceDescriptor> newSubscriptions = new ArrayList<ServiceDescriptor>();

            /* Get the current list of subscription requests for the input feed */
            List<ServiceDescriptor> subscriptionRequests = fabric.lookupSublist(nextInputFeed,
                    systemSubscriptionRequests);

            /* For each subscription request... */
            for (ServiceDescriptor nextRequest : subscriptionRequests) {

                /* Get the set of new services that match the request */
                List<ServiceDescriptor> matchingServices = matchDescriptors(nextRequest, newServices);

                /* For each matching service... */
                for (ServiceDescriptor matchingService : matchingServices) {

                    /* Subscribe */
                    RuntimeStatus subscribeStatus = subscribe(matchingService, nextInputFeed, nextRequest);

                    if (subscribeStatus.isOK()) {
                        newSubscriptions.add(matchingService);
                    }
                }
            }

            /* If any new subscriptions were made... */
            if (newSubscriptions.size() > 0) {

                /* Get the system instance that owns the input feed */
                SystemRuntime systemRuntime = activeSystems.get(nextInputFeed.toSystemDescriptor());

                /* Build the response */
                JSON response = JSONAdapter.buildSubscriptionResponse(newSubscriptions, nextInputFeed, null);

                /* Send to the client */
                systemRuntime.system().sendToClient(response.toString());

            }
        }
    }

    /**
     * Actions a notification of a lost system on the bus, cleaning up any existing subscriptions that are now broken.
     *
     * @param systemNotification
     *            a JSON message containing details of the new system.
     *
     * @throws Exception
     */
    private void pruneSubscriptions(JSON systemNotification) throws Exception {

        /* Get the list of services that have been lost */

        JSONArray servicesJSON = systemNotification.getJSONArray("services");
        ServiceDescriptor[] lostServices = new ServiceDescriptor[servicesJSON.size()];
        int s = 0;

        for (JSON serviceJSON : servicesJSON) {
            /* Build a descriptor for the next new service */
            lostServices[s++] = new ServiceDescriptor(serviceJSON.getString("id"));
        }

        synchronized (wiringLock) {

            /* For each active system... */
            for (SystemRuntime system : activeSystems.values()) {

                HashMap<ServiceDescriptor, List<ServiceDescriptor>> pruned = null;

                /* Prune subscriptions */
                pruned = system.unsubscribe(lostServices);

                /* If any subscriptions were pruned... */
                if (pruned.size() > 0) {

                    /* For each input feed... */
                    for (ServiceDescriptor inputFeed : pruned.keySet()) {

                        /* Build the response */
                        JSON response = JSONAdapter.buildLostSubscriptionMessage(pruned.get(inputFeed), inputFeed);

                        /* Send to the client */
                        system.system().sendToClient(response.toString());
                    }
                }
            }
        }
    }

    /**
     * Actions a notification of a new node on the bus, attempting to fulfill any outstanding subscriptions that might
     * now be satisfied.
     *
     * @throws Exception
     */
    public void matchSubscriptions() throws Exception {

        /* For each input feed that has subscription requests associated with it... */
        for (ServiceDescriptor nextInputFeed : systemSubscriptionRequests.keySet()) {

            /* To hold the list of new subscriptions that we make */
            List<ServiceDescriptor> subscribedList = new ArrayList<ServiceDescriptor>();

            /* Get the current list of subscription requests for the input feed */
            List<ServiceDescriptor> patternList = fabric.lookupSublist(nextInputFeed, systemSubscriptionRequests);

            if (patternList != null && patternList.size() > 0) {

                /* Attempt to subscribe */
                ServiceDescriptor[] patternArray = patternList.toArray(new ServiceDescriptor[0]);
                subscribe(patternArray, nextInputFeed, subscribedList);

                /* If any new subscriptions were made... */
                if (subscribedList.size() > 0) {

                    /* Get the system instance that owns the input feed */
                    SystemRuntime systemRuntime = activeSystems.get(nextInputFeed.toSystemDescriptor());

                    /* Build the response */
                    JSON response = JSONAdapter.buildSubscriptionResponse(subscribedList, nextInputFeed, null);

                    /* Send to the client */
                    systemRuntime.system().sendToClient(response.toString());

                }
            }
        }
    }

    /**
     * Actions a notification of a lost node on the bus, cleaning up any existing subscriptions that are now broken.
     *
     * @param node
     *            the ID of the lost node.
     *
     * @throws Exception
     */
    public void pruneSubscriptions(String node) throws Exception {

        synchronized (wiringLock) {

            /* For each active system... */
            for (SystemRuntime system : activeSystems.values()) {

                HashMap<ServiceDescriptor, List<ServiceDescriptor>> pruned = null;

                /* Prune subscriptions */
                pruned = system.unsubscribe(node);

                /* If any subscriptions were pruned... */
                if (pruned.size() > 0) {

                    /* For each input feed... */
                    for (ServiceDescriptor inputFeed : pruned.keySet()) {

                        /* Build the response */
                        JSON response = JSONAdapter.buildLostSubscriptionMessage(pruned.get(inputFeed), inputFeed);

                        /* Send to the client */
                        system.system().sendToClient(response.toString());
                    }
                }
            }
        }
    }

    /**
     * Restores the subscriptions recorded against the specified system.
     *
     * @param desc
     *            the system descriptor.
     */
    protected void restoreSystemSubscriptions(SystemDescriptor desc) {

        /* Get the system's active subscriptions */
        SystemFactory sf = FabricRegistry.getSystemFactory(QueryScope.LOCAL);
        System system = sf.getSystemsById(desc.platform(), desc.system());
        String attrString = system.getAttributes();
        JSON attr = null;
        try {
            attr = new JSON((attrString != null) ? attrString : "{}");
        } catch (IOException e) {
            attr = new JSON();
        }

        /* Get the client ID for the system */
        String clientID = attr.getString("clientID");

        JSONArray subscriptions = attr.getJSONArray("subscriptions");

        /* For each subscription... */
        for (int sub = 0; subscriptions != null && sub < subscriptions.size(); sub++) {

            String patternAndMatch = subscriptions.getString(sub);
            String[] parts = patternAndMatch.split("=");
            ServiceDescriptor inputFeed = new ServiceDescriptor(parts[0]);
            ServiceDescriptor pattern = new ServiceDescriptor(parts[1]);

            /* Attempt to subscribe */

            logger.log(Level.INFO, "System [{0}]: resubscribing to [{1}] (wired to [{2}])", new Object[] {desc,
                    pattern, inputFeed});

            ServiceDescriptor[] patternArray = new ServiceDescriptor[] {pattern};
            List<ServiceDescriptor> subscribedList = new ArrayList<ServiceDescriptor>();
            try {
                subscribe(patternArray, inputFeed, subscribedList);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Cannot restore service [{0}] subscription [{1}], input feed [{2}]",
                        new Object[] {desc, pattern, inputFeed});
            }

            /* If any new subscriptions were made... */
            if (subscribedList.size() > 0) {

                /* Get the system instance that owns the input feed */
                SystemRuntime systemRuntime = activeSystems.get(inputFeed.toSystemDescriptor());

                /* Build the response */
                JSON response = JSONAdapter.buildSubscriptionResponse(subscribedList, inputFeed, null);

                /* Send to the client */
                systemRuntime.system().sendToClient(response.toString());

            }
        }
    }

    /**
     * Searches a list of service descriptors for those matching the specified pattern and returns the matching subset.
     *
     * @param descriptorPattern
     *            the pattern to match.
     *
     * @param descriptorList
     *            the list of descriptors to check.
     *
     * @return the list of matching descriptors.
     *
     * @throws RegistryQueryException
     */
    private List<ServiceDescriptor> matchDescriptors(ServiceDescriptor descriptorPattern,
            List<ServiceDescriptor> descriptorList) {

        /* To hold the results */
        List<ServiceDescriptor> matchingDescriptors = new ArrayList<ServiceDescriptor>();

        /* Convert the pattern to its equivalent regular expression */
        String descriptorPatternString = descriptorPattern.toString().replace("*", ".*");

        /* For each descriptor... */
        for (ServiceDescriptor nextDescriptor : descriptorList) {

            /* If we have a match... */
            if (nextDescriptor.toString().matches(descriptorPatternString)) {
                matchingDescriptors.add(nextDescriptor);
            }
        }

        return matchingDescriptors;
    }

    /**
     * Cleans up any running systems associated with the specified client.
     *
     * @param client
     *            adapter-specific identifier for the client making the request.
     *
     * @return the status.
     */
    public synchronized void cleanup(Object client) {

        HashMap<SystemDescriptor, SystemRuntime> activeSystemsCopy = (HashMap<SystemDescriptor, SystemRuntime>) activeSystems
                .clone();

        /* For each active system... */
        for (SystemDescriptor nextSystem : activeSystemsCopy.keySet()) {

            /* Get the record for the next system */
            SystemRuntime nextRuntime = activeSystemsCopy.get(nextSystem);
            Object nextClient = nextRuntime.getClient();

            /* If the record is associated with the specified client... */
            if (nextClient != null && nextClient.equals(client)) {

                /* Stop the system */
                stop(nextSystem);

            }
        }
    }

    /**
     * @see fabric.bus.feeds.ISubscriptionCallback#startSubscriptionCallback()
     */
    @Override
    public void startSubscriptionCallback() {

        FLog.enter(logger, Level.FINER, this, "startSubscriptionCallback", (Object[]) null);
        FLog.exit(logger, Level.FINER, this, "startSubscriptionCallback", null);
    }

    /**
     * @see fabric.bus.feeds.ISubscriptionCallback#handleSubscriptionMessage(fabric.bus.messages.IFeedMessage)
     */
    @Override
    public synchronized void handleSubscriptionMessage(IFeedMessage message) {

        FLog.enter(logger, Level.FINER, this, "handleSubscriptionMessage", message);

        /* Get the target service of this message */
        String serviceDescriptor = message.getProperty(IServiceMessage.PROPERTY_DELIVER_TO_SERVICE);

        if (serviceDescriptor != null) {

            String payload = null;

            try {

                /* Get the message payload */
                byte[] payloadBytes = message.getPayload().getPayload();
                payload = (payloadBytes != null) ? new String(payloadBytes) : null;

                if (serviceDescriptor.equals("$fab/$reg/$updates")) {

                    /* The payload is a JSON structure containing details of a Registry update */
                    JSON triggerJSON = new JSON(payload);
                    String table = triggerJSON.getString("table");
                    String availability = triggerJSON.getString("availability");

                    if (table != null) {

                        String action = triggerJSON.getString("action");

                        if (action != null) {

                            if (availability != null) {

                                table = table.toUpperCase();
                                action = action.toUpperCase();
                                availability = availability.toUpperCase();

                                switch (table) {

                                    case "SYSTEMS":

                                        if (actionTopologyUpdates) {
                                            switch (action) {
                                                case "INSERT":
                                                case "UPDATE":
                                                    if (availability.equals("AVAILABLE")) {
                                                        SystemDescriptor desc = getSystemDescriptor(triggerJSON);
                                                        if (isLocalRunningSystem(desc)) {
                                                            restoreSystemSubscriptions(desc);
                                                        } else {
                                                            matchSubscriptions(triggerJSON);
                                                        }
                                                    }
                                                    break;
                                                case "DELETE":
                                                    pruneSubscriptions(triggerJSON);
                                                    break;
                                            }
                                        }
                                        break;

                                    case "NODE_NEIGHBOURS":

                                        if (actionTopologyUpdates) {
                                            switch (action) {
                                                case "INSERT":
                                                case "UPDATE":
                                                    if (availability.equals("AVAILABLE")) {
                                                        matchSubscriptions();
                                                    }
                                                    break;
                                                case "DELETE":
                                                    String id = triggerJSON.getString("id");
                                                    String[] nodes = id.split("/");
                                                    pruneSubscriptions(nodes[1]);
                                                    // updateRouteCache(triggerJSON);
                                                    break;
                                            }
                                        }
                                        break;
                                }
                            } else {
                                logger.log(Level.WARNING,
                                        "Unrecognised 'availability' value in registry update message: \n{0}",
                                        triggerJSON.toString());
                            }
                        } else {
                            logger.log(Level.WARNING, "Unrecognised 'action' value in registry update message: \n{0}",
                                    triggerJSON.toString());
                        }
                    } else {
                        logger.log(Level.WARNING, "Unrecognised 'table' value in registry update message: \n{0}",
                                triggerJSON.toString());
                    }
                }

            } catch (Exception e) {

                logger.log(Level.WARNING, "Error handling Registry update notification: {0}", e.getMessage());
                logger.log(Level.FINEST, "Full exception: ", e);
                logger.log(Level.FINEST, "Payload:\n{0}", payload);

            }
        }

        FLog.exit(logger, Level.FINER, this, "handleSubscriptionMessage", null);
    }

    /**
     * Answers the system ID from a JSON Registry trigger.
     *
     * @param triggerJSON
     * @return
     */
    private SystemDescriptor getSystemDescriptor(JSON triggerJSON) {

        /* Get the system descriptor from the JSON */
        String id = triggerJSON.getString("id");
        String[] idParts = id.split(":");
        SystemDescriptor systemDescriptor = new SystemDescriptor(idParts[0]);

        return systemDescriptor;
    }

    /**
     * Determines if the system specified is a locally running one.
     *
     * @param desc
     *            the system descriptor.
     *
     * @return <code>true</code> if the system is running locally, <code>false</code> otherwise.
     */
    private boolean isLocalRunningSystem(SystemDescriptor desc) {

        boolean isLocalRunningSystem = false;

        SystemRuntime systemRuntime = activeSystems.get(desc);

        /* If the system is running locally... */
        if (systemRuntime != null && systemRuntime.isRunning()) {
            isLocalRunningSystem = true;
        }

        return isLocalRunningSystem;
    }

    /**
     * @see fabric.bus.feeds.ISubscriptionCallback#handleSubscriptionEvent(fabric.bus.feeds.ISubscription,
     *      java.lang.String, fabric.bus.messages.IServiceMessage)
     */
    @Override
    public void handleSubscriptionEvent(ISubscription subscription, String event, IServiceMessage message) {

        FLog.enter(logger, Level.FINER, this, "handleSubscriptionEvent", subscription, event, message);
        FLog.exit(logger, Level.FINER, this, "handleSubscriptionEvent", null);
    }

    /**
     * @see fabric.bus.feeds.ISubscriptionCallback#cancelSubscriptionCallback()
     */
    @Override
    public void cancelSubscriptionCallback() {

        FLog.enter(logger, Level.FINER, this, "cancelSubscriptionCallback", null);
        FLog.exit(logger, Level.FINER, this, "cancelSubscriptionCallback", null);
    }
}
