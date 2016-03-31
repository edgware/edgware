/*
 * (C) Copyright IBM Corp. 2010
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import fabric.registry.exception.RegistryQueryException;

/**
 * Factory used to create Composite Services and save/delete/query them in the Fabric Registry.
 * 
 * @see fabric.registry.FabricRegistry#getCompositeSystemFactory()
 */
public interface CompositeServiceFactory extends Factory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010";

	/**
	 * Creates a Composite System, specifying only the mandatory attributes.
	 * 
	 * @param id
	 *            - the id of the composite service.
	 * @return a CompositeService object with only the required fields set; all other fields will be set to default
	 *         values.
	 */
	public CompositeService create(String id);

	/**
	 * Creates a Feed using the specified information.
	 * 
	 * @param id
	 *            - the unique id of the composite service.
	 * @param type
	 *            - the type of the service.
	 * @param affiliation
	 *            - the affiliation of the service.
	 * @param credentials
	 *            - the security credentials associated with the service.
	 * @param description
	 *            - the description.
	 * @param attributes
	 *            - the custom attributes string.
	 * @param attributesURI
	 *            - the custom attributes uri.
	 * 
	 * @return a populated CompositeService object.
	 */
	public CompositeService create(String id, String type, String affiliation, String credentials, String description,
			String attributes, String attributesURI);

	/**
	 * Get a list of systems using a custom WHERE-clause predicate.
	 * 
	 * @param queryPredicates
	 *            - the predicates to use in the WHERE clause.
	 * @return a list of systems that match or null otherwise.
	 * @throws RegistryQueryException
	 *             if the predicates are malformed.
	 */
	public CompositeService[] get(String queryPredicates) throws RegistryQueryException;

	/**
	 * Get a list of systems of a particular type.
	 * 
	 * @param type
	 *            - the type of systems to find.
	 * @return a list of composite systems of that type or null if none exist.
	 */
	public CompositeService[] getByType(String type);

	/**
	 * Get the list of all composite systems defined in the fabric registry.
	 * 
	 * @return the complete list of all composite systems.
	 */
	public CompositeService[] getAll();

	/**
	 * Get the metadata for a specific composite service.
	 * 
	 * @param id
	 *            - the id of the composite service.
	 * 
	 * @return the service or null otherwise.
	 */
	public CompositeService getById(String id);
}