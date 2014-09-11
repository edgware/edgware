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
 * Factory used to create routes between nodes and save/delete/query them in the Fabric Registry.
 * 
 * This factory can be accessed by calling FabricRegistry.getRouteFactory().
 */
public interface RouteFactory extends Factory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/**
	 * 
	 * @return
	 */
	public Route[] getAllRoutes();

	/**
	 * 
	 * @param id
	 * @return
	 */
	public Route[] getRouteByStartingNodeId(String id);

	/**
	 * 
	 * @param startNode
	 * @param endNode
	 * @return
	 */
	public Route[] getRoutes(String startNode, String endNode);

	/**
	 * 
	 * @param queryPredicates
	 * @return
	 * @throws RegistryQueryException
	 */
	public Route[] getRoutes(String queryPredicates) throws RegistryQueryException;

	/**
	 * 
	 * @param startNode
	 * @param endNode
	 * @param routeDesc
	 * @return
	 * @throws Exception
	 */
	public String[] getRouteNodes(String startNode, String endNode, String routeDesc) throws Exception;
}
