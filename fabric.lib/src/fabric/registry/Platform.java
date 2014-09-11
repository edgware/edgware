/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry;

import java.util.Map;

/**
 * Represents a Platform stored in the Fabric Registry. A Platform is attached to a Node and can have Systems attached
 * to it.
 * 
 */
public interface Platform extends RegistryObject {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

	/**
	 * 
	 * @return
	 */
	public String getAffiliation();

	/**
	 * 
	 * @param affiliation
	 */
	public void setAffiliation(String affiliation);

	/**
	 * 
	 * @return
	 */
	public double getAltitude();

	/**
	 * 
	 * @param altitude
	 */
	public void setAltitude(double altitude);

	/**
	 * 
	 * @return
	 */
	public String getAttributes();

	/**
	 * 
	 * Note: if the String is formatted as name=value pairs, separated by an &amp;, the method getAttributesMap() can be
	 * used to retrieve the attributes subsequently.
	 * 
	 * @param attributes
	 */
	public void setAttributes(String attributes);

	/**
	 * 
	 * @return
	 */
	public String getAttributesURI();

	/**
	 * 
	 * @param attributesURI
	 */
	public void setAttributesURI(String attributesURI);

	/**
	 * 
	 * @return
	 */
	public String getAvailability();

	/**
	 * 
	 * @param availability
	 */
	public void setAvailability(String availability);

	/**
	 * 
	 * @return
	 */
	public String getDescription();

	/**
	 * 
	 * @param description
	 */
	public void setDescription(String description);

	/**
	 * 
	 * @return
	 */
	public double getLatitude();

	/**
	 * 
	 * @param latitude
	 */
	public void setLatitude(double latitude);

	/**
	 * 
	 * @return
	 */
	public double getLongitude();

	/**
	 * 
	 * @param longitude
	 */
	public void setLongitude(double longitude);

	/**
	 * 
	 * @return
	 */
	public String getNodeId();

	/**
	 * 
	 * @param nodeID
	 */
	public void setNodeId(String nodeID);

	/**
	 * 
	 * @return
	 */
	public String getId();

	/**
	 * 
	 * @param platformID
	 */
	public void setId(String platformID);

	/**
	 * 
	 * @return
	 */
	public String getReadiness();

	/**
	 * 
	 * @param readiness
	 */
	public void setReadiness(String readiness);

	/**
	 * 
	 * @return
	 */
	public String getTypeId();

	/**
	 * 
	 * @param typeID
	 */
	public void setTypeId(String typeID);

	/**
	 * 
	 * @return
	 */
	public String getCredentials();

	/**
	 * 
	 * @param credentials
	 */
	public void setCredentials(String credentials);

	/**
	 * 
	 * @return
	 */
	public double getBearing();

	/**
	 * 
	 * @param bearing
	 */
	public void setBearing(double bearing);

	/**
	 * 
	 * @return
	 */
	public double getVelocity();

	/**
	 * 
	 * @param velocity
	 */
	public void setVelocity(double velocity);

	/**
	 * 
	 * @return
	 */
	public System[] getServices();

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

}
