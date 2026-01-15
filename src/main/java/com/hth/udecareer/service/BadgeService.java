package com.hth.udecareer.service;

import com.hth.udecareer.entities.BadgeEntity;
import com.hth.udecareer.entities.TranslationEntity;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.entities.UserBadgeEntity;
import com.hth.udecareer.entities.XProfileEntity;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.request.AwardBadgeRequest;
import com.hth.udecareer.model.request.BadgeRequest;
import com.hth.udecareer.model.response.BadgeResponse;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.model.response.UserBadgeResponse;
import com.hth.udecareer.model.response.XProfileResponse;
import com.hth.udecareer.repository.BadgeRepository;
import com.hth.udecareer.repository.UserBadgeRepository;
import com.hth.udecareer.repository.UserMetaRepository;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.repository.XProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final XProfileRepository xProfileRepository;
    private final UserRepository userRepository;
    private final UserMetaRepository userMetaRepository;
    private final TranslationService translationService;

    private static final List<String> VALID_TYPES = Arrays.asList(
            BadgeEntity.TYPE_ACHIEVEMENT,
            BadgeEntity.TYPE_MILESTONE,
            BadgeEntity.TYPE_SPECIAL,
            BadgeEntity.TYPE_CUSTOM
    );

    private static final List<String> VALID_REQ_TYPES = Arrays.asList(
            BadgeEntity.REQ_POSTS,
            BadgeEntity.REQ_COMMENTS,
            BadgeEntity.REQ_REACTIONS,
            BadgeEntity.REQ_FOLLOWERS,
            BadgeEntity.REQ_POINTS,
            BadgeEntity.REQ_MANUAL,
            BadgeEntity.REQ_COURSE_CERTIFICATES,
            BadgeEntity.REQ_QUIZ_CERTIFICATES,
            BadgeEntity.REQ_TOTAL_CERTIFICATES
    );

    // ============= PUBLIC METHODS =============

    /**
     * Get all active badges
     */
    @Transactional(readOnly = true)
    public List<BadgeResponse> getAllBadges() {
        return badgeRepository.findByIsActiveTrueOrderByPriorityAsc()
                .stream()
                .map(this::mapToBadgeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get badges by type
     */
    @Transactional(readOnly = true)
    public List<BadgeResponse> getBadgesByType(String type) {
        if (!VALID_TYPES.contains(type)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Invalid badge type. Must be one of: " + String.join(", ", VALID_TYPES));
        }
        return badgeRepository.findByTypeAndIsActiveTrueOrderByPriorityAsc(type)
                .stream()
                .map(this::mapToBadgeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get badge by ID
     */
    @Transactional(readOnly = true)
    public BadgeResponse getBadgeById(Long badgeId) {
        BadgeEntity badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Badge not found"));
        return mapToBadgeResponse(badge);
    }

    /**
     * Get badge by slug
     */
    @Transactional(readOnly = true)
    public BadgeResponse getBadgeBySlug(String slug) {
        BadgeEntity badge = badgeRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Badge not found"));
        return mapToBadgeResponse(badge);
    }

    /**
     * Get user's badges
     */
    @Transactional(readOnly = true)
    public List<UserBadgeResponse> getUserBadges(Long userId) {
        return userBadgeRepository.findByUserIdOrderByEarnedAtDesc(userId)
                .stream()
                .map(this::mapToUserBadgeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get user's featured badges
     */
    @Transactional(readOnly = true)
    public List<UserBadgeResponse> getFeaturedBadges(Long userId) {
        return userBadgeRepository.findByUserIdAndIsFeaturedTrueOrderByEarnedAtDesc(userId)
                .stream()
                .map(this::mapToUserBadgeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Set badge as featured
     */
    @Transactional
    public UserBadgeResponse setFeaturedBadge(Long userId, Long badgeId, boolean featured) {
        UserBadgeEntity userBadge = userBadgeRepository.findByUserIdAndBadgeId(userId, badgeId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "You don't have this badge"));

        userBadge.setIsFeatured(featured);
        userBadgeRepository.save(userBadge);
        log.info("User {} {} badge {} as featured", userId, featured ? "set" : "unset", badgeId);

        return mapToUserBadgeResponse(userBadge);
    }

    /**
     * Get users who earned a specific badge
     */
    @Transactional(readOnly = true)
    public PageResponse<UserBadgeResponse> getBadgeEarners(Long badgeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserBadgeEntity> earners = userBadgeRepository.findByBadgeIdOrderByEarnedAtDesc(badgeId, pageable);

        List<UserBadgeResponse> items = earners.getContent().stream()
                .map(this::mapToUserBadgeResponse)
                .collect(Collectors.toList());

        return PageResponse.<UserBadgeResponse>builder()
                .content(items)
                .page(earners.getNumber())
                .size(earners.getSize())
                .totalElements(earners.getTotalElements())
                .totalPages(earners.getTotalPages())
                .hasNext(earners.hasNext())
                .hasPrevious(earners.hasPrevious())
                .first(earners.isFirst())
                .last(earners.isLast())
                .build();
    }

    // ============= ADMIN METHODS =============

    /**
     * Get all badges (admin)
     */
    @Transactional(readOnly = true)
    public PageResponse<BadgeResponse> getAllBadgesAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BadgeEntity> badges = badgeRepository.findAllByOrderByPriorityAsc(pageable);

        List<BadgeResponse> items = badges.getContent().stream()
                .map(this::mapToBadgeResponse)
                .collect(Collectors.toList());

        return PageResponse.<BadgeResponse>builder()
                .content(items)
                .page(badges.getNumber())
                .size(badges.getSize())
                .totalElements(badges.getTotalElements())
                .totalPages(badges.getTotalPages())
                .hasNext(badges.hasNext())
                .hasPrevious(badges.hasPrevious())
                .first(badges.isFirst())
                .last(badges.isLast())
                .build();
    }

    /**
     * Create or update badge (admin)
     */
    @Transactional
    public BadgeResponse saveBadge(BadgeRequest request) {
        // Validate type if provided
        if (request.getType() != null && !VALID_TYPES.contains(request.getType())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Invalid badge type. Must be one of: " + String.join(", ", VALID_TYPES));
        }

        // Validate requirement type if provided
        if (request.getRequirementType() != null && !VALID_REQ_TYPES.contains(request.getRequirementType())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Invalid requirement type. Must be one of: " + String.join(", ", VALID_REQ_TYPES));
        }

        BadgeEntity badge;
        if (request.getId() != null) {
            // Update existing
            badge = badgeRepository.findById(request.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Badge not found"));
        } else {
            // Create new
            badge = new BadgeEntity();
        }

        // Generate slug if not provided
        String slug = request.getSlug();
        if (slug == null || slug.trim().isEmpty()) {
            slug = generateSlug(request.getName());
        }

        // Check slug uniqueness
        if (badge.getId() == null || !slug.equals(badge.getSlug())) {
            if (badgeRepository.existsBySlug(slug)) {
                slug = slug + "-" + System.currentTimeMillis();
            }
        }

        badge.setName(request.getName());
        badge.setSlug(slug);
        badge.setDescription(request.getDescription());
        badge.setIcon(request.getIcon());
        badge.setColor(request.getColor());
        badge.setType(request.getType() != null ? request.getType() : BadgeEntity.TYPE_ACHIEVEMENT);
        badge.setRequirementType(request.getRequirementType());
        badge.setRequirementValue(request.getRequirementValue());
        badge.setPointsReward(request.getPointsReward() != null ? request.getPointsReward() : 0);
        badge.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        badge.setPriority(request.getPriority() != null ? request.getPriority() : 0);

        badgeRepository.save(badge);
        log.info("Badge {} saved: {}", badge.getId(), badge.getName());

        return mapToBadgeResponse(badge);
    }

    /**
     * Delete badge (admin)
     */
    @Transactional
    public void deleteBadge(Long badgeId) {
        BadgeEntity badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Badge not found"));

        badgeRepository.delete(badge);
        log.info("Badge {} deleted", badgeId);
    }

    /**
     * Award badge to user (admin)
     */
    @Transactional
    public UserBadgeResponse awardBadge(Long awarderId, AwardBadgeRequest request) {
        // Verify user exists
        userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND, "User not found"));

        // Verify badge exists
        BadgeEntity badge = badgeRepository.findById(request.getBadgeId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Badge not found"));

        // Check if user already has the badge
        if (userBadgeRepository.existsByUserIdAndBadgeId(request.getUserId(), request.getBadgeId())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "User already has this badge");
        }

        // Award badge
        UserBadgeEntity userBadge = UserBadgeEntity.builder()
                .userId(request.getUserId())
                .badgeId(request.getBadgeId())
                .awardedBy(awarderId)
                .note(request.getNote())
                .build();

        userBadgeRepository.save(userBadge);
        log.info("Admin {} awarded badge {} to user {}", awarderId, request.getBadgeId(), request.getUserId());

        return mapToUserBadgeResponse(userBadge);
    }

    /**
     * Revoke badge from user (admin)
     */
    @Transactional
    public void revokeBadge(Long userId, Long badgeId) {
        UserBadgeEntity userBadge = userBadgeRepository.findByUserIdAndBadgeId(userId, badgeId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User doesn't have this badge"));

        userBadgeRepository.delete(userBadge);
        log.info("Badge {} revoked from user {}", badgeId, userId);
    }

    // ============= HELPER METHODS =============

    private String generateSlug(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "badge-" + System.currentTimeMillis();
        }

        // Remove diacritics
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);
        String withoutDiacritics = normalized.replaceAll("\\p{M}", "");

        // Replace Vietnamese specific characters
        withoutDiacritics = withoutDiacritics
                .replaceAll("[đĐ]", "d")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        return withoutDiacritics.isEmpty() ? "badge-" + System.currentTimeMillis() : withoutDiacritics;
    }

    private BadgeResponse mapToBadgeResponse(BadgeEntity badge) {
        long earnedCount = userBadgeRepository.countByBadgeId(badge.getId());

        // Get translations for current language
        Map<String, String> translations = translationService.getTranslations(
                TranslationEntity.TYPE_BADGE, badge.getId());

        // Use translated values if available, otherwise use original
        String translatedName = translations.getOrDefault(
                TranslationEntity.FIELD_NAME, badge.getName());
        String translatedDescription = translations.getOrDefault(
                TranslationEntity.FIELD_DESCRIPTION, badge.getDescription());

        return BadgeResponse.builder()
                .id(badge.getId())
                .name(translatedName)
                .slug(badge.getSlug())
                .description(translatedDescription)
                .icon(badge.getIcon())
                .color(badge.getColor())
                .type(badge.getType())
                .requirementType(badge.getRequirementType())
                .requirementValue(badge.getRequirementValue())
                .pointsReward(badge.getPointsReward())
                .isActive(badge.getIsActive())
                .priority(badge.getPriority())
                .earnedCount(earnedCount)
                .createdAt(badge.getCreatedAt())
                .updatedAt(badge.getUpdatedAt())
                .build();
    }

    private UserBadgeResponse mapToUserBadgeResponse(UserBadgeEntity userBadge) {
        BadgeEntity badge = badgeRepository.findById(userBadge.getBadgeId()).orElse(null);

        return UserBadgeResponse.builder()
                .id(userBadge.getId())
                .userId(userBadge.getUserId())
                .xprofile(getXProfile(userBadge.getUserId()))
                .badgeId(userBadge.getBadgeId())
                .badge(badge != null ? mapToBadgeResponse(badge) : null)
                .isFeatured(userBadge.getIsFeatured())
                .awardedBy(userBadge.getAwardedBy())
                .note(userBadge.getNote())
                .earnedAt(userBadge.getEarnedAt())
                .build();
    }

    private XProfileResponse getXProfile(Long userId) {
        Optional<XProfileEntity> xProfileOpt = xProfileRepository.findByUserId(userId);
        Optional<User> userOpt = userRepository.findById(userId);

        String finalAvatarFromMeta = userMetaRepository.findByUserIdAndMetaKey(userId, "wpcf-avatar")
                .map(meta -> meta.getMetaValue())
                .orElse(null);

        if (xProfileOpt.isPresent()) {
            XProfileEntity xp = xProfileOpt.get();
            String finalAvatar = StringUtils.isNotBlank(xp.getAvatar()) ? xp.getAvatar() : finalAvatarFromMeta;
            String fullName = userOpt.map(User::getDisplayName).orElse(null);

            return XProfileResponse.builder()
                    .userId(xp.getUserId())
                    .username(xp.getUsername())
                    .displayName(xp.getDisplayName())
                    .fullName(fullName)
                    .avatar(finalAvatar)
                    .totalPoints(xp.getTotalPoints())
                    .isVerified(xp.getIsVerified() != null && xp.getIsVerified() == 1)
                    .build();
        } else {
            return userOpt.map(user -> XProfileResponse.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .displayName(user.getDisplayName())
                    .fullName(user.getDisplayName())
                    .avatar(finalAvatarFromMeta)
                    .totalPoints(0)
                    .isVerified(false)
                    .build())
                    .orElse(XProfileResponse.builder()
                            .userId(userId)
                            .username("")
                            .displayName("")
                            .fullName("")
                            .avatar(null)
                            .totalPoints(0)
                            .isVerified(false)
                            .build());
        }
    }
}
