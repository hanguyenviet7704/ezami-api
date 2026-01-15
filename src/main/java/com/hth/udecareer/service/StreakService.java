package com.hth.udecareer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hth.udecareer.entities.*;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.*;
import com.hth.udecareer.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreakService {

    private final UserStreakRepository userStreakRepository;
    private final StreakActivityRepository streakActivityRepository;
    private final UserRepository userRepository;
    private final UserPointsService userPointsService;
    private final StreakBadgeIntegrationService streakBadgeIntegrationService;
    private final StreakGoalService streakGoalService;
    private final ObjectMapper objectMapper;

    /**
     * Update streak when user performs any activity.
     * This is the core method called by frontend or other services.
     */
    @Transactional
    public StreakUpdateResponse updateStreak(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND));

        UserStreakEntity streak = userStreakRepository.findByUserId(userId)
                .orElseGet(() -> initializeStreak(userId));

        LocalDate today = LocalDate.now();
        LocalDate lastActivity = streak.getLastActivityDate();

        // Check if already updated today
        if (lastActivity != null && lastActivity.equals(today)) {
            return buildStreakResponse(streak, false, false, null);
        }

        // Calculate days difference
        boolean streakBroken = false;
        boolean freezeAutoUsed = false;

        if (lastActivity == null) {
            // First activity ever
            streak.setCurrentStreak(1);
            streak.setStreakStartDate(today);
        } else {
            long daysDiff = ChronoUnit.DAYS.between(lastActivity, today);

            if (daysDiff == 1) {
                // Consecutive day - increment streak
                streak.setCurrentStreak(streak.getCurrentStreak() + 1);
            } else if (daysDiff == 2 && streak.getFreezeCount() > 0) {
                // Missed 1 day but have freeze - auto-use freeze
                streak.setCurrentStreak(streak.getCurrentStreak() + 1);
                streak.setFreezeCount(streak.getFreezeCount() - 1);
                streak.setFreezeUsedCount(streak.getFreezeUsedCount() + 1);
                freezeAutoUsed = true;

                // Log freeze usage for yesterday
                logFreezeUsage(userId, lastActivity.plusDays(1), streak.getCurrentStreak());
            } else {
                // Streak broken
                streakBroken = true;
                streak.setCurrentStreak(1);
                streak.setStreakStartDate(today);
            }
        }

        // Update longest streak
        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }

        // Update activity date and total days
        streak.setLastActivityDate(today);
        streak.setTotalDaysActive(streak.getTotalDaysActive() + 1);

        // Save streak
        userStreakRepository.save(streak);

        // Log today's activity
        logActivity(userId, today, streak.getCurrentStreak());

        // Check for milestone achievements
        List<String> milestoneRewards = checkAndAwardMilestones(userId, streak, user.getEmail());

        // Check and award streak badges
        streakBadgeIntegrationService.checkAndAwardStreakBadges(userId);

        // Check and update streak milestone goals
        streakGoalService.checkStreakMilestoneGoals(userId, streak.getCurrentStreak());

        log.info("Streak updated for user {}: current={}, longest={}, broken={}, freezeUsed={}",
                userId, streak.getCurrentStreak(), streak.getLongestStreak(), streakBroken, freezeAutoUsed);

        return buildStreakResponse(streak, streakBroken, freezeAutoUsed, milestoneRewards);
    }

    /**
     * Get current streak data for a user
     */
    @Transactional(readOnly = true)
    public CurrentStreakResponse getCurrentStreak(Long userId) {
        UserStreakEntity streak = userStreakRepository.findByUserId(userId)
                .orElseGet(() -> initializeStreak(userId));

        return buildCurrentStreakResponse(streak);
    }

    /**
     * Get streak statistics
     */
    @Transactional(readOnly = true)
    public StreakStatsResponse getStreakStats(Long userId) {
        UserStreakEntity streak = userStreakRepository.findByUserId(userId)
                .orElseGet(() -> initializeStreak(userId));

        Long rank = userStreakRepository.getUserRankByLongestStreak(userId);

        return StreakStatsResponse.builder()
                .currentStreak(streak.getCurrentStreak())
                .longestStreak(streak.getLongestStreak())
                .totalDaysActive(streak.getTotalDaysActive())
                .freezeCount(streak.getFreezeCount())
                .freezeUsedCount(streak.getFreezeUsedCount())
                .rank(rank != null ? rank.intValue() : 0)
                .streakStartDate(streak.getStreakStartDate())
                .lastActivityDate(streak.getLastActivityDate())
                .build();
    }

    /**
     * Use streak freeze manually (for future feature)
     */
    @Transactional
    public StreakFreezeResponse useStreakFreeze(Long userId) {
        UserStreakEntity streak = userStreakRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Streak not found"));

        if (streak.getFreezeCount() <= 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "No freeze available");
        }

        // Manual freeze logic (for future use - e.g., pre-freeze for tomorrow)
        // For now, freezes are auto-used when streak would break

        throw new AppException(ErrorCode.VALIDATION_ERROR, "Manual freeze not yet supported");
    }

    /**
     * Get streak leaderboard
     */
    @Transactional(readOnly = true)
    public StreakLeaderboardResponse getLeaderboard(int page, int size) {
        int offset = page * size;
        List<UserStreakEntity> topStreaks = userStreakRepository.findTopLongestStreaks(size, offset);

        List<StreakLeaderboardItemResponse> items = new ArrayList<>();
        int rank = offset + 1;

        for (UserStreakEntity streak : topStreaks) {
            User user = userRepository.findById(streak.getUserId()).orElse(null);
            if (user != null) {
                items.add(StreakLeaderboardItemResponse.builder()
                        .rank(rank++)
                        .userId(user.getId())
                        .username(user.getUsername())
                        .displayName(user.getDisplayName())
                        .longestStreak(streak.getLongestStreak())
                        .currentStreak(streak.getCurrentStreak())
                        .build());
            }
        }

        return StreakLeaderboardResponse.builder()
                .items(items)
                .page(page)
                .size(size)
                .build();
    }

    // ============= PRIVATE HELPER METHODS =============

    private UserStreakEntity initializeStreak(Long userId) {
        return UserStreakEntity.builder()
                .userId(userId)
                .currentStreak(0)
                .longestStreak(0)
                .freezeCount(0)
                .freezeUsedCount(0)
                .totalDaysActive(0)
                .totalGoalsCompleted(0)
                .totalMilestonePoints(0)
                .build();
    }

    private void logActivity(Long userId, LocalDate date, int streakDay) {
        StreakActivityEntity activity = StreakActivityEntity.builder()
                .userId(userId)
                .activityDate(date)
                .activityType("LOGIN")
                .streakDay(streakDay)
                .isFreezeUsed(false)
                .build();

        streakActivityRepository.save(activity);
    }

    private void logFreezeUsage(Long userId, LocalDate date, int streakDay) {
        StreakActivityEntity activity = StreakActivityEntity.builder()
                .userId(userId)
                .activityDate(date)
                .activityType("FREEZE_USED")
                .streakDay(streakDay)
                .isFreezeUsed(true)
                .build();

        streakActivityRepository.save(activity);
    }

    private List<String> checkAndAwardMilestones(Long userId, UserStreakEntity streak, String email) {
        List<String> rewards = new ArrayList<>();
        int currentStreak = streak.getCurrentStreak();

        // Check for freeze rewards (every 7 days)
        if (currentStreak % 7 == 0 && currentStreak > 0) {
            streak.setFreezeCount(streak.getFreezeCount() + 1);
            streak.setLastFreezeEarnedAt(LocalDateTime.now());
            rewards.add("Earned 1 streak freeze!");
        }

        // Check for point milestones
        Map<Integer, Integer> pointMilestones = Map.of(
                7, 100,
                14, 200,
                30, 500,
                50, 800,
                100, 2000
        );

        if (pointMilestones.containsKey(currentStreak)) {
            int points = pointMilestones.get(currentStreak);
            try {
                userPointsService.addPoints(email, points,
                        "POINT_STREAK_MILESTONE_" + currentStreak, null, null);
                streak.setTotalMilestonePoints(streak.getTotalMilestonePoints() + points);
                rewards.add("Earned " + points + " points for " + currentStreak + "-day streak!");
            } catch (Exception e) {
                log.error("Failed to award points for streak milestone: {}", e.getMessage());
            }
        }

        // Badge milestones will be checked by StreakBadgeIntegrationService later
        Map<Integer, String> badgeMilestones = Map.of(
                7, "streak-starter",
                30, "streak-master",
                100, "streak-legend"
        );

        if (badgeMilestones.containsKey(currentStreak)) {
            String badgeSlug = badgeMilestones.get(currentStreak);
            rewards.add("Unlocked badge: " + badgeSlug);
        }

        return rewards;
    }

    private StreakUpdateResponse buildStreakResponse(UserStreakEntity streak,
                                                       boolean streakBroken,
                                                       boolean freezeUsed,
                                                       List<String> rewards) {
        return StreakUpdateResponse.builder()
                .currentStreak(streak.getCurrentStreak())
                .longestStreak(streak.getLongestStreak())
                .freezeCount(streak.getFreezeCount())
                .streakBroken(streakBroken)
                .freezeAutoUsed(freezeUsed)
                .rewards(rewards)
                .lastActivityDate(streak.getLastActivityDate())
                .build();
    }

    private CurrentStreakResponse buildCurrentStreakResponse(UserStreakEntity streak) {
        return CurrentStreakResponse.builder()
                .currentStreak(streak.getCurrentStreak())
                .longestStreak(streak.getLongestStreak())
                .freezeCount(streak.getFreezeCount())
                .freezeUsedCount(streak.getFreezeUsedCount())
                .totalDaysActive(streak.getTotalDaysActive())
                .lastActivityDate(streak.getLastActivityDate())
                .streakStartDate(streak.getStreakStartDate())
                .build();
    }
}
