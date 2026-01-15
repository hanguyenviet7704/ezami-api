# Skill System Analysis - Root Cause of Display Issue

## 🔍 Problem Identified

**Issue:** Skill information không hiển thị đúng trong diagnostic results

**Root Cause:** Hệ thống đang dùng 2 skill systems song song:
1. **eil_skills** (175 skills - OLD)
2. **wp_ez_skills** (4,650 skills - NEW)

---

## 📊 Current System State

### Question-Skill Mapping
```
Questions → wp_ez_question_skills → wp_ez_skills (4,650 skills)
```
✅ Questions đã được map vào wp_ez_skills system

### Mastery Tracking
```
User mastery → eil_skill_mastery → eil_skills (175 skills)
```
❌ Mastery tracking vẫn dùng old eil_skills

### Result
**Mismatch:** Questions có skill_id từ wp_ez_skills, nhưng mastery lookup từ eil_skills
→ **Skill info not found/empty**

---

## 🔧 Current Code State

### SkillService
- ✅ **Migrated** to use `WpEzSkillRepository` (primary)
- ✅ `getQuestionsGroupedBySkill()` → uses wp_ez
- ✅ `getQuestionsGroupedBySkillForCertification()` → uses wp_ez
- ⚠️ `getSkillById()` → still uses **EilSkillRepository**

### MasteryService
- ❌ Uses `EilSkillMasteryRepository`
- ❌ References `EilSkillEntity`
- ❌ `getWeakSkills()` → queries eil_skills
- ❌ `toSkillMasteryResponse()` → calls skillService.getSkillById() with eil_skill IDs

### DiagnosticService
- ✅ Uses SkillService for question selection
- ❌ Calls `masteryService.getWeakSkills()` → returns skills from eil_skills
- **Result:** Weak skills empty or wrong because skill IDs don't match

---

## 💡 Solutions

### Option 1: Update MasteryService to use wp_ez_skills (RECOMMENDED)

**Impact:** Complete migration to unified skill system

**Changes needed:**
1. Update `MasteryService` to reference wp_ez_skills
2. Migrate `eil_skill_mastery.skill_id` to wp_ez_skills IDs
3. Update all mastery queries

**Effort:** 4-6 hours
**Risk:** Medium (need to migrate existing user mastery data)

### Option 2: Make SkillService.getSkillById() work with both systems

**Impact:** Minimal code change, hybrid support

**Changes needed:**
```java
public EilSkillEntity getSkillById(Long skillId) throws AppException {
    // Try wp_ez first
    Optional<WpEzSkillEntity> wpSkill = wpSkillRepository.findById(skillId);
    if (wpSkill.isPresent()) {
        return convertToEilSkill(wpSkill.get());
    }

    // Fallback to eil
    return skillRepository.findById(skillId)
        .orElseThrow(() -> new AppException(ErrorCode.EIL_SKILL_NOT_FOUND));
}
```

**Effort:** 2 hours
**Risk:** Low (backward compatible)

### Option 3: Quick Fix - Map eil_skills to wp_ez_skills

**Impact:** Fix display immediately without code changes

**Action:**
```sql
-- Create mapping from eil_skills to wp_ez_skills by code/name
UPDATE eil_skill_mastery m
JOIN eil_skills es ON m.skill_id = es.id
JOIN wp_ez_skills ws ON es.code = ws.code
SET m.skill_id = ws.id;
```

**Effort:** 30 minutes
**Risk:** Low (SQL only)

---

## 🎯 Recommended Approach

**Phase 1 (Now):** Option 3 - Quick SQL mapping
**Phase 2 (Next sprint):** Option 1 - Full migration

---

## 📝 Implementation Plan

### Quick Fix (Option 3)

1. Map skill IDs from eil → wp_ez
2. Update mastery records
3. Test diagnostic results
4. Deploy

**Script:**
```sql
-- Find matching skills
SELECT
  es.id as eil_id,
  ws.id as wp_ez_id,
  es.code,
  es.name
FROM eil_skills es
JOIN wp_ez_skills ws ON es.code = ws.code
WHERE es.is_active = 1 AND ws.status = 'active';

-- Update mastery references
UPDATE eil_skill_mastery m
JOIN eil_skills es ON m.skill_id = es.id
JOIN wp_ez_skills ws ON es.code = ws.code
SET m.skill_id = ws.id
WHERE es.is_active = 1 AND ws.status = 'active';
```

---

## ✅ Expected Result After Fix

**Diagnostic Result Response:**
```json
{
  "weakSkills": [
    {
      "skillId": 250,
      "skillCode": "PSM_TRANSPARENCY",
      "skillName": "Transparency",
      "category": "PSM_I",
      "masteryLevel": 0.35,
      "masteryLabel": "WEAK"
    }
  ],
  "categoryScores": {
    "PSM_I": {
      "totalQuestions": 10,
      "correctCount": 6,
      "accuracy": 0.6
    }
  }
}
```

---

Would you like me to implement Option 3 (quick fix)?
