# Jenkins Deployment Guide

**Pipeline:** Automated CI/CD via Jenkins
**Repository:** https://gitlab.com/eup/ezami/ezami-api
**Registry:** registry.gitlab.com/eup/ezami/ezami-api

---

## 🔄 DEPLOYMENT WORKFLOWS

### 1. Development Deployment (Auto)

**Trigger:** Push to `develop` branch

**Pipeline Flow:**
```
Push to develop
  ↓
Jenkins detects change
  ↓
Build Docker image (commit SHA)
  ↓
Push to GitLab registry
  ↓
Deploy to dev server (192.168.10.137)
  ↓
Update Docker Swarm service: ezami-api
```

**Servers:**
- Build server: `ansible@188.166.208.109`
- Deploy servers: `ansible@192.168.10.35`, `ansible@192.168.10.137`
- Service name: `ezami-api`

**Image tag:** `registry.gitlab.com/eup/ezami/ezami-api:{GIT_COMMIT_SHA}`

---

### 2. Production Deployment (Manual)

**Trigger:** Create Git tag on `main` branch

**Pipeline Flow:**
```
Merge develop → main
  ↓
Create tag (e.g., v1.3.0)
  ↓
Push tag to GitLab
  ↓
Jenkins builds production image
  ↓
Deploy to production (159.223.56.178)
  ↓
Update Docker Swarm service: ezami_api-v2
```

**Servers:**
- Build server: `ansible@188.166.208.109`
- Staging: `ansible@128.199.244.114` (pulls code)
- Production: `ansible@159.223.56.178` (Docker Swarm)
- Service name: `ezami_api-v2`

**Image tag:** `registry.gitlab.com/eup/ezami/ezami-api:{GIT_TAG}`

---

## 🚀 CURRENT DEPLOYMENT STATUS

### Latest Commit on Develop:
```
Commit: 4ca42c8
Branch: develop
Status: ✅ Ready for deployment
Changes: 9 commits, 50+ files, 8,900+ lines
```

### What Will Be Deployed:
1. ✅ Adaptive diagnostic system
2. ✅ Early termination logic
3. ✅ Confidence tracking
4. ✅ Certification taxonomy restructure
5. ✅ Performance optimizations
6. ✅ Database migrations (V9, V10)
7. ✅ Input validation
8. ✅ Feed bookmark & Space APIs

---

## 📋 PRE-DEPLOYMENT CHECKLIST

### Before Triggering Jenkins:

#### On Development:
- [x] All code committed to `develop`
- [x] All tests passed locally
- [x] Database migrations ready (V9, V10)
- [x] Documentation complete
- [x] Code review approved

#### On Jenkins:
- [ ] Verify Jenkins job configured
- [ ] Check GitLab registry credentials
- [ ] Verify SSH access to servers
- [ ] Check Docker Swarm status on target

#### On Target Servers:
- [ ] Database backup completed
- [ ] Flyway migrations ready to run
- [ ] Environment variables configured
- [ ] Health check endpoints accessible

---

## 🎯 TO TRIGGER DEPLOYMENT

### Option 1: Automatic (Develop)
**Already triggered!** Push to develop branch auto-deploys.

Check Jenkins: http://your-jenkins-url/job/ezami-api/

### Option 2: Manual (Production)
**Steps:**
```bash
# 1. Merge develop to main
git checkout main
git pull origin main
git merge develop
git push origin main

# 2. Create release tag
git tag -a v1.3.0 -m "Release v1.3.0: Adaptive Diagnostic System

Features:
- Adaptive diagnostic with early termination
- Confidence tracking (CAT mode)
- Certification taxonomy restructure
- Performance optimizations
- 46 certifications with proper categorization

Breaking Changes:
- Diagnostic API now adaptive (1 question at a time)
- questions array deprecated

Migration Required:
- Run V9 (performance indexes)
- Run V10 (certification taxonomy)
"

# 3. Push tag
git push origin v1.3.0

# 4. Jenkins auto-triggers production pipeline
```

---

## 🔍 MONITORING DEPLOYMENT

### Check Build Status:
```bash
# Watch Jenkins console output
# Or check GitLab CI/CD → Pipelines
```

### Verify Deployment:
```bash
# 1. Check service updated
ssh ansible@159.223.56.178
docker service ps ezami_api-v2 --no-trunc

# 2. Check logs
docker service logs ezami_api-v2 --tail 100 -f

# 3. Health check
curl https://api.ezami.vn/actuator/health

# 4. Test API
curl https://api.ezami.vn/api/certifications | jq '.data | length'
# Should return: 46
```

