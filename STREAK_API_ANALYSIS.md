# Streak API Error Analysis

**Date:** 2026-01-07
**Status:** INVESTIGATION IN PROGRESS
**Reported Issue:** "App vẫn hiển thị lỗi treak api"

---

## 📋 Streak API Endpoints

### Available Endpoints

| Endpoint | Method | Auth Required | Description |
|----------|--------|---------------|-------------|
| `/api/streak/current` | GET | ✅ Yes | Get current streak data |
| `/api/streak/update` | POST | ✅ Yes | Update streak progress |
| `/api/streak/stats` | GET | ✅ Yes | Get detailed statistics |
| `/api/streak/freeze` | POST | ✅ Yes | Use streak freeze |
| `/api/streak/leaderboard` | GET | ❌ No | Get top streaks (public) |
| `/api/streak/goals` | GET | ✅ Yes | Get available goals |
| `/api/streak/claim-goal` | POST | ✅ Yes | Claim goal reward |
| `/api/streak/goals/adjust` | POST | ✅ Yes | Adjust goal preferences |

**Controller:** [StreakController.java](src/main/java/com/hth/udecareer/controllers/StreakController.java)
**Service:** [StreakService.java](src/main/java/com/hth/udecareer/service/StreakService.java)

---

## ✅ Database Verification

### Tables Exist

```sql
wp_fcom_user_streaks          -- User streak data (2 records)
wp_fcom_streak_activities     -- Daily activity log
wp_fcom_streak_goals          -- Available goals
wp_fcom_user_streak_goals     -- User goal progress
```

**Status:** ✅ All tables exist and have data

---

## ⚠️ Potential Issues Found

### 1. Missing Version Column (Database Migration Pending)

**Issue:** Code expects `version` column for optimistic locking, but migration V12 may not be deployed.

**Affected Entities:**
- `EilDiagnosticAttemptEntity` - Added `@Version` field
- `EilPracticeSessionEntity` - Added `@Version` field

**Migration Required:**
```sql
-- V12__add_optimistic_locking_and_fk_constraints.sql
ALTER TABLE eil_diagnostic_attempts
ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;

ALTER TABLE eil_practice_sessions
ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;
```

**Impact on Streak API:** ❓ Unclear - streak entities don't use version column

---

### 2. SpaceUserEntity Type Change (Breaking Change)

**Issue:** `userId` changed from `String` to `Long`

**Files Affected:**
- [SpaceUserEntity.java](src/main/java/com/hth/udecareer/entities/SpaceUserEntity.java) - Field type changed
- [SpaceUserRepository.java](src/main/java/com/hth/udecareer/repository/SpaceUserRepository.java) - Method signatures changed
- [FeedService.java](src/main/java/com/hth/udecareer/service/FeedService.java) - Usage updated
- [SpaceService.java](src/main/java/com/hth/udecareer/service/SpaceService.java) - Usage updated

**Database Schema:**
```sql
-- Need to check if wp_fcom_space_user.user_id is VARCHAR or BIGINT
DESCRIBE wp_fcom_space_user;
```

