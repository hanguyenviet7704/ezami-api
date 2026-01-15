-- Script 02: Map uncategorized questions to correct categories based on quiz name
-- This maps questions with category_id = 0 to the correct category

-- PSM_I questions (category_id = 1)
UPDATE wp_learndash_pro_quiz_question q
JOIN wp_learndash_pro_quiz_master m ON q.quiz_id = m.id
SET q.category_id = 1
WHERE q.category_id = 0
  AND q.online = 1
  AND (m.name LIKE '%PSM%I%' OR m.name LIKE '%PSM_I%' OR m.name LIKE '%Scrum Master%');

-- PSPO_I questions (category_id = 11)
UPDATE wp_learndash_pro_quiz_question q
JOIN wp_learndash_pro_quiz_master m ON q.quiz_id = m.id
SET q.category_id = 11
WHERE q.category_id = 0
  AND q.online = 1
  AND (m.name LIKE '%PSPO%I%' OR m.name LIKE '%PSPO_I%' OR m.name LIKE '%Product Owner%I%');

-- PSM_II questions (category_id = 12)
UPDATE wp_learndash_pro_quiz_question q
JOIN wp_learndash_pro_quiz_master m ON q.quiz_id = m.id
SET q.category_id = 12
WHERE q.category_id = 0
  AND q.online = 1
  AND (m.name LIKE '%PSM%II%' OR m.name LIKE '%PSM_II%');

-- PSPO_II questions (category_id = 13)
UPDATE wp_learndash_pro_quiz_question q
JOIN wp_learndash_pro_quiz_master m ON q.quiz_id = m.id
SET q.category_id = 13
WHERE q.category_id = 0
  AND q.online = 1
  AND (m.name LIKE '%PSPO%II%' OR m.name LIKE '%PSPO_II%');

-- PSK questions (category_id = 27)
UPDATE wp_learndash_pro_quiz_question q
JOIN wp_learndash_pro_quiz_master m ON q.quiz_id = m.id
SET q.category_id = 27
WHERE q.category_id = 0
  AND q.online = 1
  AND (m.name LIKE '%PSK%' OR m.name LIKE '%Scrum with Kanban%');

-- CCBA questions (category_id = 14)
UPDATE wp_learndash_pro_quiz_question q
JOIN wp_learndash_pro_quiz_master m ON q.quiz_id = m.id
SET q.category_id = 14
WHERE q.category_id = 0
  AND q.online = 1
  AND (m.name LIKE '%CCBA%' AND m.name NOT LIKE '%CBAP%');

-- CBAP questions (category_id = 26)
UPDATE wp_learndash_pro_quiz_question q
JOIN wp_learndash_pro_quiz_master m ON q.quiz_id = m.id
SET q.category_id = 26
WHERE q.category_id = 0
  AND q.online = 1
  AND m.name LIKE '%CBAP%';

-- ECBA questions (category_id = 15)
UPDATE wp_learndash_pro_quiz_question q
JOIN wp_learndash_pro_quiz_master m ON q.quiz_id = m.id
SET q.category_id = 15
WHERE q.category_id = 0
  AND q.online = 1
  AND m.name LIKE '%ECBA%';

-- ISTQB CTFL questions (category_id = 25)
UPDATE wp_learndash_pro_quiz_question q
JOIN wp_learndash_pro_quiz_master m ON q.quiz_id = m.id
SET q.category_id = 25
WHERE q.category_id = 0
  AND q.online = 1
  AND (m.name LIKE '%ISTQB%CTFL%' OR m.name LIKE '%ISTQB%Foundation%');

-- ISTQB Test Manager - TM questions (category_id = 17)
UPDATE wp_learndash_pro_quiz_question q
JOIN wp_learndash_pro_quiz_master m ON q.quiz_id = m.id
SET q.category_id = 17
WHERE q.category_id = 0
  AND q.online = 1
  AND (m.name LIKE '%CTAL%TM%' OR m.name LIKE '%Test Manager%');

-- ISTQB Technical Test Analyst - TTA questions (category_id = 18)
UPDATE wp_learndash_pro_quiz_question q
JOIN wp_learndash_pro_quiz_master m ON q.quiz_id = m.id
SET q.category_id = 18
WHERE q.category_id = 0
  AND q.online = 1
  AND (m.name LIKE '%CTAL%TTA%' OR m.name LIKE '%Technical Test Analyst%');

-- ISTQB Test Analyst - TA questions (category_id = 16)
UPDATE wp_learndash_pro_quiz_question q
JOIN wp_learndash_pro_quiz_master m ON q.quiz_id = m.id
SET q.category_id = 16
WHERE q.category_id = 0
  AND q.online = 1
  AND (m.name LIKE '%ISTQB%TA%' OR m.name LIKE '%Test Analyst%');

-- Verify results
SELECT 'Questions after mapping:' as info;
SELECT c.category_name, COUNT(q.id) as question_count
FROM wp_learndash_pro_quiz_category c
LEFT JOIN wp_learndash_pro_quiz_question q ON c.category_id = q.category_id AND q.online = 1
WHERE c.category_id <= 30
GROUP BY c.category_id, c.category_name
ORDER BY question_count DESC;

-- Check remaining uncategorized
SELECT 'Remaining uncategorized:' as info;
SELECT COUNT(*) as uncategorized_count FROM wp_learndash_pro_quiz_question WHERE online = 1 AND category_id = 0;
