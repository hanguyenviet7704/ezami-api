# Phân Tích API Diagnostic - Backend vs App Flow

## 📊 Tình Trạng Hiện Tại

### Backend Implementation ✅ CHUẨN
Backend đã implement **ĐÚNG** theo spec trong `WEB_APP_ADAPTIVE_DIAGNOSTIC_GUIDE.md`:

| Endpoint | Status | Location | Chức năng |
|----------|--------|----------|-----------|
| `POST /api/eil/diagnostic/start` | ✅ | DiagnosticController:35 | Trả về `firstQuestion` only |
| `POST /api/eil/diagnostic/answer` | ✅ | DiagnosticController:59 | Submit + trả về `nextQuestion` |
| `GET /api/eil/diagnostic/next-question/{id}` | ✅ | DiagnosticController:47 | Lấy next question (optional) |
| `GET /api/eil/readiness/score` | ✅ | ReadinessController:46 | Trả về readiness (404 nếu chưa có data) |

### App Issues ⚠️ CẦN SỬA

| Issue | Severity | Mô tả |
|-------|----------|-------|
| Tìm endpoint `/submit-batch` | ⚠️ Warning | Endpoint này **KHÔNG TỒN TẠI** và **KHÔNG NÊN TỒN TẠI** |
| Fallback sang CAT mode | ✅ OK | Đây là flow ĐÚNG, không phải fallback |
| Readiness 404 | ℹ️ Info | Expected behavior khi user chưa làm diagnostic |

---

## 🔄 Flow CHUẨN (Theo Guide)

### Flow App Đang Làm (SAI)
```typescript
// ❌ SAI: App đang tìm batch endpoint
try {
  await api.post('/api/eil/diagnostic/submit-batch', {
    sessionId,
    answers: [...] // Gửi nhiều câu 1 lúc
  })
} catch (404) {
  // Fallback sang CAT mode (gửi từng câu)
  // => Đây mới là flow đúng!
}
```

### Flow ĐÚNG (Theo Spec)
```typescript
// 1. Start - nhận firstQuestion
const startResp = await api.post('/api/eil/diagnostic/start', {
  mode: "CERTIFICATION_PRACTICE",
  certificationCode: "PSM_I",
  questionCount: 30
})

let currentQuestion = startResp.data.firstQuestion // CHỈ 1 câu
// startResp.data.questions === null (DEPRECATED)

// 2. Loop: Submit answer → Nhận nextQuestion
while (currentQuestion) {
  // User trả lời...

  const answerResp = await api.post('/api/eil/diagnostic/answer', {
    sessionId: startResp.data.sessionId,
    questionId: currentQuestion.id,
    answerData: [false, true, false, false],
    timeSpentSeconds: 45
  })

  const { data } = answerResp

  // ⚠️ Kiểm tra early termination
  if (data.autoTerminated) {
    console.log('Session kết thúc:', data.terminationReason)
    showResults(sessionId)
    break
  }

  // Hiển thị warnings
  if (data.consecutiveWrong >= 2) {
    showWarning(`${data.consecutiveWrong}/3 sai liên tiếp!`)
  }

  if (data.skillConsecutiveWrong >= 1) {
    showWarning(`${data.skillConsecutiveWrong}/2 sai trong skill "${data.currentSkillName}"`)
  }

  // ✅ Lấy câu tiếp theo từ response
  currentQuestion = data.nextQuestion // null khi hết câu

  if (!currentQuestion) {
    showResults(sessionId)
    break
  }
}
```

---

## 📋 Response Structure Chi Tiết

### POST /api/eil/diagnostic/start
```json
{
  "code": 200,
  "data": {
    "sessionId": "uuid...",
    "mode": "CERTIFICATION_PRACTICE",
    "certificationCode": "PSM_I",
    "totalQuestions": 30,
    "firstQuestion": { /* CHỈ 1 câu */ },
    "questions": null,  // ⚠️ DEPRECATED - luôn null
    "status": "IN_PROGRESS"
  }
}
```

### POST /api/eil/diagnostic/answer
```json
{
  "code": 200,
  "data": {
    "isCorrect": false,
    "questionsAnswered": 6,
    "questionsRemaining": 24,
    "nextQuestion": { /* Câu tiếp theo hoặc null */ },
    "currentProgress": 0.2,

    // Termination tracking
    "autoTerminated": false,
    "terminationReason": null,
    "consecutiveWrong": 2,      // 0-3
    "skillConsecutiveWrong": 1, // 0-2
    "currentSkillName": "Scrum Theory"
  }
}
```

