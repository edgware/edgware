/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2012, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * Provides a command line interface to the Fabric Registry.
 */
public class RegistryTool {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012, 2014";

	/*
	 * Class constants
	 */

	private static final String OPT_LIST_NODES = "N";
	private static final String OPT_LIST_PLATFORMS = "P";
	private static final String OPT_LIST_SYSTEMS = "S";
	private static final String OPT_LIST_IP_MAPPINGS = "I";
	private static final String OPT_LIST_SERVICES = "SV";
	private static final String OPT_LIST_TASK_SYSTEMS = "T";
	private static final String OPT_LIST_NEIGHBOURS = "B";
	private static final String OPT_LIST_TABLES = "T";
	private static final String OPT_LIST_VIEWS = "V";

	private static final String OPT_LIST_DEFAULT_CONFIG = "d";
	private static final String OPT_SET_DEFAULT_CONFIG = "sd";
	private static final String OPT_UNSET_DEFAULT_CONFIG = "ud";

	private static final String OPT_LIST_NODE_CONFIG = "n";
	private static final String OPT_SET_NODE_CONFIG = "sn";
	private static final String OPT_UNSET_NODE_CONFIG = "un";

	private static final String OPT_RUN = "r";

	private static final String OPT_GAIAN = "g";

	/*
	 * Class fields
	 */

	private static Option optListNodes = null;
	private static Option optListPlatforms = null;
	private static Option optListSystems = null;
	private static Option optListServices = null;
	private static Option optListTaskSystems = null;
	private static Option optListIPMappings = null;
	private static Option optListNeighbours = null;
	private static Option optListTables = null;
	private static Option optListViews = null;
	private static Option optListDefaultConfig = null;
	private static Option optSetDefaultConfig = null;
	private static Option optUnsetDefaultConfig = null;
	private static Option optListNodeConfig = null;
	private static Option optSetNodeConfig = null;
	private static Option optUnsetNodeConfig = null;
	private static Option optRun = null;
	private static Option optGaian = null;

	private static Options options = new Options();

