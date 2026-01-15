# Certification Data Integrity Report

**Generated:** 2026-01-07
**Database:** ezami-api (WordPress integration)

## Executive Summary

### ✅ Good News
- **All 36 certifications** are active and have both skills and questions mapped
- **All certifications** have at least 100 questions (sufficient for diagnostic tests)
- **Database structure** is complete and functional

### ⚠️ Critical Issues Found

**1,046 skills across 34 certifications have NO questions mapped** (~32% of all skills)

This means:
- APIs return skills that cannot be tested
- Diagnostic tests may fail to cover all skill areas
- User skill mastery tracking is incomplete
- Frontend may display empty skill categories

---

## Detailed Analysis

### 1. Certifications Summary (All Active)

| Certification | Skills | Questions | Status |
|--------------|--------|-----------|--------|
| ISTQB_CTFL | 103 | 1703 | ✅ Good |
| DEV_GOLANG | 201 | 588 | ⚠️ 54 empty skills |
| PSM_I | 93 | 956 | ✅ Excellent (only 1 empty) |
| SCRUM_PSPO_I | 78 | 811 | ⚠️ 21 empty skills |
| CBAP | 32 | 873 | ✅ Good |
| CCBA | 13 | 826 | ✅ Good |
| DEV_PYTHON | 164 | 476 | ⚠️ 45 empty skills |
| ... | ... | ... | ... |

**Full list:** 36 certifications, 3,976 total skills, 10,165 total questions

---

### 2. Skills Without Questions (TOP 10 Worst Certifications)

| Rank | Certification ID | Total Skills | Empty Skills | Empty % | Questions |
|------|------------------|--------------|--------------|---------|-----------|
| 1 | **DEV_REACT** | 167 | **54** | 32.3% | 226 |
| 2 | **DEV_GOLANG** | 201 | **54** | 26.9% | 588 |
| 3 | **JAVA_OCP_17** | 182 | **52** | 28.6% | 260 |
| 4 | **DEV_NODEJS** | 167 | **52** | 31.1% | 230 |
| 5 | **DEV_SYSTEM_DESIGN** | 193 | **52** | 26.9% | 281 |
| 6 | **DEV_SQL_DATABASE** | 191 | **50** | 26.2% | 282 |
| 7 | **DEV_DEVOPS** | 174 | **47** | 27.0% | 254 |
| 8 | **DEV_FRONTEND** | 174 | **45** | 25.9% | 258 |
| 9 | **DEV_PYTHON** | 164 | **45** | 27.4% | 476 |
| 10 | **DEV_JAVASCRIPT_TS** | 152 | **44** | 28.9% | 216 |

**Pattern:** Developer-focused certifications (DEV_*) have the most unmapped skills.

---

### 3. Example Empty Skills (AWS_DOP_C02)

Sample skills with 0 questions:

```
- DOP_SDLC (SDLC Automation)
- DOP_CICD_DESIGN (CI/CD Pipeline Design)
- DOP_DEPLOY_STRATEGIES (Deployment Strategies)
- DOP_TESTING (Testing Automation)
- DOP_CONFIG_MGMT (Configuration Management)
- DOP_IAC (Infrastructure as Code)
- DOP_MONITORING (Monitoring and Logging)
- DOP_INCIDENT_MGMT (Incident Management)
... (24 total empty skills)
```

---

## Impact on APIs

### Affected Endpoints

#### ❌ `/api/certifications` (GET all certifications)
**Issue:** Returns `skillCount` including empty skills
**Impact:** Users see inflated skill counts
**Example:**
```json
{
  "certificationId": "DEV_REACT",
  "skillCount": 167,  // ❌ But 54 skills have NO questions!
  "questionCount": 226
}
```

#### ❌ `/api/certifications/{certificationId}/skills` (GET skills list)
**Issue:** Returns all skills, including those with 0 questions
**Impact:** Frontend displays skills that cannot be practiced
**Example:**
```json
[
  {
    "skillId": 123,
    "code": "REACT_HOOKS_ADV",
    "name": "Advanced React Hooks",
    "questionCount": 0  // ❌ Cannot be tested!
  }
]
```

#### ⚠️ `/api/certifications/{certificationId}/skills/tree` (GET skill tree)
**Issue:** Tree includes parent skills with no testable leaf skills
**Impact:** Collapsed categories may be empty when expanded

#### ⚠️ `/api/eil/diagnostic/start` (POST start diagnostic)
**Issue:** May fail to generate questions for certain skill areas
**Impact:** Diagnostic results incomplete for skill coverage

#### ⚠️ `/api/eil/practice/next-question` (POST get next question)
**Issue:** Adaptive practice cannot target empty skills
**Impact:** Users cannot practice specific weak skills

