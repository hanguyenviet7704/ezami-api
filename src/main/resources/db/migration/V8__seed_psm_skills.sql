-- PSM I Skills Seed Data
-- Seeds Professional Scrum Master I skill taxonomy

-- Insert Category Level Skills (Level 1)
INSERT INTO eil_skills (code, name, name_vi, description, description_vi, category, level, weight, is_active, priority) VALUES
('PSM_SCRUM_THEORY', 'Scrum Theory', 'Lý thuyết Scrum', 'Understanding the foundational theory behind Scrum framework', 'Hiểu nền tảng lý thuyết Scrum', 'PSM', 1, 1.00, TRUE, 1),
('PSM_SCRUM_VALUES', 'Scrum Values', 'Giá trị Scrum', 'The five Scrum values', 'Năm giá trị Scrum', 'PSM', 1, 1.00, TRUE, 2),
('PSM_SCRUM_TEAM', 'Scrum Team', 'Đội Scrum', 'Scrum Team composition and responsibilities', 'Thành phần và trách nhiệm của đội Scrum', 'PSM', 1, 1.00, TRUE, 3),
('PSM_SCRUM_EVENTS', 'Scrum Events', 'Sự kiện Scrum', 'The five Scrum events', 'Năm sự kiện Scrum', 'PSM', 1, 1.00, TRUE, 4),
('PSM_SCRUM_ARTIFACTS', 'Scrum Artifacts', 'Hiện vật Scrum', 'Scrum artifacts and commitments', 'Hiện vật và cam kết Scrum', 'PSM', 1, 1.00, TRUE, 5);

-- Get parent IDs
SET @theory_id = (SELECT id FROM eil_skills WHERE code = 'PSM_SCRUM_THEORY');
SET @values_id = (SELECT id FROM eil_skills WHERE code = 'PSM_SCRUM_VALUES');
SET @team_id = (SELECT id FROM eil_skills WHERE code = 'PSM_SCRUM_TEAM');
SET @events_id = (SELECT id FROM eil_skills WHERE code = 'PSM_SCRUM_EVENTS');
SET @artifacts_id = (SELECT id FROM eil_skills WHERE code = 'PSM_SCRUM_ARTIFACTS');

-- Insert Level 2 - Scrum Theory
INSERT INTO eil_skills (code, name, name_vi, description, description_vi, category, subcategory, level, parent_id, weight, is_active, priority) VALUES
('PSM_EMPIRICISM', 'Empiricism', 'Chủ nghĩa thực nghiệm', 'Empirical process control theory', 'Lý thuyết kiểm soát quy trình thực nghiệm', 'PSM', 'THEORY', 2, @theory_id, 1.00, TRUE, 1),
('PSM_LEAN_THINKING', 'Lean Thinking', 'Tư duy tinh gọn', 'Reduce waste and focus on essentials', 'Giảm lãng phí và tập trung vào yếu tố cốt lõi', 'PSM', 'THEORY', 2, @theory_id, 1.00, TRUE, 2);

-- Insert Level 2 - Scrum Team
INSERT INTO eil_skills (code, name, name_vi, description, description_vi, category, subcategory, level, parent_id, weight, is_active, priority) VALUES
('PSM_DEVELOPERS', 'Developers', 'Developers', 'Role of Developers in Scrum', 'Vai trò của Developers trong Scrum', 'PSM', 'TEAM', 2, @team_id, 1.00, TRUE, 1),
('PSM_PRODUCT_OWNER', 'Product Owner', 'Product Owner', 'Role of Product Owner', 'Vai trò của Product Owner', 'PSM', 'TEAM', 2, @team_id, 1.00, TRUE, 2),
('PSM_SCRUM_MASTER', 'Scrum Master', 'Scrum Master', 'Role of Scrum Master', 'Vai trò của Scrum Master', 'PSM', 'TEAM', 2, @team_id, 1.00, TRUE, 3);

