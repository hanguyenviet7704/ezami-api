package com.hth.udecareer.repository;

import com.hth.udecareer.entities.UserStreakGoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStreakGoalRepository extends JpaRepository<UserStreakGoalEntity, Long> {

    List<UserStreakGoalEntity> findByUserIdAndStatus(Long userId, String status);

    List<UserStreakGoalEntity> findByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<UserStreakGoalEntity> findByUserIdAndGoalIdAndStatus(Long userId, Long goalId, String status);

    boolean existsByUserIdAndGoalIdAndStatus(Long userId, Long goalId, String status);

    // Count claimed goals
    Long countByUserIdAndStatus(Long userId, String status);
}
