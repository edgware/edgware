/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2008
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

/**
 * Represents information about a node.
 * 
 * @see fabric.registry.NodeFactory#createNode(String, String, String, String, String, String, double, double, double,
 *      double, double, String, String, String)
 */
public interface Node extends RegistryObject {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2008";

	/**
	 * Get the affiliation of the node.
	 * 
	 * @return the affiliation or null if none was specified.
	 */
	public String getAffiliation();

	/**
	 * Set the affiliation of the node.
	 * 
	 * @param affiliation
	 *            - an affiliation or null.
	 */
	public void setAffiliation(String affiliation);

	/**
	 * Get the altitude in degrees of this node.
	 * 
	 * @return the altitude.
	 */
	public double getAltitude();

	/**
	 * Set the altitude in degrees of this node.
	 * 
	 * @param altitude
	 *            - the altitude.
	 */
	public void setAltitude(double altitude);

	/**
	 * Get the custom attributes for the node.
	 * 
	 * @return the attributes or null if none are specified.
	 */
	public String getAttributes();

	/**
	 * Set the custom attributes for the node.
	 * 
	 * @param attributes
	 *            - the attributes or null.
	 */
	public void setAttributes(String attributes);

	/**
	 * Get the uri of the attributes.
	 * 
	 * @return the uri or null if none were specified.
	 */
	public String getAttributesURI();

	/**
	 * Set the uri of the attributes.
	 * 
	 * @param attributesURI
	 *            - a uri or null.
	 */
	public void setAttributesURI(String attributesURI);

	/**
	 * Get the availability status of the node, used in determining whether messages can be routed via the node by the
	 * Fabric.
	 * 
	 * @return the availability status or null if none was specified.
	 */
	public String getAvailability();

	/**
	 * Set the availability status of the node, used in determining whether messages can be routed via the node by the
	 * fabric.
	 * 
	 * @param availability
	 *            - an availability status (e.g. 'Available') or null.
	 */
	public void setAvailability(String availability);

	/**
	 * Get the description of the node.
	 * 
	 * @return the description or null if none was specified.
	 */
	public String getDescription();

	/**
	 * Set the description of the node.
	 * 
	 * @param description
	 *            - the description or null.
	 */
	public void setDescription(String description);

	/**
	 * Get the latitudinal position of the node.
	 * 
	 * @return the latitude in degrees.
	 */
	public double getLatitude();

	/**
	 * Set the latitudinal position of the node.
	 * 
	 * @param latitude
	 *            - the latitude in degrees.
	 */
	public void setLatitude(double latitude);

	/**
	 * Get the longitudinal position of the node.
	 * 
	 * @return the longitude in degrees.
	 */
	public double getLongitude();

	/**
	 * Set the longitudinal position of the node.
	 * 
	 * @param longitude
	 *            - the longitude in degrees.
	 */
	public void setLongitude(double longitude);

	/**
	 * Get the identifier for the node, sometimes also referred to as the node name.
	 * 
	 * @return the id of the node.
	 */
	public String getId();

	/**
	 * Set the identifier for the node - this attribute must always be specified. Without it, the object cannot be saved
	 * to the Fabric Registry and will generate an IncompleteObjectException.
	 * 
	 * @param nodeId
	 *            - the node id.
	 */
	public void setId(String nodeId);

	/**
	 * Get the readiness status of the node.
	 * 
	 * @return the readiness state or null if none was specified.
	 */
	public String getReadiness();

	/**
	 * Set the readiness status of the node.
	 * 
	 * @param readiness
	 *            - the readiness state or null.
	 */
	public void setReadiness(String readiness);

	/**
	 * Get the type of the node.
	 * 
	 * @return the id of the type.
	 */
	public String getTypeId();

	/**
	 * Set the type of the node - all nodes must have a defined type.
	 * 
	 * @param typeId
	 *            - the id of the type.
	 */
	public void setTypeId(String typeId);

	/**
	 * Get the security classification for the node.
	 * 
	 * @return the security classification or null if none was specified.
	 */
	public String getSecurityClassification();

	/**
	 * Set the security classification for the node.
	 * 
	 * @param securityClassification
	 *            - the classification or null.
	 */
	public void setSecurityClassification(String securityClassification);

	/**
	 * Get the bearing that the node is on.
	 * 
	 * @return the bearing in degrees.
	 */
	public double getBearing();

	/**
	 * Set the bearing that the node is on.
	 * 
	 * @param bearing
	 *            - the bearing in degrees.
	 */
	public void setBearing(double bearing);

	/**
	 * Get the velocity of the node.
	 * 
	 * @return the velocity.
	 */
	public double getVelocity();

	/**
	 * Set the velocity of the node.
	 * 
	 * @param velocity
	 *            - the velocity.
	 */
	public void setVelocity(double velocity);

	/**
	 * Get the list of platforms attached to the node.
	 * 
	 * @return the list of platforms or null if no platforms are attached.
	 */
	public Platform[] getPlatforms();

	/**
	 * Get a list of neighbouring of nodes.
	 * 
	 * @return the list of the neighbours or null if there are none.
	 */
	public NodeNeighbour[] getUniqueNeighbours();

	/**
	 * Get the ip mappings for the node.
	 * 
	 * @return the ip mappings.
	 */
	public NodeIpMapping[] getAllIpMappings();

	/**
	 * Get the list of fabric plugins configured on the node.
	 * 
	 * @param localOnly
	 *            indicates wethere localOnly plugins wanted
	 * @return the list of plugins or null if none are configured.
	 */
	public FabricPlugin[] getFabricPlugins(boolean localOnly);

	/**
	 * Get the list of node plugins configured on the node.
	 * 
	 * @param localOnly
	 *            indicates wethere localOnly plugins wanted
	 * @return the list of plugins or null if none are required.
	 */
	public NodePlugin[] getNodePlugins(boolean localOnly);

}
