/*
 * (C) Copyright IBM Corp. 2009, 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import java.util.logging.Level;

import fabric.Fabric;
import fabric.registry.FabricRegistry;
import fabric.registry.QueryScope;
import fabric.registry.RegistryObject;
import fabric.registry.Type;
import fabric.registry.TypeFactory;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.exception.RegistryQueryException;
import fabric.registry.persistence.IPersistenceResultRow;

/**
 * Implementation of the factory for <code>ActorType</code>s, <code>FeedType</code>s, <code>NodeType</code>s,
 * <code>PlatformType</code>s and <code>ServiceType</code>s.
 */
public class TypeFactoryImpl extends AbstractFactory implements TypeFactory {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2014";

    /*
     * Class constants
     */

    /** Factory for local (singleton) Registry operations */
    private static TypeFactory localQueryInstance = null;

    /** Factory for distributed (Gaian) Registry operations */
    private static TypeFactory remoteQueryInstance = null;

    /*
     * Queries
     */

    /** Select all records */
    private String SELECT_ALL_QUERY_ACTOR_TYPES = null;
    private String SELECT_ALL_QUERY_SERVICE_TYPES = null;
    private String SELECT_ALL_QUERY_NODE_TYPES = null;
    private String SELECT_ALL_QUERY_PLATFORM_TYPES = null;
    private String SELECT_ALL_QUERY_SYSTEM_TYPES = null;
    private String SELECT_ALL_SERVICE_WIRING = null;

    /** Select records of a particular type */
    private String BY_ID_QUERY_ACTOR_TYPE = null;
    private String BY_ID_QUERY_SERVICE_TYPE = null;
    private String BY_ID_QUERY_NODE_TYPE = null;
    private String BY_ID_QUERY_PLATFORM_TYPE = null;
    private String BY_ID_QUERY_SYSTEM_TYPE = null;

    /** Select records using an arbitrary WHERE clause */
    private String PREDICATE_QUERY_ACTOR_TYPES = null;
    private String PREDICATE_QUERY_SERVICE_TYPES = null;
    private String PREDICATE_QUERY_NODE_TYPES = null;
    private String PREDICATE_QUERY_PLATFORM_TYPES = null;
    private String PREDICATE_QUERY_SYSTEM_TYPES = null;

    /*
     * static initialisation
     */
    static {
        localQueryInstance = new TypeFactoryImpl(QueryScope.LOCAL);
        remoteQueryInstance = new TypeFactoryImpl(QueryScope.DISTRIBUTED);
    }

    private TypeFactoryImpl(QueryScope queryScope) {

        this.queryScope = queryScope;

        SELECT_ALL_QUERY_ACTOR_TYPES = Fabric.format("select * from %s", FabricRegistry.ACTOR_TYPES);
        SELECT_ALL_QUERY_SERVICE_TYPES = Fabric.format("select * from %s", FabricRegistry.FEED_TYPES);
        SELECT_ALL_QUERY_NODE_TYPES = Fabric.format("select * from %s", FabricRegistry.NODE_TYPES);
        SELECT_ALL_QUERY_PLATFORM_TYPES = Fabric.format("select * from %s", FabricRegistry.PLATFORM_TYPES);
        SELECT_ALL_QUERY_SYSTEM_TYPES = Fabric.format("select * from %s", FabricRegistry.SYSTEM_TYPES);
        SELECT_ALL_SERVICE_WIRING = Fabric.format("select * from %s", FabricRegistry.SYSTEM_WIRING);

        BY_ID_QUERY_ACTOR_TYPE = Fabric.format("select * from %s where TYPE_ID='\\%s'", FabricRegistry.ACTOR_TYPES);
        BY_ID_QUERY_SERVICE_TYPE = Fabric.format("select * from %s where TYPE_ID='\\%s'", FabricRegistry.FEED_TYPES);
        BY_ID_QUERY_NODE_TYPE = Fabric.format("select * from %s where TYPE_ID='\\%s'", FabricRegistry.NODE_TYPES);
        BY_ID_QUERY_PLATFORM_TYPE = Fabric.format("select * from %s where TYPE_ID='\\%s'",
                FabricRegistry.PLATFORM_TYPES);
        BY_ID_QUERY_SYSTEM_TYPE = Fabric.format("select * from %s where TYPE_ID='\\%s'", FabricRegistry.SYSTEM_TYPES);

        PREDICATE_QUERY_ACTOR_TYPES = Fabric.format("select * from %s where \\%s", FabricRegistry.ACTOR_TYPES);
        PREDICATE_QUERY_SERVICE_TYPES = Fabric.format("select * from %s where \\%s", FabricRegistry.FEED_TYPES);
        PREDICATE_QUERY_NODE_TYPES = Fabric.format("select * from %s where \\%s", FabricRegistry.NODE_TYPES);
        PREDICATE_QUERY_PLATFORM_TYPES = Fabric.format("select * from %s where \\%s", FabricRegistry.PLATFORM_TYPES);
        PREDICATE_QUERY_SYSTEM_TYPES = Fabric.format("select * from %s where \\%s", FabricRegistry.SYSTEM_TYPES);
    }

