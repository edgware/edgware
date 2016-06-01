/*
 * (C) Copyright IBM Corp. 2011, 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.Service;
import fabric.registry.exception.IncompleteObjectException;

/**
 * Implementation class for a Fabric <code>Service</code>.
 */
public class ServiceImpl extends AbstractRegistryObject implements Service {

    /*
     * Class constants
     */

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2011, 2014";

    /*
     * Class fields
     */

    private String id = null;

    private String platformId = null;

    private String systemId = null;

    private String typeID = null;

    private String mode = null;

    private String credentials = null;

    private String availability = null;

    private String description = null;

    private String attributes = null;

    private String attributesURI = null;

    /**
     * Default Constructor
     * 
     */
    protected ServiceImpl() {

    }

    /**
     * Populated creation constructor
     * 
     * @param platformId
     * @param systemId
     * @param id
     * @param typeID
     * @param mode
     * @param credentials
     * @param availability
     * @param description
     * @param attributes
     * @param attributesURI
     */
    protected ServiceImpl(String platformId, String systemId, String id, String typeID, String mode,
            String credentials, String availability, String description, String attributes, String attributesURI) {

        this.id = id;
        this.platformId = platformId;
        this.systemId = systemId;
        this.typeID = typeID;
        this.mode = mode;
        this.credentials = credentials;
        this.availability = availability;
        this.description = description;
        this.attributes = attributes;
        this.attributesURI = attributesURI;
    }

    /**
     * @see fabric.registry.Service#getAttributes()
     */
    @Override
    public String getAttributes() {

        return attributes;
    }

    /**
     * @see fabric.registry.Service#getAttributesURI()
     */
    @Override
    public String getAttributesURI() {

        return attributesURI;
    }

    /**
     * @see fabric.registry.Service#getAvailability()
     */
    @Override
    public String getAvailability() {

        return availability;
    }

    /**
     * @see fabric.registry.Service#getDescription()
     */
    @Override
    public String getDescription() {

        return description;
    }

    /**
     * @see fabric.registry.Service#getCredentials()
     */
    @Override
    public String getCredentials() {

        return credentials;
    }

    /**
     * @see fabric.registry.Service#getSystemId()
     */
    @Override
    public String getSystemId() {

        return systemId;
    }

    /**
     * @see fabric.registry.Service#getTypeId()
     */
    @Override
    public String getTypeId() {

        return typeID;
    }

    /**
     * @see fabric.registry.Service#setAttributes(java.lang.String)
     */
    @Override
    public void setAttributes(String attrs) {

        attributes = attrs;
    }

    /**
     * @see fabric.registry.Service#setAttributesURI(java.lang.String)
     */
    @Override
    public void setAttributesURI(String uri) {

        attributesURI = uri;
    }

    /**
     * @see fabric.registry.Service#setAvailability(java.lang.String)
     */
    @Override
    public void setAvailability(String availability) {

        this.availability = availability;
    }

    /**
     * @see fabric.registry.Service#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String desc) {

        description = desc;
    }

    /**
     * @see fabric.registry.Service#setCredentials(java.lang.String)
     */
    @Override
    public void setCredentials(String credentials) {

        this.credentials = credentials;
    }

    /**
     * @see fabric.registry.Service#setSystemId(java.lang.String)
     */
    @Override
    public void setSystemId(String id) {

        systemId = id;
    }

    /**
     * @see fabric.registry.Service#setTypeId(java.lang.String)
     */
    @Override
    public void setTypeId(String typeID) {

        this.typeID = typeID;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuilder buf = new StringBuilder("Service::");
        buf.append(" ID: ").append(id);
        buf.append(", Platform ID: ").append(platformId);
        buf.append(", System ID: ").append(systemId);
        buf.append(", Feed Type ID: ").append(typeID);
        buf.append(", Mode: ").append(mode);
        buf.append(", Credentials: ").append(credentials);
        buf.append(", Availability: ").append(availability);
        buf.append(", Description: ").append(description);
        buf.append(", Attributes: ").append(attributes);
        buf.append(", AttributesURI: ").append(attributesURI);
        return buf.toString();
    }

    /**
     * @see fabric.registry.RegistryObject#validate()
     */
    @Override
    public void validate() throws IncompleteObjectException {

        if (this.id == null || this.platformId == null || this.systemId == null || this.typeID == null) {
            throw new IncompleteObjectException(
                    "Service name, system name, platform name and service type information are all mandatory.");
        }
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        boolean equal = false;
        if (obj instanceof Service) {
            Service f = (Service) obj;
            if (f.getId().equals(id) && f.getPlatformId().equals(platformId) && f.getSystemId().equals(systemId)
                    && f.getTypeId().equals(typeID) && f.getCredentials() == null ? credentials == null : f
                    .getCredentials().equals(credentials)
                    && f.getAvailability() == null ? availability == null : f.getAvailability().equals(availability)
                    && f.getDescription() == null ? description == null : f.getDescription().equals(description)
                    && f.getAttributes() == null ? attributes == null : f.getAttributes().equals(attributes)
                    && f.getAttributesURI() == null ? attributesURI == null : f.getAttributesURI()
                    .equals(attributesURI)) {

                equal = true;
            }
        }
        return equal;
    }

    /**
     * @see fabric.registry.Service#getId()
     */
    @Override
    public String getId() {

        return id;
    }

    /**
     * @see fabric.registry.Service#getPlatformId()
     */
    @Override
    public String getPlatformId() {

        return platformId;
    }

    /**
     * @see fabric.registry.Service#setId(java.lang.String)
     */
    @Override
    public void setId(String id) {

        this.id = id;
    }

    /**
     * @see fabric.registry.Service#setPlatformId(java.lang.String)
     */
    @Override
    public void setPlatformId(String platformId) {

        this.platformId = platformId;
    }

    /**
     * @see fabric.registry.RegistryObject#key()
     */
    @Override
    public String key() {

        return new StringBuilder(this.getPlatformId()).append('/').append(this.getSystemId()).append('/').append(
                this.getId()).append('/').append(this.getTypeId()).toString();
    }

    /**
     * @see fabric.registry.Service#getMode()
     */
    @Override
    public String getMode() {

        return mode;
    }

    /**
     * @see fabric.registry.Service#setMode(java.lang.String)
     */
    @Override
    public void setMode(String mode) {

        this.mode = mode;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        /* simplistic hashcode */
        return this.toString().hashCode();
    }
}