package com.netsim.api.service;

import com.netsim.api.dto.Dtos.SnapshotDTO;
import com.netsim.domain.Link;
import com.netsim.domain.NetworkTopology;
import com.netsim.persistence.repository.TopologySnapshotJdbcRepository;
import com.netsim.persistence.repository.TopologySnapshotJdbcRepository.SnapshotRow;
import org.springframework.stereotype.Service;

import java.util.List;

/** Phase 4: point-in-time topology snapshots, stored via raw JDBC. */
@Service
public class SnapshotService {

    private final TopologySnapshotJdbcRepository snapshotRepository;

    public SnapshotService(TopologySnapshotJdbcRepository snapshotRepository) {
        this.snapshotRepository = snapshotRepository;
    }

    public SnapshotDTO capture(NetworkTopology topology) {
        int routerCount = topology.getRouters().size();
        int linkCount = topology.getLinks().size();
        double avgLatency = topology.getLinks().stream().mapToDouble(Link::getLatencyMs).average().orElse(0.0);

        String id = snapshotRepository.insert(topology.getId(), routerCount, linkCount, avgLatency);
        return new SnapshotDTO(id, topology.getId(), routerCount, linkCount, avgLatency, java.time.Instant.now().toString());
    }

    public List<SnapshotDTO> history(String topologyId) {
        return snapshotRepository.findByTopologyId(topologyId).stream()
                .map(this::toDto)
                .toList();
    }

    private SnapshotDTO toDto(SnapshotRow row) {
        return new SnapshotDTO(row.id(), row.topologyId(), row.routerCount(), row.linkCount(),
                row.avgLinkLatencyMs(), row.capturedAt().toString());
    }
}
