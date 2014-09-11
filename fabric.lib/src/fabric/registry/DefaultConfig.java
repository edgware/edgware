/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

/**
 * Represents a default configuration value in the Registry.
 */
public interface DefaultConfig extends RegistryObject {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

	/**
	 * Get the name of the configuration value.
	 * 
	 * @return the name.
	 */
	public String getName();

	/**
	 * Set the name of the configuration value.
	 * 
	 * @param name
	 *            the new name.
	 */
	public void setName(String name);

	/**
	 * Get the configuration value.
	 * 
	 * @return the value.
	 */
	public String getValue();

	/**
	 * Set the configuration value.
	 * 
	 * @param value
	 *            the new value.
	 */
	public void setValue(String value);
}
