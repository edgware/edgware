/*
 * (C) Copyright IBM Corp. 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.json;

import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 */
public class JSONArray implements Iterable<JSON> {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    ArrayNode rootNode;
    ObjectMapper mapper = null;

    /*
     * Class methods
     */

    /**
     * Constructs an empty JSON array.
     */
    public JSONArray() {

        mapper = new ObjectMapper();
        rootNode = mapper.createArrayNode();
    }

    /**
     * Constructs a JSON array from a Jackson array node.
     *
     * @param node
     *            the Jackson array node.
     */
    public JSONArray(ArrayNode node) {

        mapper = new ObjectMapper();
        rootNode = node;
    }

    /**
     * Answers the array element at the specified index.
     *
     * @param i
     *            the index.
     *
     * @return the value, or an empty JSON object if the element is not an object.
     */
    public JSON getJSON(int i) {

        JsonNode node = rootNode.get(i);
        JSON json = null;

        if (node.isObject()) {
            json = new JSON(node);
        } else {
            json = new JSON();
        }

        return json;
    }

    /**
     * Answers the string value at the specified index.
     *
     * @param i
     *            the index.
     *
     * @return the string value, or an empty string if the value is not a text node.
     */
    public String getString(int i) {

        JsonNode node = rootNode.get(i);
        String string = "";

        if (node.isTextual()) {
            string = node.asText();
        }

        return string;
    }

    /**
     * Adds a value to the array.
     *
     * @param value
     *            the new value.
     */
    public void add(String value) {

        rootNode.add(value);
    }

    /**
     * Adds a value to the array.
     *
     * @param value
     *            the new value.
     */
    public void add(JSON value) {

        rootNode.add(value.getRootNode());
    }

    /**
     * Answers the number of elements in the array.
     *
     * @return the number of elements in the array.
     */
    public int size() {

        return rootNode.size();
    }

    /**
     * Answers the root node.
     *
     * @return the root node.
     */
    public ArrayNode getRootNode() {

        return rootNode;
    }

    public JSONArray putStringList(List<String> list) {

        if (!list.isEmpty()) {
            for (String s : list) {
                rootNode.add(s);
            }
        }
        return this;
    }

    /**
     * Iterates over the elements of the array.
     */
    @Override
    public Iterator<JSON> iterator() {

        Iterator<JSON> it = new Iterator<JSON>() {

            private int index = 0;

            @Override
            public boolean hasNext() {

                return index < rootNode.size();
            }

            @Override
            public JSON next() {

                return new JSON(rootNode.get(index++));
            }

            @Override
            public void remove() {

            }

        };

        return it;
    }

    @Override
    public String toString() {

        return rootNode.toString();
    }
}
