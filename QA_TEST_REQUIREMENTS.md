# QA Test Requirements - Web & Mobile App Integration

## 📋 Overview
Document này liệt kê tất cả test cases cần verify trước khi release adaptive diagnostic system.

---

## 🌐 WEB APP TEST REQUIREMENTS

### Phase 1: Adaptive Diagnostic Flow

#### TC-W01: Start Diagnostic - Career Assessment
**Endpoint:** `POST /api/eil/diagnostic/start`

**Request:**
```json
{
  "mode": "CAREER_ASSESSMENT",
  "careerPath": "SCRUM_MASTER",
  "questionCount": 15
}
```

**Expected Response:**
```json
{
  "code": 200,
  "data": {
    "sessionId": "uuid...",
    "mode": "CAREER_ASSESSMENT",
    "totalQuestions": 15,
    "firstQuestion": { /* QuestionResponse object */ },
    "questions": null,  // ⚠️ MUST be null (deprecated)
    "status": "IN_PROGRESS"
  }
}
```

**Verify:**
- [ ] `firstQuestion` is NOT null (có câu hỏi đầu tiên)
- [ ] `questions` array is null (không phải empty array [])
- [ ] `sessionId` is valid UUID
- [ ] `mode` matches request
- [ ] `totalQuestions` matches request

**Screenshots Required:**
- [ ] Network tab showing response
- [ ] UI displaying first question

---

#### TC-W02: Start Diagnostic - Certification Practice
**Request:**
```json
{
  "mode": "CERTIFICATION_PRACTICE",
  "certificationCode": "PSM_I",
  "questionCount": 30
}
```

**Verify:**
- [ ] `certificationCode` = "PSM_I" in response
- [ ] Questions are PSM_I related
- [ ] Same adaptive behavior (firstQuestion only)

---

#### TC-W03: Submit Answer - Correct Answer
**Endpoint:** `POST /api/eil/diagnostic/answer`

**Request:**
```json
{
  "sessionId": "from-start-response",
  "questionId": 2780,
  "answerData": [true, false, false, false],  // Correct answer
  "timeSpentSeconds": 30
}
```

**Expected Response:**
```json
{
  "code": 200,
  "data": {
    "isCorrect": true,
    "questionsAnswered": 1,
    "questionsRemaining": 14,
    "nextQuestion": { /* Next question object */ },
    "currentProgress": 0.067,
    "autoTerminated": false,
    "consecutiveWrong": 0,  // Reset to 0
    "skillConsecutiveWrong": 0,  // Reset to 0
    "currentSkillName": "Sprint Planning"
  }
}
```

**Verify:**
- [ ] `isCorrect` = true
- [ ] `consecutiveWrong` reset to 0 after correct answer
- [ ] `nextQuestion` is provided (not null)
- [ ] Progress incremented correctly
- [ ] `autoTerminated` = false

**Screenshots:**
- [ ] Correct answer feedback
- [ ] Next question loaded

---

#### TC-W04: Submit Answer - Wrong Answer (1st wrong)
**Request:** Same as TC-W03 but with wrong answerData

**Expected Response:**
```json
{
  "isCorrect": false,
  "consecutiveWrong": 1,  // Incremented
  "skillConsecutiveWrong": 1,  // Incremented
  "autoTerminated": false,  // Not yet
  "nextQuestion": { /* Still has next */ }
}
```

**Verify:**
- [ ] `consecutiveWrong` = 1
- [ ] `skillConsecutiveWrong` = 1
- [ ] `autoTerminated` = false (chưa đủ 3)
- [ ] Still get next question

**UI Expected:**
- [ ] Show warning: "1/2 wrong in current skill" (optional)
- [ ] Continue to next question

---

#### TC-W05: Early Termination - 2 Consecutive Wrong in Same Skill
**Test Scenario:**
```
Q1 (Skill A): WRONG → skillConsecutiveWrong[A] = 1
Q2 (Skill A): WRONG → skillConsecutiveWrong[A] = 2, Skill A terminated
Q3 (Skill B): Should be from different skill
```

