-- Phase 4: topology_snapshot is deliberately NOT a JPA @Entity.
-- It's written and read entirely through JdbcTemplate (see
-- TopologySnapshotJdbcRepository) as an explicit example of raw SQL
-- alongside the JPA-managed tables from Phase 2/3, and it's versioned
-- here with Flyway instead of relying on Hibernate's ddl-auto.
--
-- Portable across H2 (default), PostgreSQL, and MySQL: no vendor-
-- specific auto-increment syntax — ids are UUID strings generated in
-- application code, and DOUBLE PRECISION / TIMESTAMP / VARCHAR / INT
-- are standard across all three.

CREATE TABLE topology_snapshot (
    id                   VARCHAR(64) PRIMARY KEY,
    topology_id          VARCHAR(64) NOT NULL,
    router_count         INT NOT NULL,
    link_count           INT NOT NULL,
    avg_link_latency_ms  DOUBLE PRECISION NOT NULL,
    captured_at          TIMESTAMP NOT NULL
);

CREATE INDEX idx_topology_snapshot_topology_id ON topology_snapshot(topology_id);
