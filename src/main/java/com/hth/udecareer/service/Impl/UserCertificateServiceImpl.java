package com.hth.udecareer.service.Impl;

import com.hth.udecareer.entities.Certificate;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.enums.PostStatus;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.model.response.UserCertificateResponse;
import com.hth.udecareer.repository.CertificateRepository;
import com.hth.udecareer.service.UserCertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCertificateServiceImpl implements UserCertificateService {

    private final CertificateRepository certificateRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String TIMEZONE = "Asia/Ho_Chi_Minh";

    @Override
    public PageResponse<UserCertificateResponse> getUserCertificates(Long userId, Pageable pageable) {
        // Get all completed activities (courses and quizzes with pass)
        String sql = """
            SELECT ua.activity_id, ua.post_id, ua.course_id, ua.activity_type,
                   ua.activity_completed, p.post_title,
                   (SELECT meta_value FROM wp_postmeta WHERE post_id = ua.post_id AND meta_key = '_ld_certificate' LIMIT 1) as cert_id,
                   (SELECT activity_meta_value FROM wp_learndash_user_activity_meta WHERE activity_id = ua.activity_id AND activity_meta_key = 'percentage' LIMIT 1) as percentage,
                   (SELECT activity_meta_value FROM wp_learndash_user_activity_meta WHERE activity_id = ua.activity_id AND activity_meta_key = 'points' LIMIT 1) as points,
                   (SELECT activity_meta_value FROM wp_learndash_user_activity_meta WHERE activity_id = ua.activity_id AND activity_meta_key = 'total_points' LIMIT 1) as total_points,
                   (SELECT activity_meta_value FROM wp_learndash_user_activity_meta WHERE activity_id = ua.activity_id AND activity_meta_key = 'pass' LIMIT 1) as pass_status
            FROM wp_learndash_user_activity ua
            JOIN wp_posts p ON ua.post_id = p.ID
            WHERE ua.user_id = :userId
            AND ua.activity_status = 1
            AND ua.activity_completed IS NOT NULL
            AND ua.activity_completed > 0
            AND (
                (ua.activity_type = 'course')
                OR
                (ua.activity_type = 'quiz' AND EXISTS (
                    SELECT 1 FROM wp_learndash_user_activity_meta uam
                    WHERE uam.activity_id = ua.activity_id
                    AND uam.activity_meta_key = 'pass'
                    AND uam.activity_meta_value = '1'
                ))
            )
            ORDER BY ua.activity_completed DESC
            """;

        Query countQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM (" + sql.replace("ORDER BY ua.activity_completed DESC", "") + ") as cnt"
        );
        countQuery.setParameter("userId", userId);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("userId", userId);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<UserCertificateResponse> certificates = results.stream()
                .map(this::mapToCertificateResponse)
                .toList();

        Page<UserCertificateResponse> page = new PageImpl<>(certificates, pageable, total);
        return PageResponse.of(page);
    }

    @Override
    public PageResponse<UserCertificateResponse> getCourseCertificates(Long userId, Pageable pageable) {
        String sql = """
            SELECT ua.activity_id, ua.post_id, ua.course_id, ua.activity_type,
                   ua.activity_completed, p.post_title,
                   (SELECT meta_value FROM wp_postmeta WHERE post_id = ua.post_id AND meta_key = '_ld_certificate' LIMIT 1) as cert_id,
                   NULL as percentage, NULL as points, NULL as total_points, '1' as pass_status
            FROM wp_learndash_user_activity ua
            JOIN wp_posts p ON ua.post_id = p.ID
            WHERE ua.user_id = :userId
            AND ua.activity_type = 'course'
            AND ua.activity_status = 1
            AND ua.activity_completed IS NOT NULL
            AND ua.activity_completed > 0
            ORDER BY ua.activity_completed DESC
            """;

        Query countQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM wp_learndash_user_activity ua " +
                "WHERE ua.user_id = :userId AND ua.activity_type = 'course' " +
                "AND ua.activity_status = 1 AND ua.activity_completed IS NOT NULL AND ua.activity_completed > 0"
        );
        countQuery.setParameter("userId", userId);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("userId", userId);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<UserCertificateResponse> certificates = results.stream()
                .map(this::mapToCertificateResponse)
                .toList();

        Page<UserCertificateResponse> page = new PageImpl<>(certificates, pageable, total);
        return PageResponse.of(page);
    }

    @Override
    public PageResponse<UserCertificateResponse> getQuizCertificates(Long userId, Pageable pageable) {
        String sql = """
            SELECT ua.activity_id, ua.post_id, ua.course_id, ua.activity_type,
                   ua.activity_completed, p.post_title,
                   (SELECT meta_value FROM wp_postmeta WHERE post_id = ua.post_id AND meta_key = '_ld_certificate' LIMIT 1) as cert_id,
                   (SELECT activity_meta_value FROM wp_learndash_user_activity_meta WHERE activity_id = ua.activity_id AND activity_meta_key = 'percentage' LIMIT 1) as percentage,
                   (SELECT activity_meta_value FROM wp_learndash_user_activity_meta WHERE activity_id = ua.activity_id AND activity_meta_key = 'points' LIMIT 1) as points,
                   (SELECT activity_meta_value FROM wp_learndash_user_activity_meta WHERE activity_id = ua.activity_id AND activity_meta_key = 'total_points' LIMIT 1) as total_points,
                   (SELECT activity_meta_value FROM wp_learndash_user_activity_meta WHERE activity_id = ua.activity_id AND activity_meta_key = 'pass' LIMIT 1) as pass_status
            FROM wp_learndash_user_activity ua
            JOIN wp_posts p ON ua.post_id = p.ID
            WHERE ua.user_id = :userId
            AND ua.activity_type = 'quiz'
            AND ua.activity_status = 1
            AND ua.activity_completed IS NOT NULL
            AND ua.activity_completed > 0
            AND EXISTS (
                SELECT 1 FROM wp_learndash_user_activity_meta uam
                WHERE uam.activity_id = ua.activity_id
                AND uam.activity_meta_key = 'pass'
                AND uam.activity_meta_value = '1'
            )
            ORDER BY ua.activity_completed DESC
            """;

        String countSql = """
            SELECT COUNT(*) FROM wp_learndash_user_activity ua
            WHERE ua.user_id = :userId
            AND ua.activity_type = 'quiz'
            AND ua.activity_status = 1
            AND ua.activity_completed IS NOT NULL
            AND ua.activity_completed > 0
            AND EXISTS (
                SELECT 1 FROM wp_learndash_user_activity_meta uam
                WHERE uam.activity_id = ua.activity_id
                AND uam.activity_meta_key = 'pass'
                AND uam.activity_meta_value = '1'
            )
            """;

        Query countQuery = entityManager.createNativeQuery(countSql);
        countQuery.setParameter("userId", userId);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("userId", userId);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<UserCertificateResponse> certificates = results.stream()
                .map(this::mapToCertificateResponse)
                .toList();

        Page<UserCertificateResponse> page = new PageImpl<>(certificates, pageable, total);
        return PageResponse.of(page);
    }

    @Override
    public UserCertificateResponse getCertificateDetail(Long userId, Long activityId) {
        String sql = """
            SELECT ua.activity_id, ua.post_id, ua.course_id, ua.activity_type,
                   ua.activity_completed, p.post_title,
                   (SELECT meta_value FROM wp_postmeta WHERE post_id = ua.post_id AND meta_key = '_ld_certificate' LIMIT 1) as cert_id,
                   (SELECT activity_meta_value FROM wp_learndash_user_activity_meta WHERE activity_id = ua.activity_id AND activity_meta_key = 'percentage' LIMIT 1) as percentage,
                   (SELECT activity_meta_value FROM wp_learndash_user_activity_meta WHERE activity_id = ua.activity_id AND activity_meta_key = 'points' LIMIT 1) as points,
                   (SELECT activity_meta_value FROM wp_learndash_user_activity_meta WHERE activity_id = ua.activity_id AND activity_meta_key = 'total_points' LIMIT 1) as total_points,
                   (SELECT activity_meta_value FROM wp_learndash_user_activity_meta WHERE activity_id = ua.activity_id AND activity_meta_key = 'pass' LIMIT 1) as pass_status
            FROM wp_learndash_user_activity ua
            JOIN wp_posts p ON ua.post_id = p.ID
            WHERE ua.user_id = :userId
            AND ua.activity_id = :activityId
            AND ua.activity_status = 1
            AND ua.activity_completed IS NOT NULL
            AND ua.activity_completed > 0
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("userId", userId);
        query.setParameter("activityId", activityId);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        if (results.isEmpty()) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }

        UserCertificateResponse response = mapToCertificateResponse(results.get(0));

        // Get certificate content if available
        if (response.getCertificateId() != null && response.getCertificateId() > 0) {
            certificateRepository.findByIdAndStatus(response.getCertificateId(), PostStatus.PUBLISH)
                    .ifPresent(cert -> response.setContent(cert.getContent()));
        }

        return response;
    }

    @Override
    @Cacheable(value = "userCertificateStats", key = "#userId")
    public Map<String, Object> getCertificateStats(Long userId) {
        String sql = """
            SELECT
                SUM(CASE WHEN ua.activity_type = 'course' THEN 1 ELSE 0 END) as course_count,
                SUM(CASE WHEN ua.activity_type = 'quiz' THEN 1 ELSE 0 END) as quiz_count
            FROM wp_learndash_user_activity ua
            WHERE ua.user_id = :userId
            AND ua.activity_status = 1
            AND ua.activity_completed IS NOT NULL
            AND ua.activity_completed > 0
            AND (
                (ua.activity_type = 'course')
                OR
                (ua.activity_type = 'quiz' AND EXISTS (
                    SELECT 1 FROM wp_learndash_user_activity_meta uam
                    WHERE uam.activity_id = ua.activity_id
                    AND uam.activity_meta_key = 'pass'
                    AND uam.activity_meta_value = '1'
                ))
            )
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("userId", userId);

        Object[] result = (Object[]) query.getSingleResult();

        long courseCount = result[0] != null ? ((Number) result[0]).longValue() : 0;
        long quizCount = result[1] != null ? ((Number) result[1]).longValue() : 0;

        return Map.of(
                "totalCertificates", courseCount + quizCount,
                "courseCertificates", courseCount,
                "quizCertificates", quizCount
        );
    }

    @Override
    public boolean hasCertificate(Long userId, Long postId, String type) {
        String sql = """
            SELECT COUNT(*) FROM wp_learndash_user_activity ua
            WHERE ua.user_id = :userId
            AND ua.post_id = :postId
            AND ua.activity_type = :type
            AND ua.activity_status = 1
            AND ua.activity_completed IS NOT NULL
            AND ua.activity_completed > 0
            """;

        if ("quiz".equals(type)) {
            sql += """
                AND EXISTS (
                    SELECT 1 FROM wp_learndash_user_activity_meta uam
                    WHERE uam.activity_id = ua.activity_id
                    AND uam.activity_meta_key = 'pass'
                    AND uam.activity_meta_value = '1'
                )
                """;
        }

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("userId", userId);
        query.setParameter("postId", postId);
        query.setParameter("type", type);

        long count = ((Number) query.getSingleResult()).longValue();
        return count > 0;
    }

    private UserCertificateResponse mapToCertificateResponse(Object[] row) {
        Long activityId = row[0] != null ? ((Number) row[0]).longValue() : null;
        Long postId = row[1] != null ? ((Number) row[1]).longValue() : null;
        Long courseId = row[2] != null ? ((Number) row[2]).longValue() : null;
        String activityType = (String) row[3];
        Long completedTimestamp = row[4] != null ? ((Number) row[4]).longValue() : null;
        String postTitle = (String) row[5];
        Long certId = null;
        if (row[6] != null) {
            try {
                certId = Long.parseLong(row[6].toString());
            } catch (NumberFormatException ignored) {}
        }
        Double percentage = null;
        if (row[7] != null) {
            try {
                percentage = Double.parseDouble(row[7].toString());
            } catch (NumberFormatException ignored) {}
        }
        Integer points = null;
        if (row[8] != null) {
            try {
                points = Integer.parseInt(row[8].toString());
            } catch (NumberFormatException ignored) {}
        }
        Integer totalPoints = null;
        if (row[9] != null) {
            try {
                totalPoints = Integer.parseInt(row[9].toString());
            } catch (NumberFormatException ignored) {}
        }

        LocalDateTime earnedAt = null;
        if (completedTimestamp != null && completedTimestamp > 0) {
            earnedAt = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(completedTimestamp),
                    ZoneId.of(TIMEZONE)
            );
        }

        // Get certificate title if certId exists
        String certificateTitle = null;
        if (certId != null && certId > 0) {
            certificateTitle = certificateRepository.findByIdAndStatus(certId, PostStatus.PUBLISH)
                    .map(Certificate::getTitle)
                    .orElse(null);
        }

        // Get course title for quiz certificates
        String courseTitle = null;
        if ("quiz".equals(activityType) && courseId != null && courseId > 0) {
            String courseSql = "SELECT post_title FROM wp_posts WHERE ID = :courseId";
            Query courseQuery = entityManager.createNativeQuery(courseSql);
            courseQuery.setParameter("courseId", courseId);
            try {
                courseTitle = (String) courseQuery.getSingleResult();
            } catch (Exception ignored) {}
        }

        return UserCertificateResponse.builder()
                .id(activityId)
                .type(activityType != null ? activityType.toUpperCase() : null)
                .certificateId(certId)
                .certificateTitle(certificateTitle)
                .postId(postId)
                .postTitle(postTitle)
                .courseId(courseId)
                .courseTitle(courseTitle)
                .score(percentage)
                .points(points)
                .totalPoints(totalPoints)
                .earnedAt(earnedAt)
                .build();
    }
}
