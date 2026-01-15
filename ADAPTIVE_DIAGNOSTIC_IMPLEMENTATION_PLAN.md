# Adaptive Diagnostic Implementation Plan

## Overview
Chuyển đổi Diagnostic từ "all questions upfront" sang "adaptive one-by-one" với early termination logic.

## Requirements

### Early Termination Conditions
1. **Per-Skill Termination:** Sai liên tục 2 câu trong cùng 1 skill → Skip skill đó
2. **Overall Termination:** Sai 3 câu liên tiếp (bất kể skill) → Auto finish session

### Flow Changes
**Before:**
```
START (get all 30 questions) → SUBMIT × 30 → FINISH
```

**After:**
```
START (get first question) → SUBMIT → GET_NEXT → SUBMIT → ... → AUTO_FINISH or MANUAL_FINISH
```

---

## Implementation Details

### 1. Metadata Structure Enhancement

```json
{
  "questionIds": [123, 456, 789, ...],
  "mode": "CERTIFICATION_PRACTICE",
  "certificationCode": "PSM_I",
  "careerPath": null,

  // NEW: Tracking fields
  "consecutiveWrong": 0,              // Overall consecutive wrong count
  "skillConsecutiveWrong": {          // Per-skill consecutive wrong map
    "167": 0,
    "175": 2,                          // Skill 175 terminated (2 wrong)
    "209": 1
  },
  "terminatedSkills": [175],          // Skills that reached 2 consecutive wrong
  "currentSkillId": 167,              // Current skill being tested
  "lastAnsweredQuestionId": 456,     // Last answered question
  "questionIndex": 5                  // Current index in questionIds array
}
```

### 2. New/Modified Methods in DiagnosticService

#### A. `getNextQuestion(userId, sessionId)` - NEW
```java
/**
 * Get next adaptive question for diagnostic.
 * Skips terminated skills and checks overall termination.
 *
 * @return DiagnosticAnswerResponse with nextQuestion or null if should finish
 */
```

**Logic:**
1. Load session from DB
2. Parse metadata to get tracking state
3. Find next question from questionIds that:
   - Belongs to non-terminated skill
   - Hasn't been answered yet
4. If no valid question found → return null (should finish)
5. Return question with current tracking state

#### B. `submitAnswer(userId, request)` - MODIFIED
```java
/**
 * Submit answer and check early termination conditions.
 * Auto-finishes session if termination met.
 *
 * @return DiagnosticAnswerResponse with:
 *   - isCorrect, tracking counters
 *   - nextQuestion (if continue)
 *   - autoTerminated flag + reason (if terminated)
 */
```

**New Logic:**
1. Validate and determine correctness (existing)
2. Get current skill for question
3. **UPDATE TRACKING:**
   ```java
   if (isCorrect) {
       consecutiveWrong = 0
       skillConsecutiveWrong[skillId] = 0
   } else {
       consecutiveWrong++
       skillConsecutiveWrong[skillId]++

       // Check per-skill termination
       if (skillConsecutiveWrong[skillId] >= 2) {
           terminatedSkills.add(skillId)
           log.info("Skill {} terminated", skillId)
       }

       // Check overall termination
       if (consecutiveWrong >= 3) {
           autoFinish(sessionId, "3 consecutive wrong answers")
           return response with autoTerminated=true
       }
   }
   ```
4. Save metadata back to DB
5. Get next question (skip terminated skills)
6. Return response with nextQuestion or autoTerminated flag

#### C. `startDiagnostic(userId, request)` - MODIFIED
- Remove `getAllQuestions()` call
- Only get first question
- Set `questions` field to null (deprecated)
- Return response with only `firstQuestion`

#### D. `getActiveSession(userId)` - MODIFIED
- Remove `getAllQuestions()` call
- Only get current question based on questionIndex
- Set `questions` to null

---

## 3. Database Migration

**NO new tables needed** - using existing metadata JSON column.

Migration script to add default metadata to existing sessions:
```sql
UPDATE eil_diagnostic_attempts
SET metadata = JSON_SET(
  COALESCE(metadata, '{}'),
  '$.consecutiveWrong', 0,
  '$.skillConsecutiveWrong', JSON_OBJECT(),
  '$.terminatedSkills', JSON_ARRAY(),
  '$.questionIndex', 0
)
WHERE metadata IS NULL OR JSON_EXTRACT(metadata, '$.consecutiveWrong') IS NULL;
```

---

## 4. API Contract for Web App

### Modified Endpoints

#### POST /api/eil/diagnostic/start
**Response changes:**
```json
{
  "sessionId": "...",
  "totalQuestions": 30,
  "currentQuestion": 1,
  "firstQuestion": { /* QuestionResponse */ },
  "questions": null,  // ⚠️ DEPRECATED - will be removed
  "status": "IN_PROGRESS"
}
```

