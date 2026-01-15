package com.hth.udecareer.eil.repository;

import com.hth.udecareer.eil.entities.EilReadinessSnapshotEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EilReadinessSnapshotRepository extends JpaRepository<EilReadinessSnapshotEntity, Long> {

    Optional<EilReadinessSnapshotEntity> findFirstByUserIdOrderBySnapshotDateDesc(Long userId);

    Optional<EilReadinessSnapshotEntity> findFirstByUserIdAndTestTypeOrderBySnapshotDateDesc(Long userId, String testType);

    Page<EilReadinessSnapshotEntity> findByUserIdOrderBySnapshotDateDesc(Long userId, Pageable pageable);

    Page<EilReadinessSnapshotEntity> findByUserIdAndTestTypeOrderBySnapshotDateDesc(Long userId, String testType, Pageable pageable);
}

