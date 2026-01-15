-- Script 08: Fix corrupt questions (empty question text or no answers)

-- Step 1: Identify corrupt questions
SELECT '=== CORRUPT QUESTIONS REPORT ===' as title;

SELECT 'Total corrupt questions:' as info;
SELECT COUNT(*) as total
FROM wp_learndash_pro_quiz_question
WHERE online = 1
  AND (
      (question IS NULL OR question = '' OR LENGTH(question) < 5)
      OR (answer_data = 'a:0:{}' OR answer_data NOT LIKE '%b:1%')
  );

SELECT 'By category:' as info;
SELECT
    c.category_name,
    COUNT(q.id) as corrupt_count,
    GROUP_CONCAT(q.id ORDER BY q.id SEPARATOR ', ') as question_ids
FROM wp_learndash_pro_quiz_question q
LEFT JOIN wp_learndash_pro_quiz_category c ON q.category_id = c.category_id
WHERE q.online = 1
  AND (
      (q.question IS NULL OR q.question = '' OR LENGTH(q.question) < 5)
      OR (q.answer_data = 'a:0:{}' OR q.answer_data NOT LIKE '%b:1%')
  )
GROUP BY c.category_name;

-- Step 2: Disable corrupt questions (set online = 0)
UPDATE wp_learndash_pro_quiz_question
SET online = 0
WHERE online = 1
  AND (
      (question IS NULL OR question = '' OR LENGTH(question) < 5)
      OR (answer_data = 'a:0:{}' OR answer_data NOT LIKE '%b:1%')
  );

SELECT 'Disabled corrupt questions' as action;
SELECT ROW_COUNT() as disabled_count;

-- Step 3: Remove mappings for disabled questions
DELETE qs FROM wp_ez_question_skills qs
WHERE NOT EXISTS (
    SELECT 1 FROM wp_learndash_pro_quiz_question q
    WHERE q.id = qs.question_id AND q.online = 1
);

SELECT 'Cleaned up skill mappings' as action;
SELECT ROW_COUNT() as removed_mappings;

-- Step 4: Verify final state
SELECT 'After cleanup:' as info;
SELECT
    COUNT(*) as total_active,
    COUNT(CASE WHEN LENGTH(question) < 5 THEN 1 END) as empty_text,
    COUNT(CASE WHEN answer_data = 'a:0:{}' THEN 1 END) as empty_answers,
    COUNT(CASE WHEN answer_data NOT LIKE '%b:1%' THEN 1 END) as no_correct_answer
FROM wp_learndash_pro_quiz_question
WHERE online = 1;
