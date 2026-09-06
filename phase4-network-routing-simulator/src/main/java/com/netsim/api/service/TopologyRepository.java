package com.netsim.api.service;

import com.netsim.domain.Link;
import com.netsim.domain.NetworkTopology;
import com.netsim.domain.Router;
import com.netsim.persistence.mapper.DomainEntityMapper;
import com.netsim.persistence.repository.LinkJpaRepository;
import com.netsim.persistence.repository.RouterJpaRepository;
import com.netsim.persistence.repository.TopologyJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The live network graph used during an active simulation is kept in
 * memory (design doc section 7: "only checkpointed/persisted at run
 * boundaries"). Phase 2 adds that checkpoint: every mutation is also
 * written through to the {@code topology}/{@code router}/{@code link}
 * tables via Spring Data JPA, so topology definitions survive restarts
 * and can back an audit trail, while reads of the active graph stay
 * fast and framework-free.
 */
@Repository
public class TopologyRepository {

    private final Map<String, NetworkTopology> topologies = new ConcurrentHashMap<>();

    private final TopologyJpaRepository topologyJpaRepository;
    private final RouterJpaRepository routerJpaRepository;
    private final LinkJpaRepository linkJpaRepository;
    private final DomainEntityMapper mapper;

    public TopologyRepository(TopologyJpaRepository topologyJpaRepository,
                               RouterJpaRepository routerJpaRepository,
                               LinkJpaRepository linkJpaRepository,
                               DomainEntityMapper mapper) {
        this.topologyJpaRepository = topologyJpaRepository;
        this.routerJpaRepository = routerJpaRepository;
        this.linkJpaRepository = linkJpaRepository;
        this.mapper = mapper;
    }

    @Transactional
    public NetworkTopology create(String name) {
        String id = "topo-" + UUID.randomUUID().toString().substring(0, 6);
        NetworkTopology topology = new NetworkTopology(id, name);
        topologies.put(id, topology);
        topologyJpaRepository.save(mapper.toEntity(topology));
        return topology;
    }

    public NetworkTopology get(String id) {
        NetworkTopology topology = topologies.get(id);
        if (topology == null) {
            throw new NoSuchElementException("Unknown topology: " + id);
        }
        return topology;
    }

    public Iterable<NetworkTopology> all() {
        return topologies.values();
    }

    /** Adds a router to the live graph and checkpoints it to the database. */
    @Transactional
    public Router addRouter(NetworkTopology topology, Router router) {
        topology.addRouter(router);
        routerJpaRepository.save(mapper.toEntity(router, topology.getId()));
        return router;
    }

    /** Adds a link to the live graph and checkpoints it to the database. */
    @Transactional
    public Link addLink(NetworkTopology topology, Link link) {
        topology.addLink(link);
        linkJpaRepository.save(mapper.toEntity(link, topology.getId()));
        return link;
    }

    /** Removes a router (and its incident links) from the live graph and the database. */
    @Transactional
    public void removeRouter(NetworkTopology topology, String routerId) {
        topology.removeRouter(routerId);
        linkJpaRepository.findByTopologyId(topology.getId()).stream()
                .filter(l -> l.getSourceRouterId().equals(routerId) || l.getTargetRouterId().equals(routerId))
                .forEach(l -> linkJpaRepository.deleteById(l.getId()));
        routerJpaRepository.deleteById(routerId);
    }

    /** Removes a link from the live graph and the database. */
    @Transactional
    public void removeLink(NetworkTopology topology, String linkId) {
        topology.removeLink(linkId);
        linkJpaRepository.deleteById(linkId);
    }

    /** Removes a topology and everything in it (routers, links) from memory and the database. */
    @Transactional
    public void deleteTopology(String topologyId) {
        topologies.remove(topologyId);
        linkJpaRepository.findByTopologyId(topologyId).forEach(l -> linkJpaRepository.deleteById(l.getId()));
        routerJpaRepository.findByTopologyId(topologyId).forEach(r -> routerJpaRepository.deleteById(r.getId()));
        topologyJpaRepository.deleteById(topologyId);
    }

    /** Re-checkpoints a router's mutable status (e.g. after fault injection). */
    @Transactional
    public void checkpointRouterStatus(Router router) {
        routerJpaRepository.findById(router.getId()).ifPresent(entity -> {
            entity.setStatus(router.getStatus().name());
            routerJpaRepository.save(entity);
        });
    }

    /** Re-checkpoints a link's mutable status (e.g. after fault injection). */
    @Transactional
    public void checkpointLinkStatus(Link link) {
        linkJpaRepository.findById(link.getId()).ifPresent(entity -> {
            entity.setStatus(link.getStatus().name());
            linkJpaRepository.save(entity);
        });
    }

    /**
     * Re-hydrates the in-memory graph for a topology from its DB checkpoint.
     * Used on startup (see {@code TopologyRehydrationRunner}) so restarting
     * the app doesn't lose previously created topologies, only their
     * in-flight (never-checkpointed) state.
     */
    @Transactional(readOnly = true)
    public void rehydrate(String topologyId, String name) {
        NetworkTopology topology = new NetworkTopology(topologyId, name);
        for (var routerEntity : routerJpaRepository.findByTopologyId(topologyId)) {
            Router router = new Router(routerEntity.getId(), routerEntity.getLabel());
            router.setStatus(com.netsim.domain.RouterStatus.valueOf(routerEntity.getStatus()));
            topology.addRouter(router);
        }
        for (var linkEntity : linkJpaRepository.findByTopologyId(topologyId)) {
            Link link = new Link(linkEntity.getId(), linkEntity.getSourceRouterId(), linkEntity.getTargetRouterId(),
                    linkEntity.getBandwidthMbps(), linkEntity.getLatencyMs(), linkEntity.getReliabilityPct(),
                    linkEntity.isBidirectional());
            if (!"UP".equals(linkEntity.getStatus())) {
                link.setStatus(com.netsim.domain.LinkStatus.valueOf(linkEntity.getStatus()));
            }
            topology.addLink(link);
        }
        topologies.put(topologyId, topology);
    }
}
