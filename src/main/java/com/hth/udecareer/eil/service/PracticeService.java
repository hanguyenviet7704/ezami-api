package com.hth.udecareer.eil.service;

import com.hth.udecareer.eil.algorithm.MasteryCalculator;
import com.hth.udecareer.eil.algorithm.QuestionSelector;
import com.hth.udecareer.eil.entities.*;
import com.hth.udecareer.eil.enums.DifficultyLevel;
import com.hth.udecareer.eil.enums.SessionStatus;
import com.hth.udecareer.eil.enums.SessionType;
import com.hth.udecareer.eil.model.dto.SkillDto;
import com.hth.udecareer.eil.model.request.PracticeStartRequest;
import com.hth.udecareer.eil.model.request.PracticeSubmitRequest;
import com.hth.udecareer.eil.model.response.NextQuestionResponse;
import com.hth.udecareer.eil.model.response.PracticeResultResponse;
import com.hth.udecareer.eil.model.response.PracticeSessionResponse;
import com.hth.udecareer.eil.model.response.SkillMasteryResponse;
import com.hth.udecareer.eil.repository.EilPracticeAttemptRepository;
import com.hth.udecareer.eil.repository.EilPracticeSessionRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing adaptive practice sessions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PracticeService {

    private final EilPracticeSessionRepository sessionRepository;
    private final EilPracticeAttemptRepository attemptRepository;
    private final SkillService skillService;
    private final MasteryService masteryService;
    private final ReadinessService readinessService;
    private final MasteryCalculator masteryCalculator;
    private final QuestionSelector questionSelector;
    private final QuestionRepository questionRepository;

    @Value("${eil.practice.max-questions:20}")
    private int maxQuestions;

    @Value("${eil.practice.default-questions:10}")
    private int defaultQuestions;

    /**
     * Start a new practice session.
     */
    @Transactional
    public PracticeSessionResponse startPractice(Long userId, PracticeStartRequest request) throws AppException {
        // Generate session ID
        String sessionId = UUID.randomUUID().toString();

        // Determine session type
        SessionType sessionType = request.getSessionType() != null
                ? SessionType.valueOf(request.getSessionType())
                : SessionType.ADAPTIVE;

        // Get target skill for SKILL_FOCUS sessions
        Long targetSkillId = null;
        SkillMasteryResponse targetSkillResponse = null;
        if (sessionType == SessionType.SKILL_FOCUS) {
            // Support both single skill (targetSkillId) and multiple skills (focusSkills)
            if (request.getFocusSkills() != null && !request.getFocusSkills().isEmpty()) {
                // Use first skill from focusSkills list as primary target
                targetSkillId = request.getFocusSkills().get(0);
            } else if (request.getTargetSkillId() != null) {
                targetSkillId = request.getTargetSkillId();
            }

            if (targetSkillId != null) {
                // Validate skill exists and get info
                EilSkillEntity skill = skillService.getSkillById(targetSkillId);
                EilSkillMasteryEntity mastery = masteryService.getOrCreateMastery(userId, targetSkillId);
                targetSkillResponse = SkillMasteryResponse.builder()
                        .skillId(skill.getId())
                        .skillCode(skill.getCode())
                        .skillName(skill.getName())
                        .skillNameVi(skill.getNameVi())
                        .category(skill.getCategory())
                        .masteryLevel(mastery.getMasteryLevel().doubleValue())
                        .build();
            }
        }

        // Get max questions for session
        int maxQuestionsForSession = request.getMaxQuestions() != null
                ? Math.min(request.getMaxQuestions(), maxQuestions)
                : defaultQuestions;

        // Create session
        EilPracticeSessionEntity session = EilPracticeSessionEntity.builder()
                .userId(userId)
                .sessionId(sessionId)
                .sessionType(sessionType.name())
                .targetSkillId(targetSkillId)
                .status(SessionStatus.ACTIVE.name())
                .maxQuestions(maxQuestionsForSession)
                .totalQuestions(0)
                .correctCount(0)
                .currentDifficulty(new BigDecimal("3.00"))
                .startTime(LocalDateTime.now())
                .totalTimeSeconds(0)
                .masteryGain(BigDecimal.ZERO)
                .pointsEarned(0)
                .build();

        session = sessionRepository.save(session);

        log.info("Started practice session {} for user {}: type={}, maxQuestions={}",
                sessionId, userId, sessionType, maxQuestionsForSession);

        return PracticeSessionResponse.builder()
                .sessionId(sessionId)
                .sessionType(sessionType.name())
                .status(SessionStatus.ACTIVE.name())
                .maxQuestions(maxQuestionsForSession)
                .questionsServed(0)
                .targetSkill(targetSkillResponse)
                .startTime(session.getStartTime())
                .build();
    }

    /**
     * Get the next question for a practice session.
     */
    @Transactional
    public NextQuestionResponse getNextQuestion(Long userId, String sessionId) throws AppException {
        EilPracticeSessionEntity session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.EIL_PRACTICE_SESSION_NOT_FOUND));

        // Validate ownership
        if (!session.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Check session status
        if (!SessionStatus.ACTIVE.name().equals(session.getStatus())) {
            throw new AppException(ErrorCode.EIL_PRACTICE_SESSION_COMPLETED);
        }

        // Check if max questions reached
        if (session.getTotalQuestions() >= session.getMaxQuestions()) {
            throw new AppException(ErrorCode.EIL_PRACTICE_MAX_QUESTIONS_REACHED);
        }

        // Get user's mastery map
        Map<Long, BigDecimal> masteryMap = masteryService.getMasteryMap(userId);

        // Get recent questions to avoid
        Set<Long> recentQuestionIds = getRecentQuestionIds(session.getId());

        // Select next skill based on session type
        SessionType sessionType = SessionType.valueOf(session.getSessionType());
        Long selectedSkillId = questionSelector.selectNextSkill(
                masteryMap,
                sessionType,
                session.getTargetSkillId(),
                getRecentSkillIds(session.getId())
        );

        if (selectedSkillId == null) {
            throw new AppException(ErrorCode.EIL_PRACTICE_NO_QUESTIONS_AVAILABLE);
        }

        // Get mastery level for selected skill
        BigDecimal mastery = masteryMap.getOrDefault(selectedSkillId, masteryCalculator.getInitialMastery());

        // Determine target difficulty
        DifficultyLevel targetDifficulty = questionSelector.selectTargetDifficulty(mastery);

        // Get question candidates for this skill and difficulty
        List<Long> candidates = skillService.getQuestionIdsForSkillAndDifficulty(
                selectedSkillId, targetDifficulty.getLevel());

        // If no questions at exact difficulty, expand search
        if (candidates.isEmpty()) {
            candidates = skillService.getQuestionIdsForSkill(selectedSkillId);
        }

        // Filter out recent questions
        candidates = questionSelector.filterRecentQuestions(candidates, recentQuestionIds);

        if (candidates.isEmpty()) {
            throw new AppException(ErrorCode.EIL_PRACTICE_NO_QUESTIONS_AVAILABLE);
        }

        // Select a random question from candidates
        Long selectedQuestionId = candidates.get(new Random().nextInt(candidates.size()));

        // Load question entity and convert to response
        QuestionResponse questionResponse = questionRepository.findById(selectedQuestionId)
                .map(QuestionResponse::from)
                .orElseThrow(() -> new AppException(ErrorCode.EIL_PRACTICE_NO_QUESTIONS_AVAILABLE));

        // Get skill info
        EilSkillEntity skill = skillService.getSkillById(selectedSkillId);
        SkillDto skillDto = skillService.toDto(skill);

        int questionNumber = session.getTotalQuestions() + 1;
        boolean isLast = questionNumber >= session.getMaxQuestions();

        log.debug("Selected question {} for session {}: skill={}, difficulty={}, mastery={}",
                selectedQuestionId, sessionId, selectedSkillId, targetDifficulty, mastery);

        return NextQuestionResponse.builder()
                .sessionId(sessionId)
                .questionNumber(questionNumber)
                .totalQuestions(session.getMaxQuestions())
                .question(questionResponse)
                .targetSkill(skillDto)
                .difficulty(targetDifficulty.getLevel())
                .isLastQuestion(isLast)
                .build();
    }

    /**
     * Submit an answer for a practice question.
     */
    @Transactional
    public PracticeResultResponse submitAnswer(Long userId, PracticeSubmitRequest request) throws AppException {
        EilPracticeSessionEntity session = sessionRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new AppException(ErrorCode.EIL_PRACTICE_SESSION_NOT_FOUND));

        // Validate ownership
        if (!session.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Check session status
        if (!SessionStatus.ACTIVE.name().equals(session.getStatus())) {
            throw new AppException(ErrorCode.EIL_PRACTICE_SESSION_COMPLETED);
        }

        // Check if question already answered in this session
        Optional<EilPracticeAttemptEntity> existingAttempt = attemptRepository
                .findBySessionIdAndQuestionId(session.getId(), request.getQuestionId());
        if (existingAttempt.isPresent()) {
            throw new AppException(ErrorCode.EIL_PRACTICE_QUESTION_ALREADY_ANSWERED);
        }

        // Get skill for this question
        Long skillId = skillService.getPrimarySkillIdForQuestion(request.getQuestionId());
        if (skillId == null) {
            log.warn("No skill mapping for question {}", request.getQuestionId());
            // Get any active skill as fallback (cannot be null due to NOT NULL constraint)
            List<EilSkillEntity> leafSkills = skillService.getLeafSkills();
            if (!leafSkills.isEmpty()) {
                skillId = leafSkills.get(0).getId();
                log.info("Using fallback skill {} for question {}", skillId, request.getQuestionId());
            } else {
                log.error("No skills available in system - cannot save practice attempt");
                throw new AppException(ErrorCode.INVALID_KEY, "No skills available in system");
            }
        }

        // Determine correctness by comparing user answer with correct answer from database
        boolean isCorrect = determineCorrectness(request.getQuestionId(), request.getAnswerData());
        int timeSpent = request.getTimeSpentSeconds() != null ? request.getTimeSpentSeconds() : 0;
        int difficulty = 3; // Default difficulty

        // Get mastery before update
        BigDecimal masteryBefore = BigDecimal.ZERO;
        MasteryService.MasteryUpdateResult masteryResult = null;

        // Always update mastery since skillId is guaranteed non-null
        masteryBefore = masteryService.getOrCreateMastery(userId, skillId).getMasteryLevel();

        // Update mastery
        DifficultyLevel diffLevel = DifficultyLevel.fromLevel(difficulty);
        masteryResult = masteryService.updateMastery(userId, skillId, isCorrect, diffLevel);

        // Calculate points
        int points = calculatePoints(isCorrect, difficulty, session.getCorrectCount());

        // Save attempt - skillId is guaranteed non-null here
        EilPracticeAttemptEntity attempt = EilPracticeAttemptEntity.builder()
                .sessionId(session.getId())
                .questionId(request.getQuestionId())
                .skillId(skillId)
                .questionOrder(session.getTotalQuestions() + 1)
                .questionDifficulty(difficulty)
                .userAnswer(request.getAnswerData() != null ? request.getAnswerData().toString() : null)
                .isCorrect(isCorrect)
                .timeSpentSeconds(timeSpent)
                .masteryBefore(masteryBefore)
                .masteryAfter(masteryResult != null ? masteryResult.getMasteryAfter() : masteryBefore)
                .masteryDelta(masteryResult != null ? masteryResult.getMasteryDelta() : BigDecimal.ZERO)
                .pointsEarned(points)
                .answeredAt(LocalDateTime.now())
                .build();

        attempt = attemptRepository.save(attempt);

        // Update session
        session.setTotalQuestions(session.getTotalQuestions() + 1);
        if (isCorrect) {
            session.setCorrectCount(session.getCorrectCount() + 1);
        }
        session.setTotalTimeSeconds(session.getTotalTimeSeconds() + timeSpent);
        if (masteryResult != null) {
            session.setMasteryGain(session.getMasteryGain().add(
                    masteryResult.getMasteryDelta().max(BigDecimal.ZERO)));
        }
        session.setPointsEarned(session.getPointsEarned() + points);

        // Update current difficulty based on performance
        updateSessionDifficulty(session, isCorrect);

        // Check if session should end
        boolean isComplete = session.getTotalQuestions() >= session.getMaxQuestions();
        if (isComplete) {
            session.setStatus(SessionStatus.COMPLETED.name());
            session.setEndTime(LocalDateTime.now());
        }

        sessionRepository.save(session);

        // Create readiness snapshot when session auto-completes
        if (isComplete) {
            try {
                String testType = session.getSessionType();
                readinessService.createSnapshot(userId, testType, session.getTotalQuestions(), session.getCorrectCount());
                log.info("Created readiness snapshot for user {} after auto-completing practice session", userId);
            } catch (Exception e) {
                log.warn("Failed to create readiness snapshot for user {}: {}", userId, e.getMessage());
            }
        }

        double sessionAccuracy = session.getTotalQuestions() > 0
                ? (double) session.getCorrectCount() / session.getTotalQuestions()
                : 0.0;

        log.debug("Answer submitted for session {}: question={}, correct={}, points={}, progress={}/{}",
                request.getSessionId(), request.getQuestionId(), isCorrect, points,
                session.getTotalQuestions(), session.getMaxQuestions());

        return PracticeResultResponse.builder()
                .attemptId(attempt.getId())
                .isCorrect(isCorrect)
                .pointsEarned(points)
                .masteryBefore(masteryBefore.doubleValue())
                .masteryAfter(masteryResult != null ? masteryResult.getMasteryAfter().doubleValue() : masteryBefore.doubleValue())
                .masteryDelta(masteryResult != null ? masteryResult.getMasteryDelta().doubleValue() : 0.0)
                .masteryLabel(masteryCalculator.getMasteryLabel(
                        masteryResult != null ? masteryResult.getMasteryAfter() : masteryBefore).name())
                .questionsAnswered(session.getTotalQuestions())
                .correctCount(session.getCorrectCount())
                .sessionAccuracy(sessionAccuracy)
                .build();
    }

    /**
     * End a practice session.
     */
    @Transactional
    public PracticeSessionResponse endSession(Long userId, String sessionId) throws AppException {
        EilPracticeSessionEntity session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.EIL_PRACTICE_SESSION_NOT_FOUND));

        if (!session.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (SessionStatus.ACTIVE.name().equals(session.getStatus())) {
            session.setStatus(SessionStatus.COMPLETED.name());
            session.setEndTime(LocalDateTime.now());
            sessionRepository.save(session);

            // Create readiness snapshot after practice session completion
            try {
                String testType = session.getSessionType();
                readinessService.createSnapshot(userId, testType, session.getTotalQuestions(), session.getCorrectCount());
                log.info("Created readiness snapshot for user {} after practice session {}", userId, sessionId);
            } catch (Exception e) {
                log.warn("Failed to create readiness snapshot for user {}: {}", userId, e.getMessage());
                // Don't fail the session completion if snapshot creation fails
            }
        }

        return buildSessionResponse(session);
    }

    /**
     * Get session status.
     */
    public PracticeSessionResponse getSessionStatus(Long userId, String sessionId) throws AppException {
        EilPracticeSessionEntity session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.EIL_PRACTICE_SESSION_NOT_FOUND));

        if (!session.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return buildSessionResponse(session);
    }

    /**
     * Build session response from entity.
     */
    private PracticeSessionResponse buildSessionResponse(EilPracticeSessionEntity session) {
        SkillMasteryResponse targetSkill = null;
        if (session.getTargetSkillId() != null) {
            try {
                EilSkillEntity skill = skillService.getSkillById(session.getTargetSkillId());
                targetSkill = SkillMasteryResponse.builder()
                        .skillId(skill.getId())
                        .skillCode(skill.getCode())
                        .skillName(skill.getName())
                        .category(skill.getCategory())
                        .build();
            } catch (AppException e) {
                log.warn("Target skill not found: {}", session.getTargetSkillId());
            }
        }

        return PracticeSessionResponse.builder()
                .sessionId(session.getSessionId())
                .sessionType(session.getSessionType())
                .status(session.getStatus())
                .maxQuestions(session.getMaxQuestions())
                .questionsServed(session.getTotalQuestions())
                .targetSkill(targetSkill)
                .startTime(session.getStartTime())
                .build();
    }

    /**
     * Get recent question IDs from a session.
     */
    private Set<Long> getRecentQuestionIds(Long sessionId) {
        return attemptRepository.findBySessionId(sessionId).stream()
                .map(EilPracticeAttemptEntity::getQuestionId)
                .collect(Collectors.toSet());
    }

    /**
     * Get recent skill IDs from a session.
     */
    private Set<Long> getRecentSkillIds(Long sessionId) {
        return attemptRepository.findBySessionId(sessionId).stream()
                .map(EilPracticeAttemptEntity::getSkillId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Calculate points earned for an answer.
     */
    private int calculatePoints(boolean isCorrect, int difficulty, int currentStreak) {
        if (!isCorrect) {
            return 0;
        }

        int basePoints = 10;
        int difficultyBonus = (difficulty - 1) * 5;
        int streakBonus = Math.min(currentStreak, 5) * 2;

        return basePoints + difficultyBonus + streakBonus;
    }

    /**
     * Update session difficulty based on performance.
     */
    private void updateSessionDifficulty(EilPracticeSessionEntity session, boolean isCorrect) {
        BigDecimal current = session.getCurrentDifficulty();
        BigDecimal adjustment = new BigDecimal(isCorrect ? "0.1" : "-0.1");
        BigDecimal newDifficulty = current.add(adjustment);

        if (newDifficulty.compareTo(BigDecimal.ONE) < 0) {
            newDifficulty = BigDecimal.ONE;
        } else if (newDifficulty.compareTo(new BigDecimal("5.0")) > 0) {
            newDifficulty = new BigDecimal("5.0");
        }

        session.setCurrentDifficulty(newDifficulty);
    }

    /**
     * Determine if user's answer is correct by comparing with correct answer from database.
     * @param questionId The question ID
     * @param userAnswerData User's answer as array of booleans (true = selected, false = not selected)
     * @return true if all correct options were selected and no incorrect ones
     */
    private boolean determineCorrectness(Long questionId, List<Boolean> userAnswerData) {
        if (userAnswerData == null || userAnswerData.isEmpty()) {
            return false;
        }

        // Fetch question from database
        QuestionEntity question = questionRepository.findById(questionId).orElse(null);
        if (question == null || question.getAnswerData() == null) {
            log.warn("Question not found or has no answer data: {}", questionId);
            return false;
        }

        // Parse question to get answer options with correct flags
        QuestionResponse questionResponse = QuestionResponse.from(question);
        List<QuestionResponse.AnswerData> answerOptions = questionResponse.getAnswerData();

        if (answerOptions == null || answerOptions.isEmpty()) {
            log.warn("Question {} has no parsed answer options", questionId);
            return false;
        }

        // Validate array sizes match
        if (userAnswerData.size() != answerOptions.size()) {
            log.warn("Answer array size mismatch: user={}, question={} for questionId={}",
                    userAnswerData.size(), answerOptions.size(), questionId);
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
            boolean userSelected = i < userAnswerData.size() && Boolean.TRUE.equals(userAnswerData.get(i));

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
                questionId, correctCount, userCorrectSelections, userIncorrectSelections, isCorrect);

        return isCorrect;
    }
}
