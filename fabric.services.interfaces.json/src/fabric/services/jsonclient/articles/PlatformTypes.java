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
import fabric.registry.FabricRegistry;
import fabric.registry.QueryScope;
import fabric.registry.Type;
import fabric.registry.TypeFactory;
import fabric.services.jsonclient.utilities.AdapterConstants;
import fabric.services.jsonclient.utilities.AdapterStatus;

/**
 * Class that handles JSON commands that deal with Platform Types.
 */
public class PlatformTypes extends Article {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    /*
     * Class methods
     */

    /**
     * Inserts a Platform Type into the registry
     *
     * @param op
     *            the full JSON operation object.
     *
     * @param correlId
     *            the operation correlation ID.
     *
     * @return the status of the operation.
     */
    public static JSON register(JSON op, String correlId) {

        AdapterStatus status = new AdapterStatus(correlId);

        try {

            String typeId = op.getString(AdapterConstants.FIELD_TYPE);

            if (typeId == null) {

                status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_REGISTER,
                        AdapterConstants.ARTICLE_PLATFORM_TYPE, AdapterConstants.STATUS_MSG_FIELD_ERROR, correlId);

            } else {

                JSON attr = op.getJSON(AdapterConstants.FIELD_ATTRIBUTES);

                TypeFactory typeFactory = FabricRegistry.getTypeFactory();
                Type type = typeFactory.createPlatformType(typeId, op.getString(AdapterConstants.FIELD_DESCRIPTION),
                        (attr != null) ? attr.toString() : null, null);
                boolean success = typeFactory.save(type);

                if (!success) {

                    status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
                            AdapterConstants.ARTICLE_PLATFORM_TYPE,
                            "Insert/update of platform type into the Registry failed", correlId);

                }
            }

        } catch (Exception e) {

            String message = e.getClass().getName() + ": " + e.getMessage();
            status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
                    AdapterConstants.ARTICLE_PLATFORM_TYPE, message, correlId);

        }

        return status.toJsonObject();
    }

    /**
     * Deletes a Platform Type from the registry.
     *
     * @param platformTypeId
     *            The ID of the platform type to be deleted.
     *
     * @param correlId
     *            The correlation ID for the status message.
     *
     * @return the status of the operation.
     */
    public static JSON deregister(final String platformTypeId, final String correlId) {

        AdapterStatus status = new AdapterStatus(correlId);

        if (platformTypeId == null) {

            status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_DEREGISTER,
                    AdapterConstants.ARTICLE_PLATFORM_TYPE, AdapterConstants.STATUS_MSG_FIELD_ERROR, correlId);

        } else {

            TypeFactory typeFactory = FabricRegistry.getTypeFactory(QueryScope.LOCAL);
            Type platformType = typeFactory.getPlatformType(platformTypeId);
            boolean success = typeFactory.delete(platformType);

            if (success == false) {
                status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_DEREGISTER,
                        AdapterConstants.ARTICLE_PLATFORM_TYPE, AdapterConstants.STATUS_MSG_FAILED_DELETE, correlId);
            }
        }

        return status.toJsonObject();
    }

    /**
     * Queries the database for platform types.
     *
     * @param op
     * @param correlId
     * @return The result of the query.
     */
    public static JSON query(final JSON op, final String correlId) {

        JSON queryResult = new JSON();
        AdapterStatus status = new AdapterStatus(correlId);
        List<JSON> jsonList = new ArrayList<>();

        try {

            TypeFactory typeFactory = FabricRegistry.getTypeFactory();

            String querySQL = generatePredicate(AdapterConstants.FIELD_ID, "TYPE_ID", QUERY_ATTRIBUTES, op);

            if (querySQL == null) {

                status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_QUERY,
                        AdapterConstants.ARTICLE_PLATFORM_TYPE, AdapterConstants.STATUS_MSG_BAD_SQL, correlId);
                queryResult = status.toJsonObject();

            } else {

                Type[] resultArray = null;

                if ("".equals(querySQL)) {
                    resultArray = typeFactory.getAllPlatformTypes();
                } else {
                    resultArray = typeFactory.getPlatformTypes(querySQL);
                }

                queryResult.putString(AdapterConstants.FIELD_OPERATION,
                        AdapterConstants.OP_QUERY_RESPONSE_PLATFORM_TYPES);
                queryResult.putString(AdapterConstants.FIELD_CORRELATION_ID, correlId);

                for (int i = 0; i < resultArray.length; i++) {

                    JSON type = new JSON();
                    type.putString(AdapterConstants.FIELD_TYPE, resultArray[i].getId());
                    addOptionalFields(type, resultArray[i].getDescription(), resultArray[i].getAttributes());
                    jsonList.add(type);

                }
                queryResult = queryResult.putArray(AdapterConstants.FIELD_PLATFORM_TYPES, jsonList);
            }

        } catch (Exception e) {

            String message = e.getClass().getName() + ": " + e.getMessage();
            status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_QUERY,
                    AdapterConstants.ARTICLE_PLATFORM_TYPE, message, correlId);
            queryResult = status.toJsonObject();

        }
        return queryResult;
    }
}