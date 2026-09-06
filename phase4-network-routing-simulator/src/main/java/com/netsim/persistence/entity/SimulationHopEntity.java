package com.netsim.persistence.entity;

import jakarta.persistence.*;

/** Ordered hop-by-hop trace for a run, used for replay/analysis (section 7.1). */
@Entity
@Table(name = "simulation_hop")
public class SimulationHopEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false)
    private String runId;

    @Column(name = "sequence_no", nullable = false)
    private int sequenceNo;

    @Column(name = "router_id", nullable = false)
    private String routerId;

    @Column(name = "arrival_time_ms", nullable = false)
    private double arrivalTimeMs;

    @Column(nullable = false)
    private boolean dropped;

    protected SimulationHopEntity() {
        // JPA
    }

    public SimulationHopEntity(String runId, int sequenceNo, String routerId, double arrivalTimeMs, boolean dropped) {
        this.runId = runId;
        this.sequenceNo = sequenceNo;
        this.routerId = routerId;
        this.arrivalTimeMs = arrivalTimeMs;
        this.dropped = dropped;
    }

    public Long getId() {
        return id;
    }

    public String getRunId() {
        return runId;
    }

    public int getSequenceNo() {
        return sequenceNo;
    }

    public String getRouterId() {
        return routerId;
    }

    public double getArrivalTimeMs() {
        return arrivalTimeMs;
    }

    public boolean isDropped() {
        return dropped;
    }
}
