# Affiliate System - API Endpoints by Role

## Tổng quan
Tài liệu này phân loại các API endpoints theo 2 nhóm chính:
- **Client APIs**: Dành cho Affiliate (người dùng đăng ký làm affiliate)
- **Admin APIs**: Dành cho Admin (quản trị viên hệ thống)

---

## 🔵 CLIENT APIs (Affiliate APIs)

### 1. Authentication & Profile

#### 1.1. Đăng ký Affiliate
- **Endpoint**: `POST /api/v1/affiliates/register`
- **Auth**: Không cần (public)
- **Request**:
```json
{
  "user_id": 123,
  "first_name": "John",
  "last_name": "Doe",
  "email": "affiliate@example.com",
  "website": "https://example.com",
  "promotion_method": "Social Media",
  "terms_accepted": true
}
```
- **Response**:
```json
{
  "success": true,
  "data": {
    "affiliate_id": 456,
    "affiliate_code": "AFF123",
    "status": "pending",
    "message": "Registration submitted successfully"
  }
}
```

#### 1.2. Lấy thông tin Affiliate của mình
- **Endpoint**: `GET /api/v1/affiliates/me`
- **Auth**: Required (JWT/Nonce)
- **Response**:
```json
{
  "success": true,
  "data": {
    "affiliate_id": 456,
    "affiliate_code": "AFF123",
    "status": "active",
    "first_name": "John",
    "last_name": "Doe",
    "email": "affiliate@example.com",
    "website": "https://example.com",
    "total_commissions": 1500.00,
    "paid_commissions": 1000.00,
    "unpaid_commissions": 500.00,
    "total_referrals": 25,
    "total_visits": 150,
    "total_conversions": 20,
    "conversion_rate": 13.33
  }
}
```

#### 1.3. Cập nhật thông tin Affiliate
- **Endpoint**: `PUT /api/v1/affiliates/me`
- **Auth**: Required
- **Request**:
```json
{
  "first_name": "John",
  "last_name": "Doe Updated",
  "website": "https://newwebsite.com",
  "phone": "+1234567890"
}
```

#### 1.4. Cập nhật Payment Method
- **Endpoint**: `PUT /api/v1/affiliates/me/payment`
- **Auth**: Required
- **Request**:
```json
{
  "payment_method": "paypal",
  "paypal_email": "paypal@example.com"
}
```
hoặc
```json
{
  "payment_method": "bank_transfer",
  "bank_name": "Bank Name",
  "bank_account_number": "123456789",
  "bank_account_name": "John Doe",
  "bank_swift_code": "SWIFT123"
}
```

---

### 2. Dashboard & Statistics

#### 2.1. Lấy Dashboard Summary
- **Endpoint**: `GET /api/v1/affiliates/me/dashboard`
- **Auth**: Required
- **Response**:
```json
{
  "success": true,
  "data": {
    "total_commissions": 1500.00,
    "paid_commissions": 1000.00,
    "unpaid_commissions": 500.00,
    "pending_commissions": 200.00,
    "total_referrals": 25,
    "total_visits": 150,
    "total_conversions": 20,
    "conversion_rate": 13.33,
    "this_month_commissions": 300.00,
    "last_month_commissions": 250.00,
    "this_month_referrals": 5,
    "last_month_referrals": 4
  }
}
```

#### 2.2. Lấy Statistics theo thời gian
- **Endpoint**: `GET /api/v1/affiliates/me/statistics`
- **Auth**: Required
- **Query Parameters**:
  - `from_date`: YYYY-MM-DD
  - `to_date`: YYYY-MM-DD
  - `group_by`: day, week, month (default: day)
- **Response**:
```json
{
  "success": true,
  "data": {
    "period": {
      "from": "2024-01-01",
      "to": "2024-01-31"
    },
    "summary": {
      "total_commissions": 500.00,
      "total_referrals": 10,
      "total_visits": 50,
      "total_conversions": 8
    },
    "chart_data": [
      {
        "date": "2024-01-01",
        "commissions": 50.00,
        "referrals": 1,
        "visits": 5,
        "conversions": 1
      }
    ]
  }
}
```

---

### 3. Referrals & Commissions

