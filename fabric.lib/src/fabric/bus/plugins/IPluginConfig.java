/*
 * (C) Copyright IBM Corp. 2007, 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins;

import fabric.MetricsManager;

/**
 * Interface representing configuration information for a Fabric Manager plug-in.
 */
public interface IPluginConfig {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2009";

	/*
	 * Interface methods
	 */

	/**
	 * Answers the plug-in's name.
	 * 
	 * @return the name.
	 */
	public String getName();

	/**
	 * Sets the plug-in's name.
	 * 
	 * @param name
	 *            the name.
	 */
	public void setName(String name);

	/**
	 * Answers the plug-in's arguments.
	 * 
	 * @return the arguments.
	 */
	public String getArguments();

	/**
	 * Sets the plug-in's arguments.
	 * 
	 * @param arguments
	 *            the arguments.
	 */
	public void setArguments(String arguments);

	/**
	 * Answers the plug-in's family name.
	 * 
	 * @return the family name.
	 */
	public String getFamilyName();

	/**
	 * Sets the plug-in's family name.
	 * 
	 * @param familyName
	 *            the family name.
	 */
	public void setFamilyName(String familyName);

	/**
	 * Answers the plug-in family's family management object.
	 * 
	 * @return the family management object.
	 */
	public IFamily getFamily();

	/**
	 * Sets the plug-in family's family management object.
	 * 
	 * @param family
	 *            the family management object.
	 */
	public void setFamily(IFamily family);

	/**
	 * Answers the name of the processing node.
	 * 
	 * @return the node.
	 */
	public String getNode();

	/**
	 * Sets the name of the processing node.
	 * 
	 * @param node
	 *            the node.
	 */
	public void setNode(String node);

	/**
	 * Answers the plug-in's description.
	 * 
	 * @return the description.
	 */
	public String getDescription();

	/**
	 * Sets the plug-in's description.
	 * 
	 * @param description
	 *            the description.
	 */
	public void setDescription(String description);

	/**
	 * Answers the plug-in's metrics.
	 * 
	 * @return the metrics.
	 */
	public MetricsManager getMetricManager();

	/**
	 * Sets the plug-in's metrics.
	 * 
	 * @param metrics
	 *            the metrics.
	 */
	public void setMetricManager(MetricsManager metrics);

}