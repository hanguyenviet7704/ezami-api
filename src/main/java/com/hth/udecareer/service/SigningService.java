package com.hth.udecareer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight HMAC signing service. Replace with KMS/HSM integration for production.
 */
@Service
public class SigningService {
    private static final String HMAC_ALGO = "HmacSHA256";

    private final Map<String, SecretKeySpec> keys = new ConcurrentHashMap<>();
    private final String defaultKeyId;

    public SigningService(@Value("${app.qr.signing.key:}") String base64Key,
                          @Value("${app.qr.signing.key-id:local-1}") String keyId) {
        String keyStr = base64Key;
        if (keyStr == null || keyStr.isEmpty()) {
            // Create a deterministic key for local/dev use (not secure)
            keyStr = Base64.getUrlEncoder().withoutPadding().encodeToString("dev-secret-key-which-is-not-secure".getBytes(StandardCharsets.UTF_8));
        }
        byte[] keyBytes = Base64.getUrlDecoder().decode(keyStr);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, HMAC_ALGO);
        this.keys.put(keyId, keySpec);
        this.defaultKeyId = keyId;
    }

    public void addKey(String keyId, byte[] keyBytes) {
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, HMAC_ALGO);
        this.keys.put(keyId, keySpec);
    }

    public String getCurrentKeyId() {
        return this.defaultKeyId;
    }

    public String sign(String payload) {
        return signWithKey(payload, defaultKeyId);
    }

    public String signWithKey(String payload, String keyId) {
        try {
            SecretKeySpec keySpec = keys.get(keyId);
            if (keySpec == null) throw new IllegalArgumentException("Unknown keyId");
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(keySpec);
            byte[] sig = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(sig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String signTruncated(String payload, String keyId, int bytes) {
        try {
            SecretKeySpec keySpec = keys.get(keyId);
            if (keySpec == null) throw new IllegalArgumentException("Unknown keyId");
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(keySpec);
            byte[] sig = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            if (bytes < 0 || bytes > sig.length) bytes = sig.length;
            byte[] truncated = java.util.Arrays.copyOfRange(sig, 0, bytes);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(truncated);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean verify(String payload, String signature, String keyId) {
        try {
            String computed = signWithKey(payload, keyId);
            if (computed.equals(signature)) return true;
            // Check truncated match as well (e.g., 16 byte / 128-bit truncation used in QR)
            int tryBytes = 16; // try common truncation
            String computedTruncated = signTruncated(payload, keyId, tryBytes);
            return computedTruncated.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean verifyTruncated(String payload, String signature, String keyId, int bytes) {
        try {
            String computed = signTruncated(payload, keyId, bytes);
            return computed.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }
}