#### 3.1. Lấy danh sách Referrals của mình
- **Endpoint**: `GET /api/v1/affiliates/me/referrals`
- **Auth**: Required
- **Query Parameters**:
  - `page`: Số trang (default: 1)
  - `per_page`: Items mỗi trang (default: 20)
  - `status`: pending, approved, rejected, paid, cancelled
  - `from_date`: YYYY-MM-DD
  - `to_date`: YYYY-MM-DD
- **Response**:
```json
{
  "success": true,
  "data": {
    "referrals": [
      {
        "referral_id": 1,
        "order_id": 789,
        "customer_name": "Customer Name",
        "amount": 100.00,
        "commission_amount": 10.00,
        "status": "approved",
        "order_date": "2024-01-15 10:00:00",
        "created_at": "2024-01-15 10:00:00"
      }
    ],
    "pagination": {
      "current_page": 1,
      "per_page": 20,
      "total": 25,
      "total_pages": 2
    }
  }
}
```

#### 3.2. Lấy chi tiết Referral
- **Endpoint**: `GET /api/v1/affiliates/me/referrals/{referral_id}`
- **Auth**: Required
- **Response**:
```json
{
  "success": true,
  "data": {
    "referral_id": 1,
    "order_id": 789,
    "customer_name": "Customer Name",
    "customer_email": "customer@example.com",
    "amount": 100.00,
    "commission_type": "percentage",
    "commission_rate": 10.00,
    "commission_amount": 10.00,
    "status": "approved",
    "order_status": "completed",
    "order_date": "2024-01-15 10:00:00",
    "product_names": ["Product 1", "Product 2"],
    "created_at": "2024-01-15 10:00:00"
  }
}
```

#### 3.3. Lấy Commission Summary
- **Endpoint**: `GET /api/v1/affiliates/me/commissions/summary`
- **Auth**: Required
- **Response**:
```json
{
  "success": true,
  "data": {
    "total_commissions": 1500.00,
    "paid_commissions": 1000.00,
    "unpaid_commissions": 500.00,
    "pending_commissions": 200.00,
    "rejected_commissions": 50.00,
    "this_month": 300.00,
    "last_month": 250.00
  }
}
```

#### 3.4. Lấy Commission History
- **Endpoint**: `GET /api/v1/affiliates/me/commissions`
- **Auth**: Required
- **Query Parameters**: `status`, `from_date`, `to_date`, `page`, `per_page`

---

### 4. Affiliate Links

#### 4.1. Generate Affiliate Link
- **Endpoint**: `POST /api/v1/affiliates/me/links/generate`
- **Auth**: Required
- **Request**:
```json
{
  "url": "https://example.com/product/123",
  "campaign": "summer_sale",
  "medium": "email"
}
```
- **Response**:
```json
{
  "success": true,
  "data": {
    "link_id": 1,
    "original_url": "https://example.com/product/123",
    "affiliate_url": "https://example.com/product/123?ref=AFF123&campaign=summer_sale",
    "short_url": "https://short.ly/abc123",
    "campaign": "summer_sale"
  }
}
```

#### 4.2. Lấy danh sách Links
- **Endpoint**: `GET /api/v1/affiliates/me/links`
- **Auth**: Required
- **Query Parameters**: `page`, `per_page`, `campaign`, `is_active`
- **Response**:
```json
{
  "success": true,
  "data": {
    "links": [
      {
        "link_id": 1,
        "original_url": "https://example.com/product/123",
        "affiliate_url": "https://example.com/product/123?ref=AFF123",
        "short_url": "https://short.ly/abc123",
        "campaign": "summer_sale",
        "total_clicks": 150,
        "unique_clicks": 120,
        "total_conversions": 25,
        "total_commission": 250.00,
        "is_active": true,
        "created_at": "2024-01-01 10:00:00"
      }
    ],
    "pagination": {...}
  }
}
```

#### 4.3. Lấy Link Statistics
- **Endpoint**: `GET /api/v1/affiliates/me/links/{link_id}/stats`
- **Auth**: Required
- **Query Parameters**: `from_date`, `to_date`
- **Response**:
```json
{
  "success": true,
  "data": {
    "link_id": 1,
    "total_clicks": 150,
    "unique_clicks": 120,
    "total_conversions": 25,
    "conversion_rate": 16.67,
    "total_commission": 250.00,
    "clicks_by_date": [
      {
        "date": "2024-01-01",
        "clicks": 10,
        "conversions": 2
      }
    ]
  }
}
```

