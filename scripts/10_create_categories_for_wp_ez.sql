-- Script 10: Create categories for wp_ez_diagnostic_questions

-- Add new categories for codes not yet in LearnDash (starting from ID 42)
INSERT IGNORE INTO wp_learndash_pro_quiz_category (category_id, category_name) VALUES
-- DEV categories (42-55)
(42, 'DEV_GOLANG'),
(43, 'DEV_SYSTEM_DESIGN'),
(44, 'DEV_SQL_DATABASE'),
(45, 'DEV_FRONTEND'),
(46, 'DEV_DEVOPS'),
(47, 'DEV_BACKEND'),
(48, 'DEV_PYTHON'),
(49, 'DEV_API_DESIGN'),
(50, 'DEV_SOFTWARE_ARCH'),
(51, 'DEV_NODEJS'),
(52, 'DEV_REACT'),
(53, 'DEV_JAVASCRIPT_TS'),

-- Cloud categories (already exist but ensure)
-- (30-33, 37 already exist for AWS, AZURE, TERRAFORM)

-- Additional cloud/infra (54-60)
(54, 'GCP_ACE'),
(55, 'AWS_DOP_C02'),
(56, 'AWS_SAP_C02'),
(57, 'KUBERNETES_CKAD'),
(58, 'AGILE_SCRUM_MASTER'),
(59, 'PMI_PMP');

-- Create mapping table for quick lookup
CREATE TEMPORARY TABLE category_code_mapping (
    category_code VARCHAR(50),
    category_id INT
);

INSERT INTO category_code_mapping VALUES
-- Existing mappings
('PSM_I', 1),
('ISTQB_CTFL', 25),
('CCBA', 14),
('CBAP', 26),
('ECBA', 15),
('SCRUM_PSPO_I', 11),
('SCRUM_PSM_II', 12),
('ISTQB_AGILE', 28),
('ISTQB_AI', 29),
('AWS_SAA_C03', 30),
('AWS_DVA_C02', 31),
('AWS_SAP_C02', 56),
('AWS_DOP_C02', 55),
('AZURE_AZ104', 33),
('KUBERNETES_CKA', 34),
('KUBERNETES_CKAD', 57),
('DOCKER_DCA', 36),
('HASHICORP_TERRAFORM', 37),
('JAVA_OCP_17', 38),
('VMWARE_SPRING_PRO', 39),
('COMPTIA_SECURITY_PLUS', 40),
('ISC2_CISSP', 41),
-- New DEV mappings
('DEV_GOLANG', 42),
('DEV_SYSTEM_DESIGN', 43),
('DEV_SQL_DATABASE', 44),
('DEV_FRONTEND', 45),
('DEV_DEVOPS', 46),
('DEV_BACKEND', 47),
('DEV_PYTHON', 48),
('DEV_API_DESIGN', 49),
('DEV_SOFTWARE_ARCH', 50),
('DEV_NODEJS', 51),
('DEV_REACT', 52),
('DEV_JAVASCRIPT_TS', 53),
('GCP_ACE', 54),
('AGILE_SCRUM_MASTER', 58),
('PMI_PMP', 59);

-- Verify
SELECT 'Category mapping created:' as info;
SELECT COUNT(*) as total_mappings FROM category_code_mapping;

SELECT 'Sample mappings:' as info;
SELECT * FROM category_code_mapping LIMIT 10;
