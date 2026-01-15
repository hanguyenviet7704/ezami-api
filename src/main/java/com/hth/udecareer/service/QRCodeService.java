package com.hth.udecareer.service;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import com.google.zxing.qrcode.QRCodeWriter;
import com.hth.udecareer.service.QrTransactionService;
import com.hth.udecareer.service.QrPaymentGrantService;

import com.hth.udecareer.utils.CRC16Util;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import com.hth.udecareer.model.qr.BankProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import javax.imageio.ImageIO;

@Service
// Removed lombok RequiredArgsConstructor to manually manage constructor injection
@Slf4j
public class QRCodeService {
    private final SigningService signingService;
    public QRCodeService(SigningService signingService) {
        this.signingService = signingService;
    }
    public void generateQRCode(String bankCode, String bankAccount, String amount, String message, OutputStream outputStream) {
        try {
            String qrContent = buildQRContent(bankCode, bankAccount, amount, message);

            int qrWidth = 250;
            int qrHeight = 250;

            // Tạo QR cơ bản
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, qrWidth, qrHeight, hints);

            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            // Tạo ảnh cuối cùng (logo-text trên, QR dưới)
            int logoTextSpacing = 50;
            int finalHeight = qrHeight + logoTextSpacing;
            BufferedImage finalImage = new BufferedImage(qrWidth, finalHeight, BufferedImage.TYPE_INT_RGB);

            Graphics2D g = finalImage.createGraphics();

            // Background trắng
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, qrWidth, finalHeight);

            // --- LOGO TEXT ---
            try {
                ClassPathResource logoTextFile = new ClassPathResource("static/ezami-logo/logo-text.png");

                if (logoTextFile.exists()) {
                    BufferedImage logoTextImage = ImageIO.read(logoTextFile.getInputStream());

                    int logoTextWidth = qrWidth - 40;
                    int logoTextHeight = (int) (logoTextImage.getHeight() * ((double) logoTextWidth / logoTextImage.getWidth()));

                    if (logoTextHeight > 40) {
                        logoTextHeight = 40;
                        logoTextWidth = (int) (logoTextImage.getWidth() * ((double) logoTextHeight / logoTextImage.getHeight()));
                    }

                    int logoTextX = (qrWidth - logoTextWidth) / 2;
                    int logoTextY = 5;

                    g.drawImage(logoTextImage, logoTextX, logoTextY, logoTextWidth, logoTextHeight, null);
                }
            } catch (Exception ignore) {}

            // --- QR IMAGE ---
            g.drawImage(qrImage, 0, logoTextSpacing, qrWidth, qrHeight, null);

            // --- LOGO GIỮA QR ---
            try {
                ClassPathResource logoFile = new ClassPathResource("static/ezami-logo/logo.png");

                if (logoFile.exists()) {
                    BufferedImage logoImage = ImageIO.read(logoFile.getInputStream());

                    int logoSize = qrWidth / 7;
                    int logoX = (qrWidth - logoSize) / 2;
                    int logoY = logoTextSpacing + (qrHeight - logoSize) / 2;

                    // Nền trắng sau logo
                    g.setColor(Color.WHITE);
                    g.fillRoundRect(logoX - 3, logoY - 3, logoSize + 6, logoSize + 6, 10, 10);

                    g.drawImage(logoImage, logoX, logoY, logoSize, logoSize, null);
                }
            } catch (Exception ignore) {}

            g.dispose();

