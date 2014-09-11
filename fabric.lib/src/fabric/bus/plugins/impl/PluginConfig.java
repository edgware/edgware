/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2007, 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.plugins.impl;

import fabric.MetricsManager;
import fabric.bus.plugins.IFamily;
import fabric.bus.plugins.IPluginConfig;

/**
 * Class representing configuration information for a Fabric Manager plug-in.
 */
public class PluginConfig implements IPluginConfig {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2009";

	/*
	 * Class fields
	 */

	/** The name of the plug-in class. */
	protected String name = null;

	/** The plug-in's initialization data. */
	protected String arguments = null;

	/** The name of the plug-in's family */
	protected String familyName = null;

	/** The plug-in family's family management object. */
	protected IFamily family = null;

	/** The name of the processing node. */
	protected String node = null;

	/** The plug-in's description. */
	protected String description = null;

	/** List to record the metrics recorded by this instance of the plug-in. */
	protected MetricsManager metrics = null;

	/*
	 * Class methods
	 */

	/**
	 * Constructs a new instance.
	 */
	public PluginConfig() {
	}

	/**
	 * Constructs a new instance.
	 * 
	 * @param name
	 *            the name of the plug-in class.
	 * 
	 * @param arguments
	 *            the plug-in's initialization data.
	 * 
	 * @param familyName
	 *            the plug-in's family.
	 * 
	 * @param family
	 *            the plug-in family's family management object.
	 * 
	 * @param node
	 *            the name of the processing node.
	 * 
	 * @param description
	 *            the plug-in's description.
	 * 
	 * @param metrics
	 *            list to hold the metrics recorded by this instance of the plug-in.
	 */
	public PluginConfig(String name, String arguments, String familyName, IFamily family, String node,
			String description, MetricsManager metrics) {

		init(name, arguments, familyName, family, node, description, metrics);

	}

	/**
	 * Constructs a new instance from an existing instance.
	 * 
	 * @param sourceConfig
	 *            an existing plug-in configuration instance.
	 */
	public PluginConfig(PluginConfig sourceConfig) {

		init(sourceConfig.name, sourceConfig.arguments, sourceConfig.familyName, sourceConfig.family,
				sourceConfig.node, sourceConfig.description, sourceConfig.metrics);

	}

	/**
	 * Initializes a new instance.
	 * 
	 * @param name
	 *            the name of the plug-in class.
	 * 
	 * @param arguments
	 *            the plug-in's initialization data.
	 * 
	 * @param familyName
	 *            the plug-in's family.
	 * 
	 * @param family
	 *            the plug-in family's family management object.
	 * 
	 * @param node
	 *            the name of the processing node.
	 * 
	 * @param description
	 *            the plug-in's description.
	 * 
	 * @param metrics
	 *            list to hold the metrics recorded by this instance of the plug-in.
	 */
	private void init(String name, String arguments, String familyName, IFamily shardData, String node,
			String description, MetricsManager metrics) {

		this.name = name;
		this.arguments = arguments;
		this.familyName = familyName;
		this.family = shardData;
		this.node = node;
		this.description = description;
		this.metrics = metrics;

	}

	/**
	 * @see fabric.bus.plugins.IPluginConfig#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @see fabric.bus.plugins.IPluginConfig#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @see fabric.bus.plugins.IPluginConfig#getArguments()
	 */
	@Override
	public String getArguments() {
		return arguments;
	}

	/**
	 * @see fabric.bus.plugins.IPluginConfig#setArguments(java.lang.String)
	 */
	@Override
	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	/**
	 * @see fabric.bus.plugins.IPluginConfig#getFamilyName()
	 */
	@Override
	public String getFamilyName() {
		return familyName;
	}

	/**
	 * @see fabric.bus.plugins.IPluginConfig#setFamilyName(java.lang.String)
	 */
	@Override
	public void setFamilyName(String family) {
		this.familyName = family;
	}

	/**
	 * @see fabric.bus.plugins.IPluginConfig#getFamily()
	 */
	@Override
	public IFamily getFamily() {
		return family;
	}

	/**
	 * @see fabric.bus.plugins.IPluginConfig#setFamily(fabric.bus.plugins.IFamily)
	 */
	@Override
	public void setFamily(IFamily family) {
		this.family = family;
	}

	/**
	 * @see fabric.bus.plugins.IPluginConfig#getNode()
	 */
	@Override
	public String getNode() {
		return node;
	}

	/**
	 * @see fabric.bus.plugins.IPluginConfig#setNode(java.lang.String)
	 */
	@Override
	public void setNode(String node) {
		this.node = node;
	}

	/**
	 * @see fabric.bus.plugins.IPluginConfig#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @see fabric.bus.plugins.IPluginConfig#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @see fabric.bus.plugins.IPluginConfig#getMetricManager()
	 */
	@Override
	public MetricsManager getMetricManager() {
		return metrics;
	}

	/**
	 * @see fabric.bus.plugins.IPluginConfig#setMetricManager(fabric.MetricsManager)
	 */
	@Override
	public void setMetricManager(MetricsManager metrics) {
		this.metrics = metrics;
	}
}