    public static TypeFactory getInstance(QueryScope queryScope) {

        if (queryScope == QueryScope.LOCAL) {
            return localQueryInstance;
        } else {
            return remoteQueryInstance;
        }
    }

    @Override
    public RegistryObject create(IPersistenceResultRow row) throws PersistenceException {

        Type type = null;
        if (row != null) {
            String typeID = row.getString(1);
            String desc = row.getString(2);
            String attrs = row.getString(3);
            String attrsURI = row.getString(4);
            TypeImpl impl = new TypeImpl(typeID, desc, attrs, attrsURI);

            /* preserve these values internally */
            impl.createShadow();

            type = impl;
        }
        return type;
    }

    @Override
    public Type createActorType(String typeId, String description, String attributes, String attributesUri) {

        return createType(Type.TYPE_ACTOR, typeId, description, attributes, attributesUri);
    }

    @Override
    public Type createServiceType(String typeId, String description, String attributes, String attributesUri) {

        return createType(Type.TYPE_FEED, typeId, description, attributes, attributesUri);
    }

    @Override
    public Type createNodeType(String typeId, String description, String attributes, String attributesUri) {

        return createType(Type.TYPE_NODE, typeId, description, attributes, attributesUri);
    }

    @Override
    public Type createPlatformType(String typeId, String description, String attributes, String attributesUri) {

        return createType(Type.TYPE_PLATFORM, typeId, description, attributes, attributesUri);
    }

    @Override
    public Type createSystemType(String typeId, String description, String attributes, String attributesUri) {

        return createType(Type.TYPE_SERVICE, typeId, description, attributes, attributesUri);
    }

    private Type createType(int classifier, String typeId, String description, String attributes, String attributesUri) {

        Type t = new TypeImpl(typeId, description, attributes, attributesUri);
        t.setClassifier(classifier);
        return t;
    }

    private int getClassifierFromTableName(String tableName) {

        int classifier = -1;
        if (tableName.equals("ACTOR_TYPES") || tableName.equals("CTLT")) {
            classifier = Type.TYPE_ACTOR;
        } else if (tableName.equals("FEED_TYPES") || tableName.equals("FTLT")) {
            classifier = Type.TYPE_FEED;
        } else if (tableName.equals("NODE_TYPES") || tableName.equals("NTLT")) {
            classifier = Type.TYPE_NODE;
        } else if (tableName.equals("PLATFORM_TYPES") || tableName.equals("PTLT")) {
            classifier = Type.TYPE_PLATFORM;
        } else if (tableName.equals("SERVICE_TYPES") || tableName.equals("STLT")) {
            classifier = Type.TYPE_SERVICE;
        } else {
            logger.log(Level.WARNING, "Cannot map table name [{0}] to a type", tableName);
        }
        return classifier;
    }

