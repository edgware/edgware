/*
 * (C) Copyright IBM Corp. 2006
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.core.xml;

import java.util.ArrayList;

/**
 * Base class for data structures used to record segments in the path of an XML node.
 */
public class XMLPathSegment {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2006";

    /*
     * Class constants
     */

    /* Constants used when parsing the path of an XML node */

    private static final int STATE_NODE_NAME = 1;

    private static final int STATE_INDEX = 2;

    private static final int STATE_ATTRIBUTE_NAME = 3;

    /*
     * Class fields
     */

    /** The name of the node */
    private String nodeName = "";

    /**
     * The index of the node; the index is the order number of the node relative to its peers in the XML
     */
    private int index = 0;

    /**
     * The optional name of the XML attribute associated with this node (if it is an element node).
     */
    private String attributeName = "";

    /*
     * Class methods
     */

    /**
     * Constructs a new instance.
     */
    public XMLPathSegment() {

    }

    /**
     * Constructs a new instance.
     *
     * @param nodeName
     * @param index
     * @param attributeName
     */
    public XMLPathSegment(String nodeName, int index, String attributeName) {

        this.nodeName = (nodeName != null) ? nodeName : "";
        this.index = index;
        this.attributeName = (attributeName != null) ? attributeName : "";
    }

    public String getNodeName() {

        return nodeName;
    }

    public void setNodeName(String nodeName) {

        this.nodeName = nodeName;
    }

    public int getIndex() {

        return index;
    }

    public void setIndex(int index) {

        this.index = index;
    }

    public String getAttributeName() {

        return attributeName;
    }

    public void setAttributeName(String attributeName) {

        this.attributeName = attributeName;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        String nonNullAttribute = (attributeName != null) ? attributeName : "";

        return nodeName + "[" + index + "]" + (nonNullAttribute.equals("") ? "" : "@" + nonNullAttribute);
    }

    /**
     * Break a path it into its individual segments.
     * <p>
     * The following wild-card characters are recognized:
     *
     * @param path
     *            the path.
     */
    public static ArrayList<XMLPathSegment> split(String path) {

        /* The list of path segments */
        ArrayList<XMLPathSegment> segments = new ArrayList<XMLPathSegment>();

        /* To hold the individual tokens of the path as they are extracted */
        StringBuilder segmentChars = new StringBuilder();

        /* The current path segment (name, index, attribute name) */
        XMLPathSegment currentSegment = null;

        /* The current parsing state */
        int state = STATE_NODE_NAME;

        /* Terminate the path with the separator character to simplify parsing */
        if (!path.endsWith("/")) {
            path += '/';
        }

        /* Get the characters of the path */
        char[] pathChars = path.toCharArray();

        /* For each character in the path... */
        for (int p = 0; p < pathChars.length; p++) {

            switch (pathChars[p]) {

                case '/':

                    if (currentSegment != null) {

                        /* Complete the current segment */

                        switch (state) {

                            case STATE_NODE_NAME:

                                currentSegment.nodeName = segmentChars.toString();
                                segmentChars.setLength(0);
                                break;

                            case STATE_ATTRIBUTE_NAME:

                                currentSegment.attributeName = segmentChars.toString();
                                segmentChars.setLength(0);
                                break;

                            default:

                                throw new IllegalStateException("Syntax error in path at character " + p + ": '"
                                        + pathChars[p] + "'");

                        }

                        /* Save the current segment */
                        segments.add(currentSegment);

                    }

                    /* Create a new segment */
                    currentSegment = new XMLPathSegment();

                    /* Don't know what's coming next */
                    state = STATE_NODE_NAME;

                    break;

                case '[':

                    if (state != STATE_NODE_NAME) {
                        throw new IllegalStateException("Syntax error in path at character " + p + ": '" + pathChars[p]
                                + "'");
                    }

                    /* The node name is complete */
                    currentSegment.nodeName = segmentChars.toString();
                    segmentChars.setLength(0);

                    /* Start to capture the index */
                    state = STATE_INDEX;

                    break;

                case ']':

                    if (state != STATE_INDEX) {
                        throw new IllegalStateException("Syntax error in path at character " + p + ": '" + pathChars[p]
                                + "'");
                    }

                    /* The index is complete */
                    currentSegment.index = Integer.parseInt(segmentChars.toString());
                    segmentChars.setLength(0);

                    /* Can only be the optional attribute name next */
                    state = STATE_ATTRIBUTE_NAME;

                    break;

                case '@':

                    switch (state) {

                        case STATE_NODE_NAME:

                            /* The node name is complete */
                            currentSegment.nodeName = segmentChars.toString();
                            segmentChars.setLength(0);
                            break;

                        case STATE_ATTRIBUTE_NAME:

                            /* Nothing to do */
                            break;

                        default:

                            throw new IllegalStateException("Syntax error in path at character " + p + ": '"
                                    + pathChars[p] + "'");
                    }

                    /* Start to capture the attribute name */
                    state = STATE_ATTRIBUTE_NAME;

                    break;

                default:

                    /* Capture the next character */
                    segmentChars.append(pathChars[p]);
            }
        }

        return segments;
    }

    /**
     * Generates the string representation of a portion of a path.
     *
     * @param segments
     *            the segments of the path.
     * @param numSegments
     *            the number of segments to include in the path.
     * @return the string representation of the path.
     */
    public static String toPath(XMLPathSegment[] segments, int numSegments) {

        StringBuilder path = new StringBuilder();

        /* For each element of the path... */
        for (int s = 0; s < numSegments && s < segments.length; s++) {

            path.append('/');
            path.append(segments[s].nodeName);
            path.append('[');
            path.append(segments[s].index);
            path.append(']');
            path.append(segments[s].attributeName);
        }

        return path.toString();
    }

    /**
     * Generates the string representation of a path.
     *
     * @param segments
     *            the segments of the path.
     * @return the string representation of the path.
     */
    public static String toPath(XMLPathSegment[] segments) {

        return toPath(segments, segments.length);
    }

    /**
     * Generates the string representation of a path.
     *
     * @param segments
     *            the segments of the path.
     * @return the string representation of the path.
     */
    public static String toPath(ArrayList<XMLPathSegment> segments) {

        XMLPathSegment[] segmentsArray = segments.toArray(new XMLPathSegment[segments.size()]);
        return toPath(segmentsArray, segmentsArray.length);
    }
}
