# Tối ưu hiệu năng phân trang Quiz

## Vấn đề ban đầu

- Query lấy toàn bộ quiz rồi mới lọc/phân trang trong bộ nhớ
- N+1 queries khi load dữ liệu liên quan
- Thiếu index cho các truy vấn thường dùng

## Giải pháp đã triển khai

### 1. Database-level Pagination

- **Trước**: `findActiveQuizzes()` → lọc Java → `PageableUtil.listToPage()`
- **Sau**: `findActiveQuizzesPage()` với JPQL + `Pageable` + `countQuery`

### 2. Query Optimization

- **INNER JOIN** thay vì comma join để tối ưu execution plan
- **Batch loading** dữ liệu liên quan thay vì N+1 queries
- **Filtered queries** chỉ load dữ liệu cho trang hiện tại

### 3. Database Indexes

```sql
-- Indexes cần thiết (đã tạo trong quiz_pagination_indexes.sql)
ALTER TABLE wp_posts ADD INDEX idx_posts_status_type_date_id (post_status, post_type, post_date, ID);
ALTER TABLE wp_learndash_pro_quiz_master ADD INDEX idx_quiz_master_name (name(100));
ALTER TABLE wp_learndash_pro_quiz_master ADD INDEX idx_quiz_master_id (id);
ALTER TABLE wp_postmeta ADD INDEX idx_postmeta_post_key_value (post_id, meta_key(50), meta_value(50));
ALTER TABLE wp_learndash_user_activity ADD INDEX idx_user_activity_user_post (user_id, post_id);
ALTER TABLE wp_learndash_user_activity_meta ADD INDEX idx_user_activity_meta_activity (activity_id);
ALTER TABLE wp_learndash_pro_quiz_statistic ADD INDEX idx_quiz_statistic_ref (statistic_ref_id);
ALTER TABLE wp_learndash_pro_quiz_question ADD INDEX idx_quiz_question_quiz (quiz_id);
ALTER TABLE wp_learndash_pro_quiz_question ADD INDEX idx_quiz_question_id (id);
```

### 4. Service Layer Optimization

- **Batch queries**: Load tất cả dữ liệu liên quan trong 1 lần
- **Conditional loading**: Chỉ load activity meta khi có activity
- **Optimized user activity**: Query chỉ cho postIds trong trang hiện tại

## Cách áp dụng

### Bước 1: Chạy SQL indexes

```bash
mysql -u wordpress -p wordpress < docs/ddl/performance/quiz_pagination_indexes.sql
```

### Bước 2: Kiểm tra hiệu năng

```sql
-- Kiểm tra query plan
EXPLAIN SELECT qm.id AS id, p.name AS slug, qm.timeLimit AS timeLimit, qm.name AS name, p.id AS postId,
       p.content AS postContent, p.title AS postTitle
FROM wp_learndash_pro_quiz_master qm
INNER JOIN wp_postmeta pm ON qm.id = CAST(pm.meta_value AS integer)
INNER JOIN wp_posts p ON pm.post_id = p.ID
WHERE pm.meta_key = 'quiz_pro_id'
AND p.post_status IN ('publish', 'private')
AND p.post_type = 'sfwd-quiz'
ORDER BY qm.name ASC
LIMIT 20 OFFSET 0;
```

### Bước 3: Monitor performance

- Sử dụng `EXPLAIN` để kiểm tra query plan
- Monitor slow query log
- Kiểm tra index usage với `SHOW INDEX`

## Kết quả mong đợi

### Trước tối ưu

- Load toàn bộ quiz (có thể hàng nghìn records)
- N+1 queries cho mỗi quiz
- Phân trang trong bộ nhớ

### Sau tối ưu

- Chỉ load 20 records mỗi trang
- Batch queries cho dữ liệu liên quan
- Database-level pagination với index support

## Lưu ý quan trọng

### 1. Index Maintenance

- Indexes sẽ làm chậm INSERT/UPDATE
- Cần monitor disk space usage
- Cân nhắc drop index không dùng

### 2. Query Monitoring

```sql
-- Kiểm tra slow queries
SHOW VARIABLES LIKE 'slow_query_log';
SHOW VARIABLES LIKE 'long_query_time';

-- Kiểm tra index usage
SELECT * FROM information_schema.INDEX_STATISTICS
WHERE table_schema = 'wordpress'
AND table_name IN ('wp_posts', 'wp_postmeta', 'wp_learndash_pro_quiz_master');
```

### 3. Caching Strategy

- Cân nhắc cache cho danh sách quiz (Redis)
- Cache user activity data
- Cache quiz metadata

## Troubleshooting

### Nếu vẫn chậm

1. Kiểm tra query plan với `EXPLAIN`
2. Verify indexes được sử dụng
3. Kiểm tra data volume và selectivity
4. Cân nhắc cursor-based pagination cho offset lớn

### Performance Testing

```bash
# Load testing với Apache Bench
ab -n 1000 -c 10 "http://localhost:8080/api/quiz?page=0&size=20"

# JMeter test plan cho pagination
# Test với các page khác nhau (0, 10, 100, 1000)
```

## Kế hoạch mở rộng

### 1. Cursor-based Pagination

- Cho datasets rất lớn (>100k records)
- Sử dụng `created_at` + `id` làm cursor
- Tránh OFFSET lớn

### 2. Read Replicas

- Tách read/write operations
- Quiz listing → read replica
- Quiz submission → master

### 3. Caching Layer

- Redis cache cho quiz metadata
- Cache invalidation strategy
- CDN cho static content
