/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

/**
 * Represents a particular ip address and port mapping for a node.
 * 
 *
 */
public interface NodeIpMapping extends RegistryObject {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2014";
	
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
	public void setNodeInterface(String nodeInterface);

	/**
	 * 
	 * @return
	 */
	public String getNodeInterface();

	/**
	 * 
	 * @return
	 */
	public String getIpAddress();
	
	/**
	 * 
	 * @param ipAddress
	 */
	public void setIpAddress(String ipAddress);

	/**
	 * 
	 * @return
	 */
	public int getPort();
	
	/**
	 * 
	 * @param port
	 */
	public void setPort(int port);
	
}
