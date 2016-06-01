/*
 * (C) Copyright IBM Corp. 2009
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.FabricRegistry;
import fabric.registry.Platform;
import fabric.registry.PlatformFactory;
import fabric.registry.QueryScope;
import fabric.registry.RegistryObject;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.exception.RegistryQueryException;
import fabric.registry.persistence.IPersistenceResultRow;

/**
 * Implementation of the factory for <code>Platform</code>'s.
 */
public class PlatformFactoryImpl extends AbstractFactory implements PlatformFactory {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

    /*
     * Constants
     */

    /** Factory for local (singleton) Registry operations */
    private static PlatformFactoryImpl localQueryInstance = null;

    /** Factory for remote (distributed) Registry operations */
    private static PlatformFactoryImpl remoteQueryInstance = null;

    /*
     * Queries
     */
    /** Select all records */
    private String SELECT_ALL_QUERY = null;
    /** Select a record by ID */
    private String BY_ID_QUERY = null;
    /** Select records by node ID */
    private String BY_NODE_QUERY = null;
    /** Select records by type ID */
    private String BY_TYPE_QUERY = null;
    /** Select records using an arbitrary WHERE clause */
    private String PREDICATE_QUERY = null;

    /*
     * Static initialisation
     */
    static {
        localQueryInstance = new PlatformFactoryImpl(QueryScope.LOCAL);
        remoteQueryInstance = new PlatformFactoryImpl(QueryScope.DISTRIBUTED);
    }

    public static PlatformFactoryImpl getInstance(QueryScope queryScope) {
        if (queryScope == QueryScope.LOCAL) {
            return localQueryInstance;
        } else {
            return remoteQueryInstance;
        }
    }

    private PlatformFactoryImpl(QueryScope queryScope) {
        this.queryScope = queryScope;

        SELECT_ALL_QUERY = format("select * from %s", FabricRegistry.PLATFORMS);
        BY_ID_QUERY = format("select * from %s where PLATFORM_ID='\\%s'", FabricRegistry.PLATFORMS);
        BY_NODE_QUERY = format("select * from %s where NODE_ID='\\%s'", FabricRegistry.PLATFORMS);
        BY_TYPE_QUERY = format("select * from %s where TYPE_ID='\\%s'", FabricRegistry.PLATFORMS);
        PREDICATE_QUERY = format("select * from %s where \\%s", FabricRegistry.PLATFORMS);
    }

    @Override
    public RegistryObject create(IPersistenceResultRow row) throws PersistenceException {
        Platform platform = null;
        if (row != null) {
            PlatformImpl impl = new PlatformImpl();
            impl.setId(row.getString(1));
            impl.setTypeId(row.getString(2));
            impl.setNodeId(row.getString(3));
            impl.setAffiliation(row.getString(4));
            impl.setCredentials(row.getString(5));
            impl.setReadiness(row.getString(6));
            impl.setAvailability(row.getString(7));
            impl.setLatitude(row.getDouble(8));
            impl.setLongitude(row.getDouble(9));
            impl.setAltitude(row.getDouble(10));
            impl.setBearing(row.getDouble(11));
            impl.setVelocity(row.getDouble(12));
            impl.setDescription(row.getString(13));
            impl.setAttributes(row.getString(14));
            impl.setAttributesURI(row.getString(15));

            /* preserve these values internally */
            impl.createShadow();

            platform = impl;
        }
        return platform;
    }

    @Override
    public String getDeleteSql(RegistryObject obj) {
        StringBuilder buf = new StringBuilder();
        if (obj instanceof Platform) {
            Platform platform = (Platform) obj;
            buf.append("delete from " + FabricRegistry.PLATFORMS + " where(");
            buf.append("PLATFORM_ID=").append(nullOrString(platform.getId())).append(" AND ");
            buf.append("Type_ID=").append(nullOrString(platform.getTypeId())).append(" AND ");
            buf.append("NODE_ID=").append(nullOrString(platform.getNodeId())).append(')');
        }
        return buf.toString();
    }

    @Override
    public String getInsertSql(RegistryObject obj) {
        StringBuilder buf = new StringBuilder();
        if (obj instanceof Platform) {
            Platform platform = (Platform) obj;
            buf.append("insert into " + FabricRegistry.PLATFORMS + " values(");
            buf.append(nullOrString(platform.getId())).append(',');
            buf.append(nullOrString(platform.getTypeId())).append(',');
            buf.append(nullOrString(platform.getNodeId())).append(',');
            buf.append(nullOrString(platform.getAffiliation())).append(',');
            buf.append(nullOrString(platform.getCredentials())).append(',');
            buf.append(nullOrString(platform.getReadiness())).append(',');
            buf.append(nullOrString(platform.getAvailability())).append(',');
            buf.append(platform.getLatitude()).append(',');
            buf.append(platform.getLongitude()).append(',');
            buf.append(platform.getAltitude()).append(',');
            buf.append(platform.getBearing()).append(',');
            buf.append(platform.getVelocity()).append(',');
            buf.append(nullOrString(platform.getDescription())).append(',');
            buf.append(nullOrString(platform.getAttributes())).append(',');
            buf.append(nullOrString(platform.getAttributesURI())).append(')');
        }
        return buf.toString();
    }