-- Insert Level 2 - Scrum Events
INSERT INTO eil_skills (code, name, name_vi, description, description_vi, category, subcategory, level, parent_id, weight, is_active, priority) VALUES
('PSM_SPRINT', 'Sprint', 'Sprint', 'Container for all Scrum events', 'Container chứa tất cả sự kiện Scrum', 'PSM', 'EVENTS', 2, @events_id, 1.00, TRUE, 1),
('PSM_SPRINT_PLANNING', 'Sprint Planning', 'Sprint Planning', 'Initiates Sprint by laying out work', 'Khởi đầu Sprint bằng việc lập kế hoạch', 'PSM', 'EVENTS', 2, @events_id, 1.00, TRUE, 2),
('PSM_DAILY_SCRUM', 'Daily Scrum', 'Daily Scrum', 'Daily 15-minute event for Developers', 'Sự kiện hàng ngày 15 phút cho Developers', 'PSM', 'EVENTS', 2, @events_id, 1.00, TRUE, 3),
('PSM_SPRINT_REVIEW', 'Sprint Review', 'Sprint Review', 'Inspect outcome and determine adaptations', 'Kiểm tra kết quả và xác định điều chỉnh', 'PSM', 'EVENTS', 2, @events_id, 1.00, TRUE, 4),
('PSM_SPRINT_RETRO', 'Sprint Retrospective', 'Sprint Retrospective', 'Plan ways to increase quality and effectiveness', 'Lập kế hoạch nâng cao chất lượng và hiệu quả', 'PSM', 'EVENTS', 2, @events_id, 1.00, TRUE, 5);

-- Insert Level 2 - Scrum Artifacts
INSERT INTO eil_skills (code, name, name_vi, description, description_vi, category, subcategory, level, parent_id, weight, is_active, priority) VALUES
('PSM_PRODUCT_BACKLOG', 'Product Backlog', 'Product Backlog', 'Ordered list of work for product', 'Danh sách công việc có thứ tự cho sản phẩm', 'PSM', 'ARTIFACTS', 2, @artifacts_id, 1.00, TRUE, 1),
('PSM_SPRINT_BACKLOG', 'Sprint Backlog', 'Sprint Backlog', 'Plan for achieving Sprint Goal', 'Kế hoạch đạt Sprint Goal', 'PSM', 'ARTIFACTS', 2, @artifacts_id, 1.00, TRUE, 2),
('PSM_INCREMENT', 'Increment', 'Increment', 'Usable output of Sprint', 'Kết quả có thể sử dụng được của Sprint', 'PSM', 'ARTIFACTS', 2, @artifacts_id, 1.00, TRUE, 3);

-- Get Level 2 IDs
SET @empiricism_id = (SELECT id FROM eil_skills WHERE code = 'PSM_EMPIRICISM');
SET @developers_id = (SELECT id FROM eil_skills WHERE code = 'PSM_DEVELOPERS');
SET @po_id = (SELECT id FROM eil_skills WHERE code = 'PSM_PRODUCT_OWNER');
SET @sm_id = (SELECT id FROM eil_skills WHERE code = 'PSM_SCRUM_MASTER');
SET @sprint_id = (SELECT id FROM eil_skills WHERE code = 'PSM_SPRINT');
SET @planning_id = (SELECT id FROM eil_skills WHERE code = 'PSM_SPRINT_PLANNING');
SET @daily_id = (SELECT id FROM eil_skills WHERE code = 'PSM_DAILY_SCRUM');
SET @review_id = (SELECT id FROM eil_skills WHERE code = 'PSM_SPRINT_REVIEW');
SET @retro_id = (SELECT id FROM eil_skills WHERE code = 'PSM_SPRINT_RETRO');
SET @pb_id = (SELECT id FROM eil_skills WHERE code = 'PSM_PRODUCT_BACKLOG');
SET @sb_id = (SELECT id FROM eil_skills WHERE code = 'PSM_SPRINT_BACKLOG');
SET @inc_id = (SELECT id FROM eil_skills WHERE code = 'PSM_INCREMENT');

-- Insert Level 3 - Empiricism pillars
INSERT INTO eil_skills (code, name, name_vi, description, description_vi, category, subcategory, level, parent_id, weight, difficulty_range_min, difficulty_range_max, is_active, priority) VALUES
('PSM_TRANSPARENCY', 'Transparency', 'Minh bạch', 'Process and work visible to performers and receivers', 'Quy trình và công việc có thể nhìn thấy', 'PSM', 'THEORY', 3, @empiricism_id, 1.00, 1, 4, TRUE, 1),
('PSM_INSPECTION', 'Inspection', 'Kiểm tra', 'Frequent inspection of artifacts and progress', 'Kiểm tra thường xuyên hiện vật và tiến độ', 'PSM', 'THEORY', 3, @empiricism_id, 1.00, 1, 4, TRUE, 2),
('PSM_ADAPTATION', 'Adaptation', 'Thích ứng', 'Adjusting process when deviating from limits', 'Điều chỉnh quy trình khi lệch khỏi giới hạn', 'PSM', 'THEORY', 3, @empiricism_id, 1.00, 1, 4, TRUE, 3);

