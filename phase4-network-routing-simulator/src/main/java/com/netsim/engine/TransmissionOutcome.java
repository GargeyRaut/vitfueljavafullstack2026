package com.netsim.engine;

/** Result of simulating a single packet traversal across one {@code Link}. */
public record TransmissionOutcome(boolean dropped, double latencyMs, String reason) {

    public static TransmissionOutcome delivered(double latencyMs) {
        return new TransmissionOutcome(false, latencyMs, null);
    }

    public static TransmissionOutcome dropped(String reason) {
        return new TransmissionOutcome(true, 0.0, reason);
    }

    public boolean isDropped() {
        return dropped;
    }
}
