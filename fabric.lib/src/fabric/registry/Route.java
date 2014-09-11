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
 * Represents a Route in the Fabric Registry.
 */
public interface Route extends RegistryObject {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2008";

	/**
	 * Get the id of the last node in the route.
	 * 
	 * @return
	 */
	public String getEndNode();

	/**
	 * 
	 * @param endNode
	 */
	public void setEndNode(String endNode);

	/**
	 * 
	 * @return
	 */
	public int getOrdinal();

	/**
	 * 
	 * @param ordinal
	 */
	public void setOrdinal(int ordinal);

	/**
	 * Get the route definition.
	 * 
	 * @return
	 */
	public String getRoute();

	/**
	 * 
	 * @param route
	 */
	public void setRoute(String route);

	/**
	 * Get the id of the node at the start of the route.
	 * 
	 * @return
	 */
	public String getStartNode();

	/**
	 * 
	 * @param startNode
	 */
	public void setStartNode(String startNode);

}
