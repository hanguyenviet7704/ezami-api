-- EIL AI Tables Migration
-- Creates: eil_explanations, eil_ai_feedback

-- AI Explanations Cache Table
CREATE TABLE IF NOT EXISTS eil_explanations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cache_key VARCHAR(64) NOT NULL UNIQUE COMMENT 'SHA256 hash of (question_id + user_answer + version)',
    question_id BIGINT NOT NULL COMMENT 'References wp_learndash_pro_quiz_question.id',
    user_answer TEXT NOT NULL COMMENT 'Serialized user answer',
    correct_answer TEXT COMMENT 'Serialized correct answer',
    is_correct BOOLEAN COMMENT 'Whether user answer was correct',
    language VARCHAR(10) DEFAULT 'vi' COMMENT 'Language of explanation: vi, en',

    -- Structured explanation content
    explanation_json JSON NOT NULL COMMENT 'Full structured explanation',
    summary TEXT COMMENT 'Brief summary',
    why_correct TEXT COMMENT 'Why correct answer is right',
    why_wrong TEXT COMMENT 'Why user answer was wrong (if incorrect)',
    key_points JSON COMMENT 'Array of key learning points',
    grammar_rule TEXT COMMENT 'Relevant grammar rule',
    vocabulary_tip TEXT COMMENT 'Relevant vocabulary tip',
    examples JSON COMMENT 'Array of related examples',

    -- Metadata
    model_version VARCHAR(50) COMMENT 'LLM model version used (e.g., gpt-4o-2024-05-13)',
    prompt_version INT DEFAULT 1 COMMENT 'Version of prompt template',
    tokens_used INT DEFAULT 0 COMMENT 'Total tokens used for generation',
    generation_time_ms INT DEFAULT 0 COMMENT 'Time to generate in milliseconds',
    version INT DEFAULT 1 COMMENT 'Cache version for invalidation',
    hit_count INT DEFAULT 0 COMMENT 'Number of cache hits',
    last_accessed_at TIMESTAMP NULL COMMENT 'Last time explanation was accessed',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL COMMENT 'Optional expiry time',

    INDEX idx_eil_expl_cache (cache_key),
    INDEX idx_eil_expl_question (question_id),
    INDEX idx_eil_expl_language (language),
    INDEX idx_eil_expl_version (version),
    INDEX idx_eil_expl_expires (expires_at),
    INDEX idx_eil_expl_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- AI Feedback Storage Table
CREATE TABLE IF NOT EXISTS eil_ai_feedback (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT 'References wp_users.ID',
    feedback_type VARCHAR(30) NOT NULL COMMENT 'Type: EXPLANATION, RECOMMENDATION, SUMMARY, STUDY_PLAN',
    context_type VARCHAR(30) COMMENT 'Context: DIAGNOSTIC, PRACTICE, MOCK, GENERAL',
    context_id BIGINT COMMENT 'ID of related session/attempt',
    language VARCHAR(10) DEFAULT 'vi' COMMENT 'Language of feedback',

    -- Feedback content
    feedback_json JSON NOT NULL COMMENT 'Full structured feedback',
    summary TEXT COMMENT 'Brief summary of feedback',
    recommendations JSON COMMENT 'Array of recommendations',
    action_items JSON COMMENT 'Array of action items',

    -- Metadata
    model_version VARCHAR(50) COMMENT 'LLM model version',
    prompt_version INT DEFAULT 1 COMMENT 'Prompt template version',
    tokens_used INT DEFAULT 0 COMMENT 'Tokens used',
    generation_time_ms INT DEFAULT 0 COMMENT 'Generation time in ms',

    -- User response
    user_rating INT COMMENT 'User rating 1-5 (optional)',
    user_comment TEXT COMMENT 'User feedback comment',
    is_helpful BOOLEAN COMMENT 'User marked as helpful',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_eil_feedback_user (user_id),
    INDEX idx_eil_feedback_type (feedback_type),
    INDEX idx_eil_feedback_context (context_type, context_id),
    INDEX idx_eil_feedback_rating (user_rating),
    INDEX idx_eil_feedback_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
