package com.netsim.api.service;

import com.netsim.api.dto.Dtos.SimulationResultDTO;
import com.netsim.api.dto.Dtos.SimulationRunRequestDTO;
import com.netsim.api.dto.Dtos.SimulationRunSummaryDTO;
import com.netsim.domain.NetworkTopology;
import com.netsim.engine.SimulationEngine;
import com.netsim.engine.SimulationEvent;
import com.netsim.engine.SimulationEventType;
import com.netsim.engine.SimulationRequest;
import com.netsim.engine.SimulationResult;
import com.netsim.persistence.entity.SimulationHopEntity;
import com.netsim.persistence.entity.SimulationMetricEntity;
import com.netsim.persistence.entity.SimulationRunEntity;
import com.netsim.persistence.mapper.DomainEntityMapper;
import com.netsim.persistence.repository.SimulationHopJpaRepository;
import com.netsim.persistence.repository.SimulationMetricJpaRepository;
import com.netsim.persistence.repository.SimulationRunJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Coordinates a simulation run: resolves the topology, drives the
 * {@link SimulationEngine}, and persists run history (simulation_run,
 * simulation_hop, simulation_metric) so completed runs and their
 * hop-by-hop trace can be fetched back later, or listed per topology
 * (design doc section 7 & 8).
 */
@Service
public class SimulationOrchestratorService {

    private final TopologyRepository topologyRepository;
    private final SimulationEngine simulationEngine;
    private final SimulationEventCollector eventCollector;
    private final DomainEntityMapper mapper;
    private final SimulationRunJpaRepository runJpaRepository;
    private final SimulationHopJpaRepository hopJpaRepository;
    private final SimulationMetricJpaRepository metricJpaRepository;

    public SimulationOrchestratorService(TopologyRepository topologyRepository,
                                          SimulationEngine simulationEngine,
                                          SimulationEventCollector eventCollector,
                                          DomainEntityMapper mapper,
                                          SimulationRunJpaRepository runJpaRepository,
                                          SimulationHopJpaRepository hopJpaRepository,
                                          SimulationMetricJpaRepository metricJpaRepository) {
        this.topologyRepository = topologyRepository;
        this.simulationEngine = simulationEngine;
        this.eventCollector = eventCollector;
        this.mapper = mapper;
        this.runJpaRepository = runJpaRepository;
        this.hopJpaRepository = hopJpaRepository;
        this.metricJpaRepository = metricJpaRepository;
    }

    @Transactional
    public SimulationResultDTO runSimulation(SimulationRunRequestDTO request) {
        NetworkTopology topology = topologyRepository.get(request.topologyId());
        SimulationRequest engineRequest = SimulationRequest.of(
                request.topologyId(), request.sourceRouterId(), request.destRouterId(), request.algorithm());

        Instant startedAt = Instant.now();
        SimulationResult result = simulationEngine.run(topology, engineRequest);

        persistRun(result, request, startedAt);
        return toDto(result);
    }

    private void persistRun(SimulationResult result, SimulationRunRequestDTO request, Instant startedAt) {
        runJpaRepository.save(mapper.toEntity(result, request.topologyId(), request.sourceRouterId(),
                request.destRouterId(), request.algorithm(), startedAt));

        metricJpaRepository.save(new SimulationMetricEntity(result.runId(), result.totalLatencyMs(),
                result.hopCount(), result.packetLossPct(), 0.0));

        List<SimulationEvent> events = eventCollector.drain(result.runId());
        int sequence = 0;
        for (SimulationEvent event : events) {
            if (event.type() == SimulationEventType.HOP_REACHED
                    || event.type() == SimulationEventType.PACKET_DROPPED
                    || event.type() == SimulationEventType.DELIVERED) {
                hopJpaRepository.save(new SimulationHopEntity(result.runId(), sequence++, event.routerId(),
                        event.simulatedTimeMs(), event.type() == SimulationEventType.PACKET_DROPPED));
            }
        }
    }

    public SimulationResultDTO getRun(String runId) {
        SimulationRunEntity run = runJpaRepository.findById(runId)
                .orElseThrow(() -> new NoSuchElementException("Unknown simulation run: " + runId));
        SimulationMetricEntity metric = metricJpaRepository.findById(runId)
                .orElseThrow(() -> new NoSuchElementException("Missing metrics for run: " + runId));
        List<String> path = hopJpaRepository.findByRunIdOrderBySequenceNoAsc(runId).stream()
                .map(SimulationHopEntity::getRouterId)
                .toList();

        return new SimulationResultDTO(run.getId(), path, metric.getTotalLatencyMs(),
                metric.getHopCount(), metric.getPacketLossPct(), run.getStatus());
    }

    public Page<SimulationRunSummaryDTO> getRunHistory(String topologyId, Pageable pageable) {
        return runJpaRepository.findByTopologyIdOrderByStartedAtDesc(topologyId, pageable)
                .map(run -> new SimulationRunSummaryDTO(run.getId(), run.getSourceRouterId(), run.getDestRouterId(),
                        run.getAlgorithm(), run.getStatus(), run.getStartedAt().toString()));
    }

    private SimulationResultDTO toDto(SimulationResult result) {
        return new SimulationResultDTO(result.runId(), result.path(), result.totalLatencyMs(),
                result.hopCount(), result.packetLossPct(), result.status());
    }
}
