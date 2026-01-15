package com.hth.udecareer.service;

import com.hth.udecareer.service.Impl.EmailService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Integration test for EmailSMTPService.
 * These tests will actually send emails using AWS SES SMTP.
 *
 * Run these tests manually to verify email sending functionality.
 * They are disabled by default to prevent accidental email sending during automated tests.
 */
@SpringBootTest
@Disabled("Integration test - run manually to verify email functionality")
public class EmailSMTPServiceIntegrationTest {

    @Autowired
    private EmailService emailService;

    @Test
    void testSendSimpleEmail() {
        // given
        String emailTo = "admin@udecareer.com"; // Change to your test email
        String subject = "Test Email - Simple Text";
        String message = "<h1>Hello from Ezami!</h1><p>This is a test email sent via AWS SES SMTP.</p>";

        // when & then
        assertDoesNotThrow(() -> emailService.sendMail(emailTo, subject, message));
    }

    @Test
    void testSendVerificationCodeEmail() {
        // given
        String emailTo = "admin@udecareer.com"; // Change to your test email
        String subject = "Verification Code Test";
        String verificationCode = "123456";
        String message = getVerificationEmailContent(verificationCode, "REGISTER");

        // when & then
        assertDoesNotThrow(() -> emailService.sendMail(emailTo, subject, message));
    }

    @Test
    void testSendResetPasswordEmail() {
        // given
        String emailTo = "admin@udecareer.com"; // Change to your test email
        String subject = "Reset Password Test";
        String verificationCode = "654321";
        String message = getVerificationEmailContent(verificationCode, "RESET_PASS");

        // when & then
        assertDoesNotThrow(() -> emailService.sendMail(emailTo, subject, message));
    }

    private String getVerificationEmailContent(String confirmationCode, String verificationType) {
        String title;
        String message;
        if ("RESET_PASS".equals(verificationType)) {
            title = "Verification Code";
            message = "This is a one-time code that expires in 15 minutes. Use it to reset password. You can copy-paste it, there is no need to remember it.";
        } else {
            title = "Verification Code";
            message = "This is a one-time code that expires in 15 minutes. Use it to register account. You can copy-paste it, there is no need to remember it.";
        }
        return "<div style=\"background-color:#fff;margin:0;padding:0;width:100%\">\n" +
                "   <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" role=\"presentation\">\n" +
                "      <tbody>\n" +
                "         <tr>\n" +
                "            <td align=\"center\" style=\"background-color:#fff;padding-left:8px;padding-right:8px\">\n" +
                "               <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" role=\"presentation\" style=\"margin:0 auto;max-width:632px\">\n" +
                "                  <tbody>\n" +
                "                     <tr>\n" +
                "                        <td style=\"background-color:#fff;color:#fff;font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:28px;margin-bottom:0;margin-top:0;padding-bottom:10px;padding-left:24px;padding-right:24px;padding-top:10px\">\n" +
                "                           <h3 style=\"color:#000;font-family:Helvetica,Arial,sans-serif;font-size:24px;font-weight:700;line-height:31px;margin-bottom:16px;margin-top:0;text-align:center\">" + title + "</h3>\n" +
                "                           <p style=\"color:#010101;margin-bottom:0;margin-top:0;opacity:80%;text-align:left\">" + message + "</p>\n" +
                "                        </td>\n" +
                "                     </tr>\n" +
                "                  </tbody>\n" +
                "               </table>\n" +
                "            </td>\n" +
                "         </tr>\n" +
                "      </tbody>\n" +
                "   </table>\n" +
                "   <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" role=\"presentation\">\n" +
                "      <tbody>\n" +
                "         <tr>\n" +
                "            <td align=\"center\" style=\"background-color:#fff;padding-left:8px;padding-right:8px\">\n" +
                "               <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" role=\"presentation\" style=\"margin:0 auto;max-width:632px\">\n" +
                "                  <tbody>\n" +
                "                     <tr>\n" +
                "                        <td align=\"center\" style=\"background-color:#fff;color:#82899a;font-family:Helvetica,Arial,sans-serif;font-size:14px;line-height:21px;margin-bottom:0;margin-top:0;padding-bottom:8px;padding-left:24px;padding-right:24px;padding-top:8px\">\n" +
                "                           <table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "                              <tbody>\n" +
                "                                 <tr>\n" +
                "                                    <td width=\"755\" align=\"center\" style=\"background-color:#e3e3e3;border-radius:10px;font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:28px;margin-bottom:0;margin-top:0;padding-bottom:10px;padding-left:8px;padding-right:8px;padding-top:10px\">\n" +
                "                                       <p style=\"border-radius:1000px;color:#242b3d;font-family:Arial;font-size:50px;font-weight:700;height:100px;line-height:100px;margin-bottom:0;margin-top:0;text-align:center;width:100%\"><strong>" + confirmationCode + "</strong></p>\n" +
                "                                    </td>\n" +
                "                                 </tr>\n" +
                "                              </tbody>\n" +
                "                           </table>\n" +
                "                        </td>\n" +
                "                     </tr>\n" +
                "                  </tbody>\n" +
                "               </table>\n" +
                "            </td>\n" +
                "         </tr>\n" +
                "      </tbody>\n" +
                "   </table>\n" +
                "</div>";
    }
}
