package com.netsim.api.controller;

import com.netsim.api.dto.Dtos.LinkDTO;
import com.netsim.api.dto.Dtos.LinkStatusUpdateRequest;
import com.netsim.api.service.TopologyRepository;
import com.netsim.domain.Link;
import com.netsim.domain.LinkStatus;
import com.netsim.domain.NetworkTopology;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** FR-6 fault injection: bring a link up/down mid-simulation. */
@RestController
@RequestMapping("/api/topologies/{topologyId}/links")
public class LinkController {

    private final TopologyRepository topologyRepository;

    public LinkController(TopologyRepository topologyRepository) {
        this.topologyRepository = topologyRepository;
    }

    @PatchMapping("/{linkId}/status")
    public LinkDTO updateStatus(@PathVariable String topologyId, @PathVariable String linkId,
                                 @Valid @RequestBody LinkStatusUpdateRequest request) {
        NetworkTopology topology = topologyRepository.get(topologyId);
        LinkStatus status = LinkStatus.valueOf(request.status().toUpperCase());
        topology.setLinkStatus(linkId, status);
        Link link = topology.requireLink(linkId);
        topologyRepository.checkpointLinkStatus(link);
        return new LinkDTO(link.getId(), link.getSourceRouterId(), link.getTargetRouterId(),
                link.getBandwidthMbps(), link.getLatencyMs(), link.getReliabilityPct(), link.getStatus().name());
    }
}
