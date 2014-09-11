/*
 * Licensed Materials - Property of IBM
 * 
 * (C) Copyright IBM Corp. 2006, 2009
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.FabricPlugin;
import fabric.registry.Node;
import fabric.registry.NodeIpMapping;
import fabric.registry.NodeNeighbour;
import fabric.registry.NodePlugin;
import fabric.registry.Platform;
import fabric.registry.exception.IncompleteObjectException;

/**
 * Implementation class for a Fabric <code>Node</code>.
 */
public class NodeImpl extends AbstractRegistryObject implements Node {

	/** Copyright notice. */
	public static final String copyrightNotice = "(C) Copyright IBM Corp. 2006, 2009";

	private String id = null;

	private String typeId = null;

	private String affiliation = null;

	private String securityClassification = null;

	private String readiness = null;

	private String availability = null;

	private double latitude = 0.0;

	private double longitude = 0.0;

	private double altitude = 0.0;

	private double bearing = 0.0;

	private double velocity = 0.0;

	private String description = null;

	private String attributes = null;

	private String attributesUri = null;

	protected NodeImpl() {

	}

	protected NodeImpl(String id, String typeId, String affiliation, String securityClassification, String readiness,
			String availability, double latitude, double longitude, double altitude, double bearing, double velocity,
			String description, String attributes, String attributesURI) {

		this.id = id;
		this.typeId = typeId;
		this.affiliation = affiliation;
		this.securityClassification = securityClassification;
		this.readiness = readiness;
		this.availability = availability;
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
		this.bearing = bearing;
		this.velocity = velocity;
		this.description = description;
		this.attributes = attributes;
		this.attributesUri = attributesURI;
	}

	@Override
	public String getAffiliation() {

		return affiliation;
	}

	@Override
	public void setAffiliation(String affiliation) {

		this.affiliation = affiliation;
	}

	@Override
	public double getAltitude() {

		return altitude;
	}

	@Override
	public void setAltitude(double altitude) {

		this.altitude = altitude;
	}

	@Override
	public String getAttributes() {

		return attributes;
	}

	@Override
	public void setAttributes(String attributes) {

		this.attributes = attributes;
	}

	@Override
	public String getAttributesURI() {

		return attributesUri;
	}

	@Override
	public void setAttributesURI(String attributesUri) {

		this.attributesUri = attributesUri;
	}

	@Override
	public String getAvailability() {

		return availability;
	}

	@Override
	public void setAvailability(String availability) {

		this.availability = availability;
	}

	@Override
	public double getBearing() {

		return bearing;
	}

	@Override
	public void setBearing(double bearing) {

		this.bearing = bearing;
	}

	@Override
	public String getDescription() {

		return description;
	}

	@Override
	public void setDescription(String description) {

		this.description = description;
	}

	@Override
	public String getId() {

		return id;
	}

	@Override
	public void setId(String id) {

		this.id = id;
	}

	@Override
	public double getLatitude() {

		return latitude;
	}

	@Override
	public void setLatitude(double latitude) {

		this.latitude = latitude;
	}

	@Override
	public double getLongitude() {

		return longitude;
	}

	@Override
	public void setLongitude(double longitude) {

		this.longitude = longitude;
	}

	@Override
	public String getReadiness() {

		return readiness;
	}

	@Override
	public void setReadiness(String readiness) {

		this.readiness = readiness;
	}

	@Override
	public String getSecurityClassification() {

		return securityClassification;
	}

	@Override
	public void setSecurityClassification(String securityClassification) {

		this.securityClassification = securityClassification;
	}

	@Override
	public String getTypeId() {

		return typeId;
	}

	@Override
	public void setTypeId(String typeId) {

		this.typeId = typeId;
	}

	@Override
	public double getVelocity() {

		return velocity;
	}

	@Override
	public void setVelocity(double velocity) {

		this.velocity = velocity;
	}

	@Override
	public void validate() throws IncompleteObjectException {

		if (id == null || id.length() == 0 || typeId == null || typeId.length() == 0) {
			throw new IncompleteObjectException("Id or type id not specified.");
		}
	}

	@Override
	public String toString() {

		StringBuffer buffy = new StringBuffer("Node::");
		buffy.append(" Node ID: ").append(id);
		buffy.append(", Type ID: ").append(typeId);
		buffy.append(", Affiliation: ").append(affiliation);
		buffy.append(", Security Classification: ").append(securityClassification);
		buffy.append(", Readiness: ").append(readiness);
		buffy.append(", Availability: ").append(availability);
		buffy.append(", Latitude: ").append(latitude);
		buffy.append(", Longitude: ").append(longitude);
		buffy.append(", Altitude: ").append(altitude);
		buffy.append(", Bearing: ").append(bearing);
		buffy.append(", Velocity: ").append(velocity);
		buffy.append(", Description: ").append(description);
		buffy.append(", Attributes: ").append(attributes);
		buffy.append(", AttributesURI: ").append(attributesUri);
		return buffy.toString();
	}

	@Override
	public boolean equals(Object obj) {

		boolean equal = false;
		if (obj instanceof NodeImpl) {
			NodeImpl n = (NodeImpl) obj;
			if (n.getId().equals(id) && n.getTypeId().equals(typeId) && n.getAffiliation() == null ? affiliation == null
					: n.getAffiliation().equals(affiliation) && n.getSecurityClassification() == null ? securityClassification == null
							: n.getSecurityClassification().equals(securityClassification) && n.getReadiness() == null ? readiness == null
									: n.getReadiness().equals(readiness) && n.getAvailability() == null ? availability == null
											: n.getAvailability().equals(availability) && n.getLatitude() == latitude
													&& n.getLongitude() == longitude && n.getAltitude() == altitude
													&& n.getBearing() == bearing && n.getVelocity() == velocity
													&& n.getDescription() == null ? description == null : n
													.getDescription().equals(description)
													&& n.getAttributes() == null ? attributes == null : n
													.getAttributes().equals(attributes)
													&& n.getAttributesURI() == null ? attributesUri == null : n
													.getAttributesURI().equals(attributesUri)) {

				equal = true;
			}
		}
		return equal;
	}

	@Override
	public Platform[] getPlatforms() {

		return PlatformFactoryImpl.getInstance(false).getPlatformsByNode(id);
	}

	@Override
	public NodeNeighbour[] getUniqueNeighbours() {

		return NodeNeighbourFactoryImpl.getInstance(false).getUniqueNeighboursByNeighbourId(id);
	}

	@Override
	public NodeIpMapping[] getAllIpMappings() {

		return NodeIpMappingFactoryImpl.getInstance(false).getAllMappingsForNode(id);
	}

	@Override
	public FabricPlugin[] getFabricPlugins(boolean localOnly) {

		return FabricPluginFactoryImpl.getInstance(localOnly).getFabricPluginsByNode(id);
	}

	@Override
	public NodePlugin[] getNodePlugins(boolean localOnly) {

		return NodePluginFactoryImpl.getInstance(localOnly).getNodePluginsByNode(id);
	}

	@Override
	public String key() {

		return new StringBuffer(this.getId()).append("/").append(this.getTypeId()).toString();
	}

}
