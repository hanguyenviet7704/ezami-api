package com.hth.udecareer.service;

import com.hth.udecareer.entities.BadgeEntity;
import com.hth.udecareer.entities.UserBadgeEntity;
import com.hth.udecareer.entities.UserStreakEntity;
import com.hth.udecareer.repository.BadgeRepository;
import com.hth.udecareer.repository.UserBadgeRepository;
import com.hth.udecareer.repository.UserStreakRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service to integrate streak achievements with community badge system.
 * Automatically awards badges when users reach streak milestones.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StreakBadgeIntegrationService {

    private final UserStreakRepository userStreakRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    /**
     * Check and award streak-based badges for a user.
     * Call this after a user updates their streak.
     *
     * @param userId User ID to check
     */
    @Transactional
    public void checkAndAwardStreakBadges(Long userId) {
        UserStreakEntity streak = userStreakRepository.findByUserId(userId).orElse(null);
        if (streak == null) return;

        // Find all active badges with streak requirements
        List<BadgeEntity> streakBadges = badgeRepository.findByRequirementTypeInAndIsActiveTrue(
                List.of(BadgeEntity.REQ_STREAK, BadgeEntity.REQ_STREAK_FREEZE)
        );

        for (BadgeEntity badge : streakBadges) {
            // Skip if user already has this badge
            if (userBadgeRepository.existsByUserIdAndBadgeId(userId, badge.getId())) {
                continue;
            }

            boolean shouldAward = false;
            String reqType = badge.getRequirementType();
            int reqValue = badge.getRequirementValue() != null ? badge.getRequirementValue() : 0;

            switch (reqType) {
                case BadgeEntity.REQ_STREAK:
                    shouldAward = streak.getLongestStreak() >= reqValue;
                    break;
                case BadgeEntity.REQ_STREAK_FREEZE:
                    shouldAward = streak.getFreezeUsedCount() >= reqValue;
                    break;
            }

            if (shouldAward) {
                UserBadgeEntity userBadge = UserBadgeEntity.builder()
                        .userId(userId)
                        .badgeId(badge.getId())
                        .note("Auto-awarded for streak achievement")
                        .build();

                userBadgeRepository.save(userBadge);
                log.info("Auto-awarded streak badge '{}' to user {}", badge.getName(), userId);
            }
        }
    }
}
