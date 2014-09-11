/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2011, 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import java.util.Map;

/**
 * Represents a system in the Fabric Registry.
 * 
 * A system is associated with a particular platform and has one or more services.
 */
public interface System extends RegistryObject {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2011, 2014";

	/** System 'kind' constants */
	public static final String SENSOR_KIND = "SENSOR";
	public static final String SERVICE_KIND = "SERVICE";

	/**
	 * The name of the service.
	 * 
	 * @return
	 */
	public String getId();

	/**
	 * Set the name of the service.
	 * 
	 * @param id
	 */
	public void setId(String id);

	/**
	 * Get the type of the service.
	 * 
	 * @return the service type.
	 */
	public String getTypeId();

	/**
	 * Set the type of the service.
	 * 
	 * @param typeId
	 */
	public void setTypeId(String typeId);

	/**
	 * Get the platform this service is running on.
	 * 
	 * @return the name of the platform.
	 */
	public String getPlatformId();

	/**
	 * Set the platform this service is running on.
	 * 
	 * @param platformId
	 *            - the name of the platform.
	 */
	public void setPlatformId(String platformId);

	/**
	 * Get the credentials for this service.
	 * 
	 * @return the security credentials.
	 */
	public String getCredentials();

	/**
	 * Set the credentials for this service.
	 * 
	 * @param credentials
	 *            - the security credentials.
	 */
	public void setCredentials(String credentials);

	/**
	 * Get the readiness state of the service.
	 * 
	 * @return the readiness state such as "DEPLOYED".
	 */
	public String getReadiness();

	/**
	 * Set the readiness state.
	 * 
	 * @param readiness
	 *            - the state of readiness, e.g. "DEPLOYED".
	 */
	public void setReadiness(String readiness);

	/**
	 * Get the availability status of the service.
	 * 
	 * @return the status, e.g. "AVAILABLE" or "ACTIVE".
	 */
	public String getAvailability();

	/**
	 * Set the availability status of the service.
	 * 
	 * @param availability
	 *            - the status, e.g. "AVAILABLE" or "ACTIVE".
	 */
	public void setAvailability(String availability);

	/**
	 * Get latitude coordinate for the service.
	 * 
	 * @return the latitude coordinate.
	 */
	public double getLatitude();

	/**
	 * Set the latitude coordinate for the service.
	 * 
	 * @param latitude
	 *            - the latitudinal position.
	 */
	public void setLatitude(double latitude);

	/**
	 * Get longitude coordinate for the service.
	 * 
	 * @return the longitude coordinate.
	 */
	public double getLongitude();

	/**
	 * Set the longitude coordinate for the service.
	 * 
	 * @param longitude
	 *            - the longitudinal position.
	 */
	public void setLongitude(double longitude);

	/**
	 * Get the altitude of the service.
	 * 
	 * @return the altitude.
	 */
	public double getAltitude();

	/**
	 * Set the altitude of the service.
	 * 
	 * @param altitude
	 *            - the altitudinal position.
	 */
	public void setAltitude(double altitude);

	/**
	 * Get the bearing of the service.
	 * 
	 * @return the bearing in degrees.
	 */
	public double getBearing();

	/**
	 * Set the bearing of the service.
	 * 
	 * @param bearing
	 *            - specified in degrees.
	 */
	public void setBearing(double bearing);

	/**
	 * Get the velocity of the service.
	 * 
	 * @return the velocity.
	 */
	public double getVelocity();

	/**
	 * Set the velocity of the service.
	 * 
	 * @param velocity
	 *            - the velocity.
	 */
	public void setVelocity(double velocity);

	/**
	 * Get the description of the service.
	 * 
	 * @return the description.
	 */
	public String getDescription();

	/**
	 * Set the description of the service.
	 * 
	 * @param description
	 *            - the description.
	 */
	public void setDescription(String description);

	/**
	 * Get the custom attributes for the service.
	 * 
	 * @return the attributes for the service.
	 */
	public String getAttributes();

	/**
	 * Set the custom attributes for the service.
	 * 
	 * @param attributes
	 *            - string containing the attributes.
	 */
	public void setAttributes(String attributes);

	/**
	 * Get the attributes URI for the service.
	 * 
	 * @return the URI reference for the attributes.
	 */
	public String getAttributesURI();

	/**
	 * Set the attributes URI for the service.
	 * 
	 * @param attributesURI
	 *            - the URI reference fr the attributes.
	 */
	public void setAttributesURI(String attributesURI);

	/**
	 * Get the list of services associated with this system.
	 * 
	 * @return the list of services.
	 */
	public Service[] getServices();

	/**
	 * Get the string identifying the kind of service (a physical 'SENSOR' or software 'SERVICE').
	 * 
	 * @return the 'kind' string
	 */
	public String getKind();

	/**
	 * Set the kind of service (either a physical 'SENSOR' or software 'SERVICE').
	 * 
	 * @param kind
	 */
	public void setKind(String kind);

	/**
	 * Get the attributes for the service, expressed as a Map.
	 * 
	 * Note: this is only appropriate if the attributes were stored as a delimited string in the following format or by
	 * using setAttributesMap:
	 * 
	 * name1=value1&name2=value2...
	 * 
	 * @return
	 */
	public Map<String, String> getAttributesMap();

	/**
	 * Set the attributes for this system, expressed as a Map of string values.
	 * 
	 * Note: attributes will be persisted as a delimited string of the form:
	 * 
	 * name1=value1&name2=value2...
	 * 
	 * @param attributes
	 */
	public void setAttributesMap(Map<String, String> attributes);

}
