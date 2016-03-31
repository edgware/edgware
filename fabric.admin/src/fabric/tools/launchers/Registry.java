/*
 * (C) Copyright IBM Corp. 2012, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.tools.launchers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;

import fabric.tools.FileUtilities;
import fabric.tools.RegistryTool;

public class Registry {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012, 2014";

	protected static boolean stopRegistry(String registryType) {

		/*
		 * State machine to start/stop/delete/create the Registry (currently Gaian-only -0 more work required).
		 */
		int count = 0;
		boolean result = false;
		String connectionURL = gaianConnectionURL();
		Connection conn = null;
		Statement s = null;

		while (count < 10) {

			try {
				conn = DriverManager.getConnection(connectionURL);
				s = conn.createStatement();
				s.execute("values gkill(0)");
				s.close();
				conn.close();
			} catch (Exception e) {
				// System.out.println("Registry stop failed, may already be stopped (" + e.getMessage() + ")");
				result = true;
				break;
			} finally {
				conn = null;
				s = null;
				count++;
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// System.out.println("stopRegistry Counter : " + count + " : " + result);
		}

		// Force termination of the Registry (signal 9); Linux only for now
		File fileGaianPid = new File(FabricLauncher.PID_DIR, ".registry");
		String pid = null;

		if (fileGaianPid.exists()) {
			if (!result) {
				try {
					BufferedReader brGaianPid = new BufferedReader(
							new FileReader(FabricLauncher.PID_DIR + "/.registry"));
					while ((pid = brGaianPid.readLine()) != null) {
						Runtime.getRuntime().exec("/bin/kill -9 " + pid);
					}
					result = true;
				} catch (IOException e) {
					System.out.println("Gaian is probably not running:" + e.getMessage());
					e.printStackTrace();
				}
			}
			fileGaianPid.delete();
		}

		return result;
	}

	protected static void createRegistry(String registryType) throws Exception {

		runRegistryFiles("CREATE", registryType);
	}

	protected static void loadInitialRegistryContent(String registryType) throws Exception {

		runRegistryFiles("LOAD", registryType);
	}

	private static void runRegistryFiles(final String sqlType, final String registryType) throws Exception {

		if (!Registry.isRunning(registryType)) {
			throw new Exception("Registry not running");
		}

		File[] files = new File(FabricLauncher.REGISTRY_SQL_DIR).listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				boolean valid = false;
				String partSeparator = "_";
				// FileNames of sql files must conform to
				// xx_desc_registryType_sqlType.sql
				// where xx is sequence number
				// desc is optional description of contained sql statements e.g. default
				// registrytype to run on is all distributed singleton or gaian
				// sqlType is load or create
				String testName = name.toUpperCase();
				// Must end with .sql
				if (testName.endsWith(".SQL")) {
					testName = testName.substring(0, testName.length() - 4);
					// Must now end with the sqltype (CREATE OR LOAD)
					if (testName.endsWith(partSeparator + sqlType.toUpperCase())) {
						testName = testName.substring(0, testName.length() - sqlType.length() - partSeparator.length());
						// Does this sql run on all types of registry?
						if (testName.endsWith(partSeparator + "ALL")) {
							valid = true;
						}
						// Or does it run on this type of registry
						else if (testName.endsWith(partSeparator + registryType.toUpperCase())) {
							valid = true;
						}

					}
				}
				return valid;
			}
		});
		Arrays.sort(files);
		for (int i = 0; i < files.length; i++) {
			RegistryTool.run(files[i], false);
		}
	}

	protected static boolean deleteRegistry(String registryType) {

		boolean result = true;

		File workingDir = new File(FabricLauncher.REGISTRY_HOME, FabricLauncher.REGISTRY_SUB_DIR);

		if (workingDir.exists()) {

			if (isRunning(registryType)) {
				// Force registry to stop
				Registry.stopRegistry(registryType);
			}

			result = FileUtilities.deleteDirectoryContents(workingDir);
		}

		return result;
	}

	protected static boolean isRunning(String registryType) {

		boolean result = false;
		String connectionURL = connectionURL();
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(connectionURL);
			result = true;
			conn.close();
		} catch (SQLException e) {
			result = false;
		}
		return result;
	}

	/**
	 * Answers the connection URL for the Fabric Registry.
	 * 
	 * @return the URL
	 */
	public static String connectionURL() {

		String connectionURL = lookupProperty("registry.address");

		if (connectionURL == null) {
			connectionURL = "jdbc:derby://localhost:6414/FABRIC;create=true;user=fabric;password=fabric";
		}

		return connectionURL;
	}

	/**
	 * Answers the connection URL for the Fabric Registry.
	 * 
	 * @return the URL
	 */
	public static String gaianConnectionURL() {

		String connectionURL = lookupProperty("registry.address.gaian");

		if (connectionURL == null) {
			connectionURL = "jdbc:derby://localhost:6414/GAIANDB;create=true;user=gaiandb;password=passw0rd";
		}

		return connectionURL;
	}

	private static String lookupProperty(String name) {

		String value = null;

		/* Generate the name of the configuration settings file */
		String fabricHome = System.getenv("FABRIC_HOME");
		char sep = File.separatorChar;
		String configFile = fabricHome + sep + "osgi" + sep + "configuration" + sep + "fabricConfig_default.properties";

		/* Load the properties */
		Properties config;
		try {

			config = new Properties();
			InputStream is = new FileInputStream(configFile);
			config.load(is);
			is.close();

			/* Get the connection URL */
			value = config.getProperty(name);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return value;
	}
}
