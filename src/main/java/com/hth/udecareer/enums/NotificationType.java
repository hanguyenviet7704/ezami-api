package com.hth.udecareer.enums;

public enum NotificationType {

    // --- 1. KHÓA HỌC / HỌC TẬP ---
    COURSE_NEW_CONTENT,         // Cập nhật nội dung mới của khóa học
    COURSE_ACCESS_EXPIRY,       // Hết hạn truy cập khóa học

    // --- 2. THI CỬ / MOCK TEST ---
    QUIZ_REGISTER_SUCCESS, // Đăng ký Mock test thành công
    QUIZ_RESULT,                // Có điểm / Kết quả / Nhận xét
    QUIZ_REMINDER,              // Nhắc nhở làm bài quiz

    // --- 3. THANH TOÁN / ĐƠN HÀNG ---
    PAYMENT_SUCCESS,            // Thanh toán thành công
    PAYMENT_FAILED,             // Thanh toán thất bại
    ORDER_REFUNDED,             // Hoàn tiền
    ORDER_CANCELED,             // Hủy đơn
    PAYMENT_PENDING_REMINDER,   // Nhắc thanh toán còn đang dở (Abandoned cart)
    INVOICE_AVAILABLE,          // Hóa đơn / Biên nhận

    // --- 4. KHUYẾN MÃI / ƯU ĐÃI ---
    PROMOTION_NEW_VOUCHER,      // Voucher mới
    PROMOTION_FLASH_SALE,       // Flash sale, giảm giá
    PROMOTION_PERSONALIZED,     // Ưu đãi cá nhân hóa

    // --- 5. HỆ THỐNG / TÀI KHOẢN ---
    SYSTEM_INFO,                // Thông tin hệ thống chung
    SYSTEM_POLICY_UPDATE,       // Cập nhật điều khoản / chính sách
    SYSTEM_MAINTENANCE,         // Bảo trì hệ thống, thông báo sự cố
    ACCOUNT_SECURITY_ALERT,     // Đổi mật khẩu, cảnh báo bảo mật chung
    ACCOUNT_LOCKED,             // Cảnh báo vi phạm / Khóa tài khoản

    // --- 6. AFFILIATE / CỘNG TÁC VIÊN ---
    AFFILIATE_APPLICATION_RESULT, // Hồ sơ affiliate được duyệt / bị từ chối
    AFFILIATE_COMMISSION,         // Thông báo hoa hồng
    AFFILIATE_WITHDRAWAL_STATUS,  // Rút tiền thành công / thất bại

    // --- 7. HỖ TRỢ / CSKH ---
    SUPPORT_TICKET_REPLY,       // Trả lời ticket hỗ trợ
    SUPPORT_CHANNEL_SUGGESTION, // Gợi ý kênh hỗ trợ phù hợp
    MENTOR_FEEDBACK,            // Thông báo phản hồi từ admin/mentor

    // --- 8. TƯƠNG TÁC CƠ BẢN (COMMENT / REACT) ---
    POST_NEW_COMMENT,           // Có người comment vào bài viết của bạn
    COMMENT_REPLY,              // Có người trả lời (reply) comment của bạn
    POST_NEW_REACTION,          // Có người thả biểu cảm (react) vào bài viết
    COMMENT_NEW_REACTION,       // Có người thả biểu cảm (react) vào comment

    // --- 9. RETENTION & RECOMMENDATION (GIỮ CHÂN & GỢI Ý) ---
    // 9.1 Nhắc quay lại học
    RETENTION_INACTIVE_REMINDER,    // Nhắc sau 24h, 3 ngày... không vào app
    RETENTION_UNFINISHED_COURSE,    // Nhắc hoàn thành khóa học đang dở
    RETENTION_NEW_CHAPTER_OPEN,     // Nhắc tiếp tục chương mới
    // 9.2 Gợi ý nội dung (Personalized)
    RECOMMEND_ROLE_BASED,           // Gợi ý bài học theo role (QA/BA/PO/PM)
    RECOMMEND_WEAKNESS_FOCUS,       // Gợi ý mini test cải thiện điểm yếu
    RECOMMEND_MOCK_TEST_READY,      // Gợi ý làm mock test khi đủ kiến thức
    RECOMMEND_RELATED_ARTICLE,      // Gợi ý bài viết liên quan bài vừa học

    // --- 10. MẠNG XÃ HỘI / VIRAL ---
    SOCIAL_INVITE_FRIEND,           // Thông báo mời bạn bè cùng học
    SOCIAL_INVITE_REWARD,           // Mời bạn bè nhận voucher
    SOCIAL_ADMIN_POST,              // Có bài viết mới từ Admin
    SOCIAL_COMMUNITY_HIGHLIGHT,     // Bài viết nổi bật trong cộng đồng
    SOCIAL_FOLLOWING_NEW_POST,      // Người đang follow có bài mới
    SOCIAL_TRENDING_CONTENT,        // Nội dung đang trending
    SOCIAL_WATCHED_POST_COMMENT,    // Có bình luận mới trong bài đang theo dõi

    // --- 11. CỘNG ĐỒNG (COMMUNITY SPECIFIC) ---
    COMMUNITY_QUESTION_ANSWERED,    // Có người trả lời câu hỏi của bạn
    COMMUNITY_MENTION,              // Có người nhắc đến bạn (@mention)
    COMMUNITY_POST_REACTION,        // Có người upvote / like bài viết (Community context)
    COMMUNITY_TOPIC_MESSAGE,        // Có người gửi tin nhắn trong topic
    COMMUNITY_ROLE_ASSIGNED,        // Được thêm vào group role (QA/BA/PO/PM)

    // --- 12. BẢO MẬT & CẬP NHẬT (SECURITY & UPDATE) ---
    SECURITY_NEW_DEVICE_LOGIN,      // Cảnh báo đăng nhập thiết bị mới
    SECURITY_SUSPICIOUS_ACTIVITY,   // Phát hiện hành vi bất thường
    SYSTEM_APP_UPDATE_AVAILABLE     // Thông báo có bản cập nhật App mới
}