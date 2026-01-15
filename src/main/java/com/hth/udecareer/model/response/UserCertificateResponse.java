package com.hth.udecareer.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for user earned certificates
 * Represents a certificate that a user has earned through course/quiz completion
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCertificateResponse {

    private Long id;

    /**
     * Certificate type: COURSE or QUIZ
     */
    private String type;

    /**
     * Certificate template ID (from sfwd-certificates)
     */
    private Long certificateId;

    /**
     * Certificate title/name
     */
    private String certificateTitle;

    /**
     * Related post ID (course or quiz ID)
     */
    private Long postId;

    /**
     * Related post title (course or quiz name)
     */
    private String postTitle;

    /**
     * Course ID (for quiz certificates)
     */
    private Long courseId;

    /**
     * Course title (for quiz certificates)
     */
    private String courseTitle;

    /**
     * Score/percentage achieved (for quiz certificates)
     */
    private Double score;

    /**
     * Points earned (for quiz certificates)
     */
    private Integer points;

    /**
     * Total points possible (for quiz certificates)
     */
    private Integer totalPoints;

    /**
     * Date when certificate was earned
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime earnedAt;

    /**
     * Certificate link/URL (if available)
     */
    private String certificateLink;

    /**
     * Certificate content/HTML (for rendering)
     */
    private String content;
}
