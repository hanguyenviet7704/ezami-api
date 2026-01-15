package com.hth.udecareer.eil.service.Impl;

import com.hth.udecareer.eil.entities.EilAiFeedbackEntity;
import com.hth.udecareer.eil.repository.EilAiFeedbackRepository;
import com.hth.udecareer.eil.service.AiFeedbackService;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiFeedbackServiceImpl implements AiFeedbackService {

    private final EilAiFeedbackRepository aiFeedbackRepository;
    private final UserRepository userRepository;

    @Override
    public PageResponse<EilAiFeedbackEntity> getMyFeedback(Principal principal, int page, int size) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size);
        Page<EilAiFeedbackEntity> feedbacks = aiFeedbackRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);

        return PageResponse.of(feedbacks);
    }

    @Override
    public List<EilAiFeedbackEntity> getMyFeedbackByType(Principal principal, String feedbackType) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        return aiFeedbackRepository.findByUserIdAndFeedbackType(user.getId(), feedbackType);
    }

    @Override
    public EilAiFeedbackEntity getLatestByType(Principal principal, String feedbackType) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        return aiFeedbackRepository.findFirstByUserIdAndFeedbackTypeOrderByCreatedAtDesc(user.getId(), feedbackType)
                .orElse(null);
    }

    @Override
    @Transactional
    public EilAiFeedbackEntity rateFeedback(Principal principal, Long feedbackId, Integer rating, String comment, Boolean isHelpful) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        EilAiFeedbackEntity feedback = aiFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (!feedback.getUserId().equals(user.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        if (rating != null) {
            feedback.setUserRating(rating);
        }
        if (comment != null) {
            feedback.setUserComment(comment);
        }
        if (isHelpful != null) {
            feedback.setIsHelpful(isHelpful);
        }

        return aiFeedbackRepository.save(feedback);
    }
}
