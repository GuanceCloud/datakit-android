/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.ft.sdk.sessionreplay.internal.utils;


import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    private static final String TAG = "FileUtils";

    private static <T> T safeCall(File file, T defaultValue, InternalLogger internalLogger, FileOperation<T> operation) {
        try {
            return operation.execute(file);
        } catch (SecurityException e) {
            internalLogger.e(TAG, "Security exception was thrown for file " + file.getPath(), e);

            return defaultValue;
        } catch (Exception e) {
            internalLogger.e(TAG, "Unexpected exception was thrown for file " + file.getPath(), e);
            return defaultValue;
        }
    }

    public static boolean canWriteSafe(File file, InternalLogger internalLogger) {
        return safeCall(file, false, internalLogger, File::canWrite);
    }

    public static boolean canReadSafe(File file, InternalLogger internalLogger) {
        return safeCall(file, false, internalLogger, File::canRead);
    }

    public static boolean deleteSafe(File file, InternalLogger internalLogger) {
        return safeCall(file, false, internalLogger, File::delete);
    }

    public static boolean existsSafe(File file, InternalLogger internalLogger) {
        return safeCall(file, false, internalLogger, File::exists);
    }

    public static boolean isFileSafe(File file, InternalLogger internalLogger) {
        return safeCall(file, false, internalLogger, File::isFile);
    }

    public static boolean isDirectorySafe(File file, InternalLogger internalLogger) {
        return safeCall(file, false, internalLogger, File::isDirectory);
    }

    public static File[] listFilesSafe(File file, InternalLogger internalLogger) {
        return safeCall(file, null, internalLogger, File::listFiles);
    }

    public static File[] listFilesSafe(File file, FileFilter filter, InternalLogger internalLogger) {
        return safeCall(file, null, internalLogger, f -> f.listFiles(filter));
    }

    public static long lengthSafe(File file, InternalLogger internalLogger) {
        return safeCall(file, 0L, internalLogger, File::length);
    }

    public static boolean mkdirsSafe(File file, InternalLogger internalLogger) {
        return safeCall(file, false, internalLogger, File::mkdirs);
    }

    public static boolean renameToSafe(File file, File dest, InternalLogger internalLogger) {
        return safeCall(file, false, internalLogger, f -> f.renameTo(dest));
    }

    public static void deleteDirectoryContentsSafe(File file, InternalLogger internalLogger) {
        File[] files = listFilesSafe(file, internalLogger);
        if (files != null) {
            for (File f : files) {
                deleteSafe(f, internalLogger);
            }
        }
    }

    public static String readTextSafe(File file, Charset charset, InternalLogger internalLogger) {
        if (existsSafe(file, internalLogger) && canReadSafe(file, internalLogger)) {
            return safeCall(file, null, internalLogger, f -> {
                StringBuilder text = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        text.append(line).append("\n");
                    }
                } catch (IOException e) {
                    internalLogger.e(TAG, "Failed to read file " + file.getPath(), e);
                }
                return text.toString();
            });
        }
        return null;
    }

    public static byte[] readBytesSafe(File file, InternalLogger internalLogger) {
        if (existsSafe(file, internalLogger) && canReadSafe(file, internalLogger)) {
            return safeCall(file, null, internalLogger, f -> {
                try (FileInputStream inputStream = new FileInputStream(f)) {
                    byte[] bytes = new byte[(int) f.length()];
                    inputStream.read(bytes);
                    return bytes;
                } catch (IOException e) {
                    internalLogger.e(TAG, "Failed to read bytes from file " + file.getPath(), e);
                    return null;
                }
            });
        }
        return null;
    }

    public static List<String> readLinesSafe(File file, Charset charset, InternalLogger internalLogger) {
        if (existsSafe(file, internalLogger) && canReadSafe(file, internalLogger)) {
            return safeCall(file, null, internalLogger, f -> {
                List<String> lines = new ArrayList<>();
                try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        lines.add(line);
                    }
                } catch (IOException e) {
                    internalLogger.e(TAG, "Failed to read lines from file " + file.getPath(), e);
                }
                return lines;
            });
        }
        return null;
    }

    public static void writeTextSafe(File file, String text, Charset charset, InternalLogger internalLogger) {
        if (existsSafe(file, internalLogger) && canWriteSafe(file, internalLogger)) {
            safeCall(file, null, internalLogger, f -> {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
                    writer.write(text);
                } catch (IOException e) {
                    internalLogger.e(TAG, "Failed to write text to file " + file.getPath(), e);
                }
                return null;
            });
        }
    }

    @FunctionalInterface
    private interface FileOperation<T> {
        T execute(File file) throws Exception;
    }
}