	/**
	 * Main entry point.
	 * 
	 * @param args
	 *            command line arguments.
	 */
	@SuppressWarnings("static-access")
	public static void main(String[] args) {

		String message = null;

		/* Configure supported options */

		optListNodes = OptionBuilder.withDescription("list all Fabric nodes").withLongOpt("list-nodes").create(
				OPT_LIST_NODES);

		optListPlatforms = OptionBuilder.withDescription("list all Fabric platforms").withLongOpt("list-platforms")
				.create(OPT_LIST_PLATFORMS);

		optListSystems = OptionBuilder.withDescription("list all Fabric systems").withLongOpt("list-systems").create(
				OPT_LIST_SYSTEMS);

		optListServices = OptionBuilder.withDescription("list all Fabric services").withLongOpt("list-services")
				.create(OPT_LIST_SERVICES);

		optListTaskSystems = OptionBuilder.withDescription("list all Fabric task systems").withLongOpt(
				"list-task-systems").create(OPT_LIST_TASK_SYSTEMS);

		optListIPMappings = OptionBuilder.withDescription("list all Fabric Node/IP mappings").withLongOpt("list-ip")
				.create(OPT_LIST_IP_MAPPINGS);

		optListNeighbours = OptionBuilder.withDescription("list all Fabric node neighbours").withLongOpt(
				"list-neighbours").create(OPT_LIST_NEIGHBOURS);

		optListTables = OptionBuilder.withDescription("list all tables in the database FABRIC").withLongOpt(
				"list-tables").create(OPT_LIST_TABLES);

		optListViews = OptionBuilder.withDescription("list all views in the database FABRIC").withLongOpt("list-views")
				.create(OPT_LIST_VIEWS);

		optListDefaultConfig = OptionBuilder.withDescription("list all default configuration settings").withLongOpt(
				"list-default-config").create(OPT_LIST_DEFAULT_CONFIG);

		optSetDefaultConfig = OptionBuilder.hasArgs(2).withArgName("name> <value").withDescription(
				"set a default configuration property").withLongOpt("set-default-config")
				.create(OPT_SET_DEFAULT_CONFIG);

		optUnsetDefaultConfig = OptionBuilder.hasArgs(1).withArgName("name").withDescription(
				"unset a default configuration property").withLongOpt("unset-default-config").create(
				OPT_UNSET_DEFAULT_CONFIG);

		optListNodeConfig = OptionBuilder.withDescription("list all node configuration settings").withLongOpt(
				"list-node-config").create(OPT_LIST_NODE_CONFIG);

		optSetNodeConfig = OptionBuilder.hasArgs(3).withArgName("node> <name> <value").withDescription(
				"set a node configuration property").withLongOpt("set-node-config").create(OPT_SET_NODE_CONFIG);

		optUnsetNodeConfig = OptionBuilder.hasArgs(2).withArgName("node> <name").withDescription(
				"unset a node configuration property").withLongOpt("unset-node-config").create(OPT_UNSET_NODE_CONFIG);

		optRun = OptionBuilder.withArgName("filename").hasArg().withDescription("run a file of SQL statements")
				.withLongOpt("run-sql").create(OPT_RUN);

		optGaian = OptionBuilder.withDescription("execute queries against the Gaian Database").withLongOpt("gaian")
				.create(OPT_GAIAN);

		OptionGroup optionGroup = new OptionGroup();

		optionGroup.addOption(optListNodes);
		optionGroup.addOption(optListPlatforms);
		optionGroup.addOption(optListSystems);
		optionGroup.addOption(optListServices);
		optionGroup.addOption(optListTaskSystems);
		optionGroup.addOption(optListIPMappings);
		optionGroup.addOption(optListNeighbours);
		optionGroup.addOption(optListTables);
		optionGroup.addOption(optListViews);

		optionGroup.addOption(optListDefaultConfig);
		optionGroup.addOption(optSetDefaultConfig);
		optionGroup.addOption(optUnsetDefaultConfig);

		optionGroup.addOption(optListNodeConfig);
		optionGroup.addOption(optSetNodeConfig);
		optionGroup.addOption(optUnsetNodeConfig);

		optionGroup.addOption(optRun);

		optionGroup.setRequired(true);

		options.addOptionGroup(optionGroup);
		options.addOption(optGaian);

		/* Parse the command line arguments */

		CommandLineParser parser = new PosixParser();
		CommandLine line = null;

		try {

			line = parser.parse(options, args);

		} catch (ParseException exp) {

			System.out.println(exp.getMessage() + '\n');
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("fabreg", options);
			System.exit(1);

		}

		/* If a SQL file is to be run... */
		if (line.hasOption(OPT_RUN)) {

			try {
				RegistryTool.run(new File(line.getOptionValue(OPT_RUN)), false);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {

			/* Determine if queries are to be made against the Gaian Database (where appropriate) */

			String localDBConnect = "CONNECT 'jdbc:derby://localhost:6414/FABRIC;create=false;user=fabric;password=fabric;';";
			String distributedDBConnect = null;
			boolean isGaian = false;
			String fromSQL = null;

			if (line.hasOption(OPT_GAIAN)) {
				distributedDBConnect = "CONNECT 'jdbc:derby://localhost:6414/gaiandb;create=false;user=gaiandb;password=passw0rd;';";
				isGaian = true;
			} else {
				distributedDBConnect = localDBConnect;
			}

			/* Initialise the SQL commands */
			String[] script = new String[] {localDBConnect, null, "DISCONNECT;"};
			String fallbackSQL = null;

			/* Decode primary option and generate the required SQL */

			if (line.hasOption(OPT_LIST_NODES)) {

				listNodesSQL(distributedDBConnect, isGaian, script);

			} else if (line.hasOption(OPT_LIST_PLATFORMS)) {

				listPlatformsSQL(distributedDBConnect, isGaian, script);

			} else if (line.hasOption(OPT_LIST_SYSTEMS)) {

				listSystemsSQL(distributedDBConnect, isGaian, script);

			} else if (line.hasOption(OPT_LIST_SERVICES)) {

				listServicesSQL(distributedDBConnect, isGaian, script);

			} else if (line.hasOption(OPT_LIST_IP_MAPPINGS)) {

				listIPMappingsSQL(distributedDBConnect, isGaian, script);

			} else if (line.hasOption(OPT_LIST_TASK_SYSTEMS)) {

				listTaskSystemsSQL(distributedDBConnect, isGaian, script);

			} else if (line.hasOption(OPT_LIST_NEIGHBOURS)) {

				listNeighboursSQL(distributedDBConnect, isGaian, script);

			} else if (line.hasOption(OPT_LIST_TABLES)) {

				script[1] = "show tables in FABRIC";

			} else if (line.hasOption(OPT_LIST_VIEWS)) {

				script[1] = "show views in FABRIC";

			} else if (line.hasOption(OPT_LIST_DEFAULT_CONFIG)) {

				script[1] = "select name, value from DEFAULT_CONFIG order by name";

			} else if (line.hasOption(OPT_SET_DEFAULT_CONFIG)) {

				fallbackSQL = setDefaultConfigSQL(line, script);

			} else if (line.hasOption(OPT_UNSET_DEFAULT_CONFIG)) {

				unsetDefaultConfigSQL(line, script);

			} else if (line.hasOption(OPT_LIST_NODE_CONFIG)) {

				script[1] = "select node_id, name, value from NODE_CONFIG order by node_id, name";

			} else if (line.hasOption(OPT_SET_NODE_CONFIG)) {

				fallbackSQL = setNodeConfigSQL(line, script);

			} else if (line.hasOption(OPT_UNSET_NODE_CONFIG)) {

				unsetNodeConfgSQL(line, script);

			}

			/* If we have some SQL... */
			if (script[1] != null) {

				try {

					/* Run it */
					run(script, true, false);

				} catch (Exception e) {

					/* If an alternative statement has been generated (e.g. update versus insert)... */
					if (fallbackSQL != null) {

						try {

							/* Run it */
							script[1] = fallbackSQL;
							run(script, true, false);

						} catch (Exception e1) {
							e1.printStackTrace();
						}

					} else {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private static void unsetNodeConfgSQL(CommandLine line, String[] script) {

		String[] args = line.getOptionValues(OPT_UNSET_NODE_CONFIG);

		if (args.length != 2) {
			errorExit(String.format("Wrong number of option arguments (should be 2): %s", OPT_UNSET_NODE_CONFIG));
		}

		String node = args[0];
		String name = args[1];

		script[1] = String.format("delete from NODE_CONFIG where node_id = '%s' and name = '%s'", node, name);
	}

	private static String setNodeConfigSQL(CommandLine line, String[] script) {

		String fallbackSQL;

		String[] args = line.getOptionValues(OPT_SET_NODE_CONFIG);

		if (args.length != 3) {
			errorExit(String.format("Wrong number of option arguments (should be 3): %s", OPT_SET_NODE_CONFIG));
		}

		String node = args[0];
		String name = args[1];
		String value = args[2];

		script[1] = String.format("insert into NODE_CONFIG (node_id, name, value) values ('%s', '%s', '%s')", node,
				name, value);
		fallbackSQL = String.format("update NODE_CONFIG set value='%s' where node_id='%s' and name='%s'", value, node,
				name);
		return fallbackSQL;
	}

	private static void unsetDefaultConfigSQL(CommandLine line, String[] script) {

		String[] args = line.getOptionValues(OPT_UNSET_DEFAULT_CONFIG);

		if (args.length != 1) {
			errorExit(String.format("Wrong number of option arguments (should be 1): %s", args.length,
					OPT_UNSET_DEFAULT_CONFIG));
		}

		String name = args[0];

		script[1] = String.format("delete from DEFAULT_CONFIG where name = '%s'", name);
	}

	private static String setDefaultConfigSQL(CommandLine line, String[] script) {

		String fallbackSQL;

		String[] args = line.getOptionValues(OPT_SET_DEFAULT_CONFIG);

		if (args.length != 2) {
			errorExit(String.format("Wrong number of option arguments (should be 2): %s", OPT_SET_DEFAULT_CONFIG));
		}

		String name = args[0];
		String value = args[1];

		script[1] = String.format("insert into DEFAULT_CONFIG (name, value) values ('%s', '%s')", name, value);
		fallbackSQL = String.format("update DEFAULT_CONFIG set value='%s' where name='%s'", value, name);

		return fallbackSQL;
	}

	private static void listNeighboursSQL(String distributedDBConnect, boolean isGaian, String[] script) {

		String fromSQL;
		script[0] = distributedDBConnect;

		fromSQL = isGaian ? ", gdb_node from NODE_NEIGHBOURS_P" : " from NODE_NEIGHBOURS";
		script[1] = String.format("select node_id, neighbour_id%s order by node_id, neighbour_id", fromSQL);
	}

	private static void listTaskSystemsSQL(String distributedDBConnect, boolean isGaian, String[] script) {

		String fromSQL;
		script[0] = distributedDBConnect;

		fromSQL = isGaian ? ", gdb_node from TASK_SERVICES_P" : " from  TASK_SERVICES";
		script[1] = String
				.format("select task_id, platform_id, service_id, data_feed_id%s order by task_id, platform_id, service_id, data_feed_id",
						fromSQL);
	}

	private static void listIPMappingsSQL(String distributedDBConnect, boolean isGaian, String[] script) {

		String fromSQL;
		script[0] = distributedDBConnect;

		fromSQL = isGaian ? ", gdb_node from NODE_IP_MAPPING_P" : " from NODE_IP_MAPPING";
		script[1] = String.format("select node_id, ip, port%s order by node_id", fromSQL);
	}

	private static void listServicesSQL(String distributedDBConnect, boolean isGaian, String[] script) {

		String fromSQL;
		script[0] = distributedDBConnect;

		fromSQL = isGaian ? ", gdb_node from DATA_FEEDS_P" : " from DATA_FEEDS";
		script[1] = String
				.format("select platform_id, service_id, id, type_id, direction, availability, description%s order by platform_id, service_id, id",
						fromSQL);
	}

	private static void listSystemsSQL(String distributedDBConnect, boolean isGaian, String[] script) {

		String fromSQL;
		script[0] = distributedDBConnect;

		fromSQL = isGaian ? ", gdb_node from SERVICES_P" : " from SERVICES";
		script[1] = String.format(
				"select platform_id, id, type_id, kind, availability, description%s order by platform_id, id", fromSQL);
	}

	private static void listPlatformsSQL(String distributedDBConnect, boolean isGaian, String[] script) {

		String fromSQL;
		script[0] = distributedDBConnect;

		fromSQL = isGaian ? ", gdb_node from PLATFORMS_P" : " from PLATFORMS";
		script[1] = String.format(
				"select platform_id, type_id, node_id, affiliation, availability, description%s order by platform_id",
				fromSQL);
	}

	private static void listNodesSQL(String distributedDBConnect, boolean isGaian, String[] script) {

		String fromSQL;
		script[0] = distributedDBConnect;

		fromSQL = isGaian ? ", gdb_node from NODES_P" : " from NODES";
		script[1] = String.format("select node_id, type_id, affiliation, availability, description%s order by node_id",
				fromSQL);
	}

	/**
	 * Runs a set of SQL statements from the specified file.
	 * 
	 * @param file
	 *            the file.
	 * 
	 * @param failOnError
	 * 
	 * @throws Exception
	 */
	public static void run(File file, boolean failOnError) throws Exception {

		BufferedReader in = new BufferedReader(new FileReader(file));
		ArrayList<String> list = new ArrayList<String>();
		String currentLine = "";
		String line;

		while (true) {

			line = in.readLine();

			if (line == null) {
				break;
			}

			line = line.trim();

			if (line.length() == 0) {
				continue;
			}

			if (line.startsWith("--")) {
				continue;
			}

			currentLine += " " + line;

			if (currentLine.endsWith(";")) {
				list.add(currentLine);
				currentLine = "";
			}
		}

		in.close();

		run(list.toArray(new String[] {}), failOnError, true);
	}

	/**
	 * Runs a set of SQL statements.
	 * 
	 * @param cmds
	 *            the SQL.
	 * 
	 * @param failOnError
	 * 
	 * @param verbose
	 * 
	 * @throws Exception
	 */
	public static void run(String[] cmds, boolean failOnError, boolean verbose) throws Exception {

		String currentLine = "";

		Connection conn = null;

		/* For each SQL statement... */
		for (int j = 0; j < cmds.length; j++) {

			currentLine = cmds[j].trim();

			if (currentLine.length() == 0 || currentLine.startsWith("-")) {
				continue;
			}

			if (currentLine.endsWith(";")) {
				currentLine = currentLine.substring(0, currentLine.length() - 1);
			}

			String action = currentLine.substring(0,
					(currentLine.indexOf(" ") >= 0) ? currentLine.indexOf(" ") : currentLine.length()).toLowerCase();

			if (action.equals("connect")) {

				int start = currentLine.indexOf("'");
				int end = currentLine.lastIndexOf("'");
				String connectURI = currentLine.substring(start + 1, end);

				if (verbose) {
					System.out.println("Connect [" + connectURI + "]");
				}

				conn = DriverManager.getConnection(connectURI);

			} else if (action.equals("disconnect")) {

				if (verbose) {
					System.out.println("Disconnect");
				}

				conn.close();

			} else if (action.equals("exit")) {

			} else if (action.equals("select")) {

				if (verbose) {
					System.out.println(" - " + currentLine);
				}

				PreparedStatement s = conn.prepareStatement(currentLine, ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				s.execute();

				ResultSet rs = s.getResultSet();
				printTable(rs);

			} else if (action.equals("show")) {

				String[] parts = currentLine.split(" ");
				String type = "TABLE";

				if (parts[1].equals("tables")) {
					type = "TABLE";
				} else if (parts[1].equals("views")) {
					type = "VIEW";
				} else {
					throw new Exception("Unsupported show type:" + type);
				}

				DatabaseMetaData dbmd = conn.getMetaData();
				ResultSet rs = dbmd.getTables(null, parts[3], null, new String[] {type});

				while (rs.next()) {
					System.out.println(rs.getString("TABLE_NAME"));
				}

			} else {

				if (verbose) {
					System.out.println(" - " + currentLine);
				}

				try {
					PreparedStatement s = conn.prepareStatement(currentLine, ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
					s.execute();
				} catch (SQLException sqle) {
					if (failOnError) {
						throw sqle;
					}
					System.out.println(sqle.getMessage());
				}
			}

			currentLine = "";
		}
	}

	private static void printTable(ResultSet rs) throws SQLException {

		ResultSetMetaData rsmd = rs.getMetaData();

		int cols = rsmd.getColumnCount();
		int[] maxLengths = new int[cols];

		/* Determine the maximum width for each column */

		for (int c = 0; c < rsmd.getColumnCount(); c++) {
			String nextHeading = rsmd.getColumnName(c + 1);
			maxLengths[c] = nextHeading.length();
		}

		while (rs.next()) {
			for (int i = 0; i < cols; i++) {
				String nextField = rs.getString(i + 1);
				maxLengths[i] = (nextField.length() > maxLengths[i]) ? nextField.length() : maxLengths[i];
			}
		}

		/* Reset the result set cursor */
		rs.absolute(0);

		/* Separator line */

		StringBuilder sb = new StringBuilder("+");

		for (int i = 0; i < cols; i++) {
			for (int k = 0; k < maxLengths[i]; k++) {
				sb.append('-');
			}
			sb.append('+');
		}

		String lineSep = sb.toString();

		System.out.println(lineSep);

		/* Rows */

		System.out.print("|");

		for (int i = 1; i <= cols; i++) {
			if (i > 1) {
				System.out.print("|");
			}
			System.out.print(String.format("%-" + maxLengths[i - 1] + "s", rsmd.getColumnLabel(i)));
		}

		System.out.println("|");
		System.out.println(lineSep);

		while (rs.next()) {

			System.out.print("|");

			for (int i = 1; i <= cols; i++) {

				if (i > 1) {
					System.out.print("|");
				}

				System.out.print(String.format("%-" + maxLengths[i - 1] + "s", rs.getString(i)));
			}

			System.out.println("|");
		}

		/* Final seperator */
		System.out.println(lineSep);
	}

	/**
	 * Displays an error message and exits.
	 * 
	 * @param message
	 */
	private static void errorExit(String message) {

		System.out.println(message);
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("fabreg", options, true);
		System.exit(1);
	}
}
