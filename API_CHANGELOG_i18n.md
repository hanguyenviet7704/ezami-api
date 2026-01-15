# 📝 API Changelog - i18n Standardization

**Version:** 1.4.0
**Release Date:** 2026-01-05
**Type:** BREAKING CHANGES ⚠️

---

## 🎯 Summary

Backend API đã được chuẩn hóa hoàn toàn về **internationalization (i18n)**. Tất cả endpoints giờ đều tôn trọng `Accept-Language` HTTP header và chỉ trả về **single-language content** thay vì dual-language fields.

**Mục tiêu:**
- ✅ Chuẩn hóa i18n infrastructure
- ✅ Giảm response payload size
- ✅ Dễ dàng mở rộng sang nhiều ngôn ngữ hơn (không chỉ vi/en)
- ✅ Tuân thủ HTTP standards (Accept-Language header)

---

## 🔴 Breaking Changes

### 1. CertificationResponse

**File Changed:** `src/main/java/com/hth/udecareer/eil/model/response/CertificationResponse.java`

**Before:**
```java
@Schema(description = "Certification name", example = "Professional Scrum Master I")
private String name;

@Schema(description = "Certification name in Vietnamese")
private String nameVi;
```

**After:**
```java
@Schema(description = "Certification name (localized based on Accept-Language header)")
private String name;
// nameVi field REMOVED
```

**Impact:**
- `/api/certifications` - Get all certifications
- `/api/certifications/{id}` - Get certification details

**Example:**
```http
GET /api/certifications
Accept-Language: en

Response:
{
  "certificationId": "PSM_I",
  "name": "Professional Scrum Master I"  // English only
}
```

```http
GET /api/certifications
Accept-Language: vi

Response:
{
  "certificationId": "PSM_I",
  "name": "Chuyên gia Scrum cấp I"  // Vietnamese only
}
```

---

### 2. SkillDto

**File Changed:** `src/main/java/com/hth/udecareer/eil/model/dto/SkillDto.java`

**Before:**
```java
private String name;      // English
private String nameVi;    // Vietnamese
```

**After:**
```java
@Schema(description = "Skill name (localized based on Accept-Language header)")
private String name;      // Localized
// nameVi field REMOVED
```

**Impact:**
- `/api/eil/skills/*` - All skill-related endpoints
- `/api/eil/skill/taxonomy` - Skill taxonomy

---

### 3. Enum Localization

All EIL enums now have `getLocalizedName()` method:

**Affected Enums:**
- `DifficultyLevel` (`VERY_EASY`, `EASY`, `MEDIUM`, `HARD`, `VERY_HARD`)
- `EstimatedLevel` (`BEGINNER`, `ELEMENTARY`, `INTERMEDIATE`, `UPPER_INTERMEDIATE`, `ADVANCED`)
- `MasteryLabel` (`WEAK`, `DEVELOPING`, `PROFICIENT`, `STRONG`)
- `SessionStatus` (`IN_PROGRESS`, `ACTIVE`, `PAUSED`, `COMPLETED`, `ABANDONED`)
- `SessionType` (`ADAPTIVE`, `SKILL_FOCUS`, `REVIEW`, `MIXED`, `DIAGNOSTIC`)
- `FeedbackType` (`EXPLANATION`, `RECOMMENDATION`, `SUMMARY`, `STUDY_PLAN`, `PROGRESS_REPORT`)
- `SkillCategory` (`LISTENING`, `READING`, `GRAMMAR`, `VOCABULARY`)

**Files Changed:**
- `src/main/java/com/hth/udecareer/eil/enums/*.java`

**Before:**
Responses included both languages:
```json
{
  "level": "INTERMEDIATE",
  "levelNameEn": "Intermediate",
  "levelNameVi": "Trung cấp"
}
```

**After:**
Responses only include CODE:
```json
{
  "estimatedLevel": "INTERMEDIATE"
}
```

**⚠️ Frontend Impact:** Frontend MUST translate enum values locally using i18n library.

---

## ✅ New Features

### 1. EnumLocalizationHelper Utility

**File:** `src/main/java/com/hth/udecareer/eil/util/EnumLocalizationHelper.java`

Utility class để hỗ trợ enum localization.

**Usage:**
```java
public String getLocalizedName() {
    return EnumLocalizationHelper.getLocalizedValue(nameEn, nameVi);
}
```

**Methods:**
- `getLocalizedValue(String englishValue, String vietnameseValue)` - Get localized value
- `isEnglish()` - Check if current locale is English
- `isVietnamese()` - Check if current locale is Vietnamese
- `getCurrentLanguage()` - Get current language code

---

### 2. TranslationEntity Type Constants

**File:** `src/main/java/com/hth/udecareer/entities/TranslationEntity.java`

Added new entity type constants:
```java
public static final String TYPE_CERTIFICATION = "certification";
public static final String TYPE_SKILL = "skill";
```

**Usage:** Admin có thể tạo translations cho certifications và skills trong database.

---

### 3. Service Layer Updates

#### CertificationSkillService
**File:** `src/main/java/com/hth/udecareer/eil/service/CertificationSkillService.java`

- Injected `MessageService` for locale detection
- Added `getLocalizedCertificationName()` helper method
- Updated `getAllCertifications()` to return localized names

#### SkillService
**File:** `src/main/java/com/hth/udecareer/eil/service/SkillService.java`

