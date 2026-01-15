-- EIL Skills Tables Migration
-- Creates: eil_skills, eil_question_skills, eil_skill_mastery

-- Skill Taxonomy Table
CREATE TABLE IF NOT EXISTS eil_skills (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE COMMENT 'Unique skill code, e.g., TOEIC_LC_P1_PHOTOGRAPHS',
    name VARCHAR(255) NOT NULL COMMENT 'Skill name in English',
    name_vi VARCHAR(255) COMMENT 'Skill name in Vietnamese',
    description TEXT COMMENT 'Skill description in English',
    description_vi TEXT COMMENT 'Skill description in Vietnamese',
    category VARCHAR(50) NOT NULL COMMENT 'Main category: LISTENING, READING, GRAMMAR, VOCABULARY',
    subcategory VARCHAR(50) COMMENT 'Subcategory: PART1, PART2, etc.',
    level INT DEFAULT 1 COMMENT 'Hierarchy level: 1=category, 2=subcategory, 3=skill',
    parent_id BIGINT COMMENT 'Self-referencing for hierarchy',
    weight DECIMAL(3,2) DEFAULT 1.00 COMMENT 'Weight for scoring calculations',
    difficulty_range_min INT DEFAULT 1 COMMENT 'Minimum difficulty (1-5)',
    difficulty_range_max INT DEFAULT 5 COMMENT 'Maximum difficulty (1-5)',
    is_active BOOLEAN DEFAULT TRUE COMMENT 'Whether skill is active',
    priority INT DEFAULT 0 COMMENT 'Display priority',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_eil_skills_code (code),
    INDEX idx_eil_skills_category (category),
    INDEX idx_eil_skills_subcategory (subcategory),
    INDEX idx_eil_skills_parent (parent_id),
    INDEX idx_eil_skills_active (is_active),
    FOREIGN KEY (parent_id) REFERENCES eil_skills(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Question-Skill Mapping Table
CREATE TABLE IF NOT EXISTS eil_question_skills (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    question_id BIGINT NOT NULL COMMENT 'References wp_learndash_pro_quiz_question.id',
    skill_id BIGINT NOT NULL COMMENT 'References eil_skills.id',
    difficulty INT DEFAULT 3 COMMENT 'Question difficulty for this skill (1-5)',
    weight DECIMAL(3,2) DEFAULT 1.00 COMMENT 'Contribution weight to skill mastery',
    is_primary BOOLEAN DEFAULT TRUE COMMENT 'Primary skill for this question',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_eil_question_skill (question_id, skill_id),
    INDEX idx_eil_qs_question (question_id),
    INDEX idx_eil_qs_skill (skill_id),
    INDEX idx_eil_qs_difficulty (difficulty),
    INDEX idx_eil_qs_primary (is_primary),
    FOREIGN KEY (skill_id) REFERENCES eil_skills(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User Skill Mastery Table
CREATE TABLE IF NOT EXISTS eil_skill_mastery (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT 'References wp_users.ID',
    skill_id BIGINT NOT NULL COMMENT 'References eil_skills.id',
    mastery_level DECIMAL(5,4) DEFAULT 0.5000 COMMENT 'Mastery level 0.0000 - 1.0000',
    confidence DECIMAL(5,4) DEFAULT 0.1000 COMMENT 'Confidence in mastery estimate',
    attempts INT DEFAULT 0 COMMENT 'Total question attempts for this skill',
    correct_count INT DEFAULT 0 COMMENT 'Number of correct answers',
    streak INT DEFAULT 0 COMMENT 'Current correct answer streak',
    last_practiced_at TIMESTAMP NULL COMMENT 'Last time user practiced this skill',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_eil_user_skill (user_id, skill_id),
    INDEX idx_eil_mastery_user (user_id),
    INDEX idx_eil_mastery_skill (skill_id),
    INDEX idx_eil_mastery_level (mastery_level),
    INDEX idx_eil_mastery_confidence (confidence),
    FOREIGN KEY (skill_id) REFERENCES eil_skills(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
