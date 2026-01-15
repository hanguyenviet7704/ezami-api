# 📚 Hướng dẫn Tối ưu hóa Taxonomy (Categories/Terms)

## 🔍 Tổng quan

Tài liệu này hướng dẫn cách kiểm tra và tối ưu hóa cấu trúc danh mục (taxonomy) trong database WordPress của Ezami.

## ⚠️ Vấn đề hiện tại

### 1. Duplicate Categories nghiêm trọng
- **Tổng số categories**: 203
- **Số categories unique**: 20
- **Tỷ lệ duplicate**: ~91%

**Nguyên nhân**: Plugin Polylang (đa ngôn ngữ) tạo ra các bản sao với suffix `-pll_vi`, `-pll_en`

**Top duplicates**:
- "Agile, Scrum & Kanban": 71 bản sao
- "BABOK Guide v3 (Tiếng Việt)": 47 bản sao
- "ISTQB Foundation v3.1 (Tiếng Việt)": 27 bản sao

### 2. Cấu trúc phân mảnh
- WordPress taxonomy: `wp_terms`, `wp_term_taxonomy`, `wp_term_relationships`
- Community taxonomy: `wp_fcom_terms` (chưa sử dụng)
- Article categories: `ez_article_space_category` (1 record)
- Quiz categories: `ez_quiz_category`, `wp_learndash_pro_quiz_category`

### 3. Thiếu relationships
- Hầu hết posts không có category relationships
- Courses: 2 posts, 0 relationships
- Quizzes: 20 quizzes, 0 relationships

## 🛠️ Giải pháp

### Option 1: API Endpoints (Khuyến nghị cho kiểm tra)

#### 1.1. Kiểm tra thống kê

```bash
# Get taxonomy statistics
curl -X GET "http://localhost:8090/api/admin/taxonomy/statistics" \
  -H "Authorization: Bearer $TOKEN"

# Response:
{
  "code": 200,
  "data": {
    "category": 203,
    "post_tag": 834,
    "ld_quiz_category": 7,
    ...
  }
}
```

#### 1.2. Phân tích duplicates

```bash
# Analyze duplicates
curl -X GET "http://localhost:8090/api/admin/taxonomy/duplicates/analyze" \
  -H "Authorization: Bearer $TOKEN"

# Response:
{
  "code": 200,
  "data": {
    "duplicatesByName": {
      "Agile, Scrum & Kanban": [2152, 2298, 2299, ...],
      "BABOK Guide v3": [2116, 2248, 2249, ...]
    },
    "polylangDuplicates": {
      "2152": [2298, 2299, 2301, ...],
      "2116": [2248, 2249, 2250, ...]
    },
    "totalDuplicateGroups": 12,
    "totalPolylangGroups": 12,
    "totalDuplicateTerms": 183
  }
}
```

#### 1.3. Xem hierarchy

```bash
# Get category hierarchy
curl -X GET "http://localhost:8090/api/admin/taxonomy/hierarchy" \
  -H "Authorization: Bearer $TOKEN"

# Response:
{
  "code": 200,
  "data": {
    "ROOT": ["Kiểm thử phần mềm", "Phân tích nghiệp vụ", "Quản lý dự án"],
    "Kiểm thử phần mềm": ["ISTQB Foundation v3.1", "ISTQB Agile Tester"],
    "Quản lý dự án": ["Agile, Scrum & Kanban", "EBM Guide 2020"]
  }
}
```

#### 1.4. Dry run cleanup

```bash
# Test cleanup (không xóa gì cả)
curl -X POST "http://localhost:8090/api/admin/taxonomy/duplicates/dry-run" \
  -H "Authorization: Bearer $TOKEN"

# Response:
{
  "code": 200,
  "data": {
    "wouldRemove": 183,
    "message": "This is a dry run. No data was deleted.",
    "warning": "To actually cleanup, run the SQL script: scripts/cleanup_duplicate_categories.sql"
  }
}
```

