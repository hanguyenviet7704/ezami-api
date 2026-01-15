package com.hth.udecareer.eil.repository;

import com.hth.udecareer.eil.entities.MockResultEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MockResultRepository extends JpaRepository<MockResultEntity, Long> {

    /**
     * Find latest mock result for a user (optionally filtered by certificate code)
     */
    Optional<MockResultEntity> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<MockResultEntity> findFirstByUserIdAndCertificateCodeOrderByCreatedAtDesc(
            Long userId, String certificateCode);

    /**
     * Get mock result history for a user with pagination
     */
    Page<MockResultEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<MockResultEntity> findByUserIdAndCertificateCodeOrderByCreatedAtDesc(
            Long userId, String certificateCode, Pageable pageable);

    /**
     * Get statistics for a user's mock results
     */
    @Query("SELECT MAX(m.score) FROM MockResultEntity m WHERE m.userId = :userId AND m.certificateCode = :certificateCode")
    Double findMaxScoreByUserIdAndCertificateCode(@Param("userId") Long userId,
                                                    @Param("certificateCode") String certificateCode);

    @Query("SELECT AVG(m.score) FROM MockResultEntity m WHERE m.userId = :userId AND m.certificateCode = :certificateCode")
    Double findAvgScoreByUserIdAndCertificateCode(@Param("userId") Long userId,
                                                    @Param("certificateCode") String certificateCode);

    @Query("SELECT COUNT(m) FROM MockResultEntity m WHERE m.userId = :userId AND m.certificateCode = :certificateCode")
    Long countByUserIdAndCertificateCode(@Param("userId") Long userId,
                                          @Param("certificateCode") String certificateCode);
}
