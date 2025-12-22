package com.ft.sdk.garble.utils;

import static com.ft.sdk.garble.utils.Constants.FT_SHARE_PER_FILE;
import static com.ft.sdk.garble.utils.Constants.TAGS;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Base64;

import com.ft.sdk.FTApplication;
import com.ft.sdk.garble.bean.ResourceID;
import com.ft.sdk.garble.manager.SingletonGson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * BY huangDianHua
 * DATE:2019-11-29 17:54
 * Description:
 */
public class Utils {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "Utils";

    /**
     * Character check, check if string is not empty
     *
     * @param str
     * @return
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Check permission
     *
     * @param context
     * @param permission
     * @return
     */
    public static boolean hasPermission(Context context, String permission) {
        try {
            Class<?> contextCompat = null;
            try {
                contextCompat = Class.forName("android.support.v4.content.ContextCompat");
            } catch (Exception e) {
                //ignored
            }

            if (contextCompat == null) {
                try {
                    contextCompat = Class.forName("androidx.core.content.ContextCompat");
                } catch (Exception e) {
                    //ignored
                }
            }

            if (contextCompat == null) {
                return true;
            }

            Method checkSelfPermissionMethod = contextCompat.getMethod("checkSelfPermission", Context.class, String.class);
            int result = (int) checkSelfPermissionMethod.invoke(null, new Object[]{context, permission});
            return result == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            return true;
        }
    }

//    /**
//     * Check if network is connected
//     *
//     * @return
//     */
//    public static boolean isNetworkAvailable() {
//        return isNetworkAvailable(FTApplication.getApplication());
//    }
//
//    /**
//     * Check if network is connected
//     *
//     * @return
//     */
//    public static boolean isNetworkAvailable(Context context) {
//        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetWork = manager.getActiveNetworkInfo();
//        return activeNetWork != null && activeNetWork.isConnected();
//    }

    /**
     * Get app name from AndroidManifest.xml application.labelName
     *
     * @return
     */
    public static String getAppVersionName() {
        PackageManager manager = FTApplication.getApplication().getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(FTApplication.getApplication().getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
            return "";
        }
    }

