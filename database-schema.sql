-- ============================================
-- Affiliate System Database Schema
-- ============================================
-- Tạo các bảng cho hệ thống quản lý affiliate
-- Prefix: wp_ (WordPress default)
-- ============================================

SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

-- ============================================
-- 1. Bảng affiliates - Thông tin affiliates
-- ============================================
CREATE TABLE IF NOT EXISTS `wp_affiliates` (
  `affiliate_id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT(20) UNSIGNED NOT NULL,
  `affiliate_code` VARCHAR(50) NOT NULL,
  `status` ENUM('pending', 'active', 'inactive', 'rejected', 'suspended') NOT NULL DEFAULT 'pending',
  `first_name` VARCHAR(100) DEFAULT NULL,
  `last_name` VARCHAR(100) DEFAULT NULL,
  `email` VARCHAR(255) NOT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `website` VARCHAR(255) DEFAULT NULL,
  `promotion_method` VARCHAR(255) DEFAULT NULL,
  `payment_method` ENUM('paypal', 'bank_transfer', 'stripe', 'direct') DEFAULT NULL,
  `paypal_email` VARCHAR(255) DEFAULT NULL,
  `bank_name` VARCHAR(255) DEFAULT NULL,
  `bank_account_number` VARCHAR(100) DEFAULT NULL,
  `bank_account_name` VARCHAR(255) DEFAULT NULL,
  `bank_swift_code` VARCHAR(50) DEFAULT NULL,
  `stripe_account_id` VARCHAR(255) DEFAULT NULL,
  `terms_accepted` TINYINT(1) NOT NULL DEFAULT 0,
  `terms_accepted_at` DATETIME DEFAULT NULL,
  `rejection_reason` TEXT DEFAULT NULL,
  `notes` TEXT DEFAULT NULL,
  `total_commissions` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `paid_commissions` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `unpaid_commissions` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `total_referrals` INT(11) NOT NULL DEFAULT 0,
  `total_visits` INT(11) NOT NULL DEFAULT 0,
  `total_conversions` INT(11) NOT NULL DEFAULT 0,
  `conversion_rate` DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  `last_activity_at` DATETIME DEFAULT NULL,
  `registered_at` DATETIME NOT NULL,
  `approved_at` DATETIME DEFAULT NULL,
  `approved_by` BIGINT(20) UNSIGNED DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`affiliate_id`),
  UNIQUE KEY `unique_user_id` (`user_id`),
  UNIQUE KEY `unique_affiliate_code` (`affiliate_code`),
  KEY `idx_status` (`status`),
  KEY `idx_email` (`email`),
  KEY `idx_registered_at` (`registered_at`),
  KEY `idx_last_activity` (`last_activity_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 2. Bảng affiliate_links - Generated links (tạo trước để referrals có thể tham chiếu)
-- ============================================
CREATE TABLE IF NOT EXISTS `wp_affiliate_links` (
  `link_id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `affiliate_id` BIGINT(20) UNSIGNED NOT NULL,
  `original_url` TEXT NOT NULL,
  `affiliate_url` TEXT NOT NULL,
  `short_url` VARCHAR(255) DEFAULT NULL,
  `campaign` VARCHAR(255) DEFAULT NULL,
  `medium` VARCHAR(100) DEFAULT NULL,
  `source` VARCHAR(255) DEFAULT NULL,
  `link_type` ENUM('product', 'category', 'page', 'custom') DEFAULT 'custom',
  `product_id` BIGINT(20) UNSIGNED DEFAULT NULL,
  `category_id` BIGINT(20) UNSIGNED DEFAULT NULL,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `total_clicks` INT(11) NOT NULL DEFAULT 0,
  `unique_clicks` INT(11) NOT NULL DEFAULT 0,
  `total_conversions` INT(11) NOT NULL DEFAULT 0,
  `total_commission` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `expires_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`link_id`),
  KEY `idx_affiliate_id` (`affiliate_id`),
  KEY `idx_campaign` (`campaign`),
  KEY `idx_is_active` (`is_active`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_short_url` (`short_url`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 3. Bảng visits - Visit tracking (tạo trước để referrals có thể tham chiếu)
-- ============================================
CREATE TABLE IF NOT EXISTS `wp_affiliate_visits` (
  `visit_id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `affiliate_id` BIGINT(20) UNSIGNED NOT NULL,
  `link_id` BIGINT(20) UNSIGNED DEFAULT NULL,
  `ip_address` VARCHAR(45) DEFAULT NULL,
  `user_agent` TEXT DEFAULT NULL,
  `referrer_url` TEXT DEFAULT NULL,
  `landing_url` TEXT DEFAULT NULL,
  `campaign` VARCHAR(255) DEFAULT NULL,
  `medium` VARCHAR(100) DEFAULT NULL,
  `source` VARCHAR(255) DEFAULT NULL,
  `device_type` ENUM('desktop', 'mobile', 'tablet', 'unknown') DEFAULT 'unknown',
  `browser` VARCHAR(100) DEFAULT NULL,
  `os` VARCHAR(100) DEFAULT NULL,
  `country` VARCHAR(2) DEFAULT NULL,
  `city` VARCHAR(100) DEFAULT NULL,
  `is_unique` TINYINT(1) NOT NULL DEFAULT 1,
  `is_converted` TINYINT(1) NOT NULL DEFAULT 0,
  `converted_at` DATETIME DEFAULT NULL,
  `referral_id` BIGINT(20) UNSIGNED DEFAULT NULL,
  `session_id` VARCHAR(255) DEFAULT NULL,
  `cookie_value` VARCHAR(255) DEFAULT NULL,
  `expires_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`visit_id`),
  KEY `idx_affiliate_id` (`affiliate_id`),
  KEY `idx_link_id` (`link_id`),
  KEY `idx_ip_address` (`ip_address`),
  KEY `idx_is_converted` (`is_converted`),
  KEY `idx_referral_id` (`referral_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_cookie_value` (`cookie_value`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_campaign` (`campaign`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 4. Bảng referrals - Referrals/commissions
-- ============================================
CREATE TABLE IF NOT EXISTS `wp_affiliate_referrals` (
  `referral_id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `affiliate_id` BIGINT(20) UNSIGNED NOT NULL,
  `order_id` BIGINT(20) UNSIGNED DEFAULT NULL,
  `customer_id` BIGINT(20) UNSIGNED DEFAULT NULL,
  `customer_name` VARCHAR(255) DEFAULT NULL,
  `customer_email` VARCHAR(255) DEFAULT NULL,
  `visit_id` BIGINT(20) UNSIGNED DEFAULT NULL,
  `link_id` BIGINT(20) UNSIGNED DEFAULT NULL,
  `amount` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `commission_type` ENUM('percentage', 'fixed', 'hybrid') NOT NULL DEFAULT 'percentage',
  `commission_rate` DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  `commission_amount` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `status` ENUM('pending', 'approved', 'rejected', 'paid', 'cancelled') NOT NULL DEFAULT 'pending',
  `rejection_reason` TEXT DEFAULT NULL,
  `paid_at` DATETIME DEFAULT NULL,
  `payout_id` BIGINT(20) UNSIGNED DEFAULT NULL,
  `order_status` VARCHAR(50) DEFAULT NULL,
  `order_date` DATETIME DEFAULT NULL,
  `product_ids` TEXT DEFAULT NULL,
  `product_names` TEXT DEFAULT NULL,
  `metadata` LONGTEXT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`referral_id`),
  KEY `idx_affiliate_id` (`affiliate_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_visit_id` (`visit_id`),
  KEY `idx_link_id` (`link_id`),
  KEY `idx_status` (`status`),
  KEY `idx_payout_id` (`payout_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_order_date` (`order_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 5. Bảng payouts - Payout requests và payments
-- ============================================
CREATE TABLE IF NOT EXISTS `wp_affiliate_payouts` (
  `payout_id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `affiliate_id` BIGINT(20) UNSIGNED NOT NULL,
  `amount` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `currency` VARCHAR(3) NOT NULL DEFAULT 'USD',
  `payment_method` ENUM('paypal', 'bank_transfer', 'stripe', 'direct') NOT NULL,
  `payment_details` TEXT DEFAULT NULL,
  `status` ENUM('pending', 'processing', 'completed', 'failed', 'cancelled') NOT NULL DEFAULT 'pending',
  `transaction_id` VARCHAR(255) DEFAULT NULL,
  `transaction_fee` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `net_amount` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `referral_ids` TEXT DEFAULT NULL,
  `referral_count` INT(11) NOT NULL DEFAULT 0,
  `notes` TEXT DEFAULT NULL,
  `failure_reason` TEXT DEFAULT NULL,
  `processed_at` DATETIME DEFAULT NULL,
  `processed_by` BIGINT(20) UNSIGNED DEFAULT NULL,
  `completed_at` DATETIME DEFAULT NULL,
  `requested_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`payout_id`),
  KEY `idx_affiliate_id` (`affiliate_id`),
  KEY `idx_status` (`status`),
  KEY `idx_transaction_id` (`transaction_id`),
  KEY `idx_requested_at` (`requested_at`),
  KEY `idx_processed_at` (`processed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 6. Bảng commission_rules - Commission rules
-- ============================================
CREATE TABLE IF NOT EXISTS `wp_affiliate_commission_rules` (
  `rule_id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `rule_name` VARCHAR(255) NOT NULL,
  `rule_type` ENUM('global', 'product', 'category', 'affiliate', 'tier') NOT NULL DEFAULT 'global',
  `affiliate_id` BIGINT(20) UNSIGNED DEFAULT NULL,
  `product_id` BIGINT(20) UNSIGNED DEFAULT NULL,
  `category_id` BIGINT(20) UNSIGNED DEFAULT NULL,
  `tier_level` INT(11) DEFAULT NULL,
  `commission_type` ENUM('percentage', 'fixed', 'hybrid') NOT NULL DEFAULT 'percentage',
  `commission_rate` DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  `fixed_amount` DECIMAL(15,2) DEFAULT NULL,
  `min_order_amount` DECIMAL(15,2) DEFAULT NULL,
  `max_commission_amount` DECIMAL(15,2) DEFAULT NULL,
  `priority` INT(11) NOT NULL DEFAULT 0,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `valid_from` DATETIME DEFAULT NULL,
  `valid_until` DATETIME DEFAULT NULL,
  `conditions` LONGTEXT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`rule_id`),
  KEY `idx_rule_type` (`rule_type`),
  KEY `idx_affiliate_id` (`affiliate_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_is_active` (`is_active`),
  KEY `idx_priority` (`priority`),
  KEY `idx_valid_from` (`valid_from`),
  KEY `idx_valid_until` (`valid_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 7. Bảng affiliate_coupons - Coupon linking
-- ============================================
CREATE TABLE IF NOT EXISTS `wp_affiliate_coupons` (
  `coupon_id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `affiliate_id` BIGINT(20) UNSIGNED NOT NULL,
  `woocommerce_coupon_id` BIGINT(20) UNSIGNED DEFAULT NULL,
  `coupon_code` VARCHAR(100) NOT NULL,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `usage_count` INT(11) NOT NULL DEFAULT 0,
  `total_commission` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`coupon_id`),
  UNIQUE KEY `unique_coupon_code` (`coupon_code`),
  KEY `idx_affiliate_id` (`affiliate_id`),
  KEY `idx_woocommerce_coupon_id` (`woocommerce_coupon_id`),
  KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 8. Bảng affiliate_creatives - Creative assets
-- ============================================
CREATE TABLE IF NOT EXISTS `wp_affiliate_creatives` (
  `creative_id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `affiliate_id` BIGINT(20) UNSIGNED NOT NULL,
  `creative_name` VARCHAR(255) NOT NULL,
  `creative_type` ENUM('banner', 'text', 'email', 'social', 'video') NOT NULL DEFAULT 'banner',
  `file_url` TEXT DEFAULT NULL,
  `file_path` TEXT DEFAULT NULL,
  `width` INT(11) DEFAULT NULL,
  `height` INT(11) DEFAULT NULL,
  `html_code` LONGTEXT DEFAULT NULL,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `usage_count` INT(11) NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`creative_id`),
  KEY `idx_affiliate_id` (`affiliate_id`),
  KEY `idx_creative_type` (`creative_type`),
  KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 9. Bảng affiliate_notifications - Notifications
-- ============================================
CREATE TABLE IF NOT EXISTS `wp_affiliate_notifications` (
  `notification_id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `affiliate_id` BIGINT(20) UNSIGNED DEFAULT NULL,
  `user_id` BIGINT(20) UNSIGNED DEFAULT NULL,
  `type` VARCHAR(50) NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `message` TEXT NOT NULL,
  `related_id` BIGINT(20) UNSIGNED DEFAULT NULL,
  `related_type` VARCHAR(50) DEFAULT NULL,
  `is_read` TINYINT(1) NOT NULL DEFAULT 0,
  `read_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notification_id`),
  KEY `idx_affiliate_id` (`affiliate_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 10. Bảng affiliate_settings - Settings
-- ============================================
CREATE TABLE IF NOT EXISTS `wp_affiliate_settings` (
  `setting_id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `setting_key` VARCHAR(255) NOT NULL,
  `setting_value` LONGTEXT DEFAULT NULL,
  `setting_type` VARCHAR(50) DEFAULT 'string',
  `group_name` VARCHAR(100) DEFAULT 'general',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`setting_id`),
  UNIQUE KEY `unique_setting_key` (`setting_key`),
  KEY `idx_group_name` (`group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Fix Foreign Key Constraints (after all tables created)
-- ============================================

-- ============================================
-- Insert Default Commission Rule
-- ============================================
INSERT INTO `wp_affiliate_commission_rules` 
  (`rule_name`, `rule_type`, `commission_type`, `commission_rate`, `is_active`, `priority`) 
VALUES 
  ('Default Global Commission', 'global', 'percentage', 10.00, 1, 0)
ON DUPLICATE KEY UPDATE `rule_name` = `rule_name`;

-- ============================================
-- Insert Default Settings
-- ============================================
INSERT INTO `wp_affiliate_settings` (`setting_key`, `setting_value`, `setting_type`, `group_name`) VALUES
  ('cookie_duration', '30', 'integer', 'tracking'),
  ('minimum_payout', '50.00', 'decimal', 'payout'),
  ('default_commission_rate', '10.00', 'decimal', 'commission'),
  ('default_commission_type', 'percentage', 'string', 'commission'),
  ('require_approval', '1', 'boolean', 'registration'),
  ('auto_approve', '0', 'boolean', 'registration'),
  ('enable_email_notifications', '1', 'boolean', 'notifications')
ON DUPLICATE KEY UPDATE `setting_key` = `setting_key`;

SET FOREIGN_KEY_CHECKS = 1;

