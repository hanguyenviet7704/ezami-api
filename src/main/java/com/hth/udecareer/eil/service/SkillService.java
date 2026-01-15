package com.hth.udecareer.eil.service;

import com.hth.udecareer.eil.entities.EilQuestionSkillEntity;
import com.hth.udecareer.eil.entities.EilSkillEntity;
import com.hth.udecareer.eil.entities.WpEzQuestionSkillEntity;
import com.hth.udecareer.eil.entities.WpEzSkillEntity;
import com.hth.udecareer.eil.model.dto.SkillDto;
import com.hth.udecareer.eil.repository.EilQuestionSkillRepository;
import com.hth.udecareer.eil.repository.EilSkillRepository;
import com.hth.udecareer.eil.repository.WpEzQuestionSkillRepository;
import com.hth.udecareer.eil.repository.WpEzSkillRepository;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing EIL skills and skill mappings.
 * Now uses wp_ez_skills (WordPress) as primary source for skills and question mappings.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkillService {

    // Legacy repositories (for backward compatibility)
    private final EilSkillRepository skillRepository;
    private final EilQuestionSkillRepository questionSkillRepository;

    // New WordPress-based repositories (primary)
    private final WpEzSkillRepository wpSkillRepository;
    private final WpEzQuestionSkillRepository wpQuestionSkillRepository;

    // I18n support
    private final MessageService messageService;

    private static final String STATUS_ACTIVE = "active";

    /**
     * Get all active skills.
     */
    @Cacheable(value = "eil:skills", key = "'all'")
    public List<EilSkillEntity> getAllActiveSkills() {
        return skillRepository.findByIsActiveTrueOrderByPriorityAsc();
    }

    /**
     * Get skills by category.
     */
    @Cacheable(value = "eil:skills", key = "#category")
    public List<EilSkillEntity> getSkillsByCategory(String category) {
        return skillRepository.findByCategoryAndIsActiveTrue(category);
    }

    /**
     * Get skill by ID.
     * Tries wp_ez_skills first (new system), fallback to eil_skills (legacy).
     */
    public EilSkillEntity getSkillById(Long skillId) throws AppException {
        // Try wp_ez_skills first (primary source with 4,650 skills)
        Optional<WpEzSkillEntity> wpSkill = wpSkillRepository.findById(skillId);
        if (wpSkill.isPresent()) {
            return convertWpEzToEilSkill(wpSkill.get());
        }

        // Fallback to legacy eil_skills (175 skills)
        return skillRepository.findById(skillId)
                .orElseThrow(() -> new AppException(ErrorCode.EIL_SKILL_NOT_FOUND));
    }

    /**
     * Get all skill IDs for a specific certification.
     * Used to filter user skill mastery by certification to prevent cross-contamination.
     *
     * CRITICAL FIX: This method prevents ISTQB diagnostic from showing TOEIC skills, etc.
     *
     * @param certificationId Certification code (e.g., "ISTQB_CTFL", "PSM_I")
     * @return List of skill IDs belonging to this certification
     */
    @Cacheable(value = "eil:skills", key = "'cert:' + #certificationId + ':ids'")
    public List<Long> getSkillIdsByCertification(String certificationId) {
        log.debug("Getting skill IDs for certification: {}", certificationId);

        List<WpEzSkillEntity> skills = wpSkillRepository
                .findByCertificationIdAndStatusOrderBySortOrderAsc(certificationId, STATUS_ACTIVE);

        List<Long> skillIds = skills.stream()
                .map(WpEzSkillEntity::getId)
                .collect(Collectors.toList());

        log.debug("Found {} skills for certification {}", skillIds.size(), certificationId);

        return skillIds;
    }

    /**
     * Convert WpEzSkillEntity to EilSkillEntity for compatibility.
     */
    private EilSkillEntity convertWpEzToEilSkill(WpEzSkillEntity wpSkill) {
        EilSkillEntity eilSkill = new EilSkillEntity();
        eilSkill.setId(wpSkill.getId());
        eilSkill.setCode(wpSkill.getCode());
        eilSkill.setName(wpSkill.getName());
        eilSkill.setNameVi(wpSkill.getName()); // wp_ez doesn't have name_vi yet
        eilSkill.setCategory(wpSkill.getCertificationId());
        eilSkill.setSubcategory(null);
        eilSkill.setLevel(wpSkill.getLevel());
        eilSkill.setParentId(wpSkill.getParentId());
        eilSkill.setIsActive(wpSkill.isActive());
        return eilSkill;
    }

    /**
     * Get skill by code.
     */
    @Cacheable(value = "eil:skills", key = "'code:' + #skillCode")
    public Optional<EilSkillEntity> getSkillByCode(String skillCode) {
        return skillRepository.findByCode(skillCode);
    }

    /**
     * Get child skills of a parent skill.
     */
    public List<EilSkillEntity> getChildSkills(Long parentId) {
        return skillRepository.findByParentIdAndIsActiveTrue(parentId);
    }

    /**
     * Get leaf skills (level 3 skills) for actual question mapping.
     */
    @Cacheable(value = "eil:skills", key = "'leaf'")
    public List<EilSkillEntity> getLeafSkills() {
        return skillRepository.findAllLeafSkills();
    }

    /**
     * Get leaf skills by category.
     */
    public List<EilSkillEntity> getLeafSkillsByCategory(String category) {
        return skillRepository.findLeafSkillsByCategory(category);
    }

    /**
     * Get primary skill ID for a question.
     * CRITICAL FIX: Use eil_question_skills FIRST because diagnostic_answers has FK to eil_skills.
     * wp_ez_question_skills points to wp_ez_skills (different table) which causes FK violations.
     */
    public Long getPrimarySkillIdForQuestion(Long questionId) {
        // PRIORITY 1: Use old eil_question_skills table (FK compatible with eil_diagnostic_answers)
        Long eilSkillId = questionSkillRepository.findFirstByQuestionIdAndIsPrimaryTrue(questionId)
                .map(EilQuestionSkillEntity::getSkillId)
                .orElse(null);

        if (eilSkillId != null) {
            return eilSkillId;  // Found in eil_question_skills → compatible with FK
        }

        // PRIORITY 2: Fallback to wp_ez_question_skills (may cause FK issues in diagnostic)
        List<WpEzQuestionSkillEntity> mappings = wpQuestionSkillRepository
                .findByQuestionIdOrderByWeightDesc(questionId);

        if (!mappings.isEmpty()) {
            Long wpSkillId = mappings.get(0).getSkillId();  // Highest weight
            log.warn("Question {} only has wp_ez_skills mapping (skill_id={}), may cause FK violations in diagnostic",
                    questionId, wpSkillId);
            return wpSkillId;  // Return anyway - let FK constraint fail with clear error
        }

        return null;  // No mapping found
    }

    /**
     * Get primary skill IDs for multiple questions (bulk - avoids N+1).
     */
    public Map<Long, Long> getPrimarySkillMappingsForQuestions(List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return questionSkillRepository.findAll().stream()
                .filter(qs -> questionIds.contains(qs.getQuestionId()) && Boolean.TRUE.equals(qs.getIsPrimary()))
                .collect(Collectors.toMap(
                        EilQuestionSkillEntity::getQuestionId,
                        EilQuestionSkillEntity::getSkillId,
                        (first, second) -> first
                ));
    }

    /**
     * Get all skill IDs for a question.
     * Uses wp_ez_question_skills (new system).
     */
    public List<Long> getSkillIdsForQuestion(Long questionId) {
        // Use new wp_ez_question_skills table first
        List<Long> skillIds = wpQuestionSkillRepository.findSkillIdsByQuestionId(questionId);

        // Fallback to old table if empty
        if (skillIds.isEmpty()) {
            skillIds = questionSkillRepository.findSkillIdsByQuestionId(questionId);
        }

        return skillIds;
    }

    /**
     * Get primary skill entity for a question.
     */
    public Optional<EilSkillEntity> getPrimarySkillForQuestion(Long questionId) {
        return questionSkillRepository.findFirstByQuestionIdAndIsPrimaryTrue(questionId)
                .flatMap(qs -> skillRepository.findById(qs.getSkillId()));
    }

    /**
     * Get question IDs for a skill.
     * Uses wp_ez_question_skills (primary table with actual data).
     */
    public List<Long> getQuestionIdsForSkill(Long skillId) {
        return wpQuestionSkillRepository.findBySkillId(skillId).stream()
                .map(WpEzQuestionSkillEntity::getQuestionId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Get question IDs for a skill with specific difficulty.
     * Uses wp_ez_question_skills with confidence mapping.
     * Difficulty 1-2 → high/medium confidence
     * Difficulty 3 → medium confidence
     * Difficulty 4-5 → medium/low confidence
     */
    public List<Long> getQuestionIdsForSkillAndDifficulty(Long skillId, Integer difficulty) {
        // Map difficulty to confidence levels (Java-based - no JPQL CASE needed)
        final String CONF_HIGH = "high";
        final String CONF_MEDIUM = "medium";
        final String CONF_LOW = "low";

        List<String> confidenceLevels;
        if (difficulty <= 2) {
            confidenceLevels = List.of(CONF_HIGH, CONF_MEDIUM);
        } else if (difficulty == 3) {
            confidenceLevels = List.of(CONF_MEDIUM);
        } else {
            confidenceLevels = List.of(CONF_MEDIUM, CONF_LOW);
        }

        // Get questions for each confidence level and combine
        return confidenceLevels.stream()
                .flatMap(confidence -> wpQuestionSkillRepository.findBySkillIdAndConfidence(skillId, confidence).stream())
                .map(WpEzQuestionSkillEntity::getQuestionId)
                .distinct()
                .toList();
    }

    /**
     * Get question IDs grouped by skill.
     * Uses wp_ez_question_skills as primary source.
     */
    public Map<Long, List<Long>> getQuestionsGroupedBySkill() {
        return wpQuestionSkillRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        WpEzQuestionSkillEntity::getSkillId,
                        Collectors.mapping(WpEzQuestionSkillEntity::getQuestionId, Collectors.toList())
                ));
    }

    /**
     * Get question IDs grouped by skill, filtered by categories (certification IDs).
     * Uses wp_ez_skills as primary source.
     *
     * @param categories List of certification IDs to include (e.g., "PSM_I", "CBAP")
     * @return Map of skillId to list of questionIds
     */
    public Map<Long, List<Long>> getQuestionsGroupedBySkillForCategories(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return getQuestionsGroupedBySkill();
        }

        // Get skill IDs for the specified certifications from wp_ez_skills
        List<Long> skillIdsInCategories = categories.stream()
                .flatMap(certId -> wpSkillRepository.findByCertificationIdAndStatusOrderBySortOrderAsc(certId, STATUS_ACTIVE).stream())
                .map(WpEzSkillEntity::getId)
                .collect(Collectors.toList());

        if (skillIdsInCategories.isEmpty()) {
            log.warn("No skills found for categories/certifications: {}", categories);
            return Map.of();
        }

        // Filter question-skill mappings to only include skills in categories
        return wpQuestionSkillRepository.findBySkillIdIn(skillIdsInCategories).stream()
                .collect(Collectors.groupingBy(
                        WpEzQuestionSkillEntity::getSkillId,
                        Collectors.mapping(WpEzQuestionSkillEntity::getQuestionId, Collectors.toList())
                ));
    }

    /**
     * Get question count per skill.
     */
    public long getQuestionCountForSkill(Long skillId) {
        return questionSkillRepository.countQuestionsBySkillId(skillId);
    }

    /**
     * Map a question to a skill.
     */
    @Transactional
    public void mapQuestionToSkill(Long questionId, Long skillId, Integer difficulty, boolean isPrimary) {
        // Check if mapping already exists
        Optional<EilQuestionSkillEntity> existing = questionSkillRepository.findByQuestionIdAndSkillId(questionId, skillId);

        if (existing.isPresent()) {
            // Update existing mapping
            EilQuestionSkillEntity entity = existing.get();
            if (difficulty != null) {
                entity.setDifficulty(difficulty);
            }
            entity.setIsPrimary(isPrimary);
            questionSkillRepository.save(entity);
        } else {
            // Create new mapping
            EilQuestionSkillEntity entity = EilQuestionSkillEntity.builder()
                    .questionId(questionId)
                    .skillId(skillId)
                    .difficulty(difficulty != null ? difficulty : 3)
                    .isPrimary(isPrimary)
                    .build();
            questionSkillRepository.save(entity);
        }

        log.debug("Mapped question {} to skill {} with difficulty {} (primary: {})",
                questionId, skillId, difficulty, isPrimary);
    }

    /**
     * Convert skill entity to DTO.
     */
    public SkillDto toDto(EilSkillEntity entity) {
        if (entity == null) {
            return null;
        }

        // Get localized name based on Accept-Language header
        String localizedName = getLocalizedSkillName(entity);

        return SkillDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(localizedName)
                .category(entity.getCategory())
                .subcategory(entity.getSubcategory())
                .level(entity.getLevel())
                .build();
    }

    /**
     * Get localized skill name based on current locale from Accept-Language header.
     * Returns English (name) for "en" locale, Vietnamese (nameVi) for "vi" locale.
     */
    private String getLocalizedSkillName(EilSkillEntity skill) {
        String currentLanguage = messageService.getCurrentLanguage();

        if ("en".equals(currentLanguage)) {
            return skill.getName(); // English
        } else {
            // Default to Vietnamese for "vi" or any other language
            return skill.getNameVi() != null ? skill.getNameVi() : skill.getName();
        }
    }

    /**
     * Get all active categories.
     */
    public List<String> getAllActiveCategories() {
        return skillRepository.findAllActiveCategories();
    }

    /**
     * Get subcategories for a category.
     */
    public List<String> getSubcategoriesByCategory(String category) {
        return skillRepository.findSubcategoriesByCategory(category);
    }

    /**
     * Count leaf skills.
     */
    public long countLeafSkills() {
        return skillRepository.countLeafSkills();
    }

    /**
     * Count leaf skills by category.
     */
    public long countLeafSkillsByCategory(String category) {
        return skillRepository.countLeafSkillsByCategory(category);
    }

    /**
     * Get question IDs grouped by skill for a specific certification.
     * Uses wp_ez_skills as primary source.
     *
     * @param certificationCode Certification code (e.g., "PSM_I", "PSPO_I")
     * @return Map of skillId to list of questionIds
     */
    public Map<Long, List<Long>> getQuestionsGroupedBySkillForCertification(String certificationCode) {
        if (certificationCode == null || certificationCode.isEmpty()) {
            return getQuestionsGroupedBySkill();
        }

        // Get skills directly by certification_id from wp_ez_skills
        List<WpEzSkillEntity> skillsForCert = wpSkillRepository.findByCertificationIdAndStatusOrderBySortOrderAsc(
                certificationCode.toUpperCase(), STATUS_ACTIVE);

        if (skillsForCert.isEmpty()) {
            // Try with different variations
            skillsForCert = wpSkillRepository.findByCertificationIdAndStatusOrderBySortOrderAsc(certificationCode, STATUS_ACTIVE);
        }

        if (skillsForCert.isEmpty()) {
            log.warn("No skills found for certification: {}", certificationCode);
            return Map.of();
        }

        List<Long> skillIdsForCert = skillsForCert.stream()
                .map(WpEzSkillEntity::getId)
                .collect(Collectors.toList());

        log.info("Found {} skills for certification {}", skillIdsForCert.size(), certificationCode);

        // Get question mappings for these skills
        return wpQuestionSkillRepository.findBySkillIdIn(skillIdsForCert).stream()
                .collect(Collectors.groupingBy(
                        WpEzQuestionSkillEntity::getSkillId,
                        Collectors.mapping(WpEzQuestionSkillEntity::getQuestionId, Collectors.toList())
                ));
    }

    /**
     * Get question IDs grouped by skill for a career path.
     * Career paths map to multiple certifications and skill sets.
     * Uses wp_ez_skills as primary source.
     *
     * @param careerPath Career path code (e.g., "SCRUM_MASTER", "PRODUCT_OWNER", "DEVELOPER", "QA_ENGINEER")
     * @return Map of skillId to list of questionIds
     */
    public Map<Long, List<Long>> getQuestionsGroupedBySkillForCareerPath(String careerPath) {
        if (careerPath == null || careerPath.isEmpty()) {
            return getQuestionsGroupedBySkill();
        }

        // Map career paths to relevant certifications
        List<String> relevantCertifications = getCareerPathCertifications(careerPath);

        if (relevantCertifications.isEmpty()) {
            log.info("No specific certifications for career path {}, using all skills", careerPath);
            return getQuestionsGroupedBySkill();
        }

        log.info("Career path {} maps to certifications: {}", careerPath, relevantCertifications);
        return getQuestionsGroupedBySkillForCategories(relevantCertifications);
    }

    /**
     * Map career path to relevant certification IDs (wp_ez_skills.certification_id).
     */
    private List<String> getCareerPathCertifications(String careerPath) {
        if (careerPath == null) {
            return List.of();
        }

        switch (careerPath.toUpperCase()) {
            case "SCRUM_MASTER":
                return List.of("PSM_I", "SCRUM_PSM_II");
            case "PRODUCT_OWNER":
                return List.of("SCRUM_PSPO_I");
            case "DEVELOPER":
                return List.of("DEV_BACKEND", "DEV_FRONTEND", "DEV_NODEJS", "DEV_PYTHON", "JAVA_OCP_17");
            case "QA_ENGINEER":
            case "TESTER":
                return List.of("ISTQB_CTFL", "ISTQB_AGILE", "ISTQB_AI");
            case "BUSINESS_ANALYST":
                return List.of("CBAP", "CCBA", "ECBA");
            case "AGILE_COACH":
                return List.of("PSM_I", "SCRUM_PSPO_I", "SCRUM_PSM_II");
            case "PROJECT_MANAGER":
                return List.of("PMI_PMP");
            case "DEVOPS":
                return List.of("DEV_DEVOPS", "DOCKER_DCA", "KUBERNETES_CKA", "HASHICORP_TERRAFORM");
            case "CLOUD":
                return List.of("AWS_SAA_C03", "AWS_DVA_C02", "AZURE_AZ104", "GCP_ACE");
            case "SECURITY":
                return List.of("COMPTIA_SECURITY_PLUS", "ISC2_CISSP");
            default:
                log.info("Unknown career path: {}, returning empty certifications", careerPath);
                return List.of();
        }
    }
}
