package com.netsim.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;

/** Persisted, versioned network definition (design doc section 7.1). */
@Entity
@Table(name = "topology")
public class TopologyEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TopologyEntity() {
        // JPA
    }

    public TopologyEntity(String id, String name, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
