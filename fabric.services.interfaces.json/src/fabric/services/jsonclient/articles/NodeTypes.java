/*
 * (C) Copyright IBM Corp. 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.jsonclient.articles;

import java.util.ArrayList;
import java.util.List;

import fabric.registry.FabricRegistry;
import fabric.registry.QueryScope;
import fabric.registry.Type;
import fabric.registry.TypeFactory;
import fabric.services.json.JSON;
import fabric.services.jsonclient.utilities.AdapterConstants;
import fabric.services.jsonclient.utilities.AdapterStatus;

/**
 * Class that handles JSON commands that deal with Node Types.
 */
public class NodeTypes extends Article {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    /**
     * Inserts a Node Type into the registry.
     *
     * @param op
     *            The full JSON operation object.
     *
     * @param correlId
     *            The correlation ID of the request.
     *
     * @return A JSON status object.
     */
    public static JSON register(JSON op, String correlId) {

        AdapterStatus status = new AdapterStatus(correlId);

        try {

            String typeid = op.getString(AdapterConstants.FIELD_TYPE);

            if (typeid == null) {

                status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_REGISTER,
                        AdapterConstants.ARTICLE_NODE_TYPE, AdapterConstants.STATUS_MSG_FIELD_ERROR, correlId);

            } else {

                JSON attr = op.getJSON(AdapterConstants.FIELD_ATTRIBUTES);
                String description = op.getString(AdapterConstants.FIELD_DESCRIPTION);

                TypeFactory typeFactory = FabricRegistry.getTypeFactory();
                Type type = typeFactory.createNodeType(typeid, description, (attr != null) ? attr.toString() : null,
                        null);
                typeFactory.save(type);

            }

        } catch (Exception e) {

            String message = e.getClass().getName() + ": " + e.getMessage();
            status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
                    AdapterConstants.ARTICLE_NODE_TYPE, message, correlId);

        }

        return status.toJsonObject();
    }

    /**
     * Deletes a Node from the registry.
     *
     * @param nodeTypeId
     *            The ID of the Node to be deleted.
     * @return A JSON status object.
     */
    public static JSON deregister(String nodeTypeId, String correlId) {

        AdapterStatus status = new AdapterStatus(correlId);
        if (nodeTypeId == null) {
            status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_DEREGISTER,
                    AdapterConstants.ARTICLE_NODE_TYPE, AdapterConstants.STATUS_MSG_FIELD_ERROR, correlId);
        } else {
            TypeFactory typeFactory = FabricRegistry.getTypeFactory(QueryScope.LOCAL);
            Type NodeType = typeFactory.getNodeType(nodeTypeId);
            boolean complete = typeFactory.delete(NodeType);
            if (complete == false) {
                status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_DEREGISTER,
                        AdapterConstants.ARTICLE_NODE_TYPE, AdapterConstants.STATUS_MSG_FAILED_DELETE, correlId);
            }
        }
        return status.toJsonObject();
    }

    /**
     * Queries the database for Node types.
     *
     * @param op
     * @param correlId
     * @return The result of the query.
     */
    public static JSON query(final JSON op, final String correlId) {

        JSON queryResult = new JSON();
        AdapterStatus status = new AdapterStatus(correlId);
        List<JSON> jsonList = new ArrayList<JSON>();

        try {

            TypeFactory typeFactory = FabricRegistry.getTypeFactory();

            String querySQL = generatePredicate(AdapterConstants.FIELD_TYPE, "TYPE_ID", QUERY_DESCRIPTION
                    | QUERY_ATTRIBUTES, op);

            if (querySQL == null) {

                status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_QUERY,
                        AdapterConstants.ARTICLE_NODE_TYPE, AdapterConstants.STATUS_MSG_BAD_SQL, correlId);
                queryResult = status.toJsonObject();

            } else {

                Type[] resultArray = null;

                if ("".equals(querySQL)) {
                    resultArray = typeFactory.getAllNodeTypes();
                } else {
                    resultArray = typeFactory.getNodeTypes(querySQL);
                }

                /* Generate the response object */

                queryResult.putString(AdapterConstants.FIELD_OPERATION, AdapterConstants.OP_QUERY_RESPONSE_NODE_TYPES);
                queryResult.putString(AdapterConstants.FIELD_CORRELATION_ID, correlId);

                /* For each node type... */
                for (int i = 0; i < resultArray.length; i++) {

                    JSON type = new JSON();
                    type = type.putString(AdapterConstants.FIELD_TYPE, resultArray[i].getId());
                    addOptionalFields(type, resultArray[i].getDescription(), resultArray[i].getAttributes());
                    jsonList.add(type);

                }
                queryResult = queryResult.putArray(AdapterConstants.FIELD_NODE_TYPES, jsonList);
            }

        } catch (Exception e) {

            String message = e.getClass().getName() + ": " + e.getMessage();
            status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_QUERY,
                    AdapterConstants.ARTICLE_NODE_TYPE, message, correlId);
            queryResult = status.toJsonObject();

        }
        return queryResult;
    }
}