#### 4.4. Cập nhật Link
- **Endpoint**: `PUT /api/v1/affiliates/me/links/{link_id}`
- **Auth**: Required
- **Request**:
```json
{
  "campaign": "winter_sale",
  "is_active": false
}
```

#### 4.5. Xóa Link
- **Endpoint**: `DELETE /api/v1/affiliates/me/links/{link_id}`
- **Auth**: Required

---

### 5. Visits Tracking

#### 5.1. Lấy danh sách Visits
- **Endpoint**: `GET /api/v1/affiliates/me/visits`
- **Auth**: Required
- **Query Parameters**: `page`, `per_page`, `link_id`, `is_converted`, `from_date`, `to_date`
- **Response**:
```json
{
  "success": true,
  "data": {
    "visits": [
      {
        "visit_id": 1,
        "link_id": 1,
        "ip_address": "192.168.1.1",
        "country": "US",
        "city": "New York",
        "device_type": "mobile",
        "is_converted": true,
        "converted_at": "2024-01-15 10:00:00",
        "created_at": "2024-01-15 09:00:00"
      }
    ],
    "pagination": {...}
  }
}
```

#### 5.2. Lấy Visit Statistics
- **Endpoint**: `GET /api/v1/affiliates/me/visits/statistics`
- **Auth**: Required
- **Query Parameters**: `from_date`, `to_date`, `link_id`
- **Response**:
```json
{
  "success": true,
  "data": {
    "total_visits": 150,
    "unique_visits": 120,
    "total_conversions": 25,
    "conversion_rate": 16.67,
    "visits_by_country": [
      {
        "country": "US",
        "visits": 100,
        "conversions": 15
      }
    ],
    "visits_by_device": [
      {
        "device_type": "mobile",
        "visits": 80,
        "conversions": 12
      }
    ]
  }
}
```

---

### 6. Payouts

#### 6.1. Lấy danh sách Payouts của mình
- **Endpoint**: `GET /api/v1/affiliates/me/payouts`
- **Auth**: Required
- **Query Parameters**: `page`, `per_page`, `status`
- **Response**:
```json
{
  "success": true,
  "data": {
    "payouts": [
      {
        "payout_id": 1,
        "amount": 500.00,
        "currency": "USD",
        "payment_method": "paypal",
        "status": "completed",
        "transaction_id": "TXN123456",
        "net_amount": 495.00,
        "transaction_fee": 5.00,
        "referral_count": 10,
        "requested_at": "2024-01-01 10:00:00",
        "completed_at": "2024-01-02 15:00:00"
      }
    ],
    "pagination": {...}
  }
}
```

#### 6.2. Tạo Payout Request
- **Endpoint**: `POST /api/v1/affiliates/me/payouts/request`
- **Auth**: Required
- **Request**:
```json
{
  "amount": 500.00,
  "payment_method": "paypal"
}
```
- **Response**:
```json
{
  "success": true,
  "data": {
    "payout_id": 1,
    "amount": 500.00,
    "status": "pending",
    "message": "Payout request submitted successfully"
  }
}
```

#### 6.3. Lấy chi tiết Payout
- **Endpoint**: `GET /api/v1/affiliates/me/payouts/{payout_id}`
- **Auth**: Required
- **Response**:
```json
{
  "success": true,
  "data": {
    "payout_id": 1,
    "amount": 500.00,
    "currency": "USD",
    "payment_method": "paypal",
    "status": "completed",
    "transaction_id": "TXN123456",
    "net_amount": 495.00,
    "transaction_fee": 5.00,
    "referral_count": 10,
    "referrals": [
      {
        "referral_id": 1,
        "order_id": 789,
        "commission_amount": 50.00
      }
    ],
    "requested_at": "2024-01-01 10:00:00",
    "completed_at": "2024-01-02 15:00:00"
  }
}
```

---

### 7. Creatives

