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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
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

    public static boolean hasNetPermission() {
        return hasPermission(FTApplication.getApplication(), Manifest.permission.ACCESS_NETWORK_STATE) &&
                hasPermission(FTApplication.getApplication(), Manifest.permission.INTERNET);
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

    /**
     * 保存数据
     *
     * @param key
     * @param value
     */
    public static <T> void saveSharePreference(String key, T value) {
        SharedPreferences sp = getSharedPreferences(FTApplication.getApplication());
        if (sp != null) {
            final SharedPreferences.Editor editor = sp.edit();
            if (value instanceof String) {
                editor.putString(key, (String) value);
            } else if (value instanceof Integer) {
                editor.putInt(key, (Integer) value);
            } else if (value instanceof Float) {
                editor.putFloat(key, (Float) value);
            } else if (value instanceof Boolean) {
                editor.putBoolean(key, (Boolean) value);
            } else if (value instanceof Long) {
                editor.putLong(key, (Long) value);
            }
            editor.apply();
        }
    }

    /**
     * 获取保存的数据
     *
     * @param key
     */
    public static <T> T querySharePreference(String key, Class<T> tClass, T defaultVar) {
        SharedPreferences sp = getSharedPreferences(FTApplication.getApplication());
        if (sp != null) {
            if (defaultVar instanceof String) {
                return tClass.cast(sp.getString(key, (String) defaultVar));
            } else if (defaultVar instanceof Integer) {
                return tClass.cast(sp.getInt(key, (Integer) defaultVar));
            } else if (defaultVar instanceof Float) {
                return tClass.cast(sp.getFloat(key, (Float) defaultVar));
            } else if (defaultVar instanceof Boolean) {
                return tClass.cast(sp.getBoolean(key, (Boolean) defaultVar));
            } else if (defaultVar instanceof Long) {
                return tClass.cast(sp.getLong(key, (Long) defaultVar));
            }
        }
        return tClass.cast(null);
    }

    public static String contentMD5EncodeWithBase64(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte[] b = md.digest();
            return Base64.encodeToString(b, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String MD5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte[] digest = md.digest();
            int i;
            StringBuilder sb = new StringBuilder();
            for (int offset = 0; offset < digest.length; offset++) {
                i = digest[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    sb.append(0);
                sb.append(Integer.toHexString(i));//通过Integer.toHexString方法把值变为16进制
            }
            return sb.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String MD5_16(String str) {
        return MD5(str).substring(8, 24);
    }

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
//                case 1:
//                    //ASCII在65-90之间为大写,获取大写随机
//                    uid.append((char) (rd.nextInt(25) + 65));
//                    break;
                case 1:
                    //ASCII在97-122之间为小写，获取小写随机
                    uid.append((char) (rd.nextInt(5) + 97));
                    break;
                default:
                    break;
            }
        }
        return uid.toString();
    }

    public static BigInteger getDDtraceNewId(){
        return new BigInteger(63, new SecureRandom());
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

    public static String getHMacSha1(String accesskeySecret, String content) {
        SecretKeySpec signingKey = new SecretKeySpec(accesskeySecret.getBytes(), "HmacSHA1");
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] text = mac.doFinal(content.getBytes());
            String result = Base64.encodeToString(text, Base64.DEFAULT);
            return result.trim();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 替换逗号为下划线
     *
     * @param oldStr
     * @return
     */
    public static String replaceComma(String oldStr) {
        if (isNullOrEmpty(oldStr)) {
            return "";
        }
        if (oldStr.contains(",")) {
            return oldStr.replaceAll(",", "_");
        }
        return oldStr;
    }

    /**
     * 转义空格
     *
     * @param oldStr
     * @return
     */
    public static String replaceSpace(String oldStr) {
        if (isNullOrEmpty(oldStr)) {
            return "";
        }
        if (oldStr.contains(" ")) {
            return oldStr.replaceAll(" ", "\\\\ ");
        }
        return oldStr;
    }

    /**
     * 转义空格和替换逗号为下划线
     *
     * @param oldStr
     * @return
     */
    public static String replaceSpaceAndComma(String oldStr) {
        oldStr = replaceSpace(oldStr);
        oldStr = replaceComma(oldStr);
        return oldStr;
    }

    /**
     * 判断流程图的指标集是否合法
     *
     * @param product
     * @return
     */
    public static boolean isLegalProduct(String product) {
        String pattern = "^[A-Za-z0-9_\\-]{1,40}";

        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(product);
        return m.matches();
    }

    /**
     * For tag keys, tag values, and field keys always use a backslash character \ to escape:
     *
     * @return
     */
    public static String translateTagKeyValue(String oldStr) {
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
        return generateRandomNumber() <= sampleRate * 100;
    }

    /**
     * 生成采集随机数
     *
     * @return
     */
    public static double generateRandomNumber() {
        Random random = new Random();
        return Math.floor(random.nextDouble() * 100);
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
            LogUtils.e(TAGS, e.getMessage());
        }
        return value;
    }

    /**
     * 获取纳秒时间
     * @return
     */
    public static long getCurrentNanoTime() {
        return System.currentTimeMillis() * 1000000L + System.nanoTime() % 1000000L;
    }


    public static String identifyRequest(Request request) {
        String method = request.method();
        String url = request.url().toString();
        RequestBody body = request.body();
        if (body == null || body == RequestBody.create(new byte[0], null)) {
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
}