**Verify:**
- [ ] After 2nd wrong in Skill A, next question is from different skill
- [ ] Skill A not appearing again in subsequent questions
- [ ] Session continues (not auto-terminated)
- [ ] Log message: "Skill X terminated after 2 consecutive wrong answers"

**Screenshots:**
- [ ] Warning UI after 1st wrong in skill
- [ ] Skill change after 2nd wrong

---

#### TC-W06: Early Termination - 3 Consecutive Wrong Overall
**Test Scenario:**
```
Q1 (Skill A): WRONG → consecutive = 1
Q2 (Skill B): WRONG → consecutive = 2
Q3 (Skill C): WRONG → consecutive = 3 → AUTO TERMINATE
```

**Expected Response after Q3:**
```json
{
  "isCorrect": false,
  "questionsAnswered": 3,
  "questionsRemaining": 0,
  "nextQuestion": null,  // ⚠️ No more questions
  "autoTerminated": true,  // ⚠️ Session terminated
  "terminationReason": "3 consecutive wrong answers",
  "consecutiveWrong": 3
}
```

**Verify:**
- [ ] `autoTerminated` = true
- [ ] `terminationReason` = "3 consecutive wrong answers"
- [ ] `nextQuestion` = null
- [ ] `questionsRemaining` = 0

**UI Expected:**
- [ ] Show termination modal/alert
- [ ] Auto-navigate to results page
- [ ] Display termination reason

**Screenshots:**
- [ ] Warning after 2nd consecutive wrong
- [ ] Termination modal/alert
- [ ] Results page with early termination indicator

---

#### TC-W07: Get Results After Termination
**Endpoint:** `GET /api/eil/diagnostic/result/{sessionId}`

**Expected Response:**
```json
{
  "sessionId": "...",
  "status": "COMPLETED",
  "totalQuestions": 3,  // Only answered 3
  "correctCount": 0,
  "rawScore": 0.0,
  "estimatedLevel": "BEGINNER",
  "weakSkills": [
    {
      "skillId": 167,
      "skillName": "Scrum Theory",
      "skillNameVi": "Lý thuyết Scrum",
      "masteryLevel": 0.15,
      "attempts": 12
    }
  ],
  "recommendations": [
    "Focus on improving your psm skills...",
    "Start with basic vocabulary and grammar foundations..."
  ]
}
```

**Verify:**
- [ ] Results returned even though terminated early
- [ ] `totalQuestions` reflects actual answered (not original 30)
- [ ] Weak skills populated with names
- [ ] Recommendations provided
- [ ] Estimated level calculated

**Screenshots:**
- [ ] Results page showing all sections
- [ ] Weak skills with Vietnamese names
- [ ] Recommendations list

---

#### TC-W08: Resume Active Session (409 Handling)
**Test Scenario:**
```
1. Start session A
2. Close browser (session still active)
3. Start new session → Should get 409
```

**Expected Error Response:**
```json
{
  "code": 4013,
  "message": "Diagnostic test already in progress",
  "data": {
    "activeSessionId": "previous-session-id"
  }
}
```

**Web App Should:**
1. Catch 409 error
2. Call `GET /api/eil/diagnostic/active`
3. Check if same mode/certification
4. If same → Resume session
5. If different → Call `/restart` to abandon and start new

**Verify:**
- [ ] 409 error caught correctly
- [ ] `activeSessionId` extracted from error.data
- [ ] Resume flow works
- [ ] Restart flow works

**Screenshots:**
- [ ] Resume dialog/modal
- [ ] Resumed session showing correct progress

---

#### TC-W09: Get Next Question Explicitly
**Endpoint:** `GET /api/eil/diagnostic/next-question/{sessionId}`

**Use Case:** Alternative to getting next from submit response

**Expected Response:**
```json
{
  "code": 200,
  "data": {
    "nextQuestion": { /* QuestionResponse */ },
    "questionsAnswered": 5,
    "questionsRemaining": 10,
    "consecutiveWrong": 1,
    "skillConsecutiveWrong": 0,
    "currentSkillName": "Scrum Events"
  }
}
```

