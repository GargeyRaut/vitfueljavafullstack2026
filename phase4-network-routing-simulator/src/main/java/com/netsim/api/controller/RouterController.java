package com.netsim.api.controller;

import com.netsim.api.dto.Dtos.RouterDTO;
import com.netsim.api.dto.Dtos.RouterStatusUpdateRequest;
import com.netsim.api.service.TopologyRepository;
import com.netsim.domain.Interface;
import com.netsim.domain.NetworkTopology;
import com.netsim.domain.Router;
import com.netsim.domain.RouterStatus;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * FR-6 fault injection: bring a router up/down mid-simulation to test
 * re-routing / convergence. Note: since routers live inside a topology,
 * this endpoint is topology-scoped even though the design doc's summary
 * table shows a flat /api/routers/{id}/status path.
 */
@RestController
@RequestMapping("/api/topologies/{topologyId}/routers")
public class RouterController {

    private final TopologyRepository topologyRepository;

    public RouterController(TopologyRepository topologyRepository) {
        this.topologyRepository = topologyRepository;
    }

    @PatchMapping("/{routerId}/status")
    public RouterDTO updateStatus(@PathVariable String topologyId, @PathVariable String routerId,
                                   @Valid @RequestBody RouterStatusUpdateRequest request) {
        NetworkTopology topology = topologyRepository.get(topologyId);
        RouterStatus status = RouterStatus.valueOf(request.status().toUpperCase());
        topology.setRouterStatus(routerId, status);
        Router router = topology.requireRouter(routerId);
        topologyRepository.checkpointRouterStatus(router);
        return new RouterDTO(router.getId(), router.getLabel(), router.getStatus().name(),
                router.getInterfaces().values().stream().map(Interface::name).toList());
    }
}
