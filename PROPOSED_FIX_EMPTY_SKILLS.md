# Proposed Fix: Filter Empty Skills from API Responses

## Problem Statement

**1,046 skills (~32% of total) have NO questions mapped**, causing:
- Inflated skill counts in API responses
- Users see skills they cannot practice
- Diagnostic tests may fail for certain skill areas
- Frontend displays empty categories

## Solution Overview

**Filter skills without questions at the service layer** before returning to API.

### Benefits
- ✅ Immediate fix (no database changes)
- ✅ Backward compatible
- ✅ No migration required
- ✅ Improves user experience immediately

### Trade-offs
- ⚠️ Slightly slower queries (JOIN + COUNT)
- ⚠️ Cached counts may be incorrect (need cache invalidation)

---

## Code Changes Required

### 1. Repository Layer: Add Query Methods

**File:** `src/main/java/com/hth/udecareer/eil/repository/WpEzSkillRepository.java`

```java
package com.hth.udecareer.eil.repository;

import com.hth.udecareer.eil.entities.WpEzSkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WpEzSkillRepository extends JpaRepository<WpEzSkillEntity, Long> {

    // Existing methods...

    /**
     * Find all active skills for a certification that have at least one question mapped.
     * IMPORTANT: This filters out skills with 0 questions to prevent showing empty skills to users.
     */
    @Query("SELECT DISTINCT s FROM WpEzSkillEntity s " +
           "JOIN WpEzQuestionSkillEntity qs ON s.id = qs.skillId " +
           "WHERE s.certificationId = :certificationId " +
           "AND s.status = 'active' " +
           "ORDER BY s.sortOrder ASC")
    List<WpEzSkillEntity> findActivSkillsWithQuestions(@Param("certificationId") String certificationId);

    /**
     * Count active skills with questions for a certification.
     */
    @Query("SELECT COUNT(DISTINCT s.id) FROM WpEzSkillEntity s " +
           "JOIN WpEzQuestionSkillEntity qs ON s.id = qs.skillId " +
           "WHERE s.certificationId = :certificationId " +
           "AND s.status = 'active'")
    Long countActiveSkillsWithQuestions(@Param("certificationId") String certificationId);

    /**
     * Count skills with questions per certification (for getAllCertifications summary).
     */
    @Query("SELECT s.certificationId, COUNT(DISTINCT s.id) " +
           "FROM WpEzSkillEntity s " +
           "JOIN WpEzQuestionSkillEntity qs ON s.id = qs.skillId " +
           "WHERE s.status = 'active' " +
           "GROUP BY s.certificationId")
    List<Object[]> countSkillsWithQuestionsByCertification();

    /**
     * Find skills without any questions (for admin reports).
     */
    @Query("SELECT s FROM WpEzSkillEntity s " +
           "WHERE s.status = 'active' " +
           "AND s.id NOT IN (SELECT DISTINCT qs.skillId FROM WpEzQuestionSkillEntity qs) " +
           "ORDER BY s.certificationId, s.sortOrder")
    List<WpEzSkillEntity> findSkillsWithoutQuestions();
}
```

---

### 2. Service Layer: Update CertificationSkillService

**File:** `src/main/java/com/hth/udecareer/eil/service/CertificationSkillService.java`

#### Change 1: Update `getAllCertifications()` to use filtered skill counts

