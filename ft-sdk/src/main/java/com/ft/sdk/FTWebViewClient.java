package com.ft.sdk;

import android.graphics.Bitmap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import com.ft.sdk.garble.manager.FTWebViewEventTracker;
import com.ft.sdk.garble.utils.LogUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * author: huangDianHua
 * time: 2020/9/14 14:31:03
 * description: 拦截 WebView 中的网络请求
 */
public class FTWebViewClient extends WebViewClient {
    static OkHttpClient mClient = new OkHttpClient.Builder()
            .addInterceptor(new FTNetWorkTracerInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .build();
    private String mOriginUrl;

    private FTWebViewEventTracker mHelper = new FTWebViewEventTracker();

    public FTWebViewClient() {

    }


    private CountDownLatch mCountDownLatch = new CountDownLatch(1);

    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        mHelper.pageLoading();
        view.post(() -> {
            mOriginUrl = view.getUrl();
            mCountDownLatch.countDown();
        });
        try {
            mCountDownLatch.await(1L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            String url = request.getUrl().toString();
            if (url.equals(mOriginUrl)) {
                LogUtils.d("WebView", "URL= originUrl=" + request.getUrl().toString());
                return getNetResponse(request.getUrl().toString(), request.getRequestHeaders());
            } else {
                return super.shouldInterceptRequest(view, request);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return super.shouldInterceptRequest(view, request);
        }
    }

    private WebResourceResponse getNetResponse(String url, Map<String, String> headers) throws IOException {
        Request.Builder builder = new Request.Builder()
                .url(url.trim());
        Set<String> keySet = headers.keySet();
        for (String key : keySet) {
            builder.addHeader(key, headers.get(key));
        }
        Request request = builder.build();
        Response response = mClient.newCall(request).execute();
        String contentType = response.header("Content-Type", response.body().contentType().type());
        String temp = contentType.toLowerCase();
        if (temp.contains("charset=utf-8")) {
            contentType = contentType.replaceAll("(?i)" + "charset=utf-8", "");//不区分大小写的替换
        }
        if (contentType.contains(";")) {
            contentType = contentType.replaceAll(";", "");
            contentType = contentType.trim();
        }
        return new WebResourceResponse(contentType, response.header("Content-Encoding", "utf-8"), response.body().byteStream());
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        mHelper.pageStarted();
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        mHelper.pageFinished();
    }



}
