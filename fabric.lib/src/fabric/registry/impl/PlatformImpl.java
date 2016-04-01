/*
 * (C) Copyright IBM Corp. 2009
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import java.util.Map;

import fabric.registry.Platform;
import fabric.registry.QueryScope;
import fabric.registry.System;
import fabric.registry.exception.IncompleteObjectException;

/**
 * Implementation class for a Fabric <code>Platform</code>.
 */
public class PlatformImpl extends AbstractRegistryObject implements Platform {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

    private String id = null;

    private String typeId = null;

    private String nodeId = null;

    private String affiliation = null;

    private String credentials = null;

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

    protected PlatformImpl() {

    }

    protected PlatformImpl(String id, String typeId, String nodeId, String affiliation, String credentials,
            String readiness, String availability, double latitude, double longitude, double altitude, double bearing,
            double velocity, String description, String attributes, String attributesURI) {

        this.id = id;
        this.typeId = typeId;
        this.nodeId = nodeId;
        this.affiliation = affiliation;
        this.credentials = credentials;
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
    public Map<String, String> getAttributesMap() {

        return AbstractFactory.buildAttributesMap(attributes);
    }

    @Override
    public void setAttributesMap(Map<String, String> attributes) {

        this.attributes = AbstractFactory.convertMapToNVPString(attributes);
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
    public String getNodeId() {

        return nodeId;
    }

    @Override
    public void setNodeId(String nodeId) {

        this.nodeId = nodeId;
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
    public String getCredentials() {

        return credentials;
    }

    @Override
    public void setCredentials(String credentials) {

        this.credentials = credentials;
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

        if (id == null || id.length() == 0 || typeId == null || typeId.length() == 0 || nodeId == null
                || nodeId.length() == 0) {
            throw new IncompleteObjectException("Id, type id or node id not specified.");
        }
    }

    @Override
    public String toString() {

        StringBuffer buffy = new StringBuffer("Platform::");
        buffy.append(" Platform ID: ").append(id);
        buffy.append(", Type ID: ").append(typeId);
        buffy.append(", Node ID: ").append(nodeId);
        buffy.append(", Affiliation: ").append(affiliation);
        buffy.append(", Credentials: ").append(credentials);
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
        if (obj instanceof PlatformImpl) {
            PlatformImpl p = (PlatformImpl) obj;
            if (p.getId().equals(id) && p.getTypeId().equals(typeId) && p.getNodeId() == null ? nodeId == null : p
                    .getNodeId().equals(nodeId)
                    && p.getAffiliation() == null ? affiliation == null : p.getAffiliation().equals(affiliation)
                    && p.getCredentials() == null ? credentials == null : p.getCredentials().equals(credentials)
                    && p.getReadiness() == null ? readiness == null
                    : p.getReadiness().equals(readiness) && p.getAvailability() == null ? availability == null : p
                            .getAvailability().equals(availability)
                            && p.getLatitude() == latitude
                            && p.getLongitude() == longitude
                            && p.getAltitude() == altitude
                            && p.getBearing() == bearing
                            && p.getVelocity() == velocity
                            && p.getDescription() == null ? description == null : p.getDescription()
                            .equals(description)
                            && p.getAttributes() == null ? attributes == null : p.getAttributes().equals(attributes)
                            && p.getAttributesURI() == null ? attributesUri == null : p.getAttributesURI().equals(
                            attributesUri)) {

                equal = true;
            }
        }
        return equal;
    }

    @Override
    public System[] getServices() {

        return SystemFactoryImpl.getInstance(QueryScope.LOCAL).getSystemsByPlatform(id);
    }

    @Override
    public String key() {

        return this.getId();
    }
}
