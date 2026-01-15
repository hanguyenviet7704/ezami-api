# 🌐 Frontend Migration Guide - i18n API Changes

> **Migration Deadline:** ASAP
> **Breaking Changes:** YES
> **Affected Teams:** Mobile App (iOS/Android), Web App
> **API Version:** v1.4.0

---

## 📋 Tóm tắt Breaking Changes

Backend API đã được **chuẩn hóa i18n** để tôn trọng `Accept-Language` header. **TẤT CẢ** responses giờ chỉ trả về **1 field name duy nhất** thay vì dual-language fields (`name` + `nameVi`).

### ⚠️ CRITICAL: Phải gửi Accept-Language header

```http
Accept-Language: en      # Để nhận English content
Accept-Language: vi      # Để nhận Vietnamese content
Accept-Language: vi-VN   # Tự động map về "vi"
Accept-Language: en-US   # Tự động map về "en"
```

**Nếu KHÔNG gửi header:** Backend sẽ default về **Vietnamese** (vi).

---

## 🔧 Changes By Entity Type

### 1. **Certifications API** (`/api/certifications`)

#### ❌ BEFORE (Old Response):
```json
{
  "certificationId": "PSM_I",
  "name": "Professional Scrum Master I",       // Luôn English
  "nameVi": "Chuyên gia Scrum cấp I",         // Luôn Vietnamese
  "description": "PSM I",
  "level": "ENTRY"
}
```

#### ✅ AFTER (New Response):

**Request với `Accept-Language: en`:**
```json
{
  "certificationId": "PSM_I",
  "name": "Professional Scrum Master I",       // Localized English
  "description": "PSM I",
  "level": "ENTRY"
  // ⚠️ nameVi field đã BỊ XÓA!
}
```

**Request với `Accept-Language: vi`:**
```json
{
  "certificationId": "PSM_I",
  "name": "Chuyên gia Scrum cấp I",           // Localized Vietnamese
  "description": "PSM I",
  "level": "ENTRY"
}
```

#### 🛠️ Migration Code Example:

**Old Frontend Code (KHÔNG HOẠT ĐỘNG NỮA):**
```typescript
// ❌ This will FAIL - nameVi no longer exists
const displayName = currentLang === 'vi'
  ? certification.nameVi
  : certification.name;
```

**New Frontend Code (ĐÚNG):**
```typescript
// ✅ Just use 'name' - already localized by backend
const displayName = certification.name;

// Setup API client to send Accept-Language header
axios.defaults.headers.common['Accept-Language'] = currentLang; // 'en' or 'vi'
```

---

### 2. **Skills API** (`/api/eil/skills/*`)

#### Changes:
- `SkillDto` không còn field `nameVi`
- Chỉ còn field `name` (localized)

#### Migration:

**Before:**
```typescript
interface Skill {
  id: number;
  code: string;
  name: string;      // English
  nameVi: string;    // Vietnamese
  category: string;
}

// Display logic
const skillName = lang === 'vi' ? skill.nameVi : skill.name;
```

**After:**
```typescript
interface Skill {
  id: number;
  code: string;
  name: string;      // Already localized
  category: string;
  // nameVi removed ❌
}

// Display logic - SIMPLIFIED!
const skillName = skill.name; // Backend handles localization
```

---

### 3. **Enum Values** (All EIL Enums)

Các enums sau đã được update:
- `DifficultyLevel` (Easy, Medium, Hard...)
- `EstimatedLevel` (Beginner, Intermediate...)
- `MasteryLabel` (Weak, Developing, Proficient, Strong)
- `SessionStatus` (In Progress, Completed...)
- `SessionType` (Adaptive, Skill Focus...)
- `FeedbackType` (Explanation, Recommendation...)
- `SkillCategory` (Listening, Reading...)

#### Before:
Backend trả về dual fields:
```json
{
  "code": "INTERMEDIATE",
  "nameEn": "Intermediate",
  "nameVi": "Trung cấp"
}
```

