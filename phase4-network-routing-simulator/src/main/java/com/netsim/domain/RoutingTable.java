package com.netsim.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Maps destination router id -> next-hop router id for a given source
 * router, as produced by a {@code RoutingAlgorithm}. Also retains full
 * predecessor chains so a complete path can be resolved for simulation.
 */
public class RoutingTable {

    private final String ownerRouterId;
    private final Map<String, String> nextHop;      // destinationId -> nextHopId
    private final Map<String, String> predecessors;  // destinationId -> predecessor on shortest path
    private final Map<String, Double> distances;     // destinationId -> total path cost

    public RoutingTable(String ownerRouterId, Map<String, String> nextHop,
                         Map<String, String> predecessors, Map<String, Double> distances) {
        this.ownerRouterId = ownerRouterId;
        this.nextHop = nextHop;
        this.predecessors = predecessors;
        this.distances = distances;
    }

    public static RoutingTable empty(String ownerRouterId) {
        return new RoutingTable(ownerRouterId, Map.of(), Map.of(), Map.of());
    }

    public String nextHopFor(String destinationId) {
        return nextHop.get(destinationId);
    }

    public double distanceTo(String destinationId) {
        return distances.getOrDefault(destinationId, Double.POSITIVE_INFINITY);
    }

    /** Resolves the full router-id path from this table's owner to {@code destinationId}. */
    public List<String> resolvePath(String destinationId) {
        if (!predecessors.containsKey(destinationId) && !destinationId.equals(ownerRouterId)) {
            return List.of();
        }
        List<String> path = new ArrayList<>();
        String current = destinationId;
        path.add(current);
        while (!current.equals(ownerRouterId)) {
            String prev = predecessors.get(current);
            if (prev == null) {
                return List.of(); // unreachable
            }
            path.add(prev);
            current = prev;
        }
        java.util.Collections.reverse(path);
        return path;
    }

    public String getOwnerRouterId() {
        return ownerRouterId;
    }

    public Map<String, String> getNextHop() {
        return nextHop;
    }

    public Map<String, String> getPredecessors() {
        return predecessors;
    }

    public Map<String, Double> getDistances() {
        return distances;
    }
}
