package com.netsim.domain.algorithm;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Resolves a {@link RoutingAlgorithm} by its {@code algorithmName()} so new
 * algorithms can be added just by implementing the interface and registering
 * as a Spring bean — no controller or engine changes required (NFR-3).
 */
@Component
public class RoutingAlgorithmRegistry {

    private final Map<String, RoutingAlgorithm> byName;

    public RoutingAlgorithmRegistry(List<RoutingAlgorithm> algorithms) {
        this.byName = algorithms.stream()
                .collect(Collectors.toMap(RoutingAlgorithm::algorithmName, a -> a));
    }

    public RoutingAlgorithm resolve(String name) {
        RoutingAlgorithm algorithm = byName.get(name);
        if (algorithm == null) {
            throw new NoSuchElementException("Unknown routing algorithm: " + name
                    + ". Available: " + byName.keySet());
        }
        return algorithm;
    }
}
