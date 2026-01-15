package com.hth.udecareer.service;

import com.hth.udecareer.entities.FcomUserActivitiesEntity;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.entities.UserMetaEntity;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.enums.LeaderBoardType;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.*;
import com.hth.udecareer.repository.FcomUserActivityRepository;
import com.hth.udecareer.repository.UserMetaRepository;
import com.hth.udecareer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPointsService {

    private static final String KEY_CURRENT_POINTS = "current_points";
    private static final String KEY_POINT_WEEK = "point_week";
    private static final String KEY_POINT_MONTH = "point_month";
    private static final String KEY_POINT_YEAR = "point_year";
    private static final String KEY_POINT_WEEK_START = "point_week_start";
    private static final String KEY_POINT_MONTH_START = "point_month_start";
    private static final String KEY_POINT_YEAR_START = "point_year_start";

    private final UserRepository userRepository;
    private final UserMetaRepository userMetaRepository;
    private final FcomUserActivityRepository fcomUserActivitiesRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addPoints(String email, int pointsToAdd, String actionName, Long feedId, Long relatedId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND));

        // Check if points already added and return early (no exception, no rollback)
        if (relatedId != null && fcomUserActivitiesRepository
                .existsByUserIdAndActionNameAndRelatedId(user.getId(), actionName, relatedId)) {
            log.warn("Point already added for user {} action {} relatedId {}", user.getId(), actionName, relatedId);
            return;
        }

        if (relatedId == null && feedId != null && fcomUserActivitiesRepository
                .existsByUserIdAndActionNameAndFeedId(user.getId(), actionName, feedId)) {
            log.warn("Point already added for user {} action {} feedId {}", user.getId(), actionName, feedId);
            return;
        }

        ensurePeriodStarts(user.getId());

        incrementMeta(user.getId(), KEY_CURRENT_POINTS, pointsToAdd);
        incrementMeta(user.getId(), KEY_POINT_WEEK, pointsToAdd);
        incrementMeta(user.getId(), KEY_POINT_MONTH, pointsToAdd);
        incrementMeta(user.getId(), KEY_POINT_YEAR, pointsToAdd);

        FcomUserActivitiesEntity activity = FcomUserActivitiesEntity.builder()
                .userId(user.getId())
                .actionName(actionName)
                .message("Cộng " + pointsToAdd + " điểm.")
                .isPublic(false)
                .feedId(feedId)
                .relatedId(relatedId)
                .build();

        fcomUserActivitiesRepository.save(activity);
    }

    @Transactional
    public void removePoints(String email, int pointsToRemove, String actionName, Long feedId, Long relatedId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND));

        // Find and delete the activity record
        List<FcomUserActivitiesEntity> activities;
        if (relatedId != null) {
            activities = fcomUserActivitiesRepository
                    .findByUserIdAndActionNameAndRelatedId(user.getId(), actionName, relatedId);
        } else if (feedId != null) {
            activities = fcomUserActivitiesRepository
                    .findByUserIdAndActionNameAndFeedId(user.getId(), actionName, feedId);
        } else {
            return; // Nothing to remove
        }

        if (activities.isEmpty()) {
            return; // No activity found, nothing to remove
        }

        // Delete the activity record
        fcomUserActivitiesRepository.deleteAll(activities);

        // Deduct points
        ensurePeriodStarts(user.getId());

        incrementMeta(user.getId(), KEY_CURRENT_POINTS, -pointsToRemove);
        incrementMeta(user.getId(), KEY_POINT_WEEK, -pointsToRemove);
        incrementMeta(user.getId(), KEY_POINT_MONTH, -pointsToRemove);
        incrementMeta(user.getId(), KEY_POINT_YEAR, -pointsToRemove);
    }

    public List<LeaderBoardResponse> getLeaderboard(LeaderBoardType type) {
        if (type == null) {
            type = LeaderBoardType.WEEK;
        }

        String pointKey;
        String startKey;
        String startValue;

        switch (type) {
            case WEEK:
                pointKey = KEY_POINT_WEEK;
                startKey = KEY_POINT_WEEK_START;
                startValue = getWeekStartToday().toString();
                break;
            case MONTH:
                pointKey = KEY_POINT_MONTH;
                startKey = KEY_POINT_MONTH_START;
                startValue = getMonthStartToday().toString();
                break;
            case YEAR:
                pointKey = KEY_POINT_YEAR;
                startKey = KEY_POINT_YEAR_START;
                startValue = getYearStartToday().toString();
                break;
            default:
                pointKey = KEY_POINT_WEEK;
                startKey = KEY_POINT_WEEK_START;
                startValue = getWeekStartToday().toString();
        }

        List<Object[]> rows = userMetaRepository.findTopUsersByKeyAndStart(pointKey, startKey, startValue);
        return mapToLeaderboard(rows);
    }

    private List<LeaderBoardResponse> mapToLeaderboard(List<Object[]> rows) {
        List<LeaderBoardResponse> leaderboard = new ArrayList<>();
        int rank = 1;
        for (Object[] row : rows) {
            Long userId = row[0] != null ? ((Number) row[0]).longValue() : 0L;
            String username = row[1] != null ? (String) row[1] : "";
            String name = row[2] != null ? (String) row[2] : "Unknown User";
            Long points = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            String avatar = row[4] != null ? (String) row[4] : "";

            leaderboard.add(LeaderBoardResponse.builder()
                    .rank(rank++)
                    .userId(userId)
                    .username(username)
                    .displayName(name)
                    .totalPoints(points)
                    .avatarUrl(avatar)
                    .build());
        }
        return leaderboard;
    }

    private void ensurePeriodStarts(Long userId) {
        String weekStartNow = getWeekStartToday().toString();
        String weekStartMeta = getMeta(userId, KEY_POINT_WEEK_START);
        if (weekStartMeta == null || !weekStartMeta.equals(weekStartNow)) {
            setMeta(userId, KEY_POINT_WEEK_START, weekStartNow);
            setMeta(userId, KEY_POINT_WEEK, "0");
        }

        String monthStartNow = getMonthStartToday().toString();
        String monthStartMeta = getMeta(userId, KEY_POINT_MONTH_START);
        if (monthStartMeta == null || !monthStartMeta.equals(monthStartNow)) {
            setMeta(userId, KEY_POINT_MONTH_START, monthStartNow);
            setMeta(userId, KEY_POINT_MONTH, "0");
        }

        String yearStartNow = getYearStartToday().toString();
        String yearStartMeta = getMeta(userId, KEY_POINT_YEAR_START);
        if (yearStartMeta == null || !yearStartMeta.equals(yearStartNow)) {
            setMeta(userId, KEY_POINT_YEAR_START, yearStartNow);
            setMeta(userId, KEY_POINT_YEAR, "0");
        }
    }

    private LocalDate getWeekStartToday() {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private LocalDate getMonthStartToday() {
        return LocalDate.now().withDayOfMonth(1);
    }

    private LocalDate getYearStartToday() {
        return LocalDate.now().withDayOfYear(1);
    }

    private String getMeta(Long userId, String key) {
        return userMetaRepository.findByUserIdAndMetaKey(userId, key)
                .map(UserMetaEntity::getMetaValue)
                .orElse(null);
    }

    private void setMeta(Long userId, String key, String value) {
        UserMetaEntity meta = userMetaRepository.findByUserIdAndMetaKey(userId, key)
                .orElseGet(() -> {
                    UserMetaEntity m = new UserMetaEntity();
                    m.setUserId(userId);
                    m.setMetaKey(key);
                    return m;
                });
        meta.setMetaValue(value);
        userMetaRepository.save(meta);
    }

    private void incrementMeta(Long userId, String key, long amount) {
        UserMetaEntity meta = userMetaRepository.findByUserIdAndMetaKey(userId, key)
                .orElseGet(() -> {
                    UserMetaEntity m = new UserMetaEntity();
                    m.setUserId(userId);
                    m.setMetaKey(key);
                    m.setMetaValue("0");
                    return m;
                });

        long current = 0;
        try {
            current = Long.parseLong(meta.getMetaValue());
        } catch (Exception ignored) {
        }

        meta.setMetaValue(String.valueOf(current + amount));
        userMetaRepository.save(meta);
    }


    public UserPointsResponse getUserPoints(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND));

        long currentPoints = getPointsValue(user.getId(), KEY_CURRENT_POINTS);
        long weekPoints = getPointsValue(user.getId(), KEY_POINT_WEEK);
        long monthPoints = getPointsValue(user.getId(), KEY_POINT_MONTH);
        long yearPoints = getPointsValue(user.getId(), KEY_POINT_YEAR);

        return UserPointsResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .currentPoints(currentPoints)
                .weekPoints(weekPoints)
                .monthPoints(monthPoints)
                .yearPoints(yearPoints)
                .build();
    }

    /**
     * Lấy giá trị điểm từ meta
     */
    private long getPointsValue(Long userId, String key) {
        String value = getMeta(userId, key);
        if (value == null || value.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.warn("Invalid points value for user {} and key {}: {}", userId, key, value);
            return 0L;
        }
    }

    /**
     * Lấy lịch sử cộng điểm của user với phân trang
     * @param email Email của user
     * @param page Số trang (0-based)
     * @param size Số lượng kết quả mỗi trang
     * @return PageResponse chứa danh sách lịch sử điểm
     */
    public PageResponse<PointHistoryResponse> getUserPointHistory(String email, int page, int size) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND));

        // Validate pagination
        if (page < 0) {
            page = 0;
        }
        if (size < 1) {
            size = 20;
        }
        if (size > 100) {
            size = 100;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<FcomUserActivitiesEntity> activitiesPage = fcomUserActivitiesRepository
                .findPointHistoryByUserId(user.getId(), pageable);

        List<PointHistoryResponse> historyList = activitiesPage.getContent().stream()
                .map(this::mapToPointHistoryResponse)
                .collect(Collectors.toList());

        return PageResponse.of(historyList, pageable, activitiesPage.getTotalElements());
    }

    /**
     * Map FcomUserActivitiesEntity sang PointHistoryResponse
     */
    private PointHistoryResponse mapToPointHistoryResponse(FcomUserActivitiesEntity activity) {
        Integer points = extractPointsFromMessage(activity.getMessage());
        String actionDescription = getActionDescription(activity.getActionName());

        return PointHistoryResponse.builder()
                .id(activity.getId())
                .actionName(activity.getActionName())
                .actionDescription(actionDescription)
                .points(points)
                .message(activity.getMessage())
                .feedId(activity.getFeedId())
                .relatedId(activity.getRelatedId())
                .createdAt(activity.getCreatedAt())
                .build();
    }

    /**
     * Trích xuất số điểm từ message (ví dụ: "Cộng 20 điểm." -> 20)
     */
    private Integer extractPointsFromMessage(String message) {
        if (message == null || message.isEmpty()) {
            return 0;
        }
        Pattern pattern = Pattern.compile("Cộng\\s+(\\d+)\\s+điểm");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                log.warn("Failed to parse points from message: {}", message);
            }
        }
        return 0;
    }

    /**
     * Lấy mô tả tiếng Việt cho action name
     */
    private String getActionDescription(String actionName) {
        if (actionName == null) {
            return "Không xác định";
        }
        switch (actionName) {
            case "POINT_FEED_PUBLISHED":
                return "Đăng bài";
            case "POINT_COMMENT_ADDED":
                return "Bình luận";
            case "POINT_REACTION_LIKE":
                return "Thích bài viết";
            case "POINT_REFERRAL_GIVE":
                return "Giới thiệu thành công";
            case "POINT_REFERRAL_RECEIVE":
                return "Được giới thiệu";
            case "POINT_AFFILIATE_GIVE":
                return "Đăng ký qua Affiliate";
            case "POINT_AFFILIATE_FIRST_POST":
                return "Người được giới thiệu qua link Affiliate đăng bài đầu tiên";
            case "POINT_REFERRAL_ACTIVE":
                return "Người được giới thiệu hoạt động hơn 3 ngày";
            default:
                return actionName.replace("POINT_", "").replace("_", " ");
        }
    }

    /**
     * Lấy điểm và rank của user theo username
     * @param username Username của user
     * @return UserPointsWithRankResponse chứa điểm và rank của user
     */
    public com.hth.udecareer.model.response.UserPointsWithRankResponse getUserPointsWithRank(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND, "User not found: " + username));

        long currentPoints = getPointsValue(user.getId(), KEY_CURRENT_POINTS);
        long weekPoints = getPointsValue(user.getId(), KEY_POINT_WEEK);
        long monthPoints = getPointsValue(user.getId(), KEY_POINT_MONTH);
        long yearPoints = getPointsValue(user.getId(), KEY_POINT_YEAR);

        // Get ranks
        Integer weekRank = getUserRankByType(user.getId(), LeaderBoardType.WEEK);
        Integer monthRank = getUserRankByType(user.getId(), LeaderBoardType.MONTH);
        Integer yearRank = getUserRankByType(user.getId(), LeaderBoardType.YEAR);

        // Get avatar
        String avatarUrl = userMetaRepository.findByUserIdAndMetaKey(user.getId(), "url_image")
                .map(UserMetaEntity::getMetaValue)
                .orElse(null);

        return UserPointsWithRankResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(avatarUrl)
                .currentPoints(currentPoints)
                .weekPoints(weekPoints)
                .monthPoints(monthPoints)
                .yearPoints(yearPoints)
                .weekRank(weekRank)
                .monthRank(monthRank)
                .yearRank(yearRank)
                .build();
    }


    private Integer getUserRankByType(Long userId, LeaderBoardType type) {
        List<LeaderBoardResponse> leaderboard = getLeaderboard(type);
        for (int i = 0; i < leaderboard.size(); i++) {
            if (leaderboard.get(i).getUserId().equals(userId)) {
                return i + 1; // Rank bắt đầu từ 1
            }
        }
        return null; // User không có trong leaderboard
    }
}
