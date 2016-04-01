/*
 * (C) Copyright IBM Corp. 2014
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */
package fabric.registry.persistence.distributed;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DistributedQuery {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2014";

    private final static String CLASS_NAME = DistributedQuery.class.getName();
    private final static String PACKAGE_NAME = DistributedQuery.class.getPackage().getName();

    private final static Logger logger = Logger.getLogger(PACKAGE_NAME);

    // Json
    static public String JSON_DISTRIBUTED_QUERY = "distributedQuery";
    static public String JSON_SQL = "sql";

    private String query = null;

    public DistributedQuery(String query) {
        this.query = query;
    }

    /**
     * build object from serialise bytes
     *
     * @param bytes
     * @param format
     */
    public DistributedQuery(byte[] bytes, String format) {
        switch (format) {
            case "json":
                constructFromJson(bytes);
                break;
            default:
                logger.warning("Format for distributedQueryResult Serialisation not supported.");
                break;
        }
    }

    /**
     * Populate Object from bytes of Json
     *
     * @param bytes
     */
    private void constructFromJson(byte[] json) {
        logger.finer("Query JSON: " + new String(json));
        try {
            ObjectMapper jsonObjectMapper = new ObjectMapper();
            JsonNode rootNode = jsonObjectMapper.readTree(json);
            // Only one distributedQuery object expected.
            JsonNode distributedQuery = rootNode.findValue(JSON_DISTRIBUTED_QUERY);
            // This should have a sql object
            JsonNode sql = distributedQuery.findValue(JSON_SQL);
            this.query = sql.asText();

        } catch (JsonProcessingException e) {
            logger.warning("Error parsing Json : " + e.toString());
        } catch (IOException e) {
            logger.warning("Error parsing Json : " + e.toString());
        }
    }

    public String getQuery() {
        return query;
    }

    public String toJsonString() {
        String jsonString = null;

        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            JsonGenerator jsonGenerator = new JsonFactory().createGenerator(stream);
            // jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
            toJson(jsonGenerator);
            jsonGenerator.flush();
            jsonGenerator.close();
            jsonString = stream.toString("UTF-8");
        } catch (IOException e) {
            logger.warning("Problem building json " + e.getMessage());
        }
        logger.finest("JsonString = " + jsonString);
        return jsonString;
    }

    private void toJson(JsonGenerator jsonGenerator) throws JsonGenerationException, IOException {
        jsonGenerator.writeStartObject(); // start root object
        jsonGenerator.writeObjectFieldStart(JSON_DISTRIBUTED_QUERY); // start distributedquery
        jsonGenerator.writeStringField(JSON_SQL, query); // nodename
        jsonGenerator.writeEndObject(); // end distributedquery
        jsonGenerator.writeEndObject(); // closing root object
    }

    @Override
    public String toString() {
        // For now return Json representation
        return toJsonString();
    }

}
