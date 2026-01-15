package com.hth.udecareer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled job để tự động sync orders từ Firebase về Backend.
 * Chạy định kỳ để đảm bảo không bỏ sót order nào.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseOrderSyncScheduler {

    private final FirebaseOrderSyncService firebaseOrderSyncService;

    /**
     * Sync orders từ Firebase mỗi 5 phút.
     * Cron: 0 *\/5 * * * * = Mỗi 5 phút
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void syncFirebaseOrders() {
        if (!firebaseOrderSyncService.isFirebaseConfigured()) {
            log.debug("Firebase not configured. Skipping scheduled sync.");
            return;
        }

        log.info("Starting scheduled Firebase order sync...");

        try {
            int syncedCount = firebaseOrderSyncService.syncPendingOrders();

            if (syncedCount > 0) {
                log.info("Scheduled sync completed. Synced {} orders.", syncedCount);
            } else {
                log.debug("Scheduled sync completed. No new orders to sync.");
            }
        } catch (Exception e) {
            log.error("Error during scheduled Firebase order sync: {}", e.getMessage(), e);
        }
    }
}
