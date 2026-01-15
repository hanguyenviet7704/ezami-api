package com.hth.udecareer.eil.service;

import com.hth.udecareer.eil.entities.EilAiFeedbackEntity;
import com.hth.udecareer.model.response.PageResponse;

import java.security.Principal;
import java.util.List;

public interface AiFeedbackService {

    PageResponse<EilAiFeedbackEntity> getMyFeedback(Principal principal, int page, int size);

    List<EilAiFeedbackEntity> getMyFeedbackByType(Principal principal, String feedbackType);

    EilAiFeedbackEntity getLatestByType(Principal principal, String feedbackType);

    EilAiFeedbackEntity rateFeedback(Principal principal, Long feedbackId, Integer rating, String comment, Boolean isHelpful);
}