    /**
     * Get SDK cache {@link SharedPreferences}
     *
     * @param context
     * @return
     */
    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(FT_SHARE_PER_FILE, Context.MODE_PRIVATE);
    }

    /**
     * Get 16-character length GUID
     *
     * @return
     */
    public static String getGUID_16() {
        return randomUUID().substring(0, 16);
    }

    /**
     * Get uuid
     *
     * @return
     */
    public static String randomUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Get all-zero uuid
     *
     * @return
     */
    public static String getEmptyUUID() {
        return new UUID(0, 0).toString().replace("-", "");
    }

    /**
     * Get 64-bit random number
     *
     * @return
     */
    public static BigInteger getDDtraceNewId() {
        return new BigInteger(64, new SecureRandom());
    }

    /**
     * base64 encode string
     *
     * @param origin
     * @return
     */
    public static String encodeStringToBase64(String origin) {
        return Base64.encodeToString(origin.getBytes(UTF_8), Base64.NO_WRAP);
    }


    /**
     * For tag keys, tag values, and field keys always use a backslash character \ to escape:
     *
     * @return
     */
    public static String translateTagKeyValue(String oldStr) {
        oldStr = oldStr.replace("\n", " ");
        oldStr = oldStr.replace("\\", "\\\\");
        oldStr = translateSpecialCharacters(",", oldStr);
        oldStr = translateSpecialCharacters("=", oldStr);
        return translateSpecialCharacters(" ", oldStr);
    }

    /**
     * For measurements always use a backslash character \ to escape:
     *
     * @return
     */
    public static String translateMeasurements(String oldStr) {
        oldStr = oldStr.replace("\\", "\\\\");
        oldStr = translateSpecialCharacters(",", oldStr);
        return translateSpecialCharacters(" ", oldStr);
    }

    /**
     * For string field values use a backslash character \ to escape:
     */
    public static String translateFieldValue(String oldStr) {
        if (Utils.isJSONValid(oldStr)) {
            return JSONObject.quote(oldStr);
        } else {
            oldStr = oldStr.replace("\\", "\\\\");
            oldStr = translateSpecialCharacters("\"", oldStr);
            return "\"" + oldStr + "\"";
        }
    }

    /**
     * Escape special characters
     *
     * @param special
     * @param oldStr
     */
    public static String translateSpecialCharacters(String special, String oldStr) {
        if (oldStr.contains(special)) {
            return oldStr.replaceAll(special, "\\\\" + special);
        }
        return oldStr;
    }

    /**
     * Round double to 2 decimal places
     *
     * @param value
     * @return
     */
    public static double formatDouble(double value) {
        return (double) Math.round(value * 100) / 100;
    }


    private static final ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault());
        }
    };

    /**
     * Log date, format yyyy-MM-dd HH:mm:ss:SSS
     *
     * @return
     */
    public static String getCurrentTimeStamp() {
        Date currentTime = new Date();
        SimpleDateFormat sdf = dateFormatThreadLocal.get();
        if (sdf != null) {
            return sdf.format(currentTime);
        }
        return "";
    }

    /**
     * Random number less than sampling rate, can be sampled
     *
     * @return
     */
    public static boolean enableTraceSamplingRate(float sampleRate) {
        if (sampleRate <= 0) {
            return false;
        } else if (sampleRate >= 1) {
            return true;
        }
        return generateRandomNumber() <= sampleRate * 100;
    }

    /**
     * Generate random number for sampling
     *
     * @return
     */
    public static float generateRandomNumber() {
        Random random = new Random();
        return random.nextFloat() * 100;
    }

    /**
     * Determine whether the current process is the main process
     *
     * @return
     */
    public static boolean isMainProcess() {
        Context context = FTApplication.getApplication();
        String currentProcessName = getCurrentProcessName();
        String packageName = context.getPackageName();
        return !TextUtils.isEmpty(packageName) && TextUtils.equals(packageName, currentProcessName);
    }

    /**
     * Get current process name
     *
     * @return
     */
    public static String getCurrentProcessName() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return Application.getProcessName();
        } else {
            Context context = FTApplication.getApplication();
            int myPid = Process.myPid();
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> processes = manager.getRunningAppProcesses();
            if (processes == null || processes.isEmpty()) return null;
            for (ActivityManager.RunningAppProcessInfo info : processes) {
                if (myPid == info.pid) {
                    return info.processName;
                }
            }
        }
        return null;
    }


    /**
     * File reading
     *
     * @param path
     * @param encoding
     * @return
     */
    public static String readFile(String path, Charset encoding) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path);
            int length = fis.available();
            byte[] bytes = new byte[length];
            fis.read(bytes);
            return new String(bytes, encoding);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    LogUtils.e(TAGS, "Error closing FileInputStream: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Delete file, if it's a folder, traverse and delete, only do one-level folder traversal
     * <p>
     * Used for test cases and cleanup work after Native Crash file upload is completed
     *
     * @param path
     * @return
     */
    public static boolean deleteFile(String path) {
        File folder = new File(path);
        if (folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                file.delete();
            }
        }
        return folder.delete();
    }


    /**
     * Whether it is json
     *
     * @param json
     * @return
     */
    public static boolean isJSONValid(String json) {
        try {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(json);
            return element.isJsonObject() || element.isJsonArray();
        } catch (JsonParseException e) {
            return false;
        }
    }

    /**
     * Read application
     *
     * @param filePath
     * @param key
     * @return
     */
    public static String readSectionValueFromDump(String filePath, String key) {
        File file = new File(filePath);
        String value = "";

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(key)) {
                    value = br.readLine();

                    break;
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAGS, LogUtils.getStackTraceString(e));
        }
        return value;
    }

    /**
     * Get nanosecond time
     * <p>
     * Getting twice in the same millisecond trigger may cause time reversal issues.
     * When getting at millisecond boundaries, System.nanoTime() may encounter the end of a nanosecond cycle,
     * and the later obtained time may be smaller.
     * Currently using this method is a compromise between performance and accuracy
     *
     * @return
     */
    public static long getCurrentNanoTime() {
        return System.currentTimeMillis() * 1000000L + System.nanoTime() % 1000000L;
    }

    /**
     * Generate relatively unique resourceId
     *
     * @param request
     * @return
     */

    public static String identifyRequest(Request request) {
        ResourceID resourceID = request.tag(ResourceID.class);
        if (resourceID != null) {
            return resourceID.getUuid();
        }
        String method = request.method();
        String url = request.url().toString();
        RequestBody body = request.body();
        if (body == null) {
            return method + "_" + url;
        } else {
            long contentLength;
            try {
                contentLength = body.contentLength();
            } catch (IOException ignored) {
                contentLength = 0;
            }

            MediaType contentType = body.contentType();

            if (contentType != null || contentLength != 0L) {
                return method + "_" + url + "_" + contentLength + "_" + contentType;
            } else {
                return method + "_" + url;
            }
        }
    }

    public static URL parseFromUrl(String urlString) throws URISyntaxException, MalformedURLException {
        URL url = new URL(urlString);
        URI uri = new URI(
                url.getProtocol(),
                url.getHost(),
                url.getPath(),
                url.getRef());
        return uri.toURL();

    }

    /**
     * Get encoding
     *
     * @param contentType
     * @return
     */

    public static Charset getCharset(MediaType contentType) {
        Charset charset = contentType != null ? contentType.charset(StandardCharsets.UTF_8) : StandardCharsets.UTF_8;
        if (charset == null) charset = StandardCharsets.UTF_8;
        return charset;
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write(input, output);
        output.close();
        return output.toByteArray();
    }

    public static void write(InputStream inputStream, OutputStream outputStream) throws IOException {
        int len;
        byte[] buffer = new byte[4096];
        while ((len = inputStream.read(buffer)) != -1) outputStream.write(buffer, 0, len);
    }

    /**
     * Convert http header raw data
     *
     * @param httpHeader
     * @return
     */
    public static String convertToHttpRawData(HashMap<String, List<String>> httpHeader) {
        if (httpHeader == null) {
            return "";
        }
        StringBuilder rawData = new StringBuilder();

        for (Map.Entry<String, List<String>> entry : httpHeader.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();

            // Add each key-value pair to HTTP raw data
            rawData.append(key).append(": ");

            for (int i = 0; i < values.size(); i++) {
                rawData.append(values.get(i));
                if (i < values.size() - 1) {
                    rawData.append(", ");
                }
            }
            rawData.append("\r\n");
        }
        return rawData.toString();
    }


    /**
     * Method to convert array to json string, replacing Gson high overhead
     *
     * @param values
     * @return
     */
    public static String setToJsonString(Collection<String> values) {
        if (values == null) return null;
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[");
        Iterator<String> iterator = values.iterator();
        while (iterator.hasNext()) {
            jsonBuilder.append("\"").append(iterator.next()).append("\"");
            if (iterator.hasNext()) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }

    /**
     * Appends multiple items to an existing JSON string array.
     * If the original JSON string is null or empty, a new array will be created.
     *
     * @param jsonArrayStr Original JSON string array (e.g. ["a", "b"])
     * @param values       Collection of new items to add (e.g. ["c", "d"])
     * @return Updated JSON string array (e.g. ["a", "b", "c", "d"])
     */
    public static String addItemsToJsonArray(String jsonArrayStr, Collection<String> values) {
        if (values == null || values.isEmpty()) return jsonArrayStr;

        StringBuilder jsonBuilder = new StringBuilder();

        String trimmed = jsonArrayStr == null ? "" : jsonArrayStr.trim();
        if (trimmed.isEmpty() || trimmed.equals("[]")) {
            jsonBuilder.append("[");
        } else {
            jsonBuilder.append(trimmed);
            if (jsonBuilder.charAt(jsonBuilder.length() - 1) == ']') {
                jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
                jsonBuilder.append(",");
            }
        }

        boolean firstAdded = jsonBuilder.toString().endsWith("[") || jsonBuilder.length() == 0;
        for (String value : values) {
            if (value == null) continue;
            String quotedValue = "\"" + value + "\"";
            if (jsonBuilder.indexOf(quotedValue) >= 0) continue; // skip duplicates

            if (!firstAdded) jsonBuilder.append(" ");
            jsonBuilder.append(quotedValue).append(",");
            firstAdded = false;
        }

        if (jsonBuilder.charAt(jsonBuilder.length() - 1) == ',') {
            jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
        }

        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }

    /**
     * Adds an item to a JSON string array representation.
     * If the original JSON string is null or empty, a new array will be created.
     *
     * @param jsonArrayStr Original JSON string array (e.g. ["a", "b"])
     * @param newItem      Item to add (e.g. "c")
     * @return Updated JSON string array (e.g. ["a", "b", "c"])
     */
    public static String addItemToJsonArray(String jsonArrayStr, String newItem) {
        if (newItem == null) return jsonArrayStr;

        String quotedValue = "\"" + newItem + "\"";

        if (jsonArrayStr != null && jsonArrayStr.contains(quotedValue)) {
            return jsonArrayStr; // exist
        }

        StringBuilder jsonBuilder = new StringBuilder();

        String trimmed = jsonArrayStr == null ? "" : jsonArrayStr.trim();
        if (trimmed.isEmpty() || trimmed.equals("[]")) {
            jsonBuilder.append("[").append(quotedValue).append("]");
        } else {
            jsonBuilder.append(trimmed);
            if (jsonBuilder.charAt(jsonBuilder.length() - 1) == ']') {
                jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
            }
            jsonBuilder.append(",").append(quotedValue).append("]");
        }

        return jsonBuilder.toString();
    }


    /**
     * Convert Hashmap to json, basic types convert themselves, other types are handled by gson, can reduce overhead
     *
     * @param map
     * @return Returns json string data normally, returns empty string on conversion exception
     */

    public static <T> String hashMapObjectToJson(HashMap<String, T> map) {
        StringBuilder jsonBuilder = new StringBuilder();
        try {
            jsonBuilder.append("{");
            for (String key : map.keySet()) {
                jsonBuilder.append("\"").append(key).append("\":");
                Object value = map.get(key);
                if (value instanceof String) {
                    jsonBuilder.append("\"").append(value).append("\"");
                } else if (value instanceof Number || value instanceof Boolean) {
                    jsonBuilder.append(value);
                } else {
                    // For non-basic types, use Gson for conversion
                    jsonBuilder.append(SingletonGson.getInstance().toJson(value));
                }
                jsonBuilder.append(", ");
            }
            if (!map.isEmpty()) {
                // Delete the last comma and space
                jsonBuilder.delete(jsonBuilder.length() - 2, jsonBuilder.length());
            }
            jsonBuilder.append("}");
        } catch (Exception e) {
            LogUtils.d(TAG, LogUtils.getStackTraceString(e));
            return "";
        }
        return jsonBuilder.toString();
    }


    /**
     * Convert JSONObject to Map<String, Object>
     *
     * @param jsonObject
     * @return
     */
    public static HashMap<String, Object> jsonToMap(@Nullable JSONObject jsonObject) {
        if (jsonObject == null) return null;
        HashMap<String, Object> map = new HashMap<>();
        // Get iterator of JSONObject keys
        Iterator<String> keys = jsonObject.keys();
        // Traverse each key and put key-value pairs into HashMap
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jsonObject.opt(key);
            // Put key-value pair into HashMap
            map.put(key, value);
        }
        return map;
    }


    /**
     * Read file using MMAP method
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static String readFile(File file) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        FileChannel channel = raf.getChannel();
        MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());

        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        String content = new String(bytes);

        buffer.force();
        channel.close();
        raf.close();

        return content;
    }

    /**
     * Remove extension from filename
     *
     * @param fileName filename
     * @return
     */
    public static String getNameWithoutExtension(String fileName) {
        int pos = fileName.lastIndexOf(".");
        if (pos > 0) {
            return fileName.substring(0, pos);
        } else {
            return fileName;
        }
    }


    /**
     * Write to file
     *
     * @param file
     * @param content
     * @throws IOException
     */
    public static void writeToFile(File file, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(content);
            writer.flush();
        }
    }

    /**
     * Get logcat
     * {@see https://github.com/iqiyi/xCrash/blob/457066ceb48fb84b993f1f04871d9e634d752792/xcrash_lib/src/main/java/xcrash/Util.java}
     *
     * @param logcatMainLines   This is the main log buffer, containing most application log output, [0,500], default 200
     * @param logcatSystemLines System log buffer, containing system-level log information, [0,500], default 50
     * @param logcatEventsLines Event log buffer, mainly recording specific event information, [0,500], default 50
     * @return
     */
    public static String getLogcat(int logcatMainLines, int logcatSystemLines, int logcatEventsLines) {
        int pid = android.os.Process.myPid();
        StringBuilder sb = new StringBuilder();

        sb.append("\nlogcat:\n");

        if (logcatMainLines > 0) {
            getLogcatByBufferName(pid, sb, "main", logcatMainLines, 'D');
        }
        if (logcatSystemLines > 0) {
            getLogcatByBufferName(pid, sb, "system", logcatSystemLines, 'W');
        }
        if (logcatEventsLines > 0) {
            getLogcatByBufferName(pid, sb, "events", logcatSystemLines, 'I');
        }

        sb.append("\n");

        return sb.toString();
    }

    /**
     * {@see https://github.com/iqiyi/xCrash/blob/457066ceb48fb84b993f1f04871d9e634d752792/xcrash_lib/src/main/java/xcrash/Util.java}
     *
     * @param pid
     * @param sb
     * @param bufferName
     * @param lines
     * @param priority
     */
    private static void getLogcatByBufferName(int pid, StringBuilder sb, String bufferName, int lines, char priority) {
        boolean withPid = (android.os.Build.VERSION.SDK_INT >= 24);
        String pidString = Integer.toString(pid);
        String pidLabel = " " + pidString + " ";

        //command for ProcessBuilder
        List<String> command = new ArrayList<String>();
        command.add("/system/bin/logcat");
        command.add("-b");
        command.add(bufferName);
        command.add("-d");
        command.add("-v");
        command.add("threadtime");
        command.add("-t");
        command.add(Integer.toString(withPid ? lines : (int) (lines * 1.2)));
        if (withPid) {
            command.add("--pid");
            command.add(pidString);
        }
        command.add("*:" + priority);

        //append the command line
        Object[] commandArray = command.toArray();
        sb.append("--------- tail end of log ").append(bufferName);
        sb.append(" (").append(android.text.TextUtils.join(" ", commandArray)).append(")\n");

        //append logs
        BufferedReader br = null;
        String line;
        try {
            java.lang.Process process = new ProcessBuilder().command(command).start();
            br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = br.readLine()) != null) {
                if (withPid || line.contains(pidLabel)) {
                    sb.append(line).append("\n");
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Util run logcat command failed");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Get all thread stacks
     *
     * @return
     */
    public static String getAllThreadStack() {
        String stack = "";
        try {
            stack = StringUtils.getThreadAllStackTrace(Thread.getAllStackTraces());
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
        return stack;
    }

    /**
     * Get App startup time，System.nanoTime
     *
     * @return
     */
    public static long getAppStartTimeNs() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            long diffMs = SystemClock.elapsedRealtime() - Process.getStartElapsedRealtime();
            return System.nanoTime()- TimeUnit.MILLISECONDS.toNanos(diffMs);
        } else {
            return FTApplication.APP_START_TIME;
        }
    }

    public static String toMD5(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hashBytes = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                // Convert to two-digit hexadecimal (pad with 0 if insufficient)
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
        return "";
    }

}

