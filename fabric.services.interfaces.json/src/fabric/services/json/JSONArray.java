/*
 * Licensed Materials - Property of IBM
 *  
 * (C) Copyright IBM Corp. 2014
 * 
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.json;

import java.util.ArrayList;
import java.util.Iterator;

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
	 * 
	 */
	public JSONArray() {

		mapper = new ObjectMapper();
		rootNode = mapper.createArrayNode();
	}

	/**
	 * 
	 * @param node
	 */
	public JSONArray(ArrayNode node) {

		this();
		rootNode = node;
	}

	/**
	 * 
	 */
	@Override
	public String toString() {

		return rootNode.toString();
	}

	/**
	 * 
	 * @param i
	 * @return
	 */
	public JSON getJSON(int i) {

		JsonNode node = rootNode.get(i);
		JSON json = new JSON();
		if (node.isObject()) {
			json = new JSON(node);
		}
		return json;
	}

	/**
	 * 
	 * @param i
	 * @return
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
	 * 
	 * @return
	 */
	public int size() {

		int i = 0;
		i = rootNode.size();
		return i;
	}

	/**
	 * 
	 * @return
	 */
	public ArrayNode getRootNode() {

		return rootNode;
	}

	public JSONArray putStringList(ArrayList<String> list) {

		if (!list.isEmpty()) {
			for (String s : list) {
				rootNode.add(s);
			}
		}
		return this;
	}

	/**
	 * 
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

}
