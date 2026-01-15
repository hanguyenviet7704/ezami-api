package com.hth.udecareer.controllers;

import com.hth.udecareer.annotation.ApiPrefixController;
import com.hth.udecareer.entities.*;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.enums.PostStatus;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.model.response.UserCertificateResponse;
import com.hth.udecareer.model.response.UserResponse;
import com.hth.udecareer.repository.*;
import com.hth.udecareer.service.CertificateBadgeIntegrationService;
import com.hth.udecareer.service.CertificateService;
import com.hth.udecareer.service.UserCertificateService;
import com.hth.udecareer.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@ApiPrefixController
@RequiredArgsConstructor
@Tag(name = "LearnDash", description = "LearnDash LMS APIs - Topics, Certificates, Groups, Assignments, Quiz Leaderboard")
public class LearnDashController {

    private final TopicRepository topicRepository;
    private final CertificateService certificateService;
    private final LdGroupRepository groupRepository;
    private final AssignmentRepository assignmentRepository;
    private final LdQuizToplistRepository toplistRepository;
    private final LdQuizPrerequisiteRepository prerequisiteRepository;
    private final LdQuizCategoryRepository quizCategoryRepository;
    private final UserService userService;
    private final UserCertificateService userCertificateService;
    private final CertificateBadgeIntegrationService certificateBadgeIntegrationService;

    // ==================== TOPICS ====================

    @GetMapping("/learndash/topics/lesson/{lessonId}")
    @Operation(summary = "Get topics for a lesson")
    public ResponseEntity<List<Topic>> getTopicsByLesson(@PathVariable Long lessonId) {
        return ResponseEntity.ok(topicRepository.findPublishedByLessonId(lessonId));
    }

