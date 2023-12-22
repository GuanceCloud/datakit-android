package com.ft.sdk.garble.http;

import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.FTHttpConfigManager;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

/**
 * create: by huangDianHua
 * time: 2020/4/21 16:50:18
 * description:
 */
public class NetProxy {
    public final static String TAG = Constants.LOG_TAG_PREFIX + "NetProxy";
    /**
     * SDK 中网络的配置
     */
    private final FTHttpConfigManager ftHttpConfig = FTHttpConfigManager.get();
    /**
     * 内容类型
     */
    private static final String CONTENT_TYPE = "text/plain";
    /**
     * 字符编码
     */
    private static final String CHARSET = "UTF-8";
    private final HttpBuilder httpBuilder;
    private final INetEngine engine;

    public NetProxy(HttpBuilder httpBuilder) {
        this.httpBuilder = httpBuilder;
        engine = EngineFactory.createEngine();
        try {
            engine.defaultConfig(httpBuilder);
        } catch (Exception e) {
            LogUtils.e(TAG, e.getLocalizedMessage());
        }
    }

    /**
     * 执行网络请求
     *
     * @return
     */
    public FTResponseData execute() {
        if (!Utils.isNetworkAvailable()) {
            return new FTResponseData(NetCodeStatus.NETWORK_EXCEPTION_CODE, "网络未连接");
        }
        if (!httpBuilder.getUrl().startsWith("http://") && !httpBuilder.getUrl().startsWith("https://")) {
            //请求地址为空是提示错误
            return new FTResponseData(NetCodeStatus.UNKNOWN_EXCEPTION_CODE, "请求地址错误");
        }
        if (httpBuilder.isUseDefaultHead()) {
            //设置特有的请求头
            setHeadParams();
        }
        engine.createRequest(httpBuilder);
        return engine.execute();
    }

    /**
     * 转换时间格式
     */
    private String calculateDate() {
        Date currentTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(currentTime);
    }

    /**
     * 设置请求的 Head 参数
     */
    private void setHeadParams() {
        HashMap<String, String> head = httpBuilder.getHeadParams();
        if (head == null) {
            head = new HashMap<>();
        }
        head.put("User-Agent", ftHttpConfig.userAgent +
                ";agent_" + FTSdk.AGENT_VERSION +
                ";autotrack_" + FTSdk.PLUGIN_VERSION +
                ";native_" + FTSdk.NATIVE_VERSION
        );
        head.put("Accept-Language", "zh-CN");
        if (!head.containsKey("Content-Type")) {
            head.put("Content-Type", CONTENT_TYPE);
        }
        head.put("charset", CHARSET);
        //添加日期请求头
        head.put("Date", calculateDate());
        httpBuilder.setHeadParams(head);
    }
}
