-- ==========================================
-- ADD NEW CERTIFICATIONS TO EZAMI SYSTEM
-- ==========================================
-- This script adds 3 new certifications requested by frontend team:
-- 1. ISTQB_ADV_TM - ISTQB Advanced Test Manager
-- 2. ISTQB_ADV_TTA - ISTQB Advanced Technical Test Analyst
-- 3. SCRUM_PSPO_II - Professional Scrum Product Owner II
--
-- Table: wp_ez_certifications
-- Current certifications: 36
-- New certifications: 3
-- Total after: 39
-- ==========================================

-- Check current state
SELECT
    COUNT(*) as current_count,
    MAX(sort_order) as max_sort_order
FROM wp_ez_certifications;

-- ==========================================
-- 1. ISTQB Advanced Test Manager
-- ==========================================

INSERT INTO wp_ez_certifications (
    certification_id,
    full_name,
    short_name,
    acronym,
    category,
    vendor,
    exam_code,
    difficulty_level,
    is_active,
    sort_order,
    created_at,
    updated_at
) VALUES (
    'ISTQB_ADV_TM',
    'ISTQB Advanced Test Manager',
    'ISTQB Quản Lý Kiểm Thử Nâng Cao',
    'ISTQB-ATM',
    'testing',
    'ISTQB',
    'CTAL-TM',
    'advanced',  -- Advanced level
    1,  -- Active
    101,
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE
    full_name = VALUES(full_name),
    short_name = VALUES(short_name),
    updated_at = NOW();

-- ==========================================
-- 2. ISTQB Advanced Technical Test Analyst
-- ==========================================

INSERT INTO wp_ez_certifications (
    certification_id,
    full_name,
    short_name,
    acronym,
    category,
    vendor,
    exam_code,
    difficulty_level,
    is_active,
    sort_order,
    created_at,
    updated_at
) VALUES (
    'ISTQB_ADV_TTA',
    'ISTQB Advanced Technical Test Analyst',
    'ISTQB Chuyên Viên Phân Tích Kiểm Thử Kỹ Thuật Nâng Cao',
    'ISTQB-ATTA',
    'testing',
    'ISTQB',
    'CTAL-TTA',
    'advanced',  -- Advanced level
    1,  -- Active
    102,
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE
    full_name = VALUES(full_name),
    short_name = VALUES(short_name),
    updated_at = NOW();

-- ==========================================
-- 3. Professional Scrum Product Owner II
-- ==========================================

INSERT INTO wp_ez_certifications (
    certification_id,
    full_name,
    short_name,
    acronym,
    category,
    vendor,
    exam_code,
    difficulty_level,
    is_active,
    sort_order,
    created_at,
    updated_at
) VALUES (
    'SCRUM_PSPO_II',
    'Professional Scrum Product Owner II',
    'Chứng Chỉ Product Owner Chuyên Nghiệp Cấp II',
    'PSPO-II',
    'agile',
    'Scrum.org',
    'PSPO-II',
    'advanced',  -- Advanced level
    1,  -- Active
    103,
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE
    full_name = VALUES(full_name),
    short_name = VALUES(short_name),
    updated_at = NOW();

-- ==========================================
-- Verification Queries
-- ==========================================

-- Check if insertions succeeded
SELECT
    'New certifications added' as description,
    COUNT(*) as count
FROM wp_ez_certifications
WHERE certification_id IN ('ISTQB_ADV_TM', 'ISTQB_ADV_TTA', 'SCRUM_PSPO_II');

-- Display new certifications details
SELECT
    certification_id,
    full_name,
    short_name,
    category,
    vendor,
    difficulty_level,
    is_active,
    sort_order
FROM wp_ez_certifications
WHERE certification_id IN ('ISTQB_ADV_TM', 'ISTQB_ADV_TTA', 'SCRUM_PSPO_II')
ORDER BY sort_order;

-- Check total count
SELECT
    COUNT(*) as total_certifications,
    SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) as active_certifications
FROM wp_ez_certifications;

-- List all certifications with skills and questions count
SELECT
    c.certification_id,
    c.full_name,
    c.category,
    c.is_active,
    COUNT(DISTINCT s.id) as skill_count,
    COUNT(DISTINCT qs.question_id) as question_count
FROM wp_ez_certifications c
LEFT JOIN wp_ez_skills s ON c.certification_id = s.certification_id AND s.status = 'active'
LEFT JOIN wp_ez_question_skills qs ON s.id = qs.skill_id
WHERE c.certification_id IN ('ISTQB_ADV_TM', 'ISTQB_ADV_TTA', 'SCRUM_PSPO_II')
GROUP BY c.certification_id, c.full_name, c.category, c.is_active
ORDER BY c.sort_order;

-- ==========================================
-- IMPORTANT NOTES
-- ==========================================
--
-- 1. NEXT STEPS REQUIRED:
--    - Add skills for these certifications to wp_ez_skills table
--    - Map questions to skills in wp_ez_question_skills table
--    - Add Vietnamese translations to wp_fcom_translations table
--
-- 2. SKILL REQUIREMENTS:
--    Each certification should have:
--    - Minimum 50 skills for good coverage
--    - At least 100 questions mapped
--    - Hierarchical skill structure (parent-child relationships)
--
-- 3. BACKEND API VERIFICATION:
--    ✅ No code changes needed - APIs are dynamic
--    ✅ GET /api/certifications - will auto-include new certifications
--    ✅ GET /api/certifications/{certificationId} - will work
--    ✅ POST /api/eil/diagnostic/start - will accept new certification codes
--    ✅ Cache will auto-refresh on next request
--
-- 4. FRONTEND IMPACT:
--    - Certification dropdown will show 3 new options
--    - Diagnostic flow will support new certifications
--    - Results page will display skills for new certifications
--
-- 5. DATA PREPARATION CHECKLIST:
--    [ ] Create skill taxonomy for ISTQB_ADV_TM (50-100 skills)
--    [ ] Create skill taxonomy for ISTQB_ADV_TTA (50-100 skills)
--    [ ] Create skill taxonomy for SCRUM_PSPO_II (30-50 skills)
--    [ ] Import questions from exam banks
--    [ ] Map questions to skills
--    [ ] Add Vietnamese translations for skill names
--    [ ] Test diagnostic flow end-to-end
--
-- ==========================================
