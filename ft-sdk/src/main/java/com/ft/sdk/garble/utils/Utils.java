package com.ft.sdk.garble.utils;

import android.Manifest;
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

import androidx.core.content.ContextCompat;

import com.ft.sdk.FTApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static com.ft.sdk.garble.utils.Constants.FT_SHARE_PER_FILE;
import static com.ft.sdk.garble.utils.Constants.TAGS;
import static java.nio.charset.StandardCharsets.UTF_8;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * BY huangDianHua
 * DATE:2019-11-29 17:54
 * Description:
 */
public class Utils {
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isNetworkAvailable() {
        return isNetworkAvailable(FTApplication.getApplication());
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetWork = manager.getActiveNetworkInfo();
        return activeNetWork != null && activeNetWork.isConnected();
    }

    public static String getAppVersionName() {
        PackageManager manager = FTApplication.getApplication().getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(FTApplication.getApplication().getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(FT_SHARE_PER_FILE, Context.MODE_PRIVATE);
    }

    public static String getGUID_16() {
        StringBuilder uid = new StringBuilder();
        //??????16??????????????????
        Random rd = new SecureRandom();
        for (int i = 0; i < 16; i++) {
            //??????0-2???3????????????
            int type = rd.nextInt(2);
            switch (type) {
                case 0:
                    //0-9????????????
                    uid.append(rd.nextInt(10));
                    break;
//                case 1:
//                    //ASCII???65-90???????????????,??????????????????
//                    uid.append((char) (rd.nextInt(25) + 65));
//                    break;
                case 1:
                    //ASCII???97-122????????????????????????????????????
                    uid.append((char) (rd.nextInt(5) + 97));
                    break;
                default:
                    break;
            }
        }
        return uid.toString();
    }

    public static BigInteger getDDtraceNewId() {
        return new BigInteger(64, new SecureRandom());
    }

    /**
     * base64 ???????????????
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
        if (oldStr.equals(Constants.UNKNOWN)) {
            return "\"" + oldStr + "\"";
        } else {
            if (Utils.isJSONValid(oldStr)) {
                return JSONObject.quote(oldStr);
            } else {
                oldStr = oldStr.replace("\\", "\\\\");
                oldStr = translateSpecialCharacters("\"", oldStr);
                return "\"" + oldStr + "\"";
            }
        }
    }

    /**
     * ??????????????????
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
     * double ???????????????2?????????
     *
     * @param value
     * @return
     */
    public static double formatDouble(double value) {
        return (double) Math.round(value * 100) / 100;
    }

    /**
     * ??????????????????????????????
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
     * ??????????????????
     */
    public static String getCurrentTimeStamp() {
        Date currentTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault());
        return sdf.format(currentTime);
    }

    /**
     * ???????????????????????????????????????
     *
     * @return
     */
    public static boolean enableTraceSamplingRate(float sampleRate) {
        return generateRandomNumber() <= sampleRate * 100;
    }

    /**
     * ?????????????????????
     *
     * @return
     */
    public static double generateRandomNumber() {
        Random random = new Random();
        return Math.floor(random.nextDouble() * 100);
    }

    /**
     * ????????????????????????????????????
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
     * ????????????????????????
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
     * ????????????
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
     * ????????? json
     *
     * @param test
     * @return
     */
    public static boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    /**
     * ????????????
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
            LogUtils.e(TAGS, e.getMessage());
        }
        return value;
    }

    /**
     * ??????????????????
     * @return
     */
    public static long getCurrentNanoTime() {
        return System.currentTimeMillis() * 1000000L + System.nanoTime() % 1000000L;
    }


    public static String identifyRequest(Request request) {
        String method = request.method();
        String url = request.url().toString();
        RequestBody body = request.body();
        if (body == null || body == RequestBody.create(null, new byte[0] )) {
            return method + "_" + url;
        } else {
            long contentLength = 0;
            try {
                contentLength = body.contentLength();
            } catch (IOException e) {
                e.printStackTrace();
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
}

