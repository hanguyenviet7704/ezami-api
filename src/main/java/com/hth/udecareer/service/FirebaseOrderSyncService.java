package com.hth.udecareer.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import com.hth.udecareer.entities.OrderEntity;
import com.hth.udecareer.entities.OrderItemEntity;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.OrderStatus;
import com.hth.udecareer.model.dto.FirebaseOrderDto;
import com.hth.udecareer.repository.OrderRepository;
import com.hth.udecareer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Service để đồng bộ orders từ Firebase Firestore về Backend MySQL.
 * Hỗ trợ cả Web và Mobile App đẩy orders lên Firebase.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseOrderSyncService {

    private static final String ORDERS_COLLECTION = "orders";
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final UserAccessService userAccessService;
    private final NotificationService notificationService;

    /**
     * Kiểm tra Firebase đã được cấu hình chưa.
     */
    public boolean isFirebaseConfigured() {
        return !FirebaseApp.getApps().isEmpty();
    }

    /**
     * Lấy Firestore instance.
     */
    private Firestore getFirestore() {
        if (!isFirebaseConfigured()) {
            throw new IllegalStateException("Firebase is not configured");
        }
        return FirestoreClient.getFirestore();
    }

    /**
     * Sync tất cả orders chưa được sync từ Firestore về Backend.
     *
     * @return Số lượng orders đã sync thành công
     */
    @Transactional
    public int syncPendingOrders() {
        if (!isFirebaseConfigured()) {
            log.warn("Firebase not configured. Skipping order sync.");
            return 0;
        }

        try {
            Firestore db = getFirestore();
            CollectionReference ordersRef = db.collection(ORDERS_COLLECTION);

            // Query orders chưa sync và có trạng thái PAID
            Query query = ordersRef
                    .whereEqualTo("synced", false)
                    .whereEqualTo("status", "paid");

            ApiFuture<QuerySnapshot> future = query.get();
            QuerySnapshot snapshot = future.get();

            int syncedCount = 0;
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                try {
                    FirebaseOrderDto firebaseOrder = mapToFirebaseOrderDto(doc);
                    if (syncSingleOrder(firebaseOrder, doc.getReference())) {
                        syncedCount++;
                    }
                } catch (Exception e) {
                    log.error("Failed to sync order {}: {}", doc.getId(), e.getMessage());
                }
            }

            log.info("Synced {} orders from Firebase to Backend", syncedCount);
            return syncedCount;

        } catch (ExecutionException | InterruptedException e) {
            log.error("Error fetching orders from Firestore: {}", e.getMessage());
            Thread.currentThread().interrupt();
            return 0;
        }
    }

    /**
     * Sync một order cụ thể theo Firebase document ID.
     */
    @Transactional
    public Optional<OrderEntity> syncOrderById(String firebaseOrderId) {
        if (!isFirebaseConfigured()) {
            log.warn("Firebase not configured. Cannot sync order.");
            return Optional.empty();
        }

        try {
            Firestore db = getFirestore();
            DocumentReference docRef = db.collection(ORDERS_COLLECTION).document(firebaseOrderId);
            DocumentSnapshot doc = docRef.get().get();

            if (!doc.exists()) {
                log.warn("Firebase order not found: {}", firebaseOrderId);
                return Optional.empty();
            }

            FirebaseOrderDto firebaseOrder = mapToFirebaseOrderDto(doc);

            // Kiểm tra đã sync chưa
            if (Boolean.TRUE.equals(firebaseOrder.getSynced()) && firebaseOrder.getBackendOrderId() != null) {
                log.info("Order {} already synced to backend order #{}",
                        firebaseOrderId, firebaseOrder.getBackendOrderId());
                return orderRepository.findById(firebaseOrder.getBackendOrderId());
            }

            if (syncSingleOrder(firebaseOrder, docRef)) {
                return orderRepository.findById(firebaseOrder.getBackendOrderId());
            }

            return Optional.empty();

        } catch (ExecutionException | InterruptedException e) {
            log.error("Error syncing order {}: {}", firebaseOrderId, e.getMessage());
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    /**
     * Tạo order mới trên Firestore (cho Web sử dụng).
     */
    public String createFirebaseOrder(FirebaseOrderDto orderDto) {
        if (!isFirebaseConfigured()) {
            throw new IllegalStateException("Firebase is not configured");
        }

        try {
            Firestore db = getFirestore();
            CollectionReference ordersRef = db.collection(ORDERS_COLLECTION);

            Map<String, Object> orderData = new HashMap<>();
            orderData.put("userId", orderDto.getUserId());
            orderData.put("userEmail", orderDto.getUserEmail());
            orderData.put("totalAmount", orderDto.getTotalAmount().doubleValue());
            orderData.put("originalAmount", orderDto.getOriginalAmount() != null ?
                    orderDto.getOriginalAmount().doubleValue() : orderDto.getTotalAmount().doubleValue());
            orderData.put("discountAmount", orderDto.getDiscountAmount() != null ?
                    orderDto.getDiscountAmount().doubleValue() : 0.0);
            orderData.put("voucherCode", orderDto.getVoucherCode());
            orderData.put("status", "pending");
            orderData.put("paymentMethod", orderDto.getPaymentMethod() != null ?
                    orderDto.getPaymentMethod() : "VNPAY");
            orderData.put("source", orderDto.getSource() != null ? orderDto.getSource() : "WEB");
            orderData.put("createdAt", System.currentTimeMillis());
            orderData.put("updatedAt", System.currentTimeMillis());
            orderData.put("synced", false);

            // Add items
            if (orderDto.getItems() != null && !orderDto.getItems().isEmpty()) {
                List<Map<String, Object>> items = orderDto.getItems().stream()
                        .map(item -> {
                            Map<String, Object> itemData = new HashMap<>();
                            itemData.put("categoryCode", item.getCategoryCode());
                            itemData.put("planCode", item.getPlanCode());
                            itemData.put("price", item.getPrice().doubleValue());
                            itemData.put("durationDays", item.getDurationDays());
                            itemData.put("categoryName", item.getCategoryName());
                            itemData.put("planName", item.getPlanName());
                            return itemData;
                        })
                        .collect(Collectors.toList());
                orderData.put("items", items);
            }

            ApiFuture<DocumentReference> future = ordersRef.add(orderData);
            DocumentReference docRef = future.get();

            log.info("Created Firebase order: {}", docRef.getId());
            return docRef.getId();

        } catch (ExecutionException | InterruptedException e) {
            log.error("Error creating Firebase order: {}", e.getMessage());
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to create Firebase order", e);
        }
    }

    /**
     * Cập nhật trạng thái order trên Firestore.
     */
    public void updateFirebaseOrderStatus(String firebaseOrderId, String status,
                                          String transactionNo, String bankCode, String payDate) {
        if (!isFirebaseConfigured()) {
            log.warn("Firebase not configured. Cannot update order status.");
            return;
        }

        try {
            Firestore db = getFirestore();
            DocumentReference docRef = db.collection(ORDERS_COLLECTION).document(firebaseOrderId);

            Map<String, Object> updates = new HashMap<>();
            updates.put("status", status.toLowerCase());
            updates.put("updatedAt", System.currentTimeMillis());

            if (transactionNo != null) {
                updates.put("transactionNo", transactionNo);
            }
            if (bankCode != null) {
                updates.put("bankCode", bankCode);
            }
            if (payDate != null) {
                updates.put("payDate", payDate);
            }

            docRef.update(updates).get();
            log.info("Updated Firebase order {} status to {}", firebaseOrderId, status);

        } catch (ExecutionException | InterruptedException e) {
            log.error("Error updating Firebase order {}: {}", firebaseOrderId, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Đánh dấu order đã sync về backend.
     */
    public void markOrderAsSynced(String firebaseOrderId, Long backendOrderId) {
        if (!isFirebaseConfigured()) {
            return;
        }

        try {
            Firestore db = getFirestore();
            DocumentReference docRef = db.collection(ORDERS_COLLECTION).document(firebaseOrderId);

            Map<String, Object> updates = new HashMap<>();
            updates.put("synced", true);
            updates.put("backendOrderId", backendOrderId);
            updates.put("syncedAt", System.currentTimeMillis());

            docRef.update(updates).get();
            log.info("Marked Firebase order {} as synced with backend order #{}",
                    firebaseOrderId, backendOrderId);

        } catch (ExecutionException | InterruptedException e) {
            log.error("Error marking order as synced: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Sync một order từ Firebase về Backend MySQL.
     */
    private boolean syncSingleOrder(FirebaseOrderDto firebaseOrder, DocumentReference docRef) {
        try {
            // Resolve userId từ email nếu cần
            Long userId = firebaseOrder.getUserId();
            if (userId == null && firebaseOrder.getUserEmail() != null) {
                Optional<User> user = userRepository.findByEmail(firebaseOrder.getUserEmail());
                if (user.isPresent()) {
                    userId = user.get().getId();
                } else {
                    log.warn("User not found for email: {}", firebaseOrder.getUserEmail());
                    return false;
                }
            }

            if (userId == null) {
                log.warn("Cannot sync order without userId: {}", firebaseOrder.getFirebaseOrderId());
                return false;
            }

            // Tạo order entity
            OrderEntity order = OrderEntity.builder()
                    .userId(userId)
                    .totalAmount(firebaseOrder.getTotalAmount())
                    .originalAmount(firebaseOrder.getOriginalAmount())
                    .discountAmount(firebaseOrder.getDiscountAmount())
                    .voucherCode(firebaseOrder.getVoucherCode())
                    .status(mapStatus(firebaseOrder.getStatus()))
                    .paymentMethod(firebaseOrder.getPaymentMethod())
                    .transactionNo(firebaseOrder.getTransactionNo())
                    .bankCode(firebaseOrder.getBankCode())
                    .payDate(firebaseOrder.getPayDate())
                    .build();

            // Set timestamps
            if (firebaseOrder.getCreatedAt() != null) {
                order.setCreatedAt(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(firebaseOrder.getCreatedAt()), VIETNAM_ZONE));
            }
            if (firebaseOrder.getUpdatedAt() != null) {
                order.setUpdatedAt(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(firebaseOrder.getUpdatedAt()), VIETNAM_ZONE));
            }

            // Save order first to get ID
            OrderEntity savedOrder = orderRepository.save(order);

            // Create order items
            if (firebaseOrder.getItems() != null && !firebaseOrder.getItems().isEmpty()) {
                List<OrderItemEntity> items = firebaseOrder.getItems().stream()
                        .map(itemDto -> OrderItemEntity.builder()
                                .order(savedOrder)
                                .categoryCode(itemDto.getCategoryCode())
                                .planCode(itemDto.getPlanCode())
                                .price(itemDto.getPrice())
                                .durationDays(itemDto.getDurationDays())
                                .build())
                        .collect(Collectors.toList());
                savedOrder.setItems(items);
                orderRepository.save(savedOrder);

                // Grant access if order is PAID
                if (OrderStatus.PAID.equals(savedOrder.getStatus())) {
                    for (OrderItemEntity item : items) {
                        userAccessService.grantAccessDirectly(
                                userId,
                                item.getCategoryCode(),
                                item.getDurationDays()
                        );
                        log.info("Granted access for category {} to user {}",
                                item.getCategoryCode(), userId);
                    }
                }
            }

            // Update Firebase order with backend ID
            firebaseOrder.setBackendOrderId(savedOrder.getId());
            markOrderAsSynced(firebaseOrder.getFirebaseOrderId(), savedOrder.getId());

            log.info("Successfully synced Firebase order {} to backend order #{}",
                    firebaseOrder.getFirebaseOrderId(), savedOrder.getId());

            return true;

        } catch (Exception e) {
            log.error("Error syncing single order: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Map Firebase document to DTO.
     */
    @SuppressWarnings("unchecked")
    private FirebaseOrderDto mapToFirebaseOrderDto(DocumentSnapshot doc) {
        FirebaseOrderDto dto = new FirebaseOrderDto();
        dto.setFirebaseOrderId(doc.getId());
        dto.setUserId(doc.getLong("userId"));
        dto.setUserEmail(doc.getString("userEmail"));
        dto.setTotalAmount(toBigDecimal(doc.getDouble("totalAmount")));
        dto.setOriginalAmount(toBigDecimal(doc.getDouble("originalAmount")));
        dto.setDiscountAmount(toBigDecimal(doc.getDouble("discountAmount")));
        dto.setVoucherCode(doc.getString("voucherCode"));
        dto.setStatus(doc.getString("status"));
        dto.setPaymentMethod(doc.getString("paymentMethod"));
        dto.setTransactionNo(doc.getString("transactionNo"));
        dto.setBankCode(doc.getString("bankCode"));
        dto.setPayDate(doc.getString("payDate"));
        dto.setSource(doc.getString("source"));
        dto.setCreatedAt(doc.getLong("createdAt"));
        dto.setUpdatedAt(doc.getLong("updatedAt"));
        dto.setSynced(doc.getBoolean("synced"));
        dto.setBackendOrderId(doc.getLong("backendOrderId"));

        // Map items
        List<Map<String, Object>> itemsRaw = (List<Map<String, Object>>) doc.get("items");
        if (itemsRaw != null) {
            List<FirebaseOrderDto.FirebaseOrderItemDto> items = itemsRaw.stream()
                    .map(itemMap -> FirebaseOrderDto.FirebaseOrderItemDto.builder()
                            .categoryCode((String) itemMap.get("categoryCode"))
                            .planCode((String) itemMap.get("planCode"))
                            .price(toBigDecimal((Double) itemMap.get("price")))
                            .durationDays(((Number) itemMap.get("durationDays")).intValue())
                            .categoryName((String) itemMap.get("categoryName"))
                            .planName((String) itemMap.get("planName"))
                            .build())
                    .collect(Collectors.toList());
            dto.setItems(items);
        }

        return dto;
    }

    /**
     * Map string status to OrderStatus enum.
     */
    private OrderStatus mapStatus(String status) {
        if (status == null) {
            return OrderStatus.PENDING;
        }
        switch (status.toLowerCase()) {
            case "paid":
                return OrderStatus.PAID;
            case "failed":
                return OrderStatus.FAILED;
            case "cancelled":
                return OrderStatus.CANCELLED;
            case "expired":
                return OrderStatus.EXPIRED;
            case "refunded":
                return OrderStatus.REFUNDED;
            default:
                return OrderStatus.PENDING;
        }
    }

    /**
     * Convert Double to BigDecimal safely.
     */
    private BigDecimal toBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : BigDecimal.ZERO;
    }

    /**
     * Lấy danh sách orders từ Firestore theo userId.
     */
    public List<FirebaseOrderDto> getOrdersByUserId(Long userId) {
        if (!isFirebaseConfigured()) {
            return Collections.emptyList();
        }

        try {
            Firestore db = getFirestore();
            Query query = db.collection(ORDERS_COLLECTION)
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING);

            QuerySnapshot snapshot = query.get().get();
            return snapshot.getDocuments().stream()
                    .map(this::mapToFirebaseOrderDto)
                    .collect(Collectors.toList());

        } catch (ExecutionException | InterruptedException e) {
            log.error("Error fetching orders for user {}: {}", userId, e.getMessage());
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
    }
}
