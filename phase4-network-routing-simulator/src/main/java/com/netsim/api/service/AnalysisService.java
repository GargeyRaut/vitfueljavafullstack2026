package com.netsim.api.service;

import com.netsim.api.dto.Dtos.PairResultDTO;
import com.netsim.api.dto.Dtos.TopologyAnalysisDTO;
import com.netsim.domain.NetworkTopology;
import com.netsim.domain.Router;
import com.netsim.engine.SimulationEngine;
import com.netsim.engine.SimulationRequest;
import com.netsim.engine.SimulationResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Phase 3: network-wide reachability/latency snapshot. Runs a
 * simulated packet between every ordered pair of routers using the
 * given algorithm and aggregates the results — useful for spotting
 * unreachable pairs or latency hot-spots after a topology change,
 * without having to trigger runs one at a time.
 *
 * These probe runs are intentionally NOT persisted as simulation_run
 * rows (an N-router topology means N*(N-1) of them) — only the
 * aggregate is returned. Use POST /api/simulations/run for a run you
 * want kept in history.
 */
@Service
public class AnalysisService {

    private final SimulationEngine simulationEngine;
    private final SimulationEventCollector eventCollector;

    public AnalysisService(SimulationEngine simulationEngine, SimulationEventCollector eventCollector) {
        this.simulationEngine = simulationEngine;
        this.eventCollector = eventCollector;
    }

    public TopologyAnalysisDTO analyze(NetworkTopology topology, String algorithm) {
        List<Router> routers = new ArrayList<>(topology.getRouters());
        List<PairResultDTO> pairs = new ArrayList<>();

        int reachable = 0;
        double latencySum = 0.0;

        for (Router source : routers) {
            for (Router destination : routers) {
                if (source.getId().equals(destination.getId())) continue;

                SimulationRequest request = SimulationRequest.of(topology.getId(), source.getId(), destination.getId(), algorithm);
                SimulationResult result = simulationEngine.run(topology, request);
                eventCollector.drain(result.runId()); // discard — probe run, not kept in history

                pairs.add(new PairResultDTO(source.getId(), destination.getId(), result.status(),
                        result.totalLatencyMs(), result.hopCount()));

                if ("DELIVERED".equals(result.status())) {
                    reachable++;
                    latencySum += result.totalLatencyMs();
                }
            }
        }

        double avgLatency = reachable == 0 ? 0.0 : latencySum / reachable;
        return new TopologyAnalysisDTO(topology.getId(), algorithm, pairs.size(), reachable, avgLatency, pairs);
    }
}