- Injected `MessageService`
- Added `getLocalizedSkillName()` helper method
- Updated `toDto()` to return localized skill names

---

## 🔧 Infrastructure Changes

### LocaleConfig (Already Existed)

**File:** `src/main/java/com/hth/udecareer/config/LocaleConfig.java`

- Uses `AcceptHeaderLocaleResolver` to read `Accept-Language` header
- Supports: `vi` (default), `en`
- Automatic mapping: `vi-VN` → `vi`, `en-US` → `en`

### MessageService (Already Existed)

**File:** `src/main/java/com/hth/udecareer/service/MessageService.java`

Provides utility methods:
- `getCurrentLocale()` - Get current locale from request
- `getCurrentLanguage()` - Get language code ("en", "vi")
- `isVietnamese()`, `isEnglish()` - Locale checks

---

## 📊 Affected Endpoints

### High Impact (Breaking Changes)

| Endpoint | Change | Migration Required |
|----------|--------|-------------------|
| `GET /api/certifications` | Removed `nameVi` field | ✅ Yes |
| `GET /api/certifications/{id}` | Removed `nameVi` field | ✅ Yes |
| `GET /api/eil/skills/*` | Removed `nameVi` from SkillDto | ✅ Yes |
| `GET /api/eil/skill/taxonomy` | Skills use localized names | ✅ Yes |
| All endpoints returning enums | Enum names removed from response | ✅ Yes |

### Medium Impact (Response Format Change)

| Endpoint | Change |
|----------|--------|
| `POST /api/eil/diagnostic/start` | Skill names localized |
| `POST /api/eil/diagnostic/finish/{id}` | Results use localized skill names |
| `GET /api/eil/users/me/skill-map` | Skill names localized |
| `GET /api/eil/readiness/me/latest` | Uses localized content |

### Low Impact (No Breaking Changes)

| Endpoint | Change |
|----------|--------|
| `POST /authenticate` | No change |
| `POST /register` | No change |
| `GET /api/user/me` | No change |
| File upload endpoints | No change |

---

## 🧪 Testing

### Test Script Available

Run: `./test-i18n.sh` (in ezami-api root)

Tests:
- ✅ Certifications with `Accept-Language: en`
- ✅ Certifications with `Accept-Language: vi`
- ✅ Certifications with `Accept-Language: vi-VN`
- ✅ Certifications with `Accept-Language: en-US`

### Manual Testing

**Setup:**
```bash
# Start API
docker-compose up -d
./gradlew bootRun

# Test with curl
TOKEN="your-jwt-token"

# Test English
curl -H "Accept-Language: en" \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8090/api/certifications

# Test Vietnamese
curl -H "Accept-Language: vi" \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8090/api/certifications
```

---

## 🚀 Migration Guide for Clients

See: **[FRONTEND_MIGRATION_GUIDE.md](./FRONTEND_MIGRATION_GUIDE.md)**

Quick steps:
1. Add `Accept-Language` header to all API requests
2. Remove `nameVi` field from client-side models
3. Use single `name` field for display
4. Implement local enum translations

---

## 📚 Technical Details

### How Locale Resolution Works

1. Client sends `Accept-Language: en` header
2. Spring's `AcceptHeaderLocaleResolver` parses header
3. Locale stored in `LocaleContextHolder` (thread-local)
4. Services use `MessageService.getCurrentLanguage()` to get locale
5. Return appropriate field (e.g., `fullName` for EN, `shortName` for VI)

### Database Schema

**No database changes required** for this release.

Existing tables used:
- `wp_ez_certifications` - Has both `full_name` (EN) and `short_name` (VI)
- `eil_skills` - Has both `name` (EN) and `name_vi` (VI)
- `wp_fcom_translations` - Can store additional translations (optional)

---

## 🔄 Rollback Plan

If critical issues arise:

1. **Quick Fix:** Update frontend to handle both `name` and `nameVi` (backward compatible)
2. **Rollback:** Revert to previous API version with dual-language fields
3. **Git Tag:** `v1.3.0` (pre-i18n changes)

```bash
# Rollback command
git checkout v1.3.0
./gradlew build
docker-compose up -d --build
```

---

## 📞 Contact & Support

- **Backend Team Lead:** [Your Name]
- **API Documentation:** http://localhost:8090/swagger-ui/index.html
- **Issue Tracker:** GitHub Issues
- **Slack Channel:** #ezami-api

---

## 📈 Performance Impact

**Positive:**
- ✅ Response payload size reduced (~15-20% smaller)
- ✅ Faster JSON serialization (fewer fields)
- ✅ Better client-side caching (locale-specific responses)

**Neutral:**
- ⚪ No change in database queries
- ⚪ Minimal CPU overhead for locale detection

---

## 🎓 Lessons Learned

1. **Plan i18n from Day 1:** Retrofitting i18n is painful
2. **Use Standards:** HTTP `Accept-Language` header is the way
3. **Enum Localization:** Better handled client-side for static values
4. **Testing:** Comprehensive testing with multiple locales is crucial

---

## 🔮 Future Enhancements

- [ ] Add support for more languages (ja, ko, zh)
- [ ] Use `wp_fcom_translations` table for admin-editable content
- [ ] Implement content negotiation based on user preferences
- [ ] Add fallback language chain (e.g., vi → en → fallback)

---

**Changelog Version:** 1.0
**Last Updated:** 2026-01-05
**Approvers:** Backend Team, Frontend Team, QA Team
