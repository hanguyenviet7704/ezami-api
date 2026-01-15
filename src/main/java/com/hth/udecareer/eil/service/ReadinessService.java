package com.hth.udecareer.eil.service;

import com.hth.udecareer.eil.entities.EilReadinessSnapshotEntity;
import com.hth.udecareer.model.response.PageResponse;

import java.security.Principal;

public interface ReadinessService {
    EilReadinessSnapshotEntity getMyLatest(Principal principal, String testType);

    PageResponse<EilReadinessSnapshotEntity> getMyHistory(Principal principal, String testType, int page, int size);

    /**
     * Create a readiness snapshot for a user after completing a diagnostic or practice session.
     *
     * @param userId User ID
     * @param testType Test type (e.g., certification code or "TOEIC")
     * @param questionsAnswered Number of questions answered in this session
     * @param correctCount Number of correct answers in this session
     * @return The created snapshot entity
     */
    EilReadinessSnapshotEntity createSnapshot(Long userId, String testType, int questionsAnswered, int correctCount);
}

