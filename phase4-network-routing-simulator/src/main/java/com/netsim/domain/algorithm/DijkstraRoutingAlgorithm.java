package com.netsim.domain.algorithm;

import com.netsim.domain.Link;
import com.netsim.domain.NetworkTopology;
import com.netsim.domain.Router;
import com.netsim.domain.RoutingTable;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Priority-queue based shortest-path computation over {@link Link#weight()}
 * (link latency), rooted at the given source router.
 */
@Component("dijkstra")
public class DijkstraRoutingAlgorithm implements RoutingAlgorithm {

    @Override
    public RoutingTable computeRoutes(NetworkTopology topology, Router source) {
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> predecessor = new HashMap<>();
        Set<String> visited = new HashSet<>();

        for (Router r : topology.getRouters()) {
            dist.put(r.getId(), Double.POSITIVE_INFINITY);
        }
        dist.put(source.getId(), 0.0);

        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingDouble(dist::get));
        queue.add(source.getId());

        while (!queue.isEmpty()) {
            String currentId = queue.poll();
            if (!visited.add(currentId)) continue;

            for (Link link : topology.activeLinksFor(currentId)) {
                String neighborId = link.otherEnd(currentId);
                Router neighbor = topology.getRouters().stream()
                        .filter(r -> r.getId().equals(neighborId)).findFirst().orElse(null);
                if (neighbor == null || !neighbor.isReachable() || visited.contains(neighborId)) continue;

                double candidate = dist.get(currentId) + link.weight();
                if (candidate < dist.getOrDefault(neighborId, Double.POSITIVE_INFINITY)) {
                    dist.put(neighborId, candidate);
                    predecessor.put(neighborId, currentId);
                    queue.add(neighborId);
                }
            }
        }

        Map<String, String> nextHop = computeNextHops(source.getId(), predecessor, dist.keySet());
        return new RoutingTable(source.getId(), nextHop, predecessor, dist);
    }

    /** Walks each destination's predecessor chain back to the source to find the first hop. */
    private Map<String, String> computeNextHops(String sourceId, Map<String, String> predecessor, Set<String> destinations) {
        Map<String, String> nextHop = new HashMap<>();
        for (String destinationId : destinations) {
            if (destinationId.equals(sourceId) || !predecessor.containsKey(destinationId)) continue;
            String current = destinationId;
            String prev = predecessor.get(current);
            while (prev != null && !prev.equals(sourceId)) {
                current = prev;
                prev = predecessor.get(current);
            }
            if (prev != null) {
                nextHop.put(destinationId, current);
            }
        }
        return nextHop;
    }

    @Override
    public String algorithmName() {
        return "dijkstra";
    }
}
