/*
 * (C) Copyright IBM Corp. 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.jsonclient.articles;

import java.util.ArrayList;
import java.util.List;

import fabric.core.json.JSON;
import fabric.registry.Bearer;
import fabric.registry.BearerFactory;
import fabric.registry.FabricRegistry;
import fabric.registry.QueryScope;
import fabric.services.jsonclient.utilities.AdapterConstants;
import fabric.services.jsonclient.utilities.AdapterStatus;

/**
 * Class that handles JSON commands that deal with Nodes.
 */
public class Bearers extends Article {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    /*
     * Class methods
     */

    /**
     * Inserts a Bearer into the registry.
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
            String available = op.getString(AdapterConstants.FIELD_AVAILABILE).toLowerCase();

            if (id == null || id.length() == 0 || available == null
                    || (!available.equals("true") && !available.equals("false"))) {

                status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_REGISTER,
                        AdapterConstants.ARTICLE_BEARER, AdapterConstants.STATUS_MSG_FIELD_ERROR, correlId);

            } else {

                String description = op.getString(AdapterConstants.FIELD_DESCRIPTION);
                JSON attr = op.getJSON(AdapterConstants.FIELD_ATTRIBUTES);

                BearerFactory bearerFactory = FabricRegistry.getBearerFactory();
                Bearer bearer = bearerFactory.createBearer(id, available, description, (attr != null) ? attr.toString()
                        : null, null);
                boolean success = bearerFactory.save(bearer);

                if (!success) {

                    status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
                            AdapterConstants.ARTICLE_BEARER, "Insert/update of bearer into the Registry failed",
                            correlId);
                }
            }

        } catch (Exception e) {

            String message = e.getClass().getName() + ": " + e.getMessage();
            status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
                    AdapterConstants.ARTICLE_BEARER, message, correlId);

        }

        return status.toJsonObject();
    }

    /**
     * Deletes a Bearer from the registry.
     *
     * @param bearerID
     *            The ID of the node to be deleted.
     * @param correlId
     *            The correlation ID for the status message.
     * @return the JSON message with the status of the operation.
     */
    public static JSON deregister(String bearerID, String correlId) {

        AdapterStatus status = new AdapterStatus(correlId);

        if (bearerID == null) {

            status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_DEREGISTER,
                    AdapterConstants.ARTICLE_BEARER, AdapterConstants.STATUS_MSG_FIELD_ERROR, correlId);

        }

        BearerFactory bearerFactory = FabricRegistry.getBearerFactory(QueryScope.LOCAL);
        Bearer bearer = bearerFactory.getBearerById(bearerID);
        boolean success = bearerFactory.delete(bearer);

        if (success == false) {
            status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_DEREGISTER,
                    AdapterConstants.ARTICLE_BEARER, AdapterConstants.STATUS_MSG_FAILED_DELETE, correlId);
        }

        return status.toJsonObject();
    }

    /**
     * Queries the database for bearers.
     *
     * @param op
     * @param correlId
     * @return the JSON message with the query result.
     */
    public static JSON query(final JSON op, final String correlId) {

        JSON queryResult = new JSON();
        AdapterStatus status = new AdapterStatus(correlId);
        List<JSON> jsonList = new ArrayList<JSON>();

        try {

            BearerFactory bearerFactory = FabricRegistry.getBearerFactory();
            String querySQL = generatePredicate(AdapterConstants.FIELD_ID, "BEARER_ID", QUERY_AVAILABLE
                    | QUERY_ATTRIBUTES, op);

            if (querySQL == null) {

                status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_QUERY,
                        AdapterConstants.ARTICLE_BEARER, AdapterConstants.STATUS_MSG_BAD_SQL, correlId);
                queryResult = status.toJsonObject();

            } else {

                /* Lookup the bearer list in the Registry */
                Bearer[] resultArray = null;

                if ("".equals(querySQL)) {
                    resultArray = bearerFactory.getAllBearers();
                } else {
                    resultArray = bearerFactory.getBearers(querySQL);
                }

                /* Generate the response object */

                queryResult.putString(AdapterConstants.FIELD_OPERATION, AdapterConstants.OP_QUERY_RESPONSE_BEARERS);
                queryResult.putString(AdapterConstants.FIELD_CORRELATION_ID, correlId);

                /* For each bearer... */
                for (int i = 0; i < resultArray.length; i++) {

                    JSON bearer = new JSON();
                    bearer.putString(AdapterConstants.FIELD_ID, resultArray[i].getId());
                    bearer.putString(AdapterConstants.FIELD_AVAILABILE, resultArray[i].getAvailable());
                    addOptionalFields(bearer, resultArray[i].getDescription(), resultArray[i].getAttributes());
                    jsonList.add(bearer);

                }
                queryResult = queryResult.putArray(AdapterConstants.FIELD_BEARERS, jsonList);
            }

        } catch (Exception e) {

            String message = e.getClass().getName() + ": " + e.getMessage();
            status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_QUERY,
                    AdapterConstants.ARTICLE_BEARER, message, correlId);
            queryResult = status.toJsonObject();

        }

        return queryResult;
    }
}
