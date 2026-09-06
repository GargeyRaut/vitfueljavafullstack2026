package com.netsim.engine;

import java.util.List;

/**
 * Final outcome of a simulation run: path taken, total latency, hop count,
 * and loss percentage (FR-9).
 */
public record SimulationResult(
        String runId,
        List<String> path,
        double totalLatencyMs,
        int hopCount,
        double packetLossPct,
        String status // DELIVERED or DROPPED
) {
    public static SimulationResult delivered(String runId, List<String> path, double totalLatencyMs) {
        return new SimulationResult(runId, path, totalLatencyMs, Math.max(0, path.size() - 1), 0.0, "DELIVERED");
    }

    public static SimulationResult dropped(String runId, List<String> partialPath, double totalLatencyMs) {
        int hops = Math.max(0, partialPath.size() - 1);
        return new SimulationResult(runId, partialPath, totalLatencyMs, hops, 100.0, "DROPPED");
    }

    public static SimulationResult unreachable(String runId, String sourceId) {
        return new SimulationResult(runId, List.of(sourceId), 0.0, 0, 100.0, "UNREACHABLE");
    }
}
