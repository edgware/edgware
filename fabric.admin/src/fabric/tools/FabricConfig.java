/*
 * (C) Copyright IBM Corp. 2012, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * Command line interface to configure the Fabric.
 */
public class FabricConfig {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012, 2014";

	private static String connectionURL = null;

	public static void main(String[] args) {

		OptionGroup optionGroup = new OptionGroup();
		optionGroup.addOption(new Option("s", "set", true, "set a property (use with -d, -n, -p or -a)"));
		optionGroup.addOption(new Option("g", "get", true, "get a property (use with -d, -n, -p or -a)"));
		optionGroup.addOption(new Option("u", "unset", true, "unset a property (use with -d, -n, -p or -a)"));
		optionGroup.addOption(new Option("i", "import", true, "import properties (use with -d, -n, -p or -a)"));
		optionGroup.addOption(new Option("e", "export", true, "export properties (use with -d, -n, -p or -a)"));
		optionGroup.addOption(new Option("D", "delete", false, "delete all properties (use with -d, -n, -p or -a)"));
		optionGroup.setRequired(true);

		Options options = new Options();
		options.addOptionGroup(optionGroup);

		options.addOption("n", true, "Node name");
		options.addOption("p", true, "Platform name");
		options.addOption("a", true, "Actor name");
		options.addOption("d", false, "Default properties");

		CommandLineParser parser = new PosixParser();
		CommandLine line = null;

		try {

			/* Parse the command line arguments */
			line = parser.parse(options, args);

			int otherArgs = line.getArgList().size();

			if (line.hasOption("s")) {

				/* Rule: -s must have exactly one unparsed argument (the value) */
				if (otherArgs == 0) {
					throw new ParseException("Missing set property value");
				}
				if (otherArgs > 1) {
					throw new ParseException("Unexpected arguments: " + line.getArgList());
				}

				// if (!(line.hasOption("n") || line.hasOption("a") || line.hasOption("p"))) {
				// /* Rule: -s cannot be applied to default config -d */
				// throw new ParseException("Cannot set default property");
				// }

			} else if (otherArgs != 0) {

				/* Rule: there must be no unparsed arguments (other than -s) */
				throw new ParseException("Unexpected arguments: " + line.getArgList());

			}

			// if (line.hasOption("u") && !(line.hasOption("n") || line.hasOption("a") || line.hasOption("p"))) {
			// /* Rule: -u cannot be applied to default config -d */
			// throw new ParseException("Cannot unset default property");
			// }

			if (line.hasOption("D") && !(line.hasOption("n") || line.hasOption("a") || line.hasOption("p"))) {
				/* Rule: -D cannot be applied to default config -d */
				throw new ParseException("Cannot delete all default properties");
			}

		} catch (ParseException exp) {

			System.out.println(exp.getMessage() + '\n');
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("fabconf", options);
			System.exit(1);

		}

		String node = line.getOptionValue("n");
		String platform = line.getOptionValue("p");
		String actor = line.getOptionValue("a");

		boolean isNode = (platform == null && actor == null);
		boolean isPlatform = (platform != null && actor == null);
		boolean isActor = (platform != null && actor != null);

		boolean isDefault = line.hasOption("d") || !(isNode || isPlatform || isActor);

		try {

			if (line.hasOption("g")) {

				String property = line.getOptionValue("g");
				String result = null;

				if (isDefault) {

					result = getDefaultProperty(property);

				} else {

					if (isNode) {
						result = getNodeProperty(node, property);
					} else if (isPlatform) {
						result = getPlatformProperty(platform, property);
					} else if (isActor) {
						result = getActorProperty(platform, actor, property);
					}

					if (result == null) {
						result = getDefaultProperty(property);
					}
				}

				if (result != null) {
					System.out.println(result);
				}

			} else if (line.hasOption("s")) {

				String property = line.getOptionValue("s");
				String value = line.getArgs()[0];

				if (isDefault) {
					setDefaultProperty(property, value);
				} else if (isNode) {
					setNodeProperty(node, property, value);
				} else if (isPlatform) {
					setPlatformProperty(platform, property, value);
				} else if (isActor) {
					setActorProperty(platform, actor, property, value);
				}

			} else if (line.hasOption("u")) {

				String property = line.getOptionValue("u");

				if (isDefault) {
					unsetDefaultProperty(property);
				} else if (isNode) {
					unsetNodeProperty(node, property);
				} else if (isPlatform) {
					unsetPlatformProperty(platform, property);
				} else if (isActor) {
					unsetActorProperty(platform, actor, property);
				}

			} else if (line.hasOption("e")) {

				HashMap<String, String> result = null;
				String outputDestination = line.getOptionValue("e");

				if (isDefault) {
					result = getDefaultProperties();
				} else if (isNode) {
					result = getNodeProperties(node);
				} else if (isPlatform) {
					result = getPlatformProperties(platform);
				} else if (isActor) {
					result = getActorProperties(platform, actor);
				}

				if (result != null) {

					PrintStream out = null;
					boolean closeAtEnd = true;

					if (outputDestination.equals("-")) {
						out = System.out;
						closeAtEnd = false;
					} else {
						out = new PrintStream(new File(outputDestination));
					}

					List<String> keys = new ArrayList<String>(result.keySet());
					Collections.sort(keys);
					Iterator<String> i = keys.iterator();

					while (i.hasNext()) {

						String key = i.next();
						out.println(key + "=" + result.get(key));

					}

					if (closeAtEnd) {
						out.flush();
						out.close();
					}
				}

			} else if (line.hasOption("D")) {

				if (isNode) {
					deleteNodeProperties(node);
				} else if (isPlatform) {
					deletePlatformProperties(platform);
				} else if (isActor) {
					deleteActorProperties(platform, actor);
				}

			} else if (line.hasOption("i")) {

				if (isDefault) {
					// TODO: not currently supported.
				} else {

					String inputDestination = line.getOptionValue("i");

					InputStream is;

					if (inputDestination.equals("-")) {
						is = System.in;
					} else {
						is = new FileInputStream(inputDestination);
					}

					Properties newProps = new Properties();
					newProps.load(is);

					is.close();
					Enumeration<Object> en = newProps.keys();

					while (en.hasMoreElements()) {

						String key = (String) en.nextElement();
						String value = newProps.getProperty(key);

						if (isNode) {
							setNodeProperty(node, key, value);
						} else if (isPlatform) {
							setPlatformProperty(platform, key, value);
						} else if (isActor) {
							setActorProperty(platform, actor, key, value);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getDefaultProperty(String property) throws Exception {

		return runSQL("select VALUE from DEFAULT_CONFIG where NAME=?", new String[] {property});
	}

	public static String getNodeProperty(String node, String property) throws Exception {

		return runSQL("select VALUE from NODE_CONFIG where NODE_ID=? and NAME=?", new String[] {node, property});
	}

	public static String getPlatformProperty(String platform, String property) throws Exception {

		return runSQL("select VALUE from PLATFORM_CONFIG where PLATFORM_ID=? and NAME=?", new String[] {platform,
				property});
	}

	public static String getActorProperty(String platform, String actor, String property) throws Exception {

		return runSQL("select VALUE from ACTOR_CONFIG where PLATFORM_ID=? and ACTOR_ID=? and NAME=?", new String[] {
				platform, actor, property});
	}

	public static boolean setDefaultProperty(String property, String value) throws Exception {

		boolean isNew = true;
		String oldValue = getDefaultProperty(property);

		if (oldValue == null) {
			runSQL("insert into DEFAULT_CONFIG(NAME,VALUE) values(?,?)", new String[] {property, value});
		} else {
			isNew = false;
			runSQL("update DEFAULT_CONFIG set VALUE=? where NAME=?", new String[] {value, property});
		}

		return isNew;
	}

	public static boolean setNodeProperty(String node, String property, String value) throws Exception {

		boolean isNew = true;
		String oldValue = getNodeProperty(node, property);

		if (oldValue == null) {
			runSQL("insert into NODE_CONFIG(NODE_ID,NAME,VALUE) values(?,?,?)", new String[] {node, property, value});
		} else {
			isNew = false;
			runSQL("update NODE_CONFIG set VALUE=? where NODE_ID=? and NAME=?", new String[] {value, node, property});
		}

		return isNew;
	}

	public static boolean setPlatformProperty(String platform, String property, String value) throws Exception {

		boolean isNew = true;
		String oldValue = getPlatformProperty(platform, property);

		if (oldValue == null) {
			runSQL("insert into PLATFORM_CONFIG(PLATFORM_ID,NAME,VALUE) values(?,?,?)", new String[] {platform,
					property, value});
		} else {
			isNew = false;
			runSQL("update PLATFORM_CONFIG set VALUE=? where PLATFORM_ID=? and NAME=?", new String[] {value, platform,
					property});
		}
		return isNew;
	}

	public static boolean setActorProperty(String platform, String actor, String property, String value)
			throws Exception {

		boolean isNew = true;
		String oldValue = getActorProperty(platform, actor, property);

		if (oldValue == null) {
			runSQL("insert into ACTOR_CONFIG(PLATFORM_ID,ACTOR_ID,NAME,VALUE) values(?,?,?,?)", new String[] {platform,
					actor, property, value});
		} else {
			isNew = false;
			runSQL("update ACTOR_CONFIG set VALUE=? where PLATFORM_ID=? and ACTOR_ID=? and NAME=?", new String[] {
					value, platform, actor, property});
		}

		return isNew;
	}

	public static void unsetDefaultProperty(String property) throws Exception {

		runSQL("delete from DEFAULT_CONFIG where NAME=?", new String[] {property});
	}

	public static void unsetNodeProperty(String node, String property) throws Exception {

		runSQL("delete from NODE_CONFIG where NODE_ID=? and NAME=?", new String[] {node, property});
	}

	public static void unsetPlatformProperty(String platform, String property) throws Exception {

		runSQL("delete from PLATFORM_CONFIG where PLATFORM_ID=? and NAME=?", new String[] {platform, property});
	}

	public static void unsetActorProperty(String platform, String actor, String property) throws Exception {

		runSQL("delete from ACTOR_CONFIG where PLATFORM_ID=? and ACTOR_ID=? and NAME=?", new String[] {platform, actor,
				property});
	}

	public static void deleteNodeProperties(String node) throws Exception {

		runSQL("delete from NODE_CONFIG where NODE_ID=?", new String[] {node});
	}

	public static void deletePlatformProperties(String platform) throws Exception {

		runSQL("delete from PLATFORM_CONFIG where PLATFORM_ID=?", new String[] {platform});
	}

	public static void deleteActorProperties(String platform, String actor) throws Exception {

		runSQL("delete from ACTOR_CONFIG where PLATFORM_ID=? and ACTOR_ID=?", new String[] {platform, actor});
	}

	public static HashMap<String, String> getDefaultProperties() throws Exception {

		return runPropertiesQuery(null, null);
	}

	public static HashMap<String, String> getNodeProperties(String node) throws Exception {

		return runPropertiesQuery("select NAME,VALUE from NODE_CONFIG where NODE_ID=?", new String[] {node});
	}

	public static HashMap<String, String> getPlatformProperties(String platform) throws Exception {

		return runPropertiesQuery("select NAME,VALUE from PLATFORM_CONFIG where PLATFORM_ID=?", new String[] {platform});
	}

	public static HashMap<String, String> getActorProperties(String platform, String actor) throws Exception {

		return runPropertiesQuery("select NAME,VALUE from ACTOR_CONFIG where PLATFORM_ID=? and ACTOR_ID=?",
				new String[] {platform, actor});
	}

	private static String getConnectionURL() {

		if (connectionURL == null) {

			Properties props = new Properties();
			String configBundle = System.getProperty("fabric.config");

			if (configBundle == null) {
				configBundle = System.getenv("FABRIC_HOME") + "/osgi/configuration/fabricConfig_default.properties";
			}

			if (configBundle != null) {

				File configFile = new File(configBundle);

				if (configFile.exists()) {

					try {
						InputStream is = new FileInputStream(configFile);
						props.load(is);
						is.close();
					} catch (Exception e) {
					}
				}
			}

			connectionURL = props.getProperty("registry.address",
					"jdbc:derby://localhost:6414/FABRIC;user=fabric;password=fabric");
		}
		return connectionURL;
	}

	private static String runSQL(String sql, String[] args) throws Exception {

		String result = null;
		Connection conn = null;
		PreparedStatement s = null;

		getConnectionURL();

		try {

			conn = DriverManager.getConnection(connectionURL);
			s = conn.prepareStatement(sql);

			if (args != null) {

				for (int i = 0; i < args.length; i++) {
					s.setString(i + 1, args[i]);
				}
			}

			if (sql.toUpperCase().startsWith("SELECT ")) {

				ResultSet rs = s.executeQuery();
				rs.getMetaData();

				if (rs.next()) {
					result = rs.getString(1);
				}

			} else {

				s.executeUpdate();

			}

		} finally {

			if (s != null) {
				try {
					s.close();
				} catch (Exception e) {
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
				}
			}
		}

		return result;
	}

	private static HashMap<String, String> runPropertiesQuery(String sql, String[] args) throws Exception {

		HashMap<String, String> result = new HashMap<String, String>();
		Connection conn = null;
		PreparedStatement s = null;

		getConnectionURL();

		try {

			conn = DriverManager.getConnection(connectionURL);
			s = conn.prepareStatement("select NAME,VALUE from DEFAULT_CONFIG");
			ResultSet rs = s.executeQuery();
			rs.getMetaData();

			while (rs.next()) {
				result.put(rs.getString(1), rs.getString(2));
			}

			s.close();
			s = null;

			if (sql != null) {

				s = conn.prepareStatement(sql);

				if (args != null) {
					for (int i = 0; i < args.length; i++) {
						s.setString(i + 1, args[i]);
					}
				}

				rs = s.executeQuery();
				rs.getMetaData();

				while (rs.next()) {
					result.put(rs.getString(1), rs.getString(2));
				}
			}

		} finally {

			if (s != null) {
				try {
					s.close();
				} catch (Exception e) {
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
				}
			}
		}

		return result;
	}
}