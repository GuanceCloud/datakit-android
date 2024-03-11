package com.ft.sdk.garble.utils;

/**
 * @author Brandon
 */
public class StringUtils {


    /**
     * 删除最后的逗号
     *
     * @param sb
     */
    public static void deleteLastCharacter(StringBuilder sb, String character) {
        if (sb == null) {
            return;
        }
        int index = sb.lastIndexOf(character);
        if (index > 0 && index == sb.length() - 1) {
            sb.deleteCharAt(sb.length() - 1);
        }
    }


    /***
     * 对数据进行脱敏
     * @param str
     * @return
     */
    public static String maskHalfCharacter(String str) {
        StringBuilder sb = new StringBuilder();
        int length = str.length();
        for (int i = 0; i < length; i++) {
            if (i > length / 2) {
                sb.append(str.charAt(i));
            } else {
                sb.append("*");
            }
        }
        return sb.toString();
    }

    /**
     * 获取堆栈字符，自动换行
     * @param stackTrace
     * @return
     */
    public static String getStringFromStackTraceElement(StackTraceElement[] stackTrace) {

        StringBuilder stackTraceString = new StringBuilder();
        // 遍历堆栈跟踪信息
        for (StackTraceElement stackTraceElement : stackTrace) {
            // 获取类名
            String className = stackTraceElement.getClassName();

            // 获取方法名
            String methodName = stackTraceElement.getMethodName();

            // 获取行号
            int lineNumber = stackTraceElement.getLineNumber();

            // 获取文件名
            String fileName = stackTraceElement.getFileName();

            // 组合信息
            String stackTraceInfo = String.format("%s.%s(%s:%d)", className, methodName, fileName, lineNumber);

            // 打印信息
            stackTraceString.append(stackTraceInfo).append("\n");
        }

        return stackTraceString.toString();
    }
}
