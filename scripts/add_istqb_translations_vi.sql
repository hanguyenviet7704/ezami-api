-- ==========================================
-- ADD VIETNAMESE TRANSLATIONS FOR ISTQB SKILLS
-- ==========================================
-- This script adds Vietnamese translations for ISTQB_CTFL skills
-- to fix the bug where API returns "N/A" for skillNameVi.
--
-- Table: wp_fcom_translations
-- Entity Type: 'skill'
-- Field Name: 'name'
-- Language: 'vi'
--
-- Run this script after deploying the code fix for skill filtering.
-- ==========================================

-- Check if translations table exists
SELECT COUNT(*) as table_exists
FROM information_schema.tables
WHERE table_schema = 'wordpress'
AND table_name = 'wp_fcom_translations';

-- ==========================================
-- Chapter 1: Fundamentals of Testing
-- ==========================================

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Nền tảng kiểm thử'
FROM wp_ez_skills WHERE code = 'ISTQB_FUNDAMENTALS' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Kiểm thử là gì'
FROM wp_ez_skills WHERE code = 'ISTQB_WHAT_TESTING' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Mục tiêu kiểm thử'
FROM wp_ez_skills WHERE code = 'ISTQB_OBJECTIVES' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'QA và QC'
FROM wp_ez_skills WHERE code = 'ISTQB_QA_VS_QC' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Lỗi, khuyết tật và thất bại'
FROM wp_ez_skills WHERE code = 'ISTQB_ERRORS' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Phân tích nguyên nhân gốc rễ'
FROM wp_ez_skills WHERE code = 'ISTQB_ROOT_CAUSE' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Nguyên tắc kiểm thử'
FROM wp_ez_skills WHERE code = 'ISTQB_PRINCIPLES' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Kiểm thử chứng minh lỗi tồn tại'
FROM wp_ez_skills WHERE code = 'ISTQB_SHOWS_PRESENCE' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Kiểm thử toàn diện là bất khả thi'
FROM wp_ez_skills WHERE code = 'ISTQB_EXHAUSTIVE' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Kiểm thử sớm'
FROM wp_ez_skills WHERE code = 'ISTQB_EARLY_TESTING' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Lỗi tập trung thành cụm'
FROM wp_ez_skills WHERE code = 'ISTQB_DEFECT_CLUSTERING' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Nghịch lý thuốc trừ sâu'
FROM wp_ez_skills WHERE code = 'ISTQB_PESTICIDE' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Kiểm thử phụ thuộc ngữ cảnh'
FROM wp_ez_skills WHERE code = 'ISTQB_CONTEXT_DEPENDENT' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Ngụy biện về vắng mặt lỗi'
FROM wp_ez_skills WHERE code = 'ISTQB_ABSENCE_ERROR' AND certification_id = 'ISTQB_CTFL';

-- ==========================================
-- Chapter 2: Testing Throughout the SDLC
-- ==========================================

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Quy trình kiểm thử'
FROM wp_ez_skills WHERE code = 'ISTQB_TEST_PROCESS' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Lập kế hoạch kiểm thử'
FROM wp_ez_skills WHERE code = 'ISTQB_PLANNING' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Giám sát và kiểm soát kiểm thử'
FROM wp_ez_skills WHERE code = 'ISTQB_MONITORING_CONTROL' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Phân tích kiểm thử'
FROM wp_ez_skills WHERE code = 'ISTQB_ANALYSIS' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Thiết kế kiểm thử'
FROM wp_ez_skills WHERE code = 'ISTQB_DESIGN' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Triển khai kiểm thử'
FROM wp_ez_skills WHERE code = 'ISTQB_IMPLEMENTATION' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Thực thi kiểm thử'
FROM wp_ez_skills WHERE code = 'ISTQB_EXECUTION' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Hoàn thành kiểm thử'
FROM wp_ez_skills WHERE code = 'ISTQB_COMPLETION' AND certification_id = 'ISTQB_CTFL';

-- ==========================================
-- Chapter 3: Static Testing
-- ==========================================

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Kiểm thử tĩnh'
FROM wp_ez_skills WHERE code = 'ISTQB_STATIC_TESTING' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Đánh giá'
FROM wp_ez_skills WHERE code = 'ISTQB_REVIEWS' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Quy trình đánh giá'
FROM wp_ez_skills WHERE code = 'ISTQB_REVIEW_PROCESS' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Các loại đánh giá'
FROM wp_ez_skills WHERE code = 'ISTQB_REVIEW_TYPES' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Đánh giá phi chính thức'
FROM wp_ez_skills WHERE code = 'ISTQB_INFORMAL_REVIEW' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Đánh giá kỹ thuật'
FROM wp_ez_skills WHERE code = 'ISTQB_TECHNICAL_REVIEW' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Kiểm định'
FROM wp_ez_skills WHERE code = 'ISTQB_INSPECTION' AND certification_id = 'ISTQB_CTFL';

