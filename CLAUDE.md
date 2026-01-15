# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build
./gradlew build              # Full build with tests
./gradlew build -x test      # Build without tests
./gradlew bootJar            # Build JAR only

# Run locally (start MySQL/Redis first)
docker-compose up -d mysql redis
./gradlew bootRun

# Run with Docker (full stack)
docker-compose up -d

# Run tests
./gradlew test               # All tests
./gradlew test --tests "EmailSMTPServiceTest"  # Single test class
```

## Architecture

### Package Structure
Base package: `com.hth.udecareer`

- `annotation/` - Custom annotations (`@ApiPrefixController`, `@ApiV2PrefixController`)
- `config/` - Spring configuration (Security, Redis, CORS, WebSocket)
- `controllers/` - REST endpoints
- `entities/` - JPA entities mapping to WordPress database
- `repository/` - Spring Data JPA repositories
- `service/` - Business logic layer
- `security/` - JWT authentication, `PhpPasswordEncoder` for WordPress compatibility
- `model/` - DTOs, requests, responses

### Key Patterns

**Controller Routing**: Use `@ApiPrefixController` instead of `@RestController` to auto-prefix routes with `/api/`. Auth endpoints (`JwtAuthenticationController`) use plain `@RestController` without prefix.

**Security Whitelisting**: Public endpoints must be added to `SecurityConfig.filterChain()` with `.antMatchers().permitAll()`.

**Password Handling**: `PhpPasswordEncoder` provides compatibility with WordPress phpass/bcrypt password hashes.

**Database**: Uses WordPress database schema (`wp_posts`, `wp_users`, `wp_terms`, etc.) with JPA entities. Column mapping uses `CamelCaseToUnderscoresNamingStrategy`.

**Timezone**: All timestamps use `Asia/Ho_Chi_Minh` (configured in JPA and Jackson).

### API Versioning
- `/api/` - Version 1 endpoints (use `@ApiPrefixController`)
- `/api/v2/` - Version 2 endpoints (use `@ApiV2PrefixController`)

### Authentication Flow
- JWT tokens with configurable cookie settings
- Google OAuth with separate mobile/web success URLs
- Public endpoints: `/authenticate`, `/signup`, `/register`, `/verification-code/**`, `/auth/google/**`

## Services & Ports

| Service | Port | Notes |
|---------|------|-------|
| API     | 8090 | Spring Boot app (khi chạy với ezami-web) |
| WordPress | 8080 | Nginx từ ezami-web |
| MySQL   | 3307 | Not default 3306 to avoid conflicts |
| Redis   | 6379 | Không có password khi dùng với ezami-web |

## Local Development (tích hợp với ezami-web)

```bash
# Chạy API với Gradle (port 8090, tránh conflict với WordPress port 8080)
SERVER_PORT=8090 \
DB_DRIVER="com.mysql.cj.jdbc.Driver" \
DB_URL="jdbc:mysql://localhost:3307/wordpress?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh&useUnicode=true&characterEncoding=UTF-8" \
DB_USER="root" \
DB_PASS="12345678aA@" \
SPRING_REDIS_HOST="localhost" \
SPRING_REDIS_PORT="6379" \
SPRING_REDIS_PASSWORD="" \
JWT_SECRET="your-secret-key" \
./gradlew bootRun
```

## Test Account

| Email | Password | Notes |
|-------|----------|-------|
| hienhv0711@gmail.com | 12345678 | Test user cho Swagger UI |

**Swagger UI**: http://localhost:8090/swagger-ui/index.html
- Đăng nhập với `/authenticate` endpoint
- Copy JWT token từ response
- Bấm "Authorize" và paste token (không cần "Bearer " prefix)

## Environment Variables

Key variables required in `.env` or `docker-compose.yml`:
- `DB_DRIVER`, `DB_URL`, `DB_USER`, `DB_PASS` - Database
- `JWT_SECRET` - Token signing
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GOOGLE_REDIRECT_URI` - OAuth
- `REVENUECAT_*` - Subscription management
- `MAIL_*` - Email service (SMTP)
- `VNPAY_*` - Payment integration

## Testing

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health check: `http://localhost:8080/actuator/health`
- Tests use JUnit 5 (`useJUnitPlatform()`)

## External Integrations

- **WordPress**: Direct MySQL database integration for posts, users, categories
- **RevenueCat**: Subscription webhooks at `/webhook/revenuecat`
- **Firebase**: Push notifications via FCM
- **Google OAuth**: Two-flow auth (mobile/web)
- **Redis**: Caching with 10-minute TTL default
- **VNPay**: Payment processing
- **Cloudinary/Cloudflare R2**: File storage

## API Endpoints Reference

### Authentication

**Note:** Login endpoint có 2 paths (cả 2 đều hoạt động):

- **Primary (Recommended):** `POST /api/auth/authenticate` - Consistent với API design pattern
- **Alias (Legacy):** `POST /authenticate` - Backward compatibility cho mobile apps

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/authenticate` | ❌ | Login (Primary - use this for new code) |
| POST | `/authenticate` | ❌ | Login (Alias - backward compatible) |
| POST | `/register` | ❌ | Register with verification |
| POST | `/verification-code` | ❌ | Send verification code |
| POST | `/logout` | ✅ | Logout |
| GET | `/auth/google/login` | ❌ | Google OAuth URL |
| GET | `/auth/google/callback` | ❌ | Google OAuth callback |

### User (`/api/user/`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/user/me` | ✅ | Get current user |
| POST | `/user/update` | ✅ | Update profile |
| POST | `/user/change-pass` | ✅ | Change password |
| POST | `/user/reset-pass` | ❌ | Forgot password |

### Community Feeds (`/api/feeds/`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/feeds` | ❌ | List feeds (with filters) |
| GET | `/feeds/{id}` | ❌ | Get feed by ID |
| POST | `/feeds` | ✅ | Create feed |
| POST | `/feeds/{id}` | ✅ | Update feed |
| PATCH | `/feeds/{id}` | ✅ | Patch feed (sticky) |
| POST | `/feeds/{id}/delete` | ✅ | Soft delete feed |
| POST | `/feeds/{id}/bookmark` | ✅ | Toggle bookmark |
| POST | `/feeds/{id}/reaction` | ✅ | Like/Unlike feed |
| GET | `/feeds/{id}/comments` | ❌ | Get comments |
| POST | `/feeds/{id}/comments` | ✅ | Create comment |
| POST | `/feeds/{id}/comments/{cid}/delete` | ✅ | Delete comment |
| POST | `/comments/{id}/reaction` | ✅ | Like comment |

### Spaces (`/api/spaces/`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/spaces` | ❌ | List spaces (tree) |
| GET | `/spaces/{slug}/by-slug` | ❌ | Get by slug |
| GET | `/spaces/{id}/by-id` | ❌ | Get by ID |
| POST | `/spaces/{id}/join` | ✅ | Join space |
| POST | `/spaces/{id}/leave` | ✅ | Leave space |

### Profile & Social (`/api/profile/`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/profile/{username}` | ❌ | View profile |
| POST | `/profile/{username}/follow` | ✅ | Follow user |
| POST | `/profile/{username}/unfollow` | ✅ | Unfollow user |
| POST | `/profile/{username}/block` | ✅ | Block user |
| GET | `/profile/{username}/followers` | ❌ | Get followers |
| GET | `/profile/{username}/followings` | ❌ | Get followings |

### Courses & Lessons (`/api/`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/courses/{id}` | ✅ | Course details |
| GET | `/courses-pagination` | ✅ | List courses |
| GET | `/courses/{id}/lessons/{lid}` | ✅ | Lesson details |
| POST | `/lessons/{id}/progress` | ✅ | Update progress |

### Quiz (`/api/quiz/`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/quiz` | ✅ | Search quizzes |
| GET | `/quiz/category/public` | ❌ | Quiz categories |
| GET | `/quiz/{id}` | ✅ | Quiz details |
| POST | `/quiz/{id}` | ✅ | Submit quiz |
| GET | `/quiz/history` | ✅ | Quiz history |

### Shopping & Payment (`/api/`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/cart` | ✅ | View cart |
| POST | `/cart/add` | ✅ | Add to cart |
| DELETE | `/cart/{id}` | ✅ | Remove from cart |
| POST | `/payment/checkout` | ✅ | Checkout |
| POST | `/payment/buy-now` | ✅ | Buy now |
| GET | `/orders/history` | ✅ | Order history |
| GET | `/vouchers` | ✅ | List vouchers |

### Badges & Gamification (`/api/`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/badges` | ❌ | List badges |
| GET | `/badges/me` | ✅ | My badges |
| POST | `/badges/{id}/featured` | ✅ | Set featured |
| GET | `/leaderboard` | ❌ | Leaderboard |

### Notifications (`/api/`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/notifications` | ✅ | List notifications |
| POST | `/notifications/{id}/read` | ✅ | Mark as read |
| POST | `/notifications/read-all` | ✅ | Mark all read |
| GET | `/notifications/unread-count` | ✅ | Unread count |

### Certificates (`/api/learndash/`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/learndash/my-certificates` | ✅ | My certificates |
| GET | `/learndash/my-certificates/courses` | ✅ | Course certs |
| GET | `/learndash/my-certificates/quizzes` | ✅ | Quiz certs |
| GET | `/learndash/my-achievements` | ✅ | My achievements |

### Moderation (`/api/moderation/`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/moderation/report` | ✅ | Report content |
| GET | `/moderation/my-reports` | ✅ | My reports |

### Media (`/api/`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/image/upload` | ✅ | Upload image |

## Database Tables (WordPress Schema)

### Community Tables
- `wp_fcom_feeds` - Feed posts
- `wp_fcom_post_comments` - Comments
- `wp_fcom_post_reactions` - Likes, bookmarks (type: 'like', 'bookmark')
- `wp_fcom_spaces` - Community spaces
- `wp_fcom_space_user` - Space memberships
- `wp_fcom_user_activities` - User activity logs
- `wp_fcom_media_archives` - Media attachments

### User Tables
- `wp_users` - WordPress users
- `wp_usermeta` - User metadata
- `wp_xprofile` - Extended profiles

### E-commerce Tables
- `wp_ez_orders` - Orders
- `wp_ez_order_items` - Order items
- `wp_ez_vouchers` - Vouchers
- `wp_ez_user_vouchers` - User vouchers

### Learning Tables
- `wp_posts` - Courses, lessons (post_type: sfwd-courses, sfwd-lessons)
- `wp_learndash_pro_quiz_master` - Quizzes
- `wp_learndash_user_activity` - Learning progress

### EIL (Intelligent Learning) Tables
- `eil_skills` - Skill taxonomy (legacy)
- `wp_ez_skills` - Skill taxonomy (WordPress, primary)
- `eil_question_skills` - Question-skill mapping
- `eil_skill_mastery` - User skill mastery tracking
- `eil_diagnostic_attempts` - Diagnostic test sessions
- `eil_diagnostic_answers` - Diagnostic test answers
- `eil_practice_sessions` - Practice sessions
- `eil_practice_results` - Practice results
- `eil_explanations` - Cached AI-generated explanations
- `eil_readiness_snapshots` - Readiness tracking snapshots
- `eil_srs_cards` - Spaced repetition system cards (SM-2 algorithm)
- `eil_srs_reviews` - SRS review history
- `eil_time_estimates` - Time-to-certification estimates
- `eil_ai_feedback` - AI-generated feedback records
- `eil_mock_results` - Mock exam results

## Production Deployment Plan

### Pre-deployment Checklist

```bash
# 1. Backup production database
mysqldump -h <prod-host> -u <user> -p wordpress > backup_$(date +%Y%m%d_%H%M%S).sql

# 2. Check current Flyway migration status
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 10;
```

### Deployment Steps

#### Step 1: Build & Push Docker Image

```bash
# Local build
cd /Users/kien/eup-project/ezami/ezami-api
docker build -t ezami-api:latest .

# Tag for registry (nếu dùng Docker Hub hoặc ECR)
docker tag ezami-api:latest <registry>/ezami-api:v1.x.x
docker push <registry>/ezami-api:v1.x.x
```

#### Step 2: Database Migration (Flyway)

Flyway tự động chạy khi API khởi động với config:

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: 1
    locations: classpath:db/migration
```

**Migration Files Structure:**

```
src/main/resources/db/migration/
├── V2__create_eil_skills_tables.sql
├── V3__create_eil_diagnostic_tables.sql
├── V4__create_eil_practice_tables.sql
├── V5__create_eil_ai_tables.sql
├── V6__create_eil_mock_tables.sql
├── V7__seed_eil_skills.sql
├── V8__seed_psm_skills.sql
└── *.sql.disabled (skipped migrations)
```

#### Step 3: Deploy to Production

```bash
# SSH vào server production
ssh user@prod-server

# Pull latest image
docker pull <registry>/ezami-api:v1.x.x

# Stop current container
docker-compose down

# Start với new version
docker-compose up -d

# Check logs
docker logs -f ezami-api-server
```

#### Step 4: Verify Deployment

```bash
# Health check
curl http://localhost:8090/actuator/health

# Check Flyway migrations applied
docker exec -it ezami-mysql mysql -u root -p \
  -e "SELECT * FROM wordpress.flyway_schema_history;"

# Test new APIs
TOKEN="<jwt-token>"
curl -X POST "http://localhost:8090/api/feeds/1/bookmark" \
  -H "Authorization: Bearer $TOKEN"
curl -X POST "http://localhost:8090/api/spaces/1/join" \
  -H "Authorization: Bearer $TOKEN"
```

### Database Sync Strategy

#### Option A: Full Sync (Downtime Required)

```bash
# 1. Put site in maintenance mode
# 2. Export from staging
mysqldump -h staging-db -u user -p wordpress > staging_dump.sql

# 3. Import to production
mysql -h prod-db -u user -p wordpress < staging_dump.sql

# 4. Flyway handles pending migrations on startup
# 5. Remove maintenance mode
```

#### Option B: Schema-only Sync (Zero Downtime)

```bash
# Export schema changes only
mysqldump -h staging-db -u user -p --no-data wordpress \
  eil_skills eil_question_skills eil_skill_mastery \
  eil_diagnostic_sessions eil_diagnostic_results \
  > schema_changes.sql

# Apply to production
mysql -h prod-db -u user -p wordpress < schema_changes.sql
```

#### Option C: Flyway Migration (Recommended)

```bash
# Flyway handles schema changes automatically
# Just deploy new API version - migrations run on startup

# Verify migrations
docker exec ezami-api-server java -jar app.jar flyway info
```

### Rollback Plan

```bash
# 1. Restore previous Docker image
docker-compose down
export EZAMI_API_VERSION=v1.x.x-previous
docker-compose up -d

# 2. Rollback database (if needed)
mysql -h prod-db -u user -p wordpress < backup_YYYYMMDD_HHMMSS.sql

# 3. Verify rollback
curl http://localhost:8090/actuator/health
```

### Environment Variables for Production

```bash
# Required
DB_URL=jdbc:mysql://prod-mysql:3306/wordpress?useSSL=true&serverTimezone=Asia/Ho_Chi_Minh&useUnicode=true&characterEncoding=UTF-8
DB_USER=<prod-user>
DB_PASS=<prod-password>
JWT_SECRET=<strong-secret-key>

# Redis
SPRING_REDIS_HOST=prod-redis
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=<redis-password>

# External Services
GOOGLE_CLIENT_ID=<google-client-id>
GOOGLE_CLIENT_SECRET=<google-client-secret>
VNPAY_TMN_CODE=<vnpay-code>
VNPAY_HASH_SECRET=<vnpay-secret>
FIREBASE_CONFIG_BASE64=<firebase-config>

# Production flags
JWT_COOKIE_SECURE=true
JWT_COOKIE_SAME_SITE=None
```

### New APIs Added (v1.3.0)

| API | Method | Endpoint | Description |
|-----|--------|----------|-------------|
| Bookmark Feed | POST | `/api/feeds/{id}/bookmark` | Toggle bookmark |
| Join Space | POST | `/api/spaces/{id}/join` | Join community space |
| Leave Space | POST | `/api/spaces/{id}/leave` | Leave community space |

**No new tables required** - Uses existing:
- `wp_fcom_post_reactions` - Bookmarks with `type='bookmark'`
- `wp_fcom_space_user` - Space memberships

### Post-deployment Verification Script

```bash
#!/bin/bash
# verify_deployment.sh

API_URL="http://localhost:8090"
TOKEN="<test-jwt-token>"

echo "=== Health Check ==="
curl -s "$API_URL/actuator/health" | jq .

echo "=== Test Bookmark API ==="
curl -s -X POST "$API_URL/api/feeds/1/bookmark" \
  -H "Authorization: Bearer $TOKEN" | jq .

echo "=== Test Join Space API ==="
curl -s -X POST "$API_URL/api/spaces/1/join" \
  -H "Authorization: Bearer $TOKEN" | jq .

echo "=== Check Flyway Status ==="
docker exec ezami-mysql mysql -u root -p12345678aA@ -e \
  "SELECT version, description, success FROM wordpress.flyway_schema_history \
   ORDER BY installed_rank DESC LIMIT 5;"
```

## EIL (Intelligent Learning) APIs

### Diagnostic Assessment APIs

**Flow:** START → ANSWER (multiple) → FINISH → RESULTS

| Method | Endpoint | Description | Key Features |
|--------|----------|-------------|--------------|
| POST | `/api/eil/diagnostic/start` | Start diagnostic test | Returns ALL questions in `questions` array |
| POST | `/api/eil/diagnostic/restart` | Restart (abandon old + start new) | Handles 409 conflicts |
| GET | `/api/eil/diagnostic/next-question/{sessionId}` | Get next adaptive question | For adaptive CAT mode |
| POST | `/api/eil/diagnostic/answer` | Submit one answer | Updates mastery real-time |
| POST | `/api/eil/diagnostic/finish/{sessionId}` | Finish and get results | Full analysis + recommendations |
| GET | `/api/eil/diagnostic/result/{sessionId}` | Get completed results | Read-only |
| GET | `/api/eil/diagnostic/active` | Get active session | For resume functionality |
| GET | `/api/eil/diagnostic/status/{sessionId}` | Get session status | Progress tracking |
| POST | `/api/eil/diagnostic/abandon/{sessionId}` | Abandon session | Mark as abandoned |
| GET | `/api/eil/diagnostic/history` | Get diagnostic history | Paginated list of past tests |

**Diagnostic Modes:**
- `CAREER_ASSESSMENT`: Random questions to determine user level → recommend certifications
- `CERTIFICATION_PRACTICE`: Specific certification practice questions

**Request Example:**
```json
{
  "mode": "CERTIFICATION_PRACTICE",
  "certificationCode": "PSM_I",
  "questionCount": 30
}
```

**Response Example (start/active):**
```json
{
  "code": 200,
  "data": {
    "sessionId": "uuid...",
    "mode": "CERTIFICATION_PRACTICE",
    "certificationCode": "PSM_I",
    "totalQuestions": 30,
    "questions": [
      {"id": 2780, "title": "PSM1_All_Q65", "answerData": [...], ...},
      ...
    ],
    "status": "IN_PROGRESS"
  }
}
```

**Result Response:**
```json
{
  "sessionId": "...",
  "status": "COMPLETED",
  "correctCount": 18,
  "rawScore": 60.0,
  "estimatedLevel": "INTERMEDIATE",
  "estimatedScoreMin": 450,
  "estimatedScoreMax": 550,
  "categoryScores": {
    "PSM": {"accuracy": 0.75, "totalQuestions": 20, "correctCount": 15}
  },
  "weakSkills": [
    {"skillName": "Scrum Theory", "masteryLevel": 0.35, ...}
  ],
  "recommendations": ["Focus on...", ...]
}
```

### Adaptive Practice APIs

**Flow:** START → NEXT_QUESTION → SUBMIT → NEXT_QUESTION → ... → END

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/eil/practice/start` | Start practice session |
| GET | `/api/eil/practice/next-question/{sessionId}` | Get next question (path variable) |
| POST | `/api/eil/practice/next-question?sessionId=` | Get next question (query param, adaptive - 1 at a time) |
| POST | `/api/eil/practice/submit` | Submit answer + update mastery |
| POST | `/api/eil/practice/end/{sessionId}` | End session |
| GET | `/api/eil/practice/status/{sessionId}` | Get status |

**Practice Session Types:**
- `ADAPTIVE`: Auto-selects weak skills
- `SKILL_FOCUS`: Focus on specific skill
- `REVIEW`: Review strong skills
- `MIXED`: Mix of weak and strong

### AI Explanation APIs

**Cached explanations from `eil_explanations` table:**

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/eil/explanations/{id}` | Get explanation by ID |
| GET | `/api/eil/explanations/cache/{cacheKey}` | Get by cache key (increments hit count) |
| GET | `/api/eil/explanations/question/{questionId}` | Get all explanations for a question |
| GET | `/api/eil/explanations/popular` | Get most accessed explanations |

**Request explanation in practice submit:**
```json
{
  "sessionId": "...",
  "questionId": 123,
  "answerData": [true, false, false, false],
  "requestExplanation": true
}
```

**Note:** Cache key format: `{questionId}_{language}_{promptVersion}`

### Spaced Repetition System (SRS) APIs

**SM-2 algorithm for flashcard-style learning:**

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/eil/srs/cards` | Create SRS card for a question |
| POST | `/api/eil/srs/cards/bulk` | Bulk create multiple SRS cards |
| GET | `/api/eil/srs/cards/due` | Get cards due for review (paginated) |
| GET | `/api/eil/srs/cards` | Get all cards with filters (status, certification) |
| GET | `/api/eil/srs/cards/{cardId}` | Get specific card details |
| POST | `/api/eil/srs/cards/{cardId}/review` | Record review result (quality 0-5, updates SM-2) |
| POST | `/api/eil/srs/cards/{cardId}/suspend` | Suspend card from review schedule |
| POST | `/api/eil/srs/cards/{cardId}/resume` | Resume suspended card |
| DELETE | `/api/eil/srs/cards/{cardId}` | Delete SRS card |
| GET | `/api/eil/srs/stats` | Get SRS statistics (due count, review streak, etc.) |
| POST | `/api/eil/srs/sync` | Sync cards between client and server |

**Card Statuses:** `NEW`, `LEARNING`, `REVIEW`, `SUSPENDED`

**SM-2 Quality Scale:**
- 0: Complete blackout
- 1: Incorrect, but familiar
- 2: Incorrect, but easy to recall
- 3: Correct with difficulty
- 4: Correct with hesitation
- 5: Perfect recall

### Time Estimation APIs

**Time-to-Certification estimation and tracking:**

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/eil/estimates` | Create time estimate for certification |
| GET | `/api/eil/estimates/{certificationCode}` | Get estimate for specific certification |
| GET | `/api/eil/estimates` | Get all time estimates for user |
| GET | `/api/eil/estimates/active` | Get active time estimates |
| PUT | `/api/eil/estimates/{certificationCode}/progress` | Update progress (studied hours, completed topics) |
| PUT | `/api/eil/estimates/{certificationCode}/status` | Update status (PLANNING, IN_PROGRESS, COMPLETED, PAUSED) |
| GET | `/api/eil/estimates/{certificationCode}/pace` | Get pace analysis (on track, behind, ahead) |
| DELETE | `/api/eil/estimates/{certificationCode}` | Delete time estimate |

**Request Example:**
```json
{
  "certificationCode": "PSM_I",
  "targetDate": "2024-12-31",
  "hoursPerWeek": 10,
  "currentMasteryLevel": 0.45
}
```

**Response includes:**
- Estimated total hours needed
- Current pace vs required pace
- Projected completion date
- Daily/weekly study recommendations

### AI Feedback APIs

**AI-generated personalized feedback tracking:**

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/eil/ai-feedback/my` | Get my AI feedback history (paginated) |
| GET | `/api/eil/ai-feedback/my/type/{feedbackType}` | Get feedback by type (DIAGNOSTIC, PRACTICE, MOCK) |
| GET | `/api/eil/ai-feedback/my/latest` | Get latest feedback by type |
| POST | `/api/eil/ai-feedback/{id}/rate` | Rate feedback (helpful/not, rating 1-5, comment) |

**Feedback Types:**
- `DIAGNOSTIC`: Feedback from diagnostic test results
- `PRACTICE`: Feedback from practice sessions
- `MOCK`: Feedback from mock exams
- `READINESS`: Readiness assessment feedback
- `SKILL_GAP`: Skill gap analysis

### Mock Test Result APIs

**Mock exam results tracking:**

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/eil/mock-results/my` | Get my mock test results (paginated) |
| GET | `/api/eil/mock-results/my/type/{testType}` | Get results by test type (PSM_I, PSPO_I, etc.) |
| GET | `/api/eil/mock-results/my/latest` | Get latest mock result (optionally by test type) |
| GET | `/api/eil/mock-results/{id}` | Get specific mock result by ID |
| GET | `/api/eil/mock-results/my/stats` | Get statistics (max score, avg score, total attempts) |

**Result includes:**
- Score breakdown by skill/topic
- Time taken vs recommended time
- Comparison with previous attempts
- Detailed question analysis

### Readiness & Progress APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/eil/readiness/score` | Current readiness score |
| GET | `/api/eil/readiness/me/latest` | Latest readiness snapshot |
| GET | `/api/eil/readiness/me/history` | Readiness history |
| GET | `/api/eil/users/me/skill-map` | Complete skill mastery map |
| GET | `/api/eil/users/me/weak-skills` | Top weak skills |

### Skill Taxonomy APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/certifications` | List all certifications |
| GET | `/api/certifications/{certId}` | Certification details |
| GET | `/api/certifications/{certId}/skills` | Skills for certification |
| GET | `/api/certifications/{certId}/skills/tree` | Skill hierarchy tree |
| GET | `/api/eil/skill/taxonomy?categoryCode=` | Skill taxonomy (flexible) |
| GET | `/api/skill/taxonomy?categoryCode=` | Alternative path |
| GET | `/api/eil/users/skills/category/{categoryCode}` | Skills by category/career |

**Supported Career Paths:**
- `SCRUM_MASTER` → PSM_I, SCRUM_PSM_II
- `PRODUCT_OWNER` → SCRUM_PSPO_I
- `DEVELOPER` → DEV_BACKEND, DEV_FRONTEND, JAVA_OCP_17
- `QA_ENGINEER` → ISTQB_CTFL, ISTQB_AGILE, ISTQB_AI
- `BUSINESS_ANALYST` → CBAP, CCBA, ECBA
- `AGILE_COACH` → PSM_I, SCRUM_PSPO_I
- `PROJECT_MANAGER` → PMI_PMP
- `DEVOPS` → DEV_DEVOPS, DOCKER_DCA, KUBERNETES_CKA
- `CLOUD` → AWS_SAA_C03, AWS_DVA_C02, AZURE_AZ104, GCP_ACE
- `SECURITY` → COMPTIA_SECURITY_PLUS, ISC2_CISSP

### Key Implementation Notes

**Request Handling:**
- All EIL request DTOs have `@JsonIgnoreProperties(ignoreUnknown = true)` to handle frontend evolution

**Error Handling:**
- Error code `4013` (HTTP 409) for active session conflicts
- Error response includes `data.activeSessionId` for resume

**Performance:**
- Diagnostic returns ALL questions upfront (no repeated API calls)
- Practice returns 1 question at a time (adaptive selection)
- Redis caching for skill taxonomy

**Database Tables:**
- `eil_diagnostic_attempts` - Diagnostic sessions
- `eil_diagnostic_answers` - Diagnostic answers
- `eil_practice_sessions` - Practice sessions
- `eil_practice_results` - Practice results with mastery updates
- `eil_skills` - Skill taxonomy (legacy)
- `wp_ez_skills` - Skill taxonomy (WordPress, primary)
- `eil_skill_mastery` - User skill mastery tracking
- `eil_readiness_snapshots` - Readiness snapshots
- `eil_explanations` - Cached AI-generated explanations
- `eil_srs_cards` - Spaced repetition system cards
- `eil_srs_reviews` - SRS review history
- `eil_time_estimates` - Time-to-certification estimates
- `eil_ai_feedback` - AI-generated feedback records
- `eil_mock_results` - Mock exam results
```