    @Override
    public String getUpdateSql(RegistryObject obj) {
        StringBuilder buf = new StringBuilder();
        if (obj instanceof Platform) {
            Platform platform = (Platform) obj;
            buf.append("update " + FabricRegistry.PLATFORMS + " set ");
            buf.append("TYPE_ID=").append(nullOrString(platform.getTypeId())).append(',');
            buf.append("NODE_ID=").append(nullOrString(platform.getNodeId())).append(',');
            buf.append("AFFILIATION=").append(nullOrString(platform.getAffiliation())).append(',');
            buf.append("CREDENTIALS=").append(nullOrString(platform.getCredentials())).append(',');
            buf.append("AVAILABILITY=").append(nullOrString(platform.getAvailability())).append(',');
            buf.append("READINESS=").append(nullOrString(platform.getReadiness())).append(',');
            buf.append("LATITUDE=").append(platform.getLatitude()).append(',');
            buf.append("LONGITUDE=").append(platform.getLongitude()).append(',');
            buf.append("ALTITUDE=").append(platform.getAltitude()).append(',');
            buf.append("BEARING=").append(platform.getBearing()).append(',');
            buf.append("VELOCITY=").append(platform.getVelocity()).append(',');
            buf.append("DESCRIPTION=").append(nullOrString(platform.getDescription())).append(',');
            buf.append("ATTRIBUTES=").append(nullOrString(platform.getAttributes())).append(',');
            buf.append("ATTRIBUTES_URI=").append(nullOrString(platform.getAttributesURI()));
            buf.append(" WHERE ");

            /* if it exists, use the shadow values for the WHERE clause */
            if (platform.getShadow() != null) {
                Platform shadow = (Platform) platform.getShadow();
                buf.append("PLATFORM_ID=").append(nullOrString(shadow.getId()));
            } else {
                buf.append("PLATFORM_ID=").append(nullOrString(platform.getId()));
            }
        }
        return buf.toString();
    }

    /**
     * @see fabric.registry.PlatformFactory#createPlatform(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Platform createPlatform(String platformId, String typeId, String nodeId) {

        return createPlatform(platformId, typeId, nodeId, null, null, null, null, 0.0, 0.0, 0.0, 0.0, 0.0, null, null,
                null);
    }

    /**
     * @see fabric.registry.PlatformFactory#createPlatform(java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String, java.lang.String, double, double, double, double,
     *      double, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Platform createPlatform(String platformId, String typeId, String nodeId, String affiliation,
            String securityClassification, String readiness, String availability, double latitude, double longitude,
            double altitude, double bearing, double velocity, String description, String attributes,
            String attributesURI) {

        return new PlatformImpl(platformId, typeId, nodeId, affiliation, securityClassification, readiness,
                availability, latitude, longitude, altitude, bearing, velocity, description, attributes, attributesURI);
    }

    /**
     * @see fabric.registry.PlatformFactory#getAllPlatforms()
     */
    @Override
    public Platform[] getAllPlatforms() {
        Platform[] platforms = null;
        try {
            platforms = runQuery(SELECT_ALL_QUERY);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return platforms;
    }

    /**
     * @see fabric.registry.PlatformFactory#getPlatforms(java.lang.String)
     */
    @Override
    public Platform[] getPlatforms(String queryPredicates) throws RegistryQueryException {
        Platform[] platforms = null;
        try {
            String query = format(PREDICATE_QUERY, queryPredicates);
            platforms = runQuery(query);
        } catch (PersistenceException e) {
            e.printStackTrace();
            throw new RegistryQueryException(e.getMessage());
        }
        return platforms;
    }

    /**
     * @see fabric.registry.PlatformFactory#getPlatformById(java.lang.String)
     */
    @Override
    public Platform getPlatformById(String id) {
        Platform p = null;
        try {
            String query = format(BY_ID_QUERY, id);
            Platform[] platforms = runQuery(query);
            if (platforms != null && platforms.length > 0) {
                p = platforms[0];
            }
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return p;
    }

    @Override
    public Platform[] getPlatformsByType(String typeId) {
        Platform[] platforms = null;
        try {
            String query = format(BY_TYPE_QUERY, typeId);
            platforms = runQuery(query);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return platforms;
    }

    private Platform[] runQuery(String sql) throws PersistenceException {
        Platform[] platforms = null;
        RegistryObject[] objects = queryRegistryObjects(sql, this);
        if (objects != null && objects.length > 0) {
            // necessary
            platforms = new Platform[objects.length];
            for (int k = 0; k < objects.length; k++) {
                platforms[k] = (Platform) objects[k];
            }
        } else {
            platforms = new Platform[0];
        }
        return platforms;
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.Factory#delete(fabric.registry.RegistryObject)
     */
    @Override
    public boolean delete(RegistryObject obj) {
        if (obj != null && obj instanceof Platform) {
            return super.delete(obj, this);
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.Factory#save(fabric.registry.RegistryObject)
     */
    @Override
    public boolean save(RegistryObject obj) throws IncompleteObjectException {
        if (obj != null && obj instanceof Platform) {
            return super.save(obj, this);
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.PlatformFactory#getPlatformsByNode(java.lang.String)
     */
    @Override
    public Platform[] getPlatformsByNode(String nodeId) {
        Platform[] platforms = null;
        try {
            String query = format(BY_NODE_QUERY, nodeId);
            platforms = runQuery(query);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return platforms;
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.Factory#insert(fabric.registry.RegistryObject)
     */
    @Override
    public boolean insert(RegistryObject obj) throws IncompleteObjectException, DuplicateKeyException,
        PersistenceException {

        if (obj != null && obj instanceof Platform) {
            return super.insert(obj, this);
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.Factory#update(fabric.registry.RegistryObject)
     */
    @Override
    public boolean update(RegistryObject obj) throws IncompleteObjectException, PersistenceException {

        if (obj != null && obj instanceof Platform) {
            return super.update(obj, this);
        } else {
            return false;
        }
    }

}
