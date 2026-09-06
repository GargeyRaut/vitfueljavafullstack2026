package com.netsim.api.service;

import com.netsim.persistence.entity.TopologyEntity;
import com.netsim.persistence.repository.TopologyJpaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * On startup, reloads every previously persisted topology (routers +
 * links) from the database back into the in-memory graph, so restarting
 * the app doesn't lose topologies you created earlier — only Phase 1/2
 * behavior of "in-memory only" for the live graph is preserved between
 * requests within a running process, not across restarts.
 */
@Component
@Order(10)
public class TopologyRehydrationRunner implements CommandLineRunner {

    private final TopologyJpaRepository topologyJpaRepository;
    private final TopologyRepository topologyRepository;

    public TopologyRehydrationRunner(TopologyJpaRepository topologyJpaRepository, TopologyRepository topologyRepository) {
        this.topologyJpaRepository = topologyJpaRepository;
        this.topologyRepository = topologyRepository;
    }

    @Override
    public void run(String... args) {
        for (TopologyEntity entity : topologyJpaRepository.findAll()) {
            topologyRepository.rehydrate(entity.getId(), entity.getName());
        }
        long count = topologyJpaRepository.count();
        if (count > 0) {
            System.out.println("[startup] Rehydrated " + count + " persisted topology/topologies from the database.");
        }
    }
}
