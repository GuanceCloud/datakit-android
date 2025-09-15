package com.ft.plugin.garble;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * BY huangDianHua
 * DATE:2019-12-03 13:41
 * Description: Logger with file output support
 */
public class Logger {
    /**
     * Log output prefix
     */
    private static final String TAG = "[FT-Plugin]:";
    /**
     * Set debug. When enabled, logs will be output during the build process
     */
    private static boolean debug = true;

    /**
     * Log file writer
     */
    private static PrintWriter fileWriter;

    /**
     * Whether file logging is enabled
     */
    private static boolean fileLogEnabled = false;

    /**
     * Date formatter for log timestamps
     */
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


    /**
     * Initialize logger with file output
     *
     * @param logFile Log file path
     */
    public static void init(String logFile) {
        fileLogEnabled = true;
        initFileWriter(logFile);
    }

    /**
     * Initialize file writer
     *
     * @param logFile Log file path
     */
    private static void initFileWriter(String logFile) {
        initFileWriter(new File(logFile));
    }

    /**
     * Initialize file writer
     *
     * @param logFile Log file
     */
    private static void initFileWriter(File logFile) {
        try {
            // Create parent directories if they don't exist
            File parentDir = logFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            fileWriter = new PrintWriter(new FileWriter(logFile, true));
            System.out.println(TAG + "Logger initialized with file output: " + logFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println(TAG + "Failed to initialize file writer: " + e.getMessage());
            fileLogEnabled = false;
            fileWriter = null;
        }
    }

    /**
     * Close file writer
     */
    public static void close() {
        if (fileWriter != null) {
            fileWriter.close();
            fileWriter = null;
        }
        fileLogEnabled = false;
    }

    /**
     * Check if file logging is enabled
     *
     * @return true if file logging is enabled and fileWriter is available
     */
    public static boolean isFileLogEnabled() {
        return fileLogEnabled && fileWriter != null;
    }

    /**
     * Check if file logging is configured (regardless of fileWriter status)
     *
     * @return true if file logging is configured
     */
    public static boolean isFileLogConfigured() {
        return fileLogEnabled;
    }

    public static void setDebug(boolean debug) {
        Logger.debug = debug;
        String logMessage = TAG + "setDebug:" + debug;
        writeLog("INFO", logMessage);
        System.out.println(logMessage);
    }

    /**
     * debug level log
     *
     * @param message
     */
    public static void debug(Object message) {
        if (debug) {
            String logMessage = TAG + message;
            writeLog("DEBUG", logMessage);
            System.out.println(logMessage);
        }
    }

    /**
     * info level log
     *
     * @param message
     */
    public static void info(Object message) {
        String logMessage = TAG + message;
        writeLog("INFO", logMessage);
        System.out.println(logMessage);
    }

    /**
     * warn level log
     *
     * @param message
     */
    public static void warn(Object message) {
        String logMessage = TAG + message;
        writeLog("WARN", logMessage);
        System.out.println(logMessage);
    }

    /**
     * error level log
     *
     * @param message
     */
    public static void error(Object message) {
        String logMessage = TAG + message;
        writeLog("ERROR", logMessage);
        System.err.println(logMessage);
    }

    /**
     * error level log with exception
     *
     * @param message
     * @param throwable
     */
    public static void error(Object message, Throwable throwable) {
        String logMessage = TAG + message;
        writeLog("ERROR", logMessage, throwable);
        System.err.println(logMessage);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }

    /**
     * Write log to file
     *
     * @param level   Log level
     * @param message Log message
     */
    private static void writeLog(String level, String message) {
        writeLog(level, message, null);
    }

    /**
     * Write log to file
     *
     * @param level     Log level
     * @param message   Log message
     * @param throwable Exception (optional)
     */
    private static void writeLog(String level, String message, Throwable throwable) {
        if (fileLogEnabled && fileWriter != null) {
            try {
                String timestamp = dateFormat.format(new Date());
                String logEntry = String.format("[%s] %s %s", timestamp, level, message);
                fileWriter.println(logEntry);

                if (throwable != null) {
                    throwable.printStackTrace(fileWriter);
                }

                fileWriter.flush();
            } catch (Exception e) {
                // If file writing fails, don't crash the application
                System.err.println(TAG + "Failed to write to log file: " + e.getMessage());
                // Disable file logging if it fails
                fileLogEnabled = false;
            }
        }
    }
}
