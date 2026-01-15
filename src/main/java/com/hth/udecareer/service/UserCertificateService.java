package com.hth.udecareer.service;

import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.model.response.UserCertificateResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * Service for managing user earned certificates
 */
public interface UserCertificateService {

    /**
     * Get all certificates earned by a user (both course and quiz)
     * @param userId User ID
     * @param pageable Pagination info
     * @return Page of user certificates
     */
    PageResponse<UserCertificateResponse> getUserCertificates(Long userId, Pageable pageable);

    /**
     * Get course completion certificates for a user
     * @param userId User ID
     * @param pageable Pagination info
     * @return Page of course certificates
     */
    PageResponse<UserCertificateResponse> getCourseCertificates(Long userId, Pageable pageable);

    /**
     * Get quiz certificates for a user (passed quizzes)
     * @param userId User ID
     * @param pageable Pagination info
     * @return Page of quiz certificates
     */
    PageResponse<UserCertificateResponse> getQuizCertificates(Long userId, Pageable pageable);

    /**
     * Get certificate details for a specific activity
     * @param userId User ID
     * @param activityId Activity ID from wp_learndash_user_activity
     * @return Certificate details
     */
    UserCertificateResponse getCertificateDetail(Long userId, Long activityId);

    /**
     * Get certificate statistics for a user
     * @param userId User ID
     * @return Statistics map with counts
     */
    Map<String, Object> getCertificateStats(Long userId);

    /**
     * Check if user has earned a certificate for a specific course/quiz
     * @param userId User ID
     * @param postId Course or Quiz post ID
     * @param type "course" or "quiz"
     * @return true if certificate is earned
     */
    boolean hasCertificate(Long userId, Long postId, String type);
}
