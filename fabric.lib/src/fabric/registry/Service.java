/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2011, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

/**
 * Represents information about a service stored in the Fabric Registry.
 */
public interface Service extends RegistryObject {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2011, 2014";

	/** Interface direction constants */
	public static final String MODE_INPUT_FEED = "input";
	public static final String MODE_OUTPUT_FEED = "output";
	public static final String MODE_SOLICIT_RESPONSE = "solicit_response";
	public static final String MODE_REQUEST_RESPONSE = "request_response";
	public static final String MODE_NOTIFICATION = "notification";
	public static final String MODE_LISTENER = "one_way";

	/**
	 * Get the ID of this service. IDs are used to identify a particular service on a system.
	 * 
	 * @return the ID of the service.
	 */
	public String getId();

	/**
	 * Set the ID of this service. IDs are used to identify a particular service on a system.
	 * 
	 * @param id
	 *            the new ID.
	 */
	public void setId(String id);

	/**
	 * Get the platform to which the service is attached (via the system).
	 * 
	 * @return the ID of the platform.
	 */
	public String getPlatformId();

	/**
	 * Set the platform to which the service is attached (via the system).
	 * 
	 * @param platformId
	 *            the id of the platform.
	 */
	public void setPlatformId(String platformId);

	/**
	 * Get the name of the system with which this service is associated.
	 * 
	 * @return the name of the service.
	 */
	public String getSystemId();

	/**
	 * Set the name of the system with which this feed is associated.
	 * 
	 * @param id
	 *            the name of the service.
	 */
	public void setSystemId(String id);

	/**
	 * Get the type classifier for this service.
	 * 
	 * @return the service type.
	 */
	public String getTypeId();

	/**
	 * Set the type classifier for this service.
	 * 
	 * @param id
	 *            the service type.
	 */
	public void setTypeId(String id);

	/**
	 * Get the security credentials for this service.
	 * 
	 * @return the security credentials or <code>null</code> if none has been specified.
	 */
	public String getCredentials();

	/**
	 * Set the security credentials of this service.
	 * 
	 * @param credentials
	 *            the security credentials.
	 */
	public void setCredentials(String credentials);

	/**
	 * Get the availability status of the service.
	 * 
	 * @return the availability status string or <code>null</code> if none has been specified.
	 */
	public String getAvailability();

	/**
	 * Set the availability status of the service.
	 * 
	 * @param availability
	 *            the availability status string.
	 */
	public void setAvailability(String availability);

	/**
	 * Get the description of this service.
	 * 
	 * @return the description or <code>null</code> if none has been specified.
	 */
	public String getDescription();

	/**
	 * Set the description of this service.
	 * 
	 * @param desc
	 *            the description.
	 */
	public void setDescription(String desc);

	/**
	 * Get the custom attributes for this service.
	 * 
	 * @return the custom attributes string or <code>null</code> if none has been specified.
	 */
	public String getAttributes();

	/**
	 * Set the custom attributes for this service.
	 * 
	 * @param attrs
	 *            the custom attributes string.
	 */
	public void setAttributes(String attrs);

	/**
	 * Get the custom attributes URI.
	 * 
	 * @return the custom attributes URI or <code>null</code> if none has been specified.
	 */
	public String getAttributesURI();

	/**
	 * Set the custom attributes URI.
	 * 
	 * @param uri
	 *            the URI of the attributes.
	 */
	public void setAttributesURI(String uri);

	/**
	 * Get the direction of the interface (<code>INPUT</code> or <code>OUTPUT</code>).
	 * 
	 * @return the direction string.
	 */
	public String getMode();

	/**
	 * Set the direction of the interface (<code>INPUT</code> or <code>OUTPUT</code>).
	 * 
	 * @param direction
	 */
	public void setMode(String direction);
}