    @Override
    public String getDeleteSql(RegistryObject obj) {

        StringBuffer buf = new StringBuffer();
        if (obj instanceof Type) {
            Type type = (Type) obj;
            buf.append("delete FROM ");
            buf.append(getTableNameForUpdate(type.getClassifier()));
            buf.append(" WHERE (");
            buf.append("TYPE_ID='");
            buf.append(type.getId()).append("')");
        }
        return buf.toString();
    }

    @Override
    public String getInsertSql(RegistryObject obj) {

        StringBuffer buf = new StringBuffer();
        if (obj instanceof Type) {
            Type type = (Type) obj;
            buf.append("insert into ");
            buf.append(getTableNameForUpdate(type.getClassifier()));
            buf.append(" values(");
            buf.append("'").append(type.getId()).append("',");
            buf.append("'").append(type.getDescription()).append("',");
            buf.append("'").append(type.getAttributes()).append("',");
            buf.append("'").append(type.getAttributesUri()).append("')");
        }
        return buf.toString();
    }

    @Override
    public String getUpdateSql(RegistryObject obj) {

        StringBuffer buf = new StringBuffer();
        if (obj instanceof Type) {
            Type type = (Type) obj;
            buf.append("update ");
            buf.append(getTableNameForUpdate(type.getClassifier()));
            buf.append(" set ");
            buf.append("TYPE_ID='");
            buf.append(type.getId()).append("',");
            buf.append("DESCRIPTION='").append(type.getDescription()).append("',");
            buf.append("ATTRIBUTES='").append(type.getAttributes()).append("',");
            buf.append("ATTRIBUTES_URI='").append(type.getAttributesUri()).append("'");
            buf.append(" WHERE ");

            /* if it exists, use the shadow values for the WHERE clause */
            if (type.getShadow() != null) {
                Type shadow = (Type) type.getShadow();
                buf.append("TYPE_ID='");
                buf.append(shadow.getId());
                buf.append("'");
            } else {
                buf.append("TYPE_ID='");
                buf.append(type.getId());
                buf.append("'");
            }

        }
        return buf.toString();
    }

    @Override
    public Type[] getAllActorTypes() {

        return queryAll(SELECT_ALL_QUERY_ACTOR_TYPES, Type.TYPE_ACTOR);
    }

    @Override
    public Type[] getAllServiceTypes() {

        return queryAll(SELECT_ALL_QUERY_SERVICE_TYPES, Type.TYPE_FEED);
    }

    @Override
    public Type[] getAllNodeTypes() {

        return queryAll(SELECT_ALL_QUERY_NODE_TYPES, Type.TYPE_NODE);
    }

    @Override
    public Type[] getAllPlatformTypes() {

        return queryAll(SELECT_ALL_QUERY_PLATFORM_TYPES, Type.TYPE_PLATFORM);
    }

    @Override
    public Type[] getAllSystemTypes() {

        return queryAll(SELECT_ALL_QUERY_SYSTEM_TYPES, Type.TYPE_SERVICE);
    }

    @Override
    public Type[] getAllServiceWiring() {

        return queryAll(SELECT_ALL_SERVICE_WIRING, Type.TYPE_SERVICE);
    }

    @Override
    public Type getActorType(String id) {

        return queryById(BY_ID_QUERY_ACTOR_TYPE, id, Type.TYPE_ACTOR);
    }

    @Override
    public Type getServiceType(String id) {

        return queryById(BY_ID_QUERY_SERVICE_TYPE, id, Type.TYPE_FEED);
    }

    @Override
    public Type[] getActorTypes(String predicateQuery) throws RegistryQueryException {

        return queryUsingPredicate(PREDICATE_QUERY_ACTOR_TYPES, predicateQuery, Type.TYPE_ACTOR);
    }

    @Override
    public Type[] getServiceTypes(String predicateQuery) throws RegistryQueryException {

        return queryUsingPredicate(PREDICATE_QUERY_SERVICE_TYPES, predicateQuery, Type.TYPE_FEED);
    }

    @Override
    public Type getNodeType(String id) {

        return queryById(BY_ID_QUERY_NODE_TYPE, id, Type.TYPE_NODE);
    }