**Verify:**
- [ ] Returns same structure as submit response
- [ ] nextQuestion is valid
- [ ] Tracking counters accurate

---

### Phase 2: Skill Taxonomy & Recommendations

#### TC-W10: Get Skills by Career Path
**Endpoint:** `GET /api/eil/users/skills/category/career-scrum_master`

**Expected Response:**
```json
{
  "code": 200,
  "data": {
    "skills": [ /* Array of SkillDto */ ],
    "skillCount": 45,
    "categoryCode": "career-scrum_master"
  }
}
```

**Verify:**
- [ ] Skills returned for career path
- [ ] skillCount > 0
- [ ] Each skill has: id, code, name, nameVi, category

---

#### TC-W11: Get Skill Taxonomy
**Endpoint:** `GET /api/eil/skill/taxonomy?categoryCode=PSM_I`

**Verify:**
- [ ] Returns skills for PSM_I certification
- [ ] Skills have proper hierarchy
- [ ] Vietnamese names present

---

#### TC-W12: Readiness Score
**Endpoint:** `GET /api/eil/readiness/score`

**Expected Response:**
```json
{
  "code": 1000,
  "data": {
    "predictedScore": 650,
    "overallReadiness": 0.75,
    "passProbability": 0.82,
    "gapToTarget": 50
  }
}
```

**Note:** New users will have null data - this is normal

**Verify:**
- [ ] Returns 200 even if no data
- [ ] Data structure correct when available

---

## 📱 MOBILE APP TEST REQUIREMENTS

### MA-01: Adaptive Diagnostic on Mobile
**Same as TC-W01-W09 above**

**Additional Mobile-Specific Checks:**
- [ ] Works on iOS
- [ ] Works on Android
- [ ] Network interruption handling
- [ ] Background/foreground transitions
- [ ] Session persistence across app restarts

---

### MA-02: Push Notifications for Results
**After diagnostic completion:**

**Verify:**
- [ ] Push notification sent
- [ ] Notification includes score/level
- [ ] Tapping notification opens results screen

---

### MA-03: Offline Handling
**Test Scenario:**
```
1. Start diagnostic (online)
2. Lose connection
3. Submit answer
```

**Expected:**
- [ ] Queue answer locally
- [ ] Retry when connection restored
- [ ] Show offline indicator
- [ ] Graceful error message

---

## 🔄 CROSS-PLATFORM CONSISTENCY

### XP-01: Same Session on Web & App
**Test Scenario:**
```
1. Start diagnostic on Web
2. Close browser
3. Open mobile app
4. Should see active session
```

**Verify:**
- [ ] `GET /diagnostic/active` returns same session
- [ ] Can resume on different platform
- [ ] Progress synced correctly
- [ ] Termination state preserved

---

### XP-02: Results Consistency
**Verify:**
- [ ] Same sessionId shows identical results on web and app
- [ ] Weak skills order matches
- [ ] Recommendations identical
- [ ] Scores identical

---

## 📊 DATA VALIDATION

### DV-01: Question Data Integrity
**For each question received:**

**Verify:**
- [ ] `id` is unique and valid
- [ ] `title` is not empty
- [ ] `question` text is properly formatted
- [ ] `answerData` array has 2-5 options
- [ ] Each option has: answer text, correct flag
- [ ] At least 1 option marked as correct
- [ ] `answerType` is "single" or "multiple"

---

### DV-02: Weak Skills Accuracy
**After diagnostic completion:**

**Verify:**
- [ ] Weak skills list has 1-5 items
- [ ] Each skill has:
  - [ ] `skillId` (number)
  - [ ] `skillName` (not null, not "N/A")
  - [ ] `skillNameVi` (Vietnamese translation)
  - [ ] `masteryLevel` (0.0 - 1.0)
  - [ ] `category` (not null)
- [ ] Skills sorted by mastery (lowest first)

