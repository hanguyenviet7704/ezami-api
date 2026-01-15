-- Script 07: Improve generic explanations with context from category/certification

-- PSM_I explanations
UPDATE wp_learndash_pro_quiz_question
SET
    correct_msg = CONCAT('✓ Correct!\n\nThis answer aligns with Scrum framework principles as defined in the Scrum Guide. ',
                         'PSM I certification tests your understanding of Scrum roles, events, artifacts, and their interactions. ',
                         '\n\nRefer to the official Scrum Guide at scrum.org for detailed explanation.'),
    incorrect_msg = CONCAT('✗ Incorrect\n\nThis question tests Scrum framework knowledge required for PSM I certification. ',
                           'Review the Scrum Guide, particularly the sections on:\n',
                           '• Scrum Theory and Values\n',
                           '• Scrum Team roles\n',
                           '• Scrum Events and Artifacts\n\n',
                           'Practice similar questions to strengthen your understanding.')
WHERE online = 1
  AND category_id = 1
  AND (correct_msg LIKE '%For detailed explanation%' OR correct_msg LIKE '%[Explanation needed%');

-- PSPO_I explanations
UPDATE wp_learndash_pro_quiz_question
SET
    correct_msg = CONCAT('✓ Correct!\n\nThis answer reflects Product Owner responsibilities as outlined in the Scrum Guide. ',
                         'PSPO I certification focuses on product value maximization, backlog management, and stakeholder engagement. ',
                         '\n\nSee scrum.org Product Owner resources for more details.'),
    incorrect_msg = CONCAT('✗ Incorrect\n\nAs a Product Owner, understanding this concept is crucial. ',
                           'Review the Scrum Guide sections on:\n',
                           '• Product Owner accountabilities\n',
                           '• Product Backlog management\n',
                           '• Stakeholder collaboration\n\n',
                           'Focus on maximizing product value.')
WHERE online = 1
  AND category_id IN (11, 13)
  AND (correct_msg LIKE '%For detailed explanation%' OR correct_msg LIKE '%[Explanation needed%');

-- CBAP/CCBA explanations
UPDATE wp_learndash_pro_quiz_question
SET
    correct_msg = CONCAT('✓ Correct!\n\nThis question is based on BABOK v3 (Business Analysis Body of Knowledge). ',
                         CASE
                             WHEN category_id = 26 THEN 'CBAP certification tests advanced business analysis competencies. '
                             WHEN category_id = 14 THEN 'CCBA certification tests intermediate business analysis skills. '
                             ELSE ''
                         END,
                         '\n\nRefer to the relevant BABOK knowledge area for comprehensive coverage.'),
    incorrect_msg = CONCAT('✗ Incorrect\n\nThis topic is covered in BABOK v3. ',
                           'Review the knowledge area and ensure you understand:\n',
                           '• Key concepts and definitions\n',
                           '• Inputs and outputs\n',
                           '• Stakeholder considerations\n',
                           '• Techniques applicable to this area')
WHERE online = 1
  AND category_id IN (14, 26)
  AND (correct_msg LIKE '%For detailed explanation%' OR correct_msg LIKE '%[Explanation needed%');

-- ISTQB explanations
UPDATE wp_learndash_pro_quiz_question
SET
    correct_msg = CONCAT('✓ Correct!\n\nThis answer is based on ISTQB Foundation Level syllabus. ',
                         'Understanding testing principles, processes, and techniques is essential for CTFL certification. ',
                         '\n\nReview the ISTQB glossary and syllabus for terminology and concepts.'),
    incorrect_msg = CONCAT('✗ Incorrect\n\nISTQB questions require precise understanding of testing terminology. ',
                           'Review:\n',
                           '• Test process fundamentals\n',
                           '• Testing throughout SDLC\n',
                           '• Static and dynamic techniques\n',
                           '• Test management principles')
WHERE online = 1
  AND category_id IN (2, 3, 4, 16, 17, 18, 25)
  AND (correct_msg LIKE '%For detailed explanation%' OR correct_msg LIKE '%[Explanation needed%');

-- Cloud/DevOps certifications (AWS, Azure, K8s, Docker)
UPDATE wp_learndash_pro_quiz_question
SET
    correct_msg = CONCAT('✓ Correct!\n\n',
                         CASE category_id
                             WHEN 30 THEN 'AWS Solutions Architect: This demonstrates understanding of designing resilient, secure, and cost-effective architectures on AWS. '
                             WHEN 31 THEN 'AWS Developer: This shows knowledge of developing and deploying cloud applications using AWS services. '
                             WHEN 33 THEN 'Azure Administrator: This reflects proper Azure resource management and administration practices. '
                             WHEN 34 THEN 'Kubernetes CKA: This demonstrates cluster administration and troubleshooting skills. '
                             WHEN 36 THEN 'Docker DCA: This shows containerization best practices and Docker operations knowledge. '
                         END,
                         '\n\nRefer to official vendor documentation for in-depth coverage.'),
    incorrect_msg = CONCAT('✗ Incorrect\n\n',
                           CASE category_id
                               WHEN 30 THEN 'Review AWS Well-Architected Framework pillars and service-specific best practices.'
                               WHEN 31 THEN 'Study AWS service SDKs, deployment models, and development best practices.'
                               WHEN 33 THEN 'Review Azure governance, networking, and resource management concepts.'
                               WHEN 34 THEN 'Practice kubectl commands and study Kubernetes objects and their interactions.'
                               WHEN 36 THEN 'Review Docker CLI commands, Dockerfile best practices, and container orchestration.'
                           END)
WHERE online = 1
  AND category_id IN (30, 31, 33, 34, 36)
  AND (correct_msg LIKE '%For detailed explanation%' OR correct_msg LIKE '%[Explanation needed%');

-- Verify results
SELECT 'Updated questions by category:' as info;
SELECT
  c.category_name,
  COUNT(CASE WHEN q.correct_msg LIKE '%✓ Correct!%' THEN 1 END) as standardized,
  COUNT(CASE WHEN LENGTH(q.correct_msg) >= 100 THEN 1 END) as detailed_enough
FROM wp_learndash_pro_quiz_question q
JOIN wp_learndash_pro_quiz_category c ON q.category_id = c.category_id
WHERE q.online = 1
GROUP BY c.category_name
HAVING standardized > 0
ORDER BY standardized DESC
LIMIT 20;
