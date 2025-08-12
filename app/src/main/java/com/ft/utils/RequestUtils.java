package com.ft.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.ft.http.OkHttpClientSingleton;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.utils.LogUtils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Used for Okhttp network data request testing
 */
public class RequestUtils {
    private static final String TAG = "RequestUtils";

    /**
     * Initiate a network GET request
     *
     * @param url request address
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
                //Here we need to consume, the event listener will be called
                string = responseBody.string();
            }
            LogUtils.d(TAG, "url:" + url + "\n" + code);

        } catch (IOException e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));
        }
        return request;
    }

    /**
     * Initiate an asynchronous network GET request
     *
     * @param url request address
     * @param callback callback interface
     */
    public static void requestUrlAsync(@NonNull String url, @NonNull RequestCallback callback) {
        Request.Builder builder = new Request.Builder().url(url).method(RequestMethod.GET.name(), null);
        Request request = builder.build();
        
        OkHttpClientSingleton.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtils.e(TAG, "Async request failed: " + Log.getStackTraceString(e));
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                int code = response.code();
                ResponseBody responseBody = response.body();
                String responseString = "";
                
                if (responseBody != null) {
                    responseString = responseBody.string();
                }
                
                LogUtils.d(TAG, "Async request successful - url:" + url + ", code:" + code);
                callback.onSuccess(code, responseString);
            }
        });
    }

    /**
     * Asynchronous request callback interface
     */
    public interface RequestCallback {
        /**
         * Request success callback
         * @param code response status code
         * @param response response content
         */
        void onSuccess(int code, String response);

        /**
         * Request failure callback
         * @param e exception information
         */
        void onFailure(IOException e);
    }

}
