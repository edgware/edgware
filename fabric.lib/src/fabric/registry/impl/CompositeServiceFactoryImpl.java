/*
 * (C) Copyright IBM Corp. 2010
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.CompositeService;
import fabric.registry.CompositeServiceFactory;
import fabric.registry.FabricRegistry;
import fabric.registry.QueryScope;
import fabric.registry.RegistryObject;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.exception.RegistryQueryException;
import fabric.registry.persistence.IPersistenceResultRow;

/**
 * Implementation of the factory for <code>CompositeService</code>s.
 */
public class CompositeServiceFactoryImpl extends AbstractFactory implements CompositeServiceFactory {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2010";

    /*
     * Class Constants
     */

    /** Factory for local (singleton) Registry operations */
    protected static CompositeServiceFactoryImpl localQueryInstance = null;
    /** Factory for distributed (gaian) Registry operations */
    protected static CompositeServiceFactoryImpl remoteQueryInstance = null;

    /*
     * Queries
     */
    /** Select all records */
    private String SELECT_ALL_QUERY = null;
    /** Select records by type */
    private String BY_TYPE_QUERY = null;
    /** Select records using an arbitrary WHERE clause */
    private String PREDICATE_QUERY = null;
    /** Select record by id */
    private String BY_ID_QUERY = null;

    static {
        localQueryInstance = new CompositeServiceFactoryImpl(QueryScope.LOCAL);
        remoteQueryInstance = new CompositeServiceFactoryImpl(QueryScope.DISTRIBUTED);
    }

    public static CompositeServiceFactoryImpl getInstance(QueryScope queryScope) {
        if (queryScope == QueryScope.LOCAL) {
            return localQueryInstance;
        } else {
            return remoteQueryInstance;
        }
    }

    private CompositeServiceFactoryImpl(QueryScope queryScope) {
        this.queryScope = queryScope;

        SELECT_ALL_QUERY = format("select * from %s", FabricRegistry.COMPOSITE_SYSTEMS);
        BY_TYPE_QUERY = format("select * from %s where TYPE \\%s", FabricRegistry.COMPOSITE_SYSTEMS);
        PREDICATE_QUERY = format("select * from %s where \\%s", FabricRegistry.COMPOSITE_SYSTEMS);
        BY_ID_QUERY = format("select * from %s where ID='\\%s'", FabricRegistry.COMPOSITE_SYSTEMS);
    }

    @Override
    public CompositeService create(IPersistenceResultRow row) throws PersistenceException {
        CompositeService service = null;
        if (row != null) {
            String compositeId = row.getString(1);
            String type = row.getString(2);
            String affiliation = row.getString(3);
            String credentials = row.getString(4);
            String desc = row.getString(5);
            String attrs = row.getString(6);
            String attrsURI = row.getString(7);
            CompositeServiceImpl impl = new CompositeServiceImpl(compositeId, type, affiliation, credentials, desc,
                    attrs, attrsURI);

            /* preserve these values internally */
            impl.createShadow();

            service = impl;
        }
        return service;
    }

    @Override
    public String getDeleteSql(RegistryObject obj) {
        StringBuilder buf = new StringBuilder();
        if (obj instanceof CompositeService) {
            CompositeService service = (CompositeService) obj;
            buf.append("delete from " + FabricRegistry.COMPOSITE_SYSTEMS + " where ");
            buf.append("ID='").append(service.getId()).append('\'');
        }
        return buf.toString();
    }

    @Override
    public String getInsertSql(RegistryObject obj) {
        StringBuilder buf = new StringBuilder();
        if (obj instanceof CompositeService) {
            CompositeService service = (CompositeService) obj;
            buf.append("insert into " + FabricRegistry.COMPOSITE_SYSTEMS + " values(");
            buf.append('\'').append(service.getId()).append('\'').append(',');
            buf.append("").append(nullOrString(service.getType())).append(',');
            buf.append("").append(nullOrString(service.getAffiliation())).append(',');
            buf.append("").append(nullOrString(service.getCredentials())).append(',');
            buf.append("").append(nullOrString(service.getDescription())).append(',');
            buf.append("").append(nullOrString(service.getAttributes())).append(',');
            buf.append("").append(nullOrString(service.getAttributesURI())).append(')');
        }
        return buf.toString();
    }

