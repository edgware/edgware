/*
 * (C) Copyright IBM Corp. 2007, 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus;

import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.Fabric;
import fabric.bus.feeds.IFeedManager;
import fabric.bus.messages.IFeedMessage;
import fabric.bus.messages.INotificationMessage;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.messages.impl.ClientNotificationMessage;
import fabric.bus.messages.impl.NotificationMessage;
import fabric.bus.routing.IRouting;
import fabric.bus.services.IBusServiceDispatcher;
import fabric.bus.services.IService;
import fabric.bus.services.impl.BusServiceDispatcher;

/**
 * Handles messages received by the Fabric Manager:
 * <ul>
 * <li>Control messages (subscribe, unsubscribe, etc.)</li>
 * <li>Fabric data feeds (plug-in application, routing, delivery to users, etc.)</li>
 * </ul>
 */
public class BusMessageHandler extends Fabric {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2012";

    /*
     * Class fields
     */

    /** The Fabric Manager I/O handler. */
    private IBusServices busServices = null;

    /** The subscription/feed service. */
    private IFeedManager feedManager = null;

    /** The manager for persistent services. */
    private IBusServiceDispatcher serviceDispatcher = null;

    /*
     * Inner classes
     */

    /*
     * Class methods
     */

    /**
     * Constructs a new instance.
     *
     * @param busServices
     *            the new handler.
     */
    public BusMessageHandler(IBusServices busServices) {

        super(Logger.getLogger("fabric.bus"));

        this.busServices = busServices;

        /* Initialize the service dispatcher */
        serviceDispatcher = new BusServiceDispatcher();
        serviceDispatcher.setBusServices(busServices);

    }

    /**
     * Initializes the service handler and loads any pre-defined services.
     *
     * @param serviceName
     *            the service to load.
     *
     * @param familyName
     *            the service family name.
     *
     * @return the service.
     */
    public IService loadService(String serviceName, String familyName) {

        IService service = serviceDispatcher.registerService(serviceName, null, familyName, null);

        if (service != null) {
            logger.log(Level.FINEST, "Initialised service [{0}]", serviceName);
        }

        return service;

    }

