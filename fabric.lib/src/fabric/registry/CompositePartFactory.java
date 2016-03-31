/*
 * (C) Copyright IBM Corp. 2010
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import fabric.registry.exception.RegistryQueryException;

/**
 * Factory used to create CompositeParts and save/delete/query them in the Fabric Registry.
 * 
 * @see fabric.registry.FabricRegistry#getCompositePartFactory()
 */
public interface CompositePartFactory extends Factory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010";

	/**
	 * Creates a Composite Part, specifying only the mandatory attributes.
	 * 
	 * @param id
	 *            - the id of the composite service.
	 * @return a CompositePart object with only the required fields set; all other fields will be set to default values.
	 */
	public CompositePart create(String compositeId, String servicePlatformId, String serviceId);

	/**
	 * Creates a Composite Part using the specified information.
	 * 
	 * @param compositeId
	 *            - the unique id of the composite service.
	 * @param servicePlatformId
	 *            - the id of the platform associated with the service.
	 * @param serviceId
	 *            - the id of the service.
	 * @param attributes
	 *            - the custom attributes string.
	 * @param attributesURI
	 *            - the custom attributes uri.
	 * 
	 * @return a populated CompositePart object.
	 */
	public CompositePart create(String compositeId, String servicePlatformId, String serviceId, String attributes,
			String attributesURI);

	/**
	 * Get a list of composite service parts using a custom WHERE-clause predicate.
	 * 
	 * @param queryPredicates
	 *            - the predicates to use in the WHERE clause.
	 * @return a list of service parts that match or null otherwise.
	 * @throws RegistryQueryException
	 *             if the predicates are malformed.
	 */
	public CompositePart[] get(String queryPredicates) throws RegistryQueryException;

	/**
	 * Get the list of all composite service parts defined in the fabric registry.
	 * 
	 * @return the complete list of all parts.
	 */
	public CompositePart[] getAll();

	/**
	 * Get the metadata for a specific composite service part.
	 * 
	 * @param compositeId
	 *            - the id of the composite service.
	 * @param servicePlatformId
	 *            - the id of the output service platform.
	 * @param serviceId
	 *            - the id of the output service.
	 * 
	 * @return the composite service part or null otherwise.
	 */
	public CompositePart getById(String compositeId, String servicePlatformId, String serviceId);
}