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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

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

			StringBuilder notification = new StringBuilder('{');
			StringBuilder notificationDetail = new StringBuilder();

			if (resourceManager.isReportable(tableName)) {

				switch (tableName.toUpperCase()) {

				case "SERVICES":

					/* Lookup and record the feeds associated with this service */
					String[] keys = key.split("/");
					notificationDetail = query(keys[0], keys[1]);

				default:

					/* Build the notification message (a JSON object) */
					notification.append(String.format(
							"\"table\":\"%s\",\"key\":\"%s\",\"id\":\"%s\",\"action\":\"%s\",\"timestamp\":%d",
							tableName, key, id, action, System.currentTimeMillis()));
				}

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
	 *            the ID of the system for which the service list is required.
	 * 
	 * @return the service list encoded in JSON.
	 * 
	 * @throws SQLException
	 */
	private static StringBuilder query(String platformID, String systemID) throws SQLException {

		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		StringBuilder services = new StringBuilder(",\"services\":");

		try {

			/* Get the database connection */
			connection = DriverManager.getConnection("jdbc:default:connection");

			/* Build the query */
			String sql = "select * from data_feeds where platform_id=? and service_id=?";
			statement = connection.prepareStatement(sql);

			/* Set the parameter */
			statement.setString(1, platformID);
			statement.setString(2, systemID);
			resultSet = statement.executeQuery();

			/* Convert the result to JSON */

			while (resultSet.next()) {

				services.append("[{");
				services.append("\"id\":\"" + resultSet.getString("ID") + "\"");
				services.append("\"type\":\"" + resultSet.getString("TYPE_ID") + "\"");
				services.append("\"mode\":\"" + resultSet.getString("DIRECTION") + "\"");
				services.append('}');

				if (!resultSet.isLast()) {
					services.append(',');
				}
			}

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
}
