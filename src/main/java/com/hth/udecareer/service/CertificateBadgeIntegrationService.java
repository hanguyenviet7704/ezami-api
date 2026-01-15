package com.hth.udecareer.service;

import com.hth.udecareer.entities.BadgeEntity;
import com.hth.udecareer.entities.UserBadgeEntity;
import com.hth.udecareer.model.response.UserBadgeResponse;
import com.hth.udecareer.repository.BadgeRepository;
import com.hth.udecareer.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service to integrate certificate achievements with community badge system.
 * Automatically awards badges when users earn certificates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateBadgeIntegrationService {

    private final UserCertificateService userCertificateService;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    /**
     * Check and award certificate-based badges for a user.
     * Call this after a user earns a new certificate.
     *
     * @param userId User ID to check
     * @return List of newly awarded badges
     */
    @Transactional
    public List<UserBadgeResponse> checkAndAwardCertificateBadges(Long userId) {
        List<UserBadgeResponse> awardedBadges = new ArrayList<>();

        // Get user's certificate stats
        Map<String, Object> stats = userCertificateService.getCertificateStats(userId);
        long courseCerts = ((Number) stats.get("courseCertificates")).longValue();
        long quizCerts = ((Number) stats.get("quizCertificates")).longValue();
        long totalCerts = ((Number) stats.get("totalCertificates")).longValue();

        // Find all active badges with certificate requirements
        List<BadgeEntity> certBadges = badgeRepository.findByRequirementTypeInAndIsActiveTrue(
                List.of(
                        BadgeEntity.REQ_COURSE_CERTIFICATES,
                        BadgeEntity.REQ_QUIZ_CERTIFICATES,
                        BadgeEntity.REQ_TOTAL_CERTIFICATES
                )
        );

        for (BadgeEntity badge : certBadges) {
            // Skip if user already has this badge
            if (userBadgeRepository.existsByUserIdAndBadgeId(userId, badge.getId())) {
                continue;
            }

            boolean shouldAward = false;
            String reqType = badge.getRequirementType();
            int reqValue = badge.getRequirementValue() != null ? badge.getRequirementValue() : 0;

            switch (reqType) {
                case BadgeEntity.REQ_COURSE_CERTIFICATES:
                    shouldAward = courseCerts >= reqValue;
                    break;
                case BadgeEntity.REQ_QUIZ_CERTIFICATES:
                    shouldAward = quizCerts >= reqValue;
                    break;
                case BadgeEntity.REQ_TOTAL_CERTIFICATES:
                    shouldAward = totalCerts >= reqValue;
                    break;
            }

            if (shouldAward) {
                UserBadgeEntity userBadge = UserBadgeEntity.builder()
                        .userId(userId)
                        .badgeId(badge.getId())
                        .note("Auto-awarded for certificate achievement")
                        .build();

                userBadgeRepository.save(userBadge);
                log.info("Auto-awarded badge '{}' to user {} for {} certificates",
                        badge.getName(), userId, reqType);

                awardedBadges.add(mapToResponse(userBadge, badge));
            }
        }

        return awardedBadges;
    }

    /**
     * Get user's learning achievements summary for profile display.
     *
     * @param userId User ID
     * @return Combined stats of certificates and certificate-based badges
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getLearningAchievementsSummary(Long userId) {
        // Get certificate stats
        Map<String, Object> certStats = userCertificateService.getCertificateStats(userId);

        // Count certificate-based badges earned
        long certBadgeCount = userBadgeRepository.countCertificateBadgesByUserId(userId,
                List.of(
                        BadgeEntity.REQ_COURSE_CERTIFICATES,
                        BadgeEntity.REQ_QUIZ_CERTIFICATES,
                        BadgeEntity.REQ_TOTAL_CERTIFICATES
                )
        );

        // Get next milestone badges
        List<BadgeEntity> nextMilestones = getNextCertificateMilestones(userId, certStats);

        return Map.of(
                "certificates", certStats,
                "certificateBadgesEarned", certBadgeCount,
                "nextMilestones", nextMilestones.stream()
                        .map(b -> Map.of(
                                "id", b.getId(),
                                "name", b.getName(),
                                "requirementType", b.getRequirementType(),
                                "requirementValue", b.getRequirementValue(),
                                "icon", b.getIcon() != null ? b.getIcon() : ""
                        ))
                        .toList()
        );
    }

    /**
     * Get certificate-based badges that user is close to earning.
     */
    private List<BadgeEntity> getNextCertificateMilestones(Long userId, Map<String, Object> certStats) {
        long courseCerts = ((Number) certStats.get("courseCertificates")).longValue();
        long quizCerts = ((Number) certStats.get("quizCertificates")).longValue();
        long totalCerts = ((Number) certStats.get("totalCertificates")).longValue();

        List<BadgeEntity> allCertBadges = badgeRepository.findByRequirementTypeInAndIsActiveTrue(
                List.of(
                        BadgeEntity.REQ_COURSE_CERTIFICATES,
                        BadgeEntity.REQ_QUIZ_CERTIFICATES,
                        BadgeEntity.REQ_TOTAL_CERTIFICATES
                )
        );

        List<BadgeEntity> nextMilestones = new ArrayList<>();

        for (BadgeEntity badge : allCertBadges) {
            // Skip if already earned
            if (userBadgeRepository.existsByUserIdAndBadgeId(userId, badge.getId())) {
                continue;
            }

            int reqValue = badge.getRequirementValue() != null ? badge.getRequirementValue() : 0;
            long current = 0;

            switch (badge.getRequirementType()) {
                case BadgeEntity.REQ_COURSE_CERTIFICATES:
                    current = courseCerts;
                    break;
                case BadgeEntity.REQ_QUIZ_CERTIFICATES:
                    current = quizCerts;
                    break;
                case BadgeEntity.REQ_TOTAL_CERTIFICATES:
                    current = totalCerts;
                    break;
            }

            // Only include if user is at least 50% towards the goal
            if (current > 0 && (current * 2) >= reqValue) {
                nextMilestones.add(badge);
            }
        }

        // Limit to top 3
        return nextMilestones.stream().limit(3).toList();
    }

    private UserBadgeResponse mapToResponse(UserBadgeEntity userBadge, BadgeEntity badge) {
        return UserBadgeResponse.builder()
                .id(userBadge.getId())
                .userId(userBadge.getUserId())
                .badgeId(badge.getId())
                .isFeatured(userBadge.getIsFeatured())
                .note(userBadge.getNote())
                .earnedAt(userBadge.getEarnedAt())
                .build();
    }
}
