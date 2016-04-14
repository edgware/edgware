/*
 * (C) Copyright IBM Corp. 2009, 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import fabric.Fabric;
import fabric.registry.QueryScope;
import fabric.registry.RegistryObject;
import fabric.registry.exception.DuplicateKeyException;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.MalformedPredicateException;
import fabric.registry.exception.PersistenceException;
import fabric.registry.persistence.IPersistenceResultRow;
import fabric.registry.persistence.PersistenceManager;

/**
 */
public abstract class AbstractFactory extends Fabric {

    public AbstractFactory() {

        super(Logger.getLogger("fabric.registry"));
    }

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009, 2012";

    /* Indicates if queries are to be local or distributed */
    protected QueryScope queryScope = QueryScope.DISTRIBUTED;

    /*
     * Abstract methods
     */

    /**
     * Get the INSERT SQL statement which is particular to a given registry object.
     */
    public abstract String getInsertSql(RegistryObject obj);

    /**
     *
     * Get the UPDATE SQL statement which is particular to a given registry object.
     *
     * @param obj
     * @return
     */
    public abstract String getUpdateSql(RegistryObject obj);

    /**
     *
     * Get the DELETE SQL statement which is particular to a given registry object.
     *
     * @param obj
     * @return
     */
    public abstract String getDeleteSql(RegistryObject obj);

    /**
     *
     * @param rs
     * @return
     * @throws PersistenceException
     */
    public abstract RegistryObject create(IPersistenceResultRow row) throws PersistenceException;

    /**
     * Insert an object in the Registry.
     *
     * This method provides the default implementation for all factories.
     *
     * @param obj
     *            the object to insert.
     * @param factory
     *            the factory associated with the object
     * @return true if the operation succeeds, false otherwise
     * @throws IncompleteObjectException
     * @throws DuplicateKeyException
     * @throws PersistenceException
     */
    public boolean insert(RegistryObject obj, AbstractFactory factory) throws IncompleteObjectException,
    DuplicateKeyException, PersistenceException {

        if (obj != null && obj.isValid()) {
            try {
                logger.log(Level.FINEST, "Insert SQL: {0}", factory.getInsertSql(obj));
                boolean success = PersistenceManager.getPersistence().updateRegistryObject(factory.getInsertSql(obj));
                if (success) {
                    /* Update shadow since the object is now updated in the database */
                    ((AbstractRegistryObject) obj).createShadow();
                }
                return success;
            } catch (PersistenceException e) {
                if (e.getSqlState().equals("23505")) { // duplicate key - try an update instead
                    throw new DuplicateKeyException(e);
                } else {
                    throw e;
                }
            }
        } else {
            if (obj != null && !obj.isValid()) {
                logger.log(Level.FINEST, "Registry object is not valid and cannot be inserted: {0}", obj);
                /* call validate directly, which will throw exception that can be passed to application */
                obj.validate();
            }
            // flag as invalid
            return false;
        }
    }

    public boolean update(RegistryObject obj, AbstractFactory factory) throws IncompleteObjectException,
    PersistenceException {

        if (obj != null && obj.isValid()) {
            logger.log(Level.FINEST, "Update SQL: {0}", factory.getUpdateSql(obj));
            boolean success = PersistenceManager.getPersistence().updateRegistryObject(factory.getUpdateSql(obj));
            if (success) {
                /* Update shadow since the object is now updated in the database */
                ((AbstractRegistryObject) obj).createShadow();
            }
            return success;
        } else {
            if (obj != null && !obj.isValid()) {
                logger.log(Level.FINEST, "Registry object is not valid and cannot be updated: {0}", obj);
                /* call validate directly, which will throw exception that can be passed to application */
                obj.validate();
            }
            // flag as invalid
            return false;
        }
    }

    /**
     *
     * @param obj
     * @param factory
     * @return
     * @throws IncompleteObjectException
     */
    public boolean save(RegistryObject obj, AbstractFactory factory) throws IncompleteObjectException {

        if (obj != null && obj.isValid()) {
            try {
                boolean success = false;
                /* perform insert if no shadow */
                if (obj.getShadow() == null) {
                    try {
                        success = insert(obj, factory);
                    } catch (DuplicateKeyException e) {
                        logger.log(Level.FINEST, "Object already exists and will be updated instead [{0}]", obj);
                        success = update(obj, factory);
                    }
                } else {
                    /* update the Fabric Registry */
                    success = update(obj, factory);
                }
                return success;
            } catch (PersistenceException e) {
                logger.log(Level.WARNING, "Failed to save object: {1}\n{0}", new Object[] {obj, e.getMessage()});
                logger.log(Level.FINEST, "Full exception: ", e);
                return false;
            }
        } else {
            if (obj != null && !obj.isValid()) {
                logger.log(Level.FINE, "Registry object is not valid and will not be saved: {0}", obj);
                /* call validate directly, which will throw exception that can be passed to application */
                obj.validate();
            }
            // flag as invalid
            return false;
        }
    }

