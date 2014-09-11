/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009, 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.trigger;

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

			if (resourceManager.isReportable(tableName)) {

				/* Build the notification message (a JSON object) */
				String notification = String.format(
						"{\"table\":\"%s\",\"key\":\"%s\",\"id\":\"%s\",\"action\":\"%s\",\"timestamp\":%d}",
						tableName, key, id, action, System.currentTimeMillis());

				/* Publish to the Fabric */
				resourceManager.sendRegistryUpdate(notification);
			}

		} catch (Exception e) {

			System.out.println("Trigger exception: " + e.getMessage());
			e.printStackTrace();

		}

		return 0;
	}

}
