package com.ft.sdk.internal.anr.historical;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

final class ExitKeyFactory {
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private ExitKeyFactory() {
    }

    static String create(String packageName, ProcessExitRecord exit) {
        String canonical = "v1\0"
                + packageName + "\0"
                + exit.getProcessName() + "\0"
                + exit.getPid() + "\0"
                + exit.getTimestampMs() + "\0"
                + exit.getReason();
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8));
            char[] encoded = new char[digest.length * 2];
            for (int i = 0; i < digest.length; i++) {
                int value = digest[i] & 0xff;
                encoded[i * 2] = HEX[value >>> 4];
                encoded[i * 2 + 1] = HEX[value & 0x0f];
            }
            return new String(encoded);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
