/*
 * (C) Copyright IBM Corp. 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.jsonclient.articles;

import fabric.FabricBus;
import fabric.core.json.JSON;
import fabric.core.json.JsonUtils;
import fabric.services.jsonclient.utilities.AdapterConstants;

/**
 * Base class for JSON client article handlers.
 */
public class Article extends FabricBus {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    /* Message field codes */
    public static final int QUERY_NONE = 0x0000;
    public static final int QUERY_TYPE = 0x0001;
    public static final int QUERY_ATTRIBUTES = 0x0002;
    public static final int QUERY_DESCRIPTION = 0x0004;
    public static final int QUERY_LOCATION = 0x0008;
    public static final int QUERY_AVAILABILITY = 0x0010;
    public static final int QUERY_AVAILABLE = 0x0020;
    public static final int QUERY_ADDRESS = 0x0040;
    public static final int QUERY_AFFILIATION = 0x0080;

    /*
     * Class methods
     */

    /**
     * Build part of a SQL query predicate, where the search term is the value of a field in the specified JSON object.
     * <p>
     * The value returned is a partial predicate of the form:
     * </p>
     *
     * <pre>
     * &lt;column&gt; LIKE '&lt;field-value&gt;' AND
     * </pre>
     * <p>
     * Occurrences of the character <code>'*'</code> in the field value will be replaced with the SQL wildcard character
     * <code>'%'</code>.
     * </p>
     *
     * @param field
     *            the name of the field in the JSON object.
     *
     * @param column
     *            the column name.
     *
     * @param options
     *            a bit field indicating options columns to include in the query (see generateSQLLogic()).
     *
     * @param json
     *            the JSON object.
     *
     * @return a partial predicate, or an empty string if the field value is null.
     */
    public static String generatePredicate(String field, String column, int options, JSON json) {

        String predicate = null;
        StringBuilder predicateBuilder = new StringBuilder();

        try {

            String value = json.getString(field);

            if (value != null) {

                value = value.replace('*', '%');
                predicateBuilder.append(column + " LIKE '");
                predicateBuilder.append(value);
                predicateBuilder.append("' AND ");

            }

            predicateBuilder.append(generateOptionalSQL(json, options));
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

    /**
     * Generates SQL for a generic query type from any JSON object.
     * <p>
     * The following constants can be used to indicate which fields of the JSON object to include in the query:
     * </p>
     * <ul>
     * <li>QUERY_TYPE</li>
     * <li>QUERY_ATTRIBUTES</li>
     * <li>QUERY_DESCRIPTION</li>
     * <li>QUERY_LOCATION</li>
     * </ul>
     *
     * @param op
     *            The JSON object from which the query data is taken.
     *
     * @param fields
     *            bit-field indicating which fields from the JSON object to include in the query.
     *
     * @return A string containing SQL.
     */
    public static String generateOptionalSQL(JSON op, int fields) {

        String querySQL = null;
        StringBuilder s = new StringBuilder();

        if ((fields & QUERY_TYPE) != 0) {
            String type = op.getString(AdapterConstants.FIELD_TYPE);
            if (type != null) {
                type.replace("*", "%");
                s.append("TYPE_ID LIKE '");
                s.append(type);
                s.append("' AND ");
            }
        }

        if ((fields & QUERY_AVAILABLE) != 0) {
            String available = op.getString(AdapterConstants.FIELD_AVAILABILE);
            if (available != null) {
                available.replace("*", "%");
                s.append("AVAILABLE LIKE '");
                s.append(available);
                s.append("' AND ");
            }
        }

        if ((fields & QUERY_AVAILABILITY) != 0) {
            String availability = op.getString(AdapterConstants.FIELD_AVAILABILITY);
            if (availability != null) {
                availability.replace("*", "%");
                s.append("AVAILABILITY LIKE '");
                s.append(availability);
                s.append("' AND ");
            }
        }

        if ((fields & QUERY_LOCATION) != 0) {
            if (!op.getJSON(AdapterConstants.FIELD_LOCATION).toString().equals("{}")) {
                JSON jsonLocation = op.getJSON(AdapterConstants.FIELD_LOCATION);
                s.append("LATITUDE >= ");
                s.append(jsonLocation.getDouble(AdapterConstants.FIELD_LOCATION_BOTTOM));
                s.append(" AND LATITUDE <= ");
                s.append(jsonLocation.getDouble(AdapterConstants.FIELD_LOCATION_TOP));
                s.append(" AND ");
                s.append("LONGITUDE >= ");
                s.append(jsonLocation.getDouble(AdapterConstants.FIELD_LOCATION_LEFT));
                s.append(" AND LONGITUDE <= ");
                s.append(jsonLocation.getDouble(AdapterConstants.FIELD_LOCATION_RIGHT));
                s.append(" AND ");
            }
        }

        if ((fields & QUERY_ATTRIBUTES) != 0) {
            String attrString = null;
            try {
                JSON attr = op.getJSON(AdapterConstants.FIELD_ATTRIBUTES);
                attrString = (attr != null) ? attr.toString() : null;
            } catch (Exception e) {
            }
            if (attrString != null && !attrString.equals("{}")) {
                int start = attrString.indexOf('{') + 1;
                int end = attrString.lastIndexOf('}');
                end = (end != -1) ? end : attrString.length();
                if (start <= end) {
                    attrString = attrString.substring(start, end);
                }
                s.append("ATTRIBUTES LIKE '%");
                s.append(attrString);
                s.append("%' AND ");
            }
        }

        if ((fields & QUERY_DESCRIPTION) != 0) {
            String description = op.getString(AdapterConstants.FIELD_DESCRIPTION);
            if (description != null) {
                description.replace("*", "%");
                s.append("DESCRIPTION LIKE '");
                s.append(description);
                s.append("' AND ");
            }
        }

        if ((fields & QUERY_ADDRESS) != 0) {
            String address = op.getString(AdapterConstants.FIELD_ADDRESS);
            if (address != null) {
                address.replace("*", "%");
                s.append("IP LIKE '");
                s.append(address);
                s.append("' AND ");
            }
        }

        if ((fields & QUERY_AFFILIATION) != 0) {
            String affil = op.getString(AdapterConstants.FIELD_AFFIL);
            if (affil != null) {
                affil.replace("*", "%");
                s.append("AFFILIATION LIKE '");
                s.append(affil);
                s.append("' AND ");
            }
        }

        querySQL = s.toString();

        return querySQL;
    }

    /**
     * Adds description and attributes fields to a JSON object if they are non-empty.
     *
     * @param json
     *            the JSON object to which the values are to be added.
     *
     * @param description
     *            the description string.
     *
     * @param attributes
     *            the attributes (a string containing a JSON object).
     */
    public static void addOptionalFields(JSON json, String description, String attributes) {

        if (description != null && !description.equals("null")) {
            json.putString(AdapterConstants.FIELD_DESCRIPTION, description);

        }

        JSON attr = JsonUtils.stringTOJSON(attributes, "Attribute value is not valid JSON");
        if (attr != null && !attr.toString().equals("{}")) {
            json.putJSON(AdapterConstants.FIELD_ATTRIBUTES, attr);
        }

    }

    /**
     * Creates a JSON object with location information pulled from a Platform object.
     *
     * @param lat
     *            Latitude.
     *
     * @param lon
     *            Longitude.
     *
     * @param alt
     *            Altitude.
     *
     * @return A location JSON Object.
     */
    public static JSON buildLocationObject(Double lat, Double lon, Double alt) {

        JSON location = new JSON();
        location = location.putDouble(AdapterConstants.FIELD_LATITUDE, lat);
        location = location.putDouble(AdapterConstants.FIELD_LONGITUDE, lon);
        location = location.putDouble(AdapterConstants.FIELD_ALTITUDE, alt);
        if (location.toString().equals("{}")) {
            location = null;
        }
        return location;
    }
}
