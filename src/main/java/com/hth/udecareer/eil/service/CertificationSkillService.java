package com.hth.udecareer.eil.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.hth.udecareer.eil.entities.WpEzCertificationEntity;
import com.hth.udecareer.eil.entities.WpEzSkillEntity;
import com.hth.udecareer.eil.model.response.CertificationResponse;
import com.hth.udecareer.eil.model.response.CertificationSkillResponse;
import com.hth.udecareer.eil.model.response.CertificationSkillTreeResponse;
import com.hth.udecareer.eil.repository.WpEzCertificationRepository;
import com.hth.udecareer.eil.repository.WpEzQuestionSkillRepository;
import com.hth.udecareer.eil.repository.WpEzSkillRepository;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.service.MessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing certification skills from WordPress wp_ez_skills table.
 * Provides hierarchical skill tree, question mappings, and certification info.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CertificationSkillService {

        private final WpEzCertificationRepository certificationRepository;
        private final WpEzSkillRepository skillRepository;
        private final WpEzQuestionSkillRepository questionSkillRepository;

        /**
         * Get all available certifications with skill counts.
         */
        @Cacheable(value = "certifications", key = "'all'")
        public List<CertificationResponse> getAllCertifications() {
                log.debug("Getting all certifications from wp_ez_certifications table");

                // Get all active certifications from wp_ez_certifications table
                List<WpEzCertificationEntity> certifications = certificationRepository
                                .findByIsActiveTrueOrderBySortOrderAsc();

                // Get skill counts per certification
                List<Object[]> skillCounts = skillRepository.countSkillsByCertification();
                Map<String, Long> skillCountMap = skillCounts.stream()
                                .collect(Collectors.toMap(
                                                row -> (String) row[0],
                                                row -> (Long) row[1]));

                // Get question counts per certification
                Map<String, Integer> questionCountMap = new HashMap<>();
                for (WpEzCertificationEntity cert : certifications) {
                        List<Long> questionIds = questionSkillRepository
                                        .findQuestionIdsByCertification(cert.getCertificationId());
                        questionCountMap.put(cert.getCertificationId(), questionIds.size());
                }

                return certifications.stream()
                                .map(cert -> CertificationResponse.builder()
                                                .certificationId(cert.getCertificationId())
                                                .name(cert.getFullName())  // English name
                                                .nameVi(cert.getShortName())  // Vietnamese name
                                                .description(cert.getAcronym())
                                                .primaryCategory(cert.getCategory())
                                                .level(cert.getDifficultyLevel())
                                                .vendor(cert.getVendor())
                                                .examCode(cert.getExamCode())
                                                .skillCount(skillCountMap.getOrDefault(cert.getCertificationId(), 0L)
                                                                .intValue())
                                                .questionCount(questionCountMap.getOrDefault(cert.getCertificationId(),
                                                                0))
                                                .isFeatured(false)
                                                .build())
                                .collect(Collectors.toList());
        }

        /**
         * Get certification details by ID.
         */
        public CertificationResponse getCertification(String certificationId) throws AppException {
                List<WpEzSkillEntity> skills = skillRepository
                                .findByCertificationIdAndStatusOrderBySortOrderAsc(certificationId, "active");

                if (skills.isEmpty()) {
                        throw new AppException(ErrorCode.EIL_SKILL_NOT_FOUND);
                }

                WpEzSkillEntity rootSkill = skills.stream()
                                .filter(s -> s.getParentId() == null)
                                .findFirst()
                                .orElse(skills.get(0));

                List<Long> questionIds = questionSkillRepository.findQuestionIdsByCertification(certificationId);

                return CertificationResponse.builder()
                                .certificationId(certificationId)
                                .name(rootSkill.getName())
                                .skillCount(skills.size())
                                .questionCount(questionIds.size())
                                .build();
        }

        /**
         * Get complete skill tree for a certification.
         */
        @Cacheable(value = "skillTree", key = "#certificationId")
        public CertificationSkillTreeResponse getSkillTree(String certificationId) throws AppException {
                log.debug("Getting skill tree for certification: {}", certificationId);

                List<WpEzSkillEntity> allSkills = skillRepository
                                .findByCertificationIdAndStatusOrderBySortOrderAsc(certificationId, "active");

                if (allSkills.isEmpty()) {
                        throw new AppException(ErrorCode.EIL_SKILL_NOT_FOUND);
                }

                // Get question counts per skill
                List<Object[]> questionCounts = questionSkillRepository
                                .countQuestionsBySkillForCertification(certificationId);
                Map<Long, Long> questionCountMap = questionCounts.stream()
                                .collect(Collectors.toMap(
                                                row -> (Long) row[0],
                                                row -> (Long) row[1]));

                // Build tree structure
                Map<Long, List<WpEzSkillEntity>> childrenMap = allSkills.stream()
                                .filter(s -> s.getParentId() != null)
                                .collect(Collectors.groupingBy(WpEzSkillEntity::getParentId));

                List<WpEzSkillEntity> rootSkills = allSkills.stream()
                                .filter(s -> s.getParentId() == null)
                                .sorted(Comparator.comparing(WpEzSkillEntity::getSortOrder))
                                .collect(Collectors.toList());

                String certName = rootSkills.isEmpty() ? certificationId : rootSkills.get(0).getName();

                List<CertificationSkillResponse> skillTree = rootSkills.stream()
                                .map(root -> buildSkillNode(root, childrenMap, questionCountMap))
                                .collect(Collectors.toList());

                // Total questions
                List<Long> allQuestionIds = questionSkillRepository.findQuestionIdsByCertification(certificationId);

                return CertificationSkillTreeResponse.builder()
                                .certificationId(certificationId)
                                .certificationName(certName)
                                .totalSkills(allSkills.size())
                                .totalQuestions(allQuestionIds.size())
                                .skills(skillTree)
                                .build();
        }

        /**
         * Get flat list of all skills for a certification.
         */
        public List<CertificationSkillResponse> getSkillsList(String certificationId) {
                List<WpEzSkillEntity> skills = skillRepository
                                .findByCertificationIdAndStatusOrderBySortOrderAsc(certificationId, "active");

                // Get question counts
                List<Object[]> questionCounts = questionSkillRepository
                                .countQuestionsBySkillForCertification(certificationId);
                Map<Long, Long> questionCountMap = questionCounts.stream()
                                .collect(Collectors.toMap(
                                                row -> (Long) row[0],
                                                row -> (Long) row[1]));

                return skills.stream()
                                .map(skill -> toSkillResponse(skill,
                                                questionCountMap.getOrDefault(skill.getId(), 0L).intValue()))
                                .collect(Collectors.toList());
        }

        /**
         * Get leaf skills (skills with no children) for a certification.
         */
        public List<CertificationSkillResponse> getLeafSkills(String certificationId) {
                List<WpEzSkillEntity> leafSkills = skillRepository.findLeafSkillsByCertification(certificationId);

                List<Object[]> questionCounts = questionSkillRepository
                                .countQuestionsBySkillForCertification(certificationId);
                Map<Long, Long> questionCountMap = questionCounts.stream()
                                .collect(Collectors.toMap(
                                                row -> (Long) row[0],
                                                row -> (Long) row[1]));

                return leafSkills.stream()
                                .map(skill -> toSkillResponse(skill,
                                                questionCountMap.getOrDefault(skill.getId(), 0L).intValue()))
                                .collect(Collectors.toList());
        }

        /**
         * Get skill by ID.
         */
        public CertificationSkillResponse getSkillById(Long skillId) throws AppException {
                WpEzSkillEntity skill = skillRepository.findById(skillId)
                                .orElseThrow(() -> new AppException(ErrorCode.EIL_SKILL_NOT_FOUND));

                long questionCount = questionSkillRepository.countBySkillId(skillId);

                return toSkillResponse(skill, (int) questionCount);
        }

        /**
         * Get skills by level for a certification.
         */
        public List<CertificationSkillResponse> getSkillsByLevel(String certificationId, int level) {
                List<WpEzSkillEntity> skills = skillRepository
                                .findByCertificationIdAndLevelAndStatusOrderBySortOrderAsc(certificationId, level,
                                                "active");

                List<Object[]> questionCounts = questionSkillRepository
                                .countQuestionsBySkillForCertification(certificationId);
                Map<Long, Long> questionCountMap = questionCounts.stream()
                                .collect(Collectors.toMap(
                                                row -> (Long) row[0],
                                                row -> (Long) row[1]));

                return skills.stream()
                                .map(skill -> toSkillResponse(skill,
                                                questionCountMap.getOrDefault(skill.getId(), 0L).intValue()))
                                .collect(Collectors.toList());
        }

        /**
         * Search skills by keyword.
         */
        public List<CertificationSkillResponse> searchSkills(String certificationId, String keyword) {
                List<WpEzSkillEntity> skills = skillRepository.searchSkillsByCertification(certificationId, keyword);

                List<Object[]> questionCounts = questionSkillRepository
                                .countQuestionsBySkillForCertification(certificationId);
                Map<Long, Long> questionCountMap = questionCounts.stream()
                                .collect(Collectors.toMap(
                                                row -> (Long) row[0],
                                                row -> (Long) row[1]));

                return skills.stream()
                                .map(skill -> toSkillResponse(skill,
                                                questionCountMap.getOrDefault(skill.getId(), 0L).intValue()))
                                .collect(Collectors.toList());
        }

        /**
         * Get question IDs mapped to a specific skill.
         */
        public List<Long> getQuestionIdsForSkill(Long skillId) {
                return questionSkillRepository.findConfidentQuestionIdsBySkillId(skillId);
        }

        /**
         * Get skill IDs for a question.
         */
        public List<Long> getSkillIdsForQuestion(Long questionId) {
                return questionSkillRepository.findSkillIdsByQuestionId(questionId);
        }

        // ============= HELPER METHODS =============

        private CertificationSkillResponse buildSkillNode(
                        WpEzSkillEntity skill,
                        Map<Long, List<WpEzSkillEntity>> childrenMap,
                        Map<Long, Long> questionCountMap) {

                List<WpEzSkillEntity> children = childrenMap.getOrDefault(skill.getId(), Collections.emptyList());

                List<CertificationSkillResponse> childResponses = children.stream()
                                .sorted(Comparator.comparing(WpEzSkillEntity::getSortOrder))
                                .map(child -> buildSkillNode(child, childrenMap, questionCountMap))
                                .collect(Collectors.toList());

                return CertificationSkillResponse.builder()
                                .id(skill.getId())
                                .parentId(skill.getParentId())
                                .certificationId(skill.getCertificationId())
                                .code(skill.getCode())
                                .name(skill.getName())
                                .description(skill.getDescription())
                                .level(skill.getLevel())
                                .sortOrder(skill.getSortOrder())
                                .questionCount(questionCountMap.getOrDefault(skill.getId(), 0L).intValue())
                                .children(childResponses.isEmpty() ? null : childResponses)
                                .build();
        }

        private CertificationSkillResponse toSkillResponse(WpEzSkillEntity skill, int questionCount) {
                return CertificationSkillResponse.builder()
                                .id(skill.getId())
                                .parentId(skill.getParentId())
                                .certificationId(skill.getCertificationId())
                                .code(skill.getCode())
                                .name(skill.getName())
                                .description(skill.getDescription())
                                .level(skill.getLevel())
                                .sortOrder(skill.getSortOrder())
                                .questionCount(questionCount)
                                .children(null)
                                .build();
        }

}