-- ==========================================
-- Chapter 4: Test Design Techniques
-- ==========================================

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Kỹ thuật thiết kế kiểm thử'
FROM wp_ez_skills WHERE code = 'ISTQB_TEST_TECHNIQUES' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Kiểm thử hộp đen'
FROM wp_ez_skills WHERE code = 'ISTQB_BLACK_BOX' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Kiểm thử hộp trắng'
FROM wp_ez_skills WHERE code = 'ISTQB_WHITE_BOX' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Phân vùng tương đương'
FROM wp_ez_skills WHERE code = 'ISTQB_EQUIVALENCE' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Phân tích giá trị biên'
FROM wp_ez_skills WHERE code = 'ISTQB_BOUNDARY_VALUE' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Kiểm thử bảng quyết định'
FROM wp_ez_skills WHERE code = 'ISTQB_DECISION_TABLE' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Kiểm thử chuyển trạng thái'
FROM wp_ez_skills WHERE code = 'ISTQB_STATE_TRANSITION' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Kiểm thử use case'
FROM wp_ez_skills WHERE code = 'ISTQB_USE_CASE' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Độ bao phủ câu lệnh'
FROM wp_ez_skills WHERE code = 'ISTQB_STATEMENT_COVERAGE' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Độ bao phủ nhánh'
FROM wp_ez_skills WHERE code = 'ISTQB_BRANCH_COVERAGE' AND certification_id = 'ISTQB_CTFL';

-- ==========================================
-- Chapter 5: Test Management
-- ==========================================

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Quản lý kiểm thử'
FROM wp_ez_skills WHERE code = 'ISTQB_TEST_MANAGEMENT' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Tổ chức kiểm thử'
FROM wp_ez_skills WHERE code = 'ISTQB_TEST_ORG' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Kiểm thử dựa trên rủi ro'
FROM wp_ez_skills WHERE code = 'ISTQB_RISK_BASED' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Quản lý lỗi'
FROM wp_ez_skills WHERE code = 'ISTQB_DEFECT_MANAGEMENT' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Độ đo kiểm thử'
FROM wp_ez_skills WHERE code = 'ISTQB_TEST_METRICS' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Quản lý cấu hình'
FROM wp_ez_skills WHERE code = 'ISTQB_CONFIGURATION' AND certification_id = 'ISTQB_CTFL';

-- ==========================================
-- Chapter 6: Tool Support
-- ==========================================

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Công cụ hỗ trợ kiểm thử'
FROM wp_ez_skills WHERE code = 'ISTQB_TOOL_SUPPORT' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Phân loại công cụ kiểm thử'
FROM wp_ez_skills WHERE code = 'ISTQB_TEST_TOOLS' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Lựa chọn công cụ'
FROM wp_ez_skills WHERE code = 'ISTQB_TOOL_SELECTION' AND certification_id = 'ISTQB_CTFL';

INSERT IGNORE INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Triển khai công cụ'
FROM wp_ez_skills WHERE code = 'ISTQB_TOOL_LIFECYCLE' AND certification_id = 'ISTQB_CTFL';

-- ==========================================
-- Verification Query
-- ==========================================

-- Count translations added
SELECT
    'Total ISTQB translations added' as description,
    COUNT(*) as count
FROM wp_fcom_translations
WHERE entity_type = 'skill'
  AND language = 'vi'
  AND entity_id IN (
      SELECT id FROM wp_ez_skills
      WHERE certification_id = 'ISTQB_CTFL'
      AND status = 'active'
  );

-- Show sample translations
SELECT
    s.code,
    s.name as name_en,
    t.translated_value as name_vi
FROM wp_ez_skills s
LEFT JOIN wp_fcom_translations t
    ON t.entity_type = 'skill'
    AND t.entity_id = s.id
    AND t.field_name = 'name'
    AND t.language = 'vi'
WHERE s.certification_id = 'ISTQB_CTFL'
  AND s.status = 'active'
ORDER BY s.sort_order
LIMIT 20;

-- ==========================================
-- NOTES:
-- ==========================================
-- 1. This script adds translations for the most common ISTQB_CTFL skills
-- 2. Additional skills may need translations - check skill codes in wp_ez_skills
-- 3. Run verification query to ensure translations were added
-- 4. Update SkillService to use translations from wp_fcom_translations table
-- ==========================================
