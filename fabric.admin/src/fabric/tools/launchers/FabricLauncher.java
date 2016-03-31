/*
 * (C) Copyright IBM Corp. 2012, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.tools.launchers;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import fabric.session.RegistryDescriptor;
import fabric.tools.NetworkInterfaces;
import fabric.tools.RegistryTool;

public class FabricLauncher {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012, 2014";

	/*
	 * Class constants
	 */

	protected static final String PID_DIR = System.getenv("FABRIC_HOME") + "/pid";
	protected static final String LOG_DIR = System.getenv("FABRIC_HOME") + "/log";

	protected static final String NODE_HOME = System.getenv("FABRIC_HOME") + "/osgi";
	protected static final String NODE_CONFIG_DIR = NODE_HOME + "/configuration";

	protected static final String BROKER_HOME = System.getenv("FABRIC_HOME") + "/brokers";

	protected static final String REGISTRY_HOME = System.getenv("FABRIC_HOME") + "/db";
	// Multiple registries per install not supported, use REGISTRY_SUB_DIR
	protected static final String REGISTRY_SUB_DIR = "REGISTRY";
	protected static final String REGISTRY_SQL_DIR = System.getenv("FABRIC_HOME") + "/lib/sql";

	/*
	 * Class methods
	 */

	public static void main(String[] args) {

		if (System.getenv("FABRIC_HOME") == null) {
			System.err.println("FABRIC_HOME not set");
			System.exit(1);
		}

		Options options = new Options();

		OptionGroup actionOptions = new OptionGroup();
		actionOptions.addOption(new Option("st", "stop", false, "stop a runtime component"));
		actionOptions.addOption(new Option("configure", "configure the Fabric broker"));
		actionOptions.addOption(new Option("create", "create the Fabric Registry database"));
		actionOptions.addOption(new Option("load", "load the Fabric Registry defaults into the database"));
		actionOptions.addOption(new Option("delete", "delete the Fabric Registry database"));
		actionOptions.addOption(new Option("testConnection",
				"test the connection to the Registry (to determine if the Regstry is running)"));
		actionOptions.addOption(new Option("status", false, "determine the status of Fabric runtime components"));
		actionOptions.addOption(new Option("run", true, "run sql"));
		actionOptions.addOption(new Option("i", "interfaces", false, "list network interfaces"));

		actionOptions.setRequired(true);

		OptionGroup entityGroup = new OptionGroup();
		entityGroup.addOption(new Option("n", "node", false, "node name"));
		entityGroup.addOption(new Option("b", "broker", false, "broker name"));
		entityGroup.addOption(new Option("r", "registry", false, "Registry"));
		entityGroup.addOption(new Option("a", "all", false, "all runtime components"));

		Option passthrough = new Option("p", "passthrough", false, "passthrough (for script use only)");

		OptionGroup registryTypeGroup = new OptionGroup();
		registryTypeGroup.addOption(new Option(RegistryDescriptor.TYPE_DISTRIBUTED, false, "Distributed Registry"));
		registryTypeGroup.addOption(new Option(RegistryDescriptor.TYPE_GAIAN, false, "Gaian Registry"));
		registryTypeGroup.addOption(new Option(RegistryDescriptor.TYPE_SINGLETON, false, "Singleton Registry"));

		options.addOptionGroup(entityGroup);
		options.addOptionGroup(actionOptions);
		options.addOption(passthrough);
		options.addOptionGroup(registryTypeGroup);

		options.addOption(new Option("failOnError", "fail on error"));
		CommandLineParser parser = new PosixParser();
		CommandLine line = null;
		try {
			
			line = parser.parse(options, args);

			if ((options.hasOption("n") || options.hasOption("b")) && line.getArgs().length > 1) {
				throw new ParseException("Unexpected arguments:" + line.getArgList());
			}
			
			if (options.hasOption("configure") && !options.hasOption("b")) {
				throw new ParseException("configure only valid for brokers");
			}
			
			if (options.hasOption("b") && !options.hasOption("configure")) {
				throw new ParseException(
						"Can only generate a broker configuration, use broker commands directly to start and stop");
			}

		} catch (ParseException exp) {
			System.out.println("Unexpected exception:" + exp.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("fabadmin", options);
			System.exit(1);
		}

		try {
			
			String node = "default";
			String registryType = RegistryDescriptor.TYPE_DISTRIBUTED;
			
			// Check for left over Arguments
			if (line.getArgs().length == 1) {
				node = line.getArgs()[0];
			}
			
			if (line.hasOption("i") || line.hasOption("interfaces")) {
				NetworkInterfaces.printInterfaces();
			}
			else if (line.hasOption("n")) {
				if (line.hasOption("st") || line.hasOption("stop")) {
					FabricManager.stopNode(node);
				}
			} else if (line.hasOption("b")) {
				if (line.hasOption("configure")) {
					Broker.createBrokerConfig(node);
				}
			} else if (line.hasOption("r")) {
				// Type defaults to Distributed, check for other types
				if (line.hasOption(RegistryDescriptor.TYPE_GAIAN)) {
					registryType = RegistryDescriptor.TYPE_GAIAN;
				} else if (line.hasOption(RegistryDescriptor.TYPE_SINGLETON)) {
					registryType = RegistryDescriptor.TYPE_SINGLETON;
				}
				if (line.hasOption("st") || line.hasOption("stop")) {
					if (Registry.stopRegistry(registryType)) {
						System.exit(0);
					} else {
						System.exit(1);
					}
				} else if (line.hasOption("delete")) {
					if (Registry.deleteRegistry(registryType)) {
						System.exit(0);
					} else {
						System.exit(1);
					}
				} else if (line.hasOption("create")) {
					Registry.createRegistry(registryType);
				} else if (line.hasOption("load")) {
					Registry.loadInitialRegistryContent(registryType);
				} else if (line.hasOption("testConnection")) {
					if (Registry.isRunning(registryType)) {
						System.exit(0);
					} else {
						System.exit(1);
					}
				} else if (line.hasOption("run")) {
					RegistryTool.run(new File(line.getOptionValue("run")), line.hasOption("failOnError"));
				}
			} else if (line.hasOption("status")) {
				status();
			} else if (line.hasOption("a") && (line.hasOption("st") || line.hasOption("stop"))) {
				stopFabric();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	private static void status() throws Exception {

		File pidDir = new File(PID_DIR);

		FileFilter pidFilter = new FileFilter() {

			@Override
			public boolean accept(File pathname) {

				return pathname.isFile() && pathname.getName().toLowerCase().startsWith(".");
			}
		};
		File[] files = pidDir.listFiles(pidFilter);
		for (int i = 0; i < files.length; i++) {
			String fn = files[i].getName();
			if (fn.startsWith(".registry")) {
				System.out.println("Registry        : running");
			} else if (fn.startsWith(".fm")) {
				String name = fn.substring(4);
				System.out.println("Node:\n " + String.format("%1$-15s", name) + ": running");
			}
		}
	}

	private static void stopFabric() throws Exception {

		File pidDir = new File(PID_DIR);

		FileFilter pidFilter = new FileFilter() {

			@Override
			public boolean accept(File pathname) {

				return pathname.isFile() && pathname.getName().toLowerCase().startsWith(".");
			}
		};
		File[] files = pidDir.listFiles(pidFilter);
		// Stop nodes, then the Registry
		for (int i = 0; i < files.length; i++) {
			String fn = files[i].getName();
			if (fn.startsWith(".fm")) {
				FabricManager.stopNode(fn.substring(4));
			}
		}
		for (int i = 0; i < files.length; i++) {
			String fn = files[i].getName();
			if (fn.startsWith(".registry")) {
				Registry.stopRegistry(fn.substring(10));
			}
		}
	}
}
