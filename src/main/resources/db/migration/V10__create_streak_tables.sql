-- Streak Feature Migration
-- Creates tables for streak tracking, goals, and activities

-- User Streaks Table
CREATE TABLE IF NOT EXISTS wp_fcom_user_streaks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE COMMENT 'References wp_users.ID',

    current_streak INT DEFAULT 0 COMMENT 'Current consecutive days',
    longest_streak INT DEFAULT 0 COMMENT 'Historical best streak',
    last_activity_date DATE COMMENT 'Last activity date (YYYY-MM-DD)',
    streak_start_date DATE COMMENT 'When current streak started',

    freeze_count INT DEFAULT 0 COMMENT 'Available freeze items',
    freeze_used_count INT DEFAULT 0 COMMENT 'Total freezes used ever',
    last_freeze_earned_at TIMESTAMP NULL COMMENT 'Last time earned a freeze',

    total_days_active INT DEFAULT 0 COMMENT 'Total lifetime active days',
    total_goals_completed INT DEFAULT 0 COMMENT 'Total goals claimed',
    total_milestone_points INT DEFAULT 0 COMMENT 'Points from milestones',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_user_streak_user (user_id),
    INDEX idx_user_streak_current (current_streak DESC),
    INDEX idx_user_streak_longest (longest_streak DESC),
    INDEX idx_user_streak_last_activity (last_activity_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Streak Goals Table
CREATE TABLE IF NOT EXISTS wp_fcom_streak_goals (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    goal_type VARCHAR(30) NOT NULL COMMENT 'STREAK_MILESTONE, DAILY_TARGET, TIMED_CHALLENGE',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT 'Unique code like STREAK_7, DAILY_POST',
    name VARCHAR(100) NOT NULL COMMENT 'Display name',
    description TEXT COMMENT 'Goal description',
    icon VARCHAR(255) COMMENT 'Icon URL',

    requirement_json JSON NOT NULL COMMENT 'Flexible requirement structure',
    reward_json JSON NOT NULL COMMENT 'Flexible reward structure',

    is_active BOOLEAN DEFAULT TRUE,
    is_repeatable BOOLEAN DEFAULT FALSE COMMENT 'Can be claimed multiple times',
    priority INT DEFAULT 0 COMMENT 'Display order',

    valid_from TIMESTAMP NULL,
    valid_until TIMESTAMP NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_streak_goal_type (goal_type),
    INDEX idx_streak_goal_code (code),
    INDEX idx_streak_goal_active (is_active),
    INDEX idx_streak_goal_validity (valid_from, valid_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User Goal Progress Table
CREATE TABLE IF NOT EXISTS wp_fcom_user_streak_goals (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT 'References wp_users.ID',
    goal_id BIGINT NOT NULL COMMENT 'References wp_fcom_streak_goals.id',

    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE, COMPLETED, CLAIMED, EXPIRED',
    progress_json JSON COMMENT 'Current progress data',

    completed_at TIMESTAMP NULL COMMENT 'When goal was completed',
    claimed_at TIMESTAMP NULL COMMENT 'When reward was claimed',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY unique_user_goal (user_id, goal_id, completed_at),
    INDEX idx_user_goal_user (user_id),
    INDEX idx_user_goal_status (status),
    INDEX idx_user_goal_completed (completed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Streak Activities Log Table
CREATE TABLE IF NOT EXISTS wp_fcom_streak_activities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT 'References wp_users.ID',

    activity_date DATE NOT NULL COMMENT 'Activity date',
    activity_type VARCHAR(50) COMMENT 'LOGIN, POST, COMMENT, QUIZ, LESSON, etc.',

    posts_count INT DEFAULT 0,
    comments_count INT DEFAULT 0,
    reactions_count INT DEFAULT 0,
    quizzes_completed INT DEFAULT 0,
    lessons_completed INT DEFAULT 0,

    streak_day INT COMMENT 'What streak day this was',
    is_freeze_used BOOLEAN DEFAULT FALSE COMMENT 'Was freeze used this day',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY unique_user_activity_date (user_id, activity_date),
    INDEX idx_streak_activity_user (user_id),
    INDEX idx_streak_activity_date (activity_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert streak-related badges
INSERT INTO wp_fcom_badges (name, slug, description, icon, color, type, requirement_type, requirement_value, points_reward, is_active, priority)
VALUES
('Streak Starter', 'streak-starter', 'Achieve a 7-day streak', NULL, '#FFA500', 'milestone', 'streak', 7, 50, TRUE, 100),
('Streak Master', 'streak-master', 'Achieve a 30-day streak', NULL, '#FFD700', 'milestone', 'streak', 30, 200, TRUE, 90),
('Streak Legend', 'streak-legend', 'Achieve a 100-day streak', NULL, '#FF4500', 'special', 'streak', 100, 1000, TRUE, 80),
('Comeback King', 'comeback-king', 'Use streak freeze 5 times', NULL, '#00CED1', 'achievement', 'streak_freeze', 5, 100, TRUE, 110);

-- Insert predefined streak goals
INSERT INTO wp_fcom_streak_goals (goal_type, code, name, description, icon, requirement_json, reward_json, is_active, is_repeatable, priority)
VALUES
('STREAK_MILESTONE', 'STREAK_7', '7-Day Streak Bonus', 'Maintain a 7-day streak to earn bonus points and a freeze', NULL, '{"streak_days": 7}', '{"type": "MULTIPLE", "rewards": [{"type": "POINTS", "value": 100}, {"type": "FREEZE", "count": 1}]}', TRUE, FALSE, 10),
('STREAK_MILESTONE', 'STREAK_14', '14-Day Streak Bonus', 'Maintain a 14-day streak', NULL, '{"streak_days": 14}', '{"type": "MULTIPLE", "rewards": [{"type": "POINTS", "value": 200}, {"type": "FREEZE", "count": 1}]}', TRUE, FALSE, 20),
('STREAK_MILESTONE', 'STREAK_30', '30-Day Streak Master', 'Achieve the legendary 30-day streak', NULL, '{"streak_days": 30}', '{"type": "MULTIPLE", "rewards": [{"type": "POINTS", "value": 500}, {"type": "FREEZE", "count": 2}]}', TRUE, FALSE, 30),
('STREAK_MILESTONE', 'STREAK_100', '100-Day Streak Legend', 'Ultimate achievement: 100-day streak', NULL, '{"streak_days": 100}', '{"type": "MULTIPLE", "rewards": [{"type": "POINTS", "value": 2000}, {"type": "FREEZE", "count": 5}]}', TRUE, FALSE, 40),
('DAILY_TARGET', 'DAILY_ACTIVE', 'Daily Active', 'Login to the app', NULL, '{"login": 1}', '{"type": "POINTS", "value": 10}', TRUE, TRUE, 100),
('DAILY_TARGET', 'DAILY_POST', 'Daily Poster', 'Create 1 post today', NULL, '{"posts": 1}', '{"type": "POINTS", "value": 30}', TRUE, TRUE, 110),
('DAILY_TARGET', 'DAILY_ENGAGE', 'Daily Engager', 'Comment 3 times and react 5 times', NULL, '{"comments": 3, "reactions": 5}', '{"type": "POINTS", "value": 50}', TRUE, TRUE, 120);
