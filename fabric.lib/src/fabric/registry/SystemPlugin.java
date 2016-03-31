/*
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

/**
 * Represents a SystemPlugin in the Fabric Registry.
 */
public interface SystemPlugin extends FabricPlugin {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/**
	 * 
	 * @return
	 */
	public String getPluginType();

	/**
	 * 
	 * @param pluginType
	 */
	public void setPluginType(String pluginType);

	/**
	 * 
	 * @return
	 */
	public boolean isInbound();

	/**
	 * 
	 * @param inbound
	 */
	public void setInbound(boolean inbound);

	/**
	 * 
	 * @return
	 */
	public boolean isOutbound();

	/**
	 * 
	 * @param outbound
	 */
	public void setOutbound(boolean outbound);
}
