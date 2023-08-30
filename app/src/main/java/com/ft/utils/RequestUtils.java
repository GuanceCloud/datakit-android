package com.ft.utils;

import androidx.annotation.NonNull;

import com.ft.http.OkHttpClientSingleton;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.utils.LogUtils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RequestUtils {
    private static final String TAG = "RequestUtils";

    public static Request requestUrl(@NonNull String url) {
        Request.Builder builder = new Request.Builder().url(url).method(RequestMethod.GET.name(), null);
        Request request = null;
        try {
            Response response = OkHttpClientSingleton.getInstance().newCall(builder.build()).execute();
            request = response.request();

            ResponseBody responseBody = response.body();
            String string = "";
            if (responseBody != null) {
                string = responseBody.string();
            }
            LogUtils.d(TAG, "url:" + url + "\n" + string);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return request;
    }

    public static void asyncRequest(String url) {
        Request.Builder builder = new Request.Builder()
                .url(url).method(RequestMethod.GET.name(), null);
        OkHttpClientSingleton.getInstance()
                .newCall(builder.build()).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        // 处理响应
                        System.out.println(response.body().string());
                        response.close();
                    }
                });


    }

}
