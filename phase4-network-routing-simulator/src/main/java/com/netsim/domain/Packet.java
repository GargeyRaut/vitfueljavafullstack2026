package com.netsim.domain;

import java.util.UUID;

/**
 * A unit of data traveling across the topology (FR-5). Modeled as a
 * mutable value object so the simulation engine can update its status
 * and TTL as it advances hop-by-hop.
 */
public class Packet {

    private final String id;
    private final String sourceRouterId;
    private final String destinationRouterId;
    private final int sizeBytes;
    private final long sequenceId;
    private int ttl;
    private PacketStatus status;

    public Packet(String sourceRouterId, String destinationRouterId, int sizeBytes, int ttl, long sequenceId) {
        this.id = UUID.randomUUID().toString();
        this.sourceRouterId = sourceRouterId;
        this.destinationRouterId = destinationRouterId;
        this.sizeBytes = sizeBytes;
        this.ttl = ttl;
        this.sequenceId = sequenceId;
        this.status = PacketStatus.CREATED;
    }

    public boolean decrementTtl() {
        ttl--;
        return ttl > 0;
    }

    public String getId() {
        return id;
    }

    public String getSourceRouterId() {
        return sourceRouterId;
    }

    public String getDestinationRouterId() {
        return destinationRouterId;
    }

    public int getSizeBytes() {
        return sizeBytes;
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public int getTtl() {
        return ttl;
    }

    public PacketStatus getStatus() {
        return status;
    }

    public void setStatus(PacketStatus status) {
        this.status = status;
    }
}