```java
@Cacheable(value = "certifications", key = "'all'")
public List<CertificationResponse> getAllCertifications() {
    log.debug("Getting all certifications from wp_ez_certifications table");

    List<WpEzCertificationEntity> certifications = certificationRepository
            .findByIsActiveTrueOrderBySortOrderAsc();

    // ✅ NEW: Get skill counts ONLY for skills with questions
    List<Object[]> skillCounts = skillRepository.countSkillsWithQuestionsByCertification();
    Map<String, Long> skillCountMap = skillCounts.stream()
            .collect(Collectors.toMap(
                    row -> (String) row[0],
                    row -> (Long) row[1]));

    // Get question counts per certification
    Map<String, Integer> questionCountMap = new HashMap<>();
    for (WpEzCertificationEntity cert : certifications) {
        List<Long> questionIds = questionSkillRepository
                .findQuestionIdsByCertification(cert.getCertificationId());
        questionCountMap.put(cert.getCertificationId(), questionIds.size());
    }

    return certifications.stream()
            .map(cert -> CertificationResponse.builder()
                    .certificationId(cert.getCertificationId())
                    .name(getLocalizedCertificationName(cert))
                    .description(cert.getAcronym())
                    .primaryCategory(cert.getCategory())
                    .level(cert.getDifficultyLevel())
                    .vendor(cert.getVendor())
                    .examCode(cert.getExamCode())
                    // ✅ CHANGED: Only count skills with questions
                    .skillCount(skillCountMap.getOrDefault(cert.getCertificationId(), 0L).intValue())
                    .questionCount(questionCountMap.getOrDefault(cert.getCertificationId(), 0))
                    .isFeatured(false)
                    .build())
            .collect(Collectors.toList());
}
```

**Before/After:**
```json
// BEFORE:
{
  "certificationId": "DEV_REACT",
  "skillCount": 167,  // ❌ Includes 54 empty skills
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

#### Change 2: Update `getSkillsList()` to filter empty skills

```java
public List<CertificationSkillResponse> getSkillsList(String certificationId) {
    log.debug("Getting skills list for certification: {}", certificationId);

    // ✅ CHANGED: Use new method that filters skills with questions
    List<WpEzSkillEntity> skills = skillRepository
            .findActiveSkillsWithQuestions(certificationId);

    if (skills.isEmpty()) {
        log.warn("No skills with questions found for certification: {}", certificationId);
        return Collections.emptyList();
    }

    // Get question counts for each skill
    Map<Long, Integer> questionCountMap = new HashMap<>();
    for (WpEzSkillEntity skill : skills) {
        int count = questionSkillRepository.countBySkillId(skill.getId());
        questionCountMap.put(skill.getId(), count);
    }

    return skills.stream()
            .map(skill -> mapToSkillResponse(skill, questionCountMap.get(skill.getId())))
            .collect(Collectors.toList());
}
```

---

#### Change 3: Update `getSkillTree()` to filter empty skills

```java
public CertificationSkillTreeResponse getSkillTree(String certificationId) throws AppException {
    log.debug("Building skill tree for certification: {}", certificationId);

    // ✅ CHANGED: Use filtered skills
    List<WpEzSkillEntity> skills = skillRepository
            .findActiveSkillsWithQuestions(certificationId);

    if (skills.isEmpty()) {
        throw new AppException(ErrorCode.EIL_SKILL_NOT_FOUND);
    }

    // Build tree structure (existing logic)
    Map<Long, List<WpEzSkillEntity>> skillsByParent = skills.stream()
            .collect(Collectors.groupingBy(
                    skill -> skill.getParentId() != null ? skill.getParentId() : 0L));

    List<WpEzSkillEntity> rootSkills = skillsByParent.getOrDefault(0L, Collections.emptyList());

    // ... rest of tree building logic
}
```

---

#### Change 4: Update `getLeafSkills()` to filter empty skills

```java
public List<CertificationSkillResponse> getLeafSkills(String certificationId) {
    log.debug("Getting leaf skills for certification: {}", certificationId);

    // ✅ CHANGED: Use filtered skills
    List<WpEzSkillEntity> allSkills = skillRepository
            .findActiveSkillsWithQuestions(certificationId);

    Set<Long> parentIds = allSkills.stream()
            .map(WpEzSkillEntity::getParentId)
            .filter(parentId -> parentId != null)
            .collect(Collectors.toSet());

    List<WpEzSkillEntity> leafSkills = allSkills.stream()
            .filter(skill -> !parentIds.contains(skill.getId()))
            .collect(Collectors.toList());

    // Get question counts
    Map<Long, Integer> questionCountMap = new HashMap<>();
    for (WpEzSkillEntity skill : leafSkills) {
        int count = questionSkillRepository.countBySkillId(skill.getId());
        questionCountMap.put(skill.getId(), count);
    }

    return leafSkills.stream()
            .map(skill -> mapToSkillResponse(skill, questionCountMap.get(skill.getId())))
            .collect(Collectors.toList());
}
```

---

### 3. Cache Invalidation

**File:** `src/main/java/com/hth/udecareer/eil/service/CertificationSkillService.java`

```java
/**
 * Admin endpoint to refresh certification cache when new questions are added.
 */