    /**
     * Handles a service message, invoking any registered service handlers and routing it as required.
     * <p>
     * This method will also initialize default response notification messages that are returned across the Fabric to
     * the client when the service completes. The generation of these messages is controlled by the notification flag in
     * the request message.
     * </p>
     *
     * @param request
     *            the message.
     *
     * @throws Exception
     *             thrown if an exception is encountered whilst handling the service message.
     */
    public void handleServiceMessage(IServiceMessage request) throws Exception {

        /* Make a copy of the original request */
        IServiceMessage requestCopy = (IServiceMessage) request.replicate();

        Object[] messageID = new Object[] {requestCopy.getUID(), requestCopy.getCorrelationID()};
        logger.log(Level.FINER, "Handling message {0} (correlation ID {1})", messageID);

        /* To hold the response from the service invocation on this node (sent back to the originating node) */
        INotificationMessage nodeResponse = null;

        /*
         * To hold the possible response notifications that will be returned to the client indicating success, failure,
         * timeout, ... These are only generated on the node to which the client is connected, i.e. the first node in
         * the route.
         */
        ClientNotificationMessage[] clientResponses = null;

        /* Get the routing information for the message */
        IRouting requestRoute = request.getRouting();

        /* If this is not a duplicate message... */
        if (requestRoute == null || !requestRoute.isDuplicate(request)) {

            /* Get the caller-defined timeout period for responses to this message */
            int notificationTimeout = request.getNotificationTimeout();

            /* If a response is necessary and was requested for this service invocation... */
            if (!(request instanceof INotificationMessage) && request.getNotification()) {

                /* If we are running on the node to which the client is attached, i.e. the start node in the route... */
                if (requestRoute == null || requestRoute.startNode() == null
                        || homeNode().equals(requestRoute.startNode())) {

                    /*
                     * Create default client notification messages. These are registered to be delivered if/when a
                     * corresponding response notification is received from one of the nodes that will be handling this
                     * message en route to, or at, its final destination. The exception to this is the timeout message,
                     * which is automatically delivered if no Fabric notification is received. Note that only one of the
                     * messages will be delivered to the client; the rest are simply discarded. These default messages
                     * can be customized by the service.
                     */
                    clientResponses = new ClientNotificationMessage[4];
                    clientResponses[IService.NOTIFICATION_SUCCESS] = new ClientNotificationMessage(
                            IServiceMessage.EVENT_MESSAGE_HANDLED, request);
                    clientResponses[IService.NOTIFICATION_FAILURE] = new ClientNotificationMessage(
                            IServiceMessage.EVENT_MESSAGE_FAILED, request);
                    clientResponses[IService.NOTIFICATION_TIMEOUT] = new ClientNotificationMessage(
                            IServiceMessage.EVENT_MESSAGE_TIMEOUT, request);
                    clientResponses[IService.NOTIFICATION_SUCCESS_EN_ROUTE] = new ClientNotificationMessage(
                            IServiceMessage.EVENT_MESSAGE_HANDLED_EN_ROUTE, request);

                }

                /*
                 * Create a default Fabric notification message. Service messages are processed by the final node in
                 * their route and, optionally, each intermediate hop. Each time it is handled a response message can be
                 * generated to indicate the outcome, so we create a default response for the service handler to use.
                 */
                nodeResponse = new NotificationMessage(request);

            }

            /* If this message is to be handled on this node... */
            if (doActionMessage(request)) {

                /* Invoke the requested service */
                request = serviceDispatcher.dispatch(request, nodeResponse, clientResponses);

            }

            /* If client notifications were created... */
            if (clientResponses != null) {

                /* For each client notification message... */
                for (int m = 0; m < clientResponses.length; m++) {

                    ClientNotificationMessage cnm = clientResponses[m];

                    /* If this is the timeout message... */
                    if (m == IService.NOTIFICATION_TIMEOUT) {

                        /* If no timeout was specified by the caller... */
                        if (notificationTimeout == 0) {

                            /* Assign the default */
                            notificationTimeout = Integer.parseInt(config().getProperty(
                                    "fabric.notificationManager.defaultTimeout", "120"));

                        }
                    }

                    /* Register the notification message */
                    busServices.notificationManager().addNotification(cnm.getCorrelationID(), cnm.getEvent(),
                            cnm.getActor(), cnm.getActorPlatform(), cnm, notificationTimeout, false);

                }
            }

            /* If there is further processing for this message... */
            if (request != null) {

                /* Refresh the routing information for the message (in case the service changed it) */
                requestRoute = request.getRouting();

                /* If there might be further nodes to which to send the message... */
                if (requestRoute != null && !homeNode().equals(requestRoute.endNode())) {

                    /* Get the list of nodes and send the message on */
                    String[] nodes = requestRoute.nextNodes();
                    logger.log(Level.FINEST, "Sending message to node(s) {0}", nodeListAsString(nodes));
                    busServices.sendServiceMessage(request, nodes);

                }

            } else {

                logger.log(Level.FINER, "No further routing for service message {0} (correlation ID {1})", messageID);

            }

            /* If a Fabric notification was created... */
            if (nodeResponse != null) {
                deliverServiceNotifications(requestCopy, nodeResponse);
            }
        }

        logger.log(Level.FINER, "Completed handling message UID [{0}] (correlation ID [{1}])", messageID);
    }

    /**
     * Determine if this message is to be handled on this node.
     * <p>
     * This can get a bit complicated, so it's broken into tiny steps rather than being bundled into a big unreadable
     * if-condition.
     * </p>
     *
     * @param reuqest
     *            the message.
     *
     * @return <code>true</code> if the message is to be actioned, <code>false</code> otherwise.
     */
    private boolean doActionMessage(IServiceMessage reuqest) {

        /*
         * Flag indicating if this message does not have any routing information (some messages for local processing
         * don't)
         */
        boolean noRoute = false;

        /* Flag indicating if this message is to be actioned at each node on its route */
        boolean actionEnRoute = reuqest.getActionEnRoute();

        /* Flag indicating if we are at the end node of the route (where all messages are actioned) */
        boolean atEndNode = false;

        /* Flag indicating if the end node is undefined */
        boolean endNodeUndefined = false;

        /* Flag indicating this message has already been handled */
        boolean isDuplicate = false;

        /* Get the routing information for the message */
        IRouting requestRouting = reuqest.getRouting();

        /* If a route has been defined... */
        if (requestRouting != null) {

            /* If we are at the end node... */
            if (homeNode().equals(requestRouting.endNode())) {

                atEndNode = true;

            } else if (requestRouting.endNode() == null) {

                endNodeUndefined = true;

            }

        } else {

            noRoute = true;

        }

        /*
         * The message must not be a duplicate and any one of the remaining conditions needs to be true for it to be
         * actioned
         */
        boolean doActionMessage = !isDuplicate && (actionEnRoute || atEndNode || noRoute || endNodeUndefined);

        return doActionMessage;

    }