---

### DV-03: Recommendations Quality
**Verify:**
- [ ] 3-5 recommendations provided
- [ ] Recommendations are personalized (mention specific skills/categories)
- [ ] Level-appropriate suggestions
- [ ] Actionable advice (not generic)

**Example Good Recommendation:**
✅ "Focus on improving your PSM Scrum Theory skills. Practice with targeted exercises in this area."

**Example Bad Recommendation:**
❌ "Study more" (too generic)

---

## ⚡ PERFORMANCE REQUIREMENTS

### PERF-01: Response Times
| Endpoint | Max Response Time | Notes |
|----------|-------------------|-------|
| POST /diagnostic/start | 2 seconds | Initial question selection |
| POST /diagnostic/answer | 1 second | Submit + get next |
| GET /diagnostic/next-question | 500ms | Simple query |
| GET /diagnostic/result | 2 seconds | Complex calculation |
| GET /readiness/score | 1 second | Cached data |

**Verify:**
- [ ] All endpoints meet timing requirements
- [ ] No timeout errors under normal network
- [ ] Progress indicators shown during loading

---

### PERF-02: Memory Usage
**Verify:**
- [ ] App doesn't crash with 30-50 question sessions
- [ ] No memory leaks during long sessions
- [ ] Smooth scrolling in results page

---

## 🐛 ERROR HANDLING

### ERR-01: Network Errors
**Test Scenarios:**
| Scenario | Expected Behavior |
|----------|-------------------|
| No internet | Show "Please check connection" message |
| Timeout | Show retry button |
| 500 error | Show "Server error, try again" |
| 401 Unauthorized | Redirect to login |
| 409 Conflict | Show resume dialog |

**Verify:**
- [ ] All error scenarios handled gracefully
- [ ] User can retry failed requests
- [ ] Session state preserved across errors

---

### ERR-02: Invalid Input Handling
**Test Cases:**
| Invalid Input | Expected |
|---------------|----------|
| Empty answerData | 400 error with message |
| Invalid sessionId | 404 session not found |
| Answered same question twice | 400 "already answered" |
| Submit after completion | 400 "already completed" |

**Verify:**
- [ ] Proper error messages shown
- [ ] User guided to fix issue
- [ ] No app crashes

---

## 🎨 UI/UX REQUIREMENTS

### UX-01: Progress Indicators
**During Diagnostic:**

**Required UI Elements:**
- [ ] Progress bar: X/30 questions answered
- [ ] Current skill indicator: "Testing: Scrum Theory"
- [ ] Consecutive wrong warning when >= 2
- [ ] Per-skill warning when >= 1

**Warning Examples:**
```tsx
// Overall warning
{consecutiveWrong === 2 && (
  <Alert severity="warning">
    ⚠️ 2/3 consecutive wrong - one more wrong will end the test!
  </Alert>
)}

// Skill warning
{skillConsecutiveWrong === 1 && (
  <Alert severity="info">
    1/2 wrong in "{currentSkillName}" - this skill will be skipped if one more wrong
  </Alert>
)}
```

**Verify:**
- [ ] Warnings appear at correct times
- [ ] Warnings dismissable
- [ ] Clear messaging in Vietnamese

---

### UX-02: Termination Experience
**When Auto-Terminated:**

**Required:**
- [ ] Modal/dialog explaining termination
- [ ] Reason displayed clearly
- [ ] Auto-navigate to results after 3 seconds (or "View Results" button)
- [ ] Results show termination indicator

**Example Modal:**
```
╔════════════════════════════════════╗
║   Test Ended Early                 ║
║                                    ║
║   Reason: 3 consecutive wrong      ║
║   answers                          ║
║                                    ║
║   Don't worry! We've identified    ║
║   your weak areas to help you      ║
║   improve.                         ║
║                                    ║
║   [View Results]  [Practice Now]   ║
╚════════════════════════════════════╝
```

**Verify:**
- [ ] Modal appears immediately after termination
- [ ] User understands what happened
- [ ] Clear next actions provided

