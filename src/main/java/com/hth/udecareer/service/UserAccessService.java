package com.hth.udecareer.service;

import com.hth.udecareer.entities.User;
import com.hth.udecareer.entities.UserPurchasedEntity;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.repository.UserPurchasedRepository;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.enums.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAccessService {

    private final UserRepository userRepository;
    private final UserPurchasedRepository userPurchasedRepository;

    @Transactional(rollbackFor = Exception.class)
    public void grantAccessDirectly(Long userId, String categoryCode, Integer durationDays) {

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        String userEmail = user.getEmail();

        UserPurchasedEntity entity = userPurchasedRepository
                .findAllByUserIdOrUserEmail(userId, null)
                .stream()
                .filter(e -> categoryCode.equals(e.getCategoryCode()))
                .findFirst()
                .orElseGet(() -> {
                    UserPurchasedEntity newEntity = new UserPurchasedEntity();
                    newEntity.setUserId(userId);
                    newEntity.setUserEmail(userEmail); // Lưu email lấy từ User
                    newEntity.setCategoryCode(categoryCode);
                    return newEntity;
                });

        LocalDateTime newExpiryDate;

        if (entity.getIsPurchased() != null && entity.getIsPurchased() == 1
                && entity.getToTime() != null && entity.getToTime().isAfter(now)) {

            newExpiryDate = entity.getToTime().plusDays(durationDays);
            log.info("Extending subscription for User: {} ({}). Old: {}, New: {}",
                    userEmail, categoryCode, entity.getToTime(), newExpiryDate);

        } else {
            newExpiryDate = now.plusDays(durationDays);
            entity.setFromTime(now);
            log.info("New subscription for User: {} ({}). Expires: {}",
                    userEmail, categoryCode, newExpiryDate);
        }

        entity.setUserId(userId);
        entity.setUserEmail(userEmail);
        entity.setCategoryCode(categoryCode);
        entity.setIsPurchased(1);
        entity.setToTime(newExpiryDate);

        userPurchasedRepository.save(entity);
    }

    /**
     * Revoke access for a specific category (used for refunds).
     * Sets isPurchased = 0 and expires the subscription immediately.
     */
    @Transactional(rollbackFor = Exception.class)
    public void revokeAccess(Long userId, String categoryCode) {
        userPurchasedRepository.findAllByUserIdOrUserEmail(userId, null)
                .stream()
                .filter(e -> categoryCode.equals(e.getCategoryCode()))
                .findFirst()
                .ifPresent(entity -> {
                    entity.setIsPurchased(0);
                    entity.setToTime(LocalDateTime.now(ZoneId.systemDefault()));
                    userPurchasedRepository.save(entity);
                    log.info("Revoked access for User: {} Category: {}", userId, categoryCode);
                });
    }

}