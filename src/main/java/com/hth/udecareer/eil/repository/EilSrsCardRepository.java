package com.hth.udecareer.eil.repository;

import com.hth.udecareer.eil.entities.EilSrsCardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EilSrsCardRepository extends JpaRepository<EilSrsCardEntity, Long> {

    List<EilSrsCardEntity> findByUserId(Long userId);

    Page<EilSrsCardEntity> findByUserId(Long userId, Pageable pageable);

    Optional<EilSrsCardEntity> findByUserIdAndQuestionId(Long userId, Long questionId);

    Optional<EilSrsCardEntity> findByUserIdAndClientId(Long userId, String clientId);

    List<EilSrsCardEntity> findByUserIdAndCertificationCode(Long userId, String certificationCode);

    Page<EilSrsCardEntity> findByUserIdAndCertificationCode(
            Long userId, String certificationCode, Pageable pageable);

    // Get cards due for review
    @Query("SELECT c FROM EilSrsCardEntity c WHERE c.userId = :userId " +
           "AND c.nextReviewAt <= :now AND c.status != 'SUSPENDED' " +
           "ORDER BY c.nextReviewAt ASC")
    List<EilSrsCardEntity> findDueCards(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);

    @Query("SELECT c FROM EilSrsCardEntity c WHERE c.userId = :userId " +
           "AND c.nextReviewAt <= :now AND c.status != 'SUSPENDED' " +
           "ORDER BY c.nextReviewAt ASC")
    Page<EilSrsCardEntity> findDueCards(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    // Count due cards
    @Query("SELECT COUNT(c) FROM EilSrsCardEntity c WHERE c.userId = :userId " +
           "AND c.nextReviewAt <= :now AND c.status != 'SUSPENDED'")
    Long countDueCards(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Get cards by status
    List<EilSrsCardEntity> findByUserIdAndStatus(Long userId, String status);

    Page<EilSrsCardEntity> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    Page<EilSrsCardEntity> findByUserIdAndStatusAndCertificationCode(
            Long userId, String status, String certificationCode, Pageable pageable);

    // Statistics
    @Query("SELECT COUNT(c) FROM EilSrsCardEntity c WHERE c.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT AVG(c.easeFactor) FROM EilSrsCardEntity c WHERE c.userId = :userId")
    Double getAverageEaseFactor(@Param("userId") Long userId);

    @Query("SELECT SUM(c.totalReviews) FROM EilSrsCardEntity c WHERE c.userId = :userId")
    Long getTotalReviewsByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(c.correctReviews) FROM EilSrsCardEntity c WHERE c.userId = :userId")
    Long getTotalCorrectReviewsByUserId(@Param("userId") Long userId);

    @Query("SELECT c.status, COUNT(c) FROM EilSrsCardEntity c " +
           "WHERE c.userId = :userId GROUP BY c.status")
    List<Object[]> getCardCountByStatus(@Param("userId") Long userId);

    // Sync support - get cards updated after a certain time
    @Query("SELECT c FROM EilSrsCardEntity c WHERE c.userId = :userId " +
           "AND c.updatedAt > :since ORDER BY c.updatedAt ASC")
    List<EilSrsCardEntity> findUpdatedSince(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since);

    // Bulk operations
    @Modifying
    @Query("UPDATE EilSrsCardEntity c SET c.status = :status WHERE c.userId = :userId")
    int updateStatusForUser(@Param("userId") Long userId, @Param("status") String status);

    void deleteByUserId(Long userId);

    void deleteByUserIdAndQuestionId(Long userId, Long questionId);
}
