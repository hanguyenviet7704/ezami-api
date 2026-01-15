-- Script 09: Remove duplicate questions

-- Step 1: Find and disable duplicates (keep first ID, disable rest)
UPDATE wp_learndash_pro_quiz_question q1
SET q1.online = 0
WHERE q1.online = 1
  AND EXISTS (
      SELECT 1 FROM wp_learndash_pro_quiz_question q2
      WHERE q2.online = 1
        AND q2.title = q1.title
        AND q2.question = q1.question
        AND q2.id < q1.id  -- Keep the one with smaller ID
  );

SELECT 'Disabled duplicate questions' as action;
SELECT ROW_COUNT() as disabled_count;

-- Step 2: Clean up skill mappings for disabled questions
DELETE qs FROM wp_ez_question_skills qs
WHERE NOT EXISTS (
    SELECT 1 FROM wp_learndash_pro_quiz_question q
    WHERE q.id = qs.question_id AND q.online = 1
);

SELECT 'Removed orphaned skill mappings' as action;
SELECT ROW_COUNT() as removed_count;

-- Step 3: Verify no duplicates remain
SELECT 'Remaining duplicates:' as check_type;
SELECT COUNT(*) as dup_count
FROM (
    SELECT title, question
    FROM wp_learndash_pro_quiz_question
    WHERE online = 1 AND LENGTH(question) > 10
    GROUP BY title, question
    HAVING COUNT(*) > 1
) as dupes;

-- Step 4: Final summary
SELECT 'Final active questions:' as info;
SELECT
    COUNT(*) as total_active,
    COUNT(DISTINCT title) as unique_titles,
    COUNT(DISTINCT CONCAT(title, question)) as unique_questions
FROM wp_learndash_pro_quiz_question
WHERE online = 1;
