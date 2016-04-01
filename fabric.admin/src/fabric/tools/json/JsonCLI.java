/*
 * (C) Copyright IBM Corp. 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */
package fabric.tools.json;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

public class JsonCLI {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    private final static String CLASS_NAME = JsonCLI.class.getSimpleName();
    private final static Logger logger = Logger.getLogger(CLASS_NAME);

    private Properties cliProperties = new Properties();

    private JsonClient jsonClient = null;

    public JsonCLI(String configPropertyFileName) {
        // Configuration required for Test clients.
        try {
            cliProperties.load(new FileInputStream(configPropertyFileName));
            setUp(cliProperties);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        logger.finest("Starting");

        JsonCLI jsonCLI = new JsonCLI(System.getProperty("config"));

        if (args.length > 0) {
            try {
                jsonCLI.start(args);
            } catch (UnsupportedOperationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("JsonCLI started");
            jsonCLI.start();
            System.out.println("JsonCLI exited");
        }

    }

    protected void setUp(Properties props) throws IOException {

        jsonClient = new JsonClient(props, CLASS_NAME + System.currentTimeMillis());
        JsonCLIResponseListener listener = new JsonCLIResponseListener(jsonClient);
        listener.start();
    }

    public void start() {

        Scanner scanner = new Scanner(System.in);
        String input = "";

        System.out.println("Setting up the client ....");

        try {
            // Setup platform-type
            String registerPlatformType = "{\"op\":\"register:platform-type\",\"type\":\"JsonCLIPlatformType\",\"desc\":\"CLI Platform Type\",\"correl\":\"1\"}";
            System.out.println(registerPlatformType);
            jsonClient.publish(registerPlatformType);
            sleep(1000);

            // Setup platform
            String registerPlatform = "{\"op\":\"register:platform\",\"id\":\"JsonCLIPlatform\",\"type\":\"JsonCLIPlatformType\",\"correl\":\"2\"}";
            System.out.println(registerPlatform);
            jsonClient.publish(registerPlatform);
            sleep(1000);

            // Setup service-type
            String registerServiceType = "{\"op\":\"register:service-type\",\"type\":\"JsonCLIServiceType\",\"mode\":\"input-feed\",\"correl\":\"3\"}";
            System.out.println(registerServiceType);
            jsonClient.publish(registerServiceType);
            sleep(1000);

            // Setup system-type
            String registerSystemType = "{\"op\":\"register:system-type\",\"type\":\"JsonCLISystemType\",\"services\":[{\"type\":\"JsonCLIServiceType\"}],\"correl\":\"4\"}";
            System.out.println(registerSystemType);
            jsonClient.publish(registerSystemType);
            sleep(1000);

            // Setup system
            String registerSystem = "{\"op\":\"register:system\",\"id\":\"JsonCLIPlatform/JsonCLISystem\",\"type\":\"JsonCLISystemType\",\"desc\":\"JsonCLISubscriber\",\"correl\":\"5\"}";
            System.out.println(registerSystem);
            jsonClient.publish(registerSystem);
            sleep(1000);

            // Set system to running
            String setSystemRunning = "{\"op\":\"state:system\",\"id\":\"JsonCLIPlatform/JsonCLISystem\",\"state\":\"running\",\"correl\":\"6\"}";
            System.out.println(setSystemRunning);
            jsonClient.publish(setSystemRunning);
            sleep(1000);

            System.out.println("Reading for subscription messages");

            while (!input.equalsIgnoreCase("exit")) {
                System.out
                        .println("EXAMPLE subscribe to */*/* : {\"op\":\"subscribe\",\"output-feeds\":[\"*/*/*\"],\"input-feed\":\"JsonCLIPlatform/JsonCLISystem/JsonCLIServiceType\",\"correl\":\"7\"} ");
                System.out
                        .println("EXAMPLE unsubscribe : {\"op\":\"unsubscribe\",\"input-feed\":\"JsonCLIPlatform/JsonCLISystem/JsonCLIServiceType\",\"correl\":\"8\"}");
                System.out.print("?");
                input = scanner.nextLine();
                if (!input.equalsIgnoreCase("exit")) {
                    try {
                        if (!input.isEmpty() && input != null && !(input.length() == 0)) {

                            jsonClient.publish(input);
                        }
                    } catch (MqttPersistenceException e) {
                        e.printStackTrace();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

            }
        } catch (MqttException e) {
            e.printStackTrace();
        }

        scanner.close();
        System.exit(0);
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    void start(String[] args) throws UnsupportedOperationException, IOException {
        logger.info("Reading commands from file : " + args[0]);
        // Open file and read in commands
        Scanner scanner = new Scanner(System.in);
        BufferedReader b = new BufferedReader(new FileReader(args[0]));
        String currentLine = null;
        while ((currentLine = b.readLine()) != null) {
            try {
                if (cliProperties.getProperty("jsoncli.batch.mode", "prompt").equalsIgnoreCase("prompt")) {
                    System.out.println("\n Press enter to send this command.");
                    System.out.println("COMMAND : " + currentLine);
                    scanner.nextLine();
                }
                if (currentLine != null && currentLine != "" && !currentLine.isEmpty()) {
                    jsonClient.publish(currentLine);
                    // Pause for a moment to allow a response
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // Expected
                    }
                }
            } catch (MqttPersistenceException e) {
                e.printStackTrace();
            } catch (MqttException e) {
                e.printStackTrace();
            }

        }
        b.close();
        // Press enter to exit (allows waiting for final responses to appear.
        System.out.println("Press enter to terminate the CLI.");
        scanner.nextLine();
        scanner.close();
    }

}
