/*
 * (C) Copyright IBM Corp. 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.tools.rest.servlet;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Date;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import fabric.core.json.JSON;

/**
 * MQTT Socket class that deals with incoming and outgoing MQTT messages, and MQTT connections.
 */
public class MQTTSocket implements WebSocketListener, MqttCallback, SocketHandler {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    private Session session;
    private String brokerURL = null;
    private MqttAsyncClient client = null;
    private final String defaultSubscribeTopic = "$fabric/#";
    private final int defaultSubscribeQos = 2;

    /**
     * Create MQTT connection to broker, subscribe to everything and define our callback to be this socket.
     */
    public void init() {
        if (brokerURL != null) {
            try {
                if (client != null) {
                    client.disconnect();
                }
                client = new MqttAsyncClient(brokerURL, generateClientId());
                client.setCallback(this);
                MqttConnectOptions opt = new MqttConnectOptions();
                opt.setCleanSession(true);
                client.connect(opt).waitForCompletion();
                subscribe(defaultSubscribeTopic);
            } catch (MqttException e) {
                returnError("An MqttException has occurred: " + e.getMessage());
            }
        }
    }

    /**
     * Send the error message when we lose MQTTconnectivity.
     *
     * @param err
     */
    @Override
    public void connectionLost(Throwable err) {
        returnError(Constants.MQTTConnectionLost);
        client = null;
    }

    @Override
    public void returnError(String errorMessage) {
        try {
            session.getRemote().sendString(errorMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Once a message arrives from the broker convert it to json and send it back.
     *
     * @param topic
     * @param message
     * @throws Exception
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        JSON obj = new JSON();
        String payload = new String(message.getPayload());
        obj.putString("topic", topic);
        obj.putString("payloadString", payload);
        session.getRemote().sendString(obj.toString());
    }

    /**
     * Upon connect grab session variable and initialize MQTT connection.
     *
     * @param session
     */
    @Override
    public void onWebSocketConnect(Session session) {
        this.session = session;
    }

    /**
     * Throw error to server on websocket error.
     *
     * @param cause
     */
    @Override
    public void onWebSocketError(Throwable cause) {
        if (cause instanceof SocketTimeoutException) {
            returnError(Constants.SocketTimeout);
        }
        client = null;
    }

    /**
     * Print out reason and status as to why socket session closed.
     *
     * @param statusCode
     * @param reason
     */
    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        System.out.println("Session closed: " + statusCode + " " + reason);
        client = null;
    }

    /**
     * Generates a string client id for broker connections with date in miliseconds
     *
     * @return
     */
    private String generateClientId() {
        return "ws" + new Date().getTime();
    }

    /**
     * Inherited method from MqttCallback that tells us once an MQTT message has successfuly been delivered. Currently
     * not needed.
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken arg0) {
        // Not needed
    }

    /**
     * Handles incoming messages from the web front-end. If it's a broker URL then it connects, otherwise it is a
     * subscription message and we handle it accordingly.
     */
    @Override
    public void onWebSocketText(String message) {
        if (message.contains("tcp://")) {
            if (brokerURL == null) {
                brokerURL = message;
                init();
            }
        } else {
            subscribe(message);
        }
    }

    /**
     * Helper method to subscribe to a specific MQTT topic.
     */
    public void subscribe(String topic) {
        if (client == null) {
            return;
        }
        try {
            client.subscribe(topic, defaultSubscribeQos);
        } catch (MqttException e) {
            returnError("An MqttException has occurred: " + e.getMessage());
        }
    }

    /**
     * Inherited method from WebSocketListener that is currently not required.
     */
    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        // Not needed
    }
}
