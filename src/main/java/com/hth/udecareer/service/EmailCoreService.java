//package com.hth.udecareer.service;
//
//import com.google.api.client.auth.oauth2.Credential;
//import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
//import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
//import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
//import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
//import com.google.api.client.googleapis.json.GoogleJsonError;
//import com.google.api.client.googleapis.json.GoogleJsonResponseException;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.client.json.gson.GsonFactory;
//import com.google.api.client.util.store.FileDataStoreFactory;
//import com.google.api.services.gmail.Gmail;
//import com.google.api.services.gmail.model.Message;
//import org.apache.commons.codec.binary.Base64;
//import org.springframework.stereotype.Service;
//
//import javax.mail.Session;
//import javax.mail.internet.InternetAddress;
//import javax.mail.internet.MimeMessage;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.nio.file.Paths;
//import java.util.Objects;
//import java.util.Properties;
//import java.util.Set;
//
//import static com.google.api.services.gmail.GmailScopes.GMAIL_SEND;
//import static javax.mail.Message.RecipientType.TO;
//
//@Service
//public class EmailCoreService implements EmailService {
//    private static final String TEST_EMAIL = "admin@udecareer.com";
//    private final Gmail service;
//
//    public EmailCoreService() throws Exception {
//        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//        final GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
//        service = new Gmail.Builder(httpTransport, jsonFactory, getCredentials(httpTransport, jsonFactory))
//                .setApplicationName("Test Mailer")
//                .build();
//    }
//
//    private static Credential getCredentials(final NetHttpTransport httpTransport,
//                                             GsonFactory jsonFactory) throws IOException {
//        final GoogleClientSecrets clientSecrets =
//                GoogleClientSecrets.load(jsonFactory,
//                                         new InputStreamReader(
//                                                 Objects.requireNonNull(EmailCoreService.class.getResourceAsStream(
//                                                         "/client_secret_965194767750-ezami.json"))));
//
//        final GoogleAuthorizationCodeFlow flow =
//                new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets,
//                                                        Set.of(GMAIL_SEND))
//                        .setDataStoreFactory(new FileDataStoreFactory(Paths.get("gmail/tokens").toFile()))
//                        .setAccessType("offline")
//                        .build();
//
//        final LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
//        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
//    }
//
//    public void sendMail(String emailTo, String subject, String message) throws Exception {
//        final Properties props = new Properties();
//        final Session session = Session.getDefaultInstance(props, null);
//
//        final MimeMessage email = new MimeMessage(session);
//        email.setFrom(new InternetAddress(TEST_EMAIL, "Ezami"));
//        email.addRecipient(TO, new InternetAddress(emailTo));
//        email.setSubject(subject);
//        email.setText(message, "UTF-8", "html");
//
//        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//        email.writeTo(buffer);
//
//        final byte[] rawMessageBytes = buffer.toByteArray();
//        final String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);
//
//        Message msg = new Message();
//        msg.setRaw(encodedEmail);
//
//        try {
//            msg = service.users().messages().send("me", msg).execute();
//            System.out.println("Message id: " + msg.getId());
//            System.out.println(msg.toPrettyString());
//        } catch (GoogleJsonResponseException e) {
//            final GoogleJsonError error = e.getDetails();
//            if (error.getCode() == 403) {
//                System.err.println("Unable to send message: " + e.getDetails());
//            } else {
//                throw e;
//            }
//        }
//    }
//
//    public static void main(String[] args) throws Exception {
//        new EmailCoreService().sendMail("A new message", "<strong>bold</strong>", "test");
//    }
//}
