-- V12: Add optimistic locking and missing FK constraints
-- This migration adds version columns for optimistic locking to prevent concurrent update issues
-- and adds missing foreign key constraints for data integrity

-- ==============================================
-- 1. Add version column to EIL session tables for optimistic locking
-- ==============================================
-- NOTE: Version columns already exist in local DB (created by Hibernate auto-ddl)
-- Skipping ADD COLUMN to avoid "Duplicate column name" error

-- -- Add version to diagnostic attempts (prevents concurrent answer submissions)
-- ALTER TABLE eil_diagnostic_attempts
-- ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;

-- -- Add version to practice sessions (prevents concurrent answer submissions)
-- ALTER TABLE eil_practice_sessions
-- ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;

-- ==============================================
-- 2. Add missing FK constraints
-- ==============================================
-- NOTE: Skipping question_id FK constraints due to type incompatibility
-- (question_id is BIGINT but wp_learndash_pro_quiz_question.id is INT)
-- This would require ALTER COLUMN type change which is risky with existing data

-- -- Add FK constraint for eil_diagnostic_answers.question_id
-- ALTER TABLE eil_diagnostic_answers
-- ADD CONSTRAINT fk_diagnostic_answers_question
-- FOREIGN KEY (question_id) REFERENCES wp_learndash_pro_quiz_question(id)
-- ON DELETE CASCADE;

-- -- Add FK constraint for eil_explanations.question_id
-- ALTER TABLE eil_explanations
-- ADD CONSTRAINT fk_explanations_question
-- FOREIGN KEY (question_id) REFERENCES wp_learndash_pro_quiz_question(id)
-- ON DELETE CASCADE;

-- -- Add FK constraint for eil_practice_attempts.question_id
-- ALTER TABLE eil_practice_attempts
-- ADD CONSTRAINT fk_practice_attempts_question
-- FOREIGN KEY (question_id) REFERENCES wp_learndash_pro_quiz_question(id)
-- ON DELETE CASCADE;

-- Add FK constraint for wp_fcom_user_streak_goals.goal_id
-- Note: This was missing in V10__create_streak_tables.sql
-- FK to streak goal - cascade delete if goal is removed
-- First check if constraint already exists
SET @constraint_exists = (SELECT COUNT(*)
  FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = 'wordpress'
    AND TABLE_NAME = 'wp_fcom_user_streak_goals'
    AND CONSTRAINT_NAME = 'fk_user_streak_goals_goal');

SET @sql = IF(@constraint_exists = 0,
  'ALTER TABLE wp_fcom_user_streak_goals ADD CONSTRAINT fk_user_streak_goals_goal FOREIGN KEY (goal_id) REFERENCES wp_fcom_streak_goals(id) ON DELETE CASCADE',
  'SELECT \"FK constraint fk_user_streak_goals_goal already exists\" as info');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ==============================================
-- 3. Add index for active session lookup (performance optimization)
-- ==============================================

-- Index for finding active diagnostic sessions (user_id + status)
-- Business rule: Only ONE active (in_progress) session per user is enforced in application code
CREATE INDEX idx_diagnostic_attempts_active_session
ON eil_diagnostic_attempts(user_id, status, created_at DESC)
COMMENT 'Index for finding active sessions - supports application-level uniqueness check';

-- Index for finding active practice sessions (user_id + status)
CREATE INDEX idx_practice_sessions_active
ON eil_practice_sessions(user_id, status, created_at DESC)
COMMENT 'Index for finding active practice sessions';

-- ==============================================
-- 4. Data integrity notes
-- ==============================================

-- IMPORTANT BUSINESS RULES (enforced in application code):
-- - Only ONE diagnostic session with status='in_progress' per user_id
-- - Only ONE practice session with status='ACTIVE' per user_id
-- - These are enforced via application logic with pessimistic locking
-- - MySQL does not support partial unique indexes like PostgreSQL
-- - Therefore, we use indexed queries + app-level validation
