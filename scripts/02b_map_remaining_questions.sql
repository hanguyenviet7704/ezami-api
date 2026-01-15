-- Script 02b: Map remaining uncategorized questions

-- Add new categories for ISTQB Agile and ISTQB AI
INSERT IGNORE INTO wp_learndash_pro_quiz_category (category_id, category_name) VALUES
(28, 'ISTQB_AGILE'),
(29, 'ISTQB_AI');

-- Map ISTQB Agile questions (new category_id = 28)
UPDATE wp_learndash_pro_quiz_question q
JOIN wp_learndash_pro_quiz_master m ON q.quiz_id = m.id
SET q.category_id = 28
WHERE q.category_id = 0
  AND q.online = 1
  AND m.name LIKE '%ISTQB%Agile%';

-- Map ISTQB AI questions (new category_id = 29)
UPDATE wp_learndash_pro_quiz_question q
JOIN wp_learndash_pro_quiz_master m ON q.quiz_id = m.id
SET q.category_id = 29
WHERE q.category_id = 0
  AND q.online = 1
  AND m.name LIKE '%ISTQB%AI%';

-- Verify results
SELECT 'Updated categories:' as info;
SELECT category_id, category_name FROM wp_learndash_pro_quiz_category WHERE category_id >= 28 ORDER BY category_id;

SELECT 'Remaining uncategorized:' as info;
SELECT COUNT(*) as uncategorized_count FROM wp_learndash_pro_quiz_question WHERE online = 1 AND category_id = 0;

SELECT 'Final question count by category:' as info;
SELECT c.category_name, COUNT(q.id) as question_count
FROM wp_learndash_pro_quiz_category c
LEFT JOIN wp_learndash_pro_quiz_question q ON c.category_id = q.category_id AND q.online = 1
GROUP BY c.category_id, c.category_name
HAVING question_count > 0
ORDER BY question_count DESC;
