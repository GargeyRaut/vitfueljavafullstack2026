package com.netsim.domain.algorithm;

import com.netsim.domain.Link;
import com.netsim.domain.NetworkTopology;
import com.netsim.domain.Router;
import com.netsim.domain.RoutingTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Small 4-router diamond topology:
 *
 *   R1 --(5ms)--> R2 --(2ms)--> R4
 *   R1 --(1ms)--> R3 --(1ms)--> R4
 *
 * The R1-R3-R4 path (cost 2) should always win over R1-R2-R4 (cost 7).
 */
class RoutingAlgorithmTest {

    private NetworkTopology topology;
    private Router r1;
    private Router r4;

    @BeforeEach
    void setUp() {
        topology = new NetworkTopology("topo-test", "diamond");
        r1 = topology.addRouter(new Router("R1", "R1"));
        Router r2 = topology.addRouter(new Router("R2", "R2"));
        Router r3 = topology.addRouter(new Router("R3", "R3"));
        r4 = topology.addRouter(new Router("R4", "R4"));

        topology.addLink(new Link("L1", "R1", "R2", 100, 5, 100, true));
        topology.addLink(new Link("L2", "R2", "R4", 100, 2, 100, true));
        topology.addLink(new Link("L3", "R1", "R3", 100, 1, 100, true));
        topology.addLink(new Link("L4", "R3", "R4", 100, 1, 100, true));
    }

    @Test
    void dijkstraFindsCheaperPathViaR3() {
        RoutingTable table = new DijkstraRoutingAlgorithm().computeRoutes(topology, r1);
        assertEquals("R3", table.nextHopFor("R4"));
        assertEquals(2.0, table.distanceTo("R4"), 0.001);
        assertEquals(java.util.List.of("R1", "R3", "R4"), table.resolvePath("R4"));
    }

    @Test
    void bellmanFordAgreesWithDijkstra() {
        RoutingTable table = new BellmanFordRoutingAlgorithm().computeRoutes(topology, r1);
        assertEquals("R3", table.nextHopFor("R4"));
        assertEquals(2.0, table.distanceTo("R4"), 0.001);
    }

    @Test
    void distanceVectorConvergesToSameCost() {
        RoutingTable table = new DistanceVectorRoutingAlgorithm().computeRoutes(topology, r1);
        assertEquals(2.0, table.distanceTo("R4"), 0.001);
    }

    @Test
    void downedLinkForcesReroute() {
        topology.setLinkStatus("L3", com.netsim.domain.LinkStatus.DOWN);
        RoutingTable table = new DijkstraRoutingAlgorithm().computeRoutes(topology, r1);
        assertEquals("R2", table.nextHopFor("R4"));
        assertTrue(table.distanceTo("R4") > 2.0);
    }
}
