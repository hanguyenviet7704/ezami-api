# Production Deployment Guide

**Deployment Package:** ezami_deploy_20251227_083122.sql.gz
**Package Size:** 6.9 MB (compressed), 56 MB (uncompressed)
**Created:** 2025-12-27 08:31:22
**Status:** ✅ Ready for deployment

---

## Package Contents

```
Active Questions:     12,575  (+1,799 from production)
Categories:           59      (+32 new)
Skills:              4,650
Skill Mappings:      14,250
Certifications:      46      (+19 new)
wp_ez Questions:     6,390   (NEW - source data)
```

---

## What's New in This Deployment

### 1. **Enhanced Questions** (+1,799)
- ✅ Imported 6,922 questions from wp_ez_diagnostic_questions
- ✅ Removed 5,028 corrupt/duplicate questions
- ✅ Net gain: +1,799 quality questions

### 2. **New Certifications** (+19)
- DEV_GOLANG (588 questions)
- DEV_PYTHON (476 questions)
- JAVA_OCP_17 (260 questions)
- DEV_SYSTEM_DESIGN (282 questions)
- DEV_SQL_DATABASE (282 questions)
- DEV_FRONTEND (258 questions)
- +13 more certifications

### 3. **Data Quality Improvements**
- ✅ 100% questions have explanations (was ~75%)
- ✅ All HTML cleaned and normalized
- ✅ Distinct correct/incorrect messages
- ✅ Zero corrupt questions (removed 470)
- ✅ Zero duplicates (removed 4,558)

### 4. **Unified Skills System**
- ✅ Migrated to wp_ez_skills (4,650 skills)
- ✅ Better skill coverage per certification
- ✅ Clean skill-question mappings (14,250)

---

## Pre-Deployment Checklist

### 1. Backup Current Production ✅
```bash
# Already done
backups/database/production/production_learndash_20251226_193446.sql.gz (3.7 MB)
```

### 2. Verify Deployment Package ✅
```bash
# Check MD5 checksum
md5 ezami_deploy_20251227_083122.sql.gz
cat ezami_deploy_20251227_083122.sql.gz.md5
# Should match

# Verify file integrity
gunzip -t ezami_deploy_20251227_083122.sql.gz
# Should output: OK
```

### 3. Test in Staging (RECOMMENDED)
- [ ] Deploy to staging environment
- [ ] Run API tests
- [ ] Verify question loading
- [ ] Check explanation display
- [ ] Test all certifications

---

## Deployment Steps

### Step 1: Upload Package to Production

```bash
# Upload deployment package
scp -i ~/.ssh/id_rsa_ezami \
  ezami_deploy_20251227_083122.sql.gz \
  root@128.199.244.114:/tmp/

# Upload MD5 checksum
scp -i ~/.ssh/id_rsa_ezami \
  ezami_deploy_20251227_083122.sql.gz.md5 \
  root@128.199.244.114:/tmp/
```

### Step 2: Connect to Production

```bash
ssh -i ~/.ssh/id_rsa_ezami root@128.199.244.114
```

### Step 3: Verify Upload

```bash
cd /tmp

# Verify checksum
md5sum ezami_deploy_20251227_083122.sql.gz
cat ezami_deploy_20251227_083122.sql.gz.md5
# Should match

# Decompress
gunzip ezami_deploy_20251227_083122.sql.gz
```

### Step 4: Create Pre-Deployment Backup

```bash
# Backup current production state
docker exec ezami_mysql.1.nekptgoz8esqh91tz64g7cqcb \
  mysqldump -uezami -p'2ld12dSaqqslojsl3nsd2' ezami \
  wp_learndash_pro_quiz_question \
  wp_learndash_pro_quiz_category \
  > /tmp/pre_deploy_backup_$(date +%Y%m%d_%H%M%S).sql

echo "✅ Pre-deployment backup created"
```

### Step 5: Deploy Database

```bash
# Restore new database
docker exec -i ezami_mysql.1.nekptgoz8esqh91tz64g7cqcb \
  mysql -uezami -p'2ld12dSaqqslojsl3nsd2' ezami \
  < /tmp/ezami_deploy_20251227_083122.sql

echo "✅ Database deployed"
```

### Step 6: Verify Deployment

