-- Script 03: Sync questions to wp_ez_question_skills table
-- Map questions from categories to the unified wp_ez_skills system

-- Create mapping from category to certification
-- This creates a temporary mapping table
DROP TEMPORARY TABLE IF EXISTS category_cert_mapping;
CREATE TEMPORARY TABLE category_cert_mapping (
    category_id INT,
    certification_id VARCHAR(50)
);

INSERT INTO category_cert_mapping VALUES
(1, 'PSM_I'),
(2, 'ISTQB_CTFL'),
(3, 'ISTQB_CTFL'),
(4, 'ISTQB_CTFL'),
(5, 'CCBA'),
(6, 'CCBA'),
(7, 'CCBA'),
(8, 'CCBA'),
(9, 'CCBA'),
(10, 'CCBA'),
(11, 'SCRUM_PSPO_I'),
(12, 'SCRUM_PSM_II'),
(13, 'SCRUM_PSPO_I'),  -- PSPO_II mapped to PSPO_I cert
(14, 'CCBA'),
(15, 'ECBA'),
(16, 'ISTQB_CTFL'),  -- Test Analyst
(17, 'ISTQB_CTFL'),  -- Test Manager
(18, 'ISTQB_CTFL'),  -- TTA
(19, 'CBAP'),
(20, 'CBAP'),
(21, 'CBAP'),
(22, 'CBAP'),
(23, 'CBAP'),
(24, 'CBAP'),
(25, 'ISTQB_CTFL'),
(26, 'CBAP'),
(27, 'PSM_I');  -- PSK mapped to PSM_I

-- Step 1: Get leaf skills (skills without children) for each certification
-- These are the skills we can map questions to

-- Step 2: For each question with a category, map to a random leaf skill of that certification
-- First, let's see what skills exist for each certification
SELECT 'Skills per certification:' as info;
SELECT certification_id, COUNT(*) as skill_count
FROM wp_ez_skills
WHERE status = 'active'
GROUP BY certification_id
ORDER BY skill_count DESC
LIMIT 20;

-- Step 3: Insert question-skill mappings
-- For questions that don't have mappings yet
INSERT IGNORE INTO wp_ez_question_skills (question_id, skill_id, weight, confidence, mapped_by, mapped_at)
SELECT
    q.id as question_id,
    (
        SELECT s.id
        FROM wp_ez_skills s
        WHERE s.certification_id = ccm.certification_id
          AND s.status = 'active'
          AND NOT EXISTS (
              SELECT 1 FROM wp_ez_skills child
              WHERE child.parent_id = s.id AND child.status = 'active'
          )
        ORDER BY RAND()
        LIMIT 1
    ) as skill_id,
    1.00 as weight,
    'medium' as confidence,
    NULL as mapped_by,
    NOW() as mapped_at
FROM wp_learndash_pro_quiz_question q
JOIN category_cert_mapping ccm ON q.category_id = ccm.category_id
WHERE q.online = 1
  AND q.category_id > 0
  AND NOT EXISTS (
      SELECT 1 FROM wp_ez_question_skills qs WHERE qs.question_id = q.id
  );

-- Verify results
SELECT 'Question-skill mappings after sync:' as info;
SELECT s.certification_id, COUNT(DISTINCT qs.question_id) as mapped_questions
FROM wp_ez_question_skills qs
JOIN wp_ez_skills s ON qs.skill_id = s.id
GROUP BY s.certification_id
ORDER BY mapped_questions DESC;

DROP TEMPORARY TABLE IF EXISTS category_cert_mapping;
