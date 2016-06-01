/*
 * (C) Copyright IBM Corp. 2013, 2016
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.script;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import fabric.core.json.JSON;
import fabric.services.jsonclient.utilities.AdapterConstants;

public abstract class JSONScript implements MqttCallback {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2013, 2016";

    /*
     * Script directives
     */
    private static final String DIRECTIVE_DEFINE = "$define";
    private static final String DIRECTIVE_END = "$$";
    private static final String DIRECTIVE_ENDREPEAT = "$endrepeat";
    private static final String DIRECTIVE_EOF = "$eof";
    private static final String DIRECTIVE_PAUSE = "$pause";
    private static final String DIRECTIVE_REPEAT = "$repeat";
    private static final String DIRECTIVE_EXPECT = "$expect";
    private static final String DIRECTIVE_SEND = "$send";
    private static final String DIRECTIVE_SLEEP = "$sleep";
    private static final String DIRECTIVE_WAITFOR = "$waitfor";

    /*
     * Logging categories
     */
    protected static final String DEFINE = "DEF";
    protected static final String INFORMATION = "INFO";
    protected static final String PAUSE = "PAUSE";
    protected static final String RECEIVED = "RCV";
    protected static final String REPEATING = "RPT";
    protected static final String RESPONSE_EXPECTING = "EXP";
    protected static final String RESPONSE_FAILED = "BAD";
    protected static final String RESPONSE_OK = "OK ";
    protected static final String RUNTIME_ERROR = "ERR";
    protected static final String RUNTIME_WARNING = "WARN";
    protected static final String SCRIPT_COMPLETE = "SCRIPT COMPLETE";
    protected static final String SCRIPT_ERROR = "SCRIPT ERROR";
    protected static final String SENDING = "SND";
    protected static final String SLEEPING = "SLP";
    protected static final String WAITING = "WAIT";

    /*
     * Script parsing states
     */
    private static final int STATE_NONE = 1;
    private static final int STATE_SEND = 2;
    private static final int STATE_EXPECT = 3;
    private static final int STATE_WAITFOR = 4;

    /*
     * Class fields
     */

    protected MqttClient mqttClient = null;
    protected String clientID = null;
    protected String sendToAdapterTopic = null;
    protected String receiveFromAdapterTopic = null;
    protected int correlID = 0;
    protected BufferedReader commandLineReader = null;
    protected String disconnectMessage = null;
    protected BufferedReader scriptReader = null;
    protected String scriptFile = null;
    protected ArrayList<String> script = new ArrayList<String>();
    protected HashMap<String, String> expected = new HashMap<String, String>();
    protected int runtimeErrors = 0;
    protected int scriptErrors = 0;
    protected int responseErrors = 0;
    protected int runtimeWarnings = 0;
    protected Stack<Repeat> repeats = new Stack<Repeat>();
    protected String waitForMessage = null;
    protected boolean waitingForMessage = false;
    protected HashMap<String, String> symbols = new HashMap<String, String>();

    /*
     * Inner classes
     */
    private class Repeat {

        int maxIterations = -1;
        int iterationCount = 0;
        int startLine = 0;

    }

    /*
     * Class methods
     */

    public void init(String clientID, String node, String nodeIp, String port, String scriptFile) throws IOException {

        if (scriptFile != null) {
            this.scriptFile = scriptFile;
            Reader messageReader = new InputStreamReader(new FileInputStream(scriptFile));
            this.scriptReader = new BufferedReader(messageReader);
            log(INFORMATION, "Executing script [" + scriptFile + "]");
        }

        Reader consoleReader = new InputStreamReader(System.in);
        commandLineReader = new BufferedReader(consoleReader);

        // this.clientID = MqttConfig.generateClient(clientID);
        this.clientID = clientID;

        sendToAdapterTopic = "$fabric/" + node + "/$adapters/$mqtt/$in/" + this.clientID;
        log(INFORMATION, String.format("Sending to topic [%s]", sendToAdapterTopic));

        receiveFromAdapterTopic = "$fabric/" + node + "/$adapters/$mqtt/$out/" + this.clientID;
        log(INFORMATION, String.format("Listening to topic [%s]", receiveFromAdapterTopic));

        disconnectMessage = String.format("{\"op\":\"disconnect\",\"client-id\":\"%s\"}", this.clientID);

        try {

            mqttClient = new MqttClient("tcp://" + nodeIp + ":" + port, clientID, null);
            mqttClient.setCallback(this);
            connect();
            subscribe();

        } catch (MqttException e) {

            throw new IOException(e.getMessage());

        }
    }

    /**
     * Read the script file and prepare directives for execution.
     * <p>
     * Directives are categorized into two types:
     * <ol>
     * <li><em>compile-time:</em> those that require additional pre-processing prior to script execution</li>
     * <li><em>run-time:</em> directive that may be executed directly
     * </ol>
     * </p>
     *
     * @throws IOException
     */
    protected void readScript() throws IOException {

        String nextLine = null;
        int lineCount = 0;
        StringBuilder jsonObject = new StringBuilder();
        int state = STATE_NONE;

        while (!DIRECTIVE_EOF.equals(nextLine)) {

            nextLine = scriptReader.readLine();

            if (nextLine == null) {
                /* End of file */
                nextLine = DIRECTIVE_EOF;
            }

            nextLine = nextLine.trim();
            lineCount++;

            if (nextLine.equals("")) {

                /* Blank line, ignore */

            } else if (nextLine.startsWith("#")) {

                /* Comment, record for diagnostic purposes */
                script.add(substituteSymbols(nextLine));

            } else {

                switch (state) {

                    case STATE_NONE:

                        if (nextLine.startsWith(DIRECTIVE_DEFINE)) {

                            String expandedDirective = directiveDefine(nextLine, lineCount);

                            /* Record for diagnostic purposes */
                            script.add(expandedDirective);

                        } else if (nextLine.startsWith(DIRECTIVE_SEND)) {

                            state = STATE_SEND;

                        } else if (nextLine.startsWith(DIRECTIVE_EXPECT)) {

                            state = STATE_EXPECT;

                        } else if (nextLine.startsWith(DIRECTIVE_WAITFOR)) {

                            state = STATE_WAITFOR;

                        } else if (nextLine.equals(DIRECTIVE_EOF)) {

                            /* EOF, ignore */

                        } else if (nextLine.startsWith("$")) {

                            /* Run-time directive, record for execution */
                            script.add(substituteSymbols(nextLine));

                        } else {

                            log(SCRIPT_ERROR, String.format("[Line %d] Unrecognised directive [%s]", lineCount,
                                    nextLine));

                        }
                        break;

                    case STATE_SEND:
                    case STATE_EXPECT:
                    case STATE_WAITFOR:

                        if (nextLine.equals(DIRECTIVE_END) || nextLine.equals(DIRECTIVE_EOF)) {

                            switch (state) {

                                case STATE_SEND:

                                    saveMessage(jsonObject, lineCount);
                                    jsonObject.setLength(0);
                                    break;

                                case STATE_EXPECT:

                                    saveExpected(jsonObject, lineCount);
                                    jsonObject.setLength(0);
                                    break;

                                case STATE_WAITFOR:

                                    saveWaitForDirective(jsonObject, lineCount);
                                    jsonObject.setLength(0);
                                    break;
                            }

                            state = STATE_NONE;

                        } else if (nextLine.startsWith("$")) {

                            log(SCRIPT_ERROR, String.format("[Line %s] Unexpected directive [%s]", lineCount, nextLine));

                        } else {

                            jsonObject.append(nextLine);

                        }

                        break;
                }
            }
        }
    }

    /**
     * Sends a message to the Fabric.
     *
     * @param msg
     *            the message to send.
     *
     * @throws MqttException
     * @throws MqttPersistenceException
     */
    protected void sendMessage(String msg) throws MqttException, MqttPersistenceException {

        String correlID = messageCorrelID(msg);
        correlID = (correlID != null) ? ':' + correlID : "";
        log(SENDING + correlID, msg);
        mqttClient.getTopic(sendToAdapterTopic).publish(msg.getBytes(), 2, false);

    }

    private String normaliseJSON(String jsonString, int lineCount) {

        String normalisedJSON = "";

        if (jsonString != null && !jsonString.trim().equals("")) {

            try {

                JSON json = new JSON(jsonString);
                normalisedJSON = json.toString().trim();

            } catch (Exception e) {

                log(SCRIPT_ERROR, String.format("[Line %d] Invalid JSON object [%s]", lineCount, jsonString));

            }
        }

        return normalisedJSON;
    }

    /**
     * Substitutes symbols in the code with their corresponding values.
     *
     * @param line
     *            the line to be expanded.
     *
     * @return the expanded line.
     */
    private String substituteSymbols(String line) {

        String expandedLine = line;

        for (String key : symbols.keySet()) {

            String symbol = "\\$\\$" + key + "\\$\\$";
            String value = symbols.get(key);
            expandedLine = expandedLine.replaceAll(symbol, value);

        }

        return expandedLine;
    }

    private void saveMessage(StringBuilder buffer, int lineCount) {

        String expandedJson = substituteSymbols(buffer.toString());
        String normalisedJson = normaliseJSON(expandedJson, lineCount);

        if (!normalisedJson.equals("")) {
            script.add(normalisedJson);
        }
    }

    private void saveExpected(StringBuilder buffer, int lineCount) {

        String expandedJson = substituteSymbols(buffer.toString());
        String normalisedJson = normaliseJSON(expandedJson, lineCount);

        if (!normalisedJson.equals("")) {

            String correlID = messageCorrelID(normalisedJson);

            if (!expected.containsKey(correlID)) {
                expected.put(correlID, normalisedJson);
            } else {
                log(SCRIPT_ERROR, String.format("[Line %d] Duplicate correlation ID [%s]", lineCount, correlID));
            }
        }
    }

    private void saveWaitForDirective(StringBuilder buffer, int lineCount) {

        String expandedJson = substituteSymbols(buffer.toString());
        String normalisedJson = normaliseJSON(expandedJson, lineCount);

        if (!normalisedJson.equals("")) {
            script.add(DIRECTIVE_WAITFOR + ' ' + normalisedJson);
        } else {
            log(SCRIPT_ERROR, String.format("[Line %d] Usage: %s <expected-json-mesage>", lineCount, DIRECTIVE_WAITFOR));
        }
    }

    protected void logUnmatchedExpected() {

        Iterator<String> i = expected.keySet().iterator();

        if (i.hasNext()) {

            StringBuilder unmatched = new StringBuilder("Unmatched expected: ");

            while (i.hasNext()) {

                unmatched.append(i.next());

                if (i.hasNext()) {
                    unmatched.append(',');
                }

            }

            log(SCRIPT_COMPLETE, unmatched.toString());
        }
    }

    protected void logSummary() {
        log(SCRIPT_COMPLETE, String
                .format("%s: %d, %s: %d, %s: %d, %s: %d", "Script errors", scriptErrors, "Run-time errors",
                        runtimeErrors, "Run-time warnings", runtimeWarnings, "Bad responses", responseErrors));
        logUnmatchedExpected();
    }

    /**
     * Answers the correlation ID from a JSON message.
     *
     * @param jsonString
     *            the JSON object containing the correlation ID.
     *
     * @return the correlation ID, or <code>null</code> if none.
     */
    protected String messageCorrelID(String jsonString) {

        String correlID = null;

        try {

            JSON json = new JSON(jsonString);
            correlID = json.getString(AdapterConstants.FIELD_CORRELATION_ID);

        } catch (Exception e) {

            log(SCRIPT_ERROR, String.format("Invalid JSON object in script [%s]", jsonString));

        }

        return correlID;
    }

    /**
     * Decodes and executes a script directive.
     *
     * @param directive
     *            the directive to execute.
     *
     * @param crntLine
     *            the current line number in the script.
     *
     * @return the next line to execute in the script.
     */
    protected int executeDirective(String directive, int crntLine) {

        int nextLine = crntLine;

        if (directive.startsWith(DIRECTIVE_DEFINE)) {

            /* Already processed, so just log */
            log(DEFINE, directive);

        } else if (directive.startsWith(DIRECTIVE_ENDREPEAT)) {

            nextLine = directiveEndRepeat(directive, crntLine);

        } else if (directive.startsWith(DIRECTIVE_PAUSE)) {

            directivePause(directive);

        } else if (directive.startsWith(DIRECTIVE_REPEAT)) {

            directiveRepeat(directive, crntLine);

        } else if (directive.startsWith(DIRECTIVE_SLEEP)) {

            directiveSleep(directive);

        } else if (directive.startsWith(DIRECTIVE_WAITFOR)) {

            directiveWaitFor(directive);

        } else {

            log(SCRIPT_ERROR, String.format("Unrecognised run-time directive [%s]", directive));

        }

        return nextLine;
    }

    /**
     * Answers the parameter associated with a directive (if any).
     *
     * @param directive
     *            the name of the directive.
     *
     * @param line
     *            the full line containing the directive and its parameter (if any).
     *
     * @return the directive parameter, or the empty string (<code>""</code>) if none.
     */
    private String extractParameter(String directive, String line) {
        String detail = line.substring(directive.length()).trim();
        return detail;
    }

    private String directiveDefine(String directive, int lineCount) {

        String symbolDefinition = extractParameter(DIRECTIVE_DEFINE, directive);
        String[] definitionParts = symbolDefinition.split("=");
        String symbol = null;
        String definitionName = null;
        String definitionValue = null;

        if (definitionParts.length == 2) {

            symbol = definitionParts[0].trim();
            definitionName = definitionParts[1].trim();
            definitionValue = null;

            if (definitionName.startsWith("$")) {
                definitionValue = System.getenv(definitionName.substring(1));
            } else {
                definitionValue = System.getProperty(definitionName);
            }

            if (definitionValue != null) {
                symbols.put(symbol, definitionValue);
            } else {
                log(SCRIPT_ERROR, String.format("[Line %d] %s: symbol not found [%s]", lineCount, DIRECTIVE_DEFINE,
                        definitionName));
            }

        } else {

            log(SCRIPT_ERROR, String.format(
                    "[Line %d] Usage: %s <symbol-name>=<environment-variable>|<java-system-property>", lineCount,
                    DIRECTIVE_DEFINE));

        }

        return String.format("%s %s=%s", DIRECTIVE_DEFINE, symbol, definitionValue);
    }

    /**
     * Executes the <code>endrepeat</code> directive.
     *
     * @param directive
     *
     * @param crntLine
     *
     * @return the point in the script at which execution should continue.
     */
    private int directiveEndRepeat(String directive, int crntLine) {

        int nextLine = crntLine;

        try {

            Repeat crntRepeat = repeats.peek();
            int numRepeatLoops = repeats.size();
            crntRepeat.iterationCount++;

            if (crntRepeat.maxIterations == -1 || crntRepeat.iterationCount < crntRepeat.maxIterations) {

                nextLine = crntRepeat.startLine;

            } else {

                repeats.pop();

            }

            log(REPEATING, String.format("Loop %d completed iteration %d of %d", numRepeatLoops,
                    crntRepeat.iterationCount, crntRepeat.maxIterations));

        } catch (EmptyStackException e) {

            log(SCRIPT_ERROR, String.format("%s: no matching %s", DIRECTIVE_ENDREPEAT, DIRECTIVE_REPEAT));

        }

        return nextLine;
    }

    /**
     * Executes the <code>pause</code> directive.
     *
     * @param directive
     */
    private void directivePause(String directive) {

        String prompt = extractParameter(DIRECTIVE_PAUSE, directive);
        prompt = prompt.equals("") ? "Press enter to continue..." : prompt;

        try {

            log(PAUSE, prompt);
            commandLineReader.readLine();

        } catch (IOException e) {

            e.printStackTrace();

        }
    }

    /**
     * Executes the <code>repeat</code> directive.
     *
     * @param directive
     *
     * @param crntLine
     */
    private void directiveRepeat(String directive, int crntLine) {

        String iterations = extractParameter(DIRECTIVE_REPEAT, directive);
        Repeat repeat = new Repeat();

        try {

            if (!iterations.equals("")) {

                repeat.maxIterations = Integer.parseInt(iterations);
                log(REPEATING, String
                        .format("Loop %d starting %d iterations", repeats.size() + 1, repeat.maxIterations));

            }

            repeat.startLine = crntLine;
            repeats.push(repeat);

        } catch (IllegalArgumentException e) {

            log(SCRIPT_ERROR, String.format("%s: invalid repeat count [%s]", DIRECTIVE_REPEAT, iterations));

        }
    }

    /**
     * Executes the <code>sleep</code> directive.
     *
     * @param directive
     */
    private void directiveSleep(String directive) {

        String secs = extractParameter(DIRECTIVE_SLEEP, directive);

        try {

            long duration = Long.parseLong(secs);
            long endTime = System.currentTimeMillis() + (duration * 1000);

            log(SLEEPING, String.format("Sleeping %d seconds", duration));

            while (System.currentTimeMillis() < endTime) {
                sleep(1);
            }

        } catch (IllegalArgumentException e) {

            log(SCRIPT_ERROR, String.format("%s: invalid sleep duration [%s]", DIRECTIVE_SLEEP, secs));

        }
    }

    /**
     * Executes the <code>waitfor</code> directive.
     *
     * @param directive
     */
    private void directiveWaitFor(String directive) {

        waitForMessage = extractParameter(DIRECTIVE_WAITFOR, directive);

        if (!waitForMessage.equals("")) {

            waitingForMessage = true;

            log(WAITING, String.format("Waiting for message [%s]", waitForMessage));

            while (waitingForMessage) {
                sleep(1);
            }

            log(WAITING, "Continuing");

        } else {

            log(SCRIPT_ERROR, String.format("Usage: %s <expected-json-message>", DIRECTIVE_WAITFOR));

        }
    }

    /**
     * Answers the name of the script file.
     *
     * @return the script file name.
     */
    protected String scriptFileName() {
        return scriptFile;
    }

    /**
     * Sends a list of messages to the Fabric.
     *
     * @param messageList
     *            the message list.
     *
     * @throws MqttPersistenceException
     * @throws MqttException
     */
    protected void sendMessageList(String[] messageList) throws MqttPersistenceException, MqttException {
        for (String op : messageList) {
            send(op);
        }
    }

    /**
     * Suspend the executing thread (i.e. sleep) for the number of seconds specified.
     *
     * @param duration
     *            the number of seconds to sleep.
     */
    protected void sleep(long duration) {
        try {
            Thread.sleep(duration * 1000);
        } catch (InterruptedException e) {
        }
    }

    public void send(String string) throws MqttPersistenceException, IllegalArgumentException, MqttException {

        pause(string);
        mqttClient.getTopic(sendToAdapterTopic).publish(string.getBytes(), 2, false);

    }

    protected void log(String category, String body) {

        String message = String.format("[%-20s] %s", category, body);
        System.out.println(message);

        if (category.contains(RUNTIME_ERROR)) {
            runtimeErrors++;
        } else if (category.contains(RUNTIME_WARNING)) {
            runtimeWarnings++;
        } else if (category.contains(RESPONSE_FAILED)) {
            responseErrors++;
        } else if (category.contains(SCRIPT_ERROR)) {
            scriptErrors++;
        }

    }

    protected void pause(String message) {

        try {
            log(PAUSE, message);
            commandLineReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void runScript() throws MqttPersistenceException, MqttException, IOException {

        readScript();

        for (int n = 0; n < script.size(); n++) {

            String next = script.get(n);

            if (next.startsWith("#")) {

                /* Comment */
                log("", next);

            } else if (next.startsWith("$")) {

                n = executeDirective(next, n);

            } else {

                sendMessage(next);

            }
        }

        logSummary();
    }

    protected void connect() {

        boolean connected = false;

        while (!connected) {

            try {

                log(INFORMATION, String.format("Connecting to broker [%S] as [%s]", mqttClient.getServerURI(),
                        mqttClient.getClientId()));

                MqttConnectOptions co = new MqttConnectOptions();
                co.setCleanSession(true);
                co.setKeepAliveInterval(60);
                co.setWill(sendToAdapterTopic, disconnectMessage.getBytes(), 2, false);
                mqttClient.connect(co);
                connected = true;

            } catch (Exception e) {

                log(RUNTIME_ERROR, String.format("MQTT connection failed: %s", e.getMessage()));

                /* Wait before retrying */
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e1) {
                    log(RUNTIME_WARNING, String.format("Sleep interrupted: %s", e1.getMessage()));
                }
            }
        }
    }

    protected void subscribe() {

        boolean subscribed = false;

        while (!subscribed) {

            try {

                String[] topics = new String[] {receiveFromAdapterTopic};

                StringBuilder subscribingTo = new StringBuilder("Subscribing to topics: ");
                for (String topic : topics) {
                    subscribingTo.append(String.format("[%s]", topic));
                }
                log(INFORMATION, subscribingTo.toString());

                mqttClient.subscribe(topics, new int[] {2});
                subscribed = true;

            } catch (Exception e) {

                log(RUNTIME_ERROR, String.format("MQTT subscription failed: %s", e.getMessage()));

                /* Wait before retrying */
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    log(RUNTIME_WARNING, String.format("Sleep interrupted: %s", e1.getMessage()));
                }

            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.paho.client.mqttv3.MqttCallback#messageArrived(java.lang.String,
     * org.eclipse.paho.client.mqttv3.MqttMessage)
     */
    @Override
    public void messageArrived(String arg0, MqttMessage message) throws Exception {

        String payloadString = new String(message.getPayload());
        boolean printDefaultResponse = true;

        try {

            JSON messageJSON = new JSON(payloadString);
            String messageJSONAsString = messageJSON.toString();
            String correl = messageJSON.getString(AdapterConstants.FIELD_CORRELATION_ID);
            String expectedJSONAsString = null;

            if (correl != null && (expectedJSONAsString = expected.get(correl)) != null) {

                if (messageJSONAsString.equals(expectedJSONAsString)) {
                    log(RESPONSE_OK + ':' + correl, expectedJSONAsString);
                } else {
                    log(RESPONSE_FAILED + ':' + correl, messageJSONAsString);
                    log(RESPONSE_EXPECTING + ':' + correl, expectedJSONAsString);
                }

                expected.remove(correl);
                printDefaultResponse = false;

            }

            if (waitingForMessage && messageJSONAsString.equals(waitForMessage)) {
                waitForMessage = null;
                waitingForMessage = false;
            }

        } catch (Exception e) {
        }

        if (printDefaultResponse) {
            log(RECEIVED, payloadString);
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.paho.client.mqttv3.MqttCallback#deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken)
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken arg0) {

    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.paho.client.mqttv3.MqttCallback#connectionLost(java.lang.Throwable)
     */
    @Override
    public void connectionLost(Throwable t) {

        log(INFORMATION, String.format("MQTT connection lost (%s) re-trying", t.getMessage()));
        connect();
        subscribe();
        log(INFORMATION, "Reconnected and resubscribed");

    }

    protected void stop() throws IllegalArgumentException, MqttException, IOException {

        String[] topics = new String[] {receiveFromAdapterTopic};
        mqttClient.unsubscribe(topics);
        mqttClient.disconnect();
        commandLineReader.close();
        if (scriptReader != null) {
            scriptReader.close();
        }

    }
}