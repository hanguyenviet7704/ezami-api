-- V13: Add version columns for optimistic locking (JPA @Version support)
-- Fix for: java.sql.SQLSyntaxErrorException: Unknown column 'version' in 'field list'
--
-- This migration adds missing version columns that are required by JPA entities
-- with @Version annotation for optimistic locking to prevent concurrent update issues.

-- ==============================================
-- 1. Add version column to eil_practice_sessions
-- ==============================================

-- Check if version column exists, add if missing
SET @column_exists_practice = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'wordpress'
    AND TABLE_NAME = 'eil_practice_sessions'
    AND COLUMN_NAME = 'version'
);

SET @sql_practice = IF(
    @column_exists_practice = 0,
    'ALTER TABLE eil_practice_sessions ADD COLUMN version BIGINT DEFAULT 0 NOT NULL COMMENT ''Optimistic locking version for JPA @Version''',
    'SELECT ''Column version already exists in eil_practice_sessions'' as info'
);

PREPARE stmt FROM @sql_practice;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ==============================================
-- 2. Add version column to eil_diagnostic_attempts
-- ==============================================

-- Check if version column exists, add if missing
SET @column_exists_diagnostic = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'wordpress'
    AND TABLE_NAME = 'eil_diagnostic_attempts'
    AND COLUMN_NAME = 'version'
);

SET @sql_diagnostic = IF(
    @column_exists_diagnostic = 0,
    'ALTER TABLE eil_diagnostic_attempts ADD COLUMN version BIGINT DEFAULT 0 NOT NULL COMMENT ''Optimistic locking version for JPA @Version''',
    'SELECT ''Column version already exists in eil_diagnostic_attempts'' as info'
);

PREPARE stmt FROM @sql_diagnostic;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ==============================================
-- Notes:
-- ==============================================
-- - Version columns are used by JPA for optimistic locking
-- - Each time a record is updated, the version is automatically incremented
-- - If two transactions try to update the same record, the second will fail
-- - This prevents lost updates and race conditions
-- - Default value 0 ensures existing records can be updated without issues
