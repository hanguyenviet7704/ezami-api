package com.hth.udecareer.service.Impl;

import com.hth.udecareer.entities.User;

public interface GoogleAuthService {

    String generateGoogleAuthUrl(String state);

    User processGoogleCallback(String code) throws Exception;
    
    default User processGoogleCallback(String code, Long affiliateId) throws Exception {
       
        return processGoogleCallback(code);
    }

}