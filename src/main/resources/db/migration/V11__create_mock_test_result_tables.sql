-- V11: Create Mock Test Result tables
-- These tables store simplified mock test results with answer details

-- Main mock test results table
CREATE TABLE IF NOT EXISTS eil_mock_test_results (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT 'References wp_users.ID',
  `quiz_id` bigint NOT NULL COMMENT 'References wp_learndash_pro_quiz_master.id',
  `certificate_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Certificate type code',
  `score` double NOT NULL COMMENT 'Final score',
  `total_points` int NOT NULL COMMENT 'Total points earned',
  `correct_count` int NOT NULL COMMENT 'Number of correct answers',
  `total_questions` int NOT NULL COMMENT 'Total number of questions',
  `time_spent_seconds` int NOT NULL COMMENT 'Total time spent in seconds',
  `percentage_score` double DEFAULT NULL COMMENT 'Percentage score',
  `passed` tinyint(1) DEFAULT NULL COMMENT 'Whether passed or not',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_created` (`user_id`,`created_at` DESC),
  KEY `idx_cert_code` (`certificate_code`),
  KEY `idx_user_cert` (`user_id`,`certificate_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Answer details table
CREATE TABLE IF NOT EXISTS eil_mock_test_result_answers (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `mock_result_id` bigint NOT NULL COMMENT 'References eil_mock_test_results.id',
  `question_id` bigint NOT NULL COMMENT 'References wp_learndash_pro_quiz_question.id',
  `user_answer` text COLLATE utf8mb4_unicode_ci COMMENT 'User selected answer(s)',
  `correct_answer` text COLLATE utf8mb4_unicode_ci COMMENT 'Correct answer(s)',
  `is_correct` tinyint(1) NOT NULL COMMENT 'Whether answer was correct',
  `points_earned` double DEFAULT NULL COMMENT 'Points earned for this question',
  `max_points` double DEFAULT NULL COMMENT 'Maximum points possible',
  PRIMARY KEY (`id`),
  KEY `idx_mock_result` (`mock_result_id`),
  KEY `idx_question` (`question_id`),
  CONSTRAINT `fk_mock_test_result_answers` FOREIGN KEY (`mock_result_id`) REFERENCES `eil_mock_test_results` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
