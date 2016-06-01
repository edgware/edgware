/*
 * (C) Copyright IBM Corp. 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.DefaultConfig;
import fabric.registry.DefaultConfigFactory;
import fabric.registry.FabricRegistry;
import fabric.registry.QueryScope;
import fabric.registry.RegistryObject;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.exception.RegistryQueryException;
import fabric.registry.persistence.IPersistenceResultRow;

/**
 * Implementation of the factory for <code>DefaultConfig</code>s.
 */
public class DefaultConfigFactoryImpl extends AbstractFactory implements DefaultConfigFactory {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    /** Factory for centralised (singleton) Registry operations */
    private static DefaultConfigFactoryImpl localQueryInstance = null;

    /** Factory for distributed (gaian) Registry operations */
    private static DefaultConfigFactoryImpl remoteQueryInstance = null;

    /*
     * Queries
     */
    /** Select all records */
    private String SELECT_ALL_QUERY = null;

    /** Select records by name */
    private String BY_NAME_QUERY = null;

    /** Select records using an arbitrary WHERE clause */
    private String PREDICATE_QUERY = null;

    static {
        localQueryInstance = new DefaultConfigFactoryImpl(QueryScope.LOCAL);
        remoteQueryInstance = new DefaultConfigFactoryImpl(QueryScope.DISTRIBUTED);
    }

    public static DefaultConfigFactory getInstance(QueryScope queryScope) {

        if (queryScope == QueryScope.LOCAL) {
            return localQueryInstance;
        } else {
            return remoteQueryInstance;
        }
    }

    private DefaultConfigFactoryImpl(QueryScope queryScope) {

        this.queryScope = queryScope;

        SELECT_ALL_QUERY = format("select * from %s", FabricRegistry.DEFAULT_CONFIG);
        BY_NAME_QUERY = format("select * from %s where NAME='\\%s'", FabricRegistry.DEFAULT_CONFIG);
        PREDICATE_QUERY = format("select * from %s where \\%s", FabricRegistry.DEFAULT_CONFIG);
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.impl.AbstractFactory#create(java.sql.ResultSet)
     */
    @Override
    public RegistryObject create(IPersistenceResultRow row) throws PersistenceException {

        DefaultConfig dc = null;

        if (row != null) {

            DefaultConfigImpl dcImpl = new DefaultConfigImpl();
            dcImpl.setName(row.getString(1));
            dcImpl.setValue(row.getString(2));

            /* preserve these values internally */
            dcImpl.createShadow();

            dc = dcImpl;
        }

        return dc;
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.impl.AbstractFactory#getDeleteSql(fabric.registry.RegistryObject)
     */
    @Override
    public String getDeleteSql(RegistryObject obj) {

        StringBuilder buf = new StringBuilder();

        if (obj instanceof DefaultConfig) {

            DefaultConfig dc = (DefaultConfig) obj;

            buf.append("delete from " + FabricRegistry.DEFAULT_CONFIG + " where(");
            buf.append("NAME='").append(dc.getName()).append("')");
        }

        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.impl.AbstractFactory#getInsertSql(fabric.registry.RegistryObject)
     */
    @Override
    public String getInsertSql(RegistryObject obj) {

        StringBuilder buf = new StringBuilder();

        if (obj instanceof DefaultConfig) {

            DefaultConfig dc = (DefaultConfig) obj;

            buf.append("insert into " + FabricRegistry.DEFAULT_CONFIG + "  values(");
            buf.append('\'').append(dc.getName()).append('\'').append(',');
            buf.append('\'').append(dc.getValue()).append("')");
        }

        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.impl.AbstractFactory#getUpdateSql(fabric.registry.RegistryObject)
     */
    @Override
    public String getUpdateSql(RegistryObject obj) {

        StringBuilder buf = new StringBuilder();

        if (obj instanceof DefaultConfig) {

            DefaultConfig dc = (DefaultConfig) obj;

            buf.append("update " + FabricRegistry.DEFAULT_CONFIG + " set ");
            buf.append("VALUE='").append(dc.getValue()).append('\'');
            buf.append(" WHERE ");

            /* If it exists, use the shadow values for the WHERE clause */
            if (dc.getShadow() != null) {

                DefaultConfig shadow = (DefaultConfig) dc.getShadow();
                buf.append("NAME='").append(shadow.getName()).append('\'');

            } else {

                buf.append("NAME='").append(dc.getName()).append('\'');

            }
        }

        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.DefaultConfigFactory#createDefaultConfig(java.lang.String, java.lang.String)
     */
    @Override
    public DefaultConfig createDefaultConfig(String name, String value) {

        return new DefaultConfigImpl(name, value);
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.DefaultConfigFactory#getAllDefaultConfig()
     */
    @Override
    public DefaultConfig[] getAllDefaultConfig() {

        DefaultConfig[] dc = null;

        try {
            dc = runQuery(SELECT_ALL_QUERY);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }

        return dc;
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.DefaultConfigFactory#getDefaultConfigByName(java.lang.String)
     */
    @Override
    public DefaultConfig getDefaultConfigByName(String name) {

        DefaultConfig dc = null;

        try {

            String query = format(BY_NAME_QUERY, name);
            DefaultConfig[] dcs = runQuery(query);

            if (dcs != null && dcs.length > 0) {
                dc = dcs[0];
            }

        } catch (PersistenceException e) {
            e.printStackTrace();
        }

        return dc;
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.DefaultConfigFactory#getDefaultConfig(java.lang.String)
     */
    @Override
    public DefaultConfig[] getDefaultConfig(String predicate) throws RegistryQueryException {

        DefaultConfig[] dcs = null;

        try {

            String query = format(PREDICATE_QUERY, predicate);
            dcs = runQuery(query);

        } catch (PersistenceException e) {
            e.printStackTrace();
        }

        return dcs;
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.Factory#delete(fabric.registry.RegistryObject)
     */
    @Override
    public boolean delete(RegistryObject obj) {

        if (obj != null && obj instanceof DefaultConfig) {
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

        if (obj != null && obj instanceof DefaultConfig) {
            return super.save(obj, this);
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see fabric.registry.Factory#insert(fabric.registry.RegistryObject)
     */
    @Override
    public boolean insert(RegistryObject obj) throws IncompleteObjectException, DuplicateKeyException,
        PersistenceException {

        if (obj != null && obj instanceof DefaultConfig) {
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

        if (obj != null && obj instanceof DefaultConfig) {
            return super.update(obj, this);
        } else {
            return false;
        }
    }

    /**
     * Run the specified SQL query and return the results as an array.
     *
     * @param sql
     *            the query.
     *
     * @return the result array.
     *
     * @throws PersistenceException
     */
    private DefaultConfig[] runQuery(String sql) throws PersistenceException {

        DefaultConfig[] dcs = null;

        RegistryObject[] objects = queryRegistryObjects(sql, this);

        if (objects != null && objects.length > 0) {

            /* Necessary */

            dcs = new DefaultConfig[objects.length];

            for (int k = 0; k < objects.length; k++) {
                dcs[k] = (DefaultConfig) objects[k];
            }

        } else {

            dcs = new DefaultConfig[0];

        }

        return dcs;
    }
}
