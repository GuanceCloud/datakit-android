package com.ft.sdk.sessionreplay.internal.recorder.resources;

import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class MD5HashGenerator implements HashGenerator {
    private static final String TAG = "MD5HashGenerator";
    private final InternalLogger logger;

    public MD5HashGenerator(InternalLogger logger) {
        this.logger = logger;
    }

    @Override
    public String generate(byte[] input) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(input);
            byte[] hashBytes = messageDigest.digest();
            StringBuilder hashString = new StringBuilder();

            for (byte b : hashBytes) {
                hashString.append(String.format(Locale.US, "%02x", b));
            }

            return hashString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.e(TAG, MD5_HASH_GENERATION_ERROR, e);
            return null;
        }
    }

    private static final String MD5_HASH_GENERATION_ERROR = "Cannot generate MD5 hash.";
}