    public boolean delete(RegistryObject obj, AbstractFactory factory) {

        if (obj != null && obj.isValid()) {
            try {
                logger.log(Level.FINEST, "Delete SQL: {0}", factory.getDeleteSql(obj));
                return PersistenceManager.getPersistence().updateRegistryObject(factory.getDeleteSql(obj));
            } catch (PersistenceException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean delete(RegistryObject[] objects, AbstractFactory factory) {

        if (objects != null && objects.length > 0 && checkObjectsAreValid(objects)) {
            List<String> sqlDeletes = new ArrayList<String>();
            for (int i = 0; i < objects.length; i++) {
                sqlDeletes.add(factory.getDeleteSql(objects[i]));
            }
            try {
                PersistenceManager.getPersistence().updateRegistryObjects(sqlDeletes.toArray(new String[] {}));
                return true;
            } catch (PersistenceException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public RegistryObject[] queryRegistryObjects(String sqlString, AbstractFactory factory) throws PersistenceException {

        logger.log(Level.FINEST, "Query SQL: {0}", sqlString);
        RegistryObject[] results = null;
        if (sqlString != null && sqlString.length() > 0) {
            results = PersistenceManager.getPersistence().queryRegistryObjects(sqlString, factory, queryScope);
        }
        return results;
    }

    private boolean checkObjectsAreValid(RegistryObject[] objects) {

        boolean valid = true;
        for (int x = 0; x < objects.length; x++) {
            if (!objects[x].isValid()) {
                valid = false;
                break;
            }
        }
        return valid;
    }

    /**
     * Takes a given value and either returns null or a quoted string which is then included in the SQL. This method is
     * a workaround to prevent null values being inserted into the database as the string 'null' instead.
     *
     * @param value
     *            the String value to process
     * @return null or the value enclosed in single quotes (e.g. 'value')
     */
    protected String nullOrString(String value) {

        if (value == null) {
            return null;
        } else {
            StringBuffer buffer = new StringBuffer();
            buffer.append("'").append(value).append("'");
            return buffer.toString();
        }
    }

    /**
     * Takes a String in the following format and converts to a Map.
     *
     * Example:
     *
     * name1=value1&name2=value&name3=value3
     *
     * @param delimitedString
     * @return
     */
    public static Map<String, String> buildAttributesMap(String delimitedString) {

        HashMap<String, String> map = null;
        if (delimitedString != null) {
            map = new HashMap<String, String>();
            int counter = 0;
            StringTokenizer st = new StringTokenizer(delimitedString, "&");
            String nameValuePair = null;
            if (st.countTokens() == 1) { // either a single name/value pair or an invalid string
                counter = processNameValuePair(map, delimitedString, counter);
            } else {
                while (st.hasMoreTokens()) {
                    nameValuePair = st.nextToken();
                    counter = processNameValuePair(map, nameValuePair, counter);
                }
            }

        }

        return map;
    }

    private static int processNameValuePair(HashMap<String, String> map, String nameValuePair, int counter) {

        String[] parts = nameValuePair.split("=");
        if (parts.length == 1) { // store it as a key with a blank value
            map.put(parts[0], null);
        } else if (parts.length == 2) {
            map.put(parts[0], parts[1]);
        } else { // split at first equals and make the rest the value
            int indexOfEquals = nameValuePair.indexOf("=");
            map.put(nameValuePair.substring(0, indexOfEquals), nameValuePair.substring(indexOfEquals + 1, nameValuePair
                    .length()));
            System.out.println("key: " + nameValuePair.substring(0, indexOfEquals));
            System.out.println("value: " + nameValuePair.substring(indexOfEquals + 1, nameValuePair.length()));
        }
        return counter;
    }

    /**
     * Take a map of strings and convert it to a single string of the form:
     *
     * key1=value1&key2=value2...
     *
     * @param String
     *            the delimited string representing the Map contents.
     * @return
     */
    public static String convertMapToNVPString(Map<String, String> map) {

        String delimitedString = null;
        if (map != null) {
            StringBuffer buffy = new StringBuffer();
            Iterator<String> keys = map.keySet().iterator();
            String key = null;
            while (keys.hasNext()) {
                key = keys.next();
                buffy.append(key);
                buffy.append("=");
                buffy.append(map.get(key));
                if (keys.hasNext()) {
                    buffy.append("&");
                }
            }
            delimitedString = buffy.toString();
        }

        return delimitedString;
    }

    /**
     * Parse a query predicate, checking syntax etc.
     *
     * @param queryPredicate
     *            the predicate to parse.
     * @throws MalformedPredicateException
     *             if the predicate fails parsing.
     */
    public String parsePredicate(String queryPredicate) throws MalformedPredicateException {

        /* predicates cannot be null or empty strings */
        if (queryPredicate == null || queryPredicate.length() == 0) {
            throw new MalformedPredicateException("No predicate specified: '" + queryPredicate + "'");
        }

        /*
         * existance of an asterisk is likely either because the user wanted % or is trying something not valid in a SQL
         * predicate
         */
        if (queryPredicate.indexOf("*") != -1) {
            throw new MalformedPredicateException(
                    "Invalid token, '*', encountered in predicate. Hint: use '%' as a wildcard for SQL 'LIKE' clauses.");
        }

        /* check for double quotes and convert if necessary */
        if (queryPredicate.indexOf("\"") != -1) {
            logger.log(
                    Level.WARNING,
                    "Predicate ({0}) contains double-quotes (\") which are not supported - converting all occurences to single quotes (').",
                    queryPredicate);
            queryPredicate = queryPredicate.replaceAll("\"", "'");
            logger.log(Level.WARNING, "Converted predicate: ({0})", queryPredicate);
        }

        return queryPredicate;
    }

}