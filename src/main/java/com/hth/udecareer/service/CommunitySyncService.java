package com.hth.udecareer.service;

import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.CommunitySyncResponse;
import com.hth.udecareer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service để sync session giữa Java backend và WordPress community.
 *
 * Endpoint này được gọi từ Flutter app để:
 * 1. Lấy thông tin user từ JWT token
 * 2. Tạo/update user trong WordPress (nếu cần)
 * 3. Trả về session cookie để Flutter WebView có thể dùng
 *
 * Hiện tại chỉ trả về thông tin user.
 * Việc sync với WordPress REST API có thể được implement sau.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunitySyncService {

    private final UserRepository userRepository;

    /**
     * Sync user session với WordPress community.
     *
     * @param email Email của user từ JWT token
     * @return CommunitySyncResponse chứa thông tin sync
     */
    public CommunitySyncResponse syncSession(String email) {
        log.info("Syncing community session for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND));

        // User trong hệ thống này đã được sync với WordPress database
        // vì sử dụng chung database WordPress (wp_users table)
        // Nên chỉ cần trả về thông tin user

        log.info("Community session synced successfully for user: {} (ID: {})",
                email, user.getId());

        return CommunitySyncResponse.builder()
                .success(true)
                .message("Session synced successfully")
                .wordpressUserId(user.getId())
                .displayName(user.getDisplayName())
                .build();
    }
}
