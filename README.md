# Network Routing Simulator — Phase 4

Everything from Phases 1–3, plus more simulation features and more
explicit SQL database work.

## New in Phase 4

**Simulation:**
- **Batch/concurrent packet simulation** — `POST /api/simulations/run-batch`
  fires N packets (default 20, up to 500) between the same source/dest
  concurrently (virtual threads) and returns aggregate stats: delivered
  count, loss %, average/min/max latency, and jitter (latency std-dev).
  A single simulation only shows one random outcome of the per-link loss
  model — a batch shows the actual distribution.
- **Continuous background traffic generator** — `POST
  /api/topologies/{id}/traffic/start` kicks off a session that sends a
  packet between two random reachable routers every `intervalMs`,
  running in the background until you call `.../traffic/stop`. Poll
  `.../traffic/stats` (or watch the dashboard) for live totals: packets
  sent/delivered/dropped and average latency. One session per topology.

**SQL databases:**
- **Flyway migrations** — `db/migration/V1__topology_snapshot.sql`
  versions a new table with real SQL, instead of relying only on
  Hibernate's auto-DDL. It's portable across H2/PostgreSQL/MySQL (no
  vendor-specific auto-increment syntax).
- **Raw JDBC alongside JPA** — the new `topology_snapshot` table is
  deliberately *not* a JPA entity. It's read and written entirely
  through `JdbcTemplate` (`TopologySnapshotJdbcRepository`), as an
  explicit example of hand-written SQL next to the ORM-managed tables
  from Phase 2/3.
- **Topology snapshots** — `POST /api/topologies/{id}/snapshots`
  captures a point-in-time row (router count, link count, average link
  latency); `GET /api/topologies/{id}/snapshots` lists the history.
- **MySQL support** — a third supported database alongside H2 (default)
  and PostgreSQL. `docker-compose-mysql.yml` spins up the app against
  a MySQL 8.4 container.

**Dashboard:** updated with a "Batch simulate" button, a live
background-traffic panel (start/stop + polling stats), and a
snapshots panel.

---

## 1. Prerequisites

Same as Phase 3 — just a **Java 21 JDK**. Maven is not required; the
checked-in wrapper (`./mvnw`) handles it.

```
java -version
```

| OS | Install command |
|---|---|
| Arch Linux | `sudo pacman -S jdk21-openjdk` |
| Ubuntu/Debian | `sudo apt install openjdk-21-jdk` |
| Fedora | `sudo dnf install java-21-openjdk` |
| macOS (Homebrew) | `brew install openjdk@21` |
| Windows | Install [Eclipse Temurin 21](https://adoptium.net/) |

---

## 2. Run it

```
cd phase4-network-routing-simulator
./mvnw spring-boot:run
```
Windows: `mvnw.cmd spring-boot:run`

Same as Phase 3: first run downloads Maven + dependencies (needs
internet), the database is a file under `./data/` that survives
restarts, and — if the database is empty — a demo topology is seeded
automatically with example `curl` commands printed to the console.

Open the dashboard:
```
http://localhost:8080/
```

---

## 3. Try the new features

**Batch simulation** (replace the ids with your own — the demo-seed
console output gives you real ones):
```bash
curl -X POST http://localhost:8080/api/simulations/run-batch \
  -H 'Content-Type: application/json' \
  -d '{"topologyId":"topo-...","sourceRouterId":"R-...","destRouterId":"R-...","algorithm":"dijkstra","packetCount":50}'
```

**Background traffic:**
```bash
curl -X POST http://localhost:8080/api/topologies/topo-.../traffic/start \
  -H 'Content-Type: application/json' -d '{"algorithm":"dijkstra","intervalMs":500}'

curl http://localhost:8080/api/topologies/topo-.../traffic/stats

curl -X POST http://localhost:8080/api/topologies/topo-.../traffic/stop
```

**Snapshots:**
```bash
curl -X POST http://localhost:8080/api/topologies/topo-.../snapshots
curl http://localhost:8080/api/topologies/topo-.../snapshots
```

Or just use the dashboard — all three have a panel with buttons.

---

## 4. Common commands

```bash
./mvnw spring-boot:run                 # run the app
./mvnw test                            # run the test suite
./mvnw package                         # build target/network-routing-simulator-4.0.0.jar
java -jar target/network-routing-simulator-4.0.0.jar   # run the jar directly

# Start empty (skip demo seed)
./mvnw spring-boot:run -Dspring-boot.run.arguments=--app.demo.seed-enabled=false

# Different port
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081

# Reset all data
rm -rf data/
./mvnw spring-boot:run
```

---

## 5. Run against PostgreSQL or MySQL instead of H2 (optional)

Requires Docker.

```bash
# PostgreSQL
docker compose up --build

# MySQL
docker compose -f docker-compose-mysql.yml up --build
```

Both wire the same app image to a real database container instead of
the local H2 file. Everything else — dashboard, API, WebSocket,
Flyway migration — works identically either way.

---

## 6. Troubleshooting

**`bash: mvn: command not found`**
Use `./mvnw`, not `mvn` — see section 2. The wrapper is checked in
specifically so you don't need Maven installed.

**`./mvnw: Permission denied`**
```
chmod +x mvnw
./mvnw spring-boot:run
```

**Flyway migration error on startup**
Almost always means `./data/` has a database file from an older phase
that doesn't have Flyway's history table. Fix: `rm -rf data/` and
restart — Phase 4 will recreate everything cleanly.

**Traffic generator "not running" right after start**
Give the dashboard's poll a couple of seconds (it refreshes every
1.5s) or hit `.../traffic/stats` directly — the first tick fires
immediately but stats update after each simulated packet completes.

**Port 8080 already in use / dependency download fails / Arch mirror 404s**
Same as Phase 3 — see its troubleshooting section, unchanged here.

**Start over completely**
```
rm -rf data/ target/
./mvnw spring-boot:run
```

---

## 7. What's not implemented

Kafka event fan-out, MapStruct-generated mappers (hand-written
instead), authentication/multi-tenant workspaces, horizontal scaling,
and per-link queue/congestion modeling (batch simulation shows loss
distribution but links don't yet model finite queue capacity) —
still open "Future Enhancements" from the original design doc.