#### 7.1. Lấy danh sách Creatives
- **Endpoint**: `GET /api/v1/affiliates/me/creatives`
- **Auth**: Required
- **Query Parameters**: `page`, `per_page`, `creative_type`, `is_active`
- **Response**:
```json
{
  "success": true,
  "data": {
    "creatives": [
      {
        "creative_id": 1,
        "creative_name": "Banner 728x90",
        "creative_type": "banner",
        "file_url": "https://example.com/banner.jpg",
        "width": 728,
        "height": 90,
        "html_code": "<a href='...'><img src='...'></a>",
        "is_active": true,
        "usage_count": 50,
        "created_at": "2024-01-01 10:00:00"
      }
    ],
    "pagination": {...}
  }
}
```

#### 7.2. Upload Creative
- **Endpoint**: `POST /api/v1/affiliates/me/creatives`
- **Auth**: Required
- **Request**: Multipart form data
  - `creative_name`: string
  - `creative_type`: banner, text, email, social, video
  - `file`: file upload
  - `html_code`: string (optional)

---

### 8. Notifications

#### 8.1. Lấy danh sách Notifications
- **Endpoint**: `GET /api/v1/affiliates/me/notifications`
- **Auth**: Required
- **Query Parameters**: `page`, `per_page`, `is_read`, `type`
- **Response**:
```json
{
  "success": true,
  "data": {
    "notifications": [
      {
        "notification_id": 1,
        "type": "commission_approved",
        "title": "Commission Approved",
        "message": "Your commission of $50.00 has been approved",
        "is_read": false,
        "created_at": "2024-01-15 10:00:00"
      }
    ],
    "pagination": {...},
    "unread_count": 5
  }
}
```

#### 8.2. Đánh dấu Notification đã đọc
- **Endpoint**: `PUT /api/v1/affiliates/me/notifications/{notification_id}/read`
- **Auth**: Required

#### 8.3. Đánh dấu tất cả đã đọc
- **Endpoint**: `PUT /api/v1/affiliates/me/notifications/read-all`
- **Auth**: Required

---

## 🔴 ADMIN APIs

### 1. Affiliate Management

#### 1.1. Lấy danh sách Affiliates
- **Endpoint**: `GET /api/v1/admin/affiliates`
- **Auth**: Required (Admin only)
- **Query Parameters**:
  - `page`: Số trang (default: 1)
  - `per_page`: Items mỗi trang (default: 20)
  - `status`: active, pending, rejected, inactive, suspended
  - `search`: Tìm kiếm theo tên, email, code
  - `sort_by`: total_commissions, total_referrals, registered_at
  - `order`: ASC, DESC
  - `from_date`: YYYY-MM-DD
  - `to_date`: YYYY-MM-DD
- **Response**:
```json
{
  "success": true,
  "data": {
    "affiliates": [
      {
        "affiliate_id": 456,
        "user_id": 123,
        "affiliate_code": "AFF123",
        "status": "active",
        "first_name": "John",
        "last_name": "Doe",
        "email": "affiliate@example.com",
        "total_commissions": 1500.00,
        "paid_commissions": 1000.00,
        "unpaid_commissions": 500.00,
        "total_referrals": 25,
        "total_visits": 150,
        "registered_at": "2024-01-01 10:00:00",
        "last_activity_at": "2024-01-15 10:00:00"
      }
    ],
    "pagination": {
      "current_page": 1,
      "per_page": 20,
      "total": 100,
      "total_pages": 5
    }
  }
}
```

#### 1.2. Lấy chi tiết Affiliate
- **Endpoint**: `GET /api/v1/admin/affiliates/{affiliate_id}`
- **Auth**: Required (Admin only)
- **Response**: Chi tiết đầy đủ affiliate + statistics

#### 1.3. Approve Affiliate
- **Endpoint**: `POST /api/v1/admin/affiliates/{affiliate_id}/approve`
- **Auth**: Required (Admin only)
- **Request**:
```json
{
  "reason": "Approved based on criteria"
}
```

#### 1.4. Reject Affiliate
- **Endpoint**: `POST /api/v1/admin/affiliates/{affiliate_id}/reject`
- **Auth**: Required (Admin only)
- **Request**:
```json
{
  "reason": "Does not meet requirements"
}
```

#### 1.5. Suspend/Activate Affiliate
- **Endpoint**: `PUT /api/v1/admin/affiliates/{affiliate_id}/status`
- **Auth**: Required (Admin only)
- **Request**:
```json
{
  "status": "suspended",
  "reason": "Violation of terms"
}
```

