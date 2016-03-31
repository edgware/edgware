/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

/**
 * Factory used to create IP mapping objects for Nodes and save/delete/query them
 * in the Fabric Registry.
 * 
 * This factory can be accessed by calling FabricRegistry.getNodeIpMappingFactory();
 * 
 *
 */
public interface NodeIpMappingFactory extends Factory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";
	
	/**
	 * Create a new NodeIpMapping object.
	 * 
	 * @param nodeId - the node the mapping applies to
	 * @param nodeInterface - the interface of the node the mapping applies to
	 * @param ipAddress - the dotted decimal ip address of the node
	 * @param port - the port number that the broker is listening on
	 * @return a NodeIpMapping object with the appropriate fields set
	 */
	public NodeIpMapping createNodeIpMapping(String nodeId, String nodeInterface, String ipAddress, int port);
	
	/**
	 * Get the list of all IP mappings defined in the Fabric Registry.
	 * 
	 * @return a list of all IP mappings or an empty list if none exist.
	 */
	public NodeIpMapping[] getAllMappings();
	
	
	/**
	 * Get all the IP mappings for a particular node.
	 * 
	 * @param nodeId - the node
	 * @return the IP mapping information
	 */
	public NodeIpMapping[] getAllMappingsForNode(String nodeId);
	
	/**
	 * Get an IP mapping for a particular node.
	 * There may be more than one, which is returned is undefined.
	 * 
	 * @param nodeId - the node
	 * @return the IP mapping information
	 */
	public NodeIpMapping getAnyMappingForNode(String nodeId);
	
	/**
	 * Get the IP mapping for a particular node.
	 * 
	 * @param nodeId - the node
	 * @param nodeInterface - the node network Interface name
	 * @return the IP mapping information
	 */
	public NodeIpMapping getMappingForNode(String nodeId, String nodeInterface);
	
	/**
	 * Perform a custom query for IP mappings that meet specific criteria.
	 * 
	 * @param queryPredicates - the predicates to use that form the SQL WHERE clause.
	 * @return a list of IP mappings or an empty list if none exist
	 */
	public NodeIpMapping[] getMappings(String queryPredicates);
}
