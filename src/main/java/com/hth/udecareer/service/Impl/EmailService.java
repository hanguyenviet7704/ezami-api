package com.hth.udecareer.service.Impl;

public interface EmailService {
    void sendMail(String emailTo, String subject, String message) throws Exception;
}
