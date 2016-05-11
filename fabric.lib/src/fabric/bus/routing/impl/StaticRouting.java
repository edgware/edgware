/*
 * (C) Copyright IBM Corp. 2007, 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.routing.impl;

import java.util.Arrays;

import fabric.bus.messages.IReplicate;
import fabric.bus.routing.IRouting;
import fabric.core.xml.XML;

/**
 * Data structure representing a static route embedded in a Fabric message.
 */
public class StaticRouting extends Routing {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2007, 2012";

    /*
     * Class fields
     */

    /**
     * The current position in the route (i.e. the index of the current node in the route)
     */
    private Integer currentNodeIndex = null;

    /** The ordered list of the individual IDs of each node in the route */
    private String[] nodeIDs = new String[0];

    /*
     * Class methods
     */

    /**
     * Constructs a new instance.
     */
    public StaticRouting() {

        super();

    }

    /**
     * Constructs a new instance based upon an existing instance.
     *
     * @param source
     *            the routing instance to replicate.
     */
    public StaticRouting(StaticRouting source) {

        this(source.nodeIDs);

    }

    /**
     * Constructs a new instance.
     *
     * @param routeNodeIDs
     *            the ordered list of the individual IDs of each node in the route.
     */
    public StaticRouting(String[] routeNodeIDs) {

        super();
        setRouteNodes(routeNodeIDs);

    }

    /**
     * @see fabric.bus.routing.impl.Routing#init(java.lang.String, fabric.core.xml.XML)
     */
    @Override
    public void init(String element, XML messageXML) throws Exception {

        super.init(element, messageXML);

        /* Get the paths of each hop in the route */
        getRouteNodesFromMessage(element, messageXML);

    }

    /**
     * @see fabric.bus.routing.impl.Routing#embed(java.lang.String, fabric.core.xml.XML)
     */
    @Override
    public void embed(String element, XML messageXML) throws Exception {

        super.embed(element, messageXML);

        /* Set the paths of each hop in the route */
        setRouteNodesInMessage(element, messageXML);

    }

    /**
     * Gets the list of nodes in the route.
     *
     * @param element
     *            the element containing the XML.
     *
     * @param messageXML
     *            the Fabric message.
     */
    private void getRouteNodesFromMessage(String element, XML messageXML) {

        /* Get the XML paths for the route nodes */
        String elementPath = XML.expandPath(element);
        elementPath = XML.regexpEscape(elementPath);
        String[] routeNodeXMLPaths = messageXML.getPaths(elementPath
                + "/rt\\[.*\\]/nodes\\[.*\\]/n\\[.*\\]/\\$\\[.*\\]");
        String[] nodeIDs = new String[routeNodeXMLPaths.length];

        /* For each node in the route... */
        for (int n = 0; n < routeNodeXMLPaths.length; n++) {

            /* Get the hop name (with a path of the form "/fab/rt/nodes/n[n]/$[n]") */
            nodeIDs[n] = messageXML.get(routeNodeXMLPaths[n]);

        }

        setRouteNodes(nodeIDs);

    }

    /**
     * Sets the list of nodes in the route.
     *
     * @param element
     *            the element that will contain the XML.
     *
     * @param messageXML
     *            the Fabric message.
     */
    private void setRouteNodesInMessage(String element, XML messageXML) {

        /* For each node in the route... */
        for (int n = 0; nodeIDs != null && n < nodeIDs.length; n++) {

            /* Set the hop name (with a path of the form "/fab/rt/nodes[n]/n") */
            messageXML.set(element + "/rt/nodes/n[%d]", nodeIDs[n], n);

        }
    }

    /**
     * @see fabric.bus.routing.IRouting#currentNode()
     */
    @Override
    public String currentNode() throws UnsupportedOperationException {

        return homeNode();

    }

    /**
     * Answers the current position in the route (i.e. the current node number).
     *
     * @return the current position in the route, or <code>-1</code> if the end of the route has been reached.
     */
    public int currentNodeIndex() {

        return currentNodeIndex;

    }

    /**
     * Answers the ID of the node at the specified index in the route.
     *
     * @param index
     *            the index of the required ID (counting from zero).
     *
     * @return the ID of the node, or <code>null</code> if the index is out of range.
     */
    public String node(int index) {

        String nodeID = null;

        if (index >= 0 && index < nodeIDs.length) {
            nodeID = nodeIDs[index];
        }

        return nodeID;

    }

    /**
     * Answers the start node in the route.
     *
     * @return the node ID, or <code>null</code> if the route is empty.
     *
     * @throws UnsupportedOperationException
     *             thrown if this functionality is not implemented.
     *
     * @see fabric.bus.routing.IRouting#startNode()
     */
    @Override
    public String startNode() throws UnsupportedOperationException {

        return node(0);

    }

    /**
     * Answers the ID of the previous node in the route, i.e. the last node through which this message passed.
     *
     * @return the ID of the node, or <code>null</code> if the end of the route has been reached.
     *
     * @throws UnsupportedOperationException
     *             thrown is this operation is not supported.
     *
     * @see fabric.bus.routing.IRouting#previousNode()
     */
    @Override
    public String previousNode() throws UnsupportedOperationException {

        return node(currentNodeIndex - 1);

    }

    /**
     * Answers the ID of the next node in the route.
     *
     * @return the ID of the node, or <code>null</code> if the end of the route has been reached.
     */
    private String nextNode() {

        return node(currentNodeIndex + 1);

    }

    /**
     * @see fabric.bus.routing.IRouting#nextNodes()
     */
    @Override
    public String[] nextNodes() {

        String nextNode = nextNode();
        String[] nextNodes = null;

        if (nextNode != null) {
            nextNodes = new String[] {nextNode};
        } else {
            nextNodes = new String[0];
        }

        return nextNodes;

    }

    /**
     * Answers the end node in the route.
     *
     * @return the node ID, or <code>null</code> if the route is empty.
     *
     * @throws UnsupportedOperationException
     *             thrown if this functionality is not implemented.
     *
     * @see fabric.bus.routing.IRouting#endNode()
     */
    @Override
    public String endNode() {

        return node(nodeIDs.length - 1);

    }

    /**
     * Answers the ordered list of route node IDs.
     *
     * @return the IDs of each node in the route.
     */
    public String[] getRouteNodes() {

        return nodeIDs;

    }

    /**
     * Sets the ordered list of route node IDs.
     * <p>
     * Note: the current node index with be reset to <code>-1</code>.
     * </p>
     *
     * @param nodeIDs
     *            the IDs of each node in the route.
     */
    public void setRouteNodes(String[] nodeIDs) {

        if (nodeIDs != null) {
            this.nodeIDs = nodeIDs;
        } else {
            this.nodeIDs = new String[0];
        }

        currentNodeIndex = Arrays.asList(nodeIDs).indexOf(homeNode());

    }

    /**
     * @see fabric.bus.routing.IRouting#returnRoute()
     */
    @Override
    public IRouting returnRoute() {

        /* Replicate this instance */
        StaticRouting returnRoute = (StaticRouting) replicate();

        /* Reverse the node list */

        String[] returnRouteNodeIDs = new String[nodeIDs.length];

        int target = 0;
        int source = nodeIDs.length - 1;

        while (target < nodeIDs.length) {

            returnRouteNodeIDs[target++] = nodeIDs[source--];

        }

        returnRoute.setRouteNodes(returnRouteNodeIDs);

        return returnRoute;

    }

    /**
     * @see fabric.bus.messages.IReplicate#replicate()
     */
    @Override
    public IReplicate replicate() {

        StaticRouting replica = new StaticRouting(this);
        return replica;

    }
}
