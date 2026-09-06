package com.netsim.persistence.repository;

import com.netsim.persistence.entity.RouterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouterJpaRepository extends JpaRepository<RouterEntity, String> {
    List<RouterEntity> findByTopologyId(String topologyId);
}