```bash
# Check question count
docker exec ezami_mysql.1.nekptgoz8esqh91tz64g7cqcb \
  mysql -uezami -p'2ld12dSaqqslojsl3nsd2' ezami \
  -e "SELECT COUNT(*) as active FROM wp_learndash_pro_quiz_question WHERE online = 1;"

# Should show: 12,575

# Check categories
docker exec ezami_mysql.1.nekptgoz8esqh91tz64g7cqcb \
  mysql -uezami -p'2ld12dSaqqslojsl3nsd2' ezami \
  -e "SELECT COUNT(*) as categories FROM wp_learndash_pro_quiz_category;"

# Should show: 59
```

### Step 7: Restart API

```bash
# Restart API container to reload data
docker service update --force ezami_api

# Or if using docker-compose
docker-compose restart api

# Wait and check health
sleep 30
curl http://localhost:8090/actuator/health
```

### Step 8: Test APIs

```bash
# Get test token
TOKEN=$(curl -s -X POST "http://localhost:8090/authenticate" \
  -H "Content-Type: application/json" \
  -d '{"username":"test@ezami.com","password":"test123"}' \
  | jq -r '.data.jwtToken')

# Test Diagnostic API
curl -X POST "http://localhost:8090/api/eil/diagnostic/start" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"certificationCode":"DEV_GOLANG","questionCount":5}' \
  | jq '.data.totalQuestions'

# Should return: 5

# Test Practice API
curl -X POST "http://localhost:8090/api/eil/practice/start" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"sessionType":"ADAPTIVE","maxQuestions":10}' \
  | jq '.data.sessionId'

# Should return: session ID
```

---

## Rollback Plan (If Issues Occur)

### Quick Rollback

```bash
# Restore pre-deployment backup
docker exec -i ezami_mysql.1.nekptgoz8esqh91tz64g7cqcb \
  mysql -uezami -p'2ld12dSaqqslojsl3nsd2' ezami \
  < /tmp/pre_deploy_backup_YYYYMMDD_HHMMSS.sql

# Restart API
docker service update --force ezami_api

# Verify
curl http://localhost:8090/actuator/health
```

---

## Post-Deployment Monitoring

### Check Logs
```bash
# API logs
docker service logs ezami_api --tail 100 -f

# MySQL logs
docker service logs ezami_mysql --tail 100
```

### Monitor Metrics
- Response times
- Error rates
- Question load success rate
- User feedback on new questions

### Database Queries
```bash
# Top certifications
docker exec ezami_mysql.1.nekptgoz8esqh91tz64g7cqcb \
  mysql -uezami -p'PASSWORD' ezami -e "
SELECT s.certification_id, COUNT(DISTINCT qs.question_id) as questions
FROM wp_ez_question_skills qs
JOIN wp_ez_skills s ON qs.skill_id = s.id
JOIN wp_learndash_pro_quiz_question q ON qs.question_id = q.id
WHERE q.online = 1
GROUP BY s.certification_id
ORDER BY questions DESC
LIMIT 10;"
```

---

## Deployment Timeline

| Phase | Duration | Status |
|-------|----------|--------|
| Upload package | 2 min | ⏳ Pending |
| Verify checksum | 30 sec | ⏳ Pending |
| Pre-deploy backup | 3 min | ⏳ Pending |
| Deploy database | 5 min | ⏳ Pending |
| Restart services | 2 min | ⏳ Pending |
| Verify & test | 5 min | ⏳ Pending |
| **Total** | **~18 min** | |

---

## Success Criteria

✅ Database restored without errors
✅ Active questions = 12,575
✅ Categories = 59
✅ API health check returns 200
✅ Diagnostic API returns questions
✅ Practice API returns questions
✅ No errors in logs
✅ New certifications visible (DEV_GOLANG, JAVA, etc.)

---

## Emergency Contacts

- **Rollback:** Use pre_deploy_backup
- **Support:** Check API logs first
- **Database:** MySQL container ezami_mysql

---

**Package ready for deployment!** 🚀

Deploy command summary:
```bash
# 1. Upload
scp -i ~/.ssh/id_rsa_ezami ezami_deploy_*.sql.gz root@128.199.244.114:/tmp/

# 2. Deploy (on production server)
ssh -i ~/.ssh/id_rsa_ezami root@128.199.244.114
gunzip /tmp/ezami_deploy_*.sql.gz
docker exec -i ezami_mysql.1.* mysql -uezami -p'PASSWORD' ezami < /tmp/ezami_deploy_*.sql

# 3. Restart API
docker service update --force ezami_api
```
