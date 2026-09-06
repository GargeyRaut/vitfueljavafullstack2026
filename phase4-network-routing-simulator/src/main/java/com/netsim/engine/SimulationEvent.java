package com.netsim.engine;

import java.time.Instant;

/**
 * Immutable record of a state change during a simulation run
 * (HOP_REACHED, PACKET_DROPPED, LINK_DOWN, ROUTE_RECOMPUTED). Published
 * to WebSocket subscribers and persisted asynchronously (Observer pattern).
 */
public record SimulationEvent(
        String runId,
        SimulationEventType type,
        String routerId,
        double simulatedTimeMs,
        Instant emittedAt,
        String detail
) {
    public static SimulationEvent of(String runId, SimulationEventType type, String routerId,
                                      double simulatedTimeMs, String detail) {
        return new SimulationEvent(runId, type, routerId, simulatedTimeMs, Instant.now(), detail);
    }
}
