/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2010, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import java.util.Map;

/**
 * Represents a wire joining a system input to a system output.
 */
public interface SystemWiring extends RegistryObject {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010, 2014";

	/**
	 * Get the composite that this wiring is associated with.
	 * 
	 * @return the identifier of the composite system.
	 */
	public String getCompositeId();

	/**
	 * Set the composite system identifier.
	 * 
	 * @param compositeId
	 *            the composite id.
	 */
	public void setCompositeId(String compositeId);

	/**
	 * Get the id of the output system platform.
	 * 
	 * @return
	 */
	public String getFromSystemPlatformId();

	/**
	 * Set the id of the output system platform.
	 * 
	 * @param type
	 */
	public void setFromSystemPlatformId(String id);

	/**
	 * Get the id of the output system.
	 * 
	 * @return
	 */
	public String getFromSystemId();

	/**
	 * Set the id of the output system.
	 * 
	 * @param systemID
	 */
	public void setFromSystemId(String systemID);

	/**
	 * Get the id of the output interface.
	 * 
	 * @return
	 */
	public String getFromInterfaceId();

	/**
	 * Set the id of the output interface.
	 * 
	 * @param systemID
	 */
	public void setFromInterfaceId(String systemID);

	/**
	 * Get the id of the input system platform.
	 * 
	 * @return
	 */
	public String getToSystemPlatformId();

	/**
	 * Set the id of the input system platform.
	 * 
	 * @param type
	 */
	public void setToSystemPlatformId(String id);

	/**
	 * Get the id of the input system.
	 * 
	 * @return
	 */
	public String getToSystemId();

	/**
	 * Set the id of the input system.
	 * 
	 * @param systemID
	 */
	public void setToSystemId(String systemID);

	/**
	 * Get the id of the input interface.
	 * 
	 * @return
	 */
	public String getToInterfaceId();

	/**
	 * Set the id of the input interface.
	 * 
	 * @param systemID
	 */
	public void setToInterfaceId(String systemID);

	/**
	 * Get a list of attributes for the system, expressed as a String.
	 * 
	 * @return
	 */
	public String getAttributes();

	/**
	 * Set the attributes for the system, expressing them as a String.
	 * 
	 * Note: if the String is formatted as name=value pairs, separated by an &amp;, the method getAttributesMap() can be
	 * used to retrieve the attributes subsequently.
	 * 
	 * @param attributes
	 */
	public void setAttributes(String attributes);

	/**
	 * Get a list of attributes for the system, expressed as a java.util.Map.
	 * 
	 * @return
	 */
	public Map<String, String> getAttributesMap();

	/**
	 * Set the attributes for the system, expressing them as java.util.Map.
	 * 
	 * The attributes are stored in the database as a String containing name=value pairs separated by an &amp;
	 * 
	 * @param attributes
	 */
	public void setAttributesMap(Map<String, String> attributes);

	/**
	 * Get the attributes URI.
	 * 
	 * @return
	 */
	public String getAttributesURI();

	/**
	 * Set the attributes URI.
	 * 
	 * @param uri
	 */
	public void setAttributesURI(String uri);

}