Frontend phải chọn:
```typescript
const displayName = lang === 'vi' ? level.nameVi : level.nameEn;
```

#### After:
Backend giờ trả về enum **CODE ONLY**, frontend phải tự localize:

```json
{
  "estimatedLevel": "INTERMEDIATE"  // Just the code
}
```

Frontend mapping:
```typescript
// Option 1: Define enum translations locally
const levelTranslations = {
  en: {
    BEGINNER: "Beginner",
    INTERMEDIATE: "Intermediate",
    ADVANCED: "Advanced"
  },
  vi: {
    BEGINNER: "Mới bắt đầu",
    INTERMEDIATE: "Trung cấp",
    ADVANCED": "Cao cấp"
  }
};

const displayName = levelTranslations[currentLang][level];

// Option 2: Use i18n library
import { t } from 'i18next';
const displayName = t(`enums.level.${level}`);
```

---

## 🚀 Implementation Checklist

### For Mobile App (iOS/Android):

- [ ] **1. Update HTTP Client để gửi `Accept-Language` header**
```swift
// iOS Example
var request = URLRequest(url: url)
request.setValue(currentLanguage, forHTTPHeaderField: "Accept-Language")
```

```kotlin
// Android Example
val request = Request.Builder()
    .url(url)
    .addHeader("Accept-Language", currentLanguage)
    .build()
```

- [ ] **2. Remove tất cả `nameVi` fields từ data models**
```swift
// Before
struct Certification: Codable {
    let certificationId: String
    let name: String
    let nameVi: String  // ❌ DELETE THIS
}

// After
struct Certification: Codable {
    let certificationId: String
    let name: String    // Already localized
}
```

- [ ] **3. Update tất cả UI code để chỉ sử dụng `name` field**
```swift
// Before
let displayName = lang == "vi" ? cert.nameVi : cert.name

// After
let displayName = cert.name  // Simple!
```

- [ ] **4. Create local enum translations**
```swift
enum EstimatedLevel: String {
    case beginner = "BEGINNER"
    case intermediate = "INTERMEDIATE"

    func localized(lang: String) -> String {
        switch (self, lang) {
        case (.beginner, "vi"): return "Mới bắt đầu"
        case (.beginner, _): return "Beginner"
        case (.intermediate, "vi"): return "Trung cấp"
        case (.intermediate, _): return "Intermediate"
        // ...
        }
    }
}
```

- [ ] **5. Test với cả 2 languages**
  - Switch app language → verify API trả đúng locale
  - Check tất cả screens có certification/skill names
  - Verify enum values hiển thị đúng

---

### For Web App (React/Vue):

- [ ] **1. Setup Axios interceptor để auto-inject Accept-Language**
```typescript
// axios-config.ts
import axios from 'axios';
import i18n from './i18n'; // Your i18n config

axios.interceptors.request.use((config) => {
  config.headers['Accept-Language'] = i18n.language; // 'en' or 'vi'
  return config;
});
```

- [ ] **2. Update TypeScript interfaces**
```typescript
// types/certification.ts
export interface Certification {
  certificationId: string;
  name: string;            // Localized by backend
  // nameVi: string;       // ❌ REMOVE
  description: string;
  level: string;
}

export interface Skill {
  id: number;
  code: string;
  name: string;            // Localized by backend
  // nameVi: string;       // ❌ REMOVE
  category: string;
}
```

- [ ] **3. Remove manual language selection logic**
```typescript
// Before ❌
const displayName = currentLang === 'vi'
  ? certification.nameVi
  : certification.name;

// After ✅
const displayName = certification.name; // Already localized!
```

