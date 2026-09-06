package com.netsim.persistence.repository;

import com.netsim.persistence.entity.TopologyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopologyJpaRepository extends JpaRepository<TopologyEntity, String> {
}
