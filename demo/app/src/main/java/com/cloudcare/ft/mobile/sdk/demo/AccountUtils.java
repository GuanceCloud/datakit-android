package com.cloudcare.ft.mobile.sdk.demo;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

public class AccountUtils {
    public final static String ACCESS_SERVER_URL = "ACCESS_SERVER_URL";
    public final static String RUM_APP_ID = "RUM_APP_ID";
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
