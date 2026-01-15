-- ==========================================
-- FIX EXPLANATION QA QUESTION MAPPING
-- ==========================================
-- This script maps existing explanation QA records to real questions
-- in wp_learndash_pro_quiz_question table.
--
-- Issue: All 5 explanation records have question_id = NULL
-- Impact: Cannot retrieve explanations by question ID
-- Fix: Map by matching question text
-- ==========================================

-- Check current state
SELECT
    id,
    question_id,
    LEFT(question_text, 80) as question_preview,
    rating,
    prompt_version
FROM wp_ez_explanation_qa
ORDER BY id;

-- ==========================================
-- Manual Mapping (Need to find matching questions)
-- ==========================================

-- Explanation #1: "The Product Owner can also be a Developer"
-- Need to find matching question in database
SELECT id, quiz_id, title, LEFT(question, 100) as question_preview
FROM wp_learndash_pro_quiz_question
WHERE question LIKE '%Product Owner%Developer%'
   OR title LIKE '%Product Owner%Developer%'
LIMIT 5;

-- Explanation #2: "Sprint Review meeting"
SELECT id, quiz_id, title, LEFT(question, 100) as question_preview
FROM wp_learndash_pro_quiz_question
WHERE question LIKE '%Sprint Review%'
   OR title LIKE '%Sprint Review%'
LIMIT 5;

-- ==========================================
-- Update Mappings (Run after identifying question IDs)
-- ==========================================

-- IMPORTANT: Replace <question_id> with actual IDs from above queries

-- UPDATE wp_ez_explanation_qa
-- SET question_id = <found_question_id>
-- WHERE id = 1;

-- UPDATE wp_ez_explanation_qa
-- SET question_id = <found_question_id>
-- WHERE id = 2;

-- UPDATE wp_ez_explanation_qa
-- SET question_id = <found_question_id>
-- WHERE id = 3;

-- UPDATE wp_ez_explanation_qa
-- SET question_id = <found_question_id>
-- WHERE id = 4;

-- UPDATE wp_ez_explanation_qa
-- SET question_id = <found_question_id>
-- WHERE id = 5;

-- ==========================================
-- Verification Query
-- ==========================================

-- Verify mappings are correct
SELECT
    e.id as explanation_id,
    e.question_id,
    e.question_text as explanation_question,
    q.id as quiz_question_id,
    q.title as quiz_title,
    LEFT(q.question, 100) as quiz_question,
    CASE
        WHEN e.question_id IS NOT NULL THEN '✅ Mapped'
        ELSE '❌ Not mapped'
    END as status
FROM wp_ez_explanation_qa e
LEFT JOIN wp_learndash_pro_quiz_question q ON e.question_id = q.id
ORDER BY e.id;

-- ==========================================
-- Test API After Fix
-- ==========================================

-- After running updates, test these queries:

-- 1. Should return explanation for specific question
-- SELECT * FROM wp_ez_explanation_qa WHERE question_id = <mapped_id>;

-- 2. Should work in API
-- curl GET /api/explanation-qa/question/<question_id>

-- ==========================================
-- NOTES
-- ==========================================
--
-- Why question_id is NULL:
-- - Sample data created for testing/QA purposes
-- - Not linked to actual quiz questions
-- - Used for admin tool development and testing
--
-- Next Steps:
-- 1. Identify matching questions in wp_learndash_pro_quiz_question
-- 2. Update question_id mappings
-- 3. Verify API endpoints work
-- 4. Generate more explanations for production use
--
-- Long-term Solution:
-- - Generate explanations directly linked to question IDs
-- - Bulk generate for all 18,502 questions
-- - Implement AI explanation service integration
--
-- ==========================================
