package com.ft.plugin.garble;

/**
 * create: by huangDianHua
 * time: 2020/3/19 18:35:26
 * description:
 */
public class VersionUtils {

    /**
     * 比较版本号大小
     *
     * @param firstVer
     * @param secondVer
     * @return
     */
    public static boolean firstVerGreaterEqual(String firstVer, String secondVer) {
        String[] firstVerArr = firstVer.split("-");
        String[] secondVerArr = secondVer.split("-");
        if (firstVerArr.length == 1 && firstVerArr.length == secondVerArr.length) {
            return firstVerArr[0].hashCode() >= secondVerArr[0].hashCode();
        } else if (firstVerArr.length == 2 && firstVerArr.length == secondVerArr.length) {
            if (firstVerArr[0].hashCode() > secondVerArr[0].hashCode()) {
                return true;
            } else if (firstVerArr[0].hashCode() == secondVerArr[0].hashCode()) {
                return firstVerArr[1].hashCode() >= secondVerArr[1].hashCode();
            } else {
                return false;
            }
        } else if (firstVerArr.length == 1 && secondVerArr.length == 2) {
            if (firstVerArr[0].hashCode() > secondVerArr[0].hashCode()) {
                return true;
            } else if (firstVerArr[0].hashCode() == secondVerArr[0].hashCode()) {
                return true;
            } else {
                return false;
            }
        } else if (firstVerArr.length == 2 && secondVerArr.length == 1) {
            if (firstVerArr[0].hashCode() > secondVerArr[0].hashCode()) {
                return true;
            } else if (firstVerArr[0].hashCode() == secondVerArr[0].hashCode()) {
                return false;
            } else {
                return false;
            }
        }
        return true;
    }
}