    @Override
    public String getUpdateSql(RegistryObject obj) {
        StringBuilder buf = new StringBuilder();
        if (obj instanceof CompositeService) {
            CompositeService service = (CompositeService) obj;
            buf.append("update " + FabricRegistry.COMPOSITE_SYSTEMS + " set ");
            buf.append("ID='").append(service.getId()).append('\'').append(',');
            buf.append("TYPE=").append(nullOrString(service.getType())).append(',');
            buf.append("AFFILIATION=").append(nullOrString(service.getAffiliation())).append(',');
            buf.append("Credentials=").append(nullOrString(service.getCredentials())).append(',');
            buf.append("Description=").append(nullOrString(service.getDescription())).append(',');
            buf.append("Attributes=").append(nullOrString(service.getAttributes())).append(',');
            buf.append("Attributes_URI=").append(nullOrString(service.getAttributesURI())).append("");
            buf.append(" WHERE ");

            /* if it exists, use the shadow values for the WHERE clause */
            if (service.getShadow() != null) {
                CompositeService originalService = (CompositeService) service.getShadow();
                buf.append(" ID='").append(originalService.getId()).append('\'');
            } else {
                buf.append(" ID='").append(service.getId()).append('\'');
            }
        }
        return buf.toString();
    }

    @Override
    public CompositeService create(String id) {
        return create(id, null, null, null, null, null, null);
    }

    @Override
    public CompositeService create(String id, String type, String affiliation, String credentials, String description,
            String attributes, String attributesURI) {

        CompositeService cs = new CompositeServiceImpl(id, type, affiliation, credentials, description, attributes,
                attributesURI);
        return cs;
    }

    @Override
    public CompositeService[] get(String queryPredicates) throws RegistryQueryException {

        CompositeService[] systems = null;
        try {
            String query = format(PREDICATE_QUERY, queryPredicates);
            systems = querySystems(query);
        } catch (PersistenceException e) {
            e.printStackTrace();
            throw new RegistryQueryException(e.getMessage());
        }
        return systems;
    }

    @Override
    public CompositeService[] getAll() {
        CompositeService[] systems = null;
        try {
            systems = querySystems(SELECT_ALL_QUERY);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return systems;
    }

    @Override
    public CompositeService getById(String id) {
        CompositeService service = null;
        try {
            String query = format(BY_ID_QUERY, id);
            CompositeService[] systems = querySystems(query);
            if (systems != null && systems.length > 0) {
                service = systems[0]; /* pick the first one - ignore any duplicates */
            }
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return service;
    }

    @Override
    public CompositeService[] getByType(String type) {
        CompositeService[] systems = null;
        try {
            // null values have to be handled differently in SQL
            if (type == null) {
                type = "IS NULL";
            } else {
                type = "=" + nullOrString(type);
            }
            String query = format(BY_TYPE_QUERY, type);
            systems = querySystems(query);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return systems;
    }

    private CompositeService[] querySystems(String sql) throws PersistenceException {
        CompositeService[] systems = null;
        RegistryObject[] objects = queryRegistryObjects(sql, this);
        if (objects != null && objects.length > 0) {
            systems = new CompositeService[objects.length];
            for (int x = 0; x < objects.length; x++) {
                systems[x] = (CompositeService) objects[x];
            }
        } else {
            systems = new CompositeService[0];
        }
        return systems;
    }

    @Override
    public boolean delete(RegistryObject obj) {
        if (obj != null && obj instanceof CompositeService) {
            return super.delete(obj, this);
        } else {
            return false;
        }
    }

    @Override
    public boolean insert(RegistryObject obj) throws IncompleteObjectException, DuplicateKeyException,
        PersistenceException {

        if (obj != null && obj instanceof CompositeService) {
            return super.insert(obj, this);
        } else {
            return false;
        }
    }

    @Override
    public boolean save(RegistryObject obj) throws IncompleteObjectException {

        if (obj != null && obj instanceof CompositeService) {
            return super.save(obj, this);
        } else {
            return false;
        }
    }

    @Override
    public boolean update(RegistryObject obj) throws IncompleteObjectException, PersistenceException {

        if (obj != null && obj instanceof CompositeService) {
            return super.update(obj, this);
        } else {
            return false;
        }
    }

}
