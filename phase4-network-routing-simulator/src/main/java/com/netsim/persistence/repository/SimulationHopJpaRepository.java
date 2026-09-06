package com.netsim.persistence.repository;

import com.netsim.persistence.entity.SimulationHopEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SimulationHopJpaRepository extends JpaRepository<SimulationHopEntity, Long> {
    List<SimulationHopEntity> findByRunIdOrderBySequenceNoAsc(String runId);
}
