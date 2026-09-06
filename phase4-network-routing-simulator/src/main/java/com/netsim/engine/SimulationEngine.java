package com.netsim.engine;

import com.netsim.domain.*;
import com.netsim.domain.algorithm.RoutingAlgorithm;
import com.netsim.domain.algorithm.RoutingAlgorithmRegistry;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static com.netsim.engine.SimulationEventType.*;

/**
 * Discrete-event loop: advances simulated time, moves a packet hop-by-hop
 * across the path computed by the selected {@link RoutingAlgorithm}, and
 * emits {@link SimulationEvent}s as it goes (design doc section 9 & 10).
 *
 * Each run advances a virtual clock (cumulative link latency) rather than
 * wall-clock time, keeping runs deterministic and reproducible.
 */
@Component
public class SimulationEngine {

    private final RoutingAlgorithmRegistry algorithmRegistry;
    private final LinkSimulator linkSimulator;
    private final ApplicationEventPublisher events;
    private final AtomicLong sequenceGenerator = new AtomicLong();

    public SimulationEngine(RoutingAlgorithmRegistry algorithmRegistry,
                             LinkSimulator linkSimulator,
                             ApplicationEventPublisher events) {
        this.algorithmRegistry = algorithmRegistry;
        this.linkSimulator = linkSimulator;
        this.events = events;
    }

    public SimulationResult run(NetworkTopology topology, SimulationRequest request) {
        String runId = "run-" + UUID.randomUUID().toString().substring(0, 8);

        Router source = topology.requireRouter(request.sourceRouterId());
        Router destination = topology.requireRouter(request.destRouterId());

        RoutingAlgorithm algorithm = algorithmRegistry.resolve(request.algorithm());
        RoutingTable table = algorithm.computeRoutes(topology, source);
        source.applyRoutingTable(table);
        events.publishEvent(SimulationEvent.of(runId, ROUTE_RECOMPUTED, source.getId(), 0, "algorithm=" + algorithm.algorithmName()));

        List<String> pathIds = table.resolvePath(destination.getId());
        if (pathIds.isEmpty() || !pathIds.get(0).equals(source.getId())) {
            return SimulationResult.unreachable(runId, source.getId());
        }

        Packet packet = new Packet(source.getId(), destination.getId(), request.packetSizeBytes(),
                request.ttl(), sequenceGenerator.incrementAndGet());
        packet.setStatus(PacketStatus.QUEUED);

        double simulatedTimeMs = 0.0;
        for (int hop = 0; hop < pathIds.size() - 1; hop++) {
            String fromId = pathIds.get(hop);
            String toId = pathIds.get(hop + 1);

            Link link = topology.linkBetween(fromId, toId)
                    .orElse(null);
            if (link == null) {
                events.publishEvent(SimulationEvent.of(runId, LINK_DOWN, fromId, simulatedTimeMs, "no active link to " + toId));
                packet.setStatus(PacketStatus.DROPPED);
                return SimulationResult.dropped(runId, pathIds.subList(0, hop + 1), simulatedTimeMs);
            }

            packet.setStatus(PacketStatus.IN_TRANSIT);
            TransmissionOutcome outcome = linkSimulator.transmit(packet, link);

            if (outcome.isDropped()) {
                packet.setStatus(PacketStatus.DROPPED);
                events.publishEvent(SimulationEvent.of(runId, PACKET_DROPPED, toId, simulatedTimeMs, outcome.reason()));
                return SimulationResult.dropped(runId, pathIds.subList(0, hop + 1), simulatedTimeMs);
            }

            simulatedTimeMs += outcome.latencyMs();
            events.publishEvent(SimulationEvent.of(runId, HOP_REACHED, toId, simulatedTimeMs,
                    "hop " + (hop + 1) + "/" + (pathIds.size() - 1)));
        }

        packet.setStatus(PacketStatus.DELIVERED);
        events.publishEvent(SimulationEvent.of(runId, DELIVERED, destination.getId(), simulatedTimeMs, "packet " + packet.getId()));

        return SimulationResult.delivered(runId, pathIds, simulatedTimeMs);
    }
}
