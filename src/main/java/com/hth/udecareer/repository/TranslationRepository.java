package com.hth.udecareer.repository;

import com.hth.udecareer.entities.TranslationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TranslationRepository extends JpaRepository<TranslationEntity, Long> {

    /**
     * Find a specific translation
     */
    Optional<TranslationEntity> findByEntityTypeAndEntityIdAndFieldNameAndLanguage(
            String entityType, Long entityId, String fieldName, String language);

    /**
     * Find all translations for an entity
     */
    List<TranslationEntity> findByEntityTypeAndEntityId(String entityType, Long entityId);

    /**
     * Find all translations for an entity in a specific language
     */
    List<TranslationEntity> findByEntityTypeAndEntityIdAndLanguage(
            String entityType, Long entityId, String language);

    /**
     * Find all translations for a specific field across all entities of a type
     */
    List<TranslationEntity> findByEntityTypeAndFieldNameAndLanguage(
            String entityType, String fieldName, String language);

    /**
     * Check if a translation exists
     */
    boolean existsByEntityTypeAndEntityIdAndFieldNameAndLanguage(
            String entityType, Long entityId, String fieldName, String language);

    /**
     * Delete all translations for an entity
     */
    @Modifying
    void deleteByEntityTypeAndEntityId(String entityType, Long entityId);

    /**
     * Delete a specific translation
     */
    @Modifying
    void deleteByEntityTypeAndEntityIdAndFieldNameAndLanguage(
            String entityType, Long entityId, String fieldName, String language);

    /**
     * Get translated value with fallback to default language
     * Returns the translation for the requested language, or Vietnamese (vi) if not found
     */
    @Query(value = """
            SELECT t.translated_value FROM wp_fcom_translations t
            WHERE t.entity_type = :entityType
            AND t.entity_id = :entityId
            AND t.field_name = :fieldName
            AND t.language = :language
            UNION ALL
            SELECT t2.translated_value FROM wp_fcom_translations t2
            WHERE t2.entity_type = :entityType
            AND t2.entity_id = :entityId
            AND t2.field_name = :fieldName
            AND t2.language = 'vi'
            AND NOT EXISTS (
                SELECT 1 FROM wp_fcom_translations t3
                WHERE t3.entity_type = :entityType
                AND t3.entity_id = :entityId
                AND t3.field_name = :fieldName
                AND t3.language = :language
            )
            LIMIT 1
            """, nativeQuery = true)
    Optional<String> findTranslatedValueWithFallback(
            @Param("entityType") String entityType,
            @Param("entityId") Long entityId,
            @Param("fieldName") String fieldName,
            @Param("language") String language);

    /**
     * Batch find translations for multiple entities
     */
    @Query("SELECT t FROM TranslationEntity t WHERE t.entityType = :entityType " +
            "AND t.entityId IN :entityIds AND t.language = :language")
    List<TranslationEntity> findByEntityTypeAndEntityIdInAndLanguage(
            @Param("entityType") String entityType,
            @Param("entityIds") List<Long> entityIds,
            @Param("language") String language);

    /**
     * Find all available languages for an entity
     */
    @Query("SELECT DISTINCT t.language FROM TranslationEntity t " +
            "WHERE t.entityType = :entityType AND t.entityId = :entityId")
    List<String> findAvailableLanguages(
            @Param("entityType") String entityType,
            @Param("entityId") Long entityId);

    /**
     * Count translations by language
     */
    @Query("SELECT t.language, COUNT(t) FROM TranslationEntity t " +
            "WHERE t.entityType = :entityType GROUP BY t.language")
    List<Object[]> countByEntityTypeGroupByLanguage(@Param("entityType") String entityType);
}
