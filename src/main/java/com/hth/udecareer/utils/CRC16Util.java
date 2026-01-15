package com.hth.udecareer.utils;

public class CRC16Util {
    public static String generate(String input) {
        int crc = 0xFFFF;
        int polynomial = 0x1021;
        byte[] bytes = input.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        for (byte b : bytes) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) crc = ((crc << 1) ^ polynomial) & 0xFFFF;
                else crc = (crc << 1) & 0xFFFF;
            }
        }
        return String.format("%04X", crc & 0xFFFF);
    }
}