    /**
     * Delivers the notification message generated by a service invocation.
     *
     * @param request
     *            the original service request message.
     *
     * @param reponse
     *            the response notification message.
     */
    private void deliverServiceNotifications(IServiceMessage request, INotificationMessage response) {

        try {

            /* Get the route for the response message */
            IRouting responseRoute = response.getRouting();
            String[] notifyNodes = null;

            /* If there is one... */
            if (responseRoute != null) {

                notifyNodes = responseRoute.nextNodes();

            }

            /* If there are further nodes to send the notification to... */
            if (notifyNodes != null && notifyNodes.length > 0) {

                busServices.sendServiceMessage(response, notifyNodes);

            } else {

                /* Ensure that the notification is actioned locally */
                busServices.sendServiceMessage(response, new String[] {homeNode()});

            }

        } catch (Exception e) {

            logger.log(Level.WARNING, "Failed to send notification message for correlation ID [{0}]: {1}",
                    new Object[] {response.getCorrelationID(), e.getMessage()});
            logger.log(Level.FINEST, "Full exception: ", e);

        }
    }

    /**
     * Answers a trace message indicating the list of nodes to which a service message is to be sent.
     *
     * @param nodes
     *            the list of nodes.
     *
     * @return the trace message.
     */
    private String nodeListAsString(String[] nodes) {

        StringBuffer nodeList = new StringBuffer();

        /* If there are nodes in the list... */
        if (nodes != null && nodes.length > 0) {
            nodeList.append(": ");
        } else {
            nodes = new String[0];
        }

        /* For each node... */
        for (int n = 0; n < nodes.length; n++) {

            nodeList.append(nodes[n]);

            /* If this is not the last node in the list... */
            if (n < (nodes.length - 1)) {
                nodeList.append(", ");
            }
        }

        return nodeList.toString();

    }

    /**
     * Handles a feed message, invoking any plug-ins associated with it, and routing it to the next node or local
     * subscriber.
     *
     * @param message
     *            the message.
     *
     * @throws Exception
     *             thrown if an exception is encountered whilst handling the feed message.
     */
    public void handleFeedMessage(IFeedMessage message) throws Exception {

        /* Delegate this to the subscription handler */
        feedManager.handleFeed(message);
        logger.log(Level.FINEST, "Feed message (UID: \"{0}\") handled", message.getUID());

    }

    /**
     * Stops the message handler.
     */
    public void stop() {

        logger.log(Level.FINE, "Stopping bus message handler ({0})", this.getClass().getName());

        /* Stop the currently loaded services */
        serviceDispatcher.stopDispatcher();

        logger.log(Level.FINE, "Bus message handler ({0}) stopped", this.getClass().getName());

    }

    /**
     * Answers the Fabric Manager bus I/O handler.
     *
     * @return the handler.
     */
    public IBusServices getBusServices() {

        return busServices;
    }

    /**
     * Sets the Fabric Manager bus I/O handler.
     *
     * @param busServices
     *            the new handler.
     */
    public void setBusServices(IBusServices busServices) {

        this.busServices = busServices;
        serviceDispatcher.setBusServices(busServices);

    }

    /**
     * Sets the subscription/feed service.
     *
     * @param feedManager
     *            the handler.
     */
    public void setFeedManager(IFeedManager feedManager) {

        this.feedManager = feedManager;

    }
}