---

## Root Cause Analysis

### Why do we have empty skills?

1. **Skill taxonomy created upfront** (from certification blueprints)
2. **Questions added incrementally** (not all skills covered yet)
3. **No validation** to prevent saving skills without questions
4. **Legacy data migration** may have created orphaned skills

### Database Schema

```
wp_ez_certifications (36 rows)
    ↓ (1-to-many)
wp_ez_skills (3,976 rows) ← ❌ Some skills orphaned
    ↓ (many-to-many)
wp_ez_question_skills (mapping table)
    ↓
wp_learndash_pro_quiz_master (questions)
```

**Problem:** No foreign key constraint ensures skill → questions relationship.

---

## Recommended Solutions

### Solution 1: Filter Empty Skills in API Responses (Quick Fix)

**Priority:** HIGH
**Effort:** 2-4 hours
**Impact:** Immediate improvement

#### Changes Required:

**File:** [CertificationSkillService.java](src/main/java/com/hth/udecareer/eil/service/CertificationSkillService.java)

```java
// Method: getAllCertifications()
// BEFORE:
.skillCount(skillCountMap.getOrDefault(cert.getCertificationId(), 0L).intValue())

// AFTER:
.skillCount(getSkillsWithQuestionsCount(cert.getCertificationId()))
```

```java
// Method: getSkillsList()
// BEFORE:
List<WpEzSkillEntity> skills = skillRepository
    .findByCertificationIdAndStatusOrderBySortOrderAsc(certificationId, "active");

// AFTER:
List<WpEzSkillEntity> skills = skillRepository
    .findByCertificationIdAndStatusOrderBySortOrderAsc(certificationId, "active")
    .stream()
    .filter(skill -> questionSkillRepository.countBySkillId(skill.getId()) > 0)
    .collect(Collectors.toList());
```

#### API Response Changes:

```json
// BEFORE:
{
  "certificationId": "DEV_REACT",
  "skillCount": 167,
  "questionCount": 226
}

// AFTER:
{
  "certificationId": "DEV_REACT",
  "skillCount": 113,  // ✅ Only skills with questions
  "questionCount": 226
}
```

---

### Solution 2: Add Database Validation (Medium-term)

**Priority:** MEDIUM
**Effort:** 1 day
**Impact:** Prevent future issues

#### Changes Required:

1. **Add Flyway migration:**
   ```sql
   -- V10__add_skill_question_validation.sql

   -- Mark skills as inactive if they have no questions
   UPDATE wp_ez_skills s
   LEFT JOIN wp_ez_question_skills qs ON s.id = qs.skill_id
   SET s.status = 'inactive'
   WHERE qs.skill_id IS NULL
     AND s.status = 'active';

   -- Add index for performance
   CREATE INDEX idx_skill_id ON wp_ez_question_skills(skill_id);
   CREATE INDEX idx_status ON wp_ez_skills(status);
   ```

2. **Add scheduled job to verify integrity:**
   ```java
   @Scheduled(cron = "0 0 3 * * *") // Daily at 3 AM
   public void verifySkillIntegrity() {
       List<WpEzSkillEntity> emptySkills = skillRepository.findSkillsWithoutQuestions();
       if (!emptySkills.isEmpty()) {
           log.warn("Found {} skills without questions", emptySkills.size());
           // Send alert to admin
       }
   }
   ```

---

### Solution 3: Data Cleanup (Long-term)

**Priority:** LOW
**Effort:** 2-4 weeks (content team work)
**Impact:** Complete data integrity

#### Options:

**Option A: Deactivate empty skills**
```sql
UPDATE wp_ez_skills s
LEFT JOIN wp_ez_question_skills qs ON s.id = qs.skill_id
SET s.status = 'inactive'
WHERE qs.skill_id IS NULL AND s.status = 'active';
```
- ✅ Quick fix
- ❌ Loses skill taxonomy completeness

**Option B: Map existing questions to empty skills**
```sql
-- Assign questions to empty skills manually or via ML matching
INSERT INTO wp_ez_question_skills (question_id, skill_id)
SELECT q.id, s.id
FROM wp_learndash_pro_quiz_master q
JOIN wp_ez_skills s ON ... -- matching logic
WHERE s.id NOT IN (SELECT skill_id FROM wp_ez_question_skills);
```
- ✅ Preserves skill taxonomy
- ✅ Improves question coverage
- ❌ Requires manual review/AI assistance

**Option C: Create questions for empty skills**
- ✅ Complete coverage
- ❌ Most time-consuming (content creation)

---

## Testing Strategy

### Automated Tests to Add

**File:** `CertificationSkillServiceTest.java`

