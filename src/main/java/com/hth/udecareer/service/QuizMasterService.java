package com.hth.udecareer.service;

import static com.hth.udecareer.utils.StreamUtil.safeStream;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.Arrays;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.model.response.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hth.udecareer.entities.PostMeta;
import com.hth.udecareer.entities.QuestionEntity;
import com.hth.udecareer.entities.QuizCategoryEntity;
import com.hth.udecareer.entities.QuizMaster;
import com.hth.udecareer.entities.QuizStatisticEntity;
import com.hth.udecareer.entities.QuizStatisticId;
import com.hth.udecareer.entities.QuizStatisticRefEntity;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.entities.UserActivityEntity;
import com.hth.udecareer.entities.UserActivityMetaEntity;
import com.hth.udecareer.entities.UserPurchasedEntity;
import com.hth.udecareer.enums.PostStatus;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.dto.PurchasedDto;
import com.hth.udecareer.model.dto.QuizDto;
import com.hth.udecareer.model.dto.RevenueCatSubscriberDto;
import com.hth.udecareer.model.request.SubmitAnswerRequest;
import com.hth.udecareer.model.request.SubmitAnswerRequest.AnsweredData;
import com.hth.udecareer.model.response.QuestionResponse.AnswerData;
import com.hth.udecareer.repository.PostMetaRepository;
import com.hth.udecareer.repository.QuestionRepository;
import com.hth.udecareer.repository.QuizMasterRepository;
import com.hth.udecareer.repository.QuizStatisticRefRepository;
import com.hth.udecareer.repository.QuizStatisticRepository;
import com.hth.udecareer.repository.UserActivityMetaRepository;
import com.hth.udecareer.repository.UserActivityRepository;
import com.hth.udecareer.repository.UserPurchasedRepository;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.utils.BooleanUtil;
import com.hth.udecareer.utils.PostMetaUtil;
import com.hth.udecareer.repository.QuizCategoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizMasterService {
        private final QuizMasterRepository quizMasterRepository;
        private final QuestionRepository questionRepository;
        private final PostMetaRepository postMetaRepository;
        private final UserActivityRepository userActivityRepository;
        private final UserActivityMetaRepository userActivityMetaRepository;
        private final UserRepository userRepository;
        private final QuizStatisticRefRepository quizStatisticRefRepository;
        private final QuizStatisticRepository quizStatisticRepository;
        private final RevenueCatService revenueCatService;
        private final QuizCategoryService quizCategoryService;
        private final UserPurchasedRepository userPurchasedRepository;
        private final QuizCategoryRepository quizCategoryRepository;


    public List<QuizResponse> searchQuiz(String email, String category, String categoryCode, String typeTest) throws AppException {
        final User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

        // Logic mới: Ưu tiên categoryCode, nếu không có thì dùng category
        final String categoryTitle;
        if (!ObjectUtils.isEmpty(categoryCode)) {
            // Lấy title từ code
            Optional<QuizCategoryEntity> categoryEntity = quizCategoryRepository.findByCode(categoryCode);
            if (categoryEntity.isEmpty()) {
                // Don't throw exception for unknown category codes (e.g., career-scrum-master)
                // Just return empty results
                log.warn("Category code not found, returning empty results: {}", categoryCode);
                categoryTitle = null;
            } else {
                categoryTitle = categoryEntity.get().getTitle();
                log.info("Using categoryCode '{}' -> title '{}'", categoryCode, categoryTitle);
            }
        } else if (!ObjectUtils.isEmpty(category)) {
            // Dùng category (title) như cũ (backward compatible)
            categoryTitle = category.trim();
            log.info("Using category (title) '{}'", categoryTitle);
        } else {
            categoryTitle = null;
        }

        final List<QuizDto> quizDtoList =
                quizMasterRepository.findActiveQuizzes(List.of(PostStatus.PUBLISH, PostStatus.PRIVATE),
                                "sfwd-quiz")
                        .stream()
                        .filter(quiz -> {
                            boolean flag = ObjectUtils.isEmpty(categoryTitle) || quiz.isCategory(
                                    categoryTitle);

                            if (!ObjectUtils.isEmpty(typeTest)) {
                                if ("mini".equals(typeTest)) {
                                    if (!quiz.isMiniTest()) {
                                        flag = false;
                                    }
                                } else {
                                    if (quiz.isMiniTest()) {
                                        flag = false;
                                    }
                                }
                            }

                            return flag;
                        })
                        .toList();
        final List<Long> postIds = quizDtoList.stream().map(QuizDto::getPostId).toList();
        final List<QuizResponse> responses = safeStream(quizDtoList).map(QuizResponse::from).toList();

        final Map<Long, List<Long>> postIdToQuestionIds =
                postMetaRepository.findAllByPostIdInAndMetaKey(postIds, "ld_quiz_questions")
                        .stream()
                        .collect(toMap(PostMeta::getPostId, x -> getQuestionIds(x.getMetaValue())));

        final List<PostMeta> postMetas = postMetaRepository.findAllByPostIdInAndMetaKey(postIds, "_sfwd-quiz");
        final Map<Long, String> postIdToQuizMeta = postMetas
                .stream()
                .collect(toMap(PostMeta::getPostId, PostMeta::getMetaValue));

        // Lấy tất cả activities để tách riêng completed và draft
        final List<UserActivityEntity> allActivities = postIds.isEmpty()
                        ? Collections.emptyList()
                        : userActivityRepository.findAllActivitiesByUserIdAndPostIds(user.getId(), postIds);
        
        // Tách riêng completed activities (đã chấm điểm) và draft activities
        final Map<Long, UserActivityEntity> postIdToCompletedActivity = new HashMap<>();
        final Map<Long, UserActivityEntity> postIdToDraftActivity = new HashMap<>();
        
        // Lấy metadata cho tất cả activities
        final List<Long> allActivityIds = allActivities.stream()
                        .map(UserActivityEntity::getId)
                        .collect(Collectors.toList());
        final List<UserActivityMetaEntity> allActivityMetaList = allActivityIds.isEmpty()
                        ? Collections.emptyList()
                        : userActivityMetaRepository.findAllByActivityIdIn(allActivityIds);
        final Map<Long, Map<String, String>> allActivityMetaMap = allActivityMetaList
                        .stream()
                        .collect(groupingBy(UserActivityMetaEntity::getActivityId,
                                        toMap(UserActivityMetaEntity::getActivityMetaKey,
                                                        UserActivityMetaEntity::getActivityMetaValue)));
        
        // Phân loại activities
        for (UserActivityEntity activity : allActivities) {
                final Map<String, String> metaMap = allActivityMetaMap.get(activity.getId());
                if (metaMap != null) {
                        String isDraftValue = metaMap.get("is_draft");
                        String hasGradedValue = metaMap.get("has_graded");
                        boolean isDraft = "1".equals(isDraftValue) && "0".equals(hasGradedValue);
                        
                        // Completed activity: đã nộp và đã chấm điểm
                        if (activity.getActivityCompleted() != null && activity.getActivityCompleted() > 0
                                        && ("1".equals(hasGradedValue) || !isDraft)) {
                                // Lấy completed activity gần nhất (theo activityCompleted)
                                UserActivityEntity existing = postIdToCompletedActivity.get(activity.getPostId());
                                if (existing == null || (activity.getActivityCompleted() != null 
                                                && existing.getActivityCompleted() != null
                                                && activity.getActivityCompleted() > existing.getActivityCompleted())) {
                                        postIdToCompletedActivity.put(activity.getPostId(), activity);
                                }
                        }
                        
                        // Draft activity: chưa nộp hoặc chưa chấm điểm
                        if (isDraft) {
                                // Lấy draft activity mới nhất (theo id)
                                UserActivityEntity existing = postIdToDraftActivity.get(activity.getPostId());
                                if (existing == null || activity.getId() > existing.getId()) {
                                        postIdToDraftActivity.put(activity.getPostId(), activity);
                                }
                        }
                }
        }
        
        // Lấy metadata cho completed activities (để lấy kết quả)
        final List<Long> completedActivityIds = new ArrayList<>(postIdToCompletedActivity.values().stream()
                        .map(UserActivityEntity::getId)
                        .collect(Collectors.toList()));
        final List<UserActivityMetaEntity> completedMetaList = completedActivityIds.isEmpty()
                        ? Collections.emptyList()
                        : userActivityMetaRepository.findAllByActivityIdIn(completedActivityIds);
        final Map<Long, Map<String, String>> completedMetaMap = completedMetaList
                        .stream()
                        .collect(groupingBy(UserActivityMetaEntity::getActivityId,
                                        toMap(UserActivityMetaEntity::getActivityMetaKey,
                                                        UserActivityMetaEntity::getActivityMetaValue)));
        
        // Lấy metadata cho draft activities (để lấy thông tin draft)
        final List<Long> draftActivityIds = new ArrayList<>(postIdToDraftActivity.values().stream()
                        .map(UserActivityEntity::getId)
                        .collect(Collectors.toList()));
        final List<UserActivityMetaEntity> draftMetaList = draftActivityIds.isEmpty()
                        ? Collections.emptyList()
                        : userActivityMetaRepository.findAllByActivityIdIn(draftActivityIds);
        final Map<Long, Map<String, String>> draftMetaMap = draftMetaList
                        .stream()
                        .collect(groupingBy(UserActivityMetaEntity::getActivityId,
                                        toMap(UserActivityMetaEntity::getActivityMetaKey,
                                                        UserActivityMetaEntity::getActivityMetaValue)));
        
        // Lấy kết quả từ completed activities
        final Map<Long, Long> postIdToScore = new HashMap<>();
        final Map<Long, Long> postIdToPass = new HashMap<>();
        final Map<Long, Long> statisticRefIdToPostId = new HashMap<>();
        final Map<Long, Long> postIdToPoint = new HashMap<>();
        final Map<Long, Double> postIdToPercentage = new HashMap<>();
        final Map<Long, Long> postIdToAnsweredCount = new HashMap<>();
        final Map<Long, Boolean> postIdToIsDraft = new HashMap<>();
        final Map<Long, Map<String, Object>> postIdToSavedAnswers = new HashMap<>();
        
        responses.forEach(quizResponse -> {
                // Lấy kết quả từ completed activity
                final UserActivityEntity completedActivity = postIdToCompletedActivity.get(quizResponse.getPostId());
                if (Objects.nonNull(completedActivity)) {
                        final Map<String, String> metaMap = completedMetaMap.get(completedActivity.getId());
                        if (metaMap != null) {
                                final Long statisticRefId = getValue(metaMap.get("statistic_ref_id"), Long.class);
                                if (Objects.nonNull(statisticRefId)) {
                                        statisticRefIdToPostId.put(statisticRefId, quizResponse.getPostId());
                                }
                                postIdToScore.put(quizResponse.getPostId(), getValue(metaMap.get("score"), Long.class));
                                postIdToPass.put(quizResponse.getPostId(), getValue(metaMap.get("pass"), Long.class));
                                postIdToPoint.put(quizResponse.getPostId(), getValue(metaMap.get("points"), Long.class));
                                postIdToPercentage.put(quizResponse.getPostId(),
                                                getValue(metaMap.get("percentage"), Double.class));
                                postIdToAnsweredCount.put(quizResponse.getPostId(),
                                                getValue(metaMap.get("answeredCount"), Long.class));
                        }
                }
                
                // Kiểm tra draft và lấy thông tin draft
                final UserActivityEntity draftActivity = postIdToDraftActivity.get(quizResponse.getPostId());
                if (Objects.nonNull(draftActivity)) {
                        // Kiểm tra xem draft đã được nộp chưa
                        boolean draftWasSubmitted = false;
                        if (completedActivity != null && completedActivity.getId() > draftActivity.getId()) {
                                draftWasSubmitted = true;
                        }
                        postIdToIsDraft.put(quizResponse.getPostId(), !draftWasSubmitted);
                        
                        // Lấy thông tin draft nếu chưa được nộp (không lấy answers, chỉ lấy thông tin chung)
                        if (!draftWasSubmitted) {
                                final Map<String, String> draftMeta = draftMetaMap.get(draftActivity.getId());
                                if (draftMeta != null) {
                                        // Lấy các thông tin khác từ metadata (không lấy answers)
                                        Long activityStartTime = draftActivity.getActivityStarted();
                                        Long elapsedTime = getValue(draftMeta.get("elapsed_time"), Long.class);
                                        Long answeredCount = getValue(draftMeta.get("answeredCount"), Long.class);
                                        
                                        // Tạo Map chứa thông tin draft (không có answers)
                                        Map<String, Object> savedAnswersMap = new HashMap<>();
                                        if (activityStartTime != null) {
                                                savedAnswersMap.put("activityStartTime", activityStartTime);
                                        }
                                        if (elapsedTime != null) {
                                                savedAnswersMap.put("elapsedTime", elapsedTime);
                                        }
                                        if (answeredCount != null) {
                                                savedAnswersMap.put("answeredCount", answeredCount);
                                        }
                                        
                                        postIdToSavedAnswers.put(quizResponse.getPostId(), savedAnswersMap);
                                }
                        }
                } else {
                        postIdToIsDraft.put(quizResponse.getPostId(), false);
                }
        });

        final Multimap<Long, QuizStatisticEntity> multimap = ArrayListMultimap.create();
        final List<QuizStatisticEntity> quizStatisticEntityList =
                quizStatisticRepository.findAllById_StatisticRefIdIn(statisticRefIdToPostId.keySet());
        quizStatisticEntityList.forEach(quizStatisticEntity -> {
            final Long postId = statisticRefIdToPostId.get(quizStatisticEntity.getId().getStatisticRefId());
            multimap.put(postId, quizStatisticEntity);
        });

        final Map<Long, Long> postIdToCorrect = new HashMap<>();
        final Map<Long, Long> postIdToActualAnsweredCount = new HashMap<>();
        responses.forEach(quizResponse -> {
            final Collection<QuizStatisticEntity> quizStatisticEntities = multimap.get(
                    quizResponse.getPostId());

            final Long corrects = quizStatisticEntities.stream()
                    .filter(Objects::nonNull)
                    .mapToLong(QuizStatisticEntity::getCorrectCount)
                    .sum();

            postIdToCorrect.put(quizResponse.getPostId(), corrects);
            
            // Đếm số câu đã trả lời từ QuizStatisticEntity (có answerData không rỗng)
            final Long actualAnsweredCount = quizStatisticEntities.stream()
                    .filter(Objects::nonNull)
                    .filter(stat -> {
                            String answerData = stat.getAnswerData();
                            return answerData != null && !answerData.isEmpty() 
                                    && !answerData.equals("[]") && !answerData.trim().equals("[]");
                    })
                    .count();
            
            // Nếu đếm được > 0 thì dùng giá trị đếm được, nếu không thì dùng giá trị từ metadata
            if (actualAnsweredCount > 0) {
                    postIdToActualAnsweredCount.put(quizResponse.getPostId(), actualAnsweredCount);
            }
        });

        responses.forEach(quizResponse -> {
            final List<Long> questionIds = postIdToQuestionIds.get(quizResponse.getPostId());
            final List<QuestionEntity> questionEntities = questionRepository.findAllByIdIn(questionIds);

            quizResponse.setQuestions(Objects.isNull(questionIds) ? 0L : questionIds.size());
            quizResponse.setTotalPoints(questionEntities.stream().mapToLong(QuestionEntity::getPoints).sum());

            final String quizMeta = postIdToQuizMeta.get(quizResponse.getPostId());
            if (Objects.nonNull(quizMeta)) {
                final Map<String, Object> quizMetaMap = PostMetaUtil.getPostMetaValues(quizMeta);
                final Object passingPercentage = quizMetaMap.get("sfwd-quiz_passingpercentage");

                if (Objects.nonNull(passingPercentage)) {
                    quizResponse.setPassingPercentage(NumberUtils.toInt(passingPercentage.toString().strip(), 0));
                }
            }

            quizResponse.setAnsweredScore(postIdToScore.get(quizResponse.getPostId()));
            quizResponse.setAnsweredPoints(postIdToPoint.get(quizResponse.getPostId()));
            quizResponse.setAnsweredCorrects(postIdToCorrect.get(quizResponse.getPostId()));
            quizResponse.setPass(postIdToPass.get(quizResponse.getPostId()));
            quizResponse.setPercentage(postIdToPercentage.get(quizResponse.getPostId()));
            
            // Đếm số câu đã làm: ưu tiên đếm từ QuizStatisticEntity, nếu không có thì dùng từ metadata
            Long answeredQuestions = postIdToActualAnsweredCount.get(quizResponse.getPostId());
            if (answeredQuestions == null || answeredQuestions == 0) {
                    answeredQuestions = postIdToAnsweredCount.get(quizResponse.getPostId());
            }
            // Nếu vẫn null hoặc 0, kiểm tra answeredPoints
            if (answeredQuestions == null || answeredQuestions == 0) {
                    Long answeredPoints = postIdToPoint.get(quizResponse.getPostId());
                    if (answeredPoints != null && answeredPoints > 0) {
                            answeredQuestions = answeredPoints;
                    }
            }
            // Nếu vẫn null hoặc 0 → chưa làm bài
            if (answeredQuestions != null && answeredQuestions == 0) {
                    answeredQuestions = null;
            }
            quizResponse.setAnsweredQuestions(answeredQuestions);
            
            quizResponse.setIsDraft(postIdToIsDraft.getOrDefault(quizResponse.getPostId(), false));
            quizResponse.setSavedAnswers(postIdToSavedAnswers.get(quizResponse.getPostId()));
            
            // Populate category cho mỗi quiz
            final QuizDto quizDto = quizDtoList.stream()
                    .filter(q -> q.getPostId().equals(quizResponse.getPostId()))
                    .findFirst()
                    .orElse(null);
            if (quizDto != null) {
                    final Optional<CategoryResponse> categoryOpt = quizCategoryService.getCategory(quizDto);
                    if (categoryOpt.isPresent()) {
                            final CategoryResponse categoryResponse = categoryOpt.get();
                            quizResponse.setCategory(CategorySimple.builder()
                                    .code(categoryResponse.getCode())
                                    .title(categoryResponse.getTitle())
                                    .build());
                    }
            }
        });

        return responses;
    }

    public Page<QuizResponse> searchQuizPaged(String email, String category, String categoryCode, String quizType,
                                         Integer minTimeLimit, Integer maxTimeLimit, Long courseId,
                                         Pageable pageable) throws AppException {
        final User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

                // Logic mới: Ưu tiên categoryCode, nếu không có thì dùng category
                String categoryTitle = null;
                final boolean useCategory;
                
                if (!ObjectUtils.isEmpty(categoryCode)) {
                    // Lấy title từ code
                    Optional<QuizCategoryEntity> categoryEntity = quizCategoryRepository.findByCode(categoryCode);
                    if (categoryEntity.isEmpty()) {
                        throw new AppException(ErrorCode.INVALID_KEY, "Category code not found: " + categoryCode);
                    }
                    categoryTitle = categoryEntity.get().getTitle();
                    useCategory = true;
                    log.info("Using categoryCode '{}' -> title '{}'", categoryCode, categoryTitle);
                } else if (!ObjectUtils.isEmpty(category)) {
                    // Dùng category (title) như cũ (backward compatible)
                    categoryTitle = category.trim();
                    if (!quizCategoryRepository.existsByTitle(categoryTitle)) {
                        throw new AppException(ErrorCode.INVALID_KEY, "Category title not found: " + categoryTitle);
                    }
                    useCategory = true;
                    log.info("Using category (title) '{}'", categoryTitle);
                } else {
                    useCategory = false;
                }
                
                String categoryLike = null;
                if (useCategory && categoryTitle != null) {
                    String lowerCategory = categoryTitle.toLowerCase();
                    categoryLike = lowerCategory + " %";
                }

                final boolean onlyMini = "mini".equalsIgnoreCase(quizType);
                final boolean excludeMini = "full".equalsIgnoreCase(quizType);
                final String miniLike = "%- mini -%";
                final String courseIdValue = (courseId != null) ? String.valueOf(courseId) : null;


                final boolean useMinTimeLimit = minTimeLimit != null;
                final boolean useMaxTimeLimit = maxTimeLimit != null;

                final Page<QuizDto> quizDtoPage = quizMasterRepository.findActiveQuizzesPage(
                                List.of(PostStatus.PUBLISH, PostStatus.PRIVATE),
                                "sfwd-quiz",
                                useCategory,
                                categoryLike,
                                onlyMini,
                                excludeMini,
                                miniLike,
                                useMinTimeLimit,
                                minTimeLimit,
                                useMaxTimeLimit,
                                maxTimeLimit,
                                courseIdValue,
                                pageable);

                final List<Long> postIds = quizDtoPage.getContent().stream().map(QuizDto::getPostId).toList();
                final List<QuizResponse> responses = safeStream(quizDtoPage.getContent()).map(QuizResponse::from)
                                .toList();

                final Map<Long, List<Long>> postIdToQuestionIds = postMetaRepository
                                .findAllByPostIdInAndMetaKey(postIds, "ld_quiz_questions")
                                .stream()
                                .collect(toMap(PostMeta::getPostId, x -> getQuestionIds(x.getMetaValue())));

                final List<PostMeta> postMetas = postMetaRepository.findAllByPostIdInAndMetaKey(postIds, "_sfwd-quiz");
                final Map<Long, String> postIdToQuizMeta = postMetas
                                .stream()
                                .collect(toMap(PostMeta::getPostId, PostMeta::getMetaValue));

                // Lấy tất cả activities để tách riêng completed và draft
                final List<UserActivityEntity> allActivities = postIds.isEmpty()
                                ? Collections.emptyList()
                                : userActivityRepository.findAllActivitiesByUserIdAndPostIds(user.getId(), postIds);
                
                // Tách riêng completed activities (đã chấm điểm) và draft activities
                final Map<Long, UserActivityEntity> postIdToCompletedActivity = new HashMap<>();
                final Map<Long, UserActivityEntity> postIdToDraftActivity = new HashMap<>();
                
                // Lấy metadata cho tất cả activities
                final List<Long> allActivityIds = allActivities.stream()
                                .map(UserActivityEntity::getId)
                                .collect(Collectors.toList());
                final List<UserActivityMetaEntity> allActivityMetaList = allActivityIds.isEmpty()
                                ? Collections.emptyList()
                                : userActivityMetaRepository.findAllByActivityIdIn(allActivityIds);
                final Map<Long, Map<String, String>> allActivityMetaMap = allActivityMetaList
                                .stream()
                                .collect(groupingBy(UserActivityMetaEntity::getActivityId,
                                                toMap(UserActivityMetaEntity::getActivityMetaKey,
                                                                UserActivityMetaEntity::getActivityMetaValue)));
                
                // Phân loại activities
                for (UserActivityEntity activity : allActivities) {
                        final Map<String, String> metaMap = allActivityMetaMap.get(activity.getId());
                        if (metaMap != null) {
                                String isDraftValue = metaMap.get("is_draft");
                                String hasGradedValue = metaMap.get("has_graded");
                                boolean isDraft = "1".equals(isDraftValue) && "0".equals(hasGradedValue);
                                
                                // Completed activity: đã nộp và đã chấm điểm
                                if (activity.getActivityCompleted() != null && activity.getActivityCompleted() > 0
                                                && ("1".equals(hasGradedValue) || !isDraft)) {
                                        // Lấy completed activity gần nhất (theo activityCompleted)
                                        UserActivityEntity existing = postIdToCompletedActivity.get(activity.getPostId());
                                        if (existing == null || (activity.getActivityCompleted() != null 
                                                        && existing.getActivityCompleted() != null
                                                        && activity.getActivityCompleted() > existing.getActivityCompleted())) {
                                                postIdToCompletedActivity.put(activity.getPostId(), activity);
                                        }
                                }
                                
                                // Draft activity: chưa nộp hoặc chưa chấm điểm
                                if (isDraft) {
                                        // Lấy draft activity mới nhất (theo id)
                                        UserActivityEntity existing = postIdToDraftActivity.get(activity.getPostId());
                                        if (existing == null || activity.getId() > existing.getId()) {
                                                postIdToDraftActivity.put(activity.getPostId(), activity);
                                        }
                                }
                        }
                }
                
                // Lấy metadata cho completed activities (để lấy kết quả)
                final List<Long> completedActivityIds = new ArrayList<>(postIdToCompletedActivity.values().stream()
                                .map(UserActivityEntity::getId)
                                .collect(Collectors.toList()));
                final List<UserActivityMetaEntity> completedMetaList = completedActivityIds.isEmpty()
                                ? Collections.emptyList()
                                : userActivityMetaRepository.findAllByActivityIdIn(completedActivityIds);
                final Map<Long, Map<String, String>> completedMetaMap = completedMetaList
                                .stream()
                                .collect(groupingBy(UserActivityMetaEntity::getActivityId,
                                                toMap(UserActivityMetaEntity::getActivityMetaKey,
                                                                UserActivityMetaEntity::getActivityMetaValue)));
                
                // Lấy metadata cho draft activities (để lấy thông tin draft)
                final List<Long> draftActivityIds = new ArrayList<>(postIdToDraftActivity.values().stream()
                                .map(UserActivityEntity::getId)
                                .collect(Collectors.toList()));
                final List<UserActivityMetaEntity> draftMetaList = draftActivityIds.isEmpty()
                                ? Collections.emptyList()
                                : userActivityMetaRepository.findAllByActivityIdIn(draftActivityIds);
                final Map<Long, Map<String, String>> draftMetaMap = draftMetaList
                                .stream()
                                .collect(groupingBy(UserActivityMetaEntity::getActivityId,
                                                toMap(UserActivityMetaEntity::getActivityMetaKey,
                                                                UserActivityMetaEntity::getActivityMetaValue)));

                // Lấy kết quả từ completed activities
                final Map<Long, Long> postIdToScore = new HashMap<>();
                final Map<Long, Long> postIdToPass = new HashMap<>();
                final Map<Long, Long> statisticRefIdToPostId = new HashMap<>();
                final Map<Long, Long> postIdToPoint = new HashMap<>();
                final Map<Long, Double> postIdToPercentage = new HashMap<>();
                final Map<Long, Long> postIdToAnsweredCount = new HashMap<>();
                final Map<Long, Boolean> postIdToIsDraft = new HashMap<>();
                final Map<Long, Map<String, Object>> postIdToSavedAnswers = new HashMap<>();
                
                responses.forEach(quizResponse -> {
                        // Lấy kết quả từ completed activity
                        final UserActivityEntity completedActivity = postIdToCompletedActivity.get(quizResponse.getPostId());
                        if (Objects.nonNull(completedActivity)) {
                                final Map<String, String> metaMap = completedMetaMap.get(completedActivity.getId());
                                if (metaMap != null) {
                                        final Long statisticRefId = getValue(metaMap.get("statistic_ref_id"), Long.class);
                                        if (Objects.nonNull(statisticRefId)) {
                                                statisticRefIdToPostId.put(statisticRefId, quizResponse.getPostId());
                                        }
                                        postIdToScore.put(quizResponse.getPostId(), getValue(metaMap.get("score"), Long.class));
                                        postIdToPass.put(quizResponse.getPostId(), getValue(metaMap.get("pass"), Long.class));
                                        postIdToPoint.put(quizResponse.getPostId(), getValue(metaMap.get("points"), Long.class));
                                        postIdToPercentage.put(quizResponse.getPostId(),
                                                        getValue(metaMap.get("percentage"), Double.class));
                                        postIdToAnsweredCount.put(quizResponse.getPostId(),
                                                        getValue(metaMap.get("answeredCount"), Long.class));
                                }
                        }
                        
                        // Kiểm tra draft và lấy thông tin draft
                        final UserActivityEntity draftActivity = postIdToDraftActivity.get(quizResponse.getPostId());
                        if (Objects.nonNull(draftActivity)) {
                                // Kiểm tra xem draft đã được nộp chưa
                                boolean draftWasSubmitted = false;
                                if (completedActivity != null && completedActivity.getId() > draftActivity.getId()) {
                                        draftWasSubmitted = true;
                                }
                                postIdToIsDraft.put(quizResponse.getPostId(), !draftWasSubmitted);
                                
                                // Lấy thông tin draft nếu chưa được nộp (không lấy answers, chỉ lấy thông tin chung)
                                if (!draftWasSubmitted) {
                                        final Map<String, String> draftMeta = draftMetaMap.get(draftActivity.getId());
                                        if (draftMeta != null) {
                                                // Lấy các thông tin khác từ metadata (không lấy answers)
                                                Long activityStartTime = draftActivity.getActivityStarted();
                                                Long elapsedTime = getValue(draftMeta.get("elapsed_time"), Long.class);
                                                Long answeredCount = getValue(draftMeta.get("answeredCount"), Long.class);
                                                
                                                // Tạo Map chứa thông tin draft (không có answers)
                                                Map<String, Object> savedAnswersMap = new HashMap<>();
                                                if (activityStartTime != null) {
                                                        savedAnswersMap.put("activityStartTime", activityStartTime);
                                                }
                                                if (elapsedTime != null) {
                                                        savedAnswersMap.put("elapsedTime", elapsedTime);
                                                }
                                                if (answeredCount != null) {
                                                        savedAnswersMap.put("answeredCount", answeredCount);
                                                }
                                                
                                                postIdToSavedAnswers.put(quizResponse.getPostId(), savedAnswersMap);
                                        }
                                }
                        } else {
                                postIdToIsDraft.put(quizResponse.getPostId(), false);
                        }
                });

                final Multimap<Long, QuizStatisticEntity> multimap = ArrayListMultimap.create();
                final List<QuizStatisticEntity> quizStatisticEntityList = quizStatisticRepository
                                .findAllById_StatisticRefIdIn(statisticRefIdToPostId.keySet());
                quizStatisticEntityList.forEach(quizStatisticEntity -> {
                        final Long postId = statisticRefIdToPostId.get(quizStatisticEntity.getId().getStatisticRefId());
                        multimap.put(postId, quizStatisticEntity);
                });

                final Map<Long, Long> postIdToCorrect = new HashMap<>();
                final Map<Long, Long> postIdToActualAnsweredCount = new HashMap<>();
                responses.forEach(quizResponse -> {
                        final Collection<QuizStatisticEntity> quizStatisticEntities = multimap.get(
                                        quizResponse.getPostId());

                        final Long corrects = quizStatisticEntities.stream()
                                        .filter(Objects::nonNull)
                                        .mapToLong(QuizStatisticEntity::getCorrectCount)
                                        .sum();

                        postIdToCorrect.put(quizResponse.getPostId(), corrects);
                        
                        // Đếm số câu đã trả lời từ QuizStatisticEntity (có answerData không rỗng)
                        final Long actualAnsweredCount = quizStatisticEntities.stream()
                                        .filter(Objects::nonNull)
                                        .filter(stat -> {
                                                String answerData = stat.getAnswerData();
                                                return answerData != null && !answerData.isEmpty() 
                                                        && !answerData.equals("[]") && !answerData.trim().equals("[]");
                                        })
                                        .count();
                        
                        // Nếu đếm được > 0 thì dùng giá trị đếm được, nếu không thì dùng giá trị từ metadata
                        if (actualAnsweredCount > 0) {
                                postIdToActualAnsweredCount.put(quizResponse.getPostId(), actualAnsweredCount);
                        }
                });

                final List<Long> allQuestionIdsOnPage = postIdToQuestionIds.values().stream()
                .flatMap(List::stream)
                .distinct()
                .toList();

                final Map<Long, QuestionEntity> questionMap = questionRepository.findAllByIdIn(allQuestionIdsOnPage)
                .stream()
                .collect(toMap(QuestionEntity::getId, Function.identity()));

                // ======================= PERFORMANCE OPTIMIZATION =======================
                // BATCH LOAD PURCHASED STATUS - Tránh N+1 queries
                // Lấy tất cả category codes unique từ quizzes
                final Set<String> uniqueCategoryCodes = quizDtoPage.getContent().stream()
                        .map(quizDto -> {
                                final Optional<CategoryResponse> categoryOpt = quizCategoryService.getCategory(quizDto);
                                return categoryOpt.map(CategoryResponse::getCode).orElse(null);
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                log.debug("Batch loading purchased status for {} unique categories: {}",
                        uniqueCategoryCodes.size(), uniqueCategoryCodes);

                // Query DB một lần duy nhất cho tất cả user purchases
                final List<UserPurchasedEntity> allUserPurchases = userPurchasedRepository
                        .findAllByUserIdOrUserEmail(user.getId(), email);
                final Map<String, UserPurchasedEntity> categoryToPurchasedEntity = allUserPurchases.stream()
                        .collect(toMap(UserPurchasedEntity::getCategoryCode, Function.identity(), (o1, o2) -> o2));

                // Pre-calculate purchased status cho tất cả categories
                final Map<String, Boolean> categoryToPurchasedStatus = new HashMap<>();

                for (String catCode : uniqueCategoryCodes) {
                        boolean isPurchased = false;
                        try {
                                // Check database purchase
                                UserPurchasedEntity purchasedEntity = categoryToPurchasedEntity.get(catCode);
                                if (purchasedEntity == null) {
                                        purchasedEntity = categoryToPurchasedEntity.get("all"); // fallback to "all"
                                }

                                boolean dbPurchased = false;
                                if (purchasedEntity != null && Objects.nonNull(purchasedEntity.getIsPurchased())
                                        && purchasedEntity.getIsPurchased() > 0) {
                                        final LocalDateTime currTime = LocalDateTime.now();
                                        if ((Objects.isNull(purchasedEntity.getFromTime()) || currTime.isAfter(purchasedEntity.getFromTime()))
                                                && (Objects.isNull(purchasedEntity.getToTime()) || currTime.isBefore(purchasedEntity.getToTime()))) {
                                                dbPurchased = true;
                                        }
                                }

                                // Check RevenueCat if needed (only if not purchased from DB)
                                boolean revenuePurchased = false;
                                if (!dbPurchased) {
                                        final Optional<QuizCategoryEntity> categoryEntityOpt = quizCategoryRepository.findByCode(catCode);
                                        if (categoryEntityOpt.isPresent()) {
                                                final CategoryResponse categoryResp = CategoryResponse.builder()
                                                        .code(categoryEntityOpt.get().getCode())
                                                        .title(categoryEntityOpt.get().getTitle())
                                                        .build();
                                                final PurchasedDto revenuePurchasedDto = getPurchasedInfoFromRevenue(email, categoryResp);
                                                revenuePurchased = revenuePurchasedDto.isPurchased()
                                                        && (Objects.isNull(revenuePurchasedDto.getRemainDays()) || revenuePurchasedDto.getRemainDays() > 0);
                                        }
                                }

                                isPurchased = dbPurchased || revenuePurchased;
                        } catch (Exception e) {
                                log.warn("Failed to check purchased status for category '{}': {}", catCode, e.getMessage());
                        }

                        categoryToPurchasedStatus.put(catCode, isPurchased);
                        log.debug("Category '{}' purchased status: {}", catCode, isPurchased);
                }
                // ======================= END OPTIMIZATION =======================

                responses.forEach(quizResponse -> {
                        final List<Long> questionIds = postIdToQuestionIds.getOrDefault(quizResponse.getPostId(), Collections.emptyList());

                        final List<QuestionEntity> questionEntities = questionIds.stream()
                                .map(questionMap::get)
                                .filter(Objects::nonNull)
                                .toList();

                        quizResponse.setTotalPoints(
                                        questionEntities.stream().mapToLong(QuestionEntity::getPoints).sum());

                        final String quizMeta = postIdToQuizMeta.get(quizResponse.getPostId());
                        if (Objects.nonNull(quizMeta)) {
                                final Map<String, Object> quizMetaMap = PostMetaUtil.getPostMetaValues(quizMeta);
                                final Object passingPercentage = quizMetaMap.get("sfwd-quiz_passingpercentage");

                                if (Objects.nonNull(passingPercentage)) {
                                        quizResponse.setPassingPercentage(
                                                        NumberUtils.toInt(passingPercentage.toString().strip(), 0));
                                }
                        }

                        quizResponse.setAnsweredScore(postIdToScore.get(quizResponse.getPostId()));
                        quizResponse.setAnsweredPoints(postIdToPoint.get(quizResponse.getPostId()));
                        quizResponse.setAnsweredCorrects(postIdToCorrect.get(quizResponse.getPostId()));
                        quizResponse.setPass(postIdToPass.get(quizResponse.getPostId()));
                        quizResponse.setPercentage(postIdToPercentage.get(quizResponse.getPostId()));
                        
                        // Đếm số câu đã làm: ưu tiên đếm từ QuizStatisticEntity, nếu không có thì dùng từ metadata
                        Long answeredQuestions = postIdToActualAnsweredCount.get(quizResponse.getPostId());
                        if (answeredQuestions == null || answeredQuestions == 0) {
                                answeredQuestions = postIdToAnsweredCount.get(quizResponse.getPostId());
                        }
                        // Nếu vẫn null hoặc 0, kiểm tra answeredPoints
                        if (answeredQuestions == null || answeredQuestions == 0) {
                                Long answeredPoints = postIdToPoint.get(quizResponse.getPostId());
                                if (answeredPoints != null && answeredPoints > 0) {
                                        answeredQuestions = answeredPoints;
                                }
                        }
                        // Nếu vẫn null hoặc 0 → chưa làm bài
                        if (answeredQuestions != null && answeredQuestions == 0) {
                                answeredQuestions = null;
                        }
                        quizResponse.setAnsweredQuestions(answeredQuestions);
                        
                        quizResponse.setIsDraft(postIdToIsDraft.getOrDefault(quizResponse.getPostId(), false));
                        quizResponse.setSavedAnswers(postIdToSavedAnswers.get(quizResponse.getPostId()));
                        
                        // Populate category cho mỗi quiz
                        final QuizDto quizDto = quizDtoPage.getContent().stream()
                                .filter(q -> q.getPostId().equals(quizResponse.getPostId()))
                                .findFirst()
                                .orElse(null);
                        if (quizDto != null) {
                        // 1. Populate Category (Giữ nguyên)
                                final Optional<CategoryResponse> categoryOpt = quizCategoryService.getCategory(quizDto);
                                if (categoryOpt.isPresent()) {
                                        final CategoryResponse categoryResponse = categoryOpt.get();
                                        quizResponse.setCategory(CategorySimple.builder()
                                                .code(categoryResponse.getCode())
                                                .title(categoryResponse.getTitle())
                                                .build());

                                        // 2. OPTIMIZED: Lookup purchased status from pre-loaded cache (map.get - tức thì)
                                        Boolean isPurchased = categoryToPurchasedStatus.get(categoryResponse.getCode());
                                        quizResponse.setIsPurchased(isPurchased != null ? isPurchased : false);
                                } else {
                                        // No category found, set purchased as false
                                        quizResponse.setIsPurchased(false);
                                }

                                String postTitle = quizDto.getPostTitle();
                                if (postTitle != null && postTitle.toLowerCase().contains("- mini -")) {
                                    quizResponse.setQuizType("mini");
                                } else {
                                    quizResponse.setQuizType("full");
                                }
                        } else {
                                // No quiz DTO found, set purchased as false
                                quizResponse.setIsPurchased(false);
                        }
                });

                return new PageImpl<>(responses, pageable, quizDtoPage.getTotalElements());
        }

        @Nullable
        @SuppressWarnings("unchecked")
        private static <T extends Number> T getValue(String str, Class<T> type) {
                try {
                        if (type.equals(Long.class)) {
                                return (T) Long.valueOf(str);
                        } else if (type.equals(Double.class)) {
                                return (T) Double.valueOf(str);
                        }
                } catch (Exception ex) {
                        log.warn(ex.getMessage(), ex);
                }
                return null;
        }

        public QuizInfoResponse getQuizInfo(String email, @NotNull Long quizId) throws Exception {
                final QuizMaster quizMaster = quizMasterRepository
                                .findById(quizId)
                                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));

                final QuizDto quizDto = quizMasterRepository
                                .findActiveQuizById(quizId, List.of(PostStatus.PUBLISH, PostStatus.PRIVATE))
                                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));

                final User user = userRepository
                                .findByEmail(email)
                                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));
                if (!isPermission(user.getEmail(), user.getId(), quizDto)) {
                        return QuizInfoResponse.from(quizDto);
                }

                List<QuestionEntity> questionEntities = Collections.emptyList();
                final List<Long> questionsIds = getQuestionIds(quizDto.getPostId());
                if (!CollectionUtils.isEmpty(questionsIds)) {
                        questionEntities = questionRepository.findAllByIdIn(questionsIds);
                }

                final List<QuestionResponse> questionResponses = questionEntities
                                .stream()
                                .map(item -> QuestionResponse.from(item, quizMaster.getAnswerRandom()))
                                .collect(Collectors.collectingAndThen(Collectors.toCollection(ArrayList::new),
                                                list -> {
                                                        // Shuffle questions - Currently disabled
                                                        // if (BooleanUtil.isTrue(quizMaster.getQuestionRandom())) {
                                                        //         Collections.shuffle(list);
                                                        // }
                                                        return list;
                                                }));

                // ========== PHẦN 1: Lấy kết quả bài thi gần nhất (completed, not draft) ==========
                Long answeredQuestions = null;
                Double percentage = null;
                Long answeredCorrects = null;
                Long answeredScore = null;
                Long pass = null;
                Long latestCompletedActivityId = null;

                // Lấy tất cả activities để tìm completed activity gần nhất
                final List<UserActivityEntity> allActivities = 
                        userActivityRepository.findAllActivitiesByUserIdAndPostIds(user.getId(), List.of(quizDto.getPostId()));
                
                // Lấy metadata cho tất cả activities
                final List<Long> allActivityIds = allActivities.stream()
                                .map(UserActivityEntity::getId)
                                .collect(Collectors.toList());
                final List<UserActivityMetaEntity> allActivityMetaList = allActivityIds.isEmpty()
                                ? Collections.emptyList()
                                : userActivityMetaRepository.findAllByActivityIdIn(allActivityIds);
                final Map<Long, Map<String, String>> allActivityMetaMap = allActivityMetaList
                                .stream()
                                .collect(groupingBy(UserActivityMetaEntity::getActivityId,
                                                toMap(UserActivityMetaEntity::getActivityMetaKey,
                                                                UserActivityMetaEntity::getActivityMetaValue)));
                
                // Tìm completed activity gần nhất (đã nộp và đã chấm điểm)
                UserActivityEntity completedActivity = null;
                for (UserActivityEntity activity : allActivities) {
                        final Map<String, String> metaMap = allActivityMetaMap.get(activity.getId());
                        if (metaMap != null) {
                                String isDraftValue = metaMap.get("is_draft");
                                String hasGradedValue = metaMap.get("has_graded");
                                boolean isDraft = "1".equals(isDraftValue) && "0".equals(hasGradedValue);
                                
                                // Completed activity: đã nộp và đã chấm điểm
                                if (activity.getActivityCompleted() != null && activity.getActivityCompleted() > 0
                                                && ("1".equals(hasGradedValue) || !isDraft)) {
                                        // Lấy completed activity gần nhất (theo activityCompleted)
                                        if (completedActivity == null || (activity.getActivityCompleted() != null 
                                                        && completedActivity.getActivityCompleted() != null
                                                        && activity.getActivityCompleted() > completedActivity.getActivityCompleted())) {
                                                completedActivity = activity;
                                        }
                                }
                        }
                }

                if (completedActivity != null) {
                    latestCompletedActivityId = completedActivity.getId();
                    log.info("Found latest completed activity (ID: {}) completed at {} for user {}, quizId {}", 
                        completedActivity.getId(), completedActivity.getActivityCompleted(), email, quizId);

                    final Map<String, String> completedMetaMap = allActivityMetaMap.get(completedActivity.getId());

                    if (completedMetaMap != null) {
                        // Lấy các trường thống kê từ metadata
                        answeredQuestions = getValue(completedMetaMap.get("answeredCount"), Long.class);
                        percentage = getValue(completedMetaMap.get("percentage"), Double.class);
                        answeredScore = getValue(completedMetaMap.get("score"), Long.class);
                        pass = getValue(completedMetaMap.get("pass"), Long.class);

                        // Lấy answeredCorrects từ QuizStatisticEntity
                        final Optional<UserActivityMetaEntity> statisticRefMetaOpt = 
                                userActivityMetaRepository.findByActivityIdAndActivityMetaKey(
                                    completedActivity.getId(), "statistic_ref_id");
                        if (statisticRefMetaOpt.isPresent()) {
                            Long statisticRefId = Long.parseLong(statisticRefMetaOpt.get().getActivityMetaValue());
                            final List<QuizStatisticEntity> stats = 
                                    quizStatisticRepository.findAllById_StatisticRefId(statisticRefId);
                            answeredCorrects = stats.stream()
                                    .filter(Objects::nonNull)
                                    .mapToLong(QuizStatisticEntity::getCorrectCount)
                                    .sum();
                            
                            // Đếm số câu đã làm từ QuizStatisticEntity (có answerData không rỗng)
                            final Long actualAnsweredCount = stats.stream()
                                    .filter(Objects::nonNull)
                                    .filter(stat -> {
                                            String answerData = stat.getAnswerData();
                                            return answerData != null && !answerData.isEmpty() 
                                                    && !answerData.equals("[]") && !answerData.trim().equals("[]");
                                    })
                                    .count();
                            
                            // Nếu đếm được > 0 thì dùng giá trị đếm được
                            if (actualAnsweredCount > 0) {
                                    answeredQuestions = actualAnsweredCount;
                            }
                        }
                    }
                }

                // ========== PHẦN 2: Lấy draft mới nhất (nếu có và chưa được nộp) ==========
                Map<Long, List<Integer>> answeredDataMap = Collections.emptyMap();
                Long activityStartTime = null;
                Long elapsedTime = null;

                final Optional<UserActivityEntity> draftActivityOpt =
                        userActivityRepository.findLatestUncompletedActivity(user.getId(), quizDto.getPostId());

                if (draftActivityOpt.isPresent()) {
                    final UserActivityEntity draftActivity = draftActivityOpt.get();
                    log.info("Found uncompleted activity (ID: {}) with status: {}, completed: {} for user {}, quizId {}",
                        draftActivity.getId(), draftActivity.getActivityStatus(), draftActivity.getActivityCompleted(), email, quizId);

                    final Optional<UserActivityMetaEntity> isDraftMetaOpt =
                            userActivityMetaRepository.findByActivityIdAndActivityMetaKey(draftActivity.getId(), "is_draft");
                    final Optional<UserActivityMetaEntity> hasGradedMetaOpt =
                            userActivityMetaRepository.findByActivityIdAndActivityMetaKey(draftActivity.getId(), "has_graded");

                    log.info("Draft metadata check - is_draft: {}, has_graded: {}",
                        isDraftMetaOpt.map(UserActivityMetaEntity::getActivityMetaValue).orElse("NOT_FOUND"),
                        hasGradedMetaOpt.map(UserActivityMetaEntity::getActivityMetaValue).orElse("NOT_FOUND"));

                    boolean isActualDraft = isDraftMetaOpt.isPresent() && "1".equals(isDraftMetaOpt.get().getActivityMetaValue()) &&
                                           hasGradedMetaOpt.isPresent() && "0".equals(hasGradedMetaOpt.get().getActivityMetaValue());

                    if (isActualDraft) {
                        // Kiểm tra xem draft đã được nộp chưa (có completed activity mới hơn draft)
                        boolean draftWasSubmitted = false;
                        if (latestCompletedActivityId != null) {
                            // Nếu completed activity có id lớn hơn draft activity id, nghĩa là draft đã được nộp
                            if (latestCompletedActivityId > draftActivity.getId()) {
                                draftWasSubmitted = true;
                                log.info("Draft (ID: {}) was already submitted as completed activity (ID: {}). savedAnswers will be empty.",
                                    draftActivity.getId(), latestCompletedActivityId);
                            }
                        }

                        if (!draftWasSubmitted) {
                            log.info("✅ Confirmed valid draft activity (ID: {}) for user {}, quizId {}", draftActivity.getId(), email, quizId);

                            final Optional<UserActivityMetaEntity> elapsedTimeMetaOpt =
                                    userActivityMetaRepository.findByActivityIdAndActivityMetaKey(draftActivity.getId(), "elapsed_time");
                            if (elapsedTimeMetaOpt.isPresent()) {
                                elapsedTime = Long.parseLong(elapsedTimeMetaOpt.get().getActivityMetaValue());
                                log.info("✅ Found draft elapsed time: {} seconds", elapsedTime);
                            }

                            final Optional<UserActivityMetaEntity> refMetaOpt =
                                    userActivityMetaRepository.findByActivityIdAndActivityMetaKey(draftActivity.getId(), "statistic_ref_id");

                            if (refMetaOpt.isPresent()) {
                                Long statisticRefId = Long.parseLong(refMetaOpt.get().getActivityMetaValue());
                                activityStartTime = draftActivity.getActivityStarted();
                                log.info("Loading draft statistics for statisticRefId: {}", statisticRefId);

                                final List<QuizStatisticEntity> stats =
                                        quizStatisticRepository.findAllById_StatisticRefId(statisticRefId);

                                log.info("Found {} quiz statistics records for draft", stats.size());
                                for (QuizStatisticEntity stat : stats) {
                                    log.info("Draft Statistic - QuestionId: {}, AnswerData: {}",
                                        stat.getId().getQuestionId(), stat.getAnswerData());
                                }

                                answeredDataMap = stats.stream()
                                        .collect(Collectors.toMap(
                                                stat -> stat.getId().getQuestionId(),
                                                stat -> convertAnswerDataToList(stat.getAnswerData())
                                        ));

                                log.info("Final draft answeredDataMap: {}", answeredDataMap);
                                log.info("Successfully loaded {} saved answers from draft activityId {}", answeredDataMap.size(), draftActivity.getId());
                            } else {
                                log.warn("No statistic_ref_id metadata found for draft activityId: {}", draftActivity.getId());
                            }
                        } else {
                            // Draft đã được nộp, savedAnswers = {}
                            answeredDataMap = Collections.emptyMap();
                            activityStartTime = null;
                            elapsedTime = null;
                            log.info("Draft was submitted, savedAnswers set to empty map");
                        }
                    } else {
                        log.info("Found uncompleted activity (ID: {}) but not a valid draft for user {}, quizId {}",
                            draftActivity.getId(), email, quizId);
                    }
                } else {
                    log.info("No uncompleted activity found for user {}, quizId {}", email, quizId);
                }

            return QuizInfoResponse.from(quizDto, questionResponses, answeredDataMap, 
                    activityStartTime, elapsedTime, answeredQuestions, percentage, 
                    answeredCorrects, answeredScore, pass);
        }

        private List<Integer> convertAnswerDataToList(String answerData) {
            if (answerData == null || answerData.isEmpty() || "[]".equals(answerData)) return Collections.emptyList();
            try {
                // Parse answer data string thành list values
                List<Integer> values = Arrays.stream(answerData.replaceAll("[\\[\\]]", "").split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());

                // Trả về indexes của các values = 1 (selected answers)
                List<Integer> selectedIndexes = new ArrayList<>();
                for (int i = 0; i < values.size(); i++) {
                    if (values.get(i) == 1) {
                        selectedIndexes.add(i);
                    }
                }
                return selectedIndexes;
            } catch (Exception e) {
                log.error("Failed to parse answer data string: {}", answerData, e);
                return Collections.emptyList();
            }
        }

        private boolean isPermission(@NotNull final String email,
                        @NotNull final Long userId,
                        @NotNull final QuizDto quizDto) {
                try {
                        if (quizDto.isMiniTest()) {
                                return true;
                        }

                        final PurchasedDto purchasedDto = getPurchasedInfo(email, userId, quizDto);
                        log.info("userId: {}, quiz: {}, purchasedInfo {}", userId, quizDto.getName(),
                                        purchasedDto.toString());
                        if (purchasedDto.isPurchased()
                                        && (Objects.isNull(purchasedDto.getRemainDays())
                                                        || purchasedDto.getRemainDays() > 0)) {
                                return true;
                        }
                } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                }
                return false;
        }

        public CategoryInfo getCategoryInfo(String email, String categoryCode) throws AppException {
                final User user = userRepository
                                .findByEmail(email)
                                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

                // Lấy category entity từ code
                Optional<QuizCategoryEntity> categoryEntityOpt = quizCategoryRepository.findByCode(categoryCode);
                if (categoryEntityOpt.isEmpty()) {
                        // Don't throw exception for unknown category codes (e.g., career-scrum-master)
                        // Return null to indicate no category info available
                        log.warn("Category code not found, returning null: {}", categoryCode);
                        return null;
                }
                QuizCategoryEntity categoryEntity = categoryEntityOpt.get();

                // Đếm số full test và mini test
                final List<QuizDto> quizDtoList = quizMasterRepository
                                .findActiveQuizzes(List.of(PostStatus.PUBLISH, PostStatus.PRIVATE), "sfwd-quiz");
                
                final AtomicInteger fullTestNum = new AtomicInteger(0);
                final AtomicInteger miniTestNum = new AtomicInteger(0);
                
                quizDtoList.forEach(quizDto -> {
                        if (quizDto.isCategory(categoryEntity.getTitle())) {
                                if (quizDto.isMiniTest()) {
                                        miniTestNum.addAndGet(1);
                                } else {
                                        fullTestNum.addAndGet(1);
                                }
                        }
                });

                // Tạo numTest map
                Map<String, Integer> numTest = new HashMap<>();
                numTest.put("full", fullTestNum.get());
                numTest.put("mini", miniTestNum.get());

                return CategoryInfo.builder()
                                .code(categoryEntity.getCode())
                                .title(categoryEntity.getTitle())
                                .header(categoryEntity.getHeader())
                                .numTest(numTest)
                                .build();
        }

        private PurchasedDto getPurchasedInfo(@NotNull final String email,
                        @NotNull final Long userId,
                        @NotNull final QuizDto quizDto) {
                final Optional<CategoryResponse> optionalCategory = quizCategoryService.getCategory(quizDto);
                if (optionalCategory.isEmpty()) {
                        return PurchasedDto.empty();
                }
                final CategoryResponse category = optionalCategory.get();

                final PurchasedDto purchasedInfoFromDatabase = getPurchasedInfoFromDatabase(userId, email,
                                category.getCode());
                final PurchasedDto purchasedInfoFromRevenue = getPurchasedInfoFromRevenue(email, category);

                final boolean isPurchased = purchasedInfoFromDatabase.isPurchased()
                                || purchasedInfoFromRevenue.isPurchased();
                final Long remainDays;
                if (Objects.isNull(purchasedInfoFromDatabase.getRemainDays())) {
                        remainDays = null;
                } else {
                        remainDays = Long.max(purchasedInfoFromDatabase.getRemainDays(),
                                        purchasedInfoFromRevenue.getRemainDays());
                }
                return PurchasedDto.from(isPurchased, remainDays);
        }

        private PurchasedDto getPurchasedInfoFromRevenue(@NotNull final String email,
                        @NotNull final CategoryResponse category) {
                long daysBetweenValue = 0L;
                boolean isPurchased = false;
                try {
                        final String appUserId = DigestUtils.md5Hex(email);
                        final Optional<RevenueCatSubscriberDto> optRevenueCatSubscriber = revenueCatService
                                        .getSubscriber(appUserId);
                        if (optRevenueCatSubscriber.isEmpty()) {
                                return PurchasedDto.empty();
                        }
                        final RevenueCatSubscriberDto revenueCatSubscriber = optRevenueCatSubscriber.get();

                        final String entitlement = StringUtils.isNotBlank(category.getEntitlement())
                                        ? category.getEntitlement()
                                        : ("ez_" + category.getCode().toLowerCase());
                        final RevenueCatSubscriberDto.Entitlement entitlementObj = revenueCatSubscriber.getSubscriber()
                                        .getEntitlements().get(entitlement);

                        if (Objects.nonNull(entitlementObj)) {
                                isPurchased = true;

                                final LocalDateTime expirationDate;
                                if (Objects.nonNull(entitlementObj.getExpiresDate())) {
                                        expirationDate = entitlementObj.getExpiresDate();
                                } else {
                                        final LocalDateTime latestPurchaseDate = entitlementObj.getPurchaseDate();
                                        final String productSku = entitlementObj.getProductIdentifier();
                                        final String subscriptionDaysStr = StringUtils.substringAfterLast(productSku,
                                                        "_");

                                        final int subDays = Objects.nonNull(subscriptionDaysStr)
                                                        ? Integer.parseInt(subscriptionDaysStr)
                                                        : 0;

                                        expirationDate = latestPurchaseDate.plusDays(subDays);
                                }

                                final LocalDateTime now = LocalDateTime.now();
                                daysBetweenValue = now.equals(expirationDate) ? 0
                                                : (DAYS.between(now, expirationDate) + 1);
                                daysBetweenValue = daysBetweenValue <= 0 ? 0 : daysBetweenValue;
                        }
                } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                }
                return PurchasedDto.from(isPurchased, daysBetweenValue);
        }

        private PurchasedDto getPurchasedInfoFromDatabase(@NotNull final Long userId,
                        @NotNull final String userEmail,
                        @NotNull final String categoryCode) {
                final List<UserPurchasedEntity> userPurchasedList = userPurchasedRepository
                                .findAllByUserIdOrUserEmail(userId, userEmail);
                final Map<String, UserPurchasedEntity> categoryToPurchased = userPurchasedList.stream()
                                .collect(toMap(UserPurchasedEntity::getCategoryCode,
                                                Function.identity(), (o1, o2) -> o2));
                UserPurchasedEntity purchasedEntity = categoryToPurchased.get(categoryCode);
                if (purchasedEntity == null) {
                        purchasedEntity = categoryToPurchased.get("all");
                }
                Long daysBetweenValue = 0L;
                boolean isPurchased = false;
                if (purchasedEntity != null
                                && Objects.nonNull(purchasedEntity.getIsPurchased())
                                && purchasedEntity.getIsPurchased() > 0) {
                        final LocalDateTime currTime = LocalDateTime.now();

                        if (Objects.isNull(purchasedEntity.getFromTime())
                                        || currTime.isAfter(purchasedEntity.getFromTime())) {
                                if (Objects.isNull(purchasedEntity.getToTime())
                                                || currTime.isBefore(purchasedEntity.getToTime())) {

                                        isPurchased = true;
                                        if (Objects.isNull(purchasedEntity.getToTime())) {
                                                daysBetweenValue = null;
                                        } else {
                                                daysBetweenValue = DAYS.between(currTime, purchasedEntity.getToTime())
                                                                + 1;
                                                daysBetweenValue = daysBetweenValue <= 0 ? 0 : daysBetweenValue;
                                        }
                                }
                        }
                }

                return PurchasedDto.from(isPurchased, daysBetweenValue);
        }

        private List<Long> getQuestionIds(final Long postId) {
                final PostMeta postMeta = postMetaRepository.findByPostIdAndMetaKey(postId, "ld_quiz_questions");
                return getQuestionIds(postMeta.getMetaValue());
        }

        private static List<Long> getQuestionIds(final String questionIdsValue) {
                final Pattern pattern = Pattern.compile("i:(\\d+);");
                if (questionIdsValue != null) {
                        final List<Long> questionsIds = new ArrayList<>();
                        final Matcher matcher = pattern.matcher(questionIdsValue);
                        int i = 0;
                        while (matcher.find()) {
                                ++i;
                                if (i > 0 && i % 2 == 0) {
                                        questionsIds.add(Long.parseLong(matcher.group(1)));
                                }
                        }
                        return questionsIds;
                }
                return Collections.emptyList();
        }

        @Transactional(rollbackFor = Exception.class)
        public SubmitAnswerResponse submitAnswer(String email,
                        @NotNull Long quizId,
                        @NotNull SubmitAnswerRequest request) throws AppException {
                final boolean isDraft = request.getIsDraft() != null && request.getIsDraft();

                final User user = userRepository
                                .findByEmail(email)
                                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

                final QuizDto quizDto = quizMasterRepository
                                .findActiveQuizById(quizId, List.of(PostStatus.PUBLISH, PostStatus.PRIVATE))
                                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));

                final Long postId = quizDto.getPostId();

                List<QuestionEntity> questionEntities = Collections.emptyList();
                final List<Long> questionsIds = getQuestionIds(quizDto.getPostId());
                if (!CollectionUtils.isEmpty(questionsIds)) {
                        questionEntities = questionRepository.findAllByIdIn(questionsIds);
                }

                final Set<Long> requestQuestionIds = request.getData()
                                .stream()
                                .map(AnsweredData::getQuestionId)
                                .collect(Collectors.toSet());
                final List<QuestionResponse> questionResponses = questionEntities.stream()
                                .filter(x -> requestQuestionIds.contains(x.getId()))
                                .map(QuestionResponse::from)
                                .toList();

                final PostMeta postMeta = postMetaRepository.findByPostIdAndMetaKey(quizDto.getPostId(), "_sfwd-quiz");
                final String quizMeta = postMeta.getMetaValue();
                int passPercentage = 0;
                if (Objects.nonNull(quizMeta)) {
                        final Map<String, Object> quizMetaMap = PostMetaUtil.getPostMetaValues(quizMeta);
                        final Object passingPercentage = quizMetaMap.get("sfwd-quiz_passingpercentage");

                        if (Objects.nonNull(passingPercentage)) {
                                passPercentage = Integer.parseInt(passingPercentage.toString());
                        }
                }

                final QuizStatisticRefEntity quizStatisticRefEntity = new QuizStatisticRefEntity();
                quizStatisticRefEntity.setQuizId(quizId);
                quizStatisticRefEntity.setQuizPostId(quizDto.getPostId());
                quizStatisticRefEntity.setCoursePostId(0L);
                quizStatisticRefEntity.setUserId(user.getId());
                quizStatisticRefEntity.setCreateTime(Instant.now().getEpochSecond());
                quizStatisticRefEntity.setIsOld(0);
                quizStatisticRefEntity.setFormData(null);
                quizStatisticRefRepository.save(quizStatisticRefEntity);

                final Map<Long, AnsweredData> answeredDataMap = request.getData()
                                .stream()
                                .collect(toMap(AnsweredData::getQuestionId,
                                                Function.identity()));
                final Long answeredCount = request.getData().stream()
                                .filter(x -> x.getAnswerData() != null
                                                && x.getAnswerData().contains(Boolean.TRUE))
                                .count();
                final List<QuizStatisticEntity> quizStatisticEntities = questionResponses.stream()
                                .map(x -> createQuizStatistic(x, answeredDataMap.get(x.getId()),
                                                quizStatisticRefEntity.getId()))
                                .toList();
                quizStatisticRepository.saveAll(quizStatisticEntities);

                final UserActivityEntity userActivityEntity = new UserActivityEntity();
                userActivityEntity.setUserId(user.getId());
                userActivityEntity.setActivityStarted(request.getStartTime());
                userActivityEntity.setActivityType("quiz");
                userActivityEntity.setCourseId(0L);
                userActivityEntity.setPostId(postId);

                if (isDraft) {
                    log.info("Creating DRAFT activity for user {}, quizId {}", email, quizId);
                    userActivityEntity.setActivityCompleted(null);
                    userActivityEntity.setActivityUpdated(Instant.now().getEpochSecond());
                    userActivityEntity.setActivityStatus(0);
                } else {
                    log.info("Creating FINAL activity for user {}, quizId {}", email, quizId);
                    userActivityEntity.setActivityCompleted(request.getEndTime());
                    userActivityEntity.setActivityUpdated(request.getEndTime());
                    userActivityEntity.setActivityStatus(1);
                }
                userActivityRepository.save(userActivityEntity);

                final Long activityId = userActivityEntity.getId();
                final List<UserActivityMetaEntity> activityMetaEntities = new ArrayList<>();
                activityMetaEntities.add(createActivityMeta(activityId, "quiz", quizDto.getPostId().toString()));
                activityMetaEntities.add(
                    createActivityMeta(activityId, "statistic_ref_id",
                            String.valueOf(quizStatisticRefEntity.getId())));
                activityMetaEntities.add(createActivityMeta(activityId, "pro_quizid", String.valueOf(quizId)));
                activityMetaEntities.add(createActivityMeta(activityId, "course", "0"));
                activityMetaEntities.add(createActivityMeta(activityId, "lesson", "0"));
                activityMetaEntities.add(createActivityMeta(activityId, "topic", "0"));

                activityMetaEntities.add(createActivityMeta(activityId, "is_draft", isDraft ? "1" : "0"));
                activityMetaEntities.add(createActivityMeta(activityId, "has_graded", isDraft ? "0" : "1"));

                log.info("Creating metadata - activityId: {}, is_draft: {}, has_graded: {}",
                    activityId, isDraft ? "1" : "0", isDraft ? "0" : "1");


            if (isDraft) {

                log.info("Saving DRAFT with is_draft=1 for activityId: {}", activityId);

                long startTime = request.getStartTime();
                long endTime = request.getEndTime();

                long elapsedTime = 0;
                if (endTime > startTime) {
                    elapsedTime = endTime - startTime;
                }

                activityMetaEntities.add(createActivityMeta(activityId, "started", String.valueOf(startTime)));
                activityMetaEntities.add(createActivityMeta(activityId, "answeredCount", String.valueOf(answeredCount)));

                activityMetaEntities.add(createActivityMeta(activityId, "current_time", String.valueOf(endTime)));
                activityMetaEntities.add(createActivityMeta(activityId, "elapsed_time", String.valueOf(elapsedTime)));
                activityMetaEntities.add(createActivityMeta(activityId, "timespent", String.valueOf(elapsedTime)));

                log.info("Draft timing - startTime: {}, endTime: {}, elapsedTime: {} seconds",
                        startTime, endTime, elapsedTime);
            } else {
                log.info("Saving FINAL SUBMISSION with is_draft=0 for activityId: {}", activityId);

                final Long points = quizStatisticEntities.stream()
                                .filter(Objects::nonNull)
                                .mapToLong(QuizStatisticEntity::getPoints)
                                .sum();
                final Long corrects = quizStatisticEntities.stream()
                                .filter(Objects::nonNull)
                                .mapToLong(QuizStatisticEntity::getCorrectCount)
                                .sum();
                final Long inCorrects = quizStatisticEntities.stream()
                                .filter(Objects::nonNull)
                                .mapToLong(QuizStatisticEntity::getIncorrectCount)
                                .sum();
                final Long totalPoints = questionEntities.stream()
                        .filter(Objects::nonNull)
                        .mapToLong(QuestionEntity::getPoints)
                        .sum();

                final BigDecimal percentage = (totalPoints > 0)
                        ? new BigDecimal(points * 100.0 / totalPoints).setScale(2, RoundingMode.HALF_EVEN)
                        : BigDecimal.ZERO;
                final boolean pass = percentage.compareTo(new BigDecimal(passPercentage)) >= 0;

                long totalAnsweredTime = 0;
                if (request.getEndTime() != null && request.getStartTime() != null && request.getEndTime() > request.getStartTime()) {
                    totalAnsweredTime = request.getEndTime() - request.getStartTime();
                }

                log.info("Final submission timing - startTime: {}, endTime: {}, totalTime: {} seconds",
                    request.getStartTime(), request.getEndTime(), totalAnsweredTime);

                activityMetaEntities.add(createActivityMeta(activityId, "score", String.valueOf(points)));
                activityMetaEntities.add(createActivityMeta(activityId, "points", String.valueOf(points)));
                activityMetaEntities.add(createActivityMeta(activityId, "corrects", String.valueOf(corrects)));
                activityMetaEntities.add(createActivityMeta(activityId, "inCorrects", String.valueOf(inCorrects)));
                activityMetaEntities.add(createActivityMeta(activityId, "percentage", percentage.toString()));
                activityMetaEntities.add(createActivityMeta(activityId, "pass", pass ? "1" : "0"));
                activityMetaEntities.add(
                                createActivityMeta(activityId, "answeredCount", String.valueOf(answeredCount)));

                activityMetaEntities.add(createActivityMeta(activityId, "total_points", String.valueOf(totalPoints)));
                activityMetaEntities.add(
                                createActivityMeta(activityId, "count", String.valueOf(questionEntities.size())));
                activityMetaEntities.add(
                                createActivityMeta(activityId, "question_show_count",
                                                String.valueOf(questionEntities.size())));

                activityMetaEntities.add(
                                createActivityMeta(activityId, "time", String.valueOf(request.getEndTime())));
                activityMetaEntities.add(
                                createActivityMeta(activityId, "started", String.valueOf(request.getStartTime())));
                activityMetaEntities.add(
                                createActivityMeta(activityId, "completed", String.valueOf(request.getEndTime())));
                activityMetaEntities.add(createActivityMeta(activityId, "timespent", String.valueOf(totalAnsweredTime)));
                activityMetaEntities.add(createActivityMeta(activityId, "client_end_time", String.valueOf(request.getEndTime())));
            }

                userActivityMetaRepository.saveAll(activityMetaEntities);

            if (isDraft) {
                return SubmitAnswerResponse.builder()
                                .activityId(activityId)
                                .build();
            } else {
                final Long points = quizStatisticEntities.stream().filter(Objects::nonNull).mapToLong(QuizStatisticEntity::getPoints).sum();
                final Long corrects = quizStatisticEntities.stream().filter(Objects::nonNull).mapToLong(QuizStatisticEntity::getCorrectCount).sum();
                final Long inCorrects = quizStatisticEntities.stream().filter(Objects::nonNull).mapToLong(QuizStatisticEntity::getIncorrectCount).sum();
                final Long totalPoints = questionEntities.stream().filter(Objects::nonNull).mapToLong(QuestionEntity::getPoints).sum();

                final BigDecimal percentage = (totalPoints > 0)
                        ? new BigDecimal(points * 100.0 / totalPoints).setScale(2, RoundingMode.HALF_EVEN)
                        : BigDecimal.ZERO;
                final boolean pass = percentage.compareTo(new BigDecimal(passPercentage)) >= 0;
                long answeredTime = 0;
                if (request.getEndTime() > request.getStartTime()) {
                    answeredTime = request.getEndTime() - request.getStartTime();
                }

                return SubmitAnswerResponse.builder()
                                .activityId(activityId)
                                .point(points)
                                .totalPoint(totalPoints)
                                .percentage(percentage.doubleValue())
                                .passPercentage(passPercentage)
                                .pass(pass)
                                .corrects(corrects)
                                .inCorrects(inCorrects)
                                .questions((long) questionEntities.size())
                                .answeredQuestions(answeredCount)
                                .answeredTime(answeredTime)
                                .build();
            }
        }

        private static UserActivityMetaEntity createActivityMeta(final Long activityId,
                        final String key,
                        final String value) {
                final UserActivityMetaEntity entity = new UserActivityMetaEntity();
                entity.setActivityId(activityId);
                entity.setActivityMetaKey(key);
                entity.setActivityMetaValue(value);

                return entity;
        }

        private static QuizStatisticEntity createQuizStatistic(final @NotNull QuestionResponse question,
                        final AnsweredData answeredData,
                        final Long refId) {
                final QuizStatisticId id = new QuizStatisticId();
                id.setQuestionId(question.getId());
                id.setStatisticRefId(refId);

                final List<Integer> selectedList = (Objects.isNull(answeredData)
                                || Objects.isNull(answeredData.getAnswerData())) ? List.of()
                                                : answeredData.getAnswerData()
                                                                .stream()
                                                                .map(QuizMasterService::booleanToInt)
                                                                .toList();
                final List<Integer> correctList = (Objects.isNull(question.getAnswerData())) ? List.of()
                                : question.getAnswerData()
                                                .stream()
                                                .sorted(Comparator.comparing(
                                                                AnswerData::getIndex))
                                                .map(x -> booleanToInt(x.isCorrect()))
                                                .toList();

                final boolean correct = selectedList.toString().equals(correctList.toString());

                final QuizStatisticEntity entity = new QuizStatisticEntity();
                entity.setId(id);
                entity.setPoints(correct ? Objects.requireNonNullElse(question.getPoints(), 0).longValue() : 0L);
                entity.setAnswerData(selectedList.toString());
                entity.setHintCount(0L);
                entity.setCorrectCount(correct ? 1L : 0L);
                entity.setIncorrectCount(!correct ? 1L : 0L);
                entity.setQuestionPostId(0L);
                entity.setQuestionTime(0L);

                return entity;
        }

        private static int booleanToInt(final Boolean value) {
                return Boolean.TRUE.equals(value) ? 1 : 0;
        }

        /**
         * Lấy lịch sử làm bài của user (tất cả quiz hoặc theo quizId cụ thể)
         * Hỗ trợ filter theo category, khoảng thời gian, và sort theo điểm số
         */
        public Page<QuizHistoryResponse> getQuizHistory(String email, Long quizId, String categoryCode,
                                                        Long fromDate, Long toDate, String sortBy, String sortDirection, Pageable pageable)
                        throws AppException {
                final User user = userRepository
                                .findByEmail(email)
                                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

                // Lấy danh sách activities đã hoàn thành (không lấy draft)
                Page<UserActivityEntity> activityPage;
                if (quizId != null) {
                        // Lấy theo quizId cụ thể - cần map sang postId
                        final QuizDto quizDto = quizMasterRepository
                                        .findActiveQuizById(quizId, List.of(PostStatus.PUBLISH, PostStatus.PRIVATE))
                                        .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));
                        
                        if (fromDate != null || toDate != null) {
                                activityPage = userActivityRepository.findCompletedQuizHistoryByPostIdAndDateRange(
                                                user.getId(), quizDto.getPostId(), fromDate, toDate, pageable);
                        } else {
                                activityPage = userActivityRepository.findCompletedQuizHistoryByPostId(
                                                user.getId(), quizDto.getPostId(), pageable);
                        }
                } else {
                        // Lấy tất cả lịch sử
                        if (fromDate != null || toDate != null) {
                                activityPage = userActivityRepository.findCompletedQuizHistoryByDateRange(
                                                user.getId(), fromDate, toDate, pageable);
                        } else {
                                activityPage = userActivityRepository.findCompletedQuizHistory(user.getId(), pageable);
                        }
                }

                final List<Long> activityIds = activityPage.getContent().stream()
                                .map(UserActivityEntity::getId)
                                .collect(Collectors.toList());

                if (activityIds.isEmpty()) {
                        return new PageImpl<>(Collections.emptyList(), pageable, 0);
                }

                // Lấy metadata cho tất cả activities
                final List<UserActivityMetaEntity> activityMetaList = userActivityMetaRepository
                                .findAllByActivityIdIn(activityIds);
                final Map<Long, Map<String, String>> activityMetaMap = activityMetaList
                                .stream()
                                .collect(groupingBy(UserActivityMetaEntity::getActivityId,
                                                toMap(UserActivityMetaEntity::getActivityMetaKey,
                                                                UserActivityMetaEntity::getActivityMetaValue)));

                // Lấy thông tin quiz
                final List<Long> postIds = activityPage.getContent().stream()
                                .map(UserActivityEntity::getPostId)
                                .distinct()
                                .collect(Collectors.toList());

                final List<PostMeta> postMetas = postMetaRepository.findAllByPostIdInAndMetaKey(postIds,
                                "quiz_pro_id");
                final Map<Long, Long> postIdToQuizId = postMetas.stream()
                                .collect(toMap(PostMeta::getPostId,
                                                pm -> Long.parseLong(pm.getMetaValue())));

                final List<Long> quizIds = new ArrayList<>(postIdToQuizId.values());
                final List<QuizMaster> quizMasters = quizMasterRepository.findAllByIdIn(quizIds);
                final Map<Long, QuizMaster> quizMasterMap = quizMasters.stream()
                                .collect(toMap(QuizMaster::getId, Function.identity()));

                // Lấy thông tin quiz từ QuizDto
                final List<QuizDto> quizDtos = quizMasterRepository
                                .findActiveQuizzes(List.of(PostStatus.PUBLISH, PostStatus.PRIVATE), "sfwd-quiz")
                                .stream()
                                .filter(q -> postIds.contains(q.getPostId()))
                                .collect(Collectors.toList());
                final Map<Long, QuizDto> postIdToQuizDto = quizDtos.stream()
                                .collect(toMap(QuizDto::getPostId, Function.identity()));

                // Lấy passingPercentage từ post meta
                final List<PostMeta> quizMetaList = postMetaRepository.findAllByPostIdInAndMetaKey(postIds,
                                "_sfwd-quiz");
                final Map<Long, Integer> postIdToPassingPercentage = new HashMap<>();
                quizMetaList.forEach(pm -> {
                        final Map<String, Object> quizMetaMap = PostMetaUtil.getPostMetaValues(pm.getMetaValue());
                        final Object passingPercentage = quizMetaMap.get("sfwd-quiz_passingpercentage");
                        if (Objects.nonNull(passingPercentage)) {
                                postIdToPassingPercentage.put(pm.getPostId(),
                                                NumberUtils.toInt(passingPercentage.toString().strip(), 0));
                        }
                });

                // Lấy số lượng câu hỏi
                final Map<Long, List<Long>> postIdToQuestionIds = postMetaRepository
                                .findAllByPostIdInAndMetaKey(postIds, "ld_quiz_questions")
                                .stream()
                                .collect(toMap(PostMeta::getPostId,
                                                x -> getQuestionIds(x.getMetaValue())));

                // Lấy statistic để đếm số câu đúng
                final List<Long> statisticRefIds = new ArrayList<>();
                activityMetaMap.values().forEach(metaMap -> {
                        String refId = metaMap.get("statistic_ref_id");
                        if (refId != null) {
                                statisticRefIds.add(Long.parseLong(refId));
                        }
                });
                final List<QuizStatisticEntity> allStatistics = statisticRefIds.isEmpty()
                                ? Collections.emptyList()
                                : quizStatisticRepository.findAllById_StatisticRefIdIn(statisticRefIds);
                final Map<Long, List<QuizStatisticEntity>> refIdToStatistics = allStatistics.stream()
                                .collect(groupingBy(stat -> stat.getId().getStatisticRefId()));

                // Build response
                List<QuizHistoryResponse> responses = activityPage.getContent().stream()
                                .map(activity -> {
                                        final Map<String, String> metaMap = activityMetaMap.get(activity.getId());
                                        final Long postId = activity.getPostId();
                                        final Long qId = postIdToQuizId.get(postId);
                                        final QuizDto quizDto = postIdToQuizDto.get(postId);
                                        final List<Long> questionIds = postIdToQuestionIds.getOrDefault(postId,
                                                        Collections.emptyList());

                                        // Kiểm tra xem có phải draft không
                                        String isDraftValue = metaMap != null ? metaMap.get("is_draft") : null;
                                        String hasGradedValue = metaMap != null ? metaMap.get("has_graded") : null;
                                        boolean isDraft = "1".equals(isDraftValue) && "0".equals(hasGradedValue);

                                        Long correctAnswers = null;
                                        Long incorrectAnswers = null;
                                        if (metaMap != null && metaMap.get("statistic_ref_id") != null) {
                                                Long refId = Long.parseLong(metaMap.get("statistic_ref_id"));
                                                List<QuizStatisticEntity> stats = refIdToStatistics
                                                                .getOrDefault(refId, Collections.emptyList());
                                                correctAnswers = stats.stream()
                                                                .mapToLong(QuizStatisticEntity::getCorrectCount)
                                                                .sum();
                                                incorrectAnswers = stats.stream()
                                                                .mapToLong(QuizStatisticEntity::getIncorrectCount)
                                                                .sum();
                                        }

                                        // Populate category
                                        CategorySimple categorySimple = null;
                                        if (quizDto != null) {
                                                final Optional<CategoryResponse> categoryOpt = quizCategoryService
                                                                .getCategory(quizDto);
                                                if (categoryOpt.isPresent()) {
                                                        final CategoryResponse categoryResponse = categoryOpt.get();
                                                        categorySimple = CategorySimple.builder()
                                                                        .code(categoryResponse.getCode())
                                                                        .title(categoryResponse.getTitle())
                                                                        .build();
                                                }
                                        }

                                        return QuizHistoryResponse.builder()
                                                        .activityId(activity.getId())
                                                        .quizId(qId)
                                                        .postId(postId)
                                                        .quizTitle(quizDto != null ? quizDto.getPostTitle() : null)
                                                        .quizSlug(quizDto != null ? quizDto.getSlug() : null)
                                                        .category(categorySimple)
                                                        .quizType(quizDto != null && quizDto.isMiniTest() ? "mini"
                                                                        : "full")
                                                        .activityStarted(activity.getActivityStarted())
                                                        .activityCompleted(activity.getActivityCompleted())
                                                        .timeSpent(metaMap != null
                                                                        ? getValue(metaMap.get("timespent"), Long.class)
                                                                        : null)
                                                        .score(metaMap != null
                                                                        ? getValue(metaMap.get("score"), Long.class)
                                                                        : null)
                                                        .totalPoints(metaMap != null
                                                                        ? getValue(metaMap.get("total_points"), Long.class)
                                                                        : null)
                                                        .correctAnswers(correctAnswers)
                                                        .incorrectAnswers(incorrectAnswers)
                                                        .answeredQuestions(metaMap != null
                                                                        ? getValue(metaMap.get("answeredCount"), Long.class)
                                                                        : null)
                                                        .totalQuestions((long) questionIds.size())
                                                        .percentage(metaMap != null
                                                                        ? getValue(metaMap.get("percentage"), Double.class)
                                                                        : null)
                                                        .pass(metaMap != null && "1".equals(metaMap.get("pass")))
                                                        .passingPercentage(postIdToPassingPercentage.get(postId))
                                                        .isDraft(isDraft)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                // Filter theo category nếu có
                if (categoryCode != null && !categoryCode.isEmpty()) {
                        responses = responses.stream()
                                        .filter(resp -> resp.getCategory() != null
                                                        && categoryCode.equals(resp.getCategory().getCode()))
                                        .collect(Collectors.toList());
                }

                // Sort theo điểm số hoặc phần trăm nếu cần
                if ("score".equals(sortBy)) {
                        if ("asc".equals(sortDirection)) {
                                responses.sort(Comparator.comparing(QuizHistoryResponse::getScore,
                                                Comparator.nullsLast(Comparator.naturalOrder())));
                        } else {
                                responses.sort(Comparator.comparing(QuizHistoryResponse::getScore,
                                                Comparator.nullsLast(Comparator.reverseOrder())));
                        }
                } else if ("percentage".equals(sortBy)) {
                        if ("asc".equals(sortDirection)) {
                                responses.sort(Comparator.comparing(QuizHistoryResponse::getPercentage,
                                                Comparator.nullsLast(Comparator.naturalOrder())));
                        } else {
                                responses.sort(Comparator.comparing(QuizHistoryResponse::getPercentage,
                                                Comparator.nullsLast(Comparator.reverseOrder())));
                        }
                }
                // "time" is default, already sorted by activityCompleted DESC in query

                return new PageImpl<>(responses, pageable, activityPage.getTotalElements());
        }

        /**
         * Lấy lịch sử làm bài kèm thống kê tổng hợp
         */
        public QuizHistoryWrapperResponse getQuizHistoryWithStats(String email, Long quizId, String categoryCode,
                                                        Long fromDate, Long toDate, String sortBy, String sortDirection,
                                                        Integer page, Integer size) throws AppException {

                // Tạo Pageable cho phân trang
                Pageable pageable = org.springframework.data.domain.PageRequest.of(
                        page != null ? page : 0,
                        size != null ? size : 10
                );

                // Lấy lịch sử với phân trang
                Page<QuizHistoryResponse> historyPage = getQuizHistory(
                        email, quizId, categoryCode, fromDate, toDate, sortBy, sortDirection, pageable);

                // Lấy TẤT CẢ lịch sử (không phân trang) để tính thống kê
                Page<QuizHistoryResponse> allHistoryPage = getQuizHistory(
                        email, quizId, categoryCode, fromDate, toDate, sortBy, sortDirection,
                        org.springframework.data.domain.Pageable.unpaged());

                List<QuizHistoryResponse> allHistory = allHistoryPage.getContent();

                // Tính thống kê sử dụng QuizStatistics
                QuizStatistics stats = calculateQuizStatistics(allHistory);

                // Build response
                return QuizHistoryWrapperResponse.builder()
                        .statistics(stats)
                        .history(PageResponse.of(historyPage))
                        .build();
        }

        /**
         * Tính toán thống kê sử dụng QuizStatistics model
         */
        private QuizStatistics calculateQuizStatistics(List<QuizHistoryResponse> historyList) {
                if (historyList.isEmpty()) {
                        return QuizStatistics.builder()
                                        .totalAttempts(0L)
                                        .passedAttempts(0L)
                                        .passRate(0.0)
                                        .averageScore(0.0)
                                        .averageTimeSpent(0L)
                                        .build();
                }

                final long totalAttempts = historyList.size();
                final long passedAttempts = historyList.stream()
                                .mapToLong(h -> Boolean.TRUE.equals(h.getPass()) ? 1L : 0L)
                                .sum();

                final double passRate = totalAttempts > 0 ? (double) passedAttempts / totalAttempts * 100.0 : 0.0;

                // Tính điểm số trung bình (sử dụng percentage thay vì score để nhất quán)
                final double averageScore = historyList.stream()
                                .filter(h -> h.getPercentage() != null && h.getPercentage() > 0)
                                .mapToDouble(h -> h.getPercentage())
                                .average()
                                .orElse(0.0);

                // Tính thời gian trung bình
                final long averageTimeSpent = Math.round(historyList.stream()
                                .filter(h -> h.getTimeSpent() != null && h.getTimeSpent() > 0)
                                .mapToLong(h -> h.getTimeSpent())
                                .average()
                                .orElse(0.0));

                return QuizStatistics.builder()
                                .totalAttempts(totalAttempts)
                                .passedAttempts(passedAttempts)
                                .passRate(Math.round(passRate * 100.0) / 100.0) // Làm tròn 2 chữ số thập phân
                                .averageScore(Math.round(averageScore * 100.0) / 100.0)
                                .averageTimeSpent(averageTimeSpent)
                                .build();
        }

        /**
         * Lấy chi tiết kết quả làm bài - từng câu hỏi với đáp án và giải thích
         */
        public QuizResultDetailResponse getQuizResultDetail(String email, Long activityId) throws AppException {
                final User user = userRepository
                                .findByEmail(email)
                                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

                // Lấy activity
                final UserActivityEntity activity = userActivityRepository.findById(activityId)
                                .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY, "Activity not found"));

                // Kiểm tra quyền sở hữu
                if (!activity.getUserId().equals(user.getId())) {
                        throw new AppException(ErrorCode.FORBIDDEN, "You don't have permission to view this result");
                }

                // Lấy metadata
                final List<UserActivityMetaEntity> metaList = userActivityMetaRepository
                                .findByActivityId(activityId);
                final Map<String, String> metaMap = metaList.stream()
                                .collect(toMap(UserActivityMetaEntity::getActivityMetaKey,
                                                UserActivityMetaEntity::getActivityMetaValue));

                // Lấy statistic_ref_id
                final String statisticRefIdStr = metaMap.get("statistic_ref_id");
                if (statisticRefIdStr == null) {
                        throw new AppException(ErrorCode.INVALID_KEY, "No statistic data found for this activity");
                }
                final Long statisticRefId = Long.parseLong(statisticRefIdStr);

                // Lấy quiz info
                final Long postId = activity.getPostId();
                final PostMeta quizProIdMeta = postMetaRepository.findByPostIdAndMetaKey(postId, "quiz_pro_id");
                final Long quizId = Long.parseLong(quizProIdMeta.getMetaValue());

                final QuizDto quizDto = quizMasterRepository
                                .findActiveQuizById(quizId, List.of(PostStatus.PUBLISH, PostStatus.PRIVATE))
                                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));

                // Lấy danh sách câu hỏi
                final List<Long> questionIds = getQuestionIds(postId);
                final List<QuestionEntity> questionEntities = questionRepository.findAllByIdIn(questionIds);
                final Map<Long, QuestionEntity> questionMap = questionEntities.stream()
                                .collect(toMap(QuestionEntity::getId, Function.identity()));

                // Lấy statistics (câu trả lời của user)
                final List<QuizStatisticEntity> statistics = quizStatisticRepository
                                .findAllById_StatisticRefId(statisticRefId);
                final Map<Long, QuizStatisticEntity> statisticMap = statistics.stream()
                                .collect(toMap(stat -> stat.getId().getQuestionId(), Function.identity()));

                // Build summary
                final Long totalPoints = questionEntities.stream()
                                .mapToLong(QuestionEntity::getPoints)
                                .sum();
                final Long score = getValue(metaMap.get("score"), Long.class);
                final Long correctAnswers = statistics.stream()
                                .mapToLong(QuizStatisticEntity::getCorrectCount)
                                .sum();
                final Long incorrectAnswers = statistics.stream()
                                .mapToLong(QuizStatisticEntity::getIncorrectCount)
                                .sum();
                final Long answeredCount = getValue(metaMap.get("answeredCount"), Long.class);
                final Integer skippedCount = questionIds.size() - (answeredCount != null ? answeredCount.intValue() : 0);

                final QuizResultDetailResponse.ResultSummary summary = QuizResultDetailResponse.ResultSummary.builder()
                                .totalQuestions(questionIds.size())
                                .answeredQuestions(answeredCount)
                                .correctAnswers(correctAnswers)
                                .incorrectAnswers(incorrectAnswers)
                                .skippedQuestions(skippedCount)
                                .score(score)
                                .totalPoints(totalPoints)
                                .percentage(getValue(metaMap.get("percentage"), Double.class))
                                .pass("1".equals(metaMap.get("pass")))
                                .timeSpent(getValue(metaMap.get("timespent"), Long.class))
                                .build();

                // Build question details
                final AtomicInteger questionNumber = new AtomicInteger(1);
                final List<QuizResultDetailResponse.QuestionDetail> questionDetails = questionIds.stream()
                                .map(qId -> {
                                        final QuestionEntity question = questionMap.get(qId);
                                        if (question == null) {
                                                return null;
                                        }

                                        final QuizStatisticEntity statistic = statisticMap.get(qId);

                                        // Parse answer data
                                        final QuestionResponse questionResponse = QuestionResponse.from(question);
                                        final List<QuestionResponse.AnswerData> answerDataList = questionResponse.getAnswerData();

                                        // FIXED: Sort answerDataList theo index gốc để đảm bảo consistency với submit logic
                                        answerDataList.sort(Comparator.comparing(QuestionResponse.AnswerData::getIndex));

                                        // Get user selected indexes for isSelected flag
                                        final List<Integer> userSelectedIndexes = (statistic != null && statistic.getAnswerData() != null)
                                                ? convertAnswerDataToList(statistic.getAnswerData())
                                                : Collections.emptyList();

                                        // Build answer options with isCorrect and isSelected flags
                                        final List<QuizResultDetailResponse.AnswerOption> answerOptions = new ArrayList<>();
                                        for (int i = 0; i < answerDataList.size(); i++) {
                                                final QuestionResponse.AnswerData answerData = answerDataList.get(i);
                                                answerOptions.add(QuizResultDetailResponse.AnswerOption.builder()
                                                                .index(i)
                                                                .text(answerData.getAnswer())
                                                                .isCorrect(answerData.isCorrect())
                                                                .isSelected(userSelectedIndexes.contains(i))
                                                                .build());
                                        }

                                        // Check if user answer is correct
                                        final boolean isCorrect = statistic != null && statistic.getCorrectCount() > 0;
                                        final Long earnedPoints = statistic != null ? statistic.getPoints() : 0L;

                                        // Generate explanations
                                        final String correctExplanation = generateCorrectExplanation(question, answerDataList);
                                        final String incorrectExplanation = !isCorrect && !userSelectedIndexes.isEmpty()
                                                ? generateIncorrectExplanation(question, userSelectedIndexes, answerDataList)
                                                : null;

                                        return QuizResultDetailResponse.QuestionDetail.builder()
                                                        .questionId(qId)
                                                        .questionNumber(questionNumber.getAndIncrement())
                                                        .questionTitle(question.getTitle())
                                                        .questionText(question.getQuestion())
                                                        .questionPoints(question.getPoints())
                                                        .answerOptions(answerOptions)
                                                        .isCorrect(isCorrect)
                                                        .earnedPoints(earnedPoints)
                                                        .correctExplanation(correctExplanation)
                                                        .incorrectExplanation(incorrectExplanation)
                                                        .build();
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());

                return QuizResultDetailResponse.builder()
                                .activityId(activityId)
                                .quizId(quizId)
                                .quizTitle(quizDto.getPostTitle())
                                .summary(summary)
                                .questions(questionDetails)
                                .build();
        }

        private String generateCorrectExplanation(QuestionEntity question,
                        List<QuestionResponse.AnswerData> answerDataList) {
                // Use question's correct message if available
                if (question.getCorrectMsg() != null && !question.getCorrectMsg().isEmpty()) {
                        return question.getCorrectMsg();
                }

                // Build default explanation with correct answers
                final StringBuilder explanation = new StringBuilder("Đáp án đúng: ");
                boolean first = true;
                for (int i = 0; i < answerDataList.size(); i++) {
                        if (answerDataList.get(i).isCorrect()) {
                                if (!first) {
                                        explanation.append(", ");
                                }
                                explanation.append(answerDataList.get(i).getAnswer());
                                first = false;
                        }
                }

                return first ? "No correct answer available." : explanation.toString();
        }

        private String generateIncorrectExplanation(QuestionEntity question, List<Integer> userSelectedIndexes,
                        List<QuestionResponse.AnswerData> answerDataList) {
                // Use question's incorrect message if available
                if (question.getIncorrectMsg() != null && !question.getIncorrectMsg().isEmpty()) {
                        return question.getIncorrectMsg();
                }

                // Build default explanation showing what user selected vs correct answers
                final StringBuilder explanation = new StringBuilder("Bạn đã chọn: ");

                // Show user's selected answers
                for (int i = 0; i < userSelectedIndexes.size(); i++) {
                        if (i > 0) {
                                explanation.append(", ");
                        }
                        final int index = userSelectedIndexes.get(i);
                        if (index < answerDataList.size()) {
                                explanation.append(answerDataList.get(index).getAnswer());
                        }
                }

                // Show correct answers
                explanation.append(". Đáp án đúng là: ");
                boolean first = true;
                for (int i = 0; i < answerDataList.size(); i++) {
                        if (answerDataList.get(i).isCorrect()) {
                                if (!first) {
                                        explanation.append(", ");
                                }
                                explanation.append(answerDataList.get(i).getAnswer());
                                first = false;
                        }
                }

                return explanation.toString();
        }

}
