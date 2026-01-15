package com.hth.udecareer.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.hth.udecareer.utils.PhpPassUtil;

@Component
public class PhpPasswordEncoder implements PasswordEncoder {

    private final PhpPassUtil phpPassUtil;

    public PhpPasswordEncoder() {
        phpPassUtil = new PhpPassUtil(8);
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return phpPassUtil.HashPassword(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }
        try {
            return phpPassUtil.CheckPassword(rawPassword.toString(), encodedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
