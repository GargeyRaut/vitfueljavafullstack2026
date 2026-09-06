package com.netsim.persistence.mapper;

import com.netsim.domain.Link;
import com.netsim.domain.NetworkTopology;
import com.netsim.domain.Router;
import com.netsim.engine.SimulationResult;
import com.netsim.persistence.entity.LinkEntity;
import com.netsim.persistence.entity.RouterEntity;
import com.netsim.persistence.entity.SimulationRunEntity;
import com.netsim.persistence.entity.TopologyEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Maps in-memory domain objects (the live, framework-free graph used
 * during a simulation) to/from JPA entities used for durable storage,
 * isolating persistence concerns from the domain model (Repository
 * pattern, design doc 3.2).
 */
@Component
public class DomainEntityMapper {

    public TopologyEntity toEntity(NetworkTopology topology) {
        return new TopologyEntity(topology.getId(), topology.getName(), Instant.now());
    }

    public RouterEntity toEntity(Router router, String topologyId) {
        return new RouterEntity(router.getId(), topologyId, router.getLabel(), router.getStatus().name());
    }

    public LinkEntity toEntity(Link link, String topologyId) {
        return new LinkEntity(link.getId(), topologyId, link.getSourceRouterId(), link.getTargetRouterId(),
                link.getBandwidthMbps(), link.getLatencyMs(), link.getReliabilityPct(),
                link.isBidirectional(), link.getStatus().name());
    }

    public SimulationRunEntity toEntity(SimulationResult result, String topologyId, String sourceRouterId,
                                         String destRouterId, String algorithm, Instant startedAt) {
        return new SimulationRunEntity(result.runId(), topologyId, sourceRouterId, destRouterId,
                algorithm, startedAt, Instant.now(), result.status());
    }
}
