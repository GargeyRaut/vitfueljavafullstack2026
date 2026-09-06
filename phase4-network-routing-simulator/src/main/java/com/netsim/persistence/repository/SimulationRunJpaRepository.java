package com.netsim.persistence.repository;

import com.netsim.persistence.entity.SimulationRunEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimulationRunJpaRepository extends JpaRepository<SimulationRunEntity, String> {
    Page<SimulationRunEntity> findByTopologyIdOrderByStartedAtDesc(String topologyId, Pageable pageable);
}
