package com.netsim.persistence.repository;

import com.netsim.persistence.entity.SimulationMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimulationMetricJpaRepository extends JpaRepository<SimulationMetricEntity, String> {
}
