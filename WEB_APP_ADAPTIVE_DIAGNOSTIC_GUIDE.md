# Web App - Adaptive Diagnostic Integration Guide

## 📋 Summary of Changes

### Backend Status
✅ **Completed:**
- Response models updated (DiagnosticAnswerResponse with termination fields)
- GET /api/eil/diagnostic/next-question/{sessionId} endpoint added
- Helper methods for metadata tracking added
- Implementation plan documented

⏳ **In Progress:**
- submitAnswer() logic update with early termination
- startDiagnostic() update to remove questions array
- Testing and deployment

### Web App Action Required

## 🔄 New Diagnostic Flow

### Before (Current - Will be deprecated):
```typescript
// Get all 30 questions upfront
const { data } = await startDiagnostic({
  mode: "CERTIFICATION_PRACTICE",
  certificationCode: "PSM_I",
  questionCount: 30
})

// data.questions = [Q1, Q2, ..., Q30]  ← ALL questions
// Loop through and submit each
```

### After (New Adaptive Flow):
```typescript
// 1. Start - get ONLY first question
const startResp = await api.post('/api/eil/diagnostic/start', {
  mode: "CERTIFICATION_PRACTICE",
  certificationCode: "PSM_I",
  questionCount: 30
})

let currentQuestion = startResp.data.firstQuestion  // Only 1 question
// startResp.data.questions = null  ← DEPRECATED

// 2. Loop: Submit → Check termination → Get next
while (currentQuestion) {
  // User answers...
  const answerResp = await api.post('/api/eil/diagnostic/answer', {
    sessionId: startResp.data.sessionId,
    questionId: currentQuestion.id,
    answerData: userSelection,  // [false, true, false, false]
    timeSpentSeconds: 45
  })

  const { data } = answerResp

  // ⚠️ Check early termination
  if (data.autoTerminated) {
    console.log('Session terminated:', data.terminationReason)
    // Jump to results immediately
    showResults(await getResults(sessionId))
    break
  }

  // Show warnings to user
  if (data.consecutiveWrong >= 2) {
    showWarning(`${data.consecutiveWrong}/3 consecutive wrong - be careful!`)
  }

  if (data.skillConsecutiveWrong >= 1) {
    showWarning(`${data.skillConsecutiveWrong}/2 wrong in "${data.currentSkillName}" - this skill will be skipped if one more wrong`)
  }

  // Get next question from response
  currentQuestion = data.nextQuestion

  if (!currentQuestion) {
    // Normally finished all questions
    showResults(await finishDiagnostic(sessionId))
    break
  }
}
```

## 📡 API Changes

### 1. POST /api/eil/diagnostic/start

**Request:** (No change)
```json
{
  "mode": "CERTIFICATION_PRACTICE",
  "certificationCode": "PSM_I",
  "questionCount": 30
}
```

**Response:** (Changed)
```json
{
  "code": 200,
  "data": {
    "sessionId": "uuid...",
    "mode": "CERTIFICATION_PRACTICE",
    "certificationCode": "PSM_I",
    "totalQuestions": 30,
    "firstQuestion": { /* Only 1 question */ },
    "questions": null,  // ⚠️ DEPRECATED - will be null
    "status": "IN_PROGRESS"
  }
}
```

### 2. GET /api/eil/diagnostic/next-question/{sessionId} - NEW

**Request:** (path parameter only)
```
GET /api/eil/diagnostic/next-question/abc-123-xyz
```

**Response:**
```json
{
  "code": 200,
  "data": {
    "questionsAnswered": 5,
    "questionsRemaining": 25,
    "nextQuestion": { /* QuestionResponse */ },
    "currentProgress": 0.17,
    "autoTerminated": false,
    "consecutiveWrong": 1,
    "skillConsecutiveWrong": 0,
    "currentSkillName": "Scrum Theory"
  }
}
```

### 3. POST /api/eil/diagnostic/answer - ENHANCED

**Request:** (No change)
```json
{
  "sessionId": "uuid...",
  "questionId": 2780,
  "answerData": [false, true, false, false],
  "timeSpentSeconds": 45
}
```

**Response:** (New fields added)
```json
{
  "code": 200,
  "data": {
    "isCorrect": false,
    "questionsAnswered": 6,
    "questionsRemaining": 24,
    "nextQuestion": { /* Next question or null */ },
    "currentProgress": 0.2,

    // NEW termination tracking
    "autoTerminated": false,  // or true if terminated
    "terminationReason": null,  // or "3 consecutive wrong answers"
    "consecutiveWrong": 2,      // 0-3
    "skillConsecutiveWrong": 1, // 0-2
    "currentSkillName": "Scrum Theory"
  }
}
```

## 🚨 Early Termination Scenarios

### Scenario 1: 3 Consecutive Wrong (Overall)
```
Q1 (Skill A): ✗ wrong → consecutiveWrong = 1
Q2 (Skill B): ✗ wrong → consecutiveWrong = 2
Q3 (Skill C): ✗ wrong → consecutiveWrong = 3
→ AUTO TERMINATE
→ autoTerminated = true
→ terminationReason = "3 consecutive wrong answers"
→ nextQuestion = null
→ Jump to results page
```

