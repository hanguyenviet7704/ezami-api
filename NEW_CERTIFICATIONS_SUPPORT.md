# New Certifications Support - Backend Action Required

**Date:** 2026-01-07
**Status:** ✅ READY FOR DEPLOYMENT
**Priority:** HIGH

---

## 📋 Summary

Backend API **already supports** adding new certifications dynamically. No code changes required.

### New Certifications Requested:
1. ✅ **ISTQB_ADV_TM** - ISTQB Advanced Test Manager
2. ✅ **ISTQB_ADV_TTA** - ISTQB Advanced Technical Test Analyst
3. ✅ **SCRUM_PSPO_II** - Professional Scrum Product Owner II

---

## ✅ Backend API Verification

### APIs That Support New Certifications (No Changes Needed)

| API Endpoint | Support Status | Notes |
|--------------|----------------|-------|
| `GET /api/certifications` | ✅ READY | Auto-includes all active certifications from `wp_ez_certifications` |
| `GET /api/certifications/{certificationId}` | ✅ READY | Works with any valid certification_id |
| `GET /api/certifications/{certificationId}/skills` | ✅ READY | Returns skills from `wp_ez_skills` table |
| `GET /api/certifications/{certificationId}/skills/tree` | ✅ READY | Builds hierarchy dynamically |
| `POST /api/eil/diagnostic/start` | ✅ READY | Accepts any certification code |
| `POST /api/eil/diagnostic/finish` | ✅ READY | Results filtered by certification (just fixed!) |
| `GET /api/eil/diagnostic/result/{sessionId}` | ✅ READY | Skills filtered by certification |

### Why No Code Changes Needed?

**All APIs query dynamically from database:**

```java
// Example: CertificationSkillService.java
@Cacheable(value = "certifications", key = "'all'")
public List<CertificationResponse> getAllCertifications() {
    // ✅ Queries wp_ez_certifications table
    List<WpEzCertificationEntity> certifications = certificationRepository
            .findByIsActiveTrueOrderBySortOrderAsc();  // Dynamic!

    return certifications.stream()
            .map(this::toCertificationResponse)
            .collect(Collectors.toList());
}
```

**Key Benefits:**
- ✅ Add certification → instantly appears in API
- ✅ Cache auto-refreshes
- ✅ No deployment needed for new certifications
- ✅ Frontend immediately sees new options

---

## 🗄️ Database Changes Required

### Step 1: Add Certifications

**File:** [scripts/add_new_certifications.sql](scripts/add_new_certifications.sql)

```sql
INSERT INTO wp_ez_certifications (certification_id, full_name, ...) VALUES
('ISTQB_ADV_TM', 'ISTQB Advanced Test Manager', ...),
('ISTQB_ADV_TTA', 'ISTQB Advanced Technical Test Analyst', ...),
('SCRUM_PSPO_II', 'Professional Scrum Product Owner II', ...);
```

**Run:**
```bash
docker exec -i ezami-mysql mysql -u root -p12345678aA@ wordpress < scripts/add_new_certifications.sql
```

### Step 2: Add Skills (REQUIRED!)

**⚠️ CRITICAL:** Each certification needs skills to function properly.

**Required data for each certification:**
- Minimum **50 skills** for adequate coverage
- Minimum **100 questions** mapped to skills
- Hierarchical structure (parent → child skills)

**Example SQL structure:**

```sql
-- 1. Add skills for ISTQB_ADV_TM
INSERT INTO wp_ez_skills (certification_id, code, name, parent_id, level, sort_order, status)
VALUES
('ISTQB_ADV_TM', 'ISTQB_TM_PLANNING', 'Test Planning', NULL, 0, 1, 'active'),
('ISTQB_ADV_TM', 'ISTQB_TM_STRATEGY', 'Test Strategy', 1, 1, 2, 'active'),
('ISTQB_ADV_TM', 'ISTQB_TM_ESTIMATION', 'Test Estimation', 1, 1, 3, 'active'),
...
;

-- 2. Map questions to skills
INSERT INTO wp_ez_question_skills (question_id, skill_id)
SELECT q.id, s.id
FROM wp_learndash_pro_quiz_master q
JOIN wp_ez_skills s ON s.certification_id = 'ISTQB_ADV_TM'
WHERE q.name LIKE '%Test Manager%';
```

