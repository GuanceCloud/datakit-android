package com.ft.sdk.garble.http;

import com.ft.sdk.garble.FTHttpConfig;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.lang.reflect.Constructor;
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
    //SDK 中网络的配置
    FTHttpConfig ftHttpConfig = FTHttpConfig.get();
    //内容类型
    final String CONTENT_TYPE = "text/plain";
    //字符编码
    final String CHARSET = "UTF-8";
    private String gmtString;
    private HttpBuilder httpBuilder;
    INetEngine engine;

    public NetProxy(HttpBuilder httpBuilder) {
        this.httpBuilder = httpBuilder;
        engine = EngineFactory.createEngine();
        try {
            engine.defaultConfig(httpBuilder);
        }catch (Exception e){
            LogUtils.e(e.getLocalizedMessage());
        }
    }

    public <T extends ResponseData> T execute(Class<T> tClass){
        if(!httpBuilder.getUrl().startsWith("http://") && !httpBuilder.getUrl().startsWith("https://")){
            //请求地址为空是提示错误
            return getResponseData(tClass, NetCodeStatus.UNKNOWN_EXCEPTION_CODE, "请求地址错误");
        }
        if (httpBuilder.isUseDefaultHead()) {
            //设置 DataFlux 请求特有的请求头
            setHeadParams();
        }
        engine.createRequest(httpBuilder);
        ResponseData responseData = engine.execute();
        if(responseData != null) {
            if(httpBuilder.isShowLog()) {
                LogUtils.d("HTTP-response:[code:" + responseData.getHttpCode() + ",response:" + responseData.getData() + "]");
            }
            return getResponseData(tClass, responseData.getHttpCode(), responseData.getData());
        }else{
            return getResponseData(tClass, NetCodeStatus.UNKNOWN_EXCEPTION_CODE, "");
        }
    }

    /**
     * 构建网络请求返回对象
     *
     * @param tClass
     * @param code
     * @param message
     * @param <T>
     * @return
     */
    private <T extends ResponseData> T getResponseData(Class<T> tClass, int code, String message) {
        Constructor[] constructor = tClass.getConstructors();
        for (Constructor<T> con : constructor) {
            Class[] classes = con.getParameterTypes();
            if (classes.length == 2) {
                if (classes[0].getName().equals(int.class.getName()) &&
                        classes[1].getName().equals(String.class.getName())) {
                    try {
                        return con.newInstance(code, message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }


    /**
     * 获取签名
     * @return
     */
    private String getSignature() {
        String aks = ftHttpConfig.akSecret;
        String method = httpBuilder.getMethod().method;
        String contentMD5 = getContentMD5();
        return Utils.getHMacSha1(aks, method + "\n" + contentMD5 + "\n" + CONTENT_TYPE + "\n" + gmtString);
    }

    /**
     * MD5 加密 请求内容
     * @return
     */
    private String getContentMD5() {
        return Utils.contentMD5Encode(httpBuilder.getBodyString() == null ? "" : httpBuilder.getBodyString());
    }

    /**
     * 转换时间格式
     */
    private void calculateDate() {
        Date currentTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        gmtString = sdf.format(currentTime);
    }

    /**
     *  设置请求的 Head 参数
     */
    private void setHeadParams() {
        HashMap<String,String> head = httpBuilder.getHeadParams();
        if(head == null){
            head = new HashMap<>();
        }
        head.put("X-Datakit-UUID", ftHttpConfig.uuid);
        head.put("User-Agent", ftHttpConfig.userAgent);
        head.put("Accept-Language", "zh-CN");
        head.put("Content-Type", CONTENT_TYPE);
        head.put("charset", CHARSET);
        //计算日期
        calculateDate();
        //添加日期请求头
        head.put("Date", gmtString);
        //如果开启了签名，添加签名信息
        if (ftHttpConfig.enableRequestSigning) {
            String akId = ftHttpConfig.akId;
            head.put("Authorization", "DWAY " + akId + ":" + getSignature());
        }
        httpBuilder.setHeadParams(head);
    }
}
