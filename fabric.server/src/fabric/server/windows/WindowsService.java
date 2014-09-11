/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.server.windows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fabric.tools.launchers.Broker;

/**
 * <p>
 * The WindowsService Class provides static methods to start and stop a specified command using the Apache Commons
 * Daemon framework and the Windows binaries provided by that project known as procrun and procmgr.
 * </p>
 * <p>
 * The start and stop methods are called statically by procrun when a Windows Service is started or stopped in the
 * normal way. The start method must be called with at least one argument to state which service is to be started and
 * optionally with a second argument to state which node the command applies to.
 * </p>
 */
public class WindowsService {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/** Whether the service is running */
	private static boolean run = true;

	/** FABRIC HOME set from the environment variable */
	private static String FABRIC_HOME;
	
	private static ProcessBuilder stopservice = null;


	/**
	 * May be used to start/stop the service for testing purposes outside of the Apache Commons Daemon framework
	 * 
	 * @param args
	 *            the first argument must be "start" or "stop" with all subsequent args being passed to
	 *            {@link #start(String[]) start}
	 */
	public static void main(String[] args) {

		if ((args.length == 0) || (!args[0].equals("start")) || (!args[0].equals("stop"))) {
			System.out
					.println("You must pass the string \"start\" or \"stop\" as the first argument if starting via the main method");
		}

		String[] newArgs = new String[args.length - 1];
		System.arraycopy(args, 1, newArgs, 0, newArgs.length);

		if (args[0].equals("stop")) {
			stop(newArgs);
		} else {
			start(newArgs);
		}
	}

	/**
	 * Called by Apache Commons Daemon framework to start the command specified in the Java properties file
	 * 
	 * @param args
	 *            The first argument (required) should be one of the strings "registry", "broker" or "node". The second
	 *            argument (optional) should be the node name.
	 * 
	 */
	public static void start(String[] args) {

		FABRIC_HOME = System.getenv("FABRIC_HOME");

		if (FABRIC_HOME == null || FABRIC_HOME.isEmpty()) {
			System.out.println("The FABRIC_HOME environment variable has not been set");
		}

		String cmd = "";
		String node = "";

		if (args.length == 0) {
			System.out.println("The first argument when starting a service should be one of registry, web or node");
			return;
		} else {
			
//			
//			if (args.length == 1) {
//		}
			cmd = args[0];
//			node = "default";
//		} else if (args.length > 1) {
//			cmd = args[0];
//			node = args[1];
		}

//		WindowsServiceExec service = null;
		ProcessBuilder service = null;

		// work out what to start based on the first argument
		if (cmd.equalsIgnoreCase("registry")) {
			service = getRegistryCmd(args);
			stopservice = getRegistryStop(args);
//		} else if (cmd.equalsIgnoreCase("broker")) {
//			service = getBrokerCmd("default");
//		} else if (cmd.equalsIgnoreCase("node")) {
//			service = getNodeCmd(node);
		}

//		String[] env = getEnv(service.getEnv());
//		Process p = null;
//		InputStream is = process.getInputStream();
//		InputStreamReader isr = new InputStreamReader(is);
//		BufferedReader br = new BufferedReader(isr);
//		StringBuffer buffer = new StringBuffer();
//		String line; 
//		while ((line = br.readLine()) != null) {
//			
//				System.out.println(line);
//				buffer.append(line);
//		}
		
//		InputStream iserror = process.getErrorStream();
//		InputStreamReader iserrorr = new InputStreamReader(iserror);
//		BufferedReader brerror = new BufferedReader(iserrorr);

//		String lineerror; 
//		while ((lineerror = brerror.readLine()) != null) {
//			
//				System.out.println("ERROR : " + lineerror);
//				buffer.append(lineerror);
//		}
//		return buffer.toString();
		
		//
//		System.out.println("Working directory: " + service.getWorkingDir().toString());
//		System.out.println("Executing command: " + service.getCommand());
        Process p = null;
        System.out.println(service.command().get(0));
        System.out.println(service.command().get(1));
        System.out.println(service.command().get(2));
        System.out.println(service.command().get(3));
		try {
//			p = Runtime.getRuntime().exec(service.getCommand(), env, service.getWorkingDir());
			p = service.start();
		} catch (IOException e) {
			System.out.println("Exception raised executing the command, see standard error output for stack trace.");
			e.printStackTrace();
			return;
		}

		System.out.println("Command executed, command output follows..." + System.getProperty("line.separator"));

		// set up something to read the output of the command
		String line;
		InputStreamReader is = new InputStreamReader(p.getInputStream());
		BufferedReader in = new BufferedReader(is);

//		InputStream iserror = p.getErrorStream();
//		InputStreamReader iserrorr = new InputStreamReader(iserror);
//		BufferedReader brerror = new BufferedReader(iserrorr);

		
		try {
			// provide a way to stop the command running via a static call to the stop() function
			while (run) {
				/**
				 * we can't just do in.readLine() here since the call from Apache Commons Daemon is static and hence
				 * would be blocked by the readline() call i.e. we wouldn't be able to terminate the process exec.
				 */
				while (in.ready()) {
					line = in.readLine();
					if (line != null) {
						System.out.println(line);
					} else {
						System.out.println("Finished reading output from command");
						break;
					}
				}
//				while (brerror.ready()) {
//					line = brerror.readLine();
//					if (line != null) {
//						System.out.println("Error : " + line);
//					} else {
//						System.out.println("Finished reading error from command");
//						break;
//					}
//				}
				try {
					// put something in here so it's not a completely tight loop
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// no need to handle, we'll exit if there's no more output
				}
			}
			

			
			
		} catch (IOException e) {
			System.out
					.println("Exception raised reading output from command, see standard error output for stack trace.");
			e.printStackTrace();
		}

		// kill the exec
		System.out.println(System.getProperty("line.separator") + "...end of command output, stopping command.");
		p.destroy();
		try {
			p.waitFor();
			System.out.println("Exit code: " + p.exitValue());
		} catch (InterruptedException e) {
			System.out.println("Interrupted during shutdown, see standard error output for stack trace.");
			e.printStackTrace();
		}

	}


