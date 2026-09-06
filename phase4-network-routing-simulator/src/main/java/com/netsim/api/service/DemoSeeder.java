package com.netsim.api.service;

import com.netsim.domain.Link;
import com.netsim.domain.NetworkTopology;
import com.netsim.domain.Router;
import com.netsim.engine.SimulationEngine;
import com.netsim.engine.SimulationRequest;
import com.netsim.engine.SimulationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Phase 3: on a clean startup (no topologies rehydrated from the DB),
 * seeds one small demo topology and runs a simulation on it, printing
 * the resulting ids and ready-to-paste curl commands to the console.
 * Lets you confirm the app works end-to-end without writing any
 * requests by hand first. Disable with app.demo.seed-enabled=false.
 */
@Component
@Order(20)
public class DemoSeeder implements CommandLineRunner {

    private final TopologyRepository topologyRepository;
    private final SimulationEngine simulationEngine;
    private final SimulationEventCollector eventCollector;

    @Value("${app.demo.seed-enabled:true}")
    private boolean seedEnabled;

    public DemoSeeder(TopologyRepository topologyRepository, SimulationEngine simulationEngine,
                       SimulationEventCollector eventCollector) {
        this.topologyRepository = topologyRepository;
        this.simulationEngine = simulationEngine;
        this.eventCollector = eventCollector;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled) return;
        if (topologyRepository.all().iterator().hasNext()) {
            return; // topologies already exist (rehydrated from DB) — don't clutter with a demo
        }

        NetworkTopology topology = topologyRepository.create("demo-network");
        Router r1 = topologyRepository.addRouter(topology, new Router(idFor("R"), "R1"));
        Router r2 = topologyRepository.addRouter(topology, new Router(idFor("R"), "R2"));
        Router r3 = topologyRepository.addRouter(topology, new Router(idFor("R"), "R3"));
        Router r4 = topologyRepository.addRouter(topology, new Router(idFor("R"), "R4"));

        topologyRepository.addLink(topology, new Link(idFor("L"), r1.getId(), r2.getId(), 100, 5, 100, true));
        topologyRepository.addLink(topology, new Link(idFor("L"), r2.getId(), r4.getId(), 100, 2, 100, true));
        topologyRepository.addLink(topology, new Link(idFor("L"), r1.getId(), r3.getId(), 100, 1, 100, true));
        topologyRepository.addLink(topology, new Link(idFor("L"), r3.getId(), r4.getId(), 100, 1, 100, true));

        SimulationResult result = simulationEngine.run(topology,
                SimulationRequest.of(topology.getId(), r1.getId(), r4.getId(), "dijkstra"));
        eventCollector.drain(result.runId()); // demo probe run — not persisted to history

        System.out.println();
        System.out.println("======================================================================");
        System.out.println(" Demo topology seeded: '" + topology.getName() + "' (id: " + topology.getId() + ")");
        System.out.println(" R1(" + r1.getId() + ") --5ms--> R2(" + r2.getId() + ") --2ms--> R4(" + r4.getId() + ")");
        System.out.println(" R1(" + r1.getId() + ") --1ms--> R3(" + r3.getId() + ") --1ms--> R4(" + r4.getId() + ")");
        System.out.println(" Demo simulation R1 -> R4 via dijkstra: " + result.status()
                + ", path=" + result.path() + ", latency=" + String.format("%.2f", result.totalLatencyMs()) + "ms");
        System.out.println("----------------------------------------------------------------------");
        System.out.println(" Open the dashboard:      http://localhost:8080/");
        System.out.println(" Or try the API directly:");
        System.out.println("   curl http://localhost:8080/api/topologies/" + topology.getId());
        System.out.println("   curl -X POST http://localhost:8080/api/simulations/run \\");
        System.out.println("     -H 'Content-Type: application/json' \\");
        System.out.println("     -d '{\"topologyId\":\"" + topology.getId() + "\",\"sourceRouterId\":\"" + r1.getId()
                + "\",\"destRouterId\":\"" + r4.getId() + "\",\"algorithm\":\"dijkstra\"}'");
        System.out.println(" API docs:                 http://localhost:8080/swagger-ui.html");
        System.out.println("======================================================================");
        System.out.println();
    }

    private String idFor(String prefix) {
        return prefix + "-" + java.util.UUID.randomUUID().toString().substring(0, 6);
    }
}
