package com.netsim.persistence.repository;

import com.netsim.persistence.entity.LinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LinkJpaRepository extends JpaRepository<LinkEntity, String> {
    List<LinkEntity> findByTopologyId(String topologyId);
}