```java
@Test
public void testGetAllCertifications_ShouldNotIncludeEmptySkills() {
    List<CertificationResponse> certifications = service.getAllCertifications();

    for (CertificationResponse cert : certifications) {
        List<CertificationSkillResponse> skills = service.getSkillsList(cert.getCertificationId());

        for (CertificationSkillResponse skill : skills) {
            int questionCount = service.getQuestionIdsForSkill(skill.getSkillId()).size();
            assertTrue(questionCount > 0,
                "Skill " + skill.getCode() + " has no questions");
        }
    }
}

@Test
public void testSkillCounts_ShouldMatchActualQuestionsCount() {
    List<CertificationResponse> certifications = service.getAllCertifications();

    for (CertificationResponse cert : certifications) {
        List<Long> skillIds = skillRepository
            .findByCertificationIdAndStatusOrderBySortOrderAsc(cert.getCertificationId(), "active")
            .stream()
            .map(WpEzSkillEntity::getId)
            .collect(Collectors.toList());

        long skillsWithQuestions = skillIds.stream()
            .filter(skillId -> questionSkillRepository.countBySkillId(skillId) > 0)
            .count();

        assertEquals(skillsWithQuestions, cert.getSkillCount(),
            "Skill count mismatch for " + cert.getCertificationId());
    }
}
```

---

## Migration Plan

### Phase 1: Immediate Fix (This Week)

1. ✅ Add filter to `CertificationSkillService` methods
2. ✅ Add unit tests
3. ✅ Deploy to staging
4. ✅ Verify API responses
5. ✅ Deploy to production

**Files to modify:**
- [CertificationSkillService.java](src/main/java/com/hth/udecareer/eil/service/CertificationSkillService.java)
- Add: `CertificationSkillServiceTest.java`

### Phase 2: Database Validation (Next Sprint)

1. Add Flyway migration
2. Add scheduled integrity check job
3. Add admin alert system
4. Monitor logs for issues

**Files to create:**
- `src/main/resources/db/migration/V10__add_skill_question_validation.sql`
- `src/main/java/com/hth/udecareer/eil/service/SkillIntegrityService.java`

### Phase 3: Data Cleanup (Ongoing)

1. Review empty skills with content team
2. Map existing questions or create new ones
3. Update skill taxonomy as needed

---

## Monitoring & Alerts

### Metrics to Track

```yaml
metrics:
  - name: skills_without_questions
    query: "SELECT COUNT(*) FROM wp_ez_skills s LEFT JOIN wp_ez_question_skills qs ON s.id = qs.skill_id WHERE s.status = 'active' AND qs.skill_id IS NULL"
    threshold: 0
    alert: "Skills without questions detected"

  - name: certifications_with_low_questions
    query: "SELECT COUNT(*) FROM wp_ez_certifications c JOIN (...) WHERE question_count < 100"
    threshold: 0
    alert: "Certification has fewer than 100 questions"

  - name: api_response_accuracy
    endpoint: "/api/certifications"
    validation: "skillCount should match skills with questions"
```

### Dashboard Queries

```sql
-- Daily report: Skills added vs Questions added
SELECT
    DATE(created_at) as date,
    COUNT(DISTINCT skill_id) as skills_added,
    COUNT(DISTINCT question_id) as questions_added
FROM wp_ez_question_skills
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY DATE(created_at)
ORDER BY date DESC;
```

---

## Appendix

### SQL Queries for Investigation

See: [verify_certification_data_integrity.sql](scripts/verify_certification_data_integrity.sql)

**Key queries:**
1. Certifications without skills
2. Certifications without questions
3. Full certification summary
4. Skills without questions
5. Questions without quiz posts
6. Active certifications ready for use
7. Certifications with issues

### API Endpoints Affected

- ✅ `/api/certifications` - List all
- ✅ `/api/certifications/{id}` - Get one
- ✅ `/api/certifications/{id}/skills` - Skills list
- ✅ `/api/certifications/{id}/skills/tree` - Skill tree
- ⚠️ `/api/eil/diagnostic/start` - May skip empty skills
- ⚠️ `/api/eil/practice/next-question` - Cannot target empty skills

---

## Conclusion

**Summary:**
- ✅ All certifications functional with sufficient questions
- ⚠️ 1,046 skills (~32%) have no questions mapped
- 🔧 Quick fix: Filter empty skills in API responses
- 📊 Long-term: Data cleanup and validation

**Next Steps:**
1. Implement Solution 1 (filter empty skills) this week
2. Add automated tests
3. Plan content review with product team
4. Monitor API metrics post-deployment

**Risk:** Medium - APIs work but return incomplete data
**Priority:** High - Affects user experience and diagnostic accuracy
