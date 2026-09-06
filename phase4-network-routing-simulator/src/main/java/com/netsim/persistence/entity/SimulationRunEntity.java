package com.netsim.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;

/** One row per simulation execution (section 7.1). */
@Entity
@Table(name = "simulation_run")
public class SimulationRunEntity {

    @Id
    private String id;

    @Column(name = "topology_id", nullable = false)
    private String topologyId;

    @Column(name = "source_router_id", nullable = false)
    private String sourceRouterId;

    @Column(name = "dest_router_id", nullable = false)
    private String destRouterId;

    @Column(nullable = false)
    private String algorithm;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(nullable = false)
    private String status;

    protected SimulationRunEntity() {
        // JPA
    }

    public SimulationRunEntity(String id, String topologyId, String sourceRouterId, String destRouterId,
                                String algorithm, Instant startedAt, Instant finishedAt, String status) {
        this.id = id;
        this.topologyId = topologyId;
        this.sourceRouterId = sourceRouterId;
        this.destRouterId = destRouterId;
        this.algorithm = algorithm;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
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

    public String getDestRouterId() {
        return destRouterId;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public String getStatus() {
        return status;
    }
}
