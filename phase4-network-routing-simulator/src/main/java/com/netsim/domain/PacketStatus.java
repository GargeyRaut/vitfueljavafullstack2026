package com.netsim.domain;

/** Packet lifecycle states (design doc section 10.2). */
public enum PacketStatus {
    CREATED,
    QUEUED,
    IN_TRANSIT,
    DELIVERED,
    DROPPED
}
