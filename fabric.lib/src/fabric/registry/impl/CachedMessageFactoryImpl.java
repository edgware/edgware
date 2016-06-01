/*
 * (C) Copyright IBM Corp. 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.CachedMessage;
import fabric.registry.CachedMessageFactory;
import fabric.registry.FabricRegistry;
import fabric.registry.QueryScope;
import fabric.registry.RegistryObject;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.persistence.IPersistenceResultRow;

public class CachedMessageFactoryImpl extends AbstractFactory implements CachedMessageFactory {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

    private static CachedMessageFactory localInstance = null;
    private static CachedMessageFactory remoteInstance = null;

    private String SELECT_ALL_QUERY = null;
    private String BY_SRC_DST_QUERY = null;

    public static CachedMessageFactory getInstance(QueryScope queryScope) {
        if (queryScope == QueryScope.LOCAL) {
            if (localInstance == null) {
                localInstance = new CachedMessageFactoryImpl(QueryScope.LOCAL);
            }
            return localInstance;
        } else {
            if (remoteInstance == null) {
                remoteInstance = new CachedMessageFactoryImpl(QueryScope.DISTRIBUTED);
            }
            return remoteInstance;
        }
    }

    private CachedMessageFactoryImpl(QueryScope queryScope) {
        this.queryScope = queryScope;
        SELECT_ALL_QUERY = format("select * from %s", FabricRegistry.MESSAGE_CACHE);
        BY_SRC_DST_QUERY = format("select * from %s where SOURCE like '\\%s' and DESTINATION like '\\%s'",
                FabricRegistry.MESSAGE_CACHE);
    }

    @Override
    public String getInsertSql(RegistryObject obj) {
        StringBuilder buf = new StringBuilder();
        if (obj instanceof CachedMessage) {
            CachedMessage msg = (CachedMessage) obj;
            buf.append("insert into " + FabricRegistry.MESSAGE_CACHE + " values(");
            String ts = (new java.sql.Timestamp(msg.getTimestamp())).toString();
            buf.append('\'').append(ts).append('\'').append(',');
            buf.append('\'').append(msg.getSource().replaceAll("'", "''")).append('\'').append(',');
            buf.append('\'').append(msg.getDestination().replaceAll("'", "''")).append('\'').append(',');
            buf.append('\'').append(msg.getMessage().replaceAll("'", "''")).append("')");
        }
        return buf.toString();
    }

    @Override
    public String getUpdateSql(RegistryObject obj) {
        throw new UnsupportedOperationException("Cannot update CachedMessage objects");
    }

    @Override
    public String getDeleteSql(RegistryObject obj) {
        StringBuilder buf = new StringBuilder();
        if (obj instanceof CachedMessage) {
            CachedMessage msg = (CachedMessage) obj;
            buf.append("delete from " + FabricRegistry.MESSAGE_CACHE + " where(");
            String ts = (new java.sql.Timestamp(msg.getTimestamp())).toString();
            buf.append("timestamp='").append(ts).append("' AND ");
            buf.append("source='").append(msg.getSource().replaceAll("'", "''")).append("' AND ");
            buf.append("destination='").append(msg.getDestination().replaceAll("'", "''")).append("')");
        }
        return buf.toString();
    }

    @Override
    public RegistryObject create(IPersistenceResultRow row) throws PersistenceException {
        CachedMessage msg = null;
        if (row != null) {
            CachedMessageImpl impl = new CachedMessageImpl();
            impl.setTimestamp(row.getTimestamp(1).getTime());
            impl.setSource(row.getString(2));
            impl.setDestination(row.getString(3));
            impl.setMessage(row.getString(4));

            impl.createShadow();

            msg = impl;
        }
        return msg;
    }

    @Override
    public boolean insert(RegistryObject obj) throws IncompleteObjectException, DuplicateKeyException,
        PersistenceException {
        if (obj != null && obj instanceof CachedMessage) {
            return super.insert(obj, this);
        } else {
            return false;
        }
    }

    @Override
    public boolean update(RegistryObject obj) throws IncompleteObjectException, PersistenceException {
        if (obj != null && obj instanceof CachedMessage) {
            return super.update(obj, this);
        } else {
            return false;
        }
    }

    @Override
    public boolean save(RegistryObject obj) throws IncompleteObjectException {
        System.out.println("CMFImpl.save:" + obj);
        if (obj != null && obj instanceof CachedMessage) {
            return super.save(obj, this);
        } else {
            return false;
        }
    }

    @Override
    public boolean delete(RegistryObject obj) {
        if (obj != null && obj instanceof CachedMessage) {
            return super.delete(obj, this);
        } else {
            return false;
        }
    }

    @Override
    public CachedMessage createCachedMessage(long timestamp, String source, String destination, String message) {
        return new CachedMessageImpl(timestamp, source, destination, message);
    }

    private CachedMessage[] runQuery(String sql) throws PersistenceException {
        CachedMessage[] msgs = null;
        RegistryObject[] objects = queryRegistryObjects(sql, this);
        if (objects != null && objects.length > 0) {
            // necessary
            msgs = new CachedMessage[objects.length];
            for (int k = 0; k < objects.length; k++) {
                msgs[k] = (CachedMessage) objects[k];
            }
        } else {
            msgs = new CachedMessage[0];
        }
        return msgs;
    }

    @Override
    public CachedMessage[] getAllMessages() {
        CachedMessage[] msgs = null;
        try {
            msgs = runQuery(SELECT_ALL_QUERY);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return msgs;
    }

    @Override
    public CachedMessage[] getMessages(String source, String destination) {
        CachedMessage[] msgs = null;
        if (source == null && destination == null) {
            msgs = getAllMessages();
        } else {
            if (source == null || source.equals("")) {
                source = "%";
            }
            if (destination == null || destination.equals("")) {
                destination = "%";
            }
            // Ensure safe SQL - escape all instances of '
            source = source.replaceAll("'", "''");
            destination = destination.replaceAll("'", "''");

            try {
                msgs = runQuery(format(BY_SRC_DST_QUERY, source, destination));
            } catch (PersistenceException e) {
                e.printStackTrace();
            }
        }
        return msgs;
    }

}
