/*
 * (C) Copyright IBM Corp. 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.jsonclient.articles;

import fabric.registry.FabricRegistry;
import fabric.registry.QueryScope;
import fabric.registry.Type;
import fabric.registry.TypeFactory;
import fabric.services.json.JSON;
import fabric.services.jsonclient.utilities.AdapterConstants;
import fabric.services.jsonclient.utilities.AdapterStatus;

/**
 * Class that handles JSON commands that deal with Service Types (formerly Feed Types).
 */
public class ServiceTypes {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    /**
     * Inserts a Service Type into the registry.
     *
     * @param id
     *            the ID of the type being registered.
     *
     * @param jsonOperationObject
     *            The full JSON operation object.
     *
     * @param correlId
     *            the operation correlation ID.
     *
     * @return the status of the operation.
     */
    public static JSON register(JSON op, String correlId) {

        AdapterStatus status = new AdapterStatus(correlId);
        String typeid = op.getString(AdapterConstants.FIELD_TYPE);

        try {

            if (typeid == null) {

                status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_REGISTER,
                        AdapterConstants.ARTICLE_SERVICE_TYPE, AdapterConstants.STATUS_MSG_FIELD_ERROR, correlId);

            } else {

                /* Get the mode of the service, indicating its semantics */
                String mode = op.getString(AdapterConstants.FIELD_MODE);
                String testMode = (mode != null) ? mode : "";

                switch (testMode) {

                    case AdapterConstants.MODE_OUTPUT:
                    case AdapterConstants.MODE_INPUT:
                    case AdapterConstants.MODE_NOTIFY:
                    case AdapterConstants.MODE_LISTEN:
                    case AdapterConstants.MODE_SOLICIT:
                    case AdapterConstants.MODE_RESPONSE:
                    case "":

                        /* Create the Registry entry for the new service type */
                        TypeFactory typeFactory = FabricRegistry.getTypeFactory();
                        Type type = typeFactory.createServiceType(typeid, op
                                .getString(AdapterConstants.FIELD_DESCRIPTION), mode, null);
                        boolean success = typeFactory.save(type);

                        if (!success) {

                            status = new AdapterStatus(AdapterConstants.ERROR_ACTION,
                                    AdapterConstants.OP_CODE_REGISTER, AdapterConstants.ARTICLE_SERVICE_TYPE,
                                    "Insert/update of system type into the Registry failed", correlId);

                        }

                        break;

                    default:

                        status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_REGISTER,
                                AdapterConstants.ARTICLE_SERVICE_TYPE, AdapterConstants.STATUS_MSG_BAD_MODE, correlId);

                }
            }

        } catch (Exception e) {

            String message = e.getClass().getName() + ": " + e.getMessage();
            status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
                    AdapterConstants.ARTICLE_SERVICE_TYPE, message, correlId);

        }

        return status.toJsonObject();
    }

    /**
     * Deletes a Service Type from the registry.
     *
     * @param serviceTypeId
     *            The ID of the Type to be deleted.
     * @param correlId
     *            The correlation ID for the status message.
     * @return A status code.
     */
    public static JSON deregister(String serviceTypeId, String correlId) {

        AdapterStatus status = new AdapterStatus(correlId);

        if (serviceTypeId == null) {
            status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_DEREGISTER,
                    AdapterConstants.ARTICLE_SERVICE_TYPE, AdapterConstants.STATUS_MSG_FIELD_ERROR, correlId);
        } else {
            TypeFactory typeFactory = FabricRegistry.getTypeFactory(QueryScope.LOCAL);
            Type systemType = typeFactory.getServiceType(serviceTypeId);
            boolean complete = typeFactory.delete(systemType);
            if (complete == false) {
                status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_DEREGISTER,
                        AdapterConstants.ARTICLE_SERVICE_TYPE, AdapterConstants.STATUS_MSG_FAILED_DELETE, correlId);
            }
        }
        return status.toJsonObject();
    }

}
