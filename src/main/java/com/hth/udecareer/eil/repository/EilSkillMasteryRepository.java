package com.hth.udecareer.eil.repository;

import com.hth.udecareer.eil.entities.EilSkillMasteryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface EilSkillMasteryRepository extends JpaRepository<EilSkillMasteryEntity, Long> {

    List<EilSkillMasteryEntity> findByUserId(Long userId);

    Optional<EilSkillMasteryEntity> findByUserIdAndSkillId(Long userId, Long skillId);

    List<EilSkillMasteryEntity> findByUserIdAndSkillIdIn(Long userId, List<Long> skillIds);

    @Query("SELECT m FROM EilSkillMasteryEntity m WHERE m.userId = :userId ORDER BY m.masteryLevel ASC")
    List<EilSkillMasteryEntity> findByUserIdOrderByMasteryLevelAsc(@Param("userId") Long userId);

    @Query("SELECT m FROM EilSkillMasteryEntity m WHERE m.userId = :userId ORDER BY m.masteryLevel DESC")
    List<EilSkillMasteryEntity> findByUserIdOrderByMasteryLevelDesc(@Param("userId") Long userId);

    @Query("SELECT m FROM EilSkillMasteryEntity m WHERE m.userId = :userId AND m.masteryLevel < :threshold ORDER BY m.masteryLevel ASC")
    List<EilSkillMasteryEntity> findWeakSkills(@Param("userId") Long userId, @Param("threshold") BigDecimal threshold);

    @Query("SELECT m FROM EilSkillMasteryEntity m WHERE m.userId = :userId AND m.masteryLevel >= :threshold ORDER BY m.masteryLevel DESC")
    List<EilSkillMasteryEntity> findStrongSkills(@Param("userId") Long userId, @Param("threshold") BigDecimal threshold);

    @Query("SELECT m FROM EilSkillMasteryEntity m " +
           "JOIN EilSkillEntity s ON m.skillId = s.id " +
           "WHERE m.userId = :userId AND s.category = :category")
    List<EilSkillMasteryEntity> findByUserIdAndSkillCategory(@Param("userId") Long userId, @Param("category") String category);

    @Query("SELECT AVG(m.masteryLevel) FROM EilSkillMasteryEntity m WHERE m.userId = :userId")
    BigDecimal getAverageMasteryByUserId(@Param("userId") Long userId);

    @Query("SELECT AVG(m.masteryLevel) FROM EilSkillMasteryEntity m " +
           "JOIN EilSkillEntity s ON m.skillId = s.id " +
           "WHERE m.userId = :userId AND s.category = :category")
    BigDecimal getAverageMasteryByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category);

    @Query("SELECT SUM(m.attempts) FROM EilSkillMasteryEntity m WHERE m.userId = :userId")
    Long getTotalAttemptsByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(m.correctCount) FROM EilSkillMasteryEntity m WHERE m.userId = :userId")
    Long getTotalCorrectByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE EilSkillMasteryEntity m SET m.masteryLevel = :level, m.attempts = m.attempts + 1, " +
           "m.correctCount = m.correctCount + :correctIncrement, m.streak = :streak, m.lastPracticedAt = CURRENT_TIMESTAMP " +
           "WHERE m.userId = :userId AND m.skillId = :skillId")
    int updateMastery(@Param("userId") Long userId,
                      @Param("skillId") Long skillId,
                      @Param("level") BigDecimal level,
                      @Param("correctIncrement") Integer correctIncrement,
                      @Param("streak") Integer streak);

    @Query("SELECT COUNT(m) FROM EilSkillMasteryEntity m WHERE m.userId = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(m) FROM EilSkillMasteryEntity m WHERE m.userId = :userId AND m.masteryLevel < :threshold")
    long countWeakSkills(@Param("userId") Long userId, @Param("threshold") BigDecimal threshold);

    void deleteByUserId(Long userId);
}
