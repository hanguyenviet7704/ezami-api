package com.hth.udecareer.repository;

import com.hth.udecareer.entities.UserStreakEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStreakRepository extends JpaRepository<UserStreakEntity, Long> {

    Optional<UserStreakEntity> findByUserId(Long userId);

    // Leaderboard queries
    @Query(value = "SELECT * FROM wp_fcom_user_streaks " +
            "ORDER BY longest_streak DESC, current_streak DESC " +
            "LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<UserStreakEntity> findTopLongestStreaks(@Param("limit") int limit, @Param("offset") int offset);

    @Query(value = "SELECT COUNT(*) FROM wp_fcom_user_streaks " +
            "WHERE longest_streak > :streak",
            nativeQuery = true)
    Long countUsersWithLongerStreak(@Param("streak") int streak);

    // Get user's rank by longest streak
    @Query(value = "SELECT COUNT(DISTINCT user_id) + 1 FROM wp_fcom_user_streaks " +
            "WHERE longest_streak > (SELECT longest_streak FROM wp_fcom_user_streaks WHERE user_id = :userId)",
            nativeQuery = true)
    Long getUserRankByLongestStreak(@Param("userId") Long userId);
}
