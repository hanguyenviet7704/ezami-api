-- Quiz pagination performance indexes (authoritative, MySQL 5.7 compatible)
-- This script is idempotent: it creates indexes only if they don't exist.
-- Target tables are WordPress/LearnDash ones used by quiz listing.

DELIMITER $$

DROP PROCEDURE IF EXISTS add_index_if_missing $$
CREATE PROCEDURE add_index_if_missing(
    IN in_schema VARCHAR(64),
    IN in_table  VARCHAR(64),
    IN in_index  VARCHAR(128),
    IN in_sql    TEXT
)
BEGIN
    DECLARE idx_count INT DEFAULT 0;
    SELECT COUNT(*) INTO idx_count
    FROM information_schema.statistics
    WHERE table_schema = in_schema
      AND table_name = in_table
      AND index_name = in_index;
    IF idx_count = 0 THEN
        SET @stmt = in_sql;
        PREPARE s FROM @stmt;
        EXECUTE s;
        DEALLOCATE PREPARE s;
    END IF;
END $$

DELIMITER ;

-- 1) wp_learndash_pro_quiz_master: index on name for ORDER BY
CALL add_index_if_missing(DATABASE(), 'wp_learndash_pro_quiz_master', 'idx_quiz_master_name',
    'ALTER TABLE wp_learndash_pro_quiz_master ADD INDEX idx_quiz_master_name (name(100))');

-- 2) wp_learndash_pro_quiz_master: index on id for joins
CALL add_index_if_missing(DATABASE(), 'wp_learndash_pro_quiz_master', 'idx_quiz_master_id',
    'ALTER TABLE wp_learndash_pro_quiz_master ADD INDEX idx_quiz_master_id (id)');

-- 3) wp_postmeta: composite (post_id, meta_key, meta_value) for quiz_pro_id lookups
CALL add_index_if_missing(DATABASE(), 'wp_postmeta', 'idx_postmeta_post_key_value',
    'ALTER TABLE wp_postmeta ADD INDEX idx_postmeta_post_key_value (post_id, meta_key(50), meta_value(50))');

-- 4) wp_learndash_user_activity: composite (user_id, post_id) for user activity lookups
CALL add_index_if_missing(DATABASE(), 'wp_learndash_user_activity', 'idx_user_activity_user_post',
    'ALTER TABLE wp_learndash_user_activity ADD INDEX idx_user_activity_user_post (user_id, post_id)');

-- 5) wp_learndash_user_activity_meta: (activity_id) for meta lookups
CALL add_index_if_missing(DATABASE(), 'wp_learndash_user_activity_meta', 'idx_user_activity_meta_activity',
    'ALTER TABLE wp_learndash_user_activity_meta ADD INDEX idx_user_activity_meta_activity (activity_id)');

-- 6) wp_learndash_pro_quiz_statistic: (statistic_ref_id) for statistics lookups
CALL add_index_if_missing(DATABASE(), 'wp_learndash_pro_quiz_statistic', 'idx_quiz_statistic_ref',
    'ALTER TABLE wp_learndash_pro_quiz_statistic ADD INDEX idx_quiz_statistic_ref (statistic_ref_id)');

-- 7) wp_learndash_pro_quiz_question: (quiz_id) for question lookups
CALL add_index_if_missing(DATABASE(), 'wp_learndash_pro_quiz_question', 'idx_quiz_question_quiz',
    'ALTER TABLE wp_learndash_pro_quiz_question ADD INDEX idx_quiz_question_quiz (quiz_id)');

-- 8) wp_learndash_pro_quiz_question: (id) for IN queries
CALL add_index_if_missing(DATABASE(), 'wp_learndash_pro_quiz_question', 'idx_quiz_question_id',
    'ALTER TABLE wp_learndash_pro_quiz_question ADD INDEX idx_quiz_question_id (id)');

-- Optional: posts composite index (status,type,date,ID) often fails on legacy dumps due to defaults.
-- Uncomment only if your wp_posts schema is clean and compatible.
-- CALL add_index_if_missing(DATABASE(), 'wp_posts', 'idx_posts_status_type_date_id',
--     'ALTER TABLE wp_posts ADD INDEX idx_posts_status_type_date_id (post_status, post_type, post_date, ID)');

