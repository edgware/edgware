/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import fabric.registry.exception.RegistryQueryException;

/**
 * Factory used to create Platforms and save/delete/query them in the Fabric Registry.
 * 
 * This factory can be accessed by calling FabricRegistry.getPlatformFactory().
 */
public interface PlatformFactory extends Factory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/**
	 * Creates a Platform object, specifying only the mandatory attributes.
	 * 
	 * @param platformId
	 *            - the id of the platform.
	 * @param typeId
	 *            - the type of platform.
	 * @param nodeId
	 *            - the node to which the platform is attached.
	 * @return a Platform object with the specified fields set; all other fields will be set default values.
	 */
	public Platform createPlatform(String platformId, String typeId, String nodeId);

	/**
	 * 
	 * @param id
	 * @param typeId
	 * @param nodeId
	 * @param affiliation
	 * @param securityClassification
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
	 * @return
	 */
	public Platform createPlatform(String platformId, String typeId, String nodeId, String affiliation,
			String securityClassification, String readiness, String availability, double latitude, double longitude,
			double altitude, double bearing, double velocity, String description, String attributes,
			String attributesURI);

	/**
	 * 
	 * @return
	 */
	public Platform[] getAllPlatforms();

	/**
	 * 
	 * @param id
	 * @return
	 */
	public Platform getPlatformById(String id);

	/**
	 * 
	 * @param nodeId
	 * @return
	 */
	public Platform[] getPlatformsByNode(String nodeId);

	/**
	 * 
	 * @param typeId
	 * @return
	 */
	public Platform[] getPlatformsByType(String typeId);

	/**
	 * 
	 * @param queryPredicates
	 * @return
	 * @throws RegistryQueryException
	 */
	public Platform[] getPlatforms(String queryPredicates) throws RegistryQueryException;

}