### GET /api/eil/readiness/score
```json
{
  "code": 200,
  "data": {
    "userId": 123,
    "testType": "PSM_I",
    "passProbability": 0.75,
    "estimatedScore": 850,
    "snapshotDate": "2025-01-15T10:30:00"
  }
}
```

**Hoặc 404** nếu user chưa có readiness snapshot (chưa làm diagnostic lần nào):
```json
{
  "code": 404,
  "message": "Readiness snapshot not found"
}
```

---

## 🚨 Early Termination Scenarios

### Scenario 1: 3 Consecutive Wrong Overall
```
Q1 (Skill A): ✗ → consecutiveWrong = 1
Q2 (Skill B): ✗ → consecutiveWrong = 2
Q3 (Skill C): ✗ → consecutiveWrong = 3
→ autoTerminated = true
→ terminationReason = "3 consecutive wrong answers"
→ nextQuestion = null
```

### Scenario 2: 2 Consecutive Wrong in Same Skill
```
Q1 (Skill A): ✗ → skillConsecutiveWrong = 1
Q2 (Skill A): ✗ → skillConsecutiveWrong = 2
→ Skill A terminated (bỏ qua)
Q3 (Skill B): ... → Chuyển sang skill khác
→ autoTerminated = false (session tiếp tục)
```

---

## ✅ Đề Xuất Sửa App

### 1. XÓA logic tìm `/submit-batch`
```typescript
// ❌ XÓA CODE NÀY
const batchSubmit = async () => {
  try {
    await api.post('/api/eil/diagnostic/submit-batch', ...)
  } catch {
    // fallback...
  }
}
```

### 2. SỬ DỤNG flow chuẩn
```typescript
// ✅ SỬA THÀNH
const submitAnswer = async (sessionId, questionId, answerData) => {
  const response = await api.post('/api/eil/diagnostic/answer', {
    sessionId,
    questionId,
    answerData,
    timeSpentSeconds: calculateTime()
  })

  const { data } = response

  // Check termination
  if (data.autoTerminated) {
    toast.warning(data.terminationReason)
    router.push(`/diagnostic/results/${sessionId}`)
    return null
  }

  // Update UI warnings
  if (data.consecutiveWrong >= 2) {
    showWarning(`${data.consecutiveWrong}/3 sai liên tiếp!`)
  }

  if (data.skillConsecutiveWrong >= 1) {
    showWarning(`${data.skillConsecutiveWrong}/2 sai trong "${data.currentSkillName}"`)
  }

  return data.nextQuestion // Null khi hết
}
```

### 3. XỬ LÝ readiness 404 đúng cách
```typescript
// ✅ CÁCH XỬ LÝ ĐÚNG
const fetchReadiness = async () => {
  try {
    const { data } = await api.get('/api/eil/readiness/score')
    setReadiness(data)
  } catch (error) {
    if (error.response?.status === 404) {
      // User chưa làm diagnostic → hide readiness card
      setReadiness(null)
      console.log('No readiness data yet (user has not completed diagnostic)')
    } else {
      throw error
    }
  }
}
```

---

## 📝 Summary

### Backend: ✅ ĐÚNG - KHÔNG CẦN SỬA
- Tất cả endpoints đã implement theo spec
- Flow adaptive đã hoạt động chính xác
- Early termination logic đã có

### App: ⚠️ CẦN SỬA
1. **XÓA** logic tìm `/submit-batch` endpoint
2. **SỬ DỤNG** flow chuẩn: `answer` → nhận `nextQuestion` trong response
3. **GIỮ NGUYÊN** xử lý 404 cho readiness (đã đúng)

### Migration Path
1. ✅ Backend đã sẵn sàng
2. 🔄 App cần update code theo flow trên
3. ⏱️ Estimated: 2-4 giờ để update app

---

## 📞 Test Checklist

Sau khi app sửa, test:
- [ ] Start diagnostic → nhận đúng `firstQuestion`
- [ ] Submit answer → nhận `nextQuestion` trong response
- [ ] Warning hiển thị khi `consecutiveWrong >= 2`
- [ ] Warning hiển thị khi `skillConsecutiveWrong >= 1`
- [ ] Auto-terminate khi 3 sai liên tiếp → redirect results
- [ ] Readiness API: 404 khi chưa có data, 200 khi có
- [ ] Resume session (409 conflict handling)
