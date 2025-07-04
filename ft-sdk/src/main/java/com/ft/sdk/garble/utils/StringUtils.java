package com.ft.sdk.garble.utils;

import java.util.Map;

/**
 * @author Brandon
 */
public class StringUtils {


    /**
     * Delete the last comma
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
     * Desensitize data
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
     * Get stack trace string, auto line break
     *
     * @param stackTrace
     * @return
     */
    public static String getStringFromStackTraceElement(StackTraceElement[] stackTrace) {

        StringBuilder stackTraceString = new StringBuilder();
        // Iterate through stack trace information
        for (StackTraceElement stackTraceElement : stackTrace) {
            // Get class name
            String className = stackTraceElement.getClassName();

            // Get method name
            String methodName = stackTraceElement.getMethodName();

            // Get line number
            int lineNumber = stackTraceElement.getLineNumber();

            // Get file name
            String fileName = stackTraceElement.getFileName();

            // Combine information
            String stackTraceInfo = String.format("%s.%s(%s:%d)", className, methodName, fileName, lineNumber);

            // Print information
            stackTraceString.append(stackTraceInfo).append("\n");
        }

        return stackTraceString.toString();
    }


    /**
     * Convert Map<Thread, StackTraceElement[]> stack trace
     *
     * @param allStackTraces
     * @return
     */
    public static String getThreadAllStackTrace(Map<Thread, StackTraceElement[]> allStackTraces) {
        StringBuilder stack = new StringBuilder();
        // Iterate through Map and print stack trace information for each thread
        for (Map.Entry<Thread, StackTraceElement[]> entry : allStackTraces.entrySet()) {
            Thread thread = entry.getKey();
            StackTraceElement[] stackTraceElements = entry.getValue();
            stack.append(thread.getId()).append("-")
                    .append(thread.getName())
                    .append(" prio:").append(thread.getPriority())
                    .append(" tg:").append(thread.getThreadGroup())
                    .append(" stat:").append(thread.getState())
                    .append("\n")
                    .append(StringUtils.getStringFromStackTraceElement(stackTraceElements))
                    .append("\n");
        }
        return stack.toString();

    }

}
