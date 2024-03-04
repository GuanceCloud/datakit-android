package com.ft.sdk.garble.utils;

import static com.ft.sdk.garble.utils.Constants.FT_SHARE_PER_FILE;
import static com.ft.sdk.garble.utils.Constants.TAGS;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Process;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.ft.sdk.FTApplication;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
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
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

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
     * 字符判断，判断字符非空
     *
     * @param str
     * @return
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 检测权限
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

    /**
     * 判断是否连接网络
     *
     * @return
     */
    public static boolean isNetworkAvailable() {
        return isNetworkAvailable(FTApplication.getApplication());
    }

    /**
     * 判断是否连接网络
     *
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetWork = manager.getActiveNetworkInfo();
        return activeNetWork != null && activeNetWork.isConnected();
    }

    /**
     * 获取应用 app 名称 AndroidManifest.xml application.labelName
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
            LogUtils.e(TAG, Log.getStackTraceString(e));
            return "";
        }
    }

    /**
     * 获取 SDK 缓存  {@link SharedPreferences}
     *
     * @param context
     * @return
     */
    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(FT_SHARE_PER_FILE, Context.MODE_PRIVATE);
    }

    /**
     * 判断是否应用是否在前台
     *
     * @return true 前台，反之为后台
     */
    public static boolean isAppForeground() {
        ActivityManager am = (ActivityManager) FTApplication.getApplication().getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return false;
        List<ActivityManager.RunningAppProcessInfo> info = am.getRunningAppProcesses();
        if (info == null || info.size() == 0) return false;
        for (ActivityManager.RunningAppProcessInfo aInfo : info) {
            if (aInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (aInfo.processName.equals(FTApplication.getApplication().getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取 16 字符长度的 GUID
     *
     * @return
     */
    public static String getGUID_16() {
        StringBuilder uid = new StringBuilder();
        //产生16位的强随机数
        Random rd = new SecureRandom();
        for (int i = 0; i < 16; i++) {
            //产生0-2的3位随机数
            int type = rd.nextInt(2);
            switch (type) {
                case 0:
                    //0-9的随机数
                    uid.append(rd.nextInt(10));
                    break;
                case 1:
                    //a-f 随机
                    uid.append((char) (rd.nextInt(6) + 97));
                    break;
                default:
                    break;
            }
        }
        return uid.toString();
    }

    /**
     * 获取 uuid
     *
     * @return
     */
    public static String randomUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取 64 位随机数
     *
     * @return
     */
    public static BigInteger getDDtraceNewId() {
        return new BigInteger(64, new SecureRandom());
    }

    /**
     * base64 加密字符串
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
     * 转译特殊字符
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
     * double 四舍五入取2位小数
     *
     * @param value
     * @return
     */
    public static double formatDouble(double value) {
        return (double) Math.round(value * 100) / 100;
    }

    /**
     * 得到当前日期的字符串
     *
     * @return
     */
    public static String getDateString() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "-" +
                calendar.get(Calendar.MONTH) + "-" +
                calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 转换时间格式
     */
    public static String getCurrentTimeStamp() {
        Date currentTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault());
        return sdf.format(currentTime);
    }

    /**
     * 随机数小于采样率，可以采样
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
     * 生成采集随机数
     *
     * @return
     */
    public static float generateRandomNumber() {
        Random random = new Random();
        return random.nextFloat() * 100;
    }

    /**
     * 判断当前进程是否是主进程
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
     * 获取当前进程名称
     *
     * @return
     */
    public static String getCurrentProcessName() {
        Context context = FTApplication.getApplication();
        int myPid = Process.myPid();
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : processes) {
            if (myPid == info.pid) {
                return info.processName;
            }
        }
        return null;
    }


    /**
     * 文件读取
     *
     * @param path
     * @param encoding
     * @return
     */
    public static String readFile(String path, Charset encoding) throws IOException {
        FileInputStream fis = null;
        fis = new FileInputStream(path);
        int length = fis.available();
        byte[] bytes = new byte[length];
        fis.read(bytes);
        fis.close();
        return new String(bytes, encoding);
    }

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
     * 是否是 json
     *
     * @param json
     * @return
     */
    public static boolean isJSONValid(String json) {
        try {
            JsonParser parser = new JsonParser();
            parser.parse(json);
            return true;
        } catch (JsonParseException e) {
            return false;
        }
    }

    /**
     * 读取应用
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
            LogUtils.e(TAGS, Log.getStackTraceString(e));
        }
        return value;
    }

    /**
     * 获取纳秒时间
     *
     * @return
     */
    public static long getCurrentNanoTime() {
        return System.currentTimeMillis() * 1000000L + System.nanoTime() % 1000000L;
    }

    /**
     * 生成相对唯一的 resourceId
     *
     * @param request
     * @return
     */

    public static String identifyRequest(Request request) {
        String method = request.method();
        String url = request.url().toString();
        RequestBody body = request.body();
        if (body == null || body == RequestBody.create(null, new byte[0])) {
            return method + "_" + url;
        } else {
            long contentLength = 0;
            try {
                contentLength = body.contentLength();
            } catch (IOException e) {
                LogUtils.e(TAG, Log.getStackTraceString(e));
            }
            String contentType = body.contentType() == null ? "" : body.contentType().toString();
            return method + "_" + url + "_" + contentType + "_" + contentLength;

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
     * 转化 http header raw 数据
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

            // 添加每个键值对到HTTP原始数据
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


    // 读取文件
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
     * 写入文件
     *
     * @param file
     * @param content
     * @throws IOException
     */
    public static void writeToFile(File file, String content) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.seek(file.length()); // Move to end of file
        raf.writeBytes(content);
        raf.close();
    }

}

