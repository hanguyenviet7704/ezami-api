# Quick Integration Checklist - Adaptive Diagnostic

## 🚀 For Web Developers (MUST DO)

### Step 1: Update API Calls ⚠️ BREAKING CHANGE

**OLD CODE (Remove this):**
```typescript
const { data } = await startDiagnostic({...})
const allQuestions = data.questions  // ❌ This is now NULL
```

**NEW CODE (Use this):**
```typescript
const { data } = await startDiagnostic({...})
const firstQuestion = data.firstQuestion  // ✅ Only 1 question
// Loop and call /next-question or use nextQuestion from submit response
```

### Step 2: Handle Termination in Submit

**ADD THIS CODE:**
```typescript
const submitResponse = await submitAnswer({sessionId, questionId, answerData})
const { data } = submitResponse

// ⚠️ CRITICAL: Check auto-termination
if (data.autoTerminated) {
  console.log('Terminated:', data.terminationReason)
  router.push(`/results/${sessionId}`)  // Jump to results
  return
}

// Continue with next question
const nextQ = data.nextQuestion
```

### Step 3: Add Warning UI

**ADD THESE COMPONENTS:**
```tsx
{/* Warning: Close to overall termination */}
{consecutiveWrong >= 2 && (
  <Alert severity="warning">
    ⚠️ {consecutiveWrong}/3 sai liên tiếp - sẽ kết thúc nếu sai thêm 1 câu!
  </Alert>
)}

{/* Warning: Close to skill termination */}
{skillConsecutiveWrong >= 1 && (
  <Alert severity="info">
    {skillConsecutiveWrong}/2 sai trong "{currentSkillName}" - sẽ bỏ qua skill này nếu sai thêm 1 câu
  </Alert>
)}
```

---

## 📱 For Mobile Developers (MUST DO)

### Same as Web + Additional:

**1. Handle App Backgrounding:**
```swift
// iOS
func applicationDidEnterBackground() {
  saveCurrentDiagnosticState()  // Save sessionId, currentQuestion
}

func applicationWillEnterForeground() {
  restoreDiagnosticState()  // Resume if needed
}
```

**2. Offline Queue:**
```kotlin
// Android
if (!isOnline()) {
  queueAnswerLocally(answer)
  showOfflineIndicator()
} else {
  submitAnswerToServer(answer)
}
```

---

## ✅ QUICK TEST (5 minutes)

### Test 1: Basic Flow
1. Start diagnostic → Should see 1 question (not 30)
2. Submit 1 answer → Should get next question
3. ✅ PASS if works

### Test 2: Termination
1. Start diagnostic
2. Submit 3 wrong answers in a row
3. Should see termination message
4. Should jump to results page
5. ✅ PASS if auto-terminated

### Test 3: Results
1. Complete or terminate a diagnostic
2. Check results page
3. Verify weak skills have names (not "N/A")
4. Verify recommendations exist
5. ✅ PASS if all data present

---

## 🔥 CRITICAL GOTCHAS

### ❌ DON'T DO THIS:
```typescript
// ❌ Using deprecated questions array
const questions = startResponse.data.questions
questions.forEach(q => ...)  // WILL CRASH - questions is null!
```

### ✅ DO THIS:
```typescript
// ✅ Using adaptive flow
let currentQ = startResponse.data.firstQuestion
while (currentQ) {
  const ans = await submitAnswer(...)
  currentQ = ans.data.nextQuestion
}
```

---

### ❌ DON'T DO THIS:
```typescript
// ❌ Ignoring termination flag
const ans = await submitAnswer(...)
showNextQuestion(ans.data.nextQuestion)  // Might be null!
```

### ✅ DO THIS:
```typescript
// ✅ Checking termination
const ans = await submitAnswer(...)
if (ans.data.autoTerminated) {
  handleTermination(ans.data.terminationReason)
  return
}
if (ans.data.nextQuestion) {
  showNextQuestion(ans.data.nextQuestion)
} else {
  showResults()
}
```

---

## 🆘 COMMON ISSUES & FIXES

### Issue 1: "questions array is null"
**Cause:** Using old code expecting all questions upfront
**Fix:** Use firstQuestion + adaptive loop (see Step 1 above)

### Issue 2: Crash after 3 wrong answers
**Cause:** Not handling autoTerminated flag
**Fix:** Add termination check (see Step 2 above)

### Issue 3: Weak skills show "N/A"
**Cause:** Using old backend version
**Fix:** Deploy latest backend (commit 2db0d64+)

### Issue 4: Warning UI not showing
**Cause:** Not reading consecutiveWrong field
**Fix:** Add warning components (see Step 3 above)

---

## 📚 FULL DOCUMENTATION

Detailed guides:
- **[WEB_APP_ADAPTIVE_DIAGNOSTIC_GUIDE.md](./WEB_APP_ADAPTIVE_DIAGNOSTIC_GUIDE.md)** - Complete integration guide
- **[QA_TEST_REQUIREMENTS.md](./QA_TEST_REQUIREMENTS.md)** - Full QA checklist
- **[ADAPTIVE_DIAGNOSTIC_IMPLEMENTATION_PLAN.md](./ADAPTIVE_DIAGNOSTIC_IMPLEMENTATION_PLAN.md)** - Technical spec
- **[CLAUDE.md](./CLAUDE.md)** - Complete API reference

---

## ⏰ TIMELINE

| Task | Owner | Duration | Status |
|------|-------|----------|--------|
| Backend Implementation | Backend | 1 day | ✅ DONE |
| Web Integration | Frontend | 2-3 days | 🔄 IN PROGRESS |
| Mobile Integration | Mobile | 2-3 days | ⏳ PENDING |
| QA Testing | QA | 2 days | ⏳ PENDING |
| Production Deploy | DevOps | 1 day | ⏳ PENDING |

**Estimated Total:** 7-10 days

---

**Questions?** Slack: #ezami-dev or email: dev-team@ezami.vn
