# Migration Options: wp_ez_diagnostic_questions

## Discovery

Found **6,390 high-quality questions** in `wp_ez_diagnostic_questions` table with:
- ✅ **100% have explanations** (avg 316 chars)
- ✅ **Clean JSON format** (options_json, correct_answer_json)
- ✅ **Built-in skill mapping** (skill_id, skill_code)
- ✅ **Multi-language support** (language_code)
- ✅ **Difficulty levels** (easy, medium, hard)
- ✅ **2,752 DEV questions** (Golang, Java, Python, System Design, etc.)
- ✅ **159 AWS questions** (vs 10 in LearnDash)

## Comparison

| Metric | wp_ez_diagnostic_questions | wp_learndash_pro_quiz_question |
|--------|---------------------------|-------------------------------|
| **Total questions** | 6,390 | 5,654 |
| **Format** | Clean JSON | PHP serialized |
| **Explanations** | 100% (avg 316 chars) | 100% (avg 425 chars) |
| **Skill mapping** | Built-in | Via junction table |
| **Multi-language** | Yes (vi, en) | No |
| **DEV questions** | 2,752 | 0 |
| **AWS questions** | 159 | 10 |
| **Quality** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |

### Categories Comparison

| Category | wp_ez | LearnDash | Winner |
|----------|-------|-----------|--------|
| PSM_I | 222 | 734 | LearnDash |
| ISTQB_CTFL | 420 | 543 | LearnDash |
| CBAP | 0 | 823 | LearnDash |
| CCBA | 0 | 810 | LearnDash |
| **AWS_SAA_C03** | **159** | **10** | **wp_ez** ⭐ |
| **JAVA_OCP_17** | **260** | **0** | **wp_ez** ⭐ |
| **DEV_GOLANG** | **294** | **0** | **wp_ez** ⭐ |
| **DEV_PYTHON** | **238** | **0** | **wp_ez** ⭐ |
| **DOCKER_DCA** | **198** | **12** | **wp_ez** ⭐ |

**Conclusion:** Each source has unique strengths. **Hybrid approach recommended.**

---

## Option 1: Migrate to wp_ez_diagnostic_questions (RECOMMENDED)

### Pros
- ✅ Clean JSON format (easier to parse)
- ✅ Built-in skill mapping
- ✅ Better data model
- ✅ Multi-language ready
- ✅ +2,752 DEV questions immediately available
- ✅ Better AWS/Cloud coverage

### Cons
- ⚠️ Need code refactoring (DiagnosticService, PracticeService)
- ⚠️ Lose 5,654 LearnDash questions (unless merged)

### Implementation

1. **Create new entity:**
   ```java
   @Entity
   @Table(name = "wp_ez_diagnostic_questions")
   public class WpEzDiagnosticQuestionEntity {
       private Long id;
       private String categoryCode;
       private Long skillId;
       private String questionText;
       private String questionType;
       @Column(columnDefinition = "json")
       private String optionsJson;
       @Column(columnDefinition = "json")
       private String correctAnswerJson;
       private String explanation;
       // ...
   }
   ```

2. **Create repository:**
   ```java
   public interface WpEzDiagnosticQuestionRepository extends JpaRepository<...> {
       List<WpEzDiagnosticQuestionEntity> findByCategoryCodeAndStatus(String categoryCode, String status);
       List<WpEzDiagnosticQuestionEntity> findBySkillIdAndDifficultyAndStatus(...);
   }
   ```

3. **Update services to use new entity**

**Estimated effort:** 4-6 hours

---

## Option 2: Import to LearnDash (Quick Fix)

### Pros
- ✅ No code changes needed
- ✅ Keep existing architecture
- ✅ Combine both sources: 6,390 + 5,654 = 12,044 questions
- ✅ Can implement today

### Cons
- ⚠️ Convert JSON → PHP serialized format
- ⚠️ Lose clean data model
- ⚠️ More complex migration script

### Implementation Script

