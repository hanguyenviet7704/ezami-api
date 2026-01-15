-- V9: Create Learning Insights tables
-- Pattern Analysis, Spaced Repetition (SRS), Time Estimation

-- =====================================================
-- 1. Pattern Analysis Table
-- =====================================================
CREATE TABLE IF NOT EXISTS eil_pattern_analyses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(50) NOT NULL,
    session_type VARCHAR(30),
    certification_code VARCHAR(30),

    -- Session metrics
    total_questions INT DEFAULT 0,
    correct_count INT DEFAULT 0,
    accuracy DECIMAL(5,4),
    avg_time_per_question INT,
    total_time_seconds INT,

    -- Time patterns
    session_hour INT,
    time_of_day VARCHAR(20),
    day_of_week INT,

    -- Detected patterns (JSON)
    detected_patterns JSON,

    -- Fatigue analysis
    fatigue_detected TINYINT(1) DEFAULT 0,
    fatigue_score DECIMAL(5,4),

    -- Speed vs Accuracy
    speed_accuracy_tradeoff VARCHAR(30),

    -- Category analysis (JSON)
    weak_categories JSON,
    strong_categories JSON,

    -- Recommendations (JSON)
    recommendations JSON,

    -- Confidence
    confidence DECIMAL(5,4),

    -- Timing
    session_start_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_pattern_user_id (user_id),
    INDEX idx_pattern_session_id (session_id),
    INDEX idx_pattern_cert_code (certification_code),
    INDEX idx_pattern_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 2. SRS Cards Table (Spaced Repetition)
-- =====================================================
CREATE TABLE IF NOT EXISTS eil_srs_cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    client_id VARCHAR(50),
    question_id BIGINT NOT NULL,
    skill_id BIGINT,
    certification_code VARCHAR(30),

    -- SM-2 algorithm fields
    ease_factor DECIMAL(5,3) DEFAULT 2.500,
    interval_days INT DEFAULT 1,
    repetitions INT DEFAULT 0,
    quality_history JSON,

    -- Status: NEW, LEARNING, REVIEW, SUSPENDED
    status VARCHAR(20) DEFAULT 'NEW',

    -- Review statistics
    total_reviews INT DEFAULT 0,
    correct_reviews INT DEFAULT 0,
    last_quality INT,
    streak INT DEFAULT 0,

    -- Timing
    next_review_at DATETIME,
    last_reviewed_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Sync version for offline-first
    sync_version INT DEFAULT 1,

    INDEX idx_srs_user_id (user_id),
    INDEX idx_srs_question_id (question_id),
    INDEX idx_srs_cert_code (certification_code),
    INDEX idx_srs_next_review (next_review_at),
    INDEX idx_srs_status (status),
    INDEX idx_srs_user_cert (user_id, certification_code),
    UNIQUE KEY uk_srs_user_question (user_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 3. Time Estimates Table
-- =====================================================
CREATE TABLE IF NOT EXISTS eil_time_estimates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    certification_code VARCHAR(30) NOT NULL,
    certification_name VARCHAR(100),

    -- Current state
    current_mastery DECIMAL(5,4),
    target_mastery DECIMAL(5,4) DEFAULT 0.8000,
    mastery_gap DECIMAL(5,4),

    -- Time estimates
    estimated_days INT,
    estimated_hours INT,
    estimated_sessions INT,
    estimated_ready_date DATE,

    -- Progress tracking
    days_practiced INT DEFAULT 0,
    total_study_hours DECIMAL(6,2) DEFAULT 0,
    questions_practiced INT DEFAULT 0,
    sessions_completed INT DEFAULT 0,

    -- Learning pace
    avg_daily_hours DECIMAL(4,2),
    avg_sessions_per_day DECIMAL(4,2),
    mastery_velocity DECIMAL(6,5),

    -- Confidence
    confidence DECIMAL(5,4),
    confidence_level VARCHAR(20),

    -- Status: ACTIVE, PAUSED, COMPLETED, ABANDONED
    status VARCHAR(20) DEFAULT 'ACTIVE',

    -- Historical data (JSON)
    progress_history JSON,
    milestones JSON,

    -- Recommendations
    recommended_daily_hours DECIMAL(4,2),
    focus_areas JSON,

    -- Timing
    start_date DATE,
    target_date DATE,
    last_activity_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_estimate_user_id (user_id),
    INDEX idx_estimate_cert_code (certification_code),
    INDEX idx_estimate_user_cert (user_id, certification_code),
    INDEX idx_estimate_status (status),
    UNIQUE KEY uk_estimate_user_cert (user_id, certification_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
