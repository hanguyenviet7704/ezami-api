#!/bin/bash
# Backup script for Ezami database

BACKUP_DIR="/Users/kien/eup-project/ezami/ezami-api/backups/database"
DATE=$(date +%Y%m%d_%H%M%S)
DB_USER="root"
DB_PASS="12345678aA@"
DB_NAME="wordpress"
CONTAINER="ezami-mysql"

cd "$BACKUP_DIR" || exit 1

echo "=== Ezami Database Backup ==="
echo "Time: $(date)"
echo ""

# Full backup
echo "Creating comprehensive backup..."
docker exec $CONTAINER mysqldump -u$DB_USER -p$DB_PASS $DB_NAME \
  wp_learndash_pro_quiz_question \
  wp_learndash_pro_quiz_category \
  wp_ez_skills \
  wp_ez_question_skills \
  wp_ez_certifications \
  eil_diagnostic_attempts \
  eil_diagnostic_answers \
  eil_skill_mastery \
  eil_practice_sessions \
  eil_practice_attempts \
  > ezami_full_backup_$DATE.sql 2>/dev/null

# Active questions only (smaller)
echo "Creating active questions backup..."
docker exec $CONTAINER mysqldump -u$DB_USER -p$DB_PASS $DB_NAME \
  wp_learndash_pro_quiz_question \
  --where="online=1" \
  > questions_active_$DATE.sql 2>/dev/null

# Skills and mappings
echo "Creating skills backup..."
docker exec $CONTAINER mysqldump -u$DB_USER -p$DB_PASS $DB_NAME \
  wp_ez_skills \
  wp_ez_question_skills \
  wp_ez_certifications \
  > skills_and_mappings_$DATE.sql 2>/dev/null

# Compress old backups
echo "Compressing old backups..."
find . -name "*.sql" -mtime +1 -not -name "*$DATE*" -exec gzip {} \; 2>/dev/null

# Clean old compressed backups (keep 30 days)
find . -name "*.sql.gz" -mtime +30 -delete 2>/dev/null

echo ""
echo "=== Backup Complete ==="
ls -lh *$DATE*.sql | awk '{print $9, "-", $5}'

echo ""
echo "Total backup size:"
du -sh . | awk '{print $1}'
