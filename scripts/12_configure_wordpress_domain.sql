-- Script 12: Configure WordPress for admin.ezami.io domain

-- Update WordPress URLs for admin domain
UPDATE wp_options SET option_value = 'https://admin.ezami.io' WHERE option_name = 'siteurl';
UPDATE wp_options SET option_value = 'https://admin.ezami.io' WHERE option_name = 'home';

-- Update site name
UPDATE wp_options SET option_value = 'Ezami Admin Panel' WHERE option_name = 'blogname';

-- Ensure admin email is correct
UPDATE wp_options SET option_value = 'support@ezami.io' WHERE option_name = 'admin_email';

-- Update other domain-related options
UPDATE wp_options SET option_value = 'https://admin.ezami.io'
WHERE option_name IN ('wp_page_for_privacy_policy', 'permalink_structure')
  AND option_value LIKE '%localhost%';

-- Verify changes
SELECT '=== WordPress Configuration After Update ===' as title;

SELECT option_name, option_value
FROM wp_options
WHERE option_name IN ('siteurl', 'home', 'admin_email', 'blogname', 'blogdescription')
ORDER BY option_name;

-- Check for any remaining localhost references
SELECT '=== Remaining localhost references ===' as check_type;
SELECT option_name, SUBSTRING(option_value, 1, 100) as value_preview
FROM wp_options
WHERE option_value LIKE '%localhost%'
  AND option_name NOT LIKE '%transient%'
LIMIT 20;
