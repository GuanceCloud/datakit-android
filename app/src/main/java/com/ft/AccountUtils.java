package com.ft;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

public class AccountUtils {
    //上传数据查询的登录账号
    public final static String TEST_ACCOUNT = "TEST_ACCOUNT";
    //上传数据查询的登录密码
    public final static String TEST_PWD = "TEST_PWD";
    //上传数据加密key
    public final static String ACCESS_KEY_ID = "ACCESS_KEY_ID";
    //上传数据加密密钥
    public final static String ACCESS_KEY_SECRET = "ACCESS_KEY_SECRET";
    //上传数据地址
    public final static String ACCESS_SERVER_URL = "ACCESS_SERVER_URL";
    //高德地址逆向解析 key
    public final static String GEO_KEY = "GEO_KEY";
    /**
     * 本地配置文件中读取登录的账户数据
     */
    public static String getProperty(Context context, String key) {
        //testaccount.properties 该文件需要在资源目录assets中创建。如果本地没有需要自行创建，
        //文件的内容为
        //TEST_ACCOUNT = 你的账号
        //TEST_PWD = 你的账号密码
        Properties properties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open("testaccount.properties");
            properties.load(inputStream);
            Enumeration en = properties.propertyNames();
            while (en.hasMoreElements()) {
                String keyVar = (String)en.nextElement();
                if (key.contains(keyVar)) {
                     return properties.getProperty(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
