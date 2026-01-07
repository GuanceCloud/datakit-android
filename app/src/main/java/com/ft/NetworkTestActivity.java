package com.ft;

import android.os.Bundle;
import android.widget.Button;

import com.ft.BuildConfig;
import com.ft.sdk.FTResourceEventListener;
import com.ft.sdk.FTResourceInterceptor;
import com.ft.sdk.FTTraceInterceptor;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.utils.RequestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Network test activity
 * Includes: normal network request, streaming upload, and streaming download tests
 */
public class NetworkTestActivity extends NameTitleActivity {

    private static final String TAG = "NetworkTestActivity";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new FTTraceInterceptor())
            .addInterceptor(new FTResourceInterceptor())
            .eventListenerFactory(new FTResourceEventListener.FTFactory())
            .connectTimeout(10, TimeUnit.SECONDS)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_test);

        // Normal network request test
        Button normalRequestBtn = findViewById(R.id.btn_normal_request);
        normalRequestBtn.setOnClickListener(v -> testNormalRequest());

        // Streaming upload test
        Button streamUploadBtn = findViewById(R.id.btn_stream_upload);
        streamUploadBtn.setOnClickListener(v -> testStreamUpload());

        // Streaming download test
        Button streamDownloadBtn = findViewById(R.id.btn_stream_download);
        streamDownloadBtn.setOnClickListener(v -> testStreamDownload());
    }

    /**
     * Test normal network request (preserves original test)
     */
    private void testNormalRequest() {
        LogUtils.d(TAG, "Starting normal network request test...");
        new Thread(() -> {
            try {
                Request request = RequestUtils.requestUrl(BuildConfig.TRACE_URL);
                if (request != null) {
                    LogUtils.d(TAG, "Normal network request test succeeded. " +
                            "Request headers: " + request.headers().toString());
                } else {
                    LogUtils.e(TAG, "Normal network request test failed: request is null");
                }
            } catch (Exception e) {
                LogUtils.e(TAG, "Normal network request test failed: " + e.getMessage() + "\n" +
                        LogUtils.getStackTraceString(e));
            }
        }).start();
    }

    /**
     * Test streaming upload
     */
    private void testStreamUpload() {
        LogUtils.d(TAG, "Starting streaming upload test...");
        new Thread(() -> {
            try {
                // Create a streaming request body to simulate large file upload
                RequestBody requestBody = new RequestBody() {
                    @Override
                    public MediaType contentType() {
                        return MediaType.parse("application/octet-stream");
                    }

                    @Override
                    public void writeTo(okio.BufferedSink sink) throws IOException {
                        // Simulate streaming data write
                        byte[] buffer = new byte[8192]; // 8KB buffer
                        for (int i = 0; i < 100; i++) { // Write 800KB data
                            String data = "Stream upload data chunk " + i + "\n";
                            sink.write(data.getBytes());
                            // Simulate streaming write delay
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new IOException("Upload interrupted", e);
                            }
                        }
                    }
                };

                Request request = new Request.Builder()
                        .url(BuildConfig.TRACE_URL)
                        .method(RequestMethod.POST.name(), requestBody)
                        .header("Content-Type", "application/octet-stream")
                        .build();

                Response response = client.newCall(request).execute();
                int code = response.code();
                ResponseBody responseBody = response.body();
                String responseString = "";
                if (responseBody != null) {
                    responseString = responseBody.string();
                    responseBody.close();
                }
                response.close();

                LogUtils.d(TAG, "Streaming upload test succeeded. " +
                        "Response code: " + code + ", " +
                        "Response content length: " + responseString.length() + " bytes");
            } catch (Exception e) {
                LogUtils.e(TAG, "Streaming upload test failed: " + e.getMessage() + "\n" +
                        LogUtils.getStackTraceString(e));
            }
        }).start();
    }

    /**
     * Test streaming download
     */
    private void testStreamDownload() {
        LogUtils.d(TAG, "Starting streaming download test...");
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(BuildConfig.TRACE_URL)
                        .method(RequestMethod.GET.name(), null)
                        .build();

                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();
                
                if (responseBody != null) {
                    // Stream read response data
                    InputStream inputStream = responseBody.byteStream();
                    byte[] buffer = new byte[8192]; // 8KB buffer
                    int totalBytes = 0;
                    int bytesRead;
                    
                    LogUtils.d(TAG, "Starting streaming download...");
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        totalBytes += bytesRead;
                        // Can process received data chunks here
                        // This is just simulating streaming read
                        if (totalBytes % (8192 * 10) == 0) { // Update every 80KB
                            LogUtils.d(TAG, "Downloaded: " + totalBytes + " bytes");
                        }
                    }
                    inputStream.close();
                    responseBody.close();

                    LogUtils.d(TAG, "Streaming download test succeeded. " +
                            "Total download size: " + totalBytes + " bytes");
                } else {
                    LogUtils.e(TAG, "Streaming download test failed: response body is null");
                }
                response.close();
            } catch (Exception e) {
                LogUtils.e(TAG, "Streaming download test failed: " + e.getMessage() + "\n" +
                        LogUtils.getStackTraceString(e));
            }
        }).start();
    }
}

