/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.trigger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import fabric.core.logging.LogUtil;

/**
 * Simple Fabric Service bound as a JavaUDF in the Registry. Used to publish Registry update events (INSERT, DELETE,
 * UPDATE) to the Fabric.
 * 
 */
public class TableUpdate {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

	/*
	 * Class static fields
	 */

	private static final Logger logger = Logger.getLogger("fabric.registry.trigger");

	private static ResourceManager resourceManager = null;

	/*
	 * Class methods
	 */

	/**
	 * Publish event data to the Fabric.
	 * 
	 * @param tableName
	 *            the name of the table that was updated.
	 * 
	 * @param key
	 *            the name of the primary key column.
	 * 
	 * @param id
	 *            primary key of the row that was modified.
	 * 
	 * @param action
	 *            the type of modification.
	 * 
	 * @return code always 0.
	 */
	public synchronized static int entryModified(String tableName, String key, String id, String action)
			throws Exception {

		try {

			if (resourceManager == null) {
				resourceManager = ResourceManager.getInstance();
			}

			StringBuilder notification = new StringBuilder("{");
			StringBuilder notificationDetail = new StringBuilder();
			tableName = (tableName != null) ? tableName.toUpperCase() : null;
			action = (action != null) ? action.toUpperCase() : null;

			if (resourceManager.isReportable(tableName)) {

				String[] idParts = null;

				switch (tableName.toUpperCase()) {

				case "SYSTEMS":

					/*
					 * Determine the type of the system, and its name; the string is encoded as "id:type", where id is a
					 * "/" separated string
					 */
					idParts = id.split(":");
					String type = (idParts.length > 1) ? idParts[1] : "";
					String[] keyParts = idParts[0].split("/");

					if (action != null && (action.equals("UPDATE") || action.equals("INSERT"))) {
						/* Lookup and record the services associated with this system */
						notificationDetail = servicesForSystem(keyParts[0], keyParts[1], type);
					}

					break;

				case "NODE_NEIGHBOURS":

					/* Determine the ID of the neighbour; the string is encoded as "node_id/neighbour_id" */
					idParts = id.split("/");
					String nodeID = (idParts.length > 0) ? idParts[0] : "";
					String neighbourID = (idParts.length > 0) ? idParts[1] : "";

					if (action != null && (action.equals("UPDATE") || action.equals("INSERT"))) {
						notificationDetail = neighbourAvailability(nodeID, neighbourID);
					}

				}

				/* Build the notification message (a JSON object) */
				notification.append(String.format(
						"\"table\":\"%s\",\"key\":\"%s\",\"id\":\"%s\",\"action\":\"%s\",\"timestamp\":%d", tableName,
						key, id, action, System.currentTimeMillis()));

				notification.append(notificationDetail);
				notification.append('}');

				/* Publish to the Fabric */
				resourceManager.sendRegistryUpdate(notification.toString());
			}

		} catch (Exception e) {

			System.out.println("Trigger exception: " + e.getMessage());
			e.printStackTrace();

		}

		return 0;
	}

	/**
	 * Lookup the services associated with a system.
	 * 
	 * @param platformID
	 *            the ID of the platform to which the required system is connected.
	 * 
	 * @param systemID
	 *            the ID of the system to which the service is connected.
	 * 
	 * @param systemType
	 *            the type of the system to which the service is connected.
	 * 
	 * @return the service list encoded in JSON.
	 * 
	 * @throws SQLException
	 */
	private static StringBuilder servicesForSystem(String platformID, String systemID, String systemType)
			throws SQLException {

		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		StringBuilder services = new StringBuilder(",\"services\":[");

		try {

			/* Get the database connection */
			connection = DriverManager.getConnection("jdbc:default:connection");
//			connection = debugConnection;

			/* Execute the query */
			String sql = String.format("select * from data_feeds where platform_id='%s' and service_id='%s'",
					platformID, systemID);
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);

			/* Convert the result to JSON */

			while (resultSet.next()) {

				if (services.lastIndexOf("}") == services.length() - 1) {
					services.append(',');
				}

				services.append(String.format("{\"id\":\"%s/%s%s/%s:%s:%s\"}", //
						platformID, //
						systemID, //
						(systemType != null) ? ':' + systemType : "", //
						resultSet.getString("ID"), //
						resultSet.getString("TYPE_ID"), //
						resultSet.getString("DIRECTION")));
			}

			services.append(']');

		} catch (Exception e) {

			System.out.println("Query for system information failed: " + LogUtil.stackTrace(e));

		} finally {

			/* Tidy up */

			if (resultSet != null) {
				resultSet.close();
			}

			if (statement != null) {
				statement.close();
			}

			if (connection != null) {
				connection.close();
			}
		}

		return services;
	}

	/**
	 * Lookup the neighbour's availability.
	 * 
	 * @param nodeID
	 *            the ID of the discovering node.
	 * 
	 * @param neighbourID
	 *            the ID of the neighbour.
	 * 
	 * @throws SQLException
	 */
	private static StringBuilder neighbourAvailability(String nodeID, String neighbourID) throws SQLException {

		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		StringBuilder availabilityJSON = new StringBuilder();

		try {

			/* Get the database connection */
			connection = DriverManager.getConnection("jdbc:default:connection");
//			connection = debugConnection;

			/* Execute the query */
			String sql = String.format("select * from node_neighbours where node_id='%s' and neighbour_id='%s'",
					nodeID, neighbourID);
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);

			/* Convert the result to JSON */
			resultSet.next();
			String availability = resultSet.getString("AVAILABILITY");
			availability = (availability != null) ? availability : "";
			availabilityJSON.append(String.format(",\"availability\":\"%s\"", availability));

		} catch (Exception e) {

			System.out.println("Query for node neighbour failed: " + LogUtil.stackTrace(e));

		} finally {

			/* Tidy up */

			if (resultSet != null) {
				resultSet.close();
			}

			if (statement != null) {
				statement.close();
			}

			if (connection != null) {
				connection.close();
			}
		}

		return availabilityJSON;
	}

//  Test harness for developing/debugging trigger code outside of Derby
//	public static Connection debugConnection = null;
//
//	public static void main(String[] cla) {
//
//		try {
//
//			Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
//			debugConnection = DriverManager
//					.getConnection("jdbc:derby://localhost:6414/FABRIC;create=true;user=fabric;password=fabric");
//
//			TableUpdate.entryModified("SYSTEMS", "PLATFORM_ID/ID:TYPE_ID", "$fabric/$registry:$registry", "UPDATE");
//			TableUpdate.entryModified("NODE_NEIGHBOURS", "NODE_ID/NEIGHBOUR_ID", "scott/alan", "UPDATE");
//
//		} catch (Exception e) {
//
//			e.printStackTrace();
//
//		}
//	}
}
