package com.hth.udecareer.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import org.springframework.security.crypto.bcrypt.BCrypt;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PhpPassUtil {
    private static final String ITOA64 = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private final int iterationCountLog2;
    private final SecureRandom randomGen;

    public PhpPassUtil(int iterationCountLog2) {
        if (iterationCountLog2 < 4 || iterationCountLog2 > 31) {
            iterationCountLog2 = 8;
        }
        this.iterationCountLog2 = iterationCountLog2;
        randomGen = new SecureRandom();
    }

    private static String encode64(byte[] src, int count) {
        int i, value;
        final StringBuilder output = new StringBuilder();
        i = 0;

        if (src.length < count) {
            final byte[] t = new byte[count];
            System.arraycopy(src, 0, t, 0, src.length);
            Arrays.fill(t, src.length, count - 1, (byte) 0);
            src = t;
        }

        do {
            value = src[i] + (src[i] < 0 ? 256 : 0);
            ++i;
            output.append(ITOA64.charAt(value & 63));
            if (i < count) {
                value |= (src[i] + (src[i] < 0 ? 256 : 0)) << 8;
            }
            output.append(ITOA64.charAt((value >> 6) & 63));
            if (i++ >= count) {
                break;
            }
            if (i < count) {
                value |= (src[i] + (src[i] < 0 ? 256 : 0)) << 16;
            }
            output.append(ITOA64.charAt((value >> 12) & 63));
            if (i++ >= count) {
                break;
            }
            output.append(ITOA64.charAt((value >> 18) & 63));
        } while (i < count);
        return output.toString();
    }

    private static String cryptPrivate(String password, String setting) {
        String output = "*0";
        if (((setting.length() < 2) ? setting : setting.substring(0, 2)).equalsIgnoreCase(output)) {
            output = "*1";
        }
        final String id = (setting.length() < 3) ? setting : setting.substring(0, 3);
        if (!("$P$".equals(id) || "$H$".equals(id))) {
            return output;
        }
        final int countLog2 = ITOA64.indexOf(setting.charAt(3));
        if (countLog2 < 7 || countLog2 > 30) {
            return output;
        }
        int count = 1 << countLog2;
        final String salt = setting.substring(4, 4 + 8);
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            log.warn(e.getMessage(), e);
            return output;
        }
        final byte[] pass = stringToUtf8(password);
        byte[] hash = md.digest(stringToUtf8(salt + password));
        do {
            final byte[] t = new byte[hash.length + pass.length];
            System.arraycopy(hash, 0, t, 0, hash.length);
            System.arraycopy(pass, 0, t, hash.length, pass.length);
            hash = md.digest(t);
        } while (--count > 0);
        output = setting.substring(0, 12);
        output += encode64(hash, 16);
        return output;
    }

    private String genSaltPrivate(byte[] input) {
        String output = "$P$";
        output += ITOA64.charAt(Math.min(iterationCountLog2 + 5, 30));
        output += encode64(input, 6);
        return output;
    }

    private static byte[] stringToUtf8(String string) {
        return string.getBytes(StandardCharsets.UTF_8);
    }

    public String HashPassword(String password) {
        final byte[] random = new byte[6];
        randomGen.nextBytes(random);
        // Unportable hashes (Blowfish, EXT_DES) could be added here, but I won't do this.
        final String hash = cryptPrivate(password, genSaltPrivate(stringToUtf8(new String(random))));
        if (hash.length() == 34) {
            return hash;
        }
        return "*";
    }

    public boolean CheckPassword(String password, String storedHash) {
        String hash = cryptPrivate(password, storedHash);
        MessageDigest md = null;
        if (hash.startsWith("*")) {    // If not phpass, try some algorythms from unix crypt()
            if (storedHash.startsWith("$6$")) {
                try {
                    md = MessageDigest.getInstance("SHA-512");
                } catch (NoSuchAlgorithmException e) {
                    log.warn(e.getMessage(), e);
                }
            }
            if (md == null && storedHash.startsWith("$5$")) {
                try {
                    md = MessageDigest.getInstance("SHA-256");
                } catch (NoSuchAlgorithmException e) {
                    log.warn(e.getMessage(), e);
                }
            }
            if (md == null && storedHash.startsWith("$2")) {
                return BCrypt.checkpw(password, storedHash);
            }
            if (md == null && storedHash.startsWith("$1$")) {
                try {
                    md = MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException e) {
                    log.warn(e.getMessage(), e);
                }
            }
            // STD_DES and EXT_DES not supported yet.
            if (md != null) {
                hash = new String(md.digest(password.getBytes()));
            }
        }
        return hash.equals(storedHash);
    }
}
