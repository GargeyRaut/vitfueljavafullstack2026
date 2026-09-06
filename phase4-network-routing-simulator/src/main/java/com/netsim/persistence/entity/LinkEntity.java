package com.netsim.persistence.entity;

import jakarta.persistence.*;

/** Persisted directed/bidirectional edge between two routers (section 7.1). */
@Entity
@Table(name = "link")
public class LinkEntity {

    @Id
    private String id;

    @Column(name = "topology_id", nullable = false)
    private String topologyId;

    @Column(name = "source_router_id", nullable = false)
    private String sourceRouterId;

    @Column(name = "target_router_id", nullable = false)
    private String targetRouterId;

    @Column(name = "bandwidth_mbps", nullable = false)
    private double bandwidthMbps;

    @Column(name = "latency_ms", nullable = false)
    private double latencyMs;

    @Column(name = "reliability_pct", nullable = false)
    private double reliabilityPct;

    @Column(nullable = false)
    private boolean bidirectional;

    @Column(nullable = false)
    private String status;

    protected LinkEntity() {
        // JPA
    }

    public LinkEntity(String id, String topologyId, String sourceRouterId, String targetRouterId,
                       double bandwidthMbps, double latencyMs, double reliabilityPct,
                       boolean bidirectional, String status) {
        this.id = id;
        this.topologyId = topologyId;
        this.sourceRouterId = sourceRouterId;
        this.targetRouterId = targetRouterId;
        this.bandwidthMbps = bandwidthMbps;
        this.latencyMs = latencyMs;
        this.reliabilityPct = reliabilityPct;
        this.bidirectional = bidirectional;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getTopologyId() {
        return topologyId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
