/*
 * (C) Copyright IBM Corp. 2010, 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.FabricRegistry;
import fabric.registry.RegistryObject;
import fabric.registry.QueryScope;
import fabric.registry.SystemWiring;
import fabric.registry.SystemWiringFactory;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.exception.RegistryQueryException;
import fabric.registry.persistence.IPersistenceResultRow;

/**
 */
public class SystemWiringFactoryImpl extends AbstractFactory implements SystemWiringFactory {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010, 2014";

    /*
     * Class Constants
     */

    /** Factory for local (singleton) Registry operations */
    protected static SystemWiringFactoryImpl localQueryInstance = null;

    /** Factory for distributed (gaian) Registry operations */
    protected static SystemWiringFactoryImpl remoteQueryInstance = null;

    /*
     * Queries
     */

    /** Select all records */
    private String SELECT_ALL_QUERY = null;

    /** Select records using an arbitrary WHERE clause */
    private String PREDICATE_QUERY = null;

    /** Select record by id */
    private String BY_ID_QUERY = null;

    static {
        localQueryInstance = new SystemWiringFactoryImpl(QueryScope.LOCAL);
        remoteQueryInstance = new SystemWiringFactoryImpl(QueryScope.DISTRIBUTED);
    }

    public static SystemWiringFactoryImpl getInstance(QueryScope queryScope) {

        if (queryScope == QueryScope.LOCAL) {
            return localQueryInstance;
        } else {
            return remoteQueryInstance;
        }
    }

    private SystemWiringFactoryImpl(QueryScope queryScope) {

        this.queryScope = queryScope;

        SELECT_ALL_QUERY = format("select * from %s", FabricRegistry.SYSTEM_WIRING);
        PREDICATE_QUERY = format("select * from %s where \\%s", FabricRegistry.SYSTEM_WIRING);
        BY_ID_QUERY = format(
                "select * from %s where COMPOSITE_ID='\\%s' AND FROM_SERVICE_PLATFORM_ID='\\%s' AND FROM_SERVICE_ID='\\%s' AND "
                        + "FROM_INTERFACE_ID='\\%s' AND TO_SERVICE_PLATFORM_ID='\\%s' AND TO_SERVICE_ID='\\%s' AND "
                        + "TO_INTERFACE_ID='\\%s'", FabricRegistry.SYSTEM_WIRING);
    }

    @Override
    public RegistryObject create(IPersistenceResultRow row) throws PersistenceException {

        SystemWiring wiring = null;
        if (row != null) {
            String compositeId = row.getString(1);
            String fromSystemPlatformId = row.getString(2);
            String fromSystemId = row.getString(3);
            String fromInterfaceId = row.getString(4);
            String toSystemPlatformId = row.getString(5);
            String toSystemId = row.getString(6);
            String toInterfaceId = row.getString(7);
            String attrs = row.getString(8);
            String attrsURI = row.getString(9);
            SystemWiringImpl impl = new SystemWiringImpl(compositeId, fromSystemPlatformId, fromSystemId,
                    fromInterfaceId, toSystemPlatformId, toSystemId, toInterfaceId, attrs, attrsURI);

            /* preserve these values internally */
            impl.createShadow();

            wiring = impl;
        }
        return wiring;
    }

    @Override
    public String getDeleteSql(RegistryObject obj) {

        StringBuffer buf = new StringBuffer();
        if (obj instanceof SystemWiring) {
            SystemWiring systemWiring = (SystemWiring) obj;
            buf.append("delete from " + FabricRegistry.SYSTEM_WIRING + " where (");
            buf.append("Composite_ID='").append(systemWiring.getCompositeId()).append("' AND ");
            buf.append("From_Service_Platform_ID='").append(systemWiring.getFromSystemPlatformId()).append("' AND ");
            buf.append("From_Service_ID='").append(systemWiring.getFromSystemId()).append("' AND ");
            buf.append("From_Interface_ID='").append(systemWiring.getFromInterfaceId()).append("' AND ");
            buf.append("To_Service_Platform_ID='").append(systemWiring.getToSystemPlatformId()).append("' AND ");
            buf.append("To_Service_ID='").append(systemWiring.getToSystemId()).append("' AND ");
            buf.append("TO_INTERFACE_ID='").append(systemWiring.getToInterfaceId()).append("')");
        }
        return buf.toString();
    }