Create `10_import_ez_questions_to_learndash.sql`:
```sql
-- Import wp_ez_diagnostic_questions to LearnDash format
INSERT INTO wp_learndash_pro_quiz_question
(quiz_id, previous_id, sort, title, points, question,
 correct_msg, incorrect_msg, correct_same_text, tip_enabled,
 tip_msg, answer_type, show_points_in_box, answer_points_activated,
 answer_data, category_id, answer_points_diff_modus_activated,
 disable_correct, matrix_sort_answer_criteria_width, online)
SELECT
    0, -- quiz_id
    0, -- previous_id
    @row_number := @row_number + 1, -- sort
    CONCAT(category_code, '_', id), -- title
    1, -- points
    question_text, -- question
    explanation, -- correct_msg
    CONCAT('Incorrect. ', explanation), -- incorrect_msg
    0, -- correct_same_text
    0, -- tip_enabled
    '', -- tip_msg
    CASE question_type
        WHEN 'single_choice' THEN 'single'
        WHEN 'multiple_choice' THEN 'multiple'
        WHEN 'true_false' THEN 'single'
    END, -- answer_type
    0, 0, -- show_points_in_box, answer_points_activated
    convert_json_to_php_serialized(options_json, correct_answer_json), -- answer_data (needs function)
    get_category_id_from_code(category_code), -- category_id (needs mapping)
    0, 0, 20, 1 -- defaults
FROM wp_ez_diagnostic_questions
CROSS JOIN (SELECT @row_number := (SELECT MAX(id) FROM wp_learndash_pro_quiz_question)) r
WHERE status = 'active';
```

**Estimated effort:** 2-3 hours (mostly writing conversion functions)

---

## Option 3: Hybrid Approach (BEST)

Use both tables with priority fallback:

### Architecture
```
DiagnosticService / PracticeService
        ↓
  QuestionProvider (new abstraction)
        ↓
    ┌───────┴───────┐
    ↓               ↓
wp_ez_diagnostic  wp_learndash
(Primary)         (Fallback)
```

### Benefits
- ✅ Best of both worlds
- ✅ Use wp_ez for DEV/Cloud/new certs
- ✅ Use LearnDash for Scrum/BA/ISTQB
- ✅ Gradual migration path
- ✅ No data loss

### Implementation

1. **Create QuestionProvider interface:**
   ```java
   public interface QuestionProvider {
       List<QuestionResponse> getQuestionsByCategory(String categoryCode, int count);
       QuestionResponse getQuestionById(Long id);
   }
   ```

2. **Implement providers:**
   - `WpEzQuestionProvider` (primary)
   - `LearnDashQuestionProvider` (fallback)

3. **Update services:**
   ```java
   // Try wp_ez first, fallback to LearnDash
   questions = wpEzProvider.getQuestions(certCode);
   if (questions.isEmpty()) {
       questions = learnDashProvider.getQuestions(certCode);
   }
   ```

**Estimated effort:** 6-8 hours

---

## Recommendation: Option 3 (Hybrid)

### Phase 1: Immediate (Today)
1. Create WpEzDiagnosticQuestionEntity
2. Add to DiagnosticService as primary source
3. Fallback to LearnDash if wp_ez has no questions

**Result:** Immediately gain 2,752 DEV questions + 159 AWS questions

### Phase 2: Next Sprint
1. Import unique LearnDash questions to wp_ez
2. Migrate all services to wp_ez
3. Deprecate LearnDash questions

**Result:** Single clean data source with 12,000+ questions

---

## Quick Win: Import DEV Questions Now

Since DEV questions only exist in wp_ez, we can import them immediately without conflicts:

```bash
python3 scripts/import_ez_to_learndash.py \
  --categories "DEV_GOLANG,DEV_JAVA,DEV_PYTHON,DEV_SYSTEM_DESIGN" \
  --start-category-id 42
```

This adds **2,752 questions** with zero risk!

---

## Decision Matrix

| Factor | Option 1 (Migrate) | Option 2 (Import) | Option 3 (Hybrid) |
|--------|-------------------|-------------------|-------------------|
| **Effort** | High (6h) | Medium (3h) | High (8h) |
| **Risk** | Medium | Low | Low |
| **Questions gained** | +736 | +6,390 | +6,390 |
| **Data quality** | Best | Good | Best |
| **Future-proof** | ✅ Yes | ⚠️ No | ✅ Yes |
| **Time to production** | 1 week | Today | 3 days |

**Recommended:** Start with **Option 3 Phase 1** (hybrid with wp_ez primary) for quick gains.

Would you like me to implement Option 3?
