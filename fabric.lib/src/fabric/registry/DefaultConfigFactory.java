/*
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import fabric.registry.exception.RegistryQueryException;

/**
 * Factory used to create default configuration values and save/delete/query them in the Fabric Registry.
 */
public interface DefaultConfigFactory extends Factory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/**
	 * Creates a default configuration object.
	 * 
	 * @param name
	 *            the name of the configuration value.
	 * 
	 * @param value
	 *            the configuration value.
	 * 
	 * @return the object.
	 */
	public DefaultConfig createDefaultConfig(String name, String value);

	/**
	 * Answers the full list of default configuration values from the Registry.
	 * 
	 * @return the list of values.
	 */
	public DefaultConfig[] getAllDefaultConfig();

	/**
	 * Answers the specified default configuration value.
	 * 
	 * @param name
	 *            the name of the configuration value.
	 * 
	 * @return the configuration value, or <code>null</code> if not found.
	 */
	public DefaultConfig getDefaultConfigByName(String name);

	/**
	 * Answers a list of default configuration values, determined by the specified query predicate.
	 * 
	 * @param predicate
	 *            the query predicate.
	 * 
	 * @return the list of values.
	 * 
	 * @throws RegistryQueryException
	 */
	public DefaultConfig[] getDefaultConfig(String predicate) throws RegistryQueryException;

}