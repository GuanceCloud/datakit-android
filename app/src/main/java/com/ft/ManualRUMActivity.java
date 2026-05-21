package com.ft;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.ft.sdk.FTLogger;
import com.ft.sdk.FTResourceEventListener;
import com.ft.sdk.FTResourceInterceptor;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTraceInterceptor;
import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.bean.ErrorType;
import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.bean.ResourceParams;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Manual RUM usage example
 */
public class ManualRUMActivity extends NameTitleActivity {

    public static final String HTTPS_FAKE_URL = "https://www.fakeurl.com";
    private static final String TAG = "ManualRUMActivity";
    private static final String FILTER_SCENE = "datakit-blacklist";
    private static final String FILTER_HEADER_SCENE = "X-FT-Filter-Scene";
    private static final String FILTER_HEADER_SAMPLE = "X-FT-Filter-Sample";
    private static final String KEEP_RESOURCE_URL =
            "https://httpbin.org/status/200?ft_filter_sample=keep-200";
    private static final String DROP_404_RESOURCE_URL =
            "https://httpbin.org/status/404?ft_filter_sample=drop-404";
    private static final String DROP_503_RESOURCE_URL =
            "https://httpbin.org/status/503?ft_filter_sample=drop-503";
    int count = 0;

    private final OkHttpClient dataFilterClient = new OkHttpClient.Builder()
            .addInterceptor(new FTTraceInterceptor())
            .addInterceptor(new FTResourceInterceptor(new FTResourceInterceptor.ContentHandlerHelper() {
                @Override
                public void onRequest(Request request, HashMap<String, Object> extraData) {
                    extraData.put("filter_scene", request.header(FILTER_HEADER_SCENE));
                    extraData.put("filter_sample", request.header(FILTER_HEADER_SAMPLE));
                }

                @Override
                public void onResponse(Response response, HashMap<String, Object> extraData) {

                }
            }))
            .eventListenerFactory(new FTResourceEventListener.FTFactory())
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);
        findViewById(R.id.manual_start_action_btn).setOnClickListener(v -> {
            HashMap<String, Object> property = new HashMap<>();
            property.put("sp_count", 1);
            //Enable automatic acquisition, which will conflict with custom actions here,
            // and actions with intervals less than 100ms will be blocked. If calling frequently, please use addAction
             FTRUMGlobalManager.get().startAction("Action Start", "Button_Click", property);
            for (int i = 0; i < 10000; i++) {
                FTRUMGlobalManager.get().addAction("Add Action Start:" + (count++), "Button_Click", property);
            }

        });
        findViewById(R.id.manual_view_start_btn).setOnClickListener(v -> {
            HashMap<String, Object> property = new HashMap<>();
            property.put("sp_override", 1);
            FTRUMGlobalManager.get().startView("Manual Custom View", property);

        });

        findViewById(R.id.manual_view_stop_btn).setOnClickListener(v -> {
            HashMap<String, Object> property = new HashMap<>();
            property.put("sp_override", 2);
            property.put("sp_extra", "other");
            FTRUMGlobalManager.get().stopView(property);
        });

        Request request = new Request.Builder().url(HTTPS_FAKE_URL).build();

        String resourceId = Utils.identifyRequest(request);

        findViewById(R.id.manual_filter_pull_btn).setOnClickListener(v -> {
            if (BuildConfig.DATAKIT_URL == null || BuildConfig.DATAKIT_URL.length() == 0) {
                Toast.makeText(this, "DATAKIT_URL is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            FTSdk.setDatakitUrl(BuildConfig.DATAKIT_URL);
            Toast.makeText(this, "Datakit filter pull requested", Toast.LENGTH_SHORT).show();
            LogUtils.d(TAG, "Datakit filter pull requested, url:" + BuildConfig.DATAKIT_URL);
        });

        findViewById(R.id.manual_resource_start_btn).setOnClickListener(v -> {
            HashMap<String, Object> property = new HashMap<>();
            property.put("sp_override", 1);
            FTRUMGlobalManager.get().startResource(resourceId, property);

        });

        findViewById(R.id.manual_resource_stop_btn).setOnClickListener(v -> {
            HashMap<String, Object> property = new HashMap<>();
            property.put("sp_override", 2);
            property.put("sp_extra", "other");
            FTRUMGlobalManager.get().stopResource(resourceId, property);
            NetStatusBean netStatusBean = new NetStatusBean();
            ResourceParams params = new ResourceParams();
            params.resourceStatus = HttpURLConnection.HTTP_OK;
            params.url = HTTPS_FAKE_URL;
            FTRUMGlobalManager.get().addResource(resourceId, params, netStatusBean);

        });

        findViewById(R.id.manual_filter_resource_btn).setOnClickListener(v -> {
            sendDataFilterResource(HttpURLConnection.HTTP_OK, "keep-200");
            sendDataFilterResource(HttpURLConnection.HTTP_NOT_FOUND, "drop-404");
            sendDataFilterResource(HttpURLConnection.HTTP_UNAVAILABLE, "drop-503");
            flushSyncDataSoon();
            Toast.makeText(this, "Data filter resources sent", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.manual_filter_real_resource_btn).setOnClickListener(v -> {
            sendRealDataFilterResources();
            Toast.makeText(this, "Real OkHttp filter resources requested", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.manual_filter_log_btn).setOnClickListener(v -> {
            sendDataFilterLog(false);
            sendDataFilterLog(true);
            flushSyncDataSoon();
            Toast.makeText(this, "Data filter logs sent", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.manual_longtask_btn).setOnClickListener(v -> {
            HashMap<String, Object> property = new HashMap<>();
            property.put("sp_label", "Render");
            FTRUMGlobalManager.get().addLongTask("Manual LongTask", 10000000, property);
        });

        findViewById(R.id.manual_error_btn).setOnClickListener(v -> {
            HashMap<String, Object> property = new HashMap<>();
            property.put("sp_label", "try_catch");
            FTRUMGlobalManager.get().addError("Manual Error", "test",
                    ErrorType.JAVA, AppState.RUN, property);
        });

        findViewById(R.id.manual_log_btn).setOnClickListener(v -> {
            HashMap<String, Object> property = new HashMap<>();
            property.put("sp_label", "HomePage");
            FTLogger.getInstance().logBackground("Manual Log", Status.ERROR, property);
        });

    }

    private void sendDataFilterResource(int statusCode, String sampleName) {
        String url = HTTPS_FAKE_URL + "/data-filter/" + sampleName;
        Request request = new Request.Builder().url(url).build();
        String sampleResourceId = Utils.identifyRequest(request);

        HashMap<String, Object> property = new HashMap<>();
        property.put("filter_scene", FILTER_SCENE);
        property.put("filter_sample", sampleName);
        FTRUMGlobalManager.get().startResource(sampleResourceId, property);
        FTRUMGlobalManager.get().stopResource(sampleResourceId, property);

        ResourceParams params = new ResourceParams();
        params.resourceMethod = "GET";
        params.resourceStatus = statusCode;
        params.url = url;
        FTRUMGlobalManager.get().addResource(sampleResourceId, params, new NetStatusBean());

        LogUtils.d(TAG, "send data filter sample resource, status:" + statusCode
                + ", sample:" + sampleName + ", url:" + url);
    }

    private void sendDataFilterLog(boolean shouldDrop) {
        HashMap<String, Object> property = new HashMap<>();
        property.put("filter_scene", FILTER_SCENE);
        property.put("filter_sample", shouldDrop ? "drop-log" : "keep-log");
        String message = shouldDrop
                ? "android datakit blacklist password drop sample"
                : "android datakit blacklist keep control";
        FTLogger.getInstance().logBackground(message, Status.DEBUG, property);
        LogUtils.d(TAG, "send data filter sample log, shouldDrop:" + shouldDrop
                + ", message:" + message);
    }

    private void sendRealDataFilterResources() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                requestDataFilterResource(KEEP_RESOURCE_URL, "keep-200");
                requestDataFilterResource(DROP_404_RESOURCE_URL, "drop-404");
                requestDataFilterResource(DROP_503_RESOURCE_URL, "drop-503");
                flushSyncDataSoon();
            }
        }).start();
    }

    private void requestDataFilterResource(String url, String sampleName) {
        Request request = new Request.Builder()
                .url(url)
                .header(FILTER_HEADER_SCENE, FILTER_SCENE)
                .header(FILTER_HEADER_SAMPLE, sampleName)
                .build();
        try {
            Response response = dataFilterClient.newCall(request).execute();
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                responseBody.close();
            }
            LogUtils.d(TAG, "real data filter resource, sample:" + sampleName
                    + ", code:" + response.code() + ", url:" + url);
            response.close();
        } catch (Exception e) {
            LogUtils.e(TAG, "real data filter resource failed, sample:" + sampleName
                    + ", error:" + LogUtils.getStackTraceString(e));
        }
    }

    private void flushSyncDataSoon() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                FTSdk.flushSyncData();
            }
        }).start();
    }
}
