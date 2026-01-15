package com.hth.udecareer.eil.service;

import com.hth.udecareer.eil.entities.EilExplanationEntity;

import java.util.List;
import java.util.Optional;

public interface ExplanationService {

    Optional<EilExplanationEntity> getById(Long id);

    Optional<EilExplanationEntity> getByCacheKey(String cacheKey);

    List<EilExplanationEntity> getByQuestionId(Long questionId);

    List<EilExplanationEntity> getByQuestionIdAndLanguage(Long questionId, String language);

    List<EilExplanationEntity> getMostAccessed(int limit);
}
