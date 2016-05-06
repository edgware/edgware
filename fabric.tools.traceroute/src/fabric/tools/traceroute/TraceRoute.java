/*
 * (C) Copyright IBM Corp. 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.tools.traceroute;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.Fabric;
import fabric.bus.messages.FabricMessageFactory;
import fabric.bus.messages.IClientNotificationMessage;
import fabric.bus.messages.IMessagePayload;
import fabric.bus.messages.IServiceMessage;
import fabric.bus.messages.impl.ServiceMessage;
import fabric.bus.routing.impl.StaticRouting;
import fabric.client.FabricClient;
import fabric.client.services.IClientNotificationHandler;
import fabric.core.io.mqtt.MqttConfig;
import fabric.core.xml.XML;

/**
 * Create Service Message to obtain Trace Route information
 */
public class TraceRoute implements IClientNotificationHandler {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

    /*
     * Class methods
     */
    /*
     * Variables
     */
    private static String targetNode = null;
    private static String homeNode = null;
    private Logger logger = null;
    private FabricClient fabricClient = null;
    private List<TraceRecord> trace = new LinkedList<TraceRecord>();
    private String correlationID = "";
    private SimpleDateFormat formatter = new SimpleDateFormat("S");

    private boolean terminate = false;
    private String result = "99";