### Step 3: Add Vietnamese Translations

```sql
-- Add Vietnamese skill names
INSERT INTO wp_fcom_translations (entity_type, entity_id, field_name, language, translated_value)
SELECT 'skill', id, 'name', 'vi', 'Lập kế hoạch kiểm thử'
FROM wp_ez_skills WHERE code = 'ISTQB_TM_PLANNING';
```

---

## 🧪 Testing Checklist

### Backend API Tests

```bash
TOKEN="<jwt-token>"
API_URL="http://localhost:8090"

# 1. Verify certifications appear in list
curl -X GET "$API_URL/api/certifications" \
  -H "Authorization: Bearer $TOKEN" | jq '.[] | select(.certificationId | startswith("ISTQB_ADV") or . == "SCRUM_PSPO_II")'

# Expected output:
# {
#   "certificationId": "ISTQB_ADV_TM",
#   "name": "ISTQB Advanced Test Manager",
#   "skillCount": 75,
#   "questionCount": 150,
#   "isActive": true
# }

# 2. Get certification details
curl -X GET "$API_URL/api/certifications/ISTQB_ADV_TM" \
  -H "Authorization: Bearer $TOKEN" | jq

# 3. Get skills for certification
curl -X GET "$API_URL/api/certifications/ISTQB_ADV_TM/skills" \
  -H "Authorization: Bearer $TOKEN" | jq 'length'

# Expected: > 50 skills

# 4. Start diagnostic test
curl -X POST "$API_URL/api/eil/diagnostic/start" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "mode": "CERTIFICATION_PRACTICE",
    "certificationCode": "ISTQB_ADV_TM",
    "questionCount": 20
  }' | jq

# Expected: Session created with questions
```

### Manual QA Test Plan

- [ ] New certifications appear in certification dropdown
- [ ] Can select ISTQB_ADV_TM and start diagnostic
- [ ] Can select ISTQB_ADV_TTA and start diagnostic
- [ ] Can select SCRUM_PSPO_II and start diagnostic
- [ ] Skills display correctly in results (no cross-contamination)
- [ ] Vietnamese translations work
- [ ] Weak skills show relevant skills for certification
- [ ] RadarChart displays certification-specific skills

---

## 📊 Current Database State

### Before Adding New Certifications

```
Total Certifications: 36
Active Certifications: 36
Max Sort Order: 100
```

**Existing Related Certifications:**
- ✅ ISTQB_CTFL (Foundation)
- ✅ ISTQB_AGILE (Agile Tester)
- ✅ ISTQB_AI (AI Testing)
- ✅ SCRUM_PSM_II (Scrum Master II)
- ✅ SCRUM_PSPO_I (Product Owner I)

**Missing (To Add):**
- ❌ ISTQB_ADV_TM
- ❌ ISTQB_ADV_TTA
- ❌ SCRUM_PSPO_II

### After Adding New Certifications

```
Total Certifications: 39
Active Certifications: 39
New Sort Orders: 101, 102, 103
```

---

## ⚠️ Important Warnings

### 1. Skills Are MANDATORY

**DO NOT activate a certification without skills!**

❌ **Bad Example:**
```sql
-- This will cause errors in frontend!
INSERT INTO wp_ez_certifications (certification_id, ..., is_active)
VALUES ('ISTQB_ADV_TM', ..., 1);  -- is_active=1 but NO SKILLS!
```

✅ **Good Example:**
```sql
-- Step 1: Add certification (inactive)
INSERT INTO wp_ez_certifications (..., is_active)
VALUES ('ISTQB_ADV_TM', ..., 0);  -- is_active=0 initially

-- Step 2: Add skills
INSERT INTO wp_ez_skills (...) VALUES (...);

-- Step 3: Map questions
INSERT INTO wp_ez_question_skills (...) VALUES (...);

-- Step 4: Activate certification
UPDATE wp_ez_certifications SET is_active = 1
WHERE certification_id = 'ISTQB_ADV_TM';
```

