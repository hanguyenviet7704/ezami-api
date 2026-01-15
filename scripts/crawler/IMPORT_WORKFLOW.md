# Question Import Workflow

## ✅ Đã hoàn thành

### 1. Database Structure
- ✅ Dọn dẹp duplicate categories (giữ 29 categories gốc)
- ✅ Thêm 12 categories mới cho các certifications (ID 30-41)
- ✅ Map 10,000+ câu hỏi hiện có vào categories
- ✅ Sync 11,000+ questions vào `wp_ez_question_skills`

### 2. Code Updates
- ✅ Cập nhật `SkillService` sử dụng `wp_ez_skills` (4,650 skills)
- ✅ Cập nhật `DiagnosticService` trả về `firstQuestion` và `questions[]`
- ✅ Cập nhật career path mappings

### 3. Import Tools
- ✅ Tạo Python importer (`question_importer.py`)
- ✅ Tạo question generator (`generate_questions_from_docs.py`)
- ✅ Test import 10 câu hỏi AWS SAA thành công

## 📊 Current Database Stats

| Metric | Value |
|--------|-------|
| Total questions | 10,686 |
| Mapped to skills | 11,375 |
| Total skills | 4,650 |
| Total certifications | 36 |

### Questions by Certification

| Certification | Questions | Skills |
|---------------|-----------|--------|
| ISTQB_CTFL | 3,502 | 75 |
| CBAP | 3,050 | 32 |
| CCBA | 1,951 | 13 |
| PSM_I | 929 | 92 |
| SCRUM_PSPO_I | 776 | 57 |
| ISTQB_AGILE | 630 | 23 |
| ECBA | 567 | 7 |
| SCRUM_PSM_II | 131 | 47 |
| ISTQB_AI | 80 | 10 |
| **AWS_SAA_C03** | **10** | **10** ⭐ NEW |

## 📋 Next Steps to Import More Questions

### Option 1: Generate Templates (RECOMMENDED - SAFE)

```bash
cd scripts/crawler

# Generate question templates
python3 generate_questions_from_docs.py AWS_SAA_C03 templates/aws_saa.json
python3 generate_questions_from_docs.py KUBERNETES_CKA templates/cka.json
python3 generate_questions_from_docs.py AZURE_AZ104 templates/azure.json

# Edit the JSON files to fill in actual questions
# Then import:
python3 question_importer.py templates/aws_saa.json AWS_SAA_C03 30
python3 question_importer.py templates/cka.json KUBERNETES_CKA 34
python3 question_importer.py templates/azure.json AZURE_AZ104 33
```

### Option 2: Import from CSV

```bash
# Use the CSV template
cp templates/question_template.csv data/my_questions.csv

# Edit CSV file with your questions
# Then import:
python3 question_importer.py data/my_questions.csv AWS_SAA_C03 30 --csv
```

### Option 3: Manual SQL Import

```sql
-- Import directly via SQL
INSERT INTO wp_learndash_pro_quiz_question
(quiz_id, previous_id, sort, title, points, question, correct_msg, incorrect_msg,
 correct_same_text, tip_enabled, tip_msg, answer_type, show_points_in_box,
 answer_points_activated, answer_data, category_id, answer_points_diff_modus_activated,
 disable_correct, matrix_sort_answer_criteria_width, online)
VALUES
(0, 0, 1, 'Q_TITLE', 1, 'Question text?', 'Correct!', 'Incorrect',
 1, 0, '', 'single', 0, 0, 'a:4:{...}', 30, 0, 0, 20, 1);
```

## 🔧 Auto-Map to Skills

After importing questions, run this SQL to auto-map to skills:

```bash
cd scripts/crawler
./auto_map_to_skills.sh AWS_SAA_C03 30
```

Or manually:

```sql
INSERT IGNORE INTO wp_ez_question_skills (question_id, skill_id, weight, confidence, mapped_at)
SELECT
    q.id,
    (SELECT s.id FROM wp_ez_skills s
     WHERE s.certification_id = 'AWS_SAA_C03'
       AND s.status = 'active'
       AND NOT EXISTS (SELECT 1 FROM wp_ez_skills c WHERE c.parent_id = s.id)
     ORDER BY RAND() LIMIT 1),
    1.00, 'medium', NOW()
FROM wp_learndash_pro_quiz_question q
WHERE q.category_id = 30
  AND NOT EXISTS (SELECT 1 FROM wp_ez_question_skills qs WHERE qs.question_id = q.id);
```

## ⚠️ Legal Considerations

### DO NOT:
- ❌ Scrape actual exam questions (copyright violation)
- ❌ Use "exam dumps" from questionable sources
- ❌ Copy questions from paid platforms

### DO:
- ✅ Create original questions based on official docs
- ✅ Use officially provided free practice tests
- ✅ Generate questions from open-source materials with proper license
- ✅ Commission question writers / SMEs

## 📚 Recommended Sources for Question Creation

### AWS
- Official sample questions: https://aws.amazon.com/certification/certification-prep/
- AWS documentation: https://docs.aws.amazon.com/
- AWS Skill Builder (free tier): https://explore.skillbuilder.aws/

### Azure
- Official practice assessments: https://learn.microsoft.com/en-us/credentials/certifications/
- Microsoft Learn modules: https://learn.microsoft.com/

### Kubernetes
- Official docs: https://kubernetes.io/docs/
- CNCF training: https://www.cncf.io/certification/training/

### Other Certifications
- Vendor official documentation
- Official free practice tests
- Community-contributed original content with clear licensing

## 📈 Scaling Question Creation

For large-scale question generation:

1. **Hire SMEs** - Subject matter experts to write original questions
2. **AI-Assisted** - Use AI to draft, then have SMEs review/edit
3. **Community** - Crowdsource from users (with quality review)
4. **Partnerships** - Partner with exam prep companies

## 🔄 Category to Certification Mapping

| category_id | category_name | certification_id (wp_ez_skills) |
|-------------|---------------|--------------------------------|
| 30 | AWS_SAA_C03 | AWS_SAA_C03 |
| 31 | AWS_DVA_C02 | AWS_DVA_C02 |
| 32 | AWS_SAP_C02 | AWS_SAP_C02 |
| 33 | AZURE_AZ104 | AZURE_AZ104 |
| 34 | KUBERNETES_CKA | KUBERNETES_CKA |
| 35 | KUBERNETES_CKAD | KUBERNETES_CKAD |
| 36 | DOCKER_DCA | DOCKER_DCA |
| 37 | TERRAFORM | HASHICORP_TERRAFORM |
| 38 | JAVA_OCP_17 | JAVA_OCP_17 |
| 39 | SPRING_PRO | VMWARE_SPRING_PRO |
| 40 | SECURITY_PLUS | COMPTIA_SECURITY_PLUS |
| 41 | CISSP | ISC2_CISSP |
