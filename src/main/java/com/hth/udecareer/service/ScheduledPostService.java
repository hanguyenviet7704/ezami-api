package com.hth.udecareer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hth.udecareer.config.TimezoneConfig;
import com.hth.udecareer.entities.ScheduledPostEntity;
import com.hth.udecareer.entities.SpaceEntity;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.entities.XProfileEntity;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.request.ScheduledPostRequest;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.model.response.ScheduledPostResponse;
import com.hth.udecareer.model.response.XProfileResponse;
import com.hth.udecareer.repository.ScheduledPostRepository;
import com.hth.udecareer.repository.SpaceRepository;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledPostService {

    private final ScheduledPostRepository scheduledPostRepository;
    private final SpaceRepository spaceRepository;
    private final XProfileRepository xProfileRepository;
    private final UserRepository userRepository;
    private final UserMetaRepository userMetaRepository;
    private final ObjectMapper objectMapper;

    private static final List<String> VALID_PRIVACY = Arrays.asList(
            ScheduledPostEntity.PRIVACY_PUBLIC,
            ScheduledPostEntity.PRIVACY_FOLLOWERS,
            ScheduledPostEntity.PRIVACY_PRIVATE
    );

    /**
     * Create a scheduled post
     */
    @Transactional
    public ScheduledPostResponse createScheduledPost(Long userId, ScheduledPostRequest request) {
        // Validate scheduled time is in the future
        LocalDateTime now = TimezoneConfig.getCurrentVietnamTime();
        if (request.getScheduledAt().isBefore(now)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Scheduled time must be in the future");
        }

        // Validate privacy
        String privacy = request.getPrivacy() != null ? request.getPrivacy() : ScheduledPostEntity.PRIVACY_PUBLIC;
        if (!VALID_PRIVACY.contains(privacy)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Invalid privacy. Must be one of: " + String.join(", ", VALID_PRIVACY));
        }

        // Validate space if provided
        if (request.getSpaceId() != null) {
            spaceRepository.findById(request.getSpaceId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Space not found"));
        }

        // Convert media and topicIds to JSON
        String mediaJson = null;
        String topicIdsJson = null;
        try {
            if (request.getMedia() != null && !request.getMedia().isEmpty()) {
                mediaJson = objectMapper.writeValueAsString(request.getMedia());
            }
            if (request.getTopicIds() != null && !request.getTopicIds().isEmpty()) {
                topicIdsJson = objectMapper.writeValueAsString(request.getTopicIds());
            }
        } catch (JsonProcessingException e) {
            log.error("Error serializing media/topicIds", e);
        }

        // Create scheduled post
        ScheduledPostEntity scheduledPost = ScheduledPostEntity.builder()
                .userId(userId)
                .spaceId(request.getSpaceId())
                .title(request.getTitle())
                .message(request.getMessage())
                .media(mediaJson)
                .topicIds(topicIdsJson)
                .privacy(privacy)
                .status(ScheduledPostEntity.STATUS_SCHEDULED)
                .scheduledAt(request.getScheduledAt())
                .build();

        scheduledPostRepository.save(scheduledPost);
        log.info("User {} created scheduled post {} for {}", userId, scheduledPost.getId(), request.getScheduledAt());

        return mapToResponse(scheduledPost);
    }

    /**
     * Get user's scheduled posts
     */
    @Transactional(readOnly = true)
    public PageResponse<ScheduledPostResponse> getScheduledPosts(Long userId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ScheduledPostEntity> posts;

        if (status != null && !status.isEmpty()) {
            posts = scheduledPostRepository.findByUserIdAndStatusOrderByScheduledAtAsc(userId, status, pageable);
        } else {
            posts = scheduledPostRepository.findByUserIdOrderByScheduledAtAsc(userId, pageable);
        }

        List<ScheduledPostResponse> items = posts.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<ScheduledPostResponse>builder()
                .content(items)
                .page(posts.getNumber())
                .size(posts.getSize())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .hasNext(posts.hasNext())
                .hasPrevious(posts.hasPrevious())
                .first(posts.isFirst())
                .last(posts.isLast())
                .build();
    }

    /**
     * Get upcoming scheduled posts
     */
    @Transactional(readOnly = true)
    public PageResponse<ScheduledPostResponse> getUpcomingPosts(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime now = TimezoneConfig.getCurrentVietnamTime();
        Page<ScheduledPostEntity> posts = scheduledPostRepository.findUpcomingPosts(userId, now, pageable);

        List<ScheduledPostResponse> items = posts.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<ScheduledPostResponse>builder()
                .content(items)
                .page(posts.getNumber())
                .size(posts.getSize())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .hasNext(posts.hasNext())
                .hasPrevious(posts.hasPrevious())
                .first(posts.isFirst())
                .last(posts.isLast())
                .build();
    }

    /**
     * Get a scheduled post by ID
     */
    @Transactional(readOnly = true)
    public ScheduledPostResponse getScheduledPostById(Long userId, Long postId) {
        ScheduledPostEntity post = scheduledPostRepository.findByIdAndUserId(postId, userId);
        if (post == null) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Scheduled post not found");
        }
        return mapToResponse(post);
    }

    /**
     * Update a scheduled post
     */
    @Transactional
    public ScheduledPostResponse updateScheduledPost(Long userId, Long postId, ScheduledPostRequest request) {
        ScheduledPostEntity post = scheduledPostRepository.findByIdAndUserId(postId, userId);
        if (post == null) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Scheduled post not found");
        }

        // Can only update if still scheduled
        if (!ScheduledPostEntity.STATUS_SCHEDULED.equals(post.getStatus())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Can only update scheduled posts");
        }

        // Validate scheduled time
        LocalDateTime now = TimezoneConfig.getCurrentVietnamTime();
        if (request.getScheduledAt() != null && request.getScheduledAt().isBefore(now)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Scheduled time must be in the future");
        }

        // Update fields
        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        if (request.getMessage() != null) {
            post.setMessage(request.getMessage());
        }
        if (request.getSpaceId() != null) {
            spaceRepository.findById(request.getSpaceId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Space not found"));
            post.setSpaceId(request.getSpaceId());
        }
        if (request.getPrivacy() != null) {
            if (!VALID_PRIVACY.contains(request.getPrivacy())) {
                throw new AppException(ErrorCode.VALIDATION_ERROR,
                        "Invalid privacy. Must be one of: " + String.join(", ", VALID_PRIVACY));
            }
            post.setPrivacy(request.getPrivacy());
        }
        if (request.getScheduledAt() != null) {
            post.setScheduledAt(request.getScheduledAt());
        }

        try {
            if (request.getMedia() != null) {
                post.setMedia(objectMapper.writeValueAsString(request.getMedia()));
            }
            if (request.getTopicIds() != null) {
                post.setTopicIds(objectMapper.writeValueAsString(request.getTopicIds()));
            }
        } catch (JsonProcessingException e) {
            log.error("Error serializing media/topicIds", e);
        }

        scheduledPostRepository.save(post);
        log.info("User {} updated scheduled post {}", userId, postId);

        return mapToResponse(post);
    }

    /**
     * Cancel a scheduled post
     */
    @Transactional
    public ScheduledPostResponse cancelScheduledPost(Long userId, Long postId) {
        ScheduledPostEntity post = scheduledPostRepository.findByIdAndUserId(postId, userId);
        if (post == null) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Scheduled post not found");
        }

        // Can only cancel if still scheduled
        if (!ScheduledPostEntity.STATUS_SCHEDULED.equals(post.getStatus())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Can only cancel scheduled posts");
        }

        post.setStatus(ScheduledPostEntity.STATUS_CANCELLED);
        scheduledPostRepository.save(post);
        log.info("User {} cancelled scheduled post {}", userId, postId);

        return mapToResponse(post);
    }

    /**
     * Delete a scheduled post
     */
    @Transactional
    public void deleteScheduledPost(Long userId, Long postId) {
        ScheduledPostEntity post = scheduledPostRepository.findByIdAndUserId(postId, userId);
        if (post == null) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Scheduled post not found");
        }

        // Can only delete if cancelled or scheduled
        if (!ScheduledPostEntity.STATUS_SCHEDULED.equals(post.getStatus()) &&
                !ScheduledPostEntity.STATUS_CANCELLED.equals(post.getStatus()) &&
                !ScheduledPostEntity.STATUS_FAILED.equals(post.getStatus())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Cannot delete published posts");
        }

        scheduledPostRepository.delete(post);
        log.info("User {} deleted scheduled post {}", userId, postId);
    }

    /**
     * Get count of scheduled posts by status
     */
    @Transactional(readOnly = true)
    public long getScheduledPostCount(Long userId, String status) {
        return scheduledPostRepository.countByUserIdAndStatus(userId, status);
    }

    // ============= HELPER METHODS =============

    private ScheduledPostResponse mapToResponse(ScheduledPostEntity post) {
        // Parse media and topicIds from JSON
        List<String> media = Collections.emptyList();
        List<Long> topicIds = Collections.emptyList();

        try {
            if (post.getMedia() != null && !post.getMedia().isEmpty()) {
                media = objectMapper.readValue(post.getMedia(), new TypeReference<List<String>>() {});
            }
            if (post.getTopicIds() != null && !post.getTopicIds().isEmpty()) {
                topicIds = objectMapper.readValue(post.getTopicIds(), new TypeReference<List<Long>>() {});
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing media/topicIds JSON", e);
        }

        // Get space name
        String spaceName = null;
        if (post.getSpaceId() != null) {
            spaceName = spaceRepository.findById(post.getSpaceId())
                    .map(SpaceEntity::getTitle)
                    .orElse(null);
        }

        return ScheduledPostResponse.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .xprofile(getXProfile(post.getUserId()))
                .spaceId(post.getSpaceId())
                .spaceName(spaceName)
                .title(post.getTitle())
                .message(post.getMessage())
                .media(media)
                .topicIds(topicIds)
                .privacy(post.getPrivacy())
                .status(post.getStatus())
                .scheduledAt(post.getScheduledAt())
                .publishedAt(post.getPublishedAt())
                .publishedPostId(post.getPublishedPostId())
                .errorMessage(post.getErrorMessage())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
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
