package com.ft.sdk.garble.utils;

import java.security.SecureRandom;

/**
 * Generate pkgId for trace link tracking
 */
public class PackageIdGenerator {
    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int NANO_DIGIT = 12;

    /**
     * Base62 encoding
     */
    private static String encodeBase62(long value) {
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(BASE62.charAt((int) (value % 62)));
            value /= 62;
        }
        return sb.reverse().toString();
    }

    /**
     * Generate nanoId with corresponding number of digits
     * @return
     */
    private static String generateNanoId() {
        StringBuilder sb = new StringBuilder(NANO_DIGIT);
        for (int i = 0; i < NANO_DIGIT; i++) {
            sb.append(BASE62.charAt(RANDOM.nextInt(BASE62.length())));
        }
        return sb.toString();
    }

    /**
     * Generate packageId [base36].[base62(pid)].[base62(12_digit_number)]
     * @param numberPart
     * @param pid process id
     * @param pkgCount
     * @return
     */
    public static String generatePackageId(String numberPart, int pid, Object pkgCount) {
        String nanoPart = generateNanoId();
        String pidPart = encodeBase62(pid);
        return numberPart + "." + pidPart + "." + pkgCount + "." + nanoPart;
    }
}