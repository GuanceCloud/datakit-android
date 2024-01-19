package com.ft.sdk.tests;

import static org.junit.Assert.assertEquals;

import com.ft.sdk.garble.http.FTResponseData;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.RequestMethod;
import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.net.HttpURLConnection;

/**
 * BY huangDianHua
 * DATE:2019-12-16 13:29
 * Description:模拟网络请求测试
 */

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class HttpTest {
    /**
     * 模拟成功返回 json
     */
    private static final String SUCCESS_WITH_JSON_RESPONSE = "{\"code\":200,\"errorCode\":\"\",\"message\":\"\"}";
    /**
     * 模拟无数据返回
     */
    private static final String EMPTY_RESPONSE = "";
    private MockWebServer mMockWebServer;

    @Before
    public void setUp() {
        mMockWebServer = new MockWebServer();
    }

    @After
    public void tearDown() throws Exception {
        mMockWebServer.shutdown();
    }

    /**
     * 模拟网络 200 情况
     *
     * @throws Exception
     */
    @Test
    public void mockedRequest200() throws Exception {
        MockResponse mockResponse = new MockResponse();
        mockResponse.setBody(EMPTY_RESPONSE);
        mockResponse.setResponseCode(HttpURLConnection.HTTP_OK);
        mMockWebServer.enqueue(mockResponse);
        mMockWebServer.play();

        FTResponseData result = HttpBuilder.Builder()
                .setUrl(mMockWebServer.getUrl("/").toString())
                .setMethod(RequestMethod.POST)
                .setBodyString("")
                .executeSync();
        assertEquals(HttpURLConnection.HTTP_OK, result.getCode());
    }

    /**
     * 模拟网络 400 情况
     *
     * @throws Exception
     */
    @Test
    public void mockedRequest400() throws Exception {
        MockResponse mockResponse = new MockResponse();
        mockResponse.setBody(EMPTY_RESPONSE);
        mockResponse.setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
        mMockWebServer.enqueue(mockResponse);
        mMockWebServer.play();

        FTResponseData result = HttpBuilder.Builder()
                .setUrl(mMockWebServer.getUrl("/").toString())
                .setMethod(RequestMethod.POST)
                .setBodyString("")
                .executeSync();
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getCode());
    }

    /**
     * 模拟网络 200 且返回数据为正确的JSON格式
     *
     * @throws Exception
     */
    @Test
    public void mockedRequestJSON() throws Exception {
        MockResponse mockResponse = new MockResponse();
        mockResponse.setBody(SUCCESS_WITH_JSON_RESPONSE);
        mockResponse.setResponseCode(HttpURLConnection.HTTP_OK);
        mMockWebServer.enqueue(mockResponse);
        mMockWebServer.play();

        FTResponseData result = HttpBuilder.Builder()
                .setUrl(mMockWebServer.getUrl("/").toString())
                .setMethod(RequestMethod.POST)
                .setBodyString("")
                .executeSync();
        assertEquals(HttpURLConnection.HTTP_OK, result.getCode());
    }

}
