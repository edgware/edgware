/*
 * (C) Copyright IBM Corp. 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.jsonclient.articles;

import java.util.ArrayList;
import java.util.List;

import fabric.registry.Actor;
import fabric.registry.ActorFactory;
import fabric.registry.FabricRegistry;
import fabric.registry.QueryScope;
import fabric.services.json.JSON;
import fabric.services.jsonclient.utilities.AdapterConstants;
import fabric.services.jsonclient.utilities.AdapterStatus;

/**
 * Class that handles JSON commands that deal with Users (formerly Actors).
 */
public class Users extends Article {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    /**
     * Inserts a User into the registry.
     *
     * @param op
     *            The full JSON operation object.
     *
     * @param correlId
     *            The correlation ID of the request.
     *
     * @return A JSON status object.
     */
    public static JSON register(final JSON op, String correlId) {

        AdapterStatus status = new AdapterStatus(correlId);

        try {

            String id = op.getString(AdapterConstants.FIELD_ID);
            String type = op.getString(AdapterConstants.FIELD_TYPE);

            if (id == null || type == null) {

                status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_REGISTER,
                        AdapterConstants.ARTICLE_USER, AdapterConstants.STATUS_MSG_FIELD_ERROR, correlId);

            } else {

                JSON attr = op.getJSON(AdapterConstants.FIELD_ATTRIBUTES);

                ActorFactory actorFactory = FabricRegistry.getActorFactory();
                Actor actor = actorFactory.createActor(id, //
                        type, //
                        null, // roles
                        null, // credentials
                        op.getString(AdapterConstants.FIELD_AFFIL), // Affiliation
                        op.getString(AdapterConstants.FIELD_DESCRIPTION), // Description
                        (attr != null) ? attr.toString() : null, // Attributes
                                null); // attributesURI
                boolean success = actorFactory.save(actor);

                if (!success) {

                    status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
                            AdapterConstants.ARTICLE_USER, "Insert/update of platform type into the Registry failed",
                            correlId);

                }
            }

        } catch (Exception e) {

            String message = e.getClass().getName() + ": " + e.getMessage();
            status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_REGISTER,
                    AdapterConstants.ARTICLE_USER, message, correlId);

        }

        return status.toJsonObject();
    }

    /**
     * Deletes a User from the registry.
     *
     * @param userId
     *            The ID of the User to be deleted.
     * @param correlId
     *            The correlation ID for the status message.
     * @return A JSON status object.
     */
    public static JSON deregister(String userId, String correlId) {

        AdapterStatus status = new AdapterStatus(correlId);
        if (userId == null) {
            status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_DEREGISTER,
                    AdapterConstants.ARTICLE_USER_TYPE, AdapterConstants.STATUS_MSG_FIELD_ERROR, correlId);
        } else {
            ActorFactory actorFactory = FabricRegistry.getActorFactory(QueryScope.LOCAL);
            Actor actor = actorFactory.getActorById(userId);
            boolean complete = actorFactory.delete(actor);
            if (complete == false) {
                status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_DEREGISTER,
                        AdapterConstants.ARTICLE_USER, AdapterConstants.STATUS_MSG_FAILED_DELETE, correlId);
            }
        }
        return status.toJsonObject();
    }

    /**
     * Queries the database for Users.
     *
     * @param op
     * @param correlId
     * @return The result of the query.
     */

    public static JSON query(JSON op, final String correlId) {

        JSON usersQueryResult = new JSON();
        AdapterStatus status = new AdapterStatus(correlId);
        List<JSON> jsonList = new ArrayList<JSON>();

        try {

            ActorFactory userFactory = FabricRegistry.getActorFactory();
            String querySQL = generatePredicate(AdapterConstants.FIELD_ID, "ACTOR_ID", QUERY_AFFILIATION
                    | QUERY_ATTRIBUTES, op);

            if (querySQL == null) {

                status = new AdapterStatus(AdapterConstants.ERROR_PARSE, AdapterConstants.OP_CODE_QUERY,
                        AdapterConstants.ARTICLE_USER, AdapterConstants.STATUS_MSG_BAD_SQL, correlId);
                usersQueryResult = status.toJsonObject();

            } else {

                /* Lookup the user list in the Registry */

                Actor[] resultArray = null;

                if ("".equals(querySQL)) {
                    resultArray = userFactory.getAllActors();
                } else {
                    resultArray = userFactory.getActors(querySQL);
                }

                /* Generate the response object */

                usersQueryResult.putString(AdapterConstants.FIELD_OPERATION, AdapterConstants.OP_QUERY_RESPONSE_USERS);
                usersQueryResult.putString(AdapterConstants.FIELD_CORRELATION_ID, correlId);

                /* For each user... */
                for (int i = 0; i < resultArray.length; i++) {

                    JSON user = new JSON();
                    user.putString(AdapterConstants.FIELD_ID, resultArray[i].getId());
                    user.putString(AdapterConstants.FIELD_TYPE, resultArray[i].getTypeId());
                    addOptionalFields(user, resultArray[i].getDescription(), resultArray[i].getAttributes());
                    jsonList.add(user);

                }
                usersQueryResult.putArray(AdapterConstants.FIELD_USERS, jsonList);
            }

        } catch (Exception e) {

            String message = e.getClass().getName() + ": " + e.getMessage();
            status = new AdapterStatus(AdapterConstants.ERROR_ACTION, AdapterConstants.OP_CODE_QUERY,
                    AdapterConstants.ARTICLE_USER, message, correlId);
            usersQueryResult = status.toJsonObject();

        }

        return usersQueryResult;
    }
}
