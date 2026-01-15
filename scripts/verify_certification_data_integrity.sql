-- ==========================================
-- CERTIFICATION DATA INTEGRITY CHECK
-- ==========================================
-- This script verifies that all certifications have:
-- 1. Skills mapped
-- 2. Questions mapped to those skills
-- 3. Quiz posts exist for those questions

-- ==========================================
-- 1. CERTIFICATIONS WITHOUT SKILLS
-- ==========================================
SELECT
    c.certification_id,
    c.full_name,
    c.short_name,
    c.category,
    c.is_active,
    COUNT(s.id) as skill_count
FROM wp_ez_certifications c
LEFT JOIN wp_ez_skills s ON c.certification_id = s.certification_id AND s.status = 'active'
GROUP BY c.certification_id, c.full_name, c.short_name, c.category, c.is_active
HAVING skill_count = 0
ORDER BY c.sort_order;

-- ==========================================
-- 2. CERTIFICATIONS WITHOUT QUESTIONS
-- ==========================================
SELECT
    c.certification_id,
    c.full_name,
    c.short_name,
    c.category,
    c.is_active,
    COUNT(DISTINCT qs.question_id) as question_count
FROM wp_ez_certifications c
LEFT JOIN wp_ez_skills s ON c.certification_id = s.certification_id AND s.status = 'active'
LEFT JOIN wp_ez_question_skills qs ON s.id = qs.skill_id
GROUP BY c.certification_id, c.full_name, c.short_name, c.category, c.is_active
HAVING question_count = 0
ORDER BY c.sort_order;

-- ==========================================
-- 3. FULL CERTIFICATION SUMMARY
-- ==========================================
SELECT
    c.certification_id,
    c.full_name AS name_en,
    c.short_name AS name_vi,
    c.category,
    c.difficulty_level,
    c.vendor,
    c.is_active,
    COUNT(DISTINCT s.id) as total_skills,
    COUNT(DISTINCT CASE WHEN s.parent_id IS NULL THEN s.id END) as root_skills,
    COUNT(DISTINCT qs.question_id) as total_questions,
    COUNT(DISTINCT qm.id) as quiz_posts_count
FROM wp_ez_certifications c
LEFT JOIN wp_ez_skills s ON c.certification_id = s.certification_id AND s.status = 'active'
LEFT JOIN wp_ez_question_skills qs ON s.id = qs.skill_id
LEFT JOIN wp_learndash_pro_quiz_master qm ON qs.question_id = qm.id
GROUP BY c.certification_id, c.full_name, c.short_name, c.category, c.difficulty_level, c.vendor, c.is_active
ORDER BY c.sort_order;

-- ==========================================
-- 4. SKILLS WITHOUT QUESTIONS
-- ==========================================
SELECT
    c.certification_id,
    c.full_name,
    s.id as skill_id,
    s.code as skill_code,
    s.name as skill_name,
    s.level,
    COUNT(qs.question_id) as question_count
FROM wp_ez_certifications c
JOIN wp_ez_skills s ON c.certification_id = s.certification_id AND s.status = 'active'
LEFT JOIN wp_ez_question_skills qs ON s.id = qs.skill_id
GROUP BY c.certification_id, c.full_name, s.id, s.code, s.name, s.level
HAVING question_count = 0
ORDER BY c.certification_id, s.level, s.sort_order;

-- ==========================================
-- 5. QUESTIONS WITHOUT QUIZ POSTS
-- ==========================================
SELECT
    qs.question_id,
    COUNT(DISTINCT qs.skill_id) as skill_count,
    GROUP_CONCAT(DISTINCT s.certification_id) as certifications,
    qm.id as quiz_master_id,
    qm.name as quiz_name
FROM wp_ez_question_skills qs
JOIN wp_ez_skills s ON qs.skill_id = s.id AND s.status = 'active'
LEFT JOIN wp_learndash_pro_quiz_master qm ON qs.question_id = qm.id
WHERE qm.id IS NULL
GROUP BY qs.question_id, qm.id, qm.name
LIMIT 100;

-- ==========================================
-- 6. ACTIVE CERTIFICATIONS READY FOR USE
-- ==========================================
-- Certifications that are complete and ready for diagnostic/practice
SELECT
    c.certification_id,
    c.full_name,
    c.category,
    c.difficulty_level,
    COUNT(DISTINCT s.id) as skills,
    COUNT(DISTINCT qs.question_id) as questions,
    'READY' as status
FROM wp_ez_certifications c
JOIN wp_ez_skills s ON c.certification_id = s.certification_id AND s.status = 'active'
JOIN wp_ez_question_skills qs ON s.id = qs.skill_id
JOIN wp_learndash_pro_quiz_master qm ON qs.question_id = qm.id
WHERE c.is_active = 1
GROUP BY c.certification_id, c.full_name, c.category, c.difficulty_level
HAVING skills > 0 AND questions > 0
ORDER BY c.sort_order;

-- ==========================================
-- 7. CERTIFICATIONS WITH ISSUES
-- ==========================================
-- Certifications that have problems
SELECT
    c.certification_id,
    c.full_name,
    c.is_active,
    COUNT(DISTINCT s.id) as skills,
    COUNT(DISTINCT qs.question_id) as questions,
    CASE
        WHEN COUNT(DISTINCT s.id) = 0 THEN 'NO_SKILLS'
        WHEN COUNT(DISTINCT qs.question_id) = 0 THEN 'NO_QUESTIONS'
        WHEN COUNT(DISTINCT qs.question_id) < 10 THEN 'TOO_FEW_QUESTIONS'
        ELSE 'UNKNOWN'
    END as issue
FROM wp_ez_certifications c
LEFT JOIN wp_ez_skills s ON c.certification_id = s.certification_id AND s.status = 'active'
LEFT JOIN wp_ez_question_skills qs ON s.id = qs.skill_id
WHERE c.is_active = 1
GROUP BY c.certification_id, c.full_name, c.is_active
HAVING COUNT(DISTINCT s.id) = 0
    OR COUNT(DISTINCT qs.question_id) = 0
    OR COUNT(DISTINCT qs.question_id) < 10
ORDER BY c.sort_order;
