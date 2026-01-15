# Production Database Backup

**Server:** 128.199.244.114
**Database:** ezami
**Backup Date:** 2025-12-26 19:30
**Status:** ✅ Verified

---

## Backup Files

| File | Size | Compressed | Contents |
|------|------|------------|----------|
| production_learndash_20251226_193446.sql | 34 MB | 3.7 MB | Questions + Categories |
| production_questions_20251226_193446.sql | 31 MB | 3.4 MB | Questions only |

---

## Production Database Stats (At Backup Time)

```
Total questions:      12,079
Active questions:     10,776
Categories:           27
MySQL container:      ezami_mysql.1.nekptgoz8esqh91tz64g7cqcb
```

---

## Comparison: Production vs Local Dev

| Metric | Production | Local Dev | Difference |
|--------|------------|-----------|------------|
| Questions | 10,776 | 12,576 | +1,800 (local has wp_ez import) |
| Categories | 27 | 59 | +32 (local has new certs) |
| Skills system | ❌ No wp_ez | ✅ Has wp_ez | Local is ahead |
| Explanations | ⚠️ Original | ✅ Normalized | Local is better |

**Conclusion:** Local dev is **ahead** of production with enhanced data.

---

## Restore to Production

### Option 1: Restore Original Production Data
```bash
cd /Users/kien/eup-project/ezami/ezami-api/backups/database/production

# Decompress
gunzip -k production_learndash_20251226_193446.sql.gz

# Restore to local for testing
docker exec -i ezami-mysql mysql -uroot -p'12345678aA@' wordpress \
  < production_learndash_20251226_193446.sql
```

### Option 2: Deploy Enhanced Data to Production
```bash
# Upload local enhanced backup to production
scp -i ~/.ssh/id_rsa_ezami \
  ../ezami_production_full_20251226_180552.sql.gz \
  root@128.199.244.114:/tmp/

# SSH and restore
ssh -i ~/.ssh/id_rsa_ezami root@128.199.244.114 << 'EOF'
  gunzip /tmp/ezami_production_full_20251226_180552.sql.gz
  docker exec -i ezami_mysql.1.nekptgoz8esqh91tz64g7cqcb \
    mysql -uezami -p'2ld12dSaqqslojsl3nsd2' ezami \
    < /tmp/ezami_production_full_20251226_180552.sql
EOF
```

**Recommendation:** Test in staging first!

---

## Production Deployment Checklist

### Pre-deployment
- [ ] Backup current production (✅ DONE)
- [ ] Test enhanced data in local/staging
- [ ] Verify all APIs work correctly
- [ ] Run quality checks
- [ ] Get stakeholder approval

### Deployment
- [ ] Upload enhanced backup to production
- [ ] Put site in maintenance mode
- [ ] Restore database
- [ ] Run Flyway migrations (if needed)
- [ ] Restart API container
- [ ] Verify health checks

### Post-deployment
- [ ] Test critical user flows
- [ ] Monitor error logs
- [ ] Verify question quality
- [ ] Check performance metrics
- [ ] Remove maintenance mode

---

## What Production is Missing (In Local Dev)

### 1. Enhanced Questions (+1,800)
- ✅ 6,922 questions from wp_ez_diagnostic_questions
- ✅ Includes DEV_GOLANG, JAVA_OCP_17, etc.

### 2. Normalized Explanations
- ✅ HTML cleaned
- ✅ Distinct correct/incorrect messages
- ✅ Context-aware content

### 3. Unified Skills System
- ✅ wp_ez_skills (4,650 skills)
- ✅ wp_ez_question_skills (15,510 mappings)
- ✅ 46 certifications covered

### 4. Quality Improvements
- ✅ Removed 5,028 corrupt/duplicate questions
- ✅ 100% questions have explanations
- ✅ All data validated

---

## SSH Connection Info

```bash
Host: 128.199.244.114
Port: 22
User: root
Key:  ~/.ssh/id_rsa_ezami

MySQL:
  Container: ezami_mysql.1.nekptgoz8esqh91tz64g7cqcb
  User: ezami
  Database: ezami
  Host: mysql:3306 (internal)
```

---

## Quick Commands

### Backup from Production
```bash
ssh -i ~/.ssh/id_rsa_ezami root@128.199.244.114 \
  "docker exec ezami_mysql.1.nekptgoz8esqh91tz64g7cqcb mysqldump -uezami -p'PASSWORD' ezami \
   wp_learndash_pro_quiz_question wp_learndash_pro_quiz_category" \
  > backup.sql
```

### Upload to Production
```bash
scp -i ~/.ssh/id_rsa_ezami local_backup.sql root@128.199.244.114:/tmp/
```

### Restore on Production
```bash
ssh -i ~/.ssh/id_rsa_ezami root@128.199.244.114 \
  "docker exec -i ezami_mysql.1.nekptgoz8esqh91tz64g7cqcb \
   mysql -uezami -p'PASSWORD' ezami < /tmp/backup.sql"
```

---

**Production backup complete and stored in codebase!** ✅

Next step: Deploy enhanced local database to production (optional)
