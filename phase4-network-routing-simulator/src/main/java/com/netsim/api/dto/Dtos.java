package com.netsim.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * Request/response DTOs for the REST API (design doc section 8).
 * Grouped in one file for brevity; split into individual files as the
 * API grows.
 */
public final class Dtos {

    private Dtos() {}

    // ----- Topology -----
    public record CreateTopologyRequest(@NotBlank String name) {}

    public record TopologyDTO(String id, String name, int routerCount, int linkCount) {}

    // ----- Router -----
    public record AddRouterRequest(@NotBlank String label) {}

    public record RouterDTO(String id, String label, String status, List<String> neighborIds) {}

    public record RouterStatusUpdateRequest(@NotBlank String status) {}

    // ----- Link -----
    public record AddLinkRequest(
            @NotBlank String sourceId,
            @NotBlank String targetId,
            @Positive double bandwidthMbps,
            @Positive double latencyMs,
            double reliabilityPct,
            boolean bidirectional
    ) {}

    public record LinkDTO(String id, String sourceId, String targetId, double bandwidthMbps,
                           double latencyMs, double reliabilityPct, String status) {}

    public record LinkStatusUpdateRequest(@NotBlank String status) {}

    // ----- Simulation -----
    public record SimulationRunRequestDTO(
            @NotBlank String topologyId,
            @NotBlank String sourceRouterId,
            @NotBlank String destRouterId,
            @NotBlank String algorithm
    ) {}

    public record SimulationResultDTO(
            String runId,
            List<String> path,
            double totalLatencyMs,
            int hopCount,
            double packetLossPct,
            String status
    ) {}

    // ----- Phase 2: run history -----
    public record SimulationRunSummaryDTO(
            String runId,
            String sourceRouterId,
            String destRouterId,
            String algorithm,
            String status,
            String startedAt
    ) {}

    // ----- Phase 3: bulk import/export -----
    public record RouterImport(@NotBlank String label) {}

    public record LinkImport(
            @NotBlank String sourceLabel,
            @NotBlank String targetLabel,
            @Positive double bandwidthMbps,
            @Positive double latencyMs,
            double reliabilityPct,
            boolean bidirectional
    ) {}

    public record TopologyImportRequest(
            @NotBlank String name,
            List<RouterImport> routers,
            List<LinkImport> links
    ) {}

    public record TopologyExportDTO(
            String id,
            String name,
            List<RouterDTO> routers,
            List<LinkDTO> links
    ) {}

    // ----- Phase 3: batch analysis -----
    public record PairResultDTO(String sourceRouterId, String destRouterId, String status,
                                 double totalLatencyMs, int hopCount) {}

    public record TopologyAnalysisDTO(
            String topologyId,
            String algorithm,
            int pairsEvaluated,
            int pairsReachable,
            double avgLatencyMsAmongReachable,
            List<PairResultDTO> pairs
    ) {}

    // ----- Phase 4: snapshots (raw JDBC, not JPA) -----
    public record SnapshotDTO(
            String id,
            String topologyId,
            int routerCount,
            int linkCount,
            double avgLinkLatencyMs,
            String capturedAt
    ) {}

    // ----- Phase 4: batch / concurrent packet simulation -----
    public record BatchSimulationRequestDTO(
            @NotBlank String topologyId,
            @NotBlank String sourceRouterId,
            @NotBlank String destRouterId,
            @NotBlank String algorithm,
            @Positive int packetCount
    ) {}

    public record BatchSimulationResultDTO(
            String topologyId,
            String algorithm,
            int packetCount,
            int deliveredCount,
            int droppedCount,
            double lossPct,
            double avgLatencyMs,
            double minLatencyMs,
            double maxLatencyMs,
            double jitterMs
    ) {}

    // ----- Phase 4: continuous background traffic generator -----
    public record TrafficStartRequestDTO(
            @NotBlank String algorithm,
            @Positive int intervalMs
    ) {}

    public record TrafficStatsDTO(
            String topologyId,
            boolean running,
            String algorithm,
            long packetsSent,
            long delivered,
            long dropped,
            double avgLatencyMs
    ) {}
}
