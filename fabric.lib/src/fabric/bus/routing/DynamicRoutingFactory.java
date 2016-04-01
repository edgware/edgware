/*
 * (C) Copyright IBM Corp. 2012
 *
 * LICENSE: Eclipse Public License v1.0
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fabric.bus.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import fabric.registry.FabricRegistry;
import fabric.registry.Node;
import fabric.registry.NodeNeighbour;
import fabric.registry.QueryScope;
import fabric.registry.exception.RegistryQueryException;

public class DynamicRoutingFactory implements IRoutingFactory {

    /** Copyright notice. */
    public static final String copyrightNotice = "(C) Copyright IBM Corp. 2012";

    @Override
    public String[] getRouteNodes(String startNode, String endNode) {
        /*
         * This is a simple implementation of Dijkstra's Algorithm to determine the shortest available path between the
         * specified nodes. It current assumes a constant fixed weight for each edge in the network. It could be adapted
         * to take into account attributes of the network, such as bandwidth/reliability. All routes get the same
         * Ordinal value (1).
         */

        String[] result = new String[] {};

        // A set of all nodes that tracks which ones have not yet been examined
        HashSet<String> unvisited = new HashSet<String>();

        try {
            Node[] nodes = FabricRegistry.getNodeFactory().getNodes("AVAILABILITY='AVAILABLE'");
            for (int i = 0; i < nodes.length; i++) {
                unvisited.add(nodes[i].getId());
            }
        } catch (RegistryQueryException rqe) {
            // Never actually thrown
        }

        // Get the complete set of node neighbours
        NodeNeighbour[] nn = FabricRegistry.getNodeNeighbourFactory(QueryScope.DISTRIBUTED).getAllNeighbours();

        // An edge map of the network; maps from node to a list of its neighbours
        HashMap<String, ArrayList<NodeNeighbour>> graph = new HashMap<String, ArrayList<NodeNeighbour>>();

        for (int i = 0; i < nn.length; i++) {
            if (unvisited.contains(nn[i].getNodeId()) && unvisited.contains(nn[i].getNeighbourId())) {
                ArrayList<NodeNeighbour> edgeList = graph.get(nn[i].getNodeId());
                if (edgeList == null) {
                    edgeList = new ArrayList<NodeNeighbour>();
                }
                edgeList.add(nn[i]);
                graph.put(nn[i].getNodeId(), edgeList);
            }
        }

        // Assume a fixed weight for all edges. In the future, this could take into account
        // additional information about the edge such as bandwidth/latency.
        //
        int fixedWeight = 1;

        // A map of node name to current shortest distance from the source node
        HashMap<String, Integer> dist = new HashMap<String, Integer>();
        // A map of node name to a list of nodes that could immediately precede it in the
        // shortest path. A list is used as there could be more than one path with the
        // shortest distance.
        HashMap<String, ArrayList<String>> previous = new HashMap<String, ArrayList<String>>();

        String source = startNode;
        String target = endNode;

        dist.put(source, 0);
        unvisited.remove(source);

        String current = source;

        // While there are still nodes to consider
        while (unvisited.size() > 0) {
            // Get the list of neighbours of the current node
            ArrayList<NodeNeighbour> nodeEdges = graph.get(current);
            if (nodeEdges != null) {
                // For each neighbour, if it has not yet been processed, check
                // if we have a new shortest path to it and update the structures
                // accordingly.
                for (int i = 0; i < nodeEdges.size(); i++) {
                    String neighbour = nodeEdges.get(i).getNeighbourId();
                    if (unvisited.contains(neighbour)) {
                        int newDistance = dist.get(current) + fixedWeight;
                        if (!dist.containsKey(neighbour) || newDistance < dist.get(neighbour)) {
                            dist.put(neighbour, newDistance);
                            ArrayList<String> list = new ArrayList<String>();
                            list.add(current);
                            previous.put(neighbour, list);
                        } else if (newDistance == dist.get(neighbour)) {
                            previous.get(neighbour).add(current);
                        }
                    }
                }
            }
            // Remove the current node as it has now been processed
            unvisited.remove(current);

            if (current.equals(target)) {
                // Reached the target node so nothing more to do
                break;
            }

            // Find the next unvisited node with the shortest path
            // to the source calculated so far and use it as the
            // next node for processing.
            Iterator<String> it = unvisited.iterator();
            int minD = Integer.MAX_VALUE;
            while (it.hasNext()) {
                String n = it.next();
                if (dist.containsKey(n)) {
                    int d = dist.get(n);
                    if (d < minD) {
                        minD = d;
                        current = n;
                    }
                }
            }

            if (minD == Integer.MAX_VALUE) {
                // If none of the unvisited nodes has been reached,
                // they are not connected to the source node.
                break;
            }
        }

        if (current.equals(target)) {
            // If we have reached the target node, need to back-track through the 'previous'
            // map to find the routes.
            // As there could be more than one route with the same distance, we need to
            // build each path separately.

            // A set of complete paths from source to target
            Set<ArrayList<Object>> completePaths = new HashSet<ArrayList<Object>>();

            // A set of paths that end at target, but have not yet been fully back-tracked
            // to the source
            Set<ArrayList<Object>> incompletePaths = new HashSet<ArrayList<Object>>();

            // Start with a single path containing the target node
            ArrayList<Object> l = new ArrayList<Object>();
            l.add(target);
            incompletePaths.add(l);

            // While there are still paths to complete back-tracking to source
            while (!incompletePaths.isEmpty()) {
                ArrayList<Object> path = incompletePaths.iterator().next();
                if (path.get(0) instanceof String) {
                    // If the first element is a String then it is a single node.
                    String n = (String) path.get(0);
                    if (previous.containsKey(n)) {
                        // If there is a previous entry to this one, prepend it
                        // to the path
                        path.add(0, previous.get(n));
                    } else {
                        // No previous entry means we have reached the source.
                        // Move this incompletePath to the completePaths set
                        incompletePaths.remove(path);
                        completePaths.add(path);
                    }
                } else if (path.get(0) instanceof ArrayList) {
                    // If the first element is a List, then it is a list of nodes
                    // that could precede this one.

                    // Get the list of previous Nodes, removing it from the path
                    ArrayList<String> previousNodes = (ArrayList<String>) path.remove(0);
                    // Remove this incompletePath as we're about to fork a number
                    // of new incompletePaths
                    incompletePaths.remove(path);
                    Iterator<String> it = previousNodes.iterator();
                    while (it.hasNext()) {
                        // For each previous node, create a copy of the current incomplete
                        // path and prepend the previous node
                        String n = it.next();
                        ArrayList<Object> newPath = (ArrayList<Object>) path.clone();
                        newPath.add(0, n);
                        incompletePaths.add(newPath);
                    }
                }
            }

            if (completePaths.size() > 0) {
                ArrayList<Object> path = completePaths.iterator().next();
                result = path.toArray(result);
            }
        }

        return result;
    }

}
