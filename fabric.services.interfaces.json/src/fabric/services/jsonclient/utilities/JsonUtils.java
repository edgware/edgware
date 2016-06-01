/*
 * (C) Copyright IBM Corp. 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.jsonclient.utilities;

import java.io.IOException;

import fabric.core.json.JSON;

/**
 * This class provides some null gating utilities for JSON objects.
 */
public class JsonUtils {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    /*
     * Class methods
     */

    /**
     * Answers a JSON object corresponding to a string containing JSON.
     * <p>
     * If the string does not contain valid JSON then the specified error message is returned in a JSON object of the
     * form:
     * </p>
     * 
     * <pre>
     * {
     *    "$error":"&lt;errorMessage&gt;",
     *    "$value":"&lt;jsonString&gt;"
     * }
     * </pre>
     * 
     * @param jsonString
     *            the JSON string to parse into a JSON object.
     * 
     * @param errorMessage
     *            the error message to use.
     * 
     * @return the JSON object, or an error message encoded in a JSON object, or <code>null</code> if there are JSON
     *         parsing errors building the return value.
     */
    public static JSON stringTOJSON(String jsonString, String errorMessage) {

        JSON json = null;

        try {

            if (jsonString != null && !jsonString.equals("null") && !jsonString.equals("")) {
                json = new JSON(jsonString);
            }

        } catch (Exception e) {

            try {
                String errorJSON = String.format("{\"$error\":\"%s\",\"$value\":\"%s\"}", errorMessage, jsonString);
                json = new JSON(errorJSON);
            } catch (IOException e1) {
                /* Just return null */
            }

        }

        return json;
    }
}
