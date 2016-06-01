/*
 * (C) Copyright IBM Corp. 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.jsonclient.articles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fabric.FabricBus;
import fabric.core.json.JSON;
import fabric.registry.DefaultConfig;
import fabric.registry.DefaultConfigFactory;
import fabric.registry.FabricRegistry;
import fabric.registry.NodeConfig;
import fabric.registry.NodeConfigFactory;
import fabric.registry.QueryScope;
import fabric.registry.exception.IncompleteObjectException;
import fabric.registry.exception.PersistenceException;
import fabric.services.jsonclient.utilities.AdapterConstants;

/**
 * Class that handles JSON commands that deal with sending SQL queries to the registry.
 */
public class Registry extends FabricBus {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    private static String homeNode = null;

    /**
     * Method that handles where to direct the delete query and returns its result
     * 
     * @param jsonObject
     * @return JSON result - JSON object with success result in key "sql-update-result"
     */
    public static JSON executeDeleteQuery(JSON jsonObject) {
        String key = jsonObject.getString(AdapterConstants.FIELD_SQL_KEY);
        String value = jsonObject.getString(AdapterConstants.FIELD_SQL_VALUE);
        if (jsonObject.getString(AdapterConstants.FIELD_SQL_TABLE).equals(AdapterConstants.TABLE_DEFAULT_CONFIG)) {
            return executeDefaultConfigDeleteQuery(key, value);
        } else {
            String node = jsonObject.getString(AdapterConstants.FIELD_SQL_NODE);
            return executeNodeConfigDeleteQuery(node, key, value);
        }
    }

    /**
     * Method that interprets an incoming JSON object from HTTP and executes the update SQL query on the local node. It
     * directs update statements to the relevant methods.
     * 
     * @param jsonObject
     * @return JSON result - JSON object with success result in key "sql-update-result"
     */
    public static JSON executeUpdateQuery(JSON jsonObject) {
        String key = jsonObject.getString(AdapterConstants.FIELD_SQL_KEY);
        String value = jsonObject.getString(AdapterConstants.FIELD_SQL_VALUE);
        if (jsonObject.getString(AdapterConstants.FIELD_SQL_TABLE).equals(AdapterConstants.TABLE_DEFAULT_CONFIG)) {
            return executeDefaultConfigUpdateQuery(key, value);
        } else {
            String node = jsonObject.getString(AdapterConstants.FIELD_SQL_NODE);
            return executeNodeConfigUpdateQuery(node, key, value);
        }
    }

    /**
     * Method that interprets an incoming JSON object from HTTP and executes the select query on the local node. It
     * directs the select query to the relevant method
     * 
     * @param jsonObject
     * @return JSON result - JSON object with column:row as key:value pairs
     */
    public static JSON executeSelectQuery(JSON jsonObject) {
        if (jsonObject.getString(AdapterConstants.FIELD_SQL_TABLE).equals(AdapterConstants.TABLE_DEFAULT_CONFIG)) {
            return executeDefaultConfigSelectQuery();
        } else {
            return executeNodeConfigSelectQuery();
        }
    }

    /**
     * Responsible for executing a select query to get all elements on the default_config table. Less complex than
     * node_config due to lack of multiple entries per id.
     * 
     * @return JSON result - JSON object with column:row as key:value pairs
     */
    public static JSON executeDefaultConfigSelectQuery() {
        JSON result = new JSON();
        List<JSON> list = new ArrayList<JSON>();
        DefaultConfig[] dcs = FabricRegistry.getDefaultConfigFactory().getAllDefaultConfig();
        for (DefaultConfig row : dcs) {
            JSON value = new JSON();
            value.putString(row.getName(), row.getValue());
            list.add(value);
            result.putArray("default_config", list);
        }
        return result;
    }

