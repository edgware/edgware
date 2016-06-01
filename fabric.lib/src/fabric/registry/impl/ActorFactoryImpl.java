/*
 * (C) Copyright IBM Corp. 2009
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.Actor;
import fabric.registry.ActorFactory;
import fabric.registry.FabricRegistry;
import fabric.registry.QueryScope;
import fabric.registry.RegistryObject;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.exception.RegistryQueryException;
import fabric.registry.persistence.IPersistenceResultRow;

/**
 * Implementation of the factory for <code>Actor</code>s.
 */
public class ActorFactoryImpl extends AbstractFactory implements ActorFactory {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

    /* Query definitions */
    /** Select all records */
    private String SELECT_ALL_QUERY = null;
    /** Select a particular record by ID */
    private String BY_ID_QUERY = null;
    /** Select a particular record by type ID */
    private String BY_TYPE_QUERY = null;
    /** Select records using an arbitrary WHERE clause */
    private String PREDICATE_QUERY = null;

    /** Factory for remote (gaian) Registry operations */
    private static ActorFactoryImpl remoteQueryInstance = null;
    /** Factory for local (singleton) Registry operations */
    private static ActorFactoryImpl localQueryInstance = null;

    static {
        /* Create an instance for remote (gaian) Registry operations */
        remoteQueryInstance = new ActorFactoryImpl(QueryScope.DISTRIBUTED);
        /* Create an instance for local (singleton) Registry operations */
        localQueryInstance = new ActorFactoryImpl(QueryScope.LOCAL);
    }

    private ActorFactoryImpl(QueryScope queryScope) {
        this.queryScope = queryScope;

        SELECT_ALL_QUERY = format("select * from %s", FabricRegistry.ACTORS);
        BY_ID_QUERY = format("select * from %s where ACTOR_ID='\\%s'", FabricRegistry.ACTORS);
        BY_TYPE_QUERY = format("select * from %s where TYPE_ID='\\%s'", FabricRegistry.ACTORS);
        PREDICATE_QUERY = format("select * from %s where \\%s", FabricRegistry.ACTORS);

    }

    public static ActorFactoryImpl getInstance(QueryScope queryScope) {
        if (queryScope == QueryScope.LOCAL) {
            return localQueryInstance;
        } else {
            return remoteQueryInstance;
        }
    }

    @Override
    public RegistryObject create(IPersistenceResultRow row) throws PersistenceException {
        Actor client = null;
        if (row != null) {
            ActorImpl impl = new ActorImpl();
            impl.setId(row.getString(1));
            impl.setTypeId(row.getString(2));
            impl.setAffiliation(row.getString(3));
            impl.setRoles(row.getString(4));
            impl.setCredentials(row.getString(5));
            impl.setDescription(row.getString(6));
            impl.setAttributes(row.getString(7));
            impl.setAttributesUri(row.getString(8));

            /* preserve these values internally */
            impl.createShadow();

            client = impl;
        }
        return client;
    }

    @Override
    public String getDeleteSql(RegistryObject obj) {

        StringBuilder buf = new StringBuilder();
        if (obj instanceof Actor) {
            Actor client = (Actor) obj;
            buf.append("delete from " + FabricRegistry.ACTORS + " where(");
            buf.append("ACTOR_ID='").append(client.getId()).append("')");
        }
        return buf.toString();
    }

    @Override
    public String getInsertSql(RegistryObject obj) {
        StringBuilder buf = new StringBuilder();
        if (obj instanceof Actor) {
            Actor client = (Actor) obj;
            buf.append("insert into " + FabricRegistry.ACTORS + " values(");
            buf.append(nullOrString(client.getId())).append(',');
            buf.append(nullOrString(client.getTypeId())).append(',');
            buf.append(nullOrString(client.getAffiliation())).append(',');
            buf.append(nullOrString(client.getRoles())).append(',');
            buf.append(nullOrString(client.getCredentials())).append(',');
            buf.append(nullOrString(client.getDescription())).append(',');
            buf.append(nullOrString(client.getAttributes())).append(',');
            buf.append(nullOrString(client.getAttributesUri())).append(')');
        }
        return buf.toString();
    }

