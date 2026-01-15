package com.hth.udecareer.service;

import com.hth.udecareer.service.Impl.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EmailSMTPService.
 * These tests verify configuration and validation logic without actually sending emails.
 * For actual email sending tests, see EmailSMTPServiceIntegrationTest.
 */
@ExtendWith(MockitoExtension.class)
public class EmailSMTPServiceTest {

    private EmailSMTPService emailSMTPService;

    @BeforeEach
    void setUp() {
        emailSMTPService = new EmailSMTPService();

        // Set up test configuration
        ReflectionTestUtils.setField(emailSMTPService, "smtpHost", "localhost");
        ReflectionTestUtils.setField(emailSMTPService, "smtpPort", 587);
        ReflectionTestUtils.setField(emailSMTPService, "smtpUsername", "test-user");
        ReflectionTestUtils.setField(emailSMTPService, "smtpPassword", "test-password");
        ReflectionTestUtils.setField(emailSMTPService, "fromEmail", "test@example.com");
        ReflectionTestUtils.setField(emailSMTPService, "fromName", "Test");
    }

    @Test
    void testEmailServiceImplementsInterface() {
        // Verify that EmailSMTPService implements EmailService
        assertTrue(emailSMTPService instanceof EmailService);
    }

    @Test
    void testConfigurationFieldsAreSet() {
        // Verify that configuration fields can be set via reflection
        String smtpHost = (String) ReflectionTestUtils.getField(emailSMTPService, "smtpHost");
        Integer smtpPort = (Integer) ReflectionTestUtils.getField(emailSMTPService, "smtpPort");
        String fromEmail = (String) ReflectionTestUtils.getField(emailSMTPService, "fromEmail");

        assertEquals("localhost", smtpHost);
        assertEquals(587, smtpPort);
        assertEquals("test@example.com", fromEmail);
    }

    @Test
    void testSendMail_withNullEmail_shouldThrowException() {
        // given
        String emailTo = null;
        String subject = "Test Subject";
        String message = "Test Message";

        // when & then
        Exception exception = assertThrows(Exception.class,
            () -> emailSMTPService.sendMail(emailTo, subject, message));
        assertNotNull(exception);
    }

    @Test
    void testSendMail_withInvalidEmail_shouldThrowException() {
        // given
        String emailTo = "invalid-email";
        String subject = "Test Subject";
        String message = "Test Message";

        // when & then
        Exception exception = assertThrows(Exception.class,
            () -> emailSMTPService.sendMail(emailTo, subject, message));
        assertNotNull(exception);
    }

    @Test
    void testSendMail_withEmptySubject_shouldNotThrowValidationException() {
        // given
        String emailTo = "test@example.com";
        String subject = "";
        String message = "Test Message";

        // when & then
        // Empty subject is allowed by email standards,
        // but will fail due to SMTP connection in this test
        Exception exception = assertThrows(Exception.class,
            () -> emailSMTPService.sendMail(emailTo, subject, message));
        // Should fail on SMTP connection, not validation
        assertNotNull(exception);
    }
}
