package com.netsim.api.controller;

import com.netsim.api.dto.Dtos.TrafficStartRequestDTO;
import com.netsim.api.dto.Dtos.TrafficStatsDTO;
import com.netsim.api.service.TopologyRepository;
import com.netsim.api.service.TrafficGeneratorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** Phase 4: continuous background traffic simulation, one session per topology. */
@RestController
@RequestMapping("/api/topologies/{id}/traffic")
public class TrafficController {

    private final TopologyRepository topologyRepository;
    private final TrafficGeneratorService trafficGeneratorService;

    public TrafficController(TopologyRepository topologyRepository, TrafficGeneratorService trafficGeneratorService) {
        this.topologyRepository = topologyRepository;
        this.trafficGeneratorService = trafficGeneratorService;
    }

    @PostMapping("/start")
    public TrafficStatsDTO start(@PathVariable String id, @Valid @RequestBody TrafficStartRequestDTO request) {
        topologyRepository.get(id); // 404s if unknown
        return trafficGeneratorService.start(id, request.algorithm(), request.intervalMs());
    }

    @PostMapping("/stop")
    public TrafficStatsDTO stop(@PathVariable String id) {
        topologyRepository.get(id); // 404s if unknown
        return trafficGeneratorService.stop(id);
    }

    @GetMapping("/stats")
    public TrafficStatsDTO stats(@PathVariable String id) {
        topologyRepository.get(id); // 404s if unknown
        return trafficGeneratorService.stats(id);
    }
}