@CacheEvict(value = "certifications", allEntries = true)
public void refreshCertificationCache() {
    log.info("Certification cache cleared");
}
```

**Controller endpoint:**

```java
// Add to CertificationController.java
@PostMapping("/admin/refresh-cache")
@Operation(summary = "Refresh certification cache", description = "Admin only - refresh cache after adding questions")
public ResponseEntity<String> refreshCache() {
    certificationSkillService.refreshCertificationCache();
    return ResponseEntity.ok("Cache refreshed successfully");
}
```

---

### 4. Database Migration (Optional - for performance)

**File:** `src/main/resources/db/migration/V10__add_skill_question_indexes.sql`

```sql
-- Add indexes to improve JOIN performance for filtering empty skills

-- Index for skill-question mapping lookups
CREATE INDEX IF NOT EXISTS idx_question_skills_skill_id
    ON wp_ez_question_skills(skill_id);

CREATE INDEX IF NOT EXISTS idx_question_skills_question_id
    ON wp_ez_question_skills(question_id);

-- Index for skill status filtering
CREATE INDEX IF NOT EXISTS idx_skills_certification_status
    ON wp_ez_skills(certification_id, status);

-- Index for skill sorting
CREATE INDEX IF NOT EXISTS idx_skills_sort_order
    ON wp_ez_skills(sort_order);

-- Composite index for certification + status queries
CREATE INDEX IF NOT EXISTS idx_skills_cert_status_sort
    ON wp_ez_skills(certification_id, status, sort_order);
```

---

## Testing Plan

### Unit Tests

**File:** `src/test/java/com/hth/udecareer/eil/service/CertificationSkillServiceTest.java`

```java
package com.hth.udecareer.eil.service;

