package com.hth.udecareer.eil.service.Impl;

import com.hth.udecareer.eil.entities.EilExplanationEntity;
import com.hth.udecareer.eil.repository.EilExplanationRepository;
import com.hth.udecareer.eil.service.ExplanationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExplanationServiceImpl implements ExplanationService {

    private final EilExplanationRepository explanationRepository;

    @Override
    public Optional<EilExplanationEntity> getById(Long id) {
        return explanationRepository.findById(id);
    }

    @Override
    @Transactional
    public Optional<EilExplanationEntity> getByCacheKey(String cacheKey) {
        Optional<EilExplanationEntity> explanation = explanationRepository.findValidByCacheKey(cacheKey);
        explanation.ifPresent(e -> explanationRepository.incrementHitCount(e.getId()));
        return explanation;
    }

    @Override
    public List<EilExplanationEntity> getByQuestionId(Long questionId) {
        return explanationRepository.findByQuestionId(questionId);
    }

    @Override
    public List<EilExplanationEntity> getByQuestionIdAndLanguage(Long questionId, String language) {
        return explanationRepository.findByQuestionIdAndLanguage(questionId, language);
    }

    @Override
    public List<EilExplanationEntity> getMostAccessed(int limit) {
        List<EilExplanationEntity> all = explanationRepository.findMostAccessed();
        return all.size() > limit ? all.subList(0, limit) : all;
    }
}
