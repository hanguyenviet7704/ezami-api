-- =====================================================
-- Check Image URLs Before Deployment
-- Run this to verify current state
-- =====================================================

-- 1. Check Quiz Category Images
SELECT
    'Quiz Categories' as source,
    COUNT(*) as total,
    SUM(CASE WHEN image_uri LIKE '%admin.ezami.io%' THEN 1 ELSE 0 END) as admin_urls,
    SUM(CASE WHEN image_uri LIKE '%cdn.ezami.io%' THEN 1 ELSE 0 END) as cdn_urls,
    SUM(CASE WHEN image_uri LIKE '%api-v2.ezami.io%' THEN 1 ELSE 0 END) as api_urls,
    SUM(CASE WHEN image_uri LIKE '%asset.ezami.io%' THEN 1 ELSE 0 END) as asset_urls,
    SUM(CASE WHEN image_uri IS NULL THEN 1 ELSE 0 END) as null_urls
FROM ez_quiz_category;

-- 2. Sample Quiz Category Image URLs
SELECT id, code, title, image_uri
FROM ez_quiz_category
WHERE image_uri IS NOT NULL
ORDER BY id
LIMIT 10;

-- 3. Check WordPress Media (wp_posts)
SELECT
    'WordPress Media' as source,
    COUNT(*) as total,
    SUM(CASE WHEN guid LIKE '%admin.ezami.io%' THEN 1 ELSE 0 END) as admin_urls,
    SUM(CASE WHEN guid LIKE '%cdn.ezami.io%' THEN 1 ELSE 0 END) as cdn_urls
FROM wp_posts
WHERE post_type = 'attachment';

-- 4. Sample WordPress Media URLs
SELECT ID, post_title, guid
FROM wp_posts
WHERE post_type = 'attachment'
ORDER BY ID DESC
LIMIT 10;

-- 5. Check Media Archives (API uploads)
SELECT
    'Media Archives' as source,
    COUNT(*) as total,
    SUM(CASE WHEN file_url LIKE '%admin.ezami.io%' THEN 1 ELSE 0 END) as admin_urls,
    SUM(CASE WHEN file_url LIKE '%api-v2.ezami.io%' THEN 1 ELSE 0 END) as api_urls,
    SUM(CASE WHEN file_url LIKE '%asset.ezami.io%' THEN 1 ELSE 0 END) as asset_urls
FROM wp_fcom_media_archives;

-- 6. Sample Media Archive URLs
SELECT id, file_url, thumbnail_url
FROM wp_fcom_media_archives
ORDER BY id DESC
LIMIT 10;

-- =====================================================
-- Summary: Use this to understand current state
-- =====================================================
