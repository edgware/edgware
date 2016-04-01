/*
 * (C) Copyright IBM Corp. 2012
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric;

import java.util.logging.Level;

import fabric.core.logging.FLog;
import fabric.registry.FabricRegistry;
import fabric.registry.exception.PersistenceException;

/**
 * Provides access to actor configuration settings from the following configuration tables in the Fabric Registry (in
 * order):
 * <ol>
 * <li>Actor</li>
 * <li>Platform</li>
 * <li>Node</li>
 * <li>Default</li>
 * </ol>
 */
public class ActorConfig extends PlatformConfig {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

	/*
	 * Class fields
	 */

	/** To hold the ID of the actor for which configuration information is required. */
	private final String actor;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance, initialized from the specified properties file.
	 * 
	 * @param actor
	 *            the ID of the actor for which configuration information is required.
	 * 
	 * @param platform
	 *            the ID of the platform for which configuration information is required.
	 * 
	 * @param node
	 *            the ID of the node for which configuration information is required.
	 * 
	 * @param configFile
	 *            Fabric properties file name/path.
	 */
	public ActorConfig(String actor, String platform, String node, String configFile) {

		super(platform, node, configFile);
		this.actor = actor;

	}

	/**
	 * Answers the actor for which configuration information is being provided.
	 * 
	 * @return the actor ID.
	 */
	public String actor() {

		return actor;

	}

	/**
	 * Answers the value of the specified actor configuration property from the following configuration tables in the
	 * Fabric Registry (in order):
	 * <ol>
	 * <li>Actor</li>
	 * <li>Platform</li>
	 * <li>Node</li>
	 * <li>Default</li>
	 * </ol>
	 * 
	 * @param key
	 *            the name of the configuration property.
	 * 
	 * @return the configuration property value, or <code>null</code> if it has not been set.
	 */
	@Override
	protected String lookupRegistryProperty(String key) {

		String propertyValue = null;

		try {

			/* Query the actor configuration properties table in the Registry */
			propertyValue = FabricRegistry.runStringQuery("select VALUE from " + FabricRegistry.ACTOR_CONFIG
					+ " where ACTOR_ID='" + actor + "' and PLATFORM_ID='" + platform() + "' and NAME='" + key + "'",
					queryScope);
			logger.log(Level.FINEST, "Registry lookup for key \"{0}\" returned \"{1}\"", new Object[] {key,
					propertyValue});

			/* If we didn't find a value... */
			if (propertyValue == null) {

				/* Query the node and default configuration properties table in the Registry */
				propertyValue = super.lookupRegistryProperty(key);

			}

		} catch (PersistenceException e) {

			logger.log(Level.WARNING, "Exception looking up configuration property \"{0}\": {1}", new Object[] {key,
					FLog.stackTrace(e)});

		}

		return propertyValue;
	}

}
