package com.hth.udecareer.service.Impl;

import com.hth.udecareer.entities.OrderEntity;
import com.hth.udecareer.entities.OrderItemEntity;
import com.hth.udecareer.entities.QrTransaction;
import com.hth.udecareer.entities.QuizCategoryEntity;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.enums.OrderStatus;
import com.hth.udecareer.model.response.ApiResponse;
import com.hth.udecareer.model.webhook.SePayWebhookDto;
import com.hth.udecareer.repository.OrderRepository;
import com.hth.udecareer.repository.QrTransactionRepository;
import com.hth.udecareer.repository.QuizCategoryRepository;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.repository.UserPurchasedRepository;
import com.hth.udecareer.service.SePayWebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestPropertySource(properties = "sepay.webhookSecret=test-secret-123456")
class SePayWebhookServiceImplTest {

    @Autowired
    private SePayWebhookService sePayWebhookService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private QrTransactionRepository qrTransactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPurchasedRepository userPurchasedRepository;

    @Autowired
    private QuizCategoryRepository quizCategoryRepository;

    @BeforeEach
    void setUp() {
        // Create all required quiz categories to satisfy FK constraints
        createCategoryIfNotExists("CAT_TEST", "Test Category");
        createCategoryIfNotExists("CAT_ORDER", "Order Category");
        createCategoryIfNotExists("CAT_QR", "QR Category");
        createCategoryIfNotExists("CAT_SAMPLE", "Sample Category");
        createCategoryIfNotExists("CAT_MISMATCH", "Mismatch Category");
        createCategoryIfNotExists("CAT_TARGET", "Target Category");
    }

    private void createCategoryIfNotExists(String code, String title) {
        if (quizCategoryRepository.findByCode(code).isEmpty()) {
            QuizCategoryEntity category = new QuizCategoryEntity();
            category.setCode(code);
            category.setTitle(title);
            category.setEnable(true);
            quizCategoryRepository.save(category);
        }
    }

    private User createTestUser(String emailPrefix) {
        User u = new User();
        u.setEmail(emailPrefix + "@example.com");
        u.setUsername(emailPrefix);
        u.setPassword("password");
        u.setActivationKey("initkey");
        u.setDisplayName("Test " + emailPrefix);
        u.setNiceName(emailPrefix);
        u.setRegisteredDate(LocalDateTime.now());
        u.setStatus(0);
        u.setUserUrl("");
        return userRepository.save(u);
    }

    @Test
    void testProcess_referenceOrderShouldMarkPaid() {
        User u = createTestUser("sepaytest");

        OrderEntity order = new OrderEntity();
        order.setUserId(u.getId());
        order.setTotalAmount(BigDecimal.valueOf(100000L));
        order.setStatus(OrderStatus.PENDING);
        order = orderRepository.save(order);

        OrderItemEntity item = new OrderItemEntity();
        item.setOrder(order);
        item.setCategoryCode("CAT_TEST");
        item.setPlanCode("PLAN_30");
        item.setPrice(BigDecimal.valueOf(100000L));
        item.setDurationDays(30);
        order.setItems(new ArrayList<>(List.of(item)));
        order = orderRepository.save(order);

        SePayWebhookDto dto = new SePayWebhookDto();
        dto.setReferenceCode("Order#" + order.getId());
        dto.setTransferType("in");
        dto.setTransferAmount(100000L);

        ApiResponse resp = sePayWebhookService.process(dto);
        assertNotNull(resp);
        assertEquals(200, resp.getCode());

        OrderEntity updated = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.PAID, updated.getStatus());
        assertEquals("SEPAY", updated.getPaymentMethod());

