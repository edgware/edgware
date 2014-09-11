/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.trigger;

import java.util.Arrays;
import java.util.List;

import fabric.Fabric;

/**
 * Class that manages a Fabric Connection used by the Java UDF.
 */
public class ResourceManager {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2014";

	/*
	 * Class constants
	 */

	/* The reporting levels for Registry notification messages */
	public static final int REPORT_NONE = 0;
	public static final int REPORT_LIST = 1;
	public static final int REPORT_ALL = 2;

	/*
	 * Class static fields
	 */

	private static ResourceManager instance = null;

	/** Flag indicating if debug messages are to be sent to standard output. */
	private static int debug = -1;

	/** Flag indicating if triggers are active (set from the FABRIC_TRIGGERS environment variable). */
	private static boolean fireTriggers = true;

	/** The list of reportable tables. */
	private List<String> reportableTableList = null;

	/*
	 * Class fields
	 */

	/** Manages the connection to the Fabric, used to distribute notification messages. */
	private FabricConnection fabricConnection = null;

	/** The reporting level for Registry notification messages (from the "registry.feed.level" configuration property). */
	private int reportLevel = REPORT_LIST;

	/*
	 * Class methods
	 */

	private ResourceManager() {

		/* Determine if triggers should be active */

		String fabricTriggers = System.getenv("FABRIC_TRIGGERS");
		fireTriggers = (fabricTriggers != null) ? Boolean.parseBoolean(fabricTriggers) : true;

		if (!fireTriggers) {
			System.out.println("Fabric triggers disabled.");
		}

	}

	/**
	 * Get the single instance of the ResourceManager which will connect to the local Fabric node.
	 * 
	 * @return the instance.
	 */
	public synchronized static ResourceManager getInstance() {

		if (instance == null) {
			instance = new ResourceManager();
			instance.init();
		}
		return instance;
	}

	/**
	 * Initialise the Fabric connection and open a channel.
	 */
	private void init() {

		final ResourceManager resourceManager = this;

		if (fireTriggers) {

			/* Start the thread handling Fabric I/O and wait for it to initialise */
			fabricConnection = new FabricConnection();
			fabricConnection.start();

			/* Determine what level of reporting is configured */
			Fabric fabric = new Fabric();
			fabric.initFabricConfig();
			String feedLevel = fabric.config("registry.notifications.level");
			feedLevel = (feedLevel != null) ? feedLevel.toUpperCase() : "ALL";

			switch (feedLevel) {

			case "ALL":

				reportLevel = REPORT_ALL;
				System.out.println("Fabric triggers will be sent for all tables");
				break;

			case "NONE":

				reportLevel = REPORT_NONE;
				System.out.println("Fabric triggers will be sent for no tables");
				break;

			default:

				/* Specific tables encoded into the level */
				String[] reportableTables = feedLevel.split(",");
				reportableTableList = Arrays.asList(reportableTables);

				StringBuilder message = new StringBuilder();
				for (int r = 0; r < reportableTables.length; r++) {
					message.append(reportableTables[r]);
					if (r < reportableTables.length - 1) {
						message.append(", ");
					}
				}
				System.out.println("Fabric triggers will be sent for " + reportableTables.length + " tables: "
						+ message.toString());
				reportLevel = REPORT_LIST;
			}
		}
	}

	/**
	 * Answers <code>true</code> if changes to the specified table are reportable, <code>false</code> otherwise.
	 * 
	 * @param table
	 *            the table to check.
	 * 
	 * @return the reportability status.
	 */
	public boolean isReportable(String table) {

		boolean isReportable = false;

		if (fireTriggers) {

			switch (reportLevel) {

			case REPORT_NONE:

				break;

			case REPORT_ALL:

				isReportable = true;
				break;

			case REPORT_LIST:

				table = table.toUpperCase();

				if (reportableTableList.contains(table)) {
					isReportable = true;
				}

				break;
			}
		}

		return isReportable;
	}

	/**
	 * Publishes a Registry notification to the Fabric.
	 * 
	 * @param updateMessage
	 *            the Registry update notification message.
	 * 
	 * @throws Exception
	 *             thrown if the message cannot be sent.
	 */
	public void sendRegistryUpdate(String updateMessage) throws Exception {

		if (fireTriggers) {
			debug(updateMessage);
			fabricConnection.sendRegistryUpdate(updateMessage);
		}
	}

	/**
	 * Simple, configurable debug; if the environment variable <code>FABRIC_DEBUG</code> is set to <code>true</code>
	 * then debug messages will be sent to standard output.
	 * 
	 * @param message
	 *            the debug message.
	 */
	public static void debug(String message) {

		if (debug == -1) {
			String doDebug = System.getenv("FABRIC_DEBUG");
			if (doDebug != null && doDebug.equals("true")) {
				debug = 1;
			} else {
				debug = 0;
			}
		}

		if (debug == 1) {
			System.out.println(message);
		}
	}
}
