/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.json;

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

	private JsonNode rootNode;
	private ObjectMapper mapper = null;

	/*
	 * Class methods
	 */

	/*
	 * Constructors
	 */

	/**
	 * Constructor that creates a blank JSON object.
	 */
	public JSON() {

		mapper = new ObjectMapper();
		rootNode = mapper.createObjectNode();
	}

	/**
	 * Constructor that creates a JSON object based on the given string.
	 * 
	 * @param string
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public JSON(String string) throws JsonParseException, JsonMappingException, IOException {

		this();
		rootNode = mapper.readValue(string, ObjectNode.class);
	}

	/**
	 * Constructor that creates a JSON object from a Json Node object.
	 * 
	 * @param node
	 */
	public JSON(JsonNode node) {

		this();
		rootNode = node;
	}

	/*
	 * Get methods
	 */

	/**
	 * 
	 * @param key
	 * @return String value of the JSON.
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
	 * 
	 * @param key
	 * @return Double value of the JSON.
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
	 * 
	 * @param key
	 * @return a JSON object
	 */
	public JSON getJSON(String key) {

		JsonNode node = rootNode.path(key);
		JSON json = new JSON();
		if (!node.isMissingNode()) {
			json = new JSON(node);
		}
		return json;
	}

	/**
	 * 
	 * @param key
	 * @return a JSON Array
	 */
	public JSONArray getJSONArray(String key) {

		JsonNode node = rootNode.path(key);
		JSONArray jsonArray = null;
		if (!node.isMissingNode() && node.isArray()) {
			jsonArray = new JSONArray((ArrayNode) node);
		}
		return jsonArray;
	}

	/*
	 * Put Methods
	 */

	/**
	 * 
	 * @param key
	 * @param value
	 * @return
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
	 * 
	 * @param key
	 * @param value
	 * @return
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
	 * 
	 * @param key
	 * @param value
	 * @return
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
	 * 
	 * @param key
	 * @param value
	 * @return
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
	 * 
	 * @param key
	 * @param list
	 * @return
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

	/*
	 * Other Methods
	 */

	/**
	 * Answers the root JsonNode of this instance.
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

	/*
	 * Deprecated Methods
	 */
	/**
	 * 
	 * @param string
	 * @return
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@Deprecated
	public static JSON fromObject(String string) throws JsonParseException, JsonMappingException, IOException {

		return new JSON(string);
	}
}
