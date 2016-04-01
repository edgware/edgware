/*
 * (C) Copyright IBM Corp. 2009, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import fabric.registry.exception.RegistryQueryException;

/**
 * Factory used to create Types (Feed, Node, Platform and System) and save/delete/query them in the Fabric Registry.
 * 
 * This factory can be accessed by calling FabricRegistry.getTypeFactory().
 */
public interface TypeFactory extends Factory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2014";

	/**
	 * 
	 * @param typeId
	 * @param description
	 * @param attributes
	 * @param attributesUri
	 * @return
	 */
	public Type createActorType(String typeId, String description, String attributes, String attributesUri);

	/**
	 * 
	 * @param typeId
	 * @param description
	 * @param attributes
	 * @param attributesUri
	 * @return
	 */
	public Type createServiceType(String typeId, String description, String attributes, String attributesUri);

	/**
	 * 
	 * @param typeId
	 * @param description
	 * @param attributes
	 * @param attributesUri
	 * @return
	 */
	public Type createNodeType(String typeId, String description, String attributes, String attributesUri);

	/**
	 * 
	 * @param typeId
	 * @param description
	 * @param attributes
	 * @param attributesUri
	 * @return
	 */
	public Type createPlatformType(String typeId, String description, String attributes, String attributesUri);

	/**
	 * 
	 * @param typeId
	 * @param description
	 * @param attributes
	 * @param attributesUri
	 * @return
	 */
	public Type createSystemType(String typeId, String description, String attributes, String attributesUri);

	/**
	 * 
	 * @return
	 */
	public Type[] getAllActorTypes();

	/**
	 * 
	 * @return
	 */
	public Type[] getAllServiceTypes();

	/**
	 * 
	 * @return
	 */
	public Type[] getAllNodeTypes();

	/**
	 * 
	 * @return
	 */
	public Type[] getAllPlatformTypes();

	/**
	 * 
	 * @return
	 */
	public Type[] getAllSystemTypes();

	/**
	 * 
	 * @return
	 */
	public Type[] getAllServiceWiring();

	/**
	 * 
	 * @param id
	 * @return
	 */
	public Type getActorType(String id);

	/**
	 * 
	 * @param id
	 * @return
	 */
	public Type getServiceType(String id);

	/**
	 * 
	 * @param id
	 * @return
	 */
	public Type getNodeType(String id);

	/**
	 * 
	 * @param id
	 * @return
	 */
	public Type getPlatformType(String id);

	/**
	 * 
	 * @param id
	 * @return
	 */
	public Type getSystemType(String id);

	/**
	 * 
	 * @param predicateQuery
	 * @return
	 * @throws RegistryQueryException
	 */
	public Type[] getActorTypes(String predicateQuery) throws RegistryQueryException;

	/**
	 * 
	 * @param predicateQuery
	 * @return
	 * @throws RegistryQueryException
	 */
	public Type[] getServiceTypes(String predicateQuery) throws RegistryQueryException;

	/**
	 * 
	 * @param predicateQuery
	 * @return
	 * @throws RegistryQueryException
	 */
	public Type[] getNodeTypes(String predicateQuery) throws RegistryQueryException;

	/**
	 * 
	 * @param predicateQuery
	 * @return
	 * @throws RegistryQueryException
	 */
	public Type[] getPlatformTypes(String predicateQuery) throws RegistryQueryException;

	/**
	 * 
	 * @param predicateQuery
	 * @return
	 * @throws RegistryQueryException
	 */
	public Type[] getSystemTypes(String predicateQuery) throws RegistryQueryException;
}
