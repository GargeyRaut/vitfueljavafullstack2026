package com.netsim.api.controller;

import com.netsim.api.dto.Dtos.BatchSimulationRequestDTO;
import com.netsim.api.dto.Dtos.BatchSimulationResultDTO;
import com.netsim.api.dto.Dtos.SimulationResultDTO;
import com.netsim.api.dto.Dtos.SimulationRunRequestDTO;
import com.netsim.api.dto.Dtos.SimulationRunSummaryDTO;
import com.netsim.api.service.BatchSimulationService;
import com.netsim.api.service.SimulationOrchestratorService;
import com.netsim.api.service.TopologyRepository;
import com.netsim.domain.NetworkTopology;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simulations")
public class SimulationController {

    private final SimulationOrchestratorService orchestrator;
    private final BatchSimulationService batchSimulationService;
    private final TopologyRepository topologyRepository;

    public SimulationController(SimulationOrchestratorService orchestrator,
                                 BatchSimulationService batchSimulationService,
                                 TopologyRepository topologyRepository) {
        this.orchestrator = orchestrator;
        this.batchSimulationService = batchSimulationService;
        this.topologyRepository = topologyRepository;
    }

    @PostMapping("/run")
    @ResponseStatus(HttpStatus.OK)
    public SimulationResultDTO run(@Valid @RequestBody SimulationRunRequestDTO request) {
        return orchestrator.runSimulation(request);
    }

    /** Phase 4: fire N packets concurrently and return aggregate loss/latency/jitter stats. */
    @PostMapping("/run-batch")
    @ResponseStatus(HttpStatus.OK)
    public BatchSimulationResultDTO runBatch(@Valid @RequestBody BatchSimulationRequestDTO request) {
        NetworkTopology topology = topologyRepository.get(request.topologyId());
        return batchSimulationService.run(topology, request);
    }

    @GetMapping("/{runId}")
    public SimulationResultDTO get(@PathVariable String runId) {
        return orchestrator.getRun(runId);
    }

    /** Phase 2: paginated run-history listing for a topology (design doc section 8). */
    @GetMapping
    public Page<SimulationRunSummaryDTO> list(@RequestParam String topologyId,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orchestrator.getRunHistory(topologyId, pageable);
    }
}
