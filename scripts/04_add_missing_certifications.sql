-- Script 04: Add missing certifications and their skills

-- Add ISTQB_AI certification if not exists
INSERT IGNORE INTO wp_ez_certifications
(certification_id, vendor, full_name, short_name, category, difficulty_level, is_active, sort_order)
VALUES
('ISTQB_AI', 'ISTQB', 'ISTQB AI Testing Certification', 'AI Testing', 'testing', 'intermediate', 1, 100);

-- Add skills for ISTQB_AI
INSERT IGNORE INTO wp_ez_skills (parent_id, certification_id, code, name, description, level, sort_order, status, version)
VALUES
(NULL, 'ISTQB_AI', 'ISTQB_AI_ROOT', 'ISTQB AI Testing', 'Root skill for ISTQB AI Testing', 0, 0, 'active', 1),
(NULL, 'ISTQB_AI', 'AI_FUNDAMENTALS', 'AI Fundamentals', 'Understanding of AI/ML basics', 0, 1, 'active', 1),
(NULL, 'ISTQB_AI', 'AI_TESTING_APPROACHES', 'AI Testing Approaches', 'Testing approaches for AI systems', 0, 2, 'active', 1),
(NULL, 'ISTQB_AI', 'AI_QUALITY', 'AI Quality Characteristics', 'Quality characteristics specific to AI', 0, 3, 'active', 1);

-- Get the parent IDs for sub-skills
SET @ai_fund_id = (SELECT id FROM wp_ez_skills WHERE certification_id = 'ISTQB_AI' AND code = 'AI_FUNDAMENTALS');
SET @ai_test_id = (SELECT id FROM wp_ez_skills WHERE certification_id = 'ISTQB_AI' AND code = 'AI_TESTING_APPROACHES');
SET @ai_qual_id = (SELECT id FROM wp_ez_skills WHERE certification_id = 'ISTQB_AI' AND code = 'AI_QUALITY');

-- Add sub-skills
INSERT IGNORE INTO wp_ez_skills (parent_id, certification_id, code, name, level, sort_order, status, version) VALUES
(@ai_fund_id, 'ISTQB_AI', 'AI_ML_CONCEPTS', 'Machine Learning Concepts', 1, 1, 'active', 1),
(@ai_fund_id, 'ISTQB_AI', 'AI_NEURAL_NETWORKS', 'Neural Networks', 1, 2, 'active', 1),
(@ai_fund_id, 'ISTQB_AI', 'AI_DATA_QUALITY', 'Data Quality for AI', 1, 3, 'active', 1),
(@ai_test_id, 'ISTQB_AI', 'AI_TEST_DATA', 'Test Data for AI', 1, 1, 'active', 1),
(@ai_test_id, 'ISTQB_AI', 'AI_MODEL_TESTING', 'Model Testing', 1, 2, 'active', 1),
(@ai_test_id, 'ISTQB_AI', 'AI_PERFORMANCE_TESTING', 'AI Performance Testing', 1, 3, 'active', 1),
(@ai_qual_id, 'ISTQB_AI', 'AI_EXPLAINABILITY', 'AI Explainability', 1, 1, 'active', 1),
(@ai_qual_id, 'ISTQB_AI', 'AI_FAIRNESS', 'AI Fairness & Bias', 1, 2, 'active', 1),
(@ai_qual_id, 'ISTQB_AI', 'AI_ROBUSTNESS', 'AI Robustness', 1, 3, 'active', 1);

-- Add skills for ISTQB_AGILE if missing
INSERT IGNORE INTO wp_ez_skills (parent_id, certification_id, code, name, description, level, sort_order, status, version)
VALUES
(NULL, 'ISTQB_AGILE', 'ISTQB_AGILE_ROOT', 'ISTQB Agile Tester', 'Root skill for ISTQB Agile Tester', 0, 0, 'active', 1),
(NULL, 'ISTQB_AGILE', 'AGILE_FUNDAMENTALS', 'Agile Fundamentals', 'Agile development fundamentals', 0, 1, 'active', 1),
(NULL, 'ISTQB_AGILE', 'AGILE_TESTING', 'Agile Testing', 'Testing in agile projects', 0, 2, 'active', 1),
(NULL, 'ISTQB_AGILE', 'AGILE_METHODS', 'Agile Methods', 'Agile testing methods and techniques', 0, 3, 'active', 1);

SET @agile_fund_id = (SELECT id FROM wp_ez_skills WHERE certification_id = 'ISTQB_AGILE' AND code = 'AGILE_FUNDAMENTALS');
SET @agile_test_id = (SELECT id FROM wp_ez_skills WHERE certification_id = 'ISTQB_AGILE' AND code = 'AGILE_TESTING');
SET @agile_meth_id = (SELECT id FROM wp_ez_skills WHERE certification_id = 'ISTQB_AGILE' AND code = 'AGILE_METHODS');

INSERT IGNORE INTO wp_ez_skills (parent_id, certification_id, code, name, level, sort_order, status, version) VALUES
(@agile_fund_id, 'ISTQB_AGILE', 'AGILE_SCRUM', 'Scrum Framework', 1, 1, 'active', 1),
(@agile_fund_id, 'ISTQB_AGILE', 'AGILE_KANBAN', 'Kanban Method', 1, 2, 'active', 1),
(@agile_fund_id, 'ISTQB_AGILE', 'AGILE_XP', 'Extreme Programming', 1, 3, 'active', 1),
(@agile_test_id, 'ISTQB_AGILE', 'AGILE_TDD', 'Test-Driven Development', 1, 1, 'active', 1),
(@agile_test_id, 'ISTQB_AGILE', 'AGILE_BDD', 'Behavior-Driven Development', 1, 2, 'active', 1),
(@agile_test_id, 'ISTQB_AGILE', 'AGILE_ATDD', 'Acceptance TDD', 1, 3, 'active', 1),
(@agile_meth_id, 'ISTQB_AGILE', 'AGILE_AUTOMATION', 'Test Automation in Agile', 1, 1, 'active', 1),
(@agile_meth_id, 'ISTQB_AGILE', 'AGILE_CI_CD', 'CI/CD Testing', 1, 2, 'active', 1),
(@agile_meth_id, 'ISTQB_AGILE', 'AGILE_EXPLORATORY', 'Exploratory Testing', 1, 3, 'active', 1);

-- Verify
SELECT 'New certifications:' as info;
SELECT certification_id, full_name FROM wp_ez_certifications WHERE certification_id IN ('ISTQB_AI', 'ISTQB_AGILE');

SELECT 'Skills count:' as info;
SELECT certification_id, COUNT(*) as skill_count
FROM wp_ez_skills
WHERE certification_id IN ('ISTQB_AI', 'ISTQB_AGILE') AND status = 'active'
GROUP BY certification_id;
