/*
 * (C) Copyright IBM Corp. 2006, 2010
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.xml;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;

import fabric.core.util.Formatter;

/**
 * Base class for simple XML parsing.
 * <p>
 * The class enables XML files and strings to be parsed and their contents queried. New XML files can also be created
 * in-memory and serialized. The class uses a simple XPath-<em>like</em> syntax to identify nodes (i.e.
 * <code>element/sub-element1/sub-element2/...</code>).
 * </p>
 * <p>
 * A path to an XML node is represented as a set of "<code>/</code>" separated segments, for example:
 * </p>
 * <p>
 * <code>/book/author</code>
 * </p>
 * <p>
 * Each segment has an index off the form "<code>[n]</code>" to indicate a specific instance of the node type within the
 * parent element. This defaults to "<code>[0]</code>" if omitted. Thus the previous path is more formally written as:
 * </p>
 * <p>
 * <code>/book[0]/author[0]</code>
 * </p>
 * <p>
 * A second author entry in the same XML file would be expressed as:
 * </p>
 * <p>
 * <code>/book[0]/author[1]</code>
 * </p>
 * <p>
 * If author elements are mixed with editor elements then we might have:
 * </p>
 * <p>
 * <code>/book[0]/author[0]</code> <code>/book[0]/editor[0]</code> <code>/book[0]/author[1]</code>
 * </p>
 * Note that the order of the elements in the XML is preserved.
 * <p>
 * The path to the <em>content</em> associated with an element is written in the same way, except that the name is
 * always "<code>$</code>":
 * </p>
 * <p>
 * <code>/book[0]/author[0]/$[0]</code>
 * </p>
 * <p>
 * Methods are provided that allow developers to use regular expressions to find XML elements.
 * </p>
 */
public class XML implements IXMLTokenHandler {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2006, 2010";

    /*
     * Class constants
     */

    /* Constants for standard XML entity references */

/** Embedded XML "<" character */
    public static final String XML_LESS_THAN = "&lt;";

    /** Embedded XML ">" character */
    public static final String XML_GREATER_THAN = "&gt;";

    /** Embedded XML "&" character */
    public static final String XML_AMPERSAND = "&amp;";

    /** Embedded XML "'" character */
    public static final String XML_APOSTROPHE = "&apos;";

    /** Embedded XML '"' character */
    public static final String XML_QUOTE = "&quot;";

    /** Indent for pretty-printing the generated XML */
    private static final char XML_INDENT = '\t';
    // private static final char XML_INDENT = ' ';

    /** Regular expression for a path to a text node. */
    private static final String REGEXP_TEXT_NODE_PATH = "/.*/\\$(\\[.*\\]|$)";

    /*
     * Class fields
     */

    /** The XML to be parsed */
    protected String xmlString = null;

    /** The name of a file containing the XML to be parsed */
    protected String xmlFile = null;

    /** The stream containing the XML to be parsed */
    protected InputStream xmlStream = null;

    /** The list of processing instructions for this XML */
    protected ArrayList<String> piList = new ArrayList<String>();

    /** To hold the document element */
    protected XMLElement documentElement = null;

    /** Flag indicating if the XML document has change since it was last saved */
    protected boolean isDirty = false;

    /** Flag indicating if the XML document is read-only or read-write */
    protected boolean isReadOnly = false;

    /** The current string representation of the XML document */
    protected String toXML = null;

    /** The current byte representation of the XML document */
    protected byte[] toBytes = null;

    /** Stack of XML elements, used in the parsing process to track element nesting */
    protected Stack<XMLElement> elementStack = new Stack<XMLElement>();

    /** Flag indicating if mixed content is recognized */
    protected boolean noMixedContent = true;

    /** Flag indicating if processing instructions should be included in generated XML */
    protected boolean includePIs = false;

    /** Used to track if a single content node is broken into multiple chunks by the parser */
    protected XMLText currentText = null;

    /** Precompiled regular expression for matching a path to a text node */
    protected static Pattern regexpTextNodePath = null;

    /*
     * Class static initialization
     */

    static {

        try {
            regexpTextNodePath = Pattern.compile(REGEXP_TEXT_NODE_PATH);
        } catch (Exception e) {
            /* Internal error */
            e.printStackTrace();
        }

    }

    /*
     * Inner classes
     */

    public class XMLIterator implements Iterator<XMLNode> {

        /*
         * Class fields
         */

        /** The list of elements to be iterated across */
        private final ArrayList<String> pathList = new ArrayList<String>();

        /** An internal iterator for the element list */
        private Iterator<String> pathListIterator = null;

        /** The current iteration element */
        private String current = null;

        /**
         * Constructs a new instance.
         *
         * @param regexp
         *            the regular expression for the paths of the XML elements to be iterated across.
         */
        private XMLIterator(String regexp) {

            /* Get the list of paths */
            String[] paths = getPaths(regexp);

            /* Convert to a collection */
            pathList.addAll(Arrays.asList(paths));

            /* Prepare to iterate across the list */
            pathListIterator = pathList.iterator();
        }

        /*
         * (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {

            return pathListIterator.hasNext();
        }

        /*
         * (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        @Override
        public XMLNode next() {

            /* Get the next path */
            current = pathListIterator.next();

            /* Return the value of the element */
            return getNode(current);
        }

