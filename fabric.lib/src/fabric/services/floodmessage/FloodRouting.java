/*
 * (C) Copyright IBM Corp. 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.services.floodmessage;

import java.util.ArrayList;

import fabric.bus.messages.IFabricMessage;
import fabric.bus.messages.IReplicate;
import fabric.bus.routing.IRouting;
import fabric.bus.routing.impl.Routing;
import fabric.bus.routing.impl.StaticRouting;
import fabric.core.xml.XML;
import fabric.registry.FabricRegistry;
import fabric.registry.NodeNeighbour;
import fabric.registry.Route;
import fabric.registry.RouteFactory;
import fabric.registry.QueryScope;

/**
 * Implementation of routing that enables messages to be sent to all nodes in the fabric as efficiently as possible.
 *
 */
public class FloodRouting extends Routing {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

    /** The node that originated the flood message */
    private String startNode;

    /** The node that forwarded the message to the current node */
    private String previousNode;

    /** Whether the message should be retained for future nodes to receive. */
    private boolean retained;

    /** The time-to-live, in milliseconds, before the message can be expired. */
    private long ttl;

    /** The list of nodes to send the message on to next. */
    private String[] nextNodes = null;

    public FloodRouting() {

        super();
        this.previousNode = null;
        this.startNode = homeNode();
        this.retained = false;
        this.ttl = 0;
    }

    /**
     * Constructs a new instance
     */
    public FloodRouting(String thisNode) {

        super();
        this.previousNode = null;
        this.startNode = thisNode;
        this.retained = false;
        this.ttl = 0;
    }

    /**
     * Constructs a new instance based upon an existing instance.
     * 
     * @param source
     *            the routing instance of replicate
     */
    private FloodRouting(FloodRouting source) {

        super();
        this.startNode = source.startNode();
        this.previousNode = source.previousNode();
        this.ttl = source.getTTL();
        this.retained = source.isRetained();
        this.nextNodes = source.nextNodes();
    }

    /**
     * @see fabric.bus.routing.impl.Routing#init(java.lang.String, fabric.core.xml.XML)
     */
    @Override
    public void init(String element, XML messageXML) throws Exception {

        super.init(element, messageXML);

        /* Extract the routing specific properties from the message */
        this.startNode = messageXML.get(element + "/f:routing/f:start");
        this.previousNode = messageXML.get(element + "/f:routing/f:previous");
        this.retained = messageXML.getBoolean(element + "/f:routing/f:retain");

        String ttlString = messageXML.get(element + "/f:routing/f:ttl");
        if (ttlString != null) {
            this.ttl = Long.parseLong(ttlString);
        } else {
            this.ttl = Long.parseLong(config("routing.flood.ttl", "600000"));
        }

        /* At this point, assume this message won't be forwarded on */
        this.nextNodes = null;
    }

    /**
     * Sets the time-to-live value.
     * 
     * @param ttl
     *            time-to-live, in milliseconds
     */
    public void setTTL(long ttl) {

        this.ttl = ttl;
    }

    /**
     * Sets whether the message should be retained for future nodes. Not currently used.
     * 
     * @param retained
     */
    public void setRetained(boolean retained) {

        this.retained = retained;
    }

    /**
     * Gets the time-to-live value.
     * 
     * @return time-to-live, in milliseconds
     */
    public long getTTL() {

        return this.ttl;
    }

    /**
     * Whether this message should be retained for future nodes. Not current used.
     * 
     * @return retained
     */
    public boolean isRetained() {

        return this.retained;
    }

    /**
     * @see fabric.bus.routing.impl.Routing#embed(java.lang.String, fabric.core.xml.XML)
     */
    @Override
    public void embed(String element, XML messageXML) throws Exception {

        super.embed(element, messageXML);

        messageXML.set(element + "/f:routing/f:start", this.startNode);
        messageXML.set(element + "/f:routing/f:previous", homeNode());
        if (retained) {
            /* Only add this property if it is TRUE; XML.getBoolean defaults to FALSE if the property is not present */
            messageXML.setBoolean(element + "/f:routing/f:retain", retained);
        }
        messageXML.set(element + "/f:routing/f:ttl", Long.toString(ttl));
    }

    /**
     * @see fabric.bus.routing.IRouting#currentNode()
     */
    @Override
    public String currentNode() throws UnsupportedOperationException {

        return homeNode();
    }

    /**
     * @see fabric.bus.routing.IRouting#startNode()
     */
    @Override
    public String startNode() {

        return this.startNode;
    }

    /**
     * @see fabric.bus.routing.IRouting#previousNode()
     */
    @Override
    public String previousNode() throws UnsupportedOperationException {

        return this.previousNode;
    }

    @Override
    public String[] nextNodes() {

        /* If we haven't fetched the list of nodes yet... */
        if (nextNodes == null) {

            /* Get the complete set of unique node neighbours by 'best' interface */
            NodeNeighbour[] nn = FabricRegistry.getNodeNeighbourFactory(QueryScope.LOCAL).getUniqueNeighboursByNeighbourId(
                    homeNode());
            ArrayList<String> neighbours = new ArrayList<String>();

            /* For each neighbour... */
            for (int i = 0; i < nn.length; i++) {

                /* If the neighbour is not the previous node... */
                if (this.previousNode == null || !nn[i].getNeighbourId().equals(this.previousNode)) {

                    /* If the neighbour is not this node... */
                    if (!(nn[i].getNeighbourId().equals(homeNode()))) {

                        /* If the neighbour has not been added already... */
                        if (!neighbours.contains(nn[i].getNeighbourId())) {

                            neighbours.add(nn[i].getNeighbourId());
                        }
                    }
                }
            }

            nextNodes = neighbours.toArray(new String[neighbours.size()]);
        }

        return nextNodes;
    }

    @Override
    public String endNode() throws UnsupportedOperationException {

        /*
         * If nextNodes is empty then this is the endNode, otherwise use nextNodes[0] as an (arbitrary) endNode that is
         * not this one
         */
        nextNodes();
        return (nextNodes == null || nextNodes.length == 0) ? homeNode() : nextNodes[0];
    }

    @Override
    public IRouting returnRoute() {

        StaticRouting result = null;

        /* Get the static routes that takes us straight back to the source node */
        RouteFactory routeFactory = FabricRegistry.getRouteFactory();
        Route[] routes = routeFactory.getRoutes(homeNode(), this.startNode);

        /* If there are any available... */
        if (routes.length > 0) {
            try {
                String[] nodes = routeFactory.getRouteNodes(homeNode(), this.startNode, routes[0].getRoute());
                result = new StaticRouting(nodes);
            } catch (Exception e) {
                result = null;
            }
        }
        return result;
    }

    @Override
    public IReplicate replicate() {

        return new FloodRouting(this);
    }

    /**
     * @see IRouting#isDuplicate(IFabricMessage)
     */
    @Override
    public boolean isDuplicate(IFabricMessage message) {

        /* Check the FloodMessageService cache if this message has been seen before */
        String uid = message.getUID();
        boolean duplicate = FloodMessageService.getInstance().isDuplicate(uid);

        if (!duplicate) {
            /* Add the message to the cache */
            FloodMessageService.getInstance().addMessage(message, this.ttl, this.retained);
        }

        return duplicate;
    }
}
