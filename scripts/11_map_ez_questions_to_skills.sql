-- Script 11: Map imported wp_ez questions to skills using original mapping

-- Create mapping from imported questions back to wp_ez_diagnostic_questions
INSERT INTO wp_ez_question_skills (question_id, skill_id, weight, confidence, mapped_at)
SELECT
    q.id as question_id,
    ezq.skill_id,
    1.00 as weight,
    'high' as confidence,
    NOW() as mapped_at
FROM wp_learndash_pro_quiz_question q
JOIN wp_ez_diagnostic_questions ezq
    ON q.title = CONCAT('EZ_', ezq.category_code, '_', ezq.id)
WHERE q.online = 1
  AND q.title LIKE 'EZ_%'
  AND ezq.status = 'active'
  AND NOT EXISTS (
      SELECT 1 FROM wp_ez_question_skills qs WHERE qs.question_id = q.id
  );

SELECT 'Mapped imported questions to skills' as action;
SELECT ROW_COUNT() as mapped_count;

-- Verify mappings
SELECT 'Final skill mappings by certification:' as info;
SELECT s.certification_id, COUNT(DISTINCT qs.question_id) as questions, COUNT(DISTINCT s.id) as skills_used
FROM wp_ez_question_skills qs
JOIN wp_ez_skills s ON qs.skill_id = s.id
JOIN wp_learndash_pro_quiz_question q ON qs.question_id = q.id
WHERE q.online = 1 AND s.status = 'active'
GROUP BY s.certification_id
ORDER BY questions DESC
LIMIT 25;

-- Overall summary
SELECT 'GRAND TOTAL:' as summary;
SELECT
    (SELECT COUNT(*) FROM wp_learndash_pro_quiz_question WHERE online = 1) as total_questions,
    (SELECT COUNT(DISTINCT question_id) FROM wp_ez_question_skills) as mapped_questions,
    (SELECT COUNT(DISTINCT certification_id) FROM wp_ez_skills WHERE status = 'active') as certifications_covered
;
