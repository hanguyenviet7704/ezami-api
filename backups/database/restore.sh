#!/bin/bash
# Restore script for Ezami database

BACKUP_DIR="/Users/kien/eup-project/ezami/ezami-api/backups/database"
DB_USER="root"
DB_PASS="12345678aA@"
DB_NAME="wordpress"
CONTAINER="ezami-mysql"

if [ "$#" -lt 1 ]; then
    echo "Usage: ./restore.sh <backup_file>"
    echo ""
    echo "Available backups:"
    ls -lh *.sql *.sql.gz 2>/dev/null | awk '{print "  " $9, "-", $5}'
    exit 1
fi

BACKUP_FILE=$1

if [ ! -f "$BACKUP_FILE" ]; then
    echo "Error: Backup file not found: $BACKUP_FILE"
    exit 1
fi

echo "=== Ezami Database Restore ==="
echo "Backup file: $BACKUP_FILE"
echo "Target: $CONTAINER/$DB_NAME"
echo ""

# Confirm
read -p "This will REPLACE current data. Continue? (yes/no): " confirm
if [ "$confirm" != "yes" ]; then
    echo "Restore cancelled."
    exit 0
fi

# Decompress if needed
if [[ $BACKUP_FILE == *.gz ]]; then
    echo "Decompressing..."
    gunzip -k "$BACKUP_FILE"
    BACKUP_FILE="${BACKUP_FILE%.gz}"
fi

# Create pre-restore backup
echo "Creating safety backup..."
SAFETY_BACKUP="pre_restore_backup_$(date +%Y%m%d_%H%M%S).sql"
docker exec $CONTAINER mysqldump -u$DB_USER -p$DB_PASS $DB_NAME \
  wp_learndash_pro_quiz_question \
  wp_ez_skills \
  wp_ez_question_skills \
  > "$SAFETY_BACKUP" 2>/dev/null
echo "Safety backup: $SAFETY_BACKUP"

# Restore
echo ""
echo "Restoring database..."
docker exec -i $CONTAINER mysql -u$DB_USER -p$DB_PASS $DB_NAME < "$BACKUP_FILE"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Restore successful!"

    # Verify
    echo ""
    echo "Verification:"
    docker exec $CONTAINER mysql -u$DB_USER -p$DB_PASS $DB_NAME << 'EOF'
SELECT 'Active questions:' as metric, COUNT(*) as count FROM wp_learndash_pro_quiz_question WHERE online = 1
UNION ALL
SELECT 'Active skills:', COUNT(*) FROM wp_ez_skills WHERE status = 'active'
UNION ALL
SELECT 'Skill mappings:', COUNT(*) FROM wp_ez_question_skills
UNION ALL
SELECT 'Certifications:', COUNT(*) FROM wp_ez_certifications WHERE is_active = 1;
EOF

    echo ""
    echo "✅ Database restored and verified"
else
    echo ""
    echo "❌ Restore failed!"
    echo "To rollback, run:"
    echo "  ./restore.sh $SAFETY_BACKUP"
    exit 1
fi
