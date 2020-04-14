package com.ft.sdk.garble.utils;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;

import androidx.core.content.ContextCompat;

import com.ft.sdk.FTApplication;
import com.ft.sdk.FTSdk;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static com.ft.sdk.garble.utils.Constants.FT_SHARE_PER_FILE;

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
        return hasPermission(FTSdk.get().getApplication(), Manifest.permission.ACCESS_NETWORK_STATE) &&
                hasPermission(FTSdk.get().getApplication(), Manifest.permission.INTERNET);
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

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(FT_SHARE_PER_FILE, Context.MODE_PRIVATE);
    }

    public static String contentMD5Encode(String str) {
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
    public static String translateTagKeyValueAndFieldKey(String oldStr) {
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
        return translateSpecialCharacters("\"", oldStr);
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

}

