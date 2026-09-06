package com.netsim.persistence.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Phase 4: deliberately raw SQL (via {@link JdbcTemplate}) rather than
 * Spring Data JPA, as an explicit example of hand-written queries
 * alongside the ORM-managed repositories elsewhere in {@code persistence.repository}.
 * Backs a snapshot table that is versioned with Flyway
 * ({@code db/migration/V1__topology_snapshot.sql}) instead of
 * Hibernate's ddl-auto, and isn't mapped by any {@code @Entity}.
 */
@Repository
public class TopologySnapshotJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public TopologySnapshotJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String insert(String topologyId, int routerCount, int linkCount, double avgLinkLatencyMs) {
        String id = "snap-" + UUID.randomUUID().toString().substring(0, 8);
        jdbcTemplate.update(
                "INSERT INTO topology_snapshot (id, topology_id, router_count, link_count, avg_link_latency_ms, captured_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                id, topologyId, routerCount, linkCount, avgLinkLatencyMs, Timestamp.from(Instant.now()));
        return id;
    }

    public List<SnapshotRow> findByTopologyId(String topologyId) {
        return jdbcTemplate.query(
                "SELECT id, topology_id, router_count, link_count, avg_link_latency_ms, captured_at "
                        + "FROM topology_snapshot WHERE topology_id = ? ORDER BY captured_at DESC",
                (rs, rowNum) -> new SnapshotRow(
                        rs.getString("id"),
                        rs.getString("topology_id"),
                        rs.getInt("router_count"),
                        rs.getInt("link_count"),
                        rs.getDouble("avg_link_latency_ms"),
                        rs.getTimestamp("captured_at").toInstant()),
                topologyId);
    }

    /** Plain row projection, mapped straight from the ResultSet — no entity involved. */
    public record SnapshotRow(String id, String topologyId, int routerCount, int linkCount,
                               double avgLinkLatencyMs, Instant capturedAt) {}
}
