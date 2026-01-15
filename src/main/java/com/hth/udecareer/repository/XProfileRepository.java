package com.hth.udecareer.repository;

import com.hth.udecareer.entities.XProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface XProfileRepository extends JpaRepository<XProfileEntity, Long> {

    Optional<XProfileEntity> findByUserId(Long userId);

    Optional<XProfileEntity> findByUsername(String username);
}