import com.hth.udecareer.eil.entities.WpEzCertificationEntity;
import com.hth.udecareer.eil.entities.WpEzSkillEntity;
import com.hth.udecareer.eil.model.response.CertificationResponse;
import com.hth.udecareer.eil.model.response.CertificationSkillResponse;
import com.hth.udecareer.eil.repository.WpEzCertificationRepository;
import com.hth.udecareer.eil.repository.WpEzQuestionSkillRepository;
import com.hth.udecareer.eil.repository.WpEzSkillRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificationSkillServiceTest {

    @Mock
    private WpEzCertificationRepository certificationRepository;

    @Mock
    private WpEzSkillRepository skillRepository;

    @Mock
    private WpEzQuestionSkillRepository questionSkillRepository;

    @InjectMocks
    private CertificationSkillService service;

    @Test
    void testGetAllCertifications_ShouldOnlyCountSkillsWithQuestions() {
        // Given
        WpEzCertificationEntity cert = new WpEzCertificationEntity();
        cert.setCertificationId("PSM_I");
        cert.setFullName("Professional Scrum Master I");
        cert.setIsActive(true);

        when(certificationRepository.findByIsActiveTrueOrderBySortOrderAsc())
                .thenReturn(Arrays.asList(cert));

        // Mock: 10 skills with questions out of 15 total
        when(skillRepository.countSkillsWithQuestionsByCertification())
                .thenReturn(Arrays.asList(new Object[]{"PSM_I", 10L}));

        when(questionSkillRepository.findQuestionIdsByCertification("PSM_I"))
                .thenReturn(Arrays.asList(1L, 2L, 3L, 4L, 5L)); // 5 questions

        // When
        List<CertificationResponse> result = service.getAllCertifications();

        // Then
        assertEquals(1, result.size());
        CertificationResponse response = result.get(0);
        assertEquals("PSM_I", response.getCertificationId());
        assertEquals(10, response.getSkillCount()); // ✅ Only skills with questions
        assertEquals(5, response.getQuestionCount());
    }

    @Test
    void testGetSkillsList_ShouldExcludeSkillsWithoutQuestions() {
        // Given
        WpEzSkillEntity skillWithQuestions = createSkill(1L, "SKILL_001", "Scrum Theory");
        WpEzSkillEntity skillWithoutQuestions = createSkill(2L, "SKILL_002", "Scrum Values");

        // Mock repository returns only skills with questions
        when(skillRepository.findActiveSkillsWithQuestions("PSM_I"))
                .thenReturn(Arrays.asList(skillWithQuestions));

        when(questionSkillRepository.countBySkillId(1L)).thenReturn(5);

        // When
        List<CertificationSkillResponse> result = service.getSkillsList("PSM_I");

        // Then
        assertEquals(1, result.size());
        assertEquals("SKILL_001", result.get(0).getCode());
        assertEquals(5, result.get(0).getQuestionCount());
    }

    @Test
    void testGetSkillsList_EmptyCertification_ShouldReturnEmpty() {
        // Given
        when(skillRepository.findActiveSkillsWithQuestions("UNKNOWN_CERT"))
                .thenReturn(Arrays.asList());

        // When
        List<CertificationSkillResponse> result = service.getSkillsList("UNKNOWN_CERT");

        // Then
        assertTrue(result.isEmpty());
    }

    private WpEzSkillEntity createSkill(Long id, String code, String name) {
        WpEzSkillEntity skill = new WpEzSkillEntity();
        skill.setId(id);
        skill.setCode(code);
        skill.setName(name);
        skill.setCertificationId("PSM_I");
        skill.setStatus("active");
        return skill;
    }
}
```

---

### Integration Tests

**File:** `test_certification_api.sh`

```bash
#!/bin/bash

API_URL="http://localhost:8090"
TOKEN="your-jwt-token"

echo "=== Test 1: GET /api/certifications ==="
curl -s -X GET "$API_URL/api/certifications" \
  -H "Authorization: Bearer $TOKEN" | jq '.[] | {certificationId, skillCount, questionCount}'

echo ""
echo "=== Test 2: GET /api/certifications/PSM_I ==="
curl -s -X GET "$API_URL/api/certifications/PSM_I" \
  -H "Authorization: Bearer $TOKEN" | jq '{certificationId, skillCount, questionCount}'

echo ""
echo "=== Test 3: GET /api/certifications/PSM_I/skills ==="
SKILLS=$(curl -s -X GET "$API_URL/api/certifications/PSM_I/skills" \
  -H "Authorization: Bearer $TOKEN")

TOTAL_SKILLS=$(echo "$SKILLS" | jq 'length')
SKILLS_WITH_ZERO_QUESTIONS=$(echo "$SKILLS" | jq '[.[] | select(.questionCount == 0)] | length')

echo "Total skills returned: $TOTAL_SKILLS"
echo "Skills with 0 questions: $SKILLS_WITH_ZERO_QUESTIONS"

if [ "$SKILLS_WITH_ZERO_QUESTIONS" -eq 0 ]; then
  echo "✅ PASS: No skills with 0 questions"
else
  echo "❌ FAIL: Found $SKILLS_WITH_ZERO_QUESTIONS skills with 0 questions"
  echo "$SKILLS" | jq '.[] | select(.questionCount == 0) | {code, name, questionCount}'
fi

echo ""
echo "=== Test 4: Verify skill tree doesn't include empty skills ==="
curl -s -X GET "$API_URL/api/certifications/DEV_REACT/skills/tree" \
  -H "Authorization: Bearer $TOKEN" | jq '.skills | length'