**Impact on Streak API:** ❓ No direct impact (streak doesn't use SpaceUserEntity)

---

### 3. Timezone Changes

**Issue:** Many entities now use `TimezoneConfig.getCurrentVietnamTime()` instead of `LocalDateTime.now()`

**Affected Streak Entities:**
- `UserStreakEntity` ✅
- `StreakActivityEntity` ✅
- `StreakGoalEntity` ✅
- `UserStreakGoalEntity` ✅

**Impact on Streak API:** ✅ Should be fine (compile successful)

---

### 4. Security Configuration

**Checked:** [SecurityConfig.java](src/main/java/com/hth/udecareer/config/SecurityConfig.java)

**Public Endpoints:**
- ✅ `/api/streak/leaderboard` - Public (no auth required)

**Protected Endpoints (Auth Required):**
- 🔒 `/api/streak/current`
- 🔒 `/api/streak/update`
- 🔒 `/api/streak/stats`
- 🔒 `/api/streak/freeze`
- 🔒 `/api/streak/goals`
- 🔒 `/api/streak/claim-goal`
- 🔒 `/api/streak/goals/adjust`

**Possible Error:** 401 Unauthorized if token missing/invalid

---

## 🔍 Possible Error Scenarios

### Scenario 1: 401 Unauthorized

**Cause:** Missing or invalid JWT token

**Error Response:**
```json
{
  "code": 401,
  "message": "Unauthorized"
}
```

**Frontend Fix:**
```typescript
const response = await fetch('/api/streak/current', {
  headers: {
    'Authorization': `Bearer ${token}`  // ✅ Must include token
  }
});
```

---

### Scenario 2: 500 Internal Server Error

**Possible Causes:**
1. **Database migration not run** (version column missing)
2. **Type mismatch** (SpaceUserEntity.userId)
3. **Null pointer** exceptions in service layer

**Checklist:**
```bash
# Check if V12 migration ran
docker exec ezami-mysql mysql -u root -p12345678aA@ wordpress -e "
  SELECT * FROM flyway_schema_history
  WHERE version = '12'
  ORDER BY installed_rank DESC LIMIT 1;
"

# Check if version column exists
docker exec ezami-mysql mysql -u root -p12345678aA@ wordpress -e "
  DESCRIBE eil_diagnostic_attempts;
" | grep version

# Check SpaceUserEntity.user_id type
docker exec ezami-mysql mysql -u root -p12345678aA@ wordpress -e "
  DESCRIBE wp_fcom_space_user;
" | grep user_id
```

---

### Scenario 3: 404 Not Found

**Possible Causes:**
1. User has no streak record (first time user)
2. Goal not found
3. Endpoint path incorrect

**API Behavior:**
- `GET /api/streak/current` - Should auto-create streak if not exists
- `POST /api/streak/update` - Should auto-create streak if not exists

---

### Scenario 4: Business Logic Error

**Possible:**
- Freeze count calculation wrong
- Streak calculation wrong
- Goal completion logic broken

---

## 🧪 Manual Testing (When API is Running)

### Test 1: Get Current Streak
```bash
TOKEN="<jwt-token>"

curl -X GET "http://localhost:8090/api/streak/current" \
  -H "Authorization: Bearer $TOKEN" \
  -v
```

**Expected Response:**
```json
{
  "currentStreak": 3,
  "longestStreak": 10,
  "freezeCount": 2,
  "lastActivityDate": "2026-01-07",
  "streakStartDate": "2026-01-05"
}
```

### Test 2: Update Streak
```bash
curl -X POST "http://localhost:8090/api/streak/update" \
  -H "Authorization: Bearer $TOKEN" \
  -v
```

**Expected Response:**
```json
{
  "currentStreak": 4,
  "streakIncreased": true,
  "freezeAutoUsed": false,
  "milestoneRewards": []
}
```

### Test 3: Get Leaderboard (Public)
```bash
curl -X GET "http://localhost:8090/api/streak/leaderboard?page=0&size=10" \
  -v
```

**Expected:** 200 OK (no auth required)

---

## 🚀 Recommended Actions

### Immediate (Need More Info from User)

**Questions for User:**
1. What is the exact error message?
2. Which streak endpoint is failing? (current, update, stats, goals, etc.)
3. What HTTP status code? (401, 500, 404, etc.)
4. Is user logged in (JWT token valid)?
5. Screenshot of error?

### If Error is 500 Internal Server Error

**Check logs:**
```bash
docker logs ezami-api -f | grep -i "streak\|error"
```

**Run migration:**
```bash
# If version column missing
docker exec -i ezami-mysql mysql -u root -p12345678aA@ wordpress \
  < src/main/resources/db/migration/V12__add_optimistic_locking_and_fk_constraints.sql
```

### If Error is 401 Unauthorized

**Frontend should check:**
- Token存在 and valid
- Token included in Authorization header
- Token not expired

### If Error is Data Related

**Check user streak data:**
```sql
SELECT * FROM wp_fcom_user_streaks WHERE user_id = <user_id>;
SELECT * FROM wp_fcom_streak_activities WHERE user_id = <user_id> ORDER BY activity_date DESC LIMIT 10;
```

---

## 📝 Summary

**Status:**
- ✅ Streak API code exists and compiles
- ✅ Database tables exist
- ❓ API not currently running (cannot test)
- ❓ Specific error unknown

**Need from User:**
- Exact error message
- Endpoint failing
- HTTP status code
- Any screenshots

**Possible Fixes Ready:**
1. Run V12 migration if version column missing
2. Check SpaceUserEntity type mismatch
3. Verify authentication token

---

**Next Steps:** Waiting for user to provide specific error details
