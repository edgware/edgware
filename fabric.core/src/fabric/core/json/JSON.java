/*
 * (C) Copyright IBM Corp. 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 */
public class JSON {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    private ObjectMapper mapper = null;
    private JsonNode rootNode = null;

    /*
     * Class methods
     */

    /**
     * Constructs an empty JSON object.
     */
    public JSON() {

        mapper = new ObjectMapper();
        rootNode = mapper.createObjectNode();
    }

    /**
     * Constructs a JSON object from a string.
     *
     * @param string
     *            a string representation of a JSON object.
     *
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    public JSON(String string) throws JsonParseException, JsonMappingException, IOException {

        mapper = new ObjectMapper();
        rootNode = mapper.readValue(string, ObjectNode.class);
    }

    /**
     * Constructs a JSON object from a Jackson JSON node.
     *
     * @param node
     *            the Jackson JSON node.
     */
    public JSON(JsonNode node) {

        mapper = new ObjectMapper();
        rootNode = node;
    }

    /**
     * Answers the string value corresponding to the specified name.
     *
     * @param key
     *            the name of the value.
     *
     * @return the string value.
     */
    public String getString(String key) {

        JsonNode node = rootNode.path(key);
        String value = null;

        if (!node.isMissingNode()) {
            value = node.asText();
        }

        return value;
    }

    /**
     * Answers the <code>Double</code> value corresponding to the specified name.
     *
     * @param key
     *            the name of the value.
     *
     * @return the <code>Double</code> value.
     */
    public Double getDouble(String key) {

        JsonNode node = rootNode.path(key);
        Double value = 0.0;

        if (!node.isMissingNode()) {
            value = node.asDouble();
        }

        return value;
    }

    /**
     * Answers the <code>boolean</code> value corresponding to the specified name.
     *
     * @param key
     *            the name of the value.
     *
     * @return the <code>boolean</code> value, or <code>false</code> if there is no value.
     */
    public boolean getBoolean(String key) {

        JsonNode node = rootNode.path(key);
        boolean value = false;

        if (!node.isMissingNode()) {
            value = node.asBoolean();
        }

        return value;
    }

    /**
     * Answers the JSON object corresponding to the specified name.
     *
     * @param key
     *            the name of the value.
     *
     * @return the JSON object.
     */
    public JSON getJSON(String key) {

        JsonNode node = rootNode.path(key);
        JSON json = null;

        if (!node.isMissingNode()) {
            json = new JSON(node);
        } else {
            json = new JSON();
        }

        return json;
    }

    /**
     * Answers the JSON array corresponding to the specified name.
     *
     * @param key
     *            the name of the value.
     *
     * @return the JSON array or <code>null</code> if the value does not exist or is not an array.
     */
    public JSONArray getJSONArray(String key) {

        JsonNode node = rootNode.path(key);
        JSONArray jsonArray = null;

        if (!node.isMissingNode() && node.isArray()) {
            jsonArray = new JSONArray((ArrayNode) node);
        }

        return jsonArray;
    }

    /**
     * Sets the string value corresponding to the specified name.
     *
     * @param key
     *            the name of the value.
     *
     * @param value
     *            the new value.
     *
     * @return this instance.
     */
    public JSON putString(String key, String value) {

        if (rootNode != null && rootNode instanceof ObjectNode) {

            if (value != null && !value.equals("null")) {
                ((ObjectNode) rootNode).put(key, value);
            }

        } else {

            throw new IllegalStateException("Not an ObjectNode");

        }

        return this;
    }

    /**
     * Sets the <code>Double</code> value corresponding to the specified name.
     *
     * @param key
     *            the name of the value.
     *
     * @param value
     *            the new value.
     *
     * @return this instance.
     */
    public JSON putDouble(String key, Double value) {

        if (rootNode != null && rootNode instanceof ObjectNode) {

            if (value != null) {
                ((ObjectNode) rootNode).put(key, value);
            }

        } else {

            throw new IllegalStateException("Not an ObjectNode");

        }

        return this;
    }

    /**
     * Sets the JSON object corresponding to the specified name.
     *
     * @param key
     *            the name of the object.
     *
     * @param value
     *            the new value.
     *
     * @return this instance.
     */
    public JSON putJSON(String key, JSON value) {

        if (rootNode != null && rootNode instanceof ObjectNode) {

            if (value != null) {
                ((ObjectNode) rootNode).put(key, value.getRootNode());
            }

        } else {

            throw new IllegalStateException("Not an ObjectNode");

        }

        return this;
    }

    /**
     * Sets the JSON array corresponding to the specified name.
     *
     * @param key
     *            the name of the array.
     *
     * @param value
     *            the new value.
     *
     * @return this instance.
     */
    public JSON putJSONArray(String key, JSONArray value) {

        if (rootNode != null && rootNode instanceof ObjectNode) {

            if (value != null) {
                ((ObjectNode) rootNode).put(key, value.getRootNode());
            }

        } else {

            throw new IllegalStateException("Not an ObjectNode");

        }

        return this;
    }

    /**
     * Sets the JSON array corresponding to the specified name.
     *
     * @param key
     *            the name of the array.
     *
     * @param value
     *            the new value.
     *
     * @return this instance.
     */
    public JSON putArray(String key, List<JSON> list) {

        if (rootNode != null && rootNode instanceof ObjectNode) {

            List<JsonNode> nodes = new ArrayList<JsonNode>();

            for (JSON json : list) {
                nodes.add(json.getRootNode());
            }

            ((ObjectNode) rootNode).putArray(key).addAll(nodes);

        } else {

            throw new IllegalStateException("Not an ObjectNode");

        }

        return this;
    }

    /**
     * Answers the root node of this instance.
     *
     * @return the root node.
     */
    public JsonNode getRootNode() {

        return rootNode;
    }

    @Override
    public String toString() {

        String value = rootNode.toString();
        return value.replaceAll("^\"|\"$", "");
    }
}
