/*
 * (C) Copyright IBM Corp. 2009
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.registry.impl;

import fabric.registry.Route;
import fabric.registry.exception.IncompleteObjectException;

/**
 * Implementation class for a <code>Route</code>.
 */
public class RouteImpl extends AbstractRegistryObject implements Route {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2009";

    private String startNode = null;
    private String endNode = null;
    private int ordinal = 0;
    private String route = null;

    protected RouteImpl() {

    }

    // protected RouteImpl(String startNode, String endNode, int ordinal, String route) {
    // this.startNode = startNode;
    // this.endNode = endNode;
    // this.ordinal = ordinal;
    // this.route = route;
    // }

    @Override
    public String getEndNode() {
        return endNode;
    }

    @Override
    public void setEndNode(String endNode) {
        this.endNode = endNode;
    }

    @Override
    public int getOrdinal() {
        return ordinal;
    }

    @Override
    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    @Override
    public String getRoute() {
        return route;
    }

    @Override
    public void setRoute(String route) {
        this.route = route;
    }

    @Override
    public String getStartNode() {
        return startNode;
    }

    @Override
    public void setStartNode(String startNode) {
        this.startNode = startNode;
    }

    @Override
    public void validate() throws IncompleteObjectException {
        if (startNode == null || startNode.length() == 0 || endNode == null || endNode.length() == 0) {
            throw new IncompleteObjectException("Missing or invalid start and/or end node ids.");
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object != null && object instanceof Route) {
            Route route = (Route) object;
            if (route.getStartNode().equals(startNode) && route.getEndNode().equals(endNode)
                    && route.getOrdinal() == ordinal && route.getRoute() == null ? route == null : route.getRoute()
                    .equals(route)) {

                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder buffy = new StringBuilder("Route::");
        buffy.append(" Start Node ID: ").append(startNode);
        buffy.append(", End Node ID: ").append(endNode);
        buffy.append(", Ordinal: ").append(ordinal);
        buffy.append(", Route: ").append(route);
        return buffy.toString();
    }

    @Override
    public String key() {

        return new StringBuilder(this.getStartNode()).append('/').append(this.getEndNode()).append('/').append(
                this.getOrdinal()).toString();
    }
}
