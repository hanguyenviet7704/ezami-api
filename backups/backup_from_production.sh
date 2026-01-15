#!/bin/bash
# Backup production database from remote SSH server

# ===== CONFIGURATION - UPDATE THESE VALUES =====
PROD_SSH_HOST="your-production-server.com"
PROD_SSH_PORT="22"
PROD_SSH_USER="your-username"
PROD_SSH_KEY="~/.ssh/id_rsa"  # Path to SSH private key

# MySQL on production
PROD_MYSQL_HOST="localhost"  # or container name if using Docker
PROD_MYSQL_PORT="3306"
PROD_MYSQL_USER="root"
PROD_MYSQL_PASS="your-mysql-password"
PROD_DB_NAME="wordpress"

# Local backup directory
BACKUP_DIR="/Users/kien/eup-project/ezami/ezami-api/backups/database/production"
DATE=$(date +%Y%m%d_%H%M%S)

# ===== END CONFIGURATION =====

mkdir -p "$BACKUP_DIR"
cd "$BACKUP_DIR" || exit 1

echo "=== Production Database Backup ==="
echo "Server: $PROD_SSH_USER@$PROD_SSH_HOST"
echo "Date: $(date)"
echo ""

# Test SSH connection
echo "Testing SSH connection..."
ssh -i "$PROD_SSH_KEY" -p "$PROD_SSH_PORT" "$PROD_SSH_USER@$PROD_SSH_HOST" "echo 'SSH OK'" 2>/dev/null

if [ $? -ne 0 ]; then
    echo "❌ SSH connection failed!"
    echo "Please verify:"
    echo "  - SSH host: $PROD_SSH_HOST"
    echo "  - SSH port: $PROD_SSH_PORT"
    echo "  - SSH user: $PROD_SSH_USER"
    echo "  - SSH key: $PROD_SSH_KEY"
    exit 1
fi

echo "✅ SSH connection successful"
echo ""

# Method 1: Backup via SSH (if mysqldump available on production)
echo "Method 1: Direct mysqldump via SSH..."
ssh -i "$PROD_SSH_KEY" -p "$PROD_SSH_PORT" "$PROD_SSH_USER@$PROD_SSH_HOST" \
  "mysqldump -h $PROD_MYSQL_HOST -P $PROD_MYSQL_PORT -u$PROD_MYSQL_USER -p'$PROD_MYSQL_PASS' $PROD_DB_NAME \
   wp_learndash_pro_quiz_question \
   wp_learndash_pro_quiz_category \
   wp_ez_skills \
   wp_ez_question_skills \
   wp_ez_certifications \
   wp_ez_diagnostic_questions \
   eil_diagnostic_attempts \
   eil_diagnostic_answers \
   eil_skill_mastery \
   2>/dev/null" \
  > prod_full_backup_$DATE.sql

if [ -s prod_full_backup_$DATE.sql ]; then
    echo "✅ Full backup created: prod_full_backup_$DATE.sql"
else
    echo "⚠️  Method 1 failed, trying Method 2..."
    rm -f prod_full_backup_$DATE.sql

    # Method 2: Backup via Docker container on production
    echo "Method 2: Docker exec via SSH..."
    ssh -i "$PROD_SSH_KEY" -p "$PROD_SSH_PORT" "$PROD_SSH_USER@$PROD_SSH_HOST" \
      "docker exec ezami-mysql mysqldump -u$PROD_MYSQL_USER -p'$PROD_MYSQL_PASS' $PROD_DB_NAME \
       wp_learndash_pro_quiz_question \
       wp_learndash_pro_quiz_category \
       wp_ez_skills \
       wp_ez_question_skills \
       wp_ez_certifications \
       wp_ez_diagnostic_questions \
       2>/dev/null" \
      > prod_full_backup_$DATE.sql

    if [ -s prod_full_backup_$DATE.sql ]; then
        echo "✅ Full backup created via Docker: prod_full_backup_$DATE.sql"
    else
        echo "❌ Both methods failed!"
        exit 1
    fi
fi

# Backup questions only (lighter)
echo ""
echo "Creating questions-only backup..."
ssh -i "$PROD_SSH_KEY" -p "$PROD_SSH_PORT" "$PROD_SSH_USER@$PROD_SSH_HOST" \
  "docker exec ezami-mysql mysqldump -u$PROD_MYSQL_USER -p'$PROD_MYSQL_PASS' $PROD_DB_NAME \
   wp_learndash_pro_quiz_question --where='online=1' \
   2>/dev/null" \
  > prod_questions_only_$DATE.sql

echo "✅ Questions backup created"

# Compress backups
echo ""
echo "Compressing backups..."
gzip -k prod_full_backup_$DATE.sql 2>/dev/null
gzip -k prod_questions_only_$DATE.sql 2>/dev/null

echo ""
echo "=== Backup Complete ==="
ls -lh *$DATE* | awk '{printf "%-55s %10s\n", $9, $5}'

echo ""
echo "Total backup size:"
du -sh . | awk '{print $1}'
