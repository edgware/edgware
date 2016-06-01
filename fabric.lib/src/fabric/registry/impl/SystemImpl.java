/*
 * (C) Copyright IBM Corp. 2011
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import java.util.Map;

import fabric.registry.FabricRegistry;
import fabric.registry.Service;
import fabric.registry.System;
import fabric.registry.exception.IncompleteObjectException;

/**
 * Implementation class for a <code>System</code>.
 */
public class SystemImpl extends AbstractRegistryObject implements System {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2011";

    /** The name of the service */
    private String id = null;

    /** The type of service */
    private String typeId = null;

    /** The name of the platform */
    private String platformId = null;

    /** Security credentials for the service */
    private String credentials = null;

    /** Readiness state */
    private String readiness = null;

    /** Availability status */
    private String availability = null;

    /** Latitudinal position */
    private double latitude = 0.0;

    /** Longitudinal position */
    private double longitude = 0.0;

    /** System altitude */
    private double altitude = 0.0;

    /** The service's bearing */
    private double bearing = 0.0;

    /** The service's velocity */
    private double velocity = 0.0;

    /** The service description */
    private String description = null;

    /** The custom attributes for the service */
    private String attributes = null;

    /** The URI reference to custom attributes */
    private String attributesURI = null;

    /** The kind of service, either a physical device or software service */
    private String kind = null;

    /*
     * Constructors
     */

    /** Default constructor */
    protected SystemImpl() {

    }

    /**
     * Full constructor which takes all the meta data for a service.
     * 
     * @param platformID
     *            - the name of the platform
     * @param serviceID
     *            - the name of the service
     * @param typeID
     *            - the service type
     * @param kind
     *            - the kind of service ('SERVICE' or 'SENSOR')
     * @param credentials
     *            - the security credentials
     * @param readiness
     *            - the state of readiness (e.g. 'DEPLOYED')
     * @param availability
     *            - the availability status (e.g. 'AVAILABLE')
     * @param latitude
     *            - latitudinal position
     * @param longitude
     *            - longitudinal position
     * @param altitude
     *            - the service altitude
     * @param bearing
     *            - the bearing of the service
     * @param velocity
     *            - the velocity of the service
     * @param description
     *            - the service description
     * @param attributes
     *            - custom attributes for the service
     * @param attributesURI
     *            - the URI for custom attributes
     */
    protected SystemImpl(String platformID, String serviceID, String typeID, String kind, String credentials,
            String readiness, String availability, double latitude, double longitude, double altitude, double bearing,
            double velocity, String description, String attributes, String attributesURI) {

        this.id = serviceID;
        this.typeId = typeID;
        this.kind = kind;
        this.platformId = platformID;
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
        this.attributesURI = attributesURI;
    }

    /*
     * Instance methods
     */

    /**
     * @see fabric.registry.System#getAltitude()
     */
    @Override
    public double getAltitude() {

        return altitude;
    }

    /**
     * @see fabric.registry.System#setAltitude(double)
     */
    @Override
    public void setAltitude(double altitude) {

        this.altitude = altitude;
    }

    /**
     * @see fabric.registry.System#getAttributes()
     */
    @Override
    public String getAttributes() {

        return attributes;
    }

    /**
     * @see fabric.registry.System#setAttributes(java.lang.String)
     */
    @Override
    public void setAttributes(String attributes) {

        this.attributes = attributes;
    }

    /**
     * @see fabric.registry.System#getAttributesURI()
     */
    @Override
    public String getAttributesURI() {

        return attributesURI;
    }

    /**
     * @see fabric.registry.System#setAttributesURI(java.lang.String)
     */
    @Override
    public void setAttributesURI(String attributesURI) {

        this.attributesURI = attributesURI;
    }

    /**
     * @see fabric.registry.System#getAvailability()
     */
    @Override
    public String getAvailability() {

        return availability;
    }

    /**
     * @see fabric.registry.System#setAvailability(java.lang.String)
     */
    @Override
    public void setAvailability(String availability) {

        this.availability = availability;
    }

    /**
     * @see fabric.registry.System#getBearing()
     */
    @Override
    public double getBearing() {

        return bearing;
    }

    /**
     * @see fabric.registry.System#setBearing(double)
     */
    @Override
    public void setBearing(double bearing) {

        this.bearing = bearing;
    }

    /**
     * @see fabric.registry.System#getDescription()
     */
    @Override
    public String getDescription() {

        return description;
    }

    /**
     * @see fabric.registry.System#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {

        this.description = description;
    }

    /**
     * @see fabric.registry.System#getId()
     */
    @Override
    public String getId() {

        return id;
    }

    /**
     * @see fabric.registry.System#setId(java.lang.String)
     */
    @Override
    public void setId(String id) {

        this.id = id;
    }

    /**
     * @see fabric.registry.System#getLatitude()
     */
    @Override
    public double getLatitude() {

        return latitude;
    }

    /**
     * @see fabric.registry.System#setLatitude(double)
     */
    @Override
    public void setLatitude(double latitude) {

        this.latitude = latitude;
    }

    /**
     * @see fabric.registry.System#getLongitude()
     */
    @Override
    public double getLongitude() {

        return longitude;
    }

