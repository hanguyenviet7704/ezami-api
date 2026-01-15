package com.hth.udecareer.controllers;

import com.hth.udecareer.entities.CartItemEntity;
import com.hth.udecareer.entities.QuizCategoryEntity;
import com.hth.udecareer.entities.SubscriptionPlanEntity;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.model.qr.QrValidateRequest;
import com.hth.udecareer.model.request.VnpayPaymentRequest;
import com.hth.udecareer.repository.CartItemRepository;
import com.hth.udecareer.repository.QuizCategoryRepository;
import com.hth.udecareer.repository.SubscriptionPlanRepository;
import com.hth.udecareer.repository.UserRepository;
import com.hth.udecareer.service.QRCodeService;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class QrPaymentControllerIntegrationTest {

    @Autowired
    private QrPaymentController qrPaymentController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private QRCodeController qrController;

    @Autowired
    private QuizCategoryRepository quizCategoryRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @BeforeEach
    void setUp() {
        createCategoryIfNotExists("PLAN_30", "Plan 30 Days");
        createPlanIfNotExists("PLAN_30", "Plan 30 Days", BigDecimal.valueOf(100000), 30);
    }

    private void createPlanIfNotExists(String code, String name, BigDecimal price, int durationDays) {
        if (subscriptionPlanRepository.findByCode(code).isEmpty()) {
            SubscriptionPlanEntity plan = new SubscriptionPlanEntity();
            plan.setCode(code);
            plan.setName(name);
            plan.setPrice(price);
            plan.setDurationDays(durationDays);
            subscriptionPlanRepository.save(plan);
        }
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

    private User createTestUser(String prefix) {
        User u = new User();
        u.setEmail(prefix + "-" + UUID.randomUUID() + "@example.com");
        u.setDisplayName(prefix + " Test");
        u.setUsername(prefix);
        u.setNiceName(prefix);
        u.setUserUrl("");
        u.setPassword("password");
        u.setActivationKey("key");
        u.setRegisteredDate(LocalDateTime.now());
        u.setStatus(0);
        return userRepository.save(u);
    }

    @Test
    void buyNow_shouldReturnParsableQrString() {
        User u = createTestUser("buy-now");
        final String email = u.getEmail();
        Principal p = () -> email;

        VnpayPaymentRequest req = new VnpayPaymentRequest();
        req.setCategoryCode("PLAN_30");
        req.setPlanCode("PLAN_30");

        var response = qrPaymentController.buyNowQr(req, p, null);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("qrString"));

        String qrString = (String) body.get("qrString");
        assertNotNull(qrString);
        assertTrue(qrString.startsWith("000201"), "qrString should start with EMV header");

        var parsed = qrCodeService.parseTlv(qrString);
        assertTrue(parsed.containsKey("62"), "Parsed TLV should contain tag 62");

        var sub62 = qrCodeService.parseTlv(parsed.get("62"));
        assertTrue(sub62.containsKey("05"), "62 should contain transaction id 05");

        // Validate via QR controller
        QrValidateRequest validateReq = new QrValidateRequest();
        validateReq.setQrContent(qrString);
        var resp = qrController.validateQR(validateReq, null);
        assertTrue(resp.isValid());
        assertEquals(body.get("transactionId"), resp.getTransactionId());
    }

    @Test
    void checkout_shouldReturnParsableQrString() {
        User u = createTestUser("checkout");
        final String email = u.getEmail();
        final Long userId = u.getId();
        Principal p = () -> email;

        // Add a cart item for checkout
        CartItemEntity cart = CartItemEntity.builder()
                .userId(userId)
                .categoryCode("PLAN_30")
                .planCode("PLAN_30")
                .build();
        cartItemRepository.save(cart);

        var response = qrPaymentController.checkoutQr(null, p, null);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("qrString"));

        String qrString = (String) body.get("qrString");
        assertNotNull(qrString);
        assertTrue(qrString.startsWith("000201"), "qrString should start with EMV header");

        var parsed = qrCodeService.parseTlv(qrString);
        assertTrue(parsed.containsKey("62"), "Parsed TLV should contain tag 62");

        var sub62 = qrCodeService.parseTlv(parsed.get("62"));
        assertTrue(sub62.containsKey("05"), "62 should contain transaction id 05");

        QrValidateRequest validateReq = new QrValidateRequest();
        validateReq.setQrContent(qrString);
        var resp = qrController.validateQR(validateReq, null);
        assertTrue(resp.isValid());
        assertEquals(body.get("transactionId"), resp.getTransactionId());
    }
}
