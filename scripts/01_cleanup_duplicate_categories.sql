-- Script 01: Cleanup duplicate categories
-- Keep only categories with ID <= 30 (original ones)
-- Update questions to use original category IDs

-- Step 1: Delete duplicate categories (ID > 30)
DELETE FROM wp_learndash_pro_quiz_category WHERE category_id > 30;

-- Step 2: Verify cleanup
SELECT 'Categories after cleanup:' as info;
SELECT category_id, category_name FROM wp_learndash_pro_quiz_category ORDER BY category_id;
