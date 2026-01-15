-- EIL Practice Tables Migration
-- Creates: eil_practice_sessions, eil_practice_attempts

-- Practice Sessions Table
CREATE TABLE IF NOT EXISTS eil_practice_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT 'References wp_users.ID',
    session_id VARCHAR(36) NOT NULL UNIQUE COMMENT 'UUID for session tracking',
    session_type VARCHAR(30) DEFAULT 'ADAPTIVE' COMMENT 'Type: ADAPTIVE, SKILL_FOCUS, REVIEW, MIXED',
    target_skill_id BIGINT COMMENT 'Target skill for SKILL_FOCUS type',
    target_categories JSON COMMENT 'Target categories for practice',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'Status: ACTIVE, COMPLETED, PAUSED, ABANDONED',
    max_questions INT DEFAULT 20 COMMENT 'Maximum questions per session',
    total_questions INT DEFAULT 0 COMMENT 'Total questions served',
    correct_count INT DEFAULT 0 COMMENT 'Number of correct answers',
    current_difficulty DECIMAL(3,2) DEFAULT 3.00 COMMENT 'Running difficulty level (1.00-5.00)',
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When session started',
    end_time TIMESTAMP NULL COMMENT 'When session ended',
    total_time_seconds INT DEFAULT 0 COMMENT 'Total time spent in seconds',
    mastery_gain DECIMAL(5,4) DEFAULT 0.0000 COMMENT 'Total mastery gain in session',
    points_earned INT DEFAULT 0 COMMENT 'Points earned in session',
    metadata JSON COMMENT 'Additional metadata',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_eil_practice_user (user_id),
    INDEX idx_eil_practice_session (session_id),
    INDEX idx_eil_practice_status (status),
    INDEX idx_eil_practice_type (session_type),
    INDEX idx_eil_practice_target_skill (target_skill_id),
    INDEX idx_eil_practice_created (created_at),
    FOREIGN KEY (target_skill_id) REFERENCES eil_skills(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Practice Attempts Table
CREATE TABLE IF NOT EXISTS eil_practice_attempts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL COMMENT 'References eil_practice_sessions.id',
    question_id BIGINT NOT NULL COMMENT 'References wp_learndash_pro_quiz_question.id',
    skill_id BIGINT NOT NULL COMMENT 'References eil_skills.id',
    question_order INT COMMENT 'Order of question in session',
    question_difficulty INT COMMENT 'Difficulty level when served (1-5)',
    user_answer TEXT COMMENT 'Serialized user answer (JSON array)',
    correct_answer TEXT COMMENT 'Serialized correct answer',
    is_correct BOOLEAN COMMENT 'Whether answer was correct',
    time_spent_seconds INT DEFAULT 0 COMMENT 'Time spent on question',
    mastery_before DECIMAL(5,4) COMMENT 'Mastery level before this attempt',
    mastery_after DECIMAL(5,4) COMMENT 'Mastery level after this attempt',
    mastery_delta DECIMAL(5,4) COMMENT 'Change in mastery',
    explanation_id BIGINT COMMENT 'References eil_explanations.id if explanation was requested',
    points_earned INT DEFAULT 0 COMMENT 'Points earned for this question',
    answered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When answered',
    INDEX idx_eil_pa_session (session_id),
    INDEX idx_eil_pa_question (question_id),
    INDEX idx_eil_pa_skill (skill_id),
    INDEX idx_eil_pa_correct (is_correct),
    INDEX idx_eil_pa_answered (answered_at),
    FOREIGN KEY (session_id) REFERENCES eil_practice_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES eil_skills(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