### Option 2: SQL Scripts (Khuyến nghị cho cleanup thực tế)

#### 2.1. Backup Database

```bash
# Backup toàn bộ database
docker exec ezami-mysql mysqldump -u root -p12345678aA@ wordpress > backup_$(date +%Y%m%d_%H%M%S).sql

# Hoặc chỉ backup taxonomy tables
docker exec ezami-mysql mysqldump -u root -p12345678aA@ wordpress \
  wp_terms wp_term_taxonomy wp_term_relationships \
  > backup_taxonomy_$(date +%Y%m%d_%H%M%S).sql
```

#### 2.2. Chạy Cleanup Script

```bash
# Step 1: Review script (dry run)
docker exec -i ezami-mysql mysql -u root -p12345678aA@ < scripts/cleanup_duplicate_categories.sql

# Script sẽ hiển thị:
# - Danh sách categories sẽ bị xóa
# - Canonical terms sẽ được giữ lại
# - Số lượng duplicates

# Step 2: Kích hoạt cleanup
# Mở file và uncomment các phần:
# - Step 4: Update term_relationships
# - Step 5: Delete duplicate term_taxonomy
# - Step 6: Delete duplicate terms
# - Step 7: Clean up slugs
# - Step 8: Update term counts

# Step 3: Chạy lại script
docker exec -i ezami-mysql mysql -u root -p12345678aA@ < scripts/cleanup_duplicate_categories.sql
```

#### 2.3. Verify Results

```bash
# Kiểm tra duplicates còn lại
docker exec ezami-mysql mysql -u root -p12345678aA@ -e "
USE wordpress;
SELECT
    t.name,
    COUNT(*) as count,
    GROUP_CONCAT(t.term_id) as term_ids
FROM wp_terms t
JOIN wp_term_taxonomy tt ON t.term_id = tt.term_id
WHERE tt.taxonomy = 'category'
GROUP BY t.name
HAVING COUNT(*) > 1;
"

# Expected: Không còn duplicates (0 rows)
```

#### 2.4. Optimize Structure (Optional)

```bash
# Chạy optimization script
docker exec -i ezami-mysql mysql -u root -p12345678aA@ < scripts/optimize_taxonomy_structure.sql

# Script này sẽ:
# - Thêm indexes cho performance
# - Consolidate các bảng taxonomy
# - Update term counts
# - Xóa orphaned records
```

### Option 3: Java Service (Cho automation)

Sử dụng `TaxonomyOptimizationService` trong code:

```java
@Autowired
private TaxonomyOptimizationService taxonomyService;

// Analyze duplicates
Map<String, List<Long>> duplicates = taxonomyService.analyzeDuplicateCategories();

// Get statistics
Map<String, Long> stats = taxonomyService.getTaxonomyStatistics();

// Get hierarchy
Map<String, List<String>> hierarchy = taxonomyService.getCategoryHierarchy();

// Dry run
int wouldRemove = taxonomyService.dryRunCleanup();
```

## 📊 Cấu trúc đề xuất sau khi tối ưu

```
📁 Kiểm thử phần mềm (Software Testing)
   └─ 📁 ISTQB
      ├─ ISTQB Foundation v3.1 (Tiếng Việt)
      ├─ ISTQB Foundation Agile Tester
      └─ ISTQB AI Testing

📁 Phân tích nghiệp vụ (Business Analysis)
   └─ 📁 BABOK
      └─ BABOK Guide v3 (Tiếng Việt)

📁 Quản lý dự án (Project Management)
   └─ 📁 Agile & Scrum
      ├─ Scrum Guide
      ├─ PSM I
      ├─ PSM II
      ├─ PSPO I
      └─ EBM Guide 2020

📁 Lập trình (Development)
   └─ 📁 Java
      └─ Java OCP 17

📁 Cloud & DevOps
   ├─ 📁 AWS
   │  ├─ AWS SAA-C03
   │  └─ AWS DVA-C02
   ├─ 📁 Azure
   │  └─ Azure AZ-104
   └─ 📁 Kubernetes
      └─ CKA
```