	/**
	 * Called by Apache Commons Daemon framework to stop the command specified in the Java properties file
	 * 
	 * @param args
	 *            no arguments are required
	 */
	public static void stop(String[] args) {

		run = false;
        Process endp = null;
        System.out.println(stopservice.command().get(0));
        System.out.println(stopservice.command().get(1));
        System.out.println(stopservice.command().get(2));
        System.out.println(stopservice.command().get(3));
		try {
//			p = Runtime.getRuntime().exec(service.getCommand(), env, service.getWorkingDir());
			endp = stopservice.start();
			InputStream iserror = endp.getErrorStream();
			InputStreamReader iserrorr = new InputStreamReader(iserror);
			BufferedReader brerror = new BufferedReader(iserrorr);
			StringBuffer buffer = new StringBuffer();

			String lineerror; 
			while ((lineerror = brerror.readLine()) != null) {
				
					System.out.println("ERROR : " + lineerror);
					buffer.append(lineerror);
			}
			System.out.println(buffer.toString());
//			return buffer.toString();
		} catch (IOException e) {
			System.out.println("Exception raised executing the command, see standard error output for stack trace.");
			e.printStackTrace();
			return;
		}

	}

	/**
	 * Wraps the System.getenv() call to provide a list of environment variables as a String Array instead of a Map as
	 * required by Runtime.getRuntime().exec()
	 * 
	 * @param newEnvs
	 *            environment variables to override. If specified, these will take the place of any environment
	 *            variables of the same name or otherwise be added to the environment.
	 * 
	 * @return an array of Strings of the form key=value for the System environment variables
	 */
	private static String[] getEnv(HashMap<String, String> newEnvs) {

		Map<String, String> envMap = System.getenv();
		ArrayList<String> env = new ArrayList<String>();

		for (String envName : envMap.keySet()) {
			if (newEnvs.containsKey(envName)) {
				// overwrite existing value if one is specified
				env.add(envName + "=" + newEnvs.get(envName));
				newEnvs.remove(envName);
			} else {
				// add new env var
				env.add(envName + "=" + envMap.get(envName));
			}
		}

		// add any remaining new env vars
		for (Map.Entry<String, String> newEnv : newEnvs.entrySet()) {
			env.add(newEnv.getKey() + "=" + newEnv.getValue());
		}

		return env.toArray(new String[env.size()]);
	}

