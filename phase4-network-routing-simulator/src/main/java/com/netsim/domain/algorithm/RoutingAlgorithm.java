package com.netsim.domain.algorithm;

import com.netsim.domain.NetworkTopology;
import com.netsim.domain.Router;
import com.netsim.domain.RoutingTable;

/**
 * Strategy interface for pluggable routing algorithms (NFR-3).
 * Implementations are registered as named Spring beans and selected
 * at run time by name (see {@link RoutingAlgorithmRegistry}).
 */
public interface RoutingAlgorithm {

    RoutingTable computeRoutes(NetworkTopology topology, Router source);

    String algorithmName();
}
