package com.hth.udecareer.service;

import com.hth.udecareer.enums.OrderStatus;
import com.hth.udecareer.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled service to clean up stale orders.
 * Runs periodically to expire PENDING orders that are older than 24 hours.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCleanupScheduler {

    private static final int EXPIRE_AFTER_HOURS = 24;

    private final OrderRepository orderRepository;

    /**
     * Run every hour to expire old PENDING orders.
     * Cron: 0 0 * * * * = At minute 0 of every hour
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expireOldPendingOrders() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(EXPIRE_AFTER_HOURS);

        log.info("Running order cleanup job. Expiring PENDING orders created before: {}", cutoffTime);

        int expiredCount = orderRepository.expirePendingOrdersBefore(cutoffTime);

        if (expiredCount > 0) {
            log.info("Expired {} PENDING orders older than {} hours", expiredCount, EXPIRE_AFTER_HOURS);
        } else {
            log.debug("No PENDING orders to expire");
        }
    }
}
