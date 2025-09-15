package com.ft.plugin.garble;

import org.gradle.api.Project;

/**
 * BY huangDianHua
 * DATE:2019-12-03 13:41
 * Description: Logger using Gradle project.getLogger for log output
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
     * Gradle logger instance
     */
    private static org.gradle.api.logging.Logger logger;

    /**
     * Initialize Gradle logger
     *
     * @param project Gradle Project instance
     */
    public static void init(Project project) {
        logger = project.getLogger();
    }

    public static void setDebug(boolean debug) {
        Logger.debug = debug;
        if (logger != null) {
            logger.info(TAG + "setDebug:" + debug);
        } else {
            System.out.println(TAG + "setDebug:" + debug);
        }
    }

    /**
     * debug level log
     *
     * @param message
     */
    public static void debug(Object message) {
        if (debug) {
            if (logger != null) {
                logger.debug(TAG + message);
            } else {
                System.out.println(TAG + message);
            }
        }
    }

    /**
     * info level log
     *
     * @param message
     */
    public static void info(Object message) {
        if (logger != null) {
            logger.info(TAG + message);
        } else {
            System.out.println(TAG + message);
        }
    }

    /**
     * warn level log
     *
     * @param message
     */
    public static void warn(Object message) {
        if (logger != null) {
            logger.warn(TAG + message);
        } else {
            System.out.println(TAG + message);
        }
    }

    /**
     * error level log
     *
     * @param message
     */
    public static void error(Object message) {
        if (logger != null) {
            logger.error(TAG + message);
        } else {
            System.err.println(TAG + message);
        }
    }

    /**
     * error level log with exception
     *
     * @param message
     * @param throwable
     */
    public static void error(Object message, Throwable throwable) {
        if (logger != null) {
            logger.error(TAG + message, throwable);
        } else {
            System.err.println(TAG + message);
            if (throwable != null) {
                throwable.printStackTrace();
            }
        }
    }
}
