package com.hth.udecareer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hth.udecareer.entities.*;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.GoalClaimResponse;
import com.hth.udecareer.model.response.StreakGoalResponse;
import com.hth.udecareer.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreakGoalService {

    private final StreakGoalRepository streakGoalRepository;
    private final UserStreakGoalRepository userStreakGoalRepository;
    private final UserStreakRepository userStreakRepository;
    private final UserPointsService userPointsService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * Get all available goals for a user
     */
    @Transactional(readOnly = true)
    public List<StreakGoalResponse> getAvailableGoals(Long userId) {
        List<StreakGoalEntity> allGoals = streakGoalRepository.findByIsActiveTrueOrderByPriorityAsc();
        List<StreakGoalResponse> responses = new ArrayList<>();

        for (StreakGoalEntity goal : allGoals) {
            Optional<UserStreakGoalEntity> userGoalOpt =
                    userStreakGoalRepository.findByUserIdAndGoalIdAndStatus(
                            userId, goal.getId(), UserStreakGoalEntity.STATUS_ACTIVE);

            responses.add(mapToStreakGoalResponse(goal, userGoalOpt.orElse(null)));
        }

        return responses;
    }

    /**
     * Claim a completed goal's reward
     */
    @Transactional
    public GoalClaimResponse claimGoalReward(Long userId, Long goalId) {
        // Verify goal exists and is active
        StreakGoalEntity goal = streakGoalRepository.findById(goalId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Goal not found"));

        if (!goal.getIsActive()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Goal is not active");
        }

        // Check if goal is completed but not claimed
        UserStreakGoalEntity userGoal = userStreakGoalRepository
                .findByUserIdAndGoalIdAndStatus(userId, goalId, UserStreakGoalEntity.STATUS_COMPLETED)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR,
                        "Goal not completed or already claimed"));

        // Process rewards
        List<String> rewardsGranted = processRewards(userId, goal);

        // Mark as claimed
        userGoal.setStatus(UserStreakGoalEntity.STATUS_CLAIMED);
        userGoal.setClaimedAt(LocalDateTime.now());
        userStreakGoalRepository.save(userGoal);

        // Update user streak stats
        UserStreakEntity streak = userStreakRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        streak.setTotalGoalsCompleted(streak.getTotalGoalsCompleted() + 1);
        userStreakRepository.save(streak);

        log.info("User {} claimed goal {}: {}", userId, goalId, goal.getName());

        return GoalClaimResponse.builder()
                .goalId(goalId)
                .goalName(goal.getName())
                .rewardsGranted(rewardsGranted)
                .claimedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Check and update goal progress based on streak milestones
     */
    @Transactional
    public void checkStreakMilestoneGoals(Long userId, int currentStreak) {
        List<StreakGoalEntity> milestoneGoals = streakGoalRepository
                .findByGoalTypeAndIsActiveTrueOrderByPriorityAsc(StreakGoalEntity.TYPE_STREAK_MILESTONE);

        for (StreakGoalEntity goal : milestoneGoals) {
            // Parse requirement
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> requirement = objectMapper.readValue(
                        goal.getRequirementJson(), Map.class);
                int requiredStreak = ((Number) requirement.get("streak_days")).intValue();

                if (currentStreak >= requiredStreak) {
                    // Check if already claimed (for non-repeatable goals)
                    if (!goal.getIsRepeatable() &&
                        userStreakGoalRepository.existsByUserIdAndGoalIdAndStatus(
                                userId, goal.getId(), UserStreakGoalEntity.STATUS_CLAIMED)) {
                        continue;
                    }

                    // Mark as completed
                    UserStreakGoalEntity userGoal = userStreakGoalRepository
                            .findByUserIdAndGoalIdAndStatus(userId, goal.getId(),
                                    UserStreakGoalEntity.STATUS_ACTIVE)
                            .orElseGet(() -> {
                                UserStreakGoalEntity newGoal = UserStreakGoalEntity.builder()
                                        .userId(userId)
                                        .goalId(goal.getId())
                                        .status(UserStreakGoalEntity.STATUS_ACTIVE)
                                        .build();
                                return userStreakGoalRepository.save(newGoal);
                            });

                    userGoal.setStatus(UserStreakGoalEntity.STATUS_COMPLETED);
                    userGoal.setCompletedAt(LocalDateTime.now());
                    userStreakGoalRepository.save(userGoal);

                    log.info("Goal {} completed for user {}", goal.getName(), userId);
                }
            } catch (Exception e) {
                log.error("Error checking milestone goal {}: {}", goal.getId(), e.getMessage());
            }
        }
    }

    // ============= PRIVATE HELPER METHODS =============

    @SuppressWarnings("unchecked")
    private List<String> processRewards(Long userId, StreakGoalEntity goal) {
        List<String> rewardsGranted = new ArrayList<>();

        try {
            Map<String, Object> reward = objectMapper.readValue(goal.getRewardJson(), Map.class);
            String rewardType = (String) reward.get("type");

            switch (rewardType) {
                case "POINTS":
                    int points = ((Number) reward.get("value")).intValue();
                    User user = userRepository.findById(userId).orElse(null);
                    if (user != null) {
                        userPointsService.addPoints(user.getEmail(), points,
                                "POINT_GOAL_" + goal.getCode(), null, null);
                        rewardsGranted.add(points + " points");
                    }
                    break;

                case "FREEZE":
                    int freezeCount = ((Number) reward.get("count")).intValue();
                    UserStreakEntity streak = userStreakRepository.findByUserId(userId).orElse(null);
                    if (streak != null) {
                        streak.setFreezeCount(streak.getFreezeCount() + freezeCount);
                        userStreakRepository.save(streak);
                        rewardsGranted.add(freezeCount + " streak freeze(s)");
                    }
                    break;

                case "BADGE":
                    // Badge will be auto-awarded by BadgeService
                    rewardsGranted.add("Badge unlocked");
                    break;

                case "MULTIPLE":
                    List<Map<String, Object>> rewards = (List<Map<String, Object>>) reward.get("rewards");
                    for (Map<String, Object> subReward : rewards) {
                        String subType = (String) subReward.get("type");
                        if ("POINTS".equals(subType)) {
                            int pts = ((Number) subReward.get("value")).intValue();
                            User u = userRepository.findById(userId).orElse(null);
                            if (u != null) {
                                userPointsService.addPoints(u.getEmail(), pts,
                                        "POINT_GOAL_" + goal.getCode(), null, null);
                                rewardsGranted.add(pts + " points");
                            }
                        } else if ("FREEZE".equals(subType)) {
                            int fc = ((Number) subReward.get("count")).intValue();
                            UserStreakEntity st = userStreakRepository.findByUserId(userId).orElse(null);
                            if (st != null) {
                                st.setFreezeCount(st.getFreezeCount() + fc);
                                userStreakRepository.save(st);
                                rewardsGranted.add(fc + " streak freeze(s)");
                            }
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            log.error("Error processing rewards for goal {}: {}", goal.getId(), e.getMessage());
        }

        return rewardsGranted;
    }

    private StreakGoalResponse mapToStreakGoalResponse(StreakGoalEntity goal,
                                                         UserStreakGoalEntity userGoal) {
        return StreakGoalResponse.builder()
                .id(goal.getId())
                .goalType(goal.getGoalType())
                .code(goal.getCode())
                .name(goal.getName())
                .description(goal.getDescription())
                .icon(goal.getIcon())
                .requirementJson(goal.getRequirementJson())
                .rewardJson(goal.getRewardJson())
                .isRepeatable(goal.getIsRepeatable())
                .userStatus(userGoal != null ? userGoal.getStatus() : null)
                .completedAt(userGoal != null ? userGoal.getCompletedAt() : null)
                .build();
    }

    /**
     * Adjust user's streak goals (e.g., enable/disable goals, set custom targets)
     * This allows users to customize which goals they want to track
     */
    @Transactional
    public List<StreakGoalResponse> adjustGoals(Long userId, Map<Long, Boolean> goalPreferences) {
        log.info("Adjusting goals for user {}: {}", userId, goalPreferences);

        for (Map.Entry<Long, Boolean> entry : goalPreferences.entrySet()) {
            Long goalId = entry.getKey();
            Boolean isEnabled = entry.getValue();

            // Verify goal exists
            streakGoalRepository.findById(goalId)
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

            Optional<UserStreakGoalEntity> userGoalOpt =
                    userStreakGoalRepository.findByUserIdAndGoalIdAndStatus(
                            userId, goalId, UserStreakGoalEntity.STATUS_ACTIVE);

            if (Boolean.TRUE.equals(isEnabled)) {
                // Enable goal if not already active
                if (userGoalOpt.isEmpty()) {
                    UserStreakGoalEntity userGoal = UserStreakGoalEntity.builder()
                            .userId(userId)
                            .goalId(goalId)
                            .status(UserStreakGoalEntity.STATUS_ACTIVE)
                            .progressJson("{}")
                            .build();
                    userStreakGoalRepository.save(userGoal);
                    log.info("Enabled goal {} for user {}", goalId, userId);
                }
            } else {
                // Disable goal by setting status to EXPIRED
                if (userGoalOpt.isPresent()) {
                    UserStreakGoalEntity userGoal = userGoalOpt.get();
                    userGoal.setStatus(UserStreakGoalEntity.STATUS_EXPIRED);
                    userStreakGoalRepository.save(userGoal);
                    log.info("Disabled goal {} for user {}", goalId, userId);
                }
            }
        }

        // Return updated list of goals
        return streakGoalRepository.findAll().stream()
                .map(goal -> {
                    Optional<UserStreakGoalEntity> userGoal =
                            userStreakGoalRepository.findByUserIdAndGoalIdAndStatus(
                                    userId, goal.getId(), UserStreakGoalEntity.STATUS_ACTIVE);
                    return mapToStreakGoalResponse(goal, userGoal.orElse(null));
                })
                .toList();
    }
}
