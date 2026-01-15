# 🔴 BUG REPORT: ISTQB Foundation Hiển Thị Skills Sai

**Date:** 2026-01-07
**Priority:** P0 - CRITICAL
**Status:** ROOT CAUSE IDENTIFIED - FIX IN PROGRESS

---

## 📋 Problem Statement

Khi user hoàn thành diagnostic test cho **ISTQB Foundation**, kết quả hiển thị skills của **English test** (TOEIC Listening) thay vì skills kiểm thử phần mềm.

### User Sees (WRONG ❌):
- Nhận diện đồ vật (Object Identification)
- Câu hỏi Yes/No (Yes/No Questions)
- Câu hỏi WH (WH Questions)
- Mô tả người (People Description)
- Vị trí/Bố cảnh (Location/Setting)
- Mô tả hành động (Action Description)

### User Should See (CORRECT ✅):
- Fundamentals of Testing
- Test Design Techniques
- Static Testing
- Test Management
- Test Levels
- Test Types

---

## 🔍 Root Cause Analysis

### Bug Location

**File:** [MasteryService.java:226-234](src/main/java/com/hth/udecareer/eil/service/MasteryService.java#L226-L234)

```java
/**
 * ❌ BUG: This method does NOT filter by certification!
 * Returns top 10 skills from ANY certification the user has practiced.
 */
public List<SkillMasteryResponse> getAllSkillResults(Long userId, int limit) {
    List<EilSkillMasteryEntity> masteries = getUserMasteries(userId);  // ❌ Gets ALL masteries

    return masteries.stream()
            .limit(limit)  // ❌ Takes first 10, regardless of certification
            .map(this::toSkillMasteryResponse)
            .filter(java.util.Objects::nonNull)
            .toList();
}
```

**Called by:** [DiagnosticService.java:498](src/main/java/com/hth/udecareer/eil/service/DiagnosticService.java#L498)

```java
// ❌ BUG: No certification code passed!
var skillResults = masteryService.getAllSkillResults(userId, 10);

return DiagnosticResultResponse.builder()
        // ...
        .skillResults(skillResults)  // ❌ Wrong skills returned
        .build();
```

### Why This Happens

1. **User completes English diagnostic first** → Skills saved to `eil_skill_mastery` table
2. **User then completes ISTQB diagnostic** → New skills saved
3. **DiagnosticService** calls `getAllSkillResults(userId, 10)`
4. **MasteryService** returns **first 10 skills** from `eil_skill_mastery` table
5. **Database sorts** by `id` (creation time) → English skills come first
6. **Result:** ISTQB diagnostic shows English skills ❌

### Database Verification

```sql
-- Skills in database for ISTQB are CORRECT:
SELECT id, code, name, certification_id
FROM wp_ez_skills
WHERE certification_id = 'ISTQB_CTFL' AND status = 'active'
LIMIT 5;

-- Results:
-- 2126, ISTQB_FUNDAMENTALS, Fundamentals of Testing, ISTQB_CTFL
-- 2127, ISTQB_WHAT_TESTING, What is Testing, ISTQB_CTFL
-- 2128, ISTQB_OBJECTIVES, Testing Objectives, ISTQB_CTFL
-- 2129, ISTQB_QA_VS_QC, QA vs QC, ISTQB_CTFL
-- 2130, ISTQB_ERRORS, Errors, Defects, Failures, ISTQB_CTFL
```

✅ **Database has correct skills**
❌ **API returns wrong skills due to missing filter**

---

## 💡 Solution

### Fix 1: Add Certification Filter to MasteryService

**File:** `src/main/java/com/hth/udecareer/eil/service/MasteryService.java`

**Add new method:**

```java
/**
 * Get skill results filtered by certification.
 *
 * @param userId User ID
 * @param certificationId Certification ID (e.g., "ISTQB_CTFL", "PSM_I")
 * @param limit Max number of results
 * @return List of skill mastery responses for the specified certification
 */
public List<SkillMasteryResponse> getSkillResultsByCertification(
        Long userId,
        String certificationId,
        int limit) {

    List<EilSkillMasteryEntity> masteries = getUserMasteries(userId);

    return masteries.stream()
            .filter(mastery -> {
                try {
                    EilSkillEntity skill = skillService.getSkillById(mastery.getSkillId());
                    return skill != null && certificationId.equals(skill.getCertificationId());
                } catch (AppException e) {
                    log.warn("Skill not found: {}", mastery.getSkillId());
                    return false;
                }
            })
            .limit(limit)
            .map(this::toSkillMasteryResponse)
            .filter(java.util.Objects::nonNull)
            .toList();
}
```

### Fix 2: Update DiagnosticService to Use Certification Filter

**File:** `src/main/java/com/hth/udecareer/eil/service/DiagnosticService.java`

**Line 498 (in finishDiagnostic):**

```java
// BEFORE (❌):
var skillResults = masteryService.getAllSkillResults(userId, 10);

// AFTER (✅):
String certificationCode = extractCertificationCode(attempt);
var skillResults = certificationCode != null
        ? masteryService.getSkillResultsByCertification(userId, certificationCode, 10)
        : masteryService.getAllSkillResults(userId, 10);
```

**Line 711 (in getResult):**

```java
// BEFORE (❌):
var skillResults = masteryService.getAllSkillResults(userId, 10);

// AFTER (✅):
String certificationCode = extractCertificationCode(attempt);
var skillResults = certificationCode != null
        ? masteryService.getSkillResultsByCertification(userId, certificationCode, 10)
        : masteryService.getAllSkillResults(userId, 10);
```

**Add helper method:**

```java
/**
 * Extract certification code from diagnostic attempt metadata.
 */
private String extractCertificationCode(EilDiagnosticAttemptEntity attempt) {
    try {
        if (attempt.getMetadata() != null) {
            Map<String, Object> metadata = objectMapper.readValue(
                    attempt.getMetadata(),
                    new TypeReference<Map<String, Object>>() {});
            return (String) metadata.get("certificationCode");
        }
    } catch (Exception e) {
        log.warn("Failed to extract certificationCode from metadata: {}", e.getMessage());
    }

    // Fallback: use testType if it's a known certification code
    String testType = attempt.getTestType();
    if (testType != null && !testType.equals("TOEIC") && !testType.equals("IELTS")) {
        return testType;
    }

    return null;
}
```

---

## 🌐 Fix 3: Add Vietnamese Translations for ISTQB Skills

**Issue:** API trả về `skillNameVi = "N/A"` vì không có translations.

**Solution:** Add translations to `wp_fcom_translations` table.

**SQL Script:** See `scripts/add_istqb_translations_vi.sql`

Sample translations:
```sql
INSERT INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
VALUES
('skill', 2126, 'name', 'vi', 'Nền tảng kiểm thử'),
('skill', 2127, 'name', 'vi', 'Kiểm thử là gì'),
('skill', 2128, 'name', 'vi', 'Mục tiêu kiểm thử'),
('skill', 2129, 'name', 'vi', 'QA và QC'),
('skill', 2130, 'name', 'vi', 'Lỗi, khuyết tật, thất bại');
-- ... 98 more rows
```

---

## 📊 Testing Plan

### Unit Tests

**File:** `src/test/java/com/hth/udecareer/eil/service/MasteryServiceTest.java`

```java
@Test
void testGetSkillResultsByCertification_ShouldFilterCorrectly() {
    // Given
    Long userId = 1L;
    String certificationId = "ISTQB_CTFL";

    // Mock mastery data (mix of ISTQB and English skills)
    when(masteryRepository.findByUserId(userId))
            .thenReturn(Arrays.asList(
                    createMastery(1L, 2126L), // ISTQB skill
                    createMastery(2L, 1001L), // English skill
                    createMastery(3L, 2127L)  // ISTQB skill
            ));

    when(skillService.getSkillById(2126L))
            .thenReturn(createSkill(2126L, "ISTQB_FUNDAMENTALS", "ISTQB_CTFL"));
    when(skillService.getSkillById(1001L))
            .thenReturn(createSkill(1001L, "LC_P1_OBJ_ID", "TOEIC_LISTENING"));
    when(skillService.getSkillById(2127L))
            .thenReturn(createSkill(2127L, "ISTQB_WHAT_TESTING", "ISTQB_CTFL"));

    // When
    List<SkillMasteryResponse> results = masteryService
            .getSkillResultsByCertification(userId, certificationId, 10);

    // Then
    assertEquals(2, results.size());
    assertTrue(results.stream().allMatch(r ->
            r.getSkillCode().startsWith("ISTQB_")));
}
```

### Integration Test

**Script:** `test_diagnostic_skills.sh`

```bash
#!/bin/bash

API_URL="http://localhost:8090"
TOKEN="<jwt-token>"

echo "=== Step 1: Complete English Diagnostic ==="
# ... complete English diagnostic

echo "=== Step 2: Complete ISTQB Diagnostic ==="
# ... complete ISTQB diagnostic

echo "=== Step 3: Get ISTQB Result ==="
RESULT=$(curl -s -X GET "$API_URL/api/eil/diagnostic/result/$ISTQB_SESSION_ID" \
  -H "Authorization: Bearer $TOKEN")

echo "$RESULT" | jq '.skillResults[] | {skillCode, skillName}'

# Verify
ENGLISH_SKILLS=$(echo "$RESULT" | jq '.skillResults[] | select(.skillCode | startswith("LC_")) | .skillCode')

if [ -z "$ENGLISH_SKILLS" ]; then
  echo "✅ PASS: No English skills found in ISTQB result"
else
  echo "❌ FAIL: Found English skills in ISTQB result:"
  echo "$ENGLISH_SKILLS"
  exit 1
fi
```

---

## ✅ Acceptance Criteria

- [ ] ISTQB diagnostic result shows only ISTQB testing skills
- [ ] No English/TOEIC skills appear in ISTQB result
- [ ] Skills have Vietnamese translations (not "N/A")
- [ ] RadarChart displays correct skill names
- [ ] Weak skills list shows relevant ISTQB skills
- [ ] Multiple diagnostics don't cross-contaminate
- [ ] Existing English diagnostics still work correctly

---

## 📦 Deployment Checklist

### Code Changes
- [ ] Add `getSkillResultsByCertification()` to MasteryService
- [ ] Add `extractCertificationCode()` to DiagnosticService
- [ ] Update `finishDiagnostic()` to use filtered skills
- [ ] Update `getResult()` to use filtered skills

### Database Changes
- [ ] Run `add_istqb_translations_vi.sql` on staging
- [ ] Verify translations loaded correctly
- [ ] Run same script on production

### Testing
- [ ] Unit tests pass
- [ ] Integration test passes
- [ ] Manual QA test (complete ISTQB diagnostic end-to-end)
- [ ] Regression test (verify English diagnostic still works)

### Documentation
- [ ] Update API documentation
- [ ] Add comments in code
- [ ] Update CLAUDE.md if needed

---

## 🚀 Rollout Plan

### Phase 1: Staging (Today)
1. Deploy code changes
2. Add Vietnamese translations
3. Test with real user flow
4. Verify no regressions

### Phase 2: Production (Tomorrow)
1. Deploy during low-traffic hours
2. Monitor error logs
3. Test with real account
4. Ready rollback plan

---

## 📝 Post-Mortem

### What Went Wrong?
- `getAllSkillResults()` method name was misleading (implies "all for user", not "all from all certifications")
- No filtering by certification in skill result queries
- Missing unit tests for cross-certification scenarios

### Preventive Measures
1. **Add validation:** Throw error if skill certification doesn't match diagnostic certification
2. **Add monitoring:** Alert if diagnostic result contains skills from wrong certification
3. **Add tests:** Test multi-certification scenarios
4. **Code review:** Ensure all "get all" methods have appropriate filters

---

## 📎 Related Files

- [MasteryService.java](src/main/java/com/hth/udecareer/eil/service/MasteryService.java)
- [DiagnosticService.java](src/main/java/com/hth/udecareer/eil/service/DiagnosticService.java)
- [DiagnosticResultResponse.java](src/main/java/com/hth/udecareer/eil/model/response/DiagnosticResultResponse.java)
- [SkillMasteryResponse.java](src/main/java/com/hth/udecareer/eil/model/response/SkillMasteryResponse.java)

---

**Status:** Ready for implementation
**Estimated Time:** 2-3 hours
**Risk Level:** Low (backward compatible, adds filtering only)