        /*
         * (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {

            /*
             * Remove the element (and its sub-elements, so the user needs to be careful)
             */
            XML.this.remove(current);
        }

    }

    /*
     * Class methods
     */

    public static void main(String[] cla) throws Exception {

        FileInputStream fis = new FileInputStream("Test.xml");
        XML xml1 = new XML(fis);
        xml1.setIncludePIs(false);
        byte[] bytes = xml1.toBytes();

        XML xml2 = new XML();
        xml2.set("/payload/data", xml1.toString());

        String xmlPayload = xml2.get("/payload/data");

        StringBuilder xml = new StringBuilder();

        /* Mixed content */
        xml.append("<text encodedRef=\"&lt;the&gt;\">\n");
        xml.append("\t<para>the <em>cat</em> &apos;sat&apos; on the <em>&quot;mat&quot;</em></para>\n");
        xml.append("\t<para>hmm, &amp; so did &lt;the&gt; <em>dog</em>!</para>\n");
        xml.append("</text>\n");

        /* No mixed content */
        // xml.append("<text>\n");
        // xml.append("\t<para><em>cat</em><em>&quot;mat&quot;</em></para>\n");
        // xml.append("\t<para>hmm, &amp; so did &lt;the&gt;</para>\n");
        // xml.append("</text>\n");
        XML doc = new XML();
        doc.setMixedContent(false);
        try {
            doc.parseString(xml.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(doc.get("/text@encodedRef"));
        doc.set("/text@encodedRef", "<ook, a new value>");
        System.out.println(doc.toXML());
        System.out.println(doc.get("/text@encodedRef"));

        System.out.println(doc.get("/text/para[0]/$[0]"));
        System.out.println(doc.get("/text/para[0]/$[1]"));
        System.out.println(doc.toXML());

        doc.remove("/text[0]/para[0]/em[0]");

        doc.set("/text/para[3]", "the third paragraph!");
        doc.set("/text/para[2]@font", "times");
        doc.set("/text/para[2]", "go go go!");
        System.out.println("/text/para[5]@font = " + doc.get("/text/para[5]@font"));

        System.out.println(doc.toXML());

        String[] paths = doc.getPaths();
        for (int p = 0; p < paths.length; p++) {
            System.out.println(paths[p]);
        }
    }

    /**
     * Constructs a new instance.
     */
    public XML() {

        setDefaultPIs();
        setDirty(false);

    }

    /**
     * Constructs a new instance from the XML contained in the specified string.
     *
     * @param xmlString
     *            the XML.
     *
     * @throws Exception
     *             thrown if there is a problem parsing the XML.
     */
    public XML(String xmlString) throws Exception {

        parseString(xmlString);

    }

    /**
     * Constructs a new instance from the XML contained in the specified byte array.
     *
     * @param xmlBytes
     *            the XML.
     *
     * @throws Exception
     *             thrown if there is a problem parsing the XML.
     */
    public XML(byte[] xmlBytes) throws Exception {

        parseBytes(xmlBytes);

    }

    /**
     * Constructs a new instance from the XML contained in the specified file.
     *
     * @param xmlStream
     *            the XML.
     *
     * @throws Exception
     *             thrown if there is a problem parsing the XML.
     */
    public XML(InputStream xmlStream) throws Exception {

        parseFile(xmlStream);

    }

    /**
     * Sets the flag indicating that this XML document is read-only.
     *
     * @param isReadOnly
     *            <code>true</code> if the XML document is to be made read-only, <code>false</code> otherwise.
     */
    public void setReadOnly(boolean isReadOnly) {

        this.isReadOnly = isReadOnly;

    }

    /**
     * Gets the flag indicating that this XML document is read-only.
     *
     * @return <code>true</code> if the XML document is read-only, <code>false</code> otherwise.
     */
    public boolean isReadOnly() {

        return isReadOnly;

    }

    /**
     * Sets the flag indicating that this XML document has been modified since it was last saved.
     *
     * @param isDirty
     *            <code>true</code> if the XML document has been modified, <code>false</code> otherwise.
     */
    public void setDirty(boolean isDirty) {

        /* If the model has been updated... */
        if (isDirty) {

            /*
             * Reset the cached copies of the string and byte representations of the XML document
             */
            toXML = null;
            toBytes = null;

        }

        this.isDirty = isDirty;

    }

    /**
     * Gets the flag indicating that this XML document has been modified since it was last saved.
     *
     * @return <code>true</code> if the XML document has been modified, <code>false</code> otherwise.
     */
    public boolean isDirty() {

        return isDirty;

    }

    /**
     * Sets the flag indicating if mixed content is recognized.
     *
     * @param mixedContent
     *            <code>true</code> if the parser should treat all whitespace as XML text nodes, <code>false</code>
     *            otherwise.
     */
    public void setMixedContent(boolean mixedContent) {

        this.noMixedContent = !mixedContent;

    }

    /**
     * Parses the XML contained in the specified string.
     *
     * @param xmlString
     *            the XML.
     *
     * @throws Exception
     *             thrown if there is a problem parsing the XML.
     */
    public void parseString(String xmlString) throws Exception {

        this.xmlString = xmlString;
        this.xmlFile = null;
        this.xmlStream = null;
        parseXML();

    }

    /**
     * Parses the XML contained in the specified byte array.
     *
     * @param xmlBytes
     *            the XML.
     *
     * @throws Exception
     *             thrown if there is a problem parsing the XML.
     */
    public void parseBytes(byte[] xmlBytes) throws Exception {

        parseString(new String(xmlBytes));

    }

    /**
     * Parses the XML contained in the specified file.
     *
     * @param xmlFile
     *            the name of the XML file.
     *
     * @throws Exception
     *             thrown if there is a problem parsing the XML.
     */
    public void parseFile(String xmlFile) throws Exception {

        this.xmlFile = xmlFile;
        this.xmlString = null;
        this.xmlStream = null;
        parseXML();

    }

    /**
     * Parses the XML contained in the specified stream.
     *
     * @param xmlStream
     *            the XML.
     *
     * @throws Exception
     *             thrown if there is a problem parsing the XML.
     */
    public void parseFile(InputStream xmlStream) throws Exception {

        this.xmlStream = xmlStream;
        this.xmlFile = null;
        this.xmlString = null;
        parseXML();

    }

    /**
     * Encode the current data into its XML equivalent.
     *
     * @return the XML form of the data.
     */
    public String toXML() {

        /* If we haven't already encoded the current document... */
        if (toXML == null) {

            /* To hold the encoded XML */
            StringBuilder xml = new StringBuilder();

            if (includePIs) {

                /* Add the processing instructions */

                Iterator<String> pis = piList.iterator();

                while (pis.hasNext()) {
                    xml.append("<?");
                    xml.append(pis.next());
                    xml.append("?>\n");
                }
            }

            /* Covert to XML */
            toXML(documentElement, xml, "");
            toXML = xml.toString();

        }

        return toXML;

    }

    /**
     * Sets the flag indicating if processing instructions should be included in the generated XML.
     *
     * @param includePIs
     *            <code>true</code> if generated XML should include processing instructions, <code>false</code>
     *            otherwise.
     */
    public void setIncludePIs(boolean includePIs) {

        this.includePIs = includePIs;

    }

    /**
     * Save the XML to a file.
     *
     * @throws Exception
     *             thrown if there is a problem writing the XML.
     */
    public void write() throws Exception {

        if (xmlFile == null) {
            throw new IllegalStateException("No XML file name assigned");
        }

        write(xmlFile);

    }

    /**
     * Save the XML to a file.
     *
     * @param xmlFile
     *            the path to the XML file.
     *
     * @throws Exception
     *             thrown if there is a problem parsing the XML.
     */
    public void write(String xmlFile) throws Exception {

        /* Write the file */
        Writer xmlWriter = new FileWriter(xmlFile);
        xmlWriter.write(toXML());
        xmlWriter.close();
        setDirty(false);

        /* The new file is now the current file */
        this.xmlFile = xmlFile;
        this.xmlString = null;
        this.xmlStream = null;

    }

    /**
     * Gets the name of a file containing the XML to be parsed.
     *
     * @return the name of the XML file.
     */
    public String getSourceFile() {

        return xmlFile;

    }

    /**
     * Gets the XML to be parsed.
     *
     * @return the XML.
     */
    public String getSourceString() {

        return xmlString;

    }

    /**
     * Gets the stream containing the XML to be parsed.
     *
     * @return the XML stream.
     */
    public InputStream getSourceStream() {

        return xmlStream;

    }

    /**
     * Gets the path of the document (root) element.
     *
     * @return the element name, or <code>null</code> if the document is empty.
     */
    public String getDocumentElementPath() {

        String documentElementName = null;

        if (documentElement != null) {
            documentElementName = "/" + documentElement.getNodeName();
        }

        return documentElementName;

    }

    /**
     * Adds a processing instruction (PI) to the XML.
     * <p>
     * PIs will appear in the XML file in the order in which they are added by this method.
     * </p>
     *
     * @param pi
     *            the processing instruction to add (note that the <code>&lt;?</code> and <code>&gt;?</code> delimiters
     *            should not be included).
     */
    public void addPI(String pi) {

        if (isReadOnly) {
            throw new UnsupportedOperationException("XML document is read-only");
        }

        if (!piList.contains(pi)) {
            piList.add(pi);
            setDirty(true);
        }
    }

    /**
     * Removes a processing instruction (PI) from the XML.
     *
     * @param pi
     *            the processing instruction to remove (note that the <code>&lt;?</code> and <code>&gt;?</code>
     *            delimiters should not be included).
     *
     * @return <code>true</code> if the processing instruction was removed, <code>false</code> otherwise.
     */
    public boolean removePI(String pi) {

        if (isReadOnly) {
            throw new UnsupportedOperationException("XML document is read-only");
        }

        boolean result = piList.remove(pi);
        setDirty(true);

        return result;

    }

    /**
     * Gets the current list of processing instructions for the XML.
     *
     * @return the list of processing instructions.
     */
    public String[] getPIs() {

        String[] pis = piList.toArray(new String[piList.size()]);
        return pis;

    }

    /**
     * Gets the value of the element or element attribute at the specified path.
     * <p>
     * Paths that omit the element index for any elements are expanded to a fully qualified path. A default index of "
     * <code>[0]</code>" is assumed where necessary.
     * </p>
     * <p>
     * If the path ends with "<code>@<em>name</em></code>" then it is assumed to be the name of an attribute of the
     * element.
     * </p>
     *
     * @param path
     *            the external format path.
     *
     * @return the value, or <code>null</code> if it is not defined.
     */
    public String get(String path) {

        String value = null;

        /* If this is not an attribute path and it is not terminated properly to match a text node... */
        if (path.indexOf('@') < 0 && !(regexpTextNodePath.matcher(path).matches())) {
            path = path + "/$";
        }

        /* Expand the path and convert it to its internal format */
        ArrayList<XMLPathSegment> segments = XMLPathSegment.split(path);
        XMLPathSegment lastSegment = segments.get(segments.size() - 1);

        /* Get the node */
        XMLNode node = findNode(segments, false);

        /* If an attribute was requested... */
        if (lastSegment.getAttributeName().length() > 0) {

            /* Get it */
            XMLElement element = (XMLElement) node;
            value = (element != null) ? (String) element.getAttributes().get(lastSegment.getAttributeName()) : null;

        } else if (node != null) {

            /* Get the value */
            XMLText content = (XMLText) node;
            value = (content != null) ? content.getText() : null;

        }

        return value;
    }

    /**
     * As <code>get(<em>path</em>)</code>, but with support for inserts in the path.
     *
     * @param path
     *            the path.
     *
     * @param inserts
     *            the inserts.
     *
     * @return the value, or <code>null</code> if it is not defined.
     */
    public String get(String path, Object... inserts) {

        return get(format(path, inserts));

    }

    /**
     * As <code>get(<em>path</em>)</code>, but interprets the value as an integer.
     *
     * @param path
     *            the path.
     *
     * @return the value, or <code>0</code> if it is not defined.
     */
    public int getInt(String path) {

        String result = get(path);

        if (result == null) {
            result = "0";
        }

        return Integer.parseInt(result);

    }

    /**
     * As <code>getInt(<em>path</em>)</code>, but with support for inserts in the path.
     *
     * @param path
     *            the path.
     *
     * @param inserts
     *            the inserts.
     *
     * @return the value, or <code>0</code> if it is not defined.
     */
    public int getInt(String path, Object... inserts) {

        String result = get(path, inserts);

        if (result == null) {
            result = "0";
        }

        return Integer.parseInt(result);

    }

    /**
     * As <code>get(<em>path</em>)</code>, but interprets the value as a boolean.
     *
     * @param path
     *            the path.
     *
     * @return the value, or <code>false</code> if it is not defined.
     */
    public boolean getBoolean(String path) {

        String result = get(path);

        if (result == null) {
            result = "false";
        }

        return Boolean.parseBoolean(result);

    }

    /**
     * As <code>getBoolean(<em>path</em>)</code>, but with support for inserts in the path.
     *
     * @param path
     *            the path.
     *
     * @param inserts
     *            the inserts.
     *
     * @return the value, or <code>false</code> if it is not defined.
     */
    public boolean getBoolean(String path, Object... inserts) {

        String result = get(path, inserts);

        if (result == null) {
            result = "false";
        }

        return Boolean.parseBoolean(result);

    }

    /**
     * As <code>get(<em>path</em>)</code>, but interprets the value as a byte.
     *
     * @param path
     *            the path.
     *
     * @return the value, or <code>0</code> if it is not defined.
     */
    public byte getByte(String path) {

        String result = get(path);

        if (result == null) {
            result = "0";
        }

        return Byte.parseByte(result);

    }

    /**
     * As <code>get(<em>path</em>)</code>, but interprets the value as a Base 64 encoded byte array.
     *
     * @param path
     *            the path.
     */
    public byte[] getBytes(String path) {

        /* To hold the result */
        byte[] result = null;

        /* Get the value from the XML file */
        String xmlValue = get(path);

        /* If the value exists... */
        if (xmlValue != null) {

            try {

                /* Decode the byte array */
                result = Base64.decodeBase64(xmlValue.getBytes("UTF-8"));

            } catch (IOException e) {

                /* Wrap and re-throw as a runtime exception */
                throw new RuntimeException(e);

            }

        } else {

            /* Nothing to return */
            result = new byte[0];

        }

        return result;

    }

    /**
     * As <code>getByte(<em>path</em>)</code>, but with support for inserts in the path.
     *
     * @param path
     *            the path.
     *
     * @param inserts
     *            the inserts.
     *
     * @return the value, or <code>0</code> if it is not defined.
     */
    public byte getByte(String path, Object... inserts) {

        String result = get(path, inserts);

        if (result == null) {
            result = "0";
        }

        return Byte.parseByte(result);

    }

    /**
     * As <code>get(<em>path</em>)</code>, but interprets the value as a double.
     *
     * @param path
     *            the path.
     *
     * @return the value, or <code>0.0</code> if it is not defined.
     */
    public double getDouble(String path) {

        String result = get(path);

        if (result == null) {
            result = "0.0";
        }

        return Double.parseDouble(result);

    }

    /**
     * As <code>getDouble(<em>path</em>)</code>, but with support for inserts in the path.
     *
     * @param path
     *            the path.
     *
     * @param inserts
     *            the inserts.
     *
     * @return the value, or <code>0</code> if it is not defined.
     */
    public double getDouble(String path, Object... inserts) {

        String result = get(path, inserts);

        if (result == null) {
            result = "0.0";
        }

        return Double.parseDouble(result);

    }

    /**
     * Sets the value of the element or element attribute at the specified path.
     * <p>
     * Paths that omit the element index for any elements are expanded to a fully qualified path. A default index of "
     * <code>[0]</code>" is assumed where necessary.
     * </p>
     * <p>
     * If the path ends with "<code>@<em>name</em></code>" then it is assumed to be the name of an attribute of the
     * element.
     * </p>
     * <p>
     * If the element does not exist then it will be created. If <code>value</code> is null then the element will be
     * deleted.
     * </p>
     *
     * @param path
     *            the external format path.
     * @param value
     *            the value.
     */
    public void set(String path, String value) {

        if (isReadOnly) {
            throw new UnsupportedOperationException("XML document is read-only");
        }

        // if (value == null) {
        // throw new IllegalArgumentException("'value' parameter cannot be null");
        // }

        if (value != null) {

            /*
             * If this is not an attribute path and it is not terminated properly to match a text node...
             */
            if (path.indexOf('@') < 0 && !(regexpTextNodePath.matcher(path).matches())) {
                path = path + "/$";
            }

            /* Expand the path and convert it to its internal format */
            ArrayList<XMLPathSegment> segments = XMLPathSegment.split(path);
            XMLPathSegment lastSegment = segments.get(segments.size() - 1);

            /* Get the node */
            XMLNode node = findNode(segments, true);

            /* If an attribute was specified... */
            if (lastSegment.getAttributeName().length() > 0) {

                /* Set it */
                XMLElement element = (XMLElement) node;

                if (value != null) {
                    /* Set it */
                    element.getAttributes().put(lastSegment.getAttributeName(), value);
                } else {
                    /* Remove it */
                    element.getAttributes().remove(lastSegment.getAttributeName());
                }

            } else {

                /* Get the value */
                XMLText content = (XMLText) node;
                content.setText(value);

            }

            setDirty(true);

        } else {

            /* Remove the element */
            remove(path);

        }

    }

    /**
     * As <code>set(<em>path</em>, <em>value</em>)</code>, but with support for inserts in the path.
     *
     * @param path
     *            the path
     * @param value
     *            the new value.
     * @param inserts
     *            the inserts
     */
    public void set(String path, String value, Object... inserts) {

        set(format(path, inserts), value);

    }

    /**
     * As <code>set(<em>path</em>, <em>value</em>, <em>inserts</em>)</code>, but the value is only set if the value is
     * not null or an empty string.
     *
     * @param path
     *            the path
     * @param value
     *            the new value.
     * @param inserts
     *            the inserts
     */
    public void setIfNotEmpty(String path, String value, Object... inserts) {

        /* If the value is not null or the empty string... */
        if (value != null && !value.equals("")) {

            /* If there aren't any inserts... */
            if (inserts == null) {
                set(path, value);
            } else {
                set(path, value, inserts);
            }
        }

    }

    /**
     * As <code>set(<em>path</em>, <em>value</em>)</code>, but the value must be an integer.
     *
     * @param path
     *            the path.
     *
     * @param value
     *            the new value.
     */
    public void setInt(String path, int value) {

        set(path, Integer.toString(value));

    }

    /**
     * As <code>setInt(<em>path</em>, <em>value</em>)</code>, but with support for inserts in the path.
     *
     * @param path
     *            the path.
     *
     * @param value
     *            the new value.
     *
     * @param inserts
     *            the inserts.
     */
    public void setInt(String path, int value, Object... inserts) {

        set(path, Integer.toString(value), inserts);

    }

    /**
     * As <code>set(<em>path</em>, <em>value</em>)</code>, but the value must be a boolean.
     *
     * @param path
     *            the path.
     *
     * @param value
     *            the new value.
     */
    public void setBoolean(String path, boolean value) {

        set(path, Boolean.toString(value));

    }

    /**
     * As <code>setBoolean(<em>path</em>, <em>value</em>)</code>, but with support for inserts in the path.
     *
     * @param path
     *            the path.
     *
     * @param value
     *            the new value.
     *
     * @param inserts
     *            the inserts.
     */
    public void setBoolean(String path, boolean value, Object... inserts) {

        set(path, Boolean.toString(value), inserts);

    }

    /**
     * As <code>setByte(<em>path</em>, <em>value</em>)</code>, but with support for inserts in the path.
     *
     * @param path
     *            the path.
     *
     * @param value
     *            the new value.
     *
     * @param inserts
     *            the inserts.
     */
    public void setBoolean(String path, byte value, Object... inserts) {

        set(path, Byte.toString(value), inserts);

    }

    /**
     * As <code>set(<em>path</em>, <em>value</em>)</code>, but the value must be a byte.
     *
     * @param path
     *            the path.
     *
     * @param value
     *            the new value.
     */
    public void setByte(String path, byte value) {

        set(path, Byte.toString(value));

    }

    /**
     * As <code>set(<em>path</em>, <em>value</em>)</code>, but the value must be a byte array.
     * <p>
     * Note that the byte array will be Base 64 encoded before being added to the XML.
     * </p>
     *
     * @param path
     *            the path.
     *
     * @param value
     *            the new value.
     */
    public void setBytes(String path, byte[] value) {

        if (value == null) {
            throw new IllegalArgumentException("'value' parameter cannot be null");
        }

        /* Encode the byte array */
        String encodedBytes = null;
        try {
            encodedBytes = new String(Base64.encodeBase64(value), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        /* Add it to the XML */
        set(path, encodedBytes);

    }

    /**
     * As <code>setDouble(<em>path</em>, <em>value</em>)</code>, but with support for inserts in the path.
     *
     * @param path
     *            the path.
     *
     * @param value
     *            the new value.
     *
     * @param inserts
     *            the inserts.
     */
    public void setDouble(String path, double value, Object... inserts) {

        set(path, Double.toString(value), inserts);

    }

    /**
     * As <code>set(<em>path</em>, <em>value</em>)</code>, but the value must be a double.
     *
     * @param path
     *            the path.
     *
     * @param value
     *            the new value.
     */
    public void setDouble(String path, double value) {

        set(path, Double.toString(value));

    }

    /**
     * Removes the specified node at the specified path.
     * <p>
     * Paths that path omit the element index for any elements are expanded to a fully qualfied path. A default index of
     * "<code>[0]</code>" is assumed where necessary.
     * </p>
     * <p>
     * If the element does not exist then this method is a no-op.
     * </p>
     *
     * @param path
     *            the external path (no regular expressions allowed).
     */
    public void remove(String path) {

        if (isReadOnly) {
            throw new UnsupportedOperationException("XML document is read-only");
        }

        /* Expand the path and convert it to its internal format */
        ArrayList<XMLPathSegment> segments = XMLPathSegment.split(path);

        /* Get the node */
        XMLNode targetNode = findNode(segments, true);

        if (targetNode != null) {

            /* To delete the target node we need to remove it from its parent, so lookup the parent */
            XMLElement parent = (XMLElement) targetNode.getParent();

            /* If there is a parent node... */
            if (parent != null) {

                /* Get the list of sub-nodes from the parent */
                ArrayList<XMLNode> subNodes = parent.getSubnodes();

                boolean isDeleted = false;

                for (int s = 0; !isDeleted && s < subNodes.size(); s++) {

                    if (subNodes.get(s) == targetNode) {
                        parent.getSubnodes().remove(s);
                        isDeleted = true;
                    }
                }
            }
        }

        setDirty(true);

    }

    /**
     * As <code>remove(<em>path</em>)</code>, but with support for inserts in the path.
     *
     * @param path
     *            the path
     * @param inserts
     *            the inserts
     */
    public void remove(String path, Object... inserts) {

        remove(format(path, inserts));

    }

    /**
     * Gets the specified XML node.
     *
     * @param path
     *            the path of the node.
     * @return the node.
     */
    public XMLNode getNode(String path) {

        /* Expand the path and convert it to its internal format */
        ArrayList<XMLPathSegment> segments = XMLPathSegment.split(path);

        /* Get the node */
        XMLNode node = findNode(segments, false);

        return node;

    }

    /**
     * Gets an ordered array of the <em>external</em> XML node paths.
     *
     * @return the external element paths.
     */
    public String[] getPaths() {

        /* To hold the result */
        String[] orderedPaths = null;

        /* If there is anything in the XML document... */
        if (documentElement != null) {

            /* To hold the paths */
            ArrayList<StringBuilder> orderedPathsList = new ArrayList<StringBuilder>();

            /* To hold the first path */
            orderedPathsList.add(new StringBuilder());

            /* Build the paths */
            toPaths(documentElement, 0, orderedPathsList);

            /* Convert the paths list to a string array */

            orderedPaths = new String[orderedPathsList.size()];
            Iterator<StringBuilder> i = orderedPathsList.iterator();

            for (int p = 0; i.hasNext(); p++) {
                orderedPaths[p] = i.next().toString();
            }

        } else {

            /* Nothing to return */
            orderedPaths = new String[0];

        }

        return orderedPaths;

    }

    /**
     * Gets an ordered list of the XML node paths matching the specified pattern.
     *
     * @param regexp
     *            the regular expression of a path name matching the required paths; note that this path must be fully
     *            qualified, i.e. <code>/topElement/nextElement</code> shorthand syntax will not work, and the
     *            <code>/topElement[<em>0</em>]/nextElement[<em>n</em>]</code> stype must be used.
     *
     * @return the matching paths.
     */
    public String[] getPaths(String regexp) {

        String[] allElementPaths = getPaths();
        ArrayList<String> matchingPaths = new ArrayList<String>();
        Pattern pattern = Pattern.compile(regexp);

        for (int p = 0; p < allElementPaths.length; p++) {

            Matcher matcher = pattern.matcher(allElementPaths[p]);

            if (matcher.matches()) {
                matchingPaths.add(allElementPaths[p]);
            }
        }

        return matchingPaths.toArray(new String[matchingPaths.size()]);

    }

    /**
     * Gets a <code>java.util.Iterator</code> for this XML file.
     *
     * @param regexp
     *            a regular expression identifying the set of paths to XML nodes to be iterated.
     */
    public Iterator<XMLNode> iterator(String regexp) {

        return new XMLIterator(regexp);

    }

    /**
     * Expands an external path that omits one or more element indices to a fully qualified path.
     * <p>
     * A default index of "<code>[0]</code>" is assumed where necessary.
     * </p>
     *
     * @param path
     * @return the full path
     */
    public static String expandPath(String path) {

        String[] pathParts = null;
        String elementPath = null;
        String attributeName = null;
        StringBuilder fullPath = new StringBuilder();

        /* If this is the path to an attribute... */
        if (path.indexOf('@') >= 0) {

            /* Split out the attribute name */
            pathParts = path.split("@");
            elementPath = pathParts[0];
            attributeName = pathParts[1];

        } else {

            // /* Make sure that the path has the "content" indicator */
            //
            // pathParts = path.split("/\\$");
            //
            // if (pathParts.length == 1) {
            // elementPath = path + "/$";
            // } else {
            // elementPath = path;
            // }

            elementPath = path;
        }

        /* Split the path into its parts */
        pathParts = elementPath.split("/");

        /* For each segment in the path... */
        for (int p = 1; p < pathParts.length; p++) {

            fullPath.append("/" + pathParts[p]);

            /* If the segment is not fully qualified... */
            if (pathParts[p].indexOf('[') < 0) {
                fullPath.append("[0]");
            }
        }

        /* If this is the path to an attribute... */
        if (attributeName != null) {
            fullPath.append("@" + attributeName);
        }

        /* Look up the value */
        return fullPath.toString();

    }

    /**
     * Convenience string formatting utility.
     *
     * @param format
     * @param inserts
     * @return the formatted string.
     */
    public String format(String format, Object... inserts) {

        Formatter f = new Formatter();
        f.format(format, inserts);
        return f.toString();

    }

    /**
     * Cheap, cheerful, and sub-optimal deep copy of an XML instance.
     */
    public XML replicate() {

        XML clone = new XML();

        try {

            clone.parseString(toXML());

        } catch (Exception e) {

            /* If this happens then an internal error has occurred serializing or parsing the XML */
            throw new RuntimeException(e);

        }

        return clone;

    }

    /**
     * Copies the contents of this XML instance into another.
     *
     * @param target
     *            the target XML instance.
     */
    public void copyInto(XML target) {

        try {

            target.parseString(toString());

        } catch (Exception e) {

            /* If this happens then an internal error has occurred serializing or parsing the XML */
            throw new RuntimeException(e);

        }

    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        String toString = null;

        try {
            /* Generate the XML form of the content */
            toString = toXML();
        } catch (Exception e) {
            e.printStackTrace();
            super.toString();
        }

        return toString;

    }

    /**
     * Convert the XML representation to a byte array.
     *
     * @return the byte array
     */
    public byte[] toBytes() {

        /* If we need to encode the current document... */
        if (toBytes == null) {

            /* Convert it to its byte representation */
            toBytes = toString().getBytes();

        }

        return toBytes;

    }

    /*
     * (non-Javadoc)
     * @see fabric.core.xml.IXMLTokenHandler#xmlElementStart(java.lang.String, fabric.core.xml.XMLAttributes)
     */
    @Override
    public void xmlElementStart(String name, XMLAttributes elementAttributes) {

        // System.out.println("Start of element: #" + name + "#");
        // for (int a = 0; a < elementAttributes.size(); a++) {
        // System.out.println("Attribute " + a + ": name = #" +
        // elementAttributes.getName(a) + "#, value = #"
        // + elementAttributes.getValue(a) + "#");
        // }

        handleStartElement(name, elementAttributes);

    }

    /**
     * Handles the start tag of an XML element.
     *
     * @param basicName
     *            the name of the XML element.
     * @param elementAttributes
     *            the element's attributes (if any)
     * @return <code>true</code> if the tag was handled, <code>false</code> otherwise.
     */
    protected boolean handleStartElement(String basicName, XMLAttributes elementAttributes) {

        XMLElement parentElement = null;

        /* If there is a parent element... */
        if (elementStack.size() > 0) {
            /* Get it */
            parentElement = elementStack.peek();
        }

        /* Create the new element */
        XMLElement newElement = new XMLElement(basicName, parentElement);

        /* Capture the element's attributes */
        for (int a = 0; a < elementAttributes.size(); a++) {

            String attributeName = elementAttributes.getName(a);
            String attributeValue = elementAttributes.getValue(a);
            newElement.getAttributes().put(attributeName, attributeValue);

        }

        /* Save the new node */
        if (parentElement != null) {
            parentElement.getSubnodes().add(newElement);
        } else {
            documentElement = newElement;
        }

        /* Make this the current element */
        elementStack.push(newElement);

        /* We're no longer collecting text for a text content node */
        currentText = null;

        return true;
    }

    /*
     * (non-Javadoc)
     * @see fabric.core.xml.IXMLTokenHandler#xmlElementData(char[])
     */
    @Override
    public void xmlElementData(char[] characters) {

        // System.out.println("Trimmed characters: #" + (new
        // String(characters)).trim() + "#");

        /* Get the content string */
        String chars = new String(characters);

        /* Trim whitespace if required */
        if (noMixedContent) {
            chars = chars.trim();
        }

        /* If there is any content... */
        if (!chars.equals("")) {

            if (currentText == null) {

                /* Get the current element */
                XMLElement parentElement = elementStack.peek();

                /* Create the new content node */
                XMLText text = new XMLText(chars, parentElement);

                /* Save the content node */
                parentElement.getSubnodes().add(text);

                currentText = text;

            } else {

                currentText.appendText(chars);

            }
        }
    }

    /*
     * (non-Javadoc)
     * @see fabric.core.xml.IXMLTokenHandler#xmlComment(char[])
     */
    @Override
    public void xmlComment(char[] characters) {

        // System.out.println("Comment: #" + new String(characters) + "#");
    }

    /*
     * (non-Javadoc)
     * @see fabric.core.xml.IXMLTokenHandler#xmlElementEnd(java.lang.String)
     */
    @Override
    public void xmlElementEnd(String name) {

        // System.out.println("End of element: #" + name + "#");

        handleEndElement(name);

    }

    /**
     * Handles the end tag of an XML element.
     *
     * @param basicName
     *            the name of the element.
     * @return <code>true</code> if the tag was handled, <code>false</code> otherwise.
     */
    protected boolean handleEndElement(String basicName) {

        /* This is no longer the current element */
        elementStack.pop();

        /* We're no longer collecting text for a text content node */
        currentText = null;

        return true;
    }

    /*
     * (non-Javadoc)
     * @see fabric.core.xml.IXMLTokenHandler#xmlProcessingInstruction(java.lang.String)
     */
    @Override
    public void xmlProcessingInstruction(String pi) {

        /* Record the processing instruction */
        addPI(pi);

    }

    /**
     * Encode the nodes at the specified path and (recursively) their sub-nodes.
     *
     * @param node
     *            the next node to encode.
     * @param xml
     *            the generated XML.
     * @param indent
     *            the current indent for pretty-printing the generated XML.
     * @return the last node encoded.
     */
    protected XMLNode toXML(XMLNode node, StringBuilder xml, String indent) {

        XMLNode lastNode = null;

        /* If this is an element... */
        if (node instanceof XMLElement) {

            XMLElement element = (XMLElement) node;

            /* Add the element start tag (including its attributes) */
            if (noMixedContent && node != documentElement) {
                xml.append("\n" + indent);
            }
            xml.append("<" + element);

            /* If the element has children... */
            if (element.getSubnodes().size() > 0) {

                /* Close the element start tag */
                xml.append(">");

                Iterator<XMLNode> i = element.getSubnodes().iterator();

                /* While there are more children... */
                while (i.hasNext()) {

                    /* Encode its children */
                    lastNode = toXML(i.next(), xml, indent + XML_INDENT);
                }

                /* Add the element end tag */
                if (noMixedContent && lastNode instanceof XMLElement) {
                    xml.append("\n" + indent);
                }
                xml.append("</" + element.getNodeName() + ">");

            } else {

                /* Close the element tag */
                xml.append("/>");

            }

        } else {

            /* This is a text node */
            String value = (node != null) ? ((XMLText) node).toString() : "";
            xml.append(value);

        }

        return node;
    }

    /**
     * Generate the paths for the node in this document.
     *
     * @param node
     *            the next node to encode.
     * @param count
     *            the number of occurences of this node type at this level.
     * @param paths
     *            the list of paths.
     */
    protected void toPaths(XMLNode node, int count, ArrayList<StringBuilder> paths) {

        /* Get the current path being built */
        StringBuilder currentPath = paths.get(paths.size() - 1);

        /* Add the element */
        currentPath.append("/" + node.getNodeName() + "[" + count + "]");

        /* If this is an element... */
        if (node instanceof XMLElement) {

            XMLElement element = (XMLElement) node;

            /* If the element has children... */
            if (element.getSubnodes().size() > 0) {

                /* To hold the counts of sub-nodes */
                HashMap<String, Integer> nodeCounts = new HashMap<String, Integer>();

                /* For each sub-node... */
                for (int s = 0; s < element.getSubnodes().size(); s++) {

                    /* Start a new path */
                    StringBuilder newPath = new StringBuilder(currentPath.toString());
                    paths.add(newPath);

                    /* Get the sub-node */
                    XMLNode subElement = element.getSubnodes().get(s);

                    /* Increment the count for this node type */
                    Integer subElementCount = nodeCounts.get(subElement.getNodeName());
                    int iSubElementCount = (subElementCount != null) ? subElementCount.intValue() + 1 : 0;
                    nodeCounts.put(subElement.getNodeName(), iSubElementCount);

                    /* Encode its children */
                    toPaths(subElement, iSubElementCount, paths);
                }
            }
        }
    }

    /**
     * Decode the XML into the internal representation used by the class.
     *
     * @throws Exception
     *             thrown if there is a problem parsing the XML.
     */
    protected void parseXML() throws Exception {

        if (isReadOnly) {
            throw new UnsupportedOperationException("XML document is read-only");
        }

        setDefaultPIs();

        Reader xmlSourceReader = null;

        if (xmlString != null) {
            xmlSourceReader = new StringReader(xmlString);
        } else if (xmlFile != null) {
            xmlSourceReader = new FileReader(xmlFile);
        } else if (xmlStream != null) {
            xmlSourceReader = new InputStreamReader(xmlStream);
        } else {
            throw new IllegalStateException("No XML string or file assigned");
        }

        IXMLTokenizer xmlTokenizer = XMLTokenizerFactory.getTokenizer();

        xmlTokenizer.setHandler(this);
        xmlTokenizer.tokenize(xmlSourceReader);

        xmlSourceReader.close();

        setDirty(false);

    }

    /**
     * Set up default processing instructions.
     */
    private void setDefaultPIs() {

        piList.clear();
        addPI("xml version=\"1.0\"");
    }

    /**
     * Replaces XML special characters with their equivalent entity references:
     * <ul>
     * <li>Less-than (&lt;): <em>&amp;lt;</em></li>
     * <li>Greater-than (&gt;): <em>&amp;gt;</em></li>
     * <li>Ampersand (&amp;): <em>&amp;</em></li>
     * <li>Apostrophe (&apos;): <em>&amp;apos;</em></li>
     * <li>Double-quote (&quot;): <em>&amp;quot;</em></li>
     * </ul>
     */
    protected static String encodeEntityRefs(String content) {

        if (content == null) {
            throw new IllegalArgumentException("'content' parameter cannot be null");
        }

        StringBuilder xmlText = new StringBuilder();
        char[] contentChars = content.toCharArray();

        for (int c = 0; c < contentChars.length; c++) {

            switch (contentChars[c]) {

                case '<':

                    xmlText.append(XML_LESS_THAN);
                    break;

                case '>':

                    xmlText.append(XML_GREATER_THAN);
                    break;

                case '&':

                    xmlText.append(XML_AMPERSAND);
                    break;

                case '\'':

                    xmlText.append(XML_APOSTROPHE);
                    break;

                case '"':

                    xmlText.append(XML_QUOTE);
                    break;

                default:

                    xmlText.append(contentChars[c]);
                    break;

            }
        }

        return xmlText.toString();
    }

    /**
     * Replaces XML entity references with their character equivalients:
     * <ul>
     * <li><code>&amp;lt;</code>: <em>less-than (&lt;)</em></li>
     * <li><code>&amp;gt;</code>: <em>greater-than (&gt;)</em></li>
     * <li><code>&amp;</code>: <em>ampersand (&amp;)</em></li>
     * <li><code>&amp;apos;</code>: <em>apostrophe (&apos;)</em></li>
     * <li><code>&amp;quot;</code>: <em>double-quote (&quot;)</em></li>
     * </ul>
     */
    protected static String decodeEntityRefs(String xmlContent) {

        StringBuilder contentBuffer = new StringBuilder();
        StringBuilder entityRefBuffer = null;
        char[] xmlContentChars = xmlContent.toCharArray();

        for (int c = 0; c < xmlContentChars.length; c++) {

            /* If we are not currently decoding an entity reference... */
            if (entityRefBuffer == null) {

                /* If this is the start of a new entity reference... */
                if (xmlContentChars[c] == '&') {
                    entityRefBuffer = new StringBuilder();
                    entityRefBuffer.append(xmlContentChars[c]);
                } else {
                    contentBuffer.append(xmlContentChars[c]);
                }

            } else {

                entityRefBuffer.append(xmlContentChars[c]);

                /* If this is the end of the entity reference... */
                if (xmlContentChars[c] == ';') {

                    /* Try to match it against one of the ones we know about */

                    String entityRef = entityRefBuffer.toString();

                    if (entityRef.equals(XML_LESS_THAN)) {

                        contentBuffer.append('<');
                        entityRefBuffer = null;

                    } else if (entityRef.equals(XML_GREATER_THAN)) {

                        contentBuffer.append('>');
                        entityRefBuffer = null;

                    } else if (entityRef.equals(XML_AMPERSAND)) {

                        contentBuffer.append('&');
                        entityRefBuffer = null;

                    } else if (entityRef.equals(XML_QUOTE)) {

                        contentBuffer.append('"');
                        entityRefBuffer = null;

                    } else if (entityRef.equals(XML_APOSTROPHE)) {

                        contentBuffer.append('\'');
                        entityRefBuffer = null;
                    }
                }
            }
        }

        /* If there is an unresolved entity reference then copy it over */
        if (entityRefBuffer != null && entityRefBuffer.length() > 0) {
            contentBuffer.append(entityRefBuffer);
        }

        return contentBuffer.toString();
    }

    /**
     * Escapes characters in a string that clash with regular expression constructs.
     *
     * @param string
     *            the string.
     * @return the string with regular expression constructs escaped.
     */
    public static String regexpEscape(String string) {

        StringBuilder escapedString = new StringBuilder();
        char[] chars = string.toCharArray();

        for (int c = 0; c < chars.length; c++) {

            switch (chars[c]) {

                case '[':
                case ']':
                case '.':
                case '*':
                case '\\':
                case '$':
                case '^':
                case '?':
                case '+':
                case '{':
                case '}':
                case '|':

                    escapedString.append('\\');

                default:

                    escapedString.append(chars[c]);

            }
        }

        return escapedString.toString();
    }

    /**
     * Locatte a node based upon its path.
     *
     * @param path
     *            the path to the node.
     * @param doCreate
     *            <code>true</code> if missing nodes should be created, <code>false</code> otherwise.
     */
    protected XMLNode findNode(ArrayList<XMLPathSegment> path, boolean doCreate) {

        if (doCreate && documentElement == null) {

            XMLPathSegment segment = path.get(0);
            documentElement = new XMLElement(segment.getNodeName(), null);

        }

        ArrayList<XMLPathSegment> pathCopy = new ArrayList<XMLPathSegment>(path);
        XMLNode resultNode = findNode(documentElement, 0, pathCopy, doCreate);

        return resultNode;
    }

    /**
     * Locates a node based upon its path.
     *
     * @param node
     *            the starting node.
     * @param path
     *            the path to the node.
     * @param doCreate
     *            <code>true</code> if missing nodes should be created, <code>false</code> otherwise.
     */
    protected XMLNode findNode(XMLNode node, int index, ArrayList<XMLPathSegment> path, boolean doCreate) {

        /* Flag indicating that the required node has been located */
        boolean nodeFound = false;

        /* To hold the result */
        XMLNode resultNode = null;

        /* Get the current segment */
        XMLPathSegment currentSegment = path.get(0);

        /* To hold the current element */
        XMLElement element = null;

        /* If this is the node we are looking for... */
        if (node.getNodeName().equals(currentSegment.getNodeName()) && currentSegment.getIndex() == index) {

            if (path.size() == 1) {

                /* We've finished */
                resultNode = node;

            } else {

                /* Look through the nodes children to match the next segment */

                element = (XMLElement) node;
                currentSegment = path.get(1);

                /* If this node does not have any children, and we are looking for a text node... */
                if (element.getSubnodes().size() == 0 && currentSegment.getNodeName().equals("$")
                        && currentSegment.getIndex() == 0) {

                    /* The correct response is a text node with an empty string */
                    resultNode = new XMLText("", element);

                    /* If we've been asked to create this node permanently... */
                    if (doCreate) {

                        /* Save the content node */
                        element.getSubnodes().add(resultNode);

                    }

                } else {

                    /* Search through the children for a match */

                    /* To track the index of potential matches (to find the right occurrence of the matching node) */
                    int nodeIndex = 0;

                    /* For each sub-node or until a match is found... */
                    for (int s = 0; !nodeFound && s < element.getSubnodes().size(); s++) {

                        /* Get the next sub-node */
                        XMLNode subnode = element.getSubnodes().get(s);

                        /* If this is the type of node we are looking for... */
                        if (subnode.getNodeName().equals(currentSegment.getNodeName())) {

                            /* If this is the actual node we are looking for... */
                            if (nodeIndex == currentSegment.getIndex()) {

                                nodeFound = true;
                                path.remove(0);

                                /* If there are any path elements left... */
                                if (path.size() > 0) {
                                    resultNode = findNode(subnode, nodeIndex, path, doCreate);
                                }

                            } else {

                                nodeIndex++;

                            }
                        }
                    }
                }
            }

        } else {

            /* If the caller has used the wrong top level element in the path... */
            if (currentSegment.getIndex() == 0) {
                throw new IllegalStateException("Attempt to use inconsistent top level XML element: "
                        + currentSegment.getNodeName() + ". Correct top level element is: " + node.getNodeName());
            }

        }

        /* If the node was not found... */
        if (doCreate && resultNode == null) {

            /* Create a new node */

            /* If it is a text node... */
            if (path.get(1).getNodeName().equals("$")) {

                resultNode = new XMLText("", element);
                element.getSubnodes().add(resultNode);

            } else {

                XMLElement newElement = new XMLElement(currentSegment.getNodeName(), element);
                element.getSubnodes().add(newElement);
                resultNode = findNode(node, index, path, doCreate);

            }
        }

        return resultNode;
    }
}