package com.hth.udecareer.repository;

import com.hth.udecareer.entities.StreakActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StreakActivityRepository extends JpaRepository<StreakActivityEntity, Long> {

    Optional<StreakActivityEntity> findByUserIdAndActivityDate(Long userId, LocalDate activityDate);

    List<StreakActivityEntity> findByUserIdAndActivityDateBetween(
            Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(a) FROM StreakActivityEntity a " +
            "WHERE a.userId = :userId " +
            "AND a.activityDate BETWEEN :startDate AND :endDate")
    Long countActiveDays(@Param("userId") Long userId,
                         @Param("startDate") LocalDate startDate,
                         @Param("endDate") LocalDate endDate);
}
