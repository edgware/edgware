/*
 * (C) Copyright IBM Corp. 2012, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.tools.launchers;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Properties;

import fabric.core.properties.ConfigProperties;
import fabric.tools.FabricConfig;

public class Broker {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012, 2014";

	public static void createBrokerConfig(String node) throws Exception {

		createBrokerConfig(node, FabricLauncher.BROKER_HOME, FabricLauncher.NODE_CONFIG_DIR);
	}

	public static void createBrokerConfig(String node, String brokerHome, String nodeConfigDir) throws Exception {

		File workingDir = new File(brokerHome + "/" + node + "/");
		workingDir.mkdir();
		File configFile = new File(workingDir, "broker_" + node + ".cfg");

		File nodeConfigFile = new File(System.getProperty("fabric.config", nodeConfigDir + "/fabricConfig_" + node
				+ ".properties"));

		final String brokerLocalPort = "1884";
		final String brokerLocalAddress = "127.0.0.1";
		String brokerExternalPort = null;
		String brokerExternalAddress = null;

		/* Lookup broker values in properties file */
		if (nodeConfigFile.exists()) {

			Properties props = new Properties();
			props.load(new FileInputStream(nodeConfigFile));

			String brokerExternalInterfaceName = props.getProperty(ConfigProperties.NODE_INTERFACES);
			/* If we need to, lookup values in the Registry */
			if (brokerExternalInterfaceName == null) {
				FabricConfig.getNodeProperty(node, ConfigProperties.NODE_INTERFACES);
			}
			/* Fall back on loopback interface */
			if (brokerExternalInterfaceName == null) {
				brokerExternalInterfaceName = "lo0";
			}
			NetworkInterface networkInterface = NetworkInterface.getByName(brokerExternalInterfaceName);
			if (networkInterface != null) {
				Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress address = addresses.nextElement();
					if (address instanceof Inet4Address) {
						brokerExternalAddress = address.getHostAddress();
						break;
					}
				}
			}
			brokerExternalPort = props.getProperty(ConfigProperties.BROKER_REMOTE_PORT);
		}

		if (brokerExternalPort == null) {
			brokerExternalPort = FabricConfig.getNodeProperty(node, ConfigProperties.BROKER_REMOTE_PORT);
		}

		/* Defaults, if required */

		if (brokerExternalAddress == null) {
			brokerExternalAddress = "127.0.0.1";
		}
		if (brokerExternalPort == null) {
			brokerExternalPort = "1883";
		}

		String maxConnections = FabricConfig.getNodeProperty(node, "mqtt.maxconnections");
		maxConnections = (maxConnections == null) ? "20" : maxConnections;

		PrintWriter configOut = new PrintWriter(configFile);
		configOut.println("port " + brokerExternalPort);
		configOut.println("#  max_connections " + maxConnections);
		configOut.println("max_inflight_messages 150");
		configOut.println("max_queued_messages 1000");
		configOut.println("retry_interval 60");
		configOut.println("persistence_location " + workingDir.getAbsolutePath() + File.separator);
		// Additional listeners must go at the end of the file
		configOut.println("#add local listener for in-node messaging");
		configOut.println("listener " + brokerLocalPort + " " + brokerLocalAddress);
		configOut.flush();
		configOut.close();

	}
}
