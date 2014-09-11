/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2011, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import fabric.registry.exception.RegistryQueryException;

/**
 * Factory used to create Systems and save/delete/query them in the Fabric Registry.
 * 
 * This factory can be accessed by calling FabricRegistry.getSystemFactory().
 */
public interface SystemFactory extends Factory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2011, 2014";

	/**
	 * Instantiates a Registry object representing a system, specifying only mandatory fields.
	 * 
	 * @param platformID
	 *            the platform with which the system is associated.
	 * 
	 * @param systemID
	 *            the id of the system.
	 * 
	 * @param typeID
	 *            the type of the system.
	 * 
	 * @return the instantiated system object.
	 */
	public System createSystem(String platformID, String systemID, String typeID);

	/**
	 * Instantiates a Registry object representing a system, specifying every single field.
	 * 
	 * @param platformID
	 * @param systemID
	 * @param typeID
	 * @param credentials
	 * @param readiness
	 * @param availability
	 * @param latitude
	 * @param longitude
	 * @param altitude
	 * @param bearing
	 * @param velocity
	 * @param description
	 * @param attributes
	 * @param attributesURI
	 * @return the instantiated system object.
	 */
	public System createSystem(String platformID, String systemID, String typeID, String credentials, String readiness,
			String availability, double latitude, double longitude, double altitude, double bearing, double velocity,
			String description, String attributes, String attributesURI);

	/**
	 * Get a list of systems using a custom predicate.
	 * 
	 * @param queryPredicates
	 * @return
	 * @throws RegistryQueryException
	 */
	public System[] getSystems(String queryPredicates) throws RegistryQueryException;

	/**
	 * Get a particular system.
	 * 
	 * @param id
	 *            the name of the system.
	 * 
	 * @return the system.
	 */
	public System getSystemsById(String platformId, String systemID);

	/**
	 * Get all systems of a given type.
	 * 
	 * @param typeId
	 * 
	 * @return the list of systems.
	 */
	public System[] getSystemsByType(String typeId);

	/**
	 * Get all systems on a particular platform.
	 * 
	 * @param platformId
	 * 
	 * @return the list of systems.
	 */
	public System[] getSystemsByPlatform(String platformId);

	/**
	 * Get all the systems that are listed in the Registry.
	 * 
	 * @return the list of systems.
	 */
	public System[] getAllSystems();

	/**
	 * Get all the systems that are listed in the Registry.
	 * 
	 * @return the list of systems.
	 */
	public System[] getAll();

}