---

### UX-03: Results Page
**Required Sections:**

1. **Overall Score Card**
   - [ ] Raw score percentage
   - [ ] Estimated level (BEGINNER/INTERMEDIATE/ADVANCED)
   - [ ] Score range
   - [ ] Total questions answered

2. **Category Breakdown**
   - [ ] Each category shows: name, accuracy, correct/total
   - [ ] Visual chart (pie/bar chart)
   - [ ] Sortable by accuracy

3. **Weak Skills Section**
   - [ ] Top 5 weak skills listed
   - [ ] Each shows: name (Vietnamese), mastery level, attempts
   - [ ] Visual indicator (color-coded badges)
   - [ ] "Practice This Skill" button

4. **Recommendations Section**
   - [ ] 3-5 personalized recommendations
   - [ ] Numbered list
   - [ ] Clear, actionable advice
   - [ ] Vietnamese language

5. **Next Steps Actions**
   - [ ] "Practice Weak Skills" button → Link to practice mode
   - [ ] "Retake Assessment" button → Start new diagnostic
   - [ ] "View Recommended Certifications" → Certification suggestions based on level

**Screenshots Required:**
- [ ] Full results page
- [ ] Each section close-up
- [ ] Mobile responsive view

---

## 🔐 SECURITY & DATA

### SEC-01: Authentication
**Verify:**
- [ ] JWT token required for all endpoints
- [ ] Expired token returns 401
- [ ] Invalid token returns 401
- [ ] Cannot access other user's sessions

---

### SEC-02: Data Privacy
**Verify:**
- [ ] User can only see own diagnostic sessions
- [ ] User can only see own weak skills
- [ ] User can only see own recommendations
- [ ] Session IDs not guessable

---

## 📈 ANALYTICS & TRACKING

### ANALYTICS-01: Events to Track
**Required tracking events:**

| Event | Trigger | Data |
|-------|---------|------|
| diagnostic_started | Start button clicked | mode, certificationCode |
| diagnostic_question_answered | Answer submitted | isCorrect, timeSpent, questionNumber |
| diagnostic_terminated | Auto-terminate | reason, questionsAnswered |
| diagnostic_completed | Normal finish | totalQuestions, score, level |
| diagnostic_results_viewed | Results page loaded | sessionId, score |
| practice_started_from_results | Practice button clicked | targetSkillId |

**Verify:**
- [ ] All events firing correctly
- [ ] Event data accurate
- [ ] Events sent to analytics platform

---

## 🌍 LOCALIZATION

### LOC-01: Vietnamese Language
**All user-facing text must be in Vietnamese:**

**Verify:**
- [ ] Skill names show Vietnamese (skillNameVi)
- [ ] Error messages in Vietnamese
- [ ] UI labels in Vietnamese
- [ ] Recommendations in Vietnamese

**Fallback:**
- [ ] If skillNameVi is null, show skillName (English) as fallback
- [ ] No blank/missing text

---

### LOC-02: English Support (Optional)
**If supporting English:**

**Verify:**
- [ ] Language toggle works
- [ ] All text switches properly
- [ ] Skill names fallback to English when Vietnamese not available

---

## 🔄 EDGE CASES

### EDGE-01: All Skills Terminated Before Limit
**Test Scenario:**
```
User has only 3 testable skills
Each skill gets 2 consecutive wrong
All 3 skills terminated but only answered 6/30 questions
```

**Expected:**
- [ ] Session auto-completes with "all skills exhausted"
- [ ] Results based on 6 answered questions
- [ ] Recommendations focus on those 3 skills

---

### EDGE-02: Perfect Score
**Test Scenario:**
```
User answers all questions correctly
```

**Expected:**
- [ ] consecutiveWrong stays at 0
- [ ] All skills pass
- [ ] estimatedLevel = "ADVANCED"
- [ ] Recommendations: "Great job! Keep challenging yourself..."

---

### EDGE-03: Session Timeout
**Test Scenario:**
```
Start session
Wait > 60 minutes (timeout)
Try to submit answer
```

