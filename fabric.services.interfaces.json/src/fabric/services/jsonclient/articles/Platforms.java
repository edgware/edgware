/*
 * (C) Copyright IBM Corp. 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.jsonclient.articles;

import fabric.registry.FabricRegistry;
import fabric.registry.Platform;
import fabric.registry.PlatformFactory;
import fabric.registry.QueryScope;
import fabric.services.json.JSON;
import fabric.services.json.JSONArray;
import fabric.services.jsonclient.utilities.AdapterConstants;
import fabric.services.jsonclient.utilities.AdapterStatus;

/**
 * Class that handles JSON commands that deal with Platforms.
 */
public class Platforms extends Article {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    public static String homeNode = null;

    /*
     * Class methods
     */

    public static String getNode() {

        return homeNode;
    }

    public static void setNode(String homeNode) {

        Platforms.homeNode = homeNode;
    }

    /**
     * Inserts a Platform into the registry.
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

            String platformId = op.getString(AdapterConstants.FIELD_ID);
            String platformTypeId = op.getString(AdapterConstants.FIELD_TYPE);

            if (platformId == null || platformTypeId == null) {

                status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_REGISTER,
                        AdapterConstants.ARTICLE_PLATFORM, AdapterConstants.STATUS_MSG_FIELD_ERROR, correlId);

            } else {

                JSON location = op.getJSON(AdapterConstants.FIELD_LOCATION);

                PlatformFactory platformFactory = FabricRegistry.getPlatformFactory();

                Double lat = location.getDouble(AdapterConstants.FIELD_LATITUDE);
                Double lon = location.getDouble(AdapterConstants.FIELD_LONGITUDE);
                Double alt = location.getDouble(AdapterConstants.FIELD_ALTITUDE);
                JSON attr = op.getJSON(AdapterConstants.FIELD_ATTRIBUTES);
                String description = op.getString(AdapterConstants.FIELD_DESCRIPTION);

                Platform platform = platformFactory.createPlatform(platformId, platformTypeId, getNode(), //
                        null, // affiliation
                        null, // Security classification,
                        "DEPLOYED", // Readiness,
                        "AVAILABLE", // Availability,
                        (lat.equals(Double.NaN)) ? 0.0 : lat, // Latitude
                        (lon.equals(Double.NaN)) ? 0.0 : lon, // Longitude
                        (alt.equals(Double.NaN)) ? 0.0 : alt, // Altitude
                        0.0, // Bearing
                        0.0, // Velocity
                        description, // Description
                        (attr != null) ? attr.toString() : null, // Attributes
                        null); // Attributes URI
                boolean success = platformFactory.save(platform);

                if (!success) {

                    status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
                            AdapterConstants.ARTICLE_PLATFORM, "Insert/update of platform into the Registry failed",
                            correlId);

                }
            }

        } catch (Exception e) {

            String message = e.getClass().getName() + ": " + e.getMessage();
            status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
                    AdapterConstants.ARTICLE_PLATFORM, message, correlId);

        }

        return status.toJsonObject();
    }

    /**
     * Deletes a Platform from the registry.
     *
     * @param platformId
     *            The ID of the platform to be deleted.
     * @param correlId
     *            The correlation ID for the status message.
     * @return A status code.
     */
    public static JSON deregister(String platformId, String correlId) {

        AdapterStatus status = new AdapterStatus(correlId);

        if (platformId == null) {
            status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_DEREGISTER,
                    AdapterConstants.ARTICLE_PLATFORM, AdapterConstants.STATUS_MSG_FIELD_ERROR, correlId);
        } else {
            PlatformFactory platformFactory = FabricRegistry.getPlatformFactory(QueryScope.LOCAL);
            Platform platform = platformFactory.getPlatformById(platformId);
            boolean complete = platformFactory.delete(platform);
            if (complete == false) {
                status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_DEREGISTER,
                        AdapterConstants.ARTICLE_PLATFORM, AdapterConstants.STATUS_MSG_FAILED_DELETE, correlId);
            }
        }
        return status.toJsonObject();
    }

    /**
     * Returns the result of the Systems query a JSON Object.
     *
     * @param correlId
     *            The correlation ID of the request.
     */
    public static JSON query(JSON op, final String correlId) {

        JSON queryResult = new JSON();
        AdapterStatus status = new AdapterStatus(correlId);
        JSONArray platformList = new JSONArray();

        try {

            PlatformFactory platformFactory = FabricRegistry.getPlatformFactory();
            String querySQL = generatePredicate(AdapterConstants.FIELD_ID, "PLATFORM_ID", QUERY_TYPE | QUERY_ATTRIBUTES
                    | QUERY_LOCATION, op);

            if (querySQL == null) {
                status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_QUERY,
                        AdapterConstants.ARTICLE_PLATFORM, AdapterConstants.STATUS_MSG_BAD_SQL, correlId);
                queryResult = status.toJsonObject();
            } else {

                Platform[] resultArray = null;

                if ("".equals(querySQL)) {
                    resultArray = platformFactory.getAllPlatforms();
                } else {
                    resultArray = platformFactory.getPlatforms(querySQL);
                }

                queryResult.putString(AdapterConstants.FIELD_OPERATION, AdapterConstants.OP_QUERY_RESPONSE_PLATFORMS);
                queryResult.putString(AdapterConstants.FIELD_CORRELATION_ID, correlId);

                /* For each platform... */
                for (int i = 0; status.isOK() && i < resultArray.length; i++) {

                    JSON platform = new JSON();
                    platform.putString(AdapterConstants.FIELD_NODE, resultArray[i].getNodeId());
                    platform.putString(AdapterConstants.FIELD_ID, resultArray[i].getId());
                    platform.putString(AdapterConstants.FIELD_TYPE, resultArray[i].getTypeId());
                    platform.putJSON(AdapterConstants.FIELD_LOCATION, buildLocationObject(resultArray[i].getLatitude(),
                            resultArray[i].getLongitude(), resultArray[i].getAltitude()));
                    addOptionalFields(platform, resultArray[i].getDescription(), resultArray[i].getAttributes());
                    platformList.add(platform);

                }
                queryResult.putJSONArray(AdapterConstants.FIELD_PLATFORMS, platformList);
            }

        } catch (Exception e) {

            String message = e.getClass().getName() + ": " + e.getMessage();
            status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_QUERY,
                    AdapterConstants.ARTICLE_PLATFORM, message, correlId);
            queryResult = status.toJsonObject();

        }

        return queryResult;
    }
}
