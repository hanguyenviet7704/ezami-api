package com.hth.udecareer.service;

import com.hth.udecareer.entities.User;
import com.hth.udecareer.entities.UserMetaEntity;
import com.hth.udecareer.repository.UserMetaRepository;
import com.hth.udecareer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReferralScheduledService {

    private static final String KEY_REFERRED_BY = "referred_by";
    private static final String KEY_REFERRAL_ACTIVE_REWARDED = "referral_active_rewarded";

    private final UserMetaRepository userMetaRepository;
    private final UserRepository userRepository;
    private final UserPointsService userPointsService;

    /**
     * Scheduled job chạy mỗi ngày lúc 2h sáng để check và cộng điểm cho người giới thiệu
     * nếu user được giới thiệu đã hoạt động hơn 3 ngày
     * 
     * Cron: 0 0 2 * * ? = Mỗi ngày lúc 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void checkAndAwardActiveReferralPoints() {
        log.info("Starting scheduled job: Check and award active referral points");
        
        try {
            // Lấy tất cả users được giới thiệu
            List<Object[]> referredUsers = userMetaRepository.findAllByMetaKey(KEY_REFERRED_BY);
            
            if (referredUsers == null || referredUsers.isEmpty()) {
                log.info("No referred users found. Skipping.");
                return;
            }

            int processedCount = 0;
            int awardedCount = 0;

            for (Object[] row : referredUsers) {
                try {
                    Long referredUserId = ((Number) row[0]).longValue();
                    String referrerIdStr = (String) row[1];
                    
                    if (referrerIdStr == null || referrerIdStr.trim().isEmpty()) {
                        continue;
                    }

                    Long referrerId;
                    try {
                        referrerId = Long.parseLong(referrerIdStr.trim());
                    } catch (NumberFormatException e) {
                        log.warn("Invalid referrerId for user {}: {}", referredUserId, referrerIdStr);
                        continue;
                    }

                    // Kiểm tra đã cộng điểm chưa
                    UserMetaEntity rewardedMeta = userMetaRepository
                            .findByUserIdAndMetaKey(referrerId, KEY_REFERRAL_ACTIVE_REWARDED + "_" + referredUserId)
                            .orElse(null);

                    if (rewardedMeta != null && "true".equals(rewardedMeta.getMetaValue())) {
                        continue; // Đã cộng điểm rồi
                    }

                    // Lấy user được giới thiệu để check ngày đăng ký
                    User referredUser = userRepository.findById(referredUserId).orElse(null);
                    if (referredUser == null || referredUser.getRegisteredDate() == null) {
                        continue;
                    }

                    // Tính số ngày từ ngày đăng ký
                    LocalDate registrationDate = referredUser.getRegisteredDate().toLocalDate();
                    long daysSinceRegistration = ChronoUnit.DAYS.between(registrationDate, LocalDate.now());

                    if (daysSinceRegistration > 3) {
                        // Cộng điểm cho người giới thiệu
                        awardActiveReferralPoints(referrerId, referredUserId);
                        awardedCount++;
                    }

                    processedCount++;
                } catch (Exception e) {
                    log.error("Error processing referred user: {}", e.getMessage(), e);
                }
            }

            log.info("Scheduled job completed. Processed: {}, Awarded: {}", processedCount, awardedCount);
        } catch (Exception e) {
            log.error("Error in scheduled job checkAndAwardActiveReferralPoints: {}", e.getMessage(), e);
        }
    }

    /**
     * Cộng 50 điểm cho người giới thiệu khi user được giới thiệu hoạt động hơn 3 ngày
     */
    private void awardActiveReferralPoints(Long referrerId, Long referredUserId) {
        try {
            User referrer = userRepository.findById(referrerId).orElse(null);
            if (referrer == null) {
                log.warn("Referrer with id {} not found. Skipping active referral points.", referrerId);
                return;
            }

            // Cộng điểm cho người giới thiệu
            try {
                userPointsService.addPoints(
                        referrer.getEmail(),
                        50,
                        "POINT_REFERRAL_ACTIVE",
                        null,
                        referredUserId
                );

                // Đánh dấu đã cộng điểm
                UserMetaEntity rewardedMeta = new UserMetaEntity();
                rewardedMeta.setUserId(referrerId);
                rewardedMeta.setMetaKey(KEY_REFERRAL_ACTIVE_REWARDED + "_" + referredUserId);
                rewardedMeta.setMetaValue("true");
                userMetaRepository.save(rewardedMeta);

                log.info("Awarded 50 points to referrer {} (user: {}) for referred user {} being active for more than 3 days",
                        referrerId, referrer.getEmail(), referredUserId);
            } catch (Exception e) {
                log.info("Active referral points already awarded or failed for referrer {} and user {}. Error: {}",
                        referrerId, referredUserId, e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error awarding active referral points for referrer {} and user {}: {}",
                    referrerId, referredUserId, e.getMessage(), e);
        }
    }
}