```

---

## Deployment Plan

### Pre-deployment Checklist

- [ ] Code review completed
- [ ] Unit tests passing
- [ ] Integration tests on staging
- [ ] Database indexes added (optional)
- [ ] Cache invalidation tested
- [ ] Performance benchmarks (before/after)

### Rollout Steps

#### Step 1: Deploy to Staging

```bash
# 1. Build and deploy
./gradlew build
docker build -t ezami-api:staging .
docker-compose -f docker-compose.staging.yml up -d

# 2. Run integration tests
./test_certification_api.sh

# 3. Verify responses
curl http://staging:8090/api/certifications | jq '.[] | {certificationId, skillCount}'
```

#### Step 2: Performance Testing

```bash
# Before: Benchmark current performance
ab -n 1000 -c 10 http://localhost:8090/api/certifications

# After: Compare performance
ab -n 1000 -c 10 http://staging:8090/api/certifications
```

#### Step 3: Deploy to Production

```bash
# 1. Backup database
mysqldump -h prod-db -u user -p wordpress > backup_$(date +%Y%m%d).sql

# 2. Deploy new version
docker pull ezami-api:latest
docker-compose down
docker-compose up -d

# 3. Clear cache
curl -X POST http://localhost:8090/api/certifications/admin/refresh-cache

# 4. Verify
curl http://localhost:8090/api/certifications | jq '.[] | {certificationId, skillCount}'
```

---

## Rollback Plan

If issues occur:

```bash
# 1. Revert to previous image
docker-compose down
export EZAMI_API_VERSION=previous-version
docker-compose up -d

# 2. Verify rollback
curl http://localhost:8090/actuator/health
```

---

## Performance Impact

### Expected Impact

- **Query time:** +10-20ms per request (JOIN overhead)
- **Cache:** Existing cache still works
- **Database load:** Minimal (indexed JOINs)

### Optimization

If performance degrades:

1. **Add materialized view:**
   ```sql
   CREATE TABLE wp_ez_skill_stats AS
   SELECT
       s.id,
       s.certification_id,
       COUNT(qs.question_id) as question_count
   FROM wp_ez_skills s
   LEFT JOIN wp_ez_question_skills qs ON s.id = qs.skill_id
   GROUP BY s.id, s.certification_id;

   CREATE INDEX idx_skill_stats_cert ON wp_ez_skill_stats(certification_id);
   ```

2. **Update cache more aggressively:**
   ```java
   @Cacheable(value = "certifications", key = "'all'", unless = "#result == null")
   @Scheduled(fixedRate = 3600000) // Refresh every hour
   public List<CertificationResponse> getAllCertifications() { ... }
   ```

---

## Monitoring

### Key Metrics

```yaml
alerts:
  - name: certification_api_latency
    query: "p95(http_request_duration{endpoint='/api/certifications'})"
    threshold: 500ms
    action: "Alert if p95 latency > 500ms"

  - name: empty_skills_detected
    query: "SELECT COUNT(*) FROM wp_ez_skills WHERE id NOT IN (SELECT skill_id FROM wp_ez_question_skills)"
    threshold: 1000
    action: "Alert if > 1000 empty skills"

  - name: cache_hit_rate
    query: "cache_hit_rate{cache='certifications'}"
    threshold: 0.8
    action: "Alert if hit rate < 80%"
```

---

## Summary

**Files to modify:**
1. `WpEzSkillRepository.java` - Add new query methods
2. `CertificationSkillService.java` - Update 4 methods
3. `CertificationController.java` - Add cache refresh endpoint
4. Add: `CertificationSkillServiceTest.java` - Unit tests
5. Add: `test_certification_api.sh` - Integration tests
6. Optional: `V10__add_skill_question_indexes.sql` - DB migration

**Estimated effort:** 4-6 hours
**Risk:** Low (backward compatible, no data changes)
**Impact:** High (improves UX, prevents confusion)

**Next Steps:**
1. Review this proposal
2. Get approval from team
3. Implement changes
4. Test on staging
5. Deploy to production