        var purchases = userPurchasedRepository.findAllByUserIdOrUserEmail(u.getId(), u.getEmail());
        assertFalse(purchases.isEmpty());
        assertTrue(purchases.stream().anyMatch(p -> "CAT_TEST".equals(p.getCategoryCode())));
    }

    @Test
    void testProcess_contentOrderShouldMarkPaid() {
        User u = createTestUser("sepaytest4");

        OrderEntity order = new OrderEntity();
        order.setUserId(u.getId());
        order.setTotalAmount(BigDecimal.valueOf(120000L));
        order.setStatus(OrderStatus.PENDING);

        OrderItemEntity item = new OrderItemEntity();
        item.setOrder(order);
        item.setCategoryCode("CAT_ORDER");
        item.setPlanCode("PLAN_30");
        item.setPrice(BigDecimal.valueOf(120000L));
        item.setDurationDays(30);
        order.setItems(new ArrayList<>(List.of(item)));
        order = orderRepository.save(order);

        SePayWebhookDto dto = new SePayWebhookDto();
        dto.setContent("Order" + order.getId());
        dto.setTransferType("in");
        dto.setTransferAmount(120000L);

        ApiResponse resp = sePayWebhookService.process(dto);
        assertNotNull(resp);
        assertEquals(200, resp.getCode());

        var updated = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.PAID, updated.getStatus());
        assertEquals("SEPAY", updated.getPaymentMethod());

        var purchases = userPurchasedRepository.findAllByUserIdOrUserEmail(u.getId(), u.getEmail());
        assertFalse(purchases.isEmpty());
        assertTrue(purchases.stream().anyMatch(p -> "CAT_ORDER".equals(p.getCategoryCode())));
    }

    @Test
    void testProcess_qrTransactionMatches() {
        QrTransaction tx = new QrTransaction();
        tx.setTransactionId(UUID.randomUUID().toString());
        tx.setBankAccount("0123499999");
        tx.setBankCode("BIDV");
        tx.setAmount("2277000");
        tx.setMessage("test");
        tx.setUsed(false);
        tx.setCreatedAt(Instant.now());
        tx.setExpireAt(Instant.now().plus(Duration.ofHours(1)));
        tx = qrTransactionRepository.save(tx);

        SePayWebhookDto dto = new SePayWebhookDto();
        dto.setAccountNumber("0123499999");
        dto.setTransferType("in");
        dto.setTransferAmount(2277000L);

        ApiResponse resp = sePayWebhookService.process(dto);
        assertNotNull(resp);
        assertEquals(200, resp.getCode());

        var updated = qrTransactionRepository.findByTransactionId(tx.getTransactionId()).orElseThrow();
        assertTrue(updated.isUsed());
    }

    @Test
    void testProcess_qrTransactionMatches_withOrder() {
        User u = createTestUser("sepaytest2");

        OrderEntity order = new OrderEntity();
        order.setUserId(u.getId());
        order.setTotalAmount(BigDecimal.valueOf(2277000L));
        order.setStatus(OrderStatus.PENDING);

        OrderItemEntity item2 = new OrderItemEntity();
        item2.setOrder(order);
        item2.setCategoryCode("CAT_QR");
        item2.setPlanCode("PLAN_30");
        item2.setPrice(BigDecimal.valueOf(2277000L));
        item2.setDurationDays(30);
        order.setItems(new ArrayList<>(List.of(item2)));
        order = orderRepository.save(order);

        QrTransaction tx = new QrTransaction();
        tx.setTransactionId(UUID.randomUUID().toString());
        tx.setBankAccount("0123499999");
        tx.setBankCode("BIDV");
        tx.setAmount(String.valueOf(2277000L));
        tx.setMessage("Order#" + order.getId());
        tx.setUsed(false);
        tx.setCreatedAt(Instant.now());
        tx.setExpireAt(Instant.now().plus(Duration.ofHours(1)));
        qrTransactionRepository.save(tx);

        SePayWebhookDto dto = new SePayWebhookDto();
        dto.setAccountNumber("0123499999");
        dto.setTransferType("in");
        dto.setTransferAmount(2277000L);

        ApiResponse resp = sePayWebhookService.process(dto);
        assertNotNull(resp);
        assertEquals(200, resp.getCode());

        var updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.PAID, updatedOrder.getStatus());
        assertEquals("QRPAY", updatedOrder.getPaymentMethod());

        var purchases = userPurchasedRepository.findAllByUserIdOrUserEmail(u.getId(), u.getEmail());
        assertFalse(purchases.isEmpty());
        assertTrue(purchases.stream().anyMatch(p -> "CAT_QR".equals(p.getCategoryCode())));
    }

    @Test
    void testVerifyWebhookSignature() throws Exception {
        String secret = "test-secret-123456";
        String payload = "{\"id\":123,\"transferAmount\":1000}";

        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        javax.crypto.spec.SecretKeySpec key = new javax.crypto.spec.SecretKeySpec(
                secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(key);
        byte[] sig = mac.doFinal(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String base64 = java.util.Base64.getEncoder().encodeToString(sig);

        boolean ok = ((SePayWebhookServiceImpl) sePayWebhookService).verifyWebhookSignature(payload, base64);
        assertTrue(ok);

        assertFalse(((SePayWebhookServiceImpl) sePayWebhookService).verifyWebhookSignature(payload, "bad"));
    }

    @Test
    void testProcess_samplePayloadShouldMarkPendingOrderByAmount() {
        User u = createTestUser("sepaytest3");

        OrderEntity order = new OrderEntity();
        order.setUserId(u.getId());
        order.setTotalAmount(BigDecimal.valueOf(2277000L));
        order.setStatus(OrderStatus.PENDING);

        OrderItemEntity item = new OrderItemEntity();
        item.setOrder(order);
        item.setCategoryCode("CAT_SAMPLE");
        item.setPlanCode("PLAN_30");
        item.setPrice(BigDecimal.valueOf(2277000L));
        item.setDurationDays(30);
        order.setItems(new ArrayList<>(List.of(item)));
        order = orderRepository.save(order);

        SePayWebhookDto dto = new SePayWebhookDto();
        dto.setId("ee3ea8b0-439c-4902-b860-66938d12f3eb");
        dto.setGateway("BIDV");
        dto.setTransactionDate("2025-11-25 14:02:37");
        dto.setAccountNumber("");
        dto.setCode(null);
        dto.setContent("chuyen tien mua iphone");
        dto.setTransferType("in");
        dto.setTransferAmount(2277000L);
        dto.setAccumulated(19077000L);
        dto.setSubAccount(null);
        dto.setReferenceCode("MBVCB.3278907687");
        dto.setDescription("");

        ApiResponse resp = sePayWebhookService.process(dto);
        assertNotNull(resp);
        assertEquals(200, resp.getCode());

        var updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.PAID, updatedOrder.getStatus());
        assertEquals("BIDV", updatedOrder.getPaymentMethod());

        var purchases = userPurchasedRepository.findAllByUserIdOrUserEmail(u.getId(), u.getEmail());
        assertFalse(purchases.isEmpty());
        assertTrue(purchases.stream().anyMatch(p -> "CAT_SAMPLE".equals(p.getCategoryCode())));
    }

    @Test
    void testProcess_referenceCodeDigitsShouldNotOverrideContentOrderMatch() {
        User u = createTestUser("sepaytest5");

        // order that would accidentally match referenceCode digits
        OrderEntity orderMismatch = new OrderEntity();
        orderMismatch.setUserId(u.getId());
        orderMismatch.setTotalAmount(BigDecimal.valueOf(11111L));
        orderMismatch.setStatus(OrderStatus.PENDING);

        OrderItemEntity itemMismatch = new OrderItemEntity();
        itemMismatch.setOrder(orderMismatch);
        itemMismatch.setCategoryCode("CAT_MISMATCH");
        itemMismatch.setPlanCode("PLAN_30");
        itemMismatch.setPrice(BigDecimal.valueOf(11111L));
        itemMismatch.setDurationDays(30);
        orderMismatch.setItems(new ArrayList<>(List.of(itemMismatch)));
        orderMismatch = orderRepository.save(orderMismatch);

        // target order referenced by content Order<id>
        OrderEntity orderTarget = new OrderEntity();
        orderTarget.setUserId(u.getId());
        orderTarget.setTotalAmount(BigDecimal.valueOf(50000L));
        orderTarget.setStatus(OrderStatus.PENDING);

        OrderItemEntity itemTarget = new OrderItemEntity();
        itemTarget.setOrder(orderTarget);
        itemTarget.setCategoryCode("CAT_TARGET");
        itemTarget.setPlanCode("PLAN_30");
        itemTarget.setPrice(BigDecimal.valueOf(50000L));
        itemTarget.setDurationDays(30);
        orderTarget.setItems(new ArrayList<>(List.of(itemTarget)));
        orderTarget = orderRepository.save(orderTarget);

        SePayWebhookDto dto = new SePayWebhookDto();
        dto.setReferenceCode(orderMismatch.getId() + "-50d5dbbb-091a-4f56-ad12-b2d76847514a");
        dto.setContent("Order" + orderTarget.getId());
        dto.setTransferType("in");
        dto.setTransferAmount(50000L);

        ApiResponse resp = sePayWebhookService.process(dto);
        assertNotNull(resp);
        assertEquals(200, resp.getCode());

        var updated = orderRepository.findById(orderTarget.getId()).orElseThrow();
        assertEquals(OrderStatus.PAID, updated.getStatus());
        assertEquals("SEPAY", updated.getPaymentMethod());

        var purchases = userPurchasedRepository.findAllByUserIdOrUserEmail(u.getId(), u.getEmail());
        assertFalse(purchases.isEmpty());
        assertTrue(purchases.stream().anyMatch(p -> "CAT_TARGET".equals(p.getCategoryCode())));
    }
}
