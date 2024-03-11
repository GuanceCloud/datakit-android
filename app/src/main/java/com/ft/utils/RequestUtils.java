package com.ft.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.ft.http.OkHttpClientSingleton;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.utils.LogUtils;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 用于 Okhttp 网络数据请求测试
 */
public class RequestUtils {
    private static final String TAG = "RequestUtils";

    /**
     * 发起网络 GET 请求，
     * @param url 请求地址
     * @return
     */
    public static Request requestUrl(@NonNull String url) {
        Request.Builder builder = new Request.Builder().url(url).method(RequestMethod.GET.name(), null);
        Request request = null;
        try {
            Response response = OkHttpClientSingleton.getInstance().newCall(builder.build()).execute();
            request = response.request();

            int code = response.code();
            ResponseBody responseBody = response.body();
            String string = "";
            if (responseBody != null) {
                //这里需要消费，event listener 才会被调用
                string = responseBody.string();
            }
            LogUtils.d(TAG, "url:" + url + "\n" + code);

        } catch (IOException e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));
        }
        return request;
    }

}
