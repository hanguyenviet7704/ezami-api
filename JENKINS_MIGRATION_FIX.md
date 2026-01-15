# 🚨 CRITICAL: Jenkins Deployment Migration Issue

## Vấn đề phát hiện

### Flyway KHÔNG chạy khi deploy qua Jenkins

**Nguyên nhân:**
1. `application.yaml` có cấu hình: `flyway.enabled: ${FLYWAY_ENABLED:false}` (mặc định **false**)
2. `Jenkinsfile` KHÔNG set biến môi trường `FLYWAY_ENABLED=true`
3. Docker service update chỉ set các env khác, bỏ quên `FLYWAY_ENABLED`

**Hệ quả:**
- ✅ Migration V13 (add version columns) đã được tạo
- ❌ Nhưng sẽ KHÔNG được chạy tự động khi deploy lên production qua Jenkins!
- ❌ API sẽ bị lỗi 500 khi gọi `/api/eil/practice/start` vì thiếu cột `version`

## Giải pháp

### Option 1: Cập nhật Jenkinsfile (Recommended)

Thêm `--env-add FLYWAY_ENABLED=true` vào lệnh `docker service update`:

```groovy
// Trong Jenkinsfile, stage 'Deploy production', dòng 117-127
docker service update \
    --image registry.gitlab.com/eup/ezami/ezami-api:${env.GIT_TAG_NAME} \
    --env-add FLYWAY_ENABLED=true \  # ← THÊM DÒNG NÀY
    --env-add APP_DOMAIN=https://api-v2.ezami.io \
    --env-add ASSET_DOMAIN=https://asset.ezami.io \
    # ... các env khác ...
    ezami_api-v2
```

**Ưu điểm:**
- Migration tự động chạy khi deploy
- Không cần can thiệp thủ công vào database
- An toàn với Flyway's idempotency (chạy nhiều lần không sao)

**Rủi ro:**
- Nếu migration lỗi, service sẽ không start được
- Cần monitor logs kỹ sau khi deploy

### Option 2: Chạy migration thủ công TRƯỚC khi deploy (Safer)

#### Bước 1: SSH vào production server

```bash
ssh ansible@159.223.56.178
# hoặc
ssh ansible@128.199.244.114
```

#### Bước 2: Chạy migration V13 trực tiếp trên database

```bash
docker exec -i ezami-mysql mysql -uroot -p"${DB_PASSWORD}" wordpress << 'EOF'
-- Add version column to eil_practice_sessions
ALTER TABLE eil_practice_sessions
ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL
COMMENT 'Optimistic locking version for JPA @Version';

-- Add version column to eil_diagnostic_attempts
ALTER TABLE eil_diagnostic_attempts
ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL
COMMENT 'Optimistic locking version for JPA @Version';

-- Verify columns were added
SELECT
    'eil_practice_sessions' as table_name,
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_DEFAULT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA='wordpress'
    AND TABLE_NAME='eil_practice_sessions'
    AND COLUMN_NAME='version'
UNION ALL
SELECT
    'eil_diagnostic_attempts' as table_name,
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_DEFAULT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA='wordpress'
    AND TABLE_NAME='eil_diagnostic_attempts'
    AND COLUMN_NAME='version';
EOF
```

#### Bước 3: Cập nhật Flyway history (để Flyway biết V13 đã chạy)

```bash
docker exec -i ezami-mysql mysql -uroot -p"${DB_PASSWORD}" wordpress << 'EOF'
INSERT INTO flyway_schema_history
(installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES (
    (SELECT COALESCE(MAX(installed_rank), 0) + 1 FROM flyway_schema_history alias_table),
    '13',
    'add version columns for optimistic locking',
    'SQL',
    'V13__add_version_columns_for_optimistic_locking.sql',
    NULL,
    'root',
    NOW(),
    0,
    1
);
EOF
```

#### Bước 4: Verify migration