#### 1.6. Cập nhật Affiliate
- **Endpoint**: `PUT /api/v1/admin/affiliates/{affiliate_id}`
- **Auth**: Required (Admin only)
- **Request**: Tất cả các trường có thể cập nhật

#### 1.7. Xóa Affiliate
- **Endpoint**: `DELETE /api/v1/admin/affiliates/{affiliate_id}`
- **Auth**: Required (Admin only)
- **Request**:
```json
{
  "action": "delete" // hoặc "deactivate"
}
```

---

### 2. Referrals Management

#### 2.1. Lấy danh sách Referrals
- **Endpoint**: `GET /api/v1/admin/referrals`
- **Auth**: Required (Admin only)
- **Query Parameters**:
  - `affiliate_id`: Filter theo affiliate
  - `order_id`: Filter theo order
  - `status`: pending, approved, rejected, paid, cancelled
  - `from_date`: YYYY-MM-DD
  - `to_date`: YYYY-MM-DD
  - `page`, `per_page`: Pagination
- **Response**: Danh sách referrals với đầy đủ thông tin

#### 2.2. Lấy chi tiết Referral
- **Endpoint**: `GET /api/v1/admin/referrals/{referral_id}`
- **Auth**: Required (Admin only)

#### 2.3. Approve Referral
- **Endpoint**: `POST /api/v1/admin/referrals/{referral_id}/approve`
- **Auth**: Required (Admin only)
- **Request**:
```json
{
  "reason": "Order completed successfully"
}
```

#### 2.4. Reject Referral
- **Endpoint**: `POST /api/v1/admin/referrals/{referral_id}/reject`
- **Auth**: Required (Admin only)
- **Request**:
```json
{
  "reason": "Order cancelled"
}
```

#### 2.5. Cập nhật Referral
- **Endpoint**: `PUT /api/v1/admin/referrals/{referral_id}`
- **Auth**: Required (Admin only)
- **Request**:
```json
{
  "commission_amount": 15.00,
  "status": "approved"
}
```

#### 2.6. Bulk Update Referrals
- **Endpoint**: `PUT /api/v1/admin/referrals/bulk-update`
- **Auth**: Required (Admin only)
- **Request**:
```json
{
  "referral_ids": [1, 2, 3],
  "status": "approved",
  "reason": "Bulk approval"
}
```

---

### 3. Commission Rules Management

#### 3.1. Lấy danh sách Commission Rules
- **Endpoint**: `GET /api/v1/admin/commission-rules`
- **Auth**: Required (Admin only)
- **Query Parameters**: `rule_type`, `is_active`, `page`, `per_page`
- **Response**:
```json
{
  "success": true,
  "data": {
    "rules": [
      {
        "rule_id": 1,
        "rule_name": "Default Global Commission",
        "rule_type": "global",
        "commission_type": "percentage",
        "commission_rate": 10.00,
        "priority": 0,
        "is_active": true,
        "created_at": "2024-01-01 10:00:00"
      }
    ],
    "pagination": {...}
  }
}
```

#### 3.2. Tạo Commission Rule
- **Endpoint**: `POST /api/v1/admin/commission-rules`
- **Auth**: Required (Admin only)
- **Request**:
```json
{
  "rule_name": "Product Category Commission",
  "rule_type": "category",
  "category_id": 5,
  "commission_type": "percentage",
  "commission_rate": 15.00,
  "priority": 10,
  "is_active": true,
  "valid_from": "2024-01-01",
  "valid_until": "2024-12-31"
}
```

#### 3.3. Cập nhật Commission Rule
- **Endpoint**: `PUT /api/v1/admin/commission-rules/{rule_id}`
- **Auth**: Required (Admin only)

#### 3.4. Xóa Commission Rule
- **Endpoint**: `DELETE /api/v1/admin/commission-rules/{rule_id}`
- **Auth**: Required (Admin only)

---

### 4. Payouts Management

