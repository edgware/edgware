/*
 * (C) Copyright IBM Corp. 2014, 2016
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.jsonclient.articles;

import java.util.ArrayList;
import java.util.List;

import fabric.core.json.JSON;
import fabric.registry.FabricRegistry;
import fabric.registry.SystemWiring;
import fabric.registry.SystemWiringFactory;
import fabric.services.jsonclient.utilities.AdapterConstants;
import fabric.services.jsonclient.utilities.AdapterStatus;

/**
 * Class that handles JSON operations that deal with Systems.
 */
public class ServiceWiring extends Article {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014, 2016";

    /**
     * Returns the result of a Systems query in a JSON Object.
     *
     * @param op
     *            The full JSON operation object.
     *
     * @param correlId
     *            The correlation ID of the request.
     *
     * @return the query result JSON Object.
     */
    public static JSON query(final JSON jsonOpObject, final String correlId) {

        JSON queryResult = new JSON();
        AdapterStatus status = new AdapterStatus(correlId);
        List<JSON> serviceWiringList = new ArrayList<JSON>();

        try {

            SystemWiringFactory systemWiringFactory = FabricRegistry.getSystemWiringFactory();
            String querySQL = generateWiringPredicate(jsonOpObject);

            if (querySQL == null) {

                status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_QUERY,
                        AdapterConstants.ARTICLE_SYSTEM_TYPE, AdapterConstants.STATUS_MSG_BAD_SQL, correlId);
                queryResult = status.toJsonObject();

            } else {

                /* Lookup the system list in the Registry */

                SystemWiring[] resultArray = null;

                if ("".equals(querySQL)) {
                    resultArray = systemWiringFactory.getAll();
                }

                /* Generate the response object */

                queryResult.putString(AdapterConstants.FIELD_OPERATION,
                        AdapterConstants.OP_QUERY_RESPONSE_SERVICE_WIRING);
                queryResult.putString(AdapterConstants.FIELD_CORRELATION_ID, correlId);

                /* For each wired service... */
                for (int i = 0; i < resultArray.length; i++) {

                    JSON serviceWiring = new JSON();
                    serviceWiring.putString(AdapterConstants.FIELD_COMPOSITION, resultArray[i].getCompositeId());
                    serviceWiring.putString(AdapterConstants.FIELD_FROM_PLATFORM, resultArray[i]
                            .getFromSystemPlatformId());
                    serviceWiring.putString(AdapterConstants.FIELD_FROM_SYSTEM, resultArray[i].getFromSystemId());
                    serviceWiring.putString(AdapterConstants.FIELD_FROM_INTERFACE, resultArray[i].getFromInterfaceId());
                    serviceWiring.putString(AdapterConstants.FIELD_TO_PLATFORM, resultArray[i].getToSystemPlatformId());
                    serviceWiring.putString(AdapterConstants.FIELD_TO_SYSTEM, resultArray[i].getToSystemId());
                    serviceWiring.putString(AdapterConstants.FIELD_TO_INTERFACE, resultArray[i].getToInterfaceId());
                    addOptionalFields(serviceWiring, null, resultArray[i].getAttributes());
                    serviceWiringList.add(serviceWiring);

                }
                queryResult.putArray(AdapterConstants.FIELD_SERVICE_WIRING, serviceWiringList);
            }

        } catch (Exception e) {

            String message = e.getClass().getName() + ": " + e.getMessage();
            status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_QUERY,
                    AdapterConstants.ARTICLE_SYSTEM, message, correlId);
            queryResult = status.toJsonObject();

        }

        return queryResult;
    }

    /**
     * Returns the SQL corresponding to the Systems query.
     *
     * @param op
     *            The JSON object that needs converting to SQL.
     *
     * @return The SQL for the query.
     */
    private static String generateWiringPredicate(JSON op) {

        String predicate = null;
        StringBuilder predicateBuilder = new StringBuilder();

        try {

            String compositeID = op.getString(AdapterConstants.FIELD_COMPOSITION);
            if (compositeID != null) {
                compositeID.replace("*", "%");
                predicateBuilder.append("COMPOSITE_ID LIKE '");
                predicateBuilder.append(compositeID);
                predicateBuilder.append("' AND ");
            }

            String fromPlatform = op.getString(AdapterConstants.FIELD_FROM_PLATFORM);
            if (fromPlatform != null) {
                fromPlatform.replace("*", "%");
                predicateBuilder.append("FROM_SERVICE_PLATFORM_ID LIKE '");
                predicateBuilder.append(fromPlatform);
                predicateBuilder.append("' AND ");
            }

            String toPlatform = op.getString(AdapterConstants.FIELD_TO_PLATFORM);
            if (toPlatform != null) {
                toPlatform.replace("*", "%");
                predicateBuilder.append("TO_SERVICE_PLATFORM_ID LIKE '");
                predicateBuilder.append(toPlatform);
                predicateBuilder.append("' AND ");
            }

            JSON attr = op.getJSON(AdapterConstants.FIELD_ATTRIBUTES);
            String attrString = (attr != null) ? attr.toString() : null;
            if (attrString != null && !attrString.equals("{}")) {
                int start = attrString.indexOf('{') + 1;
                int end = attrString.lastIndexOf('}');
                end = (end != -1) ? end : attrString.length();
                if (start <= end) {
                    attrString = attrString.substring(start, end);
                }
                predicateBuilder.append("ATTRIBUTES LIKE '%");
                predicateBuilder.append(attrString);
                predicateBuilder.append("%' AND ");
            }

            int trailingAndIndex = predicateBuilder.lastIndexOf(" AND ");
            if (trailingAndIndex == predicateBuilder.length() - 5) {
                predicateBuilder.delete(trailingAndIndex, predicateBuilder.length());
            }
            predicate = predicateBuilder.toString();

        } catch (Exception e) {

            predicate = null;

        }

        return predicate;
    }
}
