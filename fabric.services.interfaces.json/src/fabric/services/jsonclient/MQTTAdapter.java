/*
 * (C) Copyright IBM Corp. 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.jsonclient;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.FabricShutdownHook;
import fabric.IFabricShutdownHookAction;
import fabric.bus.SharedChannel;
import fabric.core.io.EndPoint;
import fabric.core.io.ICallback;
import fabric.core.io.InputTopic;
import fabric.core.io.Message;
import fabric.core.io.OutputTopic;
import fabric.core.io.mqtt.MqttConfig;
import fabric.core.json.JSON;
import fabric.core.logging.FLog;
import fabric.services.jsonclient.utilities.AdapterConstants;
import fabric.services.jsonclient.utilities.AdapterStatus;

/**
 * MQTT adapter for JSON Fabric clients.
 */
public class MQTTAdapter extends JSONAdapter implements IFabricShutdownHookAction, ICallback {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    /*
     * Class constants
     */

    /**
     * The name of the class to act as a proxy for systems started by this adapter.
     */
    private static final String MQTT_SYSTEM_ADAPTER = "fabric.services.systems.MQTTSystem";

    /*
     * Class static fields
     */

    /** The shutdown hook for this service. */
    private static FabricShutdownHook shutdownHook = null;

    /*
     * Class fields
     */

    /** The topic upon which commands from MQTT adapter clients are received. */
    private InputTopic listenTopic = null;

    /**
     * The Fabric channel through which commands from MQTT adapter clients are received.
     */
    private SharedChannel listenChannel = null;

    /** The topic upon which responses to clients are sent. */
    private OutputTopic sendTopic = null;

    /** The Fabric channel through which responses to clients are sent. */
    private SharedChannel sendChannel = null;

    /*
     * Static class initialization
     */

    static {

        /* Register the shutdown hook for this JVM */
        shutdownHook = new FabricShutdownHook("fabric.services.jsonclient");
        Runtime.getRuntime().addShutdownHook(shutdownHook);

    }

    /*
     * Class methods
     */

    public MQTTAdapter() {

        this(Logger.getLogger("fabric.services.jsonclient"));
    }

    public MQTTAdapter(Logger logger) {

        this.logger = logger;
    }

