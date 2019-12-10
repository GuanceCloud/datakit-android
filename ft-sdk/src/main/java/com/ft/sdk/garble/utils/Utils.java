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

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static com.ft.sdk.garble.utils.Constants.FT_SHARE_PER_FILE;

/**
 * BY huangDianHua
 * DATE:2019-11-29 17:54
 * Description:
 */
public class Utils {

    public static boolean isNullOrEmpty(String str){
        return str == null || str.isEmpty();
    }
    public static boolean hasNetPermission() {
        return hasPermission(FTApplication.getApplication(), Manifest.permission.ACCESS_NETWORK_STATE) &&
                hasPermission(FTApplication.getApplication(), Manifest.permission.INTERNET);
    }

    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED;
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

    public static String contentMD5Encode(String str){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte[] b = md.digest();
            return Base64.encodeToString(b,Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getHnacSha1(String accesskeySecret,String content){
        SecretKeySpec signingKey = new SecretKeySpec(accesskeySecret.getBytes(),"HmacSHA1");
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] text = mac.doFinal(content.getBytes());
            String result = Base64.encodeToString(text,Base64.DEFAULT);
            return result.trim();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return "";
        }
    }
}

