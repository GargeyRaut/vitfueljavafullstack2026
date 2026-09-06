package com.netsim.api.websocket;

import com.netsim.engine.SimulationEvent;

import java.time.Instant;

/** Wire format streamed over STOMP for a single simulation event. */
public record SimulationEventDTO(
        String runId,
        String type,
        String routerId,
        double simulatedTimeMs,
        Instant emittedAt,
        String detail
) {
    public static SimulationEventDTO from(SimulationEvent event) {
        return new SimulationEventDTO(event.runId(), event.type().name(), event.routerId(),
                event.simulatedTimeMs(), event.emittedAt(), event.detail());
    }
}
