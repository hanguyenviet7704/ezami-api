package com.hth.udecareer.eil.service;

import com.hth.udecareer.eil.algorithm.QuestionSelector;
import com.hth.udecareer.eil.entities.*;
import com.hth.udecareer.eil.enums.DifficultyLevel;
import com.hth.udecareer.eil.enums.SessionStatus;
import com.hth.udecareer.eil.model.request.DiagnosticAnswerRequest;
import com.hth.udecareer.eil.model.request.DiagnosticStartRequest;
import com.hth.udecareer.eil.model.response.DiagnosticAnswerResponse;
import com.hth.udecareer.eil.model.response.DiagnosticHistoryResponse;
import com.hth.udecareer.eil.model.response.DiagnosticResultResponse;
import com.hth.udecareer.eil.model.response.DiagnosticSessionResponse;
import com.hth.udecareer.eil.repository.EilDiagnosticAnswerRepository;
import com.hth.udecareer.eil.repository.EilDiagnosticAttemptRepository;
import com.hth.udecareer.entities.QuestionEntity;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.QuestionResponse;
import com.hth.udecareer.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing diagnostic tests.
 * Diagnostic tests assess user's initial skill levels across all categories.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiagnosticService {

    private final EilDiagnosticAttemptRepository attemptRepository;
    private final EilDiagnosticAnswerRepository answerRepository;
    private final SkillService skillService;
    private final MasteryService masteryService;
    private final ReadinessService readinessService;
    private final QuestionSelector questionSelector;
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;

    @Value("${eil.diagnostic.default-questions:30}")
    private int defaultQuestions;

    @Value("${eil.diagnostic.timeout-minutes:60}")
    private int timeoutMinutes;

    /**
     * Start a new diagnostic test.
     * Supports two modes:
     * - CAREER_ASSESSMENT: Random questions across career path skills to determine user level
     * - CERTIFICATION_PRACTICE: Questions specific to a certification for practice
     */
    @Transactional
    public DiagnosticSessionResponse startDiagnostic(Long userId, DiagnosticStartRequest request) throws AppException {
        // Check if user has an in-progress diagnostic
        Optional<EilDiagnosticAttemptEntity> existingAttempt = attemptRepository
                .findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, SessionStatus.IN_PROGRESS.getCode());

        if (existingAttempt.isPresent()) {
            // Return activeSessionId in error response so frontend can resume
            Map<String, String> errorData = new HashMap<>();
            errorData.put("activeSessionId", existingAttempt.get().getSessionId());
            throw new AppException(ErrorCode.EIL_DIAGNOSTIC_ALREADY_IN_PROGRESS, errorData);
        }

        // Generate session ID
        String sessionId = UUID.randomUUID().toString();

        // Determine mode and test parameters
        String mode = request.getEffectiveMode();
        String certificationCode = request.getCertificationCode();
        String careerPath = request.getCareerPath();
        int totalQuestions = request.getQuestionCount() != null ? request.getQuestionCount() : defaultQuestions;

        // Determine test type (for backward compatibility)
        String testType = certificationCode != null ? certificationCode :
                         (request.getTestType() != null ? request.getTestType() : "TOEIC");

        // Select questions based on mode
        List<Long> selectedQuestions;
        if ("CERTIFICATION_PRACTICE".equals(mode) && certificationCode != null) {
            // Get questions for specific certification
            selectedQuestions = selectQuestionsForCertification(certificationCode, totalQuestions);
            log.info("Selected {} questions for certification {} practice", selectedQuestions.size(), certificationCode);
        } else {
            // Career assessment - random questions across career path skills
            selectedQuestions = selectQuestionsForCareerAssessment(careerPath, request.getFocusCategories(), totalQuestions);
            log.info("Selected {} questions for career assessment (path: {})", selectedQuestions.size(), careerPath);
        }

        if (selectedQuestions.isEmpty()) {
            throw new AppException(ErrorCode.EIL_DIAGNOSTIC_NO_QUESTIONS);
        }

        // Build metadata with selected question IDs, mode, and certification info
        String metadataJson = buildMetadataJson(selectedQuestions, mode, certificationCode, careerPath);

        // Create attempt entity
        EilDiagnosticAttemptEntity attempt = EilDiagnosticAttemptEntity.builder()
                .userId(userId)
                .sessionId(sessionId)
                .testType(testType)
                .status(SessionStatus.IN_PROGRESS.getCode())
                .totalQuestions(selectedQuestions.size())
                .answeredQuestions(0)
                .startTime(LocalDateTime.now())
                .metadata(metadataJson)
                .build();

        attempt = attemptRepository.save(attempt);

        log.info("Started diagnostic session {} for user {} with {} questions (mode: {})",
                sessionId, userId, selectedQuestions.size(), mode);

        // Get ALL questions for the session
        List<QuestionResponse> allQuestions = getAllQuestions(selectedQuestions);

        // Build adaptive state for UI
        DiagnosticSessionResponse.AdaptiveState adaptiveState = buildAdaptiveState(0, 0, selectedQuestions.size());

        return DiagnosticSessionResponse.builder()
                .sessionId(sessionId)
                .totalQuestions(selectedQuestions.size())
                .currentQuestion(1)
                .status(SessionStatus.IN_PROGRESS.getCode())
                .startTime(attempt.getStartTime())
                .timeoutMinutes(timeoutMinutes)
                .firstQuestion(allQuestions.isEmpty() ? null : allQuestions.get(0)) // backward compatibility
                .questions(null) // DEPRECATED: Now using adaptive mode
                .testType(testType)
                .answeredQuestions(0)
                .mode(mode)
                .certificationCode(certificationCode)
                .flowMode("ADAPTIVE") // Always adaptive mode
                .adaptiveState(adaptiveState)
                .build();
    }

    /**
     * Select questions for a specific certification practice.
     */
    private List<Long> selectQuestionsForCertification(String certificationCode, int totalQuestions) {
        // Get skills associated with this certification
        Map<Long, List<Long>> questionsBySkill = skillService.getQuestionsGroupedBySkillForCertification(certificationCode);

        if (questionsBySkill.isEmpty()) {
            log.warn("No questions found for certification: {}", certificationCode);
            // Fallback to all questions
            questionsBySkill = skillService.getQuestionsGroupedBySkill();
        }

        int questionsPerSkill = Math.max(1, totalQuestions / Math.max(1, questionsBySkill.size()));
        return questionSelector.selectDiagnosticQuestions(questionsBySkill, questionsPerSkill, totalQuestions);
    }

    /**
     * Select questions for career assessment (determine user level).
     * Randomly selects from skills relevant to the career path.
     */
    private List<Long> selectQuestionsForCareerAssessment(String careerPath, List<String> focusCategories, int totalQuestions) {
        Map<Long, List<Long>> questionsBySkill;

        if (careerPath != null && !careerPath.isEmpty()) {
            // Get questions for skills in this career path
            questionsBySkill = skillService.getQuestionsGroupedBySkillForCareerPath(careerPath);
            log.info("Filtering questions for career path: {}", careerPath);
        } else if (focusCategories != null && !focusCategories.isEmpty()) {
            questionsBySkill = skillService.getQuestionsGroupedBySkillForCategories(focusCategories);
            log.info("Filtering questions for categories: {}", focusCategories);
        } else {
            // Random across all skills
            questionsBySkill = skillService.getQuestionsGroupedBySkill();
        }

        int questionsPerSkill = Math.max(1, totalQuestions / Math.max(1, questionsBySkill.size()));
        return questionSelector.selectDiagnosticQuestions(questionsBySkill, questionsPerSkill, totalQuestions);
    }

    /**
     * Get all question responses for a list of question IDs.
     */
    private List<QuestionResponse> getAllQuestions(List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<QuestionEntity> questions = questionRepository.findAllById(questionIds);

        // Create a map for ordering
        Map<Long, QuestionEntity> questionMap = questions.stream()
                .collect(Collectors.toMap(QuestionEntity::getId, q -> q));

        // Return in the same order as questionIds
        return questionIds.stream()
                .filter(questionMap::containsKey)
                .map(id -> QuestionResponse.from(questionMap.get(id)))
                .collect(Collectors.toList());
    }

    /**
     * Submit an answer for a diagnostic question.
     * Implements adaptive early termination:
     * - Auto-finishes if 3 consecutive wrong overall
     * - Skips skills after 2 consecutive wrong in same skill
     */
    @Transactional
    public DiagnosticAnswerResponse submitAnswer(Long userId, DiagnosticAnswerRequest request) throws AppException {
        // Find active session
        EilDiagnosticAttemptEntity attempt = attemptRepository
                .findBySessionId(request.getSessionId())
                .orElseThrow(() -> new AppException(ErrorCode.EIL_DIAGNOSTIC_SESSION_NOT_FOUND));

        // Validate ownership
        if (!attempt.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Check status
        if (!SessionStatus.IN_PROGRESS.getCode().equals(attempt.getStatus())) {
            throw new AppException(ErrorCode.EIL_DIAGNOSTIC_ALREADY_COMPLETED);
        }

        // Check if question already answered
        Optional<EilDiagnosticAnswerEntity> existingAnswer = answerRepository
                .findByDiagnosticAttemptIdAndQuestionId(attempt.getId(), request.getQuestionId());
        if (existingAnswer.isPresent()) {
            throw new AppException(ErrorCode.EIL_DIAGNOSTIC_QUESTION_ALREADY_ANSWERED);
        }

        // Get skill for this question (nullable - DB allows NULL after Jan 7, 2026 fix)
        Long skillId = skillService.getPrimarySkillIdForQuestion(request.getQuestionId());
        if (skillId == null) {
            log.warn("No skill mapping found for question {} - saving with NULL skill_id", request.getQuestionId());
        }

        // Determine correctness
        boolean isCorrect = determineCorrectness(request);
        int timeSpent = request.getTimeSpentSeconds() != null ? request.getTimeSpentSeconds() : 0;

        // Build user answer string
        String userAnswerStr = request.getAnswerData() != null
                ? request.getAnswerData().toString()
                : "";

        // Save answer - skillId can be NULL if no mapping exists (DB allows NULL)
        EilDiagnosticAnswerEntity answer = EilDiagnosticAnswerEntity.builder()
                .diagnosticAttemptId(attempt.getId())
                .questionId(request.getQuestionId())
                .skillId(skillId)
                .questionOrder(attempt.getAnsweredQuestions() + 1)
                .userAnswer(userAnswerStr)
                .isCorrect(isCorrect)
                .timeSpentSeconds(timeSpent)
                .answeredAt(LocalDateTime.now())
                .build();

        answerRepository.save(answer);

        // Update attempt stats
        attempt.setAnsweredQuestions(attempt.getAnsweredQuestions() + 1);
        attempt.setTimeSpentSeconds(attempt.getTimeSpentSeconds() + timeSpent);

        // Update mastery if skill found
        if (skillId != null) {
            masteryService.updateMastery(userId, skillId, isCorrect, DifficultyLevel.MEDIUM);
        }

        // ============= ADAPTIVE EARLY TERMINATION LOGIC =============

        // Parse metadata for tracking
        Map<String, Object> metadata = parseMetadata(attempt.getMetadata());
        int consecutiveWrong = getIntFromMetadata(metadata, "consecutiveWrong", 0);

        // Check if should auto-terminate (3 consecutive wrong)
        boolean shouldAutoTerminate = !isCorrect && consecutiveWrong >= 2; // Will be 3 after update
        String terminationReason = shouldAutoTerminate ? "3 consecutive wrong answers" : null;

        // Update metadata tracking
        String updatedMetadata = updateMetadataTracking(
                attempt.getMetadata(),
                isCorrect,
                skillId,
                shouldAutoTerminate,
                terminationReason
        );
        attempt.setMetadata(updatedMetadata);

        // Get updated counters for response
        Map<String, Object> updatedMetadataMap = parseMetadata(updatedMetadata);
        int newConsecutiveWrong = getIntFromMetadata(updatedMetadataMap, "consecutiveWrong", 0);
        int newSkillConsecutiveWrong = skillId != null
                ? getIntFromMetadata(updatedMetadataMap, "skillConsecutiveWrong." + skillId, 0)
                : 0;

        // Auto-terminate if conditions met
        if (shouldAutoTerminate) {
            attempt.setStatus(SessionStatus.COMPLETED.getCode());
            attempt.setEndTime(LocalDateTime.now());

            long correctCount = answerRepository.countCorrectByDiagnosticAttemptId(attempt.getId());
            double rawScore = attempt.getTotalQuestions() > 0
                    ? ((double) correctCount / attempt.getTotalQuestions()) * 100
                    : 0.0;
            attempt.setRawScore(BigDecimal.valueOf(rawScore));

            attemptRepository.save(attempt);

            log.info("Diagnostic session {} auto-terminated: {}", request.getSessionId(), terminationReason);

            // Build adaptive state for terminated session
            DiagnosticSessionResponse.AdaptiveState adaptiveState = buildAdaptiveState(
                    (int) correctCount,
                    attempt.getAnsweredQuestions(),
                    attempt.getTotalQuestions()
            );

            return DiagnosticAnswerResponse.builder()
                    .isCorrect(isCorrect)
                    .questionsAnswered(attempt.getAnsweredQuestions())
                    .questionsRemaining(0)
                    .nextQuestion(null)
                    .currentProgress(1.0)
                    .autoTerminated(true)
                    .terminationReason(terminationReason)
                    .consecutiveWrong(newConsecutiveWrong)
                    .skillConsecutiveWrong(newSkillConsecutiveWrong)
                    .currentSkillName(getSkillName(skillId))
                    .flowMode("ADAPTIVE")
                    .adaptiveState(adaptiveState)
                    .build();
        }

        // Check if normally complete
        boolean isComplete = attempt.getAnsweredQuestions() >= attempt.getTotalQuestions();

        if (isComplete) {
            attempt.setStatus(SessionStatus.COMPLETED.getCode());
            attempt.setEndTime(LocalDateTime.now());

            long correctCount = answerRepository.countCorrectByDiagnosticAttemptId(attempt.getId());
            double rawScore = attempt.getTotalQuestions() > 0
                    ? ((double) correctCount / attempt.getTotalQuestions()) * 100
                    : 0.0;
            attempt.setRawScore(BigDecimal.valueOf(rawScore));
        }

        attemptRepository.save(attempt);

        // Get next question if not complete
        QuestionResponse nextQuestion = null;
        String nextSkillName = null;
        int nextSkillConsecutiveWrong = 0;

        if (!isComplete) {
            // Find next unanswered question from non-terminated skill
            List<Long> questionIds = getQuestionIdsFromMetadata(updatedMetadataMap);
            Set<Long> terminatedSkills = getTerminatedSkillsFromMetadata(updatedMetadataMap);
            Set<Long> answeredQuestionIds = getAnsweredQuestionIds(attempt.getId());

            for (Long qId : questionIds) {
                if (answeredQuestionIds.contains(qId)) {
                    continue;
                }

                Long nextSkillId = skillService.getPrimarySkillIdForQuestion(qId);
                if (nextSkillId != null && terminatedSkills.contains(nextSkillId)) {
                    continue; // Skip terminated skill
                }

                // Found next question
                nextQuestion = questionRepository.findById(qId)
                        .map(QuestionResponse::from)
                        .orElse(null);

                if (nextQuestion != null && nextSkillId != null) {
                    nextSkillName = getSkillName(nextSkillId);
                    nextSkillConsecutiveWrong = getIntFromMetadata(
                            updatedMetadataMap,
                            "skillConsecutiveWrong." + nextSkillId,
                            0
                    );
                }
                break;
            }

            // If no valid next question, auto-complete
            if (nextQuestion == null) {
                attempt.setStatus(SessionStatus.COMPLETED.getCode());
                attempt.setEndTime(LocalDateTime.now());
                long correctCount = answerRepository.countCorrectByDiagnosticAttemptId(attempt.getId());
                double rawScore = attempt.getTotalQuestions() > 0
                        ? ((double) correctCount / attempt.getTotalQuestions()) * 100
                        : 0.0;
                attempt.setRawScore(BigDecimal.valueOf(rawScore));
                attemptRepository.save(attempt);
                log.info("Diagnostic session {} auto-completed: all skills exhausted", request.getSessionId());
            }
        }

        // Calculate progress
        int questionsAnswered = attempt.getAnsweredQuestions();
        int questionsRemaining = attempt.getTotalQuestions() - questionsAnswered;
        double progress = attempt.getTotalQuestions() > 0
                ? (double) questionsAnswered / attempt.getTotalQuestions()
                : 0.0;

        // Calculate adaptive state for UI
        long correctCount = answerRepository.countCorrectByDiagnosticAttemptId(attempt.getId());
        DiagnosticSessionResponse.AdaptiveState adaptiveState = buildAdaptiveState(
                (int) correctCount,
                questionsAnswered,
                attempt.getTotalQuestions()
        );

        log.debug("Answer submitted for diagnostic {}: question={}, correct={}, consecutive={}, progress={}/{}",
                request.getSessionId(), request.getQuestionId(), isCorrect, newConsecutiveWrong,
                questionsAnswered, attempt.getTotalQuestions());

        return DiagnosticAnswerResponse.builder()
                .isCorrect(isCorrect)
                .questionsAnswered(questionsAnswered)
                .questionsRemaining(questionsRemaining)
                .nextQuestion(nextQuestion)
                .currentProgress(progress)
                .autoTerminated(false)
                .terminationReason(null)
                .consecutiveWrong(newConsecutiveWrong)
                .skillConsecutiveWrong(nextSkillConsecutiveWrong)
                .currentSkillName(nextSkillName)
                .flowMode("ADAPTIVE")
                .adaptiveState(adaptiveState)
                .build();
    }

    /**
     * Finish and get results for a diagnostic test.
     */
    @Transactional
    public DiagnosticResultResponse finishDiagnostic(Long userId, String sessionId) throws AppException {
        EilDiagnosticAttemptEntity attempt = attemptRepository
                .findBySessionId(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.EIL_DIAGNOSTIC_SESSION_NOT_FOUND));

        if (!attempt.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Mark as completed if still in progress
        if (SessionStatus.IN_PROGRESS.getCode().equals(attempt.getStatus())) {
            attempt.setStatus(SessionStatus.COMPLETED.getCode());
            attempt.setEndTime(LocalDateTime.now());

            // Calculate raw score
            long correctCount = answerRepository.countCorrectByDiagnosticAttemptId(attempt.getId());
            double rawScore = attempt.getTotalQuestions() > 0
                    ? ((double) correctCount / attempt.getTotalQuestions()) * 100
                    : 0.0;
            attempt.setRawScore(BigDecimal.valueOf(rawScore));

            attemptRepository.save(attempt);
        }

        // Get all answers
        List<EilDiagnosticAnswerEntity> answers = answerRepository.findByDiagnosticAttemptId(attempt.getId());

        // Calculate category scores
        Map<String, DiagnosticResultResponse.CategoryScore> categoryScores = calculateCategoryScores(answers);

        // Count correct answers
        long correctCount = answers.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count();

        // Calculate overall raw score
        double rawScore = attempt.getTotalQuestions() > 0
                ? ((double) correctCount / attempt.getTotalQuestions()) * 100
                : 0.0;

        // Get weak skills
        var weakSkills = masteryService.getWeakSkills(userId, 5);

        // Get skill results filtered by certification (FIX: Prevent cross-contamination between certifications)
        String certificationCode = extractCertificationCode(attempt);
        var skillResults = certificationCode != null
                ? masteryService.getSkillResultsByCertification(userId, certificationCode, 10)
                : masteryService.getAllSkillResults(userId, 10);

        // Calculate time spent
        Integer timeSpentSeconds = calculateTimeSpentSeconds(attempt);

        // Generate estimated level
        String estimatedLevel = estimateLevel(rawScore);

        // Generate personalized recommendations
        List<String> recommendations = generateRecommendations(categoryScores, rawScore, estimatedLevel);

        log.info("Diagnostic {} completed for user {}: rawScore={}, questions={}",
                sessionId, userId, rawScore, answers.size());

        // Create readiness snapshot after diagnostic completion
        try {
            String testType = attempt.getTestType();
            readinessService.createSnapshot(userId, testType, answers.size(), (int) correctCount);
        } catch (Exception e) {
            log.warn("Failed to create readiness snapshot for user {}: {}", userId, e.getMessage());
            // Don't fail the diagnostic completion if snapshot creation fails
        }

        return DiagnosticResultResponse.builder()
                .sessionId(sessionId)
                .status(attempt.getStatus())
                .totalQuestions(attempt.getTotalQuestions())
                .correctCount((int) correctCount)
                .rawScore(rawScore)
                .categoryScores(categoryScores)
                .skillResults(skillResults)
                .weakSkills(weakSkills)
                .estimatedLevel(estimatedLevel)
                .estimatedScoreMin(estimateScoreMin(rawScore))
                .estimatedScoreMax(estimateScoreMax(rawScore))
                .recommendations(recommendations)
                .completedAt(attempt.getEndTime())
                .timeSpentSeconds(timeSpentSeconds)
                .build();
    }

    /**
     * Get next adaptive question for diagnostic.
     * Implements early termination logic:
     * - Skips skills with 2 consecutive wrong answers
     * - Returns null if session should auto-terminate (3 consecutive wrong overall)
     */
    public DiagnosticAnswerResponse getNextQuestion(Long userId, String sessionId) throws AppException {
        EilDiagnosticAttemptEntity attempt = attemptRepository
                .findBySessionId(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.EIL_DIAGNOSTIC_SESSION_NOT_FOUND));

        if (!attempt.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (!SessionStatus.IN_PROGRESS.getCode().equals(attempt.getStatus())) {
            throw new AppException(ErrorCode.EIL_DIAGNOSTIC_ALREADY_COMPLETED);
        }

        // Parse metadata for tracking
        Map<String, Object> metadata = parseMetadata(attempt.getMetadata());
        List<Long> questionIds = getQuestionIdsFromMetadata(metadata);
        Set<Long> terminatedSkills = getTerminatedSkillsFromMetadata(metadata);
        Set<Long> answeredQuestionIds = getAnsweredQuestionIds(attempt.getId());

        // Find next unanswered question from non-terminated skill
        QuestionResponse nextQuestion = null;
        Long nextSkillId = null;

        for (Long questionId : questionIds) {
            if (answeredQuestionIds.contains(questionId)) {
                continue; // Already answered
            }

            Long skillId = skillService.getPrimarySkillIdForQuestion(questionId);
            if (skillId != null && terminatedSkills.contains(skillId)) {
                continue; // Skill terminated
            }

            // Found valid next question
            nextQuestion = questionRepository.findById(questionId)
                    .map(QuestionResponse::from)
                    .orElse(null);
            nextSkillId = skillId;
            break;
        }

        // Get current tracking state
        int consecutiveWrong = getIntFromMetadata(metadata, "consecutiveWrong", 0);
        int skillConsecutiveWrong = nextSkillId != null
                ? getIntFromMetadata(metadata, "skillConsecutiveWrong." + nextSkillId, 0)
                : 0;

        String skillName = null;
        if (nextSkillId != null) {
            try {
                EilSkillEntity skill = skillService.getSkillById(nextSkillId);
                skillName = skill.getName();
            } catch (AppException e) {
                log.warn("Skill not found: {}", nextSkillId);
            }
        }

        // Calculate adaptive state
        long correctCount = answerRepository.countCorrectByDiagnosticAttemptId(attempt.getId());
        DiagnosticSessionResponse.AdaptiveState adaptiveState = buildAdaptiveState(
                (int) correctCount,
                attempt.getAnsweredQuestions(),
                attempt.getTotalQuestions()
        );

        return DiagnosticAnswerResponse.builder()
                .questionsAnswered(attempt.getAnsweredQuestions())
                .questionsRemaining(attempt.getTotalQuestions() - attempt.getAnsweredQuestions())
                .nextQuestion(nextQuestion)
                .currentProgress((double) attempt.getAnsweredQuestions() / attempt.getTotalQuestions())
                .autoTerminated(false)
                .consecutiveWrong(consecutiveWrong)
                .skillConsecutiveWrong(skillConsecutiveWrong)
                .currentSkillName(skillName)
                .flowMode("ADAPTIVE")
                .adaptiveState(adaptiveState)
                .build();
    }

    /**
     * Get current diagnostic status.
     */
    public DiagnosticSessionResponse getDiagnosticStatus(Long userId, String sessionId) throws AppException {
        EilDiagnosticAttemptEntity attempt = attemptRepository
                .findBySessionId(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.EIL_DIAGNOSTIC_SESSION_NOT_FOUND));

        if (!attempt.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Get all questions from metadata
        List<Long> questionIds = parseQuestionIdsFromMetadata(attempt.getMetadata());
        List<QuestionResponse> allQuestions = getAllQuestions(questionIds);

        // Parse mode and certification from metadata
        String mode = parseStringFromMetadata(attempt.getMetadata(), "mode");
        String certificationCode = parseStringFromMetadata(attempt.getMetadata(), "certificationCode");

        // Get current question for backward compatibility
        int currentIndex = attempt.getAnsweredQuestions();
        QuestionResponse currentQuestion = (currentIndex < allQuestions.size()) ? allQuestions.get(currentIndex) : null;

        // Calculate adaptive state
        long correctCount = answerRepository.countCorrectByDiagnosticAttemptId(attempt.getId());
        DiagnosticSessionResponse.AdaptiveState adaptiveState = buildAdaptiveState(
                (int) correctCount,
                attempt.getAnsweredQuestions(),
                attempt.getTotalQuestions()
        );

        return DiagnosticSessionResponse.builder()
                .sessionId(sessionId)
                .totalQuestions(attempt.getTotalQuestions())
                .currentQuestion(attempt.getAnsweredQuestions() + 1)
                .status(attempt.getStatus())
                .startTime(attempt.getStartTime())
                .timeoutMinutes(timeoutMinutes)
                .firstQuestion(currentQuestion) // backward compatibility
                .questions(null) // DEPRECATED: Now using adaptive mode
                .answeredQuestions(attempt.getAnsweredQuestions())
                .testType(attempt.getTestType())
                .mode(mode)
                .certificationCode(certificationCode)
                .flowMode("ADAPTIVE")
                .adaptiveState(adaptiveState)
                .build();
    }

    /**
     * Get diagnostic result for a completed session (read-only, doesn't modify state).
     */
    public DiagnosticResultResponse getDiagnosticResult(Long userId, String sessionId) throws AppException {
        EilDiagnosticAttemptEntity attempt = attemptRepository
                .findBySessionId(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.EIL_DIAGNOSTIC_SESSION_NOT_FOUND));

        if (!attempt.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Only return results for completed diagnostics
        if (!SessionStatus.COMPLETED.getCode().equals(attempt.getStatus())) {
            throw new AppException(ErrorCode.EIL_DIAGNOSTIC_NOT_COMPLETED);
        }

        // Get all answers
        List<EilDiagnosticAnswerEntity> answers = answerRepository.findByDiagnosticAttemptId(attempt.getId());

        // Calculate category scores
        Map<String, DiagnosticResultResponse.CategoryScore> categoryScores = calculateCategoryScores(answers);

        // Count correct answers
        long correctCount = answers.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count();

        // Use stored raw score or calculate
        double rawScore = attempt.getRawScore() != null
                ? attempt.getRawScore().doubleValue()
                : (attempt.getTotalQuestions() > 0
                        ? ((double) correctCount / attempt.getTotalQuestions()) * 100
                        : 0.0);

        // Get weak skills
        var weakSkills = masteryService.getWeakSkills(userId, 5);

        // Get skill results filtered by certification (FIX: Prevent cross-contamination between certifications)
        String certificationCode = extractCertificationCode(attempt);
        var skillResults = certificationCode != null
                ? masteryService.getSkillResultsByCertification(userId, certificationCode, 10)
                : masteryService.getAllSkillResults(userId, 10);

        // Calculate time spent
        Integer timeSpentSeconds = calculateTimeSpentSeconds(attempt);

        // Determine estimated level
        String estimatedLevel = attempt.getEstimatedLevel() != null ? attempt.getEstimatedLevel() : estimateLevel(rawScore);

        // Generate personalized recommendations
        List<String> recommendations = generateRecommendations(categoryScores, rawScore, estimatedLevel);

        return DiagnosticResultResponse.builder()
                .sessionId(sessionId)
                .status(attempt.getStatus())
                .totalQuestions(attempt.getTotalQuestions())
                .correctCount((int) correctCount)
                .rawScore(rawScore)
                .categoryScores(categoryScores)
                .skillResults(skillResults)
                .weakSkills(weakSkills)
                .estimatedLevel(estimatedLevel)
                .estimatedScoreMin(attempt.getEstimatedScoreMin() != null ? attempt.getEstimatedScoreMin() : estimateScoreMin(rawScore))
                .estimatedScoreMax(attempt.getEstimatedScoreMax() != null ? attempt.getEstimatedScoreMax() : estimateScoreMax(rawScore))
                .recommendations(recommendations)
                .completedAt(attempt.getEndTime())
                .timeSpentSeconds(timeSpentSeconds)
                .build();
    }

    /**
     * Get active (IN_PROGRESS) diagnostic session for user.
     * Returns null if no active session exists.
     */
    public DiagnosticSessionResponse getActiveSession(Long userId) {
        Optional<EilDiagnosticAttemptEntity> activeAttempt = attemptRepository
                .findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, SessionStatus.IN_PROGRESS.getCode());

        if (activeAttempt.isEmpty()) {
            return null;
        }

        EilDiagnosticAttemptEntity attempt = activeAttempt.get();

        // Get all questions from metadata
        List<Long> questionIds = parseQuestionIdsFromMetadata(attempt.getMetadata());
        List<QuestionResponse> allQuestions = getAllQuestions(questionIds);

        // Parse mode and certification from metadata
        String mode = parseStringFromMetadata(attempt.getMetadata(), "mode");
        String certificationCode = parseStringFromMetadata(attempt.getMetadata(), "certificationCode");

        int currentIndex = attempt.getAnsweredQuestions();
        QuestionResponse currentQuestion = (currentIndex < allQuestions.size()) ? allQuestions.get(currentIndex) : null;

        // Calculate adaptive state
        long correctCount = answerRepository.countCorrectByDiagnosticAttemptId(attempt.getId());
        DiagnosticSessionResponse.AdaptiveState adaptiveState = buildAdaptiveState(
                (int) correctCount,
                attempt.getAnsweredQuestions(),
                attempt.getTotalQuestions()
        );

        return DiagnosticSessionResponse.builder()
                .sessionId(attempt.getSessionId())
                .totalQuestions(attempt.getTotalQuestions())
                .currentQuestion(attempt.getAnsweredQuestions() + 1)
                .answeredQuestions(attempt.getAnsweredQuestions())
                .status(attempt.getStatus())
                .startTime(attempt.getStartTime())
                .timeoutMinutes(timeoutMinutes)
                .testType(attempt.getTestType())
                .firstQuestion(currentQuestion) // backward compatibility
                .questions(null) // DEPRECATED: Now using adaptive mode
                .mode(mode)
                .certificationCode(certificationCode)
                .flowMode("ADAPTIVE")
                .adaptiveState(adaptiveState)
                .build();
    }

    /**
     * Get active session ID for user (used for error responses).
     * Returns null if no active session exists.
     */
    public String getActiveSessionId(Long userId) {
        Optional<EilDiagnosticAttemptEntity> activeAttempt = attemptRepository
                .findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, SessionStatus.IN_PROGRESS.getCode());
        return activeAttempt.map(EilDiagnosticAttemptEntity::getSessionId).orElse(null);
    }

    /**
     * Restart diagnostic test by abandoning any existing active session and starting a new one.
     */
    @Transactional
    public DiagnosticSessionResponse restartDiagnostic(Long userId, DiagnosticStartRequest request) throws AppException {
        // Abandon any existing active session
        abandonActiveSession(userId);

        // Start a new diagnostic session
        return startDiagnosticInternal(userId, request);
    }

    /**
     * Abandon a specific diagnostic session.
     */
    @Transactional
    public void abandonDiagnostic(Long userId, String sessionId) throws AppException {
        EilDiagnosticAttemptEntity attempt = attemptRepository
                .findBySessionId(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.EIL_DIAGNOSTIC_SESSION_NOT_FOUND));

        // Validate ownership
        if (!attempt.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Only abandon if still in progress
        if (SessionStatus.IN_PROGRESS.getCode().equals(attempt.getStatus())) {
            attempt.setStatus(SessionStatus.ABANDONED.getCode());
            attempt.setEndTime(LocalDateTime.now());
            attemptRepository.save(attempt);
            log.info("Abandoned diagnostic session {} for user {}", sessionId, userId);
        }
    }

    /**
     * Abandon any active diagnostic session for user.
     */
    private void abandonActiveSession(Long userId) {
        Optional<EilDiagnosticAttemptEntity> activeAttempt = attemptRepository
                .findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, SessionStatus.IN_PROGRESS.getCode());

        if (activeAttempt.isPresent()) {
            EilDiagnosticAttemptEntity attempt = activeAttempt.get();
            attempt.setStatus(SessionStatus.ABANDONED.getCode());
            attempt.setEndTime(LocalDateTime.now());
            attemptRepository.save(attempt);
            log.info("Abandoned active diagnostic session {} for user {}", attempt.getSessionId(), userId);
        }
    }

    /**
     * Internal method to start diagnostic without checking for existing session.
     * Used by restartDiagnostic after abandoning existing session.
     */
    private DiagnosticSessionResponse startDiagnosticInternal(Long userId, DiagnosticStartRequest request) throws AppException {
        // Generate session ID
        String sessionId = UUID.randomUUID().toString();

        // Determine mode and test parameters
        String mode = request.getEffectiveMode();
        String certificationCode = request.getCertificationCode();
        String careerPath = request.getCareerPath();
        int totalQuestions = request.getQuestionCount() != null ? request.getQuestionCount() : defaultQuestions;

        // Determine test type (for backward compatibility)
        String testType = certificationCode != null ? certificationCode :
                         (request.getTestType() != null ? request.getTestType() : "TOEIC");

        // Select questions based on mode
        List<Long> selectedQuestions;
        if ("CERTIFICATION_PRACTICE".equals(mode) && certificationCode != null) {
            // Get questions for specific certification
            selectedQuestions = selectQuestionsForCertification(certificationCode, totalQuestions);
            log.info("Selected {} questions for certification {} practice", selectedQuestions.size(), certificationCode);
        } else {
            // Career assessment - random questions across career path skills
            selectedQuestions = selectQuestionsForCareerAssessment(careerPath, request.getFocusCategories(), totalQuestions);
            log.info("Selected {} questions for career assessment (path: {})", selectedQuestions.size(), careerPath);
        }

        if (selectedQuestions.isEmpty()) {
            throw new AppException(ErrorCode.EIL_DIAGNOSTIC_NO_QUESTIONS);
        }

        // Build metadata with selected question IDs, mode, and certification info
        String metadataJson = buildMetadataJson(selectedQuestions, mode, certificationCode, careerPath);

        // Create attempt entity
        EilDiagnosticAttemptEntity attempt = EilDiagnosticAttemptEntity.builder()
                .userId(userId)
                .sessionId(sessionId)
                .testType(testType)
                .status(SessionStatus.IN_PROGRESS.getCode())
                .totalQuestions(selectedQuestions.size())
                .answeredQuestions(0)
                .startTime(LocalDateTime.now())
                .metadata(metadataJson)
                .build();

        attempt = attemptRepository.save(attempt);

        log.info("Started diagnostic session {} for user {} with {} questions (mode: {})",
                sessionId, userId, selectedQuestions.size(), mode);

        // Get ALL questions for the session
        List<QuestionResponse> allQuestions = getAllQuestions(selectedQuestions);

        // Build adaptive state for UI
        DiagnosticSessionResponse.AdaptiveState adaptiveState = buildAdaptiveState(0, 0, selectedQuestions.size());

        return DiagnosticSessionResponse.builder()
                .sessionId(sessionId)
                .totalQuestions(selectedQuestions.size())
                .currentQuestion(1)
                .answeredQuestions(0)
                .status(SessionStatus.IN_PROGRESS.getCode())
                .startTime(attempt.getStartTime())
                .timeoutMinutes(timeoutMinutes)
                .firstQuestion(allQuestions.isEmpty() ? null : allQuestions.get(0)) // backward compatibility
                .questions(null) // DEPRECATED: Now using adaptive mode
                .testType(testType)
                .mode(mode)
                .certificationCode(certificationCode)
                .flowMode("ADAPTIVE")
                .adaptiveState(adaptiveState)
                .build();
    }

    /**
     * Get paginated history of user's diagnostic attempts.
     */
    public DiagnosticHistoryResponse getDiagnosticHistory(Long userId, int page, int size) {
        // Get completed diagnostics
        List<EilDiagnosticAttemptEntity> allCompleted = attemptRepository.findCompletedByUserId(userId);
        long totalCount = allCompleted.size();

        // Apply pagination manually (could use Pageable for better performance with large datasets)
        int start = page * size;
        int end = Math.min(start + size, allCompleted.size());

        List<DiagnosticHistoryResponse.DiagnosticHistoryItem> items;
        if (start >= allCompleted.size()) {
            items = new ArrayList<>();
        } else {
            items = allCompleted.subList(start, end).stream()
                    .map(this::toHistoryItem)
                    .collect(Collectors.toList());
        }

        return DiagnosticHistoryResponse.builder()
                .items(items)
                .totalCount(totalCount)
                .page(page)
                .size(size)
                .build();
    }

    /**
     * Convert attempt entity to history item DTO.
     */
    private DiagnosticHistoryResponse.DiagnosticHistoryItem toHistoryItem(EilDiagnosticAttemptEntity attempt) {
        double rawScore = attempt.getRawScore() != null ? attempt.getRawScore().doubleValue() : 0.0;

        return DiagnosticHistoryResponse.DiagnosticHistoryItem.builder()
                .sessionId(attempt.getSessionId())
                .testType(attempt.getTestType())
                .status(attempt.getStatus())
                .totalQuestions(attempt.getTotalQuestions())
                .answeredQuestions(attempt.getAnsweredQuestions())
                .rawScore(attempt.getRawScore())
                .estimatedLevel(attempt.getEstimatedLevel() != null ? attempt.getEstimatedLevel() : estimateLevel(rawScore))
                .estimatedScoreMin(attempt.getEstimatedScoreMin())
                .estimatedScoreMax(attempt.getEstimatedScoreMax())
                .timeSpentSeconds(attempt.getTimeSpentSeconds())
                .startTime(attempt.getStartTime())
                .endTime(attempt.getEndTime())
                .createdAt(attempt.getCreatedAt())
                .build();
    }

    /**
     * Determine if an answer is correct by comparing user selection against question bank.
     *
     * @param request Contains questionId and answerData (List<Boolean> where true = selected)
     * @return true if user's selected answers match the correct answers in question bank
     */
    private boolean determineCorrectness(DiagnosticAnswerRequest request) {
        if (request.getAnswerData() == null || request.getAnswerData().isEmpty()) {
            return false;
        }

        // Fetch question from database
        QuestionEntity question = questionRepository.findById(request.getQuestionId()).orElse(null);
        if (question == null || question.getAnswerData() == null) {
            log.warn("Question not found or has no answer data: {}", request.getQuestionId());
            return false;
        }

        // Parse question to get answer options with correct flags
        QuestionResponse questionResponse = QuestionResponse.from(question);
        List<QuestionResponse.AnswerData> answerOptions = questionResponse.getAnswerData();

        if (answerOptions == null || answerOptions.isEmpty()) {
            log.warn("Question {} has no parsed answer options", request.getQuestionId());
            return false;
        }

        List<Boolean> userSelections = request.getAnswerData();

        // Validate array sizes match
        if (userSelections.size() != answerOptions.size()) {
            log.warn("Answer array size mismatch: user={}, question={} for questionId={}",
                    userSelections.size(), answerOptions.size(), request.getQuestionId());
            // Still try to validate with available data
        }

        // Compare user selections against correct answers
        // For single choice: exactly one correct answer must be selected
        // For multiple choice: all correct answers must be selected, no incorrect ones
        int correctCount = 0;
        int userCorrectSelections = 0;
        int userIncorrectSelections = 0;

        for (int i = 0; i < answerOptions.size(); i++) {
            QuestionResponse.AnswerData option = answerOptions.get(i);
            boolean isCorrectOption = option.isCorrect();
            boolean userSelected = i < userSelections.size() && Boolean.TRUE.equals(userSelections.get(i));

            if (isCorrectOption) {
                correctCount++;
                if (userSelected) {
                    userCorrectSelections++;
                }
            } else {
                if (userSelected) {
                    userIncorrectSelections++;
                }
            }
        }

        // Answer is correct if user selected all correct options and no incorrect ones
        boolean isCorrect = (userCorrectSelections == correctCount) && (userIncorrectSelections == 0);

        log.debug("Question {}: correctOptions={}, userCorrect={}, userIncorrect={}, result={}",
                request.getQuestionId(), correctCount, userCorrectSelections, userIncorrectSelections, isCorrect);

        return isCorrect;
    }

    /**
     * Calculate category-level scores.
     */
    private Map<String, DiagnosticResultResponse.CategoryScore> calculateCategoryScores(List<EilDiagnosticAnswerEntity> answers) {
        Map<String, List<EilDiagnosticAnswerEntity>> byCategory = new HashMap<>();

        for (EilDiagnosticAnswerEntity answer : answers) {
            if (answer.getSkillId() != null) {
                try {
                    EilSkillEntity skill = skillService.getSkillById(answer.getSkillId());
                    String category = skill.getCategory();
                    byCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(answer);
                } catch (AppException e) {
                    log.warn("Skill not found: {}", answer.getSkillId());
                }
            }
        }

        Map<String, DiagnosticResultResponse.CategoryScore> result = new HashMap<>();
        for (Map.Entry<String, List<EilDiagnosticAnswerEntity>> entry : byCategory.entrySet()) {
            List<EilDiagnosticAnswerEntity> categoryAnswers = entry.getValue();
            int total = categoryAnswers.size();
            int correct = (int) categoryAnswers.stream()
                    .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                    .count();
            double accuracy = total > 0 ? (double) correct / total : 0.0;

            result.put(entry.getKey(), DiagnosticResultResponse.CategoryScore.builder()
                    .category(entry.getKey())
                    .totalQuestions(total)
                    .correctCount(correct)
                    .accuracy(accuracy)
                    .build());
        }

        return result;
    }

    /**
     * Calculate time spent in seconds.
     */
    private Integer calculateTimeSpentSeconds(EilDiagnosticAttemptEntity attempt) {
        if (attempt.getStartTime() == null) {
            return attempt.getTimeSpentSeconds();
        }
        LocalDateTime end = attempt.getEndTime() != null ? attempt.getEndTime() : LocalDateTime.now();
        return (int) Duration.between(attempt.getStartTime(), end).toSeconds();
    }

    /**
     * Estimate proficiency level based on raw score.
     */
    private String estimateLevel(double rawScore) {
        if (rawScore >= 90) return "ADVANCED";
        if (rawScore >= 75) return "UPPER_INTERMEDIATE";
        if (rawScore >= 60) return "INTERMEDIATE";
        if (rawScore >= 45) return "PRE_INTERMEDIATE";
        if (rawScore >= 30) return "ELEMENTARY";
        return "BEGINNER";
    }

    /**
     * Estimate minimum TOEIC score based on raw score.
     */
    private Integer estimateScoreMin(double rawScore) {
        // Map raw percentage to TOEIC score range (10-990)
        return (int) Math.max(10, rawScore * 9 - 50);
    }

    /**
     * Estimate maximum TOEIC score based on raw score.
     */
    private Integer estimateScoreMax(double rawScore) {
        return (int) Math.min(990, rawScore * 10 + 50);
    }

    /**
     * Generate recommendations based on diagnostic results.
     *
     * @param categoryScores Category-level performance
     * @param rawScore       Overall raw score percentage
     * @param estimatedLevel Estimated proficiency level
     * @return List of personalized recommendations
     */
    private List<String> generateRecommendations(
            Map<String, DiagnosticResultResponse.CategoryScore> categoryScores,
            double rawScore,
            String estimatedLevel) {

        List<String> recommendations = new ArrayList<>();

        // Find weakest categories
        List<Map.Entry<String, DiagnosticResultResponse.CategoryScore>> sortedCategories = categoryScores.entrySet().stream()
                .sorted(Comparator.comparingDouble(e -> e.getValue().getAccuracy()))
                .collect(Collectors.toList());

        // Add category-specific recommendations for weak areas
        for (int i = 0; i < Math.min(2, sortedCategories.size()); i++) {
            Map.Entry<String, DiagnosticResultResponse.CategoryScore> entry = sortedCategories.get(i);
            String category = entry.getKey();
            double accuracy = entry.getValue().getAccuracy();

            if (accuracy < 0.5) {
                recommendations.add(String.format(
                        "Focus on improving your %s skills. Practice with targeted exercises in this area.",
                        category.toLowerCase().replace("_", " ")));
            } else if (accuracy < 0.7) {
                recommendations.add(String.format(
                        "Your %s skills need some work. Regular practice will help strengthen this area.",
                        category.toLowerCase().replace("_", " ")));
            }
        }

        // Add level-specific recommendations
        switch (estimatedLevel) {
            case "BEGINNER":
                recommendations.add("Start with basic vocabulary and grammar foundations.");
                recommendations.add("Use English learning apps for daily practice sessions.");
                break;
            case "ELEMENTARY":
                recommendations.add("Build your vocabulary with common words and phrases.");
                recommendations.add("Practice listening with simple audio content.");
                break;
            case "PRE_INTERMEDIATE":
                recommendations.add("Focus on expanding vocabulary and understanding context.");
                recommendations.add("Try reading short articles and news in English.");
                break;
            case "INTERMEDIATE":
                recommendations.add("Challenge yourself with more complex reading materials.");
                recommendations.add("Practice speaking and writing to build confidence.");
                break;
            case "UPPER_INTERMEDIATE":
                recommendations.add("Focus on advanced grammar structures and idioms.");
                recommendations.add("Engage with native English content like podcasts and movies.");
                break;
            case "ADVANCED":
                recommendations.add("Maintain your skills with regular exposure to English content.");
                recommendations.add("Focus on nuanced vocabulary and professional English.");
                break;
        }

        // Add general recommendations based on raw score
        if (rawScore < 40) {
            recommendations.add("Consider taking structured English courses for faster progress.");
        } else if (rawScore >= 80) {
            recommendations.add("Great job! Keep challenging yourself with advanced materials.");
        }

        // Limit to 5 recommendations
        return recommendations.size() > 5 ? recommendations.subList(0, 5) : recommendations;
    }

    /**
     * Build metadata JSON with question IDs.
     */
    private String buildMetadataJson(List<Long> questionIds) {
        return buildMetadataJson(questionIds, null, null, null);
    }

    /**
     * Build metadata JSON with question IDs and additional info.
     */
    private String buildMetadataJson(List<Long> questionIds, String mode, String certificationCode, String careerPath) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("questionIds", questionIds);
            if (mode != null) {
                metadata.put("mode", mode);
            }
            if (certificationCode != null) {
                metadata.put("certificationCode", certificationCode);
            }
            if (careerPath != null) {
                metadata.put("careerPath", careerPath);
            }
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            log.error("Failed to build metadata JSON", e);
            return null;
        }
    }

    /**
     * Parse question IDs from metadata JSON.
     */
    @SuppressWarnings("unchecked")
    private List<Long> parseQuestionIdsFromMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            Map<String, Object> metadata = objectMapper.readValue(metadataJson, new TypeReference<Map<String, Object>>() {});
            Object questionIdsObj = metadata.get("questionIds");
            if (questionIdsObj instanceof List) {
                List<?> rawList = (List<?>) questionIdsObj;
                return rawList.stream()
                        .map(obj -> {
                            if (obj instanceof Number) {
                                return ((Number) obj).longValue();
                            }
                            return Long.valueOf(obj.toString());
                        })
                        .collect(Collectors.toList());
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse question IDs from metadata", e);
        }
        return Collections.emptyList();
    }

    /**
     * Parse a string value from metadata JSON.
     */
    private String parseStringFromMetadata(String metadataJson, String key) {
        if (metadataJson == null || metadataJson.isEmpty() || key == null) {
            return null;
        }
        try {
            Map<String, Object> metadata = objectMapper.readValue(metadataJson, new TypeReference<Map<String, Object>>() {});
            Object value = metadata.get(key);
            return value != null ? value.toString() : null;
        } catch (JsonProcessingException e) {
            log.error("Failed to parse {} from metadata", key, e);
        }
        return null;
    }

    /**
     * Get question by index from list of question IDs.
     */
    private QuestionResponse getQuestionByIndex(List<Long> questionIds, int index) {
        if (questionIds == null || questionIds.isEmpty() || index < 0 || index >= questionIds.size()) {
            return null;
        }
        Long questionId = questionIds.get(index);
        return questionRepository.findById(questionId)
                .map(QuestionResponse::from)
                .orElse(null);
    }

    // ============= ADAPTIVE DIAGNOSTIC HELPER METHODS =============

    /**
     * Parse metadata JSON to Map.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(metadataJson, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse metadata", e);
            return new HashMap<>();
        }
    }

    /**
     * Get question IDs from metadata (same as parseQuestionIdsFromMetadata but from Map).
     */
    @SuppressWarnings("unchecked")
    private List<Long> getQuestionIdsFromMetadata(Map<String, Object> metadata) {
        Object questionIdsObj = metadata.get("questionIds");
        if (questionIdsObj instanceof List) {
            List<?> rawList = (List<?>) questionIdsObj;
            return rawList.stream()
                    .map(obj -> {
                        if (obj instanceof Number) {
                            return ((Number) obj).longValue();
                        }
                        return Long.valueOf(obj.toString());
                    })
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Get terminated skills from metadata.
     */
    @SuppressWarnings("unchecked")
    private Set<Long> getTerminatedSkillsFromMetadata(Map<String, Object> metadata) {
        Object terminatedObj = metadata.get("terminatedSkills");
        if (terminatedObj instanceof List) {
            List<?> rawList = (List<?>) terminatedObj;
            return rawList.stream()
                    .map(obj -> {
                        if (obj instanceof Number) {
                            return ((Number) obj).longValue();
                        }
                        return Long.valueOf(obj.toString());
                    })
                    .collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    /**
     * Get answered question IDs for a diagnostic session.
     */
    private Set<Long> getAnsweredQuestionIds(Long diagnosticAttemptId) {
        List<EilDiagnosticAnswerEntity> answers = answerRepository.findByDiagnosticAttemptId(diagnosticAttemptId);
        return answers.stream()
                .map(EilDiagnosticAnswerEntity::getQuestionId)
                .collect(Collectors.toSet());
    }

    /**
     * Get integer value from nested metadata path.
     */
    private int getIntFromMetadata(Map<String, Object> metadata, String key, int defaultValue) {
        try {
            if (key.contains(".")) {
                // Handle nested keys like "skillConsecutiveWrong.167"
                String[] parts = key.split("\\.", 2);
                Object nestedObj = metadata.get(parts[0]);
                if (nestedObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nested = (Map<String, Object>) nestedObj;
                    Object value = nested.get(parts[1]);
                    if (value instanceof Number) {
                        return ((Number) value).intValue();
                    }
                }
            } else {
                Object value = metadata.get(key);
                if (value instanceof Number) {
                    return ((Number) value).intValue();
                }
            }
        } catch (Exception e) {
            log.debug("Failed to get int from metadata key: {}", key);
        }
        return defaultValue;
    }

    /**
     * Get skill name by ID (helper).
     */
    private String getSkillName(Long skillId) {
        if (skillId == null) return null;
        try {
            return skillService.getSkillById(skillId).getName();
        } catch (AppException e) {
            return null;
        }
    }

    /**
     * Calculate confidence level based on answers (IRT-inspired).
     * Higher confidence = more certain about user's ability level.
     */
    private double calculateConfidence(Long diagnosticAttemptId) {
        List<EilDiagnosticAnswerEntity> answers = answerRepository.findByDiagnosticAttemptId(diagnosticAttemptId);

        if (answers.isEmpty()) {
            return 0.5; // Neutral starting point
        }

        // Base accuracy
        long correctCount = answers.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count();
        double accuracy = (double) correctCount / answers.size();

        // Consecutive correct bonus (stability)
        int maxConsecutiveCorrect = 0;
        int currentStreak = 0;

        for (EilDiagnosticAnswerEntity answer : answers) {
            if (Boolean.TRUE.equals(answer.getIsCorrect())) {
                currentStreak++;
                maxConsecutiveCorrect = Math.max(maxConsecutiveCorrect, currentStreak);
            } else {
                currentStreak = 0;
            }
        }

        // Confidence = accuracy (60%) + stability (40%)
        double stabilityBonus = Math.min(0.4, maxConsecutiveCorrect * 0.1);
        double confidence = (accuracy * 0.6) + stabilityBonus;

        return Math.max(0.0, Math.min(1.0, confidence));
    }

    /**
     * Build adaptive state for CAT mode.
     */
    private DiagnosticSessionResponse.AdaptiveState buildAdaptiveState(
            EilDiagnosticAttemptEntity attempt,
            double targetConfidence) {

        double currentConfidence = attempt.getAnsweredQuestions() > 0
                ? calculateConfidence(attempt.getId())
                : 0.5;

        return DiagnosticSessionResponse.AdaptiveState.builder()
                .currentConfidence(currentConfidence)
                .targetConfidence(targetConfidence)
                .maxQuestions(attempt.getTotalQuestions())
                .canTerminateEarly(true)
                .build();
    }

    /**
     * Update metadata with new tracking values.
     */
    private String updateMetadataTracking(String metadataJson, boolean isCorrect, Long skillId,
                                           boolean shouldTerminate, String terminationReason) {
        try {
            Map<String, Object> metadata = parseMetadata(metadataJson);

            // Get current counters
            int consecutiveWrong = getIntFromMetadata(metadata, "consecutiveWrong", 0);
            Set<Long> terminatedSkills = getTerminatedSkillsFromMetadata(metadata);

            // Get or create skillConsecutiveWrong map
            @SuppressWarnings("unchecked")
            Map<String, Integer> skillConsecutiveWrong =
                    (Map<String, Integer>) metadata.getOrDefault("skillConsecutiveWrong", new HashMap<>());

            if (isCorrect) {
                // Reset counters on correct answer
                consecutiveWrong = 0;
                if (skillId != null) {
                    skillConsecutiveWrong.put(skillId.toString(), 0);
                }
            } else {
                // Increment counters on wrong answer
                consecutiveWrong++;
                if (skillId != null) {
                    int skillWrong = skillConsecutiveWrong.getOrDefault(skillId.toString(), 0) + 1;
                    skillConsecutiveWrong.put(skillId.toString(), skillWrong);

                    // Check if skill should be terminated (2 consecutive wrong)
                    if (skillWrong >= 2) {
                        terminatedSkills.add(skillId);
                        log.info("Skill {} terminated after 2 consecutive wrong answers", skillId);
                    }
                }
            }

            // Update metadata
            metadata.put("consecutiveWrong", consecutiveWrong);
            metadata.put("skillConsecutiveWrong", skillConsecutiveWrong);
            metadata.put("terminatedSkills", new ArrayList<>(terminatedSkills));
            if (shouldTerminate) {
                metadata.put("terminationReason", terminationReason);
            }

            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            log.error("Failed to update metadata tracking", e);
            return metadataJson; // Return original if update fails
        }
    }

    /**
     * Build adaptive state based on current progress.
     * Estimates confidence based on correct/total ratio.
     */
    private DiagnosticSessionResponse.AdaptiveState buildAdaptiveState(int correctCount, int totalAnswered, int maxQuestions) {
        double targetConfidence = 0.80; // 80% target confidence

        // Calculate current confidence (accuracy ratio)
        double currentConfidence = totalAnswered > 0
            ? (double) correctCount / totalAnswered
            : 0.0;

        // Can terminate early if:
        // 1. Confidence >= target AND answered at least 5 questions (minimum sample)
        // 2. OR answered all questions
        boolean canTerminateEarly = (currentConfidence >= targetConfidence && totalAnswered >= 5)
                                     || totalAnswered >= maxQuestions;

        return DiagnosticSessionResponse.AdaptiveState.builder()
                .currentConfidence(currentConfidence)
                .targetConfidence(targetConfidence)
                .maxQuestions(maxQuestions)
                .canTerminateEarly(canTerminateEarly)
                .build();
    }

    /**
     * Extract certification code from diagnostic attempt metadata.
     *
     * CRITICAL FIX: This method prevents cross-contamination of skills between different certifications.
     * When a user completes multiple diagnostic tests (e.g., TOEIC, ISTQB, PSM_I), their skill results
     * must be filtered by the certification they just tested on.
     *
     * Example scenario:
     * 1. User completes English diagnostic → Skills saved to eil_skill_mastery (LC_P1_OBJ_ID, etc.)
     * 2. User completes ISTQB diagnostic → New skills saved (ISTQB_FUNDAMENTALS, etc.)
     * 3. Without this filter: ISTQB result shows English skills ❌
     * 4. With this filter: ISTQB result shows only ISTQB skills ✅
     *
     * @param attempt Diagnostic attempt entity containing metadata
     * @return Certification code (e.g., "ISTQB_CTFL", "PSM_I") or null if not found
     */
    private String extractCertificationCode(EilDiagnosticAttemptEntity attempt) {
        // Try to extract from metadata first (preferred source)
        try {
            if (attempt.getMetadata() != null && !attempt.getMetadata().isEmpty()) {
                String certCode = parseStringFromMetadata(attempt.getMetadata(), "certificationCode");
                if (certCode != null && !certCode.isEmpty()) {
                    log.debug("Extracted certificationCode from metadata: {}", certCode);
                    return certCode;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract certificationCode from metadata: {}", e.getMessage());
        }

        // Fallback: use testType if it's a known certification code
        // testType is used for backward compatibility with older diagnostic sessions
        // QUICK FIX: Don't filter out TOEIC/IELTS/TOEFL - let them pass through
        // This allows career assessment to work properly even when testType is set to legacy values
        String testType = attempt.getTestType();
        if (testType != null && !testType.isEmpty()) {
            log.debug("Using testType as certificationCode fallback: {}", testType);
            return testType;
        }

        // No certification code found - will return all skills (legacy behavior)
        log.warn("No certificationCode found for diagnostic session {}, falling back to all skills",
                attempt.getSessionId());
        return null;
    }
}
