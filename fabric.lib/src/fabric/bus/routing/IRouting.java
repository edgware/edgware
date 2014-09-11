/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2007, 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.routing;

import fabric.bus.messages.IEmbeddedXML;
import fabric.bus.messages.IFabricMessage;

/**
 * Base interface for all Fabric message routing components.
 */
public interface IRouting extends IEmbeddedXML {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2009";

	/*
	 * Interface methods
	 */

	/**
	 * Gets the value of the specified property from the route.
	 * 
	 * @param key
	 *            the name of the property.
	 * 
	 * @return the property value.
	 */
	public String getProperty(String key);

	/**
	 * Sets the value of the specified property in the route.
	 * 
	 * @param key
	 *            the name of the property.
	 * 
	 * @param value
	 *            the new value from the property.
	 */
	public void setProperty(String key, String value);

	/**
	 * Answers the ID of the current node in the route.
	 * 
	 * @return the ID of the current node, or <code>null</code> if the end of the route has been reached.
	 * 
	 * @throws UnsupportedOperationException
	 *             thrown if this functionality is not implemented.
	 */
	public String currentNode() throws UnsupportedOperationException;

	/**
	 * Answers the start node in the route.
	 * 
	 * @return the node ID.
	 * 
	 * @throws UnsupportedOperationException
	 *             thrown if this functionality is not implemented.
	 */
	public String startNode();

	/**
	 * Answers the ID of the previous node in the route, i.e. the last node through which this message passed.
	 * 
	 * @return the ID of the node, or <code>null</code> if the end of the route has been reached.
	 * 
	 * @throws UnsupportedOperationException
	 *             thrown if this functionality is not implemented.
	 */
	public String previousNode() throws UnsupportedOperationException;

	/**
	 * Answers the next node(s) to which the containing message should be sent.
	 * 
	 * @return the list of Fabric node names.
	 */
	public String[] nextNodes();

	/**
	 * Answers the end node in the route.
	 * 
	 * @return the node ID.
	 * 
	 * @throws UnsupportedOperationException
	 *             thrown if this functionality is not implemented.
	 */
	public String endNode() throws UnsupportedOperationException;

	/**
	 * Answers a routing object representing the return route.
	 * 
	 * @return the return route.
	 */
	public IRouting returnRoute();

	/**
	 * Answers whether this message is a duplicate of a previous message.
	 * 
	 * @return whether this is a duplicate message.
	 */
	public boolean isDuplicate(IFabricMessage message);

}