package com.hth.udecareer.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hth.udecareer.entities.QuizCategoryEntity;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.entities.UserCourseEntity;
import com.hth.udecareer.entities.UserPurchasedEntity;
import com.hth.udecareer.repository.QuizCategoryRepository;
import com.hth.udecareer.repository.UserCourseRepository;
import com.hth.udecareer.repository.UserPurchasedRepository;
import com.hth.udecareer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WebhookContentAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPurchasedRepository userPurchasedRepository;

    @Autowired
    private UserCourseRepository userCourseRepository;

    @Autowired
    private QuizCategoryRepository quizCategoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Create required quiz category to satisfy FK constraint
        createCategoryIfNotExists("post_123", "Post 123");
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
    void testPostAccess_check_shouldReturnHasAccess_true() throws Exception {
        User u = createTestUser("webhook-access");

        // create a purchased post record
        UserPurchasedEntity p = new UserPurchasedEntity();
        p.setUserId(u.getId());
        p.setUserEmail(u.getEmail());
        p.setCategoryCode("post_123");
        p.setIsPurchased(1);
        p.setFromTime(LocalDateTime.now());
        userPurchasedRepository.save(p);

        String requestJson = """
                {
                    "contentType": "post",
                    "contentId": "123",
                    "userEmail": "%s"
                }
                """.formatted(u.getEmail());

        mockMvc.perform(post("/webhook/content/checkPaid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.hasAccess").value(true));
    }

    @Test
    void testCourseAccess_shouldReturnHasAccess_true() throws Exception {
        User u = createTestUser("course-access");

        // create user course record
        UserCourseEntity uc = new UserCourseEntity();
        uc.setUserId(u.getId());
        uc.setCourseId(101L);
        uc.setCurrentLessonId(0L);
        uc.setProgressPercent(0);
        uc.setStatus("enrolled");
        uc.setLngCode("vn");
        uc.setIsGradable(false);
        uc.setStartTime(System.currentTimeMillis() / 1000);
        uc.setEndTime(null);
        userCourseRepository.save(uc);

        String requestJson = """
                {
                    "contentType": "course",
                    "contentId": "101",
                    "userEmail": "%s"
                }
                """.formatted(u.getEmail());

        mockMvc.perform(post("/webhook/content/checkPaid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.hasAccess").value(true));
    }
}
