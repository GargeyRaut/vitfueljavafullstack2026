package com.netsim.engine;

import com.netsim.domain.Link;
import com.netsim.domain.Packet;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Applies latency, bandwidth throttling, jitter, and probabilistic loss
 * when a packet traverses a {@link Link} (domain service, per design 5.1).
 */
@Component
public class LinkSimulator {

    private static final double JITTER_RATIO = 0.10; // +/-10% of base latency

    public TransmissionOutcome transmit(Packet packet, Link link) {
        if (!link.isUp()) {
            return TransmissionOutcome.dropped("LINK_DOWN");
        }
        if (!packet.decrementTtl()) {
            return TransmissionOutcome.dropped("TTL_EXPIRED");
        }

        double lossRoll = ThreadLocalRandom.current().nextDouble(0, 100);
        if (lossRoll > link.getReliabilityPct()) {
            return TransmissionOutcome.dropped("LINK_LOSS");
        }

        double transmissionDelayMs = (packet.getSizeBytes() * 8.0 / 1_000_000.0) / Math.max(link.getBandwidthMbps(), 0.001) * 1000.0;
        double jitterMs = link.getLatencyMs() * JITTER_RATIO * (ThreadLocalRandom.current().nextDouble() * 2 - 1);
        double totalLatencyMs = Math.max(0, link.getLatencyMs() + transmissionDelayMs + jitterMs);

        return TransmissionOutcome.delivered(totalLatencyMs);
    }
}
