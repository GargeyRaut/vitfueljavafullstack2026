package com.netsim.api.controller;

import com.netsim.api.dto.Dtos.*;
import com.netsim.api.service.TopologyRepository;
import com.netsim.domain.Interface;
import com.netsim.domain.Link;
import com.netsim.domain.NetworkTopology;
import com.netsim.domain.Router;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/topologies")
public class TopologyController {

    private final TopologyRepository topologyRepository;

    public TopologyController(TopologyRepository topologyRepository) {
        this.topologyRepository = topologyRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TopologyDTO create(@Valid @RequestBody CreateTopologyRequest request) {
        NetworkTopology topology = topologyRepository.create(request.name());
        return toDto(topology);
    }

    /** Phase 3: list every topology currently known (in memory, rehydrated from DB on startup). */
    @GetMapping
    public List<TopologyDTO> list() {
        List<TopologyDTO> result = new java.util.ArrayList<>();
        topologyRepository.all().forEach(t -> result.add(toDto(t)));
        return result;
    }

    @GetMapping("/{id}")
    public TopologyDTO get(@PathVariable String id) {
        return toDto(topologyRepository.get(id));
    }

    /** Phase 3: full graph (all routers + links), also used as the export payload. */
    @GetMapping("/{id}/export")
    public TopologyExportDTO export(@PathVariable String id) {
        NetworkTopology topology = topologyRepository.get(id);
        List<RouterDTO> routers = topology.getRouters().stream().map(this::toDto).toList();
        List<LinkDTO> links = topology.getLinks().stream().map(this::toDto).toList();
        return new TopologyExportDTO(topology.getId(), topology.getName(), routers, links);
    }

    /** Phase 3: bulk-create a topology from a JSON payload, referencing routers by label. */
    @PostMapping("/import")
    @ResponseStatus(HttpStatus.CREATED)
    public TopologyExportDTO importTopology(@Valid @RequestBody TopologyImportRequest request) {
        NetworkTopology topology = topologyRepository.create(request.name());
        Map<String, String> idByLabel = new HashMap<>();

        for (RouterImport ri : request.routers() == null ? List.<RouterImport>of() : request.routers()) {
            String routerId = "R-" + UUID.randomUUID().toString().substring(0, 6);
            topologyRepository.addRouter(topology, new Router(routerId, ri.label()));
            idByLabel.put(ri.label(), routerId);
        }

        for (LinkImport li : request.links() == null ? List.<LinkImport>of() : request.links()) {
            String sourceId = requireLabel(idByLabel, li.sourceLabel());
            String targetId = requireLabel(idByLabel, li.targetLabel());
            String linkId = "L-" + UUID.randomUUID().toString().substring(0, 6);
            topologyRepository.addLink(topology, new Link(linkId, sourceId, targetId,
                    li.bandwidthMbps(), li.latencyMs(), li.reliabilityPct(), li.bidirectional()));
        }

        return export(topology.getId());
    }

    /** Phase 3: delete a topology and everything in it. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        topologyRepository.get(id); // 404s if unknown
        topologyRepository.deleteTopology(id);
    }

    @PostMapping("/{id}/routers")
    @ResponseStatus(HttpStatus.CREATED)
    public RouterDTO addRouter(@PathVariable String id, @Valid @RequestBody AddRouterRequest request) {
        NetworkTopology topology = topologyRepository.get(id);
        String routerId = "R-" + UUID.randomUUID().toString().substring(0, 6);
        Router router = topologyRepository.addRouter(topology, new Router(routerId, request.label()));
        return toDto(router);
    }

    /** Phase 3: remove a router (and any links touching it). */
    @DeleteMapping("/{id}/routers/{routerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRouter(@PathVariable String id, @PathVariable String routerId) {
        NetworkTopology topology = topologyRepository.get(id);
        topology.requireRouter(routerId); // 404s if unknown
        topologyRepository.removeRouter(topology, routerId);
    }

    @PostMapping("/{id}/links")
    @ResponseStatus(HttpStatus.CREATED)
    public LinkDTO addLink(@PathVariable String id, @Valid @RequestBody AddLinkRequest request) {
        NetworkTopology topology = topologyRepository.get(id);
        topology.requireRouter(request.sourceId());
        topology.requireRouter(request.targetId());
        String linkId = "L-" + UUID.randomUUID().toString().substring(0, 6);
        Link link = topologyRepository.addLink(topology, new Link(linkId, request.sourceId(), request.targetId(),
                request.bandwidthMbps(), request.latencyMs(), request.reliabilityPct(), request.bidirectional()));
        return toDto(link);
    }

    /** Phase 3: remove a link. */
    @DeleteMapping("/{id}/links/{linkId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLink(@PathVariable String id, @PathVariable String linkId) {
        NetworkTopology topology = topologyRepository.get(id);
        topology.requireLink(linkId); // 404s if unknown
        topologyRepository.removeLink(topology, linkId);
    }

    private String requireLabel(Map<String, String> idByLabel, String label) {
        String id = idByLabel.get(label);
        if (id == null) {
            throw new IllegalArgumentException("Link references unknown router label: " + label);
        }
        return id;
    }

    private TopologyDTO toDto(NetworkTopology topology) {
        return new TopologyDTO(topology.getId(), topology.getName(),
                topology.getRouters().size(), topology.getLinks().size());
    }

    private RouterDTO toDto(Router router) {
        return new RouterDTO(router.getId(), router.getLabel(), router.getStatus().name(),
                router.getInterfaces().values().stream().map(Interface::name).toList());
    }

    private LinkDTO toDto(Link link) {
        return new LinkDTO(link.getId(), link.getSourceRouterId(), link.getTargetRouterId(),
                link.getBandwidthMbps(), link.getLatencyMs(), link.getReliabilityPct(), link.getStatus().name());
    }
}