    @Override
    public String getInsertSql(RegistryObject obj) {

        StringBuffer buf = new StringBuffer();
        if (obj instanceof SystemWiring) {
            SystemWiring systemWiring = (SystemWiring) obj;
            buf.append("insert into " + FabricRegistry.SYSTEM_WIRING + " values(");
            buf.append("'").append(systemWiring.getCompositeId()).append("',");
            buf.append("'").append(systemWiring.getFromSystemPlatformId()).append("',");
            buf.append("'").append(systemWiring.getFromSystemId()).append("',");
            buf.append("'").append(systemWiring.getFromInterfaceId()).append("',");
            buf.append("'").append(systemWiring.getToSystemPlatformId()).append("',");
            buf.append("'").append(systemWiring.getToSystemId()).append("',");
            buf.append("'").append(systemWiring.getToInterfaceId()).append("',");
            buf.append("").append(nullOrString(systemWiring.getAttributes())).append(",");
            buf.append("").append(nullOrString(systemWiring.getAttributesURI())).append(")");
        }
        return buf.toString();
    }

    @Override
    public String getUpdateSql(RegistryObject obj) {

        StringBuffer buf = new StringBuffer();
        if (obj instanceof SystemWiring) {
            SystemWiring systemWiring = (SystemWiring) obj;
            buf.append("update " + FabricRegistry.SYSTEM_WIRING + " set ");
            buf.append("COMPOSITE_ID='").append(systemWiring.getCompositeId()).append("',");
            buf.append("FROM_SERVICE_PLATFORM_ID='").append(systemWiring.getFromSystemPlatformId()).append("',");
            buf.append("FROM_SERVICE_ID='").append(systemWiring.getFromSystemId()).append("',");
            buf.append("FROM_INTERFACE_ID='").append(systemWiring.getFromInterfaceId()).append("',");
            buf.append("TO_SERVICE_PLATFORM_ID='").append(systemWiring.getToSystemPlatformId()).append("',");
            buf.append("TO_SERVICE_ID='").append(systemWiring.getToSystemId()).append("',");
            buf.append("TO_INTERFACE_ID='").append(systemWiring.getToInterfaceId()).append("',");
            buf.append("Attributes=").append(nullOrString(systemWiring.getAttributes())).append(",");
            buf.append("Attributes_URI=").append(nullOrString(systemWiring.getAttributesURI())).append("");
            buf.append(" WHERE ");

            /* if it exists, use the shadow values for the WHERE clause */
            if (systemWiring.getShadow() != null) {
                SystemWiring originalSystemWiring = (SystemWiring) systemWiring.getShadow();
                buf.append(" COMPOSITE_ID='").append(originalSystemWiring.getCompositeId()).append("' AND ");
                buf.append(" FROM_SERVICE_PLATFORM_ID='").append(originalSystemWiring.getFromSystemPlatformId())
                .append("' AND ");
                buf.append(" FROM_SERVICE_ID='").append(originalSystemWiring.getFromSystemId()).append("' AND ");
                buf.append(" FROM_INTERFACE_ID='").append(originalSystemWiring.getFromInterfaceId()).append("' AND ");
                buf.append(" TO_SERVICE_PLATFORM_ID='").append(originalSystemWiring.getToSystemPlatformId()).append(
                        "' AND ");
                buf.append(" TO_SERVICE_ID='").append(originalSystemWiring.getToSystemId()).append("' AND ");
                buf.append(" TO_INTERFACE_ID='").append(originalSystemWiring.getToInterfaceId()).append("'");
            } else {
                buf.append(" COMPOSITE_ID='").append(systemWiring.getCompositeId()).append("' AND ");
                buf.append(" FROM_SERVICE_PLATFORM_ID='").append(systemWiring.getFromSystemPlatformId()).append(
                        "' AND ");
                buf.append(" FROM_SERVICE_ID='").append(systemWiring.getFromSystemId()).append("' AND ");
                buf.append(" FROM_INTERFACE_ID='").append(systemWiring.getFromInterfaceId()).append("' AND ");
                buf.append(" TO_SERVICE_PLATFORM_ID='").append(systemWiring.getToSystemPlatformId()).append("' AND ");
                buf.append(" TO_SERVICE_ID='").append(systemWiring.getToSystemId()).append("' AND ");
                buf.append(" TO_INTERFACE_ID='").append(systemWiring.getToInterfaceId()).append("'");
            }
        }
        return buf.toString();
    }