    @Override
    public String getUpdateSql(RegistryObject obj) {
        StringBuilder buf = new StringBuilder();
        if (obj instanceof Actor) {
            Actor client = (Actor) obj;
            buf.append("update " + FabricRegistry.ACTORS + " set ");
            buf.append("ACTOR_ID='").append(client.getId()).append('\'').append(',');
            buf.append("TYPE_ID=").append(nullOrString(client.getTypeId())).append(',');
            buf.append("ROLES=").append(nullOrString(client.getRoles())).append(',');
            buf.append("CREDENTIALS=").append(nullOrString(client.getCredentials())).append(',');
            buf.append("AFFILIATION=").append(nullOrString(client.getAffiliation())).append(',');
            buf.append("DESCRIPTION=").append(nullOrString(client.getDescription())).append(',');
            buf.append("ATTRIBUTES=").append(nullOrString(client.getAttributes())).append(',');
            buf.append("ATTRIBUTES_URI=").append(nullOrString(client.getAttributesUri()));
            buf.append(" WHERE ");

            /* if it exists, use the shadow values for the WHERE clause */
            if (client.getShadow() != null) {
                Actor originalClient = (Actor) client.getShadow();
                buf.append("ACTOR_ID='").append(originalClient.getId()).append("' AND ");
                buf.append("TYPE_ID='").append(originalClient.getTypeId()).append('\'');
            } else {
                buf.append("ACTOR_ID='").append(client.getId()).append("' AND ");
                buf.append("TYPE_ID='").append(client.getTypeId()).append('\'');
            }
        }
        return buf.toString();
    }

    /**
     * @see fabric.registry.ActorFactory#createActor(java.lang.String, java.lang.String)
     */
    @Override
    public Actor createActor(String id, String typeId) {
        return createActor(id, typeId, null, null, null, null, null, null);
    }

    /**
     * @see fabric.registry.ActorFactory#createActor(java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Actor createActor(String id, String typeId, String roles, String credentials, String affiliation,
            String description, String attributes, String attributesURI) {

        return new ActorImpl(id, typeId, roles, credentials, affiliation, description, attributes, attributesURI);
    }

    /**
     * @see fabric.registry.ActorFactory#getAllActors()
     */
    @Override
    public Actor[] getAllActors() {
        Actor[] clients = null;
        try {
            clients = runQuery(SELECT_ALL_QUERY);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return clients;
    }

    @Override
    public Actor getActorById(String id) {
        Actor client = null;
        try {
            String query = format(BY_ID_QUERY, id);
            Actor[] clients = runQuery(query);
            if (clients != null && clients.length > 0) {
                client = clients[0];
            }
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return client;
    }

    @Override
    public Actor[] getActors(String queryPredicates) throws RegistryQueryException {

        Actor[] clients = null;
        try {
            String query = format(PREDICATE_QUERY, queryPredicates);
            clients = runQuery(query);
        } catch (PersistenceException e) {
            e.printStackTrace();
            throw new RegistryQueryException("Invalid query: " + PREDICATE_QUERY + queryPredicates);
        }
        return clients;
    }

    private Actor[] runQuery(String sql) throws PersistenceException {
        Actor[] clients = null;
        RegistryObject[] objects = queryRegistryObjects(sql, this);
        if (objects != null && objects.length > 0) {
            // necessary
            clients = new Actor[objects.length];
            for (int k = 0; k < objects.length; k++) {
                clients[k] = (Actor) objects[k];
            }
        } else {
            clients = new Actor[0];
        }
        return clients;
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.Factory#delete(fabric.registry.RegistryObject)
     */
    @Override
    public boolean delete(RegistryObject obj) {
        if (obj != null && obj instanceof Actor) {
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
        if (obj != null && obj instanceof Actor) {
            return super.save(obj, this);
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.ClientFactory#getClientsByType(java.lang.String)
     */
    @Override
    public Actor[] getActorsByType(String typeId) {
        Actor[] clients = null;
        try {
            String query = format(BY_TYPE_QUERY, typeId);
            clients = runQuery(query);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return clients;
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.Factory#insert(fabric.registry.RegistryObject)
     */
    @Override
    public boolean insert(RegistryObject obj) throws IncompleteObjectException, DuplicateKeyException,
    PersistenceException {

        if (obj != null && obj instanceof Actor) {
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

        if (obj != null && obj instanceof Actor) {
            return super.update(obj, this);
        } else {
            return false;
        }
    }

}
