package com.netsim.api.controller;

import com.netsim.api.dto.Dtos.SnapshotDTO;
import com.netsim.api.service.SnapshotService;
import com.netsim.api.service.TopologyRepository;
import com.netsim.domain.NetworkTopology;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Phase 4: point-in-time topology snapshots, persisted via raw JDBC (not JPA). */
@RestController
@RequestMapping("/api/topologies")
public class SnapshotController {

    private final TopologyRepository topologyRepository;
    private final SnapshotService snapshotService;

    public SnapshotController(TopologyRepository topologyRepository, SnapshotService snapshotService) {
        this.topologyRepository = topologyRepository;
        this.snapshotService = snapshotService;
    }

    @PostMapping("/{id}/snapshots")
    @ResponseStatus(HttpStatus.CREATED)
    public SnapshotDTO capture(@PathVariable String id) {
        NetworkTopology topology = topologyRepository.get(id);
        return snapshotService.capture(topology);
    }

    @GetMapping("/{id}/snapshots")
    public List<SnapshotDTO> history(@PathVariable String id) {
        topologyRepository.get(id); // 404s if unknown
        return snapshotService.history(id);
    }
}
