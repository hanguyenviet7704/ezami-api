-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 34.81.52.243
-- Generation Time: May 04, 2024 at 03:12 AM
-- Server version: 8.0.32
-- PHP Version: 8.1.27

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `wordpress`
--

-- --------------------------------------------------------

--
-- Table structure for table `ez_app`
--

CREATE TABLE `ez_app` (
  `id` int UNSIGNED NOT NULL,
  `app_code` varchar(50) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'ezami',
  `ios_store_url` varchar(250) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `android_store_url` varchar(250) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `enable` tinyint NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ez_article_space`
--

CREATE TABLE `ez_article_space` (
  `id` bigint UNSIGNED NOT NULL,
  `title` varchar(250) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `article_category` text COLLATE utf8mb4_unicode_ci,
  `app_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT 'Application code',
  `order` int NOT NULL DEFAULT '0',
  `enable` tinyint(1) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ez_article_space_category`
--

CREATE TABLE `ez_article_space_category` (
  `id` bigint UNSIGNED NOT NULL,
  `space_id` bigint UNSIGNED NOT NULL DEFAULT '0',
  `category_slug` varchar(250) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `category_name` varchar(250) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `language` varchar(5) COLLATE utf8mb4_unicode_520_ci NOT NULL COMMENT 'vn or en',
  `order` int NOT NULL DEFAULT '0',
  `enable` tinyint(1) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
-- --------------------------------------------------------

--
-- Table structure for table `ez_quiz_category`
--

CREATE TABLE `ez_quiz_category` (
  `id` bigint UNSIGNED NOT NULL,
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `title` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '' COMMENT 'To filter quiz by name',
  `header` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '' COMMENT 'To display on header of test detail screen',
  `image_uri` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `order` int NOT NULL DEFAULT '0',
  `enable` tinyint(1) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
-- --------------------------------------------------------

--
-- Table structure for table `ez_user_purchased`
--

CREATE TABLE `ez_user_purchased` (
  `id` bigint UNSIGNED NOT NULL,
  `user_id` bigint UNSIGNED NOT NULL DEFAULT '0',
  `user_email` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `category_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `is_purchased` tinyint(1) NOT NULL DEFAULT '0',
  `from_time` datetime DEFAULT NULL,
  `to_time` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ez_verification_code`
--

CREATE TABLE `ez_verification_code` (
  `id` bigint UNSIGNED NOT NULL,
  `user_id` bigint UNSIGNED DEFAULT '0',
  `email` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `code` varchar(50) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `type` varchar(20) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `expiry_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ez_version`
--

CREATE TABLE `ez_version` (
  `id` int UNSIGNED NOT NULL,
  `app_code` varchar(50) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'ezami',
  `platform` varchar(10) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `build_number` varchar(10) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `version_name` varchar(10) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `force_download` tinyint NOT NULL DEFAULT '0',
  `note` text COLLATE utf8mb4_unicode_520_ci,
  `latest` tinyint NOT NULL DEFAULT '0',
  `enable` tinyint NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_actionscheduler_actions`
--

CREATE TABLE `wp_actionscheduler_actions` (
  `action_id` bigint UNSIGNED NOT NULL,
  `hook` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `scheduled_date_gmt` datetime DEFAULT '0000-00-00 00:00:00',
  `scheduled_date_local` datetime DEFAULT '0000-00-00 00:00:00',
  `args` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `schedule` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `group_id` bigint UNSIGNED NOT NULL DEFAULT '0',
  `attempts` int NOT NULL DEFAULT '0',
  `last_attempt_gmt` datetime DEFAULT '0000-00-00 00:00:00',
  `last_attempt_local` datetime DEFAULT '0000-00-00 00:00:00',
  `claim_id` bigint UNSIGNED NOT NULL DEFAULT '0',
  `extended_args` varchar(8000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `priority` tinyint UNSIGNED NOT NULL DEFAULT '10'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_actionscheduler_claims`
--

CREATE TABLE `wp_actionscheduler_claims` (
  `claim_id` bigint UNSIGNED NOT NULL,
  `date_created_gmt` datetime DEFAULT '0000-00-00 00:00:00'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_actionscheduler_groups`
--

CREATE TABLE `wp_actionscheduler_groups` (
  `group_id` bigint UNSIGNED NOT NULL,
  `slug` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_actionscheduler_logs`
--

CREATE TABLE `wp_actionscheduler_logs` (
  `log_id` bigint UNSIGNED NOT NULL,
  `action_id` bigint UNSIGNED NOT NULL,
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `log_date_gmt` datetime DEFAULT '0000-00-00 00:00:00',
  `log_date_local` datetime DEFAULT '0000-00-00 00:00:00'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_aioseo_cache`
--

CREATE TABLE `wp_aioseo_cache` (
  `id` bigint UNSIGNED NOT NULL,
  `key` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `value` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `expiration` datetime DEFAULT NULL,
  `created` datetime NOT NULL,
  `updated` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_aioseo_crawl_cleanup_blocked_args`
--

CREATE TABLE `wp_aioseo_crawl_cleanup_blocked_args` (
  `id` bigint UNSIGNED NOT NULL,
  `key` text COLLATE utf8mb4_unicode_520_ci,
  `value` text COLLATE utf8mb4_unicode_520_ci,
  `key_value_hash` varchar(40) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `regex` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `hits` int NOT NULL DEFAULT '0',
  `created` datetime NOT NULL,
  `updated` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_aioseo_crawl_cleanup_logs`
--

CREATE TABLE `wp_aioseo_crawl_cleanup_logs` (
  `id` bigint UNSIGNED NOT NULL,
  `slug` text COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `key` text COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `value` text COLLATE utf8mb4_unicode_520_ci,
  `hash` varchar(40) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `hits` int NOT NULL DEFAULT '1',
  `created` datetime NOT NULL,
  `updated` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_aioseo_notifications`
--

CREATE TABLE `wp_aioseo_notifications` (
  `id` bigint UNSIGNED NOT NULL,
  `slug` varchar(13) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `addon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `level` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `notification_id` bigint UNSIGNED DEFAULT NULL,
  `notification_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `start` datetime DEFAULT NULL,
  `end` datetime DEFAULT NULL,
  `button1_label` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `button1_action` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `button2_label` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `button2_action` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `dismissed` tinyint(1) NOT NULL DEFAULT '0',
  `new` tinyint(1) NOT NULL DEFAULT '1',
  `created` datetime NOT NULL,
  `updated` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_aioseo_posts`
--

CREATE TABLE `wp_aioseo_posts` (
  `id` bigint UNSIGNED NOT NULL,
  `post_id` bigint UNSIGNED NOT NULL,
  `title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `keywords` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `keyphrases` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `page_analysis` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `primary_term` longtext COLLATE utf8mb4_unicode_520_ci,
  `canonical_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `og_title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `og_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `og_object_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT 'default',
  `og_image_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT 'default',
  `og_image_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `og_image_width` int DEFAULT NULL,
  `og_image_height` int DEFAULT NULL,
  `og_image_custom_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `og_image_custom_fields` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `og_video` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `og_custom_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `og_article_section` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `og_article_tags` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `twitter_use_og` tinyint(1) DEFAULT '0',
  `twitter_card` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT 'default',
  `twitter_image_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT 'default',
  `twitter_image_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `twitter_image_custom_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `twitter_image_custom_fields` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `twitter_title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `twitter_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `seo_score` int NOT NULL DEFAULT '0',
  `schema` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `schema_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT 'default',
  `schema_type_options` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `pillar_content` tinyint(1) DEFAULT NULL,
  `robots_default` tinyint(1) NOT NULL DEFAULT '1',
  `robots_noindex` tinyint(1) NOT NULL DEFAULT '0',
  `robots_noarchive` tinyint(1) NOT NULL DEFAULT '0',
  `robots_nosnippet` tinyint(1) NOT NULL DEFAULT '0',
  `robots_nofollow` tinyint(1) NOT NULL DEFAULT '0',
  `robots_noimageindex` tinyint(1) NOT NULL DEFAULT '0',
  `robots_noodp` tinyint(1) NOT NULL DEFAULT '0',
  `robots_notranslate` tinyint(1) NOT NULL DEFAULT '0',
  `robots_max_snippet` int DEFAULT NULL,
  `robots_max_videopreview` int DEFAULT NULL,
  `robots_max_imagepreview` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT 'large',
  `images` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `image_scan_date` datetime DEFAULT NULL,
  `priority` float DEFAULT NULL,
  `frequency` tinytext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `videos` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `video_thumbnail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `video_scan_date` datetime DEFAULT NULL,
  `local_seo` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `limit_modified_date` tinyint(1) NOT NULL DEFAULT '0',
  `options` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `created` datetime NOT NULL,
  `updated` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_commentmeta`
--

CREATE TABLE `wp_commentmeta` (
  `meta_id` bigint UNSIGNED NOT NULL,
  `comment_id` bigint UNSIGNED NOT NULL DEFAULT '0',
  `meta_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `meta_value` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_comments`
--

CREATE TABLE `wp_comments` (
  `comment_ID` bigint UNSIGNED NOT NULL,
  `comment_post_ID` bigint UNSIGNED NOT NULL DEFAULT '0',
  `comment_author` tinytext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `comment_author_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `comment_author_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `comment_author_IP` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `comment_date` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `comment_date_gmt` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `comment_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `comment_karma` int NOT NULL DEFAULT '0',
  `comment_approved` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '1',
  `comment_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `comment_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'comment',
  `comment_parent` bigint UNSIGNED NOT NULL DEFAULT '0',
  `user_id` bigint UNSIGNED NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_gla_attribute_mapping_rules`
--

CREATE TABLE `wp_gla_attribute_mapping_rules` (
  `id` bigint NOT NULL,
  `attribute` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `source` varchar(100) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `category_condition_type` varchar(10) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `categories` text COLLATE utf8mb4_unicode_520_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_gla_budget_recommendations`
--

CREATE TABLE `wp_gla_budget_recommendations` (
  `id` bigint NOT NULL,
  `currency` varchar(3) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `country` varchar(2) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `daily_budget` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_gla_merchant_issues`
--

CREATE TABLE `wp_gla_merchant_issues` (
  `id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `issue` varchar(200) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `code` varchar(100) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `severity` varchar(20) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'warning',
  `product` varchar(100) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `action` text COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `action_url` varchar(1024) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `applicable_countries` text COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `source` varchar(10) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'mc',
  `type` varchar(10) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'product',
  `created_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_gla_shipping_rates`
--

CREATE TABLE `wp_gla_shipping_rates` (
  `id` bigint NOT NULL,
  `country` varchar(2) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `currency` varchar(3) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `rate` double NOT NULL DEFAULT '0',
  `options` text COLLATE utf8mb4_unicode_520_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_gla_shipping_times`
--

CREATE TABLE `wp_gla_shipping_times` (
  `id` bigint NOT NULL,
  `country` varchar(2) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `time` bigint NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_learndash_pro_quiz_category`
--

CREATE TABLE `wp_learndash_pro_quiz_category` (
  `category_id` int UNSIGNED NOT NULL,
  `category_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_learndash_pro_quiz_form`
--

CREATE TABLE `wp_learndash_pro_quiz_form` (
  `form_id` int NOT NULL,
  `quiz_id` int NOT NULL,
  `fieldname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `type` tinyint NOT NULL,
  `required` tinyint UNSIGNED NOT NULL,
  `sort` tinyint NOT NULL,
  `data` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_learndash_pro_quiz_lock`
--

CREATE TABLE `wp_learndash_pro_quiz_lock` (
  `quiz_id` int NOT NULL,
  `lock_ip` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `user_id` bigint UNSIGNED NOT NULL,
  `lock_type` tinyint UNSIGNED NOT NULL,
  `lock_date` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_learndash_pro_quiz_master`
--

CREATE TABLE `wp_learndash_pro_quiz_master` (
  `id` int NOT NULL,
  `name` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `result_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `result_grade_enabled` tinyint(1) NOT NULL,
  `title_hidden` tinyint(1) NOT NULL,
  `btn_restart_quiz_hidden` tinyint(1) NOT NULL,
  `btn_view_question_hidden` tinyint(1) NOT NULL,
  `question_random` tinyint(1) NOT NULL,
  `answer_random` tinyint(1) NOT NULL,
  `time_limit` int NOT NULL,
  `statistics_on` tinyint(1) NOT NULL,
  `statistics_ip_lock` int UNSIGNED NOT NULL,
  `show_points` tinyint(1) NOT NULL,
  `quiz_run_once` tinyint(1) NOT NULL,
  `quiz_run_once_type` tinyint NOT NULL,
  `quiz_run_once_cookie` tinyint(1) NOT NULL,
  `quiz_run_once_time` int UNSIGNED NOT NULL,
  `numbered_answer` tinyint(1) NOT NULL,
  `hide_answer_message_box` tinyint(1) NOT NULL,
  `disabled_answer_mark` tinyint(1) NOT NULL,
  `show_max_question` tinyint(1) NOT NULL,
  `show_max_question_value` int UNSIGNED NOT NULL,
  `show_max_question_percent` tinyint(1) NOT NULL,
  `toplist_activated` tinyint(1) NOT NULL,
  `toplist_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `show_average_result` tinyint(1) NOT NULL,
  `prerequisite` tinyint(1) NOT NULL,
  `quiz_modus` tinyint UNSIGNED NOT NULL,
  `show_review_question` tinyint(1) NOT NULL,
  `quiz_summary_hide` tinyint(1) NOT NULL,
  `skip_question_disabled` tinyint(1) NOT NULL,
  `email_notification` tinyint UNSIGNED NOT NULL,
  `user_email_notification` tinyint UNSIGNED NOT NULL,
  `show_category_score` tinyint UNSIGNED NOT NULL,
  `hide_result_correct_question` tinyint UNSIGNED NOT NULL DEFAULT '0',
  `hide_result_quiz_time` tinyint UNSIGNED NOT NULL DEFAULT '0',
  `hide_result_points` tinyint UNSIGNED NOT NULL DEFAULT '0',
  `autostart` tinyint UNSIGNED NOT NULL DEFAULT '0',
  `forcing_question_solve` tinyint UNSIGNED NOT NULL DEFAULT '0',
  `hide_question_position_overview` tinyint UNSIGNED NOT NULL DEFAULT '0',
  `hide_question_numbering` tinyint UNSIGNED NOT NULL DEFAULT '0',
  `form_activated` tinyint UNSIGNED NOT NULL,
  `form_show_position` tinyint UNSIGNED NOT NULL,
  `start_only_registered_user` tinyint UNSIGNED NOT NULL,
  `questions_per_page` tinyint UNSIGNED NOT NULL,
  `sort_categories` tinyint UNSIGNED NOT NULL,
  `show_category` tinyint UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_learndash_pro_quiz_prerequisite`
--

CREATE TABLE `wp_learndash_pro_quiz_prerequisite` (
  `prerequisite_quiz_id` int NOT NULL,
  `quiz_id` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_learndash_pro_quiz_question`
--

CREATE TABLE `wp_learndash_pro_quiz_question` (
  `id` int NOT NULL,
  `quiz_id` int NOT NULL,
  `online` tinyint UNSIGNED NOT NULL,
  `previous_id` int NOT NULL,
  `sort` smallint UNSIGNED NOT NULL,
  `title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `points` int NOT NULL,
  `question` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `correct_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `incorrect_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `correct_same_text` tinyint(1) NOT NULL,
  `tip_enabled` tinyint(1) NOT NULL,
  `tip_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `answer_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `show_points_in_box` tinyint(1) NOT NULL,
  `answer_points_activated` tinyint(1) NOT NULL,
  `answer_data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `category_id` int UNSIGNED NOT NULL,
  `answer_points_diff_modus_activated` tinyint UNSIGNED NOT NULL,
  `disable_correct` tinyint UNSIGNED NOT NULL,
  `matrix_sort_answer_criteria_width` tinyint UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_learndash_pro_quiz_statistic`
--

CREATE TABLE `wp_learndash_pro_quiz_statistic` (
  `statistic_ref_id` int UNSIGNED NOT NULL,
  `question_id` int NOT NULL,
  `question_post_id` int NOT NULL,
  `correct_count` int UNSIGNED NOT NULL,
  `incorrect_count` int UNSIGNED NOT NULL,
  `hint_count` int UNSIGNED NOT NULL,
  `points` int UNSIGNED NOT NULL,
  `question_time` int UNSIGNED NOT NULL,
  `answer_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_learndash_pro_quiz_statistic_ref`
--

CREATE TABLE `wp_learndash_pro_quiz_statistic_ref` (
  `statistic_ref_id` int UNSIGNED NOT NULL,
  `quiz_id` int NOT NULL,
  `quiz_post_id` int NOT NULL,
  `course_post_id` int NOT NULL,
  `user_id` bigint UNSIGNED NOT NULL,
  `create_time` int NOT NULL,
  `is_old` tinyint UNSIGNED NOT NULL,
  `form_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_learndash_pro_quiz_template`
--

CREATE TABLE `wp_learndash_pro_quiz_template` (
  `template_id` int NOT NULL,
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `type` tinyint UNSIGNED NOT NULL,
  `data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_learndash_pro_quiz_toplist`
--

CREATE TABLE `wp_learndash_pro_quiz_toplist` (
  `toplist_id` int NOT NULL,
  `quiz_id` int NOT NULL,
  `date` int UNSIGNED NOT NULL,
  `user_id` bigint UNSIGNED NOT NULL,
  `name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `email` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `points` int UNSIGNED NOT NULL,
  `result` float UNSIGNED NOT NULL,
  `ip` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_learndash_user_activity`
--

CREATE TABLE `wp_learndash_user_activity` (
  `activity_id` bigint UNSIGNED NOT NULL,
  `user_id` bigint UNSIGNED NOT NULL DEFAULT '0',
  `post_id` bigint UNSIGNED NOT NULL DEFAULT '0',
  `course_id` bigint UNSIGNED NOT NULL DEFAULT '0',
  `activity_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `activity_status` tinyint UNSIGNED DEFAULT NULL,
  `activity_started` int UNSIGNED DEFAULT NULL,
  `activity_completed` int UNSIGNED DEFAULT NULL,
  `activity_updated` int UNSIGNED DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_learndash_user_activity_meta`
--

CREATE TABLE `wp_learndash_user_activity_meta` (
  `activity_meta_id` bigint UNSIGNED NOT NULL,
  `activity_id` bigint UNSIGNED NOT NULL DEFAULT '0',
  `activity_meta_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `activity_meta_value` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_links`
--

CREATE TABLE `wp_links` (
  `link_id` bigint UNSIGNED NOT NULL,
  `link_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `link_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `link_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `link_target` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `link_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `link_visible` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'Y',
  `link_owner` bigint UNSIGNED NOT NULL DEFAULT '1',
  `link_rating` int NOT NULL DEFAULT '0',
  `link_updated` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `link_rel` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `link_notes` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `link_rss` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_litespeed_url`
--

CREATE TABLE `wp_litespeed_url` (
  `id` bigint NOT NULL,
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `cache_tags` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_litespeed_url_file`
--

CREATE TABLE `wp_litespeed_url_file` (
  `id` bigint NOT NULL,
  `url_id` bigint NOT NULL,
  `vary` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT 'md5 of final vary',
  `filename` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT 'md5 of file content',
  `type` tinyint NOT NULL COMMENT 'css=1,js=2,ccss=3,ucss=4',
  `mobile` tinyint NOT NULL COMMENT 'mobile=1',
  `webp` tinyint NOT NULL COMMENT 'webp=1',
  `expired` int NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_automations`
--

CREATE TABLE `wp_mailpoet_automations` (
  `id` int UNSIGNED NOT NULL,
  `name` varchar(191) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `author` bigint NOT NULL,
  `status` varchar(191) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `meta` longtext COLLATE utf8mb4_unicode_520_ci,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `activated_at` timestamp NULL DEFAULT NULL,
  `deleted_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_automation_runs`
--

CREATE TABLE `wp_mailpoet_automation_runs` (
  `id` int UNSIGNED NOT NULL,
  `automation_id` int UNSIGNED NOT NULL,
  `version_id` int UNSIGNED NOT NULL,
  `trigger_key` varchar(191) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `status` varchar(191) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `next_step_id` varchar(191) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_automation_run_logs`
--

CREATE TABLE `wp_mailpoet_automation_run_logs` (
  `id` int UNSIGNED NOT NULL,
  `automation_run_id` int UNSIGNED NOT NULL,
  `step_id` varchar(191) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `step_type` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `step_key` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `status` varchar(191) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `started_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `run_number` int NOT NULL,
  `data` longtext COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `error` longtext COLLATE utf8mb4_unicode_520_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_automation_run_subjects`
--

CREATE TABLE `wp_mailpoet_automation_run_subjects` (
  `id` int UNSIGNED NOT NULL,
  `automation_run_id` int UNSIGNED NOT NULL,
  `key` varchar(191) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `args` longtext COLLATE utf8mb4_unicode_520_ci,
  `hash` varchar(191) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_automation_triggers`
--

CREATE TABLE `wp_mailpoet_automation_triggers` (
  `automation_id` int UNSIGNED NOT NULL,
  `trigger_key` varchar(191) COLLATE utf8mb4_unicode_520_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_automation_versions`
--

CREATE TABLE `wp_mailpoet_automation_versions` (
  `id` int UNSIGNED NOT NULL,
  `automation_id` int UNSIGNED NOT NULL,
  `steps` longtext COLLATE utf8mb4_unicode_520_ci,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_custom_fields`
--

CREATE TABLE `wp_mailpoet_custom_fields` (
  `id` int UNSIGNED NOT NULL,
  `name` varchar(90) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `type` varchar(90) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `params` longtext COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_dynamic_segment_filters`
--

CREATE TABLE `wp_mailpoet_dynamic_segment_filters` (
  `id` int UNSIGNED NOT NULL,
  `segment_id` int UNSIGNED NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `filter_data` longblob,
  `filter_type` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `action` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_feature_flags`
--

CREATE TABLE `wp_mailpoet_feature_flags` (
  `id` int UNSIGNED NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `value` tinyint(1) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_forms`
--

CREATE TABLE `wp_mailpoet_forms` (
  `id` int UNSIGNED NOT NULL,
  `name` varchar(90) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'enabled',
  `body` longtext COLLATE utf8mb4_unicode_520_ci,
  `settings` longtext COLLATE utf8mb4_unicode_520_ci,
  `styles` longtext COLLATE utf8mb4_unicode_520_ci,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_log`
--

CREATE TABLE `wp_mailpoet_log` (
  `id` bigint UNSIGNED NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `level` int DEFAULT NULL,
  `message` longtext COLLATE utf8mb4_unicode_520_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `raw_message` longtext COLLATE utf8mb4_unicode_520_ci,
  `context` longtext COLLATE utf8mb4_unicode_520_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_migrations`
--

CREATE TABLE `wp_mailpoet_migrations` (
  `id` int UNSIGNED NOT NULL,
  `name` varchar(191) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `started_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `completed_at` timestamp NULL DEFAULT NULL,
  `retries` int UNSIGNED NOT NULL DEFAULT '0',
  `error` text COLLATE utf8mb4_unicode_520_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_newsletters`
--

CREATE TABLE `wp_mailpoet_newsletters` (
  `id` int UNSIGNED NOT NULL,
  `hash` varchar(150) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `parent_id` int UNSIGNED DEFAULT NULL,
  `subject` varchar(250) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `type` varchar(150) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'standard',
  `sender_address` varchar(150) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `sender_name` varchar(150) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `status` varchar(20) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'draft',
  `reply_to_address` varchar(150) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `reply_to_name` varchar(150) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `preheader` varchar(250) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `body` longtext COLLATE utf8mb4_unicode_520_ci,
  `sent_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL,
  `unsubscribe_token` char(15) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `ga_campaign` varchar(250) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `wp_post_id` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_newsletter_links`
--

CREATE TABLE `wp_mailpoet_newsletter_links` (
  `id` int UNSIGNED NOT NULL,
  `newsletter_id` int UNSIGNED NOT NULL,
  `queue_id` int UNSIGNED NOT NULL,
  `url` varchar(2083) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `hash` varchar(20) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_newsletter_option`
--

CREATE TABLE `wp_mailpoet_newsletter_option` (
  `id` int UNSIGNED NOT NULL,
  `newsletter_id` int UNSIGNED NOT NULL,
  `option_field_id` int UNSIGNED NOT NULL,
  `value` longtext COLLATE utf8mb4_unicode_520_ci,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_newsletter_option_fields`
--

CREATE TABLE `wp_mailpoet_newsletter_option_fields` (
  `id` int UNSIGNED NOT NULL,
  `name` varchar(90) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `newsletter_type` varchar(90) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_newsletter_posts`
--

CREATE TABLE `wp_mailpoet_newsletter_posts` (
  `id` int UNSIGNED NOT NULL,
  `newsletter_id` int UNSIGNED NOT NULL,
  `post_id` int UNSIGNED NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_newsletter_segment`
--

CREATE TABLE `wp_mailpoet_newsletter_segment` (
  `id` int UNSIGNED NOT NULL,
  `newsletter_id` int UNSIGNED NOT NULL,
  `segment_id` int UNSIGNED NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_newsletter_templates`
--

CREATE TABLE `wp_mailpoet_newsletter_templates` (
  `id` int UNSIGNED NOT NULL,
  `newsletter_id` int DEFAULT '0',
  `name` varchar(250) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `categories` varchar(250) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '[]',
  `description` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `body` longtext COLLATE utf8mb4_unicode_520_ci,
  `thumbnail` longtext COLLATE utf8mb4_unicode_520_ci,
  `thumbnail_data` longtext COLLATE utf8mb4_unicode_520_ci,
  `readonly` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_scheduled_tasks`
--

CREATE TABLE `wp_mailpoet_scheduled_tasks` (
  `id` int UNSIGNED NOT NULL,
  `type` varchar(90) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `status` varchar(12) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `priority` mediumint NOT NULL DEFAULT '0',
  `scheduled_at` timestamp NULL DEFAULT NULL,
  `processed_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL,
  `in_progress` int DEFAULT NULL,
  `reschedule_count` int NOT NULL DEFAULT '0',
  `meta` longtext COLLATE utf8mb4_unicode_520_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_scheduled_task_subscribers`
--

CREATE TABLE `wp_mailpoet_scheduled_task_subscribers` (
  `task_id` int UNSIGNED NOT NULL,
  `subscriber_id` int UNSIGNED NOT NULL,
  `processed` int NOT NULL,
  `failed` smallint NOT NULL DEFAULT '0',
  `error` text COLLATE utf8mb4_unicode_520_ci,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_segments`
--

CREATE TABLE `wp_mailpoet_segments` (
  `id` int UNSIGNED NOT NULL,
  `name` varchar(90) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `type` varchar(90) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'default',
  `description` varchar(250) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL,
  `average_engagement_score` float UNSIGNED DEFAULT NULL,
  `average_engagement_score_updated_at` timestamp NULL DEFAULT NULL,
  `display_in_manage_subscription_page` tinyint(1) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_sending_queues`
--

CREATE TABLE `wp_mailpoet_sending_queues` (
  `id` int UNSIGNED NOT NULL,
  `task_id` int UNSIGNED NOT NULL,
  `newsletter_id` int UNSIGNED DEFAULT NULL,
  `newsletter_rendered_body` longtext COLLATE utf8mb4_unicode_520_ci,
  `newsletter_rendered_subject` varchar(250) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `subscribers` longtext COLLATE utf8mb4_unicode_520_ci,
  `count_total` int UNSIGNED NOT NULL DEFAULT '0',
  `count_processed` int UNSIGNED NOT NULL DEFAULT '0',
  `count_to_process` int UNSIGNED NOT NULL DEFAULT '0',
  `meta` longtext COLLATE utf8mb4_unicode_520_ci,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_settings`
--

CREATE TABLE `wp_mailpoet_settings` (
  `id` int UNSIGNED NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `value` longtext COLLATE utf8mb4_unicode_520_ci,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_statistics_bounces`
--

CREATE TABLE `wp_mailpoet_statistics_bounces` (
  `id` int UNSIGNED NOT NULL,
  `newsletter_id` int UNSIGNED NOT NULL,
  `subscriber_id` int UNSIGNED NOT NULL,
  `queue_id` int UNSIGNED NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_statistics_clicks`
--

CREATE TABLE `wp_mailpoet_statistics_clicks` (
  `id` int UNSIGNED NOT NULL,
  `newsletter_id` int UNSIGNED NOT NULL,
  `subscriber_id` int UNSIGNED NOT NULL,
  `queue_id` int UNSIGNED NOT NULL,
  `link_id` int UNSIGNED NOT NULL,
  `user_agent_id` int UNSIGNED DEFAULT NULL,
  `user_agent_type` tinyint(1) NOT NULL DEFAULT '0',
  `count` int UNSIGNED NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_statistics_forms`
--

CREATE TABLE `wp_mailpoet_statistics_forms` (
  `id` int UNSIGNED NOT NULL,
  `form_id` int UNSIGNED NOT NULL,
  `subscriber_id` int UNSIGNED NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_statistics_newsletters`
--

CREATE TABLE `wp_mailpoet_statistics_newsletters` (
  `id` int UNSIGNED NOT NULL,
  `newsletter_id` int UNSIGNED NOT NULL,
  `subscriber_id` int UNSIGNED NOT NULL,
  `queue_id` int UNSIGNED NOT NULL,
  `sent_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_statistics_opens`
--

CREATE TABLE `wp_mailpoet_statistics_opens` (
  `id` int UNSIGNED NOT NULL,
  `newsletter_id` int UNSIGNED NOT NULL,
  `subscriber_id` int UNSIGNED NOT NULL,
  `queue_id` int UNSIGNED NOT NULL,
  `user_agent_id` int UNSIGNED DEFAULT NULL,
  `user_agent_type` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_statistics_unsubscribes`
--

CREATE TABLE `wp_mailpoet_statistics_unsubscribes` (
  `id` int UNSIGNED NOT NULL,
  `newsletter_id` int UNSIGNED DEFAULT NULL,
  `subscriber_id` int UNSIGNED NOT NULL,
  `queue_id` int UNSIGNED DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `source` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT 'unknown',
  `meta` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `method` varchar(40) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'unknown'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_statistics_woocommerce_purchases`
--

CREATE TABLE `wp_mailpoet_statistics_woocommerce_purchases` (
  `id` int UNSIGNED NOT NULL,
  `newsletter_id` int UNSIGNED DEFAULT NULL,
  `subscriber_id` int UNSIGNED NOT NULL,
  `queue_id` int UNSIGNED NOT NULL,
  `click_id` int UNSIGNED NOT NULL,
  `order_id` bigint UNSIGNED NOT NULL,
  `order_currency` char(3) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `order_price_total` float NOT NULL COMMENT 'With shipping and taxes in order_currency',
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` varchar(40) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'unknown'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_stats_notifications`
--

CREATE TABLE `wp_mailpoet_stats_notifications` (
  `id` int UNSIGNED NOT NULL,
  `newsletter_id` int UNSIGNED NOT NULL,
  `task_id` int UNSIGNED NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_subscribers`
--

CREATE TABLE `wp_mailpoet_subscribers` (
  `id` int UNSIGNED NOT NULL,
  `wp_user_id` bigint DEFAULT NULL,
  `is_woocommerce_user` int NOT NULL DEFAULT '0',
  `first_name` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `last_name` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `email` varchar(150) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `status` varchar(12) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'unconfirmed',
  `subscribed_ip` varchar(45) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `confirmed_ip` varchar(45) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `confirmed_at` timestamp NULL DEFAULT NULL,
  `last_subscribed_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL,
  `unconfirmed_data` longtext COLLATE utf8mb4_unicode_520_ci,
  `source` enum('form','imported','administrator','api','wordpress_user','woocommerce_user','woocommerce_checkout','unknown') COLLATE utf8mb4_unicode_520_ci DEFAULT 'unknown',
  `count_confirmations` int UNSIGNED NOT NULL DEFAULT '0',
  `unsubscribe_token` char(15) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `link_token` char(32) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `engagement_score` float UNSIGNED DEFAULT NULL,
  `engagement_score_updated_at` timestamp NULL DEFAULT NULL,
  `last_engagement_at` timestamp NULL DEFAULT NULL,
  `woocommerce_synced_at` timestamp NULL DEFAULT NULL,
  `email_count` int UNSIGNED NOT NULL DEFAULT '0',
  `last_sending_at` timestamp NULL DEFAULT NULL,
  `last_open_at` timestamp NULL DEFAULT NULL,
  `last_click_at` timestamp NULL DEFAULT NULL,
  `last_purchase_at` timestamp NULL DEFAULT NULL,
  `last_page_view_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_subscriber_custom_field`
--

CREATE TABLE `wp_mailpoet_subscriber_custom_field` (
  `id` int UNSIGNED NOT NULL,
  `subscriber_id` int UNSIGNED NOT NULL,
  `custom_field_id` int UNSIGNED NOT NULL,
  `value` text COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_subscriber_ips`
--

CREATE TABLE `wp_mailpoet_subscriber_ips` (
  `ip` varchar(45) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_subscriber_segment`
--

CREATE TABLE `wp_mailpoet_subscriber_segment` (
  `id` int UNSIGNED NOT NULL,
  `subscriber_id` int UNSIGNED NOT NULL,
  `segment_id` int UNSIGNED NOT NULL,
  `status` varchar(12) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'subscribed',
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_subscriber_tag`
--

CREATE TABLE `wp_mailpoet_subscriber_tag` (
  `id` int UNSIGNED NOT NULL,
  `subscriber_id` int UNSIGNED NOT NULL,
  `tag_id` int UNSIGNED NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_tags`
--

CREATE TABLE `wp_mailpoet_tags` (
  `id` int UNSIGNED NOT NULL,
  `name` varchar(191) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_user_agents`
--

CREATE TABLE `wp_mailpoet_user_agents` (
  `id` int UNSIGNED NOT NULL,
  `hash` varchar(32) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `user_agent` text COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_mailpoet_user_flags`
--

CREATE TABLE `wp_mailpoet_user_flags` (
  `id` int UNSIGNED NOT NULL,
  `user_id` bigint NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `value` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_nf3_actions`
--

CREATE TABLE `wp_nf3_actions` (
  `id` int NOT NULL,
  `title` longtext COLLATE utf8mb4_general_ci,
  `key` longtext COLLATE utf8mb4_general_ci,
  `type` longtext COLLATE utf8mb4_general_ci,
  `active` tinyint(1) DEFAULT '1',
  `parent_id` int NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `label` longtext COLLATE utf8mb4_general_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_nf3_action_meta`
--

CREATE TABLE `wp_nf3_action_meta` (
  `id` int NOT NULL,
  `parent_id` int NOT NULL,
  `key` longtext COLLATE utf8mb4_general_ci NOT NULL,
  `value` longtext COLLATE utf8mb4_general_ci,
  `meta_key` longtext COLLATE utf8mb4_general_ci,
  `meta_value` longtext COLLATE utf8mb4_general_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_nf3_chunks`
--

CREATE TABLE `wp_nf3_chunks` (
  `id` int NOT NULL,
  `name` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `value` longtext COLLATE utf8mb4_general_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_nf3_fields`
--

CREATE TABLE `wp_nf3_fields` (
  `id` int NOT NULL,
  `label` longtext COLLATE utf8mb4_general_ci,
  `key` longtext COLLATE utf8mb4_general_ci,
  `type` longtext COLLATE utf8mb4_general_ci,
  `parent_id` int NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `field_label` longtext COLLATE utf8mb4_general_ci,
  `field_key` longtext COLLATE utf8mb4_general_ci,
  `order` int DEFAULT NULL,
  `required` bit(1) DEFAULT NULL,
  `default_value` longtext COLLATE utf8mb4_general_ci,
  `label_pos` varchar(15) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `personally_identifiable` bit(1) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_nf3_field_meta`
--

CREATE TABLE `wp_nf3_field_meta` (
  `id` int NOT NULL,
  `parent_id` int NOT NULL,
  `key` longtext COLLATE utf8mb4_general_ci NOT NULL,
  `value` longtext COLLATE utf8mb4_general_ci,
  `meta_key` longtext COLLATE utf8mb4_general_ci,
  `meta_value` longtext COLLATE utf8mb4_general_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_nf3_forms`
--

CREATE TABLE `wp_nf3_forms` (
  `id` int NOT NULL,
  `title` longtext COLLATE utf8mb4_general_ci,
  `key` longtext COLLATE utf8mb4_general_ci,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `views` int DEFAULT NULL,
  `subs` int DEFAULT NULL,
  `form_title` longtext COLLATE utf8mb4_general_ci,
  `default_label_pos` varchar(15) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `show_title` bit(1) DEFAULT NULL,
  `clear_complete` bit(1) DEFAULT NULL,
  `hide_complete` bit(1) DEFAULT NULL,
  `logged_in` bit(1) DEFAULT NULL,
  `seq_num` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_nf3_form_meta`
--

CREATE TABLE `wp_nf3_form_meta` (
  `id` int NOT NULL,
  `parent_id` int NOT NULL,
  `key` longtext COLLATE utf8mb4_general_ci NOT NULL,
  `value` longtext COLLATE utf8mb4_general_ci,
  `meta_key` longtext COLLATE utf8mb4_general_ci,
  `meta_value` longtext COLLATE utf8mb4_general_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_nf3_objects`
--

CREATE TABLE `wp_nf3_objects` (
  `id` int NOT NULL,
  `type` longtext COLLATE utf8mb4_general_ci,
  `title` longtext COLLATE utf8mb4_general_ci,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `object_title` longtext COLLATE utf8mb4_general_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_nf3_object_meta`
--

CREATE TABLE `wp_nf3_object_meta` (
  `id` int NOT NULL,
  `parent_id` int NOT NULL,
  `key` longtext COLLATE utf8mb4_general_ci NOT NULL,
  `value` longtext COLLATE utf8mb4_general_ci,
  `meta_key` longtext COLLATE utf8mb4_general_ci,
  `meta_value` longtext COLLATE utf8mb4_general_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_nf3_relationships`
--

CREATE TABLE `wp_nf3_relationships` (
  `id` int NOT NULL,
  `child_id` int NOT NULL,
  `child_type` longtext COLLATE utf8mb4_general_ci NOT NULL,
  `parent_id` int NOT NULL,
  `parent_type` longtext COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_nf3_upgrades`
--

CREATE TABLE `wp_nf3_upgrades` (
  `id` int NOT NULL,
  `cache` longtext COLLATE utf8mb4_general_ci,
  `stage` int NOT NULL DEFAULT '0',
  `maintenance` bit(1) DEFAULT b'0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_options`
--

CREATE TABLE `wp_options` (
  `option_id` bigint UNSIGNED NOT NULL,
  `option_name` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `option_value` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `autoload` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'yes'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_pieregister_code`
--

CREATE TABLE `wp_pieregister_code` (
  `id` int NOT NULL,
  `created` date NOT NULL,
  `modified` date NOT NULL,
  `name` text NOT NULL,
  `count` int NOT NULL,
  `status` int NOT NULL,
  `code_usage` int NOT NULL,
  `expiry_date` date NOT NULL,
  `code_description` varchar(255) DEFAULT NULL,
  `code_user_role` varchar(255) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_pieregister_custom_user_roles`
--

CREATE TABLE `wp_pieregister_custom_user_roles` (
  `id` int NOT NULL,
  `role_key` varchar(150) NOT NULL,
  `role_name` varchar(150) NOT NULL,
  `wp_role_name` varchar(150) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_pieregister_invite_code_emails`
--

CREATE TABLE `wp_pieregister_invite_code_emails` (
  `id` int NOT NULL,
  `code_id` int NOT NULL,
  `email_address` varchar(150) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_pieregister_lockdowns`
--

CREATE TABLE `wp_pieregister_lockdowns` (
  `id` int NOT NULL,
  `user_id` int NOT NULL,
  `login_attempt` int NOT NULL,
  `attempt_from` varchar(56) NOT NULL,
  `is_security_captcha` tinyint NOT NULL DEFAULT '0',
  `attempt_time` datetime NOT NULL,
  `release_time` datetime NOT NULL,
  `user_ip` varchar(30) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `wp_pieregister_redirect_settings`
--

CREATE TABLE `wp_pieregister_redirect_settings` (
  `id` int NOT NULL,
  `user_role` varchar(100) NOT NULL,
  `logged_in_url` text NOT NULL,
  `logged_in_page_id` int NOT NULL,
  `log_out_url` text NOT NULL,
  `log_out_page_id` int NOT NULL,
  `status` bit(1) NOT NULL DEFAULT b'1'
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `wp_pmpro_discount_codes`
--

CREATE TABLE `wp_pmpro_discount_codes` (
  `id` bigint UNSIGNED NOT NULL,
  `code` varchar(32) NOT NULL,
  `starts` date NOT NULL,
  `expires` date NOT NULL,
  `uses` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_pmpro_discount_codes_levels`
--

CREATE TABLE `wp_pmpro_discount_codes_levels` (
  `code_id` bigint UNSIGNED NOT NULL,
  `level_id` int UNSIGNED NOT NULL,
  `initial_payment` decimal(18,8) NOT NULL DEFAULT '0.00000000',
  `billing_amount` decimal(18,8) NOT NULL DEFAULT '0.00000000',
  `cycle_number` int NOT NULL DEFAULT '0',
  `cycle_period` varchar(10) DEFAULT 'Month',
  `billing_limit` int NOT NULL COMMENT 'After how many cycles should billing stop?',
  `trial_amount` decimal(18,8) NOT NULL DEFAULT '0.00000000',
  `trial_limit` int NOT NULL DEFAULT '0',
  `expiration_number` int UNSIGNED NOT NULL,
  `expiration_period` varchar(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_pmpro_discount_codes_uses`
--

CREATE TABLE `wp_pmpro_discount_codes_uses` (
  `id` bigint UNSIGNED NOT NULL,
  `code_id` bigint UNSIGNED NOT NULL,
  `user_id` bigint UNSIGNED NOT NULL,
  `order_id` bigint UNSIGNED NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_pmpro_memberships_categories`
--

CREATE TABLE `wp_pmpro_memberships_categories` (
  `membership_id` int UNSIGNED NOT NULL,
  `category_id` bigint UNSIGNED NOT NULL,
  `modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_pmpro_memberships_pages`
--

CREATE TABLE `wp_pmpro_memberships_pages` (
  `membership_id` int UNSIGNED NOT NULL,
  `page_id` bigint UNSIGNED NOT NULL,
  `modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_pmpro_memberships_users`
--

CREATE TABLE `wp_pmpro_memberships_users` (
  `id` bigint UNSIGNED NOT NULL,
  `user_id` bigint UNSIGNED NOT NULL,
  `membership_id` int UNSIGNED NOT NULL,
  `code_id` bigint UNSIGNED NOT NULL,
  `initial_payment` decimal(18,8) NOT NULL,
  `billing_amount` decimal(18,8) NOT NULL,
  `cycle_number` int NOT NULL,
  `cycle_period` varchar(10) NOT NULL DEFAULT 'Month',
  `billing_limit` int NOT NULL,
  `trial_amount` decimal(18,8) NOT NULL,
  `trial_limit` int NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'active',
  `startdate` datetime NOT NULL,
  `enddate` datetime DEFAULT NULL,
  `modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_pmpro_membership_levelmeta`
--

CREATE TABLE `wp_pmpro_membership_levelmeta` (
  `meta_id` bigint UNSIGNED NOT NULL,
  `pmpro_membership_level_id` int UNSIGNED NOT NULL,
  `meta_key` varchar(255) NOT NULL,
  `meta_value` longtext
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_pmpro_membership_levels`
--

CREATE TABLE `wp_pmpro_membership_levels` (
  `id` int UNSIGNED NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` longtext NOT NULL,
  `confirmation` longtext NOT NULL,
  `initial_payment` decimal(18,8) NOT NULL DEFAULT '0.00000000',
  `billing_amount` decimal(18,8) NOT NULL DEFAULT '0.00000000',
  `cycle_number` int NOT NULL DEFAULT '0',
  `cycle_period` varchar(10) DEFAULT 'Month',
  `billing_limit` int NOT NULL COMMENT 'After how many cycles should billing stop?',
  `trial_amount` decimal(18,8) NOT NULL DEFAULT '0.00000000',
  `trial_limit` int NOT NULL DEFAULT '0',
  `allow_signups` tinyint NOT NULL DEFAULT '1',
  `expiration_number` int UNSIGNED NOT NULL,
  `expiration_period` varchar(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_pmpro_membership_ordermeta`
--

CREATE TABLE `wp_pmpro_membership_ordermeta` (
  `meta_id` bigint UNSIGNED NOT NULL,
  `pmpro_membership_order_id` int UNSIGNED NOT NULL,
  `meta_key` varchar(255) NOT NULL,
  `meta_value` longtext
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_pmpro_membership_orders`
--

CREATE TABLE `wp_pmpro_membership_orders` (
  `id` bigint UNSIGNED NOT NULL,
  `code` varchar(32) NOT NULL,
  `session_id` varchar(64) NOT NULL DEFAULT '',
  `user_id` bigint UNSIGNED NOT NULL DEFAULT '0',
  `membership_id` int UNSIGNED NOT NULL DEFAULT '0',
  `paypal_token` varchar(64) NOT NULL DEFAULT '',
  `billing_name` varchar(128) NOT NULL DEFAULT '',
  `billing_street` varchar(128) NOT NULL DEFAULT '',
  `billing_city` varchar(128) NOT NULL DEFAULT '',
  `billing_state` varchar(32) NOT NULL DEFAULT '',
  `billing_zip` varchar(16) NOT NULL DEFAULT '',
  `billing_country` varchar(128) NOT NULL,
  `billing_phone` varchar(32) NOT NULL,
  `subtotal` varchar(16) NOT NULL DEFAULT '',
  `tax` varchar(16) NOT NULL DEFAULT '',
  `couponamount` varchar(16) NOT NULL DEFAULT '',
  `checkout_id` bigint NOT NULL DEFAULT '0',
  `certificate_id` int NOT NULL DEFAULT '0',
  `certificateamount` varchar(16) NOT NULL DEFAULT '',
  `total` varchar(16) NOT NULL DEFAULT '',
  `payment_type` varchar(64) NOT NULL DEFAULT '',
  `cardtype` varchar(32) NOT NULL DEFAULT '',
  `accountnumber` varchar(32) NOT NULL DEFAULT '',
  `expirationmonth` char(2) NOT NULL DEFAULT '',
  `expirationyear` varchar(4) NOT NULL DEFAULT '',
  `status` varchar(32) NOT NULL DEFAULT '',
  `gateway` varchar(64) NOT NULL,
  `gateway_environment` varchar(64) NOT NULL,
  `payment_transaction_id` varchar(64) NOT NULL,
  `subscription_transaction_id` varchar(32) NOT NULL,
  `timestamp` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `affiliate_id` varchar(32) NOT NULL,
  `affiliate_subid` varchar(32) NOT NULL,
  `notes` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_postmeta`
--

CREATE TABLE `wp_postmeta` (
  `meta_id` bigint UNSIGNED NOT NULL,
  `post_id` bigint UNSIGNED NOT NULL DEFAULT '0',
  `meta_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `meta_value` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_posts`
--

CREATE TABLE `wp_posts` (
  `ID` bigint UNSIGNED NOT NULL,
  `post_author` bigint UNSIGNED NOT NULL DEFAULT '0',
  `post_date` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `post_date_gmt` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `post_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `post_title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `post_excerpt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `post_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'publish',
  `comment_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'open',
  `ping_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'open',
  `post_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `post_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `to_ping` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `pinged` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `post_modified` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `post_modified_gmt` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `post_content_filtered` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `post_parent` bigint UNSIGNED NOT NULL DEFAULT '0',
  `guid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `menu_order` int NOT NULL DEFAULT '0',
  `post_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'post',
  `post_mime_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `comment_count` bigint NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_swpm_membership_meta_tbl`
--

CREATE TABLE `wp_swpm_membership_meta_tbl` (
  `id` int NOT NULL,
  `level_id` int NOT NULL,
  `meta_key` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `meta_label` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `meta_value` text COLLATE utf8mb4_unicode_520_ci,
  `meta_type` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'text',
  `meta_default` text COLLATE utf8mb4_unicode_520_ci,
  `meta_context` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'default'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_swpm_membership_tbl`
--

CREATE TABLE `wp_swpm_membership_tbl` (
  `id` int NOT NULL,
  `alias` varchar(127) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `role` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'subscriber',
  `permissions` tinyint NOT NULL DEFAULT '0',
  `subscription_period` varchar(11) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '-1',
  `subscription_duration_type` tinyint NOT NULL DEFAULT '0',
  `subscription_unit` varchar(20) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `loginredirect_page` text COLLATE utf8mb4_unicode_520_ci,
  `category_list` longtext COLLATE utf8mb4_unicode_520_ci,
  `page_list` longtext COLLATE utf8mb4_unicode_520_ci,
  `post_list` longtext COLLATE utf8mb4_unicode_520_ci,
  `comment_list` longtext COLLATE utf8mb4_unicode_520_ci,
  `attachment_list` longtext COLLATE utf8mb4_unicode_520_ci,
  `custom_post_list` longtext COLLATE utf8mb4_unicode_520_ci,
  `disable_bookmark_list` longtext COLLATE utf8mb4_unicode_520_ci,
  `options` longtext COLLATE utf8mb4_unicode_520_ci,
  `protect_older_posts` tinyint(1) NOT NULL DEFAULT '0',
  `campaign_name` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_swpm_members_tbl`
--

CREATE TABLE `wp_swpm_members_tbl` (
  `member_id` int NOT NULL,
  `user_name` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `first_name` varchar(64) COLLATE utf8mb4_unicode_520_ci DEFAULT '',
  `last_name` varchar(64) COLLATE utf8mb4_unicode_520_ci DEFAULT '',
  `password` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `member_since` date NOT NULL DEFAULT '0000-00-00',
  `membership_level` smallint NOT NULL,
  `more_membership_levels` varchar(100) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `account_state` enum('active','inactive','activation_required','expired','pending','unsubscribed') COLLATE utf8mb4_unicode_520_ci DEFAULT 'pending',
  `last_accessed` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `last_accessed_from_ip` varchar(128) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `phone` varchar(64) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `address_street` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `address_city` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `address_state` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `address_zipcode` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `home_page` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `country` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `gender` enum('male','female','not specified') COLLATE utf8mb4_unicode_520_ci DEFAULT 'not specified',
  `referrer` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `extra_info` text COLLATE utf8mb4_unicode_520_ci,
  `reg_code` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `subscription_starts` date DEFAULT NULL,
  `initial_membership_level` smallint DEFAULT NULL,
  `txn_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT '',
  `subscr_id` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT '',
  `company_name` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT '',
  `notes` text COLLATE utf8mb4_unicode_520_ci,
  `flags` int DEFAULT '0',
  `profile_image` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_swpm_payments_tbl`
--

CREATE TABLE `wp_swpm_payments_tbl` (
  `id` int NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `first_name` varchar(64) COLLATE utf8mb4_unicode_520_ci DEFAULT '',
  `last_name` varchar(64) COLLATE utf8mb4_unicode_520_ci DEFAULT '',
  `member_id` varchar(16) COLLATE utf8mb4_unicode_520_ci DEFAULT '',
  `membership_level` varchar(64) COLLATE utf8mb4_unicode_520_ci DEFAULT '',
  `txn_date` date NOT NULL DEFAULT '0000-00-00',
  `txn_id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `subscr_id` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `reference` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `payment_amount` varchar(32) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `gateway` varchar(32) COLLATE utf8mb4_unicode_520_ci DEFAULT '',
  `status` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT '',
  `ip_address` varchar(128) COLLATE utf8mb4_unicode_520_ci DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_termmeta`
--

CREATE TABLE `wp_termmeta` (
  `meta_id` bigint UNSIGNED NOT NULL,
  `term_id` bigint UNSIGNED NOT NULL DEFAULT '0',
  `meta_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `meta_value` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_terms`
--

CREATE TABLE `wp_terms` (
  `term_id` bigint UNSIGNED NOT NULL,
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `slug` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `term_group` bigint NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_term_relationships`
--

CREATE TABLE `wp_term_relationships` (
  `object_id` bigint UNSIGNED NOT NULL DEFAULT '0',
  `term_taxonomy_id` bigint UNSIGNED NOT NULL DEFAULT '0',
  `term_order` int NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_term_taxonomy`
--

CREATE TABLE `wp_term_taxonomy` (
  `term_taxonomy_id` bigint UNSIGNED NOT NULL,
  `term_id` bigint UNSIGNED NOT NULL DEFAULT '0',
  `taxonomy` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `description` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `parent` bigint UNSIGNED NOT NULL DEFAULT '0',
  `count` bigint NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_usermeta`
--

CREATE TABLE `wp_usermeta` (
  `umeta_id` bigint UNSIGNED NOT NULL,
  `user_id` bigint UNSIGNED NOT NULL DEFAULT '0',
  `meta_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `meta_value` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_users`
--

CREATE TABLE `wp_users` (
  `ID` bigint UNSIGNED NOT NULL,
  `user_login` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `user_pass` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `user_nicename` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `user_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `user_url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `user_registered` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `user_activation_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `user_status` int NOT NULL DEFAULT '0',
  `display_name` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `app_user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `app_code` varchar(50) COLLATE utf8mb4_unicode_520_ci DEFAULT 'ezami'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wc_admin_notes`
--

CREATE TABLE `wp_wc_admin_notes` (
  `note_id` bigint UNSIGNED NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `locale` varchar(20) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `title` longtext COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `content` longtext COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `content_data` longtext COLLATE utf8mb4_unicode_520_ci,
  `status` varchar(200) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `source` varchar(200) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `date_created` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `date_reminder` datetime DEFAULT NULL,
  `is_snoozable` tinyint(1) NOT NULL DEFAULT '0',
  `layout` varchar(20) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `image` varchar(200) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `is_read` tinyint(1) NOT NULL DEFAULT '0',
  `icon` varchar(200) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT 'info'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wc_admin_note_actions`
--

CREATE TABLE `wp_wc_admin_note_actions` (
  `action_id` bigint UNSIGNED NOT NULL,
  `note_id` bigint UNSIGNED NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `label` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `query` longtext COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `actioned_text` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `nonce_action` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `nonce_name` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wc_category_lookup`
--

CREATE TABLE `wp_wc_category_lookup` (
  `category_tree_id` bigint UNSIGNED NOT NULL,
  `category_id` bigint UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wc_customer_lookup`
--

CREATE TABLE `wp_wc_customer_lookup` (
  `customer_id` bigint UNSIGNED NOT NULL,
  `user_id` bigint UNSIGNED DEFAULT NULL,
  `username` varchar(60) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `first_name` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `last_name` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `date_last_active` timestamp NULL DEFAULT NULL,
  `date_registered` timestamp NULL DEFAULT NULL,
  `country` char(2) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `postcode` varchar(20) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `city` varchar(100) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `state` varchar(100) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wc_download_log`
--

CREATE TABLE `wp_wc_download_log` (
  `download_log_id` bigint UNSIGNED NOT NULL,
  `timestamp` datetime NOT NULL,
  `permission_id` bigint UNSIGNED NOT NULL,
  `user_id` bigint UNSIGNED DEFAULT NULL,
  `user_ip_address` varchar(100) COLLATE utf8mb4_unicode_520_ci DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wc_orders`
--

CREATE TABLE `wp_wc_orders` (
  `id` bigint UNSIGNED NOT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `currency` varchar(10) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `type` varchar(20) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `tax_amount` decimal(26,8) DEFAULT NULL,
  `total_amount` decimal(26,8) DEFAULT NULL,
  `customer_id` bigint UNSIGNED DEFAULT NULL,
  `billing_email` varchar(320) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `date_created_gmt` datetime DEFAULT NULL,
  `date_updated_gmt` datetime DEFAULT NULL,
  `parent_order_id` bigint UNSIGNED DEFAULT NULL,
  `payment_method` varchar(100) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `payment_method_title` text COLLATE utf8mb4_unicode_520_ci,
  `transaction_id` varchar(100) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `ip_address` varchar(100) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `user_agent` text COLLATE utf8mb4_unicode_520_ci,
  `customer_note` text COLLATE utf8mb4_unicode_520_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wc_orders_meta`
--

CREATE TABLE `wp_wc_orders_meta` (
  `id` bigint UNSIGNED NOT NULL,
  `order_id` bigint UNSIGNED DEFAULT NULL,
  `meta_key` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `meta_value` text COLLATE utf8mb4_unicode_520_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wc_order_addresses`
--

CREATE TABLE `wp_wc_order_addresses` (
  `id` bigint UNSIGNED NOT NULL,
  `order_id` bigint UNSIGNED NOT NULL,
  `address_type` varchar(20) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `first_name` text COLLATE utf8mb4_unicode_520_ci,
  `last_name` text COLLATE utf8mb4_unicode_520_ci,
  `company` text COLLATE utf8mb4_unicode_520_ci,
  `address_1` text COLLATE utf8mb4_unicode_520_ci,
  `address_2` text COLLATE utf8mb4_unicode_520_ci,
  `city` text COLLATE utf8mb4_unicode_520_ci,
  `state` text COLLATE utf8mb4_unicode_520_ci,
  `postcode` text COLLATE utf8mb4_unicode_520_ci,
  `country` text COLLATE utf8mb4_unicode_520_ci,
  `email` varchar(320) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `phone` varchar(100) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wc_order_coupon_lookup`
--

CREATE TABLE `wp_wc_order_coupon_lookup` (
  `order_id` bigint UNSIGNED NOT NULL,
  `coupon_id` bigint NOT NULL,
  `date_created` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `discount_amount` double NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wc_order_operational_data`
--

CREATE TABLE `wp_wc_order_operational_data` (
  `id` bigint UNSIGNED NOT NULL,
  `order_id` bigint UNSIGNED DEFAULT NULL,
  `created_via` varchar(100) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `woocommerce_version` varchar(20) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `prices_include_tax` tinyint(1) DEFAULT NULL,
  `coupon_usages_are_counted` tinyint(1) DEFAULT NULL,
  `download_permission_granted` tinyint(1) DEFAULT NULL,
  `cart_hash` varchar(100) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `new_order_email_sent` tinyint(1) DEFAULT NULL,
  `order_key` varchar(100) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `order_stock_reduced` tinyint(1) DEFAULT NULL,
  `date_paid_gmt` datetime DEFAULT NULL,
  `date_completed_gmt` datetime DEFAULT NULL,
  `shipping_tax_amount` decimal(26,8) DEFAULT NULL,
  `shipping_total_amount` decimal(26,8) DEFAULT NULL,
  `discount_tax_amount` decimal(26,8) DEFAULT NULL,
  `discount_total_amount` decimal(26,8) DEFAULT NULL,
  `recorded_sales` tinyint(1) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wc_order_product_lookup`
--

CREATE TABLE `wp_wc_order_product_lookup` (
  `order_item_id` bigint UNSIGNED NOT NULL,
  `order_id` bigint UNSIGNED NOT NULL,
  `product_id` bigint UNSIGNED NOT NULL,
  `variation_id` bigint UNSIGNED NOT NULL,
  `customer_id` bigint UNSIGNED DEFAULT NULL,
  `date_created` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `product_qty` int NOT NULL,
  `product_net_revenue` double NOT NULL DEFAULT '0',
  `product_gross_revenue` double NOT NULL DEFAULT '0',
  `coupon_amount` double NOT NULL DEFAULT '0',
  `tax_amount` double NOT NULL DEFAULT '0',
  `shipping_amount` double NOT NULL DEFAULT '0',
  `shipping_tax_amount` double NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wc_order_stats`
--

CREATE TABLE `wp_wc_order_stats` (
  `order_id` bigint UNSIGNED NOT NULL,
  `parent_id` bigint UNSIGNED NOT NULL DEFAULT '0',
  `date_created` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `date_created_gmt` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `date_paid` datetime DEFAULT '0000-00-00 00:00:00',
  `date_completed` datetime DEFAULT '0000-00-00 00:00:00',
  `num_items_sold` int NOT NULL DEFAULT '0',
  `total_sales` double NOT NULL DEFAULT '0',
  `tax_total` double NOT NULL DEFAULT '0',
  `shipping_total` double NOT NULL DEFAULT '0',
  `net_total` double NOT NULL DEFAULT '0',
  `returning_customer` tinyint(1) DEFAULT NULL,
  `status` varchar(200) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `customer_id` bigint UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wc_order_tax_lookup`
--

CREATE TABLE `wp_wc_order_tax_lookup` (
  `order_id` bigint UNSIGNED NOT NULL,
  `tax_rate_id` bigint UNSIGNED NOT NULL,
  `date_created` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `shipping_tax` double NOT NULL DEFAULT '0',
  `order_tax` double NOT NULL DEFAULT '0',
  `total_tax` double NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wc_product_attributes_lookup`
--

CREATE TABLE `wp_wc_product_attributes_lookup` (
  `product_id` bigint NOT NULL,
  `product_or_parent_id` bigint NOT NULL,
  `taxonomy` varchar(32) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `term_id` bigint NOT NULL,
  `is_variation_attribute` tinyint(1) NOT NULL,
  `in_stock` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wc_product_download_directories`
--

CREATE TABLE `wp_wc_product_download_directories` (
  `url_id` bigint UNSIGNED NOT NULL,
  `url` varchar(256) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wc_product_meta_lookup`
--

CREATE TABLE `wp_wc_product_meta_lookup` (
  `product_id` bigint NOT NULL,
  `sku` varchar(100) COLLATE utf8mb4_unicode_520_ci DEFAULT '',
  `virtual` tinyint(1) DEFAULT '0',
  `downloadable` tinyint(1) DEFAULT '0',
  `min_price` decimal(19,4) DEFAULT NULL,
  `max_price` decimal(19,4) DEFAULT NULL,
  `onsale` tinyint(1) DEFAULT '0',
  `stock_quantity` double DEFAULT NULL,
  `stock_status` varchar(100) COLLATE utf8mb4_unicode_520_ci DEFAULT 'instock',
  `rating_count` bigint DEFAULT '0',
  `average_rating` decimal(3,2) DEFAULT '0.00',
  `total_sales` bigint DEFAULT '0',
  `tax_status` varchar(100) COLLATE utf8mb4_unicode_520_ci DEFAULT 'taxable',
  `tax_class` varchar(100) COLLATE utf8mb4_unicode_520_ci DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wc_rate_limits`
--

CREATE TABLE `wp_wc_rate_limits` (
  `rate_limit_id` bigint UNSIGNED NOT NULL,
  `rate_limit_key` varchar(200) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `rate_limit_expiry` bigint UNSIGNED NOT NULL,
  `rate_limit_remaining` smallint NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wc_reserved_stock`
--

CREATE TABLE `wp_wc_reserved_stock` (
  `order_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `stock_quantity` double NOT NULL DEFAULT '0',
  `timestamp` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `expires` datetime NOT NULL DEFAULT '0000-00-00 00:00:00'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wc_tax_rate_classes`
--

CREATE TABLE `wp_wc_tax_rate_classes` (
  `tax_rate_class_id` bigint UNSIGNED NOT NULL,
  `name` varchar(200) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `slug` varchar(200) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wc_webhooks`
--

CREATE TABLE `wp_wc_webhooks` (
  `webhook_id` bigint UNSIGNED NOT NULL,
  `status` varchar(200) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `name` text COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `user_id` bigint UNSIGNED NOT NULL,
  `delivery_url` text COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `secret` text COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `topic` varchar(200) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `date_created` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `date_created_gmt` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `date_modified` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `date_modified_gmt` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `api_version` smallint NOT NULL,
  `failure_count` smallint NOT NULL DEFAULT '0',
  `pending_delivery` tinyint(1) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_woocommerce_api_keys`
--

CREATE TABLE `wp_woocommerce_api_keys` (
  `key_id` bigint UNSIGNED NOT NULL,
  `user_id` bigint UNSIGNED NOT NULL,
  `description` varchar(200) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `permissions` varchar(10) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `consumer_key` char(64) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `consumer_secret` char(43) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `nonces` longtext COLLATE utf8mb4_unicode_520_ci,
  `truncated_key` char(7) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `last_access` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_woocommerce_attribute_taxonomies`
--

CREATE TABLE `wp_woocommerce_attribute_taxonomies` (
  `attribute_id` bigint UNSIGNED NOT NULL,
  `attribute_name` varchar(200) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `attribute_label` varchar(200) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `attribute_type` varchar(20) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `attribute_orderby` varchar(20) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `attribute_public` int NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_woocommerce_downloadable_product_permissions`
--

CREATE TABLE `wp_woocommerce_downloadable_product_permissions` (
  `permission_id` bigint UNSIGNED NOT NULL,
  `download_id` varchar(36) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `product_id` bigint UNSIGNED NOT NULL,
  `order_id` bigint UNSIGNED NOT NULL DEFAULT '0',
  `order_key` varchar(200) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `user_email` varchar(200) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `user_id` bigint UNSIGNED DEFAULT NULL,
  `downloads_remaining` varchar(9) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `access_granted` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `access_expires` datetime DEFAULT NULL,
  `download_count` bigint UNSIGNED NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_woocommerce_log`
--

CREATE TABLE `wp_woocommerce_log` (
  `log_id` bigint UNSIGNED NOT NULL,
  `timestamp` datetime NOT NULL,
  `level` smallint NOT NULL,
  `source` varchar(200) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `message` longtext COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `context` longtext COLLATE utf8mb4_unicode_520_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_woocommerce_order_itemmeta`
--

CREATE TABLE `wp_woocommerce_order_itemmeta` (
  `meta_id` bigint UNSIGNED NOT NULL,
  `order_item_id` bigint UNSIGNED NOT NULL,
  `meta_key` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `meta_value` longtext COLLATE utf8mb4_unicode_520_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_woocommerce_order_items`
--

CREATE TABLE `wp_woocommerce_order_items` (
  `order_item_id` bigint UNSIGNED NOT NULL,
  `order_item_name` text COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `order_item_type` varchar(200) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `order_id` bigint UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_woocommerce_payment_tokenmeta`
--

CREATE TABLE `wp_woocommerce_payment_tokenmeta` (
  `meta_id` bigint UNSIGNED NOT NULL,
  `payment_token_id` bigint UNSIGNED NOT NULL,
  `meta_key` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `meta_value` longtext COLLATE utf8mb4_unicode_520_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_woocommerce_payment_tokens`
--

CREATE TABLE `wp_woocommerce_payment_tokens` (
  `token_id` bigint UNSIGNED NOT NULL,
  `gateway_id` varchar(200) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `token` text COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `user_id` bigint UNSIGNED NOT NULL DEFAULT '0',
  `type` varchar(200) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `is_default` tinyint(1) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_woocommerce_sessions`
--

CREATE TABLE `wp_woocommerce_sessions` (
  `session_id` bigint UNSIGNED NOT NULL,
  `session_key` char(32) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `session_value` longtext COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `session_expiry` bigint UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_woocommerce_shipping_zones`
--

CREATE TABLE `wp_woocommerce_shipping_zones` (
  `zone_id` bigint UNSIGNED NOT NULL,
  `zone_name` varchar(200) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `zone_order` bigint UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_woocommerce_shipping_zone_locations`
--

CREATE TABLE `wp_woocommerce_shipping_zone_locations` (
  `location_id` bigint UNSIGNED NOT NULL,
  `zone_id` bigint UNSIGNED NOT NULL,
  `location_code` varchar(200) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `location_type` varchar(40) COLLATE utf8mb4_unicode_520_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_woocommerce_shipping_zone_methods`
--

CREATE TABLE `wp_woocommerce_shipping_zone_methods` (
  `zone_id` bigint UNSIGNED NOT NULL,
  `instance_id` bigint UNSIGNED NOT NULL,
  `method_id` varchar(200) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `method_order` bigint UNSIGNED NOT NULL,
  `is_enabled` tinyint(1) NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_woocommerce_tax_rates`
--

CREATE TABLE `wp_woocommerce_tax_rates` (
  `tax_rate_id` bigint UNSIGNED NOT NULL,
  `tax_rate_country` varchar(2) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `tax_rate_state` varchar(200) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `tax_rate` varchar(8) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `tax_rate_name` varchar(200) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `tax_rate_priority` bigint UNSIGNED NOT NULL,
  `tax_rate_compound` int NOT NULL DEFAULT '0',
  `tax_rate_shipping` int NOT NULL DEFAULT '1',
  `tax_rate_order` bigint UNSIGNED NOT NULL,
  `tax_rate_class` varchar(200) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_woocommerce_tax_rate_locations`
--

CREATE TABLE `wp_woocommerce_tax_rate_locations` (
  `location_id` bigint UNSIGNED NOT NULL,
  `location_code` varchar(200) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `tax_rate_id` bigint UNSIGNED NOT NULL,
  `location_type` varchar(40) COLLATE utf8mb4_unicode_520_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wpforms_logs`
--

CREATE TABLE `wp_wpforms_logs` (
  `id` bigint NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `message` longtext COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `types` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `create_at` datetime NOT NULL,
  `form_id` bigint DEFAULT NULL,
  `entry_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wpforms_payments`
--

CREATE TABLE `wp_wpforms_payments` (
  `id` bigint NOT NULL,
  `form_id` bigint NOT NULL,
  `status` varchar(10) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `subtotal_amount` decimal(26,8) NOT NULL DEFAULT '0.00000000',
  `discount_amount` decimal(26,8) NOT NULL DEFAULT '0.00000000',
  `total_amount` decimal(26,8) NOT NULL DEFAULT '0.00000000',
  `currency` varchar(3) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `entry_id` bigint NOT NULL DEFAULT '0',
  `gateway` varchar(20) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `type` varchar(12) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `mode` varchar(4) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `transaction_id` varchar(40) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `customer_id` varchar(40) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `subscription_id` varchar(40) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `subscription_status` varchar(10) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `title` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL DEFAULT '',
  `date_created_gmt` datetime NOT NULL,
  `date_updated_gmt` datetime NOT NULL,
  `is_published` tinyint(1) NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wpforms_payment_meta`
--

CREATE TABLE `wp_wpforms_payment_meta` (
  `id` bigint NOT NULL,
  `payment_id` bigint NOT NULL,
  `meta_key` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
  `meta_value` longtext COLLATE utf8mb4_unicode_520_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wpforms_tasks_meta`
--

CREATE TABLE `wp_wpforms_tasks_meta` (
  `id` bigint NOT NULL,
  `action` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `date` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wpmailsmtp_debug_events`
--

CREATE TABLE `wp_wpmailsmtp_debug_events` (
  `id` int UNSIGNED NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `initiator` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci,
  `event_type` tinyint UNSIGNED NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wp_wpmailsmtp_tasks_meta`
--

CREATE TABLE `wp_wpmailsmtp_tasks_meta` (
  `id` bigint NOT NULL,
  `action` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,
  `date` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `ez_app`
--
ALTER TABLE `ez_app`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `ez_app_code_uq` (`app_code`);

--
-- Indexes for table `ez_article_space`
--
ALTER TABLE `ez_article_space`
  ADD PRIMARY KEY (`id`),
  ADD KEY `key_enable` (`enable`);

--
-- Indexes for table `ez_article_space_category`
--
ALTER TABLE `ez_article_space_category`
  ADD PRIMARY KEY (`id`),
  ADD KEY `key_enable` (`enable`),
  ADD KEY `key_space_id` (`space_id`);

--
-- Indexes for table `ez_part_config`
--
ALTER TABLE `ez_part_config`
  ADD PRIMARY KEY (`id`),
  ADD KEY `ez_part_config_code_idx` (`app_code`,`exam_code`,`part_code`);

--
-- Indexes for table `ez_quiz_category`
--
ALTER TABLE `ez_quiz_category`
  ADD PRIMARY KEY (`id`),
  ADD KEY `key_code` (`code`),
  ADD KEY `key_enable` (`enable`);

--
-- Indexes for table `ez_quiz_category_v2`
--
ALTER TABLE `ez_quiz_category_v2`
  ADD PRIMARY KEY (`id`),
  ADD KEY `key_code` (`code`),
  ADD KEY `key_enable` (`enable`),
  ADD KEY `key_parent_id` (`parent_id`),
  ADD KEY `key_app_code` (`app_code`);

--
-- Indexes for table `ez_user_purchased`
--
ALTER TABLE `ez_user_purchased`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `idx_user_id_category_code` (`user_id`,`category_code`),
  ADD KEY `key_user_id` (`user_id`),
  ADD KEY `key_category_code` (`category_code`);

--
-- Indexes for table `ez_verification_code`
--
ALTER TABLE `ez_verification_code`
  ADD PRIMARY KEY (`id`),
  ADD KEY `code_idx` (`code`),
  ADD KEY `type_idx` (`type`),
  ADD KEY `user_id_idx` (`user_id`);

--
-- Indexes for table `ez_version`
--
ALTER TABLE `ez_version`
  ADD PRIMARY KEY (`id`),
  ADD KEY `ez_version_code_idx` (`app_code`,`platform`);

--
-- Indexes for table `wp_actionscheduler_actions`
--
ALTER TABLE `wp_actionscheduler_actions`
  ADD PRIMARY KEY (`action_id`),
  ADD KEY `hook` (`hook`),
  ADD KEY `status` (`status`),
  ADD KEY `scheduled_date_gmt` (`scheduled_date_gmt`),
  ADD KEY `args` (`args`),
  ADD KEY `group_id` (`group_id`),
  ADD KEY `last_attempt_gmt` (`last_attempt_gmt`),
  ADD KEY `claim_id_status_scheduled_date_gmt` (`claim_id`,`status`,`scheduled_date_gmt`),
  ADD KEY `hook_status_scheduled_date_gmt` (`hook`(163),`status`,`scheduled_date_gmt`),
  ADD KEY `status_scheduled_date_gmt` (`status`,`scheduled_date_gmt`);

--
-- Indexes for table `wp_actionscheduler_claims`
--
ALTER TABLE `wp_actionscheduler_claims`
  ADD PRIMARY KEY (`claim_id`),
  ADD KEY `date_created_gmt` (`date_created_gmt`);

--
-- Indexes for table `wp_actionscheduler_groups`
--
ALTER TABLE `wp_actionscheduler_groups`
  ADD PRIMARY KEY (`group_id`),
  ADD KEY `slug` (`slug`(191));

--
-- Indexes for table `wp_actionscheduler_logs`
--
ALTER TABLE `wp_actionscheduler_logs`
  ADD PRIMARY KEY (`log_id`),
  ADD KEY `action_id` (`action_id`),
  ADD KEY `log_date_gmt` (`log_date_gmt`);

--
-- Indexes for table `wp_aioseo_cache`
--
ALTER TABLE `wp_aioseo_cache`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `ndx_aioseo_cache_key` (`key`),
  ADD KEY `ndx_aioseo_cache_expiration` (`expiration`);

--
-- Indexes for table `wp_aioseo_crawl_cleanup_blocked_args`
--
ALTER TABLE `wp_aioseo_crawl_cleanup_blocked_args`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `ndx_aioseo_crawl_cleanup_blocked_args_key_value_hash` (`key_value_hash`),
  ADD UNIQUE KEY `ndx_aioseo_crawl_cleanup_blocked_args_regex` (`regex`);

--
-- Indexes for table `wp_aioseo_crawl_cleanup_logs`
--
ALTER TABLE `wp_aioseo_crawl_cleanup_logs`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `ndx_aioseo_crawl_cleanup_logs_hash` (`hash`);

--
-- Indexes for table `wp_aioseo_notifications`
--
ALTER TABLE `wp_aioseo_notifications`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `ndx_aioseo_notifications_slug` (`slug`),
  ADD KEY `ndx_aioseo_notifications_dates` (`start`,`end`),
  ADD KEY `ndx_aioseo_notifications_type` (`type`),
  ADD KEY `ndx_aioseo_notifications_dismissed` (`dismissed`);

--
-- Indexes for table `wp_aioseo_posts`
--
ALTER TABLE `wp_aioseo_posts`
  ADD PRIMARY KEY (`id`),
  ADD KEY `ndx_aioseo_posts_post_id` (`post_id`);

--
-- Indexes for table `wp_commentmeta`
--
ALTER TABLE `wp_commentmeta`
  ADD PRIMARY KEY (`meta_id`),
  ADD KEY `comment_id` (`comment_id`),
  ADD KEY `meta_key` (`meta_key`(191));

--
-- Indexes for table `wp_comments`
--
ALTER TABLE `wp_comments`
  ADD PRIMARY KEY (`comment_ID`),
  ADD KEY `comment_post_ID` (`comment_post_ID`),
  ADD KEY `comment_approved_date_gmt` (`comment_approved`,`comment_date_gmt`),
  ADD KEY `comment_date_gmt` (`comment_date_gmt`),
  ADD KEY `comment_parent` (`comment_parent`),
  ADD KEY `comment_author_email` (`comment_author_email`(10)),
  ADD KEY `woo_idx_comment_type` (`comment_type`);

--
-- Indexes for table `wp_gla_attribute_mapping_rules`
--
ALTER TABLE `wp_gla_attribute_mapping_rules`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `wp_gla_budget_recommendations`
--
ALTER TABLE `wp_gla_budget_recommendations`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `country_currency` (`country`,`currency`);

--
-- Indexes for table `wp_gla_merchant_issues`
--
ALTER TABLE `wp_gla_merchant_issues`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `wp_gla_shipping_rates`
--
ALTER TABLE `wp_gla_shipping_rates`
  ADD PRIMARY KEY (`id`),
  ADD KEY `country` (`country`),
  ADD KEY `currency` (`currency`);

--
-- Indexes for table `wp_gla_shipping_times`
--
ALTER TABLE `wp_gla_shipping_times`
  ADD PRIMARY KEY (`id`),
  ADD KEY `country` (`country`);

--
-- Indexes for table `wp_learndash_pro_quiz_category`
--
ALTER TABLE `wp_learndash_pro_quiz_category`
  ADD PRIMARY KEY (`category_id`);

--
-- Indexes for table `wp_learndash_pro_quiz_form`
--
ALTER TABLE `wp_learndash_pro_quiz_form`
  ADD PRIMARY KEY (`form_id`),
  ADD KEY `quiz_id` (`quiz_id`);

--
-- Indexes for table `wp_learndash_pro_quiz_lock`
--
ALTER TABLE `wp_learndash_pro_quiz_lock`
  ADD PRIMARY KEY (`quiz_id`,`lock_ip`,`user_id`,`lock_type`);

--
-- Indexes for table `wp_learndash_pro_quiz_master`
--
ALTER TABLE `wp_learndash_pro_quiz_master`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `wp_learndash_pro_quiz_prerequisite`
--
ALTER TABLE `wp_learndash_pro_quiz_prerequisite`
  ADD PRIMARY KEY (`prerequisite_quiz_id`,`quiz_id`);

--
-- Indexes for table `wp_learndash_pro_quiz_question`
--
ALTER TABLE `wp_learndash_pro_quiz_question`
  ADD PRIMARY KEY (`id`),
  ADD KEY `quiz_id` (`quiz_id`),
  ADD KEY `category_id` (`category_id`);

--
-- Indexes for table `wp_learndash_pro_quiz_statistic`
--
ALTER TABLE `wp_learndash_pro_quiz_statistic`
  ADD PRIMARY KEY (`statistic_ref_id`,`question_id`);

--
-- Indexes for table `wp_learndash_pro_quiz_statistic_ref`
--
ALTER TABLE `wp_learndash_pro_quiz_statistic_ref`
  ADD PRIMARY KEY (`statistic_ref_id`),
  ADD KEY `quiz_id` (`quiz_id`,`user_id`),
  ADD KEY `time` (`create_time`);

--
-- Indexes for table `wp_learndash_pro_quiz_template`
--
ALTER TABLE `wp_learndash_pro_quiz_template`
  ADD PRIMARY KEY (`template_id`);

--
-- Indexes for table `wp_learndash_pro_quiz_toplist`
--
ALTER TABLE `wp_learndash_pro_quiz_toplist`
  ADD PRIMARY KEY (`toplist_id`,`quiz_id`);

--
-- Indexes for table `wp_learndash_user_activity`
--
ALTER TABLE `wp_learndash_user_activity`
  ADD PRIMARY KEY (`activity_id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `post_id` (`post_id`),
  ADD KEY `course_id` (`course_id`),
  ADD KEY `activity_status` (`activity_status`),
  ADD KEY `activity_type` (`activity_type`),
  ADD KEY `activity_started` (`activity_started`),
  ADD KEY `activity_completed` (`activity_completed`),
  ADD KEY `activity_updated` (`activity_updated`);

--
-- Indexes for table `wp_learndash_user_activity_meta`
--
ALTER TABLE `wp_learndash_user_activity_meta`
  ADD PRIMARY KEY (`activity_meta_id`),
  ADD KEY `activity_id` (`activity_id`),
  ADD KEY `activity_meta_key` (`activity_meta_key`(191));

--
-- Indexes for table `wp_links`
--
ALTER TABLE `wp_links`
  ADD PRIMARY KEY (`link_id`),
  ADD KEY `link_visible` (`link_visible`);

--
-- Indexes for table `wp_litespeed_url`
--
ALTER TABLE `wp_litespeed_url`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `url` (`url`(191)),
  ADD KEY `cache_tags` (`cache_tags`(191));

--
-- Indexes for table `wp_litespeed_url_file`
--
ALTER TABLE `wp_litespeed_url_file`
  ADD PRIMARY KEY (`id`),
  ADD KEY `filename` (`filename`),
  ADD KEY `type` (`type`),
  ADD KEY `url_id_2` (`url_id`,`vary`,`type`),
  ADD KEY `filename_2` (`filename`,`expired`),
  ADD KEY `url_id` (`url_id`,`expired`);

--
-- Indexes for table `wp_mailpoet_automations`
--
ALTER TABLE `wp_mailpoet_automations`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `wp_mailpoet_automation_runs`
--
ALTER TABLE `wp_mailpoet_automation_runs`
  ADD PRIMARY KEY (`id`),
  ADD KEY `automation_id` (`automation_id`,`status`),
  ADD KEY `created_at` (`created_at`),
  ADD KEY `version_id` (`version_id`),
  ADD KEY `status` (`status`),
  ADD KEY `next_step_id` (`next_step_id`);

--
-- Indexes for table `wp_mailpoet_automation_run_logs`
--
ALTER TABLE `wp_mailpoet_automation_run_logs`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `automation_run_id_step_id` (`automation_run_id`,`step_id`),
  ADD KEY `status` (`status`),
  ADD KEY `step_id` (`step_id`);

--
-- Indexes for table `wp_mailpoet_automation_run_subjects`
--
ALTER TABLE `wp_mailpoet_automation_run_subjects`
  ADD PRIMARY KEY (`id`),
  ADD KEY `automation_run_id` (`automation_run_id`),
  ADD KEY `hash` (`hash`);

--
-- Indexes for table `wp_mailpoet_automation_triggers`
--
ALTER TABLE `wp_mailpoet_automation_triggers`
  ADD PRIMARY KEY (`automation_id`,`trigger_key`);

--
-- Indexes for table `wp_mailpoet_automation_versions`
--
ALTER TABLE `wp_mailpoet_automation_versions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `automation_id` (`automation_id`);

--
-- Indexes for table `wp_mailpoet_custom_fields`
--
ALTER TABLE `wp_mailpoet_custom_fields`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Indexes for table `wp_mailpoet_dynamic_segment_filters`
--
ALTER TABLE `wp_mailpoet_dynamic_segment_filters`
  ADD PRIMARY KEY (`id`),
  ADD KEY `segment_id` (`segment_id`);

--
-- Indexes for table `wp_mailpoet_feature_flags`
--
ALTER TABLE `wp_mailpoet_feature_flags`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Indexes for table `wp_mailpoet_forms`
--
ALTER TABLE `wp_mailpoet_forms`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `wp_mailpoet_log`
--
ALTER TABLE `wp_mailpoet_log`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `wp_mailpoet_migrations`
--
ALTER TABLE `wp_mailpoet_migrations`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Indexes for table `wp_mailpoet_newsletters`
--
ALTER TABLE `wp_mailpoet_newsletters`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unsubscribe_token` (`unsubscribe_token`),
  ADD KEY `type_status` (`type`,`status`),
  ADD KEY `wp_post_id` (`wp_post_id`);

--
-- Indexes for table `wp_mailpoet_newsletter_links`
--
ALTER TABLE `wp_mailpoet_newsletter_links`
  ADD PRIMARY KEY (`id`),
  ADD KEY `newsletter_id` (`newsletter_id`),
  ADD KEY `queue_id` (`queue_id`),
  ADD KEY `url` (`url`(100));

--
-- Indexes for table `wp_mailpoet_newsletter_option`
--
ALTER TABLE `wp_mailpoet_newsletter_option`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `newsletter_id_option_field_id` (`newsletter_id`,`option_field_id`);

--
-- Indexes for table `wp_mailpoet_newsletter_option_fields`
--
ALTER TABLE `wp_mailpoet_newsletter_option_fields`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name_newsletter_type` (`newsletter_type`,`name`);

--
-- Indexes for table `wp_mailpoet_newsletter_posts`
--
ALTER TABLE `wp_mailpoet_newsletter_posts`
  ADD PRIMARY KEY (`id`),
  ADD KEY `newsletter_id` (`newsletter_id`);

--
-- Indexes for table `wp_mailpoet_newsletter_segment`
--
ALTER TABLE `wp_mailpoet_newsletter_segment`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `newsletter_segment` (`newsletter_id`,`segment_id`);

--
-- Indexes for table `wp_mailpoet_newsletter_templates`
--
ALTER TABLE `wp_mailpoet_newsletter_templates`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `wp_mailpoet_scheduled_tasks`
--
ALTER TABLE `wp_mailpoet_scheduled_tasks`
  ADD PRIMARY KEY (`id`),
  ADD KEY `type` (`type`),
  ADD KEY `status` (`status`);

--
-- Indexes for table `wp_mailpoet_scheduled_task_subscribers`
--
ALTER TABLE `wp_mailpoet_scheduled_task_subscribers`
  ADD PRIMARY KEY (`task_id`,`subscriber_id`),
  ADD KEY `subscriber_id` (`subscriber_id`);

--
-- Indexes for table `wp_mailpoet_segments`
--
ALTER TABLE `wp_mailpoet_segments`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`),
  ADD KEY `average_engagement_score_updated_at` (`average_engagement_score_updated_at`);

--
-- Indexes for table `wp_mailpoet_sending_queues`
--
ALTER TABLE `wp_mailpoet_sending_queues`
  ADD PRIMARY KEY (`id`),
  ADD KEY `task_id` (`task_id`),
  ADD KEY `newsletter_id` (`newsletter_id`);

--
-- Indexes for table `wp_mailpoet_settings`
--
ALTER TABLE `wp_mailpoet_settings`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Indexes for table `wp_mailpoet_statistics_bounces`
--
ALTER TABLE `wp_mailpoet_statistics_bounces`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `wp_mailpoet_statistics_clicks`
--
ALTER TABLE `wp_mailpoet_statistics_clicks`
  ADD PRIMARY KEY (`id`),
  ADD KEY `newsletter_id_subscriber_id_user_agent_type` (`newsletter_id`,`subscriber_id`,`user_agent_type`),
  ADD KEY `queue_id` (`queue_id`),
  ADD KEY `subscriber_id` (`subscriber_id`);

--
-- Indexes for table `wp_mailpoet_statistics_forms`
--
ALTER TABLE `wp_mailpoet_statistics_forms`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `form_subscriber` (`form_id`,`subscriber_id`);

--
-- Indexes for table `wp_mailpoet_statistics_newsletters`
--
ALTER TABLE `wp_mailpoet_statistics_newsletters`
  ADD PRIMARY KEY (`id`),
  ADD KEY `newsletter_id` (`newsletter_id`),
  ADD KEY `subscriber_id` (`subscriber_id`);

--
-- Indexes for table `wp_mailpoet_statistics_opens`
--
ALTER TABLE `wp_mailpoet_statistics_opens`
  ADD PRIMARY KEY (`id`),
  ADD KEY `newsletter_id_subscriber_id_user_agent_type` (`newsletter_id`,`subscriber_id`,`user_agent_type`),
  ADD KEY `queue_id` (`queue_id`),
  ADD KEY `subscriber_id` (`subscriber_id`),
  ADD KEY `created_at` (`created_at`),
  ADD KEY `subscriber_id_created_at` (`subscriber_id`,`created_at`);

--
-- Indexes for table `wp_mailpoet_statistics_unsubscribes`
--
ALTER TABLE `wp_mailpoet_statistics_unsubscribes`
  ADD PRIMARY KEY (`id`),
  ADD KEY `newsletter_id_subscriber_id` (`newsletter_id`,`subscriber_id`),
  ADD KEY `queue_id` (`queue_id`),
  ADD KEY `subscriber_id` (`subscriber_id`);

--
-- Indexes for table `wp_mailpoet_statistics_woocommerce_purchases`
--
ALTER TABLE `wp_mailpoet_statistics_woocommerce_purchases`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `click_id_order_id` (`click_id`,`order_id`),
  ADD KEY `newsletter_id` (`newsletter_id`),
  ADD KEY `queue_id` (`queue_id`),
  ADD KEY `subscriber_id` (`subscriber_id`),
  ADD KEY `status` (`status`);

--
-- Indexes for table `wp_mailpoet_stats_notifications`
--
ALTER TABLE `wp_mailpoet_stats_notifications`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `newsletter_id_task_id` (`newsletter_id`,`task_id`),
  ADD KEY `task_id` (`task_id`);

--
-- Indexes for table `wp_mailpoet_subscribers`
--
ALTER TABLE `wp_mailpoet_subscribers`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `unsubscribe_token` (`unsubscribe_token`),
  ADD KEY `wp_user_id` (`wp_user_id`),
  ADD KEY `updated_at` (`updated_at`),
  ADD KEY `status_deleted_at` (`status`,`deleted_at`),
  ADD KEY `last_subscribed_at` (`last_subscribed_at`),
  ADD KEY `engagement_score_updated_at` (`engagement_score_updated_at`),
  ADD KEY `link_token` (`link_token`),
  ADD KEY `first_name` (`first_name`(10)),
  ADD KEY `last_name` (`last_name`(10)),
  ADD KEY `last_sending_at` (`last_sending_at`),
  ADD KEY `last_open_at` (`last_open_at`),
  ADD KEY `last_click_at` (`last_click_at`),
  ADD KEY `last_purchase_at` (`last_purchase_at`),
  ADD KEY `last_page_view_at` (`last_page_view_at`);

--
-- Indexes for table `wp_mailpoet_subscriber_custom_field`
--
ALTER TABLE `wp_mailpoet_subscriber_custom_field`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `subscriber_id_custom_field_id` (`subscriber_id`,`custom_field_id`);

--
-- Indexes for table `wp_mailpoet_subscriber_ips`
--
ALTER TABLE `wp_mailpoet_subscriber_ips`
  ADD PRIMARY KEY (`created_at`,`ip`),
  ADD KEY `ip` (`ip`);

--
-- Indexes for table `wp_mailpoet_subscriber_segment`
--
ALTER TABLE `wp_mailpoet_subscriber_segment`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `subscriber_segment` (`subscriber_id`,`segment_id`),
  ADD KEY `segment_id` (`segment_id`);

--
-- Indexes for table `wp_mailpoet_subscriber_tag`
--
ALTER TABLE `wp_mailpoet_subscriber_tag`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `subscriber_tag` (`subscriber_id`,`tag_id`),
  ADD KEY `tag_id` (`tag_id`);

--
-- Indexes for table `wp_mailpoet_tags`
--
ALTER TABLE `wp_mailpoet_tags`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Indexes for table `wp_mailpoet_user_agents`
--
ALTER TABLE `wp_mailpoet_user_agents`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `hash` (`hash`);

--
-- Indexes for table `wp_mailpoet_user_flags`
--
ALTER TABLE `wp_mailpoet_user_flags`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `user_id_name` (`user_id`,`name`);

--
-- Indexes for table `wp_nf3_actions`
--
ALTER TABLE `wp_nf3_actions`
  ADD UNIQUE KEY `id` (`id`);

--
-- Indexes for table `wp_nf3_action_meta`
--
ALTER TABLE `wp_nf3_action_meta`
  ADD UNIQUE KEY `id` (`id`);

--
-- Indexes for table `wp_nf3_chunks`
--
ALTER TABLE `wp_nf3_chunks`
  ADD UNIQUE KEY `id` (`id`);

--
-- Indexes for table `wp_nf3_fields`
--
ALTER TABLE `wp_nf3_fields`
  ADD UNIQUE KEY `id` (`id`);

--
-- Indexes for table `wp_nf3_field_meta`
--
ALTER TABLE `wp_nf3_field_meta`
  ADD UNIQUE KEY `id` (`id`);

--
-- Indexes for table `wp_nf3_forms`
--
ALTER TABLE `wp_nf3_forms`
  ADD UNIQUE KEY `id` (`id`);

--
-- Indexes for table `wp_nf3_form_meta`
--
ALTER TABLE `wp_nf3_form_meta`
  ADD UNIQUE KEY `id` (`id`);

--
-- Indexes for table `wp_nf3_objects`
--
ALTER TABLE `wp_nf3_objects`
  ADD UNIQUE KEY `id` (`id`);

--
-- Indexes for table `wp_nf3_object_meta`
--
ALTER TABLE `wp_nf3_object_meta`
  ADD UNIQUE KEY `id` (`id`);

--
-- Indexes for table `wp_nf3_relationships`
--
ALTER TABLE `wp_nf3_relationships`
  ADD UNIQUE KEY `id` (`id`);

--
-- Indexes for table `wp_nf3_upgrades`
--
ALTER TABLE `wp_nf3_upgrades`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `wp_options`
--
ALTER TABLE `wp_options`
  ADD PRIMARY KEY (`option_id`),
  ADD UNIQUE KEY `option_name` (`option_name`),
  ADD KEY `autoload` (`autoload`);

--
-- Indexes for table `wp_pieregister_code`
--
ALTER TABLE `wp_pieregister_code`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `wp_pieregister_custom_user_roles`
--
ALTER TABLE `wp_pieregister_custom_user_roles`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `role_key` (`role_key`);

--
-- Indexes for table `wp_pieregister_invite_code_emails`
--
ALTER TABLE `wp_pieregister_invite_code_emails`
  ADD PRIMARY KEY (`id`),
  ADD KEY `code_id` (`code_id`);

--
-- Indexes for table `wp_pieregister_lockdowns`
--
ALTER TABLE `wp_pieregister_lockdowns`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `wp_pieregister_redirect_settings`
--
ALTER TABLE `wp_pieregister_redirect_settings`
  ADD PRIMARY KEY (`user_role`),
  ADD UNIQUE KEY `id` (`id`);

--
-- Indexes for table `wp_pmpro_discount_codes`
--
ALTER TABLE `wp_pmpro_discount_codes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `code` (`code`),
  ADD KEY `starts` (`starts`),
  ADD KEY `expires` (`expires`);

--
-- Indexes for table `wp_pmpro_discount_codes_levels`
--
ALTER TABLE `wp_pmpro_discount_codes_levels`
  ADD PRIMARY KEY (`code_id`,`level_id`),
  ADD KEY `initial_payment` (`initial_payment`);

--
-- Indexes for table `wp_pmpro_discount_codes_uses`
--
ALTER TABLE `wp_pmpro_discount_codes_uses`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `timestamp` (`timestamp`);

--
-- Indexes for table `wp_pmpro_memberships_categories`
--
ALTER TABLE `wp_pmpro_memberships_categories`
  ADD PRIMARY KEY (`membership_id`,`category_id`),
  ADD UNIQUE KEY `category_membership` (`category_id`,`membership_id`);

--
-- Indexes for table `wp_pmpro_memberships_pages`
--
ALTER TABLE `wp_pmpro_memberships_pages`
  ADD PRIMARY KEY (`page_id`,`membership_id`),
  ADD UNIQUE KEY `membership_page` (`membership_id`,`page_id`);

--
-- Indexes for table `wp_pmpro_memberships_users`
--
ALTER TABLE `wp_pmpro_memberships_users`
  ADD PRIMARY KEY (`id`),
  ADD KEY `membership_id` (`membership_id`),
  ADD KEY `modified` (`modified`),
  ADD KEY `code_id` (`code_id`),
  ADD KEY `enddate` (`enddate`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `status` (`status`);

--
-- Indexes for table `wp_pmpro_membership_levelmeta`
--
ALTER TABLE `wp_pmpro_membership_levelmeta`
  ADD PRIMARY KEY (`meta_id`),
  ADD KEY `pmpro_membership_level_id` (`pmpro_membership_level_id`),
  ADD KEY `meta_key` (`meta_key`);

--
-- Indexes for table `wp_pmpro_membership_levels`
--
ALTER TABLE `wp_pmpro_membership_levels`
  ADD PRIMARY KEY (`id`),
  ADD KEY `allow_signups` (`allow_signups`),
  ADD KEY `initial_payment` (`initial_payment`),
  ADD KEY `name` (`name`);

--
-- Indexes for table `wp_pmpro_membership_ordermeta`
--
ALTER TABLE `wp_pmpro_membership_ordermeta`
  ADD PRIMARY KEY (`meta_id`),
  ADD KEY `pmpro_membership_order_id` (`pmpro_membership_order_id`),
  ADD KEY `meta_key` (`meta_key`);

--
-- Indexes for table `wp_pmpro_membership_orders`
--
ALTER TABLE `wp_pmpro_membership_orders`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `code` (`code`),
  ADD KEY `session_id` (`session_id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `membership_id` (`membership_id`),
  ADD KEY `status` (`status`),
  ADD KEY `timestamp` (`timestamp`),
  ADD KEY `gateway` (`gateway`),
  ADD KEY `gateway_environment` (`gateway_environment`),
  ADD KEY `payment_transaction_id` (`payment_transaction_id`),
  ADD KEY `subscription_transaction_id` (`subscription_transaction_id`),
  ADD KEY `affiliate_id` (`affiliate_id`),
  ADD KEY `affiliate_subid` (`affiliate_subid`),
  ADD KEY `checkout_id` (`checkout_id`);

--
-- Indexes for table `wp_postmeta`
--
ALTER TABLE `wp_postmeta`
  ADD PRIMARY KEY (`meta_id`),
  ADD KEY `post_id` (`post_id`),
  ADD KEY `meta_key` (`meta_key`(191));

--
-- Indexes for table `wp_posts`
--
ALTER TABLE `wp_posts`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `post_name` (`post_name`(191)),
  ADD KEY `type_status_date` (`post_type`,`post_status`,`post_date`,`ID`),
  ADD KEY `post_parent` (`post_parent`),
  ADD KEY `post_author` (`post_author`);

--
-- Indexes for table `wp_swpm_membership_meta_tbl`
--
ALTER TABLE `wp_swpm_membership_meta_tbl`
  ADD PRIMARY KEY (`id`),
  ADD KEY `level_id` (`level_id`);

--
-- Indexes for table `wp_swpm_membership_tbl`
--
ALTER TABLE `wp_swpm_membership_tbl`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `wp_swpm_members_tbl`
--
ALTER TABLE `wp_swpm_members_tbl`
  ADD PRIMARY KEY (`member_id`);

--
-- Indexes for table `wp_swpm_payments_tbl`
--
ALTER TABLE `wp_swpm_payments_tbl`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `wp_termmeta`
--
ALTER TABLE `wp_termmeta`
  ADD PRIMARY KEY (`meta_id`),
  ADD KEY `term_id` (`term_id`),
  ADD KEY `meta_key` (`meta_key`(191));

--
-- Indexes for table `wp_terms`
--
ALTER TABLE `wp_terms`
  ADD PRIMARY KEY (`term_id`),
  ADD KEY `slug` (`slug`(191)),
  ADD KEY `name` (`name`(191));

--
-- Indexes for table `wp_term_relationships`
--
ALTER TABLE `wp_term_relationships`
  ADD PRIMARY KEY (`object_id`,`term_taxonomy_id`),
  ADD KEY `term_taxonomy_id` (`term_taxonomy_id`);

--
-- Indexes for table `wp_term_taxonomy`
--
ALTER TABLE `wp_term_taxonomy`
  ADD PRIMARY KEY (`term_taxonomy_id`),
  ADD UNIQUE KEY `term_id_taxonomy` (`term_id`,`taxonomy`),
  ADD KEY `taxonomy` (`taxonomy`);

--
-- Indexes for table `wp_usermeta`
--
ALTER TABLE `wp_usermeta`
  ADD PRIMARY KEY (`umeta_id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `meta_key` (`meta_key`(191));

--
-- Indexes for table `wp_users`
--
ALTER TABLE `wp_users`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `user_login_key` (`user_login`),
  ADD KEY `user_nicename` (`user_nicename`),
  ADD KEY `user_email` (`user_email`);

--
-- Indexes for table `wp_wc_admin_notes`
--
ALTER TABLE `wp_wc_admin_notes`
  ADD PRIMARY KEY (`note_id`);

--
-- Indexes for table `wp_wc_admin_note_actions`
--
ALTER TABLE `wp_wc_admin_note_actions`
  ADD PRIMARY KEY (`action_id`),
  ADD KEY `note_id` (`note_id`);

--
-- Indexes for table `wp_wc_category_lookup`
--
ALTER TABLE `wp_wc_category_lookup`
  ADD PRIMARY KEY (`category_tree_id`,`category_id`);

--
-- Indexes for table `wp_wc_customer_lookup`
--
ALTER TABLE `wp_wc_customer_lookup`
  ADD PRIMARY KEY (`customer_id`),
  ADD UNIQUE KEY `user_id` (`user_id`),
  ADD KEY `email` (`email`);

--
-- Indexes for table `wp_wc_download_log`
--
ALTER TABLE `wp_wc_download_log`
  ADD PRIMARY KEY (`download_log_id`),
  ADD KEY `permission_id` (`permission_id`),
  ADD KEY `timestamp` (`timestamp`);

--
-- Indexes for table `wp_wc_orders`
--
ALTER TABLE `wp_wc_orders`
  ADD PRIMARY KEY (`id`),
  ADD KEY `status` (`status`),
  ADD KEY `date_created` (`date_created_gmt`),
  ADD KEY `customer_id_billing_email` (`customer_id`,`billing_email`(171)),
  ADD KEY `billing_email` (`billing_email`(191)),
  ADD KEY `type_status_date` (`type`,`status`,`date_created_gmt`),
  ADD KEY `parent_order_id` (`parent_order_id`),
  ADD KEY `date_updated` (`date_updated_gmt`);

--
-- Indexes for table `wp_wc_orders_meta`
--
ALTER TABLE `wp_wc_orders_meta`
  ADD PRIMARY KEY (`id`),
  ADD KEY `meta_key_value` (`meta_key`(100),`meta_value`(82)),
  ADD KEY `order_id_meta_key_meta_value` (`order_id`,`meta_key`(100),`meta_value`(82));

--
-- Indexes for table `wp_wc_order_addresses`
--
ALTER TABLE `wp_wc_order_addresses`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `address_type_order_id` (`address_type`,`order_id`),
  ADD KEY `order_id` (`order_id`),
  ADD KEY `email` (`email`(191)),
  ADD KEY `phone` (`phone`);

--
-- Indexes for table `wp_wc_order_coupon_lookup`
--
ALTER TABLE `wp_wc_order_coupon_lookup`
  ADD PRIMARY KEY (`order_id`,`coupon_id`),
  ADD KEY `coupon_id` (`coupon_id`),
  ADD KEY `date_created` (`date_created`);

--
-- Indexes for table `wp_wc_order_operational_data`
--
ALTER TABLE `wp_wc_order_operational_data`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `order_id` (`order_id`),
  ADD KEY `order_key` (`order_key`);

--
-- Indexes for table `wp_wc_order_product_lookup`
--
ALTER TABLE `wp_wc_order_product_lookup`
  ADD PRIMARY KEY (`order_item_id`),
  ADD KEY `order_id` (`order_id`),
  ADD KEY `product_id` (`product_id`),
  ADD KEY `customer_id` (`customer_id`),
  ADD KEY `date_created` (`date_created`);

--
-- Indexes for table `wp_wc_order_stats`
--
ALTER TABLE `wp_wc_order_stats`
  ADD PRIMARY KEY (`order_id`),
  ADD KEY `date_created` (`date_created`),
  ADD KEY `customer_id` (`customer_id`),
  ADD KEY `status` (`status`(191));

--
-- Indexes for table `wp_wc_order_tax_lookup`
--
ALTER TABLE `wp_wc_order_tax_lookup`
  ADD PRIMARY KEY (`order_id`,`tax_rate_id`),
  ADD KEY `tax_rate_id` (`tax_rate_id`),
  ADD KEY `date_created` (`date_created`);

--
-- Indexes for table `wp_wc_product_attributes_lookup`
--
ALTER TABLE `wp_wc_product_attributes_lookup`
  ADD PRIMARY KEY (`product_or_parent_id`,`term_id`,`product_id`,`taxonomy`),
  ADD KEY `is_variation_attribute_term_id` (`is_variation_attribute`,`term_id`);

--
-- Indexes for table `wp_wc_product_download_directories`
--
ALTER TABLE `wp_wc_product_download_directories`
  ADD PRIMARY KEY (`url_id`),
  ADD KEY `url` (`url`(191));

--
-- Indexes for table `wp_wc_product_meta_lookup`
--
ALTER TABLE `wp_wc_product_meta_lookup`
  ADD PRIMARY KEY (`product_id`),
  ADD KEY `virtual` (`virtual`),
  ADD KEY `downloadable` (`downloadable`),
  ADD KEY `stock_status` (`stock_status`),
  ADD KEY `stock_quantity` (`stock_quantity`),
  ADD KEY `onsale` (`onsale`),
  ADD KEY `min_max_price` (`min_price`,`max_price`);

--
-- Indexes for table `wp_wc_rate_limits`
--
ALTER TABLE `wp_wc_rate_limits`
  ADD PRIMARY KEY (`rate_limit_id`),
  ADD UNIQUE KEY `rate_limit_key` (`rate_limit_key`(191));

--
-- Indexes for table `wp_wc_reserved_stock`
--
ALTER TABLE `wp_wc_reserved_stock`
  ADD PRIMARY KEY (`order_id`,`product_id`);

--
-- Indexes for table `wp_wc_tax_rate_classes`
--
ALTER TABLE `wp_wc_tax_rate_classes`
  ADD PRIMARY KEY (`tax_rate_class_id`),
  ADD UNIQUE KEY `slug` (`slug`(191));

--
-- Indexes for table `wp_wc_webhooks`
--
ALTER TABLE `wp_wc_webhooks`
  ADD PRIMARY KEY (`webhook_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `wp_woocommerce_api_keys`
--
ALTER TABLE `wp_woocommerce_api_keys`
  ADD PRIMARY KEY (`key_id`),
  ADD KEY `consumer_key` (`consumer_key`),
  ADD KEY `consumer_secret` (`consumer_secret`);

--
-- Indexes for table `wp_woocommerce_attribute_taxonomies`
--
ALTER TABLE `wp_woocommerce_attribute_taxonomies`
  ADD PRIMARY KEY (`attribute_id`),
  ADD KEY `attribute_name` (`attribute_name`(20));

--
-- Indexes for table `wp_woocommerce_downloadable_product_permissions`
--
ALTER TABLE `wp_woocommerce_downloadable_product_permissions`
  ADD PRIMARY KEY (`permission_id`),
  ADD KEY `download_order_key_product` (`product_id`,`order_id`,`order_key`(16),`download_id`),
  ADD KEY `download_order_product` (`download_id`,`order_id`,`product_id`),
  ADD KEY `order_id` (`order_id`),
  ADD KEY `user_order_remaining_expires` (`user_id`,`order_id`,`downloads_remaining`,`access_expires`);

--
-- Indexes for table `wp_woocommerce_log`
--
ALTER TABLE `wp_woocommerce_log`
  ADD PRIMARY KEY (`log_id`),
  ADD KEY `level` (`level`);

--
-- Indexes for table `wp_woocommerce_order_itemmeta`
--
ALTER TABLE `wp_woocommerce_order_itemmeta`
  ADD PRIMARY KEY (`meta_id`),
  ADD KEY `order_item_id` (`order_item_id`),
  ADD KEY `meta_key` (`meta_key`(32));

--
-- Indexes for table `wp_woocommerce_order_items`
--
ALTER TABLE `wp_woocommerce_order_items`
  ADD PRIMARY KEY (`order_item_id`),
  ADD KEY `order_id` (`order_id`);

--
-- Indexes for table `wp_woocommerce_payment_tokenmeta`
--
ALTER TABLE `wp_woocommerce_payment_tokenmeta`
  ADD PRIMARY KEY (`meta_id`),
  ADD KEY `payment_token_id` (`payment_token_id`),
  ADD KEY `meta_key` (`meta_key`(32));

--
-- Indexes for table `wp_woocommerce_payment_tokens`
--
ALTER TABLE `wp_woocommerce_payment_tokens`
  ADD PRIMARY KEY (`token_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `wp_woocommerce_sessions`
--
ALTER TABLE `wp_woocommerce_sessions`
  ADD PRIMARY KEY (`session_id`),
  ADD UNIQUE KEY `session_key` (`session_key`);

--
-- Indexes for table `wp_woocommerce_shipping_zones`
--
ALTER TABLE `wp_woocommerce_shipping_zones`
  ADD PRIMARY KEY (`zone_id`);

--
-- Indexes for table `wp_woocommerce_shipping_zone_locations`
--
ALTER TABLE `wp_woocommerce_shipping_zone_locations`
  ADD PRIMARY KEY (`location_id`),
  ADD KEY `location_id` (`location_id`),
  ADD KEY `location_type_code` (`location_type`(10),`location_code`(20));

--
-- Indexes for table `wp_woocommerce_shipping_zone_methods`
--
ALTER TABLE `wp_woocommerce_shipping_zone_methods`
  ADD PRIMARY KEY (`instance_id`);

--
-- Indexes for table `wp_woocommerce_tax_rates`
--
ALTER TABLE `wp_woocommerce_tax_rates`
  ADD PRIMARY KEY (`tax_rate_id`),
  ADD KEY `tax_rate_country` (`tax_rate_country`),
  ADD KEY `tax_rate_state` (`tax_rate_state`(2)),
  ADD KEY `tax_rate_class` (`tax_rate_class`(10)),
  ADD KEY `tax_rate_priority` (`tax_rate_priority`);

--
-- Indexes for table `wp_woocommerce_tax_rate_locations`
--
ALTER TABLE `wp_woocommerce_tax_rate_locations`
  ADD PRIMARY KEY (`location_id`),
  ADD KEY `tax_rate_id` (`tax_rate_id`),
  ADD KEY `location_type_code` (`location_type`(10),`location_code`(20));

--
-- Indexes for table `wp_wpforms_logs`
--
ALTER TABLE `wp_wpforms_logs`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `wp_wpforms_payments`
--
ALTER TABLE `wp_wpforms_payments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `form_id` (`form_id`),
  ADD KEY `status` (`status`(8)),
  ADD KEY `total_amount` (`total_amount`),
  ADD KEY `type` (`type`(8)),
  ADD KEY `transaction_id` (`transaction_id`(32)),
  ADD KEY `customer_id` (`customer_id`(32)),
  ADD KEY `subscription_id` (`subscription_id`(32)),
  ADD KEY `subscription_status` (`subscription_status`(8)),
  ADD KEY `title` (`title`(64));

--
-- Indexes for table `wp_wpforms_payment_meta`
--
ALTER TABLE `wp_wpforms_payment_meta`
  ADD PRIMARY KEY (`id`),
  ADD KEY `payment_id` (`payment_id`),
  ADD KEY `meta_key` (`meta_key`(191)),
  ADD KEY `meta_value` (`meta_value`(191));

--
-- Indexes for table `wp_wpforms_tasks_meta`
--
ALTER TABLE `wp_wpforms_tasks_meta`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `wp_wpmailsmtp_debug_events`
--
ALTER TABLE `wp_wpmailsmtp_debug_events`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `wp_wpmailsmtp_tasks_meta`
--
ALTER TABLE `wp_wpmailsmtp_tasks_meta`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `ez_app`
--
ALTER TABLE `ez_app`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ez_article_space`
--
ALTER TABLE `ez_article_space`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ez_article_space_category`
--
ALTER TABLE `ez_article_space_category`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ez_part_config`
--
ALTER TABLE `ez_part_config`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ez_quiz_category`
--
ALTER TABLE `ez_quiz_category`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ez_quiz_category_v2`
--
ALTER TABLE `ez_quiz_category_v2`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ez_user_purchased`
--
ALTER TABLE `ez_user_purchased`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ez_verification_code`
--
ALTER TABLE `ez_verification_code`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `ez_version`
--
ALTER TABLE `ez_version`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_actionscheduler_actions`
--
ALTER TABLE `wp_actionscheduler_actions`
  MODIFY `action_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_actionscheduler_claims`
--
ALTER TABLE `wp_actionscheduler_claims`
  MODIFY `claim_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_actionscheduler_groups`
--
ALTER TABLE `wp_actionscheduler_groups`
  MODIFY `group_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_actionscheduler_logs`
--
ALTER TABLE `wp_actionscheduler_logs`
  MODIFY `log_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_aioseo_cache`
--
ALTER TABLE `wp_aioseo_cache`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_aioseo_crawl_cleanup_blocked_args`
--
ALTER TABLE `wp_aioseo_crawl_cleanup_blocked_args`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_aioseo_crawl_cleanup_logs`
--
ALTER TABLE `wp_aioseo_crawl_cleanup_logs`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_aioseo_notifications`
--
ALTER TABLE `wp_aioseo_notifications`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_aioseo_posts`
--
ALTER TABLE `wp_aioseo_posts`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_commentmeta`
--
ALTER TABLE `wp_commentmeta`
  MODIFY `meta_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_comments`
--
ALTER TABLE `wp_comments`
  MODIFY `comment_ID` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_gla_attribute_mapping_rules`
--
ALTER TABLE `wp_gla_attribute_mapping_rules`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_gla_budget_recommendations`
--
ALTER TABLE `wp_gla_budget_recommendations`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_gla_merchant_issues`
--
ALTER TABLE `wp_gla_merchant_issues`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_gla_shipping_rates`
--
ALTER TABLE `wp_gla_shipping_rates`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_gla_shipping_times`
--
ALTER TABLE `wp_gla_shipping_times`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_learndash_pro_quiz_category`
--
ALTER TABLE `wp_learndash_pro_quiz_category`
  MODIFY `category_id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_learndash_pro_quiz_form`
--
ALTER TABLE `wp_learndash_pro_quiz_form`
  MODIFY `form_id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_learndash_pro_quiz_master`
--
ALTER TABLE `wp_learndash_pro_quiz_master`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_learndash_pro_quiz_question`
--
ALTER TABLE `wp_learndash_pro_quiz_question`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_learndash_pro_quiz_statistic_ref`
--
ALTER TABLE `wp_learndash_pro_quiz_statistic_ref`
  MODIFY `statistic_ref_id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_learndash_pro_quiz_template`
--
ALTER TABLE `wp_learndash_pro_quiz_template`
  MODIFY `template_id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_learndash_pro_quiz_toplist`
--
ALTER TABLE `wp_learndash_pro_quiz_toplist`
  MODIFY `toplist_id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_learndash_user_activity`
--
ALTER TABLE `wp_learndash_user_activity`
  MODIFY `activity_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_learndash_user_activity_meta`
--
ALTER TABLE `wp_learndash_user_activity_meta`
  MODIFY `activity_meta_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_links`
--
ALTER TABLE `wp_links`
  MODIFY `link_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_litespeed_url`
--
ALTER TABLE `wp_litespeed_url`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_litespeed_url_file`
--
ALTER TABLE `wp_litespeed_url_file`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_automations`
--
ALTER TABLE `wp_mailpoet_automations`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_automation_runs`
--
ALTER TABLE `wp_mailpoet_automation_runs`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_automation_run_logs`
--
ALTER TABLE `wp_mailpoet_automation_run_logs`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_automation_run_subjects`
--
ALTER TABLE `wp_mailpoet_automation_run_subjects`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_automation_versions`
--
ALTER TABLE `wp_mailpoet_automation_versions`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_custom_fields`
--
ALTER TABLE `wp_mailpoet_custom_fields`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_dynamic_segment_filters`
--
ALTER TABLE `wp_mailpoet_dynamic_segment_filters`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_feature_flags`
--
ALTER TABLE `wp_mailpoet_feature_flags`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_forms`
--
ALTER TABLE `wp_mailpoet_forms`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_log`
--
ALTER TABLE `wp_mailpoet_log`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_migrations`
--
ALTER TABLE `wp_mailpoet_migrations`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_newsletters`
--
ALTER TABLE `wp_mailpoet_newsletters`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_newsletter_links`
--
ALTER TABLE `wp_mailpoet_newsletter_links`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_newsletter_option`
--
ALTER TABLE `wp_mailpoet_newsletter_option`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_newsletter_option_fields`
--
ALTER TABLE `wp_mailpoet_newsletter_option_fields`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_newsletter_posts`
--
ALTER TABLE `wp_mailpoet_newsletter_posts`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_newsletter_segment`
--
ALTER TABLE `wp_mailpoet_newsletter_segment`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_newsletter_templates`
--
ALTER TABLE `wp_mailpoet_newsletter_templates`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_scheduled_tasks`
--
ALTER TABLE `wp_mailpoet_scheduled_tasks`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_segments`
--
ALTER TABLE `wp_mailpoet_segments`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_sending_queues`
--
ALTER TABLE `wp_mailpoet_sending_queues`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_settings`
--
ALTER TABLE `wp_mailpoet_settings`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_statistics_bounces`
--
ALTER TABLE `wp_mailpoet_statistics_bounces`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_statistics_clicks`
--
ALTER TABLE `wp_mailpoet_statistics_clicks`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_statistics_forms`
--
ALTER TABLE `wp_mailpoet_statistics_forms`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_statistics_newsletters`
--
ALTER TABLE `wp_mailpoet_statistics_newsletters`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_statistics_opens`
--
ALTER TABLE `wp_mailpoet_statistics_opens`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_statistics_unsubscribes`
--
ALTER TABLE `wp_mailpoet_statistics_unsubscribes`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_statistics_woocommerce_purchases`
--
ALTER TABLE `wp_mailpoet_statistics_woocommerce_purchases`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_stats_notifications`
--
ALTER TABLE `wp_mailpoet_stats_notifications`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_subscribers`
--
ALTER TABLE `wp_mailpoet_subscribers`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_subscriber_custom_field`
--
ALTER TABLE `wp_mailpoet_subscriber_custom_field`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_subscriber_segment`
--
ALTER TABLE `wp_mailpoet_subscriber_segment`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_subscriber_tag`
--
ALTER TABLE `wp_mailpoet_subscriber_tag`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_tags`
--
ALTER TABLE `wp_mailpoet_tags`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_user_agents`
--
ALTER TABLE `wp_mailpoet_user_agents`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_mailpoet_user_flags`
--
ALTER TABLE `wp_mailpoet_user_flags`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_nf3_actions`
--
ALTER TABLE `wp_nf3_actions`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_nf3_action_meta`
--
ALTER TABLE `wp_nf3_action_meta`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_nf3_chunks`
--
ALTER TABLE `wp_nf3_chunks`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_nf3_fields`
--
ALTER TABLE `wp_nf3_fields`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_nf3_field_meta`
--
ALTER TABLE `wp_nf3_field_meta`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_nf3_forms`
--
ALTER TABLE `wp_nf3_forms`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_nf3_form_meta`
--
ALTER TABLE `wp_nf3_form_meta`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_nf3_objects`
--
ALTER TABLE `wp_nf3_objects`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_nf3_object_meta`
--
ALTER TABLE `wp_nf3_object_meta`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_nf3_relationships`
--
ALTER TABLE `wp_nf3_relationships`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_options`
--
ALTER TABLE `wp_options`
  MODIFY `option_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_pieregister_code`
--
ALTER TABLE `wp_pieregister_code`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_pieregister_custom_user_roles`
--
ALTER TABLE `wp_pieregister_custom_user_roles`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_pieregister_invite_code_emails`
--
ALTER TABLE `wp_pieregister_invite_code_emails`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_pieregister_lockdowns`
--
ALTER TABLE `wp_pieregister_lockdowns`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_pieregister_redirect_settings`
--
ALTER TABLE `wp_pieregister_redirect_settings`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_pmpro_discount_codes`
--
ALTER TABLE `wp_pmpro_discount_codes`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_pmpro_discount_codes_uses`
--
ALTER TABLE `wp_pmpro_discount_codes_uses`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_pmpro_memberships_users`
--
ALTER TABLE `wp_pmpro_memberships_users`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_pmpro_membership_levelmeta`
--
ALTER TABLE `wp_pmpro_membership_levelmeta`
  MODIFY `meta_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_pmpro_membership_levels`
--
ALTER TABLE `wp_pmpro_membership_levels`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_pmpro_membership_ordermeta`
--
ALTER TABLE `wp_pmpro_membership_ordermeta`
  MODIFY `meta_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_pmpro_membership_orders`
--
ALTER TABLE `wp_pmpro_membership_orders`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_postmeta`
--
ALTER TABLE `wp_postmeta`
  MODIFY `meta_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_posts`
--
ALTER TABLE `wp_posts`
  MODIFY `ID` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_swpm_membership_meta_tbl`
--
ALTER TABLE `wp_swpm_membership_meta_tbl`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_swpm_membership_tbl`
--
ALTER TABLE `wp_swpm_membership_tbl`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_swpm_members_tbl`
--
ALTER TABLE `wp_swpm_members_tbl`
  MODIFY `member_id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_swpm_payments_tbl`
--
ALTER TABLE `wp_swpm_payments_tbl`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_termmeta`
--
ALTER TABLE `wp_termmeta`
  MODIFY `meta_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_terms`
--
ALTER TABLE `wp_terms`
  MODIFY `term_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_term_taxonomy`
--
ALTER TABLE `wp_term_taxonomy`
  MODIFY `term_taxonomy_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_usermeta`
--
ALTER TABLE `wp_usermeta`
  MODIFY `umeta_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_users`
--
ALTER TABLE `wp_users`
  MODIFY `ID` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_wc_admin_notes`
--
ALTER TABLE `wp_wc_admin_notes`
  MODIFY `note_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_wc_admin_note_actions`
--
ALTER TABLE `wp_wc_admin_note_actions`
  MODIFY `action_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_wc_customer_lookup`
--
ALTER TABLE `wp_wc_customer_lookup`
  MODIFY `customer_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_wc_download_log`
--
ALTER TABLE `wp_wc_download_log`
  MODIFY `download_log_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_wc_orders_meta`
--
ALTER TABLE `wp_wc_orders_meta`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_wc_order_addresses`
--
ALTER TABLE `wp_wc_order_addresses`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_wc_order_operational_data`
--
ALTER TABLE `wp_wc_order_operational_data`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_wc_product_download_directories`
--
ALTER TABLE `wp_wc_product_download_directories`
  MODIFY `url_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_wc_rate_limits`
--
ALTER TABLE `wp_wc_rate_limits`
  MODIFY `rate_limit_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_wc_tax_rate_classes`
--
ALTER TABLE `wp_wc_tax_rate_classes`
  MODIFY `tax_rate_class_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_wc_webhooks`
--
ALTER TABLE `wp_wc_webhooks`
  MODIFY `webhook_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_woocommerce_api_keys`
--
ALTER TABLE `wp_woocommerce_api_keys`
  MODIFY `key_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_woocommerce_attribute_taxonomies`
--
ALTER TABLE `wp_woocommerce_attribute_taxonomies`
  MODIFY `attribute_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_woocommerce_downloadable_product_permissions`
--
ALTER TABLE `wp_woocommerce_downloadable_product_permissions`
  MODIFY `permission_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_woocommerce_log`
--
ALTER TABLE `wp_woocommerce_log`
  MODIFY `log_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_woocommerce_order_itemmeta`
--
ALTER TABLE `wp_woocommerce_order_itemmeta`
  MODIFY `meta_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_woocommerce_order_items`
--
ALTER TABLE `wp_woocommerce_order_items`
  MODIFY `order_item_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_woocommerce_payment_tokenmeta`
--
ALTER TABLE `wp_woocommerce_payment_tokenmeta`
  MODIFY `meta_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_woocommerce_payment_tokens`
--
ALTER TABLE `wp_woocommerce_payment_tokens`
  MODIFY `token_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_woocommerce_sessions`
--
ALTER TABLE `wp_woocommerce_sessions`
  MODIFY `session_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_woocommerce_shipping_zones`
--
ALTER TABLE `wp_woocommerce_shipping_zones`
  MODIFY `zone_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_woocommerce_shipping_zone_locations`
--
ALTER TABLE `wp_woocommerce_shipping_zone_locations`
  MODIFY `location_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_woocommerce_shipping_zone_methods`
--
ALTER TABLE `wp_woocommerce_shipping_zone_methods`
  MODIFY `instance_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_woocommerce_tax_rates`
--
ALTER TABLE `wp_woocommerce_tax_rates`
  MODIFY `tax_rate_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_woocommerce_tax_rate_locations`
--
ALTER TABLE `wp_woocommerce_tax_rate_locations`
  MODIFY `location_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_wpforms_logs`
--
ALTER TABLE `wp_wpforms_logs`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_wpforms_payments`
--
ALTER TABLE `wp_wpforms_payments`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_wpforms_payment_meta`
--
ALTER TABLE `wp_wpforms_payment_meta`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_wpforms_tasks_meta`
--
ALTER TABLE `wp_wpforms_tasks_meta`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_wpmailsmtp_debug_events`
--
ALTER TABLE `wp_wpmailsmtp_debug_events`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wp_wpmailsmtp_tasks_meta`
--
ALTER TABLE `wp_wpmailsmtp_tasks_meta`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
