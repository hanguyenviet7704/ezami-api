-- EIL Skills Seed Data
-- Seeds TOEIC skill taxonomy

-- Insert Category Level Skills (Level 1)
INSERT INTO eil_skills (code, name, name_vi, description, description_vi, category, level, weight, is_active, priority) VALUES
('LISTENING', 'Listening', 'Nghe', 'Listening comprehension skills', 'Kỹ năng nghe hiểu', 'LISTENING', 1, 1.00, TRUE, 1),
('READING', 'Reading', 'Đọc', 'Reading comprehension skills', 'Kỹ năng đọc hiểu', 'READING', 1, 1.00, TRUE, 2);

-- Get parent IDs for Listening and Reading
SET @listening_id = (SELECT id FROM eil_skills WHERE code = 'LISTENING');
SET @reading_id = (SELECT id FROM eil_skills WHERE code = 'READING');

-- Insert Subcategory Level Skills (Level 2) - Listening
INSERT INTO eil_skills (code, name, name_vi, description, description_vi, category, subcategory, level, parent_id, weight, is_active, priority) VALUES
('LC_PART1', 'Part 1: Photographs', 'Phần 1: Mô tả hình ảnh', 'Describe photographs', 'Mô tả hình ảnh', 'LISTENING', 'PART1', 2, @listening_id, 1.00, TRUE, 1),
('LC_PART2', 'Part 2: Question-Response', 'Phần 2: Hỏi đáp', 'Question and response', 'Câu hỏi và phản hồi', 'LISTENING', 'PART2', 2, @listening_id, 1.00, TRUE, 2),
('LC_PART3', 'Part 3: Conversations', 'Phần 3: Đoạn hội thoại', 'Short conversations', 'Đoạn hội thoại ngắn', 'LISTENING', 'PART3', 2, @listening_id, 1.00, TRUE, 3),
('LC_PART4', 'Part 4: Talks', 'Phần 4: Bài nói', 'Short talks', 'Bài nói ngắn', 'LISTENING', 'PART4', 2, @listening_id, 1.00, TRUE, 4);

-- Insert Subcategory Level Skills (Level 2) - Reading
INSERT INTO eil_skills (code, name, name_vi, description, description_vi, category, subcategory, level, parent_id, weight, is_active, priority) VALUES
('RC_PART5', 'Part 5: Incomplete Sentences', 'Phần 5: Điền vào chỗ trống', 'Complete sentences', 'Hoàn thành câu', 'READING', 'PART5', 2, @reading_id, 1.00, TRUE, 5),
('RC_PART6', 'Part 6: Text Completion', 'Phần 6: Hoàn thành đoạn văn', 'Text completion', 'Hoàn thành đoạn văn', 'READING', 'PART6', 2, @reading_id, 1.00, TRUE, 6),
('RC_PART7', 'Part 7: Reading Comprehension', 'Phần 7: Đọc hiểu', 'Reading passages', 'Đọc hiểu đoạn văn', 'READING', 'PART7', 2, @reading_id, 1.00, TRUE, 7);

-- Get parent IDs for subcategories
SET @lc_p1_id = (SELECT id FROM eil_skills WHERE code = 'LC_PART1');
SET @lc_p2_id = (SELECT id FROM eil_skills WHERE code = 'LC_PART2');
SET @lc_p3_id = (SELECT id FROM eil_skills WHERE code = 'LC_PART3');
SET @lc_p4_id = (SELECT id FROM eil_skills WHERE code = 'LC_PART4');
SET @rc_p5_id = (SELECT id FROM eil_skills WHERE code = 'RC_PART5');
SET @rc_p6_id = (SELECT id FROM eil_skills WHERE code = 'RC_PART6');
SET @rc_p7_id = (SELECT id FROM eil_skills WHERE code = 'RC_PART7');

