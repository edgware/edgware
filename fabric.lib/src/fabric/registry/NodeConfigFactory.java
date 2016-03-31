/*
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import fabric.registry.exception.RegistryQueryException;

/**
 * Factory used to create node configuration values and save/delete/query them in the Fabric Registry.
 */
public interface NodeConfigFactory extends Factory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/**
	 * Creates a default configuration object.
	 * 
	 * @param node
	 *            the name of the node.
	 * 
	 * @param name
	 *            the name of the configuration value.
	 * 
	 * @param value
	 *            the configuration value.
	 * 
	 * @return the object.
	 */
	public NodeConfig createNodeConfig(String node, String name, String value);

	/**
	 * Answers the full list of node configuration values from the Registry.
	 * 
	 * @return the list of values.
	 */
	public NodeConfig[] getAllNodeConfig();

	/**
	 * Answers the specified node configuration value.
	 * 
	 * @param node
	 *            the name of the node.
	 * 
	 * @param name
	 *            the name of the configuration value.
	 * 
	 * @return the configuration value, or <code>null</code> if not found.
	 */
	public NodeConfig getNodeConfigByName(String node, String name);

	/**
	 * Answers a list of node configuration values, determined by the specified query predicate.
	 * 
	 * @param predicate
	 *            the query predicate.
	 * 
	 * @return the list of values.
	 * 
	 * @throws RegistryQueryException
	 */
	public NodeConfig[] getNodeConfig(String predicate) throws RegistryQueryException;

}