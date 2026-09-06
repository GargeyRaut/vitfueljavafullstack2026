package com.netsim.api.service;

import com.netsim.engine.SimulationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Observer-pattern buffer: listens for every {@link SimulationEvent}
 * published by the {@code SimulationEngine} and accumulates them per
 * run id, so {@link SimulationOrchestratorService} can persist the full
 * hop-by-hop trace as {@code simulation_hop} rows once a run finishes,
 * independent of the (separately subscribed) live WebSocket stream.
 */
@Component
public class SimulationEventCollector {

    private final Map<String, List<SimulationEvent>> buffered = new ConcurrentHashMap<>();

    @EventListener
    public void onEvent(SimulationEvent event) {
        buffered.computeIfAbsent(event.runId(), id -> new CopyOnWriteArrayList<>()).add(event);
    }

    /** Returns and removes the buffered events for a run, freeing memory once drained. */
    public List<SimulationEvent> drain(String runId) {
        List<SimulationEvent> events = buffered.remove(runId);
        return events == null ? List.of() : events;
    }
}
