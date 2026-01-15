package com.hth.udecareer.utils;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import java.net.URLEncoder;

@Slf4j
@Component
public class VnpayHashUtil {

    public String hmacSHA512(Map<String, String> fields, String hashSecret) {

        if (hashSecret == null) {
            throw new RuntimeException("HashSecret is null. Cannot compute hash.");
        }

        try {
            List<Map.Entry<String, String>> fieldList = new ArrayList<>(fields.entrySet());
            Collections.sort(fieldList, Comparator.comparing(Map.Entry::getKey));

            StringBuilder hashData = new StringBuilder();
            for (Map.Entry<String, String> entry : fieldList) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue();

                if ((fieldValue != null) && (!fieldValue.isEmpty())) {

                    String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString());

                    if (hashData.length() > 0) {
                        hashData.append('&');
                    }
                    hashData.append(fieldName).append('=').append(encodedValue);
                }
            }
            log.info("🔍 VNPAY HASH DATA: {}", hashData.toString());

            return hmacSHA512_core(hashSecret, hashData.toString());

        } catch (Exception e) {
            throw new RuntimeException("Error calculating VNPAY HMAC-SHA512 hash", e);
        }
    }

    private static String hmacSHA512_core(String key, String data) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac.init(secretKey);
        byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(bytes);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}