```bash
# Kiểm tra version columns
docker exec ezami-mysql mysql -uroot -p"${DB_PASSWORD}" -e \
  "SHOW COLUMNS FROM wordpress.eil_practice_sessions LIKE 'version';"

docker exec ezami-mysql mysql -uroot -p"${DB_PASSWORD}" -e \
  "SHOW COLUMNS FROM wordpress.eil_diagnostic_attempts LIKE 'version';"

# Kiểm tra Flyway history
docker exec ezami-mysql mysql -uroot -p"${DB_PASSWORD}" -e \
  "SELECT version, description, success, installed_on FROM wordpress.flyway_schema_history ORDER BY installed_rank DESC LIMIT 3;"
```

#### Bước 5: Deploy như bình thường qua Jenkins

Sau khi migration thủ công thành công, deploy code mới qua Jenkins.

**Ưu điểm:**
- Kiểm soát được quá trình migration
- Nếu migration lỗi, không ảnh hưởng đến service đang chạy
- Có thể rollback dễ dàng

**Nhược điểm:**
- Phải can thiệp thủ công
- Dễ quên bước nào đó

### Option 3: Enable Flyway mặc định (Not Recommended for Production)

Sửa `application.yaml`:

```yaml
flyway:
  enabled: ${FLYWAY_ENABLED:true}  # Đổi từ false → true
```

**❌ KHÔNG khuyến khích** vì:
- Migration tự động chạy mọi lúc, kể cả khi không mong muốn
- Khó debug khi có vấn đề
- Không có cơ hội review migration trước khi apply

## Kiểm tra hiện tại

### Local Development (đã fix)
```bash
✅ Version columns đã được thêm thủ công:
- eil_practice_sessions.version: BIGINT DEFAULT 0
- eil_diagnostic_attempts.version: BIGINT DEFAULT 0

✅ API hoạt động bình thường:
POST /api/eil/practice/start → HTTP 200
```

### Production (cần action)
```bash
❌ Chưa có version columns
❌ API sẽ bị lỗi 500 khi call practice/start
❌ Cần apply V13 migration TRƯỚC hoặc TRONG lần deploy tiếp theo
```

## Action Items

### Ngay lập tức (trước deploy tiếp theo):

- [ ] Quyết định Option 1 hay Option 2
- [ ] Nếu chọn Option 1: Cập nhật Jenkinsfile
- [ ] Nếu chọn Option 2: Chạy migration thủ công

### Dài hạn:

- [ ] Standardize migration process trong CI/CD
- [ ] Thêm pre-deployment migration check
- [ ] Document migration process trong CLAUDE.md
- [ ] Cân nhắc tạo separate migration job trong Jenkins

## Lệnh hữu ích

### Kiểm tra Flyway status từ xa

```bash
# Check current Flyway migrations
ssh ansible@159.223.56.178 "docker exec ezami-mysql mysql -uroot -p'password' -e \
  'SELECT version, description, success FROM wordpress.flyway_schema_history ORDER BY installed_rank DESC LIMIT 10;'"
```

### Backup database trước khi migrate

```bash
ssh ansible@159.223.56.178 "docker exec ezami-mysql mysqldump -uroot -p'password' wordpress > /backup/pre_v13_$(date +%Y%m%d_%H%M%S).sql"
```

### Rollback V13 nếu cần

```bash
# Xóa version columns
docker exec -i ezami-mysql mysql -uroot -p"password" wordpress << 'EOF'
ALTER TABLE eil_practice_sessions DROP COLUMN version;
ALTER TABLE eil_diagnostic_attempts DROP COLUMN version;
DELETE FROM flyway_schema_history WHERE version = '13';
EOF
```

## Tài liệu liên quan

- Migration file: `src/main/resources/db/migration/V13__add_version_columns_for_optimistic_locking.sql`
- Entity files:
  - `src/main/java/com/hth/udecareer/eil/entities/EilPracticeSessionEntity.java`
  - `src/main/java/com/hth/udecareer/eil/entities/EilDiagnosticAttemptEntity.java`
- Jenkins: `Jenkinsfile` (lines 101-133)
- Config: `src/main/resources/application.yaml` (lines 45-55)

---

**Created:** 2026-01-07
**Author:** Claude Code
**Severity:** P0 - CRITICAL (blocks practice API functionality)
