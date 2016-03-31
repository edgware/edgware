/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import fabric.registry.exception.RegistryQueryException;

/**
 * Factory used to create Nodes and save/delete/query them in the Fabric Registry.
 * 
 * This factory can be accessed by calling FabricRegistry.getNodeFactory().
 */
public interface NodeFactory extends Factory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/**
	 * Creates a Node object, specifying only the mandatory attributes.
	 * 
	 * @param id
	 *            - the identifier for the node.
	 * @param typeId
	 *            - the type of node.
	 * @return a Node object, with only the specified fields set; all other fields will be set to default values.
	 */
	public Node createNode(String id, String typeId);

	/**
	 * 
	 * @param id
	 * @param typeId
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
	 * @param ipAddress
	 * @param port
	 * @return
	 */
	public Node createNode(String id, String typeId, String affiliation, String securityClassification,
			String readiness, String availability, double latitude, double longitude, double altitude, double bearing,
			double velocity, String description, String attributes, String attributesURI);

	/**
	 * 
	 * @return
	 */
	public Node[] getAllNodes();

	/**
	 * 
	 * @param id
	 * @return
	 */
	public Node getNodeById(String id);

	/**
	 * 
	 * @param typeId
	 * @return
	 */
	public Node[] getNodesByType(String typeId);

	/**
	 * 
	 * @param queryPredicates
	 * @return
	 * @throws RegistryQueryException
	 */
	public Node[] getNodes(String queryPredicates) throws RegistryQueryException;

}
