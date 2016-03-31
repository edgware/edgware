/*
 * (C) Copyright IBM Corp. 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.tools;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Logger;

import fabric.client.FabricClient;
import fabric.core.io.mqtt.MqttConfig;
import fabric.registry.exception.PersistenceException;
import fabric.registry.persistence.Persistence;
import fabric.registry.persistence.PersistenceManager;
import fabric.registry.persistence.distributed.DistributedJDBCPersistence;
import fabric.registry.persistence.distributed.DistributedQueryResult;

public class DistributedRegistryCLI {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    private final static String PACKAGE_NAME = DistributedRegistryCLI.class.getPackage().getName();
    private final static Logger logger = Logger.getLogger(PACKAGE_NAME);

    private Properties cliProperties = new Properties();
    DistributedJDBCPersistence persistence = null;

    FabricClient fabricClient;

    public DistributedRegistryCLI(String configPropertyFileName) {
        // Configuration required for Test clients.
        try {
            cliProperties.load(new FileInputStream(configPropertyFileName));
            setUp();
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
        DistributedRegistryCLI registry = new DistributedRegistryCLI(System.getProperty("config",
                "../config/DistributedRegistryCLI.properties"));
        if (args.length > 0) {
            registry.start(args);
        } else {
            System.out.println("RegistryCLI started");
            registry.start();
            System.out.println("RegistryCLI exited");
        }

    }

    public void start() {

        Scanner scanner = new Scanner(System.in);
        String input = "";

        while (!input.equalsIgnoreCase("exit")) {
            System.out.print("?");
            input = scanner.nextLine();
            if (!input.equalsIgnoreCase("exit")) {
                String response = issueQuery(input);
                System.out.println(response);
            }

        }
        scanner.close();
        fabricClient.close();
        System.exit(0);
    }

    void start(String[] args) throws UnsupportedOperationException {
        String response = issueQuery(args[0]);
        fabricClient.close();
        System.out.println(response);
    }

    protected void setUp() {

        String PLATFORM_NAME = MqttConfig.generateClient("RQA_");
        String ACTOR_NAME = "DistributedRegistryCLI";
        logger.finest("Creating a Fabric Client using Actor = " + ACTOR_NAME + " and platform " + PLATFORM_NAME);
        try {
            System.setProperty("fabric.node", cliProperties.getProperty("fabric.node"));
            fabricClient = new FabricClient(ACTOR_NAME, PLATFORM_NAME);
            fabricClient.connect();
        } catch (Exception e) {
            logger.warning("Error occured connecting the Client");
            e.printStackTrace();
        }
        Persistence pm = PersistenceManager.getPersistence();
        if (pm != null && pm instanceof DistributedJDBCPersistence) {
            persistence = (DistributedJDBCPersistence) pm;
        } else {
            System.out.println("UNABLE TO FIND A DISTRIBUTED REGISTRY PERSISTENCE LAYER!!");
            System.exit(-1);
        }
    }

    /**
     * Send Query to Edgware Node
     * 
     * @param query
     * @return
     */
    private String issueQuery(String query) {
        String response = query + "\n";
        DistributedQueryResult results = null;
        try {
            results = persistence.distributedQuery(query, false);
            response = results.toString();
            response = response + "\nLast Query : " + query;
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return response;
    }
}
