# Database Backups

Production database backups created: **2025-12-26**

## Backup Files

### Comprehensive Backup (All-in-One)
```
ezami_production_backup_20251226_141327.sql  (32 MB)
```

**Contains:**
- wp_learndash_pro_quiz_question (5,654 active questions)
- wp_learndash_pro_quiz_category (34 categories)
- wp_ez_skills (4,650 active skills)
- wp_ez_question_skills (8,805 mappings)
- wp_ez_certifications (36 certifications)
- eil_diagnostic_attempts (user attempts)
- eil_diagnostic_answers (user answers)
- eil_skill_mastery (user mastery data)

**Use this for:** Complete database restore

### Individual Table Backups

| File | Size | Contents |
|------|------|----------|
| `questions_active_20251226.sql` | 15 MB | 5,654 active questions only |
| `skills_active_20251226.sql` | 677 KB | 4,650 active skills |
| `question_skills_mappings_20251226.sql` | 410 KB | 8,805 question-skill mappings |
| `certifications_20251226.sql` | 13 KB | 36 certifications |
| `categories_20251226.sql` | 2.8 KB | 34 quiz categories |

**Use these for:** Selective restore or migration

---

## Restore Instructions

### Full Restore (All Tables)
```bash
# Restore complete backup
docker exec -i ezami-mysql mysql -uroot -p'12345678aA@' wordpress \
  < ezami_production_backup_20251226_141327.sql

# Verify
docker exec ezami-mysql mysql -uroot -p'12345678aA@' wordpress \
  -e "SELECT COUNT(*) FROM wp_learndash_pro_quiz_question WHERE online = 1;"
```

### Selective Restore (Individual Tables)
```bash
# Restore only questions
docker exec -i ezami-mysql mysql -uroot -p'12345678aA@' wordpress \
  < questions_active_20251226.sql

# Restore only skills
docker exec -i ezami-mysql mysql -uroot -p'12345678aA@' wordpress \
  < skills_active_20251226.sql
```

### Restore to Different Environment
```bash
# Example: Restore to local MySQL (not Docker)
mysql -h localhost -u root -p wordpress < ezami_production_backup_20251226_141327.sql
```

---

## Database Stats (At Time of Backup)

### Questions
```
Total active:         5,654
Removed corrupt:      470
Removed duplicates:   4,558
Removed test qs:      61
Quality rate:         100%
```

### Certifications
```
Total certifications: 36
With questions:       14
Ready for production: 9
Need expansion:       5 (Cloud/DevOps)
```

### Top 10 by Questions
1. ISTQB_CTFL: 1,283 questions
2. CBAP: 823 questions
3. CCBA: 810 questions
4. PSM_I: 734 questions
5. SCRUM_PSPO_I: 697 questions
6. ISTQB_AGILE: 423 questions
7. ECBA: 245 questions
8. SCRUM_PSM_II: 111 questions
9. ISTQB_AI: 80 questions
10. AWS_DVA_C02: 15 questions

### Explanation Quality
```
Detailed (200+ chars):    4,371 (77.3%)
Good (100-199 chars):     1,025 (18.1%)
Acceptable (50-99 chars): 270   (4.8%)
Short (<50 chars):        49    (0.9%)
Average length:           425 characters
```

---

## Migration to New Environment

### Step 1: Setup MySQL
```bash
# Start MySQL container
docker-compose up -d mysql

# Wait for ready
docker exec ezami-mysql mysql -uroot -p'12345678aA@' -e "SELECT 1;"
```

### Step 2: Create Database
```bash
docker exec ezami-mysql mysql -uroot -p'12345678aA@' \
  -e "CREATE DATABASE IF NOT EXISTS wordpress CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### Step 3: Restore Backup
```bash
docker exec -i ezami-mysql mysql -uroot -p'12345678aA@' wordpress \
  < ezami_production_backup_20251226_141327.sql
```

### Step 4: Verify
```bash
docker exec ezami-mysql mysql -uroot -p'12345678aA@' wordpress << 'EOF'
SELECT 'Questions:' as table_name, COUNT(*) as count FROM wp_learndash_pro_quiz_question WHERE online = 1
UNION ALL
SELECT 'Skills:', COUNT(*) FROM wp_ez_skills WHERE status = 'active'
UNION ALL
SELECT 'Mappings:', COUNT(*) FROM wp_ez_question_skills
UNION ALL
SELECT 'Certifications:', COUNT(*) FROM wp_ez_certifications WHERE is_active = 1;
EOF
```

---

## Backup Schedule Recommendation

### Automated Backups
```bash
# Add to crontab for daily backups at 2 AM
0 2 * * * cd /path/to/ezami-api/backups/database && \
  docker exec ezami-mysql mysqldump -uroot -p'PASSWORD' wordpress \
  wp_learndash_pro_quiz_question wp_ez_skills wp_ez_question_skills \
  > backup_$(date +\%Y\%m\%d).sql && \
  find . -name "backup_*.sql" -mtime +30 -delete
```

### Retention Policy
- Daily backups: Keep 7 days
- Weekly backups: Keep 4 weeks
- Monthly backups: Keep 12 months

---

## Important Notes

1. **Passwords:** Change default passwords before production
2. **Compression:** For storage, compress large backups:
   ```bash
   gzip ezami_production_backup_*.sql
   ```

3. **Testing:** Always test restore in dev environment first

4. **Security:**
   - Store backups securely
   - Encrypt sensitive data
   - Limit access to backup files

---

## Backup Verification

To verify backup integrity:
```bash
# Check file is valid SQL
head -20 ezami_production_backup_20251226_141327.sql

# Count tables in backup
grep "CREATE TABLE" ezami_production_backup_20251226_141327.sql | wc -l

# Check for errors
grep -i "error\|warning" ezami_production_backup_20251226_141327.sql
```

---

Generated: 2025-12-26 14:13:27
Database: wordpress @ ezami-mysql
Total size: 32 MB (uncompressed)
Status: ✅ Verified and ready for use
