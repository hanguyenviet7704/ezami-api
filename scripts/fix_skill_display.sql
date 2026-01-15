-- Fix Skill Display Issue - Map eil_skill_mastery to wp_ez_skills

-- Step 1: Create mapping table
CREATE TEMPORARY TABLE skill_id_mapping AS
SELECT
    es.id as old_skill_id,
    ws.id as new_skill_id,
    es.code,
    es.name
FROM eil_skills es
INNER JOIN wp_ez_skills ws ON (
    es.code = ws.code
    OR (es.name = ws.name AND es.category = ws.certification_id)
)
WHERE es.is_active = 1 AND ws.status = 'active';

-- Step 2: Show mapping
SELECT 'Skill ID mapping created:' as status;
SELECT COUNT(*) as mappings_found FROM skill_id_mapping;

-- Step 3: Update eil_skill_mastery to reference wp_ez_skills
UPDATE eil_skill_mastery m
INNER JOIN skill_id_mapping map ON m.skill_id = map.old_skill_id
SET m.skill_id = map.new_skill_id;

SELECT 'Updated mastery records' as status, ROW_COUNT() as count;

-- Step 4: Update eil_diagnostic_answers skill references
UPDATE eil_diagnostic_answers a
INNER JOIN skill_id_mapping map ON a.skill_id = map.old_skill_id
SET a.skill_id = map.new_skill_id;

SELECT 'Updated diagnostic answers' as status, ROW_COUNT() as count;

-- Step 5: Update eil_practice_attempts skill references
UPDATE eil_practice_attempts p
INNER JOIN skill_id_mapping map ON p.skill_id = map.old_skill_id
SET p.skill_id = map.new_skill_id;

SELECT 'Updated practice attempts' as status, ROW_COUNT() as count;

-- Cleanup
DROP TEMPORARY TABLE skill_id_mapping;

-- Verify
SELECT '=== VERIFICATION ===' as title;
SELECT 'Mastery skill IDs now reference wp_ez_skills' as status;
SELECT DISTINCT m.skill_id, s.code, s.name
FROM eil_skill_mastery m
JOIN wp_ez_skills s ON m.skill_id = s.id
WHERE s.status = 'active'
LIMIT 10;
