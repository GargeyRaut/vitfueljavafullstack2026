package com.netsim.engine;

public record SimulationRequest(
        String topologyId,
        String sourceRouterId,
        String destRouterId,
        String algorithm,
        int packetSizeBytes,
        int ttl
) {
    public static SimulationRequest of(String topologyId, String sourceRouterId, String destRouterId, String algorithm) {
        return new SimulationRequest(topologyId, sourceRouterId, destRouterId, algorithm, 512, 64);
    }
}