	/**
	 * Returns the command and environmental information required to start the Edgware Registry
	 * 
	 * @param registryType
	 *            the type of registry (gaian or distributed)

	 * @param registryNode
	 *            the node name for triggers to be sent too
	 * 
	 * @return a {@link WindowsServiceExec} object populated with the Java command to start the registry, the working
	 *         directory from which that command should be run and a CLASSPATH environment variable
	 */
	private static ProcessBuilder getRegistryCmd(String[] args) {
		String registryHome = FABRIC_HOME + "/db/REGISTRY";
		
		if (args.length != 3) {
			System.out.println("Insufficient parameters, need to specify registry type and node name for triggers to be sent to");
			System.exit(1);
		}
		
		String registryType = args[1];
		String registryNode = args[0];		

		File registryHomeFile = new File(registryHome);
		if (!registryHomeFile.exists()) {
			System.out.println("Registry does not exists at " + registryHome + ", make sure you have run fabinstall");
			System.exit(1);
		}
//
//		String classpath = "";
//
//		String[] libdirs = {"gaiandb/lib", "derby", "plugins", "fabric", "wmqtt"};
//
//		for (String libdir : libdirs) {
//
//			File libdirFile = new File(FABRIC_HOME + "/lib/" + libdir);
//
//			File[] jars = libdirFile.listFiles(new FilenameFilter() {
//
//				@Override
//				public boolean accept(File dir, String name) {
//
//					return name.endsWith(".jar");
//				}
//			});
//
//			if (jars != null) {
//				for (File jar : jars) {
//					classpath += ";" + libdirFile.toString() + "/" + jar.getName();
//				}
//			}
//		}
//
//		HashMap<String, String> envMap = new HashMap<String, String>();
////		if (!classpath.isEmpty()) {
////			// get rid of first semicolon
////			classpath = classpath.substring(1);
////
////			System.out.println("CLASSPATH: " + classpath);
////			envMap.put("CLASSPATH", classpath);
////		}
//
//		String command = "java -Djava.util.logging.config.file=" + registryHome + "/logging.properties -Dfabric.node="
//				+ registryNode + " -Xmx128m com.ibm.gaiandb.GaianNode -c ./gaiandb_config_fabric.properties";
//
//		return new WindowsServiceExec(command, registryHomeFile, envMap);
//		
//		
		ArrayList<String> command = new ArrayList<String>();
			command.add("fabadmin.bat");
//			command.add(Constants.FABRIC_HOME_DIR+ "/bin/win32/fabadmin.bat");
//		command.add("-d");
		command.add("-s");
		command.add("-r");
		command.add("-" + registryType);
		command.add(registryNode);

//		Properties testProperties = loadProperties(Constants.TEST_CONFIG_DIR + "/test.properties");
//		String javaHome = Utilities.isWindows() ? testProperties.getProperty("java.home.windows") : (Utilities.isMac() ? testProperties.getProperty("java.home.mac") : testProperties.getProperty("java.home.linux"));
//		String os = Utilities.isWindows() ? "win32" : "linux";
		ProcessBuilder pb = new ProcessBuilder(command);
//		File wDF = new File(Constants.FABRIC_HOME_DIR);
//		wDF.mkdir();
//		pb.directory(wDF);
		

		// set the paths needed to install and start the registry (GaianDB)
//		pb.environment().put("FABRIC_HOME", Constants.FABRIC_HOME_DIR);
//		pb.environment().put("JAVA_HOME", javaHome);		
//		pb.environment().put("PATH", System.getenv("PATH") + File.pathSeparator + pb.environment().get("JAVA_HOME") + File.pathSeparator + Constants.FABRIC_HOME_DIR + "/bin/" + os);
//		return pb;
//		ProcessBuilder pb = Utilities.issueNativeCommand(command);
		return pb;
	}

