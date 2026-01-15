# EIL Assessment API Endpoints - Complete Reference

**Version:** v1.3.1
**Base URL:** https://api-v2.ezami.io
**Database:** 12,575 questions, 46 certifications

---

## 🎯 Diagnostic API (`/api/eil/diagnostic`)

### Start & Control

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| POST | `/start` | Start diagnostic test | sessionId, firstQuestion, questions[] |
| POST | `/restart` | Abandon current & start new | sessionId, firstQuestion, questions[] |
| POST | `/abandon/{sessionId}` | Abandon session | 200 OK |
| GET | `/active` | Get active session | sessionId, currentQuestion |
| GET | `/status/{sessionId}` | Get session status | status, currentQuestion |

### Questions & Results

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/next-question/{sessionId}` | Get next question | question object |
| POST | `/answer` | Submit answer | isCorrect, progress |
| POST | `/finish/{sessionId}` | Finish & get results | results, weakSkills, recommendations |
| GET | `/result/{sessionId}` | Get completed results | Full analysis |
| GET | `/history` | Get diagnostic history | Paginated list |

**Request Example:**
```json
POST /api/eil/diagnostic/start
{
  "certificationCode": "DEV_GOLANG",
  "questionCount": 10,
  "mode": "CERTIFICATION_PRACTICE"
}
```

**Response:**
```json
{
  "sessionId": "uuid",
  "totalQuestions": 10,
  "firstQuestion": {
    "id": 5288,
    "title": "EZ_DEV_GOLANG_5288",
    "question": "What is...",
    "answerData": [...],
    "correctMsg": "✓ Correct! ...",
    "incorrectMsg": "✗ Incorrect..."
  }
}
```

---

## 🎮 Practice API (`/api/eil/practice`)

### Session Management

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| POST | `/start` | Start practice session | sessionId, maxQuestions |
| POST | `/end/{sessionId}` | End session | Session summary |
| GET | `/status/{sessionId}` | Get session status | Progress, stats |

### Questions & Submission

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/next-question/{sessionId}` | Get next adaptive question | question, targetSkill, difficulty |
| POST | `/next-question?sessionId=xxx` | Same (POST fallback) | question object |
| POST | `/submit` | Submit answer | isCorrect, mastery delta, points |

**Practice Types:**
- `ADAPTIVE`: Auto-select weak skills
- `SKILL_FOCUS`: Focus on specific skill
- `REVIEW`: Review strong skills
- `MIXED`: Mix of weak and strong

**Request Example:**
```json
POST /api/eil/practice/start
{
  "sessionType": "ADAPTIVE",
  "maxQuestions": 20,
  "targetSkillId": null
}
```

**Response:**
```json
{
  "sessionId": "uuid",
  "sessionType": "ADAPTIVE",
  "maxQuestions": 20,
  "questionsServed": 0
}
```

---

## 📊 Readiness API (`/api/eil/readiness`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/score` | Current readiness score |
| GET | `/me/latest` | Latest readiness snapshot |
| GET | `/me/history` | Readiness history over time |

**Response:**
```json
{
  "readinessScore": 650,
  "estimatedLevel": "INTERMEDIATE",
  "readyForExam": false,
  "recommendedCertifications": ["PSM_I", "ISTQB_CTFL"]
}
```

---

## 🎓 Certification API (`/api/certifications`)

### Browse Certifications

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | List all certifications |
| GET | `/{certId}` | Get certification details |

### Skills & Questions

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/{certId}/skills` | All skills for certification |
| GET | `/{certId}/skills/tree` | Hierarchical skill tree |
| GET | `/{certId}/skills/leaf` | Leaf skills only |
| GET | `/{certId}/skills/level/{level}` | Skills by level |
| GET | `/{certId}/skills/search?keyword=xxx` | Search skills |
| GET | `/skills/{skillId}` | Skill details |
| GET | `/skills/{skillId}/questions` | Questions for skill |
| GET | `/questions/{questionId}/skills` | Skills for question |

**Available Certifications (46 total):**
- **Scrum:** PSM_I (734), SCRUM_PSPO_I (697), SCRUM_PSM_II (111)
- **ISTQB:** ISTQB_CTFL (1,283), ISTQB_AGILE (423), ISTQB_AI (80)
- **BA:** CBAP (823), CCBA (810), ECBA (245)
- **DEV:** DEV_GOLANG (588), DEV_PYTHON (476), JAVA_OCP_17 (260)
- **Cloud:** AWS_SAA_C03 (169), AZURE_AZ104 (15), GCP_ACE (132)
- **DevOps:** DOCKER_DCA (210), KUBERNETES_CKA (15), TERRAFORM (150)
- +30 more...

---

## 🔍 Skill Map API (`/api/eil/users`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/me/skill-map` | Complete skill mastery map |
| GET | `/me/weak-skills` | Top weak skills |
| GET | `/skills/category/{categoryCode}` | Skills by category |

---

## 📝 Explanation API (`/api/eil/explanations`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/{questionId}` | Get AI explanation |
| POST | `/{questionId}/feedback` | Vote on explanation |

---

## ✅ API Status Summary

### Production Ready

**Diagnostic API:**
- ✅ Returns firstQuestion with full data
- ✅ Returns questions[] array for upfront loading
- ✅ All explanations normalized (avg 425 chars)
- ✅ Supports 46 certifications

**Practice API:**
- ✅ Adaptive question selection
- ✅ Returns question object (fixed null issue)
- ✅ Real-time mastery updates
- ✅ Points & gamification

**Data Quality:**
- ✅ 12,575 questions (100% valid)
- ✅ Zero corrupt questions
- ✅ All have explanations
- ✅ 14,250 skill mappings

---

## 🧪 Test Examples

### 1. Start Diagnostic for GOLANG
```bash
curl -X POST "https://api-v2.ezami.io/api/eil/diagnostic/start" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "certificationCode": "DEV_GOLANG",
    "questionCount": 10
  }'
```

### 2. Get Next Practice Question
```bash
curl -X GET "https://api-v2.ezami.io/api/eil/practice/next-question/{sessionId}" \
  -H "Authorization: Bearer $TOKEN"
```

### 3. List All Certifications
```bash
curl "https://api-v2.ezami.io/api/certifications"
```

---

## 📊 Database Coverage

| Category | Questions | Skills | Ready |
|----------|-----------|--------|-------|
| Development | 3,250 | 1,000+ | ✅ |
| Cloud & DevOps | 1,100+ | 500+ | ✅ |
| Testing (ISTQB) | 1,787 | 108 | ✅ |
| Agile (Scrum) | 1,543 | 226 | ✅ |
| Business Analysis | 1,878 | 52 | ✅ |
| **Total** | **12,575** | **4,650** | ✅ |

---

**All assessment APIs verified and production ready!** ✅
