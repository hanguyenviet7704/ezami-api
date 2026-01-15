-- ===================================================================
-- SCRIPT: Optimize and Consolidate Taxonomy Structure
-- ===================================================================
-- Purpose: Unify taxonomy structure across different content types
-- Warning: Run cleanup_duplicate_categories.sql FIRST!
--
-- How to run:
--   mysql -u root -p wordpress < optimize_taxonomy_structure.sql
-- ===================================================================

USE wordpress;

-- ===================================================================
-- Part 1: Analyze current taxonomy usage
-- ===================================================================

-- Check taxonomy distribution
SELECT
    tt.taxonomy,
    COUNT(*) as total_terms,
    COUNT(DISTINCT tr.object_id) as posts_using,
    SUM(tt.count) as total_assignments
FROM wp_term_taxonomy tt
LEFT JOIN wp_term_relationships tr ON tt.term_taxonomy_id = tr.term_taxonomy_id
GROUP BY tt.taxonomy
ORDER BY total_terms DESC;

-- ===================================================================
-- Part 2: Migrate LearnDash Quiz Categories to wp_term_taxonomy
-- ===================================================================

-- Check if migration is needed
SELECT
    'LearnDash Quiz Categories' as migration_target,
    COUNT(*) as current_count
FROM wp_term_taxonomy
WHERE taxonomy = 'ld_quiz_category';

-- Create standard quiz category taxonomy if not exists
-- (Uncomment to execute)
/*
INSERT IGNORE INTO wp_terms (name, slug, term_group)
SELECT DISTINCT
    q.category_name as name,
    LOWER(REPLACE(q.category_name, ' ', '-')) as slug,
    0 as term_group
FROM wp_learndash_pro_quiz_category q
WHERE q.category_name IS NOT NULL
  AND q.category_name != ''
  AND NOT EXISTS (
      SELECT 1 FROM wp_terms t WHERE t.name = q.category_name
  );

INSERT IGNORE INTO wp_term_taxonomy (term_id, taxonomy, description, parent, count)
SELECT
    t.term_id,
    'ld_quiz_category' as taxonomy,
    '' as description,
    0 as parent,
    0 as count
FROM wp_terms t
WHERE t.name IN (SELECT DISTINCT category_name FROM wp_learndash_pro_quiz_category)
  AND NOT EXISTS (
      SELECT 1
      FROM wp_term_taxonomy tt
      WHERE tt.term_id = t.term_id AND tt.taxonomy = 'ld_quiz_category'
  );
*/

-- ===================================================================
-- Part 3: Create unified hierarchy for certifications
-- ===================================================================

-- Suggested hierarchy:
-- 📁 Kiểm thử phần mềm (Software Testing)
--    └─ 📁 ISTQB
--       ├─ ISTQB Foundation v3.1
--       ├─ ISTQB Agile Tester
--       └─ ISTQB AI Testing
-- 📁 Phân tích nghiệp vụ (Business Analysis)
--    └─ 📁 BABOK
--       └─ BABOK Guide v3
-- 📁 Quản lý dự án (Project Management)
--    └─ 📁 Agile & Scrum
--       ├─ Scrum Guide
--       ├─ PSM I
--       └─ PSPO I

-- Show current parent-child relationships
SELECT
    p.name as parent_category,
    t.name as child_category,
    tt.count as usage_count
FROM wp_term_taxonomy tt
JOIN wp_terms t ON tt.term_id = t.term_id
LEFT JOIN wp_term_taxonomy ttp ON tt.parent = ttp.term_taxonomy_id
LEFT JOIN wp_terms p ON ttp.term_id = p.term_id
WHERE tt.taxonomy = 'category'
  AND tt.parent > 0
ORDER BY p.name, t.name;

-- ===================================================================
-- Part 4: Optimize ez_article_space_category
-- ===================================================================

-- Check space categories
SELECT * FROM ez_article_space_category;

-- Recommendation: Migrate to wp_fcom_terms for consistency
-- (Uncomment to execute)
/*
INSERT INTO wp_fcom_terms (parent_id, taxonomy_name, slug, title, description, settings)
SELECT
    NULL as parent_id,
    'article_category' as taxonomy_name,
    category_slug,
    category_name,
    '' as description,
    JSON_OBJECT(
        'language', language,
        'order', `order`,
        'enable', enable,
        'space_id', space_id
    ) as settings
FROM ez_article_space_category
WHERE NOT EXISTS (
    SELECT 1 FROM wp_fcom_terms ft
    WHERE ft.slug = ez_article_space_category.category_slug
);
*/

-- ===================================================================
-- Part 5: Add missing indexes for performance
-- ===================================================================

-- Check current indexes
SHOW INDEX FROM wp_term_taxonomy;
SHOW INDEX FROM wp_term_relationships;
SHOW INDEX FROM wp_fcom_terms;

-- Add composite indexes for faster queries
-- (Uncomment to execute)
/*
ALTER TABLE wp_term_taxonomy
    ADD INDEX idx_taxonomy_parent (taxonomy, parent),
    ADD INDEX idx_term_taxonomy (term_id, taxonomy);

ALTER TABLE wp_term_relationships
    ADD INDEX idx_object_taxonomy (object_id, term_taxonomy_id);

ALTER TABLE wp_fcom_terms
    ADD INDEX idx_taxonomy_slug (taxonomy_name, slug),
    ADD INDEX idx_parent_taxonomy (parent_id, taxonomy_name);
*/

-- ===================================================================
-- Part 6: Update term counts
-- ===================================================================

-- Recalculate all term counts
-- (Uncomment to execute)
/*
UPDATE wp_term_taxonomy tt
SET tt.count = (
    SELECT COUNT(DISTINCT tr.object_id)
    FROM wp_term_relationships tr
    WHERE tr.term_taxonomy_id = tt.term_taxonomy_id
);
*/

-- ===================================================================
-- VERIFICATION QUERIES
-- ===================================================================

-- Verify taxonomy structure
SELECT
    'WordPress Categories' as taxonomy_type,
    COUNT(*) as total
FROM wp_term_taxonomy
WHERE taxonomy = 'category'
UNION ALL
SELECT
    'Community Terms',
    COUNT(*)
FROM wp_fcom_terms
UNION ALL
SELECT
    'Quiz Categories',
    COUNT(*)
FROM wp_term_taxonomy
WHERE taxonomy = 'ld_quiz_category'
UNION ALL
SELECT
    'Space Categories',
    COUNT(*)
FROM ez_article_space_category;

-- Check for orphaned terms (terms without taxonomy)
SELECT
    t.term_id,
    t.name,
    t.slug
FROM wp_terms t
WHERE NOT EXISTS (
    SELECT 1 FROM wp_term_taxonomy tt WHERE tt.term_id = t.term_id
);

-- Check for orphaned relationships
SELECT COUNT(*) as orphaned_relationships
FROM wp_term_relationships tr
WHERE NOT EXISTS (
    SELECT 1 FROM wp_term_taxonomy tt WHERE tt.term_taxonomy_id = tr.term_taxonomy_id
);
