package com.netsim.api.service;

import com.netsim.api.dto.Dtos.TrafficStatsDTO;
import com.netsim.domain.NetworkTopology;
import com.netsim.domain.Router;
import com.netsim.engine.SimulationEngine;
import com.netsim.engine.SimulationRequest;
import com.netsim.engine.SimulationResult;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * Phase 4: simulates ongoing background traffic — every {@code intervalMs},
 * a packet is sent between two random reachable routers in the topology
 * and the running totals are updated. One session per topology id; start
 * it, watch stats climb, stop it. Ticks are not persisted individually
 * (would flood run history) — only the cumulative counters are kept, in
 * memory, for as long as the session runs.
 */
@Service
public class TrafficGeneratorService {

    private final SimulationEngine simulationEngine;
    private final TopologyRepository topologyRepository;
    private final SimulationEventCollector eventCollector;
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public TrafficGeneratorService(SimulationEngine simulationEngine, TopologyRepository topologyRepository,
                                    SimulationEventCollector eventCollector) {
        this.simulationEngine = simulationEngine;
        this.topologyRepository = topologyRepository;
        this.eventCollector = eventCollector;
    }

    public TrafficStatsDTO start(String topologyId, String algorithm, int intervalMs) {
        stop(topologyId); // restart cleanly if already running

        Session session = new Session(algorithm);
        ThreadFactory daemonFactory = runnable -> {
            Thread t = new Thread(runnable, "traffic-" + topologyId);
            t.setDaemon(true);
            return t;
        };
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(daemonFactory);
        session.executor = executor;

        executor.scheduleAtFixedRate(() -> tick(topologyId, session), 0, Math.max(intervalMs, 50), TimeUnit.MILLISECONDS);
        sessions.put(topologyId, session);
        return stats(topologyId);
    }

    public TrafficStatsDTO stop(String topologyId) {
        Session session = sessions.remove(topologyId);
        if (session != null && session.executor != null) {
            session.executor.shutdownNow();
        }
        return new TrafficStatsDTO(topologyId, false, session == null ? null : session.algorithm,
                session == null ? 0 : session.sent.get(),
                session == null ? 0 : session.delivered.get(),
                session == null ? 0 : session.dropped.get(),
                session == null ? 0.0 : average(session));
    }

    public TrafficStatsDTO stats(String topologyId) {
        Session session = sessions.get(topologyId);
        if (session == null) {
            return new TrafficStatsDTO(topologyId, false, null, 0, 0, 0, 0.0);
        }
        return new TrafficStatsDTO(topologyId, true, session.algorithm,
                session.sent.get(), session.delivered.get(), session.dropped.get(), average(session));
    }

    private void tick(String topologyId, Session session) {
        try {
            NetworkTopology topology = topologyRepository.get(topologyId);
            List<Router> up = topology.getRouters().stream().filter(Router::isReachable).toList();
            if (up.size() < 2) return;

            Router source = up.get(ThreadLocalRandom.current().nextInt(up.size()));
            Router dest;
            do {
                dest = up.get(ThreadLocalRandom.current().nextInt(up.size()));
            } while (dest.getId().equals(source.getId()));

            SimulationResult result = simulationEngine.run(topology,
                    SimulationRequest.of(topologyId, source.getId(), dest.getId(), session.algorithm));
            eventCollector.drain(result.runId()); // background tick — not persisted or subscribed to

            session.sent.incrementAndGet();
            if ("DELIVERED".equals(result.status())) {
                session.delivered.incrementAndGet();
                session.latencySum.add(result.totalLatencyMs());
            } else {
                session.dropped.incrementAndGet();
            }
        } catch (Exception ignored) {
            // topology may have been deleted mid-session, or a transient error — just skip this tick
        }
    }

    private double average(Session session) {
        long delivered = session.delivered.get();
        return delivered == 0 ? 0.0 : session.latencySum.sum() / delivered;
    }

    @PreDestroy
    public void shutdownAll() {
        sessions.values().forEach(s -> { if (s.executor != null) s.executor.shutdownNow(); });
    }

    private static final class Session {
        final String algorithm;
        final AtomicLong sent = new AtomicLong();
        final AtomicLong delivered = new AtomicLong();
        final AtomicLong dropped = new AtomicLong();
        final DoubleAdder latencySum = new DoubleAdder();
        volatile ScheduledExecutorService executor;

        Session(String algorithm) {
            this.algorithm = algorithm;
        }
    }
}
