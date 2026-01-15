-- ===================================================================
-- SCRIPT: Cleanup Duplicate Categories from Polylang Plugin
-- ===================================================================
-- Purpose: Remove duplicate categories created by Polylang plugin
-- Warning: BACKUP DATABASE BEFORE RUNNING THIS SCRIPT!
--
-- How to run:
--   mysql -u root -p wordpress < cleanup_duplicate_categories.sql
-- ===================================================================

USE wordpress;

-- Step 1: Backup original data
CREATE TABLE IF NOT EXISTS wp_terms_backup_20251225 AS SELECT * FROM wp_terms;
CREATE TABLE IF NOT EXISTS wp_term_taxonomy_backup_20251225 AS SELECT * FROM wp_term_taxonomy;
CREATE TABLE IF NOT EXISTS wp_term_relationships_backup_20251225 AS SELECT * FROM wp_term_relationships;

-- Step 2: Create temporary table with canonical (original) term IDs
DROP TEMPORARY TABLE IF EXISTS canonical_terms;
CREATE TEMPORARY TABLE canonical_terms AS
SELECT
    MIN(t.term_id) as canonical_term_id,
    t.name,
    tt.taxonomy,
    GROUP_CONCAT(t.term_id ORDER BY t.term_id) as all_term_ids
FROM wp_terms t
JOIN wp_term_taxonomy tt ON t.term_id = tt.term_id
WHERE tt.taxonomy = 'category'
  AND t.slug LIKE '%pll_%'  -- Only duplicates with Polylang suffix
GROUP BY t.name, tt.taxonomy;

-- Step 3: Show what will be cleaned up (DRY RUN)
SELECT
    name,
    taxonomy,
    LENGTH(all_term_ids) - LENGTH(REPLACE(all_term_ids, ',', '')) + 1 as duplicate_count,
    canonical_term_id as will_keep,
    all_term_ids as will_delete_except_first
FROM canonical_terms
ORDER BY duplicate_count DESC;

-- Step 4: Update term_relationships to point to canonical terms
-- (Uncomment to execute)
/*
UPDATE wp_term_relationships tr
JOIN wp_term_taxonomy tt ON tr.term_taxonomy_id = tt.term_taxonomy_id
JOIN canonical_terms ct ON tt.term_id != ct.canonical_term_id
                        AND FIND_IN_SET(tt.term_id, ct.all_term_ids)
JOIN wp_term_taxonomy tt_canonical ON tt_canonical.term_id = ct.canonical_term_id
                                   AND tt_canonical.taxonomy = ct.taxonomy
SET tr.term_taxonomy_id = tt_canonical.term_taxonomy_id;
*/

-- Step 5: Delete duplicate term_taxonomy entries
-- (Uncomment to execute)
/*
DELETE tt FROM wp_term_taxonomy tt
JOIN canonical_terms ct ON tt.term_id != ct.canonical_term_id
                        AND FIND_IN_SET(tt.term_id, ct.all_term_ids)
WHERE tt.taxonomy = 'category';
*/

-- Step 6: Delete duplicate terms
-- (Uncomment to execute)
/*
DELETE t FROM wp_terms t
JOIN canonical_terms ct ON t.term_id != ct.canonical_term_id
                        AND FIND_IN_SET(t.term_id, ct.all_term_ids);
*/

-- Step 7: Clean up canonical term slugs (remove -pll_* suffix)
-- (Uncomment to execute)
/*
UPDATE wp_terms t
JOIN canonical_terms ct ON t.term_id = ct.canonical_term_id
SET t.slug = REGEXP_REPLACE(t.slug, '-pll_(vi|en)(-[0-9]+)?.*$', '')
WHERE t.slug LIKE '%pll_%';
*/

-- Step 8: Update term counts
-- (Uncomment to execute)
/*
UPDATE wp_term_taxonomy tt
SET tt.count = (
    SELECT COUNT(*)
    FROM wp_term_relationships tr
    WHERE tr.term_taxonomy_id = tt.term_taxonomy_id
);
*/

-- ===================================================================
-- VERIFICATION QUERIES (run after cleanup)
-- ===================================================================
/*
-- Check remaining duplicates
SELECT
    t.name,
    COUNT(*) as count,
    GROUP_CONCAT(t.term_id) as term_ids
FROM wp_terms t
JOIN wp_term_taxonomy tt ON t.term_id = tt.term_id
WHERE tt.taxonomy = 'category'
GROUP BY t.name
HAVING COUNT(*) > 1;

-- Check total categories
SELECT COUNT(*) as total_categories FROM wp_term_taxonomy WHERE taxonomy = 'category';

-- Check orphaned relationships (should be 0)
SELECT COUNT(*)
FROM wp_term_relationships tr
LEFT JOIN wp_term_taxonomy tt ON tr.term_taxonomy_id = tt.term_taxonomy_id
WHERE tt.term_taxonomy_id IS NULL;
*/

-- ===================================================================
-- ROLLBACK (if needed)
-- ===================================================================
/*
TRUNCATE wp_terms;
TRUNCATE wp_term_taxonomy;
TRUNCATE wp_term_relationships;

INSERT INTO wp_terms SELECT * FROM wp_terms_backup_20251225;
INSERT INTO wp_term_taxonomy SELECT * FROM wp_term_taxonomy_backup_20251225;
INSERT INTO wp_term_relationships SELECT * FROM wp_term_relationships_backup_20251225;
*/
