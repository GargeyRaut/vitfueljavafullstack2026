package com.netsim.persistence.entity;

import jakarta.persistence.*;

/** Aggregated metrics for a completed run (section 7.1). */
@Entity
@Table(name = "simulation_metric")
public class SimulationMetricEntity {

    @Id
    @Column(name = "run_id")
    private String runId;

    @Column(name = "total_latency_ms", nullable = false)
    private double totalLatencyMs;

    @Column(name = "hop_count", nullable = false)
    private int hopCount;

    @Column(name = "packet_loss_pct", nullable = false)
    private double packetLossPct;

    @Column(name = "throughput_kbps")
    private double throughputKbps;

    protected SimulationMetricEntity() {
        // JPA
    }

    public SimulationMetricEntity(String runId, double totalLatencyMs, int hopCount,
                                   double packetLossPct, double throughputKbps) {
        this.runId = runId;
        this.totalLatencyMs = totalLatencyMs;
        this.hopCount = hopCount;
        this.packetLossPct = packetLossPct;
        this.throughputKbps = throughputKbps;
    }

    public String getRunId() {
        return runId;
    }

    public double getTotalLatencyMs() {
        return totalLatencyMs;
    }

    public int getHopCount() {
        return hopCount;
    }

    public double getPacketLossPct() {
        return packetLossPct;
    }

    public double getThroughputKbps() {
        return throughputKbps;
    }
}
