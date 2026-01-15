package com.hth.udecareer.eil.repository;

import com.hth.udecareer.eil.entities.EilTimeEstimateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EilTimeEstimateRepository extends JpaRepository<EilTimeEstimateEntity, Long> {

    List<EilTimeEstimateEntity> findByUserId(Long userId);

    List<EilTimeEstimateEntity> findByUserIdAndStatus(Long userId, String status);

    Optional<EilTimeEstimateEntity> findByUserIdAndCertificationCode(Long userId, String certificationCode);

    List<EilTimeEstimateEntity> findByUserIdOrderByUpdatedAtDesc(Long userId);

    @Query("SELECT e FROM EilTimeEstimateEntity e WHERE e.userId = :userId " +
           "AND e.status = 'ACTIVE' ORDER BY e.lastActivityAt DESC")
    List<EilTimeEstimateEntity> findActiveEstimates(@Param("userId") Long userId);

    @Query("SELECT e FROM EilTimeEstimateEntity e WHERE e.userId = :userId " +
           "AND e.status = 'COMPLETED' ORDER BY e.updatedAt DESC")
    List<EilTimeEstimateEntity> findCompletedEstimates(@Param("userId") Long userId);

    @Query("SELECT COUNT(e) FROM EilTimeEstimateEntity e " +
           "WHERE e.userId = :userId AND e.status = 'COMPLETED'")
    Long countCompletedByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(e.totalStudyHours) FROM EilTimeEstimateEntity e WHERE e.userId = :userId")
    Double getTotalStudyHoursByUserId(@Param("userId") Long userId);

    void deleteByUserId(Long userId);

    void deleteByUserIdAndCertificationCode(Long userId, String certificationCode);
}
