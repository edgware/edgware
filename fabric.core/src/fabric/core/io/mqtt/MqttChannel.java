/*
 * (C) Copyright IBM Corp. 2007, 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.io.mqtt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import fabric.core.io.Channel;
import fabric.core.io.ICallback;
import fabric.core.io.InputTopic;
import fabric.core.io.Message;
import fabric.core.io.MessageQoS;
import fabric.core.io.OutputTopic;
import fabric.core.io.Topic;
import fabric.core.util.RandomID;

/**
 * Class representing an I/O channel to an end point, implemented using MQTT/MQTT-S as the transport.
 */
public class MqttChannel extends Channel {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2014";

    /*
     * Class constants
     */

    /** The header for MQTT-S messages indicating a PUBLISH message (0x0C) */
    private static final byte[] MQTTS_HEADER = new byte[] {0x0C, 0x60};

    /** Separator for the topic part of the header */
    private static final byte[] MQTTS_TOPIC_SEPARATOR = new byte[] {' ', ' '};

    /*
     * Class fields
     */

    /** The end point for this channel */
    private MqttEndPoint endPoint = null;

    /** The MQTT QoS setting */
    private byte mqttQos = 0;

    /** The "retain publication" flag */
    private boolean retain = false;

    /** Indicates if the MQTT-S is enabled */
    private boolean mqttsEnabled = false;

    /** The default <code>MessageQoS</code> for messages */
    private MessageQoS defaultMessageQos = MessageQoS.UNKNOWN;

    /** The maximum size of a message (in bytes) that can be sent as <code>MessageQoS.BEST_EFFORT</code> */
    private int maxMqttsPayload = 500;

    /** The buffer used to store messages arriving on this channel */
    private final ArrayList<Message> buffer = new ArrayList<Message>();

    /** The topic to which to send messages to the target */
    private OutputTopic outputTopic = null;

    /** The topic from which reply messages will be received (i.e. the local topic) */
    private InputTopic inputTopic = null;

    /** The callback to handle incoming messages (used for asynchronous I/O) */
    private ArrayList<ICallback> callbacks = new ArrayList<ICallback>();

    /** To manage worker threads */
    private ExecutorService executor = null;

    /*
     * Inner classes
     */

    /**
     * Runnable class used to invoke asynchronous I/O callbacks.
     */
    private class CallbackInvoker implements Runnable {

        private ICallback callback = null;

        private Message message = null;

        /**
         * Constructs a new instance.
         *
         * @param callback
         *            the callback to invoke.
         *
         * @param message
         *            the callback argument.
         */
        public CallbackInvoker(ICallback callback, Message message) {

            this.callback = callback;
            this.message = message;

        }

