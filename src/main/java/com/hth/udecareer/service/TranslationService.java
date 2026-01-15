package com.hth.udecareer.service;

import com.hth.udecareer.config.LocaleConfig;
import com.hth.udecareer.entities.TranslationEntity;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.request.TranslationRequest;
import com.hth.udecareer.model.response.TranslationResponse;
import com.hth.udecareer.repository.TranslationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {

    private final TranslationRepository translationRepository;

    /**
     * Get current language from request context
     */
    public String getCurrentLanguage() {
        return LocaleContextHolder.getLocale().getLanguage();
    }

    /**
     * Get translated value for a specific field
     * Falls back to Vietnamese if translation not found
     */
    @Transactional(readOnly = true)
    public String getTranslatedValue(String entityType, Long entityId, String fieldName, String defaultValue) {
        String language = getCurrentLanguage();
        return getTranslatedValue(entityType, entityId, fieldName, language, defaultValue);
    }

    /**
     * Get translated value for a specific field with explicit language
     */
    @Transactional(readOnly = true)
    public String getTranslatedValue(String entityType, Long entityId, String fieldName,
                                     String language, String defaultValue) {
        return translationRepository.findTranslatedValueWithFallback(entityType, entityId, fieldName, language)
                .orElse(defaultValue);
    }

    /**
     * Get all translations for an entity in current language
     */
    @Transactional(readOnly = true)
    public Map<String, String> getTranslations(String entityType, Long entityId) {
        String language = getCurrentLanguage();
        return getTranslations(entityType, entityId, language);
    }

    /**
     * Get all translations for an entity in a specific language
     */
    @Transactional(readOnly = true)
    public Map<String, String> getTranslations(String entityType, Long entityId, String language) {
        List<TranslationEntity> translations = translationRepository
                .findByEntityTypeAndEntityIdAndLanguage(entityType, entityId, language);

        return translations.stream()
                .collect(Collectors.toMap(
                        TranslationEntity::getFieldName,
                        TranslationEntity::getTranslatedValue,
                        (v1, v2) -> v1  // Keep first if duplicate
                ));
    }

    /**
     * Get all translations for an entity (all languages)
     */
    @Transactional(readOnly = true)
    public Map<String, Map<String, String>> getAllTranslations(String entityType, Long entityId) {
        List<TranslationEntity> translations = translationRepository
                .findByEntityTypeAndEntityId(entityType, entityId);

        Map<String, Map<String, String>> result = new HashMap<>();
        for (TranslationEntity t : translations) {
            result.computeIfAbsent(t.getLanguage(), k -> new HashMap<>())
                    .put(t.getFieldName(), t.getTranslatedValue());
        }
        return result;
    }

    /**
     * Batch get translations for multiple entities
     * Returns Map<entityId, Map<fieldName, translatedValue>>
     */
    @Transactional(readOnly = true)
    public Map<Long, Map<String, String>> getBatchTranslations(String entityType, List<Long> entityIds) {
        String language = getCurrentLanguage();
        return getBatchTranslations(entityType, entityIds, language);
    }

    /**
     * Batch get translations for multiple entities with explicit language
     */
    @Transactional(readOnly = true)
    public Map<Long, Map<String, String>> getBatchTranslations(String entityType, List<Long> entityIds, String language) {
        if (entityIds == null || entityIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<TranslationEntity> translations = translationRepository
                .findByEntityTypeAndEntityIdInAndLanguage(entityType, entityIds, language);

        Map<Long, Map<String, String>> result = new HashMap<>();
        for (TranslationEntity t : translations) {
            result.computeIfAbsent(t.getEntityId(), k -> new HashMap<>())
                    .put(t.getFieldName(), t.getTranslatedValue());
        }
        return result;
    }

    /**
     * Get available languages for an entity
     */
    @Transactional(readOnly = true)
    public List<String> getAvailableLanguages(String entityType, Long entityId) {
        return translationRepository.findAvailableLanguages(entityType, entityId);
    }

    // ============= ADMIN METHODS =============

    /**
     * Save or update a translation
     */
    @Transactional
    public TranslationResponse saveTranslation(TranslationRequest request) {
        validateLanguage(request.getLanguage());

        TranslationEntity translation = translationRepository
                .findByEntityTypeAndEntityIdAndFieldNameAndLanguage(
                        request.getEntityType(),
                        request.getEntityId(),
                        request.getFieldName(),
                        request.getLanguage())
                .orElse(new TranslationEntity());

        translation.setEntityType(request.getEntityType());
        translation.setEntityId(request.getEntityId());
        translation.setFieldName(request.getFieldName());
        translation.setLanguage(request.getLanguage());
        translation.setTranslatedValue(request.getTranslatedValue());

        translationRepository.save(translation);
        log.info("Translation saved: {} {} {} {}",
                request.getEntityType(), request.getEntityId(),
                request.getFieldName(), request.getLanguage());

        return mapToResponse(translation);
    }

    /**
     * Batch save translations for an entity
     */
    @Transactional
    public List<TranslationResponse> saveTranslations(String entityType, Long entityId,
                                                      Map<String, Map<String, String>> translations) {
        // translations: Map<language, Map<fieldName, value>>
        List<TranslationResponse> responses = new ArrayList<>();

        for (Map.Entry<String, Map<String, String>> langEntry : translations.entrySet()) {
            String language = langEntry.getKey();
            validateLanguage(language);

            for (Map.Entry<String, String> fieldEntry : langEntry.getValue().entrySet()) {
                TranslationRequest request = TranslationRequest.builder()
                        .entityType(entityType)
                        .entityId(entityId)
                        .fieldName(fieldEntry.getKey())
                        .language(language)
                        .translatedValue(fieldEntry.getValue())
                        .build();

                responses.add(saveTranslation(request));
            }
        }

        return responses;
    }

    /**
     * Delete a specific translation
     */
    @Transactional
    public void deleteTranslation(String entityType, Long entityId, String fieldName, String language) {
        translationRepository.deleteByEntityTypeAndEntityIdAndFieldNameAndLanguage(
                entityType, entityId, fieldName, language);
        log.info("Translation deleted: {} {} {} {}", entityType, entityId, fieldName, language);
    }

    /**
     * Delete all translations for an entity
     */
    @Transactional
    public void deleteAllTranslations(String entityType, Long entityId) {
        translationRepository.deleteByEntityTypeAndEntityId(entityType, entityId);
        log.info("All translations deleted for: {} {}", entityType, entityId);
    }

    /**
     * Get translation by ID
     */
    @Transactional(readOnly = true)
    public TranslationResponse getTranslationById(Long id) {
        TranslationEntity translation = translationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Translation not found"));
        return mapToResponse(translation);
    }

    /**
     * Get all translations for admin view
     */
    @Transactional(readOnly = true)
    public List<TranslationResponse> getTranslationsForAdmin(String entityType, Long entityId) {
        List<TranslationEntity> translations = translationRepository
                .findByEntityTypeAndEntityId(entityType, entityId);

        return translations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get translation statistics by language
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getTranslationStats(String entityType) {
        List<Object[]> stats = translationRepository.countByEntityTypeGroupByLanguage(entityType);
        Map<String, Long> result = new HashMap<>();
        for (Object[] row : stats) {
            result.put((String) row[0], (Long) row[1]);
        }
        return result;
    }

    // ============= HELPER METHODS =============

    private void validateLanguage(String language) {
        if (!LocaleConfig.isSupported(LocaleConfig.getSupportedLocale(language))) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Unsupported language: " + language + ". Supported: vi, en");
        }
    }

    private TranslationResponse mapToResponse(TranslationEntity entity) {
        return TranslationResponse.builder()
                .id(entity.getId())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .fieldName(entity.getFieldName())
                .language(entity.getLanguage())
                .translatedValue(entity.getTranslatedValue())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