    /**
     * @see fabric.registry.System#setLongitude(double)
     */
    @Override
    public void setLongitude(double longitude) {

        this.longitude = longitude;
    }

    /**
     * @see fabric.registry.System#getPlatformId()
     */
    @Override
    public String getPlatformId() {

        return platformId;
    }

    /**
     * @see fabric.registry.System#setPlatformId(java.lang.String)
     */
    @Override
    public void setPlatformId(String platformId) {

        this.platformId = platformId;
    }

    /**
     * @see fabric.registry.System#getReadiness()
     */
    @Override
    public String getReadiness() {

        return readiness;
    }

    /**
     * @see fabric.registry.System#setReadiness(java.lang.String)
     */
    @Override
    public void setReadiness(String readiness) {

        this.readiness = readiness;
    }

    /**
     * @see fabric.registry.System#getCredentials()
     */
    @Override
    public String getCredentials() {

        return credentials;
    }

    /**
     * @see fabric.registry.System#setCredentials(java.lang.String)
     */
    @Override
    public void setCredentials(String credentials) {

        this.credentials = credentials;
    }

    /**
     * @see fabric.registry.System#getTypeId()
     */
    @Override
    public String getTypeId() {

        return typeId;
    }

    /**
     * @see fabric.registry.System#setTypeId(java.lang.String)
     */
    @Override
    public void setTypeId(String typeId) {

        this.typeId = typeId;
    }

    /**
     * @see fabric.registry.System#getVelocity()
     */
    @Override
    public double getVelocity() {

        return velocity;
    }

    /**
     * @see fabric.registry.System#setVelocity(double)
     */
    @Override
    public void setVelocity(double velocity) {

        this.velocity = velocity;
    }

    /**
     * @see fabric.registry.RegistryObject#validate()
     */
    @Override
    public void validate() throws IncompleteObjectException {

        if (this.id == null || this.id.length() == 0 || this.typeId == null || this.typeId.length() == 0
                || this.platformId == null || this.platformId.length() == 0) {
            throw new IncompleteObjectException(
                    "ID, type and platform information have not been specified for this System");
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuilder buf = new StringBuilder("System::");
        buf.append(" System ID: ").append(id);
        buf.append(", Type ID: ").append(typeId);
        buf.append(", Platform ID: ").append(platformId);
        buf.append(", Credentials: ").append(credentials);
        buf.append(", Availability: ").append(availability);
        buf.append(", Readiness: ").append(readiness);
        buf.append(", Altitude: ").append(altitude);
        buf.append(", Bearing: ").append(bearing);
        buf.append(", Latitude: ").append(latitude);
        buf.append(", Longitude: ").append(longitude);
        buf.append(", Velocity: ").append(velocity);
        buf.append(", Description: ").append(description);
        buf.append(", Attributes: ").append(attributes);
        buf.append(", AttributesURI: ").append(attributesURI);
        return buf.toString();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        boolean equal = false;
        if (obj instanceof SystemImpl) {
            SystemImpl s = (SystemImpl) obj;
            if (s.getId().equals(id) && s.getTypeId().equals(typeId) && s.getPlatformId().equals(platformId)
                    && s.getCredentials() == null ? credentials == null : s.getCredentials().equals(credentials)
                    && s.getReadiness() == null ? readiness == null
                    : s.getReadiness().equals(readiness) && s.getAvailability() == null ? availability == null : s
                            .getAvailability().equals(availability)
                            && s.getLatitude() == latitude
                            && s.getLongitude() == longitude
                            && s.getAltitude() == altitude
                            && s.getBearing() == bearing
                            && s.getVelocity() == velocity
                            && s.getDescription() == null ? description == null : s.getDescription()
                            .equals(description)
                            && s.getAttributes() == null ? attributes == null : s.getAttributes().equals(attributes)
                            && s.getAttributesURI() == null ? attributesURI == null : s.getAttributesURI().equals(
                            attributesURI)) {

                equal = true;
            }
        }
        return equal;
    }

    /**
     * @see fabric.registry.System#getServices()
     */
    @Override
    public Service[] getServices() {

        return FabricRegistry.getServiceFactory().getServicesBySystem(platformId, id);
    }

    /**
     * @see fabric.registry.RegistryObject#key()
     */
    @Override
    public String key() {

        return new StringBuilder(this.getPlatformId()).append('/').append(this.getId()).append('/').append(
                this.getTypeId()).toString();
    }

    /**
     * @see fabric.registry.System#getKind()
     */
    @Override
    public String getKind() {

        return kind;
    }

    /**
     * @see fabric.registry.System#setKind(java.lang.String)
     */
    @Override
    public void setKind(String kind) {

        this.kind = kind;
    }

    /**
     * @see fabric.registry.System#getAttributesMap()
     */
    @Override
    public Map<String, String> getAttributesMap() {

        return AbstractFactory.buildAttributesMap(attributes);
    }

    /**
     * @see fabric.registry.System#setAttributesMap(java.util.Map)
     */
    @Override
    public void setAttributesMap(Map<String, String> attributes) {

        this.attributes = AbstractFactory.convertMapToNVPString(attributes);
    }

}