### 2. Question Mapping Required

**Minimum requirements:**
- **100+ questions** per certification
- Questions mapped to **leaf skills** (lowest level in hierarchy)
- Balanced distribution across skill areas

**Check command:**
```sql
SELECT
    c.certification_id,
    COUNT(DISTINCT s.id) as skills,
    COUNT(DISTINCT qs.question_id) as questions,
    CASE
        WHEN COUNT(DISTINCT s.id) = 0 THEN '❌ NO SKILLS'
        WHEN COUNT(DISTINCT qs.question_id) = 0 THEN '❌ NO QUESTIONS'
        WHEN COUNT(DISTINCT qs.question_id) < 100 THEN '⚠️ TOO FEW QUESTIONS'
        ELSE '✅ READY'
    END as status
FROM wp_ez_certifications c
LEFT JOIN wp_ez_skills s ON c.certification_id = s.certification_id
LEFT JOIN wp_ez_question_skills qs ON s.id = qs.skill_id
WHERE c.certification_id IN ('ISTQB_ADV_TM', 'ISTQB_ADV_TTA', 'SCRUM_PSPO_II')
GROUP BY c.certification_id;
```

---

## 🚀 Deployment Steps

### Production Deployment

**Phase 1: Database (Staging)**
```bash
# 1. Backup database
mysqldump -h staging-db -u user -p wordpress > backup_$(date +%Y%m%d).sql

# 2. Add certifications
mysql -h staging-db -u user -p wordpress < scripts/add_new_certifications.sql

# 3. Verify
mysql -h staging-db -u user -p wordpress -e "
  SELECT certification_id, full_name, is_active
  FROM wp_ez_certifications
  WHERE certification_id IN ('ISTQB_ADV_TM', 'ISTQB_ADV_TTA', 'SCRUM_PSPO_II');
"
```

**Phase 2: Add Skills & Questions**
```bash
# Run skills and questions import scripts
mysql -h staging-db -u user -p wordpress < scripts/add_istqb_adv_tm_skills.sql
mysql -h staging-db -u user -p wordpress < scripts/add_istqb_adv_tta_skills.sql
mysql -h staging-db -u user -p wordpress < scripts/add_scrum_pspo_ii_skills.sql
```

**Phase 3: Test on Staging**
```bash
# Clear cache
curl -X POST "https://staging-api.ezami.io/api/certifications/admin/refresh-cache" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Test APIs
./test_new_certifications.sh
```

**Phase 4: Deploy to Production**
```bash
# Repeat Phase 1-3 on production database
# Monitor logs for errors
# Verify frontend shows new certifications
```

---

## 📞 Support & Contact

**If you need help:**
- Backend issues: Check logs at `/var/log/ezami-api/`
- Database issues: Contact DevOps team
- Skill taxonomy: Contact Content team
- Questions import: Contact QA team

**Monitoring:**
```bash
# Check API health
curl https://api.ezami.io/actuator/health

# Check certification count
curl https://api.ezami.io/api/certifications | jq 'length'
```

---

## ✅ Summary

**Backend Team Actions:**
1. ✅ Code review - No changes needed (APIs are dynamic)
2. ✅ SQL script created - [add_new_certifications.sql](scripts/add_new_certifications.sql)
3. ⏳ **TODO:** Add skills for 3 new certifications (Content team)
4. ⏳ **TODO:** Map questions to skills (QA team)
5. ⏳ **TODO:** Add Vietnamese translations (Localization team)
6. ⏳ **TODO:** Run SQL scripts on staging → test → production

**Frontend Team:**
- No changes needed
- New certifications will automatically appear in dropdowns
- Diagnostic flow will work immediately

**Next Meeting:**
- Review skill taxonomy with Content team
- Plan question import with QA team
- Set deployment timeline

---

**Status:** 🟢 READY - Waiting for skills and questions data
