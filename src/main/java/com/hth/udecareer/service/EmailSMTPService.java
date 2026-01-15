package com.hth.udecareer.service;

import com.hth.udecareer.service.Impl.EmailService;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class EmailSMTPService implements EmailService {

    @Value("${mail.smtp.host}")
    private String smtpHost;

    @Value("${mail.smtp.port}")
    private int smtpPort;

    @Value("${mail.smtp.username}")
    private String smtpUsername;

    @Value("${mail.smtp.password}")
    private String smtpPassword;

    @Value("${mail.smtp.from}")
    private String fromEmail;

    @Value("${mail.smtp.from.name:Ezami}")
    private String fromName;

    @Override
    public void sendMail(String emailTo, String subject, String message) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUsername, smtpPassword);
            }
        });

        try {
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(fromEmail, fromName));
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(emailTo));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(message, "UTF-8", "html");

            Transport.send(mimeMessage);

            log.info("Email sent successfully to: {}", emailTo);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", emailTo, e);
            throw new Exception("Failed to send email: " + e.getMessage(), e);
        }
    }
}
