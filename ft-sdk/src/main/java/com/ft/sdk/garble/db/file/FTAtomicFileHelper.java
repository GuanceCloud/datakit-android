package com.ft.sdk.garble.db.file;

import android.util.AtomicFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Atomic file helpers used by the file-backed data store.
 */
public class FTAtomicFileHelper {
    private static final int BUFFER_SIZE = 8 * 1024;

    private FTAtomicFileHelper() {
    }

    public static void writeUtf8(File target, String content) throws IOException {
        byte[] bytes = content == null ? new byte[0] : content.getBytes(StandardCharsets.UTF_8);
        writeBytes(target, bytes);
    }

    public static String readUtf8(File target) throws IOException {
        return new String(readBytes(target), StandardCharsets.UTF_8);
    }

    public static void writeBytes(File target, byte[] data) throws IOException {
        ensureParent(target);
        AtomicFile atomicFile = new AtomicFile(target);
        FileOutputStream stream = null;
        try {
            stream = atomicFile.startWrite();
            stream.write(data == null ? new byte[0] : data);
            stream.getFD().sync();
            atomicFile.finishWrite(stream);
        } catch (IOException e) {
            if (stream != null) {
                atomicFile.failWrite(stream);
            }
            throw e;
        }
    }

    public static byte[] readBytes(File target) throws IOException {
        AtomicFile atomicFile = new AtomicFile(target);
        try (FileInputStream stream = atomicFile.openRead();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        }
    }

    private static void ensureParent(File target) throws IOException {
        File parent = target.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.isDirectory()) {
            throw new IOException("Failed to create file directory: " + parent.getAbsolutePath());
        }
    }
}