    /**
     * Program entry point.
     * <p>
     * One argument is expected:
     * <ol>
     * <li>The ID of the node to be traced</li>
     * </li>
     * </ol>
     *
     * @param cla
     *            the command line arguments.
     */
    public static void main(String[] cla) {

        @SuppressWarnings("unused")
        List<TraceRecord> results = new LinkedList<TraceRecord>();

        try {

            /* Extract the command line arguments */
            targetNode = cla[0];

            /* Send the command */
            TraceRoute traceRoute = new TraceRoute();
            System.out.println("Entering run");
            traceRoute.run(targetNode);

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    public TraceRoute() {

        logger = Logger.getLogger("fabric.tools.traceroute");
    }

    /**
     * Sends the trace route command message to the target node.
     *
     * @param node
     *            the ID of the node where the service is to be invoked.
     *
     * @throws Exception
     */

    private void run(String targetNode) throws Exception {

        /* Connect to the Fabric */
        String actor = "TraceRouteActor";
        String platform = MqttConfig.generateClient("TRS_");
        fabricClient = new FabricClient(actor, platform);
        fabricClient.connect();
        homeNode = fabricClient.homeNode();

        /* Create the service message */
        ServiceMessage serviceMessage = new ServiceMessage();

        /* Set the service name: i.e. indicate that this is a message for the trace filter service */
        serviceMessage.setServiceName(TraceRouteService.class.getName());

        /* Indicate that this is a built-in Fabric plug-in ???? */
        serviceMessage.setServiceFamilyName(Fabric.FABRIC_PLUGIN_FAMILY);

        /* Set the message's routing */
        String[] routeNodes = null;
        try {
            routeNodes = fabricClient.defaultRoute(targetNode);
            if (routeNodes.length == 0) {
                throw new Exception();
            }
            StaticRouting messageRouting = new StaticRouting(routeNodes);
            serviceMessage.setRouting(messageRouting);

            /* Set properties to get interim Node processing and notifications */
            serviceMessage.setActionEnRoute(true);
            serviceMessage.setNotification(true);

            correlationID = FabricMessageFactory.generateUID();
            serviceMessage.setCorrelationID(correlationID);

            fabricClient.registerNotificationHandler(correlationID, this);

            serviceMessage.setProperty(IServiceMessage.PROPERTY_ACTOR, fabricClient.actor());
            serviceMessage.setProperty(IServiceMessage.PROPERTY_ACTOR_PLATFORM, fabricClient.platform());

            /* Send the command to the local Fabric Manager */
            logger.log(Level.FINE, "Sending command: {0}", serviceMessage.toString());
            fabricClient.getIOChannels().sendCommandsChannel.write(serviceMessage.toWireBytes());
            for (int i = 1; i < 11; i++) {
                System.out.println("waiting on notification");
                Thread.sleep(3000);
                if (terminate) {
                    break;
                }
            }

        } catch (Exception e) {
            System.out.println("Node not known or route to node not found");
            // TODO not terminating here

        }
        //

    }

    /*
     * Handle each notification returned by TraceRouteServices on the nodes on the route. - For a message_handled case
     * extract the Node and Timestamp from the Notification Payload -- If the notification is from the target node, set
     * the handler to terminate - For a timeout or error, just set the handler to terminate (non-Javadoc)
     * @see
     * fabric.client.services.IClientNotificationHandler#handleNotification(fabric.bus.messages.IClientNotificationMessage
     * )
     */
    @Override
    public void handleNotification(IClientNotificationMessage message) {

        logger.log(Level.FINE, "Entering TraceRoute handleNotification");
        // What type of result is this?
        String notificationResult = message.getNotificationEvent();
        result = message.getEvent();

        // System.out.println("Result: " + result + "NotificationResult: " +notificationResult);

        switch ((result != null) ? result : "") {
            case IServiceMessage.EVENT_MESSAGE_HANDLED:
                extractTraceRouteItem(message);
                String notifying_node = message.getProperty(IServiceMessage.PROPERTY_NOTIFYING_NODE);
                logger.log(Level.FINE, "Node responded; target node: {0}", targetNode);
                if (notifying_node.equalsIgnoreCase(targetNode)) {
                    // Need to terminate
                    terminate = true;
                }
                break;
            case IServiceMessage.EVENT_MESSAGE_HANDLED_IN_FLIGHT:
                extractTraceRouteItem(message);
                notifying_node = message.getProperty(IServiceMessage.PROPERTY_NOTIFYING_NODE);
                System.out.println("Node responded: " + targetNode + " Notifying Node: " + notifying_node);
                if (notifying_node.equalsIgnoreCase(targetNode)) {
                    // Need to terminate
                    terminate = true;
                }
                break;
            case IServiceMessage.EVENT_MESSAGE_TIMEOUT:
                terminate = true;
                break;
            case IServiceMessage.EVENT_MESSAGE_FAILED:
                terminate = true;
                break;
            default:
                System.out.println("Event type not known" + result);
                break;
        }

        if (terminate) {
            System.out.println("Terminating: code: " + result);
            end();
        }
    }

    /**
     * Method: extractTraceRouteItem Retrieves the node and timestamp from the Notification payload
     *
     * @param message
     */
    private List<TraceRecord> extractTraceRouteItem(IClientNotificationMessage message) {

        logger.log(Level.FINE, "Entering extratcTraceRouteItem");
        // Notification messages have 2 payloads
        IMessagePayload notificationPayload = message.getPayload();
        try {
            XML xmlPayload = new XML(notificationPayload.getPayloadText());
            // System.out.println(xmlPayload);
            String nodeName = xmlPayload.get("/traceroute/node[0]@name");
            String timeStamp = xmlPayload.get("/traceroute/node[0]@timestamp");
            Date time = formatter.parse(timeStamp);
            TraceRecord tr = new TraceRecord(nodeName, time);
            // Add latest Trace record to results
            Boolean success = trace.add(tr);
            // System.out.println("Trace List: ");
            // System.out.println("Node Name             TimeStamp ");
            // for (TraceRecord traceitem: trace) {
            // System.out.println(traceitem.nodeName() + " : " + traceitem.timeStamp().toString());
            // }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return trace;
    }

    /*
     * Method: end Handle disconnecting from the client and other cleanup
     */
    public void end() {

        /* Disconnect from the Fabric */
        System.out.println("Ending: " + terminate);

        fabricClient.disconnectFabric();

        printTraceInformation(targetNode, homeNode, trace);

        /* All done */
        System.exit(0);
    }

    /*
     * Method: printTraceInformation responsible for formatting the results, including : - original request - Response
     * code - Information from each nope on the route and elapsed time
     */
    private void printTraceInformation(String targetNode2, String homeNode2, List<TraceRecord> trace2) {

        System.out.println("Tracing route to node: " + targetNode2 + " from node: " + homeNode2);
        System.out.println("Response code: " + result);

        String heading1 = "Node";
        String heading2 = "Time";

        System.out.printf("%-20s %15s %n", heading1, heading2);

        // Sort list of TraceRecords
        Collections.sort(trace2, new TraceRecordComparator());

        long time = 0;
        long baseTime = 0;
        long relativeTime = 0;

        for (TraceRecord traceitem : trace2) {
            time = traceitem.timeStamp().getTime();
            if (homeNode2.equalsIgnoreCase(traceitem.nodeName())) {
                baseTime = time;
            }
            System.out.printf("%-20s %15s %n", traceitem.nodeName(), (time - baseTime));

        }
    }

}