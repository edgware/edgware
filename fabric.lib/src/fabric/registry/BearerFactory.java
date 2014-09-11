/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import fabric.registry.exception.RegistryQueryException;

/**
 * Factory used to create <code>Bearer</code>s and save/delete/query them in the Fabric Registry.
 * 
 * This factory can be accessed by calling FabricRegistry.getBearerFactory().
 */
public interface BearerFactory extends Factory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/**
	 * Creates a <code>Bearer</code> object, specifying only the mandatory attributes.
	 * 
	 * @param id
	 *            the identifier for the bearer.
	 * 
	 * @param availability
	 *            the availability status of the bearer.
	 * 
	 * @return a Bearer object, with only the specified fields set; all other fields will be set to default values.
	 */
	public Bearer createBearer(String id, String availability);

	/**
	 * Creates a <code>Bearer</code> object.
	 * 
	 * @param id
	 *            the identifier for the bearer.
	 * 
	 * @param availability
	 *            the availability status of the bearer.
	 * 
	 * @param description
	 *            the description of the bearer.
	 * 
	 * @param attributes
	 *            the bearer attributes.
	 * 
	 * @param attributesURI
	 *            the URI of bearer attributes.
	 * 
	 * @return the new instance.
	 */
	public Bearer createBearer(String id, String availability, String description, String attributes,
			String attributesURI);

	/**
	 * Answers a list of all registered bearers.
	 * 
	 * @return the list.
	 */
	public Bearer[] getAllBearers();

	/**
	 * Answers the bearer matching the specified ID (if any).
	 * 
	 * @param id
	 *            the ID of the bearer.
	 * 
	 * @return the bearer, or <code>null</code> if no match is found.
	 */
	public Bearer getBearerById(String id);

	/**
	 * Answers the list of all bearers matching the specified query predicate (if any).
	 * 
	 * @param queryPredicates
	 *            the predicate
	 * 
	 * @return the list.
	 * 
	 * @throws RegistryQueryException
	 */
	public Bearer[] getBearers(String queryPredicates) throws RegistryQueryException;

}