    /**
     * Main entry point.
     *
     * @param cla
     *            the command line arguments.
     */
    public static void main(final String[] cla) {

        try {

            MQTTAdapter adapter = new MQTTAdapter();

            /* Register this instance with the shutdown hook */
            shutdownHook.addAction(adapter);

            adapter.init();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @see fabric.services.jsonclient.JSONAdapter#init()
     */
    @Override
    public void init() throws Exception {

        setAdapterUserID(MqttConfig.generateClient("MA"));
        setAdapterPlatformID(MqttConfig.generateClient("MA"));
        setAdapterPlatformType("MQTT_ADAPTER");
        super.init();

        /* Open the command channels */
        openChannels();

        /* Start pre-registered systems */
        startSystems();

    }

    /**
     * Open the Fabric channels for inbound and outbound adapter messages.
     *
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    private void openChannels() throws IOException, UnsupportedOperationException {

        /* Open the channel used for inbound MQTT messages */

        listenTopic = new InputTopic(config("fabric.adapters.mqtt.intopic", null, homeNode()) + "/+");
        listenChannel = homeNodeEndPoint().openInputChannel(listenTopic, this);

        String inMessage = String.format("MQTT adapter: listening on topic [%s]", listenTopic);
        System.out.println(inMessage);
        logger.info(inMessage);

        /* Open the channel used for outbound MQTT messages */

        sendTopic = new OutputTopic(config("fabric.adapters.mqtt.outtopic", null, homeNode()));
        sendChannel = homeNodeEndPoint().openOutputChannel(sendTopic);

        String outMessage = String.format("MQTT adapter: sending to topic [%s]", sendTopic);
        System.out.println(outMessage);
        logger.info(outMessage);
    }

    /**
     * @see fabric.services.jsonclient.JSONAdapter#adapterProxy()
     */
    @Override
    public String adapterProxy() {

        return MQTT_SYSTEM_ADAPTER;
    }

    /**
     * Invoked when this callback is being initialised.
     *
     * @see fabric.core.io.ICallback#startCallback(java.lang.Object)
     */
    @Override
    public void startCallback(final Object arg1) {

        /* No initialization required. */

    }

    /**
     * Invoked when an adapter message arrives.
     *
     * @param message
     *            the new adapter message.
     *
     * @see fabric.core.io.ICallback#handleMessage(fabric.core.io.Message)
     */
    @Override
    public synchronized void handleMessage(final Message message) {

        FLog.enter(logger, Level.FINER, this, "handleMessage", message);

        JSON response = null;
        String correlationID = null;

        /* Get the ID of the MQTT adapter client */
        String[] topicParts = ((String) message.topic).split("/");
        String adapterClientID = topicParts[topicParts.length - 1];
        logger.log(Level.FINEST, "Client ID: {0}", adapterClientID);

        /* Get the JSON payload */
        String opString = new String(message.data);

        /* Convert the message to a JSON object */
        try {

            logger.log(Level.FINEST, "Handling JSON message:\n{0}", opString);
            JSON op = new JSON(opString);

            /* Get the message correlation ID from the message (if any) */
            correlationID = op.getString(AdapterConstants.FIELD_CORRELATION_ID);
            logger.log(Level.FINEST, "Correlation ID: {0}", correlationID);

            /* Handle the message */
            response = handleAdapterMessage(op, correlationID, adapterClientID);

        } catch (Exception e) {

            logger.log(Level.INFO, "Failed to parse JSON string:\n{0}");
            AdapterStatus status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_NONE,
                    AdapterConstants.ARTICLE_JSON, AdapterConstants.STATUS_MSG_BAD_JSON + ": " + e.getMessage(),
                    (correlationID == null) ? "Unknown" : correlationID, opString);
            response = status.toJsonObject();

        }

        /* If a response is required by the client... */
        if (response != null) {
            try {
                logger.log(Level.FINEST, "Response to client:\n{0}", response.toString());
                sendChannel.write(response.toString().getBytes(), new OutputTopic(sendTopic + "/" + adapterClientID));
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to send response to client [{0}] (correlation ID [{1}]): {2}",
                        new Object[] {adapterClientID, correlationID, e.getMessage()});
                logger.log(Level.FINEST, "Full exception: ", e);
            }
        }

        FLog.exit(logger, Level.FINER, this, "handleMessage", null);
    }

    /**
     * Invoked when this callback is being cancelled.
     *
     * @see fabric.core.io.ICallback#cancelCallback(java.lang.Object)
     */
    @Override
    public void cancelCallback(final Object arg1) {

        /* No cleanup required */
    }

    /**
     * @see fabric.IFabricShutdownHookAction#shutdown()
     */
    @Override
    public void shutdown() {

        super.stop();
    }

    /**
     * @see fabric.services.jsonclient.JSONAdapter#endPointConnected(fabric.core.io.EndPoint)
     */
    @Override
    public void endPointConnected(EndPoint ep) {

        super.endPointConnected(ep);

    }

    /**
     * @see fabric.services.jsonclient.JSONAdapter#endPointDisconnected(fabric.core.io.EndPoint)
     */
    @Override
    public void endPointDisconnected(EndPoint ep) {

        super.endPointDisconnected(ep);

    }

    /**
     * @see fabric.services.jsonclient.JSONAdapter#endPointReconnected(fabric.core.io.EndPoint)
     */
    @Override
    public void endPointReconnected(EndPoint ep) {

        super.endPointReconnected(ep);

    }

    /**
     * @see fabric.services.jsonclient.JSONAdapter#endPointClosed(fabric.core.io.EndPoint)
     */
    @Override
    public void endPointClosed(EndPoint ep) {

        super.endPointClosed(ep);

    }
}