            ImageIO.write(finalImage, "PNG", outputStream);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo QR: " + e.getMessage(), e);
        }
    }

    public void generateImageFromQrContent(String qrContent, OutputStream outputStream) {
        try {
            int qrWidth = 250;
            int qrHeight = 250;

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, qrWidth, qrHeight, hints);

            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            int logoTextSpacing = 50;
            int finalHeight = qrHeight + logoTextSpacing;
            BufferedImage finalImage = new BufferedImage(qrWidth, finalHeight, BufferedImage.TYPE_INT_RGB);

            Graphics2D g = finalImage.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, qrWidth, finalHeight);

            // --- LOGO TEXT ---
            try {
                ClassPathResource logoTextFile = new ClassPathResource("static/ezami-logo/logo-text.png");
                if (logoTextFile.exists()) {
                    BufferedImage logoTextImage = ImageIO.read(logoTextFile.getInputStream());
                    int logoTextWidth = qrWidth - 40;
                    int logoTextHeight = (int) (logoTextImage.getHeight() * ((double) logoTextWidth / logoTextImage.getWidth()));
                    if (logoTextHeight > 40) {
                        logoTextHeight = 40;
                        logoTextWidth = (int) (logoTextImage.getWidth() * ((double) logoTextHeight / logoTextImage.getHeight()));
                    }
                    int logoTextX = (qrWidth - logoTextWidth) / 2;
                    int logoTextY = 5;
                    g.drawImage(logoTextImage, logoTextX, logoTextY, logoTextWidth, logoTextHeight, null);
                }
            } catch (Exception ignore) {}

            g.drawImage(qrImage, 0, logoTextSpacing, qrWidth, qrHeight, null);

            // --- LOGO GIỮA QR ---
            try {
                ClassPathResource logoFile = new ClassPathResource("static/ezami-logo/logo.png");
                if (logoFile.exists()) {
                    BufferedImage logoImage = ImageIO.read(logoFile.getInputStream());
                    int logoSize = qrWidth / 7;
                    int logoX = (qrWidth - logoSize)/2;
                    int logoY = logoTextSpacing + (qrHeight - logoSize)/2;
                    g.setColor(Color.WHITE);
                    g.fillRoundRect(logoX - 3, logoY - 3, logoSize + 6, logoSize + 6, 10, 10);
                    g.drawImage(logoImage, logoX, logoY, logoSize, logoSize, null);
                }
            } catch (Exception ignore) {}

            g.dispose();
            ImageIO.write(finalImage, "PNG", outputStream);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo QR: " + e.getMessage(), e);
        }
    }

    // Simple TLV parser to extract subfield 05 (reference/transaction id) inside tag 62
    public String extractTransactionIdFromQrContent(String qrContent) {
        if (qrContent == null) return null;
        try {
            // Try to extract raw TLV substring if content is wrapped with extra text
            qrContent = extractEmvTlVFromAnyString(qrContent);
            // If the input is an image data URI or base64 image, attempt to decode QR string from it
            if (qrContent.startsWith("data:") || looksLikeBase64Image(qrContent)) {
                var decoded = decodeQrFromImageData(qrContent);
                if (decoded != null) qrContent = decoded;
            }
            var map = parseTlv(qrContent);
            // Ưu tiên lấy transactionId ở 62.05
            if (map.containsKey("62")) {
                String sub = map.get("62");
                var subMap = parseTlv(sub);
                if (subMap.get("05") != null) return subMap.get("05");
            }
            // Nếu không có, thử tìm ở tag 38 (nhiều QR chuẩn ngân hàng để transactionId ở 38)
            if (map.containsKey("38")) {
                String maiSub = map.get("38");
                var maiMap = parseTlv(maiSub);
                if (maiMap.get("05") != null) return maiMap.get("05");
            }
            // Nếu vẫn không có, thử tìm ở các tag khác hoặc log debug
            // Có thể transactionId nằm ở tag khác do tuỳ biến QR
            for (String key : map.keySet()) {
                var subMap = parseTlv(map.get(key));
                if (subMap.get("05") != null) return subMap.get("05");
            }
            // Fallback: search for '05' TLV pattern anywhere in byte stream (more tolerant)
            String fallback = extractTlvByTagAnyplace(qrContent, "05");
            if (fallback != null) {
                log.debug("Fallback TLV extraction found 05: {}", fallback);
                return fallback;
            }
            // Nếu vẫn không có, log rõ để debug
            log.warn("transactionId not found in QR; top-level tags: {} ; raw={}", map.keySet(), (qrContent.length() > 200 ? qrContent.substring(0, 200) + "..." : qrContent));
            return null;
        } catch (Exception e) {
            log.warn("parse error when extracting txId: {}", e.getMessage());
            return null;
        }

        
    }

    /**
     * Public sanitizer: clean input safety and extract TLV from wrapped content and images.
     */
    public String sanitizeQrContent(String input) {
        if (input == null) return null;
        String s = input.trim();
        // If it's a data URI or looks like base64 image, attempt to decode
        if (s.startsWith("data:") || looksLikeBase64Image(s)) {
            String decoded = decodeQrFromImageData(s);
            if (decoded != null) return decoded;
        }
        // If contains TLV pattern, extract the substring
        return extractEmvTlVFromAnyString(s);
    }

    // Fallback helper: search for the given tag anywhere and extract its value by
    // reading the two-byte length (ASCII digits) and then the value by bytes.
    private String extractTlvByTagAnyplace(String tlvString, String tag) {
        if (tlvString == null || tag == null || tag.length() != 2) return null;
        byte[] bytes = tlvString.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (log.isDebugEnabled()) {
            String snippet = new String(bytes, 0, Math.min(bytes.length, 80), java.nio.charset.StandardCharsets.UTF_8);
            log.debug("parseTlv: input len(chars)={} bytes={} snippet='{}'", tlvString.length(), bytes.length, snippet);
        }
        // search for tag bytes anywhere
        byte[] tagBytes = tag.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        for (int i = 0; i + 4 <= bytes.length; i++) {
            if (bytes[i] == tagBytes[0] && bytes[i+1] == tagBytes[1]) {
                // try read two-digit length
                if (i + 4 <= bytes.length) {
                    int len1 = bytes[i+2];
                    int len2 = bytes[i+3];
                    if (len1 >= '0' && len1 <= '9' && len2 >= '0' && len2 <= '9') {
                        int len = (len1 - '0') * 10 + (len2 - '0');
                        int start = i + 4;
                        if (start + len <= bytes.length) {
                            return new String(bytes, start, len, java.nio.charset.StandardCharsets.UTF_8);
                        }
                    }
                }
            }
        }
        return null;
    }

    private String tlv(String tag, String value) {
        if (value == null) value = "";
        byte[] raw = value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (raw.length > 99) {
            // Truncate to 99 bytes to keep TLV length field at two digits
            log.warn("TLV value for tag {} exceeds 99 bytes ({}), truncating", tag, raw.length);
            raw = java.util.Arrays.copyOf(raw, 99);
            value = new String(raw, java.nio.charset.StandardCharsets.UTF_8);
        }
        String len = String.format("%02d", value.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
        return tag + len + value;
    }

    private boolean looksLikeBase64Image(String s) {
        // heuristic: long string with a base64 marker or PNG header
        if (s == null) return false;
        if (s.length() > 100 && s.contains("iVBORw0KGgo")) return true; // PNG header in base64
        return false;
    }

    // Helper: construct TLV from tag and bytes (ensures 2-digit length and truncation to 99 bytes)
    private byte[] makeTlvBytes(String tag, byte[] val) {
        if (tag == null) tag = "";
        if (val == null) val = new byte[0];
        int len = val.length;
        if (len > 99) {
            log.warn("TLV value for tag {} exceeds 99 bytes ({}), truncating", tag, len);
            val = java.util.Arrays.copyOf(val, 99);
            len = 99;
        }
        String lenStr = String.format("%02d", len);
        byte[] tagBytes = tag.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] lenBytes = lenStr.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] out = new byte[2 + 2 + val.length];
        System.arraycopy(tagBytes, 0, out, 0, 2);
        System.arraycopy(lenBytes, 0, out, 2, 2);
        System.arraycopy(val, 0, out, 4, val.length);
        return out;
    }

    private byte[] makeTlvBytes(String tag, String value) {
        byte[] raw = value == null ? new byte[0] : value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return makeTlvBytes(tag, raw);
    }

    private void writeBytes(java.io.ByteArrayOutputStream baos, byte[] data) {
        try {
            baos.write(data, 0, data.length);
        } catch (Exception e) {
            // ByteArrayOutputStream write won't fail in practice; log for completeness
            log.error("Error writing bytes to baos: {}", e.getMessage());
        }
    }

    /**
     * Attempt to decode a QR content string from image bytes (data URI or base64) using ZXing.
     */
    private String decodeQrFromImageData(String dataUriOrBase64) {
        try {
            String base64;
            if (dataUriOrBase64.startsWith("data:")) {
                int comma = dataUriOrBase64.indexOf(',');
                if (comma < 0) return null;
                base64 = dataUriOrBase64.substring(comma + 1);
            } else {
                base64 = dataUriOrBase64;
            }
            byte[] bytes = Base64.getDecoder().decode(base64);
            var img = ImageIO.read(new ByteArrayInputStream(bytes));
            if (img == null) return null;
            var source = new BufferedImageLuminanceSource(img);
            var bitmap = new com.google.zxing.BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap);
            if (result == null) return null;
            return result.getText();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse a TLV string into a map of tag -> value. Assumes 2-char tag and 2-char length, value length in bytes.
     */
    public java.util.Map<String, String> parseTlv(String tlvString) {
        java.util.Map<String, String> map = new java.util.LinkedHashMap<>();
        if (tlvString == null) return map;
        // clean up common wrapping chars: remove leading/trailing quotes
        if (tlvString.startsWith("\"") && tlvString.endsWith("\"")) {
            tlvString = tlvString.substring(1, tlvString.length()-1);
        }
        // we accept data URIs or base64 images upstream; here we only parse raw TLV
        if (tlvString.startsWith("data:image")) return map;

        byte[] bytes = tlvString.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int pos = 0;
        while (pos + 4 <= bytes.length) {
            try {
            String tag;
            try {
                tag = new String(bytes, pos, 2, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception ex) {
                // malformed tag; break
                break;
            }
            String lenStr;
            try {
                lenStr = new String(bytes, pos + 2, 2, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception ex) {
                break;
            }
            int len;
            try {
                len = Integer.parseInt(lenStr);
            } catch (NumberFormatException e) {
                // Invalid TLV length: stop parsing further and return what we have so far.
                log.warn("parseTlv: Tag {} has invalid length byte field: '{}'. Stopping parse.", tag, lenStr);
                break;
            }
            pos += 4;
            if (pos + len > bytes.length) {
                int contextStart = Math.max(0, pos - 16);
                int contextEnd = Math.min(bytes.length, pos + Math.min(len, 64));
                String context = new String(bytes, contextStart, contextEnd - contextStart, java.nio.charset.StandardCharsets.UTF_8);
                log.warn("parseTlv: malformed length for tag {}, len={}, remaining={}, snippet='{}'", tag, len, (bytes.length - pos), context);
                break;
            }
            String value = new String(bytes, pos, len, java.nio.charset.StandardCharsets.UTF_8);
            map.put(tag, value);
            pos += len;
            } catch (IndexOutOfBoundsException | NegativeArraySizeException ex) {
                log.warn("parseTlv: Indexing error while parsing TLV: {} pos={}, bytes.length={}", ex.getMessage(), pos, bytes.length);
                break;
            }
        }
        return map;
    }

    /**
     * Analyze TLV payload and return diagnostics messages without throwing.
     * Useful for debug endpoints to provide user-friendly diagnostics.
     */
    public java.util.List<String> analyzeTlvDiagnostics(String qrContent) {
        java.util.List<String> diagnostics = new java.util.ArrayList<>();
        if (qrContent == null || qrContent.isBlank()) {
            diagnostics.add("Empty QR content");
            return diagnostics;
        }
        String t = extractEmvTlVFromAnyString(qrContent);
        if (t == null || t.isBlank()) {
            diagnostics.add("No EMV TLV detected in input");
            return diagnostics;
        }
        byte[] bytes = t.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        // Check for CRC tag 63
        if (!t.contains("63")) {
            diagnostics.add("Missing CRC tag (63)");
        }
        int pos = 0;
        while (pos + 4 <= bytes.length) {
            String tag;
            try {
                tag = new String(bytes, pos, 2, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception ex) {
                diagnostics.add("Malformed tag at position " + pos);
                break;
            }
            String lenStr;
            try {
                lenStr = new String(bytes, pos + 2, 2, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception ex) {
                diagnostics.add("Malformed length field for tag " + tag + " at position " + pos);
                break;
            }
            int len;
            try {
                len = Integer.parseInt(lenStr);
            } catch (NumberFormatException e) {
                diagnostics.add("Invalid length for tag " + tag + ": '" + lenStr + "'");
                break;
            }
            pos += 4;
            if (pos + len > bytes.length) {
                int remaining = bytes.length - pos;
                int contextStart = Math.max(0, pos - 16);
                int contextEnd = Math.min(bytes.length, pos + Math.min(len, 64));
                String context = new String(bytes, contextStart, contextEnd - contextStart, java.nio.charset.StandardCharsets.UTF_8);
                diagnostics.add("Malformed length for tag " + tag + ", declared=" + len + ", remaining=" + remaining + ", snippet='" + context + "'");
                break;
            }
            pos += len;
        }
        return diagnostics;
    }

    /**
     * Quick validation for major EMV tags and CRC correctness. Returns true when the payload looks valid.
     */
    public boolean isValidEmvQrContent(String qrContent) {
        if (qrContent == null) return false;
        try {
            var map = parseTlv(qrContent);
            // required tags
            if (!map.containsKey("00")) return false; // payload format
            if (!map.containsKey("01")) return false; // initiation method
            if (!map.containsKey("53")) return false; // currency
            if (!map.containsKey("58")) return false; // country
            if (!map.containsKey("54")) return false; // amount
            if (!map.containsKey("62")) return false; // additional data
            if (!map.containsKey("63")) return false; // crc

            // Validate CRC
            String crc = map.get("63");
            // The CRC value is 4 hex chars, but may include leading zeros
            String withoutCrc = qrContent.substring(0, qrContent.indexOf("6304") + 4); // '6304' and its value we will include? adjust
            // We need to compute CRC over everything before the CRC value (i.e., up to and including 63 tag and length '04')
            int idx63 = findTagIndex(qrContent, "63");
            if (idx63 < 0) return false;
            byte[] bytes = qrContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            String toCrc = new String(bytes, 0, idx63 + 4, java.nio.charset.StandardCharsets.UTF_8);
            String computed = CRC16Util.generate(toCrc);
            return computed.equalsIgnoreCase(crc);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Find the position of a tag (two-character) in a TLV string by parsing sequentially.
     */
    private int findTagIndex(String tlvString, String tag) {
        if (tlvString == null) return -1;
        byte[] bytes = tlvString.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int pos = 0;
        while (pos + 4 <= bytes.length) {
            String t;
            try {
                t = new String(bytes, pos, 2, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception e) {
                break;
            }
            int len;
            try {
                len = Integer.parseInt(new String(bytes, pos + 2, 2, java.nio.charset.StandardCharsets.UTF_8));
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                // malformed length; can't parse reliably
                return -1;
            }
            if (t.equals(tag)) return pos;
            pos += 4 + len;
        }
        return -1;
    }

    /**
     * Verify the signature stored inside 62.09 and 62.10
     */
    public boolean verifySignature(String qrContent) {
        try {
            if (qrContent == null) return false;
            var map = parseTlv(qrContent);
            if (!map.containsKey("62")) return false;
            String add = map.get("62");
            var addMap = parseTlv(add);
            String signature = addMap.get("09");
            String keyId = addMap.get("10");
            if (signature == null || keyId == null) return false;

            // Rebuild 62 without 09 and 10
            var cleanAddSb = new StringBuilder();
            for (var e : addMap.entrySet()) {
                if ("09".equals(e.getKey()) || "10".equals(e.getKey())) continue;
                cleanAddSb.append(e.getKey()).append(String.format("%02d", e.getValue().getBytes(java.nio.charset.StandardCharsets.UTF_8).length)).append(e.getValue());
            }
            String clean62 = "62" + String.format("%02d", cleanAddSb.length()) + cleanAddSb.toString();

            // Replace original 62 in qrContent with clean 62
            int pos62 = findTagIndex(qrContent, "62");
            if (pos62 < 0) return false;
            byte[] bytes = qrContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            int len62 = Integer.parseInt(new String(bytes, pos62 + 2, 2, java.nio.charset.StandardCharsets.UTF_8));
            int end62 = pos62 + 4 + len62;
            // rebuild cleaned byte array
            byte[] prefix = java.util.Arrays.copyOfRange(bytes, 0, pos62);
            byte[] clean62Bytes = clean62.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] suffix = java.util.Arrays.copyOfRange(bytes, end62, bytes.length);
            byte[] cleanedBytes = new byte[prefix.length + clean62Bytes.length + suffix.length];
            System.arraycopy(prefix, 0, cleanedBytes, 0, prefix.length);
            System.arraycopy(clean62Bytes, 0, cleanedBytes, prefix.length, clean62Bytes.length);
            System.arraycopy(suffix, 0, cleanedBytes, prefix.length + clean62Bytes.length, suffix.length);
            String cleaned = new String(cleanedBytes, java.nio.charset.StandardCharsets.UTF_8);

            // We should sign the payload excluding CRC tag (63) and verify
            int idx63 = findTagIndex(cleaned, "63");
            byte[] cleanedB = cleaned.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            String toSign = idx63 >= 0 ? new String(cleanedB, 0, idx63, java.nio.charset.StandardCharsets.UTF_8) : cleaned;
            // Signature may have been truncated to 16, 12, 8, 6, or 4 bytes - try those
            int[] tryBytesList = new int[]{16, 12, 8, 6, 4};
            for (int b : tryBytesList) {
                if (signingService.verifyTruncated(toSign, signature, keyId, b)) return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }

        /**
         * This method was moved out to `QRCodeValidationService` to keep the controller thin
         * and keep QRCodeService focused on QR utilities like generating, parsing and CRC checks.
         */
    }

    public String getSigningKeyId() {
        return signingService.getCurrentKeyId();
    }


    // ----------------- BUILD QR CONTENT -------------------

    private String buildQRContent(String bankCode, String bankAccount, String amount, String message) {
        String key = bankCode == null ? null : bankCode.trim().toLowerCase();
        var profile = bankProfiles().get(key);
        String bankId = bankMaps().get(key);
        if (bankId == null) throw new IllegalArgumentException("Mã ngân hàng không hợp lệ");
        // nested or flat MAI structure based on profile
        boolean nestedBank = profile == null ? true : profile.isNestedBankInfo();
        String nested = null;
        if (nestedBank) nested = tlv("00", bankId) + tlv("01", bankAccount);

        var part11 = new StringBuilder();
        part11.append(tlv("00", profile != null ? profile.getGuid() : "A000000727"));
        if (nestedBank) part11.append(tlv("01", nested)); else part11.append(tlv("01", bankAccount));
        part11.append(tlv("02", "QRIBFTTA"));
        String part1 = tlv("38", part11.toString());

        String part21 = tlv("08", message);
        String part2 = tlv("53", "704") + tlv("54", amount) + tlv("58", "VN") + tlv("62", part21);
        String payload = tlv("00", "01") + tlv("01", "12") + part1 + part2;
        String toCrc = payload + "6304";
        String crc = CRC16Util.generate(toCrc);
        payload += tlv("63", crc);
        return payload;
    }

    /**
     * Build QR content that includes additional dynamic fields used for security (transaction id, timestamp, expiry)
     * Additional Data 62 contains several sub-fields: 05 (reference), 06 (timestamp), 07 (expiry), 08 (message)
     */
    public String buildQRContent(String bankCode, String bankAccount, String amount, String message,
                                 String transactionId, long timestampEpochSeconds, long expireEpochSeconds) {
        return buildQRContent(bankCode, bankAccount, amount, message, transactionId, timestampEpochSeconds, expireEpochSeconds, null);
    }

    public String buildQRContent(String bankCode, String bankAccount, String amount, String message,
                                 String transactionId, long timestampEpochSeconds, long expireEpochSeconds, String signatureKeyId) {
        String key = bankCode == null ? null : bankCode.trim().toLowerCase();
        var profile = bankProfiles().get(key);
        String bankId = bankMaps().get(key);
        if (bankId == null) throw new IllegalArgumentException("Mã ngân hàng không hợp lệ");

        // Use class-level makeTlvBytes/writeBytes helpers

    // Build payload bytes using byte-safe TLV helper
    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
    writeBytes(baos, makeTlvBytes("00", "01"));
    writeBytes(baos, makeTlvBytes("01", "12"));

    // Merchant Account Information (MAI). We use tag 38 for VietQR. It has sub-TLVs.
    // Follow VietQR / EMVCo inside MAI we include:
    // 00: GUID, 01: nested TLV or flat account depending on bank profile, 02: service code
    // Build MAI sub-TLVs in bytes
    java.io.ByteArrayOutputStream maiSubBaos = new java.io.ByteArrayOutputStream();
    writeBytes(maiSubBaos, makeTlvBytes("00", profile != null ? profile.getGuid() : "A000000727"));

    boolean nested = profile == null ? true : profile.isNestedBankInfo();
    if (nested) {
        java.io.ByteArrayOutputStream nestedBankBaos = new java.io.ByteArrayOutputStream();
    writeBytes(nestedBankBaos, makeTlvBytes("00", bankId)); // bank identifier
    writeBytes(nestedBankBaos, makeTlvBytes("01", bankAccount)); // account inside nested TLV
    writeBytes(maiSubBaos, makeTlvBytes("01", nestedBankBaos.toByteArray()));
    } else {
        // flat bank info - put the bank account in tag 01 directly
    writeBytes(maiSubBaos, makeTlvBytes("01", bankAccount));
    }
    // subtag 02 for service code
    writeBytes(maiSubBaos, makeTlvBytes("02", "QRIBFTTA"));
    // Build MAI 38 bytes from nested or flat layout
    byte[] maiBytes = makeTlvBytes("38", maiSubBaos.toByteArray());
    writeBytes(baos, maiBytes);

    // Transaction Currency (tag 53) = VND (704)
    writeBytes(baos, makeTlvBytes("53", "704"));

        // Amount (tag 54) - only include the number, without thousands separators or currency symbols
    writeBytes(baos, makeTlvBytes("54", amount));

        // Country Code (tag 58) = VN
    writeBytes(baos, makeTlvBytes("58", "VN"));

        // Merchant Name (59) - optional. Mask part of account to increase privacy if needed
        String merchantName = "Ezami";
    writeBytes(baos, makeTlvBytes("59", merchantName));

        // Merchant City (60) - optional
    writeBytes(baos, makeTlvBytes("60", "HN"));

    // Additional Data Field Template (62) - contains sub-TLVs including reference label (05) for transaction
    java.io.ByteArrayOutputStream addSubBaos = new java.io.ByteArrayOutputStream();
    if (transactionId != null) writeBytes(addSubBaos, makeTlvBytes("05", transactionId));
    writeBytes(addSubBaos, makeTlvBytes("06", String.valueOf(timestampEpochSeconds)));
    writeBytes(addSubBaos, makeTlvBytes("07", String.valueOf(expireEpochSeconds)));
    if (message != null) writeBytes(addSubBaos, makeTlvBytes("08", message));
        // We will compute signature AFTER we finalize the 62 block; signature must reflect the final 62 content.
        // Append a placeholder 62 first: it will be replaced after signature is computed
    // Append 62 without signature now
    writeBytes(baos, makeTlvBytes("62", addSubBaos.toByteArray()));

        // Iteratively compute signature after carefully ensuring 62 with 09+10 fits within 99 bytes.
        String keyId = signatureKeyId == null ? signingService.getCurrentKeyId() : signatureKeyId;
        // Fixpoint loop: compute signature for current payload (without 09/10), attempt to add 09/10, and if the final 62 exceeds 99 bytes, trim message and retry.
        boolean signatureIncluded = false;
    int[] signatureAttempts = new int[]{16, 12, 8, 6, 4};
    for (int attempt = 0; attempt < 10; attempt++) {
            // Build payload where 62 currently is the 'addSub' without signature and CRC
            byte[] payloadBytes = baos.toByteArray();
            int idx62 = findTagIndex(new String(payloadBytes, java.nio.charset.StandardCharsets.UTF_8), "62");
            if (idx62 < 0) break; // shouldn't happen
            int len62 = Integer.parseInt(new String(payloadBytes, idx62 + 2, 2, java.nio.charset.StandardCharsets.UTF_8));
            int end62 = idx62 + 4 + len62;
            byte[] prefix = java.util.Arrays.copyOfRange(payloadBytes, 0, idx62);
            byte[] suffix = java.util.Arrays.copyOfRange(payloadBytes, end62, payloadBytes.length);
            // Compute toSign as everything before CRC tag (63)
            String tmpNoSig = new String(payloadBytes, java.nio.charset.StandardCharsets.UTF_8);
            int idx63 = findTagIndex(tmpNoSig, "63");
            String toSign = idx63 >= 0 ? tmpNoSig.substring(0, idx63) : tmpNoSig;

            // Try different truncated sizes to make signature fit into 62 TLV
            String signature = null;
            int chosenBytes = -1;
            for (int bytesTry : signatureAttempts) {
                String sigTry = signingService.signTruncated(toSign, keyId, bytesTry);
                byte[] sigBytesTry = makeTlvBytes("09", sigTry);
                byte[] keyBytesTry = makeTlvBytes("10", keyId);
                int candidateLenTry = addSubBaos.toByteArray().length + sigBytesTry.length + keyBytesTry.length;
                if (candidateLenTry <= 99) {
                    signature = sigTry;
                    chosenBytes = bytesTry;
                    break;
                }
            }
            if (signature == null) {
                // none fit - fallback to attempt with largest and trim message
                signature = signingService.signTruncated(toSign, keyId, signatureAttempts[0]);
            }
            // build candidate bytes for 62 with signature and key id
            byte[] currentAddBytes = addSubBaos.toByteArray();
            byte[] sigBytes = makeTlvBytes("09", signature);
            byte[] keyBytes2 = makeTlvBytes("10", keyId);
            byte[] candidateBytes = new byte[currentAddBytes.length + sigBytes.length + keyBytes2.length];
            System.arraycopy(currentAddBytes, 0, candidateBytes, 0, currentAddBytes.length);
            System.arraycopy(sigBytes, 0, candidateBytes, currentAddBytes.length, sigBytes.length);
            System.arraycopy(keyBytes2, 0, candidateBytes, currentAddBytes.length + sigBytes.length, keyBytes2.length);
            if (candidateBytes.length <= 99) {
                // fits, assemble final payload
                byte[] new62Bytes = makeTlvBytes("62", candidateBytes);
                baos = new java.io.ByteArrayOutputStream();
                baos.write(prefix, 0, prefix.length);
                baos.write(new62Bytes, 0, new62Bytes.length);
                baos.write(suffix, 0, suffix.length);
                payloadBytes = baos.toByteArray();
                signatureIncluded = true;
                break;
            }
            // Exceeded 99: reduce message 08 first and try again
            var subMap = parseTlv(new String(addSubBaos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8));
            String msg = subMap.getOrDefault("08", "");
            if (msg == null || msg.isEmpty()) {
                // No message left to trim and still exceeds - can't include signature. Keep without signature.
                log.warn("Cannot fit signature and key into 62 even after trimming; skipping signature inclusion");
                signatureIncluded = false;
                break;
            }
            // Determine how many bytes to trim to fit
            int currentLen = addSubBaos.toByteArray().length + sigBytes.length + keyBytes2.length;
            int excess = currentLen - 99;
            if (excess <= 0) excess = 1;
            // Trim 'excess' bytes from message in UTF-8 safe manner
            byte[] rawMsg = msg.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            int newLen = Math.max(0, rawMsg.length - excess);
            if (newLen < rawMsg.length) {
                String truncated = new String(java.util.Arrays.copyOf(rawMsg, newLen), java.nio.charset.StandardCharsets.UTF_8);
                // rebuild addSub with truncated message
                java.io.ByteArrayOutputStream newAddBaos = new java.io.ByteArrayOutputStream();
                if (subMap.containsKey("05")) { writeBytes(newAddBaos, makeTlvBytes("05", subMap.get("05"))); }
                if (subMap.containsKey("06")) { writeBytes(newAddBaos, makeTlvBytes("06", subMap.get("06"))); }
                if (subMap.containsKey("07")) { writeBytes(newAddBaos, makeTlvBytes("07", subMap.get("07"))); }
                // Ensure we preserve at least a minimal snippet so UI can display a readable message (read-only)
                if (!truncated.isEmpty()) {
                    writeBytes(newAddBaos, makeTlvBytes("08", truncated));
                } else {
                    // fallback: if the truncated message becomes empty, preserve a tiny snippet from original message
                    String fallbackSnippet = "";
                    if (message != null && !message.isEmpty()) {
                        // Keep first 4 bytes of the original message as safe UTF-8 substring
                        byte[] rawOrig = message.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                        int snippetLen = Math.min(16, rawOrig.length);
                        // ensure not to break UTF-8 sequence: create using new String with snippetLen
                        fallbackSnippet = new String(java.util.Arrays.copyOf(rawOrig, snippetLen), java.nio.charset.StandardCharsets.UTF_8);
                    }
                    if (!fallbackSnippet.isEmpty()) writeBytes(newAddBaos, makeTlvBytes("08", fallbackSnippet));
                }
                addSubBaos = newAddBaos;
                // replace 62 in payload with truncated version
                byte[] new62Bytes = makeTlvBytes("62", addSubBaos.toByteArray());
                baos = new java.io.ByteArrayOutputStream();
                baos.write(prefix, 0, prefix.length);
                baos.write(new62Bytes, 0, new62Bytes.length);
                baos.write(suffix, 0, suffix.length);
                payloadBytes = baos.toByteArray();
                // continue loop to recompute signature for new truncated message
                continue;
            } else {
                // can't shrink message, break
                log.warn("Unable to reduce 62 size further to include signature");
                signatureIncluded = false;
                break;
            }
        }

    // Compute CRC16 over the payload bytes before CRC tag (i.e., up to and including '63' tag and length '04')
    byte[] beforeCrcBytes = baos.toByteArray();
    String toCrc = new String(beforeCrcBytes, java.nio.charset.StandardCharsets.UTF_8) + "6304";
    String crc = CRC16Util.generate(toCrc);
    writeBytes(baos, makeTlvBytes("63", crc));
    String finalPayload = new String(baos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
    // Debugging: log final payload length and a short snippet to analyze TLV production
    byte[] finalBytes = baos.toByteArray();
    String shortSnip = new String(finalBytes, 0, Math.min(80, finalBytes.length), java.nio.charset.StandardCharsets.UTF_8);
    log.debug("buildQRContent: final bytes len={}, snippet='{}'", finalBytes.length, shortSnip);
    return finalPayload;
    }

    /**
     * Given a string that may contain EMV TLV payload mixed with text or other wrappers,
     * try to find the EMV TLV substring starting with 000201 and ending with CRC tag 63xx.
     * Returns the original string if no TLV detected.
     */
    private String extractEmvTlVFromAnyString(String s) {
        if (s == null) return null;
        byte[] bytes = s.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        // find ASCII "000201" in bytes
        byte[] pattern = "000201".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int start = -1;
        for (int i = 0; i + pattern.length <= bytes.length; i++) {
            boolean match = true;
            for (int j = 0; j < pattern.length; j++) {
                if (bytes[i + j] != pattern[j]) { match = false; break; }
            }
            if (match) { start = i; break; }
        }
        if (start < 0) return s; // no TLV found

        // Parse TLV sequentially starting at 'start' to find the CRC tag 63 at correct TLV boundaries.
        int pos = start;
        while (pos + 4 <= bytes.length) {
            // read tag and length
            String tag;
            try {
                tag = new String(bytes, pos, 2, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception ex) {
                break;
            }
            // attempt to read length
            int len;
            try {
                String lenStr = new String(bytes, pos + 2, 2, java.nio.charset.StandardCharsets.UTF_8);
                len = Integer.parseInt(lenStr);
            } catch (Exception ex) {
                // malformed length; abort parsing
                break;
            }
            pos += 4; // move to value start
            if (pos + len > bytes.length) {
                // truncated value - abort, cannot find CRC safely
                break;
            }
            if ("63".equals(tag)) {
                // Found CRC at a valid TLV boundary
                int end = pos + len;
                return new String(bytes, start, end - start, java.nio.charset.StandardCharsets.UTF_8);
            }
            // skip the value bytes and continue parsing
            pos += len;
        }
        // fallback: return from start to end of string
        return new String(bytes, start, bytes.length - start, java.nio.charset.StandardCharsets.UTF_8);
    }


    private Map<String, String> bankMaps() {
        Map<String, String> map = new HashMap<>();
    map.put("vcb", "970436");
    map.put("vietinbank", "970415");
    map.put("mb", "970422");
    map.put("bidv", "970418");
    map.put("agribank", "970405");
    map.put("ocb", "970448");
    map.put("acb", "970416");
    map.put("vpbank", "970432");
    map.put("tpbank", "970423");
    map.put("hdbank", "970437");
    map.put("vietcapitalbank", "970454");
    map.put("scb", "970429");
    map.put("vib", "970441");
    map.put("shb", "970443");
    map.put("eximbank", "970431");
    map.put("msb", "970426");
    map.put("cake", "546034");
        return map;
    }

    private Map<String, BankProfile> bankProfiles() {
        Map<String, BankProfile> p = new HashMap<>();
        // Most banks use nested structure; MB historically required nested tags.
    p.put("mb", new BankProfile("A000000727", true));
        p.put("vietinbank", new BankProfile("A000000727", true));
        p.put("bidv", new BankProfile("A000000727", true));
        p.put("agribank", new BankProfile("A000000727", true));
        // Default to nested if not present
        return p;
    }
}