    /**
     * Responsible for executing a select query to get all elements on the node_config table
     * 
     * @return JSON result - JSON object with column:row as key:value pairs
     */
    public static JSON executeNodeConfigSelectQuery() {
        JSON result = new JSON();
        JSON nodes = new JSON();
        HashMap<String, List<JSON>> nodeMap = new HashMap<String, List<JSON>>();
        NodeConfig[] nfs = FabricRegistry.getNodeConfigFactory().getAllNodeConfig();

        for (NodeConfig row : nfs) {
            JSON value = new JSON();
            value.putString(row.getName(), row.getValue());
            if (!nodeMap.containsKey(row.getNode())) {
                List<JSON> list = new ArrayList<JSON>();
                list.add(value);
                nodeMap.put(row.getNode(), list);
            } else {
                nodeMap.get(row.getNode()).add(value);
            }
        }

        Iterator<?> it = nodeMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<JSON>> pairs = (Map.Entry<String, List<JSON>>) it.next();
            nodes.putArray(pairs.getKey(), pairs.getValue());
        }
        result.putJSON("node_config", nodes);
        return result;
    }

    /**
     * Responsible for executing a delete query on the default_config table
     * 
     * @param key
     *            The column where the data lies
     * @param value
     *            The row value to delete
     * @return JSON result - JSON object with success result in key "sql-update-result"
     */
    public static JSON executeDefaultConfigDeleteQuery(String key, String value) {
        JSON result = new JSON();
        Boolean success = false;

        DefaultConfigFactory dfc = FabricRegistry.getDefaultConfigFactory(QueryScope.LOCAL);
        DefaultConfig dc = dfc.getDefaultConfigByName(key);
        if (dc == null) {
            result.putString(AdapterConstants.FIELD_SQL_UPDATE_RESULT, success.toString());
        } else {
            success = dfc.delete(dc);
            result.putString(AdapterConstants.FIELD_SQL_UPDATE_RESULT, success.toString());
        }

        return result;
    }

    /**
     * Responsible for executing a delete query on the default_config table
     * 
     * @param node
     *            The relevant node to delete the data from
     * @param key
     *            The column where the data lies
     * @param value
     *            The row value to delete
     * @return JSON result - JSON object with success result in key "sql-update-result"
     */
    public static JSON executeNodeConfigDeleteQuery(String node, String key, String value) {
        JSON result = new JSON();
        Boolean success = false;

        NodeConfigFactory nfc = FabricRegistry.getNodeConfigFactory(QueryScope.LOCAL);
        NodeConfig nc = nfc.getNodeConfigByName(node, key);
        if (nc == null) {
            result.putString(AdapterConstants.FIELD_SQL_UPDATE_RESULT, success.toString());
        } else {
            success = nfc.delete(nc);
            result.putString(AdapterConstants.FIELD_SQL_UPDATE_RESULT, success.toString());
        }

        return result;
    }

    /**
     * Responsible for executing an update query on the default_config table via the registry API
     * 
     * @param node
     *            Name of the fabric node
     * @param key
     *            The column name
     * @param value
     *            The row element for that column
     * @return JSON result - JSON object with success result in key "sql-update-result"
     */
    public static JSON executeDefaultConfigUpdateQuery(String key, String value) {
        JSON result = new JSON();
        Boolean success = false;

        DefaultConfigFactory dfc = FabricRegistry.getDefaultConfigFactory();
        DefaultConfig dc = dfc.getDefaultConfigByName(key);
        if (dc != null) {
            try {
                dc.setValue(value);
                success = dfc.update(dc);
            } catch (IncompleteObjectException e) {
                e.printStackTrace();
            } catch (PersistenceException e) {
                e.printStackTrace();
            }
        } else {
            DefaultConfig newdc = dfc.createDefaultConfig(key, value);
            try {
                success = dfc.save(newdc);
            } catch (IncompleteObjectException e) {
                e.printStackTrace();
            }
        }
        result.putString(AdapterConstants.FIELD_SQL_UPDATE_RESULT, success.toString());

        return result;
    }

    /**
     * Responsible for executing an update query on the node_config table via the registry API
     * 
     * @param node
     *            Name of the fabric node
     * @param key
     *            The column name
     * @param value
     *            The row element for that column
     * @return JSON result - JSON object with success result in key "sql-update-result"
     */
    public static JSON executeNodeConfigUpdateQuery(String node, String key, String value) {
        JSON result = new JSON();
        Boolean success = false;

        NodeConfigFactory nfc = FabricRegistry.getNodeConfigFactory();
        NodeConfig nc = nfc.getNodeConfigByName(node, key);
        if (nc != null) {
            try {
                nc.setValue(value);
                success = nfc.update(nc);
            } catch (IncompleteObjectException e) {
                e.printStackTrace();
            } catch (PersistenceException e) {
                e.printStackTrace();
            }
        } else {
            NodeConfig newdc = nfc.createNodeConfig(node, key, value);
            try {
                success = nfc.save(newdc);
            } catch (IncompleteObjectException e) {
                e.printStackTrace();
            }
        }
        result.putString(AdapterConstants.FIELD_SQL_UPDATE_RESULT, success.toString());

        return result;
    }

    public static String getNode() {

        return homeNode;
    }

    public static void setNode(String homeNode) {

        Registry.homeNode = homeNode;
    }
}
