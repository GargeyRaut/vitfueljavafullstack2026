package com.netsim.domain;

/**
 * A directed or bidirectional connection between two routers (FR-2).
 * Holds bandwidth, latency and reliability, used by {@code LinkSimulator}
 * to model per-hop physics, and {@code weight()} for routing algorithms.
 */
public class Link {

    private final String id;
    private final String sourceRouterId;
    private final String targetRouterId;
    private final double bandwidthMbps;
    private final double latencyMs;
    private final double reliabilityPct; // 0.0 - 100.0, chance a packet survives the hop
    private final boolean bidirectional;
    private volatile LinkStatus status;

    public Link(String id, String sourceRouterId, String targetRouterId,
                double bandwidthMbps, double latencyMs, double reliabilityPct,
                boolean bidirectional) {
        this.id = id;
        this.sourceRouterId = sourceRouterId;
        this.targetRouterId = targetRouterId;
        this.bandwidthMbps = bandwidthMbps;
        this.latencyMs = latencyMs;
        this.reliabilityPct = reliabilityPct;
        this.bidirectional = bidirectional;
        this.status = LinkStatus.UP;
    }

    /** Edge weight consumed by routing algorithms; latency is used as the metric. */
    public double weight() {
        return latencyMs;
    }

    public boolean connects(String routerId) {
        return sourceRouterId.equals(routerId) || (bidirectional && targetRouterId.equals(routerId));
    }

    /** Returns the id of the router on the other end of this link from {@code fromRouterId}. */
    public String otherEnd(String fromRouterId) {
        if (sourceRouterId.equals(fromRouterId)) return targetRouterId;
        if (bidirectional && targetRouterId.equals(fromRouterId)) return sourceRouterId;
        throw new IllegalArgumentException("Router " + fromRouterId + " is not an endpoint of link " + id);
    }

    public boolean isUp() {
        return status == LinkStatus.UP;
    }

    public void setStatus(LinkStatus status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getSourceRouterId() {
        return sourceRouterId;
    }

    public String getTargetRouterId() {
        return targetRouterId;
    }

    public double getBandwidthMbps() {
        return bandwidthMbps;
    }

    public double getLatencyMs() {
        return latencyMs;
    }

    public double getReliabilityPct() {
        return reliabilityPct;
    }

    public boolean isBidirectional() {
        return bidirectional;
    }

    public LinkStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Link{" + sourceRouterId + "->" + targetRouterId + ", latency=" + latencyMs + "ms, status=" + status + '}';
    }
}
