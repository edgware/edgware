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
 * Object representing a neighbouring node and associated connection details.
 * 
 *
 */
public interface NodeNeighbour extends RegistryObject {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2008";
	
	/** A constant to represent a STATIC neighbour for purposes of discoveredBy. */
	public static final String DISCOVEREDBY_STATIC = "STATIC";

	/** A constant to represent availability of AVAILABLE. */
	public static final String AVAILABLE = "AVAILABLE";
	/** A constant to represent availability of UNAVAILABLE. */
	public static final String UNAVAILABLE = "UNAVAILABLE";
	
	/**
	 * 
	 * @return
	 */
	public String getNodeId();

	/**
	 * 
	 * @param nodeId
	 */
	public void setNodeId(String nodeId);
	
	/**
	 * 
	 * @return
	 */
	public String getNodeInterface();

	/**
	 * 
	 * @param nodeInterface
	 */
	public void setNodeInterface(String nodeInterface);


	/**
	 * 
	 * @return
	 */
	public String getNeighbourId();

	/**
	 * 
	 * @param nodeNeighbourId
	 */
	public void setNeighbourId(String nodeNeighbourId);
	
	/**
	 * 
	 * @return
	 */
	public String getNeighbourInterface();

	/**
	 * 
	 * @param nodeNeighbourId
	 */
	public void setNeighbourInterface(String nodeNeighbourInterface);


	/**
	 * 
	 * @return
	 */
	public String getDiscoveredBy();

	/**
	 * 
	 * @param nodeId
	 */
	public void setDiscoveredBy(String discoveredBy);
	
	/**
	 * 
	 * @return
	 */
	public String getAvailability();

	/**
	 * 
	 * @param nodeId
	 */
	public void setAvailability(String availability);
	/**
	 * 
	 * @return
	 */
	public String getBearerId();

	/**
	 * 
	 * @param nodeId
	 */
	public void setBearerId(String bearerId);
	
	/**
	 * 
	 * @return
	 */
	public String getConnectionAttributes();

	
	/**
	 * @param connectionAttributes
	 */
	public void setConnectionAttributes(String connectionAttributes);

	/**
	 * 
	 * @return
	 */
	public String getConnectionAttributesUri();

	/**
	 * 
	 * @param uri
	 */
	public void setConnectionAttributesUri(String uri);
	
	/**
	 * Get the IP address mappings for this node neighbour
	 * @return
	 */
	public NodeIpMapping getIpMappingForNeighbour();

}
