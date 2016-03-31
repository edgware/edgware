/*
 * (C) Copyright IBM Corp. 2008
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import fabric.registry.exception.RegistryQueryException;
import fabric.session.NodeDescriptor;

/**
 * Factory used to create node neighbour associations and save/delete/query them in the Fabric Registry.
 * 
 * This factory can be accessed by calling FabricRegistry.getNodeNeighbourFactory().
 * 
 * 
 */
public interface NodeNeighbourFactory extends Factory {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2008";

	/**
	 * 
	 * @param nodeId
	 * @param nodeInterface
	 * @param neighbourId
	 * @param neighbourInterface
	 * @param discoveredBy
	 * @param availability
	 * @param bearerId
	 * @param connectionAttributes
	 * @param uri
	 * @return
	 */
	public NodeNeighbour createNodeNeighbour(String nodeId, String nodeInterface, String neighbourId,
			String neighbourInterface, String discoveredBy, String availability, String bearerId,
			String connectionAttributes, String uri);

	/**
	 * 
	 * @return
	 */
	public NodeNeighbour[] getAllNeighbours();

	/**
	 * Returns the unique neighbours based on neighbourid, A neighbour may be a neighbour on multiple interfaces only
	 * one neighbour is returned. Which interface is returned is undefined
	 * 
	 * @param nodeId
	 * @return
	 */
	public NodeNeighbour[] getUniqueNeighboursByNeighbourId(String nodeId);

	/**
	 * 
	 * @param queryPredicates
	 * @return
	 * @throws RegistryQueryException
	 */
	public NodeNeighbour[] getNeighbours(String queryPredicates) throws RegistryQueryException;

	/**
	 * Returns the neighbours entries between <code>nodeId</code> and <code>neighbourId</code>. Could be multiple due to
	 * multiple interfaces on <code>nodeId</code> and/or <code>neighbourId</code>,
	 * 
	 * @param nodeId
	 * @param neighbourId
	 * @return
	 */
	public NodeNeighbour[] getAvailableNeighboursEntries(String nodeId, String neighbourId);

	/**
	 * Resets all Statically Discovered Neighbours to available (DiscoveredBy = 'STATIC').
	 * 
	 */
	public boolean markStaticNeighboursAsAvailable(String localNode);

	/**
	 * Marks any neighbour table entries corresponding to the neighbour described in the nodeDescriptor as UNAVAILABLE
	 * 
	 * @param nodeDescriptor
	 */
	public boolean markUnavailable(String localNode, NodeDescriptor nodeDescriptor);
}
