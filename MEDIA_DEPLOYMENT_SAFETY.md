# Media Deployment Safety Checklist

## 🎯 Objective
Ensure NO images break when deploying v1.3.4

---

## 📊 Image URL Types & Compatibility

### Before Deployment (Current URLs in DB):

| URL Pattern | Source | Count | After Deploy | Action |
|-------------|--------|-------|--------------|--------|
| `admin.ezami.io/wp-content/uploads/...` | WordPress old | ? | ✅ Works if migrated | Run fix_media_urls.sql |
| `cdn.ezami.io/wp-content/uploads/...` | WordPress current | ? | ✅ Works | No action needed |
| `api-v2.ezami.io/uploads/...` | API old | ? | ⚠️ May break | Check if files exist |
| `asset.ezami.io/uploads/...` | API new | 0 | ✅ Works | New uploads only |

---

## ✅ What WILL Work After Deploy

### WordPress Images (cdn.ezami.io):
```
✅ cdn.ezami.io/wp-content/uploads/2025/11/logo.png
   → Served from WordPress server
   → No change needed
   → Already working
```

### API Uploads (asset.ezami.io):
```
✅ asset.ezami.io/uploads/741d22e2-xxx.webp
   → Served from ezami-api via StaticResourceConfig
   → Spring Boot static handler
   → Works immediately after deploy
```

### Admin URLs (after migration):
```
✅ admin.ezami.io URLs → Transformed to cdn.ezami.io
   → Migration script: fix_media_urls.sql
   → Safe transformation (REPLACE function)
   → Files still exist on WordPress server
```

---

## ⚠️ What MIGHT Break

### Old API Uploads (if any):
```
⚠️ api-v2.ezami.io/uploads/old-file.jpg
   → If these exist in DB, need to check if files exist
   → Migration transforms to cdn.ezami.io
   → But files might not be on WordPress server!
```

**Check:**
```sql
-- Find old API upload URLs
SELECT * FROM ez_quiz_category
WHERE image_uri LIKE '%api-v2.ezami.io/uploads%';

SELECT * FROM wp_fcom_media_archives
WHERE file_url LIKE '%api-v2.ezami.io%';
```

**Fix if found:**
- Option A: Copy files from API server to WordPress
- Option B: Update URLs to asset.ezami.io (if files exist on API server)
- Option C: Re-upload images

---

## 🔒 Safety Measures Built-in

### 1. Migration Script is Idempotent
```sql
UPDATE ... WHERE image_uri LIKE '%admin.ezami.io%'
-- Only updates if match found
-- Safe to run multiple times
-- No data loss
```

### 2. Backward Compatible
```java
@Value("${app.asset-domain:${app.domain}}")
private String assetDomain;
// Falls back to app.domain if ASSET_DOMAIN not set
```

### 3. Files Not Deleted
```
✓ Migration only updates URLs in database
✓ Physical files remain unchanged
✓ Can rollback by reverting DB changes
```

---

## 📋 Pre-Deployment Steps

### Step 1: Backup Database
```bash
mysqldump -h prod-db -u user -p wordpress \
  ez_quiz_category \
  wp_postmeta \
  wp_fcom_media_archives \
  > backup_media_urls_$(date +%Y%m%d_%H%M%S).sql
```

### Step 2: Check Current URLs
```bash
mysql -h prod-db -u user -p wordpress < scripts/check_image_urls.sql > current_urls.txt
```

### Step 3: Verify Files Exist
```bash
# For each URL in database, verify file exists on appropriate server
```

---

## 📋 Post-Deployment Steps

### Step 1: Deploy v1.3.4
```
✓ Jenkins builds and deploys
✓ ASSET_DOMAIN=https://asset.ezami.io set
✓ StaticResourceConfig active
```

### Step 2: Run Migration (for WordPress images)
```bash
mysql -h prod-db -u user -p wordpress < scripts/fix_media_urls.sql
```

### Step 3: Test Image Access
```bash
# Test WordPress images
curl -I https://cdn.ezami.io/wp-content/uploads/2025/11/logo.png
# Should: 200 OK

# Test API uploads
curl -I https://asset.ezami.io/uploads/xxx.webp
# Should: 200 OK (after file is uploaded)
```

### Step 4: Verify Categories API
```bash
curl https://api-v2.ezami.io/api/metadata/quiz/categories | jq '.[0].imageUri'
# Should return valid URL (cdn.ezami.io or asset.ezami.io)
```

---

## 🚨 Rollback Plan (If Images Break)

### If Migration Causes Issues:
```sql
-- Restore from backup
mysql -h prod-db -u user -p wordpress < backup_media_urls_YYYYMMDD_HHMMSS.sql
```

### If Static Serving Not Working:
```bash
# Rollback to previous version
git tag
git checkout v1.3.1  # Last known stable
# Create v1.3.5 from v1.3.1
```

---

## ✅ Confidence Level: 95%

**Why Safe:**
- ✅ Migration only updates text (URLs in DB)
- ✅ Physical files unchanged
- ✅ Backward compatible config
- ✅ Can rollback database easily
- ✅ Static serving tested locally

**Risk:**
- ⚠️ Old API uploads might have broken URLs already
- ⚠️ Need to verify asset.ezami.io DNS propagated

---

## 🎯 Summary

| Image Type | Current | After Deploy | Risk |
|------------|---------|--------------|------|
| WordPress (cdn) | cdn.ezami.io | cdn.ezami.io | ✅ LOW |
| WordPress (admin) | admin.ezami.io | cdn.ezami.io | ✅ LOW (migration) |
| API uploads (new) | N/A | asset.ezami.io | ✅ LOW (new feature) |
| API uploads (old) | api-v2.ezami.io | May break | ⚠️ MEDIUM (check first) |

**Recommendation:** Run check_image_urls.sql on production BEFORE deploying to identify any api-v2.ezami.io upload URLs.