#### 4.1. Lấy danh sách Payouts
- **Endpoint**: `GET /api/v1/admin/payouts`
- **Auth**: Required (Admin only)
- **Query Parameters**: `status`, `affiliate_id`, `from_date`, `to_date`, `page`, `per_page`
- **Response**:
```json
{
  "success": true,
  "data": {
    "payouts": [
      {
        "payout_id": 1,
        "affiliate_id": 456,
        "affiliate_code": "AFF123",
        "affiliate_name": "John Doe",
        "amount": 500.00,
        "currency": "USD",
        "payment_method": "paypal",
        "status": "pending",
        "referral_count": 10,
        "requested_at": "2024-01-01 10:00:00"
      }
    ],
    "pagination": {...}
  }
}
```

#### 4.2. Lấy chi tiết Payout
- **Endpoint**: `GET /api/v1/admin/payouts/{payout_id}`
- **Auth**: Required (Admin only)

#### 4.3. Process Payout
- **Endpoint**: `POST /api/v1/admin/payouts/{payout_id}/process`
- **Auth**: Required (Admin only)
- **Request**:
```json
{
  "transaction_id": "TXN123456",
  "transaction_fee": 5.00,
  "notes": "Processed via PayPal"
}
```

#### 4.4. Complete Payout
- **Endpoint**: `POST /api/v1/admin/payouts/{payout_id}/complete`
- **Auth**: Required (Admin only)
- **Request**:
```json
{
  "transaction_id": "TXN123456",
  "notes": "Payment completed"
}
```

#### 4.5. Reject/Cancel Payout
- **Endpoint**: `POST /api/v1/admin/payouts/{payout_id}/reject`
- **Auth**: Required (Admin only)
- **Request**:
```json
{
  "reason": "Insufficient funds"
}
```

#### 4.6. Bulk Process Payouts
- **Endpoint**: `POST /api/v1/admin/payouts/bulk-process`
- **Auth**: Required (Admin only)
- **Request**:
```json
{
  "payout_ids": [1, 2, 3],
  "payment_method": "paypal"
}
```

---

### 5. Visits Management

#### 5.1. Lấy danh sách Visits
- **Endpoint**: `GET /api/v1/admin/visits`
- **Auth**: Required (Admin only)
- **Query Parameters**: `affiliate_id`, `link_id`, `is_converted`, `from_date`, `to_date`, `page`, `per_page`

#### 5.2. Lấy Visit Statistics
- **Endpoint**: `GET /api/v1/admin/visits/statistics`
- **Auth**: Required (Admin only)
- **Query Parameters**: `from_date`, `to_date`, `affiliate_id`, `link_id`
- **Response**:
```json
{
  "success": true,
  "data": {
    "total_visits": 10000,
    "unique_visits": 8000,
    "total_conversions": 500,
    "conversion_rate": 5.00,
    "visits_by_affiliate": [...],
    "visits_by_country": [...],
    "visits_by_device": [...],
    "visits_by_date": [...]
  }
}
```

---

### 6. Links Management

#### 6.1. Lấy danh sách Links
- **Endpoint**: `GET /api/v1/admin/links`
- **Auth**: Required (Admin only)
- **Query Parameters**: `affiliate_id`, `campaign`, `is_active`, `page`, `per_page`

#### 6.2. Lấy Link Statistics
- **Endpoint**: `GET /api/v1/admin/links/{link_id}/stats`
- **Auth**: Required (Admin only)

---

### 7. Dashboard & Reports

#### 7.1. Admin Dashboard Summary
- **Endpoint**: `GET /api/v1/admin/dashboard`
- **Auth**: Required (Admin only)
- **Response**:
```json
{
  "success": true,
  "data": {
    "total_affiliates": 100,
    "active_affiliates": 80,
    "pending_affiliates": 10,
    "total_commissions": 50000.00,
    "paid_commissions": 40000.00,
    "unpaid_commissions": 10000.00,
    "total_referrals": 1000,
    "total_visits": 50000,
    "total_conversions": 500,
    "conversion_rate": 1.00,
    "pending_payouts": 5,
    "pending_payouts_amount": 2500.00,
    "this_month_commissions": 5000.00,
    "last_month_commissions": 4500.00,
    "top_affiliates": [
      {
        "affiliate_id": 456,
        "affiliate_code": "AFF123",
        "name": "John Doe",
        "total_commissions": 1500.00
      }
    ]
  }
}
```

