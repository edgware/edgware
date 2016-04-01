/*
 * (C) Copyright IBM Corp. 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.persistence.impl;

import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Class to represent and allow access to the column names of a 'row' of results from a Persistence Query
 *
 * Primarily used in conjunction with fabric.registry.persistence.impl.PersistenceResultRow.
 */

public class PersistenceResultKeys implements java.io.Serializable {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    /**
     *
     */
    private static final long serialVersionUID = 6079655378367428666L;

    private final static String CLASS_NAME = PersistenceResultKeys.class.getName();
    private final static String PACKAGE_NAME = PersistenceResultKeys.class.getPackage().getName();
    public static String JSON_COLNAMES = "colNames";

    private final static Logger logger = Logger.getLogger(PACKAGE_NAME);

    private List<String> keys = new Vector<String>();

    /**
     * Construct keys from JsonNode
     */
    public PersistenceResultKeys(JsonNode node) {
        // Verify this JsonNode is an array of Strings
        if (node.isArray()) {
            for (Iterator<JsonNode> iterator = node.elements(); iterator.hasNext();) {
                JsonNode colName = iterator.next();
                if (colName.isTextual()) {
                    keys.add(colName.asText());
                } else {
                    keys.add("UNKNOWN");
                }
            }
        }
    }

    /**
     * Construct keys from resultset metadata
     */
    public PersistenceResultKeys(ResultSetMetaData rsMetaData) {

        keys = new Vector<String>();
        try {
            for (int i = 0; i < rsMetaData.getColumnCount(); i++) {
                keys.add(rsMetaData.getColumnLabel(i + 1));
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to process result set meta data: ", e);
        }
    }

    public boolean isEmpty() {

        return keys.isEmpty();
    }

    public int size() {

        return keys.size();
    }

    /**
     *
     * @param index
     *            the first key is 1, the second is 2
     * @return
     */
    public String get(int index) {

        return keys.get(index - 1);
    }

    public int getIndex(String key) {

        return keys.indexOf(key);
    }

    @Override
    public String toString() {

        String resultString = "";
        for (int i = 0; i < keys.size(); i++) {
            resultString = resultString + keys.get(i) + "\t";
        }
        return resultString;
    }

    /**
     * Generate Json for these keys using jsonGenerator provided.
     *
     * @param jsonGenerator
     * @throws JsonGenerationException
     * @throws IOException
     */
    public void toJson(JsonGenerator jsonGenerator) throws JsonGenerationException, IOException {
        // column headings
        jsonGenerator.writeArrayFieldStart(JSON_COLNAMES);
        for (int i = 0; i < keys.size(); i++) {
            String colName = keys.get(i);
            jsonGenerator.writeString(colName);
        }
        jsonGenerator.writeEndArray();
    }

}
