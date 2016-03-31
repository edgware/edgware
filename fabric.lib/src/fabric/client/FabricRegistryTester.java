/*
 * (C) Copyright IBM Corp. 2008, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.client;

import fabric.FabricBus;
import fabric.registry.FabricRegistry;
import fabric.registry.Task;

/**
 * Class used to test Fabric connectivity to the Registry.
 */
public class FabricRegistryTester extends FabricBus {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2008, 2012";

	/*
	 * Class fields
	 */

	/*
	 * Class methods
	 */

	/**
	 * Test harness entry point.
	 * 
	 * Any command line arguments are ignored since the configuration for this class is specified via system properties
	 * or a FabricConfiguration.properties file.
	 * 
	 * @param cla
	 *            the command line arguments.
	 */
	public static void main(String[] cla) {
		new FabricRegistryTester();

	}

	public FabricRegistryTester() {
		try {

			/* Get the identification information for the Fabric node */
			initFabricConfig();

			/* Try initialising the connection to the Registry */
			/* If auto reconnect is turned on, this call will block */
			/* until the connection is made. */
			initRegistry();

			/* Access the full Fabric configuration for this node */
			initNodeConfig();

			Task[] tasks = FabricRegistry.getTaskFactory().getAllTasks();
			if (tasks != null && tasks.length > 1) {
				System.out.println("FabricRegistryTester: Successfully connected to the Registry.");
			}
		} catch (Exception e) {

			e.printStackTrace();

		}

	}

}