#### 7.2. Generate Report
- **Endpoint**: `POST /api/v1/admin/reports/generate`
- **Auth**: Required (Admin only)
- **Request**:
```json
{
  "report_type": "commissions", // commissions, referrals, payouts, affiliates
  "from_date": "2024-01-01",
  "to_date": "2024-01-31",
  "format": "csv" // csv, pdf, excel
}
```

#### 7.3. Export Data
- **Endpoint**: `POST /api/v1/admin/export`
- **Auth**: Required (Admin only)
- **Request**:
```json
{
  "export_type": "affiliates", // affiliates, referrals, payouts, visits
  "filters": {
    "status": "active",
    "from_date": "2024-01-01",
    "to_date": "2024-01-31"
  },
  "format": "csv"
}
```

---

### 8. Settings Management

#### 8.1. Lấy Settings
- **Endpoint**: `GET /api/v1/admin/settings`
- **Auth**: Required (Admin only)
- **Query Parameters**: `group_name`
- **Response**:
```json
{
  "success": true,
  "data": {
    "tracking": {
      "cookie_duration": 30
    },
    "payout": {
      "minimum_payout": 50.00
    },
    "commission": {
      "default_commission_rate": 10.00,
      "default_commission_type": "percentage"
    },
    "registration": {
      "require_approval": true,
      "auto_approve": false
    }
  }
}
```

#### 8.2. Cập nhật Settings
- **Endpoint**: `PUT /api/v1/admin/settings`
- **Auth**: Required (Admin only)
- **Request**:
```json
{
  "cookie_duration": 60,
  "minimum_payout": 100.00,
  "default_commission_rate": 12.00
}
```

---

### 9. Coupons Management

#### 9.1. Lấy danh sách Coupons
- **Endpoint**: `GET /api/v1/admin/coupons`
- **Auth**: Required (Admin only)
- **Query Parameters**: `affiliate_id`, `is_active`, `page`, `per_page`

#### 9.2. Link Coupon với Affiliate
- **Endpoint**: `POST /api/v1/admin/coupons/link`
- **Auth**: Required (Admin only)
- **Request**:
```json
{
  "affiliate_id": 456,
  "woocommerce_coupon_id": 789,
  "coupon_code": "SUMMER2024"
}
```

---

### 10. Creatives Management

#### 10.1. Lấy danh sách Creatives
- **Endpoint**: `GET /api/v1/admin/creatives`
- **Auth**: Required (Admin only)
- **Query Parameters**: `affiliate_id`, `creative_type`, `is_active`, `page`, `per_page`

---

## 🔐 Authentication

### JWT Token Authentication
- **Endpoint**: `POST /api/v1/auth/login`
- **Request**:
```json
{
  "username": "affiliate_user",
  "password": "password123"
}
```
- **Response**:
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expires_in": 3600,
    "user": {
      "user_id": 123,
      "affiliate_id": 456,
      "role": "affiliate"
    }
  }
}
```

### WordPress Nonce Authentication
- Sử dụng WordPress nonce cho AJAX requests
- Nonce được generate và pass qua headers hoặc request body

---

## 📊 Response Format Standard

Tất cả API responses đều follow format:

```json
{
  "success": true|false,
  "data": {...},
  "message": "Success message",
  "errors": [...],
  "pagination": {
    "current_page": 1,
    "per_page": 20,
    "total": 100,
    "total_pages": 5
  }
}
```

---

## 🔒 Authorization Rules

### Client APIs
- Chỉ có thể truy cập dữ liệu của chính mình
- Không thể xem/update dữ liệu của affiliate khác
- Một số endpoints public (register, track visit)

### Admin APIs
- Yêu cầu admin role hoặc capability `manage_affiliates`
- Có thể xem/update tất cả dữ liệu
- Có thể approve/reject affiliates và referrals
- Có thể process payouts

---

## 📝 Notes

1. **Pagination**: Tất cả list endpoints đều support pagination
2. **Filtering**: Hầu hết endpoints support filtering theo date range, status, etc.
3. **Sorting**: List endpoints support sorting với `sort_by` và `order`
4. **Rate Limiting**: Nên implement rate limiting cho public endpoints
5. **Caching**: Có thể cache statistics và dashboard data
6. **Webhooks**: Có thể thêm webhooks cho các events quan trọng (commission approved, payout completed, etc.)

