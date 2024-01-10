package com.ft;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.ft.sdk.FTLogger;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.bean.ErrorType;
import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.bean.ResourceParams;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.utils.Utils;

import java.net.HttpURLConnection;
import java.util.HashMap;

import okhttp3.Request;

/**
 * 手动使用 RUM 例子
 */
public class ManualRUMActivity extends NameTitleActivity {

    public static final String HTTPS_FAKE_URL = "https://www.fakeurl.com";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);
        findViewById(R.id.manual_start_action_btn).setOnClickListener(v -> {
            HashMap<String, Object> property = new HashMap<>();
            property.put("sp_count", 1);
            FTRUMGlobalManager.get().startAction("Action Start", "Button_Click", property);

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
}