## 🔐 Security & Permissions

**QUAN TRỌNG**: Tất cả admin endpoints yêu cầu `ADMIN` role:

```java
@PreAuthorize("hasRole('ADMIN')")
```

Đảm bảo user có role ADMIN trong database:

```sql
-- Check user roles
SELECT u.user_login, um.meta_value as roles
FROM wp_users u
JOIN wp_usermeta um ON u.ID = um.user_id
WHERE um.meta_key = 'wp_capabilities';

-- Grant admin role (nếu cần)
UPDATE wp_usermeta
SET meta_value = 'a:1:{s:13:"administrator";b:1;}'
WHERE user_id = 1 AND meta_key = 'wp_capabilities';
```

## ⚡ Performance Tips

### 1. Indexes đã được thêm

```sql
-- wp_term_taxonomy
ALTER TABLE wp_term_taxonomy
    ADD INDEX idx_taxonomy_parent (taxonomy, parent),
    ADD INDEX idx_term_taxonomy (term_id, taxonomy);

-- wp_term_relationships
ALTER TABLE wp_term_relationships
    ADD INDEX idx_object_taxonomy (object_id, term_taxonomy_id);
```

### 2. Caching

Service đã sử dụng Spring Cache:

```java
@Cacheable(value = "taxonomy_stats")
public Map<String, Long> getTaxonomyStatistics() { ... }
```

## 🔄 Rollback Plan

Nếu có vấn đề, rollback từ backup:

```bash
# Stop API
docker-compose down

# Restore backup
docker exec -i ezami-mysql mysql -u root -p12345678aA@ wordpress < backup_YYYYMMDD_HHMMSS.sql

# Hoặc chỉ restore taxonomy tables
docker exec -i ezami-mysql mysql -u root -p12345678aA@ -e "
USE wordpress;
TRUNCATE wp_terms;
TRUNCATE wp_term_taxonomy;
TRUNCATE wp_term_relationships;
"
docker exec -i ezami-mysql mysql -u root -p12345678aA@ wordpress < backup_taxonomy_YYYYMMDD_HHMMSS.sql

# Restart API
docker-compose up -d
```

## 📝 Checklist

### Pre-Cleanup
- [ ] Backup database
- [ ] Test API endpoints với dry-run
- [ ] Review danh sách duplicates
- [ ] Thông báo team về maintenance

### Cleanup
- [ ] Chạy dry-run script trước
- [ ] Verify kết quả dry-run
- [ ] Uncomment các câu lệnh DELETE/UPDATE
- [ ] Chạy cleanup script thực tế
- [ ] Verify không còn duplicates

### Post-Cleanup
- [ ] Test API endpoints
- [ ] Kiểm tra frontend hiển thị categories
- [ ] Verify relationships intact
- [ ] Update documentation
- [ ] Xóa backup cũ (sau 7 ngày)

## 📞 Support

Nếu có vấn đề:
1. Check logs: `docker logs -f ezami-api-server`
2. Review backup file
3. Rollback nếu cần
4. Contact team lead

## 📚 Related Files

- Service: [TaxonomyOptimizationService.java](../src/main/java/com/hth/udecareer/service/TaxonomyOptimizationService.java)
- Controller: [TaxonomyAdminController.java](../src/main/java/com/hth/udecareer/controllers/TaxonomyAdminController.java)
- Repositories:
  - [TermRepository.java](../src/main/java/com/hth/udecareer/repository/TermRepository.java)
  - [TermTaxonomyRepository.java](../src/main/java/com/hth/udecareer/repository/TermTaxonomyRepository.java)
- Scripts:
  - [cleanup_duplicate_categories.sql](cleanup_duplicate_categories.sql)
  - [optimize_taxonomy_structure.sql](optimize_taxonomy_structure.sql)

---

**Last Updated**: 2024-12-25
**Version**: 1.0
**Author**: Claude Code
