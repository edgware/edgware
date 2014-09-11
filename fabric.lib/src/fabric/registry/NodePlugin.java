/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2008, 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

/**
 * Represents a node plugin stored in the Fabric Registry.
 */
public interface NodePlugin extends SystemPlugin {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2008, 2009";

	/**
	 * Get the runtime ordinal of this plugin.
	 * 
	 * @return the integer ordinal
	 */
	public int getOrdinal();

	/**
	 * Set the runtime ordinal of this plugin.
	 * 
	 * @param ordinal
	 */
	public void setOrdinal(int ordinal);

}
