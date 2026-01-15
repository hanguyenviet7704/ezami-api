-- Script 05: Map remaining questions to skills

-- Map ISTQB_AGILE questions (category 28)
INSERT IGNORE INTO wp_ez_question_skills (question_id, skill_id, weight, confidence, mapped_at)
SELECT
    q.id as question_id,
    (
        SELECT s.id
        FROM wp_ez_skills s
        WHERE s.certification_id = 'ISTQB_AGILE'
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
    NOW() as mapped_at
FROM wp_learndash_pro_quiz_question q
WHERE q.online = 1
  AND q.category_id = 28
  AND NOT EXISTS (
      SELECT 1 FROM wp_ez_question_skills qs WHERE qs.question_id = q.id
  );

-- Map ISTQB_AI questions (category 29)
INSERT IGNORE INTO wp_ez_question_skills (question_id, skill_id, weight, confidence, mapped_at)
SELECT
    q.id as question_id,
    (
        SELECT s.id
        FROM wp_ez_skills s
        WHERE s.certification_id = 'ISTQB_AI'
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
    NOW() as mapped_at
FROM wp_learndash_pro_quiz_question q
WHERE q.online = 1
  AND q.category_id = 29
  AND NOT EXISTS (
      SELECT 1 FROM wp_ez_question_skills qs WHERE qs.question_id = q.id
  );

-- Final summary
SELECT 'Final question-skill mappings:' as info;
SELECT s.certification_id, COUNT(DISTINCT qs.question_id) as mapped_questions
FROM wp_ez_question_skills qs
JOIN wp_ez_skills s ON qs.skill_id = s.id
GROUP BY s.certification_id
ORDER BY mapped_questions DESC;

SELECT 'Total mapped questions:' as info;
SELECT COUNT(DISTINCT question_id) as total FROM wp_ez_question_skills;

SELECT 'Total questions in database:' as info;
SELECT COUNT(*) as total FROM wp_learndash_pro_quiz_question WHERE online = 1;