-- Insert Skill Level (Level 3) - Part 1: Photographs
INSERT INTO eil_skills (code, name, name_vi, description, description_vi, category, subcategory, level, parent_id, weight, difficulty_range_min, difficulty_range_max, is_active, priority) VALUES
('LC_P1_OBJECT', 'Object Identification', 'Nhận diện đồ vật', 'Identify objects in photographs', 'Nhận diện đồ vật trong ảnh', 'LISTENING', 'PART1', 3, @lc_p1_id, 1.00, 1, 3, TRUE, 1),
('LC_P1_ACTION', 'Action Description', 'Mô tả hành động', 'Describe actions in photographs', 'Mô tả hành động trong ảnh', 'LISTENING', 'PART1', 3, @lc_p1_id, 1.00, 1, 4, TRUE, 2),
('LC_P1_LOCATION', 'Location/Setting', 'Vị trí/Bối cảnh', 'Identify location and setting', 'Nhận diện vị trí và bối cảnh', 'LISTENING', 'PART1', 3, @lc_p1_id, 1.00, 1, 4, TRUE, 3),
('LC_P1_PEOPLE', 'People Description', 'Mô tả người', 'Describe people in photographs', 'Mô tả người trong ảnh', 'LISTENING', 'PART1', 3, @lc_p1_id, 1.00, 1, 4, TRUE, 4);

-- Insert Skill Level (Level 3) - Part 2: Question-Response
INSERT INTO eil_skills (code, name, name_vi, description, description_vi, category, subcategory, level, parent_id, weight, difficulty_range_min, difficulty_range_max, is_active, priority) VALUES
('LC_P2_WH', 'WH Questions', 'Câu hỏi WH', 'Who, What, When, Where, Why, How questions', 'Câu hỏi Who, What, When, Where, Why, How', 'LISTENING', 'PART2', 3, @lc_p2_id, 1.00, 1, 4, TRUE, 1),
('LC_P2_YESNO', 'Yes/No Questions', 'Câu hỏi Yes/No', 'Yes/No and tag questions', 'Câu hỏi Yes/No và câu hỏi đuôi', 'LISTENING', 'PART2', 3, @lc_p2_id, 1.00, 1, 3, TRUE, 2),
('LC_P2_CHOICE', 'Choice Questions', 'Câu hỏi lựa chọn', 'Alternative and choice questions', 'Câu hỏi lựa chọn', 'LISTENING', 'PART2', 3, @lc_p2_id, 1.00, 2, 4, TRUE, 3),
('LC_P2_STATEMENT', 'Statement Responses', 'Phản hồi câu phát biểu', 'Responses to statements', 'Phản hồi cho câu phát biểu', 'LISTENING', 'PART2', 3, @lc_p2_id, 1.00, 2, 5, TRUE, 4);

-- Insert Skill Level (Level 3) - Part 3: Conversations
INSERT INTO eil_skills (code, name, name_vi, description, description_vi, category, subcategory, level, parent_id, weight, difficulty_range_min, difficulty_range_max, is_active, priority) VALUES
('LC_P3_MAIN', 'Main Idea', 'Ý chính', 'Understand main idea of conversation', 'Hiểu ý chính của đoạn hội thoại', 'LISTENING', 'PART3', 3, @lc_p3_id, 1.00, 2, 4, TRUE, 1),
('LC_P3_DETAIL', 'Detail Comprehension', 'Chi tiết', 'Understand specific details', 'Hiểu chi tiết cụ thể', 'LISTENING', 'PART3', 3, @lc_p3_id, 1.00, 2, 5, TRUE, 2),
('LC_P3_INFERENCE', 'Inference', 'Suy luận', 'Make inferences from conversation', 'Suy luận từ hội thoại', 'LISTENING', 'PART3', 3, @lc_p3_id, 1.00, 3, 5, TRUE, 3),
('LC_P3_INTENT', 'Speaker Intent', 'Ý định người nói', 'Understand speaker intent', 'Hiểu ý định của người nói', 'LISTENING', 'PART3', 3, @lc_p3_id, 1.00, 3, 5, TRUE, 4),
('LC_P3_GRAPHIC', 'Graphic Questions', 'Câu hỏi đồ họa', 'Questions with graphics', 'Câu hỏi kèm hình ảnh/biểu đồ', 'LISTENING', 'PART3', 3, @lc_p3_id, 1.00, 3, 5, TRUE, 5);