    @Override
    public SystemWiring create(String compositeId, String fromSystemPlatformId, String fromSystemId,
            String fromInterfaceId, String toSystemPlatformId, String toSystemId, String toInterfaceId) {

        return create(compositeId, fromSystemPlatformId, fromSystemId, fromInterfaceId, toSystemPlatformId, toSystemId,
                toInterfaceId, null, null);
    }

    @Override
    public SystemWiring create(String compositeId, String fromSystemPlatformId, String fromSystemId,
            String fromInterfaceId, String toSystemPlatformId, String toSystemId, String toInterfaceId,
            String attributes, String attributesURI) {

        SystemWiring sw = new SystemWiringImpl(compositeId, fromSystemPlatformId, fromSystemId, fromInterfaceId,
                toSystemPlatformId, toSystemId, toInterfaceId, attributes, attributesURI);
        return sw;
    }

    @Override
    public SystemWiring[] get(String queryPredicates) throws RegistryQueryException {

        SystemWiring[] systemWiring = null;
        try {
            String query = format(PREDICATE_QUERY, queryPredicates);
            systemWiring = queryWiring(query);
        } catch (PersistenceException e) {
            e.printStackTrace();
            throw new RegistryQueryException(e.getMessage());
        }
        return systemWiring;
    }

    @Override
    public SystemWiring[] getAll() {

        SystemWiring[] systemWiring = null;
        try {
            String query = format(SELECT_ALL_QUERY);
            systemWiring = queryWiring(query);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return systemWiring;
    }

    @Override
    public SystemWiring getById(String compositeId, String fromSystemPlatformId, String fromSystemId,
            String fromInterfaceId, String toSystemPlatformId, String toSystemId, String toInterfaceId) {

        SystemWiring systemWiring = null;
        try {
            String query = format(BY_ID_QUERY, compositeId, fromSystemPlatformId, fromSystemId, fromInterfaceId,
                    toSystemPlatformId, toSystemId, toInterfaceId);
            SystemWiring[] systemWiringList = queryWiring(query);
            if (systemWiringList != null && systemWiringList.length > 0) {
                systemWiring = systemWiringList[0]; /* pick the first one - ignore any duplicates */
            }
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return systemWiring;
    }

    private SystemWiring[] queryWiring(String sql) throws PersistenceException {

        SystemWiring[] systemWiringList = null;
        RegistryObject[] objects = queryRegistryObjects(sql, this);
        if (objects != null && objects.length > 0) {
            systemWiringList = new SystemWiring[objects.length];
            for (int x = 0; x < objects.length; x++) {
                systemWiringList[x] = (SystemWiring) objects[x];
            }
        } else {
            systemWiringList = new SystemWiring[0];
        }
        return systemWiringList;
    }

    @Override
    public boolean delete(RegistryObject obj) {

        if (obj != null && obj instanceof SystemWiring) {
            return super.delete(obj, this);
        } else {
            return false;
        }
    }

    @Override
    public boolean insert(RegistryObject obj) throws IncompleteObjectException, DuplicateKeyException,
    PersistenceException {

        if (obj != null && obj instanceof SystemWiring) {
            return super.insert(obj, this);
        } else {
            return false;
        }
    }

    @Override
    public boolean save(RegistryObject obj) throws IncompleteObjectException {

        if (obj != null && obj instanceof SystemWiring) {
            return super.save(obj, this);
        } else {
            return false;
        }
    }

    @Override
    public boolean update(RegistryObject obj) throws IncompleteObjectException, PersistenceException {

        if (obj != null && obj instanceof SystemWiring) {
            return super.update(obj, this);
        } else {
            return false;
        }
    }

}
