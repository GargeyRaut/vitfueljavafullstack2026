package com.netsim.api.service;

import com.netsim.api.dto.Dtos.BatchSimulationRequestDTO;
import com.netsim.api.dto.Dtos.BatchSimulationResultDTO;
import com.netsim.domain.NetworkTopology;
import com.netsim.engine.SimulationEngine;
import com.netsim.engine.SimulationRequest;
import com.netsim.engine.SimulationResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Phase 4: fires many packets between the same source/destination pair
 * concurrently (loss and jitter are probabilistic per-packet in
 * {@code LinkSimulator}, so a single run doesn't show the real
 * distribution) and aggregates loss %, average/min/max latency, and
 * jitter (latency standard deviation among delivered packets).
 *
 * Individual packets are not persisted as simulation_run rows — only
 * the aggregate is returned — to avoid flooding run history with
 * hundreds of near-identical rows per batch.
 */
@Service
public class BatchSimulationService {

    private static final int MAX_PACKETS = 500;

    private final SimulationEngine simulationEngine;
    private final SimulationEventCollector eventCollector;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public BatchSimulationService(SimulationEngine simulationEngine, SimulationEventCollector eventCollector) {
        this.simulationEngine = simulationEngine;
        this.eventCollector = eventCollector;
    }

    public BatchSimulationResultDTO run(NetworkTopology topology, BatchSimulationRequestDTO request) {
        int packetCount = Math.min(Math.max(request.packetCount(), 1), MAX_PACKETS);
        SimulationRequest engineRequest = SimulationRequest.of(
                topology.getId(), request.sourceRouterId(), request.destRouterId(), request.algorithm());

        List<CompletableFuture<SimulationResult>> futures = java.util.stream.IntStream.range(0, packetCount)
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                    SimulationResult result = simulationEngine.run(topology, engineRequest);
                    eventCollector.drain(result.runId()); // probe packet — discard buffered events
                    return result;
                }, executor))
                .toList();

        List<SimulationResult> results = futures.stream().map(CompletableFuture::join).toList();

        long delivered = results.stream().filter(r -> "DELIVERED".equals(r.status())).count();
        long dropped = packetCount - delivered;

        double[] latencies = results.stream()
                .filter(r -> "DELIVERED".equals(r.status()))
                .mapToDouble(SimulationResult::totalLatencyMs)
                .toArray();

        double avg = average(latencies);
        double min = latencies.length == 0 ? 0.0 : java.util.Arrays.stream(latencies).min().orElse(0.0);
        double max = latencies.length == 0 ? 0.0 : java.util.Arrays.stream(latencies).max().orElse(0.0);
        double jitter = stdDev(latencies, avg);
        double lossPct = packetCount == 0 ? 0.0 : (dropped * 100.0) / packetCount;

        return new BatchSimulationResultDTO(topology.getId(), request.algorithm(), packetCount,
                (int) delivered, (int) dropped, lossPct, avg, min, max, jitter);
    }

    private double average(double[] values) {
        return values.length == 0 ? 0.0 : java.util.Arrays.stream(values).average().orElse(0.0);
    }

    private double stdDev(double[] values, double mean) {
        if (values.length < 2) return 0.0;
        double sumSq = 0.0;
        for (double v : values) {
            sumSq += (v - mean) * (v - mean);
        }
        return Math.sqrt(sumSq / (values.length - 1));
    }
}