-- Insert Skill Level (Level 3) - Part 4: Talks
INSERT INTO eil_skills (code, name, name_vi, description, description_vi, category, subcategory, level, parent_id, weight, difficulty_range_min, difficulty_range_max, is_active, priority) VALUES
('LC_P4_ANNOUNCE', 'Announcements', 'Thông báo', 'Understand announcements', 'Hiểu các thông báo', 'LISTENING', 'PART4', 3, @lc_p4_id, 1.00, 2, 4, TRUE, 1),
('LC_P4_INSTRUCT', 'Instructions', 'Hướng dẫn', 'Understand instructions and directions', 'Hiểu hướng dẫn và chỉ dẫn', 'LISTENING', 'PART4', 3, @lc_p4_id, 1.00, 2, 5, TRUE, 2),
('LC_P4_AD', 'Advertisements', 'Quảng cáo', 'Understand advertisements', 'Hiểu quảng cáo', 'LISTENING', 'PART4', 3, @lc_p4_id, 1.00, 2, 4, TRUE, 3),
('LC_P4_MESSAGE', 'Messages/Voicemails', 'Tin nhắn/Hộp thư thoại', 'Understand messages and voicemails', 'Hiểu tin nhắn và hộp thư thoại', 'LISTENING', 'PART4', 3, @lc_p4_id, 1.00, 2, 5, TRUE, 4),
('LC_P4_NEWS', 'News/Reports', 'Tin tức/Báo cáo', 'Understand news and reports', 'Hiểu tin tức và báo cáo', 'LISTENING', 'PART4', 3, @lc_p4_id, 1.00, 3, 5, TRUE, 5);

-- Insert Skill Level (Level 3) - Part 5: Incomplete Sentences
INSERT INTO eil_skills (code, name, name_vi, description, description_vi, category, subcategory, level, parent_id, weight, difficulty_range_min, difficulty_range_max, is_active, priority) VALUES
('RC_P5_VOCAB', 'Vocabulary', 'Từ vựng', 'Vocabulary in context', 'Từ vựng trong ngữ cảnh', 'READING', 'PART5', 3, @rc_p5_id, 1.00, 1, 5, TRUE, 1),
('RC_P5_TENSE', 'Verb Tenses', 'Thì động từ', 'Verb tense usage', 'Sử dụng thì động từ', 'READING', 'PART5', 3, @rc_p5_id, 1.00, 1, 4, TRUE, 2),
('RC_P5_POS', 'Parts of Speech', 'Loại từ', 'Parts of speech identification', 'Nhận diện loại từ', 'READING', 'PART5', 3, @rc_p5_id, 1.00, 1, 4, TRUE, 3),
('RC_P5_PREP', 'Prepositions', 'Giới từ', 'Preposition usage', 'Sử dụng giới từ', 'READING', 'PART5', 3, @rc_p5_id, 1.00, 1, 4, TRUE, 4),
('RC_P5_CONJ', 'Conjunctions', 'Liên từ', 'Conjunction usage', 'Sử dụng liên từ', 'READING', 'PART5', 3, @rc_p5_id, 1.00, 2, 4, TRUE, 5),
('RC_P5_PRONOUN', 'Pronouns', 'Đại từ', 'Pronoun usage and reference', 'Sử dụng đại từ', 'READING', 'PART5', 3, @rc_p5_id, 1.00, 1, 4, TRUE, 6),
('RC_P5_SUBJECT_VERB', 'Subject-Verb Agreement', 'Hòa hợp chủ-vị', 'Subject-verb agreement', 'Sự hòa hợp chủ ngữ - động từ', 'READING', 'PART5', 3, @rc_p5_id, 1.00, 2, 4, TRUE, 7);

