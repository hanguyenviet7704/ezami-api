package com.hth.udecareer.repository;

import com.hth.udecareer.entities.StreakGoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StreakGoalRepository extends JpaRepository<StreakGoalEntity, Long> {

    List<StreakGoalEntity> findByIsActiveTrueOrderByPriorityAsc();

    List<StreakGoalEntity> findByGoalTypeAndIsActiveTrueOrderByPriorityAsc(String goalType);

    Optional<StreakGoalEntity> findByCode(String code);

    // Find valid goals (considering time-based validity)
    List<StreakGoalEntity> findByIsActiveTrueAndValidFromBeforeAndValidUntilAfter(
            LocalDateTime validFrom, LocalDateTime validUntil);
}
