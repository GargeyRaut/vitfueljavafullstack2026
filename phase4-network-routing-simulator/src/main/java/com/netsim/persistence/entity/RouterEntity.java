package com.netsim.persistence.entity;

import jakarta.persistence.*;

/** Persisted router belonging to exactly one topology (section 7.1). */
@Entity
@Table(name = "router")
public class RouterEntity {

    @Id
    private String id;

    @Column(name = "topology_id", nullable = false)
    private String topologyId;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String status;

    protected RouterEntity() {
        // JPA
    }

    public RouterEntity(String id, String topologyId, String label, String status) {
        this.id = id;
        this.topologyId = topologyId;
        this.label = label;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getTopologyId() {
        return topologyId;
    }

    public String getLabel() {
        return label;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
