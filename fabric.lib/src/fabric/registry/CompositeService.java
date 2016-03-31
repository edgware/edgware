/*
 * (C) Copyright IBM Corp. 2010
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import java.util.Map;

/**
 * Represents a composite service within the Fabric.
 */
public interface CompositeService extends RegistryObject {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010";

	/**
	 * Returns the id of the composite service.
	 * 
	 * @return
	 */
	public String getId();

	/**
	 * Sets the id of the composite service.
	 * 
	 * @param id
	 */
	public void setId(String id);

	/**
	 * Get the type of the composite service.
	 * 
	 * @return
	 */
	public String getType();

	/**
	 * Set the type of the composite service.
	 * 
	 * @param type
	 */
	public void setType(String type);

	/**
	 * Get the affiliation of the composite service.
	 * 
	 * @return
	 */
	public String getAffiliation();

	/**
	 * Set the affiliation of the composite service.
	 * 
	 * @param affiliation
	 */
	public void setAffiliation(String affiliation);

	/**
	 * Get the credentials attributed to the composite service.
	 * 
	 * @return
	 */
	public String getCredentials();

	/**
	 * Set the credentials associated with the composite service.
	 * 
	 * @param credentials
	 */
	public void setCredentials(String credentials);

	/**
	 * Returns a brief description of the composite service.
	 * 
	 * @return
	 */
	public String getDescription();

	/**
	 * Set a description for the composite service.
	 * 
	 * @param description
	 */
	public void setDescription(String description);

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
