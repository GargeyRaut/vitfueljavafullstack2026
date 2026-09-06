package com.netsim.domain.algorithm;

import com.netsim.domain.Link;
import com.netsim.domain.NetworkTopology;
import com.netsim.domain.Router;
import com.netsim.domain.RoutingTable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Classic Bellman-Ford relaxation over all active links, |V|-1 times.
 * Slower than Dijkstra but tolerates negative-ish weighting schemes and
 * mirrors distance-vector style convergence.
 */
@Component("bellmanFord")
public class BellmanFordRoutingAlgorithm implements RoutingAlgorithm {

    @Override
    public RoutingTable computeRoutes(NetworkTopology topology, Router source) {
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> predecessor = new HashMap<>();

        for (Router r : topology.getRouters()) {
            dist.put(r.getId(), Double.POSITIVE_INFINITY);
        }
        dist.put(source.getId(), 0.0);

        int vertexCount = topology.getRouters().size();
        for (int i = 0; i < Math.max(0, vertexCount - 1); i++) {
            boolean relaxed = false;
            for (Link link : topology.getLinks()) {
                if (!link.isUp()) continue;
                relaxed |= relax(dist, predecessor, link.getSourceRouterId(), link.getTargetRouterId(), link.weight());
                if (link.isBidirectional()) {
                    relaxed |= relax(dist, predecessor, link.getTargetRouterId(), link.getSourceRouterId(), link.weight());
                }
            }
            if (!relaxed) break; // converged early
        }

        Map<String, String> nextHop = new HashMap<>();
        for (String destinationId : dist.keySet()) {
            if (destinationId.equals(source.getId()) || !predecessor.containsKey(destinationId)) continue;
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

        return new RoutingTable(source.getId(), nextHop, predecessor, dist);
    }

    private boolean relax(Map<String, Double> dist, Map<String, String> predecessor,
                           String fromId, String toId, double weight) {
        double fromDist = dist.getOrDefault(fromId, Double.POSITIVE_INFINITY);
        double candidate = fromDist + weight;
        if (fromDist != Double.POSITIVE_INFINITY && candidate < dist.getOrDefault(toId, Double.POSITIVE_INFINITY)) {
            dist.put(toId, candidate);
            predecessor.put(toId, fromId);
            return true;
        }
        return false;
    }

    @Override
    public String algorithmName() {
        return "bellman-ford";
    }
}
