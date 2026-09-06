package com.netsim.api.controller;

import com.netsim.api.dto.Dtos.TopologyAnalysisDTO;
import com.netsim.api.service.AnalysisService;
import com.netsim.api.service.TopologyRepository;
import com.netsim.domain.NetworkTopology;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/topologies")
public class AnalysisController {

    private final TopologyRepository topologyRepository;
    private final AnalysisService analysisService;

    public AnalysisController(TopologyRepository topologyRepository, AnalysisService analysisService) {
        this.topologyRepository = topologyRepository;
        this.analysisService = analysisService;
    }

    /** Phase 3: simulate every router pair and return reachability + latency stats. */
    @GetMapping("/{id}/analyze")
    public TopologyAnalysisDTO analyze(@PathVariable String id,
                                        @RequestParam(defaultValue = "dijkstra") String algorithm) {
        NetworkTopology topology = topologyRepository.get(id);
        return analysisService.analyze(topology, algorithm);
    }
}
