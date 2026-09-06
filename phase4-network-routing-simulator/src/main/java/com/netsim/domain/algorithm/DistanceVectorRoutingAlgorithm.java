package com.netsim.domain.algorithm;

import com.netsim.domain.Link;
import com.netsim.domain.NetworkTopology;
import com.netsim.domain.Router;
import com.netsim.domain.RoutingTable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Simulates classic distributed distance-vector convergence
 * (Bellman-Ford-Chandy style, RIP-like): each router iteratively
 * exchanges {destination -> cost} vectors with its neighbors and adopts
 * any strictly better route it hears about, until a full pass produces
 * no further improvement (i.e. the network has converged).
 *
 * Functionally similar in outcome to {@link BellmanFordRoutingAlgorithm}
 * but modeled as neighbor-to-neighbor vector exchanges rather than a
 * global edge relaxation, which is closer to how link-state/distance-
 * vector protocols actually reconverge after a topology change
 * (design doc section 10.3).
 */
@Component("distanceVector")
public class DistanceVectorRoutingAlgorithm implements RoutingAlgorithm {

    private static final int MAX_ROUNDS = 50;

    @Override
    public RoutingTable computeRoutes(NetworkTopology topology, Router source) {
        // vector.get(routerId) = {destinationId -> best known cost from routerId}
        Map<String, Map<String, Double>> vectors = new HashMap<>();
        Map<String, String> predecessor = new HashMap<>();

        for (Router r : topology.getRouters()) {
            Map<String, Double> initial = new HashMap<>();
            initial.put(r.getId(), 0.0);
            vectors.put(r.getId(), initial);
        }

        boolean changed = true;
        int round = 0;
        while (changed && round < MAX_ROUNDS) {
            changed = false;
            round++;
            for (Router router : topology.getRouters()) {
                Map<String, Double> ownVector = vectors.get(router.getId());
                for (Link link : topology.activeLinksFor(router.getId())) {
                    String neighborId = link.otherEnd(router.getId());
                    Map<String, Double> neighborVector = vectors.get(neighborId);
                    if (neighborVector == null) continue;

                    for (Map.Entry<String, Double> entry : neighborVector.entrySet()) {
                        String destinationId = entry.getKey();
                        double costViaNeighbor = entry.getValue() + link.weight();
                        double currentCost = ownVector.getOrDefault(destinationId, Double.POSITIVE_INFINITY);
                        if (costViaNeighbor < currentCost) {
                            ownVector.put(destinationId, costViaNeighbor);
                            if (router.getId().equals(source.getId())) {
                                predecessor.put(destinationId, neighborId);
                            }
                            changed = true;
                        }
                    }
                }
            }
        }

        Map<String, Double> distances = vectors.getOrDefault(source.getId(), Map.of());
        Map<String, String> nextHop = new HashMap<>();
        for (String destinationId : distances.keySet()) {
            if (destinationId.equals(source.getId())) continue;
            String current = destinationId;
            String prev = predecessor.get(current);
            while (prev != null && !prev.equals(source.getId())) {
                current = prev;
                prev = predecessor.get(current);
            }
            if (prev != null) {
                nextHop.put(destinationId, current);
            }
        }

        return new RoutingTable(source.getId(), nextHop, predecessor, distances);
    }

    @Override
    public String algorithmName() {
        return "distance-vector";
    }
}
