package com.hth.udecareer.service;

import com.hth.udecareer.entities.User;
import com.hth.udecareer.entities.UserCourseEntity;
import com.hth.udecareer.entities.UserPurchasedEntity;
import com.hth.udecareer.entities.QuizCategoryEntity;
import com.hth.udecareer.model.webhook.ContentAccessRequest;
import com.hth.udecareer.model.webhook.ContentAccessResponse;
import com.hth.udecareer.repository.PostMetaRepository;
import com.hth.udecareer.repository.QuizCategoryRepository;
import com.hth.udecareer.repository.UserCourseRepository;
import com.hth.udecareer.repository.UserPurchasedRepository;
import com.hth.udecareer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentAccessService {

    private final UserRepository userRepository;
    private final UserPurchasedRepository userPurchasedRepository;
    private final UserCourseRepository userCourseRepository;
    private final QuizCategoryRepository quizCategoryRepository;
    private final PostMetaRepository postMetaRepository;

    /**
     * Check whether a user (identified by userId or userEmail) has access to content
     * supported types: course, lesson, quiz, topic, post.
     */
    public ContentAccessResponse hasAccess(ContentAccessRequest req) {
        Long userId = req.getUserId();
        String userEmail = req.getUserEmail();
        if (userId == null && (userEmail == null || userEmail.isBlank())) {
            return new ContentAccessResponse(false, "Missing userId or userEmail", null);
        }

        Optional<User> userOptional = Optional.empty();
        if (userId != null) userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty() && userEmail != null && !userEmail.isBlank()) {
            userOptional = userRepository.findByEmail(userEmail);
        }

        if (userOptional.isEmpty()) {
            log.debug("User not found for access check: userId={}, userEmail={}", userId, userEmail);
            return new ContentAccessResponse(false, "User not found", null);
        }

        Long resolvedUserId = userOptional.get().getId();

        String type = req.getContentType();
        String contentId = req.getContentId();

        try {
            switch (type.toLowerCase()) {
                case "course":
                    return checkCourseAccess(resolvedUserId, contentId);
                case "lesson":
                    return checkLessonAccess(resolvedUserId, contentId);
                case "quiz":
                    return checkQuizAccess(resolvedUserId, contentId);
                case "topic":
                    return checkTopicAccess(resolvedUserId, contentId);
                case "post":
                    return checkPostAccess(resolvedUserId, contentId);
                default:
                    return new ContentAccessResponse(false, "Unsupported content type: " + type, null);
            }
        } catch (Exception e) {
            log.error("Error checking content access: {}", e.getMessage(), e);
            return new ContentAccessResponse(false, "Internal error", null);
        }
    }

    private ContentAccessResponse checkCourseAccess(Long userId, String contentId) {
        // contentId is likely the course id (numeric)
        try {
            Long courseId = Long.parseLong(contentId);
            Optional<UserCourseEntity> opt = userCourseRepository.findByUserIdAndCourseId(userId, courseId);
            if (opt.isPresent()) {
                UserCourseEntity ue = opt.get();
                LocalDateTime expires = ue.getEndTime() == null ? null : LocalDateTime.ofEpochSecond(ue.getEndTime(), 0, java.time.ZoneOffset.UTC);
                return new ContentAccessResponse(true, "Has course access", expires);
            }
            return new ContentAccessResponse(false, "No course access", null);
        } catch (NumberFormatException ex) {
            return new ContentAccessResponse(false, "Invalid course id", null);
        }
    }

    private ContentAccessResponse checkLessonAccess(Long userId, String contentId) {
        // Try to fetch course id via postmeta for the lesson
        try {
            Long lessonId = Long.parseLong(contentId);
            var pm = postMetaRepository.findByPostIdAndMetaKey(lessonId, "course_id");
            if (pm == null || pm.getMetaValue() == null) {
                return new ContentAccessResponse(false, "Lesson does not belong to a course or no course metadata", null);
            }
            Long courseId = Long.valueOf(pm.getMetaValue());
            return checkCourseAccess(userId, courseId.toString());
        } catch (NumberFormatException e) {
            return new ContentAccessResponse(false, "Invalid lesson id", null);
        }
    }

    private ContentAccessResponse checkQuizAccess(Long userId, String contentId) {
        // contentId may be either numeric category id or category code
        try {
            Long catId = Long.parseLong(contentId);
            Optional<QuizCategoryEntity> opt = quizCategoryRepository.findById(catId);
            if (opt.isPresent()) {
                String code = opt.get().getCode();
                boolean ok = userPurchasedRepository.existsByUserIdAndCategoryCodeAndIsPurchased(userId, code, 1);
                return ok ? new ContentAccessResponse(true, "Has quiz access", null) : new ContentAccessResponse(false, "No quiz access", null);
            }
        } catch (NumberFormatException ignored) {
            // then treat as category code
            boolean ok = userPurchasedRepository.existsByUserIdAndCategoryCodeAndIsPurchased(userId, contentId, 1);
            return ok ? new ContentAccessResponse(true, "Has quiz access", null) : new ContentAccessResponse(false, "No quiz access", null);
        }
        return new ContentAccessResponse(false, "Quiz category not found", null);
    }

    private ContentAccessResponse checkTopicAccess(Long userId, String contentId) {
        // Topic may map to a category or Post meta; fallback to looking up UserPurchasedEntity
        boolean ok = userPurchasedRepository.existsByUserIdAndCategoryCodeAndIsPurchased(userId, "topic_" + contentId, 1);
        if (ok) return new ContentAccessResponse(true, "Has topic access", null);
        // fallback: check post access for topic ID
        ok = userPurchasedRepository.existsByUserIdAndCategoryCodeAndIsPurchased(userId, contentId, 1);
        return ok ? new ContentAccessResponse(true, "Has topic access", null) : new ContentAccessResponse(false, "No topic access", null);
    }

    private ContentAccessResponse checkPostAccess(Long userId, String contentId) {
        // Posts are wordpress posts. We will try direct 'post_{id}' and then 'id' match in UserPurchased
        boolean ok = userPurchasedRepository.existsByUserIdAndCategoryCodeAndIsPurchased(userId, "post_" + contentId, 1);
        if (ok) return new ContentAccessResponse(true, "Has post access", null);
        ok = userPurchasedRepository.existsByUserIdAndCategoryCodeAndIsPurchased(userId, contentId, 1);
        return ok ? new ContentAccessResponse(true, "Has post access", null) : new ContentAccessResponse(false, "No post access", null);
    }
}
