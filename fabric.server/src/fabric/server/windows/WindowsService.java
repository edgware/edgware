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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * <p>
 * The WindowsService Class provides static methods to start and stop a specified command using the Apache Commons
 * Daemon framework and the Windows binaries provided by that project known as procrun and procmgr.
 * </p>
 * <p>
 * The start and stop methods are called statically by procrun when a Windows Service is started or stopped in the
 * normal way. The fabadmin command is run using the parameters passed in.
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

		if ((args.length == 0) || (!args[0].equals("start") && !args[0].equals("stop"))) {
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

		if (args.length == 0) {
			System.out.println("The first argument when starting a service should be one of registry, web or node");
			return;
		}
		String cmd = args[0];

		ProcessBuilder service = null;
		// work out what to start based on the first argument
		if (cmd.equalsIgnoreCase("registry")) {
			service = getRegistryCmd(args);
			stopservice = getRegistryStop(args);
		}
		if (cmd.equalsIgnoreCase("node")) {
			service = getNodeCmd(args);
			stopservice = getNodeStop(args);
		}
		if (cmd.equalsIgnoreCase("webserver")) {
			service = getWebServerCmd(args);
			stopservice = getWebServerStop(args);
		}

		Process p = null;
		String commandString = "";
		for (Iterator<String> iterator = service.command().iterator(); iterator.hasNext();) {
			commandString = commandString + iterator.next();
		}
		System.out.println(commandString);

		try {
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

			System.out.println("Run stop command now...");


			Process endp = null;
			try {
				//p = Runtime.getRuntime().exec(service.getCommand(), env, service.getWorkingDir());
				endp = stopservice.start();

				String result = getStreamsFromProcess(endp);




				//InputStream iserror = endp.getErrorStream();
				//InputStreamReader iserrorr = new InputStreamReader(iserror);
				//BufferedReader brerror = new BufferedReader(iserrorr);
				//StringBuffer buffer = new StringBuffer();
				//
				//String lineerror; 
				//while ((lineerror = brerror.readLine()) != null) {
				//
				System.out.println(result);
				//	buffer.append(lineerror);
				//}
				//System.out.println(buffer.toString());
				//return buffer.toString();
			} catch (IOException e) {
				System.out.println("Exception raised executing the command, see standard error output for stack trace.");
				e.printStackTrace();
				return;
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
			System.out.println("Invalid parameters, need to specify registry type and node name for triggers to be sent to");
			System.exit(1);
		}
		
		String registryType = args[1];
		String registryNode = args[2];		

		File registryHomeFile = new File(registryHome);
		if (!registryHomeFile.exists()) {
			System.out.println("Registry does not exists at " + registryHome + ", make sure you have run fabinstall");
			System.exit(1);
		}

		ArrayList<String> command = new ArrayList<String>();
			command.add("fabadmin.bat");
		command.add("-s");
		command.add("-" + registryType);
		command.add("-r");
		command.add(registryNode);

		ProcessBuilder pb = new ProcessBuilder(command);
		return pb;
	}

	private static ProcessBuilder getRegistryStop(String[] args) {
		if (args.length != 3) {
			System.out.println("Invalid parameters, need to specify registry type and node name for triggers to be sent to");
			System.exit(1);
		}
		
		String registryType = args[1];
		ArrayList<String> command = new ArrayList<String>();
			command.add("fabadmin.bat");
		command.add("-st");
		command.add("-" + registryType);
		command.add("-r");

		ProcessBuilder pb = new ProcessBuilder(command);
		return pb;
	}

	private static ProcessBuilder getNodeCmd(String[] args) {

		if (args.length != 2) {
			System.out.println("Insufficient parameters, need to specify registry type and node name for triggers to be sent to");
			System.exit(1);
		}

		String nodeName = args[1];		

		ArrayList<String> command = new ArrayList<String>();
		command.add("fabadmin.bat");
		command.add("-s");
		command.add("-n");
		command.add(nodeName);

		ProcessBuilder pb = new ProcessBuilder(command);
		return pb;
	}

	private static ProcessBuilder getNodeStop(String[] args) {
		if (args.length != 2) {
			System.out.println("Insufficient parameters, need to specify registry type and node name for triggers to be sent to");
			System.exit(1);
		}

		String nodeName = args[1];		

		ArrayList<String> command = new ArrayList<String>();
		command.add("fabadmin.bat");
		command.add("-st");
		command.add("-n");
		command.add(nodeName);

		ProcessBuilder pb = new ProcessBuilder(command);
		return pb;
	}
	
	private static ProcessBuilder getWebServerCmd(String[] args) {
		if (args.length != 2) {
			System.out.println("Insufficient parameters, need to specify registry type and node name for triggers to be sent to");
			System.exit(1);
		}

		String nodeName = args[1];		

		ArrayList<String> command = new ArrayList<String>();
		command.add("fabadmin.bat");
		command.add("-s");
		command.add("-w");
		command.add(nodeName);

		ProcessBuilder pb = new ProcessBuilder(command);
		return pb;
	}

	private static ProcessBuilder getWebServerStop(String[] args) {
		if (args.length != 2) {
			System.out.println("Insufficient parameters, need to specify registry type and node name for triggers to be sent to");
			System.exit(1);
		}

		String nodeName = args[1];		

		ArrayList<String> command = new ArrayList<String>();
		command.add("fabadmin.bat");
		command.add("-st");
		command.add("-w");

		ProcessBuilder pb = new ProcessBuilder(command);
		return pb;
	}

	public static String getStreamsFromProcess(Process process) throws IOException {
		InputStream is = process.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		StringBuffer buffer = new StringBuffer();
		String line; 
		while ((line = br.readLine()) != null) {
			
				System.out.println(line);
				buffer.append(line);
		}
		
		InputStream iserror = process.getErrorStream();
		InputStreamReader iserrorr = new InputStreamReader(iserror);
		BufferedReader brerror = new BufferedReader(iserrorr);

		String lineerror; 
		while ((lineerror = brerror.readLine()) != null) {
			
				System.out.println("ERROR : " + lineerror);
				buffer.append(lineerror);
		}
		return buffer.toString();
		
	}

}
