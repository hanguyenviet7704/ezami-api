-- Script 06: Add basic explanations for questions missing them
-- This creates placeholder explanations that can be improved later via WordPress admin

-- Step 1: Add generic explanation for completely missing ones
UPDATE wp_learndash_pro_quiz_question
SET
    correct_msg = CONCAT('✓ Correct!\n\nThis is the correct answer for question: ', title, '\n\nFor detailed explanation, please refer to the official documentation or study materials.'),
    incorrect_msg = CONCAT('✗ Incorrect\n\nPlease review the question and try again. Check the answer explanation when you submit to understand the correct approach.')
WHERE online = 1
  AND (correct_msg IS NULL OR correct_msg = '' OR correct_msg = '[Explanation needed - please review]')
  AND id < 26876;  -- Don't overwrite new AWS/Azure/K8s questions

-- Step 2: Update questions where correct_msg = incorrect_msg
UPDATE wp_learndash_pro_quiz_question
SET incorrect_msg = CONCAT('✗ Incorrect\n\n', correct_msg)
WHERE online = 1
  AND correct_msg = incorrect_msg
  AND correct_msg != ''
  AND correct_msg != '[Explanation needed - please review]'
  AND LENGTH(correct_msg) > 20;

-- Step 3: Verify results
SELECT 'After adding explanations:' as info;
SELECT
  COUNT(*) as total,
  COUNT(CASE WHEN correct_msg IS NULL OR correct_msg = '' OR LENGTH(correct_msg) < 10 THEN 1 END) as still_missing,
  COUNT(CASE WHEN correct_msg = incorrect_msg THEN 1 END) as same_messages
FROM wp_learndash_pro_quiz_question
WHERE online = 1;

-- Summary by category
SELECT 'By category:' as info;
SELECT c.category_name,
       COUNT(q.id) as total,
       COUNT(CASE WHEN LENGTH(q.correct_msg) < 20 THEN 1 END) as needs_improvement
FROM wp_learndash_pro_quiz_question q
JOIN wp_learndash_pro_quiz_category c ON q.category_id = c.category_id
WHERE q.online = 1
GROUP BY c.category_name
HAVING total > 50
ORDER BY needs_improvement DESC
LIMIT 15;
