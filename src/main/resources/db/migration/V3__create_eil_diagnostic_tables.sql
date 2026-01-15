-- EIL Diagnostic Tables Migration
-- Creates: eil_diagnostic_attempts, eil_diagnostic_answers

-- Diagnostic Attempts Table
CREATE TABLE IF NOT EXISTS eil_diagnostic_attempts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT 'References wp_users.ID',
    session_id VARCHAR(36) NOT NULL UNIQUE COMMENT 'UUID for session tracking',
    test_type VARCHAR(30) DEFAULT 'TOEIC' COMMENT 'Type of diagnostic: TOEIC, IELTS, etc.',
    status VARCHAR(20) DEFAULT 'IN_PROGRESS' COMMENT 'Status: IN_PROGRESS, COMPLETED, ABANDONED',
    total_questions INT DEFAULT 0 COMMENT 'Total questions in diagnostic',
    answered_questions INT DEFAULT 0 COMMENT 'Number of answered questions',
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When diagnostic started',
    end_time TIMESTAMP NULL COMMENT 'When diagnostic ended',
    time_spent_seconds INT DEFAULT 0 COMMENT 'Total time spent in seconds',
    raw_score DECIMAL(5,2) DEFAULT 0.00 COMMENT 'Raw score percentage 0-100',
    estimated_level VARCHAR(20) COMMENT 'Estimated level: BEGINNER, ELEMENTARY, INTERMEDIATE, UPPER_INTERMEDIATE, ADVANCED',
    estimated_score_min INT COMMENT 'Estimated minimum score (e.g., TOEIC score)',
    estimated_score_max INT COMMENT 'Estimated maximum score',
    skill_coverage JSON COMMENT 'JSON array of covered skill IDs',
    weak_skills JSON COMMENT 'JSON array of weak skill IDs identified',
    strong_skills JSON COMMENT 'JSON array of strong skill IDs identified',
    metadata JSON COMMENT 'Additional metadata (device, app version, etc.)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_eil_diag_user (user_id),
    INDEX idx_eil_diag_session (session_id),
    INDEX idx_eil_diag_status (status),
    INDEX idx_eil_diag_test_type (test_type),
    INDEX idx_eil_diag_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Diagnostic Answers Table
CREATE TABLE IF NOT EXISTS eil_diagnostic_answers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    diagnostic_attempt_id BIGINT NOT NULL COMMENT 'References eil_diagnostic_attempts.id',
    question_id BIGINT NOT NULL COMMENT 'References wp_learndash_pro_quiz_question.id',
    skill_id BIGINT NOT NULL COMMENT 'References eil_skills.id',
    question_order INT COMMENT 'Order of question in diagnostic',
    difficulty INT COMMENT 'Question difficulty when served (1-5)',
    user_answer TEXT COMMENT 'Serialized user answer (JSON array of booleans)',
    correct_answer TEXT COMMENT 'Serialized correct answer',
    is_correct BOOLEAN COMMENT 'Whether answer was correct',
    time_spent_seconds INT DEFAULT 0 COMMENT 'Time spent on this question',
    answered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When question was answered',
    INDEX idx_eil_diag_ans_attempt (diagnostic_attempt_id),
    INDEX idx_eil_diag_ans_question (question_id),
    INDEX idx_eil_diag_ans_skill (skill_id),
    INDEX idx_eil_diag_ans_correct (is_correct),
    FOREIGN KEY (diagnostic_attempt_id) REFERENCES eil_diagnostic_attempts(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES eil_skills(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
