package com.ft.sdk.garble.utils;

/**
 * create: by huangDianHua
 * time: 2020/3/19 18:35:26
 * description: Used for comparing Native SDK, Agent SDK version numbers, beta, alpha, and official version numbers are also applicable
 */
public class VersionUtils {
    /*public static void main(String[] args) {
        System.out.println("1.0.1>=1.0.1 result="+firstVerGreaterEqual("1.0.1","1.0.1"));
        System.out.println("1.0.01>=1.0.1 result="+firstVerGreaterEqual("1.0.1","1.0.1"));
        System.out.println("1.1.01>=1.0.1 result="+firstVerGreaterEqual("1.1.01","1.0.1"));
        System.out.println("1.01.01>=1.2.1 result="+firstVerGreaterEqual("1.01.01","1.2.1"));
        System.out.println("2.01.01>=1.2.1 result="+firstVerGreaterEqual("2.01.01","1.2.1"));
        System.out.println("1.1.2-alpha1>=1.1.2-alpha1 result="+firstVerGreaterEqual("1.1.2-alpha1","1.1.2-alpha1"));
        System.out.println("1.1.2-alpha01>=1.1.2-alpha1 result="+firstVerGreaterEqual("1.1.2-alpha01","1.1.2-alpha1"));
        System.out.println("1.1.2-alpha04>=1.1.2-alpha1 result="+firstVerGreaterEqual("1.1.2-alpha04","1.1.2-alpha1"));
        System.out.println("1.1.2-alpha01>=1.1.2-alpha04 result="+firstVerGreaterEqual("1.1.2-alpha01","1.1.2-alpha04"));
        System.out.println("1.1.2-beta01>=1.1.2-alpha04 result="+firstVerGreaterEqual("1.1.2-beta01","1.1.2-alpha04"));
        System.out.println("1.1.2-beta04>=1.1.2-alpha04 result="+firstVerGreaterEqual("1.1.2-beta04","1.1.2-alpha04"));
        System.out.println("1.1.2-beta04>=1.1.2-beta05 result="+firstVerGreaterEqual("1.1.2-beta04","1.1.2-beta05"));
    }*/

    /**
     * Version format 1.0.2, 1.3.55
     *
     * @param first
     * @param second
     * @return
     */
    private static int compareVersion(String first, String second) {
        String[] splitFirst = first.split("\\.");
        String[] splitSecond = second.split("\\.");
        int maxLength = Math.max(splitFirst.length, splitSecond.length);
        String str1, str2;
        for (int index = 0; index < maxLength; index++) {
            if (splitFirst.length > index) {
                str1 = splitFirst[index];
            } else {
                return -1;
            }
            if (splitSecond.length > index) {
                str2 = splitSecond[index];
            } else {
                return 1;
            }
            if (str1 != null && str2 != null) {
                try {
                    int num1 = Integer.parseInt(str1);
                    int num2 = Integer.parseInt(str2);
                    if (num1 != num2) {
                        return num1 - num2 > 0 ? 1 : -1;
                    }
                } catch (Exception ignored) {
                    return 0;
                }
            }
        }
        return 0;
    }

    /**
     * Compare version number size
     *
     * @param firstVer
     * @param secondVer
     * @return
     */
    public static boolean firstVerGreaterEqual(String firstVer, String secondVer) {
        if (Utils.isNullOrEmpty(firstVer) || Utils.isNullOrEmpty(secondVer)) return false;

        String[] firstVerArr = firstVer.replace("-SNAPSHOT", "").split("-");
        String[] secondVerArr = secondVer.replace("-SNAPSHOT", "").split("-");
        if (firstVerArr.length == 1 && firstVerArr.length == secondVerArr.length) {
            return compareVersion(firstVerArr[0], secondVerArr[0]) >= 0;
        } else if (firstVerArr.length == 2 && firstVerArr.length == secondVerArr.length) {
            int result = compareVersion(firstVerArr[0], secondVerArr[0]);
            if (result > 0) {
                return true;
            } else if (result == 0) {
                String first = firstVerArr[1];
                String second = secondVerArr[1];
                if (first.charAt(0) > second.charAt(0)) {
                    return true;
                } else if (first.charAt(0) == second.charAt(0)) {
                    String firstEndNum = first.replace("alpha", "").replace("beta", "");
                    String secondEndNum = second.replace("alpha", "").replace("beta", "");
                    return compareVersion(firstEndNum, secondEndNum) >= 0;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else if (firstVerArr.length == 1 && secondVerArr.length == 2) {
            int result = compareVersion(firstVerArr[0], secondVerArr[0]);
            if (result >= 0) {
                return true;
            } else {
                return false;
            }
        } else if (firstVerArr.length == 2 && secondVerArr.length == 1) {
            int result = compareVersion(firstVerArr[0], secondVerArr[0]);
            if (result > 0) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }
}
