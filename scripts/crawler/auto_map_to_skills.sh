#!/bin/bash
# Auto-map questions to skills based on certification

if [ "$#" -ne 2 ]; then
    echo "Usage: ./auto_map_to_skills.sh <certification_code> <category_id>"
    echo "Example: ./auto_map_to_skills.sh AWS_SAA_C03 30"
    exit 1
fi

CERT_CODE=$1
CATEGORY_ID=$2

echo "Mapping questions from category $CATEGORY_ID to $CERT_CODE skills..."

docker exec ezami-mysql mysql -uroot -p'12345678aA@' wordpress << EOF
INSERT IGNORE INTO wp_ez_question_skills (question_id, skill_id, weight, confidence, mapped_at)
SELECT
    q.id as question_id,
    (
        SELECT s.id
        FROM wp_ez_skills s
        WHERE s.certification_id = '$CERT_CODE'
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
  AND q.category_id = $CATEGORY_ID
  AND NOT EXISTS (
      SELECT 1 FROM wp_ez_question_skills qs WHERE qs.question_id = q.id
  );

SELECT CONCAT('✅ Mapped questions for ', '$CERT_CODE') as result;
SELECT COUNT(DISTINCT qs.question_id) as mapped_questions
FROM wp_ez_question_skills qs
JOIN wp_ez_skills s ON qs.skill_id = s.id
WHERE s.certification_id = '$CERT_CODE';
EOF