-- Insert Skill Level (Level 3) - Part 6: Text Completion
INSERT INTO eil_skills (code, name, name_vi, description, description_vi, category, subcategory, level, parent_id, weight, difficulty_range_min, difficulty_range_max, is_active, priority) VALUES
('RC_P6_VOCAB_CONTEXT', 'Contextual Vocabulary', 'Từ vựng ngữ cảnh', 'Vocabulary in extended context', 'Từ vựng trong ngữ cảnh mở rộng', 'READING', 'PART6', 3, @rc_p6_id, 1.00, 2, 5, TRUE, 1),
('RC_P6_GRAMMAR', 'Grammar in Context', 'Ngữ pháp ngữ cảnh', 'Grammar usage in context', 'Ngữ pháp trong ngữ cảnh', 'READING', 'PART6', 3, @rc_p6_id, 1.00, 2, 5, TRUE, 2),
('RC_P6_SENTENCE', 'Sentence Insertion', 'Chèn câu', 'Insert appropriate sentences', 'Chèn câu phù hợp', 'READING', 'PART6', 3, @rc_p6_id, 1.00, 3, 5, TRUE, 3),
('RC_P6_COHERENCE', 'Text Coherence', 'Mạch lạc văn bản', 'Understand text coherence', 'Hiểu sự mạch lạc của văn bản', 'READING', 'PART6', 3, @rc_p6_id, 1.00, 3, 5, TRUE, 4);

-- Insert Skill Level (Level 3) - Part 7: Reading Comprehension
INSERT INTO eil_skills (code, name, name_vi, description, description_vi, category, subcategory, level, parent_id, weight, difficulty_range_min, difficulty_range_max, is_active, priority) VALUES
('RC_P7_SINGLE', 'Single Passages', 'Đoạn đơn', 'Single passage comprehension', 'Đọc hiểu đoạn văn đơn', 'READING', 'PART7', 3, @rc_p7_id, 1.00, 2, 4, TRUE, 1),
('RC_P7_DOUBLE', 'Double Passages', 'Đoạn đôi', 'Double passage comprehension', 'Đọc hiểu đoạn văn đôi', 'READING', 'PART7', 3, @rc_p7_id, 1.00, 3, 5, TRUE, 2),
('RC_P7_TRIPLE', 'Triple Passages', 'Đoạn ba', 'Triple passage comprehension', 'Đọc hiểu đoạn văn ba', 'READING', 'PART7', 3, @rc_p7_id, 1.00, 4, 5, TRUE, 3),
('RC_P7_MAIN_IDEA', 'Main Idea', 'Ý chính', 'Identify main idea', 'Xác định ý chính', 'READING', 'PART7', 3, @rc_p7_id, 1.00, 2, 4, TRUE, 4),
('RC_P7_DETAIL', 'Detail Finding', 'Tìm chi tiết', 'Find specific details', 'Tìm chi tiết cụ thể', 'READING', 'PART7', 3, @rc_p7_id, 1.00, 2, 4, TRUE, 5),
('RC_P7_INFERENCE', 'Inference', 'Suy luận', 'Make inferences', 'Suy luận từ đoạn văn', 'READING', 'PART7', 3, @rc_p7_id, 1.00, 3, 5, TRUE, 6),
('RC_P7_VOCAB_CONTEXT', 'Vocabulary in Context', 'Từ vựng ngữ cảnh', 'Understand vocabulary in context', 'Hiểu từ vựng trong ngữ cảnh', 'READING', 'PART7', 3, @rc_p7_id, 1.00, 2, 5, TRUE, 7),
('RC_P7_PURPOSE', 'Author Purpose', 'Mục đích tác giả', 'Understand author purpose', 'Hiểu mục đích của tác giả', 'READING', 'PART7', 3, @rc_p7_id, 1.00, 3, 5, TRUE, 8),
('RC_P7_NOT_STATED', 'NOT/TRUE Questions', 'Câu hỏi NOT/TRUE', 'NOT mentioned or TRUE questions', 'Câu hỏi KHÔNG được đề cập hoặc ĐÚNG', 'READING', 'PART7', 3, @rc_p7_id, 1.00, 3, 5, TRUE, 9);