	private static ProcessBuilder getRegistryStop(String[] args) {
//		
		if (args.length != 3) {
			System.out.println("Insufficient parameters, need to specify registry type and node name for triggers to be sent to");
			System.exit(1);
		}
		
		String registryType = args[1];
		ArrayList<String> command = new ArrayList<String>();
			command.add("fabadmin.bat");
//			command.add(Constants.FABRIC_HOME_DIR+ "/bin/win32/fabadmin.bat");
//		command.add("-d");
		command.add("-st");
		command.add("-r");
		command.add("-" + registryType);
//		command.add(registryNode);

//		Properties testProperties = loadProperties(Constants.TEST_CONFIG_DIR + "/test.properties");
//		String javaHome = Utilities.isWindows() ? testProperties.getProperty("java.home.windows") : (Utilities.isMac() ? testProperties.getProperty("java.home.mac") : testProperties.getProperty("java.home.linux"));
//		String os = Utilities.isWindows() ? "win32" : "linux";
		ProcessBuilder pb = new ProcessBuilder(command);
//		File wDF = new File(Constants.FABRIC_HOME_DIR);
//		wDF.mkdir();
//		pb.directory(wDF);
		

		// set the paths needed to install and start the registry (GaianDB)
//		pb.environment().put("FABRIC_HOME", Constants.FABRIC_HOME_DIR);
//		pb.environment().put("JAVA_HOME", javaHome);		
//		pb.environment().put("PATH", System.getenv("PATH") + File.pathSeparator + pb.environment().get("JAVA_HOME") + File.pathSeparator + Constants.FABRIC_HOME_DIR + "/bin/" + os);
//		return pb;
//		ProcessBuilder pb = Utilities.issueNativeCommand(command);
		return pb;
	}

	
	/**
	 * Returns the command and environmental information required to start the Fabric Broker
	 * 
	 * @param brokerNode
	 *            the node name (currently always set to "default")
	 * @return a {@link WindowsServiceExec} object populated with the Java command to start the broker and the working
	 *         directory from which that command should be run
	 */
	private static WindowsServiceExec getBrokerCmd(String brokerNode) {

		String brokerHome = FABRIC_HOME + "/brokers/" + brokerNode;
		String nodeConfigDir = FABRIC_HOME + "/osgi/configuration";
		File brokerConfigFile = new File(brokerHome + "/broker_" + brokerNode + ".cfg");

		if (!brokerConfigFile.exists()) {
			try {
				Broker.createBrokerConfig(brokerNode, brokerHome, nodeConfigDir);
			} catch (Exception e) {
				System.out
						.println("Exception raised creating the broker config, see standard error output for stack trace.");
				e.printStackTrace();
				System.exit(1);
			}
		}

		File amqtdd_upd = new File(brokerHome + "/amqtdd.upd");
		if (amqtdd_upd.exists()) {
			amqtdd_upd.delete();
		}

		File workingDir = new File(FABRIC_HOME + "/bin/amqtdd");
		String command = workingDir.toString() + "/amqtdd " + brokerConfigFile.toString();

		return new WindowsServiceExec(command, workingDir);
	}

	/**
	 * Returns the command and environmental information required to start a Fabric Node
	 * 
	 * @param node
	 *            the node name
	 * @return a {@link WindowsServiceExec} object populated with the Java command to start the node and the working
	 *         directory from which that command should be run
	 */
	private static WindowsServiceExec getNodeCmd(String node) {

		String nodeHome = FABRIC_HOME + "/osgi";
		String nodeConfigDir = nodeHome + "/configuration";
		File nodeLoggingFile = new File(nodeConfigDir + "/logging_" + node + ".properties");
		File nodeConfigFile = new File(nodeConfigDir + "/fabricConfig_" + node + ".properties");

		if (!nodeLoggingFile.exists()) {
			nodeLoggingFile = new File(nodeConfigDir + "/logging.properties");
		}

		if (!nodeConfigFile.exists()) {
			System.out.println("Cannot find node configuration file: " + nodeConfigFile.toString());
		}

		File workingDir = new File(nodeHome);

		File[] jars = workingDir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {

				return ((name.startsWith("org.eclipse.osgi_") && name.endsWith(".jar")));
			}
		});

		File osgiJar = jars[jars.length - 1];

		String command = "java -Dfabric.config=" + nodeConfigFile.toString() + " -Djava.util.logging.config.file="
				+ nodeLoggingFile.toString() + " -jar " + osgiJar;

		return new WindowsServiceExec(command, workingDir);
	}
}
