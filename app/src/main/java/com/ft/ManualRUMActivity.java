package com.ft;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.ft.sdk.FTLogger;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.bean.ErrorType;
import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.bean.ResourceParams;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.net.HttpURLConnection;
import java.util.HashMap;

import okhttp3.Request;

/**
 * Manual RUM usage example
 */
public class ManualRUMActivity extends NameTitleActivity {

    public static final String HTTPS_FAKE_URL = "https://www.fakeurl.com";
    private static final String TAG = "ManualRUMActivity";
    int count = 0;

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
            for (int i = 0; i < 10; i++) {
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
            Toast.makeText(this, "Data filter resources sent", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.manual_filter_log_btn).setOnClickListener(v -> {
            sendDataFilterLog(false);
            sendDataFilterLog(true);
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
        property.put("filter_sample", shouldDrop ? "drop-log" : "keep-log");
        String message = shouldDrop
                ? "android data filter password sample"
                : "android data filter password keep control";
        FTLogger.getInstance().logBackground(message, Status.DEBUG, property);
        LogUtils.d(TAG, "send data filter sample log, shouldDrop:" + shouldDrop
                + ", message:" + message);
    }
}
