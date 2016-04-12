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

import fabric.Fabric;
import fabric.registry.FabricRegistry;
import fabric.registry.Node;
import fabric.registry.NodeFactory;
import fabric.registry.NodeIpMapping;
import fabric.registry.NodeIpMappingFactory;
import fabric.registry.NodeNeighbour;
import fabric.registry.NodeNeighbourFactory;
import fabric.registry.QueryScope;
import fabric.services.json.JSON;
import fabric.services.json.JSONArray;
import fabric.services.jsonclient.utilities.AdapterConstants;
import fabric.services.jsonclient.utilities.AdapterStatus;

/**
 * Class that handles JSON commands that deal with Nodes.
 */
public class Nodes extends Article {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    private static String homeNode = null;

    /*
     * Class methods
     */

    /**
     * Inserts a Node into the registry.
     *
     * @param op
     *            the full JSON operation object.
     *
     * @param correlId
     *            the operation correlation ID.
     *
     * @return the JSON message with the status of the operation.
     */

    public static JSON register(JSON op, String correlId) {

        AdapterStatus status = new AdapterStatus(correlId);

        try {

            String id = op.getString(AdapterConstants.FIELD_ID);
            JSONArray interfaces = op.getJSONArray(AdapterConstants.FIELD_INTERFACES);
            String typeId = op.getString(AdapterConstants.FIELD_TYPE);

            if (id == null || interfaces == null) {

                status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_REGISTER,
                        AdapterConstants.ARTICLE_NODE, AdapterConstants.STATUS_MSG_FIELD_ERROR, correlId);

            } else {

                if (typeId == null) {
                    typeId = AdapterConstants.FIELD_VALUE_UKNOWN;
                }

                String description = op.getString(AdapterConstants.FIELD_DESCRIPTION);
                JSON attr = op.getJSON(AdapterConstants.FIELD_ATTRIBUTES);

                NodeFactory nodeFactory = FabricRegistry.getNodeFactory();
                Node node = nodeFactory.createNode(id, typeId, // Node type
                        null, // affiliation
                        null, // Security Classification
                        null, // readiness
                        null, // availability,
                        0, // latitude,
                        0, // longitude,
                        0, // altitude,
                        0, // bearing,
                        0, // velocity,
                        description, (attr != null) ? attr.toString() : null, null);
                boolean success = nodeFactory.save(node);

                if (!success) {

                    status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
                            AdapterConstants.ARTICLE_NODE, "Insert/update of node into the Registry failed", correlId);

                } else {

                    /* Map the Node to its network interfaces */

                    for (Iterator<JSON> interfacesIterator = interfaces.iterator(); interfacesIterator.hasNext()
                            && success;) {

                        JSON nextInterface = interfacesIterator.next();

                        String address = nextInterface.getString(AdapterConstants.FIELD_ADDRESS).substring(5);
                        String[] ipParts = address.split(":");
                        String ipAddress = ipParts[0];
                        int port = Integer.parseInt(ipParts[1]);

                        String nodeInterface = nextInterface.getString(AdapterConstants.FIELD_NODE_INTERFACE);

                        NodeIpMappingFactory ipFactory = FabricRegistry.getNodeIpMappingFactory();
                        NodeIpMapping ip = ipFactory.createNodeIpMapping(id, nodeInterface, ipAddress, port);
                        success = ipFactory.save(ip);

                        if (!success) {
                            status = new AdapterStatus(AdapterConstants.ERROR_ACTION,
                                    AdapterConstants.OP_CODE_REGISTER, AdapterConstants.ARTICLE_NODE,
                                    "Insert/update of node/IP address mapping into the Registry failed", correlId);
                        }
                    }
                }
            }

        } catch (Exception e) {

            String message = e.getClass().getName() + ": " + e.getMessage();
            status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
                    AdapterConstants.ARTICLE_NODE, message, correlId);

        }

        return status.toJsonObject();
    }

    public static String getNode() {

        return homeNode;
    }

    public static void setNode(String homeNode) {

        Nodes.homeNode = homeNode;
    }

    /**
     * Deletes a Node from the registry.
     *
     * @param nodeId
     *            The ID of the node to be deleted.
     * @param correlId
     *            The correlation ID for the status message.
     * @return the JSON message with the status of the operation.
     */
    public static JSON deregister(String nodeId, String correlId) {

        AdapterStatus status = new AdapterStatus(correlId);

        if (nodeId == null) {
            status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_DEREGISTER,
                    AdapterConstants.ARTICLE_NODE, AdapterConstants.STATUS_MSG_FIELD_ERROR, correlId);
        }

        /* Delete the node */
        NodeFactory nodeFactory = FabricRegistry.getNodeFactory(QueryScope.LOCAL);
        Node node = nodeFactory.getNodeById(nodeId);
        boolean success = nodeFactory.delete(node);

        if (success == false) {
            status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_DEREGISTER,
                    AdapterConstants.ARTICLE_NODE, AdapterConstants.STATUS_MSG_FAILED_DELETE, correlId);
        } else {

            /* Delete the node's network interfaces */
            NodeIpMappingFactory ipMappingFactory = FabricRegistry.getNodeIpMappingFactory(QueryScope.LOCAL);
            NodeIpMapping[] ipMappingsForNode = ipMappingFactory.getAllMappingsForNode(nodeId);
            for (int ipm = 0; ipm < ipMappingsForNode.length && success; ipm++) {

                success = ipMappingFactory.delete(ipMappingsForNode[ipm]);

                if (success == false) {
                    status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_DEREGISTER,
                            AdapterConstants.ARTICLE_NODE, AdapterConstants.STATUS_MSG_FAILED_DELETE, correlId);
                }
            }
        }

        return status.toJsonObject();
    }

    /**
     * Returns the local node ID
     *
     * @param correlId
     * @return the JSON message with the local node ID.
     */

    public static JSON queryLocalNode(final String correlId) {

        String nodeID = getNode();
        JSON nodeIDQueryResult = new JSON();
        nodeIDQueryResult.putString(AdapterConstants.FIELD_OPERATION, AdapterConstants.OP_QUERY_RESPONSE_LOCAL_NODE);
        nodeIDQueryResult.putString(AdapterConstants.FIELD_CORRELATION_ID, correlId);
        nodeIDQueryResult.putString(AdapterConstants.FIELD_ID, nodeID);
        return nodeIDQueryResult;

    }

    /**
     * Queries the database for nodes.
     *
     * @param op
     * @param correlId
     * @return the JSON message with the query result.
     */
    public static JSON query(final JSON op, final String correlId) {

        JSON queryResult = new JSON();
        AdapterStatus status = new AdapterStatus(correlId);
        List<JSON> jsonList = new ArrayList<JSON>();
        String address = op.getString(AdapterConstants.FIELD_ADDRESS);

        try {

            NodeFactory nodeFactory = FabricRegistry.getNodeFactory();
            String querySQL = generatePredicate(AdapterConstants.FIELD_ID, "NODE_ID", QUERY_ATTRIBUTES, op);

            if (querySQL == null) {

                status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_QUERY,
                        AdapterConstants.ARTICLE_NODE, AdapterConstants.STATUS_MSG_BAD_SQL, correlId);
                queryResult = status.toJsonObject();

            } else {

                /* Lookup the node list in the Registry */
                Node[] resultArray = null;

                if ("".equals(querySQL)) {
                    resultArray = nodeFactory.getAllNodes();
                } else {
                    resultArray = nodeFactory.getNodes(querySQL);
                }

                /* Generate the response object */

                queryResult.putString(AdapterConstants.FIELD_OPERATION, AdapterConstants.OP_QUERY_RESPONSE_NODES);
                queryResult.putString(AdapterConstants.FIELD_CORRELATION_ID, correlId);

                NodeIpMapping[] nodeIPMappings = null;

                /* For each Node... */
                for (int i = 0; i < resultArray.length; i++) {

                    /* Build the JSON describing the node */

                    JSON node = new JSON();
                    node.putString(AdapterConstants.FIELD_ID, resultArray[i].getId());

                    /* Add the node's IP addresse(s) */

                    nodeIPMappings = resultArray[i].getAllIpMappings();
                    JSONArray nodeAddresses = new JSONArray();

                    for (int j = 0; j < nodeIPMappings.length; j++) {

                        JSON nextInterface = new JSON();

                        String interfaceName = nodeIPMappings[j].getNodeInterface();
                        nextInterface.putString(AdapterConstants.FIELD_NODE_INTERFACE, interfaceName);

                        /* Generate a URI of the form "ip://<address>:<port>" */
                        String ipMapping = String.format("ip://%s:%s", nodeIPMappings[j].getIpAddress(),
                                nodeIPMappings[j].getPort());
                        nextInterface.putString(AdapterConstants.FIELD_ADDRESS, ipMapping);

                        nodeAddresses.add(nextInterface);

                    }

                    /* Filter results on the specified address (if included in the request) */
                    if (address == null || nodeAddresses.toString().contains(address)) {
                        node.putJSONArray(AdapterConstants.FIELD_INTERFACES, nodeAddresses);
                        addOptionalFields(node, resultArray[i].getDescription(), resultArray[i].getAttributes());
                        jsonList.add(node);
                    }
                }

                queryResult = queryResult.putArray(AdapterConstants.FIELD_NODES, jsonList);
            }

        } catch (Exception e) {

            String message = e.getClass().getName() + ": " + e.getMessage();
            status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_QUERY,
                    AdapterConstants.ARTICLE_NODE, message, correlId);
            queryResult = status.toJsonObject();

        }

        return queryResult;
    }

    /**
     * Queries the database for node neighbours.
     *
     * @param op
     * @param correlId
     * @return the JSON message with the query result.
     */
    public static JSON queryNeighbours(final JSON op, final String correlId) {

        JSON neighbourQueryResult = new JSON();
        AdapterStatus status = new AdapterStatus(correlId);

        try {

            String target = op.getString("target");
            QueryScope queryScope = (target != null && target.equals("local")) ? QueryScope.LOCAL
                    : QueryScope.DISTRIBUTED;
            NodeNeighbourFactory neighbourFactory = FabricRegistry.getNodeNeighbourFactory(queryScope);
            String querySQL = generatePredicate(AdapterConstants.FIELD_ID, "NODE_ID", QUERY_NONE, op);

            if (querySQL == null) {

                status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_QUERY,
                        AdapterConstants.ARTICLE_NEIGHBOURS, AdapterConstants.STATUS_MSG_BAD_SQL, correlId);
                neighbourQueryResult = status.toJsonObject();

            } else {

                /* Lookup the neighbour list in the Registry */
                NodeNeighbour[] resultArray = null;

                if ("".equals(querySQL)) {
                    resultArray = neighbourFactory.getAllNeighbours();
                } else {
                    resultArray = neighbourFactory.getNeighbours(querySQL);
                }

                /* Generate the response object */

                neighbourQueryResult.putString(AdapterConstants.FIELD_OPERATION,
                        AdapterConstants.OP_QUERY_RESPONSE_NEIGHBOURS);
                neighbourQueryResult.putString(AdapterConstants.FIELD_CORRELATION_ID, correlId);

                HashMap<String, JSONArray> neighbourRecordsMap = new HashMap<String, JSONArray>();
                Fabric fabric = new Fabric();

                /* For each neighbour record... */
                for (int i = 0; i < resultArray.length; i++) {

                    /* Get the list of neighbour records for the node */
                    String nodeID = resultArray[i].getNodeId();
                    JSONArray neighbourRecordsList = neighbourRecordsMap.get(nodeID);
                    if (neighbourRecordsList == null) {
                        neighbourRecordsList = new JSONArray();
                        neighbourRecordsMap.put(nodeID, neighbourRecordsList);
                    }

                    /* Create a new neighbour record */
                    JSON neighbour = new JSON();
                    neighbour.putString(AdapterConstants.FIELD_NEIGHBOUR, resultArray[i].getNeighbourId());
                    neighbour.putString(AdapterConstants.FIELD_NEIGHBOUR_INTERFACE, resultArray[i]
                            .getNeighbourInterface());
                    neighbour.putString(AdapterConstants.FIELD_NODE_INTERFACE, resultArray[i].getNodeInterface());

                    neighbourRecordsList.add(neighbour);
                }

                JSONArray allNeighbourRecords = new JSONArray();

                /* For each node... */
                for (String nodeID : neighbourRecordsMap.keySet()) {

                    JSON nodeRecord = new JSON();
                    nodeRecord.putString(AdapterConstants.FIELD_ID, nodeID);
                    nodeRecord.putJSONArray(AdapterConstants.FIELD_NEIGHBOURS, neighbourRecordsMap.get(nodeID));
                    allNeighbourRecords.add(nodeRecord);

                }
                neighbourQueryResult.putJSONArray(AdapterConstants.FIELD_NODES, allNeighbourRecords);
            }

        } catch (Exception e) {

            String message = e.getClass().getName() + ": " + e.getMessage();
            status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_QUERY,
                    AdapterConstants.ARTICLE_NODE, message, correlId);
            neighbourQueryResult = status.toJsonObject();

        }

        return neighbourQueryResult;
    }
}