- [ ] **4. Create enum translation files**
```typescript
// translations/en.json
{
  "enums": {
    "level": {
      "BEGINNER": "Beginner",
      "INTERMEDIATE": "Intermediate",
      "ADVANCED": "Advanced"
    },
    "mastery": {
      "WEAK": "Weak",
      "DEVELOPING": "Developing",
      "PROFICIENT": "Proficient",
      "STRONG": "Strong"
    }
  }
}

// translations/vi.json
{
  "enums": {
    "level": {
      "BEGINNER": "Mới bắt đầu",
      "INTERMEDIATE": "Trung cấp",
      "ADVANCED": "Cao cấp"
    },
    "mastery": {
      "WEAK": "Yếu",
      "DEVELOPING": "Đang phát triển",
      "PROFICIENT": "Thành thạo",
      "STRONG": "Mạnh"
    }
  }
}
```

- [ ] **5. Update components sử dụng enum values**
```typescript
import { useTranslation } from 'react-i18next';

function DiagnosticResult({ level }: { level: string }) {
  const { t } = useTranslation();

  return (
    <div>
      {t(`enums.level.${level}`)}
    </div>
  );
}
```

- [ ] **6. Test language switching**
  - Test switch từ EN → VI và ngược lại
  - Verify API calls có đúng `Accept-Language` header
  - Check console network tab để confirm

---

## 🧪 Testing Scenarios

### Test Case 1: Certification List
```bash
# English
curl -H "Accept-Language: en" http://localhost:8090/api/certifications
# Expected: "name": "Professional Scrum Master I"

# Vietnamese
curl -H "Accept-Language: vi" http://localhost:8090/api/certifications
# Expected: "name": "Chuyên gia Scrum cấp I"
```

### Test Case 2: Skills by Category
```bash
curl -H "Accept-Language: en" \
  "http://localhost:8090/api/eil/skill/taxonomy?categoryCode=PSM_I"

curl -H "Accept-Language: vi" \
  "http://localhost:8090/api/eil/skill/taxonomy?categoryCode=PSM_I"
```

### Test Case 3: Diagnostic Results
```bash
curl -H "Accept-Language: en" \
  -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8090/api/eil/diagnostic/result/{sessionId}"
```

---

## ⚡ Quick Migration Steps

1. **Add Accept-Language header** to all API requests
2. **Remove nameVi** from all TypeScript/Model definitions
3. **Simplify UI code** - use `name` directly
4. **Create enum translations** locally (frontend-side)
5. **Test thoroughly** with both EN and VI languages

---

## 🆘 Troubleshooting

### Q: API vẫn trả Vietnamese dù đã gửi `Accept-Language: en`?
**A:** Check:
- Header có đúng format không? `Accept-Language: en` (không có space thừa)
- Backend có đang chạy version mới nhất không?
- Clear cache nếu sử dụng Redux/Vuex cache

### Q: Enum values không hiển thị?
**A:** Backend giờ CHỈ trả CODE (e.g., "INTERMEDIATE"). Frontend phải tự translate. Setup enum translation files như hướng dẫn ở trên.

### Q: Có cần migrate dần hay phải làm hết 1 lần?
**A:** **Phải làm hết 1 lần** vì đây là breaking change. API cũ không còn trả `nameVi` nữa.

### Q: Có API nào KHÔNG bị ảnh hưởng?
**A:** Có - các API KHÔNG trả về entity names (e.g., authentication, file upload, simple CRUD operations).

---

## 📞 Support

- **Backend Team:** [Your Team Contact]
- **API Docs:** http://localhost:8090/swagger-ui/index.html
- **Test Script:** `/test-i18n.sh` trong ezami-api repo

---

## 📅 Timeline

| Date | Milestone |
|------|-----------|
| Today | Backend deployed với breaking changes |
| +1 day | Mobile app update Accept-Language header |
| +1 day | Web app update Accept-Language header |
| +2 days | Remove all `nameVi` references |
| +3 days | Complete enum localization |
| +4 days | Full testing & QA |
| +5 days | Production deployment |

---

**Generated:** 2026-01-05
**Version:** 1.0
**Last Updated:** Initial Release
