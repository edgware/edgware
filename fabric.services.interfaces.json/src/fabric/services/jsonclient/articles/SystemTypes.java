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

import fabric.core.json.JSON;
import fabric.core.json.JSONArray;
import fabric.registry.FabricRegistry;
import fabric.registry.QueryScope;
import fabric.registry.Type;
import fabric.registry.TypeFactory;
import fabric.services.jsonclient.utilities.AdapterConstants;
import fabric.services.jsonclient.utilities.AdapterStatus;
import fabric.services.jsonclient.utilities.JsonUtils;

/**
 * Class that handles JSON operations that deal with System Types.
 */
public class SystemTypes extends Article {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    /**
     * Inserts a System Type into the registry.
     *
     * @param op
     *            the full JSON operation object.
     *
     * @param correlId
     *            the operation correlation ID.
     *
     * @return the status of the operation.
     */
    public static JSON register(final JSON op, String correlId) {

        AdapterStatus status = new AdapterStatus(correlId);

        try {

            /* The get the ID of the system type */
            String typeid = op.getString(AdapterConstants.FIELD_TYPE);

            if (typeid == null) {

                status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_REGISTER,
                        AdapterConstants.ARTICLE_SYSTEM_TYPE, AdapterConstants.STATUS_MSG_FIELD_ERROR, correlId);

            } else {

                /* Build a comma-separated list of services associated with this system type */

                JSONArray servicesArray = op.getJSONArray(AdapterConstants.FIELD_SERVICES);
                StringBuilder serviceTypeList = new StringBuilder();
                boolean isValidMessage = true;

                for (Iterator<JSON> servicesIterator = servicesArray.iterator(); isValidMessage
                        && servicesIterator.hasNext();) {

                    JSON nextServiceType = servicesIterator.next();
                    String type = nextServiceType.getString(AdapterConstants.FIELD_TYPE);
                    String mode = nextServiceType.getString(AdapterConstants.FIELD_MODE);
                    mode = (mode != null) ? mode : "";
                    String name = nextServiceType.getString(AdapterConstants.FIELD_ID);
                    name = (name != null) ? name : type;

                    switch (mode) {

                        case AdapterConstants.MODE_OUTPUT:
                        case AdapterConstants.MODE_INPUT:
                        case AdapterConstants.MODE_NOTIFY:
                        case AdapterConstants.MODE_LISTEN:
                        case AdapterConstants.MODE_SOLICIT:
                        case AdapterConstants.MODE_RESPONSE:
                        case "":

                            serviceTypeList.append(type);
                            serviceTypeList.append(':');
                            serviceTypeList.append(mode);
                            serviceTypeList.append(':');
                            serviceTypeList.append(name);
                            serviceTypeList.append(',');

                            break;

                        default:

                            status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_REGISTER,
                                    AdapterConstants.ARTICLE_SYSTEM_TYPE, AdapterConstants.STATUS_MSG_BAD_MODE,
                                    correlId);
                            isValidMessage = false;

                    }
                }

                if (isValidMessage) {

                    if (serviceTypeList.toString().endsWith(",")) {
                        serviceTypeList.setLength(serviceTypeList.length() - 1);
                    }

                    /* Get the attributes of the service */
                    JSON attr = op.getJSON(AdapterConstants.FIELD_ATTRIBUTES);
                    attr = (attr != null) ? attr : new JSON();

                    /*
                     * Add the system type to the Registry (a system's services are currently recorded in its attributes
                     * field)
                     */
                    TypeFactory typeFactory = FabricRegistry.getTypeFactory();
                    attr.putString("serviceTypes", serviceTypeList.toString());
                    Type type = typeFactory.createSystemType(typeid, op.getString(AdapterConstants.FIELD_DESCRIPTION),
                            attr.toString(), null);
                    boolean success = typeFactory.save(type);

                    if (!success) {

                        status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
                                AdapterConstants.ARTICLE_SYSTEM_TYPE,
                                "Insert/update of system type into the Registry failed", correlId);

                    }
                }
            }

        } catch (Exception e) {

            String message = e.getClass().getName() + ": " + e.getMessage();
            status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
                    AdapterConstants.ARTICLE_SYSTEM_TYPE, message, correlId);

        }

        return status.toJsonObject();
    }

    /**
     * Deletes a System Type from the registry.
     *
     * @param systemTypeId
     *            The ID of the Type to be deleted.
     * @param correlId
     *            The correlation ID for the status message.
     * @return A JSON status object.
     */
    public static JSON deregister(String systemTypeId, String correlId) {

        AdapterStatus status = new AdapterStatus(correlId);

        if (systemTypeId == null) {
            status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_DEREGISTER,
                    AdapterConstants.ARTICLE_SYSTEM_TYPE, AdapterConstants.STATUS_MSG_FIELD_ERROR, correlId);
        } else {
            TypeFactory typeFactory = FabricRegistry.getTypeFactory(QueryScope.LOCAL);
            Type systemType = typeFactory.getSystemType(systemTypeId);
            boolean complete = typeFactory.delete(systemType);
            if (complete == false) {
                status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_DEREGISTER,
                        AdapterConstants.ARTICLE_SYSTEM_TYPE, AdapterConstants.STATUS_MSG_FAILED_DELETE, correlId);
            }
        }
        return status.toJsonObject();
    }

    /**
     * Returns the result of the System Type query a JSON Object.
     *
     * @param correlId
     *            The correlation ID of the request.
     *
     * @return The query result JSON Object.
     */
    public static JSON query(final JSON op, final String correlId) {

        JSON systemTypesQueryResult = new JSON();
        AdapterStatus status = new AdapterStatus(correlId);

        try {

            TypeFactory typeFactory = FabricRegistry.getTypeFactory();

            String querySQL = generatePredicate(AdapterConstants.FIELD_TYPE, "TYPE_ID", QUERY_ATTRIBUTES, op);

            if (querySQL == null) {

                status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_QUERY,
                        AdapterConstants.ARTICLE_SYSTEM_TYPE, AdapterConstants.STATUS_MSG_BAD_SQL, correlId);
                systemTypesQueryResult = status.toJsonObject();

            } else {

                /* Get all of the service types that match the query */

                Type[] matchingSystemTypes = null;

                if ("".equals(querySQL)) {
                    matchingSystemTypes = typeFactory.getAllSystemTypes();
                } else {
                    matchingSystemTypes = typeFactory.getSystemTypes(querySQL);
                }

                /* Get the list of specific service types that matching system types must include */

                JSONArray serviceTypesFilter = op.getJSONArray(AdapterConstants.FIELD_SERVICES);
                ArrayList<Type> matchingServiceTypes = new ArrayList<Type>();

                for (JSON nextFilter : serviceTypesFilter) {

                    Type[] serviceTypes = null;
                    // TODO querySQL = queryTypesSQL(nextFilter);

                    if ("".equals(querySQL)) {
                        serviceTypes = typeFactory.getAllServiceTypes();
                    } else {
                        serviceTypes = typeFactory.getServiceTypes(querySQL);
                    }

                    for (Type nextType : serviceTypes) {
                        if (!matchingServiceTypes.contains(nextType)) {
                            matchingServiceTypes.add(nextType);
                        }
                    }
                }

                /* Filter the list of system types by service types (if specified) */

                for (int i = 0; i < matchingSystemTypes.length; i++) {

                    String thisSystemsServices = matchingSystemTypes[i].getAttributes();

                    if (thisSystemsServices != null) {

                        boolean discard = true;

                        for (Type nextType : matchingServiceTypes) {
                            if (thisSystemsServices.matches(".*,*" + nextType.getId() + ":.*:.*,*.*")) {
                                discard = false;
                                break;
                            }
                        }

                        if (discard) {
                            matchingSystemTypes[i] = null;
                        }
                    }
                }

                /* Build a map of all possible service types */
                Type[] allServiceTypes = typeFactory.getAllServiceTypes();
                HashMap<String, Type> typeMap = new HashMap<String, Type>();
                for (Type nextType : allServiceTypes) {
                    typeMap.put(nextType.getId(), nextType);
                }

                /* Build the result */

                systemTypesQueryResult.putString(AdapterConstants.FIELD_OPERATION,
                        AdapterConstants.OP_QUERY_RESPONSE_SYSTEM_TYPES);
                systemTypesQueryResult.putString(AdapterConstants.FIELD_CORRELATION_ID, correlId);

                List<JSON> systemTypeJSONArray = new ArrayList<JSON>();

                for (Type nextType : matchingSystemTypes) {

                    if (nextType != null) {

                        /* Add basic system type information */

                        JSON nextTypeJSON = new JSON();

                        nextTypeJSON.putString(AdapterConstants.FIELD_TYPE, nextType.getId());
                        nextTypeJSON.putString(AdapterConstants.FIELD_DESCRIPTION, nextType.getDescription());

                        String attrString = nextType.getAttributes();
                        JSON attr = JsonUtils.stringTOJSON(attrString, "Attribute value is not valid JSON");
                        if (attr != null) {
                            nextTypeJSON.putJSON(AdapterConstants.FIELD_ATTRIBUTES, attr);
                        }

                        /* Get the list of services offered by this system type */

                        String[] servicesOffered = attrString.split(",");
                        JSONArray servicesOfferedJSONArray = new JSONArray();

                        for (String service : servicesOffered) {

                            String[] serviceParts = service.split(":");
                            Type serviceToAdd = typeMap.get(serviceParts[0]);

                            String serviceTypeJSONString = String.format(
                                    "\"type\":\"%s\",\"attr\":\"%s\",\"desc\":\"%s\"", serviceToAdd.getId(),
                                    serviceToAdd.getAttributes(), serviceToAdd.getDescription());
                            JSON serviceTypeJSON = new JSON(serviceTypeJSONString);
                            servicesOfferedJSONArray.add(serviceTypeJSON);
                        }

                        nextTypeJSON.putJSONArray(AdapterConstants.FIELD_SERVICES, servicesOfferedJSONArray);
                        systemTypeJSONArray.add(nextTypeJSON);
                    }
                }

                systemTypesQueryResult.putArray(AdapterConstants.FIELD_SYSTEM_TYPES, systemTypeJSONArray);
            }

        } catch (Exception e) {

            String message = e.getClass().getName() + ": " + e.getMessage();
            status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_QUERY,
                    AdapterConstants.ARTICLE_SYSTEM_TYPE, message, correlId);
            systemTypesQueryResult = status.toJsonObject();

        }

        return systemTypesQueryResult;
    }
}