        /*
         * (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {

            try {

                /* Invoke the callback with the new message */
                logger.log(Level.FINEST, "Thread running; sending message to callback \"{0}\"", callback.toString());
                callback.handleMessage(message);

            } catch (Exception e) {

                logger.log(Level.WARNING, "Exception in callback: ", e);

            }
        }
    }

    /*
     * Class methods
     */

    /**
     * Constructs a new instance.
     *
     * @param endPoint
     *            the end point associated with this channel.
     *
     * @param inputTopic
     *            the name of the inbound topic.
     */
    public MqttChannel(MqttEndPoint endPoint, InputTopic inputTopic) {

        this(endPoint, inputTopic, null);

    }

    /**
     * Constructs a new instance.
     *
     * @param endPoint
     *            the end point associated with this channel.
     *
     * @param inputTopic
     *            the name of the local (reply) topic (i.e. topic name).
     *
     * @param outputTopic
     *            the name of the remote (target) topic (i.e. topic name).
     */
    public MqttChannel(MqttEndPoint endPoint, InputTopic inputTopic, OutputTopic outputTopic) {

        super();

        this.endPoint = endPoint;
        this.inputTopic = new InputTopic(toValidTopic(inputTopic));
        this.outputTopic = outputTopic;

        this.mqttQos = ((MqttConfig) endPoint.getConfig()).getMqttQos();
        this.retain = ((MqttConfig) endPoint.getConfig()).isRetain();
        this.mqttsEnabled = ((MqttConfig) endPoint.getConfig()).isMqttsEnabled();
        this.defaultMessageQos = ((MqttConfig) endPoint.getConfig()).getDefaultMessageQos();
        this.maxMqttsPayload = ((MqttConfig) endPoint.getConfig()).getMaxMqttsPayload();

    }

    /**
     * Opens this channel.
     *
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    @Override
    public void open() throws IOException, UnsupportedOperationException {

        /* Subscribe to the inbound topic */
        subscribe();

        /* Create the callback thread pool */
        executor = Executors.newSingleThreadExecutor();
        logger.log(Level.FINEST, "Callbacks will be invoked using Executor class type \"{0}\"", executor.getClass()
                .getName());

    }

    /**
     * Closes this channel.
     *
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    @Override
    public void close() throws IOException, UnsupportedOperationException {

        /* Cancel any active asynchronous I/O */
        cancelCallbacks();

        try {
            /* Unsubscribe from the inbound topic */
            unsubscribe();
        } catch (Exception e) {
            /* Since we're cleaning up, ignore and carry on */
        }

        /* Finished with the thread pool */
        executor.shutdown();

        /* Tell the end point that we're done */
        endPoint.dispose(this);

        /* Reset */
        outputTopic = null;
        inputTopic = null;
        buffer.clear();

    }

    /**
     * Writes a message to the end point associated with this channel.
     *
     * @param message
     *            the mesage data.
     *
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    @Override
    public void write(byte[] message) throws IOException, UnsupportedOperationException {

        write(message, outputTopic);
    }

    /**
     * Writes a message to the end point associated with this channel.
     *
     * @param message
     *            the mesage data.
     *
     * @param outputTopic
     *            the name of the remote (target) topic.
     *
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    @Override
    public void write(byte[] message, OutputTopic outputTopic) throws IOException, UnsupportedOperationException {

        write(message, outputTopic, MessageQoS.DEFAULT);

    }

    /**
     * Writes a message to the end point associated with this channel.
     *
     * @param message
     *            the message data.
     *
     * @param outputTopic
     *            the name of the remote (target) topic (i.e. topic name).
     *
     * @param qos
     *            the quality of service required for this message.
     *
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    @Override
    public void write(byte[] message, OutputTopic outputTopic, MessageQoS qos) throws IOException,
    UnsupportedOperationException {

        if (outputTopic != null) {

            MqttConfig config = (MqttConfig) endPoint.getConfig();

            try {

                /* If were are not going to try and send this as an MQTT-S message... */
                if (!mqttsEnabled || qos == MessageQoS.RELIABLE
                        || (qos == MessageQoS.DEFAULT && defaultMessageQos == MessageQoS.RELIABLE)) {

                    /* Publish a MQTT message */
                    logger.log(Level.FINEST, "Publishing {0} byte MQTT payload to \"{1}\"", new Object[] {
                            message.length, outputTopic});
                    endPoint.getMqttClient().publish(outputTopic.name(), message, mqttQos, retain);

                } else {

                    /* Generate an MQTT-S message */
                    byte[] finalPacketBytes = buildMqttsMessage(outputTopic.name(), message);

                    /* If the message is still small enough to be sent as a single datagram... */
                    if (effectiveQoS(finalPacketBytes.length, qos) == MessageQoS.BEST_EFFORT) {

                        /* Send it as a datagram */
                        logger.log(Level.FINEST, "Publishing {0} byte MQTT-S payload to \"{1}\"", new Object[] {
                                message.length, outputTopic});
                        DatagramPacket packet = new DatagramPacket(finalPacketBytes, finalPacketBytes.length, endPoint
                                .getDatagramAddress(), config.getIPPort());
                        endPoint.getDatagramSocket().send(packet);

                    } else {

                        /* Fallback to publishing it as an MQTT message */
                        logger.log(Level.FINEST, "Publishing {0} byte MQTT payload to \"{1}\"", new Object[] {
                                message.length, outputTopic});
                        endPoint.getMqttClient().publish(outputTopic.name(), message, mqttQos, retain);

                    }
                }

            } catch (Exception e) {

                logger.log(Level.FINER, "Channel write failed: {0}", e.getMessage());
                throw new IOException(e.getMessage());

            }

        } else {

            throw new IOException("No output topic; cannot publish message");

        }
    }

    /**
     * Builds an MQTT-S message.
     *
     * @param topic
     *            the topic to which the message is to be sent.
     *
     * @param payload
     *            the message payload.
     *
     * @return the MQTT-S message bytes.
     *
     * @throws IOException
     */
    private byte[] buildMqttsMessage(String topic, byte[] payload) throws IOException {

        ByteArrayOutputStream packetBuilder = new ByteArrayOutputStream();

        packetBuilder.write(MQTTS_HEADER);

        /* Add the topic */
        byte[] topicBytes = topic.getBytes();
        packetBuilder.write(new byte[] {(byte) (topicBytes.length / 256), (byte) (topicBytes.length % 256)});
        packetBuilder.write(MQTTS_TOPIC_SEPARATOR);
        packetBuilder.write(topicBytes);

        /* Add the payload */
        packetBuilder.write(payload);

        /* Determine the message length */

        int packetSize = packetBuilder.size();
        byte[] packetSizeBytes = null;

        /* If this is a long message... */
        if (packetSize >= 254) {
            packetSizeBytes = new byte[] {1, (byte) ((packetSize + 3) / 256), (byte) ((packetSize + 3) % 256)};
        } else {
            packetSizeBytes = new byte[] {(byte) (packetSize + 1)};
        }

        /* Compile the full packet */
        ByteArrayOutputStream finalPacket = new ByteArrayOutputStream();
        finalPacket.write(packetSizeBytes);
        packetBuilder.writeTo(finalPacket);
        byte[] finalPacketBytes = finalPacket.toByteArray();

        return finalPacketBytes;
    }

    /**
     * Determines the correct QoS setting to use for a message.
     *
     * @param packetLength
     *            the length of the packet in bytes.
     *
     * @param qos
     *            the requested QoS.
     */
    private MessageQoS effectiveQoS(int packetLength, MessageQoS qos) {

        /* Determine the QoS setting for the message */
        MessageQoS effectiveQoS = (qos == MessageQoS.DEFAULT) ? defaultMessageQos : qos;

        /* If the message is too big to be sent "best effort"... */
        if (effectiveQoS == MessageQoS.BEST_EFFORT && packetLength > maxMqttsPayload) {

            /* Send it reliably */
            effectiveQoS = MessageQoS.RELIABLE;

        }

        return effectiveQoS;

    }

    /**
     * Reads the next message from the end point associated with this channel, blocking until the message is available.
     *
     * @param replyContainer
     *            to hold the reply message.
     *
     * @return the next available message.
     *
     * @throws IOException
     *
     * @throws UnsupportedOperationException
     *
     * @throws IllegalStateException
     *             thrown if this channel is configured for asynchronous I/O (i.e. one or more callbacks has been
     *             registered).
     */
    @Override
    public byte[] read(Message replyContainer) throws IOException, UnsupportedOperationException, IllegalStateException {

        Message incomingMessage = null;

        if (inputTopic != null) {

            /* If there are any callbacks registered... */
            if (callbacks.size() > 0) {
                throw new IllegalStateException("Synchronous I/O attempted on channel configured for asynchronous I/O");
            }

            synchronized (buffer) {

                while (buffer.size() == 0) {
                    try {
                        buffer.wait();
                    } catch (InterruptedException e) {
                    }
                }

                incomingMessage = buffer.remove(0);
            }

            /* If the user wants the full details of the message... */
            if (replyContainer != null) {
                replyContainer.set(incomingMessage);
            }

        } else {

            throw new IOException("No input topic configured; cannot read message");

        }

        return incomingMessage.data;
    }

    /**
     * Asynchronously reads the next available message from the end point associated with this channel.
     *
     * @param callback
     *            invoked when the next message is available.
     *
     * @throws IOException
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void read(ICallback callback) throws IOException, UnsupportedOperationException {

        if (inputTopic != null) {

            if (callback == null) {
                throw new IllegalArgumentException("Callback is null");
            }

            /* Record the callback */
            callbacks.add(callback);

            try {

                /* Initialize it */
                callback.startCallback(null);

            } catch (Exception e) {

                logger.log(Level.WARNING, "Exception in callback: ", e);

            }

        } else {

            throw new IOException("No input topic configured; cannot read message");

        }
    }

    /**
     * Answers the ID of the local (inbound) topic associated with this channel.
     *
     * @return the inbound topic name.
     *
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    @Override
    public InputTopic inputTopic() throws UnsupportedOperationException {

        return inputTopic;
    }

    /**
     * Answers the ID of the (outbound) topic associated with this channel.
     *
     * @return the outbound topic.
     *
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    @Override
    public OutputTopic outputTopic() throws UnsupportedOperationException {

        return outputTopic;
    }

    /**
     * Invoked when a new message arrives on this channel.
     * <p>
     * This method can reject a new message (if, for example, the message buffer is full). The calling code can
     * optionally retry later.
     * </p>
     *
     * @param message
     *            the new message.
     *
     * @return true if the message can be accepted at this time, false otherwise.
     */
    protected boolean messageArrived(Message message) {

        boolean messageAccepted = true;

        synchronized (buffer) {

            /* If there is a callback... */
            if (callbacks.size() > 0) {

                for (ICallback callback : callbacks) {

                    /* Invoke the callback on a thread from the pool */
                    CallbackInvoker invoker = new CallbackInvoker(callback, message);
                    executor.execute(invoker);

                }

            } else {

                if (bufferLimit != 0 && buffer.size() == bufferLimit) {

                    /* We can't handle it */
                    messageAccepted = false;

                } else {

                    /* Add this message to the buffer */
                    buffer.add(message);

                    /* Notify anyone who is waiting for data */
                    buffer.notifyAll();

                }
            }
        }

        return messageAccepted;
    }

    /**
     * Unsubscribe from the reply topic configured for this channel.
     *
     * @throws IOException
     *             thrown if the subscription fails.
     */
    protected void unsubscribe() throws IOException {

        try {

            if (inputTopic != null && !(inputTopic.name() == null || inputTopic.equals(""))) {

                String[] topics = new String[] {inputTopic.name()};
                endPoint.getMqttClient().unsubscribe(topics);
                logger.log(Level.FINER, "Unsubscribed from topic \"{0}\"", inputTopic);

            }

        } catch (Exception e) {

            logger.log(Level.FINER, "Unsubscribe failed: ", e);
            throw new IOException(e.getMessage());

        }
    }

    /**
     * Subscribe to the reply topic (i.e. the inbound port) configured for this channel.
     */
    protected void subscribe() {

        if (inputTopic != null && !(inputTopic.name() == null || inputTopic.equals(""))) {

            String[] subscriptionArray = new String[] {inputTopic.name()};
            int[] mqttQosArray = new int[] {((MqttConfig) endPoint.getConfig()).getMqttQos()};
            boolean subscribed = false;

            while (!subscribed) {

                try {

                    logger.log(Level.FINER, "Subscribing to topic \"{0}\"", new Object[] {subscriptionArray[0]});
                    endPoint.getMqttClient().subscribe(subscriptionArray, mqttQosArray);
                    subscribed = true;

                } catch (Exception e) {

                    logger.log(Level.WARNING, "Subscription failed: ", e);

                    /* Wait before retrying */
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                    }

                }
            }
        }
    }

    /**
     * Ensures that a topic name is non-null and non-empty, generating a random name if required.
     *
     * @return the topic name.
     */
    private String toValidTopic(Topic topic) {

        String validTopic = null;

        if (topic == null) {
            /* Do nothing */
        } else if (topic.name().equals("")) {
            validTopic = "topic" + RandomID.generate(32);
        } else {
            validTopic = topic.name();
        }

        return validTopic;
    }

    /**
     * @see fabric.core.io.Channel#cancelCallback(fabric.core.io.ICallback)
     */
    @Override
    public Object cancelCallback(ICallback callback) {

        ICallback oldCallback = null;

        if (callbacks.contains(callback)) {

            synchronized (buffer) {

                try {

                    /* Invoke the callback's clean up method */
                    callback.cancelCallback(null);

                } catch (Exception e) {

                    logger.log(Level.WARNING, "Exception in callback: ", e);

                }

                callbacks.remove(callback);
                oldCallback = callback;

            }
        }

        return oldCallback;
    }

    /**
     * @see fabric.core.io.Channel#cancelCallbacks()
     */
    @Override
    public void cancelCallbacks() {

        synchronized (buffer) {

            ArrayList<ICallback> callbacksCopy = (ArrayList<ICallback>) callbacks.clone();

            for (ICallback callback : callbacksCopy) {
                cancelCallback(callback);
            }

        }
    }
}