    @Override
    public Type[] getNodeTypes(String predicateQuery) throws RegistryQueryException {

        return queryUsingPredicate(PREDICATE_QUERY_NODE_TYPES, predicateQuery, Type.TYPE_NODE);
    }

    @Override
    public Type getPlatformType(String id) {

        return queryById(BY_ID_QUERY_PLATFORM_TYPE, id, Type.TYPE_PLATFORM);
    }

    @Override
    public Type[] getPlatformTypes(String predicateQuery) throws RegistryQueryException {

        return queryUsingPredicate(PREDICATE_QUERY_PLATFORM_TYPES, predicateQuery, Type.TYPE_PLATFORM);
    }

    @Override
    public Type getSystemType(String id) {

        return queryById(BY_ID_QUERY_SYSTEM_TYPE, id, Type.TYPE_SERVICE);
    }

    @Override
    public Type[] getSystemTypes(String predicateQuery) throws RegistryQueryException {

        return queryUsingPredicate(PREDICATE_QUERY_SYSTEM_TYPES, predicateQuery, Type.TYPE_SERVICE);
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.Factory#delete(fabric.registry.RegistryObject)
     */
    @Override
    public boolean delete(RegistryObject obj) {

        if (obj != null && obj instanceof Type) {
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

        if (obj != null && obj instanceof Type) {
            return super.save(obj, this);
        } else {
            return false;
        }
    }

    /**
     *
     * @param classifier
     * @return
     */
    private String getTableNameForUpdate(int classifier) {

        String tableSql = null;
        switch (classifier) {
            case Type.TYPE_ACTOR:
                tableSql = FabricRegistry.ACTOR_TYPES;
                break;
            case Type.TYPE_FEED:
                tableSql = FabricRegistry.FEED_TYPES;
                break;
            case Type.TYPE_NODE:
                tableSql = FabricRegistry.NODE_TYPES;
                break;
            case Type.TYPE_PLATFORM:
                tableSql = FabricRegistry.PLATFORM_TYPES;
                break;
            case Type.TYPE_SERVICE:
                tableSql = FabricRegistry.SYSTEM_TYPES;
                break;
            default:
                break;
        }
        return tableSql;
    }

    /**
     *
     * @param sql
     * @return
     */
    private Type[] queryAll(String sql, int classifier) {

        Type[] types = null;
        try {
            types = runQuery(sql, classifier);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return types;
    }

    /**
     *
     * @param sql
     * @param id
     * @return
     */
    private Type queryById(String sql, String id, int classifier) {

        Type type = null;
        try {
            String query = Fabric.format(sql, id);
            Type[] types = runQuery(query, classifier);
            if (types.length > 0) {
                type = types[0];
            }
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return type;
    }

    /**
     *
     * @param sql
     * @param predicates
     * @return
     * @throws RegistryQueryException
     */
    private Type[] queryUsingPredicate(String sql, String predicates, int classifier) throws RegistryQueryException {

        Type[] types = null;
        try {
            String query = Fabric.format(sql, predicates);
            types = runQuery(query, classifier);
        } catch (PersistenceException e) {
            e.printStackTrace();
            throw new RegistryQueryException(e.getMessage());
        }
        return types;
    }

    /**
     *
     * @param sql
     * @return
     * @throws PersistenceException
     */
    private Type[] runQuery(String sql, int classifier) throws PersistenceException {

        Type[] types = null;
        RegistryObject[] objects = queryRegistryObjects(sql, this);
        if (objects != null && objects.length > 0) {
            // necessary
            types = new Type[objects.length];
            for (int k = 0; k < objects.length; k++) {
                types[k] = (Type) objects[k];
                types[k].setClassifier(classifier);
            }
        } else {
            types = new Type[0];
        }
        return types;
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.Factory#insert(fabric.registry.RegistryObject)
     */
    @Override
    public boolean insert(RegistryObject obj) throws IncompleteObjectException, DuplicateKeyException,
        PersistenceException {

        if (obj != null && obj instanceof Type) {
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

        if (obj != null && obj instanceof Type) {
            return super.update(obj, this);
        } else {
            return false;
        }
    }
}