---

## 🗄️ DATABASE MIGRATION ON PRODUCTION

### After Docker Deploy:

**Jenkins does NOT auto-run Flyway!** Must run manually:

```bash
# SSH to production
ssh root@128.199.244.114

# Check current migration status
docker exec ezami_mysql.1.nekptgoz8esqh91tz64g7cqcb \
  mysql -uezami -p'2ld12dSaqqslojsl3nsd2' ezami \
  -e "SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;"

# Migrations will auto-run when API starts IF Flyway enabled
# OR run manually:
docker exec ezami_api-v2.xxx \
  java -jar app.jar flyway migrate
```

**Expected Migrations:**
- V9: Performance indexes (20+ indexes)
- V10: Certification taxonomy (2 new tables)

---

## 🚨 ROLLBACK PLAN

### If Deployment Fails:

#### Quick Rollback (5 minutes):
```bash
# Get previous image tag
PREVIOUS_TAG=$(docker service inspect ezami_api-v2 --format '{{.PreviousSpec.TaskTemplate.ContainerSpec.Image}}')

# Rollback
docker service update --image $PREVIOUS_TAG ezami_api-v2

# Verify
curl https://api.ezami.vn/actuator/health
```

#### Database Rollback (if needed):
```bash
# Restore from backup (created earlier)
mysql -uezami -p ezami < /root/backups/ezami_backup_20251225_130157.sql
```

---

## 📊 POST-DEPLOYMENT VERIFICATION

### Smoke Tests:
```bash
TOKEN="your-test-token"
API_URL="https://api.ezami.vn"

# 1. Health check
curl $API_URL/actuator/health

# 2. Certifications count
curl $API_URL/api/certifications -H "Authorization: Bearer $TOKEN" | jq '.data | length'
# Expected: 46

# 3. Start adaptive diagnostic
curl -X POST $API_URL/api/eil/diagnostic/start \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"mode":"CERTIFICATION_PRACTICE","certificationCode":"PSM_I","questionCount":10}'
# Expected: firstQuestion (not questions array)

# 4. Check taxonomy
curl "$API_URL/api/eil/skill/taxonomy?categoryCode=PSM_I" \
  -H "Authorization: Bearer $TOKEN" | jq '.data.skillCount'
# Expected: 93
```

---

## ⚙️ ENVIRONMENT VARIABLES

### Required on Production:
```env
# Database
DB_URL=jdbc:mysql://ezami_mysql:3306/ezami
DB_USER=ezami
DB_PASS=***

# Redis
SPRING_REDIS_HOST=ezami_redis
SPRING_REDIS_PORT=6379

# JWT
JWT_SECRET=***
JWT_COOKIE_SECURE=true

# Flyway
SPRING_FLYWAY_ENABLED=true
SPRING_FLYWAY_BASELINE_ON_MIGRATE=true

# External Services
GOOGLE_CLIENT_ID=***
GOOGLE_CLIENT_SECRET=***
FIREBASE_CONFIG_BASE64=***
```

---

## 🎯 DEPLOYMENT DECISION

### Current Status:
- Code: ✅ Ready (commit 4ca42c8)
- Tests: ✅ Passed
- Docs: ✅ Complete
- Review: ✅ Approved (Grade A)

### Recommended Path:

**Option 1: Deploy to Dev First (RECOMMENDED)**
```bash
# Already auto-triggered by push to develop
# Monitor Jenkins: http://jenkins-url/job/ezami-api/
# Wait 10-15 minutes for build + deploy
# Then test on dev: http://dev-api.ezami.vn
```

**Option 2: Direct to Production (RISKY)**
```bash
# Only if urgent and confident
git checkout main
git merge develop
git tag v1.3.0
git push origin main --tags
# Then monitor production carefully
```

---

**MY RECOMMENDATION:**

1. ✅ **Let Jenkins deploy to dev** (already triggered)
2. ⏰ **Wait 15 minutes** for pipeline
3. 🧪 **Test on dev environment**
4. ✅ **If tests pass** → Create tag for production
5. 🚀 **Production deploy tomorrow**

**Current Jenkins status: Should be running now (triggered by your push to develop)**

Check: Jenkins dashboard → ezami-api job → Latest build

---

**Need me to create the production release tag now?** (Not recommended - test dev first)