**Expected:**
- [ ] Error or auto-complete
- [ ] Results still accessible
- [ ] Clear message about timeout

---

## 📝 REGRESSION TESTS

### REG-01: Existing Features Still Work
**Verify these weren't broken:**

- [ ] Practice mode (adaptive learning) still works
- [ ] Mock tests still work
- [ ] AI explanations still work
- [ ] Readiness score still works
- [ ] Feed bookmark still works
- [ ] Space join/leave still works
- [ ] Quiz search still works

---

### REG-02: Backward Compatibility
**Old Clients (if any exist):**

**Verify:**
- [ ] Old apps using `questions` array don't crash (get null gracefully)
- [ ] Can still use old flow with individual answer submissions
- [ ] Don't get early termination benefits but still functional

---

## ✅ ACCEPTANCE CRITERIA

### Must Pass Before Release:

**Critical (P0):**
- [ ] Adaptive mode returns firstQuestion only
- [ ] 3 consecutive wrong auto-terminates
- [ ] Results page shows weak skills with names
- [ ] Recommendations provided
- [ ] 409 conflict handling works

**Important (P1):**
- [ ] 2 consecutive per-skill termination works
- [ ] Warning UI for termination conditions
- [ ] Vietnamese translations complete
- [ ] Performance requirements met

**Nice to Have (P2):**
- [ ] Smooth animations
- [ ] Detailed analytics
- [ ] Advanced error recovery

---

## 📸 REQUIRED DELIVERABLES

### From QA Team:
1. **Test Execution Report**
   - All test cases with PASS/FAIL status
   - Screenshots for each scenario
   - Video recording of full flow

2. **Bug Reports** (if any)
   - Steps to reproduce
   - Expected vs actual behavior
   - Screenshots/videos
   - Device/browser info

3. **Performance Report**
   - Response times for each endpoint
   - Network usage
   - Memory usage

### From Dev Team:
1. **API Documentation**
   - Swagger/OpenAPI updated
   - Postman collection
   - cURL examples

2. **Integration Guide**
   - Already provided: [WEB_APP_ADAPTIVE_DIAGNOSTIC_GUIDE.md](./WEB_APP_ADAPTIVE_DIAGNOSTIC_GUIDE.md)
   - Code examples
   - Common pitfalls

---

## 🎯 TEST ENVIRONMENTS

### Development
- **API:** http://localhost:8090
- **Web:** http://localhost:3000
- **DB:** localhost:3307

### Staging (if applicable)
- **API:** https://staging-api.ezami.vn
- **Web:** https://staging.ezami.vn

### Production
- **API:** https://api.ezami.vn (or production URL)
- **Web:** https://ezami.vn

**Test on ALL environments before production release.**

---

## 📞 SUPPORT CONTACTS

**Backend Issues:**
- Check logs: `docker logs ezami-api-server --tail 100`
- Review: [CLAUDE.md](./CLAUDE.md) for API reference

**Frontend Issues:**
- Review: [WEB_APP_ADAPTIVE_DIAGNOSTIC_GUIDE.md](./WEB_APP_ADAPTIVE_DIAGNOSTIC_GUIDE.md)
- Check browser console for errors

**Database Issues:**
- Verify EIL tables exist (run Flyway migrations if needed)
- Check skill mappings in eil_question_skills

---

## ⏱️ TIMELINE

| Phase | Duration | Deadline |
|-------|----------|----------|
| Backend Implementation | COMPLETED | ✅ Done |
| Web App Integration | 3-5 days | TBD |
| QA Testing | 2-3 days | TBD |
| Bug Fixes | 1-2 days | TBD |
| Production Deploy | 1 day | TBD |

**Total Estimated:** 7-11 days from web integration start

---

**Sign-off:**
- [ ] Backend Lead: ________________ Date: ________
- [ ] Frontend Lead: ________________ Date: ________
- [ ] QA Lead: ________________ Date: ________
- [ ] Product Owner: ________________ Date: ________