### Scenario 2: 2 Consecutive Wrong in Same Skill
```
Q1 (Skill A): ✗ wrong → skillConsecutiveWrong[A] = 1
Q2 (Skill A): ✗ wrong → skillConsecutiveWrong[A] = 2
→ Skill A TERMINATED (added to terminatedSkills)
Q3 (Skill B): ... → Continue with next skill
→ autoTerminated = false (continue session)
```

### Scenario 3: Mix
```
Q1 (Skill A): ✗ → consecutive = 1, skillA = 1
Q2 (Skill A): ✗ → consecutive = 2, skillA = 2 → Skill A terminated
Q3 (Skill B): ✗ → consecutive = 3 → AUTO TERMINATE
```

## 🎨 UI Implementation

### Progress Indicators
```tsx
function DiagnosticQuestion({ sessionData }) {
  const {consecutiveWrong, skillConsecutiveWrong, currentSkillName} = sessionData

  return (
    <>
      {/* Overall warning */}
      {consecutiveWrong >= 2 && (
        <Alert severity="warning">
          ⚠️ {consecutiveWrong}/3 consecutive wrong answers
          - Session will auto-finish if one more wrong!
        </Alert>
      )}

      {/* Per-skill warning */}
      {skillConsecutiveWrong >= 1 && (
        <Alert severity="info">
          {skillConsecutiveWrong}/2 wrong in "{currentSkillName}"
          - This skill will be skipped if one more wrong
        </Alert>
      )}

      {/* Question UI... */}
    </>
  )
}
```

### Handle Auto-Termination
```typescript
async function submitAnswer(sessionId, questionId, answerData) {
  const response = await api.post('/api/eil/diagnostic/answer', {
    sessionId,
    questionId,
    answerData,
    timeSpentSeconds: calculateTimeSpent()
  })

  const { data } = response

  // Check auto-termination
  if (data.autoTerminated) {
    // Show termination message
    toast.warning(data.terminationReason)

    // Fetch results
    const results = await api.get(`/api/eil/diagnostic/result/${sessionId}`)

    // Navigate to results page
    router.push(`/diagnostic/results/${sessionId}`)
    return null  // Stop loop
  }

  // Update UI with tracking state
  setConsecutiveWrong(data.consecutiveWrong)
  setSkillConsecutiveWrong(data.skillConsecutiveWrong)
  setCurrentSkill(data.currentSkillName)

  return data.nextQuestion
}
```

## 📊 State Management

```typescript
interface DiagnosticState {
  sessionId: string
  currentQuestion: QuestionResponse | null
  questionsAnswered: number
  totalQuestions: number
  consecutiveWrong: number          // 0-3
  skillConsecutiveWrong: number     // 0-2
  currentSkillName: string | null
  autoTerminated: boolean
  terminationReason: string | null
}

// Update after each answer submission
function updateState(answerResponse: DiagnosticAnswerResponse) {
  setState(prev => ({
    ...prev,
    currentQuestion: answerResponse.nextQuestion,
    questionsAnswered: answerResponse.questionsAnswered,
    consecutiveWrong: answerResponse.consecutiveWrong,
    skillConsecutiveWrong: answerResponse.skillConsecutiveWrong,
    currentSkillName: answerResponse.currentSkillName,
    autoTerminated: answerResponse.autoTerminated,
    terminationReason: answerResponse.terminationReason
  }))
}
```

## 🔄 Migration Path

### Phase 1: Backend Deploy (This week)
- Deploy new backend with adaptive logic
- `questions` field returns null (deprecated)
- Old clients still work but don't get early termination benefits

### Phase 2: Web App Update (Next sprint)
- Update to use `firstQuestion` + `/next-question`
- Add termination warnings UI
- Handle `autoTerminated` flag
- Test thoroughly

### Phase 3: Cleanup (Later)
- Remove `questions` field from response models
- Remove old non-adaptive code paths

## ✅ Testing Checklist for Web Team

- [ ] Start diagnostic → receives first question only
- [ ] Submit correct answer → get next question
- [ ] Submit 2 wrong in same skill → skill skipped, jump to different skill
- [ ] Submit 3 consecutive wrong → auto-terminate with results
- [ ] Warning UI shows for 1/2 skill wrong and 2/3 overall wrong
- [ ] Termination message displayed correctly
- [ ] Results page shows correct data after auto-termination
- [ ] Resume session works (409 handling)
- [ ] Works on both web and mobile

## 🐛 Troubleshooting

### Issue: Still getting full questions array
**Cause:** Using old backend version
**Fix:** Ensure backend deployed with version >= 1.3.0

### Issue: nextQuestion always null
**Cause:** All skills terminated
**Fix:** This is correct behavior - should show results

### Issue: Auto-terminate not working
**Cause:** Not checking `autoTerminated` flag
**Fix:** Always check this flag after submit and handle accordingly

---

## 📞 Support

Questions? Check:
- [ADAPTIVE_DIAGNOSTIC_IMPLEMENTATION_PLAN.md](./ADAPTIVE_DIAGNOSTIC_IMPLEMENTATION_PLAN.md)
- [CLAUDE.md](./CLAUDE.md) - Complete API reference
- Backend logs for detailed error messages

**Backend implementation: 70% complete**
**Estimated completion: 2-3 hours remaining**
