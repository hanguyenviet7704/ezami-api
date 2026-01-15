package com.hth.udecareer.service;

import com.hth.udecareer.entities.FollowEntity;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.entities.XProfileEntity;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.FollowItemResponse;
import com.hth.udecareer.model.response.FollowResponse;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.model.response.XProfileResponse;
import com.hth.udecareer.repository.FollowRepository;
import com.hth.udecareer.repository.UserMetaRepository;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.repository.XProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final XProfileRepository xProfileRepository;
    private final UserRepository userRepository;
    private final UserMetaRepository userMetaRepository;

    /**
     * Follow a user
     */
    @Transactional
    public FollowResponse follow(Long currentUserId, String targetUsername) {
        User targetUser = getUserByUsername(targetUsername);
        Long targetUserId = targetUser.getId();

        validateNotSelf(currentUserId, targetUserId);

        // Check if already following
        Optional<FollowEntity> existingFollow = followRepository.findByFollowerIdAndFollowedId(currentUserId, targetUserId);

        if (existingFollow.isPresent()) {
            FollowEntity follow = existingFollow.get();
            if (follow.isBlocked()) {
                // If blocked, change to following
                follow.setLevel(FollowEntity.LEVEL_FOLLOWING);
                followRepository.save(follow);
                return buildFollowResponse(follow, "Successfully followed user", targetUserId);
            }
            // Already following
            return buildFollowResponse(follow, "Already following this user", targetUserId);
        }

        // Create new follow relationship
        FollowEntity follow = FollowEntity.builder()
                .followerId(currentUserId)
                .followedId(targetUserId)
                .level(FollowEntity.LEVEL_FOLLOWING)
                .build();
        followRepository.save(follow);

        log.info("User {} followed user {}", currentUserId, targetUserId);
        return buildFollowResponse(follow, "Successfully followed user", targetUserId);
    }

    /**
     * Unfollow a user
     */
    @Transactional
    public FollowResponse unfollow(Long currentUserId, String targetUsername) {
        User targetUser = getUserByUsername(targetUsername);
        Long targetUserId = targetUser.getId();

        validateNotSelf(currentUserId, targetUserId);

        Optional<FollowEntity> existingFollow = followRepository.findByFollowerIdAndFollowedId(currentUserId, targetUserId);

        if (existingFollow.isEmpty() || existingFollow.get().isBlocked()) {
            return FollowResponse.builder()
                    .success(true)
                    .message("Not following this user")
                    .xprofile(getXProfile(targetUserId))
                    .build();
        }

        followRepository.delete(existingFollow.get());
        log.info("User {} unfollowed user {}", currentUserId, targetUserId);

        return FollowResponse.builder()
                .success(true)
                .message("Successfully unfollowed user")
                .xprofile(getXProfile(targetUserId))
                .build();
    }

    /**
     * Block a user
     */
    @Transactional
    public FollowResponse block(Long currentUserId, String targetUsername) {
        User targetUser = getUserByUsername(targetUsername);
        Long targetUserId = targetUser.getId();

        validateNotSelf(currentUserId, targetUserId);

        Optional<FollowEntity> existingFollow = followRepository.findByFollowerIdAndFollowedId(currentUserId, targetUserId);

        FollowEntity follow;
        if (existingFollow.isPresent()) {
            follow = existingFollow.get();
            follow.setLevel(FollowEntity.LEVEL_BLOCKED);
        } else {
            follow = FollowEntity.builder()
                    .followerId(currentUserId)
                    .followedId(targetUserId)
                    .level(FollowEntity.LEVEL_BLOCKED)
                    .build();
        }
        followRepository.save(follow);

        log.info("User {} blocked user {}", currentUserId, targetUserId);
        return buildFollowResponse(follow, "Successfully blocked user", targetUserId);
    }

    /**
     * Unblock a user
     */
    @Transactional
    public FollowResponse unblock(Long currentUserId, String targetUsername) {
        User targetUser = getUserByUsername(targetUsername);
        Long targetUserId = targetUser.getId();

        validateNotSelf(currentUserId, targetUserId);

        Optional<FollowEntity> existingFollow = followRepository.findByFollowerIdAndFollowedId(currentUserId, targetUserId);

        if (existingFollow.isEmpty() || !existingFollow.get().isBlocked()) {
            return FollowResponse.builder()
                    .success(true)
                    .message("User is not blocked")
                    .xprofile(getXProfile(targetUserId))
                    .build();
        }

        followRepository.delete(existingFollow.get());
        log.info("User {} unblocked user {}", currentUserId, targetUserId);

        return FollowResponse.builder()
                .success(true)
                .message("Successfully unblocked user")
                .xprofile(getXProfile(targetUserId))
                .build();
    }

    /**
     * Toggle notifications for a followed user
     */
    @Transactional
    public FollowResponse toggleNotification(Long currentUserId, String targetUsername, Boolean enable) {
        User targetUser = getUserByUsername(targetUsername);
        Long targetUserId = targetUser.getId();

        validateNotSelf(currentUserId, targetUserId);

        Optional<FollowEntity> existingFollow = followRepository.findByFollowerIdAndFollowedId(currentUserId, targetUserId);

        if (existingFollow.isEmpty() || !existingFollow.get().isFollowing()) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "You must follow this user first");
        }

        FollowEntity follow = existingFollow.get();
        int newLevel = enable != null && enable
                ? FollowEntity.LEVEL_FOLLOWING_WITH_NOTIFICATIONS
                : FollowEntity.LEVEL_FOLLOWING;
        follow.setLevel(newLevel);
        followRepository.save(follow);

        String message = newLevel == FollowEntity.LEVEL_FOLLOWING_WITH_NOTIFICATIONS
                ? "Notifications enabled for this user"
                : "Notifications disabled for this user";

        log.info("User {} {} notifications for user {}", currentUserId,
                newLevel == FollowEntity.LEVEL_FOLLOWING_WITH_NOTIFICATIONS ? "enabled" : "disabled", targetUserId);

        return buildFollowResponse(follow, message, targetUserId);
    }

    /**
     * Get followers of a user
     */
    @Transactional(readOnly = true)
    public PageResponse<FollowItemResponse> getFollowers(String targetUsername, Long currentUserId, int page, int size) {
        User targetUser = getUserByUsername(targetUsername);
        Long targetUserId = targetUser.getId();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<FollowEntity> followers = followRepository.findFollowersByUserId(targetUserId, pageable);

        List<FollowItemResponse> items = followers.getContent().stream()
                .map(f -> mapToFollowItemResponse(f, f.getFollowerId(), currentUserId))
                .collect(Collectors.toList());

        return PageResponse.<FollowItemResponse>builder()
                .content(items)
                .page(followers.getNumber())
                .size(followers.getSize())
                .totalElements(followers.getTotalElements())
                .totalPages(followers.getTotalPages())
                .hasNext(followers.hasNext())
                .hasPrevious(followers.hasPrevious())
                .first(followers.isFirst())
                .last(followers.isLast())
                .build();
    }

    /**
     * Get users that a user is following
     */
    @Transactional(readOnly = true)
    public PageResponse<FollowItemResponse> getFollowings(String targetUsername, Long currentUserId, int page, int size) {
        User targetUser = getUserByUsername(targetUsername);
        Long targetUserId = targetUser.getId();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<FollowEntity> followings = followRepository.findFollowingsByUserId(targetUserId, pageable);

        List<FollowItemResponse> items = followings.getContent().stream()
                .map(f -> mapToFollowItemResponse(f, f.getFollowedId(), currentUserId))
                .collect(Collectors.toList());

        return PageResponse.<FollowItemResponse>builder()
                .content(items)
                .page(followings.getNumber())
                .size(followings.getSize())
                .totalElements(followings.getTotalElements())
                .totalPages(followings.getTotalPages())
                .hasNext(followings.hasNext())
                .hasPrevious(followings.hasPrevious())
                .first(followings.isFirst())
                .last(followings.isLast())
                .build();
    }

    /**
     * Get blocked users
     */
    @Transactional(readOnly = true)
    public PageResponse<FollowItemResponse> getBlockedUsers(Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<FollowEntity> blocked = followRepository.findBlockedByUserId(currentUserId, pageable);

        List<FollowItemResponse> items = blocked.getContent().stream()
                .map(f -> mapToFollowItemResponse(f, f.getFollowedId(), currentUserId))
                .collect(Collectors.toList());

        return PageResponse.<FollowItemResponse>builder()
                .content(items)
                .page(blocked.getNumber())
                .size(blocked.getSize())
                .totalElements(blocked.getTotalElements())
                .totalPages(blocked.getTotalPages())
                .hasNext(blocked.hasNext())
                .hasPrevious(blocked.hasPrevious())
                .first(blocked.isFirst())
                .last(blocked.isLast())
                .build();
    }

    /**
     * Get follow counts for a user
     */
    @Transactional(readOnly = true)
    public long getFollowersCount(Long userId) {
        return followRepository.countFollowersByUserId(userId);
    }

    @Transactional(readOnly = true)
    public long getFollowingsCount(Long userId) {
        return followRepository.countFollowingsByUserId(userId);
    }

    /**
     * Check if current user is following target user
     */
    @Transactional(readOnly = true)
    public boolean isFollowing(Long currentUserId, Long targetUserId) {
        return followRepository.isFollowing(currentUserId, targetUserId);
    }

    /**
     * Check if current user has blocked target user
     */
    @Transactional(readOnly = true)
    public boolean isBlocked(Long currentUserId, Long targetUserId) {
        return followRepository.isBlocked(currentUserId, targetUserId);
    }

    /**
     * Get public profile by username
     */
    @Transactional(readOnly = true)
    public XProfileResponse getPublicProfile(String username, Long currentUserId) {
        User user = getUserByUsername(username);
        Long userId = user.getId();

        XProfileResponse profile = getXProfile(userId);

        // Add follow information if current user is logged in
        if (currentUserId != null && !currentUserId.equals(userId)) {
            boolean isFollowing = followRepository.isFollowing(currentUserId, userId);
            boolean isBlocked = followRepository.isBlocked(currentUserId, userId);
            boolean followsMe = followRepository.isFollowing(userId, currentUserId);

            // Update profile with follow info
            profile.setIsFollowing(isFollowing);
            profile.setIsBlocked(isBlocked);
            profile.setFollowsMe(followsMe);
        }

        // Add follower/following counts
        profile.setFollowersCount(followRepository.countFollowersByUserId(userId));
        profile.setFollowingsCount(followRepository.countFollowingsByUserId(userId));

        return profile;
    }

    // ============= HELPER METHODS =============

    private User getUserByUsername(String username) {
        // First try to find by username in XProfile
        Optional<XProfileEntity> xProfile = xProfileRepository.findByUsername(username);
        if (xProfile.isPresent()) {
            return userRepository.findById(xProfile.get().getUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND, "User not found"));
        }

        // Fallback to User table
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND, "User not found: " + username));
    }

    private void validateNotSelf(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Cannot perform this action on yourself");
        }
    }

    private FollowResponse buildFollowResponse(FollowEntity follow, String message, Long targetUserId) {
        return FollowResponse.builder()
                .id(follow.getId())
                .followerId(follow.getFollowerId())
                .followedId(follow.getFollowedId())
                .level(follow.getLevel())
                .xprofile(getXProfile(targetUserId))
                .message(message)
                .success(true)
                .build();
    }

    private FollowItemResponse mapToFollowItemResponse(FollowEntity follow, Long userIdToShow, Long currentUserId) {
        // Get current user's follow status to this user
        Integer myFollowStatus = null;
        Boolean followsMe = false;

        if (currentUserId != null && !currentUserId.equals(userIdToShow)) {
            Optional<FollowEntity> myFollow = followRepository.findByFollowerIdAndFollowedId(currentUserId, userIdToShow);
            if (myFollow.isPresent() && myFollow.get().isFollowing()) {
                myFollowStatus = myFollow.get().getLevel();
            }

            // Check if this user follows me
            followsMe = followRepository.isFollowing(userIdToShow, currentUserId);
        }

        return FollowItemResponse.builder()
                .id(follow.getId())
                .followerId(follow.getFollowerId())
                .followedId(follow.getFollowedId())
                .level(follow.getLevel())
                .user(getXProfile(userIdToShow))
                .myFollowStatus(myFollowStatus)
                .followsMe(followsMe)
                .createdAt(follow.getCreatedAt())
                .build();
    }

    /**
     * Get XProfile for a user - reused pattern from FeedService
     */
    private XProfileResponse getXProfile(Long userId) {
        Optional<XProfileEntity> xProfileOpt = xProfileRepository.findByUserId(userId);
        Optional<User> userOpt = userRepository.findById(userId);

        // Try to get avatar from UserMeta
        String finalAvatarFromMeta = userMetaRepository.findByUserIdAndMetaKey(userId, "url_image")
                .map(meta -> meta.getMetaValue())
                .orElse(null);

        String finalGender = userMetaRepository.findByUserIdAndMetaKey(userId , "gender")
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
                    .gender(finalGender)
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
                    .gender(finalGender)
                    .totalPoints(0)
                    .isVerified(false)
                    .build())
                    .orElse(XProfileResponse.builder()
                            .userId(userId)
                            .username("")
                            .displayName("")
                            .fullName("")
                            .avatar(null)
                            .gender(finalGender)
                            .totalPoints(0)
                            .isVerified(false)
                            .build());
        }
    }
}
