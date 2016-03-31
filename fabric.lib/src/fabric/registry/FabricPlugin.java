/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

/**
 * Represents information about a FabricPlugin.
 */
public interface FabricPlugin extends RegistryObject {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/**
	 * Get the node with which this plugin is associated.
	 * 
	 * @return the node id.
	 */
	public String getNodeId();

	/**
	 * Set the node with which this plugin is associated.
	 * 
	 * @param nodeId
	 *            - the id of the node
	 */
	public void setNodeId(String nodeId);

	/**
	 * Get the fully-qualified class name of the plugin implementation.
	 * 
	 * @return the class name.
	 */
	public String getName();

	/**
	 * Set the fully-qualified class name of the plugin implementation.
	 * 
	 * @param name
	 *            - the class name for the plugin.
	 */
	public void setName(String name);

	/**
	 * Get the description of the plugin.
	 * 
	 * @return the description.
	 */
	public String getDescription();

	/**
	 * Set the description of the plugin.
	 * 
	 * @param description
	 */
	public void setDescription(String description);

	/**
	 * Get the custom arguments for this plugin.
	 * 
	 * @return the string containing the arguments.
	 */
	public String getArguments();

	/**
	 * Set the custom arguments for this plugin
	 * 
	 * @param arguments
	 *            - the string containing the argument.
	 */
	public void setArguments(String arguments);

	/**
	 * Get the family that this plugin is part of.
	 * 
	 * @return the name of the plugin family.
	 */
	public String getFamilyName();

	/**
	 * Set the family that this plugin is part of.
	 * 
	 * @param familyName
	 *            - the name of the plugin family.
	 */
	public void setFamily(String familyName);
}