-- Insert Level 3 - Scrum Values
INSERT INTO eil_skills (code, name, name_vi, description, description_vi, category, subcategory, level, parent_id, weight, difficulty_range_min, difficulty_range_max, is_active, priority) VALUES
('PSM_COMMITMENT', 'Commitment', 'Cam kết', 'Team commits to goals and supporting each other', 'Đội cam kết với mục tiêu và hỗ trợ lẫn nhau', 'PSM', 'VALUES', 3, @values_id, 1.00, 1, 3, TRUE, 1),
('PSM_FOCUS', 'Focus', 'Tập trung', 'Focus on Sprint work for best progress', 'Tập trung vào công việc Sprint để đạt tiến bộ tốt nhất', 'PSM', 'VALUES', 3, @values_id, 1.00, 1, 3, TRUE, 2),
('PSM_OPENNESS', 'Openness', 'Cởi mở', 'Open about work and challenges', 'Cởi mở về công việc và thách thức', 'PSM', 'VALUES', 3, @values_id, 1.00, 1, 3, TRUE, 3),
('PSM_RESPECT', 'Respect', 'Tôn trọng', 'Respect each other as capable people', 'Tôn trọng lẫn nhau như những người có năng lực', 'PSM', 'VALUES', 3, @values_id, 1.00, 1, 3, TRUE, 4),
('PSM_COURAGE', 'Courage', 'Can đảm', 'Courage to do right thing and work on tough problems', 'Can đảm làm đúng và giải quyết vấn đề khó', 'PSM', 'VALUES', 3, @values_id, 1.00, 1, 3, TRUE, 5);

-- Insert Level 3 - Team Characteristics
INSERT INTO eil_skills (code, name, name_vi, description, description_vi, category, subcategory, level, parent_id, weight, difficulty_range_min, difficulty_range_max, is_active, priority) VALUES
('PSM_SELF_MANAGING', 'Self-Managing', 'Tự quản lý', 'Team decides who, how, what to work on', 'Đội tự quyết định ai, cách nào, làm gì', 'PSM', 'TEAM', 3, @team_id, 1.00, 2, 4, TRUE, 10),
('PSM_CROSS_FUNCTIONAL', 'Cross-Functionality', 'Đa chức năng', 'All skills needed to create value', 'Tất cả kỹ năng cần thiết để tạo giá trị', 'PSM', 'TEAM', 3, @team_id, 1.00, 2, 4, TRUE, 11);

-- Insert Level 3 - Sprint Details
INSERT INTO eil_skills (code, name, name_vi, description, description_vi, category, subcategory, level, parent_id, weight, difficulty_range_min, difficulty_range_max, is_active, priority) VALUES
('PSM_SPRINT_GOAL', 'Sprint Goal', 'Sprint Goal', 'Single objective for the Sprint', 'Mục tiêu duy nhất cho Sprint', 'PSM', 'EVENTS', 3, @sprint_id, 1.00, 1, 4, TRUE, 1),
('PSM_SPRINT_LENGTH', 'Sprint Length', 'Thời lượng Sprint', 'One month or less for consistency', 'Một tháng hoặc ít hơn để nhất quán', 'PSM', 'EVENTS', 3, @sprint_id, 1.00, 1, 3, TRUE, 2);

-- Insert Level 3 - Artifact Commitments
INSERT INTO eil_skills (code, name, name_vi, description, description_vi, category, subcategory, level, parent_id, weight, difficulty_range_min, difficulty_range_max, is_active, priority) VALUES
('PSM_PRODUCT_GOAL', 'Product Goal', 'Product Goal', 'Long-term objective for the Scrum Team', 'Mục tiêu dài hạn cho đội Scrum', 'PSM', 'ARTIFACTS', 3, @pb_id, 1.00, 2, 4, TRUE, 1),
('PSM_DEFINITION_OF_DONE', 'Definition of Done', 'Definition of Done', 'Quality standards for Increment', 'Tiêu chuẩn chất lượng cho Increment', 'PSM', 'ARTIFACTS', 3, @inc_id, 1.00, 2, 5, TRUE, 1);
