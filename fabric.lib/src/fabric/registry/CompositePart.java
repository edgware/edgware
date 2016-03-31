/*
 * (C) Copyright IBM Corp. 2010
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import java.util.Map;

/**
 * Represents an individual service that forms a part of a specific composite service within the Fabric.
 */
public interface CompositePart extends RegistryObject {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010";

	/**
	 * Returns the id of the associated composite service.
	 * 
	 * @return
	 */
	public String getCompositeId();

	/**
	 * Sets the id of the associated composite service.
	 * 
	 * @param id
	 */
	public void setCompositeId(String id);

	/**
	 * Get the id of the service platform.
	 * 
	 * @return
	 */
	public String getServicePlatformId();

	/**
	 * Set the id of the service platform.
	 * 
	 * @param type
	 */
	public void setServicePlatformId(String id);

	/**
	 * Get the id of the service.
	 * 
	 * @return
	 */
	public String getServiceId();

	/**
	 * Set the id of the service.
	 * 
	 * @param serviceId
	 */
	public void setServiceId(String serviceId);

	/**
	 * Get a list of attributes for the service, expressed as a String.
	 * 
	 * @return
	 */
	public String getAttributes();

	/**
	 * Set the attributes for the service, expressing them as a String.
	 * 
	 * Note: if the String is formatted as name=value pairs, separated by an &amp;, the method getAttributesMap() can be
	 * used to retrieve the attributes subsequently.
	 * 
	 * @param attributes
	 */
	public void setAttributes(String attributes);

	/**
	 * Get a list of attributes for the service, expressed as a java.util.Map.
	 * 
	 * @return
	 */
	public Map<String, String> getAttributesMap();

	/**
	 * Set the attributes for the service, expressing them as java.util.Map.
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
