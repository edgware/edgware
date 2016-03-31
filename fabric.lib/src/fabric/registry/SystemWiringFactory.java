/*
 * (C) Copyright IBM Corp. 2010
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import fabric.registry.exception.RegistryQueryException;

/**
 * Factory used to create SystemWirings and save/delete/query them in the Fabric Registry.
 * 
 * @see fabric.registry.FabricRegistry#getSystemWiringFactory()
 */
public interface SystemWiringFactory extends Factory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010";

	/**
	 * Creates a SystemWiring, specifying only the mandatory attributes.
	 * 
	 * @param compositeId
	 *            the id of the composite.
	 * @param fromSystemPlatformId
	 *            the id of the output system platform.
	 * @param fromSystemId
	 *            the id of the output system.
	 * @param fromInterfaceId
	 *            the id of the output system interface.
	 * @param toSystemPlatformId
	 *            the id of the input system platform.
	 * @param toSystemId
	 *            the id of the input system.
	 * @param toInterfaceId
	 *            the id of the input system interface.
	 * @return a SystemWiring object with only the required fields set; all other fields will be set to default values.
	 */
	public SystemWiring create(String compositeId, String fromSystemPlatformId, String fromSystemId,
			String fromInterfaceId, String toSystemPlatformId, String toSystemId, String toInterfaceId);

	/**
	 * Creates a SystemWiring using the specified information.
	 * 
	 * @param compositeId
	 *            the id of the composite system.
	 * @param fromSystemPlatformId
	 *            the id of the output system platform.
	 * @param fromSystemId
	 *            the id of the output system.
	 * @param fromInterfaceId
	 *            the id of the output system interface.
	 * @param toSystemPlatformId
	 *            the id of the input system platform.
	 * @param toSystemId
	 *            the id of the input system.
	 * @param toInterfaceId
	 *            the id of the input system interface.
	 * @param attributes
	 *            the custom attributes string.
	 * @param attributesURI
	 *            the custom attributes uri.
	 * 
	 * @return a populated SystemWiring object.
	 */
	public SystemWiring create(String compositeId, String fromSystemPlatformId, String fromSystemId,
			String fromInterfaceId, String toSystemPlatformId, String toSystemId, String toInterfaceId,
			String attributes, String attributesURI);

	/**
	 * Get a list of system wirings using a custom WHERE-clause predicate.
	 * 
	 * @param queryPredicates
	 *            the predicates to use in the WHERE clause.
	 * @return a list of system wirings that match or null otherwise.
	 * @throws RegistryQueryException
	 *             if the predicates are malformed.
	 */
	public SystemWiring[] get(String queryPredicates) throws RegistryQueryException;

	/**
	 * Get the list of all system wirings defined in the fabric registry.
	 * 
	 * @return the complete list of all wirings.
	 */
	public SystemWiring[] getAll();

	/**
	 * Get the metadata for a specific system wiring.
	 * 
	 * @param fromSystemPlatformId
	 *            the id of the output system platform.
	 * @param fromSystemId
	 *            the id of the output system.
	 * @param fromInterfaceId
	 *            the id of the output system interface.
	 * @param toSystemPlatformId
	 *            the id of the input system platform.
	 * @param toSystemId
	 *            the id of the input system.
	 * @param toInterfaceId
	 *            the id of the input system interface.
	 * 
	 * @return the system wiring or null otherwise.
	 */
	public SystemWiring getById(String compositeId, String fromSystemPlatformId, String fromSystemId,
			String fromInterfaceId, String toSystemPlatformId, String toSystemId, String toInterfaceId);
}