#### GET /api/eil/diagnostic/next-question/{sessionId} - NEW
**Response:**
```json
{
  "isCorrect": null,  // N/A for next-question call
  "questionsAnswered": 5,
  "questionsRemaining": 25,
  "nextQuestion": { /* QuestionResponse or null if finished */ },
  "currentProgress": 0.17,
  "autoTerminated": false,
  "consecutiveWrong": 1,
  "skillConsecutiveWrong": 1,
  "currentSkillName": "Scrum Theory"
}
```

#### POST /api/eil/diagnostic/answer
**Response enhanced:**
```json
{
  "isCorrect": false,
  "questionsAnswered": 6,
  "questionsRemaining": 24,
  "nextQuestion": { /* Next question or null */ },
  "currentProgress": 0.2,

  // NEW fields:
  "autoTerminated": true,           // Session auto-finished
  "terminationReason": "3 consecutive wrong answers",
  "consecutiveWrong": 3,            // Overall consecutive wrong
  "skillConsecutiveWrong": 2,       // Wrong in current skill
  "currentSkillName": "Scrum Theory"
}
```

**Auto-termination scenarios:**
- `autoTerminated: true` + `terminationReason: "3 consecutive wrong answers"` → Finish session immediately
- `autoTerminated: true` + `terminationReason: "All skills exhausted"` → No more valid skills to test

---

## 5. Web App Integration Guide

### New Flow

```typescript
// 1. Start diagnostic
const startResponse = await startDiagnostic({
  mode: "CERTIFICATION_PRACTICE",
  certificationCode: "PSM_I",
  questionCount: 30
})

let currentQuestion = startResponse.data.firstQuestion
let sessionId = startResponse.data.sessionId

// 2. Loop: Submit answer → Check termination → Get next
while (currentQuestion) {
  // User answers question...
  const answerResponse = await submitAnswer({
    sessionId,
    questionId: currentQuestion.id,
    answerData: userSelection
  })

  // Check early termination
  if (answerResponse.data.autoTerminated) {
    console.log("Auto terminated:", answerResponse.data.terminationReason)
    // Jump to results page
    const results = await getDiagnosticResult(sessionId)
    showResults(results)
    break
  }

  // Get next question
  currentQuestion = answerResponse.data.nextQuestion

  if (!currentQuestion) {
    // Normally completed all questions
    const results = await finishDiagnostic(sessionId)
    showResults(results)
    break
  }

  // Show next question...
}
```

### Alternative: Explicit next-question call

```typescript
// After submit, explicitly call next-question
const answerResponse = await submitAnswer(...)

if (!answerResponse.data.autoTerminated) {
  const nextResponse = await getNextQuestion(sessionId)
  currentQuestion = nextResponse.data.nextQuestion
}
```

### UI Indicators

```tsx
// Show termination warning
{consecutiveWrong >= 2 && (
  <Alert variant="warning">
    ⚠️ {consecutiveWrong}/3 consecutive wrong -
    session will auto-finish if one more wrong answer
  </Alert>
)}

{skillConsecutiveWrong >= 1 && (
  <Alert variant="info">
    {skillConsecutiveWrong}/2 wrong in "{currentSkillName}" -
    will skip this skill if one more wrong
  </Alert>
)}
```

---

## 6. Backward Compatibility

**Old clients (using `questions` array):**
- `questions` field will be `null` but won't break parsing
- Can still use old flow with individual `answer` calls
- Just won't see new early termination features

**Migration path:**
1. Deploy backend with both fields
2. Update web app to use `firstQuestion` + `next-question`
3. Later: Remove `questions` field completely

---

## 7. Testing Checklist

- [ ] Start diagnostic → get first question
- [ ] Submit correct answer → get next question
- [ ] Submit 2 wrong in same skill → skill terminated, jump to next skill
- [ ] Submit 3 wrong consecutive → auto finish with results
- [ ] GET /next-question returns proper question
- [ ] Auto-terminated session has proper results
- [ ] Resume active session works
- [ ] 409 conflict handling still works

---

## 8. Edge Cases

### Case 1: All skills terminated before reaching question limit
```
Tested 3 skills:
- Skill A: 2 wrong → terminated
- Skill B: 2 wrong → terminated
- Skill C: 1 wrong, then all remaining questions in Skill C
```
**Behavior:** Continue until question limit or 3 consecutive wrong overall.

### Case 2: Hit 3 consecutive wrong while in middle of skill
```
Skill A: wrong, wrong
Skill B: wrong → TERMINATE (3 consecutive)
```
**Behavior:** Auto finish immediately.

### Case 3: User manually calls finish before auto-terminate
**Behavior:** Normal finish, ignore termination tracking.

---

## Implementation Priority

1. ✅ Update response models
2. 🔄 Implement `getNextQuestion()`
3. ⏳ Update `submitAnswer()` with termination logic
4. ⏳ Update `startDiagnostic()` to remove questions array
5. ⏳ Add metadata helper methods
6. ⏳ Test end-to-end
7. ⏳ Create web app migration guide

---

**Review this plan and confirm before I proceed with full implementation.**
