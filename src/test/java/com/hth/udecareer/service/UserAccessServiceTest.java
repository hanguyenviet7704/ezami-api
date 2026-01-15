package com.hth.udecareer.service;

import com.hth.udecareer.entities.QuizCategoryEntity;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.entities.UserPurchasedEntity;
import com.hth.udecareer.repository.QuizCategoryRepository;
import com.hth.udecareer.repository.UserPurchasedRepository;
import com.hth.udecareer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserAccessServiceTest {

    @Autowired
    private UserAccessService userAccessService;

    @Autowired
    private UserPurchasedRepository userPurchasedRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuizCategoryRepository quizCategoryRepository;

    @BeforeEach
    void setUp() {
        // Create required quiz category to satisfy FK constraint
        if (quizCategoryRepository.findByCode("CAT_TEST").isEmpty()) {
            QuizCategoryEntity category = new QuizCategoryEntity();
            category.setCode("CAT_TEST");
            category.setTitle("Test Category");
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
    void grantAccessDirectly_createsPurchaseWithExpiry() {
        User u = createTestUser("accessTest");

        LocalDateTime before = LocalDateTime.now(ZoneId.systemDefault());
        userAccessService.grantAccessDirectly(u.getId(), "CAT_TEST", 30);

        List<UserPurchasedEntity> purchases = userPurchasedRepository.findAllByUserIdOrUserEmail(u.getId(), u.getEmail());
        assertFalse(purchases.isEmpty());

        var p = purchases.get(0);
        assertEquals("CAT_TEST", p.getCategoryCode());
        assertNotNull(p.getToTime());
        assertTrue(p.getToTime().isAfter(before.plusDays(29)));
    }

    @Test
    void grantAccessDirectly_extendsExistingExpiry() {
        User u = createTestUser("accessExtTest");

        // create existing purchase
        UserPurchasedEntity existing = new UserPurchasedEntity();
        existing.setUserId(u.getId());
        existing.setUserEmail(u.getEmail());
        existing.setCategoryCode("CAT_TEST");
        existing.setIsPurchased(1);
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        existing.setFromTime(now.minusDays(10));
        existing.setToTime(now.plusDays(10));
        userPurchasedRepository.save(existing);

        userAccessService.grantAccessDirectly(u.getId(), "CAT_TEST", 30);

        List<UserPurchasedEntity> purchases = userPurchasedRepository.findAllByUserIdOrUserEmail(u.getId(), u.getEmail());
        assertFalse(purchases.isEmpty());

        var found = purchases.stream()
                .filter(p -> "CAT_TEST".equals(p.getCategoryCode()))
                .findFirst();
        assertTrue(found.isPresent());

        var p = found.get();
        // new expiry should be old expiry plus 30 days
        assertEquals(now.plusDays(10).plusDays(30).toLocalDate(), p.getToTime().toLocalDate());
    }
}