-- Cleanup helper
DROP PROCEDURE IF EXISTS add_index_if_missing;

-- Verification (uncomment to inspect)
-- SHOW INDEX FROM wp_learndash_pro_quiz_master;
-- SHOW INDEX FROM wp_postmeta;
-- SHOW INDEX FROM wp_learndash_user_activity;
-- SHOW INDEX FROM wp_learndash_user_activity_meta;
-- SHOW INDEX FROM wp_learndash_pro_quiz_statistic;
-- SHOW INDEX FROM wp_learndash_pro_quiz_question;
-- Performance optimization indexes for Quiz pagination
-- Apply these indexes to improve query performance for quiz listing

-- 1. Composite index for wp_posts table (status, type, date, ID)
-- This covers the main filtering conditions in the quiz query
ALTER TABLE wp_posts 
ADD INDEX idx_posts_status_type_date_id (post_status, post_type, post_date, ID);

-- 2. Index for wp_postmeta table (meta_key, post_id)
-- This is already present but ensure it's optimized
-- ALTER TABLE wp_postmeta ADD INDEX idx_postmeta_key_post (meta_key(191), post_id);

-- 3. Index for wp_learndash_pro_quiz_master table (name for sorting)
-- This helps with ORDER BY name queries
ALTER TABLE wp_learndash_pro_quiz_master 
ADD INDEX idx_quiz_master_name (name(100));

-- 4. Index for wp_learndash_pro_quiz_master table (id for joins)
-- This helps with the CAST operation in the query
ALTER TABLE wp_learndash_pro_quiz_master 
ADD INDEX idx_quiz_master_id (id);

-- 5. Composite index for wp_postmeta (post_id, meta_key, meta_value)
-- This helps with the quiz_pro_id lookup
ALTER TABLE wp_postmeta 
ADD INDEX idx_postmeta_post_key_value (post_id, meta_key(50), meta_value(50));

-- 6. Index for user activity queries (user_id, post_id)
-- This helps with user activity lookups
ALTER TABLE wp_learndash_user_activity 
ADD INDEX idx_user_activity_user_post (user_id, post_id);

-- 7. Index for user activity meta queries (activity_id)
-- This helps with activity meta lookups
ALTER TABLE wp_learndash_user_activity_meta 
ADD INDEX idx_user_activity_meta_activity (activity_id);

-- 8. Index for quiz statistics queries (statistic_ref_id)
-- This helps with quiz statistics lookups
ALTER TABLE wp_learndash_pro_quiz_statistic 
ADD INDEX idx_quiz_statistic_ref (statistic_ref_id);

-- 9. Index for quiz questions queries (quiz_id)
-- This helps with question lookups
ALTER TABLE wp_learndash_pro_quiz_question 
ADD INDEX idx_quiz_question_quiz (quiz_id);

-- 10. Index for quiz questions by ID (for IN queries)
-- This helps with question ID lookups
ALTER TABLE wp_learndash_pro_quiz_question 
ADD INDEX idx_quiz_question_id (id);

-- Performance monitoring queries
-- Use these to check query performance before and after index creation

-- Check current indexes
-- SHOW INDEX FROM wp_posts;
-- SHOW INDEX FROM wp_postmeta;
-- SHOW INDEX FROM wp_learndash_pro_quiz_master;

-- Analyze query performance
-- EXPLAIN SELECT qm.id AS id, p.name AS slug, qm.timeLimit AS timeLimit, qm.name AS name, p.id AS postId,
--        p.content AS postContent, p.title AS postTitle
-- FROM wp_learndash_pro_quiz_master qm, wp_postmeta pm, wp_posts p
-- WHERE qm.id = CAST(pm.metaValue AS integer)
-- AND pm.postId = p.id
-- AND pm.metaKey = 'quiz_pro_id'
-- AND p.post_status IN ('publish', 'private')
-- AND p.post_type = 'sfwd-quiz'
-- AND LOWER(qm.name) LIKE 'category%'
-- ORDER BY qm.name ASC
-- LIMIT 20 OFFSET 0;