    @GetMapping("/learndash/topics/{id}")
    @Operation(summary = "Get topic by ID")
    public ResponseEntity<Topic> getTopicById(@PathVariable Long id) {
        return ResponseEntity.ok(
                topicRepository.findByIdAndStatus(id, PostStatus.PUBLISH)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND))
        );
    }

    // ==================== CERTIFICATES ====================

    @GetMapping("/learndash/certificates")
    @Operation(summary = "Get all published certificates (cached)")
    public ResponseEntity<List<Certificate>> getAllCertificates() {
        return ResponseEntity.ok(certificateService.getAllPublished());
    }

    @GetMapping("/learndash/certificates/{id}")
    @Operation(summary = "Get certificate by ID (cached)")
    public ResponseEntity<Certificate> getCertificateById(@PathVariable Long id) {
        return ResponseEntity.ok(certificateService.getById(id));
    }

    // ==================== GROUPS ====================

    @GetMapping("/learndash/groups")
    @Operation(summary = "Get all published groups")
    public ResponseEntity<List<LdGroup>> getAllGroups() {
        return ResponseEntity.ok(groupRepository.findAllPublished());
    }

    @GetMapping("/learndash/groups/{id}")
    @Operation(summary = "Get group by ID")
    public ResponseEntity<LdGroup> getGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(
                groupRepository.findByIdAndStatus(id, PostStatus.PUBLISH)
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND))
        );
    }

    @GetMapping("/learndash/groups/search")
    @Operation(summary = "Search groups by title")
    public ResponseEntity<List<LdGroup>> searchGroups(@RequestParam String keyword) {
        return ResponseEntity.ok(groupRepository.searchByTitle(keyword));
    }

    // ==================== ASSIGNMENTS ====================

    @GetMapping("/learndash/assignments/my")
    @Operation(summary = "Get my assignments", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<Assignment>> getMyAssignments(
            @Parameter(hidden = true) Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UserResponse user = userService.findByEmail(principal.getName());
        if (user == null) throw new AppException(ErrorCode.EMAIL_USER_NOT_FOUND);

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(assignmentRepository.findByAuthorIdOrderByDateDesc(user.getId(), pageable));
    }

    @GetMapping("/learndash/assignments/lesson/{lessonId}")
    @Operation(summary = "Get assignments for a lesson")
    public ResponseEntity<Page<Assignment>> getAssignmentsByLesson(
            @PathVariable Long lessonId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(assignmentRepository.findByParentIdOrderByDateDesc(lessonId, pageable));
    }

    @GetMapping("/learndash/assignments/{id}")
    @Operation(summary = "Get assignment by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Assignment> getAssignmentById(
            @Parameter(hidden = true) Principal principal,
            @PathVariable Long id
    ) {
        UserResponse user = userService.findByEmail(principal.getName());
        if (user == null) throw new AppException(ErrorCode.EMAIL_USER_NOT_FOUND);

        return ResponseEntity.ok(
                assignmentRepository.findByIdAndAuthorId(id, user.getId())
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND))
        );
    }

    @GetMapping("/learndash/assignments/my/stats")
    @Operation(summary = "Get my assignment stats", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> getMyAssignmentStats(
            @Parameter(hidden = true) Principal principal
    ) {
        UserResponse user = userService.findByEmail(principal.getName());
        if (user == null) throw new AppException(ErrorCode.EMAIL_USER_NOT_FOUND);

        long total = assignmentRepository.countByUserId(user.getId());
        long graded = assignmentRepository.countGradedByUserId(user.getId());

        return ResponseEntity.ok(Map.of(
                "total", total,
                "graded", graded,
                "pending", total - graded
        ));
    }

    // ==================== QUIZ LEADERBOARD (TOPLIST) ====================

    @GetMapping("/learndash/quiz/{quizId}/leaderboard")
    @Operation(summary = "Get quiz leaderboard")
    public ResponseEntity<List<LdQuizToplistEntity>> getQuizLeaderboard(
            @PathVariable Long quizId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(0, limit);
        return ResponseEntity.ok(toplistRepository.findTopByQuizId(quizId, pageable));
    }

    @GetMapping("/learndash/leaderboard/global")
    @Operation(summary = "Get global leaderboard across all quizzes")
    public ResponseEntity<List<LdQuizToplistEntity>> getGlobalLeaderboard(
            @RequestParam(defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(0, limit);
        return ResponseEntity.ok(toplistRepository.findGlobalTop(pageable));
    }

    @GetMapping("/learndash/quiz/{quizId}/my-rank")
    @Operation(summary = "Get my rank in quiz leaderboard", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> getMyQuizRank(
            @Parameter(hidden = true) Principal principal,
            @PathVariable Long quizId
    ) {
        UserResponse user = userService.findByEmail(principal.getName());
        if (user == null) throw new AppException(ErrorCode.EMAIL_USER_NOT_FOUND);

        Integer bestScore = toplistRepository.findBestScoreByQuizAndUser(quizId, user.getId());
        if (bestScore == null) {
            return ResponseEntity.ok(Map.of(
                    "hasScore", false,
                    "rank", -1,
                    "bestScore", 0
            ));
        }

        long betterScores = toplistRepository.countBetterScores(quizId, bestScore);

        return ResponseEntity.ok(Map.of(
                "hasScore", true,
                "rank", betterScores + 1,
                "bestScore", bestScore
        ));
    }

    @GetMapping("/learndash/leaderboard/my")
    @Operation(summary = "Get my leaderboard entries", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<LdQuizToplistEntity>> getMyLeaderboardEntries(
            @Parameter(hidden = true) Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UserResponse user = userService.findByEmail(principal.getName());
        if (user == null) throw new AppException(ErrorCode.EMAIL_USER_NOT_FOUND);

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(toplistRepository.findByUserIdOrderByDateDesc(user.getId(), pageable));
    }

    // ==================== QUIZ PREREQUISITES ====================

    @GetMapping("/learndash/quiz/{quizId}/prerequisites")
    @Operation(summary = "Get quiz prerequisites")
    public ResponseEntity<List<Long>> getQuizPrerequisites(@PathVariable Long quizId) {
        return ResponseEntity.ok(prerequisiteRepository.findPrerequisiteQuizIds(quizId));
    }

    // ==================== QUIZ CATEGORIES (LD Pro) ====================

    @GetMapping("/learndash/quiz-categories")
    @Operation(summary = "Get all LearnDash Pro quiz categories")
    public ResponseEntity<List<LdQuizCategoryEntity>> getQuizCategories() {
        return ResponseEntity.ok(quizCategoryRepository.findAllByOrderByCategoryNameAsc());
    }

    // ==================== USER CERTIFICATES (EARNED) ====================

    @GetMapping("/learndash/my-certificates")
    @Operation(summary = "Get all certificates earned by the current user",
            description = "Returns paginated list of all certificates (course and quiz) that the user has earned",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PageResponse<UserCertificateResponse>> getMyCertificates(
            @Parameter(hidden = true) Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UserResponse user = userService.findByEmail(principal.getName());
        if (user == null) throw new AppException(ErrorCode.EMAIL_USER_NOT_FOUND);

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userCertificateService.getUserCertificates(user.getId(), pageable));
    }

    @GetMapping("/learndash/my-certificates/courses")
    @Operation(summary = "Get course completion certificates for the current user",
            description = "Returns paginated list of certificates earned from completing courses",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PageResponse<UserCertificateResponse>> getMyCourseCertificates(
            @Parameter(hidden = true) Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UserResponse user = userService.findByEmail(principal.getName());
        if (user == null) throw new AppException(ErrorCode.EMAIL_USER_NOT_FOUND);

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userCertificateService.getCourseCertificates(user.getId(), pageable));
    }

    @GetMapping("/learndash/my-certificates/quizzes")
    @Operation(summary = "Get quiz certificates for the current user",
            description = "Returns paginated list of certificates earned from passing quizzes",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PageResponse<UserCertificateResponse>> getMyQuizCertificates(
            @Parameter(hidden = true) Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UserResponse user = userService.findByEmail(principal.getName());
        if (user == null) throw new AppException(ErrorCode.EMAIL_USER_NOT_FOUND);

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userCertificateService.getQuizCertificates(user.getId(), pageable));
    }

    @GetMapping("/learndash/my-certificates/{activityId}")
    @Operation(summary = "Get details of a specific earned certificate",
            description = "Returns detailed certificate information including content for rendering",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserCertificateResponse> getMyCertificateDetail(
            @Parameter(hidden = true) Principal principal,
            @PathVariable Long activityId
    ) {
        UserResponse user = userService.findByEmail(principal.getName());
        if (user == null) throw new AppException(ErrorCode.EMAIL_USER_NOT_FOUND);

        return ResponseEntity.ok(userCertificateService.getCertificateDetail(user.getId(), activityId));
    }

    @GetMapping("/learndash/my-certificates/stats")
    @Operation(summary = "Get certificate statistics for the current user",
            description = "Returns counts of total, course, and quiz certificates",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> getMyCertificateStats(
            @Parameter(hidden = true) Principal principal
    ) {
        UserResponse user = userService.findByEmail(principal.getName());
        if (user == null) throw new AppException(ErrorCode.EMAIL_USER_NOT_FOUND);

        return ResponseEntity.ok(userCertificateService.getCertificateStats(user.getId()));
    }

    @GetMapping("/learndash/my-certificates/check")
    @Operation(summary = "Check if user has earned a certificate for a specific course/quiz",
            description = "Returns true if the user has completed the course or passed the quiz",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> checkMyCertificate(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "Course or Quiz post ID") @RequestParam Long postId,
            @Parameter(description = "Type: 'course' or 'quiz'") @RequestParam String type
    ) {
        UserResponse user = userService.findByEmail(principal.getName());
        if (user == null) throw new AppException(ErrorCode.EMAIL_USER_NOT_FOUND);

        boolean hasCertificate = userCertificateService.hasCertificate(user.getId(), postId, type);
        return ResponseEntity.ok(Map.of(
                "postId", postId,
                "type", type,
                "hasCertificate", hasCertificate
        ));
    }

    // ==================== LEARNING ACHIEVEMENTS (INTEGRATION) ====================

    @GetMapping("/learndash/my-achievements")
    @Operation(summary = "Get learning achievements summary",
            description = "Returns combined stats of certificates, certificate-based badges, and next milestones",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> getMyLearningAchievements(
            @Parameter(hidden = true) Principal principal
    ) {
        UserResponse user = userService.findByEmail(principal.getName());
        if (user == null) throw new AppException(ErrorCode.EMAIL_USER_NOT_FOUND);

        return ResponseEntity.ok(certificateBadgeIntegrationService.getLearningAchievementsSummary(user.getId()));
    }

    @PostMapping("/learndash/my-achievements/check-badges")
    @Operation(summary = "Check and award certificate-based badges",
            description = "Checks if user qualifies for any certificate-based badges and awards them automatically",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> checkAndAwardBadges(
            @Parameter(hidden = true) Principal principal
    ) {
        UserResponse user = userService.findByEmail(principal.getName());
        if (user == null) throw new AppException(ErrorCode.EMAIL_USER_NOT_FOUND);

        var awardedBadges = certificateBadgeIntegrationService.checkAndAwardCertificateBadges(user.getId());

        return ResponseEntity.ok(Map.of(
                "awarded", !awardedBadges.isEmpty(),
                "count", awardedBadges.size(),
                "badges", awardedBadges
        ));
    }
}
