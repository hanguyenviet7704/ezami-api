package com.hth.udecareer.service;

import com.hth.udecareer.model.response.AnalyticsActivityResponse;
import com.hth.udecareer.model.response.AnalyticsWidgetResponse;
import com.hth.udecareer.model.response.LeaderboardItemResponse;
import com.hth.udecareer.model.response.XProfileResponse;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.entities.XProfileEntity;
import com.hth.udecareer.repository.UserMetaRepository;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.repository.XProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final EntityManager entityManager;
    private final XProfileRepository xProfileRepository;
    private final UserRepository userRepository;
    private final UserMetaRepository userMetaRepository;

    /**
     * Get overview widget stats
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "analytics", key = "'overview_widget_' + #days")
    public AnalyticsWidgetResponse getOverviewWidget(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime prevStartDate = LocalDateTime.now().minusDays(days * 2);

        Map<String, AnalyticsWidgetResponse.AnalyticsMetric> data = new LinkedHashMap<>();

        // Total Posts
        long totalPosts = countPosts(null);
        long currentPosts = countPosts(startDate);
        long prevPosts = countPostsBetween(prevStartDate, startDate);
        data.put("posts", buildMetric("Total Posts", totalPosts, currentPosts, prevPosts));

        // Total Comments
        long totalComments = countComments(null);
        long currentComments = countComments(startDate);
        long prevComments = countCommentsBetween(prevStartDate, startDate);
        data.put("comments", buildMetric("Total Comments", totalComments, currentComments, prevComments));

        // Total Reactions
        long totalReactions = countReactions(null);
        long currentReactions = countReactions(startDate);
        long prevReactions = countReactionsBetween(prevStartDate, startDate);
        data.put("reactions", buildMetric("Total Reactions", totalReactions, currentReactions, prevReactions));

        // Active Users
        long activeUsers = countActiveUsers(startDate);
        long prevActiveUsers = countActiveUsersBetween(prevStartDate, startDate);
        data.put("active_users", buildMetric("Active Users", activeUsers, activeUsers, prevActiveUsers));

        return AnalyticsWidgetResponse.builder().data(data).build();
    }

    /**
     * Get activity chart data
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "analytics", key = "'activity_' + #activity + '_' + #days")
    public AnalyticsActivityResponse getActivityChart(String activity, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        List<AnalyticsActivityResponse.ActivityDataPoint> dataPoints = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            long value = switch (activity.toLowerCase()) {
                case "posts" -> countPostsBetween(dayStart, dayEnd);
                case "comments" -> countCommentsBetween(dayStart, dayEnd);
                case "reactions" -> countReactionsBetween(dayStart, dayEnd);
                case "users" -> countNewUsersBetween(dayStart, dayEnd);
                default -> countPostsBetween(dayStart, dayEnd);
            };

            dataPoints.add(AnalyticsActivityResponse.ActivityDataPoint.builder()
                    .date(date.format(formatter))
                    .value(value)
                    .build());
        }

        String title = switch (activity.toLowerCase()) {
            case "posts" -> "Posts Activity";
            case "comments" -> "Comments Activity";
            case "reactions" -> "Reactions Activity";
            case "users" -> "New Users";
            default -> "Activity";
        };

        return AnalyticsActivityResponse.builder()
                .activity(activity)
                .title(title)
                .data(dataPoints)
                .build();
    }

    /**
     * Get members widget stats
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "analytics", key = "'members_widget_' + #days")
    public AnalyticsWidgetResponse getMembersWidget(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime prevStartDate = LocalDateTime.now().minusDays(days * 2);

        Map<String, AnalyticsWidgetResponse.AnalyticsMetric> data = new LinkedHashMap<>();

        // Total Members
        long totalMembers = countTotalMembers();
        long newMembers = countNewUsersBetween(startDate, LocalDateTime.now());
        long prevNewMembers = countNewUsersBetween(prevStartDate, startDate);
        data.put("total_members", buildMetric("Total Members", totalMembers, newMembers, prevNewMembers));

        // Active Members
        long activeMembers = countActiveUsers(startDate);
        long prevActiveMembers = countActiveUsersBetween(prevStartDate, startDate);
        data.put("active_members", buildMetric("Active Members", activeMembers, activeMembers, prevActiveMembers));

        return AnalyticsWidgetResponse.builder().data(data).build();
    }

    /**
     * Get top members
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "analytics", key = "'top_members_' + #limit")
    public List<LeaderboardItemResponse> getTopMembers(int limit) {
        String sql = "SELECT user_id, SUM(points) as total_points FROM (" +
                "  SELECT p.user_id, COUNT(r.id) as points " +
                "  FROM wp_fcom_posts p " +
                "  INNER JOIN wp_fcom_post_reactions r ON r.object_id = p.id AND r.object_type = 'feed' " +
                "  WHERE p.status = 'published' " +
                "  GROUP BY p.user_id " +
                "  UNION ALL " +
                "  SELECT c.user_id, COUNT(r.id) as points " +
                "  FROM wp_fcom_post_comments c " +
                "  INNER JOIN wp_fcom_post_reactions r ON r.object_id = c.id AND r.object_type = 'comment' " +
                "  WHERE c.status = 'published' " +
                "  GROUP BY c.user_id " +
                ") AS combined " +
                "GROUP BY user_id " +
                "ORDER BY total_points DESC";

        Query query = entityManager.createNativeQuery(sql);
        query.setMaxResults(limit);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<LeaderboardItemResponse> items = new ArrayList<>();
        int rank = 1;
        for (Object[] row : results) {
            Long userId = ((Number) row[0]).longValue();
            Integer totalPoints = ((Number) row[1]).intValue();

            items.add(LeaderboardItemResponse.builder()
                    .userId(userId)
                    .totalPoints(totalPoints)
                    .rank(rank++)
                    .xprofile(getXProfile(userId))
                    .build());
        }

        return items;
    }

    /**
     * Get spaces widget stats
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "analytics", key = "'spaces_widget'")
    public AnalyticsWidgetResponse getSpacesWidget() {
        Map<String, AnalyticsWidgetResponse.AnalyticsMetric> data = new LinkedHashMap<>();

        long totalSpaces = countSpaces();
        data.put("total_spaces", AnalyticsWidgetResponse.AnalyticsMetric.builder()
                .totalRecords(totalSpaces)
                .title("Total Spaces")
                .comparison("")
                .build());

        return AnalyticsWidgetResponse.builder().data(data).build();
    }

    /**
     * Get popular spaces
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "analytics", key = "'popular_spaces_' + #limit")
    public List<Map<String, Object>> getPopularSpaces(int limit) {
        String sql = "SELECT s.id, s.title, s.slug, COUNT(p.id) as post_count " +
                "FROM wp_fcom_spaces s " +
                "LEFT JOIN wp_fcom_posts p ON p.space_id = s.id AND p.status = 'published' " +
                "WHERE s.status = 'published' " +
                "GROUP BY s.id, s.title, s.slug " +
                "ORDER BY post_count DESC";

        Query query = entityManager.createNativeQuery(sql);
        query.setMaxResults(limit);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<Map<String, Object>> spaces = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> space = new LinkedHashMap<>();
            space.put("id", ((Number) row[0]).longValue());
            space.put("title", row[1]);
            space.put("slug", row[2]);
            space.put("post_count", ((Number) row[3]).longValue());
            spaces.add(space);
        }

        return spaces;
    }

    // ============= HELPER METHODS =============

    private AnalyticsWidgetResponse.AnalyticsMetric buildMetric(String title, long total, long current, long previous) {
        String comparison = "";
        if (previous > 0) {
            double change = ((double) (current - previous) / previous) * 100;
            comparison = String.format("%+.0f%%", change);
        } else if (current > 0) {
            comparison = "+100%";
        }

        return AnalyticsWidgetResponse.AnalyticsMetric.builder()
                .totalRecords(total)
                .title(title)
                .comparison(comparison)
                .build();
    }

    private long countPosts(LocalDateTime since) {
        String sql = "SELECT COUNT(*) FROM wp_fcom_posts WHERE status = 'published'";
        if (since != null) {
            sql += " AND created_at >= :since";
        }
        Query query = entityManager.createNativeQuery(sql);
        if (since != null) {
            query.setParameter("since", since);
        }
        return ((Number) query.getSingleResult()).longValue();
    }

    private long countPostsBetween(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT COUNT(*) FROM wp_fcom_posts WHERE status = 'published' AND created_at >= :start AND created_at < :end";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("start", start);
        query.setParameter("end", end);
        return ((Number) query.getSingleResult()).longValue();
    }

    private long countComments(LocalDateTime since) {
        String sql = "SELECT COUNT(*) FROM wp_fcom_post_comments WHERE status = 'published'";
        if (since != null) {
            sql += " AND created_at >= :since";
        }
        Query query = entityManager.createNativeQuery(sql);
        if (since != null) {
            query.setParameter("since", since);
        }
        return ((Number) query.getSingleResult()).longValue();
    }

    private long countCommentsBetween(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT COUNT(*) FROM wp_fcom_post_comments WHERE status = 'published' AND created_at >= :start AND created_at < :end";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("start", start);
        query.setParameter("end", end);
        return ((Number) query.getSingleResult()).longValue();
    }

    private long countReactions(LocalDateTime since) {
        String sql = "SELECT COUNT(*) FROM wp_fcom_post_reactions";
        if (since != null) {
            sql += " WHERE created_at >= :since";
        }
        Query query = entityManager.createNativeQuery(sql);
        if (since != null) {
            query.setParameter("since", since);
        }
        return ((Number) query.getSingleResult()).longValue();
    }

    private long countReactionsBetween(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT COUNT(*) FROM wp_fcom_post_reactions WHERE created_at >= :start AND created_at < :end";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("start", start);
        query.setParameter("end", end);
        return ((Number) query.getSingleResult()).longValue();
    }

    private long countActiveUsers(LocalDateTime since) {
        String sql = "SELECT COUNT(DISTINCT user_id) FROM (" +
                "SELECT user_id FROM wp_fcom_posts WHERE status = 'published' AND created_at >= :since " +
                "UNION SELECT user_id FROM wp_fcom_post_comments WHERE status = 'published' AND created_at >= :since " +
                "UNION SELECT user_id FROM wp_fcom_post_reactions WHERE created_at >= :since" +
                ") AS active";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("since", since);
        return ((Number) query.getSingleResult()).longValue();
    }

    private long countActiveUsersBetween(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT COUNT(DISTINCT user_id) FROM (" +
                "SELECT user_id FROM wp_fcom_posts WHERE status = 'published' AND created_at >= :start AND created_at < :end " +
                "UNION SELECT user_id FROM wp_fcom_post_comments WHERE status = 'published' AND created_at >= :start AND created_at < :end " +
                "UNION SELECT user_id FROM wp_fcom_post_reactions WHERE created_at >= :start AND created_at < :end" +
                ") AS active";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("start", start);
        query.setParameter("end", end);
        return ((Number) query.getSingleResult()).longValue();
    }

    private long countTotalMembers() {
        String sql = "SELECT COUNT(*) FROM wp_users WHERE user_status = 0";
        Query query = entityManager.createNativeQuery(sql);
        return ((Number) query.getSingleResult()).longValue();
    }

    private long countNewUsersBetween(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT COUNT(*) FROM wp_users WHERE user_registered >= :start AND user_registered < :end";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("start", start);
        query.setParameter("end", end);
        return ((Number) query.getSingleResult()).longValue();
    }

    private long countSpaces() {
        String sql = "SELECT COUNT(*) FROM wp_fcom_spaces WHERE status = 'published'";
        Query query = entityManager.createNativeQuery(sql);
        return ((Number) query.getSingleResult()).longValue();
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
                    .orElse(null);
        }
    }
}
