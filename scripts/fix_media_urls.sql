-- =====================================================
-- Fix Media URLs: admin.ezami.io → cdn.ezami.io
-- Run this on production database after deployment
-- =====================================================

-- Backup before making changes
-- CREATE TABLE ez_quiz_category_backup AS SELECT * FROM ez_quiz_category;

-- 1. Update Quiz Category Images
-- Transform: admin.ezami.io → cdn.ezami.io
UPDATE ez_quiz_category
SET image_uri = REPLACE(image_uri, 'admin.ezami.io', 'cdn.ezami.io')
WHERE image_uri LIKE '%admin.ezami.io%';

-- Transform: http → https
UPDATE ez_quiz_category
SET image_uri = REPLACE(image_uri, 'http://', 'https://')
WHERE image_uri LIKE 'http://%';

-- Transform: api-v2.ezami.io → cdn.ezami.io (if any from old uploads)
UPDATE ez_quiz_category
SET image_uri = REPLACE(image_uri, 'api-v2.ezami.io', 'cdn.ezami.io')
WHERE image_uri LIKE '%api-v2.ezami.io%';

-- 2. Check results
SELECT id, code, title, image_uri
FROM ez_quiz_category
WHERE image_uri IS NOT NULL
LIMIT 10;

-- 3. Update WordPress Post Images (if any)
UPDATE wp_postmeta
SET meta_value = REPLACE(meta_value, 'admin.ezami.io', 'cdn.ezami.io')
WHERE meta_key IN ('_thumbnail_id', 'featured_image', 'image_url')
  AND meta_value LIKE '%admin.ezami.io%';

-- 4. Update WordPress Options (site URL)
UPDATE wp_options
SET option_value = REPLACE(option_value, 'admin.ezami.io', 'ezami.io')
WHERE option_name IN ('siteurl', 'home')
  AND option_value LIKE '%admin.ezami.io%';

-- 5. Update any media archive URLs
UPDATE wp_fcom_media_archives
SET file_url = REPLACE(file_url, 'admin.ezami.io', 'cdn.ezami.io')
WHERE file_url LIKE '%admin.ezami.io%';

UPDATE wp_fcom_media_archives
SET thumbnail_url = REPLACE(thumbnail_url, 'admin.ezami.io', 'cdn.ezami.io')
WHERE thumbnail_url LIKE '%admin.ezami.io%';

-- 6. Verify changes
SELECT 'Quiz Categories' as table_name, COUNT(*) as updated_count
FROM ez_quiz_category
WHERE image_uri LIKE '%cdn.ezami.io%'

UNION ALL

SELECT 'Post Meta' as table_name, COUNT(*) as updated_count
FROM wp_postmeta
WHERE meta_value LIKE '%cdn.ezami.io%'

UNION ALL

SELECT 'Media Archives' as table_name, COUNT(*) as updated_count
FROM wp_fcom_media_archives
WHERE file_url LIKE '%cdn.ezami.io%' OR thumbnail_url LIKE '%cdn.ezami.io%';

-- =====================================================
-- Summary
-- =====================================================
-- This script replaces all admin.ezami.io media URLs with cdn.ezami.io
-- Safe to run multiple times (idempotent)
-- Affects: Quiz categories, WordPress posts, media archives
