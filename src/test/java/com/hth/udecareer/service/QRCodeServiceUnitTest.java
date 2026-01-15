package com.hth.udecareer.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class QRCodeServiceUnitTest {

    @Test
    public void testExtractTransactionIdFromSimpleTlV() {
    var signingService = Mockito.mock(SigningService.class);
    var s = new QRCodeService(signingService);

        String txid = "txn123";
        String sub = "05" + String.format("%02d", txid.length()) + txid; // 05 + len + value
        String full = "62" + String.format("%02d", sub.length()) + sub;

        String extracted = s.extractTransactionIdFromQrContent(full);
        assertEquals(txid, extracted);
    }

    @Test
    public void testExtractTransactionIdFromImageBase64() throws Exception {
    var signingService = Mockito.mock(SigningService.class);
    var s = new QRCodeService(signingService);

        String txid = "txn456";
        String sub = "05" + String.format("%02d", txid.length()) + txid; // 05 + len + value
        String full = "62" + String.format("%02d", sub.length()) + sub;

        // generate png image from the QR content
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(full, BarcodeFormat.QR_CODE, 200, 200);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", baos);

        String dataUri = "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());

        String extracted = s.extractTransactionIdFromQrContent(dataUri);
        assertEquals(txid, extracted);
    }

    @Test
    public void testExtractTransactionIdFromGeneratedContentWithUtf8() {
    var signingService = Mockito.mock(SigningService.class);
        Mockito.when(signingService.getCurrentKeyId()).thenReturn("testKey");
        Mockito.when(signingService.signTruncated(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn("sig");
        Mockito.when(signingService.signTruncated(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn("sig");
        Mockito.when(signingService.verifyTruncated(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(true);
    var s = new QRCodeService(signingService);

        String txid = "txnUTF8";
        String qr = s.buildQRContent("vcb", "12345678", "100000", "Thanh toán khóa học", txid, System.currentTimeMillis()/1000, System.currentTimeMillis()/1000 + 300);
        String extracted = s.extractTransactionIdFromQrContent(qr);
        org.junit.jupiter.api.Assertions.assertEquals(txid, extracted);
    }

    @Test
    public void testGeneratedContentHasValidCRC() {
    var signingService = Mockito.mock(SigningService.class);
        Mockito.when(signingService.getCurrentKeyId()).thenReturn("testKey");
        Mockito.when(signingService.signTruncated(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn("sig");
    var s = new QRCodeService(signingService);

        String txid = "txnCRC";
        String qr = s.buildQRContent("vcb", "12345678", "100000", "Thanh toán", txid, System.currentTimeMillis()/1000, System.currentTimeMillis()/1000 + 300);
        assertTrue(s.isValidEmvQrContent(qr));
    }

    @Test
    public void testSignatureRoundTrip() {
        byte[] keyBytes = new byte[32];
        new java.util.Random(12345).nextBytes(keyBytes);
        String base64 = Base64.getUrlEncoder().withoutPadding().encodeToString(keyBytes);
        var signingService = new SigningService(base64, "testKey");
        String payload = "000201010212";
        String signature = signingService.signTruncated(payload, "testKey", 16);
        assertTrue(signingService.verifyTruncated(payload, signature, "testKey", 16));
    }

    @Test
    public void testBankCodeNormalization() {
    var signingService = Mockito.mock(SigningService.class);
        Mockito.when(signingService.getCurrentKeyId()).thenReturn("testKey");
        Mockito.when(signingService.signTruncated(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn("sig");
    var s = new QRCodeService(signingService);

        String txid1 = "txnNorm1";
        String qr1 = s.buildQRContent("MB", "12345678", "100000", "Test", txid1, System.currentTimeMillis()/1000, System.currentTimeMillis()/1000 + 300);
        String txid2 = "txnNorm2";
        String qr2 = s.buildQRContent("mb", "12345678", "100000", "Test", txid2, System.currentTimeMillis()/1000, System.currentTimeMillis()/1000 + 300);
        assertNotNull(qr1);
        assertNotNull(qr2);
        assertTrue(s.extractTransactionIdFromQrContent(qr1).equals(txid1));
        assertTrue(s.extractTransactionIdFromQrContent(qr2).equals(txid2));
    }

    @Test
    public void testExtractTransactionIdFromCorrupted62Length() {
    var signingService = Mockito.mock(SigningService.class);
    var s = new QRCodeService(signingService);

        // Build a TLV string where 62 has incorrect length so top-level parser will break
        String txid = "txnCorrupt";
        String sub = "05" + String.format("%02d", txid.length()) + txid; // proper sub TLV
        // But we put wrong length for 62: claim 02 but actually longer
        String broken62 = "6202" + sub; // length 02 is wrong, but contains '05' pattern
        String full = "000201010212" + broken62 + "6304FFFF";

        String extracted = s.extractTransactionIdFromQrContent(full);
        assertEquals(txid, extracted);
    }

    @Test
    public void testPreserveMessageSnippetOnTrim() {
        var signingService = Mockito.mock(SigningService.class);
        // Simulate a very large signature that won't fit so message must be trimmed
        String longSig = new String(new char[120]).replace('\0', 'A'); // 120 chars
        Mockito.when(signingService.getCurrentKeyId()).thenReturn("testKey");
        Mockito.when(signingService.signTruncated(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(longSig);
    var s = new QRCodeService(signingService);

        String txid = "txnSnippet";
        String longMessage = new String(new char[400]).replace('\0', 'M');
        String qr = s.buildQRContent("vcb", "12345678", "100000", longMessage, txid, System.currentTimeMillis()/1000, System.currentTimeMillis()/1000 + 300);
        var top = s.parseTlv(qr);
        assertTrue(top.containsKey("62"));
        var sub = s.parseTlv(top.get("62"));
        assertTrue(sub.containsKey("08"));
        String snippet = sub.get("08");
        assertNotNull(snippet);
        assertFalse(snippet.isEmpty(), "Expected fallback snippet not empty");
        // Ensure the snippet is a prefix of the original message
        assertTrue(longMessage.startsWith(snippet));
    }

    @Test
    public void testDeclaredTlvLengthMatchesActualBytes() {
        var signingService = Mockito.mock(SigningService.class);
        Mockito.when(signingService.getCurrentKeyId()).thenReturn("testKey");
        Mockito.when(signingService.signTruncated(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn("sig");
    var s = new QRCodeService(signingService);

        String txid = "txnLenTest";
        String qr = s.buildQRContent("vcb", "12345678", "100000", "Thanh toán", txid, System.currentTimeMillis()/1000, System.currentTimeMillis()/1000 + 300);
        assertNotNull(qr);
        // Find 62 position and declared length
        // Find 62 via a raw byte search to avoid depending on private helpers
        byte[] bytes = qr.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int idx62 = -1;
        for (int i = 0; i + 2 <= bytes.length; i++) {
            if (bytes[i] == '6' && bytes[i+1] == '2') { idx62 = i; break; }
        }
        assertTrue(idx62 >= 0, "62 tag should be present");
        int declaredLen = Integer.parseInt(new String(bytes, idx62 + 2, 2, java.nio.charset.StandardCharsets.UTF_8));
        // Ensure declared length is within bounds and that parseTlv gives us a value of that length
        assertTrue(declaredLen >= 0 && (idx62 + 4 + declaredLen) <= bytes.length, "Declared length should fit within payload bytes");
        String raw62 = new String(bytes, idx62 + 4, declaredLen, java.nio.charset.StandardCharsets.UTF_8);
        var parsedTop = s.parseTlv(qr);
        assertTrue(parsedTop.containsKey("62"));
        assertEquals(raw62, parsedTop.get("62"));
    }

    @Test
    public void testDeclaredTlvLengthMatchesActualBytes_tag38() {
        var signingService = Mockito.mock(SigningService.class);
        Mockito.when(signingService.getCurrentKeyId()).thenReturn("testKey");
        Mockito.when(signingService.signTruncated(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn("sig");
    var s = new QRCodeService(signingService);

        String txid = "txnLen38Test";
        String qr = s.buildQRContent("vcb", "12345678", "100000", "Thanh toán", txid, System.currentTimeMillis()/1000, System.currentTimeMillis()/1000 + 300);
        assertNotNull(qr);
        byte[] bytes = qr.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int idx38 = -1;
        for (int i = 0; i + 2 <= bytes.length; i++) {
            if (bytes[i] == '3' && bytes[i+1] == '8') { idx38 = i; break; }
        }
        assertTrue(idx38 >= 0, "38 tag should be present");
        int declaredLen = Integer.parseInt(new String(bytes, idx38 + 2, 2, java.nio.charset.StandardCharsets.UTF_8));
        assertTrue(declaredLen >= 0 && (idx38 + 4 + declaredLen) <= bytes.length, "Declared length for 38 should fit within payload bytes");
        String raw38 = new String(bytes, idx38 + 4, declaredLen, java.nio.charset.StandardCharsets.UTF_8);
        var parsedTop = s.parseTlv(qr);
        // If parse failed due to malformed 38, parseTlv won't contain 38; but raw bytes still show it must fit
        if (parsedTop.containsKey("38")) assertEquals(raw38, parsedTop.get("38"));
        // Ensure we can extract full TLV using the extractor (no early '63' mis-detection)
    String extracted = s.sanitizeQrContent(qr);
        assertEquals(qr, extracted, "extractEmvTlVFromAnyString should return the full TLV payload for a properly built QR");
    }
}
