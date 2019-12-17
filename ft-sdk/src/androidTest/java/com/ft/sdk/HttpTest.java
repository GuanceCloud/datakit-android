package com.ft.sdk;

import com.ft.sdk.garble.bean.OP;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.http.FTHttpClient;
import com.ft.sdk.garble.http.FTResponseData;
import com.ft.sdk.garble.http.HttpCallback;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.http.ResponseData;
import com.ft.sdk.garble.manager.SyncDataManager;
import com.ft.sdk.garble.utils.LogUtils;

import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

/**
 * BY huangDianHua
 * DATE:2019-12-16 13:29
 * Description:
 */
public class HttpTest {

    /**
     * 初始化SDK
     */
    private void initSDK(){
        FTSDKConfig config = new FTSDKConfig("http://10.100.64.106:19557/v1/write/metrics",
                true,
                "accid",
                "accsk");
        FTSdk.install(config);
    }

    /**
     * 创建模拟数据
     * @return
     */
    private String createImitateData(){
        RecordData recordData = new RecordData();
        recordData.setOp(OP.LANC.value);
        recordData.setTime(System.currentTimeMillis());
        recordData.setId(1);
        ArrayList<RecordData> recordDatas = new ArrayList<>();
        recordDatas.add(recordData);
        SyncDataManager syncDataManager = new SyncDataManager();
        return syncDataManager.getBodyContent(recordDatas);
    }


    private void imitateRequest(String body){
        FTHttpClient.Builder()
                .setMethod(RequestMethod.POST)
                .setBodyString(body)
                .execute(new HttpCallback<FTResponseData>() {
                    @Override
                    public void onComplete(FTResponseData result) {
                        LogUtils.d(result);
                        assertTrue(result!=null && result.getCode() == HttpURLConnection.HTTP_OK);
                    }
                });
    }

    @Test
    public void testRequest(){
        FTHttpClient.Builder()
                .setUrl("http://baidu.com?query=汉字")
                .setMethod(RequestMethod.POST)
                .execute(new HttpCallback<FTResponseData>() {
                    @Override
                    public void onComplete(FTResponseData result) {
                        LogUtils.d(result);
                        assertTrue(result!=null && result.getHttpCode() == HttpURLConnection.HTTP_OK);
                    }
                });
    }

    @Test
    public void testRequest1(){
        FTHttpClient.Builder()
                .setUrl("http://baidu.com?query=汉字")
                .setMethod(RequestMethod.POST)
                .execute(new HttpCallback<ResponseData>() {
                    @Override
                    public void onComplete(ResponseData result) {
                        LogUtils.d(result);
                        assertTrue(result!=null && result.getHttpCode() == HttpURLConnection.HTTP_OK);
                    }
                });
    }


    @Test
    public void testRequest2(){
        initSDK();
        String body = createImitateData();
        imitateRequest(body);
    }
}
