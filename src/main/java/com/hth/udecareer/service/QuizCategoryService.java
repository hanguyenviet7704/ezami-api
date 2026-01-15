package com.hth.udecareer.service;

import java.time.ZoneOffset;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.hth.udecareer.enums.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.hth.udecareer.entities.User;
import com.hth.udecareer.entities.UserPurchasedEntity;
import com.hth.udecareer.enums.PostStatus;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.dto.QuizDto;
import com.hth.udecareer.model.response.CategoryResponse;
import com.hth.udecareer.model.response.CategoryResponse.PurchasedInfo;
import com.hth.udecareer.repository.QuizCategoryRepository;
import com.hth.udecareer.repository.QuizMasterRepository;
import com.hth.udecareer.repository.UserPurchasedRepository;
import com.hth.udecareer.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizCategoryService {
    private final UserRepository userRepository;
    private final QuizMasterRepository quizMasterRepository;
    private final UserPurchasedRepository userPurchasedRepository;
    private final QuizCategoryRepository quizCategoryRepository;

    public List<CategoryResponse> findAll(String email) throws AppException {
        return findAll(email, null);
    }

    public List<CategoryResponse> findAll(String email, String title) throws AppException {
        final User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

        final List<UserPurchasedEntity> userPurchasedList =
                userPurchasedRepository.findAllByUserIdOrUserEmail(user.getId(), user.getEmail());
        final Map<String, UserPurchasedEntity> categoryToPurchased =
                userPurchasedList.stream()
                        .collect(Collectors.toMap(UserPurchasedEntity::getCategoryCode,
                                Function.identity(), (o1, o2) -> o2));

        final List<QuizDto> quizDtoList =
                quizMasterRepository.findActiveQuizzes(List.of(PostStatus.PUBLISH, PostStatus.PRIVATE),
                        "sfwd-quiz");

        final List<CategoryResponse> responseList;
        if (title != null && !title.trim().isEmpty()) {
            responseList = quizCategoryRepository.findAllActiveOrderByOrderWithTitleFilter(title.trim())
                    .stream()
                    .map(CategoryResponse::from)
                    .collect(Collectors.toList());
        } else {
            responseList = quizCategoryRepository.findAllActiveOrderByOrder()
                    .stream()
                    .map(CategoryResponse::from)
                    .collect(Collectors.toList());
        }

        responseList.forEach(cat -> {
            final AtomicInteger fullTestNum = new AtomicInteger(0);
            final AtomicInteger miniTestNum = new AtomicInteger(0);

            quizDtoList.forEach(quizDto -> {
                if (quizDto.isCategory(cat.getTitle())) {
                    if (quizDto.isMiniTest()) {
                        miniTestNum.addAndGet(1);
                    } else {
                        fullTestNum.addAndGet(1);
                    }
                }
            });

            cat.setNumFullTest(fullTestNum.get());
            cat.setNumMiniTest(miniTestNum.get());

            // set purchase info
            UserPurchasedEntity purchasedEntity = categoryToPurchased.get(cat.getCode());
            if (purchasedEntity == null) {
                purchasedEntity = categoryToPurchased.get("all");
            }
            if (purchasedEntity != null) {
                final PurchasedInfo purchasedInfo = PurchasedInfo.builder().build();
                purchasedInfo.setIsPurchased(
                        purchasedEntity.getIsPurchased() != null && purchasedEntity.getIsPurchased() > 0);
                purchasedInfo.setFromTime(purchasedEntity.getFromTime() == null ? null
                        :
                        purchasedEntity.getFromTime().toInstant(ZoneOffset.UTC).toEpochMilli());
                purchasedInfo.setToTime(purchasedEntity.getToTime() == null ? null
                        :
                        purchasedEntity.getToTime().toInstant(ZoneOffset.UTC).toEpochMilli());

                cat.setPurchasedInfo(purchasedInfo);
            } else {
                cat.setPurchasedInfo(PurchasedInfo.builder().isPurchased(false).build());
            }
        });

        return responseList;
    }


    public List<CategoryResponse> findAllPublic() {
        return findAllPublic(null);
    }

    public List<CategoryResponse> findAllPublic(String title) {
        final List<QuizDto> quizDtoList =
                quizMasterRepository.findActiveQuizzes(List.of(PostStatus.PUBLISH, PostStatus.PRIVATE),
                        "sfwd-quiz");

        final List<CategoryResponse> responseList;
        if (title != null && !title.trim().isEmpty()) {
            responseList = quizCategoryRepository.findAllActiveOrderByOrderWithTitleFilter(title.trim())
                    .stream()
                    .map(CategoryResponse::from)
                    .collect(Collectors.toList());
        } else {
            responseList = quizCategoryRepository.findAllActiveOrderByOrder()
                    .stream()
                    .map(CategoryResponse::from)
                    .collect(Collectors.toList());
        }

        responseList.forEach(cat -> {
            final AtomicInteger fullTestNum = new AtomicInteger(0);
            final AtomicInteger miniTestNum = new AtomicInteger(0);

            quizDtoList.forEach(quizDto -> {
                if (quizDto.isCategory(cat.getTitle())) {
                    if (quizDto.isMiniTest()) {
                        miniTestNum.addAndGet(1);
                    } else {
                        fullTestNum.addAndGet(1);
                    }
                }
            });

            cat.setNumFullTest(fullTestNum.get());
            cat.setNumMiniTest(miniTestNum.get());

            cat.setPurchasedInfo(null);
        });

        return responseList;
    }

    public Page<CategoryResponse> findAllPublicPaged(Pageable pageable) {
        return findAllPublicPaged(null, pageable);
    }

    public Page<CategoryResponse> findAllPublicPaged(String title, Pageable pageable) {
        final List<QuizDto> quizDtoList =
                quizMasterRepository.findActiveQuizzes(List.of(PostStatus.PUBLISH, PostStatus.PRIVATE),
                        "sfwd-quiz");

        Page<CategoryResponse> responsePage;
        if (title != null && !title.trim().isEmpty()) {
            responsePage = quizCategoryRepository.findAllActiveOrderByOrderWithTitleFilter(title.trim(), pageable)
                    .map(CategoryResponse::from);
        } else {
            responsePage = quizCategoryRepository.findAllActiveOrderByOrder(pageable)
                    .map(CategoryResponse::from);
        }

        responsePage.forEach(cat -> {
            final AtomicInteger fullTestNum = new AtomicInteger(0);
            final AtomicInteger miniTestNum = new AtomicInteger(0);

            quizDtoList.forEach(quizDto -> {
                if (quizDto.isCategory(cat.getTitle())) {
                    if (quizDto.isMiniTest()) {
                        miniTestNum.addAndGet(1);
                    } else {
                        fullTestNum.addAndGet(1);
                    }
                }
            });

            cat.setNumFullTest(fullTestNum.get());
            cat.setNumMiniTest(miniTestNum.get());

            cat.setPurchasedInfo(null);
        });

        return responsePage;
    }

    public Page<CategoryResponse> findAllPaged(String email, Pageable pageable) throws AppException {
        return findAllPaged(email, null, pageable);
    }

    public Page<CategoryResponse> findAllPaged(String email, String title, Pageable pageable) throws AppException {
        final User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

        final List<UserPurchasedEntity> userPurchasedList =
                userPurchasedRepository.findAllByUserIdOrUserEmail(user.getId(), user.getEmail());
        final Map<String, UserPurchasedEntity> categoryToPurchased =
                userPurchasedList.stream()
                        .collect(Collectors.toMap(UserPurchasedEntity::getCategoryCode,
                                Function.identity(), (o1, o2) -> o2));

        final List<QuizDto> quizDtoList =
                quizMasterRepository.findActiveQuizzes(List.of(PostStatus.PUBLISH, PostStatus.PRIVATE),
                        "sfwd-quiz");

        Page<CategoryResponse> responsePage;
        if (title != null && !title.trim().isEmpty()) {
            responsePage = quizCategoryRepository.findAllActiveOrderByOrderWithTitleFilter(title.trim(), pageable)
                    .map(CategoryResponse::from);
        } else {
            responsePage = quizCategoryRepository.findAllActiveOrderByOrder(pageable)
                    .map(CategoryResponse::from);
        }

        responsePage.forEach(cat -> {
            final AtomicInteger fullTestNum = new AtomicInteger(0);
            final AtomicInteger miniTestNum = new AtomicInteger(0);

            quizDtoList.forEach(quizDto -> {
                if (quizDto.isCategory(cat.getTitle())) {
                    if (quizDto.isMiniTest()) {
                        miniTestNum.addAndGet(1);
                    } else {
                        fullTestNum.addAndGet(1);
                    }
                }
            });

            cat.setNumFullTest(fullTestNum.get());
            cat.setNumMiniTest(miniTestNum.get());

            // set purchase info
            UserPurchasedEntity purchasedEntity = categoryToPurchased.get(cat.getCode());
            if (purchasedEntity == null) {
                purchasedEntity = categoryToPurchased.get("all");
            }
            if (purchasedEntity != null) {
                final PurchasedInfo purchasedInfo = PurchasedInfo.builder().build();
                purchasedInfo.setIsPurchased(
                        purchasedEntity.getIsPurchased() != null && purchasedEntity.getIsPurchased() > 0);
                purchasedInfo.setFromTime(purchasedEntity.getFromTime() == null ? null
                        :
                        purchasedEntity.getFromTime().toInstant(ZoneOffset.UTC).toEpochMilli());
                purchasedInfo.setToTime(purchasedEntity.getToTime() == null ? null
                        :
                        purchasedEntity.getToTime().toInstant(ZoneOffset.UTC).toEpochMilli());

                cat.setPurchasedInfo(purchasedInfo);
            } else {
                cat.setPurchasedInfo(PurchasedInfo.builder().isPurchased(false).build());
            }
        });

        return responsePage;
    }

    public Optional<CategoryResponse> getCategory(@NotNull QuizDto quizDto) {
        final List<CategoryResponse> categoryList = quizCategoryRepository.findAllActiveOrderByOrder()
                .stream()
                .map(CategoryResponse::from)
                .toList();

        return categoryList.stream().filter(cat -> quizDto.isCategory(cat.getTitle())).findFirst();
    }
}
